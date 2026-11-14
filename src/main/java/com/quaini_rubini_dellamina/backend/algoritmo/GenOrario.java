package com.quaini_rubini_dellamina.backend.algoritmo;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;

import java.io.IOException;
import java.util.*;

public class GenOrario {

    protected static final int GIORNI = Slot.GIORNI_SETTIMANA; // 5
    protected static final int ORE = Slot.ORE_GIORNO;          // 9 (6 mattina + pausa + 2 pomeriggio martedì)

    private static final int MAX_SLOT = 32; // 5 giorni x 6 ore + 2 ore pomeriggio martedì
    private static final int MAX_TENTATIVI = 200;

    protected final Set<String> docentiUsati = new HashSet<>();
    protected final Map<String, int[]> oreDocenteGiorno = new HashMap<>();
    protected final Map<String, List<String>> erroriClassi = new LinkedHashMap<>();

    private final Map<String, List<Slot>> slotPreassegnati = new HashMap<>();

    // Risultato di un singolo tentativo di piazzamento
    private record Esito(List<Slot> slots, boolean[][] occupato, List<String> overflow, List<String> nonPiazzate) {
        int piazzati() { return slots.size(); }
    }

    // Prime 6 ore (8-14) tutti i giorni; pausa mai assegnabile; 15-17 solo al martedì
    public static boolean isSlotEsistente(int giorno, int ora) {
        int idxPausa = OrarioLezioni.PAUSA.ordinal();
        if (ora == idxPausa) return false;
        return ora < idxPausa || giorno == Giorni.MARTEDI.ordinal();
    }

    // Ultima ora: 16-17 (indice 8) al martedì, 13-14 (indice 5) negli altri giorni
    public static int ultimaOraGiorno(int giorno) {
        return (giorno == Giorni.MARTEDI.ordinal())
                ? OrarioLezioni.OTTAVA.ordinal()
                : OrarioLezioni.SESTA.ordinal();
    }

    public List<Slot> genOrarioOnClasse(List<Lezione> tutteLeLezioni, String keyClasse) {
        List<Lezione> lezClasse = tutteLeLezioni.stream()
                .filter(l -> l.getClasse().getChiave().equals(keyClasse))
                .toList();
        return generaCasualePerClasse(lezClasse, keyClasse);
    }

    public List<Slot> generaCasuale(List<Lezione> lezioni) {
        String keyClasse = lezioni.isEmpty() ? "?" : lezioni.getFirst().getClasse().getChiave();
        return generaCasualePerClasse(lezioni, keyClasse);
    }

    protected List<Slot> generaCasualePerClasse(List<Lezione> lezioni, String keyClasse) {
        List<Slot> pre = slotPreassegnati.getOrDefault(keyClasse, Collections.emptyList());
        int target = Math.min(pre.size() + espandiInSlotRimanenti(lezioni, pre).size(), MAX_SLOT);

        Set<String> backupUsati = new HashSet<>(docentiUsati);
        Map<String, int[]> backupOre = clonaOre(oreDocenteGiorno);

        // Riprova finché tutti gli slot piazzabili sono assegnati; altrimenti tiene l'ultimo tentativo
        Esito e = null;
        for (int t = 0; t < MAX_TENTATIVI; t++) {
            e = tentaPiazzamento(lezioni, pre, keyClasse, backupUsati, backupOre);
            if (e.piazzati() == target) break;
        }

        List<String> err = new ArrayList<>(e.overflow());
        if (e.piazzati() < target) {
            err.addAll(e.nonPiazzate());
            String msg = "ATTENZIONE: classe " + keyClasse + " piazzate solo " + e.piazzati()
                    + "/" + target + " dopo " + MAX_TENTATIVI + " tentativi";
            System.err.println(msg);
            err.add(0, msg);
        }

        List<Slot> risultato = new ArrayList<>(e.slots());
        for (int g = 0; g < GIORNI; g++)
            for (int o = 0; o < ORE; o++)
                if (isSlotEsistente(g, o) && !e.occupato()[g][o])
                    risultato.add(Slot.vuoto(g, o));

        if (err.isEmpty()) erroriClassi.remove(keyClasse);
        else erroriClassi.put(keyClasse, err);

        return risultato;
    }

    private Esito tentaPiazzamento(List<Lezione> lezioni, List<Slot> pre, String keyClasse,
                                   Set<String> backupUsati, Map<String, int[]> backupOre) {
        docentiUsati.clear();
        docentiUsati.addAll(backupUsati);
        ripristinaOre(backupOre);

        Random rnd = new Random();
        List<Slot> daPiazzare = espandiInSlotRimanenti(lezioni, pre);
        Collections.shuffle(daPiazzare, rnd);
        // Prima gli slot con più docenti in compresenza (i più vincolati)
        daPiazzare.sort(Comparator.comparingInt(
                (Slot s) -> s.getLezione().getDocentiCompresenza().size()).reversed());

        boolean[][] occupato = new boolean[GIORNI][ORE];
        List<Slot> assegnati = new ArrayList<>(pre);
        for (Slot p : pre) occupato[p.indexGiorno()][p.indexOra()] = true;

        List<String> overflow = new ArrayList<>();
        List<String> nonPiazzate = new ArrayList<>();

        int richieste = pre.size() + daPiazzare.size();
        if (richieste > MAX_SLOT) {
            overflow.add("Classe " + keyClasse + ": richieste " + richieste
                    + " ore settimanali, ma gli slot massimi sono " + MAX_SLOT
                    + " (" + (richieste - MAX_SLOT) + " ore escluse).");
        }

        for (Slot slot : daPiazzare) {
            if (assegnati.size() >= MAX_SLOT) break;

            Lezione lez = slot.getLezione();
            List<Docente> docenti = lez.getAllDocenti();
            List<int[]> liberi = getSlotLiberi(occupato, docenti, rnd);

            if (liberi.isEmpty()) {
                nonPiazzate.add("Classe " + keyClasse + ": ora non piazzata di "
                        + lez.getMateria().getNome() + " (" + lez.getNomiDocenti()
                        + ") per conflitto orario o vincoli docenti.");
                continue;
            }

            int g = liberi.getFirst()[0];
            int o = liberi.getFirst()[1];
            slot.assegna(g, o);
            occupato[g][o] = true;
            for (Docente d : docenti) registra(d.getNome(), g, o);
            assegnati.add(slot);
        }
        return new Esito(assegnati, occupato, overflow, nonPiazzate);
    }

    // Tutti gli slot della classe tranne quelli già preassegnati (conteggiati per materia)
    private List<Slot> espandiInSlotRimanenti(List<Lezione> lezioni, List<Slot> preAssegnati) {
        List<Slot> tutti = espandiInSlot(lezioni);
        if (preAssegnati.isEmpty()) return tutti;

        Map<String, Integer> daRimuovere = new HashMap<>();
        for (Slot pre : preAssegnati)
            daRimuovere.merge(chiaveMateria(pre), 1, Integer::sum);

        List<Slot> rimanenti = new ArrayList<>();
        for (Slot s : tutti) {
            String mat = chiaveMateria(s);
            int count = daRimuovere.getOrDefault(mat, 0);
            if (count > 0) daRimuovere.put(mat, count - 1);
            else rimanenti.add(s);
        }
        return rimanenti;
    }

    private static String chiaveMateria(Slot s) {
        return s.getLezione().getMateria().getNome().trim().toUpperCase();
    }

    // Indirizzi in cui TEDESCO e FRANCESE si fanno insieme (sdoppiamento)
    private static boolean haSdoppiamentoLingue(String indirizzo) {
        return "TUR".equalsIgnoreCase(indirizzo) || "AFM".equalsIgnoreCase(indirizzo);
    }

    // Sincronizza le coppie di classi dello stesso indirizzo e anno con lingue separate (es. 4A-TUR Francese / 4B-TUR Tedesco)
    private void sincronizzaLingueTur(Set<Classe> classi, List<Lezione> lessons) {
        slotPreassegnati.clear();
        if (classi == null || lessons == null) return;

        // Chiave "indirizzo + anno": TUR e AFM non vanno mai mescolati
        Map<String, List<Classe>> perIndirizzoAnno = new TreeMap<>();
        for (Classe c : classi)
            if (haSdoppiamentoLingue(c.getIndirizzo()))
                perIndirizzoAnno.computeIfAbsent(c.getIndirizzo().toUpperCase() + c.getAnno(), k -> new ArrayList<>()).add(c);

        Random rnd = new Random();
        for (List<Classe> gruppo : perIndirizzoAnno.values()) {
            if (gruppo.size() < 2) continue;
            gruppo.sort(Comparator.comparing(Classe::getChiave));
            Classe cA = gruppo.get(0);
            Classe cB = gruppo.get(1);

            List<Slot> slotsA = espandiInSlot(lessons.stream().filter(l -> l.getClasse().equals(cA)).toList());
            List<Slot> slotsB = espandiInSlot(lessons.stream().filter(l -> l.getClasse().equals(cB)).toList());

            // Le due classi occupano sempre gli stessi slot insieme: basta una sola griglia
            boolean[][] occupato = new boolean[GIORNI][ORE];
            abbinaSlotParalleli(cA.getChiave(), filtraPerMateria(slotsA, "TEDESCO"),
                    cB.getChiave(), filtraPerMateria(slotsB, "FRANCESE"), occupato, rnd);
            abbinaSlotParalleli(cA.getChiave(), filtraPerMateria(slotsA, "FRANCESE"),
                    cB.getChiave(), filtraPerMateria(slotsB, "TEDESCO"), occupato, rnd);
        }
    }

    private List<Slot> filtraPerMateria(List<Slot> slots, String materiaUpper) {
        return slots.stream()
                .filter(s -> s.getLezione() != null && s.getLezione().getMateria() != null
                        && chiaveMateria(s).equals(materiaUpper))
                .toList();
    }

    private void abbinaSlotParalleli(String keyA, List<Slot> listA, String keyB, List<Slot> listB,
                                     boolean[][] occupato, Random rnd) {
        int coppie = Math.min(listA.size(), listB.size());
        for (int i = 0; i < coppie; i++) {
            Slot sA = listA.get(i);
            Slot sB = listB.get(i);

            List<Docente> docenti = new ArrayList<>(sA.getLezione().getAllDocenti());
            docenti.addAll(sB.getLezione().getAllDocenti());

            List<int[]> liberi = getSlotLiberi(occupato, docenti, rnd);
            if (liberi.isEmpty()) continue;

            int g = liberi.getFirst()[0];
            int o = liberi.getFirst()[1];
            sA.assegna(g, o);
            sB.assegna(g, o);
            occupato[g][o] = true;
            for (Docente d : docenti) registra(d.getNome(), g, o);

            slotPreassegnati.computeIfAbsent(keyA, k -> new ArrayList<>()).add(sA);
            slotPreassegnati.computeIfAbsent(keyB, k -> new ArrayList<>()).add(sB);
        }
    }

    /*
     Espande le lezioni della classe in singoli slot.
     Più docenti sulla stessa materia vengono accoppiati in compresenza.
     Nelle classi TUR e AFM, TEDESCO e FRANCESE vengono accoppiati negli stessi slot (sdoppiamento).
     */
    public List<Slot> espandiInSlot(List<Lezione> lezioni) {
        Map<String, List<Lezione>> perMateria = new LinkedHashMap<>();
        for (Lezione l : lezioni)
            perMateria.computeIfAbsent(l.getMateria().getNome().trim().toUpperCase(), k -> new ArrayList<>()).add(l);

        List<Slot> slots = new ArrayList<>();

        boolean sdoppia = !lezioni.isEmpty()
                && lezioni.getFirst().getClasse() != null
                && haSdoppiamentoLingue(lezioni.getFirst().getClasse().getIndirizzo());
        if (sdoppia && perMateria.containsKey("TEDESCO") && perMateria.containsKey("FRANCESE")) {
            slots.addAll(sdoppiaLingueTur(perMateria.remove("TEDESCO"), perMateria.remove("FRANCESE")));
        }

        for (List<Lezione> gruppo : perMateria.values())
            slots.addAll(espandiGruppoMateria(gruppo));

        return slots;
    }

    private List<Slot> sdoppiaLingueTur(List<Lezione> tedList, List<Lezione> fraList) {
        Lezione ted = tedList.getFirst();
        Lezione fra = fraList.getFirst();
        int maxOre = Math.max(ted.getOreSettimanali(), fra.getOreSettimanali());
        Materia matSdoppiata = new Materia("FRANCESE / TEDESCO");

        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < maxOre; i++) {
            List<Docente> docentiSlot = new ArrayList<>();
            if (i < fra.getOreSettimanali())
                for (Lezione l : fraList) docentiSlot.addAll(l.getAllDocenti());
            if (i < ted.getOreSettimanali())
                for (Lezione l : tedList)
                    for (Docente d : l.getAllDocenti())
                        if (!docentiSlot.contains(d)) docentiSlot.add(d);

            if (docentiSlot.isEmpty()) continue;
            Docente primo = docentiSlot.getFirst();
            List<Docente> altri = docentiSlot.subList(1, docentiSlot.size());
            slots.add(new Slot(altri.isEmpty()
                    ? new Lezione(primo, ted.getClasse(), matSdoppiata, maxOre)
                    : new Lezione(primo, altri, ted.getClasse(), matSdoppiata, maxOre)));
        }
        return slots;
    }

    // Il docente con più ore è il titolare, gli altri vanno in compresenza (un solo docente = nessuna compresenza)
    private List<Slot> espandiGruppoMateria(List<Lezione> gruppo) {
        gruppo.sort((a, b) -> Integer.compare(b.getOreSettimanali(), a.getOreSettimanali()));
        Lezione principale = gruppo.getFirst();

        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < principale.getOreSettimanali(); i++) {
            List<Docente> compresenze = new ArrayList<>();
            for (Lezione comp : gruppo.subList(1, gruppo.size())) {
                if (i < comp.getOreSettimanali()
                        && !principale.getDocente().equals(comp.getDocente())
                        && !compresenze.contains(comp.getDocente())) {
                    compresenze.add(comp.getDocente());
                }
            }
            slots.add(new Slot(compresenze.isEmpty()
                    ? principale
                    : new Lezione(principale.getDocente(), compresenze, principale.getClasse(),
                    principale.getMateria(), principale.getOreSettimanali())));
        }
        return slots;
    }

    protected List<int[]> getSlotLiberi(boolean[][] occupato, String nomeDocente, Random rnd) {
        return getSlotLiberi(occupato, List.of(new Docente(nomeDocente)), rnd);
    }

    protected List<int[]> getSlotLiberi(boolean[][] occupato, List<Docente> docenti, Random rnd) {
        List<int[]> liberi = new ArrayList<>();
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                if (!isSlotEsistente(g, o) || occupato[g][o]) continue;
                final int giorno = g, ora = o;
                if (docenti.stream().allMatch(d -> isSlotValido(d.getNome(), giorno, ora)))
                    liberi.add(new int[]{g, o});
            }
        }
        Collections.shuffle(liberi, rnd);
        ordinaSlotLiberi(liberi, docenti);
        return liberi;
    }

    protected void ordinaSlotLiberi(List<int[]> liberi, String nomeDocente) {
    }

    protected void ordinaSlotLiberi(List<int[]> liberi, List<Docente> docenti) {
        if (docenti != null && !docenti.isEmpty())
            ordinaSlotLiberi(liberi, docenti.getFirst().getNome());
    }

    public Set<String> getDocentiUsati() {
        return Collections.unmodifiableSet(docentiUsati);
    }

    public Map<String, List<String>> getErroriClassi() {
        return Collections.unmodifiableMap(erroriClassi);
    }

    // Il docente non deve essere occupato in un'altra classe alla stessa ora
    protected boolean isSlotValido(String nomeDocente, int giorno, int ora) {
        return !docentiUsati.contains(chiaveDocente(nomeDocente, giorno, ora));
    }

    protected void registra(String nomeDocente, int giorno, int ora) {
        docentiUsati.add(chiaveDocente(nomeDocente, giorno, ora));
        oreDocenteGiorno.computeIfAbsent(nomeDocente, k -> new int[GIORNI])[giorno]++;
    }

    protected String chiaveDocente(String nomeDocente, int giorno, int ora) {
        return nomeDocente + "_" + giorno + "_" + ora;
    }

    protected Map<String, int[]> clonaOre(Map<String, int[]> source) {
        Map<String, int[]> copy = new HashMap<>();
        source.forEach((k, v) -> copy.put(k, v.clone()));
        return copy;
    }

    protected void ripristinaOre(Map<String, int[]> backup) {
        oreDocenteGiorno.clear();
        backup.forEach((k, v) -> oreDocenteGiorno.put(k, v.clone()));
    }

    // Genera l'orario di tutte le classi (e deduce quello dei prof)
    public Istituto generaIstituto(ReadCsv r, List<Lezione> lessons, String cartella) throws IOException, CsvValidationException {
        erroriClassi.clear();
        Set<Classe> classi = r.getAllClassi();
        Istituto school = new Istituto(classi, r.getAllDocenti());

        // 1. Pre-assegna le ore incrociate TEDESCO <-> FRANCESE tra classi TUR parallele
        sincronizzaLingueTur(classi, lessons);

        // 2. Prima le classi con più compresenze (più vincolate)
        Map<String, Long> compresenze = new HashMap<>();
        for (Classe c : classi) {
            long n = espandiInSlot(lessons.stream().filter(l -> l.getClasse().equals(c)).toList()).stream()
                    .filter(s -> s.getLezione().hasCompresenza())
                    .count();
            compresenze.put(c.getChiave(), n);
        }

        List<Classe> ordinate = classi.stream()
                .sorted(Comparator
                        .comparingLong((Classe c) -> compresenze.get(c.getChiave())).reversed()
                        .thenComparing(Classe::getChiave))
                .toList();

        for (Classe c : ordinate)
            school.setHforClasse(c.getChiave(), new ArrayList<>(genOrarioOnClasse(lessons, c.getChiave())));

        school.deduciOrariProf();
        return school;
    }
}
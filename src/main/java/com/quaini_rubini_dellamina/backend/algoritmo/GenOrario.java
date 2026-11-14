package com.quaini_rubini_dellamina.backend.algoritmo;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;

import java.io.IOException;
import java.util.*;

public class GenOrario {

    protected static final int GIORNI  = Slot.GIORNI_SETTIMANA; // 5
    protected static final int ORE     = Slot.ORE_GIORNO;       // 6

    protected final Set<String> docentiUsati = new HashSet<>(); // Set (Lista univoca) dei docenti occupati
    protected final Map<String, int[]> oreDocenteGiorno = new HashMap<>(); // ore per docente e giorno
    protected final Map<String, List<String>> erroriClassi = new LinkedHashMap<>();

    public List<Slot> genOrarioOnClasse(List<Lezione> tutteLeLezioni, String keyClasse) {
        List<Lezione> lezClasse = new ArrayList<>();
        for (Lezione l : tutteLeLezioni) {
            if (l.getClasse().getChiave().equals(keyClasse)) {
                lezClasse.add(l);
            }
        }
        return generaCasualePerClasse(lezClasse, keyClasse);
    }

    public List<Slot> generaCasuale(List<Lezione> lezioni) {
        String keyClasse = lezioni.isEmpty() ? "?" : lezioni.getFirst().getClasse().getChiave();
        return generaCasualePerClasse(lezioni, keyClasse);
    }

    protected List<Slot> generaCasualePerClasse(List<Lezione> lezioni, String keyClasse) {
        final int MAX_SLOT = GIORNI * ORE; // 30
        final int MAX_TENTATIVI = 200;

        Set<String> docentiUsatiBackup = new HashSet<>(docentiUsati);
        Map<String, int[]> oreBackup = clonaOre(oreDocenteGiorno);

        List<Slot> templateSlot = espandiInSlot(lezioni);
        int targetPiazzabili = Math.min(templateSlot.size(), MAX_SLOT);

        for (int tentativo = 0; tentativo < MAX_TENTATIVI; tentativo++) {
            docentiUsati.clear();
            docentiUsati.addAll(docentiUsatiBackup);
            ripristinaOre(oreBackup);

            Random rnd = new Random();
            List<Slot> daPiazzare = espandiInSlot(lezioni);
            Collections.shuffle(daPiazzare, rnd);
            // Piazza prima gli slot in compresenza (richiedono 2 docenti liberi contemporaneamente)
            daPiazzare.sort((s1, s2) -> Boolean.compare(
                    s2.getLezione().hasCompresenza(),
                    s1.getLezione().hasCompresenza()
            ));

            boolean[][] occupato = new boolean[GIORNI][ORE];
            List<Slot> assegnati = new ArrayList<>();

            for (Slot slot : daPiazzare) {
                if (assegnati.size() >= MAX_SLOT)
                    break;

                Lezione lez = slot.getLezione();
                String doc1 = lez.getDocente().getNome();
                String doc2 = lez.hasCompresenza() ? lez.getDocenteCompresenza().getNome() : null;
                List<int[]> liberi = getSlotLiberi(occupato, doc1, doc2, rnd);

                if (liberi.isEmpty())
                    continue;

                int[] scelta = liberi.getFirst();
                int giorno = scelta[0];
                int ora    = scelta[1];

                slot.assegna(giorno, ora);
                occupato[giorno][ora] = true;
                registra(doc1, giorno, ora);
                if (doc2 != null) {
                    registra(doc2, giorno, ora);
                }
                assegnati.add(slot);
            }

            if (assegnati.size() == targetPiazzabili) {
                List<String> err = new ArrayList<>();
                if (daPiazzare.size() > MAX_SLOT) {
                    err.add("Classe " + keyClasse + ": richieste " + daPiazzare.size()
                            + " ore settimanali, ma gli slot massimi sono " + MAX_SLOT
                            + " (" + (daPiazzare.size() - MAX_SLOT) + " ore escluse).");
                }
                for (int g = 0; g < GIORNI; g++) {
                    for (int o = 0; o < ORE; o++) {
                        if (!occupato[g][o]) {
                            assegnati.add(Slot.vuoto(g, o));
                        }
                    }
                }
                if (!err.isEmpty()) {
                    erroriClassi.put(keyClasse, err);
                } else {
                    erroriClassi.remove(keyClasse);
                }
                return assegnati;
            }
        }

        // Fallback: ultimo tentativo, registra gli errori effettivi
        docentiUsati.clear();
        docentiUsati.addAll(docentiUsatiBackup);
        ripristinaOre(oreBackup);

        Random rnd = new Random();
        List<Slot> daPiazzare = espandiInSlot(lezioni);
        Collections.shuffle(daPiazzare, rnd);
        daPiazzare.sort((s1, s2) -> Boolean.compare(
                s2.getLezione().hasCompresenza(),
                s1.getLezione().hasCompresenza()
        ));

        boolean[][] occupato = new boolean[GIORNI][ORE];
        List<Slot> assegnati = new ArrayList<>();
        List<String> err = new ArrayList<>();

        if (daPiazzare.size() > MAX_SLOT) {
            err.add("Classe " + keyClasse + ": richieste " + daPiazzare.size()
                    + " ore settimanali, ma gli slot massimi sono " + MAX_SLOT
                    + " (" + (daPiazzare.size() - MAX_SLOT) + " ore escluse).");
        }

        for (Slot slot : daPiazzare) {
            if (assegnati.size() >= MAX_SLOT) break;

            Lezione lez = slot.getLezione();
            String doc1 = lez.getDocente().getNome();
            String doc2 = lez.hasCompresenza() ? lez.getDocenteCompresenza().getNome() : null;
            List<int[]> liberi = getSlotLiberi(occupato, doc1, doc2, rnd);

            if (liberi.isEmpty()) {
                err.add("Classe " + keyClasse + ": ora non piazzata di "
                        + lez.getMateria().getNome() + " (" + lez.getNomiDocenti()
                        + ") per conflitto orario o vincoli docenti.");
                continue;
            }

            int[] scelta = liberi.getFirst();
            slot.assegna(scelta[0], scelta[1]);
            occupato[scelta[0]][scelta[1]] = true;
            registra(doc1, scelta[0], scelta[1]);
            if (doc2 != null) {
                registra(doc2, scelta[0], scelta[1]);
            }
            assegnati.add(slot);
        }

        int piazzati = assegnati.size();
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                if (!occupato[g][o]) {
                    assegnati.add(Slot.vuoto(g, o));
                }
            }
        }

        if (piazzati < targetPiazzabili) {
            String msgAttenzione = "ATTENZIONE: classe " + keyClasse
                    + " piazzate solo " + piazzati + "/" + targetPiazzabili
                    + " dopo " + MAX_TENTATIVI + " tentativi";
            System.err.println(msgAttenzione);
            err.add(0, msgAttenzione);
        }

        if (!err.isEmpty()) {
            erroriClassi.put(keyClasse, err);
        } else {
            erroriClassi.remove(keyClasse);
        }

        return assegnati;
    }

    /*
     Espande le lezioni della classe in singoli slot.
     Se due docenti insegnano la stessa materia nella stessa classe, vengono accoppiati in compresenza.
     */
    public List<Slot> espandiInSlot(List<Lezione> lezioni) {
        List<Slot> slots = new ArrayList<>();
        Map<String, List<Lezione>> perMateria = new LinkedHashMap<>();

        for (Lezione l : lezioni) {
            String keyMateria = l.getMateria().getNome().trim().toUpperCase();
            perMateria.computeIfAbsent(keyMateria, k -> new ArrayList<>()).add(l);
        }

        for (List<Lezione> gruppo : perMateria.values()) {
            if (gruppo.size() == 1) {
                Lezione l = gruppo.getFirst();
                for (int i = 0; i < l.getOreSettimanali(); i++) {
                    slots.add(new Slot(l));
                }
            } else {
                // Ordina per ore settimanali decrescenti: il docente con più ore è il titolare, l'altro va in compresenza
                gruppo.sort((a, b) -> Integer.compare(b.getOreSettimanali(), a.getOreSettimanali()));
                Lezione principale = gruppo.get(0);
                Lezione compresenza = gruppo.get(1);

                for (int i = 0; i < principale.getOreSettimanali(); i++) {
                    if (i < compresenza.getOreSettimanali()
                            && !principale.getDocente().equals(compresenza.getDocente())) {
                        Lezione lezComp = new Lezione(
                                principale.getDocente(),
                                compresenza.getDocente(),
                                principale.getClasse(),
                                principale.getMateria(),
                                principale.getOreSettimanali()
                        );
                        slots.add(new Slot(lezComp));
                    } else {
                        slots.add(new Slot(principale));
                    }
                }

                // Eventuali ulteriori docenti oltre il secondo sulla stessa materia
                for (int k = 2; k < gruppo.size(); k++) {
                    Lezione extra = gruppo.get(k);
                    for (int i = 0; i < extra.getOreSettimanali(); i++) {
                        slots.add(new Slot(extra));
                    }
                }
            }
        }
        return slots;
    }

    protected List<int[]> getSlotLiberi(boolean[][] occupato, String nomeDocente, Random rnd) {
        return getSlotLiberi(occupato, nomeDocente, null, rnd);
    }

    protected List<int[]> getSlotLiberi(boolean[][] occupato, String doc1, String doc2, Random rnd) {
        List<int[]> liberi = new ArrayList<>();
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                boolean ok1 = isSlotValido(doc1, g, o);
                boolean ok2 = (doc2 == null) || isSlotValido(doc2, g, o);
                if (!occupato[g][o] && ok1 && ok2) {
                    liberi.add(new int[]{g, o});
                }
            }
        }
        Collections.shuffle(liberi, rnd);
        ordinaSlotLiberi(liberi, doc1, doc2);
        return liberi;
    }

    protected void ordinaSlotLiberi(List<int[]> liberi, String nomeDocente) {
    }

    protected void ordinaSlotLiberi(List<int[]> liberi, String doc1, String doc2) {
        ordinaSlotLiberi(liberi, doc1);
    }

    public Set<String> getDocentiUsati() {
        return Collections.unmodifiableSet(docentiUsati);
    }

    public Map<String, List<String>> getErroriClassi() {
        return Collections.unmodifiableMap(erroriClassi);
    }

    // Controlla che il docente non sia occupato in un'altra classe
    protected boolean isSlotValido(String nomeDocente, int giorno, int ora) {
        return !docentiUsati.contains(chiaveDocente(nomeDocente, giorno, ora));
    }

    // Registra un'ora assegnata
    protected void registra(String nomeDocente, int giorno, int ora) {
        docentiUsati.add(chiaveDocente(nomeDocente, giorno, ora));
        oreDocenteGiorno.computeIfAbsent(nomeDocente, k -> new int[GIORNI])[giorno]++;
    }

    protected String chiaveDocente(String nomeDocente, int giorno, int ora) {
        return nomeDocente + "_" + giorno + "_" + ora;
    }

    protected Map<String, int[]> clonaOre(Map<String, int[]> source) {
        Map<String, int[]> copy = new HashMap<>();
        for (Map.Entry<String, int[]> e : source.entrySet()) {
            copy.put(e.getKey(), e.getValue().clone());
        }
        return copy;
    }

    protected void ripristinaOre(Map<String, int[]> backup) {
        oreDocenteGiorno.clear();
        for (Map.Entry<String, int[]> e : backup.entrySet()) {
            oreDocenteGiorno.put(e.getKey(), e.getValue().clone());
        }
    }

    // Genera l'orario di tutte le classi dell'Istituto (e deduce anche l'orario dei prof)
    public Istituto generaIstituto(ReadCsv r, List<Lezione> lessons, String cartella) throws IOException, CsvValidationException {
        erroriClassi.clear();
        Istituto school = new Istituto(r.getAllClassi(), r.getAllDocenti());

        for (Classe s : r.getAllClassi()) {
            ArrayList<Slot> slots = new ArrayList<>(genOrarioOnClasse(lessons, s.getChiave()));
            school.setHforClasse(s.getChiave(), slots);
        }

        school.deduciOrariProf();

        return school;
    }
}

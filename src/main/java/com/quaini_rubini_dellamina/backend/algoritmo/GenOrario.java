package com.quaini_rubini_dellamina.backend.algoritmo;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.BuildCsv;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;
import com.quaini_rubini_dellamina.backend.io.TabellaOrsonPDF;

import java.io.IOException;
import java.util.*;


public class GenOrario {

    protected static final int GIORNI  = Slot.GIORNI_SETTIMANA; // 5
    protected static final int ORE     = Slot.ORE_GIORNO;       // 6

    protected final Set<String> docentiUsati = new HashSet<>(); // Set (Lista univoca) dei docente occupati
    protected final Map<String, int[]> oreDocenteGiorno = new HashMap<>(); // ore per docente e giorno


    public List<Slot> genOrarioOnClasse(List<Lezione> tutteLeLezioni, String keyClasse) {
        List<Lezione> lezClasse = new ArrayList<>();
        for (Lezione l : tutteLeLezioni) {
            if (l.getClasse().getChiave().equals(keyClasse)) {
                lezClasse.add(l);
            }//considero solo le lezioni con la keyClasse come classe
        }
        return generaCasuale(lezClasse);
    }

    public List<Slot> generaCasuale(List<Lezione> lezioni) {
        final int MAX_SLOT = GIORNI * ORE; // 30
        final int MAX_TENTATIVI = 200;

        Set<String> docentiUsatiBackup = new HashSet<>(docentiUsati);
        Map<String, int[]> oreBackup = clonaOre(oreDocenteGiorno);

        for (int tentativo = 0; tentativo < MAX_TENTATIVI; tentativo++) {
            // Ripristina lo stato dei docenti ad ogni tentativo
            docentiUsati.clear();
            docentiUsati.addAll(docentiUsatiBackup);
            ripristinaOre(oreBackup);

            Random rnd = new Random();
            List<Slot> daPiazzare = espandiInSlot(lezioni);
            Collections.shuffle(daPiazzare, rnd);

            boolean[][] occupato = new boolean[GIORNI][ORE];
            List<Slot> assegnati = new ArrayList<>();

            for (Slot slot : daPiazzare) {
                if (assegnati.size() >= MAX_SLOT)
                    break; // 30 raggiunti

                String nomeDocente = slot.getLezione().getDocente().getNome();
                List<int[]> liberi = getSlotLiberi(occupato, nomeDocente, rnd);

                if (liberi.isEmpty())
                    continue; // conflitto docente, provo la prossima lezione

                int[] scelta = liberi.getFirst();
                int giorno = scelta[0];
                int ora    = scelta[1];

                slot.assegna(giorno, ora);
                occupato[giorno][ora] = true;
                registra(nomeDocente, giorno, ora);
                assegnati.add(slot);
            }

            if (assegnati.size() == MAX_SLOT) {
                return assegnati; // 30/30 raggiunto
            }
        }

        // Fallback: ultimo tentativo, restituisce ciò che riesce + buche
        docentiUsati.clear();
        docentiUsati.addAll(docentiUsatiBackup);
        ripristinaOre(oreBackup);

        Random rnd = new Random();
        List<Slot> daPiazzare = espandiInSlot(lezioni);
        Collections.shuffle(daPiazzare, rnd);
        boolean[][] occupato = new boolean[GIORNI][ORE];
        List<Slot> assegnati = new ArrayList<>();

        for (Slot slot : daPiazzare) {
            if (assegnati.size() >= MAX_SLOT) break;
            String nomeDocente = slot.getLezione().getDocente().getNome();
            List<int[]> liberi = getSlotLiberi(occupato, nomeDocente, rnd);
            if (liberi.isEmpty())
                continue;
            int[] scelta = liberi.getFirst();
            slot.assegna(scelta[0], scelta[1]);
            occupato[scelta[0]][scelta[1]] = true;
            registra(nomeDocente, scelta[0], scelta[1]);
            assegnati.add(slot);
        }

        int piazzati = assegnati.size();
        // Riempie eventuali buche residue
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                if (!occupato[g][o]) {
                    assegnati.add(Slot.vuoto(g, o));
                }
            }
        }

        if (piazzati < MAX_SLOT) {
            String classe = lezioni.isEmpty() ? "?" : lezioni.getFirst().getClasse().getChiave();
            System.err.println("ATTENZIONE: classe " + classe
                    + " piazzate solo " + piazzati + "/" + MAX_SLOT
                    + " dopo " + 200 + " tentativi");
        }

        return assegnati;
    }

    public List<Slot> espandiInSlot(List<Lezione> lezioni) {
        List<Slot> slots = new ArrayList<>();
        for (Lezione l : lezioni) {
            for (int i = 0; i < l.getOreSettimanali(); i++) {
                slots.add(new Slot(l));
            }
        }
        return slots;
    }

    protected List<int[]> getSlotLiberi(boolean[][] occupato, String nomeDocente, Random rnd) {
        List<int[]> liberi = new ArrayList<>();
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                if (!occupato[g][o] && isSlotValido(nomeDocente, g, o))
                    liberi.add(new int[]{g, o});
            }
        }
        Collections.shuffle(liberi, rnd); // ordine casuale base
        ordinaSlotLiberi(liberi, nomeDocente);
        return liberi;
    }

    protected void ordinaSlotLiberi(List<int[]> liberi, String nomeDocente) {

    }

    public Set<String> getDocentiUsati() {
        return Collections.unmodifiableSet(docentiUsati);
    }

    //controlla che il docente non sia accupato in un altra classe
    protected boolean isSlotValido(String nomeDocente, int giorno, int ora) {
        return !docentiUsati.contains(chiaveDocente(nomeDocente, giorno, ora));
    }

    //  Registra un'ora assegnata
    protected void registra(String nomeDocente, int giorno, int ora) {
        docentiUsati.add(chiaveDocente(nomeDocente, giorno, ora));
        oreDocenteGiorno.computeIfAbsent(nomeDocente, k -> new int[GIORNI])[giorno]++;
    }

    protected String chiaveDocente(String nomeDocente, int giorno, int ora) {
        return nomeDocente + "_" + giorno + "_" + ora;
    }// per evitare che un prof possa essere contemporaneamente in due classi, key univoca

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

    //  Genera l'orario di tutte le classi dell'Istituto (e deduce anche l'orario dei prof)
    public Istituto generaIstituto(ReadCsv r, List<Lezione> lessons, String cartella) throws IOException, CsvValidationException {
        Istituto school = new Istituto(r.getAllClassi(), r.getAllDocenti());

        for (Classe s : r.getAllClassi()) {
            ArrayList<Slot> slots = new ArrayList<>(genOrarioOnClasse(lessons, s.getChiave()));
            school.setHforClasse(s.getChiave(), slots);
        }

        school.deduciOrariProf();

        return school;
    }

}
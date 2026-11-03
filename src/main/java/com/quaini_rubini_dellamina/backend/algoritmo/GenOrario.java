package com.quaini_rubini_dellamina.backend.algoritmo;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.BuildCsv;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;
import com.quaini_rubini_dellamina.backend.io.TabellaOrsonPDF;

import java.io.IOException;
import java.util.*;


public class GenOrario {

    private static final int GIORNI  = Slot.GIORNI_SETTIMANA; // 5
    private static final int ORE     = Slot.ORE_GIORNO;       // 6

    private final Set<String> docentiUsati = new HashSet<>(); // Set (Lista univoca) dei docente occupati


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
        Random rnd = new Random();
        List<Slot> daPiazzare = espandiInSlot(lezioni);
        // Mescola casualmente per distribuire le lezioni in modo vario
        Collections.shuffle(daPiazzare,rnd);
        // Griglia di boolean, valore true se slot già usato per questa classe
        boolean[][] occupato = new boolean[GIORNI][ORE];

        List<Slot> assegnati = new ArrayList<>();

        for (Slot slot : daPiazzare) {
            String nomeDocente = slot.getLezione().getDocente().getNome();
            // Costruisce la lista degli slot liberi in ordine casuale
            List<int[]> liberi = getSlotLiberi(occupato, docentiUsati, nomeDocente, rnd);

            if (liberi.isEmpty()) {
                System.err.println("Slot liberi terminati");
                return assegnati;
            }

            int[] scelta = liberi.getFirst();
            int giorno = scelta[0];
            int ora    = scelta[1];

            slot.assegna(giorno, ora);
            occupato[giorno][ora] = true;
            docentiUsati.add(chiaveDocente(nomeDocente, giorno, ora));
            assegnati.add(slot);
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

    private List<int[]> getSlotLiberi(boolean[][] occupato, Set<String> docentiUsati, String nomeDocente, Random rnd) {
        List<int[]> liberi = new ArrayList<>();
        for (int g = 0; g < GIORNI; g++) {
            for (int o = 0; o < ORE; o++) {
                if (!occupato[g][o] && !docentiUsati.contains(chiaveDocente(nomeDocente, g, o)))
                    liberi.add(new int[]{g, o});
            }
        }
        Collections.shuffle(liberi, rnd);//ordine causuale
        return liberi;
    }

    private String chiaveDocente(String nomeDocente, int giorno, int ora) {
        return nomeDocente + "_" + giorno + "_" + ora;
    }// per evitare che un prof possa essere contemporaneamente in due classi, key univoca

//test
    public static void main( String[] args) throws CsvValidationException, IOException {
        ReadCsv r= new ReadCsv();
        List<Lezione> lessons;
        lessons = r.leggi();
        ArrayList<Slot> slots;
        Istituto school = new Istituto(r.getAllClassi(),r.getAllDocenti());
        GenOrario generatore = new GenOrario();

        for(Classe s : r.getAllClassi()){
            slots = (ArrayList<Slot>) generatore.genOrarioOnClasse(lessons,s.getChiave());
            school.setHforClasse(s.getChiave(),slots);
            BuildCsv.scriviOrario(slots,"/Users/francy_behemmy/Developer/PjOrarioScolastico/esOut",true);
        }

        school.deduciOrariProf();
        Map<String, ArrayList<Slot> > doc = school.getDocenti_tabelleh();
        for( String key : doc.keySet() ){
            BuildCsv.scriviOrario(doc.get(key),"/Users/francy_behemmy/Developer/PjOrarioScolastico/esOut",false);
        }
//        Map<String, ArrayList<Slot>> mappa = school.getClassi_tabelleh();
//        for (String chiave : mappa.keySet())
//            System.out.println("key: " + chiave + " value: " + mappa.get(chiave));

    }
}


package com.quaini_rubini_dellamina.backend.algoritmo;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;
import com.quaini_rubini_dellamina.backend.io.ReadVincoliJSON;

import java.io.IOException;
import java.util.*;

public class GenOrarioVincoli extends GenOrario {

    private final ConfiguraVincoli vincoli;
    private final Map<String, Preferenza> preferenzeDocenti = new HashMap<>();

    public GenOrarioVincoli(ConfiguraVincoli cfg) {
        this.vincoli = cfg;
        if (cfg != null && cfg.preferenze() != null) {
            for (Preferenza p : cfg.preferenze()) {
                if (p.docente() != null) {
                    preferenzeDocenti.put(p.docente().trim().toLowerCase(), p);
                }
            }
        }
    }

    public GenOrarioVincoli() throws IOException {
        this(ReadVincoliJSON.leggi());
    }

    public ConfiguraVincoli getVincoli() {
        return vincoli;
    }

    public Preferenza getPreferenzaDocente(String nomeDocente) {
        if (nomeDocente == null) return null;
        return preferenzeDocenti.get(nomeDocente.trim().toLowerCase());
    }


    // VINCOLI OBBLIGATORI, se violati, lo slot viene scartato

    @Override
    protected boolean isSlotValido(String nomeDocente, int giorno, int ora) {
        //  Contemporaneità docente (gestita nella classe padre: docente non in due classi contemporaneamente)
        if (!super.isSlotValido(nomeDocente, giorno, ora)) {
            return false;
        }

        Preferenza pref = getPreferenzaDocente(nomeDocente);
        if (pref == null) {
            return true;
        }

        //  Giorno libero obbligatorio
        int giornoLiberoIdx = indiceGiorno(pref.giornoLibero());
        if (giornoLiberoIdx >= 0 && giornoLiberoIdx == giorno) {
            return false;
        }

        //  Obbligo prima ora libera
        if (pref.noPrimaH() && ora == 0) {
            return false;
        }

        //  Obbligo ultima ora libera
        if (pref.noUltimaH() && ora == (ORE - 1)) {
            return false;
        }

        //  Obbligo limite orario giornaliero
        if (pref.limiteH() > 0) {
            int[] oreGiorno = oreDocenteGiorno.get(nomeDocente);
            if (oreGiorno != null && oreGiorno[giorno] >= pref.limiteH()) {
                return false;
            }
        }

        return true;
    }

    // Vincolo non obbligatorio
    // Riduzione delle ore buche

    @Override
    protected void ordinaSlotLiberi(List<int[]> liberi, String nomeDocente) {
        if (liberi == null || liberi.size() <= 1) return;
        // Ordina prioritariamente gli slot a minor incremento di ore buche
        liberi.sort(Comparator.comparingInt(slot -> calcolaPenalitaSoft(nomeDocente, slot[0], slot[1])));
    }

    /*
     Calcola la penalità associata all'assegnazione dello slot (giorno, ora) per nomeDocente.
      Penalizza l'introduzione di ore buche rispetto alle ore che il docente ha già in quel giorno.
     */
    public int calcolaPenalitaSoft(String nomeDocente, int giorno, int ora) {
        Preferenza pref = getPreferenzaDocente(nomeDocente);
        if (pref == null || !pref.riduciOreBuche()) {
            return 0;
        }

        List<Integer> oreGiorno = getOreDocenteNelGiorno(nomeDocente, giorno);
        if (oreGiorno.isEmpty()) {
            return 0; // Prima lezione del giorno: nessuna ora buca
        }

        int buchePrima = calcolaOreBucheGiorno(oreGiorno);

        List<Integer> oreIpotetiche = new ArrayList<>(oreGiorno);
        oreIpotetiche.add(ora);
        int bucheDopo = calcolaOreBucheGiorno(oreIpotetiche);

        int deltaBuche = bucheDopo - buchePrima;
        return (deltaBuche > 0) ? (deltaBuche * 20) : 0;
    }

    //Ritorna la lista ordinata delle ore [0..5] in cui il docente ha già lezione nel giorno indicato.

    public List<Integer> getOreDocenteNelGiorno(String nomeDocente, int giorno) {
        List<Integer> ore = new ArrayList<>();
        if (nomeDocente == null) return ore;
        for (int h = 0; h < ORE; h++) {
            if (docentiUsati.contains(chiaveDocente(nomeDocente, giorno, h))) {
                ore.add(h);
            }
        }
        Collections.sort(ore);
        return ore;
    }

    /*
      Calcola il numero di ore buche in una giornata:
      Ore buche = (maxOra - minOra + 1) - numeroLezioni.
     */
    public static int calcolaOreBucheGiorno(List<Integer> ore) {
        if (ore == null || ore.size() <= 1) return 0;
        int min = Collections.min(ore);
        int max = Collections.max(ore);
        int span = max - min + 1;
        return span - ore.size();
    }

    public static int indiceGiorno(String nomeGiorno) {
        if (nomeGiorno == null || nomeGiorno.isBlank()) return -1;
        for (Giorni g : Giorni.values()) {
            if (g.getNome().equalsIgnoreCase(nomeGiorno.trim())) {
                return g.ordinal();
            }
        }
        return -1;
    }

    public static Istituto generaConVincoli(ReadCsv r, List<Lezione> lezioni, String cartella) throws IOException, CsvValidationException {
        ConfiguraVincoli vincoli = ReadVincoliJSON.leggi();
        return new GenOrarioVincoli(vincoli).generaIstituto(r, lezioni, cartella);
    }


    // report e verifica delle preferenze


    public static class RisultatoVerificaDocente {
        public final String docente;
        public final int totOre;
        public final int[] oreGiorno = new int[GIORNI];
        public final int oreBucheTotali;
        public boolean violazioneGiornoLibero = false;
        public boolean violazioneNoPrimaH = false;
        public boolean violazioneNoUltimaH = false;
        public boolean violazioneLimiteH = false;
        public final List<String> motivazioniNonSoddisfatte = new ArrayList<>();

        public RisultatoVerificaDocente(String docente, int totOre, int oreBucheTotali) {
            this.docente = docente;
            this.totOre = totOre;
            this.oreBucheTotali = oreBucheTotali;
        }

        public boolean hardRispettati() {
            return !violazioneGiornoLibero && !violazioneNoPrimaH && !violazioneNoUltimaH && !violazioneLimiteH;
        }
    }

    public static Map<String, RisultatoVerificaDocente> analizzaOrario(ConfiguraVincoli vincoli, Istituto school) {
        Map<String, RisultatoVerificaDocente> analisi = new LinkedHashMap<>();
        Map<String, ArrayList<Slot>> docMap = school.getDocenti_tabelleh();

        if (vincoli == null || vincoli.preferenze() == null) {
            return analisi;
        }

        for (Preferenza p : vincoli.preferenze()) {
            String nomeProf = p.docente();
            ArrayList<Slot> oreProf = docMap.get(nomeProf);
            int totOre = (oreProf != null) ? oreProf.size() : 0;

            Map<Integer, List<Integer>> orePerGiornoMap = new HashMap<>();
            for (int g = 0; g < GIORNI; g++) orePerGiornoMap.put(g, new ArrayList<>());

            if (oreProf != null) {
                for (Slot s : oreProf) {
                    if (s == null || s.isVuoto()) continue;
                    orePerGiornoMap.get(s.indexGiorno()).add(s.indexOra());
                }
            }

            int bucheTot = 0;
            int[] oreGiornoArr = new int[GIORNI];

            for (int g = 0; g < GIORNI; g++) {
                List<Integer> listOre = orePerGiornoMap.get(g);
                Collections.sort(listOre);
                oreGiornoArr[g] = listOre.size();
                bucheTot += calcolaOreBucheGiorno(listOre);
            }

            RisultatoVerificaDocente res = new RisultatoVerificaDocente(nomeProf, totOre, bucheTot);
            System.arraycopy(oreGiornoArr, 0, res.oreGiorno, 0, GIORNI);

            // Verifica vincoli obbligatori
            if (p.giornoLibero() != null) {
                int gLibero = indiceGiorno(p.giornoLibero());
                if (gLibero >= 0 && res.oreGiorno[gLibero] > 0) {
                    res.violazioneGiornoLibero = true;
                }
            }
            if (p.limiteH() > 0) {
                for (int oreG : res.oreGiorno) {
                    if (oreG > p.limiteH()) res.violazioneLimiteH = true;
                }
            }
            if (oreProf != null) {
                for (Slot s : oreProf) {
                    if (s == null || s.isVuoto()) continue;
                    if (p.noPrimaH() && s.indexOra() == 0) res.violazioneNoPrimaH = true;
                    if (p.noUltimaH() && s.indexOra() == (ORE - 1)) res.violazioneNoUltimaH = true;
                }
            }

            // Verifica vincolo non obbligatorio
            if (p.riduciOreBuche() && bucheTot > 0) {
                res.motivazioniNonSoddisfatte.add("Preferenza 'riduzione-ore-buche': riscontrate " + bucheTot
                        + " ore buche complessive nella settimana. Motivazione: le lezioni assegnate a classi differenti"
                        + " non hanno trovato slot contigui liberi senza generare contemporaneità docente o violare vincoli obbligatori.");
            }

            analisi.put(nomeProf, res);
        }

        return analisi;
    }
}

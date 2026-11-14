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
        if (!super.isSlotValido(nomeDocente, giorno, ora)) {
            return false;
        }

        Preferenza pref = getPreferenzaDocente(nomeDocente);
        if (pref == null) {
            return true;
        }

        // Giorno libero obbligatorio
        int giornoLiberoIdx = indiceGiorno(pref.giornoLibero());
        if (giornoLiberoIdx >= 0 && giornoLiberoIdx == giorno) {
            return false;
        }

        // Obbligo prima ora libera
        if (pref.noPrimaH() && ora == 0) {
            return false;
        }

        // Obbligo ultima ora libera (16-17 al martedì, 13-14 negli altri giorni)
        if (pref.noUltimaH() && ora == ultimaOraGiorno(giorno)) {
            return false;
        }

        // Obbligo limite orario giornaliero
        if (pref.limiteH() > 0) {
            int[] oreGiorno = oreDocenteGiorno.get(nomeDocente);
            if (oreGiorno != null && oreGiorno[giorno] >= pref.limiteH()) {
                return false;
            }
        }

        return true;
    }

    // Vincolo non obbligatorio: riduzione delle ore buche (considera tutti i docenti in compresenza)
    @Override
    protected void ordinaSlotLiberi(List<int[]> liberi, List<Docente> docenti) {
        if (liberi == null || liberi.size() <= 1 || docenti == null) return;
        liberi.sort(Comparator.comparingInt(slot -> {
            int penalita = 0;
            for (Docente d : docenti) {
                penalita += calcolaPenalitaSoft(d.getNome(), slot[0], slot[1]);
            }
            return penalita;
        }));
    }

    public int calcolaPenalitaSoft(String nomeDocente, int giorno, int ora) {
        if (nomeDocente == null) return 0;
        Preferenza pref = getPreferenzaDocente(nomeDocente);
        if (pref == null || !pref.riduciOreBuche()) {
            return 0;
        }

        List<Integer> oreGiorno = getOreDocenteNelGiorno(nomeDocente, giorno);
        if (oreGiorno.isEmpty()) {
            return 0;
        }

        int buchePrima = calcolaOreBucheGiorno(oreGiorno);

        List<Integer> oreIpotetiche = new ArrayList<>(oreGiorno);
        oreIpotetiche.add(ora);
        int bucheDopo = calcolaOreBucheGiorno(oreIpotetiche);

        int deltaBuche = bucheDopo - buchePrima;
        return (deltaBuche > 0) ? (deltaBuche * 20) : 0;
    }

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

    public static int calcolaOreBucheGiorno(List<Integer> ore) {
        if (ore == null || ore.size() <= 1) return 0;
        int min = Collections.min(ore);
        int max = Collections.max(ore);
        int span = max - min + 1;
        int idxPausa = OrarioLezioni.PAUSA.ordinal();
        if (min < idxPausa && max > idxPausa) {
            span--; // la pausa (14-15) non viene conteggiata come ora buca
        }
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

    // Report e verifica delle preferenze e dei conflitti
    public static class RisultatoVerificaDocente {
        public final String docente;
        public final int totOre;
        public final int[] oreGiorno = new int[GIORNI];
        public final int oreBucheTotali;
        public boolean violazioneGiornoLibero = false;
        public boolean violazioneNoPrimaH = false;
        public boolean violazioneNoUltimaH = false;
        public boolean violazioneLimiteH = false;
        public boolean violazioneContemporaneita = false;
        public final List<String> motivazioniNonSoddisfatte = new ArrayList<>();

        public RisultatoVerificaDocente(String docente, int totOre, int oreBucheTotali) {
            this.docente = docente;
            this.totOre = totOre;
            this.oreBucheTotali = oreBucheTotali;
        }

        public boolean hardRispettati() {
            return !violazioneGiornoLibero && !violazioneNoPrimaH && !violazioneNoUltimaH
                    && !violazioneLimiteH && !violazioneContemporaneita;
        }

        public boolean haSegnalazioni() {
            return !hardRispettati() || !motivazioniNonSoddisfatte.isEmpty();
        }

        public List<String> getElencoSegnalazioni() {
            List<String> list = new ArrayList<>();
            if (violazioneContemporaneita) list.add("[ERRORE] Contemporaneità: docente assegnato a più classi nella stessa ora.");
            if (violazioneGiornoLibero) list.add("[VINCOLO OBBLIGATORIO] Giorno libero non rispettato.");
            if (violazioneNoPrimaH) list.add("[VINCOLO OBBLIGATORIO] Presente alla 1ª ora nonostante vincolo 'no-prima-h'.");
            if (violazioneNoUltimaH) list.add("[VINCOLO OBBLIGATORIO] Presente all'ultima ora nonostante vincolo 'no-ultima-h'.");
            if (violazioneLimiteH) list.add("[VINCOLO OBBLIGATORIO] Superato il limite massimo di ore giornaliere.");
            list.addAll(motivazioniNonSoddisfatte);
            return list;
        }
    }

    public static Map<String, RisultatoVerificaDocente> analizzaOrario(ConfiguraVincoli vincoli, Istituto school) {
        Map<String, RisultatoVerificaDocente> analisi = new LinkedHashMap<>();
        if (school == null) return analisi;

        Map<String, ArrayList<Slot>> docMap = school.getDocenti_tabelleh();
        Map<String, Preferenza> prefByDocLower = new HashMap<>();
        if (vincoli != null && vincoli.preferenze() != null) {
            for (Preferenza p : vincoli.preferenze()) {
                if (p.docente() != null) {
                    prefByDocLower.put(p.docente().trim().toLowerCase(), p);
                }
            }
        }

        for (Docente doc : school.getDocentiUni()) {
            String nomeProf = doc.getNome();
            ArrayList<Slot> oreProf = docMap.get(nomeProf);
            int totOre = (oreProf != null) ? oreProf.size() : 0;

            Map<Integer, List<Integer>> orePerGiornoMap = new HashMap<>();
            for (int g = 0; g < GIORNI; g++) orePerGiornoMap.put(g, new ArrayList<>());

            Set<Integer> indiciVisti = new HashSet<>();
            boolean doppiaAssegnazione = false;

            if (oreProf != null) {
                for (Slot s : oreProf) {
                    if (s == null || s.isVuoto()) continue;
                    if (!indiciVisti.add(s.getIndice())) {
                        doppiaAssegnazione = true;
                    }
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
            res.violazioneContemporaneita = doppiaAssegnazione;

            Preferenza p = prefByDocLower.get(nomeProf.trim().toLowerCase());
            if (p != null) {
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
                        if (p.noUltimaH() && s.indexOra() == ultimaOraGiorno(s.indexGiorno())) res.violazioneNoUltimaH = true;
                    }
                }
                if (p.riduciOreBuche() && bucheTot > 0) {
                    res.motivazioniNonSoddisfatte.add("[PREFERENZA NON SODDISFATTA] 'riduci-ore-buche': "
                            + bucheTot + " ore buche settimanali.");
                }
            }

            analisi.put(nomeProf, res);
        }

        return analisi;
    }
}

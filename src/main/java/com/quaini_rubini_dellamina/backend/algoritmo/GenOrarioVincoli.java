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

    @Override
    protected boolean isSlotValido(String nomeDocente, int giorno, int ora) {
        // Contemporaneità (controllo della classe base)
        if (!super.isSlotValido(nomeDocente, giorno, ora)) {
            return false;
        }

        // Vincoli/preferenze
        Preferenza pref = preferenzeDocenti.get(nomeDocente);
        if (pref == null) {
            return true;
        }
        // obbligo gionro libero
        int giornoLiberoIdx = indiceGiorno(pref.giornoLibero());
        if (giornoLiberoIdx >= 0 && giornoLiberoIdx == giorno) {
            return false;
        }
        //obbligo prima ora libera
        if (pref.noPrimaH() && ora == 0) {
            return false;
        }
        //obbligo ultima ora libera
        if (pref.noUltimaH() && ora == (ORE - 1)) {
            return false;
        }
        //obbligo limite orario giornaliero
        if (pref.limiteH() > 0) {
            int[] oreGiorno = oreDocenteGiorno.get(nomeDocente);
            if (oreGiorno != null && oreGiorno[giorno] >= pref.limiteH()) {
                return false;
            }
        }
        return true;
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

    public static void stampaVerifica(ConfiguraVincoli vincoli, Istituto school) {
        final int GIORNI = Slot.GIORNI_SETTIMANA;
        final int ORE    = Slot.ORE_GIORNO;

        System.out.println("Verifica vincoli configurati:");
        Map<String, ArrayList<Slot>> doc = school.getDocenti_tabelleh();

        for (Preferenza p : vincoli.preferenze()) {
            ArrayList<Slot> oreProf = doc.get(p.docente());
            int numOre = (oreProf != null) ? oreProf.size() : 0;
            System.out.println("Docente: " + p.docente() + " (tot ore assegnate: " + numOre + ")");
            if (oreProf == null) continue;

            int[] orePerGiorno = new int[GIORNI];
            boolean violazionePrimaOra = false;
            boolean violazioneUltimaOra = false;

            for (Slot s : oreProf) {
                if (s.isVuoto()) continue;
                int g = s.indexGiorno();
                int o = s.indexOra();
                orePerGiorno[g]++;
                if (p.noPrimaH() && o == 0) violazionePrimaOra = true;
                if (p.noUltimaH() && o == (ORE - 1)) violazioneUltimaOra = true;
            }

            if (p.giornoLibero() != null) {
                int gLibero = indiceGiorno(p.giornoLibero());
                System.out.println("  - Giorno libero (" + p.giornoLibero() + "): ore assegnate = "
                        + (gLibero >= 0 ? orePerGiorno[gLibero] : "N/D"));
            }
            if (p.limiteH() > 0) {
                System.out.println("  - Limite ore/giorno (" + p.limiteH() + "): max riscontrato = "
                        + Arrays.stream(orePerGiorno).max().orElse(0));
            }
            if (p.noPrimaH()) {
                System.out.println("  - No prima ora: " + (violazionePrimaOra ? "VIOLATO" : "RISPETTATO"));
            }
            if (p.noUltimaH()) {
                System.out.println("  - No ultima ora: " + (violazioneUltimaOra ? "VIOLATO" : "RISPETTATO"));
            }
        }
    }
}
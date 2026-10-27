package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVWriter;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BuildCsv {

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina/output.csv";


    public void scriviOrario(List<Slot> slotAssegnati, String outputPath) throws IOException {
        // Costruisce una griglia [giorno][ora] → nome materia (stringa vuota se libero)
        String[][] griglia = new String[Slot.GIORNI_SETTIMANA][Slot.ORE_GIORNO];
        for (String[] riga : griglia)
            Arrays.fill(riga,"");

        for (Slot slot : slotAssegnati) {
            if (!slot.isAssegnato()) continue;
            int g = slot.indexGiorno();
            int o = slot.indexOra();
            if (g >= 0 && g < Slot.GIORNI_SETTIMANA && o >= 0 && o < Slot.ORE_GIORNO) {
                Lezione l = slot.getLezione();
                // Formato cella: materia (docente)
                griglia[g][o] = l.getMateria().getNome() + " (" + l.getDocente().getNome() + ")";
            }
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            // Intestazione
            writer.writeNext(new String[]{
                "Ora",
                Giorni.LUNEDI.getNome(),
                Giorni.MARTEDI.getNome(),
                Giorni.MERCOLEDI.getNome(),
                Giorni.GIOVEDI.getNome(),
                Giorni.VENERDI.getNome()
            });

            // Righe: una per ogni ora della giornata
            for (int ora = 0; ora < Slot.ORE_GIORNO; ora++) {
                String[] riga = new String[Slot.GIORNI_SETTIMANA + 1];
                riga[0] = OrarioLezioni.getNome(ora);
                for (int giorno = 0; giorno < Slot.GIORNI_SETTIMANA; giorno++) {
                    riga[giorno + 1] = griglia[giorno][ora];
                }
                writer.writeNext(riga);
            }
        }
    }

    public void scriviOrario(List<Slot> slotAssegnati) throws IOException {
        scriviOrario(slotAssegnati, DEFAULT_OUTPUT_PATH);
    }
}


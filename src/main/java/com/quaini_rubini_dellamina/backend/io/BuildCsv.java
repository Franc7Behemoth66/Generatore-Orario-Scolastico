package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVWriter;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildCsv {

    private static final Logger LOGGER = Logger.getLogger(BuildCsv.class.getName());

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina";


    public static void scriviOrario(List<Slot> slotAssegnati, String outputPath, boolean isClasse) throws IOException {
        if (slotAssegnati == null || slotAssegnati.isEmpty()) {
            return;
        }

        // Costruisce una griglia [giorno][ora] → nome materia (stringa vuota se libero)
        String[][] griglia = new String[Slot.GIORNI_SETTIMANA][Slot.ORE_GIORNO];
        for (String[] riga : griglia)
            Arrays.fill(riga, "");

        int rigaPausa = OrarioLezioni.PAUSA.ordinal();

        slotAssegnati = slotAssegnati.stream()
                .filter(s -> s != null && !s.isVuoto() && s.getLezione() != null && s.getLezione().getMateria() != null)
                .toList();

        for (Slot slot : slotAssegnati) {
            if (!slot.isAssegnato()) continue;
            int g = slot.indexGiorno();
            int o = slot.indexOra();
            if (g >= 0 && g < Slot.GIORNI_SETTIMANA && o >= 0 && o < Slot.ORE_GIORNO) {
                Lezione l = slot.getLezione();
                // Formato cella: materia (docente)
                if (isClasse)
                    griglia[g][o] = l.getMateria().getNome() + " (" + l.getNomiDocenti() + ")";
                    // Formato cella: key della classe
                else
                    griglia[g][o] = l.getClasse().getChiave();

                // Pomeriggio in questo giorno → compare la pausa (martedì o giovedì, dedotto dagli slot)
                if (o > rigaPausa) griglia[g][rigaPausa] = "PAUSA";
            }
        }

        Slot primoValido = null;
        for (Slot s : slotAssegnati) {
            if (s != null && !s.isVuoto() && s.getLezione() != null) {
                primoValido = s;
                break;
            }
        }
        if (primoValido == null) {
            return;
        }

        String nomeORClasse;
        if (isClasse) {
            nomeORClasse = primoValido.getLezione().getClasse().getChiave();
            outputPath = outputPath + "/" + nomeORClasse + ".csv";
        } else {
            nomeORClasse = primoValido.getLezione().getDocente().getNome();
            outputPath = outputPath + "/" + nomeORClasse + ".csv";
        }

        File targetFile = new File(outputPath);
        File dir = targetFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(targetFile))) {
            // Intestazione
            writer.writeNext(new String[]{
                    "Ora",
                    Giorni.LUNEDI.getNome(),
                    Giorni.MARTEDI.getNome(),
                    Giorni.MERCOLEDI.getNome(),
                    Giorni.GIOVEDI.getNome(),
                    Giorni.VENERDI.getNome(),
                    Giorni.SABATO.getNome()
            });

            // Righe: una per ogni ora della giornata (inclusa la pausa 14-15)
            for (int ora = 0; ora < Slot.ORE_GIORNO; ora++) {
                String[] riga = new String[Slot.GIORNI_SETTIMANA + 1];
                riga[0] = OrarioLezioni.getNome(ora);
                for (int giorno = 0; giorno < Slot.GIORNI_SETTIMANA; giorno++) {
                    riga[giorno + 1] = griglia[giorno][ora];
                }
                writer.writeNext(riga);
            }
            apriPDF(targetFile);
        }
    }


    private static void apriPDF(File file) {
        if (!Desktop.isDesktopSupported()) return;
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) return;
        try {
            desktop.open(file);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Impossibile aprire automaticamente il file CSV: " + file.getAbsolutePath(), e);
        }
    }
}
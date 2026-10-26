package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.Giorni;
import com.quaini_rubini_dellamina.backend.entità.Lezione;
import com.quaini_rubini_dellamina.backend.entità.OrarioLezioni;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


// errori logici, per ora è stato allestito solo come prova di output
public class BuildCsv {

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina/output.csv";

    public void scriviOrario(List<Lezione> orario, String outputPath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {

            writer.writeNext(new String[]{"ora", Giorni.LUNEDI.getNome(), Giorni.MARTEDI.getNome(), Giorni.MERCOLEDI.getNome(), Giorni.GIOVEDI.getNome(),
            Giorni.VENERDI.getNome()});

            for (int ora = 0; ora < 6; ora++) {
                String[] riga = new String[6];
                riga[0] = OrarioLezioni.getNome(ora);

                for (int giorno = 0; giorno < 5; giorno++) {
                    Lezione l = orario.get(giorno * 6 + ora);
                    if (l == null) {
                        riga[giorno + 1] = "";
                    } else {
                        riga[giorno + 1] = l.getMateria().getNome()
                                + " (" + l.getDocente().getNome()
                                + ", " + l.getClasse().getChiave() + ")";
                    }
                }
                writer.writeNext(riga);
            }
        }
    }


    public void scriviOrario(List<Lezione> orario) throws IOException {
        scriviOrario(orario, DEFAULT_OUTPUT_PATH);
    }

    public static void main (String[] args) throws CsvValidationException, IOException {
        BuildCsv cvsB = new BuildCsv();
        ReadCsv read = new ReadCsv();
        List<Lezione> lessons = read.leggi();
        cvsB.scriviOrario(lessons);



    }

}




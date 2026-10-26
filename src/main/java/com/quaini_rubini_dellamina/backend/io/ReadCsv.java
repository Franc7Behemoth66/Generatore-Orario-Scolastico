package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ReadCsv {

    private static final String DEFAULT_PATH =
            "src/main/java/com/quaini_rubini_dellamina/source.csv";

    private final String filepath;


    public ReadCsv() {
        this(DEFAULT_PATH);
    }

    public ReadCsv(String filepath) {
        this.filepath = filepath;
    }


    public List<Lezione> leggi() throws IOException, CsvValidationException {
        List<Lezione> lezioni = new ArrayList<>();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(filepath)).withSkipLines(1) // salta la riga di intestazione
                .build()) {

            String[] riga;
            int numeroRiga = 1;
            while ((riga = csvReader.readNext()) != null) {
                numeroRiga++;
                if (riga.length < 6) {
                    System.err.println("Riga " + numeroRiga + " ignorata (colonne insufficienti): "
                            + String.join(",", riga));
                    continue;
                }

                try {
                    String nomeDocente  = riga[0].trim();
                    int    anno         = Integer.parseInt(riga[1].trim());
                    char   sezione      = riga[2].trim().charAt(0);
                    String indirizzo    = riga[3].trim();
                    String nomeMateria  = riga[4].trim();
                    int    ore          = Integer.parseInt(riga[5].trim());

                    Docente docente = new Docente(nomeDocente);
                    Classe  classe  = new Classe(anno, sezione, indirizzo);
                    Materia materia = new Materia(nomeMateria);

                    lezioni.add(new Lezione(docente, classe, materia, ore));

                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    System.err.println("Riga " + numeroRiga + " ignorata (formato non valido): "
                            + String.join(",", riga) + " → " + e.getMessage());
                }
            }
        }

        return lezioni;
    }

//    public static void main(String[] args) {
//        ReadCsv reader = new ReadCsv();
//        try {
//            List<Lezione> lezioni = reader.leggi();
//            System.out.println("File csv letto con successo");
//            System.out.println("Lezioni caricate: " + lezioni.size());
//            lezioni.forEach(System.out::println);
//        } catch (IOException | CsvValidationException e) {
//            System.err.println("Errore nella lettura del CSV: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}

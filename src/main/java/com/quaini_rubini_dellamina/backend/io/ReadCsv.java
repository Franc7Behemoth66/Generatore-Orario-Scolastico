package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class ReadCsv {

    private static final String DEFAULT_PATH =
            "src/main/java/com/quaini_rubini_dellamina/source.csv";

    private final String filepath;
    private HashSet<Classe> classi = new HashSet<>();
    private HashSet<Docente> docenti = new HashSet<>();



    public ReadCsv() {
        this(DEFAULT_PATH);
    }

    public ReadCsv(String filepath) {
        this.filepath = filepath;
    }


    public List<Lezione> leggi() throws IOException, CsvValidationException {
        List<Lezione> lezioni = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(filepath))){
            csvReader.readNext(); //ignora nome colonne
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


    public HashSet<Classe> getAllClassi() throws CsvValidationException, IOException {
        if(classi.isEmpty()) {
            for (Lezione lezione : leggi())
                classi.add(lezione.getClasse());
        }
        return classi;
    } //restituisce in modo univoco le classi

    public HashSet<Docente> getAllDocenti() throws CsvValidationException, IOException {
        if(docenti.isEmpty()) {
            for (Lezione lezione : leggi())
                docenti.add(lezione.getDocente());
        }
        return docenti;
    } //restituisce in modo univoco i docenti


    public static void main(String[] args) {
        ReadCsv reader = new ReadCsv();
        try {
            List<Lezione> lezioni = reader.leggi();
            System.out.println("File csv letto con successo");
            //System.out.println("Lezioni caricate: " + lezioni.size());
            //lezioni.forEach(System.out::println);
            System.out.println(lezioni.size());
            for(Classe e : reader.getAllClassi())
                System.out.println(e);
            for(Docente d : reader.getAllDocenti())
                System.out.println(d);

        } catch (IOException | CsvValidationException e) {
            System.err.println("Errore nella lettura del CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

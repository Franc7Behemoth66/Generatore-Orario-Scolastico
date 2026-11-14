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
    private final List<String> erroriCsv = new ArrayList<>();

    public ReadCsv() {
        this(DEFAULT_PATH);
    }

    public ReadCsv(String filepath) {
        this.filepath = filepath;
    }

    public List<Lezione> leggi() throws IOException, CsvValidationException {
        List<Lezione> lezioni = new ArrayList<>();
        erroriCsv.clear();

        try (CSVReader csvReader = new CSVReader(new FileReader(filepath))) {
            csvReader.readNext(); // ignora nome colonne
            String[] riga;
            int numeroRiga = 1;
            while ((riga = csvReader.readNext()) != null) {
                numeroRiga++;
                if (riga.length == 1 && riga[0].trim().isEmpty()) {
                    continue; // salta eventuali righe completamente vuote
                }
                if (riga.length < 6) {
                    String msg = "Riga " + numeroRiga + " ignorata (colonne insufficienti): " + String.join(",", riga);
                    System.err.println(msg);
                    erroriCsv.add(msg);
                    continue;
                }

                try {
                    String nomeDocente = riga[0].trim();
                    String annoStr     = riga[1].trim();
                    String sezioneStr  = riga[2].trim();
                    String indirizzo   = riga[3].trim();
                    String nomeMateria = riga[4].trim();
                    String oreStr      = riga[5].trim();

                    if (nomeDocente.isEmpty() || annoStr.isEmpty() || sezioneStr.isEmpty()
                            || indirizzo.isEmpty() || nomeMateria.isEmpty() || oreStr.isEmpty()) {
                        String msg = "Riga " + numeroRiga + " ignorata (dati mancanti): " + String.join(",", riga);
                        System.err.println(msg);
                        erroriCsv.add(msg);
                        continue;
                    }

                    int  anno    = Integer.parseInt(annoStr);
                    char sezione = sezioneStr.charAt(0);
                    int  ore     = Integer.parseInt(oreStr);

                    if (anno <= 0 || ore <= 0) {
                        String msg = "Riga " + numeroRiga + " ignorata (anno o ore non validi/negativi): " + String.join(",", riga);
                        System.err.println(msg);
                        erroriCsv.add(msg);
                        continue;
                    }

                    Docente docente = new Docente(nomeDocente);
                    Classe  classe  = new Classe(anno, sezione, indirizzo);
                    Materia materia = new Materia(nomeMateria);

                    lezioni.add(new Lezione(docente, classe, materia, ore));

                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    String msg = "Riga " + numeroRiga + " ignorata (formato non valido): "
                            + String.join(",", riga) + " → " + e.getMessage();
                    System.err.println(msg);
                    erroriCsv.add(msg);
                }
            }
        }

        return lezioni;
    }

    public List<String> getErroriCsv() {
        return erroriCsv;
    }

    public HashSet<Classe> getAllClassi() throws CsvValidationException, IOException {
        if (classi.isEmpty()) {
            for (Lezione lezione : leggi())
                classi.add(lezione.getClasse());
        }
        return classi;
    }

    public HashSet<Docente> getAllDocenti() throws CsvValidationException, IOException {
        if (docenti.isEmpty()) {
            for (Lezione lezione : leggi())
                docenti.add(lezione.getDocente());
        }
        return docenti;
    }
}

package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ReadCsv {

    public static void main(String[] args) {
        List<String> Dati = new ArrayList<>();

        String filepath = "src/main/java/com/quaini_rubini_dellamina/source.csv";
        try (CSVReader csvReader = new CSVReader(new FileReader(filepath) )) {
            String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {

            Dati.add(Arrays.toString(nextRecord));
        }
            System.out.println(Dati);

    } catch (Exception e) {
        e.printStackTrace();
    }
    }
}

package com.quaini_rubini_dellamina.backend.algoritmo;
import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.Docente;
import com.quaini_rubini_dellamina.backend.entità.Lezione;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;

public class GenOrario {
    public void genOrario(Docente prof){
        ReadCsv reader = new ReadCsv();

        try{
            List<Lezione> ehy = reader.leggi();
        }
        catch (IOException | CsvValidationException e)
        {

        }


    }
}

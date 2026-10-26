package com.quaini_rubini_dellamina.backend.algoritmo;
import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.entità.Lezione;
import com.quaini_rubini_dellamina.backend.entità.OrarioLezioni;
import com.quaini_rubini_dellamina.backend.io.BuildCsv;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// implemetazione iniziale, non vengono ancora usate le istanze della classe Slot
public class GenOrario {


    public static void genOrarioOnClasse(List<Lezione> leziones, String keyClasse) throws IOException {
        List<Lezione> lezioniToCheck = new ArrayList<>();
        for(Lezione l : leziones){
            if(l.getClasse().getChiave().equals(keyClasse))
                lezioniToCheck.add(l);
        }
        BuildCsv builder = new BuildCsv();
        builder.scriviOrario(lezioniToCheck);


    }


     static void main(String[] args) throws CsvValidationException, IOException {
        ReadCsv red = new ReadCsv();
        List<Lezione> ls = new ArrayList<>(red.leggi());
//
        GenOrario.genOrarioOnClasse(ls,"5A-INFO");
    }


}

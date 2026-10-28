package com.quaini_rubini_dellamina.backend.entità;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PlessoScuola {

    SSS("Romegialli"),
    MAT("Romegialli"),
    TUR("Saraceno"),
    SIA("Saraceno"),
    AFM("Saraceno"),
    INFO("Saraceno"),
    CAT("Saraceno");

    private final String nome;
    PlessoScuola( String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static List<String> plessoSaraceno(){
        List<String> CSaraceno = new ArrayList<>();
        for(int i = 0; i < values().length; i++ ){
            if(values()[i].getNome().equals("Saraceno"))
                CSaraceno.add(values()[i].name());
        }
        return CSaraceno;
    }

    public static List<String> plessoRomegialli(){
        List<String> CRomegialli = new ArrayList<>();
        for(int i = 0; i < values().length; i++ ){
            if(values()[i].getNome().equals("Romegialli"))
                CRomegialli.add(values()[i].name());
        }
        return CRomegialli;
    }
}

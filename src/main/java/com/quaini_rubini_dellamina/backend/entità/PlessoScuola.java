package com.quaini_rubini_dellamina.backend.entità;

public enum PlessoScuola {

    SSA("Romegialli"),
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


}

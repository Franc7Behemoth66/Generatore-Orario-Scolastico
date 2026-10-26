package com.quaini_rubini_dellamina.backend.entità;

public enum Giorni {
    LUNEDI("Lunedì"),
    MARTEDI("Martedì"),
    MERCOLEDI("Mercoledì"),
    GIOVEDI("Giovedì"),
    VENERDI("Venerdì");

    private final String nome;

    Giorni(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
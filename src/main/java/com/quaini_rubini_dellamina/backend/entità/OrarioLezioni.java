package com.quaini_rubini_dellamina.backend.entità;

public enum OrarioLezioni {
    PRIMA("8-9"),
    SECONDA("9-10"),
    TERZA("10-11"),
    QUARTA("11-12"),
    QUINTA("12-13"),
    SESTA("13-14");

    private final String nome;

    OrarioLezioni(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static String getNome(int index){
        if(index >= 0 && index < values().length)
            return values()[index].getNome();
        return "";
    }
}

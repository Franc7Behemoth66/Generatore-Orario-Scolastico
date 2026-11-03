package com.quaini_rubini_dellamina.backend.entità;

import com.fasterxml.jackson.annotation.JsonProperty;
public record  Preferenza (
        //abbino la key del json agli attributi della classe Preferenza
            @JsonProperty("docente")  String docente,
            @JsonProperty("giorno-libero")  String giornoLibero,
            @JsonProperty("limite-h-giornaliere")  int limiteH,
            @JsonProperty("no-prima-h")  boolean noPrimaH,
            @JsonProperty("no-ultima-h")  boolean noUltimaH)
    {
    @Override
    public String toString() {
        return "Preferenza{" +
                "docente='" + docente + '\'' +
                ", giornoLibero='" + giornoLibero + '\'' +
                ", limiteH=" + limiteH +
                ", noPrimaH=" + noPrimaH +
                ", noUltimaH=" + noUltimaH +
                '}';
    }
}
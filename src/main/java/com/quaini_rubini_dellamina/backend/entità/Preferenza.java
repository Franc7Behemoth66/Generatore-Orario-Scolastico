package com.quaini_rubini_dellamina.backend.entità;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Preferenza(
        // Chiavi JSON abbinate agli attributi del record Preferenza
        @JsonProperty("docente") String docente,
        @JsonProperty("giorno-libero") String giornoLibero,
        @JsonProperty("limite-h-giornaliere") int limiteH,
        @JsonProperty("no-prima-h") boolean noPrimaH,
        @JsonProperty("no-ultima-h") boolean noUltimaH,
        //  vincolo non obbligatorio
        @JsonProperty("riduci-ore-buche") boolean riduciOreBuche
) {
    public Preferenza(String docente, String giornoLibero, int limiteH, boolean noPrimaH, boolean noUltimaH) {
        this(docente, giornoLibero, limiteH, noPrimaH, noUltimaH, false);
    }

    @Override
    public String toString() {
        return "Preferenza{" +
                "docente='" + docente + '\'' +
                ", giornoLibero='" + giornoLibero + '\'' +
                ", limiteH=" + limiteH +
                ", noPrimaH=" + noPrimaH +
                ", noUltimaH=" + noUltimaH +
                ", riduciOreBuche=" + riduciOreBuche +
                '}';
    }
}

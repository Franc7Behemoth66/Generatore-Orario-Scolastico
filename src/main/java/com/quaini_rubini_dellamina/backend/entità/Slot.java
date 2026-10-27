package com.quaini_rubini_dellamina.backend.entità;

import java.util.Objects;

//Rappresenta un'ora di lezione piazzata nell'orario settimanale.
//indice = giorno * 6 + ora  (5 giorni × 6 ore = 30 slot totali per classe/ orario docenti futuro)

public class Slot {
    private Lezione lezione;
    private int indice = -1;
    private boolean assegnato;

    public static final int ORE_GIORNO = OrarioLezioni.values().length;  // 6
    public static final int GIORNI_SETTIMANA = Giorni.values().length;   // 5

    public Slot(Lezione lezione, int indice) {
        this.lezione = lezione;
        this.indice = indice;
        this.assegnato = (indice >= 0);
    }

    public Slot(Lezione lezione) {
        this.lezione = lezione;
    }

    public Lezione getLezione() {
        return lezione;
    }

    public int getIndice() {
        return indice;
    }

    public boolean isAssegnato() {
        return assegnato;
    }

    public void setAssegnato(boolean assegnato) {
        this.assegnato = assegnato;
    }

    //Assegna lo slot a un preciso giorno (0-4) e ora (0-5).
    public void assegna(int giorno, int ora) {
        this.indice   = giorno * ORE_GIORNO + ora;
        this.assegnato = true;
    }

    // Restituisce l'indice del giorno (0 = Lunedì … 4 = Venerdì).
    public int indexGiorno() {
        return indice / ORE_GIORNO;
    }

    // Restituisce l'indice dell'ora nella giornata (0 = prima … 5 = sesta).
    public int indexOra() {
        return indice % ORE_GIORNO;
    }

    @Override
    public String toString() {
        if (!assegnato) return "[non assegnato] " + lezione;
        String giorno = Giorni.values()[indexGiorno()].getNome();
        String ora    = OrarioLezioni.values()[indexOra()].getNome();
        return giorno + " " + ora + " → " + lezione;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return indice == slot.indice && Objects.equals(lezione, slot.lezione);
    }

}

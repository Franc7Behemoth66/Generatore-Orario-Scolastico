package com.quaini_rubini_dellamina.backend.entità;

import java.util.Objects;

public class Slot {
    private Lezione lezione;
    private int indice;

    public Slot(Lezione lezione, int indice) {
        this.lezione = lezione;
        this.indice = indice;
    }

    public Lezione getLezione() {
        return lezione;
    }

    public int getIndice() {
        return indice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return indice == slot.indice && Objects.equals(lezione, slot.lezione);
    }

    public int indexGiorno(){
        return indice/6;
    }
}

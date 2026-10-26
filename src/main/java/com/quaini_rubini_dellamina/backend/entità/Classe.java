package com.quaini_rubini_dellamina.backend.entità;

import java.util.Objects;

public class Classe {
    private final int anno;         // es. 1, 2, 3, 4, 5
    private final char sezione;     // es. 'A', 'B', 'C'
    private final String indirizzo; // es. "INFO", "AFM", "TUR"

    public Classe(int anno, char sezione, String indirizzo) {
        this.anno = anno;
        this.sezione = sezione;
        this.indirizzo = indirizzo.trim();
    }

    public int getAnno() {
        return anno;
    }

    public char getSezione() {
        return sezione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    // Restituisce una chiave leggibile, es. "5A-INFO"
    public String getChiave() {
        return anno + "" + sezione + "-" + indirizzo;
    }

    @Override
    public String toString() {
        return getChiave();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classe)) return false;
        Classe c = (Classe) o;
        return anno == c.anno && sezione == c.sezione
                && indirizzo.equalsIgnoreCase(c.indirizzo);
    }


}

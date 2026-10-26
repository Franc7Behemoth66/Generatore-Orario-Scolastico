package com.quaini_rubini_dellamina.backend.entità;

import java.util.Objects;

public class Docente {
    private final String nome; // nome completo come appare nel CSV

    public Docente(String nome) {
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Docente)) return false;
        return nome.equalsIgnoreCase(((Docente) o).nome);
    }


}

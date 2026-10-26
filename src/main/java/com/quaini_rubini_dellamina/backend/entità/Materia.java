package com.quaini_rubini_dellamina.backend.entità;

import java.util.Objects;

public class Materia {
    private final String nome;

    public Materia(String nome) {
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
        if (!(o instanceof Materia)) return false;
        return nome.equalsIgnoreCase(((Materia) o).nome);
    }

}

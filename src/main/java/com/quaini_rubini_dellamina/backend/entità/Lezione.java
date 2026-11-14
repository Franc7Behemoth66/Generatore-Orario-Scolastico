package com.quaini_rubini_dellamina.backend.entità;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 Rappresenta un'assegnazione docente-classe-materia con il numero di ore settimanali.
 Supporta anche uno o più docenti in compresenza.
 */
public class Lezione {
    private final Docente docente;
    private final List<Docente> docentiCompresenza;
    private final Classe classe;
    private final Materia materia;
    private final int oreSettimanali;

    public Lezione(Docente docente, Classe classe, Materia materia, int oreSettimanali) {
        this(docente, Collections.emptyList(), classe, materia, oreSettimanali);
    }

    public Lezione(Docente docente, Docente docenteCompresenza, Classe classe, Materia materia, int oreSettimanali) {
        this(docente,
                docenteCompresenza != null ? List.of(docenteCompresenza) : Collections.emptyList(),
                classe, materia, oreSettimanali);
    }

    public Lezione(Docente docente, List<Docente> docentiCompresenza, Classe classe, Materia materia, int oreSettimanali) {
        this.docente = docente;
        this.docentiCompresenza = (docentiCompresenza == null)
                ? Collections.emptyList()
                : List.copyOf(docentiCompresenza);
        this.classe = classe;
        this.materia = materia;
        this.oreSettimanali = oreSettimanali;
    }

    public Docente getDocente() {
        return docente;
    }

    public List<Docente> getDocentiCompresenza() {
        return docentiCompresenza;
    }

    public List<Docente> getAllDocenti() {
        List<Docente> tutti = new ArrayList<>();
        if (docente != null) tutti.add(docente);
        tutti.addAll(docentiCompresenza);
        return tutti;
    }

    public boolean hasCompresenza() {
        return !docentiCompresenza.isEmpty();
    }

    public String getNomiDocenti() {
        if (docente == null) return "";
        if (!hasCompresenza()) return docente.getNome();
        return getAllDocenti().stream()
                .map(Docente::getNome)
                .collect(Collectors.joining(" / "));
    }

    public Classe getClasse() {
        return classe;
    }

    public Materia getMateria() {
        return materia;
    }

    public int getOreSettimanali() {
        return oreSettimanali;
    }

    @Override
    public String toString() {
        return getNomiDocenti() + " → " + classe + " | " + materia + " (" + oreSettimanali + "h)";
    }
}

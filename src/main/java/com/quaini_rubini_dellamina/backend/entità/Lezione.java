package com.quaini_rubini_dellamina.backend.entità;

/*
 Rappresenta un'assegnazione docente-classe-materia con il numero di ore settimanali.
 Supporta anche un eventuale secondo docente in compresenza.
 */
public class Lezione {
    private final Docente docente;
    private final Docente docenteCompresenza;
    private final Classe classe;
    private final Materia materia;
    private final int oreSettimanali;

    public Lezione(Docente docente, Classe classe, Materia materia, int oreSettimanali) {
        this(docente, null, classe, materia, oreSettimanali);
    }

    public Lezione(Docente docente, Docente docenteCompresenza, Classe classe, Materia materia, int oreSettimanali) {
        this.docente = docente;
        this.docenteCompresenza = docenteCompresenza;
        this.classe = classe;
        this.materia = materia;
        this.oreSettimanali = oreSettimanali;
    }

    public Docente getDocente() {
        return docente;
    }

    public Docente getDocenteCompresenza() {
        return docenteCompresenza;
    }

    public boolean hasCompresenza() {
        return docenteCompresenza != null;
    }

    public String getNomiDocenti() {
        if (docente == null) return "";
        if (docenteCompresenza == null) return docente.getNome();
        return docente.getNome() + " / " + docenteCompresenza.getNome();
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

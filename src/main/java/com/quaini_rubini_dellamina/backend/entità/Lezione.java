package com.quaini_rubini_dellamina.backend.entità;

/*
 Rappresenta un'assegnazione docente-classe-materia con il numero di ore settimanali.
 Corrisponde a una riga del CSV sorgente.
 */
public class Lezione {
    private final Docente docente;
    private final Classe classe;
    private final Materia materia;
    private final int oreSettimanali;

    public Lezione(Docente docente, Classe classe, Materia materia, int oreSettimanali) {
        this.docente = docente;
        this.classe = classe;
        this.materia = materia;
        this.oreSettimanali = oreSettimanali;
    }

    public Docente getDocente() {
        return docente;
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
        return docente + " → " + classe + " | " + materia + " (" + oreSettimanali + "h)";
    }
}

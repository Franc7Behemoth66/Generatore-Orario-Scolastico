package com.quaini_rubini_dellamina.backend.entità;

import java.util.*;

public class Istituto {
    private final List<Docente> docentiUni;
    private final List<Classe> classiUni;

    private HashMap<String, ArrayList<Slot>> classi_tabelleh = new HashMap<>();
    private HashMap<String, ArrayList<Slot>> docenti_tabelleh = new HashMap<>();

    public Istituto(Set<Classe> classiUni, Set<Docente> docentiUni) {
        this.classiUni = classiUni.stream()
                .sorted(Comparator.comparing(Classe::getChiave))
                .toList();

        this.docentiUni = docentiUni.stream()
                .sorted(Comparator.comparing(Docente::getNome))
                .toList();

        for (Classe c : classiUni)
            classi_tabelleh.put(c.getChiave(), null);
        for (Docente d : this.docentiUni)
            docenti_tabelleh.put(d.getNome(), null);
    }

    public List<Docente> getDocentiUni() {
        return docentiUni;
    }

    public List<Classe> getClassiUni() {
        return classiUni;
    }

    public HashMap<String, ArrayList<Slot>> getDocenti_tabelleh() {
        return docenti_tabelleh;
    }

    public HashMap<String, ArrayList<Slot>> getClassi_tabelleh() {
        return classi_tabelleh;
    }

    public void setHforDocente(String key, ArrayList<Slot> orarioGenerated) {
        if (!docenti_tabelleh.containsKey(key))
            throw new IllegalArgumentException("Docente sconosciuto: " + key);
        docenti_tabelleh.put(key, orarioGenerated);
    }

    public void setHforClasse(String key, ArrayList<Slot> orarioGenerated) {
        if (!classi_tabelleh.containsKey(key))
            throw new IllegalArgumentException("Classe sconosciuto: " + key);
        classi_tabelleh.put(key, orarioGenerated);
    }

    public void deduciOrariProf() {
        for (String chiaveDocente : docenti_tabelleh.keySet()) {
            ArrayList<Slot> risultato = new ArrayList<>();

            for (ArrayList<Slot> provvisorioHClasse : classi_tabelleh.values()) {
                // Protezione se la lista di slot della classe è null
                if (provvisorioHClasse == null) continue;

                for (Slot s : provvisorioHClasse) {
                    //  verifica che lo slot sia assegnato e la lezione/docente non siano null
                    if (s != null && s.isAssegnato() && s.getLezione() != null &&
                            s.getLezione().getDocente() != null &&
                            s.getLezione().getDocente().getNome() != null) {

                        if (s.getLezione().getDocente().getNome().equals(chiaveDocente)) {
                            risultato.add(s);
                        }
                    }
                }
            }
            docenti_tabelleh.put(chiaveDocente, risultato);
        }
    }
}
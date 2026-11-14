package com.quaini_rubini_dellamina.backend.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quaini_rubini_dellamina.backend.entità.ConfiguraVincoli;
import com.quaini_rubini_dellamina.backend.entità.Preferenza;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteOnJSON {

    private static final String FILE_OUTPUT_VINCOLI =
            "src/main/java/com/quaini_rubini_dellamina/backend/entità/preferenze.json";

    public static void salva(Preferenza nuovaPreferenza) throws IOException {
        salva(FILE_OUTPUT_VINCOLI, nuovaPreferenza);
    }

    public static void salva(String path, Preferenza nuovaPreferenza) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        File fileJson = new File(path);

        String nomeScuola = "Saraceno-Romegialli";
        List<Preferenza> listaPreferenze = new ArrayList<>();

        if (fileJson.exists() && fileJson.length() > 0) {
            ConfiguraVincoli attuale = ReadVincoliJSON.leggi(path);
            if (attuale != null) {
                if (attuale.nomeScuola() != null && !attuale.nomeScuola().isBlank()) {
                    nomeScuola = attuale.nomeScuola();
                }
                if (attuale.preferenze() != null) {
                    listaPreferenze.addAll(attuale.preferenze());
                }
            }
        }

        // Aggiorna la preferenza del docente senza creare duplicati
        listaPreferenze.removeIf(p -> p.docente().equalsIgnoreCase(nuovaPreferenza.docente()));
        listaPreferenze.add(nuovaPreferenza);

        ConfiguraVincoli aggiornato = new ConfiguraVincoli(nomeScuola, listaPreferenze);
        mapper.writeValue(fileJson, aggiornato);
    }
}
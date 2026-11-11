package com.quaini_rubini_dellamina.backend.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.io.File;
import java.io.IOException;

public class ReadVincoliJSON {

    private static final String FILE_INPUT_VINCOLI =
            "src/main/java/com/quaini_rubini_dellamina/backend/entità/preferenze.json";

    private ConfiguraVincoli configuraVincoli;

    public ReadVincoliJSON() throws IOException {
        this(FILE_INPUT_VINCOLI);
    }

    public ReadVincoliJSON(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.configuraVincoli = objectMapper.readValue(new File(path), ConfiguraVincoli.class);

        System.out.println(configuraVincoli.nomeScuola());
        for (Preferenza p : configuraVincoli.preferenze()) {
            System.out.println(p);
        }
    }

    public ConfiguraVincoli getConfiguraVincoli() {
        return configuraVincoli;
    }

    public static ConfiguraVincoli leggi() throws IOException {
        return leggi(FILE_INPUT_VINCOLI);
    }

    public static ConfiguraVincoli leggi(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(path), ConfiguraVincoli.class);
    }

}

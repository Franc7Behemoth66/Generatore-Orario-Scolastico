package com.quaini_rubini_dellamina.backend.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.io.File;
import java.io.IOException;

public class ReadVincoliJSON {

    private static final String FILE_INPUT_VINCOLI =
            "src/main/java/com/quaini_rubini_dellamina/backend/entità/preferenze.json";

    public ReadVincoliJSON() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConfiguraVincoli cfg = objectMapper.readValue(new File(FILE_INPUT_VINCOLI), ConfiguraVincoli.class);

        System.out.println(cfg.nomeScuola());
        for (Preferenza p : cfg.preferenze()) {
            System.out.println(p);
        }
    }

    public static void main(String[] args) throws IOException {
        new ReadVincoliJSON();
    }
}

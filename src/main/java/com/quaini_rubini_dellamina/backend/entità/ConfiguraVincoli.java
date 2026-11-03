package com.quaini_rubini_dellamina.backend.entità;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record  ConfiguraVincoli(     //abbino key json alle variabili
              @JsonProperty("nome-scuola")  String nomeScuola,
              @JsonProperty("preferenze") List<Preferenza> preferenze)
{}
package com.quaini_rubini_dellamina;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.quaini_rubini_dellamina.backend.OrarioManager;
import com.quaini_rubini_dellamina.frontend.HomeWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatDarculaLaf.setup();


        // Inizializza il backend (carica CSV e genera orario) in un thread separato
        // così la GUI si apre subito e ci sono meno probabilità di crash.
        new Thread(() -> OrarioManager.getInstance().inizializza()).start();

        SwingUtilities.invokeLater(HomeWindow::new);
    }
}

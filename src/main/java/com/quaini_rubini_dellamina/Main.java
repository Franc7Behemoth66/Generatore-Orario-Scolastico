package com.quaini_rubini_dellamina;


import com.formdev.flatlaf.FlatDarculaLaf;
import com.quaini_rubini_dellamina.frontend.ClasseOrario;
import com.quaini_rubini_dellamina.frontend.HomeWindow;


import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("flatlaf.logLevel", "off");
        FlatDarculaLaf.setup();
        SwingUtilities.invokeLater(HomeWindow::new);
    }
}

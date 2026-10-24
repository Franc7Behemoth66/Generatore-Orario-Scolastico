package com.quaini_rubini_dellamina;


import com.formdev.flatlaf.FlatDarculaLaf;
import com.quaini_rubini_dellamina.frontend.Gui;


import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        FlatDarculaLaf.setup();
        SwingUtilities.invokeLater(Gui::new);
    }
}
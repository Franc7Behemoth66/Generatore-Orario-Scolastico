package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

import java.awt.*;

public class Gui extends JFrame {

    public Gui(){


        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JRootPane root = this.getRootPane();
        root.putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
        root.putClientProperty("apple.awt.transparentTitleBar", true);
        root.putClientProperty("apple.awt.windowTitleVisible", false);
        root.putClientProperty("apple.awt.fullWindowContent", true);
        root.putClientProperty("apple.awt.transparentTitleBar", true);
        root.putClientProperty(FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING,
                FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING_LARGE);

        // Placeholder che riserva lo spazio dei pallini rossi/gialli/verdi
        JPanel placeholder = new JPanel();
        placeholder.putClientProperty(
                FlatClientProperties.FULL_WINDOW_CONTENT_BUTTONS_PLACEHOLDER, "mac");

        JLabel lblTitle = new JLabel("Mio Progetto");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 12f));

        JTextField searchField = new JTextField(15);
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cerca...");

        JToolBar right = new JToolBar();
        right.setFloatable(false);
        right.add(new JButton("Impostazioni"));
        right.add(new JButton("Profilo"));

        // Leading: pallini + titolo
        JPanel leading = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leading.setOpaque(false);
        leading.add(placeholder);
        leading.add(lblTitle);

        // Centro: ricerca
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(searchField);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.add(leading, BorderLayout.WEST);
        titleBar.add(center, BorderLayout.CENTER);
        titleBar.add(right, BorderLayout.EAST);

        add(titleBar, BorderLayout.NORTH);
        add(new JPanel(), BorderLayout.CENTER); // il tuo contenuto

        setVisible(true);
    }

}

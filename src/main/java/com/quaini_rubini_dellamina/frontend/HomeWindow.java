package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class HomeWindow extends JFrame {
    private final JButton btnTema;
    private final JLabel l2;
    private final JPanel cardConsultazione;
    private final JPanel cardPlesso;
    private final JToggleButton professori;
    private final JToggleButton classi;
    private final JToggleButton saraceno;
    private final JToggleButton romegialli;

    public HomeWindow() {
        super("Orario Scolastico - Istituto Saraceno Romegialli");

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // ====================================================
        // HEADER MODERNO
        // ====================================================
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblTitle = new JLabel("Orario Scolastico");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setIcon(new FlatSVGIcon("icons/calendar.svg", 28, 28));
        lblTitle.setIconTextGap(12);

        JLabel lblSubtitle = new JLabel("Istituto di Istruzione Superiore Saraceno - Romegialli");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(160, 160, 160));

        JLabel lblStats = new JLabel("✓ Dataset attivo: 679 lezioni • 52 classi • 129 docenti");
        lblStats.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStats.setForeground(new Color(40, 167, 69));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSubtitle);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(lblStats);

        // Bottone cambio tema stile pillola
        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro",
                new FlatSVGIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style",
                "arc: 999; " +
                        "margin: 8,16,8,16; " +
                        "borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(btnTema);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // ====================================================
        // SEZIONE 1: CONSULTAZIONE (TITOLETTO ESTERNO)
        // ====================================================
        Font titleFont = new Font("SansSerif", Font.BOLD, 13);

        JLabel l1 = new JLabel("CHE ORARIO DESIDERI CONSULTARE?");
        l1.setFont(titleFont);
        l1.setForeground(new Color(170, 170, 170));
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        professori = createSeamlessToggle("PROFESSORI", "icons/user.svg");
        professori.setBackground(new Color(0, 0, 0, 0));
        classi = createSeamlessToggle("CLASSI", "icons/hat.svg");
        classi.setBackground(new Color(0, 0, 0, 0));

        ButtonGroup gruppoTipo = new ButtonGroup();
        gruppoTipo.add(professori);
        gruppoTipo.add(classi);

        JPanel p1Buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        p1Buttons.setOpaque(false);
        p1Buttons.add(professori);
        p1Buttons.add(classi);

        cardConsultazione = createCardContainer(p1Buttons);

        // ====================================================
        // SEZIONE 2: SELEZIONE PLESSO (TITOLETTO ESTERNO)
        // ====================================================
        l2 = new JLabel("SELEZIONA IL PLESSO");
        l2.setFont(titleFont);
        l2.setForeground(new Color(170, 170, 170));
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        l2.setVisible(false);

        saraceno = createSeamlessToggle("PLESSO SARACENO", "icons/school.svg");
        saraceno.setBackground(new Color(0, 0, 0, 0));
        romegialli = createSeamlessToggle("PLESSO ROMEGIALLI", "icons/school.svg");
        romegialli.setBackground(new Color(0, 0, 0, 0));

        ButtonGroup gruppoPlesso = new ButtonGroup();
        gruppoPlesso.add(saraceno);
        gruppoPlesso.add(romegialli);

        JPanel p2Buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        p2Buttons.setOpaque(false);
        p2Buttons.add(saraceno);
        p2Buttons.add(romegialli);

        cardPlesso = createCardContainer(p2Buttons);
        cardPlesso.setVisible(false);

        // ====================================================
        // BOTTONI AZIONE BOTTOM
        // ====================================================
        JButton cerca = new JButton("CERCA ORARIO", new FlatSVGIcon("icons/lens.svg", 18, 18));
        cerca.setFont(new Font("SansSerif", Font.BOLD, 14));
        cerca.setIconTextGap(10);
        cerca.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cerca.putClientProperty("FlatLaf.style",
                "background: #0d6efd; " +
                        "foreground: #ffffff; " +
                        "arc: 12; " +
                        "margin: 10,22,10,22; " +
                        "borderWidth: 0; " +
                        "focusWidth: 0");

        JButton carica = new JButton("CARICA CSV", new FlatSVGIcon("icons/caricaCSV.svg", 18, 18));
        carica.setFont(new Font("SansSerif", Font.BOLD, 14));
        carica.setIconTextGap(10);
        carica.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        carica.putClientProperty("FlatLaf.style",
                "arc: 12; " +
                        "margin: 10,20,10,20; " +
                        "borderWidth: 1; " +
                        "focusWidth: 0");

        JPanel p3Actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        p3Actions.setOpaque(false);
        p3Actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p3Actions.add(cerca);
        p3Actions.add(carica);

        // ====================================================
        // STRUTTURA E DISPOSIZIONE LAYOUT
        // ====================================================
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(25));

        mainPanel.add(l1);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cardConsultazione);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(l2);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cardPlesso);
        mainPanel.add(Box.createVerticalStrut(25));

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(p3Actions);

        // Gestione visibilità selezione
        classi.addActionListener(e -> {
            l2.setVisible(true);
            cardPlesso.setVisible(true);
            revalidate();
            repaint();
        });

        professori.addActionListener(e -> {
            l2.setVisible(false);
            cardPlesso.setVisible(false);
            revalidate();
            repaint();
        });

        cerca.addActionListener(e -> {
            if (professori.isSelected()) {
                new ProfessoreOrario();
                dispose();
            } else if (classi.isSelected() && saraceno.isSelected()) {
                new ClasseOrario("saraceno");
                dispose();
            } else if (classi.isSelected() && romegialli.isSelected()) {
                new ClasseOrario("romegialli");
                dispose();
            }
        });

        carica.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("File CSV", "csv"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                System.out.println("Selezionato: " + fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Applica gli stili dinamici per i colori in base al tema attivo
        applyThemeStyles();

        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Contenitore principale per le opzioni.
     */
    private JPanel createCardContainer(JComponent content) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.add(content);
        return card;
    }

    /**
     * Toggle button integrato senza bordi visibili.
     */
    private JToggleButton createSeamlessToggle(String text, String iconPath) {
        JToggleButton btn = new JToggleButton(text, new FlatSVGIcon(iconPath, 20, 20));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setIconTextGap(10);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Aggiorna lo stile dinamico dei contenitori e dei pulsanti quando cambia il tema.
     */
    private void applyThemeStyles() {
        boolean isDark = FlatLaf.isLafDark();

        // Grigio scuro (#2b2b2e) per tema Dark, grigio visibile (#d8d8de) per tema Light
        String bgHex = isDark ? "#2b2b2e" : "#d8d8de";
        String borderHex = isDark ? "#3a3a3d" : "#c2c2c8";
        String cardStyle = "arc: 16; background: " + bgHex + "; borderWidth: 1; borderColor: " + borderHex;

        if (cardConsultazione != null) cardConsultazione.putClientProperty("FlatLaf.style", cardStyle);
        if (cardPlesso != null) cardPlesso.putClientProperty("FlatLaf.style", cardStyle);

        // Sfondo evidenziato per il pulsante selezionato
        String selectedBgHex = isDark ? "#3b3b3f" : "#c5c5cb";
        String toggleStyle = "arc: 12; margin: 8,18,8,18; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0; " +
                "background: transparent; borderColor: transparent; focusedBorderColor: transparent; " +
                "selectedBackground: " + selectedBgHex + "; selectedForeground: $Label.foreground";

        if (professori != null) professori.putClientProperty("FlatLaf.style", toggleStyle);
        if (classi != null) classi.putClientProperty("FlatLaf.style", toggleStyle);
        if (saraceno != null) saraceno.putClientProperty("FlatLaf.style", toggleStyle);
        if (romegialli != null) romegialli.putClientProperty("FlatLaf.style", toggleStyle);
    }

    /**
     * Alterna il tema chiaro/scuro e riaggiorna l'interfaccia.
     */
    private void toggleTheme() {
        try {
            if (FlatLaf.isLafDark()) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnTema.setText("Scuro");
                btnTema.setIcon(new FlatSVGIcon("icons/moon.svg", 18, 18));
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnTema.setText("Chiaro");
                btnTema.setIcon(new FlatSVGIcon("icons/sun.svg", 18, 18));
            }
            applyThemeStyles();
            FlatLaf.updateUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
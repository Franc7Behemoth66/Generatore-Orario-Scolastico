package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.OrarioManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class HomeWindow extends JFrame {

    private final JButton btnTema;
    private final JButton btnSegnalazioni;
    private final JLabel l2;
    private final JLabel lblStats;
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

        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JLabel lblTitle = new JLabel("Orario Scolastico");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setIcon(createAdaptiveIcon("icons/calendar.svg", 28, 28));
        lblTitle.setIconTextGap(12);

        JLabel lblSubtitle = new JLabel("Istituto di Istruzione Superiore Saraceno - Romegialli");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(160, 160, 160));

        lblStats = new JLabel();
        lblStats.setFont(new Font("SansSerif", Font.BOLD, 13));

        btnSegnalazioni = new JButton();
        btnSegnalazioni.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSegnalazioni.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSegnalazioni.addActionListener(e -> mostraReportSegnalazioni());

        aggiornaStats();

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSubtitle);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(lblStats);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(btnSegnalazioni);

        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro",
                createAdaptiveIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16; borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(btnTema);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // SEZIONE
        Font titleFont = new Font("SansSerif", Font.BOLD, 13);

        JLabel l1 = new JLabel("CHE ORARIO DESIDERI CONSULTARE?");
        l1.setFont(titleFont);
        l1.setForeground(new Color(170, 170, 170));
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        professori = createSeamlessToggle("PROFESSORI", "icons/user.svg");
        classi = createSeamlessToggle("CLASSI", "icons/hat.svg");

        ButtonGroup gruppoTipo = new ButtonGroup();
        gruppoTipo.add(professori);
        gruppoTipo.add(classi);

        JPanel p1Buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        p1Buttons.setOpaque(false);
        p1Buttons.add(professori);
        p1Buttons.add(classi);

        cardConsultazione = createCardContainer(p1Buttons);

        l2 = new JLabel("SELEZIONA IL PLESSO");
        l2.setFont(titleFont);
        l2.setForeground(new Color(170, 170, 170));
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        l2.setVisible(false);

        saraceno = createSeamlessToggle("PLESSO SARACENO", "icons/school.svg");
        romegialli = createSeamlessToggle("PLESSO ROMEGIALLI", "icons/school.svg");

        ButtonGroup gruppoPlesso = new ButtonGroup();
        gruppoPlesso.add(saraceno);
        gruppoPlesso.add(romegialli);

        JPanel p2Buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        p2Buttons.setOpaque(false);
        p2Buttons.add(saraceno);
        p2Buttons.add(romegialli);

        cardPlesso = createCardContainer(p2Buttons);
        cardPlesso.setVisible(false);

        // ACTION BUTTON INFERIORI
        JButton cerca = new JButton("CERCA ORARIO", createWhiteIcon("icons/lens.svg", 18, 18));
        cerca.setFont(new Font("SansSerif", Font.BOLD, 14));
        cerca.setIconTextGap(10);
        cerca.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cerca.putClientProperty("FlatLaf.style",
                "background: #0d6efd; foreground: #ffffff; arc: 12; margin: 10,22,10,22; borderWidth: 0; focusWidth: 0");

        JButton carica = new JButton("CARICA CSV", createAdaptiveIcon("icons/caricaCSV.svg", 18, 18));
        carica.setFont(new Font("SansSerif", Font.BOLD, 14));
        carica.setIconTextGap(10);
        carica.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        carica.putClientProperty("FlatLaf.style", "arc: 12; margin: 10,20,10,20; borderWidth: 1; focusWidth: 0");

        JPanel p3Actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        p3Actions.setOpaque(false);
        p3Actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p3Actions.add(cerca);
        p3Actions.add(carica);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(l1);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cardConsultazione);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(l2);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cardPlesso);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(p3Actions);

        // GESTIONE EVENTI E CARICAMENTO CSV
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
            OrarioManager mgr = OrarioManager.getInstance();
            if (!mgr.isInizializzato()) {
                JOptionPane.showMessageDialog(this,
                        "Nessun dataset caricato.\nUsa il pulsante \"CARICA CSV\" prima di cercare un orario.",
                        "Dataset mancante", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (professori.isSelected()) {
                SwingUtilities.invokeLater(ProfessoreOrario::new);
                dispose();
            } else if (classi.isSelected() && saraceno.isSelected()) {
                SwingUtilities.invokeLater(() -> new ClasseOrario("saraceno"));
                dispose();
            } else if (classi.isSelected() && romegialli.isSelected()) {
                SwingUtilities.invokeLater(() -> new ClasseOrario("romegialli"));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleziona il tipo di orario (Professori / Classi) e, se necessario, il plesso.",
                        "Selezione richiesta", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        carica.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileFilter(new FileNameExtensionFilter("File CSV (*.csv)", "csv"));
            int scelta = fileChooser.showOpenDialog(this);
            if (scelta != JFileChooser.APPROVE_OPTION) return;

            String path = fileChooser.getSelectedFile().getAbsolutePath();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            carica.setEnabled(false);
            cerca.setEnabled(false);

            new Thread(() -> {
                try {
                    OrarioManager.getInstance().caricaNuovoCsv(path);
                    SwingUtilities.invokeLater(() -> {
                        aggiornaStats();
                        setCursor(Cursor.getDefaultCursor());
                        carica.setEnabled(true);
                        cerca.setEnabled(true);
                        OrarioManager mgr = OrarioManager.getInstance();
                        int nSegn = mgr.getReportErroriEVincoli().size();
                        JOptionPane.showMessageDialog(this,
                                "File CSV caricato con successo!\n"
                                        + "Orario d'istituto generato.\n\n"
                                        + "• Lezioni: " + mgr.getNumeroLezioni() + "\n"
                                        + "• Classi:  " + mgr.getNumeroClassi() + "\n"
                                        + "• Docenti: " + mgr.getNumeroDocenti() + "\n"
                                        + "• Segnalazioni (errori/vincoli): " + nSegn,
                                "Caricamento completato",
                                nSegn > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        carica.setEnabled(true);
                        cerca.setEnabled(true);
                        JOptionPane.showMessageDialog(this,
                                "Errore durante l'importazione:\n" + ex.getMessage(),
                                "Errore Caricamento", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });

        new Timer(500, e -> {
            if (OrarioManager.getInstance().isInizializzato()) {
                aggiornaStats();
                ((Timer) e.getSource()).stop();
            }
        }).start();

        applyThemeStyles();

        setSize(720, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // AGGIORNAMENTO STATISTICHE E SEGNALAZIONI
    private void aggiornaStats() {
        OrarioManager mgr = OrarioManager.getInstance();
        if (mgr.isInizializzato()) {
            lblStats.setText(" ✔︎ Dataset caricato: " + mgr.getNumeroLezioni() + " lezioni • "
                    + mgr.getNumeroClassi() + " classi • " + mgr.getNumeroDocenti() + " docenti");
            lblStats.setForeground(new Color(40, 167, 69));

            List<String> segnalazioni = mgr.getReportErroriEVincoli();
            btnSegnalazioni.setVisible(true);
            if (segnalazioni.isEmpty()) {
                btnSegnalazioni.setText("✔ Nessun errore o vincolo violato");
                btnSegnalazioni.putClientProperty("FlatLaf.style",
                        "background: #198754; foreground: #ffffff; arc: 999; margin: 4,12,4,12; borderWidth: 0");
            } else {
                btnSegnalazioni.setText("⚠ " + segnalazioni.size() + " errori / vincoli non rispettati (Dettagli)");
                btnSegnalazioni.putClientProperty("FlatLaf.style",
                        "background: #dc3545; foreground: #ffffff; arc: 999; margin: 4,12,4,12; borderWidth: 0");
            }
        } else {
            lblStats.setText("⚠ Nessun dataset caricato – usa \"CARICA CSV\"");
            lblStats.setForeground(new Color(220, 53, 69));
            btnSegnalazioni.setVisible(false);
        }
    }

    private void mostraReportSegnalazioni() {
        List<String> segnalazioni = OrarioManager.getInstance().getReportErroriEVincoli();
        if (segnalazioni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tutti gli orari sono stati generati senza errori e tutti i vincoli dei docenti sono rispettati.",
                    "Verifica Orari", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segnalazioni.size(); i++) {
            sb.append(i + 1).append(". ").append(segnalazioni.get(i)).append("\n\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 18, 65);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll,
                "Segnalazione Errori e Vincoli Non Rispettati (" + segnalazioni.size() + ")",
                JOptionPane.WARNING_MESSAGE);
    }

    // METODI DI SUPPORTO UI E TEMA
    private JPanel createCardContainer(JComponent content) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.add(content);
        return card;
    }

    private JToggleButton createSeamlessToggle(String text, String iconPath) {
        JToggleButton btn = new JToggleButton(text, createAdaptiveIcon(iconPath, 20, 20));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setIconTextGap(10);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addItemListener(e -> updateToggleStyle(btn));
        return btn;
    }

    private FlatSVGIcon createAdaptiveIcon(String path, int width, int height) {
        FlatSVGIcon icon = new FlatSVGIcon(path, width, height);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("Label.foreground")));
        return icon;
    }

    private FlatSVGIcon createWhiteIcon(String path, int width, int height) {
        FlatSVGIcon icon = new FlatSVGIcon(path, width, height);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        return icon;
    }

    private void updateToggleStyle(JToggleButton btn) {
        if (btn == null) return;
        boolean isDark = FlatLaf.isLafDark();
        boolean selected = btn.isSelected();
        String selectedBgHex = isDark ? "#3b3b3f" : "#c5c5cb";
        String bg = selected ? selectedBgHex : "#00000000";
        String border = selected ? "#198754" : "#00000000";
        String style = "arc: 12; margin: 8,18,8,18; "
                + "borderWidth: 2; borderColor: " + border + "; "
                + "focusWidth: 0; innerFocusWidth: 0; focusedBorderColor: " + border + "; "
                + "hoverBorderColor: " + border + "; pressedBorderColor: " + border + "; "
                + "background: " + bg + "; selectedBackground: " + selectedBgHex + "; "
                + "selectedForeground: $Label.foreground";
        btn.putClientProperty("FlatLaf.style", style);
        btn.repaint();
    }

    private void applyThemeStyles() {
        boolean isDark = FlatLaf.isLafDark();
        Color bgColor     = isDark ? new Color(0x2b, 0x2b, 0x2e) : new Color(0xd8, 0xd8, 0xde);
        Color borderColor = isDark ? new Color(0x3a, 0x3a, 0x3d) : new Color(0xc2, 0xc2, 0xc8);
        String cardStyle = "arc: 16; background: " + String.format("#%06x", bgColor.getRGB() & 0xFFFFFF);

        if (cardConsultazione != null) {
            cardConsultazione.putClientProperty("FlatLaf.style", cardStyle);
            cardConsultazione.setBackground(bgColor);
            cardConsultazione.setBorder(new CompoundBorder(
                    new LineBorder(borderColor, 1, true),
                    new EmptyBorder(0, 0, 0, 0)));
        }
        if (cardPlesso != null) {
            cardPlesso.putClientProperty("FlatLaf.style", cardStyle);
            cardPlesso.setBackground(bgColor);
            cardPlesso.setBorder(new CompoundBorder(
                    new LineBorder(borderColor, 1, true),
                    new EmptyBorder(0, 0, 0, 0)));
        }
        updateToggleStyle(professori);
        updateToggleStyle(classi);
        updateToggleStyle(saraceno);
        updateToggleStyle(romegialli);
    }

    private void toggleTheme() {
        try {
            if (FlatLaf.isLafDark()) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnTema.setText("Scuro");
                btnTema.setIcon(createAdaptiveIcon("icons/moon.svg", 18, 18));
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnTema.setText("Chiaro");
                btnTema.setIcon(createAdaptiveIcon("icons/sun.svg", 18, 18));
            }
            applyThemeStyles();
            FlatLaf.updateUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
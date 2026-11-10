package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.entità.Docente;
import com.quaini_rubini_dellamina.backend.entità.Giorni;
import com.quaini_rubini_dellamina.backend.entità.OrarioLezioni;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ProfessoreOrario extends JFrame {
    private final JLabel lblInfoBottom;
    private final JButton btnTema;
    private final JComboBox<String> comboProf;

    public ProfessoreOrario() {
        super("Orario Professori - Istituto Saraceno Romegialli");

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        Font fontLabel = new Font("SansSerif", Font.BOLD, 14);
        Font fontButton = new Font("SansSerif", Font.BOLD, 13);

        // ====================================================
        // ZONA NORD: HEADER + FILTRO PROFESSORE
        // ====================================================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Orario Professori", createAdaptiveIcon("icons/user.svg", 22, 22), JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro", createAdaptiveIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(fontButton);
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16; borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(btnTema, BorderLayout.EAST);

        JLabel labelProf = new JLabel("Professore:");
        labelProf.setFont(fontLabel);

        // Caricamento dinamico dei docenti dal file CSV tramite ReadCsv.getAllDocenti()
        String[] professori = caricaDocentiDaCSV();
        comboProf = new JComboBox<>(professori);
        comboProf.setPreferredSize(new Dimension(200, 32));

        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO", createWhiteIcon("icons/lens.svg", 16, 16));
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVisualizza.putClientProperty("FlatLaf.style", "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 6,15,6,15; borderWidth: 0; focusWidth: 0");

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        filterPanel.add(labelProf);
        filterPanel.add(comboProf);
        filterPanel.add(btnVisualizza);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formPanel.add(filterPanel);

        topPanel.add(headerPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(formPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ====================================================
        // ZONA CENTRO: TABELLA ORARIO PROFESSORE
        // ====================================================
        Giorni[] giorniValues = Giorni.values();
        String[] columnNames = new String[giorniValues.length + 1];
        columnNames[0] = "ORE";
        for (int i = 0; i < giorniValues.length; i++) {
            columnNames[i + 1] = giorniValues[i].getNome().toUpperCase();
        }

        OrarioLezioni[] orariValues = OrarioLezioni.values();
        Object[][] data = new Object[orariValues.length][columnNames.length];
        for (int i = 0; i < orariValues.length; i++) {
            data[i][0] = orariValues[i].getNome();
            for (int j = 1; j < columnNames.length; j++) {
                data[i][j] = "";
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ====================================================
        // ZONA SUD: PULSANTI E LABEL INFORMATIVA
        // ====================================================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnIndietro = new JButton("Torna Indietro", createAdaptiveIcon("icons/arrow.svg", 16, 16));
        btnIndietro.setFont(fontButton);
        btnIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");

        lblInfoBottom = new JLabel("Seleziona un professore per caricare l'orario");
        lblInfoBottom.setFont(new Font("SansSerif", Font.PLAIN, 13));

        leftBottomPanel.add(btnIndietro);
        leftBottomPanel.add(Box.createHorizontalStrut(10));
        leftBottomPanel.add(lblInfoBottom);

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnPdf = new JButton("SALVA PDF", createWhiteIcon("icons/pdfSave.svg", 16, 16));
        JButton btnCsv = new JButton("SALVA CSV", createWhiteIcon("icons/csvSave.svg", 16, 16));

        btnPdf.setFont(fontButton);
        btnCsv.setFont(fontButton);
        btnPdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCsv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPdf.putClientProperty("FlatLaf.style", "background: #198754; foreground: #ffffff; arc: 10; margin: 8,15,8,15; borderWidth: 0; focusWidth: 0");
        btnCsv.putClientProperty("FlatLaf.style", "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 8,15,8,15; borderWidth: 0; focusWidth: 0");

        rightButtonsPanel.add(btnPdf);
        rightButtonsPanel.add(btnCsv);

        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(HomeWindow::new);
            dispose();
        });

        btnVisualizza.addActionListener(e -> {
            if (comboProf.getSelectedItem() != null) {
                String LettoProf = comboProf.getSelectedItem().toString();
                lblInfoBottom.setText("Professore: " + LettoProf + " • Orario generato");
            }
        });

        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Utilizza la classe ReadCsv per caricare tutti i docenti univoci tramite getAllDocenti().
     */
    private String[] caricaDocentiDaCSV() {
        ReadCsv reader = new ReadCsv();
        try {
            HashSet<Docente> setDocenti = reader.getAllDocenti();
            List<String> nomiDocenti = new ArrayList<>();
            for (Docente d : setDocenti) {
                if (d != null && d.getNome() != null && !d.getNome().isEmpty()) {
                    nomiDocenti.add(d.getNome());
                }
            }
            Collections.sort(nomiDocenti);
            return nomiDocenti.toArray(new String[0]);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei docenti dal CSV: " + e.getMessage());
            return new String[]{"Nessun docente trovato"};
        }
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
            FlatLaf.updateUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
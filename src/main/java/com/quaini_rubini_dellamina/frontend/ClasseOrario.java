package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.entità.Classe;
import com.quaini_rubini_dellamina.backend.entità.Giorni;
import com.quaini_rubini_dellamina.backend.entità.OrarioLezioni;
import com.quaini_rubini_dellamina.backend.entità.PlessoScuola;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClasseOrario extends JFrame {
    private final String plesso;
    private final JComboBox<String> comboIndirizzo;
    private final JComboBox<String> comboAnno;
    private final JComboBox<String> comboChiaveClasse;
    private final JLabel lblInfoBottom;
    private final JButton btnTema;

    public ClasseOrario(String p) {
        super("Orario Classi - Plesso " + p.toUpperCase());
        this.plesso = p;

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        Font fontLabel = new Font("SansSerif", Font.BOLD, 14);
        Font fontButton = new Font("SansSerif", Font.BOLD, 13);

        // ====================================================
        // ZONA NORD: HEADER + FILTRI
        // ====================================================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Header (Pulsante Home rimosso)
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Orario Classi [" + p.toUpperCase() + "]", new FlatSVGIcon("icons/hat.svg", 22, 22), JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

        // Bottone cambio tema stile pillola
        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro",
                new FlatSVGIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(fontButton);
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16; borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(btnTema, BorderLayout.EAST);

        // Filtri
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        JLabel lblAnno = new JLabel("Classe:");
        lblAnno.setFont(fontLabel);
        comboAnno = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        comboAnno.setPreferredSize(new Dimension(60, 32));

        String[] profPlesso = plesso.equalsIgnoreCase("saraceno")
                ? PlessoScuola.plessoSaraceno().toArray(new String[0])
                : PlessoScuola.plessoRomegialli().toArray(new String[0]);
        comboIndirizzo = new JComboBox<>(profPlesso);
        comboIndirizzo.setPreferredSize(new Dimension(85, 32));

        comboChiaveClasse = new JComboBox<>();
        comboChiaveClasse.setPreferredSize(new Dimension(90, 32));

        comboIndirizzo.addActionListener(e -> aggiornaChiaviClasse());
        comboAnno.addActionListener(e -> aggiornaChiaviClasse());
        aggiornaChiaviClasse();

        filterPanel.add(lblAnno);
        filterPanel.add(comboAnno);
        filterPanel.add(comboIndirizzo);
        filterPanel.add(comboChiaveClasse);

        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO", new FlatSVGIcon("icons/lens.svg", 16, 16));
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVisualizza.putClientProperty("FlatLaf.style", "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 6,15,6,15; borderWidth: 0; focusWidth: 0");

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formPanel.add(filterPanel);
        formPanel.add(btnVisualizza);

        topPanel.add(headerPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(formPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ====================================================
        // ZONA CENTRO: TABELLA
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
            public boolean isCellEditable(int row, int column) { return false; }
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
        // ZONA SUD: PULSANTI ESPORTAZIONE E INFO
        // ====================================================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnIndietro = new JButton("Torna Indietro", new FlatSVGIcon("icons/arrow.svg", 16, 16));
        btnIndietro.setFont(fontButton);
        btnIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");
        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(HomeWindow::new);
            dispose();
        });

        lblInfoBottom = new JLabel("Seleziona una classe per caricare i dati");
        lblInfoBottom.setFont(new Font("SansSerif", Font.PLAIN, 13));

        leftBottomPanel.add(btnIndietro);
        leftBottomPanel.add(Box.createHorizontalStrut(10));
        leftBottomPanel.add(lblInfoBottom);

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnPdf = new JButton("SALVA PDF", new FlatSVGIcon("icons/pdfSave.svg", 16, 16));
        JButton btnCsv = new JButton("SALVA CSV", new FlatSVGIcon("icons/csvSave.svg", 16, 16));

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

        btnVisualizza.addActionListener(e -> {
            String classeSelezionata = (String) comboChiaveClasse.getSelectedItem();
            System.out.println("Classe selezionata: " + classeSelezionata);
            lblInfoBottom.setText("Classe: " + classeSelezionata + " • Orario generato");
        });

        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

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
            FlatLaf.updateUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void aggiornaChiaviClasse() {
        comboChiaveClasse.removeAllItems();
        String indirizzo = (String) comboIndirizzo.getSelectedItem();
        String annoStr = (String) comboAnno.getSelectedItem();

        if (indirizzo != null && annoStr != null) {
            int anno = Integer.parseInt(annoStr);
            char[] sezioni = {'A', 'B', 'C', 'D'};
            for (char sez : sezioni) {
                Classe c = new Classe(anno, sez, indirizzo);
                comboChiaveClasse.addItem(c.getChiave());
            }
        }
    }
}
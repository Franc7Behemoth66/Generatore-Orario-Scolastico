package com.quaini_rubini_dellamina.frontend;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClasseOrario extends JFrame {
    public ClasseOrario() {
        super("ORARIO CLASSI ROMEGIALLI");

        // Arrotonda gli angoli della finestra
        getRootPane().putClientProperty("JRootPane.style", "arc: 20");

        // Layout principale a zone (North, Center, South)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        Font fontLabel = new Font("Serif", Font.BOLD, 16);
        Font fontButton = new Font("Serif", Font.BOLD, 14);

        // ====================================================
        // 1. ZONA NORD: Selezione Indirizzo, Classe, Sezione e Tasto
        // ====================================================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Form allineato con GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Riga 1: SELEZIONA INDIRIZZO ---
        JLabel lblIndirizzo = new JLabel("SELEZIONA INDIRIZZO");
        lblIndirizzo.setFont(fontLabel);
        JComboBox<String> comboIndirizzo = new JComboBox<>(new String[]{"SSS", "ITI", "AFM", "CAT"});
        comboIndirizzo.setFont(fontLabel);
        comboIndirizzo.setPreferredSize(new Dimension(160, 30));

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblIndirizzo, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(comboIndirizzo, gbc);

        // --- Riga 2: SELEZIONA CLASSE ---
        JLabel lblClasse = new JLabel("SELEZIONA CLASSE");
        lblClasse.setFont(fontLabel);
        JComboBox<String> comboClasse = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        comboClasse.setFont(fontLabel);
        comboClasse.setPreferredSize(new Dimension(160, 30));

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblClasse, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(comboClasse, gbc);

        // --- Riga 3: SELEZIONA SEZIONE (A, B, C) ---
        JLabel lblSezione = new JLabel("SELEZIONA SEZIONE");
        lblSezione.setFont(fontLabel);
        JComboBox<String> comboSezione = new JComboBox<>(new String[]{"A", "B", "C"});
        comboSezione.setFont(fontLabel);
        comboSezione.setPreferredSize(new Dimension(160, 30));

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblSezione, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(comboSezione, gbc);

        // Pulsante CONFERMA
        JButton btnConferma = new JButton("CONFERMA");
        btnConferma.setFont(fontButton);
        btnConferma.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConferma.putClientProperty("FlatLaf.style", "arc: 12; margin: 6,20,6,20");

        topPanel.add(formPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(btnConferma);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ====================================================
        // 2. ZONA CENTRO: La tabella JTable
        // ====================================================
        String[] columnNames = {"ORE", "LUNEDÌ", "MARTEDÌ", "MERCOLEDÌ", "GIOVEDÌ", "VENERDÌ"};
        Object[][] data = {
                {"8-9", "", "", "", "", ""},
                {"9-10", "", "", "", "", ""},
                {"10-11", "", "", "", "", ""},
                {"11-12", "", "", "", "", ""},
                {"12-13", "", "", "", "", ""},
                {"13-14", "", "", "", "", ""}
        };

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ====================================================
        // 3. ZONA SUD: Pulsanti di gestione
        // ====================================================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton btnIndietro = new JButton("TORNA INDIETRO");
        btnIndietro.setFont(fontButton);
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 12; margin: 6,15,6,15");

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnPdf = new JButton("SALVA PDF");
        JButton btnCsv = new JButton("SALVA CSV");

        btnPdf.setFont(fontButton);
        btnCsv.setFont(fontButton);
        btnPdf.putClientProperty("FlatLaf.style", "arc: 12; margin: 6,15,6,15");
        btnCsv.putClientProperty("FlatLaf.style", "arc: 12; margin: 6,15,6,15");

        rightButtonsPanel.add(btnPdf);
        rightButtonsPanel.add(btnCsv);

        bottomPanel.add(btnIndietro, BorderLayout.WEST);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(HomeWindow::new);
            setVisible(false);
        });

        // Configurazione della finestra
        setSize(750, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProfessoreOrario extends JFrame {
    String plesso;
    public ProfessoreOrario(String p) {
        super("ORARIO PROFESSORI SARACENO");
        this.plesso = p;


        // Arrotonda gli angoli della finestra
        getRootPane().putClientProperty("JRootPane.style", "arc: 20");


        // Layout principale a zone (North, Center, South) con margini
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);


        // Font definitiì
        Font fontLabel = new Font("Serif", Font.BOLD, 16);
        Font fontButton = new Font("Serif", Font.BOLD, 14);


        // ====================================================
        // 1. ZONA NORD: Selezione Professore + Tasto Conferma
        // ====================================================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));


        // Riga della JComboBox
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JLabel labelProf = new JLabel("SELEZIONA PROFESSORE");
        labelProf.setFont(fontLabel);


        String[] professori = {"BARINI", "ROSSI", "BIANCHI", "VERDI"};
        JComboBox<String> comboProf = new JComboBox<>(professori);
        comboProf.setFont(fontLabel);
        comboProf.setPreferredSize(new Dimension(160, 32));


        selectionPanel.add(labelProf);
        selectionPanel.add(comboProf);


        // Pulsante CONFERMA
        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO");
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVisualizza.putClientProperty("FlatLaf.style", "arc: 12; margin: 10,30,10,30");


        topPanel.add(selectionPanel);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(btnVisualizza);


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
        table.setRowHeight(32);
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


        // Pulsante a sinistra
        JButton btnIndietro = new JButton("TORNA INDIETRO");
        btnIndietro.setFont(fontButton);
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 12; margin: 6,15,6,15");


        // Pulsanti a destra
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
            SwingUtilities.invokeLater(com.quaini_rubini_dellamina.frontend.HomeWindow::new);
            setVisible(false);
        });


        btnVisualizza.addActionListener(e -> {
            String LettoProf = comboProf.getSelectedItem().toString();
        });


        setSize(720, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.quaini_rubini_dellamina.backend.entità.PlessoScuola;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClasseOrario extends JFrame {
    String plesso;
    public ClasseOrario(String p) {
        super("ORARIO CLASSI ROMEGIALLI");
        plesso = p;


        // Arrotonda gli angoli della finestra
        getRootPane().putClientProperty("JRootPane.style", "arc: 20");


        // Layout principale a zone (North, Center, South)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);


        // Font definitiì
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


        // --- SELEZIONA CLASSE ---
        JLabel lblClasse = new JLabel("CLASSE: ");
        lblClasse.setFont(fontLabel);
        JComboBox<String> comboClasse = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        comboClasse.setFont(fontLabel);
        comboClasse.setPreferredSize(new Dimension(160, 30));


        // --- SELEZIONA SEZIONE ---
        JComboBox<String> comboSezione = new JComboBox<>(new String[]{"A", "B", "C"});
        comboSezione.setFont(fontLabel);
        comboSezione.setPreferredSize(new Dimension(160, 30));


        // --- SELEZIONA INDIRIZZO ---
        String[] profPlesso = new String[]{};
        if(plesso.equals("saraceno")){
            profPlesso = PlessoScuola.plessoSaraceno().toArray(new String[0]);
        }
        else {
            profPlesso = PlessoScuola.plessoRomegialli().toArray(new String[0]);
        }

        JComboBox<String> comboIndirizzo = new JComboBox<>(profPlesso);
        comboIndirizzo.setFont(fontLabel);
        comboIndirizzo.setPreferredSize(new Dimension(160, 30));


        // ====================================================
        // --- JPANEL CONTENENTE CLASSE, SEZIONE E INDIRIZZO ---
        // ====================================================


        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        northPanel.add(lblClasse);
        northPanel.add(comboClasse);
        northPanel.add(comboSezione);
        northPanel.add(comboIndirizzo);


        formPanel.add(northPanel, gbc);


        // Pulsante CONFERMA
        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO");
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVisualizza.putClientProperty("FlatLaf.style", "arc: 12; margin: 10,30,10,30");


        topPanel.add(formPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(btnVisualizza);


        mainPanel.add(topPanel, BorderLayout.NORTH);


        // ====================================================
        // ZONA CENTRO: TABELLA
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
        // 3. ZONA SUD: Pulsanti
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


        //Action listener bottone torna indietro
        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(com.quaini_rubini_dellamina.frontend.HomeWindow::new);
            setVisible(false);
        });


        //Action listener bottone conferma
        btnVisualizza.addActionListener(e -> {
            String LettaClasse = comboClasse.getSelectedItem().toString();
            String LettaSezione = comboSezione.getSelectedItem().toString();
            String LettoIndirizzo = comboIndirizzo.getSelectedItem().toString();
            System.out.println(LettaClasse+" "+LettoIndirizzo+" "+LettaSezione);
        });


        // Configurazione della finestra
        setSize(720, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

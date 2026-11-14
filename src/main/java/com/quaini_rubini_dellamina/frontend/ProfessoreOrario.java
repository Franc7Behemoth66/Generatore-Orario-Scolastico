package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.OrarioManager;
import com.quaini_rubini_dellamina.backend.algoritmo.GenOrarioVincoli;
import com.quaini_rubini_dellamina.backend.entità.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ProfessoreOrario extends JFrame {

    private final JLabel lblInfoBottom;
    private final JButton btnVincoliProf;
    private final JButton btnTema;
    private final JComboBox<String> comboProf;
    private final DefaultTableModel tableModel;
    private List<Slot> slotsCorrente = null;
    private List<String> segnalazioniProfCorrente = Collections.emptyList();
    private Preferenza prefCorrente = null;

    public ProfessoreOrario() {
        super("Orario Professori - Istituto Saraceno Romegialli");

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        Font fontLabel  = new Font("SansSerif", Font.BOLD, 14);
        Font fontButton = new Font("SansSerif", Font.BOLD, 13);

        // ====================================================
        // ZONA NORD: HEADER + FILTRO PROFESSORE
        // ====================================================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Orario Professori",
                createAdaptiveIcon("icons/user.svg", 22, 22), JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro",
                createAdaptiveIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(fontButton);
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16; borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(btnTema, BorderLayout.EAST);

        JLabel labelProf = new JLabel("Professore:");
        labelProf.setFont(fontLabel);

        String[] professori = caricaDocenti();
        comboProf = new JComboBox<>(professori);
        comboProf.setPreferredSize(new Dimension(220, 32));

        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO",
                createWhiteIcon("icons/lens.svg", 16, 16));
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVisualizza.putClientProperty("FlatLaf.style",
                "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 6,15,6,15; borderWidth: 0; focusWidth: 0");

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
        // ZONA CENTRO: TABELLA ORARIO
        // ====================================================
        Giorni[] giorniValues = Giorni.values();
        String[] columnNames = new String[giorniValues.length + 1];
        columnNames[0] = "ORE";
        for (int i = 0; i < giorniValues.length; i++)
            columnNames[i + 1] = giorniValues[i].getNome().toUpperCase();

        OrarioLezioni[] orariValues = OrarioLezioni.values();
        Object[][] data = new Object[orariValues.length][columnNames.length];
        for (int i = 0; i < orariValues.length; i++) {
            data[i][0] = orariValues[i].getNome();
            for (int j = 1; j < columnNames.length; j++) data[i][j] = "";
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(58);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (column == 0) {
                    c.setBackground(table.getTableHeader().getBackground());
                    c.setForeground(table.getTableHeader().getForeground());
                    setFont(getFont().deriveFont(Font.BOLD));
                    return c;
                }

                setFont(getFont().deriveFont(Font.PLAIN));

                if (value != null && !value.toString().trim().isEmpty()) {
                    boolean isDark = FlatLaf.isLafDark();
                    boolean cellaViolaVincolo = isCellaViolata(row, column - 1);

                    if (cellaViolaVincolo) {
                        c.setBackground(isDark ? new Color(150, 35, 45) : new Color(255, 205, 210));
                        c.setForeground(isDark ? Color.WHITE : new Color(120, 10, 20));
                    } else {
                        c.setBackground(isDark ? new Color(0, 70, 140) : new Color(215, 235, 255));
                        c.setForeground(isDark ? Color.WHITE : new Color(10, 40, 80));
                    }
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }

                return c;
            }
        });

        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ====================================================
        // ZONA SUD: BOTTONI ESPORTAZIONE + LABEL INFO + VINCOLI
        // ====================================================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnIndietro = new JButton("Torna Indietro",
                createAdaptiveIcon("icons/arrow.svg", 16, 16));
        btnIndietro.setFont(fontButton);
        btnIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");

        lblInfoBottom = new JLabel("Seleziona un professore per caricare l'orario");
        lblInfoBottom.setFont(new Font("SansSerif", Font.PLAIN, 13));

        btnVincoliProf = new JButton("⚠ Vincoli");
        btnVincoliProf.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVincoliProf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVincoliProf.putClientProperty("FlatLaf.style",
                "background: #dc3545; foreground: #ffffff; arc: 10; margin: 6,12,6,12; borderWidth: 0");
        btnVincoliProf.setVisible(false);
        btnVincoliProf.addActionListener(e -> {
            if (!segnalazioniProfCorrente.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        String.join("\n• ", segnalazioniProfCorrente),
                        "Vincoli / Segnalazioni Docente",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        leftBottomPanel.add(btnIndietro);
        leftBottomPanel.add(Box.createHorizontalStrut(8));
        leftBottomPanel.add(lblInfoBottom);
        leftBottomPanel.add(btnVincoliProf);

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnPdf = new JButton("SALVA PDF", createWhiteIcon("icons/pdfSave.svg", 16, 16));
        JButton btnCsv = new JButton("SALVA CSV", createWhiteIcon("icons/csvSave.svg", 16, 16));
        btnPdf.setFont(fontButton);
        btnCsv.setFont(fontButton);
        btnPdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCsv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPdf.putClientProperty("FlatLaf.style",
                "background: #198754; foreground: #ffffff; arc: 10; margin: 8,15,8,15; borderWidth: 0; focusWidth: 0");
        btnCsv.putClientProperty("FlatLaf.style",
                "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 8,15,8,15; borderWidth: 0; focusWidth: 0");

        rightButtonsPanel.add(btnPdf);
        rightButtonsPanel.add(btnCsv);

        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(HomeWindow::new);
            dispose();
        });

        // ====================================================
        // LISTENER – VISUALIZZA ORARIO
        // ====================================================
        btnVisualizza.addActionListener(e -> {
            if (comboProf.getSelectedItem() == null) return;
            String nomeProf = comboProf.getSelectedItem().toString();

            OrarioManager mgr = OrarioManager.getInstance();
            slotsCorrente = mgr.getOrarioDocente(nomeProf);
            prefCorrente = mgr.getPreferenzaDocente(nomeProf);

            GenOrarioVincoli.RisultatoVerificaDocente analisi = mgr.getAnalisiDocente(nomeProf);
            segnalazioniProfCorrente = (analisi != null)
                    ? analisi.getElencoSegnalazioni()
                    : Collections.emptyList();

            for (int row = 0; row < tableModel.getRowCount(); row++)
                for (int col = 1; col < tableModel.getColumnCount(); col++)
                    tableModel.setValueAt("", row, col);

            for (Slot slot : slotsCorrente) {
                if (slot == null || !slot.isAssegnato() || slot.isVuoto() || slot.getLezione() == null)
                    continue;
                int ora    = slot.indexOra();
                int giorno = slot.indexGiorno();
                Lezione l = slot.getLezione();
                String chiaveClasse = l.getClasse() != null ? l.getClasse().getChiave() : "";

                String testoCella = chiaveClasse;
                if (l.hasCompresenza()) {
                    String collega = l.getDocente().getNome().equalsIgnoreCase(nomeProf)
                            ? l.getDocenteCompresenza().getNome()
                            : l.getDocente().getNome();
                    testoCella = "<html><center>" + chiaveClasse
                            + "<br><small>(comp. " + collega + ")</small></center></html>";
                }
                tableModel.setValueAt(testoCella, ora, giorno + 1);
            }

            int totOre = (int) slotsCorrente.stream()
                    .filter(s -> s != null && !s.isVuoto() && s.getLezione() != null)
                    .count();

            if (segnalazioniProfCorrente.isEmpty()) {
                lblInfoBottom.setText("Professore: " + nomeProf + " • " + totOre + " ore settimanali");
                lblInfoBottom.setForeground(UIManager.getColor("Label.foreground"));
                btnVincoliProf.setVisible(false);
            } else {
                lblInfoBottom.setText("Professore: " + nomeProf + " • " + totOre + " ore");
                btnVincoliProf.setText("⚠ Vincoli (" + segnalazioniProfCorrente.size() + ")");
                btnVincoliProf.setVisible(true);
            }

            table.repaint();
        });

        btnPdf.addActionListener(e -> {
            if (slotsCorrente == null || slotsCorrente.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Visualizza prima un orario.", "Nessun orario", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Scegli la cartella di destinazione");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String nome = comboProf.getSelectedItem().toString();
                    OrarioManager.getInstance().esportaPdfDocente(nome, fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "PDF salvato correttamente.", "Salvato", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Errore nel salvataggio PDF:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCsv.addActionListener(e -> {
            if (slotsCorrente == null || slotsCorrente.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Visualizza prima un orario.", "Nessun orario", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Scegli la cartella di destinazione");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String nome = comboProf.getSelectedItem().toString();
                    OrarioManager.getInstance().esportaCsvDocente(nome, fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "CSV salvato correttamente.", "Salvato", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Errore nel salvataggio CSV:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setSize(980, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private boolean isCellaViolata(int ora, int giorno) {
        if (prefCorrente == null) return false;
        int gLibero = GenOrarioVincoli.indiceGiorno(prefCorrente.giornoLibero());
        if (gLibero >= 0 && gLibero == giorno) return true;
        if (prefCorrente.noPrimaH() && ora == 0) return true;
        if (prefCorrente.noUltimaH() && ora == (Slot.ORE_GIORNO - 1)) return true;
        if (prefCorrente.noUltimaH() && ora == GenOrarioVincoli.ultimaOraGiorno(giorno)) return true;
        return false;
    }

    private String[] caricaDocenti() {
        try {
            return OrarioManager.getInstance().getDocenti().stream()
                    .map(Docente::getNome)
                    .sorted()
                    .toArray(String[]::new);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento docenti: " + e.getMessage());
            return new String[]{"Nessun docente disponibile"};
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

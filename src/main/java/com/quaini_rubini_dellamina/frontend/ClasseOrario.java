package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.OrarioManager;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.ColoriOrario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ClasseOrario extends JFrame {

    private final String plesso;
    private final JComboBox<String> comboIndirizzo;
    private final JComboBox<String> comboAnno;
    private final JComboBox<String> comboChiaveClasse;
    private final JLabel lblInfoBottom;
    private final JButton btnErroriClasse;
    private final JButton btnTema;
    private final DefaultTableModel tableModel;
    private List<Slot> slotsCorrente = null;
    private List<String> erroriClasseCorrente = Collections.emptyList();
    private Map<String, Color> mappaColori = Collections.emptyMap();

    public ClasseOrario(String p) {
        super("Orario Classi - Plesso " + p.toUpperCase());
        this.plesso = p;

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        Font fontLabel  = new Font("SansSerif", Font.BOLD, 14);
        Font fontButton = new Font("SansSerif", Font.BOLD, 13);

        // ZONA NORD: HEADER E FILTRI
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel headerPanel = new JPanel(new BorderLayout());
        FlatSVGIcon hatIcon = createAdaptiveIcon("icons/hat.svg", 22, 22);
        JLabel lblTitle = new JLabel("Orario Classi [" + p.toUpperCase() + "]", hatIcon, JLabel.CENTER);
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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        JLabel lblAnno = new JLabel("Classe:");
        lblAnno.setFont(fontLabel);
        comboAnno = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        comboAnno.setPreferredSize(new Dimension(60, 32));

        String[] indirizziPlesso = getIndirizziPerPlesso();
        comboIndirizzo = new JComboBox<>(indirizziPlesso);
        comboIndirizzo.setPreferredSize(new Dimension(85, 32));

        comboChiaveClasse = new JComboBox<>();
        comboChiaveClasse.setPreferredSize(new Dimension(110, 32));

        comboIndirizzo.addActionListener(e -> aggiornaChiaviClasse());
        comboAnno.addActionListener(e -> aggiornaChiaviClasse());
        aggiornaChiaviClasse();

        filterPanel.add(lblAnno);
        filterPanel.add(comboAnno);
        filterPanel.add(comboIndirizzo);
        filterPanel.add(comboChiaveClasse);

        JButton btnVisualizza = new JButton("VISUALIZZA ORARIO",
                createWhiteIcon("icons/lens.svg", 16, 16));
        btnVisualizza.setFont(fontButton);
        btnVisualizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVisualizza.putClientProperty("FlatLaf.style",
                "background: #0d6efd; foreground: #ffffff; arc: 10; margin: 6,15,6,15; borderWidth: 0; focusWidth: 0");

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formPanel.add(filterPanel);
        formPanel.add(btnVisualizza);

        topPanel.add(headerPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(formPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ZONA CENTRO: TABELLA ORARIO
        Giorni[] giorniValues = Giorni.values();
        String[] columnNames = new String[giorniValues.length + 1];
        columnNames[0] = "ORE";
        for (int i = 0; i < giorniValues.length; i++)
            columnNames[i + 1] = giorniValues[i].getNome().toUpperCase();

        OrarioLezioni[] orariValues = OrarioLezioni.values();
        Object[][] data = new Object[orariValues.length][columnNames.length];
        for (int i = 0; i < orariValues.length; i++) {
            data[i][0] = orariValues[i].getNome();
            String valDefault = (i == OrarioLezioni.PAUSA.ordinal()) ? "PAUSA" : "";
            for (int j = 1; j < columnNames.length; j++) data[i][j] = valDefault;
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(0).setMinWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(160);
        }
        table.setRowHeight(50);
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

                if (row == OrarioLezioni.PAUSA.ordinal()) {
                    boolean isDark = FlatLaf.isLafDark();
                    c.setBackground(isDark ? new Color(55, 55, 60) : new Color(232, 232, 236));
                    c.setForeground(isDark ? new Color(180, 180, 185) : new Color(100, 100, 105));
                    setFont(getFont().deriveFont(Font.ITALIC));
                    return c;
                }

                setFont(getFont().deriveFont(Font.PLAIN));

                if ((value != null && !value.toString().trim().isEmpty() && slotsCorrente != null)) {
                    String testo = value.toString();

                    String materia = testo;
                    if (testo.contains("<br>")) {
                        materia = testo.replaceAll("<html><center>", "")
                                .split("<br>")[0]
                                .trim();
                    }

                    Color coloreMateria = mappaColori.get(materia);
                    if (coloreMateria != null) {
                        c.setBackground(coloreMateria);
                        c.setForeground(new Color(30, 30, 30));
                    } else {
                        boolean isDark = FlatLaf.isLafDark();
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

        // ZONA SUD: BOTTONI ESPORTAZIONE E INFO
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnIndietro = new JButton("Torna Indietro",
                createAdaptiveIcon("icons/arrow.svg", 16, 16));
        btnIndietro.setFont(fontButton);
        btnIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");
        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(HomeWindow::new);
            dispose();
        });

        lblInfoBottom = new JLabel("Seleziona una classe per caricare i dati");
        lblInfoBottom.setFont(new Font("SansSerif", Font.PLAIN, 13));

        btnErroriClasse = new JButton("⚠ Dettagli Errori");
        btnErroriClasse.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnErroriClasse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnErroriClasse.putClientProperty("FlatLaf.style",
                "background: #dc3545; foreground: #ffffff; arc: 10; margin: 6,12,6,12; borderWidth: 0");
        btnErroriClasse.setVisible(false);
        btnErroriClasse.addActionListener(e -> {
            if (!erroriClasseCorrente.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        String.join("\n• ", erroriClasseCorrente),
                        "Errori Orario Classe",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        leftBottomPanel.add(btnIndietro);
        leftBottomPanel.add(Box.createHorizontalStrut(8));
        leftBottomPanel.add(lblInfoBottom);
        leftBottomPanel.add(btnErroriClasse);

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

        // GESTIONE EVENTI E SALVATAGGIO
        btnVisualizza.addActionListener(e -> {
            String chiaveClasse = (String) comboChiaveClasse.getSelectedItem();
            if (chiaveClasse == null) {
                lblInfoBottom.setText("Nessuna classe disponibile per i filtri selezionati.");
                btnErroriClasse.setVisible(false);
                return;
            }

            slotsCorrente = OrarioManager.getInstance().getOrarioClasse(chiaveClasse);
            erroriClasseCorrente = OrarioManager.getInstance().getErroriClasse(chiaveClasse);

            mappaColori = ColoriOrario.buildColoriMaterie(
                    slotsCorrente.stream()
                            .filter(s -> s != null && s.getLezione() != null && s.getLezione().getMateria() != null)
                            .toList());

            int rigaPausa = OrarioLezioni.PAUSA.ordinal();
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                String valDefault = (row == rigaPausa) ? "PAUSA" : "";
                for (int col = 1; col < tableModel.getColumnCount(); col++)
                    tableModel.setValueAt(valDefault, row, col);
            }

            for (Slot slot : slotsCorrente) {
                if (slot == null || !slot.isAssegnato() || slot.isVuoto() || slot.getLezione() == null)
                    continue;
                int ora    = slot.indexOra();
                int giorno = slot.indexGiorno();
                Lezione l  = slot.getLezione();
                String materia = l.getMateria() != null ? l.getMateria().getNome() : "";
                String docenti = l.getNomiDocenti();
                String testoCella = "<html><center>" + materia + "<br>(" + docenti + ")</center></html>";

                tableModel.setValueAt(testoCella, ora, giorno + 1);
            }

            int totOre = (int) slotsCorrente.stream()
                    .filter(s -> s != null && !s.isVuoto() && s.getLezione() != null)
                    .count();

            if (erroriClasseCorrente.isEmpty()) {
                lblInfoBottom.setText("Classe: " + chiaveClasse + " • " + totOre + " ore settimanali");
                lblInfoBottom.setForeground(UIManager.getColor("Label.foreground"));
                btnErroriClasse.setVisible(false);
            } else {
                lblInfoBottom.setText("Classe: " + chiaveClasse + " • " + totOre + " ore (⚠ " + erroriClasseCorrente.size() + " errori)");
                lblInfoBottom.setForeground(new Color(220, 53, 69));
                btnErroriClasse.setText("⚠ Errori (" + erroriClasseCorrente.size() + ")");
                btnErroriClasse.setVisible(true);
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
                    String chiave = (String) comboChiaveClasse.getSelectedItem();
                    OrarioManager.getInstance().esportaPdfClasse(chiave, fc.getSelectedFile().getAbsolutePath());
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
                    String chiave = (String) comboChiaveClasse.getSelectedItem();
                    OrarioManager.getInstance().esportaCsvClasse(chiave, fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "CSV salvato correttamente.", "Salvato", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Errore nel salvataggio CSV:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setSize(1080, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private String[] getIndirizziPerPlesso() {
        List<String> indirizziPlesso = plesso.equalsIgnoreCase("saraceno")
                ? PlessoScuola.plessoSaraceno()
                : PlessoScuola.plessoRomegialli();

        Set<String> presenti = new HashSet<>();
        for (Classe c : OrarioManager.getInstance().getClassi())
            presenti.add(c.getIndirizzo().toUpperCase());

        List<String> filtrati = new ArrayList<>();
        for (String ind : indirizziPlesso)
            if (presenti.contains(ind.toUpperCase())) filtrati.add(ind);

        return filtrati.isEmpty()
                ? indirizziPlesso.toArray(new String[0])
                : filtrati.toArray(new String[0]);
    }

    private void aggiornaChiaviClasse() {
        comboChiaveClasse.removeAllItems();
        String indirizzo = (String) comboIndirizzo.getSelectedItem();
        String annoStr   = (String) comboAnno.getSelectedItem();
        if (indirizzo == null || annoStr == null) return;

        int anno = Integer.parseInt(annoStr);
        List<String> chiaviFiltrate = new ArrayList<>();

        for (Classe c : OrarioManager.getInstance().getClassi()) {
            if (c.getAnno() == anno && c.getIndirizzo().equalsIgnoreCase(indirizzo))
                chiaviFiltrate.add(c.getChiave());
        }
        Collections.sort(chiaviFiltrate);
        for (String chiave : chiaviFiltrate) comboChiaveClasse.addItem(chiave);
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
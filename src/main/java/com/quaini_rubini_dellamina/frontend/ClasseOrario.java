package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.OrarioManager;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.ColoriOrario;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClasseOrario extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ClasseOrario.class.getName());

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

        // null come prima voce: filtro nessun anno
        comboAnno = new JComboBox<>(new String[]{null, "1", "2", "3", "4", "5"});
        comboAnno.setPreferredSize(new Dimension(60, 32));

        // null come prima voce: filtro nessun indirizzo
        String[] indirizziPlesso = getIndirizziPerPlesso();
        String[] indirizziConVuoto = new String[indirizziPlesso.length + 1];
        System.arraycopy(indirizziPlesso, 0, indirizziConVuoto, 1, indirizziPlesso.length);
        comboIndirizzo = new JComboBox<>(indirizziConVuoto);
        comboIndirizzo.setPreferredSize(new Dimension(110, 32));

        comboChiaveClasse = new JComboBox<>();
        comboChiaveClasse.setPreferredSize(new Dimension(110, 32));
        AutoCompleteDecorator.decorate(comboChiaveClasse);

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
            for (int j = 1; j < columnNames.length; j++) data[i][j] = "";
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
        table.setRowHeight(48);
        table.setRowHeight(OrarioLezioni.PAUSA.ordinal(), 26);
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

                if (row == OrarioLezioni.PAUSA.ordinal() && value != null && !value.toString().trim().isEmpty()) {
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
                JOptionPane.showMessageDialog(this,
                        "Nessuna classe trovata per l'anno e l'indirizzo selezionati.",
                        "Nessuna Classe",
                        JOptionPane.INFORMATION_MESSAGE);
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
                for (int col = 1; col < tableModel.getColumnCount(); col++)
                    tableModel.setValueAt("", row, col);
            }

            boolean[] haPomeriggio = new boolean[Giorni.values().length];

            for (Slot slot : slotsCorrente) {
                if (slot == null || !slot.isAssegnato() || slot.isVuoto() || slot.getLezione() == null)
                    continue;
                int ora    = slot.indexOra();
                int giorno = slot.indexGiorno();
                Lezione l  = slot.getLezione();
                String materia = l.getMateria() != null ? l.getMateria().getNome() : "";
                String docenti = l.getAllDocenti().stream()
                        .map(Docente::getNome)
                        .collect(Collectors.joining("<br>"));
                String testoCella = "<html><center>" + materia + "<br>(" + docenti + ")</center></html>";

                tableModel.setValueAt(testoCella, ora, giorno + 1);

                if (ora > rigaPausa && giorno >= 0 && giorno < haPomeriggio.length) {
                    haPomeriggio[giorno] = true;
                }
            }

            for (int g = 0; g < haPomeriggio.length; g++) {
                if (haPomeriggio[g]) {
                    tableModel.setValueAt("PAUSA", rigaPausa, g + 1);
                }
            }

            for (int row = 0; row < table.getRowCount(); row++) {
                if (row == rigaPausa) {
                    table.setRowHeight(row, 26);
                    continue;
                }
                int maxRighe = 1;
                for (int col = 1; col < table.getColumnCount(); col++) {
                    Object val = table.getValueAt(row, col);
                    if (val != null && !val.toString().isEmpty()) {
                        maxRighe = Math.max(maxRighe, val.toString().split("<br>").length);
                    }
                }
                table.setRowHeight(row, Math.max(46, maxRighe * 17));
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

        setSize(1120, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private String[] getIndirizziPerPlesso() {
        List<String> indirizziPlesso = plesso.equalsIgnoreCase("Saraceno")
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

        // null = filtro non impostato, quindi quel criterio non restringe la lista
        Integer anno = annoStr == null ? null : Integer.valueOf(annoStr);
        List<String> chiaviFiltrate = new ArrayList<>();

        for (Classe c : OrarioManager.getInstance().getClassi()) {
            boolean okPlesso    = plesso.equalsIgnoreCase(PlessoScuola.getPlesso(c.getIndirizzo()));
            boolean okAnno      = anno == null || c.getAnno() == anno;
            boolean okIndirizzo = indirizzo == null || indirizzo.equalsIgnoreCase(c.getIndirizzo()) ;
            if (okAnno && okIndirizzo && okPlesso)
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
            LOGGER.log(Level.WARNING, "Errore durante il cambio del tema", ex);
        }
    }
}
package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.quaini_rubini_dellamina.backend.OrarioManager;
import com.quaini_rubini_dellamina.backend.entità.ConfiguraVincoli;
import com.quaini_rubini_dellamina.backend.entità.Docente;
import com.quaini_rubini_dellamina.backend.entità.Giorni;
import com.quaini_rubini_dellamina.backend.entità.Preferenza;
import com.quaini_rubini_dellamina.backend.io.ReadVincoliJSON;
import com.quaini_rubini_dellamina.backend.io.WriteOnJSON;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelezionaPreferenze extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(SelezionaPreferenze.class.getName());

    private final JButton btnTema;
    private final JPanel cardForm;
    private final JComboBox<String> comboDocente;
    private final JComboBox<String> comboGiornoLibero;
    private final JSpinner spinnerLimiteOre;
    private final JCheckBox chkNoPrimaOra;
    private final JCheckBox chkNoUltimaOra;
    private final JCheckBox chkRiduciOreBuche;

    public SelezionaPreferenze() {
        super("Preferenze Docenti - Istituto Saraceno Romegialli");

        getRootPane().putClientProperty("JRootPane.style", "arc: 18");

        JPanel mainPanel = new JPanel(new BorderLayout(15, 20));
        mainPanel.setBorder(new EmptyBorder(20, 28, 20, 28));
        setContentPane(mainPanel);

        Font fontLabel  = new Font("SansSerif", Font.BOLD, 14);
        Font fontButton = new Font("SansSerif", Font.BOLD, 13);

        // INTESTAZIONE E PULSANTE CAMBIO TEMA
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Preferenze e Vincoli Docente",
                createAdaptiveIcon("icons/user.svg", 24, 24), JLabel.LEFT);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setIconTextGap(10);

        boolean isDark = FlatLaf.isLafDark();
        btnTema = new JButton(isDark ? "Chiaro" : "Scuro",
                createAdaptiveIcon(isDark ? "icons/sun.svg" : "icons/moon.svg", 18, 18));
        btnTema.setFont(fontButton);
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16; borderWidth: 1");
        btnTema.addActionListener(e -> toggleTheme());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnTema, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        //SELEZIONE PREFERENZE E VINCOLI
        cardForm = new JPanel(new BorderLayout(0, 16));

// Griglia etichetta | campo
        JPanel campi = new JPanel(new GridLayout(3, 2, 12, 12));
        campi.setOpaque(false);

        JLabel lblDocente = new JLabel("Docente:", createAdaptiveIcon("icons/user.svg", 16, 16), JLabel.LEFT);
        lblDocente.setFont(fontLabel);
        lblDocente.setIconTextGap(8);
        comboDocente = new JComboBox<>(caricaDocenti());
        AutoCompleteDecorator.decorate(comboDocente); // per fare in modo che si possa cercare il nome del docente nella combo

        JLabel lblGiorno = new JLabel("Giorno libero:", createAdaptiveIcon("icons/calendar.svg", 16, 16), JLabel.LEFT);
        lblGiorno.setFont(fontLabel);
        lblGiorno.setIconTextGap(8);
        String[] giorni = {"Nessuno", Giorni.LUNEDI.getNome(), Giorni.MARTEDI.getNome(), Giorni.MERCOLEDI.getNome(),
                Giorni.GIOVEDI.getNome(), Giorni.VENERDI.getNome()};
        comboGiornoLibero = new JComboBox<>(giorni);

        JLabel lblLimiteOre = new JLabel("Limite ore giornaliere (3 - 8):",
                createAdaptiveIcon("icons/school.svg", 16, 16), JLabel.LEFT);
        lblLimiteOre.setFont(fontLabel);
        lblLimiteOre.setIconTextGap(8);
        spinnerLimiteOre = new JSpinner(new SpinnerNumberModel(8, 3, 8, 1));
        spinnerLimiteOre.setFont(new Font("SansSerif", Font.BOLD, 14));

        campi.add(lblDocente);
        campi.add(comboDocente);
        campi.add(lblGiorno);
        campi.add(comboGiornoLibero);
        campi.add(lblLimiteOre);
        campi.add(spinnerLimiteOre);

// Checkbox in colonna
        chkNoPrimaOra = new JCheckBox("Evita la prima ora (no-prima-h)");
        chkNoPrimaOra.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chkNoPrimaOra.setOpaque(false);

        chkNoUltimaOra = new JCheckBox("Evita l'ultima ora (no-ultima-h)");
        chkNoUltimaOra.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chkNoUltimaOra.setOpaque(false);

        chkRiduciOreBuche = new JCheckBox("Riduci ore buche (riduci-ore-buche)");
        chkRiduciOreBuche.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chkRiduciOreBuche.setOpaque(false);

        JPanel vincoli = new JPanel(new GridLayout(0, 1, 0, 6));
        vincoli.setOpaque(false);
        vincoli.add(chkNoPrimaOra);
        vincoli.add(chkNoUltimaOra);
        vincoli.add(chkRiduciOreBuche);

        cardForm.add(campi, BorderLayout.NORTH);
        cardForm.add(vincoli, BorderLayout.CENTER);

        mainPanel.add(cardForm, BorderLayout.CENTER);

        // BARRA INFERIORE CON PULSANTI DI AZIONE
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton btnIndietro = new JButton("Torna Indietro", createAdaptiveIcon("icons/arrow.svg", 16, 16));
        btnIndietro.setFont(fontButton);
        btnIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIndietro.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,16,8,16");

        JButton btnApplica = new JButton("APPLICA", createWhiteIcon("icons/lens.svg", 16, 16));
        btnApplica.setFont(fontButton);
        btnApplica.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApplica.putClientProperty("FlatLaf.style",
                "background: #198754; foreground: #ffffff; arc: 10; margin: 8,20,8,20; borderWidth: 0; focusWidth: 0");

        bottomPanel.add(btnIndietro, BorderLayout.WEST);
        bottomPanel.add(btnApplica, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        //  ACTION LISTENER
        comboDocente.addActionListener(e -> caricaPreferenzeEsistenti());
        caricaPreferenzeEsistenti();

        btnIndietro.addActionListener(e -> {
            SwingUtilities.invokeLater(ProfessoreOrario::new);
            dispose();
        });

        btnApplica.addActionListener(e -> {
            if (comboDocente.getSelectedItem() == null || "Nessun docente disponibile".equals(comboDocente.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this,
                        "Nessun docente selezionato o disponibile.",
                        "Selezione Richiesta",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String docente = comboDocente.getSelectedItem().toString();
            String giornoScelto = comboGiornoLibero.getSelectedItem().toString();
            String giornoLibero = giornoScelto.equals("Nessuno") ? "" : giornoScelto;
            int limiteH = (Integer) spinnerLimiteOre.getValue();
            boolean noPrimaH = chkNoPrimaOra.isSelected();
            boolean noUltimaH = chkNoUltimaOra.isSelected();
            boolean riduciOreBuche = chkRiduciOreBuche.isSelected();

            Preferenza nuovaPreferenza = new Preferenza(
                    docente,
                    giornoLibero,
                    limiteH,
                    noPrimaH,
                    noUltimaH,
                    riduciOreBuche
            );

            try {
                WriteOnJSON.salva(nuovaPreferenza);
                try {
                    OrarioManager.getInstance().caricaDataset();
                } catch (Exception reloadEx) {
                    LOGGER.log(Level.WARNING, "Errore ricaricamento orario post salvataggio preferenze", reloadEx);
                    JOptionPane.showMessageDialog(this,
                            "Preferenze salvate, ma si è verificato un errore durante l'aggiornamento dell'orario:\n" + reloadEx.getMessage(),
                            "Avviso Ricaricamento",
                            JOptionPane.WARNING_MESSAGE);
                }
                JOptionPane.showMessageDialog(this,
                        "Preferenze salvate nel file JSON per " + docente + "!",
                        "Preferenze Salvate",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Errore durante il salvataggio preferenze", ex);
                JOptionPane.showMessageDialog(this,
                        "Errore durante il salvataggio nel file JSON:\n" + ex.getMessage(),
                        "Errore Scrittura JSON",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        applyThemeStyles();

        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // LETTURA E CARICAMENTO DATI (DOCENTI E PREFERENZE)
    private void caricaPreferenzeEsistenti() {
        if (comboDocente.getSelectedItem() == null) return;
        String nomeProf = comboDocente.getSelectedItem().toString();
        Preferenza pref = OrarioManager.getInstance().getPreferenzaDocente(nomeProf);

        if (pref == null) {
            try {
                ConfiguraVincoli cfg = ReadVincoliJSON.leggi();
                if (cfg != null && cfg.preferenze() != null) {
                    pref = cfg.preferenze().stream()
                            .filter(p -> p.docente().equalsIgnoreCase(nomeProf))
                            .findFirst()
                            .orElse(null);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Impossibile leggere file vincoli JSON per il docente " + nomeProf, e);
            }
        }

        if (pref != null) {
            if (pref.giornoLibero() == null || pref.giornoLibero().isBlank()) {
                comboGiornoLibero.setSelectedItem("Nessuno");
            } else {
                comboGiornoLibero.setSelectedItem(pref.giornoLibero());
            }
            int ore = Math.max(3, Math.min(8, pref.limiteH()));
            spinnerLimiteOre.setValue(ore);
            chkNoPrimaOra.setSelected(pref.noPrimaH());
            chkNoUltimaOra.setSelected(pref.noUltimaH());
            chkRiduciOreBuche.setSelected(pref.riduciOreBuche());
        } else {
            comboGiornoLibero.setSelectedItem("Nessuno");
            spinnerLimiteOre.setValue(8);
            chkNoPrimaOra.setSelected(false);
            chkNoUltimaOra.setSelected(false);
            chkRiduciOreBuche.setSelected(false);
        }
    }

    private String[] caricaDocenti() {
        try {
            List<Docente> docenti = OrarioManager.getInstance().getDocenti();
            if (docenti.isEmpty()) {
                return new String[]{"Nessun docente disponibile"};
            }
            return docenti.stream()
                    .map(Docente::getNome)
                    .sorted()
                    .toArray(String[]::new);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento docenti", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Errore durante il caricamento dell'elenco docenti:\n" + e.getMessage(),
                    "Errore Docenti", JOptionPane.ERROR_MESSAGE));
            return new String[]{"Nessun docente disponibile"};
        }
    }

    // GESTIONE ICONE E TEMI (CHIARO / SCURO)
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

    private void applyThemeStyles() {
        boolean isDark = FlatLaf.isLafDark();
        Color bgColor     = isDark ? new Color(0x2b, 0x2b, 0x2e) : new Color(0xe8, 0xe8, 0xee);
        Color borderColor = isDark ? new Color(0x3a, 0x3a, 0x3d) : new Color(0xc8, 0xc8, 0xd0);
        String cardStyle  = "arc: 16; background: " + String.format("#%06x", bgColor.getRGB() & 0xFFFFFF);

        if (cardForm != null) {
            cardForm.putClientProperty("FlatLaf.style", cardStyle);
            cardForm.setBackground(bgColor);
            cardForm.setBorder(new CompoundBorder(
                    new LineBorder(borderColor, 1, true),
                    new EmptyBorder(15, 20, 15, 20)));
        }
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
            LOGGER.log(Level.WARNING, "Errore durante il cambio del tema", ex);
        }
    }
}
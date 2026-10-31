package com.quaini_rubini_dellamina.frontend;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

public class HomeWindow extends JFrame {
    public HomeWindow() {
        super("scegli che tipo di orario vuoi vedere");


        // Arrotonda gli angoli della finestra
        getRootPane().putClientProperty("JRootPane.style", "arc: 20");


        // 2. Container principale con layout verticale e margini esterni (padding)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25)); // Spazio dai bordi della finestra
        setContentPane(mainPanel);


        // Font definiti
        Font titleFont = new Font("Serif", Font.BOLD, 18);
        Font radioFont = new Font("Serif", Font.BOLD, 18);
        Font buttonFont = new Font("Serif", Font.BOLD, 15);


        // --- SEZIONE 1: PLESSO ---
        JLabel l2 = new JLabel("DI CHE PLESSO DESIDERI VEDERE L'ORARIO");
        l2.setFont(titleFont);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);


        JRadioButton saraceno = new JRadioButton("SARACENO");
        saraceno.setFont(radioFont);
        JRadioButton romegialli = new JRadioButton("ROMEGIALLI");
        romegialli.setFont(radioFont);


        ButtonGroup gruppo1 = new ButtonGroup();
        gruppo1.add(saraceno);
        gruppo1.add(romegialli);


        // Pannello per i RadioButton
        JPanel p2 = createCardPanel();
        p2.add(saraceno);
        p2.add(Box.createHorizontalStrut(25));
        p2.add(romegialli);


        // --- SEZIONE 2: TIPO ORARIO ---
        JLabel l1 = new JLabel("CHE ORARIO VUOI VEDERE");
        l1.setFont(titleFont);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);


        JRadioButton professori = new JRadioButton("PROFESSORI");
        professori.setFont(radioFont);
        JRadioButton classi = new JRadioButton("CLASSI");
        classi.setFont(radioFont);


        ButtonGroup gruppo2 = new ButtonGroup();
        gruppo2.add(professori);
        gruppo2.add(classi);


        JPanel p1 = createCardPanel();
        p1.add(professori);
        p1.add(Box.createHorizontalStrut(25));
        p1.add(classi);


        // --- SEZIONE 3: BOTTONI DI AZIONE ---
        JButton cerca = new JButton("CERCA ORARIO");
        JButton carica = new JButton("CARICA CSV");


        cerca.setFont(buttonFont);
        carica.setFont(buttonFont);


        // Stile bottoni
        cerca.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16");
        carica.putClientProperty("FlatLaf.style", "arc: 999; margin: 8,16,8,16");


        JPanel p3 = createCardPanel();
        p3.add(cerca);
        p3.add(Box.createHorizontalStrut(20));
        p3.add(carica);


        // --- LINEA DIVISORIA ---
        JSeparator linea = new JSeparator(SwingConstants.HORIZONTAL);
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));


        // --- ASSEMBLAGGIO LAYOUT ---
        mainPanel.add(l1);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(p1);
        mainPanel.add(Box.createVerticalStrut(15));

            p2.setVisible(false);
            l2.setVisible(false);

        mainPanel.add(linea);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(l2);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(p2);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(p3);

        classi.addActionListener(e -> {
            p2.setVisible(true);
            l2.setVisible(true);
            setSize(550, 360);
        });

        professori.addActionListener(e -> {
            p2.setVisible(false);
            l2.setVisible(false);
            setSize(550, 300);
        });

        cerca.addActionListener(e -> {
            if (professori.isSelected()) {
                new ProfessoreOrario();
                setVisible(false);
            }
            else if (classi.isSelected() && saraceno.isSelected()) {
                new ClasseOrario("saraceno");
                setVisible(false);
            }
            else if (classi.isSelected() && romegialli.isSelected()) {
                new ClasseOrario("romegialli");
                setVisible(false);
            }
        });

        carica.addActionListener(e ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("File csv", "csv");
            fileChooser.setFileFilter(filter);//solo file csv accettabili come input
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            }
        });


        pack(); // Adatta automaticamente l'altezza della finestra al contenuto
        setMinimumSize(new Dimension(550, getHeight()));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


    }


    // Metodo helper per creare i riquadri grigi arrotondati (Card)
    private JPanel createCardPanel() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        card.putClientProperty("FlatLaf.style", "arc: 16; background: lighten($Panel.background, 4%)");
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return card;
    }
}

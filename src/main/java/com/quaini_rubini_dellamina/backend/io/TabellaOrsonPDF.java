package com.quaini_rubini_dellamina.backend.io;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.PDFHints;
import com.orsonpdf.Page;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TabellaOrsonPDF {

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina";

    public static void creaPDF(List<Slot> slotPDF, String dir, boolean isClasse) {
        PDFDocument pdfDoc = new PDFDocument();
        Page page = pdfDoc.createPage(new Rectangle(1500, 800));
        PDFGraphics2D g2 = page.getGraphics2D();
        g2.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR);

        String[] colonne = {"Ora", Giorni.LUNEDI.getNome(), Giorni.MARTEDI.getNome(), Giorni.MERCOLEDI.getNome(),
                Giorni.GIOVEDI.getNome(), Giorni.VENERDI.getNome()};

        String[][] dati = new String[Slot.ORE_GIORNO][colonne.length];
        Color[][] colori = new Color[Slot.ORE_GIORNO][colonne.length];
        for (int ora = 0; ora < Slot.ORE_GIORNO; ora++) {
            Arrays.fill(dati[ora], "");
            dati[ora][0] = OrarioLezioni.getNome(ora);
        }

        // Recupera la mappa dei colori delegando a ColoriOrario
        Map<String, Color> coloriMaterie = ColoriOrario.buildColoriMaterie(slotPDF);

        for (Slot slot : slotPDF) {
            if (!slot.isAssegnato()) continue;
            int g = slot.indexGiorno();
            int o = slot.indexOra();
            if (g >= 0 && g < Slot.GIORNI_SETTIMANA && o >= 0 && o < Slot.ORE_GIORNO) {
                Lezione l = slot.getLezione();
                String materia = String.valueOf(l.getMateria().getNome());
                if (isClasse) {
                    dati[o][g + 1] = materia + " (" + l.getDocente().getNome() + ")";
                    colori[o][g + 1] = coloriMaterie.get(materia);
                } else {
                    dati[o][g + 1] = l.getClasse().getChiave();
                    colori[o][g + 1] = ColoriOrario.getColoreProfessore();
                }
            }
        }

        Lezione primaLezione = slotPDF.get(0).getLezione();
        String nomeORClasse;
        String tit;
        if (isClasse) {
            nomeORClasse = primaLezione.getClasse().getChiave();
            tit = "Classe " + nomeORClasse;
        } else {
            nomeORClasse = primaLezione.getDocente().getNome();
            tit = "Orario del/della prof " + nomeORClasse;
        }
        File fileOutput = new File(dir, nomeORClasse + ".pdf");

        int xInizio = 40;
        int yInizio = 60;
        int altezzaRiga = 30;
        int[] larghezze = {60, 250, 250, 250, 250, 250};
        int larghezzaTotale = Arrays.stream(larghezze).sum();
        int altezzaTabellaTotale = altezzaRiga * (dati.length + 1);

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        FontMetrics fm = g2.getFontMetrics();
        int xTitolo = xInizio + (larghezzaTotale - fm.stringWidth(tit)) / 2;
        g2.setPaint(new Color(77, 146, 173));
        g2.drawString(tit, xTitolo, 40);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setPaint(new Color(60, 60, 60));
        g2.fillRect(xInizio, yInizio, larghezzaTotale, altezzaRiga);
        g2.setPaint(Color.WHITE);
        int xCorrente = xInizio;
        for (int i = 0; i < colonne.length; i++) {
            g2.drawString(colonne[i], xCorrente + 8, yInizio + 20);
            xCorrente += larghezze[i];
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int riga = 0; riga < dati.length; riga++) {
            int yCorrente = yInizio + ((riga + 1) * altezzaRiga);
            xCorrente = xInizio;
            for (int col = 0; col < dati[riga].length; col++) {
                if (colori[riga][col] != null) {
                    g2.setPaint(colori[riga][col]);
                    g2.fillRect(xCorrente, yCorrente, larghezze[col], altezzaRiga);
                }
                g2.setPaint(Color.BLACK);
                g2.drawString(dati[riga][col], xCorrente + 8, yCorrente + 20);
                xCorrente += larghezze[col];
            }
        }

        g2.setPaint(Color.GRAY);
        g2.drawRect(xInizio, yInizio, larghezzaTotale, altezzaTabellaTotale);
        for (int riga = 0; riga <= dati.length; riga++) {
            int y = yInizio + ((riga + 1) * altezzaRiga);
            g2.drawLine(xInizio, y, xInizio + larghezzaTotale, y);
        }

        xCorrente = xInizio;
        for (int i = 0; i < larghezze.length - 1; i++) {
            xCorrente += larghezze[i];
            g2.drawLine(xCorrente, yInizio, xCorrente, yInizio + altezzaTabellaTotale);
        }

        pdfDoc.writeToFile(fileOutput);
        System.out.println("Tabella PDF generata in: " + fileOutput.getAbsolutePath());
        apriPDF(fileOutput);
    }

    public static void creaPDF(List<Slot> slotAssegnati, boolean isClasse) throws IOException {
        creaPDF(slotAssegnati, DEFAULT_OUTPUT_PATH, isClasse);
    }

    private static void apriPDF(File file) {
        if (!Desktop.isDesktopSupported()) return;
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) return;
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
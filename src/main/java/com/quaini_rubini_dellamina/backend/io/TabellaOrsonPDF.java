package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFHints;
import com.orsonpdf.Page;
import com.orsonpdf.PDFGraphics2D;
import com.quaini_rubini_dellamina.backend.algoritmo.GenOrario;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TabellaOrsonPDF {
    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina/Orario.pdf";


    public static void creaPDF(List<Slot> slotPDF, String dir, String titolo) {
        PDFDocument pdfDoc = new PDFDocument();
        Rectangle bounds = new Rectangle(1500, 800);
        Page page = pdfDoc.createPage(bounds);
        PDFGraphics2D g2 = page.getGraphics2D();
        g2.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR); //Necessario per renderizzare anche carateri no ASCII come ì

        String[] colonne = {"Ora", Giorni.LUNEDI.getNome(), Giorni.MARTEDI.getNome(), Giorni.MERCOLEDI.getNome(),
                Giorni.GIOVEDI.getNome(), Giorni.VENERDI.getNome()};

        // dati[ora][colonna]: colonna 0 = nome ora, 1..5 = giorni
        String[][] dati = new String[Slot.ORE_GIORNO][colonne.length];
        for (int ora = 0; ora < Slot.ORE_GIORNO; ora++) {
            Arrays.fill(dati[ora], "");
            dati[ora][0] = OrarioLezioni.getNome(ora);
        }

        for (Slot slot : slotPDF) {
            if (!slot.isAssegnato()) continue;
            int g = slot.indexGiorno();
            int o = slot.indexOra();
            if (g >= 0 && g < Slot.GIORNI_SETTIMANA && o >= 0 && o < Slot.ORE_GIORNO) {
                Lezione l = slot.getLezione();
                dati[o][g + 1] = l.getMateria().getNome() + " (" + l.getDocente().getNome() + ")";
            }
        }

        int xInizio = 40;
        int yInizio = 60;
        int altezzaRiga = 30;
        int[] larghezze = {60, 200, 200, 200, 200, 200};
        int larghezzaTotale = Arrays.stream(larghezze).sum();

        //titolo

        g2.setPaint(new Color(77, 146, 173));
        FontMetrics fm = g2.getFontMetrics(); //per ricavare la lughezza della stringa
        String tit = "Classe " + titolo;

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        int xPosScrittaCent = (larghezzaTotale - fm.stringWidth(tit) ) / 2;
        g2.drawString(tit, xPosScrittaCent, 40);

        //  Header tabella
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setPaint(new Color(60, 60, 60));
        g2.fillRect(xInizio, yInizio, larghezzaTotale, altezzaRiga);

        g2.setPaint(Color.WHITE);
        int xCorrente = xInizio;
        for (int i = 0; i < colonne.length; i++) {
            g2.drawString(colonne[i], xCorrente + 8, yInizio + 20);
            xCorrente += larghezze[i];
        }

        // Righe
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int riga = 0; riga < dati.length; riga++) {
            int yCorrente = yInizio + ((riga + 1) * altezzaRiga);

            g2.setPaint(riga % 2 == 0 ? new Color(245, 245, 245) : Color.WHITE);
            g2.fillRect(xInizio, yCorrente, larghezzaTotale, altezzaRiga);

            g2.setPaint(Color.BLACK);
            xCorrente = xInizio;
            for (int col = 0; col < dati[riga].length; col++) {
                g2.drawString(dati[riga][col], xCorrente + 8, yCorrente + 20);
                xCorrente += larghezze[col];
            }

            g2.setPaint(Color.LIGHT_GRAY);
            g2.drawLine(xInizio, yCorrente + altezzaRiga, xInizio + larghezzaTotale, yCorrente + altezzaRiga);
        }

        //  Bordi e linee verticali
        g2.setPaint(Color.GRAY);
        int altezzaTabellaTotale = altezzaRiga * (dati.length + 1);
        g2.drawRect(xInizio, yInizio, larghezzaTotale, altezzaTabellaTotale);

        xCorrente = xInizio;
        for (int i = 0; i < larghezze.length - 1; i++) {
            xCorrente += larghezze[i];
            g2.drawLine(xCorrente, yInizio, xCorrente, yInizio + altezzaTabellaTotale);
        }

        File fileOutput = new File(dir);
        pdfDoc.writeToFile(fileOutput);
        System.out.println("Tabella PDF generata in: " + fileOutput.getAbsolutePath());
    }

    public static void creaPDF(List<Slot> slotAssegnati, String titolo) throws IOException {
        creaPDF(slotAssegnati, DEFAULT_OUTPUT_PATH, titolo);
    }

    public static void main(String[] args) throws CsvValidationException, IOException {
        ReadCsv read = new ReadCsv();
        GenOrario genny = new GenOrario();
        List<Slot> slotts;
        List<Lezione> lessons;
        lessons= read.leggi();
        slotts = genny.genOrarioOnClasse(lessons,"5A-INFO");

        TabellaOrsonPDF.creaPDF(slotts,"5A-INFO");
    }
}

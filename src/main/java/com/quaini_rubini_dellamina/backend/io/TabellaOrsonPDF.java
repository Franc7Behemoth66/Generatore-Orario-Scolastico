package com.quaini_rubini_dellamina.backend.io;

import com.opencsv.exceptions.CsvValidationException;
import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.PDFHints;
import com.orsonpdf.Page;
import com.quaini_rubini_dellamina.backend.algoritmo.GenOrario;
import com.quaini_rubini_dellamina.backend.algoritmo.GenOrarioVincoli;
import com.quaini_rubini_dellamina.backend.entità.*;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class TabellaOrsonPDF {

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/java/com/quaini_rubini_dellamina";

    // Colori pastello: una materia prende il colore con il suo indice (in ordine alfabetico)
    private static final Color[] COLORI = {
            new Color(255, 179, 186), // rosa
            new Color(255, 223, 186), // arancio
            new Color(255, 255, 186), // giallo
            new Color(186, 255, 201), // verde
            new Color(186, 225, 255), // azzurro
            new Color(218, 186, 255), // viola
            new Color(255, 204, 229), // fucsia chiaro
            new Color(204, 255, 242), // turchese
            new Color(230, 230, 250), // lavanda
            new Color(240, 230, 140), // kaki
            new Color(255, 218, 193), // pesca
            new Color(202, 255, 191), // verde menta
            new Color(189, 224, 254), // azzurro cielo
            new Color(255, 198, 255), // rosa orchidea
            new Color(253, 255, 182), // giallo limone
            new Color(160, 196, 255), // blu pervinca
            new Color(255, 214, 165), // albicocca
            new Color(155, 246, 255), // ciano
            new Color(224, 187, 228), // malva
            new Color(210, 235, 190)  // verde salvia
    };

    /** Un colore fisso per ogni materia presente negli slot. */
    private static Map<String, Color> coloriMaterie(List<Slot> slots) {
        TreeSet<String> materie = new TreeSet<>();
        for (Slot slot : slots) {
            if (slot.isAssegnato()) {
                materie.add(String.valueOf(slot.getLezione().getMateria().getNome()));
            }
        }
        Map<String, Color> mappa = new HashMap<>();
        int i = 0;
        for (String materia : materie) {
            mappa.put(materia, COLORI[i % COLORI.length]);
            i++;
        }
        return mappa;
    }

    public static void creaPDF(List<Slot> slotPDF, String dir, boolean isClasse) {
        PDFDocument pdfDoc = new PDFDocument();
        Page page = pdfDoc.createPage(new Rectangle(1500, 800));
        PDFGraphics2D g2 = page.getGraphics2D();
        // Necessario per renderizzare anche caratteri non ASCII come ì
        g2.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR);

        String[] colonne = {"Ora", Giorni.LUNEDI.getNome(), Giorni.MARTEDI.getNome(), Giorni.MERCOLEDI.getNome(),
                Giorni.GIOVEDI.getNome(), Giorni.VENERDI.getNome()};

        // dati[ora][colonna]: colonna 0 = nome ora, 1..5 = giorni
        String[][] dati = new String[Slot.ORE_GIORNO][colonne.length];
        Color[][] colori = new Color[Slot.ORE_GIORNO][colonne.length];
        for (int ora = 0; ora < Slot.ORE_GIORNO; ora++) {
            Arrays.fill(dati[ora], "");
            dati[ora][0] = OrarioLezioni.getNome(ora);
        }

        // Colore per materia
        Map<String, Color> coloriMaterie = coloriMaterie(slotPDF);

        // Riempie le celle
        for (Slot slot : slotPDF) {
            if (!slot.isAssegnato()) continue;
            int g = slot.indexGiorno();
            int o = slot.indexOra();
            if (g >= 0 && g < Slot.GIORNI_SETTIMANA && o >= 0 && o < Slot.ORE_GIORNO) {
                Lezione l = slot.getLezione();
                String materia = String.valueOf(l.getMateria().getNome());
                if (isClasse) {
                    dati[o][g + 1] = materia + " (" + l.getDocente().getNome() + ")";
                } else {
                    dati[o][g + 1] = l.getClasse().getChiave();
                }
                colori[o][g + 1] = coloriMaterie.get(materia);
            }
        }

        // Titolo e nome file
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

        // Titolo centrato: il font va impostato prima di calcolare la larghezza
        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        FontMetrics fm = g2.getFontMetrics();
        int xTitolo = xInizio + (larghezzaTotale - fm.stringWidth(tit)) / 2;
        g2.setPaint(new Color(77, 146, 173));
        g2.drawString(tit, xTitolo, 40);

        // Intestazione
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setPaint(new Color(60, 60, 60));
        g2.fillRect(xInizio, yInizio, larghezzaTotale, altezzaRiga);
        g2.setPaint(Color.WHITE);
        int xCorrente = xInizio;
        for (int i = 0; i < colonne.length; i++) {
            g2.drawString(colonne[i], xCorrente + 8, yInizio + 20);
            xCorrente += larghezze[i];
        }

        // Celle
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

        // Bordi e linee
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

    public static void main(String[] args) throws CsvValidationException, IOException {
        ReadCsv read = new ReadCsv();
        GenOrario genny = new GenOrarioVincoli();
        List<Slot> slotts;
        List<Lezione> lessons;
        lessons = read.leggi();
        slotts = genny.genOrarioOnClasse(lessons, "5A-INFO");

        BuildCsv.scriviOrario(slotts, true);

        TabellaOrsonPDF.creaPDF(slotts, true);
    }
}
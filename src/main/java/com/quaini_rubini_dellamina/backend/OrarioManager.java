package com.quaini_rubini_dellamina.backend;

import com.opencsv.exceptions.CsvValidationException;
import com.quaini_rubini_dellamina.backend.algoritmo.GenOrarioVincoli;
import com.quaini_rubini_dellamina.backend.entità.*;
import com.quaini_rubini_dellamina.backend.io.BuildCsv;
import com.quaini_rubini_dellamina.backend.io.ReadCsv;
import com.quaini_rubini_dellamina.backend.io.ReadVincoliJSON;
import com.quaini_rubini_dellamina.backend.io.TabellaOrsonPDF;

import java.io.File;
import java.io.IOException;
import java.util.*;

/*
  Classe ponte tra backend e frontend.
 */
public class OrarioManager {

    private static final String DEFAULT_CSV_PATH =
            "src/main/java/com/quaini_rubini_dellamina/source.csv";
    private static final String DEFAULT_OUTPUT_DIR = "TabelleOrariOutput";

    private static final OrarioManager INSTANCE = new OrarioManager();

    private OrarioManager() {}

    public static OrarioManager getInstance() { return INSTANCE; }

    private String currentCsvPath = DEFAULT_CSV_PATH;
    private ReadCsv reader;
    private List<Lezione>  lezioni  = new ArrayList<>();
    private ConfiguraVincoli vincoli;
    private GenOrarioVincoli generatore;
    private Istituto istituto;
    private Map<String, GenOrarioVincoli.RisultatoVerificaDocente> analisiDocenti = new HashMap<>();
    private Map<String, List<String>> erroriClassi = new LinkedHashMap<>();
    private List<String> erroriCsv = new ArrayList<>();
    private volatile boolean inizializzato = false;

    public synchronized void inizializza() {
        if (inizializzato) return;
        try {
            caricaDataset(DEFAULT_CSV_PATH);
        } catch (Exception e) {
            System.err.println("[OrarioManager] Errore inizializzazione: " + e.getMessage());
        }
    }

    public synchronized void caricaDataset(String csvPath) throws IOException, CsvValidationException {
        File fileCsv = new File(csvPath);
        if (!fileCsv.exists())
            throw new IOException("File CSV non trovato: " + fileCsv.getAbsolutePath());

        this.currentCsvPath = csvPath;
        this.reader  = new ReadCsv(csvPath);
        this.lezioni = reader.leggi();
        this.erroriCsv = new ArrayList<>(reader.getErroriCsv());

        try {
            this.vincoli = ReadVincoliJSON.leggi();
        } catch (Exception e) {
            System.err.println("[OrarioManager] Vincoli JSON non caricati: " + e.getMessage());
            this.vincoli = new ConfiguraVincoli("Saraceno-Romegialli", Collections.emptyList());
        }

        this.generatore = new GenOrarioVincoli(vincoli);
        new File(DEFAULT_OUTPUT_DIR).mkdirs();

        this.istituto = generatore.generaIstituto(reader, lezioni, DEFAULT_OUTPUT_DIR);
        this.erroriClassi = new LinkedHashMap<>(generatore.getErroriClassi());

        if (vincoli != null && istituto != null)
            this.analisiDocenti = GenOrarioVincoli.analizzaOrario(vincoli, istituto);

        this.inizializzato = true;
        System.out.println("[OrarioManager] Caricato: "
                + lezioni.size() + " lezioni, "
                + istituto.getClassiUni().size() + " classi, "
                + istituto.getDocentiUni().size() + " docenti.");
    }

    public synchronized void caricaNuovoCsv(String path) throws IOException, CsvValidationException {
        inizializzato = false;
        caricaDataset(path);
    }

    public boolean isInizializzato() { return inizializzato; }

    public synchronized int getNumeroLezioni() {
        return lezioni != null ? lezioni.size() : 0;
    }

    public synchronized int getNumeroClassi() {
        return (istituto != null) ? istituto.getClassiUni().size() : 0;
    }

    public synchronized int getNumeroDocenti() {
        return (istituto != null) ? istituto.getDocentiUni().size() : 0;
    }

    public synchronized List<Classe> getClassi() {
        if (!inizializzato) inizializza();
        return (istituto != null) ? istituto.getClassiUni() : Collections.emptyList();
    }

    public synchronized List<Docente> getDocenti() {
        if (!inizializzato) inizializza();
        return (istituto != null) ? istituto.getDocentiUni() : Collections.emptyList();
    }

    public synchronized List<Slot> getOrarioClasse(String chiaveClasse) {
        if (!inizializzato) inizializza();
        if (istituto == null || chiaveClasse == null) return Collections.emptyList();
        ArrayList<Slot> s = istituto.getClassi_tabelleh().get(chiaveClasse);
        return (s != null) ? s : Collections.emptyList();
    }

    public synchronized List<Slot> getOrarioDocente(String nomeDocente) {
        if (!inizializzato) inizializza();
        if (istituto == null || nomeDocente == null) return Collections.emptyList();
        ArrayList<Slot> s = istituto.getDocenti_tabelleh().get(nomeDocente);
        return (s != null) ? s : Collections.emptyList();
    }

    public synchronized Preferenza getPreferenzaDocente(String nomeDocente) {
        if (generatore == null || nomeDocente == null) return null;
        return generatore.getPreferenzaDocente(nomeDocente);
    }

    public synchronized GenOrarioVincoli.RisultatoVerificaDocente getAnalisiDocente(String nomeDocente) {
        return (analisiDocenti != null) ? analisiDocenti.get(nomeDocente) : null;
    }

    public synchronized List<String> getErroriClasse(String chiaveClasse) {
        if (erroriClassi == null || chiaveClasse == null) return Collections.emptyList();
        return erroriClassi.getOrDefault(chiaveClasse, Collections.emptyList());
    }

    public synchronized List<String> getErroriCsv() {
        return (erroriCsv != null) ? Collections.unmodifiableList(erroriCsv) : Collections.emptyList();
    }

    public synchronized List<String> getReportErroriEVincoli() {
        List<String> report = new ArrayList<>();

        if (erroriCsv != null) {
            for (String errCsv : erroriCsv) {
                report.add("[ERRORE CSV] " + errCsv);
            }
        }

        if (erroriClassi != null) {
            for (List<String> errs : erroriClassi.values()) {
                for (String e : errs) {
                    report.add("[ERRORE GENERAZIONE] " + e);
                }
            }
        }

        if (analisiDocenti != null) {
            for (GenOrarioVincoli.RisultatoVerificaDocente res : analisiDocenti.values()) {
                if (res != null && res.haSegnalazioni()) {
                    for (String msg : res.getElencoSegnalazioni()) {
                        report.add("Docente " + res.docente + " → " + msg);
                    }
                }
            }
        }
        return report;
    }

    public synchronized Istituto getIstituto() {
        if (!inizializzato) inizializza();
        return istituto;
    }

    public synchronized ConfiguraVincoli getVincoli() { return vincoli; }

    public synchronized List<Lezione> getLezioni() { return lezioni; }

    public synchronized void esportaPdfClasse(String chiaveClasse, String cartella) throws Exception {
        List<Slot> slots = getOrarioClasse(chiaveClasse);
        if (slots.isEmpty())
            throw new IllegalStateException("Nessun orario per la classe " + chiaveClasse);
        String dir = (cartella != null && !cartella.isBlank()) ? cartella : DEFAULT_OUTPUT_DIR;
        TabellaOrsonPDF.creaPDF(slots, dir, true);
    }

    public synchronized void esportaCsvClasse(String chiaveClasse, String cartella) throws Exception {
        List<Slot> slots = getOrarioClasse(chiaveClasse);
        if (slots.isEmpty())
            throw new IllegalStateException("Nessun orario per la classe " + chiaveClasse);
        String dir = (cartella != null && !cartella.isBlank()) ? cartella : DEFAULT_OUTPUT_DIR;
        BuildCsv.scriviOrario(slots, dir, true);
    }

    public synchronized void esportaPdfDocente(String nomeDocente, String cartella) throws Exception {
        List<Slot> slots = getOrarioDocente(nomeDocente);
        if (slots.isEmpty())
            throw new IllegalStateException("Nessun orario per il docente " + nomeDocente);
        String dir = (cartella != null && !cartella.isBlank()) ? cartella : DEFAULT_OUTPUT_DIR;
        TabellaOrsonPDF.creaPDF(slots, dir, false);
    }

    public synchronized void esportaCsvDocente(String nomeDocente, String cartella) throws Exception {
        List<Slot> slots = getOrarioDocente(nomeDocente);
        if (slots.isEmpty())
            throw new IllegalStateException("Nessun orario per il docente " + nomeDocente);
        String dir = (cartella != null && !cartella.isBlank()) ? cartella : DEFAULT_OUTPUT_DIR;
        BuildCsv.scriviOrario(slots, dir, false);
    }
}

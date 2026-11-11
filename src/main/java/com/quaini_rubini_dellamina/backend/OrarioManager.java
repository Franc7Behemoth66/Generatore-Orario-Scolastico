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
  Classe che ponte tra backend e frontend.

  Responsabilità:
   - Carica il CSV (ReadCsv) e i vincoli JSON (ReadVincoliJSON)
   - Genera l'orario completo d'istituto via GenOrarioVincoli
   - Espone le liste di classi/docenti e gli slot degli orari al frontend
   - Gestisce l'esportazione PDF e CSV su richiesta della GUI
 */
public class OrarioManager {

    private static final String DEFAULT_CSV_PATH =
            "src/main/java/com/quaini_rubini_dellamina/source.csv";
    private static final String DEFAULT_OUTPUT_DIR = "TabelleOrariOutput";

    // per fare in modo che esista una sola instanza
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
    private volatile boolean inizializzato = false;


    // Inizzializzazione / caricamento dataset

    public synchronized void inizializza() {
        if (inizializzato) return;
        try {
            caricaDataset(DEFAULT_CSV_PATH);
        } catch (Exception e) {
            System.err.println("[OrarioManager] Errore inizializzazione: " + e.getMessage());
        }
    }

    //Carica un CSV specifico e rigenera l'intero orario d'istituto con vincoli
    public synchronized void caricaDataset(String csvPath) throws IOException, CsvValidationException {
        File fileCsv = new File(csvPath);
        if (!fileCsv.exists())
            throw new IOException("File CSV non trovato: " + fileCsv.getAbsolutePath());

        this.currentCsvPath = csvPath;
        this.reader  = new ReadCsv(csvPath);
        this.lezioni = reader.leggi();

        // Vincoli JSON — fallback a lista vuota se il file non è raggiungibile
        try {
            this.vincoli = ReadVincoliJSON.leggi();
        } catch (Exception e) {
            System.err.println("[OrarioManager] Vincoli JSON non caricati: " + e.getMessage());
            this.vincoli = new ConfiguraVincoli("Saraceno-Romegialli", Collections.emptyList());
        }

        this.generatore = new GenOrarioVincoli(vincoli);

        // Crea la cartella di output se non esiste
        new File(DEFAULT_OUTPUT_DIR).mkdirs();

        // Genera l'orario completo (scrive anche i CSV per ogni classe/docente)
        this.istituto = generatore.generaIstituto(reader, lezioni, DEFAULT_OUTPUT_DIR);

        // Analisi vincoli e soft-constraint docenti
        if (vincoli != null && istituto != null)
            this.analisiDocenti = GenOrarioVincoli.analizzaOrario(vincoli, istituto);

        this.inizializzato = true;
        System.out.println("[OrarioManager] Caricato: "
                + lezioni.size() + " lezioni, "
                + istituto.getClassiUni().size() + " classi, "
                + istituto.getDocentiUni().size() + " docenti.");
    }

    // Permette il caricamento di un nuovo CSV dall'interfaccia utente
    public synchronized void caricaNuovoCsv(String path) throws IOException, CsvValidationException {
        inizializzato = false;      // resetta così da poter ricaricare
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

    // getter dati (chiamati dal frontend)


    // Lista ordinata di tutte le classi dell'istituto
    public synchronized List<Classe> getClassi() {
        if (!inizializzato) inizializza();
        return (istituto != null) ? istituto.getClassiUni() : Collections.emptyList();
    }

    // Lista ordinata di tutti i docenti dell'istituto
    public synchronized List<Docente> getDocenti() {
        if (!inizializzato) inizializza();
        return (istituto != null) ? istituto.getDocentiUni() : Collections.emptyList();
    }

    // Restituisce gli slot dell'orario di una classe (chiave es. "5A-INFO")
    public synchronized List<Slot> getOrarioClasse(String chiaveClasse) {
        if (!inizializzato) inizializza();
        if (istituto == null || chiaveClasse == null) return Collections.emptyList();
        ArrayList<Slot> s = istituto.getClassi_tabelleh().get(chiaveClasse);
        return (s != null) ? s : Collections.emptyList();
    }

    // Restituisce gli slot dell'orario di un docente (nome completo)
    public synchronized List<Slot> getOrarioDocente(String nomeDocente) {
        if (!inizializzato) inizializza();
        if (istituto == null || nomeDocente == null) return Collections.emptyList();
        ArrayList<Slot> s = istituto.getDocenti_tabelleh().get(nomeDocente);
        return (s != null) ? s : Collections.emptyList();
    }

    // Preferenza del docente (da vincoli JSON)
    public synchronized Preferenza getPreferenzaDocente(String nomeDocente) {
        if (generatore == null || nomeDocente == null) return null;
        return generatore.getPreferenzaDocente(nomeDocente);
    }

    // Risultato analisi vincoli per un docente specifico
    public synchronized GenOrarioVincoli.RisultatoVerificaDocente getAnalisiDocente(String nomeDocente) {
        return (analisiDocenti != null) ? analisiDocenti.get(nomeDocente) : null;
    }

    public synchronized Istituto getIstituto() {
        if (!inizializzato) inizializza();
        return istituto;
    }

    public synchronized ConfiguraVincoli getVincoli() { return vincoli; }

    public synchronized List<Lezione> getLezioni() { return lezioni; }


    // esportazione PDF e CSV (delegano a BuildCsv / TabellaOrsonPDF)

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

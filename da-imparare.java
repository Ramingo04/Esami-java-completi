import java.util.*;
import java.time.*;

/* ============================================================================
 * MEGA RECAP DEFINITIVO - ESAME JAVA OOP & MULTITHREADING (NETBEANS)
 * ============================================================================
 *
 * 1. STRATEGIA DELLE 3 ORE (METODO DELLO SCHELETRO)
 * ----------------------------------------------------------------------------
 * - STEP 1 (Scheletro completo): Leggi tutta la traccia e crea solo le scatole
 *   vuote (Eccezione, Interfacce, Classi base, Classi Key con equals/hashCode,
 *   Gestore con Map/List, Monitor vuoto, 3 Thread vuoti con costruttore e run).
 *   Obiettivo: avere tutta la struttura con ZERO errori rossi.
 * - STEP 2 (Logica Parte 1 + Test): Riempi i metodi del Gestore e testali
 *   subito nel main con 2-3 oggetti di prova. Metti la Parte 1 in cassaforte.
 * - STEP 3 (Logica Parte 2 + Thread): Riempi while/wait/notifyAll nel Monitor,
 *   metti i cicli for nei Thread e avvia tutto nel main con .start().
 *
 *
 * 2. REGOLE D'ORO PARTE 1 (OOP E STRUTTURE DATI)
 * ----------------------------------------------------------------------------
 * - PRIMO SECONDO D'ESAME: Scrivi subito in cima:
 *     import java.util.*;
 *     import java.time.*;
 * - REGOLA DELLA TASCA (Parametri e this.):
 *     * Dato già negli attributi private in cima ("in tasca") -> ZERO parametri ().
 *     * Dato che arriva da fuori (passato nel main) -> METTI parametro (Tipo x).
 *     * Usa "this." SOLO dentro i costruttori.
 * - CHIAVI DELLE HASHMAP:
 *     * 1 solo dato (ID, email, matricola, VIN) -> NESSUNA classe, usa direttamente
 *       Integer o String (es. Map<String, Veicolo>).
 *     * 2 o più dati insieme (nome + produttore, id + corso) -> CREA classe Key
 *       e genera subito equals() e hashCode() spuntando tutti i campi.
 * - CICLI FOR-EACH E RIMOZIONI:
 *     * Su List / Set -> for (Veicolo v : lista)
 *     * Su Map        -> for (Veicolo v : mappa.values())
 *     * MAI usare lista.remove(...) dentro un for-each normale (crasha!).
 *       Per rimuovere usa:
 *         1) lista.remove(oggetto);
 *         2) for-each + if (...) { lista.remove(x); break; } (se ne togli 1 solo)
 *         3) lista.removeIf(x -> x.condizione()); (se ne togli tanti)
 * - TIPI DI RITORNO DEI METODI:
 *     * Risponde Sì/No ("verifica se...", "se possibile... altrimenti false") -> boolean
 *     * Fa un'azione o lancia eccezione se fallisce ("altrimenti solleva...") -> void
 *     * Estrae un oggetto, calcolo o lista -> TipoDelDato (es. double, List<X>)
 * - ECCEZIONE UNICA: Crea una sola classe "class MiaException extends RuntimeException"
 *   e usala ovunque cambiando messaggio (non obbliga a mettere throws sui metodi).
 * - STRINGHE: Usa sempre .equalsIgnoreCase(...) al posto di .equals(...).
 *
 *
 * 3. SCORCIATOIE SALVA-VITA SU NETBEANS
 * ----------------------------------------------------------------------------
 * - GENERA CODICE (Constructor, Getter/Setter, equals/hashCode, toString, Override):
 *     Tasto Destro dentro la classe -> Insert Code... (oppure Alt + Ins).
 * - RIPARA TUTTO SUGLI ERRORI ROSSI (Alt + Invio con Enter grande, o clic lampadina):
 *     * Implementa interfacce: scrivi "implements NomeInterfaccia" -> riga rossa ->
 *       Alt + Invio -> "Implement all abstract methods".
 *     * Importa librerie: vai sulla parola rossa -> Alt + Invio -> "Add import...".
 *     * Eccezioni Thread: Alt + Invio su wait() -> mette "throws InterruptedException".
 *       Alt + Invio su Thread.sleep() -> mette blocco "try-catch".
 * - COMPLETAMENTO SINTASSI:
 *     * Scrivi 3 lettere (es. Arr o Hash) + Ctrl + Spazio -> completa ArrayList / HashMap.
 *     * Scrivi "lista." o "mappa." (col punto) -> mostra tutti i metodi disponibili.
 * - ABBREVIAZIONI + TAB:
 *     * sout + Tab -> System.out.println("");
 *     * psvm + Tab -> public static void main(String[] args) { }
 *     * fore + Tab -> ciclo for-each pronto.
 * - ALTRE UTILI:
 *     * Alt + Shift + F        -> Formatta tutto il file (trova parentesi } perse).
 *     * Ctrl + R               -> Rinomina una variabile in tutto il file.
 *     * Ctrl + Shift + Giù     -> Duplica la riga o il blocco selezionato.
 *
 *
 * 4. I THREAD IN 5 FASI (COSA FAI [TU] vs COSA FA [NETBEANS])
 * ----------------------------------------------------------------------------
 * - FASE 1 (Monitor): Crea la classe condivisa. Metti "synchronized" sui metodi.
 *   Chi inserisce: while (pieno) { wait(); } -> inserisci -> notifyAll();
 *   Chi toglie:    while (vuoto) { wait(); } -> togli -> notifyAll();
 *   Chi legge:     solo synchronized + stampa (no wait, no notifyAll).
 *   Premi Alt + Invio su wait() per far mettere "throws InterruptedException".
 * - FASE 2 (1° Thread): Scrivi "class Produttore extends Thread" con attributo
 *   "private Monitor monitor;". Usa Insert Code -> Constructor e Override -> run().
 * - FASE 3 (Corpo run): Scrivi ciclo for, chiamata al monitor, Thread.sleep(300);
 *   e premi Alt + Invio per far mettere il try-catch a NetBeans.
 * - FASE 4 (Copia-Incolla): Duplica Produttore per fare Consumatore e Lettore,
 *   cambiando solo il nome della classe e la chiamata dentro il ciclo for.
 * - FASE 5 (Main): Crea 1 SOLO Monitor, passalo a tutti, avvia con .start().
 * ============================================================================ */


// ============================================================================
// ESEMPIO 1: STAMPINO THREAD CON LISTA (ES. TRACCIA 1 - DIREZIONE / DOCENTI / SPONSOR)
// ============================================================================

class MonitorLista {
    // [TU] Collezione condivisa e capacità massima
    private List<String> coda = new ArrayList<>();
    private int max = 5;

    // 1. CHI METTE (Produttore / Docente / Writer)
    // [TU] Scrivi "public synchronized void inserisci(String dato)"
    // [NETBEANS] Aggiunge "throws InterruptedException" con Alt+Invio su wait()
    public synchronized void inserisci(String dato) throws InterruptedException {
        // [TU] Sempre WHILE (mai if!)
        while (coda.size() >= max) {
            System.out.println("[PRODUTTORE] Coda piena, aspetto...");
            wait();
        }
        coda.add(dato);
        System.out.println("[PRODUTTORE] Inserito: " + dato);
        notifyAll(); // [TU] Sveglia sempre tutti alla fine
    }

    // 2. CHI TOGLIE (Consumatore / Sponsor / Cancellatore)
    public synchronized String preleva() throws InterruptedException {
        // [TU] Sempre WHILE (mai if!)
        while (coda.isEmpty()) {
            System.out.println("[CONSUMATORE] Coda vuota, aspetto...");
            wait();
        }
        // [TU] remove(0) toglie il primo arrivato -> modalità FIFO
        String estratto = coda.remove(0);
        System.out.println("[CONSUMATORE] Prelevato: " + estratto);
        notifyAll();
        return estratto;
    }

    // 3. CHI LEGGE E BASTA (Lettore / Monitor / Osservatore)
    // [TU] Basta synchronized (niente wait, niente notifyAll)
    public synchronized void leggi() {
        System.out.println("[LETTORE] Stato coda (" + coda.size() + "/" + max + "): " + coda);
    }
}

class Produttore extends Thread {
    private MonitorLista monitor;

    // [NETBEANS] Tasto Destro -> Insert Code... -> Constructor...
    public Produttore(MonitorLista monitor) {
        this.monitor = monitor;
    }

    // [NETBEANS] Tasto Destro -> Insert Code... -> Override Method... -> run()
    @Override
    public void run() {
        // [NETBEANS] Alt+Invio su Thread.sleep() -> Surround Block with try-catch
        try {
            // [TU] Ciclo for finito + chiamata al monitor + sleep
            for (int i = 1; i <= 8; i++) {
                monitor.inserisci("Elemento_" + i);
                Thread.sleep(300);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumatore extends Thread {
    private MonitorLista monitor;

    // [NETBEANS] Insert Code -> Constructor...
    public Consumatore(MonitorLista monitor) {
        this.monitor = monitor;
    }

    // [NETBEANS] Insert Code -> Override Method... -> run()
    @Override
    public void run() {
        // [NETBEANS] Alt+Invio -> Surround Block with try-catch
        try {
            // [TU] Copia-incolla del Produttore: cambi solo la chiamata nel for!
            for (int i = 1; i <= 8; i++) {
                monitor.preleva();
                Thread.sleep(500);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

class Lettore extends Thread {
    private MonitorLista monitor;

    // [NETBEANS] Insert Code -> Constructor...
    public Lettore(MonitorLista monitor) {
        this.monitor = monitor;
    }

    // [NETBEANS] Insert Code -> Override Method... -> run()
    @Override
    public void run() {
        // [NETBEANS] Alt+Invio -> Surround Block with try-catch
        try {
            // [TU] Frequenza più lenta (sleep più alto)
            for (int i = 1; i <= 5; i++) {
                monitor.leggi();
                Thread.sleep(700);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}


// ============================================================================
// ESEMPIO 2: STAMPINO THREAD CON HASHMAP (ES. TRACCIA 2 - AGENZIA NOLEGGIO)
// ============================================================================

class MonitorMappa {
    // [TU] Mappa condivisa (Map<email, VIN>), capacità massima e Random
    private Map<String, String> mappaNoleggi = new HashMap<>();
    private int size;
    private Random random = new Random();

    // [NETBEANS] Insert Code -> Constructor... (selezionando solo "size")
    public MonitorMappa(int size) {
        this.size = size;
    }

    // 1. CHI METTE (Inserisce nella Map)
    public synchronized void inserisciNoleggio(String userEmail) throws InterruptedException {
        while (mappaNoleggi.size() >= size) {
            System.out.println("[PRODUTTORE] Tutti i " + size + " veicoli occupati. Attendo...");
            wait();
        }
        String vinScelto;
        do {
            int num = random.nextInt(size) + 1;
            vinScelto = "vin" + num;
        } while (mappaNoleggi.containsValue(vinScelto));

        mappaNoleggi.put(userEmail, vinScelto);
        System.out.println("[PRODUTTORE] Inserito: " + userEmail + " -> " + vinScelto);
        notifyAll();
    }

    // 2. CHI TOGLIE (Rimuove dalla Map)
    public synchronized void restituisciNoleggio() throws InterruptedException {
        while (mappaNoleggi.isEmpty()) {
            System.out.println("[CONSUMATORE] Archivio vuoto. Attendo...");
            wait();
        }
        // Prende la prima chiave presente nella mappa e la rimuove
        String primaEmail = mappaNoleggi.keySet().iterator().next();
        String vinLiberato = mappaNoleggi.remove(primaEmail);
        System.out.println("[CONSUMATORE] Restituito: " + vinLiberato + " (di " + primaEmail + ")");
        notifyAll();
    }

    // 3. CHI LEGGE E BASTA
    public synchronized void leggiNoleggiInAtto() {
        System.out.println("[LETTORE] Noleggi attivi (" + mappaNoleggi.size() + "/" + size + "): " + mappaNoleggi);
    }
}


// ============================================================================
// ESEMPIO 3: MAIN DI AVVIO (1 SOLO MONITOR + .start() SU TUTTI)
// ============================================================================

public class MegaRecapEsameJava {
    public static void main(String[] args) {
        // 1. Crea UN SOLO oggetto Monitor (se ne crei due, i thread non si parlano!)
        MonitorLista monitor = new MonitorLista();

        // 2. Crea i Thread passandogli lo STESSO Monitor
        Produttore p = new Produttore(monitor);
        Consumatore c = new Consumatore(monitor);
        Lettore l = new Lettore(monitor);

        // 3. Falli partire con .start() (ERRORE GRAVE SE SCRIVI .run()!)
        p.start();
        c.start();
        l.start();
    }
}

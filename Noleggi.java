import java.util.*;

// ============================================================================
// 1. ECCEZIONE UNICA (Così non perdi tempo a crearne 10 diverse)
// ============================================================================
class NoleggioException extends RuntimeException {
    public NoleggioException(String message) {
        super(message);
    }
}

// ============================================================================
// 2. INTERFACCE (Noleggiabile e ConPasseggeri)
// ============================================================================
interface Noleggiabile {
    // REGOLA DELLA TASCA: chi noleggia e per quanti giorni vengono da fuori -> PARAMETRI!
    void noleggia(Utente utente, int giorni);

    // REGOLA DELLA TASCA: lo stato del noleggio, il pagamento, il reset e il calcolo
    // del costo usano dati che il veicolo ha GIÀ nei suoi attributi -> ZERO PARAMETRI!
    Noleggio getStatoNoleggio();
    void pagaNoleggio();
    void restituisciVeicolo();
    double calcolaCostoGiornaliero();
}

interface ConPasseggeri {
    // REGOLA DELLA TASCA: Auto e Pulmino hanno già l'attributo "numeroPosti" -> ZERO PARAMETRI!
    int capienza_passeggeri();
}

// ============================================================================
// 3. CLASSI DI SUPPORTO: Utente e Noleggio
// ============================================================================
class Utente {
    private String nome;
    private String email;

    public Utente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nome + " (" + email + ")";
    }
}

class Noleggio {
    private int numeroGiorni;
    private Utente utente;
    private double costoTotale;
    private boolean pagato;

    public Noleggio(int numeroGiorni, Utente utente, double costoTotale) {
        this.numeroGiorni = numeroGiorni;
        this.utente = utente;
        this.costoTotale = costoTotale;
        this.pagato = false;
    }

    public int getNumeroGiorni() { return numeroGiorni; }
    public Utente getUtente() { return utente; }
    public double getCostoTotale() { return costoTotale; }
    public boolean isPagato() { return pagato; }
    public void setPagato(boolean pagato) { this.pagato = pagato; }

    @Override
    public String toString() {
        return "Utente=" + utente + ", Giorni=" + numeroGiorni + ", Costo=" + costoTotale + "€, Pagato=" + pagato;
    }
}

// ============================================================================
// 4. CLASSI ASTRATTE: Veicolo e VeicoloTargato
// Nota: "this" usato SOLO nel costruttore! Nei metodi usiamo direttamente i nomi dei campi.
// ============================================================================
abstract class Veicolo implements Noleggiabile {
    // --- LA "TASCA" DI VEICOLO ---
    private String marca;
    private int vin;
    private String dataImmissione;
    private Noleggio noleggioCorrente;

    public Veicolo(String marca, int vin, String dataImmissione) {
        if (vin <= 0) {
            throw new NoleggioException("Errore: il VIN deve essere positivo!");
        }
        this.marca = marca;
        this.vin = vin;
        this.dataImmissione = dataImmissione;
        this.noleggioCorrente = null;
    }

    @Override
    public void noleggia(Utente utente, int giorni) {
        // Niente "this": usiamo direttamente noleggioCorrente e vin!
        if (noleggioCorrente != null) {
            throw new NoleggioException("Veicolo con VIN " + vin + " già noleggiato!");
        }
        double costoTot = calcolaCostoGiornaliero() * giorni;
        noleggioCorrente = new Noleggio(giorni, utente, costoTot);
    }

    @Override
    public Noleggio getStatoNoleggio() {
        return noleggioCorrente;
    }

    @Override
    public void pagaNoleggio() {
        if (noleggioCorrente == null) {
            throw new NoleggioException("Nessun noleggio da pagare per il VIN " + vin);
        }
        noleggioCorrente.setPagato(true);
    }

    @Override
    public void restituisciVeicolo() {
        if (noleggioCorrente == null) {
            throw new NoleggioException("Impossibile rilasciare: il veicolo VIN " + vin + " non è noleggiato!");
        }
        noleggioCorrente = null;
    }

    public String getMarca() { return marca; }
    public int getVin() { return vin; }
    public String getDataImmissione() { return dataImmissione; }

    @Override
    public String toString() {
        String stato = (noleggioCorrente == null) ? "LIBERO" : "NOLEGGIATO [" + noleggioCorrente + "]";
        return "VIN=" + vin + ", Marca=" + marca + ", Data=" + dataImmissione + ", Stato=" + stato;
    }
}

abstract class VeicoloTargato extends Veicolo {
    private String targa;

    public VeicoloTargato(String marca, int vin, String dataImmissione, String targa) {
        super(marca, vin, dataImmissione);
        this.targa = targa;
    }

    public String getTarga() { return targa; }

    @Override
    public String toString() {
        return super.toString() + ", Targa=" + targa;
    }
}

// ============================================================================
// 5. SOTTOCLASSI CONCRETE: Bici, Auto, FurgoneMerci, FurgonePersone
// ============================================================================
class Bici extends Veicolo {
    private String modello; // "city", "mountain", "elettrica"

    public Bici(String marca, int vin, String dataImmissione, String modello) {
        super(marca, vin, dataImmissione);
        this.modello = modello.toLowerCase();
    }

    // REGOLA DELLA TASCA: "modello" è già un attributo qui sopra -> zero parametri!
    @Override
    public double calcolaCostoGiornaliero() {
        if (modello.equals("city")) return 20.0;
        return 30.0;
    }

    @Override
    public String toString() {
        return "[BICI] " + super.toString() + ", Modello=" + modello + ", Costo/dì=" + calcolaCostoGiornaliero() + "€";
    }
}

class Auto extends VeicoloTargato implements ConPasseggeri {
    private int numeroPosti;
    private int cilindrata;
    private double costoGiornaliero;

    public Auto(String marca, int vin, String dataImmissione, String targa, int numeroPosti, int cilindrata, double costoGiornaliero) {
        super(marca, vin, dataImmissione, targa);
        if (costoGiornaliero <= 0 || cilindrata <= 0) {
            throw new NoleggioException("Cilindrata o costo auto non validi!");
        }
        this.numeroPosti = numeroPosti;
        this.cilindrata = cilindrata;
        this.costoGiornaliero = costoGiornaliero;
    }

    @Override
    public int capienza_passeggeri() {
        return numeroPosti;
    }

    @Override
    public double calcolaCostoGiornaliero() {
        return costoGiornaliero;
    }

    public int getCilindrata() { return cilindrata; }

    @Override
    public String toString() {
        return "[AUTO] " + super.toString() + ", Posti=" + numeroPosti + ", Cilindrata=" + cilindrata + ", Costo/dì=" + costoGiornaliero + "€";
    }
}

class FurgoneMerci extends VeicoloTargato {
    private double cubatura;

    public FurgoneMerci(String marca, int vin, String dataImmissione, String targa, double cubatura) {
        super(marca, vin, dataImmissione, targa);
        this.cubatura = cubatura;
    }

    // REGOLA DELLA TASCA: "cubatura" è già in tasca -> zero parametri!
    @Override
    public double calcolaCostoGiornaliero() {
        return cubatura * 7.0;
    }

    @Override
    public String toString() {
        return "[FURGONE MERCI] " + super.toString() + ", Cubatura=" + cubatura + ", Costo/dì=" + calcolaCostoGiornaliero() + "€";
    }
}

class FurgonePersone extends VeicoloTargato implements ConPasseggeri {
    private int numeroPosti;

    public FurgonePersone(String marca, int vin, String dataImmissione, String targa, int numeroPosti) {
        super(marca, vin, dataImmissione, targa);
        this.numeroPosti = numeroPosti;
    }

    @Override
    public int capienza_passeggeri() {
        return numeroPosti;
    }

    // REGOLA DELLA TASCA: "numeroPosti" è già in tasca -> zero parametri!
    @Override
    public double calcolaCostoGiornaliero() {
        return numeroPosti * 10.0;
    }

    @Override
    public String toString() {
        return "[PULMINO] " + super.toString() + ", Posti=" + numeroPosti + ", Costo/dì=" + calcolaCostoGiornaliero() + "€";
    }
}

// ============================================================================
// 6. ARCHIVIO AGENZIA (Gestione HashMap - Punti 1, 2, 4, 5, 6)
// Guarda la "Tasca" di ArchivioAgenzia: ha solo le due Map!
// ============================================================================
class ArchivioAgenzia {
    // --- LA "TASCA" DI ARCHIVIO AGENZIA ---
    private Map<Integer, Veicolo> parcoVeicoli = new HashMap<>();
    private Map<String, Integer> archivioUtenti = new HashMap<>();

    // Punto 2: Quale veicolo devo inserire? Non ce l'ho in tasca -> Parametro (Veicolo v)
    public void inserisciVeicolo(Veicolo v) {
        if (parcoVeicoli.containsKey(v.getVin())) {
            throw new NoleggioException("Veicolo con VIN " + v.getVin() + " già presente!");
        }
        parcoVeicoli.put(v.getVin(), v);
    }

    // Punto 1: Devo stampare tutta la mappa. La mappa ce l'ho già in tasca -> ZERO PARAMETRI!
    public void stampaTuttiIVeicoli() {
        System.out.println("--- PARCO VEICOLI COMPLETO ---");
        for (Veicolo v : parcoVeicoli.values()) {
            System.out.println(v);
        }
    }

    // Punto 4: "ricercare auto, DATI cilindrata e costo massimo".
    // I criteri di ricerca non sono in tasca -> PARAMETRI (int cilindrata, double costoMax)!
    public List<Auto> cercaAuto(int cilindrata, double costoMax) {
        List<Auto> risultato = new ArrayList<>();
        for (Veicolo v : parcoVeicoli.values()) {
            if (v instanceof Auto) {
                Auto a = (Auto) v;
                if (a.getCilindrata() == cilindrata && a.calcolaCostoGiornaliero() <= costoMax) {
                    risultato.add(a);
                }
            }
        }
        return risultato;
    }

    // Punto 5 (Noleggio): Quale VIN? Quale Utente? Quanti giorni? Non li ho in tasca -> PARAMETRI!
    public void noleggiaVeicolo(int vin, Utente utente, int giorni) {
        Veicolo v = parcoVeicoli.get(vin);
        if (v == null) {
            throw new NoleggioException("Veicolo con VIN " + vin + " non trovato!");
        }
        v.noleggia(utente, giorni);
        archivioUtenti.put(utente.getEmail(), vin);
        System.out.println("Noleggio effettuato! Costo totale: " + v.getStatoNoleggio().getCostoTotale() + "€");
    }

    // Punto 5 (Rilascio): Quale veicolo della mappa vuoi rilasciare? Mi serve il VIN -> PARAMETRO (int vin)!
    public void rilasciaVeicolo(int vin) {
        Veicolo v = parcoVeicoli.get(vin);
        if (v == null) {
            throw new NoleggioException("Veicolo con VIN " + vin + " non trovato!");
        }
        if (v.getStatoNoleggio() == null) {
            throw new NoleggioException("Il veicolo VIN " + vin + " non è attualmente noleggiato!");
        }
        String emailUtente = v.getStatoNoleggio().getUtente().getEmail();
        v.restituisciVeicolo(); // Qui sul singolo veicolo è senza parametri!
        archivioUtenti.remove(emailUtente);
        System.out.println("Veicolo VIN " + vin + " restituito da " + emailUtente);
    }

    // Punto 6: Stampare i veicoli noleggiati. La mappa è già in tasca -> ZERO PARAMETRI!
    public void stampaVeicoliNoleggiati() {
        System.out.println("--- VEICOLI ATTUALMENTE NOLEGGIATI ---");
        for (Veicolo v : parcoVeicoli.values()) {
            if (v.getStatoNoleggio() != null) {
                System.out.println(v);
            }
        }
    }
}

// ============================================================================
// 7. MULTITHREADING SU ArchivioUtenti (Pagina 2 della traccia)
// ============================================================================
class ArchivioUtentiConcorrente {
    private Map<String, String> mappaNoleggi = new HashMap<>();
    private int size;
    private Random random = new Random();

    public ArchivioUtentiConcorrente(int size) {
        this.size = size;
    }

    // Il Produttore passa l'email generata ("user1@gmail.com") -> Parametro (String userEmail)
    public synchronized void inserisciNoleggio(String userEmail) throws InterruptedException {
        while (mappaNoleggi.size() >= size) {
            System.out.println("[PRODUTTORE] Tutti i " + size + " veicoli occupati. Attendo...");
            wait();
        }

        String vinScelto;
        do {
            int numRandom = random.nextInt(size) + 1;
            vinScelto = "vin" + numRandom;
        } while (mappaNoleggi.containsValue(vinScelto));

        mappaNoleggi.put(userEmail, vinScelto);
        System.out.println("[PRODUTTORE] Inserito: " + userEmail + " -> " + vinScelto);

        notifyAll();
    }

    // Il Consumatore estrae dalla mappa che è già in tasca -> ZERO PARAMETRI!
    public synchronized void restituisciNoleggio() throws InterruptedException {
        while (mappaNoleggi.isEmpty()) {
            System.out.println("[CONSUMATORE] Archivio vuoto. Attendo...");
            wait();
        }

        String primaEmail = mappaNoleggi.keySet().iterator().next();
        String vinLiberato = mappaNoleggi.remove(primaEmail);
        System.out.println("[CONSUMATORE] Restituito: " + vinLiberato + " (di " + primaEmail + ")");

        notifyAll();
    }

    // Il Lettore legge la mappa che è già in tasca -> ZERO PARAMETRI!
    public synchronized void leggiNoleggiInAtto() {
        System.out.println("[LETTORE] Noleggi attivi (" + mappaNoleggi.size() + "/" + size + "): " + mappaNoleggi);
    }
}

class ThreadProduttore extends Thread {
    private ArchivioUtentiConcorrente archivio;
    private int progressivo = 1;

    public ThreadProduttore(ArchivioUtentiConcorrente archivio) {
        this.archivio = archivio;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 8; i++) {
                String email = "user" + (progressivo++) + "@gmail.com";
                archivio.inserisciNoleggio(email);
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class ThreadConsumatore extends Thread {
    private ArchivioUtentiConcorrente archivio;

    public ThreadConsumatore(ArchivioUtentiConcorrente archivio) {
        this.archivio = archivio;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 8; i++) {
                archivio.restituisciNoleggio();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class ThreadLettore extends Thread {
    private ArchivioUtentiConcorrente archivio;

    public ThreadLettore(ArchivioUtentiConcorrente archivio) {
        this.archivio = archivio;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                archivio.leggiNoleggiInAtto();
                Thread.sleep(700);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// ============================================================================
// 8. MAIN DI PROVA
// ============================================================================
public class EsameNoleggio {
    public static void main(String[] args) {
        System.out.println("================ TEST PARTE 1: OOP E ARCHIVIO ================");
        ArchivioAgenzia agenzia = new ArchivioAgenzia();

        agenzia.inserisciVeicolo(new Bici("Bianchi", 101, "2023-01-10", "city"));
        agenzia.inserisciVeicolo(new Auto("Fiat Panda", 102, "2022-05-15", "AA111BB", 5, 1200, 40.0));
        agenzia.inserisciVeicolo(new Auto("Fiat 500", 103, "2023-03-20", "CC222DD", 4, 1200, 55.0));
        agenzia.inserisciVeicolo(new FurgoneMerci("Iveco Daily", 104, "2021-09-01", "EE333FF", 15.0));
        agenzia.inserisciVeicolo(new FurgonePersone("Ford Transit", 105, "2022-11-11", "GG444HH", 9));

        agenzia.stampaTuttiIVeicoli();

        System.out.println("\n--- RICERCA AUTO (Cilindrata 1200, Costo max 45€) ---");
        List<Auto> autoTrovate = agenzia.cercaAuto(1200, 45.0);
        for (Auto a : autoTrovate) {
            System.out.println("Trovata: " + a);
        }

        System.out.println("\n--- TEST NOLEGGIO E RILASCIO ---");
        Utente u1 = new Utente("Mario Rossi", "mario@gmail.com");
        agenzia.noleggiaVeicolo(102, u1, 3);
        agenzia.stampaVeicoliNoleggiati();

        try {
            agenzia.rilasciaVeicolo(101); // Non è noleggiato -> lancia eccezione!
        } catch (NoleggioException e) {
            System.out.println("Eccezione catturata: " + e.getMessage());
        }

        agenzia.rilasciaVeicolo(102);

        System.out.println("\n================ TEST PARTE 2: MULTITHREADING ================");
        ArchivioUtentiConcorrente archivioConcorrente = new ArchivioUtentiConcorrente(3);

        ThreadProduttore prod = new ThreadProduttore(archivioConcorrente);
        ThreadConsumatore cons = new ThreadConsumatore(archivioConcorrente);
        ThreadLettore lett = new ThreadLettore(archivioConcorrente);

        prod.start();
        cons.start();
        lett.start();
    }
}

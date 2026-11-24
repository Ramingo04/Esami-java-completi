import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

// =========================================================================
// 1. ECCEZIONI PERSONALIZZATE (Ne chiede almeno 3)[span_9](start_span)[span_9](end_span)
// TRUCCHETTO: Estendi sempre RuntimeException -> zero obbligo di "throws"
// =========================================================================
class DatiNonValidiException extends RuntimeException {
    public DatiNonValidiException(String msg) { super(msg); }
}

class VincoloEtaException extends RuntimeException {
    public VincoloEtaException(String msg) { super(msg); }
}

class AssociazioneNonValidaException extends RuntimeException {
    public AssociazioneNonValidaException(String msg) { super(msg); }
}

// =========================================================================
// 2. INTERFACCIA CONVENZIONE[span_10](start_span)[span_10](end_span)
// TRUCCHETTO REGOLA DELLA TASCA:
// - setDiscount(double): salva la percentuale (1 parametro)[span_11](start_span)[span_11](end_span)
// - getDiscount(): legge la percentuale che ha già in tasca (0 parametri)[span_12](start_span)[span_12](end_span)
// =========================================================================
interface Convenzionato {
    void setDiscount(double discount);
    double getDiscount();
}

// =========================================================================
// 3. CLASSE PADRE ASTRATTA + SOTTOCLASSI CLIENTE[span_13](start_span)[span_13](end_span)
// TRUCCHETTO: Metti i controlli nei setter e richiamali nel costruttore,
// così qualsiasi modifica futura resta protetta da eccezione[span_14](start_span)[span_14](end_span)!
// =========================================================================
abstract class Cliente {
    private String nomeCompleto;
    private String codiceFiscale; // Chiave univoca[span_15](start_span)[span_15](end_span)
    private int eta;
    private String email;

    public Cliente(String nomeCompleto, String codiceFiscale, int eta, String email) {
        setNomeCompleto(nomeCompleto);
        setCodiceFiscale(codiceFiscale);
        setEta(eta);
        setEmail(email);
    }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            throw new DatiNonValidiException("Nome completo non valido");
        }
        this.nomeCompleto = nomeCompleto;
    }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            throw new DatiNonValidiException("Codice fiscale non valido");
        }
        this.codiceFiscale = codiceFiscale;
    }

    public int getEta() { return eta; }
    public void setEta(int eta) {
        if (eta < 0) {
            throw new DatiNonValidiException("Età negativa non ammessa");
        }
        this.eta = eta;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new DatiNonValidiException("Email non valida");
        }
        this.email = email;
    }

    // TRUCCHETTO NETBEANS: Insert Code -> equals() and hashCode()
    // Spunta SOLO codiceFiscale (chiave univoca) per confrontare due clienti.[span_16](start_span)[span_16](end_span)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(codiceFiscale, cliente.codiceFiscale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codiceFiscale);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{nome='" + nomeCompleto + "', CF='" + codiceFiscale + "', eta=" + eta + "}";
    }
}

// SOTTOCLASSE 1: Bambino (< 10 anni)[span_17](start_span)[span_17](end_span)
class Bambino extends Cliente {
    public Bambino(String nomeCompleto, String codiceFiscale, int eta, String email) {
        super(nomeCompleto, codiceFiscale, eta, email);
        if (eta >= 10) {
            throw new VincoloEtaException("Bambino deve avere età inferiore a 10 anni"); //[span_18](start_span)[span_18](end_span)
        }
    }
}

// SOTTOCLASSE 2: Ragazzo (tra 10 e 18 anni non compiuti)[span_19](start_span)[span_19](end_span)
class Ragazzo extends Cliente {
    public Ragazzo(String nomeCompleto, String codiceFiscale, int eta, String email) {
        super(nomeCompleto, codiceFiscale, eta, email);
        if (eta < 10 || eta >= 18) {
            throw new VincoloEtaException("Ragazzo deve avere età tra 10 e 17 anni"); //[span_20](start_span)[span_20](end_span)
        }
    }
}

// SOTTOCLASSE 2-BIS: Ragazzo in convenzione[span_21](start_span)[span_21](end_span)
class RagazzoConvenzionato extends Ragazzo implements Convenzionato {
    private String nomeConvenzione;
    private double discount;

    public RagazzoConvenzionato(String nomeCompleto, String codiceFiscale, int eta, String email, String nomeConvenzione, double discount) {
        super(nomeCompleto, codiceFiscale, eta, email);
        if (nomeConvenzione == null || nomeConvenzione.trim().isEmpty()) {
            throw new DatiNonValidiException("Nome convenzione non valido");
        }
        this.nomeConvenzione = nomeConvenzione;
        setDiscount(discount);
    }

    public String getNomeConvenzione() { return nomeConvenzione; }

    @Override
    public void setDiscount(double discount) {
        if (discount < 0 || discount > 100) {
            throw new DatiNonValidiException("Sconto percentuale non valido");
        }
        this.discount = discount;
    }

    @Override
    public double getDiscount() { return discount; }
}

// SOTTOCLASSE 3: Maggiorenne (>= 18 anni, unico con parcheggio)[span_22](start_span)[span_22](end_span)
class Maggiorenne extends Cliente {
    private boolean parking = false;

    public Maggiorenne(String nomeCompleto, String codiceFiscale, int eta, String email) {
        super(nomeCompleto, codiceFiscale, eta, email);
        if (eta < 18) {
            throw new VincoloEtaException("Maggiorenne deve avere almeno 18 anni"); //[span_23](start_span)[span_23](end_span)
        }
    }

    // Metodo richiesto dalla traccia[span_24](start_span)[span_24](end_span)
    public void getpass() {
        this.parking = true;
    }

    public boolean hasParking() {
        return parking;
    }
}

// SOTTOCLASSE 3-BIS: Maggiorenne in convenzione[span_25](start_span)[span_25](end_span)
class MaggiorenneConvenzionato extends Maggiorenne implements Convenzionato {
    private String nomeConvenzione;
    private double discount;

    public MaggiorenneConvenzionato(String nomeCompleto, String codiceFiscale, int eta, String email, String nomeConvenzione, double discount) {
        super(nomeCompleto, codiceFiscale, eta, email);
        if (nomeConvenzione == null || nomeConvenzione.trim().isEmpty()) {
            throw new DatiNonValidiException("Nome convenzione non valido");
        }
        this.nomeConvenzione = nomeConvenzione;
        setDiscount(discount);
    }

    public String getNomeConvenzione() { return nomeConvenzione; }

    @Override
    public void setDiscount(double discount) {
        if (discount < 0 || discount > 100) {
            throw new DatiNonValidiException("Sconto percentuale non valido");
        }
        this.discount = discount;
    }

    @Override
    public double getDiscount() { return discount; }
}

// =========================================================================
// 4. CLASSE ZONA (Con riconoscimento automatico tramite if/else if)[span_26](start_span)[span_26](end_span)
// TRUCCHETTO: Passi solo il nome della zona e il costruttore assegna il prezzo
// corretto da solo, lanciando eccezione se il nome è sbagliato!
// =========================================================================
class Zona {
    private String nome;
    private double prezzo;

    public Zona(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DatiNonValidiException("Nome zona mancante");
        }
        this.nome = nome;

        if (nome.equalsIgnoreCase("zona principale") || nome.equalsIgnoreCase("zona piscine e scivoli")) {
            this.prezzo = 10.0; //[span_27](start_span)[span_27](end_span)
        } else if (nome.equalsIgnoreCase("zona giostra") || nome.equalsIgnoreCase("zona baby")) {
            this.prezzo = 15.0; //[span_28](start_span)[span_28](end_span)
        } else if (nome.equalsIgnoreCase("zona percorsiAvanzati")) {
            this.prezzo = 20.0; //[span_29](start_span)[span_29](end_span)
        } else {
            throw new DatiNonValidiException("Zona non riconosciuta: " + nome);
        }
    }

    public String getNome() { return nome; }
    public double getPrezzo() { return prezzo; }

    @Override
    public String toString() {
        return nome + " (" + prezzo + "€)";
    }
}

// =========================================================================
// 5. CLASSE BIGLIETTO (Contiene List<Zona> + Comparable per TreeMap)[span_30](start_span)[span_30](end_span)[span_31](start_span)[span_31](end_span)
// =========================================================================
class Biglietto implements Comparable<Biglietto> {
    private static int contatoreSerie = 1; // ID progressivo automatico[span_32](start_span)[span_32](end_span)
    private int id;
    private LocalDate data;
    private List<Zona> zone; // Lista delle zone scelte dal cliente[span_33](start_span)[span_33](end_span)
    private double prezzoFinale;

    public Biglietto(LocalDate data) {
        if (data == null) {
            throw new DatiNonValidiException("Data biglietto non valida");
        }
        this.id = contatoreSerie++;
        this.data = data;
        this.zone = new ArrayList<>();
        // Accesso base incluso per tutti nella zona principale (10€)[span_34](start_span)[span_34](end_span)
        this.zone.add(new Zona("zona principale"));
    }

    public int getId() { return id; }
    public LocalDate getData() { return data; }
    public List<Zona> getZone() { return zone; }
    public double getPrezzoFinale() { return prezzoFinale; }

    // Aggiunta di una nuova zona (sia all'acquisto che dopo)[span_35](start_span)[span_35](end_span)
    public void aggiungiZona(Zona z, Cliente c) {
        if (z == null || c == null) {
            throw new DatiNonValidiException("Zona o cliente null");
        }
        // Controllo vincolo età per percorsiAvanzati[span_36](start_span)[span_36](end_span)
        if (z.getNome().equalsIgnoreCase("zona percorsiAvanzati") && c.getEta() < 18) {
            throw new VincoloEtaException("Zona percorsiAvanzati riservata ai maggiorenni!"); //[span_37](start_span)[span_37](end_span)
        }
        zone.add(z);
        aggiornaPrezzo(c);
    }

    // Calcolo prezzo totale: somma lista zone -> applica sconto -> aggiungi parcheggio[span_38](start_span)[span_38](end_span)
    public void aggiornaPrezzo(Cliente c) {
        double totale = 0; // Variabile d'appoggio per sommare la lista
        for (Zona z : zone) {
            totale += z.getPrezzo();
        }
        // Se il cliente implementa Convenzionato, uso getDiscount()[span_39](start_span)[span_39](end_span)
        if (c instanceof Convenzionato) {
            double sconto = ((Convenzionato) c).getDiscount();
            totale = totale * (1 - sconto / 100.0);
        }
        // Se è Maggiorenne e ha chiesto il pass parcheggio, aggiungo 4€[span_40](start_span)[span_40](end_span)
        if (c instanceof Maggiorenne && ((Maggiorenne) c).hasParking()) {
            totale += 4.0;
        }
        this.prezzoFinale = totale;
    }

    // Serve alla TreeMap per ordinare automaticamente in base all'ID biglietto[span_41](start_span)[span_41](end_span)
    @Override
    public int compareTo(Biglietto altro) {
        return Integer.compare(this.id, altro.id);
    }

    @Override
    public String toString() {
        return "Biglietto{id=" + id + ", data=" + data + ", zone=" + zone + ", prezzo=" + prezzoFinale + "€}";
    }
}

// =========================================================================
// 6. GESTORE PARCO GIOCHI (Mappa + Report)[span_42](start_span)[span_42](end_span)[span_43](start_span)[span_43](end_span)
// =========================================================================
class GestoreParco {
    // TRUCCHETTO TREEMAP: Ordina da sola le entry in base all'ID del Biglietto[span_44](start_span)[span_44](end_span)!
    private Map<Biglietto, Cliente> mappaBiglietti = new TreeMap<>();

    // Staccare un nuovo biglietto = inserire nella mappa con .put(chiave, valore)[span_45](start_span)[span_45](end_span)
    public void staccaBiglietto(Biglietto b, Cliente c) {
        if (b == null || c == null) {
            throw new AssociazioneNonValidaException("Biglietto o Cliente null");
        }
        // 1. Il biglietto non è cedibile (non può essere già in mappa)[span_46](start_span)[span_46](end_span)
        if (mappaBiglietti.containsKey(b)) {
            throw new AssociazioneNonValidaException("Biglietto già associato ad un altro cliente!");
        }
        // 2. Un cliente non può avere due biglietti nello stesso giorno[span_47](start_span)[span_47](end_span)
        for (Map.Entry<Biglietto, Cliente> entry : mappaBiglietti.entrySet()) {
            if (entry.getValue().equals(c) && entry.getKey().getData().equals(b.getData())) {
                throw new AssociazioneNonValidaException("Il cliente ha già un biglietto per questa data!");
            }
        }
        // 3. Verifica vincolo età sulle zone già presenti nel biglietto[span_48](start_span)[span_48](end_span)
        for (Zona z : b.getZone()) {
            if (z.getNome().equalsIgnoreCase("zona percorsiAvanzati") && c.getEta() < 18) {
                throw new VincoloEtaException("Cliente minorenne non ammesso a percorsiAvanzati!");
            }
        }

        b.aggiornaPrezzo(c);
        mappaBiglietti.put(b, c);
    }

    // Identificare il cliente dato il biglietto -> mappa.get(chiave)[span_49](start_span)[span_49](end_span)
    public Cliente getClienteDaBiglietto(Biglietto b) {
        Cliente trovato = mappaBiglietti.get(b);
        if (trovato == null) {
            throw new AssociazioneNonValidaException("Biglietto non trovato!");
        }
        return trovato;
    }

    // Stampa di tutte le entry in ordine di ID biglietto[span_50](start_span)[span_50](end_span)
    public void stampaEntryOrdinate() {
        for (Map.Entry<Biglietto, Cliente> entry : mappaBiglietti.entrySet()) {
            System.out.println(entry.getKey() + " ---> " + entry.getValue());
        }
    }

    // Prezzo medio dei biglietti venduti[span_51](start_span)[span_51](end_span)
    public double calcolaPrezzoMedio() {
        if (mappaBiglietti.isEmpty()) return 0.0;
        double somma = 0; // Variabile d'appoggio
        for (Biglietto b : mappaBiglietti.keySet()) {
            somma += b.getPrezzoFinale();
        }
        return somma / mappaBiglietti.size();
    }

    // Numero di clienti che hanno usufruito della zona percorsiAvanzati[span_52](start_span)[span_52](end_span)
    public int contaClientiPercorsiAvanzati() {
        int contatore = 0; // Variabile d'appoggio
        for (Biglietto b : mappaBiglietti.keySet()) {
            for (Zona z : b.getZone()) {
                if (z.getNome().equalsIgnoreCase("zona percorsiAvanzati")) {
                    contatore++;
                    break;
                }
            }
        }
        return contatore;
    }
}

// =========================================================================
// 7. PARTE 2: MONITOR E THREAD CONCORRENTI (STAMPINO FISSO)[span_53](start_span)[span_53](end_span)
// =========================================================================
class MonitorPercorsiAvanzati {
    public static final int MAX_CLIENTI = 5; //[span_54](start_span)[span_54](end_span)
    private List<String> clientiPresenti = new ArrayList<>();

    public synchronized void entra(String cliente) throws InterruptedException {
        while (clientiPresenti.size() >= MAX_CLIENTI) {
            wait(); // Pieno -> aspetto[span_55](start_span)[span_55](end_span)
        }
        clientiPresenti.add(cliente);
        System.out.println("[INGRESSO] " + cliente + " entrato | Presenti: " + clientiPresenti.size());
        notifyAll();
    }

    public synchronized void esci() throws InterruptedException {
        while (clientiPresenti.isEmpty()) {
            wait(); // Vuoto -> aspetto[span_56](start_span)[span_56](end_span)
        }
        int indiceCasuale = new Random().nextInt(clientiPresenti.size());
        String uscito = clientiPresenti.remove(indiceCasuale); //[span_57](start_span)[span_57](end_span)
        System.out.println("[USCITA] " + uscito + " uscito | Presenti: " + clientiPresenti.size());
        notifyAll();
    }

    public synchronized void svuota() {
        clientiPresenti.clear(); //[span_58](start_span)[span_58](end_span)
        System.out.println("[RESET] Zona svuotata completamente!");
        notifyAll(); //[span_59](start_span)[span_59](end_span)
    }

    public synchronized void leggiStato(String nomeLettore) {
        System.out.println("[" + nomeLettore + "] Elenco aggiornato: " + clientiPresenti); //[span_60](start_span)[span_60](end_span)
    }
}

class ThreadIngresso extends Thread {
    private MonitorPercorsiAvanzati monitor;
    private int progressivo = 1;

    public ThreadIngresso(String name, MonitorPercorsiAvanzati monitor) {
        super(name);
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String cliente = "Cliente_" + getName() + "_" + (progressivo++); //[span_61](start_span)[span_61](end_span)
                monitor.entra(cliente);
                Thread.sleep(100); //[span_62](start_span)[span_62](end_span)
            }
        } catch (InterruptedException e) { }
    }
}

class ThreadUscita extends Thread {
    private MonitorPercorsiAvanzati monitor;

    public ThreadUscita(String name, MonitorPercorsiAvanzati monitor) {
        super(name);
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                monitor.esci();
                Thread.sleep(300); //[span_63](start_span)[span_63](end_span)
            }
        } catch (InterruptedException e) { }
    }
}

class ThreadReset extends Thread {
    private MonitorPercorsiAvanzati monitor;

    public ThreadReset(String name, MonitorPercorsiAvanzati monitor) {
        super(name);
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(10000); //[span_64](start_span)[span_64](end_span)
                monitor.svuota();
            }
        } catch (InterruptedException e) { }
    }
}

class ThreadLettore extends Thread {
    private MonitorPercorsiAvanzati monitor;

    public ThreadLettore(String name, MonitorPercorsiAvanzati monitor) {
        super(name);
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(3000); //[span_65](start_span)[span_65](end_span)
                monitor.leggiStato(getName());
            }
        } catch (InterruptedException e) { }
    }
}

// =========================================================================
// 8. MAIN DI VERIFICA (PARTE 1 + PARTE 2)[span_66](start_span)[span_66](end_span)
// =========================================================================
public class EsameParcoGiochi {
    public static void main(String[] args) {
        // --- VERIFICA PARTE 1 ---[span_67](start_span)[span_67](end_span)
        GestoreParco parco = new GestoreParco();

        // Creo le zone passando solo il nome (il prezzo lo mette l'if/else di Zona!)[span_68](start_span)[span_68](end_span)
        Zona piscine = new Zona("zona piscine e scivoli");
        Zona giostra = new Zona("zona giostra");
        Zona baby = new Zona("zona baby");
        Zona avanzati = new Zona("zona percorsiAvanzati");

        Maggiorenne m1 = new Maggiorenne("Mario Rossi", "RSSMRA80A01C351K", 35, "mario@mail.it");
        m1.getpass(); // Attiva parcheggio (+4€)[span_69](start_span)[span_69](end_span)

        MaggiorenneConvenzionato m2 = new MaggiorenneConvenzionato("Laura Bianchi", "BNCLRA90A41C351Z", 28, "laura@mail.it", "UniCT", 20.0);
        RagazzoConvenzionato r1 = new RagazzoConvenzionato("Luca Verdi", "VRDLCU10A01C351X", 14, "luca@mail.it", "Scuola", 10.0);
        Bambino b1 = new Bambino("Sofia Neri", "NRESFO20A41C351Y", 6, "sofia@mail.it");

        Biglietto big1 = new Biglietto(LocalDate.now());
        big1.aggiungiZona(avanzati, m1);

        Biglietto big2 = new Biglietto(LocalDate.now());
        big2.aggiungiZona(piscine, m2);
        big2.aggiungiZona(avanzati, m2);

        Biglietto big3 = new Biglietto(LocalDate.now());
        big3.aggiungiZona(giostra, r1);

        Biglietto big4 = new Biglietto(LocalDate.now());
        big4.aggiungiZona(baby, b1);

        parco.staccaBiglietto(big1, m1);
        parco.staccaBiglietto(big2, m2);
        parco.staccaBiglietto(big3, r1);
        parco.staccaBiglietto(big4, b1);

        // Aggiunta zona in un secondo momento[span_70](start_span)[span_70](end_span)
        big3.aggiungiZona(piscine, r1);

        // Test eccezioni con try-catch[span_71](start_span)[span_71](end_span)
        try {
            big3.aggiungiZona(avanzati, r1); // Errore: ragazzo su percorsiAvanzati[span_72](start_span)[span_72](end_span)
        } catch (VincoloEtaException e) {
            System.out.println("Eccezione OK: " + e.getMessage());
        }

        try {
            parco.staccaBiglietto(new Biglietto(LocalDate.now()), m1); // Errore: stesso giorno[span_73](start_span)[span_73](end_span)
        } catch (AssociazioneNonValidaException e) {
            System.out.println("Eccezione OK: " + e.getMessage());
        }

        System.out.println("\n--- REPORT BIGLIETTI ---");
        parco.stampaEntryOrdinate();
        System.out.println("Cliente big1: " + parco.getClienteDaBiglietto(big1));
        System.out.println("Prezzo medio: " + parco.calcolaPrezzoMedio() + "€");
        System.out.println("Clienti percorsiAvanzati: " + parco.contaClientiPercorsiAvanzati());

        // --- VERIFICA PARTE 2 (THREAD) ---[span_74](start_span)[span_74](end_span)
        System.out.println("\n--- AVVIO SIMULAZIONE CONCORRENTE ---");
        MonitorPercorsiAvanzati monitor = new MonitorPercorsiAvanzati();

        new ThreadIngresso("Ingresso1", monitor).start(); //[span_75](start_span)[span_75](end_span)
        new ThreadIngresso("Ingresso2", monitor).start(); //[span_76](start_span)[span_76](end_span)
        new ThreadIngresso("Ingresso3", monitor).start(); //[span_77](start_span)[span_77](end_span)
        new ThreadUscita("Uscita1", monitor).start();     //[span_78](start_span)[span_78](end_span)
        new ThreadReset("Reset", monitor).start();        //[span_79](start_span)[span_79](end_span)
        new ThreadLettore("Lettore1", monitor).start();   //[span_80](start_span)[span_80](end_span)
        new ThreadLettore("Lettore2", monitor).start();   //[span_81](start_span)[span_81](end_span)
    }
}
import java.util.*;

// ============================================================================
// 1. ECCEZIONE UNICA (Per non perdere tempo a crearne 10 diverse)
// ============================================================================
class PalestraException extends RuntimeException {
    public PalestraException(String message) {
        super(message);
    }
}

// ============================================================================
// 2. GERARCHIA PERSONE (Persona, Impiegato, Istruttore, Utente)
// REGOLA DELLA TASCA: Controlla sempre se il dato ce l'hai già in "tasca" (attributi) 
// o se arriva da fuori (parametro). Niente "this" nei metodi normali!
// ============================================================================
abstract class Persona {
    private String nome;
    private int eta;
    private String codiceFiscale;

    public Persona(String nome, int eta, String codiceFiscale) {
        this.nome = nome;
        this.eta = eta;
        this.codiceFiscale = codiceFiscale;
    }

    public String getNome() { return nome; }
    public int getEta() { return eta; }
    public String getCodiceFiscale() { return codiceFiscale; }

    @Override
    public String toString() {
        return "Nome=" + nome + ", Eta=" + eta + ", CF=" + codiceFiscale;
    }
}

// --- IMPIEGATO ---
class Impiegato extends Persona {
    private String ruolo;
    private double stipendio;
    private int oreStraordinario;

    public Impiegato(String nome, int eta, String codiceFiscale, String ruolo, double stipendio) {
        super(nome, eta, codiceFiscale);
        this.ruolo = ruolo;
        this.stipendio = stipendio;
        this.oreStraordinario = 0;
    }

    // REGOLA DELLA TASCA: Le ore da accumulare arrivano da fuori -> PARAMETRO (int ore)!
    public void accumulaStraordinario(int ore) {
        if (ore > 0) {
            oreStraordinario += ore;
        }
    }

    // REGOLA DELLA TASCA: Stipendio e ore straordinario sono già in tasca -> ZERO PARAMETRI!
    public double riceviBustaPaga() {
        double bustaPaga = stipendio + (oreStraordinario * 10.0);
        oreStraordinario = 0; // Reset del campo come richiesto dalla traccia
        return bustaPaga;
    }

    @Override
    public String toString() {
        return "[IMPIEGATO] " + super.toString() + ", Ruolo=" + ruolo + ", Stipendio=" + stipendio + "€";
    }
}

// --- ISTRUTTORE ---
class Istruttore extends Persona {
    private double remunerazioneBase;
    private int ticketSupplenza;
    private List<Corso> corsiTitolarita; // Lista dei corsi di cui è titolare

    public Istruttore(String nome, int eta, String codiceFiscale, double remunerazioneBase) {
        super(nome, eta, codiceFiscale);
        this.remunerazioneBase = remunerazioneBase;
        this.ticketSupplenza = 0;
        this.corsiTitolarita = new ArrayList<>();
    }

    public void accumulaTicket(int n) {
        if (n > 0) {
            ticketSupplenza += n;
        }
    }

    // Metodo richiesto dalla traccia: insegno(...)
    public void insegno(Corso corso) {
        if (corso == null) {
            throw new PalestraException("Corso non valido!");
        }
        if (corsiTitolarita.contains(corso)) {
            throw new PalestraException("L'istruttore è già titolare di questo corso!");
        }
        corsiTitolarita.add(corso);
    }

    // Metodo richiesto dalla traccia: afferenza(...) restituisce il numero di turni a cui insegna
    public int afferenza() {
        int totaleTurni = 0;
        // REGOLA DELLA TASCA: Scorriamo la lista corsiTitolarita (List) -> for (Tipo x : lista)
        for (Corso c : corsiTitolarita) {
            totaleTurni += c.contaTurniDellIstruttore(this);
        }
        return totaleTurni;
    }

    // REGOLA DELLA TASCA: Dati in tasca -> ZERO PARAMETRI!
    public double riceviBustaPaga() {
        double bustaPaga = remunerazioneBase + (corsiTitolarita.size() * 200.0) + (ticketSupplenza * 10.0);
        ticketSupplenza = 0; // Reset del campo
        return bustaPaga;
    }

    @Override
    public String toString() {
        return "[ISTRUTTORE] " + super.toString() + ", CorsiTitolarita=" + corsiTitolarita.size();
    }
}

// --- UTENTE ---
class Utente extends Persona {
    private double abbonamento;
    private List<String> requisitiUtente; // Es: ["gestante", "studente"]
    private Corso corsoIscritto; // Per semplicità un utente può essere iscritto a un solo corso

    public Utente(String nome, int eta, String codiceFiscale, double abbonamento, List<String> requisitiUtente) {
        super(nome, eta, codiceFiscale);
        this.abbonamento = abbonamento;
        this.requisitiUtente = requisitiUtente;
        this.corsoIscritto = null;
    }

    // Metodo idoneo(...) richiesto dalla traccia
    public boolean idoneo(Corso corso) {
        // Controllo vincolo età del corso
        if (getEta() < corso.getEtaMinima() || getEta() > corso.getEtaMassima()) {
            return false;
        }
        // Controllo requisiti: l'utente deve possedere almeno i requisiti richiesti dal corso
        for (String reqCorso : corso.getRequisitiIdoneita()) {
            if (!requisitiUtente.contains(reqCorso)) {
                return false;
            }
        }
        return true;
    }

    // Metodo afferenza(...) per l'utente: restituisce il numero di turni a cui è iscritto
    public int afferenza(Corso corso) {
        if (corsoIscritto == corso) {
            return corso.contaTurniDellUtente(this);
        }
        return 0;
    }

    public Corso getCorsoIscritto() { return corsoIscritto; }
    public void setCorsoIscritto(Corso corsoIscritto) { this.corsoIscritto = corsoIscritto; }

    @Override
    public String toString() {
        return "[UTENTE] " + super.toString();
    }
}

// ============================================================================
// 3. CLASSE COMPOSIZIONE (Gestisce l'istruttore e la lista partecipanti per turno)
// ============================================================================
class Composizione {
    private Istruttore istruttore;
    private List<Utente> partecipanti;
    private int capacita;

    public Composizione(Istruttore istruttore, int capacita) {
        this.istruttore = istruttore;
        this.partecipanti = new ArrayList<>();
        this.capacita = capacita;
    }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) { this.istruttore = istruttore; }
    public List<Utente> getPartecipanti() { return partecipanti; }
    public int getCapacita() { return capacita; }
}

// ============================================================================
// 4. CLASSE CORSO
// ============================================================================
class Corso {
    private String nome;
    private int codiceUnivoco;
    private int capacitaCorso;
    private List<String> requisitiIdoneita;
    private int etaMinima;
    private int etaMassima;
    
    // Mappa dei turni: Chiave = Turno (es. String "Lunedì 10:00"), Valore = Composizione
    private Map<String, Composizione> turni;

    public Corso(String nome, int codiceUnivoco, int capacitaCorso, List<String> requisitiIdoneita, int etaMinima, int etaMassima) {
        this.nome = nome;
        this.codiceUnivoco = codiceUnivoco;
        this.capacitaCorso = capacitaCorso;
        this.requisitiIdoneita = requisitiIdoneita;
        this.etaMinima = etaMinima;
        this.etaMassima = etaMassima;
        this.turni = new HashMap<>();
    }

    // Aggiungere un turno specificando un istruttore di riferimento
    public void aggiungiTurno(String turnoKey, Istruttore istruttore) {
        if (turni.containsKey(turnoKey)) {
            throw new PalestraException("Il turno esiste già!");
        }
        turni.put(turnoKey, new Composizione(istruttore, capacitaCorso));
    }

    // Cancellare un turno
    public void cancellaTurno(String turnoKey) {
        if (!turni.containsKey(turnoKey)) {
            throw new PalestraException("Turno non trovato!");
        }
        turni.remove(turnoKey);
    }

    // Verificare la presenza di un utente in base al codice fiscale
    public boolean verificaUtentePerCF(String codiceFiscale) {
        // REGOLA DELLA TASCA: turni è una Map -> usiamo .values() per scorrerla
        for (Composizione comp : turni.values()) {
            for (Utente u : comp.getPartecipanti()) {
                if (u.getCodiceFiscale().equals(codiceFiscale)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Inserire un utente in uno specifico turno (metodo iscrivi richiesto)
    public void iscriviUtente(String turnoKey, Utente utente) {
        Composizione comp = turni.get(turnoKey);
        if (comp == null) {
            throw new PalestraException("Turno inesistente!");
        }
        // Controllo idoneità e spazio
        if (!utente.idoneo(this)) {
            throw new PalestraException("L'utente non possiede i requisiti o l'età per questo corso!");
        }
        if (comp.getPartecipanti().size() >= comp.getCapacita()) {
            throw new PalestraException("Capacità massima del turno raggiunta!");
        }
        if (comp.getPartecipanti().contains(utente)) {
            throw new PalestraException("L'utente è già iscritto a questo turno!");
        }
        if (utente.getCorsoIscritto() != null && utente.getCorsoIscritto() != this) {
            throw new PalestraException("L'utente è già iscritto a un altro corso!");
        }

        comp.getPartecipanti().add(utente);
        utente.setCorsoIscritto(this);
    }

    // Cancellare un utente da un turno
    public void rimuoviUtente(String turnoKey, Utente utente) {
        Composizione comp = turni.get(turnoKey);
        if (comp == null) {
            throw new PalestraException("Turno inesistente!");
        }
        boolean rimosso = comp.getPartecipanti().remove(utente);
        if (rimosso && comp.getPartecipanti().isEmpty()) {
            utente.setCorsoIscritto(null);
        }
    }

    // Cambiare istruttore in un turno
    public void cambiaIstruttore(String turnoKey, Istruttore nuovoIstruttore) {
        Composizione comp = turni.get(turnoKey);
        if (comp == null) {
            throw new PalestraException("Turno inesistente!");
        }
        comp.setIstruttore(nuovoIstruttore);
    }

    public int contaTurniDellIstruttore(Istruttore istruttore) {
        int count = 0;
        for (Composizione comp : turni.values()) {
            if (comp.getIstruttore() == istruttore) {
                count++;
            }
        }
        return count;
    }

    public int contaTurniDellUtente(Utente utente) {
        int count = 0;
        for (Composizione comp : turni.values()) {
            if (comp.getPartecipanti().contains(utente)) {
                count++;
            }
        }
        return count;
    }

    // Stampare il valore corrente di tutti i turni, l'istruttore e numero utenti
    public void stampaStatoCorso() {
        System.out.println("=== CORSO: " + nome + " (Codice: " + codiceUnivoco + ") ===");
        // REGOLA DELLA TASCA: Scorriamo la Map 'turni' usando .entrySet() per avere chiave e valore
        for (Map.Entry<String, Composizione> entry : turni.entrySet()) {
            String turno = entry.getKey();
            Composizione comp = entry.getValue();
            System.out.println(" Turno: " + turno + 
                               " | Istruttore: " + comp.getIstruttore().getNome() + 
                               " | Iscritti: " + comp.getPartecipanti().size() + "/" + comp.getCapacita());
        }
    }

    public List<String> getRequisitiIdoneita() { return requisitiIdoneita; }
    public int getEtaMinima() { return etaMinima; }
    public int getEtaMassima() { return etaMassima; }
    public String getNome() { return nome; }
}

// ============================================================================
// 5. MONITOR MAP & MULTITHREADING (Richiesto dalla seconda parte della traccia)
// MonitorMap<Integer, ComposizioneOraria> con chiavi 0-9 e MAX=10 iscritti
// ============================================================================
class ComposizioneOraria {
    private String codiceIstruttore; // "I" + index (0-3)
    private int numeroIscritti;
    private final int MAX = 10;

    public ComposizioneOraria(String codiceIstruttore, int numeroIscritti) {
        this.codiceIstruttore = codiceIstruttore;
        this.numeroIscritti = Math.min(numeroIscritti, MAX);
    }

    public String getCodiceIstruttore() { return codiceIstruttore; }
    public void setCodiceIstruttore(String codiceIstruttore) { this.codiceIstruttore = codiceIstruttore; }
    public int getNumeroIscritti() { return numeroIscritti; }
    public void setNumeroIscritti(int numeroIscritti) { this.numeroIscritti = Math.min(numeroIscritti, MAX); }

    @Override
    public String toString() {
        return "[Istruttore=" + codiceIstruttore + ", Iscritti=" + numeroIscritti + "/" + MAX + "]";
    }
}

class MonitorMap {
    private Map<Integer, ComposizioneOraria> mappaTurni = new HashMap<>();
    private Random random = new Random();

    // Writer: inserisce o aggiorna un turno (chiave 0-9) con un istruttore random ("I0"-"I3")
    public synchronized void writer() {
        int turno = random.nextInt(10); // 0-9
        String istruttore = "I" + random.nextInt(4); // "I0" a "I3"
        
        ComposizioneOraria comp = mappaTurni.get(turno);
        if (comp == null) {
            mappaTurni.put(turno, new ComposizioneOraria(istruttore, 0));
            System.out.println("[WRITER] Creato turno " + turno + conIstruttore(istruttore));
        } else {
            comp.setCodiceIstruttore(istruttore);
            System.out.println("[WRITER] Aggiornato turno " + turno + conIstruttore(istruttore));
        }
        notifyAll();
    }

    private String conIstruttore(String ist) { return " con istruttore " + ist; }

    // Producer: incrementa il numero iscritti se possibile, altrimenti si blocca in attesa
    public synchronized void producer() throws InterruptedException {
        while (mappaTurni.isEmpty()) {
            wait(); // Aspetta che ci sia almeno un turno creato da un Writer
        }
        // Prende un turno a caso tra quelli esistenti
        List<Integer> turniEsistenti = new ArrayList<>(mappaTurni.keySet());
        int turnoScelto = turniEsistenti.get(random.nextInt(turniEsistenti.size()));
        ComposizioneOraria comp = mappaTurni.get(turnoScelto);

        while (comp.getNumeroIscritti() >= 10) { // MAX = 10
            wait(); // Si blocca in attesa se il corso è pieno
        }

        comp.setNumeroIscritti(comp.getNumeroIscritti() + 1);
        System.out.println("[PRODUCER] Iscrito utente al turno " + turnoScelto + ". Totale: " + comp.getNumeroIscritti());
        notifyAll();
    }

    // Consumer: decrementa il numero iscritti se possibile, altrimenti si blocca
    public synchronized void consumer() throws InterruptedException {
        while (mappaTurni.isEmpty()) {
            wait();
        }
        List<Integer> turniEsistenti = new ArrayList<>(mappaTurni.keySet());
        int turnoScelto = turniEsistenti.get(random.nextInt(turniEsistenti.size()));
        ComposizioneOraria comp = mappaTurni.get(turnoScelto);

        while (comp.getNumeroIscritti() <= 0) {
            wait(); // Si blocca se non ci sono iscritti da rimuovere
        }

        comp.setNumeroIscritti(comp.getNumeroIscritti() - 1);
        System.out.println("[CONSUMER] Rimossa iscrizione dal turno " + turnoScelto + ". Rimasti: " + comp.getNumeroIscritti());
        notifyAll();
    }

    // Eraser: azzera la configurazione del corso (pulisce la mappa)
    public synchronized void eraser() {
        mappaTurni.clear();
        System.out.println("[ERASER] Configurazione del corso azzerata!");
        notifyAll();
    }

    // Reader: legge la composizione del corso
    public synchronized void reader() {
        System.out.println("[READER] Stato MonitorMap: " + mappaTurni);
    }
}

// --- I THREAD DELLO STAFF ---
class StaffWriter extends Thread {
    private MonitorMap monitor;
    public StaffWriter(MonitorMap monitor) { this.monitor = monitor; }
    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                monitor.writer();
                Thread.sleep(400);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StaffProducer extends Thread {
    private MonitorMap monitor;
    public StaffProducer(MonitorMap monitor) { this.monitor = monitor; }
    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                monitor.producer();
                Thread.sleep(300);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StaffConsumer extends Thread {
    private MonitorMap monitor;
    public StaffConsumer(MonitorMap monitor) { this.monitor = monitor; }
    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                monitor.consumer();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StaffReader extends Thread {
    private MonitorMap monitor;
    public StaffReader(MonitorMap monitor) { this.monitor = monitor; }
    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                monitor.reader();
                Thread.sleep(600);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

// ============================================================================
// 6. CLASSE MAIN DI PROVA
// ============================================================================
public class EsamePalestra {
    public static void main(String[] args) {
        System.out.println("================ TEST PARTE 1: PALESTRA E CORSI ================");
        
        // 1. Creiamo un istruttore e un utente
        Istruttore ist = new Istruttore("Mario Rossi", 35, "RSSMRA85A01H501Z", 1200.0);
        
        List<String> reqUtente = new ArrayList<>();
        reqUtente.add("studente");
        Utente utente = new Utente("Luigi Verdi", 22, "VRDLGU04B22Z100Y", 50.0, reqUtente);

        // 2. Creiamo un corso con requisiti ("studente") ed età (18-50)
        List<String> reqCorso = new ArrayList<>();
        reqCorso.add("studente");
        Corso pilates = new Corso("Pilates", 1, 2, reqCorso, 18, 50);

        // 3. L'istruttore prende titolarità del corso
        ist.insegno(pilates);

        // 4. Aggiungiamo un turno
        pilates.aggiungiTurno("Lunedì 10:00", ist);

        // 5. Iscriviamo l'utente al turno
        pilates.iscriviUtente("Lunedì 10:00", utente);

        // 6. Stampiamo lo stato del corso
        pilates.stampaStatoCorso();

        // Test busta paga impiegato
        Impiegato imp = new Impiegato("Anna Neri", 40, "NRENN48C41Z100W", "Segretaria", 1000.0);
        imp.accumulaStraordinario(5);
        System.out.println("Busta paga impiegato: " + imp.riceviBustaPaga() + "€");

        System.out.println("\n================ TEST PARTE 2: MULTITHREADING MONITOR MAP ================");
        MonitorMap monitor = new MonitorMap();

        StaffWriter w = new StaffWriter(monitor);
        StaffProducer p = new StaffProducer(monitor);
        StaffConsumer c = new StaffConsumer(monitor);
        StaffReader r = new StaffReader(monitor);

        w.start();
        p.start();
        c.start();
        r.start();
    }
}

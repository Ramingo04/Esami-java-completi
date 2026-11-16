import java.time.LocalDate;
import java.util.*;

// ============================================================================
// 1. ECCEZIONE UNICA
// ============================================================================
class StoreException extends RuntimeException {
    public StoreException(String message) {
        super(message);
    }
}

// ============================================================================
// 2. CLASSE UTENTE
// ============================================================================
class User {
    private String nome;
    private String codiceFiscale;
    private String email;

    public User(String nome, String codiceFiscale, String email) {
        this.nome = nome;
        this.codiceFiscale = codiceFiscale;
        this.email = email;
    }

    public String getNome() { return nome; }
    public String getCodiceFiscale() { return codiceFiscale; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nome + " (CF: " + codiceFiscale + ", Email: " + email + ")";
    }
}

// ============================================================================
// 3. CLASSI DI SUPPORTO PER PRESTITI E LICENZE
// ============================================================================
class Prestito {
    private User user;
    private String startDate;
    private int duration; // in giorni

    public Prestito(User user, String startDate, int duration) {
        this.user = user;
        this.startDate = startDate;
        this.duration = duration;
    }

    public User getUser() { return user; }
    public String getStartDate() { return startDate; }
    public int getDuration() { return duration; }

    @Override
    public String toString() {
        return "Utente: " + user.getNome() + ", Inizio: " + startDate + ", Durata: " + duration + " giorni";
    }
}

class Licenza {
    private User user;
    private LocalDate expiringDate;

    public Licenza(User user, LocalDate expiringDate) {
        this.user = user;
        this.expiringDate = expiringDate;
    }

    public User getUser() { return user; }
    public LocalDate getExpiringDate() { return expiringDate; }

    @Override
    public String toString() {
        return "Utente: " + user.getNome() + ", Scadenza: " + expiringDate;
    }
}

// ============================================================================
// 4. INTERFACCE PER GESTIONE PRESTITI E CONDIVISIONI
// ============================================================================
interface Prestabile {
    boolean presta(User user, String startDate, int duration);
    boolean restituisci(User user);
    void sollecitoRecupero();
    ArrayList<Prestito> mostraPrestiti();
    int getCopieDisponibili();
    void incrementaCopie(int quantita);
}

interface Condivisibile {
    void condividi(User user, LocalDate expiringDate);
    boolean revocaCondivisione(User user);
    void refresh();
    int licenzeDisponibili();
    void incrementaLicenze(int quantita);
}

interface Matchable {
    boolean matchTitle(String title);
}

// ============================================================================
// 5. CLASSE ASTRATTA PRODOTTO
// ============================================================================
abstract class Prodotto implements Matchable {
    private int id; // Assegnato dall'inventario (>0)
    private final String chiave; // Fornita in fase di creazione (ISBN, ISWC o ISAN)

    public Prodotto(int id, String chiave) {
        if (id <= 0) {
            throw new StoreException("L'id del prodotto deve essere maggiore di zero!");
        }
        this.id = id;
        this.chiave = chiave;
    }

    public int getId() { return id; }
    public String getChiave() { return chiave; }

    // Metodo astratto richiesto dalla traccia: la stringa restituita cambia in base al tipo
    public abstract String getTitle();

    @Override
    public String toString() {
        return "ID=" + id + ", Chiave=" + chiave + ", Titolo=" + getTitle();
    }
}

// ============================================================================
// 6. SOTTOCLASSI DI PRODOTTO: LIBRI (Cartacei o Digitali)
// ============================================================================
class Libro extends Prodotto implements Matchable {
    private String press;
    private int pages;
    private List<String> autori;
    private String titolo;
    private boolean cartaceo; // true = prestabile, false = condivisibile (e-book)

    // Per prestabile (Cartaceo)
    private int copieDisponibili;
    private ArrayList<Prestito> prestiti;

    // Per condivisibile (Digitale / E-book)
    private int maxLicenze;
    private List<Licenza> licenzeAttive;

    public Libro(int id, String isbn, String press, int pages, List<String> autori, String titolo, boolean cartaceo, int quantitaIniziale) {
        super(id, isbn);
        this.press = press;
        this.pages = pages;
        this.autori = autori;
        this.titolo = titolo;
        this.cartaceo = cartaceo;
        if (cartaceo) {
            this.copieDisponibili = quantitaIniziale;
            this.prestiti = new ArrayList<>();
        } else {
            this.maxLicenze = quantitaIniziale;
            this.licenzeAttive = new ArrayList<>();
        }
    }

    @Override
    public String getTitle() {
        return titolo;
    }

    @Override
    public boolean matchTitle(String title) {
        return titolo.toLowerCase().contains(title.toLowerCase());
    }

    public boolean isCartaceo() { return cartaceo; }

    // Se cartaceo implementa Prestabile
    public boolean presta(User user, String startDate, int duration) {
        if (!cartaceo || copieDisponibili <= 0) return false;
        prestiti.add(new Prestito(user, startDate, duration));
        copieDisponibili--;
        return true;
    }

    public boolean restituisci(User user) {
        Prestito daRimuovere = null;
        for (Prestito p : prestiti) {
            if (p.getUser().equals(user)) {
                daRimuovere = p;
                break;
            }
        }
        if (daRimuovere != null) {
            prestiti.remove(daRimuovere);
            copieDisponibili++;
            System.out.println("RESTITUZIONE EFFETTUATA - Info risorsa: " + this);
            return true;
        }
        return false;
    }

    public void sollecitoRecupero() {
        for (Prestito p : prestiti) {
            System.out.println("EMAIL SOLLECITO inviata a " + p.getUser().getEmail() + " per il libro prestato: " + titolo);
        }
    }

    public ArrayList<Prestito> mostraPrestiti() {
        return prestiti;
    }

    public int getCopieDisponibili() { return copieDisponibili; }
    public void incrementaCopie(int quantita) { this.copieDisponibili += quantita; }

    // Se digitale implementa Condivisibile
    public void condividi(User user, LocalDate expiringDate) {
        if (cartaceo || licenzeDisponibili() <= 0) {
            throw new StoreException("Impossibile condividere e-book: licenze esaurite o formato cartaceo.");
        }
        licenzeAttive.add(new Licenza(user, expiringDate));
    }

    public boolean revocaCondivisione(User user) {
        Licenza daRimuovere = null;
        for (Licenza l : licenzeAttive) {
            if (l.getUser().equals(user)) {
                daRimuovere = l;
                break;
            }
        }
        if (daRimuovere != null) {
            licenzeAttive.remove(daRimuovere);
            return true;
        }
        return false;
    }

    public void refresh() {
        LocalDate oggi = LocalDate.now();
        licenzeAttive.removeIf(l -> l.getExpiringDate().isBefore(oggi));
    }

    public int licenzeDisponibili() {
        return maxLicenze - licenzeAttive.size();
    }

    public void incrementaLicenze(int quantita) {
        this.maxLicenze += quantita;
    }
}

// ============================================================================
// 7. SOTTOCLASSE: MUSICA (CD o AudioRecord)
// ============================================================================
class Musica extends Prodotto implements Matchable {
    private String recordCompany;
    private int duration; // in secondi
    private List<String> titoliBrani;
    private String nomeArtista; // cantante, band, orchestra, musicista
    private String tipoMusica; // classica, jazz, opera
    private boolean cdFisico; // true = prestabile, false = condivisibile (AudioRecord)

    private int copieDisponibili;
    private ArrayList<Prestito> prestiti;

    private int maxLicenze;
    private List<Licenza> licenzeAttive;

    public Musica(int id, String iswc, String recordCompany, int duration, List<String> titoliBrani, String nomeArtista, String tipoMusica, boolean cdFisico, int quantitaIniziale) {
        super(id, iswc);
        this.recordCompany = recordCompany;
        this.duration = duration;
        this.titoliBrani = titoliBrani;
        this.nomeArtista = nomeArtista;
        this.tipoMusica = tipoMusica;
        this.cdFisico = cdFisico;
        if (cdFisico) {
            this.copieDisponibili = quantitaIniziale;
            this.prestiti = new ArrayList<>();
        } else {
            this.maxLicenze = quantitaIniziale;
            this.licenzeAttive = new ArrayList<>();
        }
    }

    @Override
    public String getTitle() {
        // Traccia: "titolo dell'album o della composizione principale"
        return titoliBrani.isEmpty() ? "Album senza titolo" : titoliBrani.get(0);
    }

    @Override
    public boolean matchTitle(String title) {
        if (getTitle().toLowerCase().contains(title.toLowerCase())) return true;
        for (String brano : titoliBrani) {
            if (brano.toLowerCase().contains(title.toLowerCase())) return true;
        }
        return false;
    }

    public boolean isCdFisico() { return cdFisico; }

    // Metodi Prestabile
    public boolean presta(User user, String startDate, int duration) {
        if (!cdFisico || copieDisponibili <= 0) return false;
        prestiti.add(new Prestito(user, startDate, duration));
        copieDisponibili--;
        return true;
    }

    public boolean restituisci(User user) {
        Prestito daRimuovere = null;
        for (Prestito p : prestiti) {
            if (p.getUser().equals(user)) {
                daRimuovere = p;
                break;
            }
        }
        if (daRimuovere != null) {
            prestiti.remove(daRimuovere);
            copieDisponibili++;
            System.out.println("RESTITUZIONE EFFETTUATA - Info risorsa: " + this);
            return true;
        }
        return false;
    }

    public void sollecitoRecupero() {
        for (Prestito p : prestiti) {
            System.out.println("EMAIL SOLLECITO inviata a " + p.getUser().getEmail() + " per il CD prestato: " + getTitle());
        }
    }

    public ArrayList<Prestito> mostraPrestiti() { return prestiti; }
    public int getCopieDisponibili() { return copieDisponibili; }
    public void incrementaCopie(int quantita) { this.copieDisponibili += quantita; }

    // Metodi Condivisibile
    public void condividi(User user, LocalDate expiringDate) {
        if (cdFisico || licenzeDisponibili() <= 0) {
            throw new StoreException("Impossibile condividere AudioRecord: licenze esaurite o supporto fisico.");
        }
        licenzeAttive.add(new Licenza(user, expiringDate));
    }

    public boolean revocaCondivisione(User user) {
        Licenza daRimuovere = null;
        for (Licenza l : licenzeAttive) {
            if (l.getUser().equals(user)) {
                daRimuovere = l;
                break;
            }
        }
        if (daRimuovere != null) {
            licenzeAttive.remove(daRimuovere);
            return true;
        }
        return false;
    }

    public void refresh() {
        LocalDate oggi = LocalDate.now();
        licenzeAttive.removeIf(l -> l.getExpiringDate().isBefore(oggi));
    }

    public int licenzeDisponibili() { return maxLicenze - licenzeAttive.size(); }
    public void incrementaLicenze(int quantita) { this.maxLicenze += quantita; }
}

// ============================================================================
// 8. SOTTOCLASSE: VIDEO (DVD o VideoRecorded)
// ============================================================================
class Video extends Prodotto implements Matchable {
    private String tipoVideo; // film, documentario, registrazione evento
    private String genere; // Letterario, giallo, storico, sci fi...
    private String producerCompany;
    private int duration; // in secondi
    private String regista;
    private String casaProduttrice;
    private String altreInfo; // trama, attori...
    private boolean dvdFisico; // true = prestabile, false = condivisibile (MovieRecord)

    private int copieDisponibili;
    private ArrayList<Prestito> prestiti;

    private int maxLicenze;
    private List<Licenza> licenzeAttive;

    public Video(int id, String isan, String tipoVideo, String genere, String producerCompany, int duration, String regista, String casaProduttrice, String altreInfo, boolean dvdFisico, int quantitaIniziale) {
        super(id, isan);
        this.tipoVideo = tipoVideo;
        this.genere = genere;
        this.producerCompany = producerCompany;
        this.duration = duration;
        this.regista = regista;
        this.casaProduttrice = casaProduttrice;
        this.altreInfo = altreInfo;
        this.dvdFisico = dvdFisico;
        if (dvdFisico) {
            this.copieDisponibili = quantitaIniziale;
            this.prestiti = new ArrayList<>();
        } else {
            this.maxLicenze = quantitaIniziale;
            this.licenzeAttive = new ArrayList<>();
        }
    }

    @Override
    public String getTitle() {
        // Traccia: "i video hanno un titolo del film" (usiamo altreInfo o producerCompany come titolo identificativo se non specificato separatamente, oppure passiamolo)
        return "Film di " + regista + " (" + genere + ")";
    }

    @Override
    public boolean matchTitle(String title) {
        return getTitle().toLowerCase().contains(title.toLowerCase()) || altreInfo.toLowerCase().contains(title.toLowerCase());
    }

    public boolean isDvdFisico() { return dvdFisico; }

    // Metodi Prestabile
    public boolean presta(User user, String startDate, int duration) {
        if (!dvdFisico || copieDisponibili <= 0) return false;
        prestiti.add(new Prestito(user, startDate, duration));
        copieDisponibili--;
        return true;
    }

    public boolean restituisci(User user) {
        Prestito daRimuovere = null;
        for (Prestito p : prestiti) {
            if (p.getUser().equals(user)) {
                daRimuovere = p;
                break;
            }
        }
        if (daRimuovere != null) {
            prestiti.remove(daRimuovere);
            copieDisponibili++;
            System.out.println("RESTITUZIONE EFFETTUATA - Info risorsa: " + this);
            return true;
        }
        return false;
    }

    public void sollecitoRecupero() {
        for (Prestito p : prestiti) {
            System.out.println("EMAIL SOLLECITO inviata a " + p.getUser().getEmail() + " per il DVD prestato.");
        }
    }

    public ArrayList<Prestito> mostraPrestiti() { return prestiti; }
    public int getCopieDisponibili() { return copieDisponibili; }
    public void incrementaCopie(int quantita) { this.copieDisponibili += quantita; }

    // Metodi Condivisibile
    public void condividi(User user, LocalDate expiringDate) {
        if (dvdFisico || licenzeDisponibili() <= 0) {
            throw new StoreException("Impossibile condividere MovieRecord: licenze esaurite o supporto fisico.");
        }
        licenzeAttive.add(new Licenza(user, expiringDate));
    }

    public boolean revocaCondivisione(User user) {
        Licenza daRimuovere = null;
        for (Licenza l : licenzeAttive) {
            if (l.getUser().equals(user)) {
                daRimuovere = l;
                break;
            }
        }
        if (daRimuovere != null) {
            licenzeAttive.remove(daRimuovere);
            return true;
        }
        return false;
    }

    public void refresh() {
        LocalDate oggi = LocalDate.now();
        licenzeAttive.removeIf(l -> l.getExpiringDate().isBefore(oggi));
    }

    public int licenzeDisponibili() { return maxLicenze - licenzeAttive.size(); }
    public void incrementaLicenze(int quantita) { this.maxLicenze += quantita; }
}

// ============================================================================
// 9. MULTIMEDIA STORE (Gestione dell'Inventario)
// ============================================================================
class MultimediaStore {
    private ArrayList<Libro> libri = new ArrayList<>();
    private ArrayList<Musica> musicaList = new ArrayList<>();
    private ArrayList<Video> videoList = new ArrayList<>();
    private int prossimoId = 1; // ID incrementale automatico

    // Aggiunta (add) di una nuova risorsa. 
    // Traccia: "se l'elemento aggiunto ha lo stesso tipo e la stessa chiave primaria va ad accrescere il numero di risorse materiali o di licenze"
    public void addProdotto(Prodotto p, int quantitaAggiuntiva) {
        if (p instanceof Libro) {
            Libro nuovo = (Libro) p;
            for (Libro l : libri) {
                if (l.getChiave().equalsIgnoreCase(nuovo.getChiave()) && l.isCartaceo() == nuovo.isCartaceo()) {
                    if (l.isCartaceo()) l.incrementaCopie(quantitaAggiuntiva);
                    else l.incrementaLicenze(quantitaAggiuntiva);
                    System.out.println("Risorsa esistente aggiornata con nuove copie/licenze.");
                    return;
                }
            }
            libri.add(nuovo);
        } else if (p instanceof Musica) {
            Musica nuovo = (Musica) p;
            for (Musica m : musicaList) {
                if (m.getChiave().equalsIgnoreCase(nuovo.getChiave()) && m.isCdFisico() == nuovo.isCdFisico()) {
                    if (m.isCdFisico()) m.incrementaCopie(quantitaAggiuntiva);
                    else m.incrementaLicenze(quantitaAggiuntiva);
                    System.out.println("Risorsa musicale esistente aggiornata.");
                    return;
                }
            }
            musicaList.add(nuovo);
        } else if (p instanceof Video) {
            Video nuovo = (Video) p;
            for (Video v : videoList) {
                if (v.getChiave().equalsIgnoreCase(nuovo.getChiave()) && v.isDvdFisico() == nuovo.isDvdFisico()) {
                    if (v.isDvdFisico()) v.incrementaCopie(quantitaAggiuntiva);
                    else v.incrementaLicenze(quantitaAggiuntiva);
                    System.out.println("Risorsa video esistente aggiornata.");
                    return;
                }
            }
            videoList.add(nuovo);
        }
    }

    public int generaId() {
        return prossimoId++;
    }

    // Ricerca (find) in base a tipo di risorsa e chiave primaria
    public Prodotto findByTipoEChiave(String tipo, String chiave) {
        if (tipo.equalsIgnoreCase("libro")) {
            for (Libro l : libri) {
                if (l.getChiave().equalsIgnoreCase(chiave)) return l;
            }
        } else if (tipo.equalsIgnoreCase("musica")) {
            for (Musica m : musicaList) {
                if (m.getChiave().equalsIgnoreCase(chiave)) return m;
            }
        } else if (tipo.equalsIgnoreCase("video")) {
            for (Video v : videoList) {
                if (v.getChiave().equalsIgnoreCase(chiave)) return v;
            }
        }
        return null;
    }

    // Ricerca per ID
    public Prodotto findById(int id) {
        for (Libro l : libri) if (l.getId() == id) return l;
        for (Musica m : musicaList) if (m.getId() == id) return m;
        for (Video v : videoList) if (v.getId() == id) return v;
        return null;
    }

    // Ricerca di tutte le risorse con lo stesso titolo (restituisce una lista)
    public ArrayList<Prodotto> findByTitle(String title) {
        ArrayList<Prodotto> risultati = new ArrayList<>();
        for (Libro l : libri) {
            if (l.matchTitle(title)) risultati.add(l);
        }
        for (Musica m : musicaList) {
            if (m.matchTitle(title)) risultati.add(m);
        }
        for (Video v : videoList) {
            if (v.matchTitle(title)) risultati.add(v);
        }
        return risultati;
    }

    // Stampa del contenuto dell'inventario
    public void stampaInventario() {
        System.out.println("================ INVENTARIO MULTIMEDIA STORE ================");
        System.out.println("--- LIBRI ---");
        for (Libro l : libri) {
            System.out.println(l + " | Tipo: " + (l.isCartaceo() ? "Cartaceo (Copie: " + l.getCopieDisponibili() + ")" : "E-book (Licenze disp: " + l.licenzeDisponibili() + ")"));
        }
        System.out.println("--- MUSICA ---");
        for (Musica m : musicaList) {
            System.out.println(m + " | Tipo: " + (m.isCdFisico() ? "CD Fisico (Copie: " + m.getCopieDisponibili() + ")" : "AudioRecord (Licenze disp: " + m.licenzeDisponibili() + ")"));
        }
        System.out.println("--- VIDEO ---");
        for (Video v : videoList) {
            System.out.println(v + " | Tipo: " + (v.isDvdFisico() ? "DVD Fisico (Copie: " + v.getCopieDisponibili() + ")" : "MovieRecord (Licenze disp: " + v.licenzeDisponibili() + ")"));
        }
        System.out.println("=============================================================");
    }
}

// ============================================================================
// 10. CLASSE MAIN DI PROVA
// ============================================================================
public class EsameMultimedia {
    public static void main(String[] args) {
        System.out.println("=== INIZIO TEST MULTIMEDIA STORE ===");
        MultimediaStore store = new MultimediaStore();

        // 1. Creazione e inserimento di una risorsa per ogni tipologia
        Libro libroCartaceo = new Libro(store.generaId(), "ISBN-001", "Mondadori", 300, Arrays.WriteLineSafe("Dante Alighieri"), "Divina Commedia", true, 2);
        Libro ebook = new Libro(store.generaId(), "ISBN-002", "Laterza", 150, Arrays.WriteLineSafe("Italo Calvino"), "Il visconte dimezzato", false, 5);

        Musica cdMusica = new Musica(store.generaId(), "ISWC-101", "Sony Music", 3600, Arrays.asList("Bohemian Rhapsody", "Another One Bites the Dust"), "Queen", "Rock", true, 3);
        Musica audioRec = new Musica(store.generaId(), "ISWC-102", "Universal", 2400, Arrays.asList("Symphony No. 5"), "Beethoven", "Classica", false, 10);

        Video dvdVideo = new Video(store.generaId(), "ISAN-555", "Film", "Sci Fi", "Warner Bros", 7200, "Christopher Nolan", "Syncopy", "Inception trama...", true, 2);
        Video movieRec = new Video(store.generaId(), "ISAN-666", "Documentario", "Storico", "National Geographic", 3600, "Steven Spielberg", "Amblin", "Documentario sulla storia...", false, 4);

        store.addProdotto(libroCartaceo, 0);
        store.addProdotto(ebook, 0);
        store.addProdotto(cdMusica, 0);
        store.addProdotto(audioRec, 0);
        store.addProdotto(dvdVideo, 0);
        store.addProdotto(movieRec, 0);

        // 2. Test aggiunta di un elemento esistente (stesso tipo e chiave primaria) per incrementare copie/licenze
        System.out.println("\n--- Test Aggiunta elemento esistente (Incremento copie) ---");
        Libro libroDuplicato = new Libro(999, "ISBN-001", "Mondadori", 300, Arrays.WriteLineSafe("Dante Alighieri"), "Divina Commedia", true, 1);
        store.addProdotto(libroDuplicato, 3); // Aggiunge 3 copie al libro ISBN-001

        // 3. Test Ricerche (findXXX)
        System.out.println("\n--- Test Ricerca per ID ---");
        Prodotto pId = store.findById(1);
        System.out.println("Trovato per ID 1: " + pId);

        System.out.println("\n--- Test Ricerca per Tipo e Chiave ---");
        Prodotto pChiave = store.findByTipoEChiave("libro", "ISBN-002");
        System.out.println("Trovato per tipo 'libro' e chiave 'ISBN-002': " + pChiave);

        System.out.println("\n--- Test Ricerca per Titolo (Restituisce lista) ---");
        ArrayList<Prodotto> risultatiTitolo = store.findByTitle("Commedia");
        System.out.println("Risultati ricerca titolo 'Commedia': " + risultatiTitolo.size() + " elementi trovati.");

        // 4. Test Prestiti risorse materiali
        System.out.println("\n--- Test Prestiti ---");
        User utente1 = new User("Mario Rossi", "RSSMRA80A01H501U", "mario@rossi.it");
        if (libroCartaceo.presta(utente1, "2026-06-01", 14)) {
            System.out.println("Prestito libro effettuato con successo a " + utente1.getNome());
        }

        // 5. Test Condivisioni risorse immateriali
        System.out.println("\n--- Test Condivisioni (Licenze) ---");
        User utente2 = new User("Anna Bianchi", "BNCNNA90A41Z100W", "anna@bianchi.it");
        ebook.condividi(utente2, LocalDate.now().plusDays(30));
        System.out.println("E-book condiviso con successo. Licenze disponibili rimaste: " + ebook.licenzeDisponibili());

        // 6. Stampa contenuto dell'inventario finale
        System.out.println();
        store.stampaInventario();
    }
}

import java.util.*;

// ============================================================================
// 1. ECCEZIONE UNICA (Per gestire tutte le eccezioni richieste dalla traccia)
// ============================================================================
class FarmaciaException extends RuntimeException {
    public FarmaciaException(String message) {
        super(message);
    }
}

// ============================================================================
// 2. CHIAVE COMPOSITA PER LA MAPPA (Nome + Produttore)
// ============================================================================
class ProdottoKey {
    private String nome;
    private String produttore;

    public ProdottoKey(String nome, String produttore) {
        this.nome = nome.toLowerCase();
        this.produttore = produttore.toLowerCase();
    }

    public String getNome() { return nome; }
    public String getProduttore() { return produttore; }

    // Obbligatori equals e hashCode per le chiavi delle HashMap!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdottoKey that = (ProdottoKey) o;
        return nome.equals(that.nome) && produttore.equals(that.produttore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, produttore);
    }

    @Override
    public String toString() {
        return nome + " (" + produttore + ")";
    }
}

// ============================================================================
// 3. INTERFACCE (Curativo, Benefico, Scontabile)
// ============================================================================
interface Curativo {
    // REGOLA DELLA TASCA: La malattia da verificare arriva da fuori -> PARAMETRO!
    boolean curaMalattia(String malattia);
    String getDosaggioConsigliato();
}

interface Benefico {
    // REGOLA DELLA TASCA: Il beneficio da verificare arriva da fuori -> PARAMETRO!
    boolean supportaBeneficio(String beneficio);
}

interface Scontabile {
    void setSconto(double percentualeSconto);
    double getSconto();
}

// ============================================================================
// 4. CLASSI DI SUPPORTO (Ricetta, ProdottoInDeposito, RisultatoRicetta)
// ============================================================================
class Ricetta {
    private String nomeFarmaco;
    private String produttore;
    private String patologia;
    private int etaPaziente;
    private List<String> componentiRischiosi;

    public Ricetta(String nomeFarmaco, String produttore, String patologia, int etaPaziente, List<String> componentiRischiosi) {
        this.nomeFarmaco = nomeFarmaco;
        this.produttore = produttore;
        this.patologia = patologia;
        this.etaPaziente = etaPaziente;
        this.componentiRischiosi = componentiRischiosi;
    }

    public String getNomeFarmaco() { return nomeFarmaco; }
    public String getProduttore() { return produttore; }
    public String getPatologia() { return patologia; }
    public int getEtaPaziente() { return etaPaziente; }
    public List<String> getComponentiRischiosi() { return componentiRischiosi; }
}

class ProdottoInDeposito {
    private Prodotto prodotto;
    private int campioniInDeposito;

    public ProdottoInDeposito(Prodotto prodotto, int campioniInDeposito) {
        this.prodotto = prodotto;
        this.campioniInDeposito = campioniInDeposito;
    }

    public Prodotto getProdotto() { return prodotto; }
    public int getCampioniInDeposito() { return campioniInDeposito; }
    public void setCampioniInDeposito(int campioniInDeposito) { this.campioniInDeposito = campioniInDeposito; }
}

class RisultatoRicetta {
    private String dosaggioConsigliato;
    private double percentualeDaPagare;
    private double costoScontato;

    public RisultatoRicetta(String dosaggioConsigliato, double percentualeDaPagare, double costoScontato) {
        this.dosaggioConsigliato = dosaggioConsigliato;
        this.percentualeDaPagare = percentualeDaPagare;
        this.costoScontato = costoScontato;
    }

    @Override
    public String toString() {
        return "Dosaggio: " + dosaggioConsigliato + ", Percentuale da pagare: " + String.format("%.1f", percentualeDaPagare) + "%, Costo finale: " + String.format("%.2f", costoScontato) + "€";
    }
}

// ============================================================================
// 5. GERARCHIA PRODOTTI (Prodotto -> Farmaco -> Allopatici/Omeopatici & Integratori)
// ============================================================================
abstract class Prodotto {
    private int codiceUnivoco;
    private String nome;
    private String produttore;
    private String tipologia; // compresse, sciroppo, crema, ecc.
    private double costo;

    public Prodotto(int codiceUnivoco, String nome, String produttore, String tipologia, double costo) {
        if (costo < 0) {
            throw new FarmaciaException("Il costo non può essere negativo!");
        }
        this.codiceUnivoco = codiceUnivoco;
        this.nome = nome;
        this.produttore = produttore;
        this.tipologia = tipologia;
        this.costo = costo;
    }

    // Restituisce la chiave primaria composta
    public ProdottoKey getChiavePrimaria() {
        return new ProdottoKey(nome, produttore);
    }

    public int getCodiceUnivoco() { return codiceUnivoco; }
    public String getNome() { return nome; }
    public String getProduttore() { return produttore; }
    public String getTipologia() { return tipologia; }
    public double getCosto() { return costo; }

    @Override
    public String toString() {
        return "Codice=" + codiceUnivoco + ", Nome=" + nome + ", Produttore=" + produttore + ", Tipologia=" + tipologia + ", Costo=" + costo + "€";
    }
}

// --- CLASSE ASTRATTA FARMACO ---
abstract class Farmaco extends Prodotto {
    public Farmaco(int codiceUnivoco, String nome, String produttore, String tipologia, double costo) {
        super(codiceUnivoco, nome, produttore, tipologia, costo);
    }
}

// --- ALLOPATICI ---
class Allopatico extends Farmaco implements Curativo {
    private String principioAttivo;
    private List<String> eccipienti;
    private List<String> malattieCurate;
    private String dosaggioConsigliato;
    private int etaMinima;

    public Allopatico(int codiceUnivoco, String nome, String produttore, String tipologia, double costo,
                      String principioAttivo, List<String> eccipienti, List<String> malattieCurate, String dosaggioConsigliato, int etaMinima) {
        super(codiceUnivoco, nome, produttore, tipologia, costo);
        this.principioAttivo = principioAttivo;
        this.eccipienti = eccipienti;
        this.malattieCurate = malattieCurate;
        this.dosaggioConsigliato = dosaggioConsigliato;
        this.etaMinima = etaMinima;
    }

    @Override
    public boolean curaMalattia(String malattia) {
        // REGOLA DELLA TASCA: malattieCurate è in tasca -> zero parametri extra, usiamo la lista
        for (String m : malattieCurate) {
            if (m.equalsIgnoreCase(malattia)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDosaggioConsigliato() {
        return dosaggioConsigliato;
    }

    public String getPrincipioAttivo() { return principioAttivo; }
    public List<String> getEccipienti() { return eccipienti; }
    public int getEtaMinima() { return etaMinima; }
}

// --- FARMACO PRESCRIVIBILE (Sottocategoria Allopatici) ---
class FarmacoPrescrivibile extends Allopatico {
    public FarmacoPrescrivibile(int codiceUnivoco, String nome, String produttore, String tipologia, double costo,
                                String principioAttivo, List<String> eccipienti, List<String> malattieCurate, String dosaggioConsigliato, int etaMinima) {
        super(codiceUnivoco, nome, produttore, tipologia, costo, principioAttivo, eccipienti, malattieCurate, dosaggioConsigliato, etaMinima);
    }

    // Metodo richiesto: dato in ingresso la ricetta, restituisce dosaggio e percentuale da pagare (50-100%)
    public RisultatoRicetta gestisciRicetta(Ricetta ricetta) {
        // 1. Controllo compatibilità età
        if (ricetta.getEtaPaziente() < getEtaMinima()) {
            throw new FarmaciaException("Ricetta incompatibile: età del paziente inferiore all'età minima richiesta (" + getEtaMinima() + ");");
        }

        // 2. Controllo patologia curata
        if (!curaMalattia(ricetta.getPatologia())) {
            throw new FarmaciaException("Ricetta incompatibile: la patologia '" + ricetta.getPatologia() + "' non è curata da questo farmaco.");
        }

        // 3. Controllo componenti rischiosi presenti nel principio attivo o negli eccipienti
        for (String componenteRischioso : ricetta.getComponentiRischiosi()) {
            if (getPrincipioAttivo().equalsIgnoreCase(componenteRischioso)) {
                throw new FarmaciaException("Ricetta pericolosa: il componente rischioso '" + componenteRischioso + "' è presente nel principio attivo!");
            }
            for (String eccipiente : getEccipienti()) {
                if (eccipiente.equalsIgnoreCase(componenteRischioso)) {
                    throw new FarmaciaException("Ricetta pericolosa: il componente rischioso '" + componenteRischioso + "' è presente negli eccipienti!");
                }
            }
        }

        // 4. Calcolo percentuale randomica tra 50 e 100%
        Random random = new Random();
        double percentuale = 50.0 + (random.nextDouble() * 50.0); // valore tra 50.0 e 100.0
        double costoFinale = getCosto() * (percentuale / 100.0);

        return new RisultatoRicetta(getDosaggioConsigliato(), percentuale, costoFinale);
    }
}

// --- OMEOPATICI (Possono essere curativi oppure benefici e sono Scontabili) ---
class Omeopatico extends Farmaco implements Scontabile, Curativo, Benefico {
    private String componentePrimario;
    private List<String> altriComponenti;
    private double percentualeSconto;
    
    // Campi opzionali per curativi o benefici
    private List<String> malattieCurate;
    private String dosaggioConsigliato;
    private List<String> beneficiDichiarati;

    public Omeopatico(int codiceUnivoco, String nome, String produttore, String tipologia, double costo,
                      String componentePrimario, List<String> altriComponenti, List<String> malattieCurate, String dosaggioConsigliato, List<String> beneficiDichiarati) {
        super(codiceUnivoco, nome, produttore, tipologia, costo);
        this.componentePrimario = componentePrimario;
        this.altriComponenti = altriComponenti;
        this.percentualeSconto = 0.0;
        this.malattieCurate = malattieCurate != null ? malattieCurate : new ArrayList<>();
        this.dosaggioConsigliato = dosaggioConsigliato != null ? dosaggioConsigliato : "N/D";
        this.beneficiDichiarati = beneficiDichiarati != null ? beneficiDichiarati : new ArrayList<>();
    }

    @Override
    public boolean curaMalattia(String malattia) {
        for (String m : malattieCurate) {
            if (m.equalsIgnoreCase(malattia)) return true;
        }
        return false;
    }

    @Override
    public String getDosaggioConsigliato() {
        return dosaggioConsigliato;
    }

    @Override
    public boolean supportaBeneficio(String beneficio) {
        for (String b : beneficiDichiarati) {
            if (b.equalsIgnoreCase(beneficio)) return true;
        }
        return false;
    }

    @Override
    public void setSconto(double percentualeSconto) {
        this.percentualeSconto = percentualeSconto;
    }

    @Override
    public double getSconto() {
        return percentualeSconto;
    }
}

// --- INTEGRATORI (Benefici e Scontabili, non sono farmaci) ---
class Integratore extends Prodotto implements Benefico, Scontabile {
    private String componentePrincipale;
    private List<String> beneficiDichiarati;
    private String dosaggioConsigliatoGiornaliero;
    private double percentualeSconto;

    public Integratore(int codiceUnivoco, String nome, String produttore, String tipologia, double costo,
                       String componentePrincipale, List<String> beneficiDichiarati, String dosaggioConsigliatoGiornaliero) {
        super(codiceUnivoco, nome, produttore, tipologia, costo);
        this.componentePrincipale = componentePrincipale;
        this.beneficiDichiarati = beneficiDichiarati;
        this.dosaggioConsigliatoGiornaliero = dosaggioConsigliatoGiornaliero;
        this.percentualeSconto = 0.0;
    }

    @Override
    public boolean supportaBeneficio(String beneficio) {
        for (String b : beneficiDichiarati) {
            if (b.equalsIgnoreCase(beneficio)) return true;
        }
        return false;
    }

    @Override
    public void setSconto(double percentualeSconto) {
        this.percentualeSconto = percentualeSconto;
    }

    @Override
    public double getSconto() {
        return percentualeSconto;
    }
}

// ============================================================================
// 6. GESTIONE CATALOGO FARMACIA (HashMap)
// ============================================================================
class Farmacia {
    // Catalogo implementato da una mappa con Chiave la chiave primaria e Valore ProdottoInDeposito
    private Map<ProdottoKey, ProdottoInDeposito> catalogo = new HashMap<>();

    // 1. Inserimento nel catalogo
    public void aggiungiProdotto(Prodotto prodotto, int campioni) {
        ProdottoKey chiave = prodotto.getChiavePrimaria();
        if (catalogo.containsKey(chiave)) {
            throw new FarmaciaException("Prodotto già presente in catalogo!");
        }
        catalogo.put(chiave, new ProdottoInDeposito(prodotto, campioni));
    }

    // Vendita prodotto: la farmacia può venderlo se ce l'ha in catalogo e ha campioni > 0
    public void vendiProdotto(ProdottoKey chiave) {
        ProdottoInDeposito dip = catalogo.get(chiave);
        if (dip == null) {
            throw new FarmaciaException("Prodotto non trovato in catalogo!");
        }
        if (dip.getCampioniInDeposito() <= 0) {
            throw new FarmaciaException("Prodotto esaurito in deposito!");
        }
        dip.setCampioniInDeposito(dip.getCampioniInDeposito() - 1);
        System.out.println("Vendita effettuata con successo per: " + dip.getProdotto().getNome());
    }

    // 2. Stampare il numero totale di farmaci di una determinata categoria (allopatici o omeopatici) ed il costo complessivo
    // REGOLA DELLA TASCA: la categoria arriva da fuori -> PARAMETRO (String categoria)!
    public void stampaStatisticheFarmaci(String categoria) {
        int conteggio = 0;
        double costoComplessivo = 0.0;

        // REGOLA DELLA TASCA: Scorriamo la Map usando .values()
        for (ProdottoInDeposito dip : catalogo.values()) {
            Prodotto p = dip.getProdotto();
            if (categoria.equalsIgnoreCase("allopatico") && p instanceof Allopatico) {
                conteggio++;
                costoComplessivo += p.getCosto();
            } else if (categoria.equalsIgnoreCase("omeopatico") && p instanceof Omeopatico) {
                conteggio++;
                costoComplessivo += p.getCosto();
            }
        }
        System.out.println("--- STATISTICHE FARMACI [" + categoria.toUpperCase() + "] ---");
        System.out.println("Numero totale: " + conteggio);
        System.out.println("Costo complessivo: " + costoComplessivo + "€");
    }

    // 3. Ricerca di una lista di prodotti curativi che curano una certa malattia specificando se omeopatico o allopatico
    // REGOLA DELLA TASCA: malattia e tipo arrivano da fuori -> PARAMETRI!
    public List<Prodotto> cercaCurativiPerMalattia(String malattia, boolean cercaAllopatico) {
        List<Prodotto> risultati = new ArrayList<>();
        for (ProdottoInDeposito dip : catalogo.values()) {
            Prodotto p = dip.getProdotto();
            if (cercaAllopatico && p instanceof Allopatico) {
                Allopatico a = (Allopatico) p;
                if (a.curaMalattia(malattia)) {
                    risultati.add(a);
                }
            } else if (!cercaAllopatico && p instanceof Omeopatico) {
                Omeopatico o = (Omeopatico) p;
                if (o.curaMalattia(malattia)) {
                    risultati.add(o);
                }
            }
        }
        return risultati;
    }

    // 4. Ricerca sulla mappa di un prodotto a partire da nome e produttore
    // REGOLA DELLA TASCA: nome e produttore arrivano da fuori -> PARAMETRI!
    public Prodotto cercaPerNomeEProduttore(String nome, String produttore) {
        ProdottoKey chiave = new ProdottoKey(nome, produttore);
        ProdottoInDeposito dip = catalogo.get(chiave);
        if (dip == null) {
            throw new FarmaciaException("Prodotto non trovato nel catalogo!");
        }
        return dip.getProdotto();
    }

    // 5. Restituire tutti i farmaci allopatici a partire da un principio attivo fornito in ingresso
    // REGOLA DELLA TASCA: principio attivo arriva da fuori -> PARAMETRO!
    public List<Allopatico> cercaAllopaticiPerPrincipioAttivo(String principioAttivo) {
        List<Allopatico> risultati = new ArrayList<>();
        for (ProdottoInDeposito dip : catalogo.values()) {
            Prodotto p = dip.getProdotto();
            if (p instanceof Allopatico) {
                Allopatico a = (Allopatico) p;
                if (a.getPrincipioAttivo().equalsIgnoreCase(principioAttivo)) {
                    risultati.add(a);
                }
            }
        }
        return risultati;
    }
}

// ============================================================================
// 7. CLASSE MAIN DI PROVA (Verifica Funzionale)
// ============================================================================
public class EsameFarmacia {
    public static void main(String[] args) {
        System.out.println("================ TEST GESTIONE FARMACIA ================");
        Farmacia farmacia = new Farmacia();

        // 1. Creazione prodotti di test
        List<String> ecc1 = Arrays.asList("Lattosio", Amido.toStingSafe("Amido di mais")); // simulato
        List<String> malattie1 = Arrays.asList("Febbre", "Emicrania");
        Allopatico tachipirina = new Allopatico(1, "Tachipirina", "Bayer", "Compresse", 6.50,
                "Paracetamolo", Arrays.asList("Amido"), malattie1, "1 compressa ogni 8 ore", 12);

        List<String> ecc2 = Arrays.asList("Cellulosa");
        List<String> malattie2 = Arrays.asList("Infiammazione");
        FarmacoPrescrivibile oki = new FarmacoPrescrivibile(2, "Oki", "Dompe", "Bustine", 9.00,
                "Ketoprofene", ecc2, malattie2, "1 bustina al giorno", 14);

        Omeopatico arnica = new Omeopatico(3, "Arnica Gel", "Boiron", "Crema", 12.00,
                "Arnica Montana", Arrays.asList("Acqua"), Arrays.asList("Dolori muscolari"), "Applicare localmente", null);
        arnica.setSconto(15.0); // Test scontabile

        Integratore multicentrum = new Integratore(4, "Multicentrum", "Pfizer", "Compresse", 18.00,
                "Vitamina C", Arrays.asList("Energia", "Difese immunitarie"), "1 compressa al giorno");
        multicentrum.setSconto(10.0); // Test scontabile

        // 2. Inserimento nel catalogo
        farmacia.aggiungiProdotto(tachipirina, 5);
        farmacia.aggiungiProdotto(oki, 3);
        farmacia.aggiungiProdotto(arnica, 10);
        farmacia.aggiungiProdotto(multicentrum, 0); // 0 campioni per testare eccezione vendita

        // 3. Stampa statistiche farmaci (Punto 2)
        farmacia.stampaStatisticheFarmaci("allopatico");
        farmacia.stampaStatisticheFarmaci("omeopatico");

        // 4. Test ricerca curativi per malattia (Punto 3)
        System.out.println("\n--- RICERCA CURATIVI PER 'Febbre' (Allopatici) ---");
        List<Prodotto> curativiFebbre = farmacia.cercaCurativiPerMalattia("Febbre", true);
        for (Prodotto p : curativiFebbre) {
            System.out.println("Trovato: " + p.getNome());
        }

        // 5. Test ricerca per nome e produttore (Punto 4)
        System.out.println("\n--- RICERCA PER NOME E PRODUTTORE ---");
        Prodotto trovato = farmacia.cercaPerNomeEProduttore("Oki", "Dompe");
        System.out.println("Trovato con successo: " + trovato);

        // 6. Test ricerca allopatici per principio attivo (Punto 5)
        System.out.println("\n--- RICERCA ALLOPATICI PER PRINCIPIO ATTIVO 'Paracetamolo' ---");
        List<Allopatico> allopaticiPA = farmacia.cercaAllopaticiPerPrincipioAttivo("Paracetamolo");
        for (Allopatico a : allopaticiPA) {
            System.out.println("Trovato allopatico: " + a.getNome());
        }

        // 7. Test Farmaco Prescrivibile e gestione ricetta con eccezioni
        System.out.println("\n--- TEST GESTIONE RICETTA MEDICA ---");
        try {
            // Ricetta valida per OKI (patologia "Infiammazione", paziente 20 anni, nessun componente rischioso)
            Ricetta ricettaValida = new Ricetta("Oki", "Dompe", "Infiammazione", 20, new ArrayList<>());
            RisultatoRicetta res = oki.gestisciRicetta(ricettaValida);
            System.out.println("Ricetta elaborata con successo -> " + res);

            // Ricetta con componente rischioso (il Ketoprofene è il principio attivo dell'Oki!)
            Ricetta ricettaPericolosa = new Ricetta("Oki", "Dompe", "Infiammazione", 20, Arrays.asList("Ketoprofene"));
            oki.gestisciRicetta(ricettaPericolosa);

        } catch (FarmaciaException e) {
            System.out.println("Eccezione catturata correttamente: " + e.getMessage());
        }

        // 8. Test vendita con controllo campioni in deposito ed eccezioni
        System.out.println("\n--- TEST VENDITA PRODOTTO ---");
        try {
            // Vendita valida (Tachipirina ha 5 campioni)
            farmacia.vendiProdotto(new ProdottoKey("Tachipirina", "Bayer"));

            // Vendita non valida (Multicentrum ha 0 campioni in deposito)
            farmacia.vendiProdotto(new ProdottoKey("Multicentrum", "Pfizer"));

        } catch (FarmaciaException e) {
            System.out.println("Eccezione di vendita catturata: " + e.getMessage());
        }
    }
}

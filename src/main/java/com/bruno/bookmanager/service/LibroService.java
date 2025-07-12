package com.bruno.bookmanager.service;

import com.bruno.bookmanager.dao.LibroDAO;
import com.bruno.bookmanager.exception.*;
import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.model.Libro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service principale per la gestione dei libri.
 * <p>
 * Fornisce tutte le funzionalità richieste dal frontend:
 * <ul>
 *     <li>CRUD operations sui libri</li>
 *     <li>Ricerca e filtri</li>
 *     <li>Ordinamento</li>
 *     <li>Validazione dei dati</li>
 *     <li>Gestione persistenza</li>
 * </ul>
 */
public final class LibroService {

    private static final Logger logger = LoggerFactory.getLogger(LibroService.class);

    // Singleton instance
    private static volatile LibroService instance;

    private LibroDAO libroDAO;

    private LibroService() {
        logger.info("LibroService inizializzato");
    }

    public synchronized static LibroService getInstance() {
        if (instance == null) {
            instance = new LibroService();
        }
        return instance;
    }

    /**
     * Imposta il tipo di DAO e il path del file da utilizzare.
     *
     * @param type tipo di DAO da impostare
     * @param path percorso del file su cui avviare il DAO
     */
    public void setDAO(DAOType type, String path) {
        this.libroDAO = DAOFactory.createDAO(type, path);
        logger.info("Strategia DAO cambiata a: {}", type.name());
    }

    public void setLibroDAO(LibroDAO libroDAO) {
        this.libroDAO = libroDAO;
    }

    private void checkDAOInitialized() throws BookManagerException {
        if (libroDAO == null) {
            throw new BookManagerException("DAO non inizializzato. Chiamare setLibroDAO() prima di usare il service.");
        }
    }

    // ============= OPERAZIONI CRUD =============

    /**
     * Aggiunge un nuovo libro alla collezione.
     *
     * @param libro libro da aggiungere
     * @throws ValidationException         se i dati del libro non sono validi
     * @throws LibroAlreadyExistsException se il libro esiste già
     * @throws BookManagerException        per altri errori
     */
    public void aggiungiLibro(Libro libro) throws BookManagerException {
        checkDAOInitialized();
        Validator.validateLibro(libro);
        try {
            libroDAO.add(libro);
            logger.info("Libro aggiunto con successo: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());
        } catch (DAOException e) {
            logger.error("Errore durante l'aggiunta del libro", e);
            throw new BookManagerException("Impossibile aggiungere il libro", e);
        }
    }

    /**
     * Rimuove un libro dalla collezione tramite ISBN.
     *
     * @param isbn ISBN del libro da rimuovere
     * @throws LibroNotFoundException se il libro non viene trovato
     * @throws ValidationException    se l'ISBN non è valido
     * @throws BookManagerException   per altri errori
     */
    public void rimuoviLibro(String isbn) throws BookManagerException {
        checkDAOInitialized();
        Validator.validateIsbn(isbn);
        try {
            libroDAO.removeByIsbn(isbn);
            logger.info("Libro rimosso con successo: ISBN {}", isbn);
        } catch (DAOException e) {
            logger.error("Errore durante la rimozione del libro", e);
            throw new BookManagerException("Impossibile rimuovere il libro", e);
        }
    }

    /**
     * Aggiorna le informazioni di un libro esistente.
     *
     * @param libro libro con le informazioni aggiornate
     * @throws ValidationException    se i dati del libro non sono validi
     * @throws LibroNotFoundException se il libro non viene trovato
     * @throws BookManagerException   per altri errori
     */
    public void aggiornaLibro(Libro libro) throws BookManagerException {
        checkDAOInitialized();
        Validator.validateLibro(libro);

        try {
            libroDAO.update(libro);
            logger.info("Libro aggiornato con successo: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());
        } catch (DAOException e) {
            logger.error("Errore durante l'aggiornamento del libro", e);
            throw new BookManagerException("Impossibile aggiornare il libro", e);
        }
    }

    /**
     * Cerca un libro tramite ISBN.
     *
     * @param isbn ISBN del libro cercato
     * @return Optional contenente il libro se trovato
     * @throws ValidationException  se l'ISBN non è valido
     * @throws BookManagerException per errori di accesso ai dati
     */
    public Optional<Libro> trovaLibroPerIsbn(String isbn) throws BookManagerException {
        checkDAOInitialized();
        Validator.validateIsbn(isbn);

        try {
            return libroDAO.getByIsbn(isbn);
        } catch (DAOException e) {
            logger.error("Errore durante la ricerca del libro con ISBN {}", isbn, e);
            throw new BookManagerException("Impossibile cercare il libro", e);
        }
    }

    /**
     * Restituisce tutti i libri della collezione.
     *
     * @return lista di tutti i libri
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> getAllLibri() throws BookManagerException {
        checkDAOInitialized();

        try {
            List<Libro> libri = libroDAO.getAll();
            logger.debug("Recuperati {} libri dalla collezione", libri.size());
            return libri;
        } catch (DAOException e) {
            logger.error("Errore durante il recupero di tutti i libri", e);
            throw new BookManagerException("Impossibile recuperare i libri", e);
        }
    }

    // ============= RICERCA E FILTRI =============

    /**
     * Cerca libri per titolo (ricerca parziale case-insensitive).
     *
     * @param titolo titolo o parte del titolo da cercare
     * @return lista dei libri che contengono il titolo specificato
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> cercaPerTitolo(String titolo) throws BookManagerException {
        if (titolo == null || titolo.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Libro> tuttiILibri = getAllLibri();
        String titoloLower = titolo.toLowerCase().trim();

        return tuttiILibri.stream().filter(libro -> libro.getTitolo().toLowerCase().contains(titoloLower)).toList();
    }

    /**
     * Cerca libri per autore (ricerca parziale case-insensitive).
     *
     * @param autore autore o parte del nome dell'autore da cercare
     * @return lista dei libri dell'autore specificato
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> cercaPerAutore(String autore) throws BookManagerException {
        if (autore == null || autore.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Libro> tuttiILibri = getAllLibri();
        String autoreLower = autore.trim().toLowerCase();

        return tuttiILibri.stream().filter(libro -> libro.getAutore().toLowerCase().contains(autoreLower)).toList();
    }
    /*
     *//**
     * Filtra libri per genere.
     *
     * @param genere genere da filtrare
     * @return lista dei libri del genere specificato
     * @throws BookManagerException per errori di accesso ai dati
     *//*
    public List<Libro> filtraPerGenere(Genere genere) throws BookManagerException {
        checkDAOInitialized();

        try {
            return libroDAO.getByFilter(new GenereFilter(genere));
        } catch (DAOException e) {
            logger.error("Errore durante il filtro per genere {}", genere, e);
            throw new BookManagerException("Impossibile filtrare per genere", e);
        }
    }*/


    /**
     * Applica un filtro personalizzato.
     *
     * @param filter filtro da applicare
     * @return lista dei libri che soddisfano il filtro
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> filtraLibri(Filter<Libro> filter) throws BookManagerException {
        checkDAOInitialized();

        try {
            return libroDAO.getByFilter(filter);
        } catch (DAOException e) {
            logger.error("Errore durante l'applicazione del filtro personalizzato", e);
            throw new BookManagerException("Impossibile applicare il filtro", e);
        }
    }

    // ============= ORDINAMENTO =============

    /**
     * Ordina una lista di libri per titolo.
     *
     * @param libri     lista di libri da ordinare
     * @param crescente true per ordine crescente, false per decrescente
     * @return lista ordinata
     */
    public List<Libro> ordinaPerTitolo(List<Libro> libri, boolean crescente) {
        Comparator<Libro> comparator = Comparator.comparing(Libro::getTitolo);
        if (!crescente) {
            comparator = comparator.reversed();
        }

        return libri.stream().sorted(comparator).toList();
    }

    /**
     * Ordina una lista di libri per autore.
     *
     * @param libri     lista di libri da ordinare
     * @param crescente true per ordine crescente, false per decrescente
     * @return lista ordinata
     */
    public List<Libro> ordinaPerAutore(List<Libro> libri, boolean crescente) {
        Comparator<Libro> comparator = Comparator.comparing(Libro::getAutore);
        if (!crescente) {
            comparator = comparator.reversed();
        }

        return libri.stream().sorted(comparator).toList();
    }

    /**
     * Ordina una lista di libri per valutazione.
     *
     * @param libri     lista di libri da ordinare
     * @param crescente true per ordine crescente, false per decrescente
     * @return lista ordinata
     */
    public List<Libro> ordinaPerValutazione(List<Libro> libri, boolean crescente) {
        Comparator<Libro> comparator = Comparator.comparing(Libro::getValutazione);
        if (!crescente) {
            comparator = comparator.reversed();
        }

        return libri.stream().sorted(comparator).toList();
    }

    // ============= PERSISTENZA =============

    /**
     * Carica la collezione da memoria secondaria.
     *
     * @throws BookManagerException per errori di caricamento
     */
    public void caricaCollezione() throws BookManagerException {
        checkDAOInitialized();

        try {
            List<Libro> libri = libroDAO.getAll();
            logger.info("Collezione caricata con successo: {} libri", libri.size());
        } catch (DAOException e) {
            logger.error("Errore durante il caricamento della collezione", e);
            throw new BookManagerException("Impossibile caricare la collezione", e);
        }
    }

    // ============= STATISTICHE E UTILITY =============

    /**
     * Restituisce il numero totale di libri nella collezione.
     *
     * @return numero di libri
     * @throws BookManagerException per errori di accesso ai dati
     */
    public int contaLibri() throws BookManagerException {
        return getAllLibri().size();
    }

    /**
     * Restituisce la valutazione media di tutti i libri.
     *
     * @return valutazione media, 0.0 se non ci sono libri
     * @throws BookManagerException per errori di accesso ai dati
     */
    public double getValutazioneMedia() throws BookManagerException {
        List<Libro> tuttiILibri = getAllLibri();

        return tuttiILibri.stream().mapToInt(Libro::getValutazione).average().orElse(0.0);
    }
}

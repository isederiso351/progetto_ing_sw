package com.bruno.bookmanager.service;

import com.bruno.bookmanager.dao.DAOFactory;
import com.bruno.bookmanager.dao.DAOType;
import com.bruno.bookmanager.dao.LibroDAO;
import com.bruno.bookmanager.dao.OptimizedSearch;
import com.bruno.bookmanager.exception.*;
import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.filters.ISBNFilter;
import com.bruno.bookmanager.filters.SearchCriteria;
import com.bruno.bookmanager.model.Libro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    /**
     * Verifica se il DAO corrente supporta ricerche ottimizzate.
     */
    public boolean supportsOptimizedSearch() {
        return libroDAO instanceof OptimizedSearch;
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


    public List<Libro> search(SearchCriteria criteria) throws BookManagerException {
        checkDAOInitialized();
        if (criteria == null) {
            criteria = SearchCriteria.all();
        }

        try {
            if (supportsOptimizedSearch()) {
                // Usa ricerca ottimizzata
                OptimizedSearch optimizedDAO = (OptimizedSearch) libroDAO;
                List<Libro> result = optimizedDAO.search(criteria);
                logger.debug("Ricerca ottimizzata completata: {} libri trovati", result.size());
                return result;
            } else {
                // Fallback: ricerca in memoria
                List<Libro> result = applyCriteria(criteria);
                logger.debug("Ricerca in memoria completata: {} libri trovati", result.size());
                return result;
            }
        } catch (DAOException e) {
            logger.error("Errore durante la ricerca", e);
            throw new BookManagerException("Impossibile eseguire la ricerca", e);
        }

    }

    private List<Libro> applyCriteria(SearchCriteria criteria) throws DAOException {
        List<Libro> allBooks = libroDAO.getAll();
        Stream<Libro> stream = allBooks.stream();

        // Applica filtro se presente
        if (criteria.hasFilter()) {
            stream = stream.filter(criteria.getFilter()::test);
        }

        // Applica ordinamento se presente
        if (criteria.hasSorting()) {
            Comparator<Libro> comparator = switch (criteria.getSortField()) {
                case TITOLO -> Comparator.comparing(Libro::getTitolo);
                case AUTORE -> Comparator.comparing(Libro::getAutore);
                case VALUTAZIONE -> Comparator.comparing(Libro::getValutazione);
                case GENERE ->
                        Comparator.comparing(libro -> libro.getGenere() != null ? libro.getGenere().name() : null,
                                Comparator.nullsLast(String::compareTo));
                case STATO -> Comparator.comparing(libro -> libro.getStatoLettura().name());
                case ISBN -> Comparator.comparing(Libro::getIsbn);
            };

            if (!criteria.isSortAsc()) {
                comparator = comparator.reversed();
            }

            stream = stream.sorted(comparator);
        }

        return stream.toList();
    }

    //Metodi di convenienza
    /**
     * Cerca libri per titolo (ricerca parziale case-insensitive).
     *
     * @param titolo titolo o parte del titolo da cercare
     * @return lista dei libri che contengono il titolo specificato
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> cercaPerTitolo(String titolo) throws BookManagerException {
        if (titolo == null || titolo.trim().isEmpty()) {
            return getAllLibri();
        }
        return search(SearchCriteria.byTitle(titolo));
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
            return getAllLibri();
        }
        return search(SearchCriteria.byAuthor(autore));
    }

    /**
     * Cerca libri per ISBN (ricerca parziale).
     *
     * @param isbn parte dell'ISBN da cercare
     * @return lista dei libri con parte di ISBN corrispondente
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> cercaPerIsbn(String isbn) throws BookManagerException {
        if (isbn == null || isbn.trim().isEmpty()) {
            return getAllLibri();
        }
        return search(new SearchCriteria.Builder().filter(new ISBNFilter(isbn)).build());
    }

    /**
     * Applica un filtro personalizzato.
     *
     * @param filter filtro da applicare
     * @return lista dei libri che soddisfano il filtro
     * @throws BookManagerException per errori di accesso ai dati
     */
    public List<Libro> filtraLibri(Filter<Libro> filter) throws BookManagerException {
        return search(SearchCriteria.byFilter(filter));
    }

}

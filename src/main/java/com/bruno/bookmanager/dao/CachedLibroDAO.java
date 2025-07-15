package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Libro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione di {@link LibroDAO} che aggiunge una cache in memoria sopra un altro DAO.
 * <p>
 * Utilizza il pattern Decorator per migliorare le performance evitando accessi ripetuti al DAO sottostante.
 * La cache viene inizializzata al primo accesso e aggiornata ad ogni modifica.
 */
public class CachedLibroDAO implements LibroDAO {

    private static final Logger logger = LoggerFactory.getLogger(CachedLibroDAO.class);

    private final LibroDAO delegate;
    private List<Libro> cache;

    /**
     * Costruisce un CachedLibroDAO che decora un altro LibroDAO.
     *
     * @param delegate DAO sottostante che effettua la persistenza.
     */
    public CachedLibroDAO(LibroDAO delegate) {
        this.delegate = delegate;
        logger.debug("Creato CachedLibroDAO con delegate: {}", delegate.getClass().getSimpleName());
    }

    private List<Libro> getCache() throws DAOException {
        if (cache == null) {
            cache = delegate.getAll();
        }
        return cache;
    }

    private void invalidateCache() {
        cache = null;
        logger.debug("Cache invalidata");
    }

    @Override
    public List<Libro> getAll() throws DAOException {
        List<Libro> result = new ArrayList<>(getCache());
        logger.debug("Restituiti {} libri dalla cache", result.size());
        return result;
    }

    @Override
    public void saveAll(List<Libro> libri) throws DAOException {
        try {
            delegate.saveAll(libri);
            cache = new ArrayList<>(libri);
            logger.info("Cache aggiornata con {} libri dopo saveAll", libri.size());
        } catch (DAOException e) {
            logger.error("Errore durante saveAll, invalidazione cache", e);
            invalidateCache();
            throw e;
        }
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) throws DAOException {
        Optional<Libro> result = getCache().stream().filter(l -> l.getIsbn().equals(isbn)).findFirst();

        logger.debug("Ricerca in cache per ISBN {}: {}", isbn, result.isPresent() ? "trovato" : "non trovato");
        return result;
    }

    @Override
    public void add(Libro libro) throws LibroAlreadyExistsException, DAOException {
        try {
            if (getCache().contains(libro)) {
                logger.warn("Tentativo di aggiunta libro già presente in cache con ISBN {}", libro.getIsbn());
                throw new LibroAlreadyExistsException(libro.getIsbn());
            }

            cache.add(libro);

            if (delegate.prefersBatchOperations()) {
                delegate.saveAll(cache);
                logger.debug("Usata strategia batch per delegate {}", delegate.getClass().getSimpleName());
            } else {
                delegate.add(libro);
                logger.debug("Usata strategia singola per delegate {}", delegate.getClass().getSimpleName());
            }

            logger.info("Libro aggiunto a cache e persistenze: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());

        } catch (DAOException | LibroAlreadyExistsException e) {
            logger.error("Errore durante add, invalidazione cache", e);
            invalidateCache();
            throw e;
        }
    }

    @Override
    public void removeByIsbn(String isbn) throws LibroNotFoundException, DAOException {
        try {
            if (getCache().stream().noneMatch(l -> l.getIsbn().equals(isbn))) {
                logger.warn("Tentativo di rimozione libro non presente con ISBN {}", isbn);
                throw new LibroNotFoundException(isbn);
            }

            cache.removeIf(l -> l.getIsbn().equals(isbn));

            if (delegate.prefersBatchOperations()) {
                delegate.saveAll(cache);
                logger.debug("Usata strategia batch per rimozione con delegate {}",
                        delegate.getClass().getSimpleName());
            } else {
                delegate.removeByIsbn(isbn);
                logger.debug("Usata strategia singola per rimozione con delegate {}",
                        delegate.getClass().getSimpleName());
            }

            logger.info("Libro rimosso dalla cache e persistenza: ISBN {}", isbn);

        } catch (DAOException | LibroNotFoundException e) {
            logger.error("Errore durante removeByIsbn, invalidazione cache", e);
            invalidateCache();
            throw e;
        }
    }

    @Override
    public void update(Libro libro) throws LibroNotFoundException, DAOException {
        try {
            List<Libro> cache = getCache();
            boolean found = false;
            for (int i = 0; i < cache.size(); i++) {
                if (cache.get(i).getIsbn().equals(libro.getIsbn())) {
                    cache.set(i, libro);
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.warn("Tentativo di aggiornamento libro non presente con ISBN {}", libro.getIsbn());
                throw new LibroNotFoundException(libro.getIsbn());
            }

            if (delegate.prefersBatchOperations()) {
                delegate.saveAll(cache);
                logger.debug("Usata strategia batch per aggiornamento con delegate {}",
                        delegate.getClass().getSimpleName());
            } else {
                delegate.update(libro);
                logger.debug("Usata strategia singola per aggiornamento con delegate {}",
                        delegate.getClass().getSimpleName());
            }

            logger.info("Libro aggiornato in cache e persistenza: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());

        } catch (DAOException | LibroNotFoundException e) {
            logger.error("Errore durante update, invalidazione cache", e);
            invalidateCache();
            throw e;
        }
    }

    /**
     * Invalida e ricarica la cache dal delegate.
     *
     * @throws DAOException se si verifica un errore durante il ricaricamento
     */
    public void refreshCache() throws DAOException {
        logger.debug("Refresh manuale della cache richiesto");
        invalidateCache();
        getCache();
        logger.info("Cache ricaricata manualmente con {} libri", cache.size());
    }

    /**
     * @return true se la cache è inizializzata, false altrimenti
     */
    public boolean isCacheInitialized() {
        return cache != null;
    }

    /**
     * Restituisce il numero di elementi nella cache.
     *
     * @return numero di libri in cache, -1 se la cache non è inizializzata
     */
    public int getCacheSize() {
        return cache != null ? cache.size() : -1;
    }
}

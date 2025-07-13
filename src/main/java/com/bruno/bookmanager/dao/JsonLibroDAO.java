package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Libro;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Implementazione dell'interfaccia {@link LibroDAO} che salva e carica i dati da un file JSON.
 * <p>
 * Utilizza la libreria Jackson per serializzare e deserializzare la lista di libri.
 * È una soluzione semplice e adatta a piccoli dataset persistenti su disco.
 */
public class JsonLibroDAO implements LibroDAO {

    private final static Logger logger = LoggerFactory.getLogger(JsonLibroDAO.class);

    private final String filePath;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Crea un JsonLibroDAO con percorso file personalizzato.
     *
     * @param filePath percorso del file JSON usato per salvare/caricare i libri.
     */
    public JsonLibroDAO(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Libro> getAll() throws DAOException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.debug("File {} non esistente, ritorno lista vuota ", filePath);
                return new ArrayList<>();
            }
            List<Libro> libri = mapper.readValue(file, new TypeReference<List<Libro>>() {
            });
            logger.debug("Caricati {} libri da {}", libri.size(), filePath);
            return libri;
        } catch (IOException e) {
            logger.error("Errore durante la lettura del file {}", filePath, e);
            throw new DAOException("Impossibile leggere i dati dal file " + filePath, e);
        }
    }

    @Override
    public void saveAll(List<Libro> libri) throws DAOException {
        try {
            File file = new File(filePath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, libri);
            logger.debug("Salvati {} libri in {}", libri.size(), filePath);
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio nel file {}", filePath, e);
            throw new DAOException("Impossibile salvare i dati nel file " + filePath, e);
        }
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) throws DAOException {
        List<Libro> libri = getAll();
        Optional<Libro> result = libri.stream().filter(l -> l.getIsbn().equals(isbn)).findFirst();
        logger.debug("Ricerca libro con ISBN {}: {}", isbn, result.isPresent() ? "trovato" : "non trovato");
        return result;
    }

    @Override
    public void add(Libro libro) throws LibroAlreadyExistsException, DAOException {
        List<Libro> libri = getAll();

        if (libri.contains(libro)) {
            logger.warn("Tentativo di aggiunta libro già esistente con ISBN {}", libro.getIsbn());
            throw new LibroAlreadyExistsException(libro.getIsbn());
        }

        libri.add(libro);
        saveAll(libri);
        logger.info("Aggiunto libro: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());
    }

    @Override
    public void removeByIsbn(String isbn) throws LibroNotFoundException, DAOException {
        List<Libro> libri = getAll();

        boolean rimosso = libri.removeIf(libro -> libro.getIsbn().equals(isbn));
        if (!rimosso) {
            logger.warn("Tentativo di rimozione libro non esistente con ISBN {}", isbn);
            throw new LibroNotFoundException(isbn);
        }
        saveAll(libri);
        logger.info("Rimosso libro con ISBN {}", isbn);
    }

    @Override
    public void update(Libro libro) throws LibroNotFoundException, DAOException {
        List<Libro> libri = getAll();

        for (int i = 0; i < libri.size(); i++) {
            if (libri.get(i).equals(libro)) {
                libri.set(i, libro);
                saveAll(libri);
                logger.info("Aggiornato libro: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());
                return;
            }
        }
        logger.warn("Tentativo di aggiornamento libro non esistente con ISBN {}", libro.getIsbn());
        throw new LibroNotFoundException(libro.getIsbn());
    }

    @Override
    public boolean prefersBatchOperations() {
        return true;
    }
}

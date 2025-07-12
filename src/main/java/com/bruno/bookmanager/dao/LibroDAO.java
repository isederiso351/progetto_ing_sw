package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.dao.filters.Filter;
import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Libro;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per l'accesso e la gestione dei dati relativi ai libri.
 * Rappresenta un contratto che può essere implementato con varie tecnologie di persistenza
 * (es. file JSON, database SQLite, cache in memoria, ecc.).
 */
public interface LibroDAO {

    /**
     * Restituisce la lista completa dei libri memorizzati.
     *
     * @return lista di tutti i libri
     * @throws DAOException se si verifica un errore nell'accesso ai dati
     */
    List<Libro> getAll() throws DAOException;

    /**
     * Sovrascrive l'intera collezione di libri persistendo i dati forniti.
     *
     * @param libri lista completa dei libri da salvare
     * @throws DAOException se si verifica un errore nel salvataggio
     */
    void saveAll(List<Libro> libri) throws DAOException;

    /**
     * Cerca un libro tramite il suo ISBN
     *
     * @param isbn ISBN del libro cercato
     * @return Optional contenente il libro, se trovato
     * @throws DAOException se si verifica un errore nell'accesso ai dati
     */
    Optional<Libro> getByIsbn(String isbn) throws DAOException;

    /**
     * Aggiunge un nuovo libro alla collezione
     *
     * @param libro il libro da agiungere
     * @throws LibroAlreadyExistsException se un libro con lo stesso ISBN è già presente
     * @throws DAOException                se si verifica un errore nell'accesso ai dati
     */
    void add(Libro libro) throws LibroAlreadyExistsException, DAOException;

    /**
     * Rimuove un libro tramite il suo ISBN
     *
     * @param isbn ISBN del libro da rimuovere
     * @throws LibroNotFoundException se il libro non viene trovato
     * @throws DAOException           se si verifica un errore nell'accesso ai dati
     */
    void removeByIsbn(String isbn) throws LibroNotFoundException, DAOException;

    /**
     * Aggiorna le informazioni di un libro già presente, identificato da ISBN
     *
     * @param libro libro aggiornato
     * @throws LibroNotFoundException se il libro non viene trovato
     * @throws DAOException           se si verifica un errore nell'accesso ai dati
     */
    void update(Libro libro) throws LibroNotFoundException, DAOException;


    /**
     * Filtra i libri secondo il filtro specificato
     *
     * @param filter filtro da applicare
     * @return lista di libri che soddisfano il filtro
     * @throws DAOException se si verifica un errore nell'accesso ai dati
     */
    List<Libro> getByFilter(Filter<Libro> filter) throws DAOException;

    /**
     * Indica se questa implementazione preferisce operazioni batch (saveAll)
     * rispetto a operazioni singole (add/update/remove).
     * <p>
     * Le implementazioni basate su file (JSON, CSV) dovrebbero restituire true,
     * mentre quelle basate su database dovrebbero restituire false.
     *
     * @return true se preferisce operazioni batch, false se preferisce operazioni singole
     */
    default boolean prefersBatchOperations() {
        return false;
    }
}

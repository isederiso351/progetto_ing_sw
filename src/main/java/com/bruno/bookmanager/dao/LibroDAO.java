package com.bruno.bookmanager.dao;

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
     * Restituisce la lista completa dei librimemorizzati.
     *
     * @return lista di tutti i libri
     */
    List<Libro> getAll();

    /**
     * Sovrascrive l'intera collezione di libri persistendo i dati forniti.
     *
     * @param libri lista completa dei libri da salvare
     */
    void saveAll(List<Libro> libri);

    /**
     * Cerca un libro tramite il suo ISBN
     *
     * @param isbn ISBN del libro cercato
     * @return Optional contenente il libro, se trovato
     */
    Optional<Libro> getByIsbn(String isbn);

    /**
     * Aggiunge un nuovo libro alla collezione
     *
     * @param libro il libro da agiungere
     * @throws IllegalArgumentException se un libro con lo stesso ISBN è già presente
     */
    void add(Libro libro);

    /**
     * Rimuove un libro tramite il suo ISBN, se presente
     *
     * @param isbn ISBN del libro da rimuovere
     * @return true se il libro è stato trovato e rimosso, false altrimenti
     */
    boolean removeByIsbn(String isbn);

    /**
     * Aggiorna le informazioni di un libro già presente, identificato da ISBN
     *
     * @param libro libro aggiornato
     * @return true se l'aggiornamento è avvenuto con successo, false se il libro non esisteva
     */
    boolean update(Libro libro);
}

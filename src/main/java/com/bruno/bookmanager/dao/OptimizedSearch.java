package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.service.SearchCriteria;

import java.util.List;

/**
 * Interfaccia per DAO che supportano ricerche e ordinamenti ottimizzati.
 * I DAO basati su database possono implementare questa interfaccia per fornire
 * query SQL ottimizzate invece di caricare tutti i dati in memoria.
 */
public interface OptimizedSearch {
    /**
     * Cerca, filtra e ordina libri seguento il SearchCriteria passato.
     *
     * @param criteria criteri di ricerca (null per tutti i libri)
     * @return lista dei libri che soddisfano i criteri
     * @throws DAOException se si verifica un errore durante la ricerca
     */
    List<Libro> search(SearchCriteria criteria) throws DAOException;
}
package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.dao.filters.Filter;
import com.bruno.bookmanager.model.Libro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione di {@link LibroDAO} che aggiunge una cache in memoria sopra un altro DAO.
 * <p>
 * Utilizza il pattern Decorator per migliorare le performance evitando accessi ripetuti al DAO sottostante.
 * La cache viene inizializzata al primo accesso e aggiornata ad ogni modifica.
 */
public class CachedLibroDAO implements LibroDAO {

    private final LibroDAO delegate;
    private List<Libro> cache;

    /**
     * Costruisce un CachedLibroDAO che decora un altro LibroDAO.
     *
     * @param delegate DAO sottostante che effettua la persistenza.
     */
    public CachedLibroDAO(LibroDAO delegate) {
        this.delegate = delegate;
    }

    private List<Libro> getCache() {
        if (cache == null) {
            cache = delegate.getAll();
        }
        return cache;
    }

    @Override
    public List<Libro> getAll() {
        return new ArrayList<>(getCache());
    }

    @Override
    public void saveAll(List<Libro> libri) {
        cache = new ArrayList<>(libri);
        delegate.saveAll(cache);
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) {
        return getCache().stream().filter(l -> l.getIsbn().equals(isbn)).findFirst();
    }

    @Override
    public void add(Libro libro) {
        if (getCache().contains(libro))
            throw new IllegalArgumentException("Libro già presente con isbn: " + libro.getIsbn());
        cache.add(libro);
        delegate.saveAll(cache);
    }

    @Override
    public boolean removeByIsbn(String isbn) {
        if (getCache().removeIf(l -> l.getIsbn().equals(isbn))) {
            delegate.saveAll(cache);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Libro libro) {
        getCache();
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).equals(libro)) {
                cache.set(i, libro);
                delegate.saveAll(cache);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Libro> getByFilter(Filter<Libro> filter) {
        return getCache().stream().filter(filter::test).collect(Collectors.toList());
    }
}

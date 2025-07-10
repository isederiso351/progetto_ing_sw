package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.dao.filters.Filter;
import com.bruno.bookmanager.model.Libro;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione dell'interfaccia {@link LibroDAO} che salva e carica i dati da un file JSON.
 * <p>
 * Utilizza la libreria Jackson per serializzare e deserializzare la lista di libri.
 * È una soluzione semplice e adatta a piccoli dataset persistenti su disco.
 */
public class JsonLibroDAO implements LibroDAO {

    private final String filePath;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonLibroDAO() {
        this("libri.json");
    }

    /**
     * Crea un JsonLibroDAO con percorso file personalizzato.
     *
     * @param filePath percorso del file JSON usato per salvare/caricare i libri.
     */
    public JsonLibroDAO(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Libro> getAll() {
        try {
            File file = new File(filePath);
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<List<Libro>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(List<Libro> libri) {
        try {
            File file = new File(filePath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, libri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) {
        List<Libro> libri = getAll();
        return libri.stream().filter(l -> l.getIsbn().equals(isbn)).findAny();
    }

    @Override
    public void add(Libro libro) {
        List<Libro> libri = getAll();
        if (libri.contains(libro))
            throw new IllegalArgumentException("Libro già presente con isbn: " + libro.getIsbn());
        libri.add(libro);
        saveAll(libri);
    }

    @Override
    public boolean removeByIsbn(String isbn) {
        List<Libro> libri = getAll();
        boolean rimosso = libri.removeIf(libro -> libro.getIsbn().equals(isbn));
        if (rimosso) saveAll(libri);
        return rimosso;
    }

    @Override
    public boolean update(Libro libro) {
        List<Libro> libri = getAll();
        for (int i = 0; i < libri.size(); i++) {
            if (libri.get(i).equals(libro)) {
                libri.set(i, libro);
                saveAll(libri);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Libro> getByFilter(Filter<Libro> filter) {
        List<Libro> libri = getAll();
        return libri.stream().filter(filter::test).collect(Collectors.toList());
    }
}

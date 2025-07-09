package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.model.Libro;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private final String filePath;
    private final ObjectMapper mapper = new ObjectMapper();

    public LibroDAO() {
        this("libri.json");
    }

    public LibroDAO(String filePath) {
        this.filePath = filePath;
    }

    public List<Libro> scaricaLibri() {
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

    public void caricaLibri(List<Libro> libri) {
        try {
            File file = new File(filePath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, libri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean eliminaLibro(String isbn) {
        List<Libro> libri = scaricaLibri();
        boolean rimosso = libri.removeIf(libro -> libro.getIsbn().equals(isbn));
        if (rimosso) caricaLibri(libri);
        return rimosso;
    }

    public boolean aggiornaLibro(Libro libro) {
        List<Libro> libri = scaricaLibri();
        for (int i = 0; i < libri.size(); i++) {
            if (libri.get(i).equals(libro)) {
                libri.set(i, libro);
                caricaLibri(libri);
                return true;
            }
        }
        return false;
    }


}

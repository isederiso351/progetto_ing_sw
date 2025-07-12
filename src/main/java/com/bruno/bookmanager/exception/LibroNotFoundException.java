package com.bruno.bookmanager.exception;

/**
 * Eccezione lanciata quando un libro cercato non viene trovato.
 */
public class LibroNotFoundException extends BookManagerException {
    public LibroNotFoundException(String isbn) {
        super("Libro con ISBN " + isbn + " non trovato");
    }
}

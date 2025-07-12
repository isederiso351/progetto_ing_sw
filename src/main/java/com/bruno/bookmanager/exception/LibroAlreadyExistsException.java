package com.bruno.bookmanager.exception;

/**
 * Eccezione lanciata quando si tenta di aggiungere un libro già esistente.
 */
public class LibroAlreadyExistsException extends BookManagerException {
    public LibroAlreadyExistsException(String isbn) {
        super("Libro con ISBN " + isbn + " è già presente nella collezione");
    }
}

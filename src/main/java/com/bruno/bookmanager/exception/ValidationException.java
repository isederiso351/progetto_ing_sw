package com.bruno.bookmanager.exception;

/**
 * Eccezione per errori di validazione dei dati.
 */
public class ValidationException extends BookManagerException {
    public ValidationException(String message) {
        super(message);
    }
}
package com.bruno.bookmanager.exception;


/**
 * Eccezione per errori di accesso ai dati.
 */
public class DAOException extends BookManagerException {
    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

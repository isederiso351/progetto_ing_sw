package com.bruno.bookmanager.exception;

/**
 * Eccezione base per tutte le eccezioni del Book Manager.
 */
public class BookManagerException extends Exception {
    public BookManagerException(String message) {
        super(message);
    }

    public BookManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

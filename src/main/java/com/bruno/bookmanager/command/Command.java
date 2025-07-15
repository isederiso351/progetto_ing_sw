package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;

/**
 * Interfaccia base per il pattern Command.
 * Permette di incapsulare operazioni come oggetti, abilitando undo/redo per il controller.
 */
public interface Command {
    /**
     * Esegue il comando.
     *
     * @throws BookManagerException se l'operazione fallisce
     */
    void execute() throws BookManagerException;

    /**
     * Annulla l'operazione eseguita dal comando.
     *
     * @throws BookManagerException se l'annullamento fallisce
     */
    void undo() throws BookManagerException;

    /**
     * Indica se questo comando può essere annullato.
     * Alcuni comandi potrebbero non essere reversibili.
     *
     * @return true se il comando può essere annullato
     */
    default boolean canUndo() {
        return true;
    }

    /**
     * Restituisce una descrizione dell'operazione per logging e UI.
     *
     * @return descrizione dell'operazione
     */
    String getDescription();
}

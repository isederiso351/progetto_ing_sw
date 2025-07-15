package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * Gestisce la cronologia dei comandi per implementare undo/redo.
 */
public final class CommandHistory {

    private static final Logger logger = LoggerFactory.getLogger(CommandHistory.class);
    private static volatile CommandHistory instance;
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private final int maxHistorySize = 50;


    private CommandHistory() {
    }

    public synchronized static CommandHistory getInstance() {
        if (instance == null) {
            instance = new CommandHistory();
        }
        return instance;
    }

    /**
     * Esegue un comando e lo aggiunge alla cronologia.
     *
     * @param command comando da eseguire
     * @throws BookManagerException se l'esecuzione fallisce
     */
    public void executeCommand(Command command) throws BookManagerException {
        command.execute();

        undoStack.push(command);
        redoStack.clear(); // Cancella la cronologia redo dopo una nuova operazione

        while (undoStack.size() > maxHistorySize) {
            undoStack.removeFirst();
        }
        logger.debug("Comando eseguito e aggiunto alla cronologia: {}", command.getDescription());
    }

    /**
     * Annulla l'ultimo comando eseguito.
     *
     * @throws BookManagerException se l'annullamento fallisce
     */
    public void undo() throws BookManagerException {
        if (undoStack.isEmpty()) {
            throw new BookManagerException("Nessun comando da annullare");
        }

        Command command = undoStack.pop();

        if (!command.canUndo()) {
            throw new BookManagerException("Il comando non può essere annullato: " + command.getDescription());
        }

        command.undo();
        redoStack.push(command);

        logger.info("Comando annullato: {}", command.getDescription());
    }

    /**
     * Ripete l'ultimo comando annullato.
     *
     * @throws BookManagerException se la ripetizione fallisce
     */
    public void redo() throws BookManagerException {
        if (redoStack.isEmpty()) {
            throw new BookManagerException("Nessun comando da ripetere");
        }

        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);

        logger.info("Comando ripetuto: {}", command.getDescription());
    }

    /**
     * Verifica se è possibile annullare un comando.
     *
     * @return true se c'è almeno un comando annullabile nella cronologia
     */
    public boolean canUndo() {
        return !undoStack.isEmpty() && undoStack.peek().canUndo();
    }

    /**
     * Verifica se è possibile ripetere un comando.
     *
     * @return true se c'è almeno un comando nella cronologia redo
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Restituisce la descrizione dell'ultimo comando eseguito.
     *
     * @return descrizione del comando o null se non ci sono comandi
     */
    public String getLastCommandDescription() {
        return undoStack.isEmpty() ? null : undoStack.peek().getDescription();
    }

    /**
     * Restituisce la descrizione del prossimo comando da ripetere.
     *
     * @return descrizione del comando o null se non ci sono comandi da ripetere
     */
    public String getNextRedoCommandDescription() {
        return redoStack.isEmpty() ? null : redoStack.peek().getDescription();
    }

    /**
     * Pulisce completamente la cronologia.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        logger.info("Cronologia comandi cancellata");
    }
}

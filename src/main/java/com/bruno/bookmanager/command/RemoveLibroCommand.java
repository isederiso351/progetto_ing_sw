package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.service.LibroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Comando per rimuovere un libro dalla collezione.
 */
public class RemoveLibroCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(RemoveLibroCommand.class);

    private final LibroService service;
    private final String isbn;
    private Libro removedLibro; // Backup per undo

    public RemoveLibroCommand(LibroService service, String isbn) {
        this.isbn = isbn;
        this.service = service;
    }

    @Override
    public void execute() throws BookManagerException {
        Optional<Libro> libroOpt = service.trovaLibroPerIsbn(isbn);
        if (libroOpt.isEmpty()) {
            throw new LibroNotFoundException(isbn);
        }

        removedLibro = libroOpt.get();
        service.rimuoviLibro(isbn);
        logger.info("Eseguito: {}", getDescription());
    }

    @Override
    public void undo() throws BookManagerException {
        if (removedLibro == null) {
            throw new BookManagerException("Impossibile annullare: libro rimosso non trovato");
        }

        service.aggiungiLibro(removedLibro);
        logger.info("Annullato: {}", getDescription());
    }

    @Override
    public String getDescription() {
        String titolo = removedLibro != null ? removedLibro.getTitolo() : "sconosciuto";
        return "Rimozione libro: " + titolo + " (ISBN: " + isbn + ")";
    }
}

package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.service.LibroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Comando per aggiornare un libro esistente.
 */
public class UpdateLibroCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLibroCommand.class);

    private final LibroService service;
    private final Libro newLibro;
    private Libro oldLibro;

    public UpdateLibroCommand(LibroService service, Libro newLibro) {
        this.newLibro = newLibro;
        this.service = service;
    }

    @Override
    public void execute() throws BookManagerException {
        Optional<Libro> libroOpt = service.trovaLibroPerIsbn(newLibro.getIsbn());
        if (libroOpt.isEmpty()) {
            throw new LibroNotFoundException(newLibro.getIsbn());
        }

        oldLibro = libroOpt.get();
        service.aggiornaLibro(newLibro);
        logger.info("Eseguito: {}", getDescription());
    }

    @Override
    public void undo() throws BookManagerException {
        if (oldLibro == null) {
            throw new BookManagerException("Impossibile annullare: stato precedente non trovato");
        }

        service.aggiornaLibro(oldLibro);
        logger.info("Annullato: {}", getDescription());
    }

    @Override
    public String getDescription() {
        return "Aggiornamento libro: " + newLibro.getTitolo() + " (ISBN: " + newLibro.getIsbn() + ")";
    }
}
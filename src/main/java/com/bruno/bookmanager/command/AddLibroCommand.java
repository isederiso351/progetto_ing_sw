package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.service.LibroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddLibroCommand implements Command {

    private static Logger logger = LoggerFactory.getLogger(AddLibroCommand.class);

    private final LibroService service;
    private final Libro libro;

    public AddLibroCommand(LibroService service, Libro libro) {
        this.libro = libro;
        this.service = service;
    }

    @Override
    public void execute() throws BookManagerException {
        service.aggiungiLibro(libro);
        logger.info("Eseguito: {}", getDescription());
    }

    @Override
    public void undo() throws BookManagerException {
        service.rimuoviLibro(libro.getIsbn());
        logger.info("Annullato: {}", getDescription());
    }

    @Override
    public String getDescription() {
        return "Aggiunta libro: " + libro.getTitolo() + " (ISBN: " + libro.getIsbn() + ")";
    }
}

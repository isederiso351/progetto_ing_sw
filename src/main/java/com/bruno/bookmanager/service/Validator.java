package com.bruno.bookmanager.service;

import com.bruno.bookmanager.exception.ValidationException;
import com.bruno.bookmanager.model.Libro;


public class Validator {

    public static void validateLibro(Libro libro) throws ValidationException {
        if (libro == null) {
            throw new ValidationException("Libro non può essere null");
        }
        if (libro.getTitolo() == null || libro.getTitolo().isBlank()) {
            throw new ValidationException("Titolo non può essere vuoto");
        }
        if (libro.getTitolo().length() > 255) {
            throw new ValidationException("Il titolo non può superare i 255 caratteri");
        }
        if (libro.getAutore()!=null&& libro.getAutore().length() > 255) {
            throw new ValidationException("L'autore non può superare i 255 caratteri");
        }
        validateIsbn(libro.getIsbn());
        if (libro.getValutazione() < 0 || libro.getValutazione() > 5) {
            throw new ValidationException("Valutazione deve essere tra 0 e 5");
        }
        if (libro.getStatoLettura() == null) {
            throw new ValidationException("Stato lettura non può essere null");
        }
    }

    public static void validateIsbn(String isbn) throws ValidationException {
        if (isbn == null || isbn.isBlank()) {
            throw new ValidationException("ISBN non può essere vuoto");
        }
        if(!isbn.matches("^(\\d{9}[\\dX]|\\d{13})$"))
            throw new ValidationException("ISBN non valido");
    }


}

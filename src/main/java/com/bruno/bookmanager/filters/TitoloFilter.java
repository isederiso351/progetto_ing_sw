package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Libro;

public class TitoloFilter implements Filter<Libro> {
    private final String titolo;

    public TitoloFilter(String titolo) {
        this.titolo = titolo != null ? titolo.trim() : "";
    }

    @Override
    public boolean test(Libro libro) {
        return libro.getTitolo().toLowerCase().contains(titolo.toLowerCase());
    }

    @Override
    public String toSqlClause() {
        return "titolo LIKE '%" + titolo + "%'";
    }
}

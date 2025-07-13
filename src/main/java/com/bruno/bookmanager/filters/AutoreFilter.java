package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Libro;

public class AutoreFilter implements Filter<Libro>{
    private final String autore;

    public AutoreFilter(String autore) {
        this.autore = autore != null ? autore.trim() : "";
    }

    @Override
    public boolean test(Libro libro) {
        return libro.getTitolo().toLowerCase().contains(autore.toLowerCase());
    }

    @Override
    public String toSqlClause() {
        return "autore LIKE '%" + autore + "%'";
    }
}

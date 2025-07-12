package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;

public class GenereFilter implements Filter<Libro>{
    private final Genere genere;

    public GenereFilter(Genere genere) {
        this.genere = genere;
    }

    @Override
    public boolean test(Libro libro) {
        return libro.getGenere().equals(genere);
    }

    @Override
    public String toSqlClause() {
        return "genere = '" + genere.name() + "'";
    }
}

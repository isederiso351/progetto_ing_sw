package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;

public class StatoLetturaFilter implements Filter<Libro>{

    private final StatoLettura statoLettura;

    public StatoLetturaFilter(StatoLettura stato) {
        this.statoLettura = stato;
    }

    @Override
    public boolean test(Libro libro) {
        return libro.getStatoLettura().equals(statoLettura);
    }

    @Override
    public String toSqlClause() {
        return "stato = '" + statoLettura.name() + "'";
    }
}

package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Libro;

public class ValutazioneFilter implements Filter<Libro> {

    private final int valutazione;

    public ValutazioneFilter(int valutazione) {
        this.valutazione = valutazione;
    }

    @Override
    public boolean test(Libro libro) {
        return valutazione == libro.getValutazione();
    }

    @Override
    public String toSqlClause() {
        return "valutazione = '" + valutazione + "'";
    }
}

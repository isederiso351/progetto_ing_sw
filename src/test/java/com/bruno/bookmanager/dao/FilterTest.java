package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.filters.StatoLetturaFilter;
import com.bruno.bookmanager.filters.ValutazioneFilter;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterTest {
    private List<Libro> libri;

    @BeforeEach
    void setUp() {
        libri = List.of(new Libro("Titolo 1", "Autore 1", "123", Genere.DISTOPIA, 5, StatoLettura.LETTO),
                new Libro("Titolo 2", "Autore 2", "456", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA),
                new Libro("Titolo 3", "Autore 3", "789", Genere.ROMANZO_STORICO, 5, StatoLettura.LETTO),
                new Libro("Titolo 4", "Autore 4", "000", Genere.FANTASCIENZA, 3, StatoLettura.DA_LEGGERE));
    }

    @Test
    void genereFilterTest() {
        GenereFilter filter = new GenereFilter(Genere.FANTASCIENZA);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA));
        assertEquals("genere = 'FANTASCIENZA'", filter.toSqlClause());
    }

    @Test
    void statoLetturaFilterTest() {
        StatoLetturaFilter filter = new StatoLetturaFilter(StatoLettura.LETTO);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getStatoLettura() == StatoLettura.LETTO));

        assertEquals("stato = 'LETTO'", filter.toSqlClause());
    }

    @Test
    void valutazioneFilterTest() {
        ValutazioneFilter filter = new ValutazioneFilter(5);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getValutazione() == 5));

        assertEquals("valutazione = '5'", filter.toSqlClause());
    }

    @Test
    void andFilterTest() {
        Filter<Libro> genereFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> valutazioneFilter = new ValutazioneFilter(4);
        Filter<Libro> andFilter = genereFilter.and(valutazioneFilter);

        List<Libro> result = libri.stream().filter(andFilter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Titolo 2", result.getFirst().getTitolo());

        assertEquals("(genere = 'FANTASCIENZA' AND valutazione = '4')", andFilter.toSqlClause());
    }

    @Test
    void orFilterTest() {
        Filter<Libro> genereFilter = new GenereFilter(Genere.DISTOPIA);
        Filter<Libro> statoFilter = new StatoLetturaFilter(StatoLettura.DA_LEGGERE);
        Filter<Libro> orFilter = genereFilter.or(statoFilter);

        List<Libro> result = libri.stream().filter(orFilter::test).toList();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getTitolo().equals("Titolo 1")));
        assertTrue(result.stream().anyMatch(l -> l.getTitolo().equals("Titolo 4")));

        assertEquals("(genere = 'DISTOPIA' OR stato = 'DA_LEGGERE')", orFilter.toSqlClause());
    }

    @Test
    void complexFilterChainTest() {
        // (FANTASCIENZA AND valutazione = 3) OR (LETTO)
        Filter<Libro> fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> val3Filter = new ValutazioneFilter(3);
        Filter<Libro> lettoFilter = new StatoLetturaFilter(StatoLettura.LETTO);

        Filter<Libro> complexFilter = fantascienzaFilter.and(val3Filter).or(lettoFilter);

        List<Libro> result = libri.stream().filter(complexFilter::test).toList();

        assertEquals(3, result.size());
        assertEquals("((genere = 'FANTASCIENZA' AND valutazione = '3') OR stato = 'LETTO')",
                complexFilter.toSqlClause());
    }
}

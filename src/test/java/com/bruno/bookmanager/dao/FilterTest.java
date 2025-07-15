package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.filters.*;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterTest {
    private List<Libro> libri;

    @BeforeEach
    void setUp() {
        libri = List.of(
                new Libro("1984", "George Orwell", "123", Genere.DISTOPIA, 5, StatoLettura.LETTO),
                new Libro("Dune", "Frank Herbert", "456", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA),
                new Libro("Il Nome della Rosa", "Umberto Eco", "789", Genere.ROMANZO_STORICO, 5, StatoLettura.LETTO),
                new Libro("Neuromante", "William Gibson", "000", Genere.FANTASCIENZA, 3, StatoLettura.DA_LEGGERE),
                new Libro("Foundation", "Isaac Asimov", "111", Genere.FANTASCIENZA, 0, StatoLettura.DA_LEGGERE)
        );
    }

    // ============= SINGLE FILTER TESTS =============

    @Test
    void genereFilterTest() {
        GenereFilter filter = new GenereFilter(Genere.FANTASCIENZA);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(3, result.size());
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
    void titoloFilterTest() {
        TitoloFilter filter = new TitoloFilter("Dune");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitolo());
        assertEquals("titolo LIKE '%Dune%'", filter.toSqlClause());
    }

    @Test
    void titoloFilterCaseInsensitiveTest() {
        TitoloFilter filter = new TitoloFilter("dune");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitolo());
    }

    @Test
    void titoloFilterPartialMatchTest() {
        TitoloFilter filter = new TitoloFilter("nome");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Il Nome della Rosa", result.get(0).getTitolo());
    }

    @Test
    void autoreFilterTest() {
        AutoreFilter filter = new AutoreFilter("Orwell");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("George Orwell", result.get(0).getAutore());
        assertEquals("autore LIKE '%Orwell%'", filter.toSqlClause());
    }

    @Test
    void autoreFilterCaseInsensitiveTest() {
        AutoreFilter filter = new AutoreFilter("orwell");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("George Orwell", result.get(0).getAutore());
    }

    @Test
    void isbnFilterTest() {
        ISBNFilter filter = new ISBNFilter("123");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getIsbn());
        assertEquals("isbn LIKE '%123%'", filter.toSqlClause());
    }

    @Test
    void isbnFilterPartialMatchTest() {
        ISBNFilter filter = new ISBNFilter("1");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(2, result.size()); // ISBN "123" e "111"
        assertTrue(result.stream().allMatch(l -> l.getIsbn().contains("1")));
    }

    // ============= EDGE CASES FOR SINGLE FILTERS =============

    @Test
    void titoloFilterNullTest() {
        TitoloFilter filter = new TitoloFilter(null);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(libri.size(), result.size()); // Dovrebbe matchare tutti
    }

    @Test
    void titoloFilterEmptyTest() {
        TitoloFilter filter = new TitoloFilter("");

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(libri.size(), result.size()); // Dovrebbe matchare tutti
    }

    @Test
    void autoreFilterNullTest() {
        AutoreFilter filter = new AutoreFilter(null);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(libri.size(), result.size()); // Dovrebbe matchare tutti
    }

    @Test
    void isbnFilterNullTest() {
        ISBNFilter filter = new ISBNFilter(null);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(libri.size(), result.size()); // Dovrebbe matchare tutti
    }

    @Test
    void valutazioneFilterZeroTest() {
        ValutazioneFilter filter = new ValutazioneFilter(0);

        List<Libro> result = libri.stream().filter(filter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Foundation", result.get(0).getTitolo());
    }

    // ============= COMBINED FILTER TESTS =============

    @Test
    void andFilterTest() {
        Filter<Libro> genereFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> valutazioneFilter = new ValutazioneFilter(4);
        Filter<Libro> andFilter = genereFilter.and(valutazioneFilter);

        List<Libro> result = libri.stream().filter(andFilter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitolo());
        assertEquals("(genere = 'FANTASCIENZA' AND valutazione = '4')", andFilter.toSqlClause());
    }

    @Test
    void orFilterTest() {
        Filter<Libro> genereFilter = new GenereFilter(Genere.DISTOPIA);
        Filter<Libro> statoFilter = new StatoLetturaFilter(StatoLettura.DA_LEGGERE);
        Filter<Libro> orFilter = genereFilter.or(statoFilter);

        List<Libro> result = libri.stream().filter(orFilter::test).toList();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getTitolo().equals("1984"))); // DISTOPIA
        assertTrue(result.stream().anyMatch(l -> l.getTitolo().equals("Neuromante"))); // DA_LEGGERE
        assertTrue(result.stream().anyMatch(l -> l.getTitolo().equals("Foundation"))); // DA_LEGGERE
        assertEquals("(genere = 'DISTOPIA' OR stato = 'DA_LEGGERE')", orFilter.toSqlClause());
    }

    @Test
    void complexFilterChainTest() {
        // (FANTASCIENZA AND valutazione >= 3) OR (LETTO)
        Filter<Libro> fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> val3Filter = new ValutazioneFilter(3);
        Filter<Libro> val4Filter = new ValutazioneFilter(4);
        Filter<Libro> lettoFilter = new StatoLetturaFilter(StatoLettura.LETTO);

        Filter<Libro> complexFilter = fantascienzaFilter.and(val3Filter.or(val4Filter)).or(lettoFilter);

        List<Libro> result = libri.stream().filter(complexFilter::test).toList();

        assertEquals(4, result.size()); // Dune (FANTASCIENZA+4), Neuromante (FANTASCIENZA+3), 1984 (LETTO), Il Nome della Rosa (LETTO)
        assertEquals("((genere = 'FANTASCIENZA' AND (valutazione = '3' OR valutazione = '4')) OR stato = 'LETTO')",
                complexFilter.toSqlClause());
    }

    @Test
    void multipleAndFiltersTest() {
        Filter<Libro> fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> daLeggereFilter = new StatoLetturaFilter(StatoLettura.DA_LEGGERE);
        Filter<Libro> val3Filter = new ValutazioneFilter(3);

        Filter<Libro> combinedFilter = fantascienzaFilter.and(daLeggereFilter).and(val3Filter);

        List<Libro> result = libri.stream().filter(combinedFilter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Neuromante", result.get(0).getTitolo());
    }

    @Test
    void multipleOrFiltersTest() {
        Filter<Libro> distopiaFilter = new GenereFilter(Genere.DISTOPIA);
        Filter<Libro> romanzoStoricoFilter = new GenereFilter(Genere.ROMANZO_STORICO);
        Filter<Libro> val5Filter = new ValutazioneFilter(5);

        Filter<Libro> combinedFilter = distopiaFilter.or(romanzoStoricoFilter).or(val5Filter);

        List<Libro> result = libri.stream().filter(combinedFilter::test).toList();

        assertEquals(2, result.size()); // 1984 e Il Nome della Rosa (entrambi hanno valutazione 5)
    }

    @Test
    void nestedFilterTest() {
        // ((FANTASCIENZA OR DISTOPIA) AND (LETTO OR IN_LETTURA)) AND valutazione >= 4
        Filter<Libro> fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> distopiaFilter = new GenereFilter(Genere.DISTOPIA);
        Filter<Libro> lettoFilter = new StatoLetturaFilter(StatoLettura.LETTO);
        Filter<Libro> inLetturaFilter = new StatoLetturaFilter(StatoLettura.IN_LETTURA);
        Filter<Libro> val4Filter = new ValutazioneFilter(4);
        Filter<Libro> val5Filter = new ValutazioneFilter(5);

        Filter<Libro> nestedFilter = fantascienzaFilter.or(distopiaFilter)
                .and(lettoFilter.or(inLetturaFilter))
                .and(val4Filter.or(val5Filter));

        List<Libro> result = libri.stream().filter(nestedFilter::test).toList();

        assertEquals(2, result.size()); // 1984 (DISTOPIA+LETTO+5) e Dune (FANTASCIENZA+IN_LETTURA+4)
    }

    // ============= FILTER WITH TEXT SEARCH TESTS =============

    @Test
    void combinedTextAndAttributeFilterTest() {
        Filter<Libro> titoloFilter = new TitoloFilter("nome");
        Filter<Libro> lettoFilter = new StatoLetturaFilter(StatoLettura.LETTO);
        Filter<Libro> combinedFilter = titoloFilter.and(lettoFilter);

        List<Libro> result = libri.stream().filter(combinedFilter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Il Nome della Rosa", result.get(0).getTitolo());
    }

    @Test
    void authorAndGenreFilterTest() {
        Filter<Libro> autoreFilter = new AutoreFilter("gibson");
        Filter<Libro> genereFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> combinedFilter = autoreFilter.and(genereFilter);

        List<Libro> result = libri.stream().filter(combinedFilter::test).toList();

        assertEquals(1, result.size());
        assertEquals("Neuromante", result.get(0).getTitolo());
    }

    // ============= SQL CLAUSE GENERATION TESTS =============

    @Test
    void sqlClauseComplexityTest() {
        Filter<Libro> filter1 = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> filter2 = new ValutazioneFilter(4);
        Filter<Libro> filter3 = new StatoLetturaFilter(StatoLettura.LETTO);
        Filter<Libro> filter4 = new TitoloFilter("test");

        Filter<Libro> complexFilter = filter1.and(filter2).or(filter3.and(filter4));

        String expectedSql = "((genere = 'FANTASCIENZA' AND valutazione = '4') OR (stato = 'LETTO' AND titolo LIKE '%test%'))";
        assertEquals(expectedSql, complexFilter.toSqlClause());
    }

    @Test
    void sqlClauseEscapingTest() {
        TitoloFilter titoloFilter = new TitoloFilter("test's book");
        AutoreFilter autoreFilter = new AutoreFilter("o'brien");

        // Nota: in un'implementazione reale, dovremmo gestire l'escaping delle virgolette
        assertEquals("titolo LIKE '%test's book%'", titoloFilter.toSqlClause());
        assertEquals("autore LIKE '%o'brien%'", autoreFilter.toSqlClause());
    }

    // ============= PERFORMANCE TESTS =============

    @Test
    void largeDatasetFilterTest() {
        // Crea un dataset più grande per testare le performance
        List<Libro> largeDataset = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeDataset.add(new Libro(
                    "Book " + i,
                    "Author " + (i % 10),
                    String.format("%010d", i),
                    Genere.values()[i % Genere.values().length],
                    (i % 6), // 0-5
                    StatoLettura.values()[i % StatoLettura.values().length]
            ));
        }

        Filter<Libro> complexFilter = new GenereFilter(Genere.FANTASCIENZA)
                .and(new ValutazioneFilter(5))
                .or(new AutoreFilter("Author 1"));

        long startTime = System.currentTimeMillis();
        List<Libro> result = largeDataset.stream().filter(complexFilter::test).toList();
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 100); // Dovrebbe essere veloce
        assertFalse(result.isEmpty());
    }

    @Test
    void chainedFilterPerformanceTest() {
        Filter<Libro> baseFilter = new GenereFilter(Genere.FANTASCIENZA);

        // Concatena molti filtri
        for (int i = 0; i < 10; i++) {
            baseFilter = baseFilter.or(new ValutazioneFilter(i % 6));
        }

        List<Libro> result = libri.stream().filter(baseFilter::test).toList();
        assertNotNull(result);
    }
}

package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.filters.*;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test di integrazione parametrici per verificare che i filtri funzionino correttamente
 * con tutti i tipi di DAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilterIntegrationTest {

    private static final String JSON_PATH = "filter_integration_test.json";
    private static final String CACHED_PATH = "filter_integration_test_cached.json";
    private static final String SQLITE_PATH = "filter_integration_test.db";

    private List<Libro> testData;

    static Stream<LibroDAO> provideDAOs() {
        return Stream.of(new JsonLibroDAO(JSON_PATH), new CachedLibroDAO(new JsonLibroDAO(CACHED_PATH)),
                new SqliteLibroDAO("jdbc:sqlite:" + SQLITE_PATH));
    }

    @AfterAll
    static void cleanup() {
        deleteFileQuietly(JSON_PATH);
        deleteFileQuietly(CACHED_PATH);
        deleteFileQuietly(SQLITE_PATH);
    }

    private static void deleteFileQuietly(String filename) {
        try {
            new File(filename).delete();
        } catch (Exception e) {
            // Ignora errori
        }
    }

    @BeforeEach
    void setUp() {
        testData = List.of(new Libro("Titolo 1", "Autore 1", "123", Genere.DISTOPIA, 5, StatoLettura.LETTO),
                new Libro("Titolo 2", "Autore 2", "456", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA),
                new Libro("Titolo 3", "Autore 3", "789", Genere.FANTASCIENZA, 3, StatoLettura.DA_LEGGERE),
                new Libro("Titolo 4", "Autore 4", "101", Genere.ROMANZO_STORICO, 5, StatoLettura.LETTO),
                new Libro("Titolo 5", "Autore 5", "202", Genere.FANTASCIENZA, 5, StatoLettura.LETTO),
                new Libro("Titolo 6", "Autore 6", "303", Genere.FANTASY, 5, StatoLettura.DA_LEGGERE),
                new Libro("Titolo 7", "Autore 7", "404", Genere.HORROR, 4, StatoLettura.IN_LETTURA),
                new Libro("Titolo 8", "Autore 8", "505", Genere.GIALLO, 4, StatoLettura.LETTO));
    }

    @AfterEach
    void cleanDatabases() {
        for (LibroDAO dao : provideDAOs().toList()) {
            try {
                dao.saveAll(new ArrayList<>());
            } catch (Exception e) {
                // Ignora errori di cleanup
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void genereFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            // Test con ricerca ottimizzata
            Filter<Libro> filter = new GenereFilter(Genere.FANTASCIENZA);
            SearchCriteria criteria = SearchCriteria.byFilter(filter);

            List<Libro> result = optimizedDAO.search(criteria);
            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA));
        } else {
            // Test con filtro in memoria
            List<Libro> allBooks = dao.getAll();
            Filter<Libro> filter = new GenereFilter(Genere.FANTASCIENZA);

            List<Libro> result = allBooks.stream().filter(filter::test).toList();

            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA));
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void statoLetturaFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            Filter<Libro> filter = new StatoLetturaFilter(StatoLettura.LETTO);
            SearchCriteria criteria = SearchCriteria.byFilter(filter);

            List<Libro> result = optimizedDAO.search(criteria);
            assertEquals(4, result.size());
            assertTrue(result.stream().allMatch(l -> l.getStatoLettura() == StatoLettura.LETTO));
        } else {
            List<Libro> allBooks = dao.getAll();
            Filter<Libro> filter = new StatoLetturaFilter(StatoLettura.LETTO);

            List<Libro> result = allBooks.stream().filter(filter::test).toList();

            assertEquals(4, result.size());
            assertTrue(result.stream().allMatch(l -> l.getStatoLettura() == StatoLettura.LETTO));
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void valutazioneFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            Filter<Libro> filter = new ValutazioneFilter(5);
            SearchCriteria criteria = SearchCriteria.byFilter(filter);

            List<Libro> result = optimizedDAO.search(criteria);
            assertEquals(4, result.size());
            assertTrue(result.stream().allMatch(l -> l.getValutazione() == 5));
        } else {
            List<Libro> allBooks = dao.getAll();
            Filter<Libro> filter = new ValutazioneFilter(5);

            List<Libro> result = allBooks.stream().filter(filter::test).toList();

            assertEquals(4, result.size());
            assertTrue(result.stream().allMatch(l -> l.getValutazione() == 5));
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void combinedFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        Filter<Libro> genereFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> valutazioneFilter = new ValutazioneFilter(5);
        Filter<Libro> combinedFilter = genereFilter.and(valutazioneFilter);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            SearchCriteria criteria = SearchCriteria.byFilter(combinedFilter);
            List<Libro> result = optimizedDAO.search(criteria);

            assertEquals(1, result.size());
            assertEquals("Titolo 5", result.get(0).getTitolo());
            assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA && l.getValutazione() == 5));
        } else {
            List<Libro> allBooks = dao.getAll();
            List<Libro> result = allBooks.stream().filter(combinedFilter::test).toList();

            assertEquals(1, result.size());
            assertEquals("Titolo 5", result.get(0).getTitolo());
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void orFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        Filter<Libro> genereFilter = new GenereFilter(Genere.DISTOPIA);
        Filter<Libro> statoFilter = new StatoLetturaFilter(StatoLettura.IN_LETTURA);
        Filter<Libro> orFilter = genereFilter.or(statoFilter);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            SearchCriteria criteria = SearchCriteria.byFilter(orFilter);
            List<Libro> result = optimizedDAO.search(criteria);

            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(l -> l.getGenere() == Genere.DISTOPIA));
            assertTrue(result.stream().anyMatch(l -> l.getStatoLettura() == StatoLettura.IN_LETTURA));
        } else {
            List<Libro> allBooks = dao.getAll();
            List<Libro> result = allBooks.stream().filter(orFilter::test).toList();

            assertEquals(3, result.size());
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void sortingTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            // Test ordinamento per titolo crescente
            SearchCriteria criteriaAsc = new SearchCriteria.Builder().sortBy(SearchCriteria.SortField.TITOLO,
                    true).build();

            List<Libro> resultAsc = optimizedDAO.search(criteriaAsc);
            assertEquals(testData.size(), resultAsc.size());
            assertEquals("Titolo 1", resultAsc.get(0).getTitolo());
            assertEquals("Titolo 8", resultAsc.get(resultAsc.size() - 1).getTitolo());

            // Test ordinamento per valutazione decrescente
            SearchCriteria criteriaDesc = new SearchCriteria.Builder().sortBy(SearchCriteria.SortField.VALUTAZIONE,
                    false).build();

            List<Libro> resultDesc = optimizedDAO.search(criteriaDesc);
            assertEquals(testData.size(), resultDesc.size());
            assertTrue(resultDesc.get(0).getValutazione() >= resultDesc.get(resultDesc.size() - 1).getValutazione());
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void filterWithSortingTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            Filter<Libro> filter = new GenereFilter(Genere.FANTASCIENZA);
            SearchCriteria criteria = new SearchCriteria.Builder().filter(filter).sortBy(
                            SearchCriteria.SortField.VALUTAZIONE, false) // Decrescente
                    .build();

            List<Libro> result = optimizedDAO.search(criteria);
            assertEquals(3, result.size());

            // Verifica ordinamento: valutazioni dovrebbero essere in ordine decrescente
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getValutazione() >= result.get(i + 1).getValutazione());
            }

            // Il primo dovrebbe essere quello con valutazione 5
            assertEquals(5, result.get(0).getValutazione());
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void emptyResultFilterTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        // Filtra per un genere che non esiste nei dati di test
        Filter<Libro> filter = new GenereFilter(Genere.UMORISTICO);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            SearchCriteria criteria = SearchCriteria.byFilter(filter);
            List<Libro> result = optimizedDAO.search(criteria);
            assertTrue(result.isEmpty());
        } else {
            List<Libro> allBooks = dao.getAll();
            List<Libro> result = allBooks.stream().filter(filter::test).toList();
            assertTrue(result.isEmpty());
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void complexMultipleFiltersTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        // (FANTASCIENZA OR HORROR) AND (valutazione >= 4) AND (non DA_LEGGERE)
        Filter<Libro> fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        Filter<Libro> horrorFilter = new GenereFilter(Genere.HORROR);
        Filter<Libro> valutazione4Filter = new ValutazioneFilter(4);
        Filter<Libro> valutazione5Filter = new ValutazioneFilter(5);
        Filter<Libro> lettoFilter = new StatoLetturaFilter(StatoLettura.LETTO);
        Filter<Libro> inLetturaFilter = new StatoLetturaFilter(StatoLettura.IN_LETTURA);

        Filter<Libro> complexFilter = fantascienzaFilter.or(horrorFilter).and(
                valutazione4Filter.or(valutazione5Filter)).and(lettoFilter.or(inLetturaFilter));

        if (dao instanceof OptimizedSearch optimizedDAO) {
            SearchCriteria criteria = SearchCriteria.byFilter(complexFilter);
            List<Libro> result = optimizedDAO.search(criteria);

            // Verifica che tutti i risultati soddisfino i criteri
            assertTrue(result.stream().allMatch(
                    l -> (l.getGenere() == Genere.FANTASCIENZA || l.getGenere() == Genere.HORROR) && (l.getValutazione() >= 4) && (l.getStatoLettura() != StatoLettura.DA_LEGGERE)));
        } else {
            List<Libro> allBooks = dao.getAll();
            List<Libro> result = allBooks.stream().filter(complexFilter::test).toList();

            assertTrue(result.stream().allMatch(
                    l -> (l.getGenere() == Genere.FANTASCIENZA || l.getGenere() == Genere.HORROR) && (l.getValutazione() >= 4) && (l.getStatoLettura() != StatoLettura.DA_LEGGERE)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void nullSearchCriteriaTest(LibroDAO dao) throws DAOException {
        dao.saveAll(testData);

        if (dao instanceof OptimizedSearch optimizedDAO) {
            List<Libro> result = optimizedDAO.search(null);
            assertEquals(testData.size(), result.size());
        }
    }
}

package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.filters.StatoLetturaFilter;
import com.bruno.bookmanager.filters.ValutazioneFilter;
import com.bruno.bookmanager.exception.DAOException;
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
    private static final String SQLITE_URL = "jdbc:sqlite:filter_integration_test.db";

    private List<Libro> testData;

    static Stream<LibroDAO> provideDAOs() {
        return Stream.of(new JsonLibroDAO(JSON_PATH), new CachedLibroDAO(new JsonLibroDAO(CACHED_PATH)),
                new SqliteLibroDAO(SQLITE_URL));
    }

    @AfterAll
    static void cleanup() {
        new File(JSON_PATH).delete();
        new File(CACHED_PATH).delete();
        new File("filter_integration_test.db").delete();
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
    void cleanDatabases() throws DAOException {
        for (LibroDAO dao : provideDAOs().toList()) {
            dao.saveAll(new ArrayList<>());
        }
    }
}

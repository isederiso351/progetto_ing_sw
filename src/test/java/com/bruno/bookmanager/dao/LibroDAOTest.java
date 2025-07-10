package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibroDAOTest {

    private static final String JSON_PATH = "libri_test.json";
    private static final String CACHED_PATH = "libri_test_cache.json";
    private static final String SQLITE_URL = "jdbc:sqlite:libri_test.db";

    private List<Libro> libri;
    private Comparator<Libro> byIsbn = Comparator.comparing(Libro::getIsbn);

    static Stream<LibroDAO> provideDAOs() {
        return Stream.of(new JsonLibroDAO(JSON_PATH), new CachedLibroDAO(new JsonLibroDAO(CACHED_PATH)), new SqliteLibroDAO(SQLITE_URL));
    }

    @BeforeEach
    void setupLibri() {
        libri = new ArrayList<>();
        libri.add(new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO));
        libri.add(new Libro("Titolo 2", "Autore 2", "987654321", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA));
        libri.sort(byIsbn);
    }

    @AfterAll
    void cleanup() {
        new File(JSON_PATH).delete();
        new File(CACHED_PATH).delete();
        new File("libri_test.db").delete();
    }


    @ParameterizedTest
    @MethodSource("provideDAOs")
    void save_getTest(LibroDAO dao) {
        dao.saveAll(libri);
        List<Libro> scaricati = dao.getAll();
        scaricati.sort(byIsbn);
        assertTrue(scaricati.equals(libri));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void removeTest(LibroDAO dao) {
        dao.saveAll(libri);
        assertTrue(dao.removeByIsbn("123456789"));

        List<Libro> dopo = dao.getAll();
        assertEquals(1, dopo.size());
        assertEquals("987654321", dopo.getFirst().getIsbn());

        assertFalse(dao.removeByIsbn("000000000"));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void updateTest(LibroDAO dao) {
        dao.saveAll(libri);
        Libro aggiornato = new Libro("Titolo 1 Aggiornato", "Autore 1", "123456789", Genere.FANTASY, 5, StatoLettura.IN_LETTURA);
        assertTrue(dao.update(aggiornato));

        List<Libro> dopo = dao.getAll();
        Optional<Libro> trovato = dopo.stream().filter(l -> l.getIsbn().equals("123456789")).findFirst();
        assertTrue(trovato.isPresent());
        assertEquals("Titolo 1 Aggiornato", trovato.get().getTitolo());

        Libro nonEsistente = new Libro("Titolo X", "Autore X", "000000000", Genere.FANTASCIENZA, 1, StatoLettura.DA_LEGGERE);
        assertFalse(dao.update(nonEsistente));
    }

}

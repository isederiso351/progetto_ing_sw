package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.filters.ValutazioneFilter;
import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
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
    private final Comparator<Libro> byIsbn = Comparator.comparing(Libro::getIsbn);
    private List<Libro> libri;

    static Stream<LibroDAO> provideDAOs() {
        return Stream.of(new JsonLibroDAO(JSON_PATH), new CachedLibroDAO(new JsonLibroDAO(CACHED_PATH)),
                new SqliteLibroDAO(SQLITE_URL));
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
    void saveAndGetAllTest(LibroDAO dao) throws DAOException {
        dao.saveAll(libri);
        List<Libro> scaricati = dao.getAll();
        scaricati.sort(byIsbn);
        assertEquals(libri, scaricati);
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void addAndGetByIsbnTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException {

        dao.saveAll(new ArrayList<>());

        Libro libro = new Libro("Titolo Test", "Autore Test", "111111111", Genere.THRILLER, 3, StatoLettura.DA_LEGGERE);

        dao.add(libro);

        Optional<Libro> found = dao.getByIsbn(libro.getIsbn());
        assertTrue(found.isPresent());
        assertEquals(libro, found.get());

        assertThrows(LibroAlreadyExistsException.class, () -> dao.add(libro));
    }


    @ParameterizedTest
    @MethodSource("provideDAOs")
    void removeByIsbnTest(LibroDAO dao) throws DAOException {
        dao.saveAll(libri);

        assertDoesNotThrow(() -> dao.removeByIsbn("123456789"));

        List<Libro> dopo = dao.getAll();
        assertEquals(1, dopo.size());
        assertEquals("987654321", dopo.getFirst().getIsbn());

        assertThrows(LibroNotFoundException.class, () -> dao.removeByIsbn("000000000"));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void updateTest(LibroDAO dao) throws DAOException {
        dao.saveAll(libri);

        Libro updated = new Libro("Titolo 1 Aggiornato", "Autore 1", "123456789", Genere.FANTASY, 5,
                StatoLettura.IN_LETTURA);
        assertDoesNotThrow(() -> dao.update(updated));

        Optional<Libro> found = dao.getByIsbn("123456789");
        assertTrue(found.isPresent());
        assertEquals("Titolo 1 Aggiornato", found.get().getTitolo());
        assertEquals(Genere.FANTASY, found.get().getGenere());
        assertEquals(StatoLettura.IN_LETTURA, found.get().getStatoLettura());

        Libro nonEsistente = new Libro("Titolo X", "Autore X", "000000000", Genere.FANTASCIENZA, 1, StatoLettura.DA_LEGGERE);
        assertThrows(LibroNotFoundException.class, () -> dao.update(nonEsistente));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void complexOperationSequenceTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException, LibroNotFoundException {
        dao.saveAll(new ArrayList<>());

        Libro libro1 = new Libro("Book 1", "Author 1", "1111111111", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        Libro libro2 = new Libro("Book 2", "Author 2", "2222222222", Genere.FANTASCIENZA, 5, StatoLettura.LETTO);

        dao.add(libro1);
        dao.add(libro2);

        assertEquals(2, dao.getAll().size());

        Libro libro1Updated = new Libro("Book 1 Updated", "Author 1", "1111111111", Genere.THRILLER, 5, StatoLettura.IN_LETTURA);
        dao.update(libro1Updated);

        Optional<Libro> found = dao.getByIsbn("1111111111");
        assertTrue(found.isPresent());
        assertEquals("Book 1 Updated", found.get().getTitolo());
        assertEquals(Genere.THRILLER, found.get().getGenere());

        dao.removeByIsbn("2222222222");
        assertEquals(1, dao.getAll().size());

        assertFalse(dao.getByIsbn("2222222222").isPresent());
    }

}

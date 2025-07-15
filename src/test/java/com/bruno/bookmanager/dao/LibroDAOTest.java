package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibroDAOTest {

    private static final String JSON_PATH = "libri_test.json";
    private static final String CACHED_PATH = "libri_test_cache.json";
    private static final String SQLITE_PATH = "libri_test.db";

    private final Comparator<Libro> byIsbn = Comparator.comparing(Libro::getIsbn);
    private List<Libro> libri;

    static Stream<LibroDAO> provideDAOs() {
        return Stream.of(new JsonLibroDAO(JSON_PATH), new CachedLibroDAO(new JsonLibroDAO(CACHED_PATH)),
                new SqliteLibroDAO("jdbc:sqlite:" + SQLITE_PATH));
    }

    @BeforeEach
    void setupLibri() {
        libri = new ArrayList<>();
        libri.add(new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO));
        libri.add(new Libro("Titolo 2", "Autore 2", "987654321", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA));
        libri.sort(byIsbn);
    }

    @AfterEach
    void cleanupAfterEach() throws DAOException {
        // Pulisci tutti i DAO dopo ogni test
        for (LibroDAO dao : provideDAOs().toList()) {
            try {
                dao.saveAll(new ArrayList<>());
            } catch (Exception e) {
                // Ignora errori di cleanup
            }
        }
    }

    @AfterAll
    void cleanup() {
        // Elimina i file di test
        deleteFileQuietly(JSON_PATH);
        deleteFileQuietly(CACHED_PATH);
        deleteFileQuietly(SQLITE_PATH);
    }

    private void deleteFileQuietly(String filename) {
        try {
            new File(filename).delete();
        } catch (Exception e) {
            // Ignora errori di eliminazione
        }
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void saveAndGetAllTest(LibroDAO dao) throws DAOException {
        dao.saveAll(libri);
        List<Libro> scaricati = dao.getAll();
        scaricati.sort(byIsbn);
        assertEquals(libri.size(), scaricati.size());
        assertEquals(libri, scaricati);
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void addAndGetByIsbnTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException {
        // Inizializza con una lista vuota
        dao.saveAll(new ArrayList<>());

        Libro libro = new Libro("Titolo Test", "Autore Test", "111111111", Genere.THRILLER, 3, StatoLettura.DA_LEGGERE);

        dao.add(libro);

        Optional<Libro> found = dao.getByIsbn(libro.getIsbn());
        assertTrue(found.isPresent());
        assertEquals(libro, found.get());

        // Test aggiunta libro duplicato
        assertThrows(LibroAlreadyExistsException.class, () -> dao.add(libro));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void getByIsbnNotFoundTest(LibroDAO dao) throws DAOException {
        dao.saveAll(new ArrayList<>());

        Optional<Libro> found = dao.getByIsbn("999999999");
        assertFalse(found.isPresent());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void removeByIsbnTest(LibroDAO dao) throws DAOException {
        dao.saveAll(libri);

        assertDoesNotThrow(() -> dao.removeByIsbn("123456789"));

        List<Libro> dopo = dao.getAll();
        assertEquals(1, dopo.size());
        assertEquals("987654321", dopo.get(0).getIsbn());

        // Test rimozione libro non esistente
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

        // Test aggiornamento libro non esistente
        Libro nonEsistente = new Libro("Titolo X", "Autore X", "000000000", Genere.FANTASCIENZA, 1,
                StatoLettura.DA_LEGGERE);
        assertThrows(LibroNotFoundException.class, () -> dao.update(nonEsistente));
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void complexOperationSequenceTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException, LibroNotFoundException {
        dao.saveAll(new ArrayList<>());

        Libro libro1 = new Libro("Book 1", "Author 1", "1111111111", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        Libro libro2 = new Libro("Book 2", "Author 2", "2222222222", Genere.FANTASCIENZA, 5, StatoLettura.LETTO);

        // Aggiungi due libri
        dao.add(libro1);
        dao.add(libro2);

        assertEquals(2, dao.getAll().size());

        // Aggiorna il primo libro
        Libro libro1Updated = new Libro("Book 1 Updated", "Author 1", "1111111111", Genere.THRILLER, 5,
                StatoLettura.IN_LETTURA);
        dao.update(libro1Updated);

        Optional<Libro> found = dao.getByIsbn("1111111111");
        assertTrue(found.isPresent());
        assertEquals("Book 1 Updated", found.get().getTitolo());
        assertEquals(Genere.THRILLER, found.get().getGenere());

        // Rimuovi il secondo libro
        dao.removeByIsbn("2222222222");
        assertEquals(1, dao.getAll().size());

        assertFalse(dao.getByIsbn("2222222222").isPresent());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void emptyDatabaseTest(LibroDAO dao) throws DAOException {
        dao.saveAll(new ArrayList<>());

        List<Libro> libri = dao.getAll();
        assertTrue(libri.isEmpty());

        Optional<Libro> libro = dao.getByIsbn("123456789");
        assertFalse(libro.isPresent());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void multipleUpdatesTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException, LibroNotFoundException {
        dao.saveAll(new ArrayList<>());

        Libro libro = new Libro("Original", "Author", "123456789", Genere.ROMANZO, 3, StatoLettura.DA_LEGGERE);
        dao.add(libro);

        // Prima modifica
        Libro updated1 = new Libro("Updated 1", "Author", "123456789", Genere.THRILLER, 4, StatoLettura.IN_LETTURA);
        dao.update(updated1);

        // Seconda modifica
        Libro updated2 = new Libro("Updated 2", "Author", "123456789", Genere.HORROR, 5, StatoLettura.LETTO);
        dao.update(updated2);

        Optional<Libro> found = dao.getByIsbn("123456789");
        assertTrue(found.isPresent());
        assertEquals("Updated 2", found.get().getTitolo());
        assertEquals(Genere.HORROR, found.get().getGenere());
        assertEquals(5, found.get().getValutazione());
        assertEquals(StatoLettura.LETTO, found.get().getStatoLettura());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void largeBatchOperationTest(LibroDAO dao) throws DAOException {
        List<Libro> largeBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeBatch.add(new Libro("Libro " + i, "Autore " + i, String.format("%010d", i),
                    Genere.values()[i % Genere.values().length], (i % 5) + 1,
                    StatoLettura.values()[i % StatoLettura.values().length]));
        }

        dao.saveAll(largeBatch);

        List<Libro> retrieved = dao.getAll();
        assertEquals(100, retrieved.size());

        // Verifica alcuni record specifici
        Optional<Libro> libro50 = dao.getByIsbn("0000000050");
        assertTrue(libro50.isPresent());
        assertEquals("Libro 50", libro50.get().getTitolo());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void specialCharactersTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException {
        dao.saveAll(new ArrayList<>());

        Libro libroSpeciale = new Libro("Título con àccènti e símb£los €", "Autòre with «special» chars", "123456789X",
                Genere.AUTOBIOGRAFIA, 4, StatoLettura.LETTO);

        dao.add(libroSpeciale);

        Optional<Libro> found = dao.getByIsbn("123456789X");
        assertTrue(found.isPresent());
        assertEquals("Título con àccènti e símb£los €", found.get().getTitolo());
        assertEquals("Autòre with «special» chars", found.get().getAutore());
    }

    @ParameterizedTest
    @MethodSource("provideDAOs")
    void nullAndEmptyValuesTest(LibroDAO dao) throws DAOException, LibroAlreadyExistsException {
        dao.saveAll(new ArrayList<>());

        // Libro con autore null
        Libro libroAutoreNull = new Libro("Titolo", null, "123456789", Genere.ROMANZO, 0, StatoLettura.DA_LEGGERE);
        dao.add(libroAutoreNull);

        Optional<Libro> found = dao.getByIsbn("123456789");
        assertTrue(found.isPresent());
        assertNull(found.get().getAutore());

        // Libro con autore vuoto
        Libro libroAutoreVuoto = new Libro("Titolo 2", "", "987654321", Genere.ROMANZO, 0, StatoLettura.DA_LEGGERE);
        dao.add(libroAutoreVuoto);

        Optional<Libro> found2 = dao.getByIsbn("987654321");
        assertTrue(found2.isPresent());
        assertEquals("", found2.get().getAutore());
    }
}
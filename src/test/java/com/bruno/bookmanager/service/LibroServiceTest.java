package com.bruno.bookmanager.service;

import com.bruno.bookmanager.dao.DAOType;
import com.bruno.bookmanager.dao.LibroDAO;
import com.bruno.bookmanager.dao.OptimizedSearch;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.exception.*;
import com.bruno.bookmanager.filters.SearchCriteria;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LibroServiceTest {

    @Mock
    private LibroDAO mockDAO;

    private LibroService service;
    private List<Libro> testBooks;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        service = LibroService.getInstance();
        service.setLibroDAO(mockDAO);

        // Setup test data
        testBooks = List.of(
                new Libro("1984", "George Orwell", "1234567890", Genere.DISTOPIA, 5, StatoLettura.LETTO),
                new Libro("Dune", "Frank Herbert", "0987654321", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA),
                new Libro("Neuromante", "William Gibson", "1111111111", Genere.FANTASCIENZA, 3, StatoLettura.DA_LEGGERE),
                new Libro("Il Signore degli Anelli", "J.R.R. Tolkien", "2222222222", Genere.FANTASY, 5, StatoLettura.LETTO),
                new Libro("Foundation", "Isaac Asimov", "3333333333", Genere.FANTASCIENZA, 0, StatoLettura.DA_LEGGERE)
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    // ============= VALIDATION TESTS =============

    @Test
    void validateLibroNullTest() {
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(null));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateTitoloNullTest() {
        Libro libro = new Libro(null, "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateTitoloVuotoTest() {
        Libro libro = new Libro("", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateTitoloBlankTest() {
        Libro libro = new Libro("   ", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateTitoloTroppoLungoTest() {
        String titoloLungo = "a".repeat(256); // 256 caratteri
        Libro libro = new Libro(titoloLungo, "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateAutoreNullValidTest() throws Exception {
        Libro libro = new Libro("Test Book", null, "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    @Test
    void validateAutoreVuotoValidTest() throws Exception {
        Libro libro = new Libro("Test Book", "", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    @Test
    void validateAutoreTroppoLungoTest() {
        String autoreLungo = "a".repeat(256); // 256 caratteri
        Libro libro = new Libro("Test Book", autoreLungo, "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateIsbnNullTest() {
        Libro libro = new Libro("Test Book", "Test Author", null, Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateIsbnFormatoInvalidoTest() {
        // ISBN troppo corto
        Libro libro1 = new Libro("Test Book 1", "Test Author", "123456789", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro1));

        // ISBN troppo lungo
        Libro libro2 = new Libro("Test Book 2", "Test Author", "12345678901234", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro2));

        // ISBN con caratteri invalidi (eccetto X per ISBN-10)
        Libro libro3 = new Libro("Test Book 3", "Test Author", "123456789A", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro3));

        // ISBN con caratteri speciali
        Libro libro4 = new Libro("Test Book 4", "Test Author", "1234-5678-90", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro4));

        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateIsbnValidoTest() throws Exception {
        // ISBN-10 valido con X
        Libro libro1 = new Libro("Test Book 1", "Test Author", "123456789X", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertDoesNotThrow(() -> service.aggiungiLibro(libro1));

        // ISBN-10 valido solo numeri
        Libro libro2 = new Libro("Test Book 2", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertDoesNotThrow(() -> service.aggiungiLibro(libro2));

        // ISBN-13 valido
        Libro libro3 = new Libro("Test Book 3", "Test Author", "1234567890123", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertDoesNotThrow(() -> service.aggiungiLibro(libro3));

        verify(mockDAO, times(3)).add(any(Libro.class));
    }

    @Test
    void validateValutazioneFuoriRangeTest() {
        Libro libro1 = new Libro("Test Book 1", "Test Author", "1234567890", Genere.ROMANZO, -1, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro1));

        Libro libro2 = new Libro("Test Book 2", "Test Author", "1234567891", Genere.ROMANZO, 6, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro2));

        Libro libro3 = new Libro("Test Book 3", "Test Author", "1234567892", Genere.ROMANZO, 10, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro3));

        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateValutazioneValidaTest() throws Exception {
        for (int i = 0; i <= 5; i++) {
            Libro libro = new Libro("Test Book " + i, "Test Author", "123456789" + i, Genere.ROMANZO, i, StatoLettura.DA_LEGGERE);
            assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        }
        verify(mockDAO, times(6)).add(any(Libro.class));
    }

    @Test
    void validateStatoLetturaNullTest() {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, null);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void validateGenereNullValidTest() throws Exception {
        // Il genere può essere null
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", null, 4, StatoLettura.DA_LEGGERE);

        assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    // ============= CRUD OPERATIONS TESTS =============

    @Test
    void aggiungiLibroSuccessTest() throws Exception {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        service.aggiungiLibro(libro);

        verify(mockDAO).add(libro);
    }

    @Test
    void aggiungiLibroAlreadyExistsTest() throws Exception {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        doThrow(new LibroAlreadyExistsException("1234567890")).when(mockDAO).add(libro);

        assertThrows(LibroAlreadyExistsException.class, () -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    @Test
    void aggiungiLibroDAOExceptionTest() throws Exception {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        doThrow(new DAOException("Database error")).when(mockDAO).add(libro);

        BookManagerException exception = assertThrows(BookManagerException.class, () -> service.aggiungiLibro(libro));
        assertEquals("Impossibile aggiungere il libro", exception.getMessage());
        assertInstanceOf(DAOException.class, exception.getCause());
        verify(mockDAO).add(libro);
    }

    @Test
    void rimuoviLibroSuccessTest() throws Exception {
        String isbn = "1234567890";

        service.rimuoviLibro(isbn);

        verify(mockDAO).removeByIsbn(isbn);
    }

    @Test
    void rimuoviLibroNotFoundTest() throws Exception {
        String isbn = "9999999999";
        doThrow(new LibroNotFoundException(isbn)).when(mockDAO).removeByIsbn(isbn);

        assertThrows(LibroNotFoundException.class, () -> service.rimuoviLibro(isbn));
        verify(mockDAO).removeByIsbn(isbn);
    }

    @Test
    void rimuoviLibroDAOExceptionTest() throws Exception {
        String isbn = "1234567890";
        doThrow(new DAOException("Database error")).when(mockDAO).removeByIsbn(isbn);

        BookManagerException exception = assertThrows(BookManagerException.class, () -> service.rimuoviLibro(isbn));
        assertEquals("Impossibile rimuovere il libro", exception.getMessage());
        assertInstanceOf(DAOException.class, exception.getCause());
        verify(mockDAO).removeByIsbn(isbn);
    }

    @Test
    void rimuoviLibroIsbnInvalidTest() {
        assertThrows(ValidationException.class, () -> service.rimuoviLibro(null));
        assertThrows(ValidationException.class, () -> service.rimuoviLibro(""));
        assertThrows(ValidationException.class, () -> service.rimuoviLibro("   "));
        assertThrows(ValidationException.class, () -> service.rimuoviLibro("123")); // ISBN troppo corto
        verifyNoInteractions(mockDAO);
    }

    @Test
    void aggiornaLibroSuccessTest() throws Exception {
        Libro libro = new Libro("Updated Book", "Test Author", "1234567890", Genere.ROMANZO, 5, StatoLettura.LETTO);

        service.aggiornaLibro(libro);

        verify(mockDAO).update(libro);
    }

    @Test
    void aggiornaLibroNotFoundTest() throws Exception {
        Libro libro = new Libro("Updated Book", "Test Author", "9999999999", Genere.ROMANZO, 5, StatoLettura.LETTO);
        doThrow(new LibroNotFoundException("9999999999")).when(mockDAO).update(libro);

        assertThrows(LibroNotFoundException.class, () -> service.aggiornaLibro(libro));
        verify(mockDAO).update(libro);
    }

    @Test
    void aggiornaLibroDAOExceptionTest() throws Exception {
        Libro libro = new Libro("Updated Book", "Test Author", "1234567890", Genere.ROMANZO, 5, StatoLettura.LETTO);
        doThrow(new DAOException("Database error")).when(mockDAO).update(libro);

        BookManagerException exception = assertThrows(BookManagerException.class, () -> service.aggiornaLibro(libro));
        assertEquals("Impossibile aggiornare il libro", exception.getMessage());
        assertInstanceOf(DAOException.class, exception.getCause());
        verify(mockDAO).update(libro);
    }

    @Test
    void aggiornaLibroValidationFailTest() {
        Libro libroInvalido = new Libro("", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiornaLibro(libroInvalido));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void trovaLibroPerIsbnSuccessTest() throws Exception {
        String isbn = "1234567890";
        Libro libro = testBooks.get(0);
        when(mockDAO.getByIsbn(isbn)).thenReturn(Optional.of(libro));

        Optional<Libro> result = service.trovaLibroPerIsbn(isbn);

        assertTrue(result.isPresent());
        assertEquals(libro, result.get());
        verify(mockDAO).getByIsbn(isbn);
    }

    @Test
    void trovaLibroPerIsbnNotFoundTest() throws Exception {
        String isbn = "9999999999";
        when(mockDAO.getByIsbn(isbn)).thenReturn(Optional.empty());

        Optional<Libro> result = service.trovaLibroPerIsbn(isbn);

        assertFalse(result.isPresent());
        verify(mockDAO).getByIsbn(isbn);
    }

    @Test
    void trovaLibroPerIsbnInvalidTest() {
        assertThrows(ValidationException.class, () -> service.trovaLibroPerIsbn(null));
        assertThrows(ValidationException.class, () -> service.trovaLibroPerIsbn(""));
        assertThrows(ValidationException.class, () -> service.trovaLibroPerIsbn("   "));
        assertThrows(ValidationException.class, () -> service.trovaLibroPerIsbn("123")); // ISBN troppo corto
        verifyNoInteractions(mockDAO);
    }

    @Test
    void getAllLibriTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.getAllLibri();

        assertEquals(testBooks, result);
        verify(mockDAO).getAll();
    }

    @Test
    void getAllLibriDAOExceptionTest() throws Exception {
        doThrow(new DAOException("Database error")).when(mockDAO).getAll();

        BookManagerException exception = assertThrows(BookManagerException.class, () -> service.getAllLibri());
        assertEquals("Impossibile recuperare i libri", exception.getMessage());
        assertInstanceOf(DAOException.class, exception.getCause());
        verify(mockDAO).getAll();
    }

    // ============= SEARCH AND FILTER TESTS =============

    @Test
    void searchWithOptimizedDAOTest() throws Exception {
        // Create a combined interface mock
        LibroDAO optimizedDAO = mock(LibroDAO.class, withSettings().extraInterfaces(OptimizedSearch.class));
        service.setLibroDAO(optimizedDAO);

        SearchCriteria criteria = SearchCriteria.byTitle("1984");
        when(((OptimizedSearch) optimizedDAO).search(criteria)).thenReturn(List.of(testBooks.get(0)));

        List<Libro> result = service.search(criteria);

        assertEquals(1, result.size());
        assertEquals("1984", result.get(0).getTitolo());
        verify((OptimizedSearch) optimizedDAO).search(criteria);
        assertTrue(service.supportsOptimizedSearch());
    }

    @Test
    void searchWithRegularDAOTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);
        SearchCriteria criteria = SearchCriteria.byTitle("1984");

        List<Libro> result = service.search(criteria);

        assertEquals(1, result.size());
        assertEquals("1984", result.get(0).getTitolo());
        verify(mockDAO).getAll();
        assertFalse(service.supportsOptimizedSearch());
    }

    @Test
    void cercaPerTitoloPartialMatchTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerTitolo("Signore");

        assertEquals(1, result.size());
        assertEquals("Il Signore degli Anelli", result.get(0).getTitolo());
    }

    @Test
    void cercaPerTitoloVuotoTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerTitolo("");

        assertEquals(testBooks.size(), result.size());
        verify(mockDAO).getAll();
    }

    @Test
    void cercaPerTitoloNullTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerTitolo(null);

        assertEquals(testBooks.size(), result.size());
        verify(mockDAO).getAll();
    }

    @Test
    void cercaPerTitoloNotFoundTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerTitolo("NonEsiste");

        assertTrue(result.isEmpty());
    }

    @Test
    void cercaPerAutorePartialMatchTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerAutore("George");

        assertEquals(1, result.size());
        assertEquals("George Orwell", result.get(0).getAutore());
    }

    @Test
    void cercaPerAutoreVuotoTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerAutore("");

        assertEquals(testBooks.size(), result.size());
        verify(mockDAO).getAll();
    }

    @Test
    void cercaPerAutoreNullTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerAutore(null);

        assertEquals(testBooks.size(), result.size());
        verify(mockDAO).getAll();
    }

    @Test
    void cercaPerIsbnTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerIsbn("1234");

        assertEquals(1, result.size());
        assertEquals("1234567890", result.get(0).getIsbn());
        verify(mockDAO).getAll();
    }

    @Test
    void cercaPerIsbnVuotoTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        List<Libro> result = service.cercaPerIsbn("");

        assertEquals(testBooks.size(), result.size());
        verify(mockDAO).getAll();
    }


    @Test
    void filtraLibriTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);
        GenereFilter filter = new GenereFilter(Genere.FANTASCIENZA);

        List<Libro> result = service.filtraLibri(filter);

        assertEquals(3, result.size()); // Dune, Neuromante, Foundation
        assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA));
        verify(mockDAO).getAll();
    }

    @Test
    void filtraLibriNessunRisultatoTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);
        GenereFilter filter = new GenereFilter(Genere.UMORISTICO);

        List<Libro> result = service.filtraLibri(filter);

        assertTrue(result.isEmpty());
        verify(mockDAO).getAll();
    }

    @Test
    void searchWithFilterAndSortingTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        SearchCriteria criteria = new SearchCriteria.Builder()
                .filter(new GenereFilter(Genere.FANTASCIENZA))
                .sortBy(SearchCriteria.SortField.TITOLO, true)
                .build();

        List<Libro> result = service.search(criteria);

        assertEquals(3, result.size()); // Dune, Foundation, Neuromante
        assertTrue(result.stream().allMatch(l -> l.getGenere() == Genere.FANTASCIENZA));
        // Verifica ordinamento crescente per titolo
        assertEquals("Dune", result.get(0).getTitolo());
        assertEquals("Foundation", result.get(1).getTitolo());
        assertEquals("Neuromante", result.get(2).getTitolo());
        verify(mockDAO).getAll();
    }

    @Test
    void searchWithComplexFilterTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        // Filtro: (FANTASCIENZA OR FANTASY) AND valutazione >= 4
        GenereFilter fantascienzaFilter = new GenereFilter(Genere.FANTASCIENZA);
        GenereFilter fantasyFilter = new GenereFilter(Genere.FANTASY);

        SearchCriteria criteria = new SearchCriteria.Builder()
                .filter(fantascienzaFilter.or(fantasyFilter))
                .sortBy(SearchCriteria.SortField.VALUTAZIONE, false)
                .build();

        List<Libro> result = service.search(criteria);

        assertEquals(4, result.size()); // Dune, Il Signore degli Anelli, Neuromante, Foundation
        assertTrue(result.stream().allMatch(l ->
                l.getGenere() == Genere.FANTASCIENZA || l.getGenere() == Genere.FANTASY));
        verify(mockDAO).getAll();
    }

    // ============= TEST CONFIGURAZIONE SERVICE =============

    @Test
    void setDAOWithTypeAndPathTest() {
        File testFile = new File("test_libri.json");

        assertDoesNotThrow(() -> service.setDAO(DAOType.JSON, testFile.getAbsolutePath()));
        assertDoesNotThrow(() -> service.getAllLibri());
    }

    @Test
    void setDAOWithInvalidPathTest() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setDAO(DAOType.JSON, null));

        assertThrows(IllegalArgumentException.class,
                () -> service.setDAO(DAOType.JSON, ""));

        assertThrows(IllegalArgumentException.class,
                () -> service.setDAO(DAOType.JSON, "   "));
    }


    @Test
    void multipleOperationsSequenceTest() throws Exception {
        Libro libro1 = new Libro("Book 1", "Author 1", "1111111111", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        Libro libro2 = new Libro("Book 2", "Author 2", "2222222222", Genere.FANTASCIENZA, 5, StatoLettura.LETTO);
        Libro libro1Updated = new Libro("Book 1 Updated", "Author 1", "1111111111", Genere.THRILLER, 5, StatoLettura.IN_LETTURA);

        when(mockDAO.getAll()).thenReturn(List.of(libro1, libro2));
        when(mockDAO.getByIsbn("1111111111")).thenReturn(Optional.of(libro1));

        // Aggiungi due libri
        service.aggiungiLibro(libro1);
        service.aggiungiLibro(libro2);


        // Trova un libro
        Optional<Libro> found = service.trovaLibroPerIsbn("1111111111");
        assertTrue(found.isPresent());
        assertEquals("Book 1", found.get().getTitolo());

        // Aggiorna il libro
        service.aggiornaLibro(libro1Updated);

        // Rimuovi un libro
        service.rimuoviLibro("2222222222");

        // Verifica tutte le chiamate
        verify(mockDAO, times(2)).add(any(Libro.class));
        verify(mockDAO).update(libro1Updated);
        verify(mockDAO).removeByIsbn("2222222222");
        verify(mockDAO).getByIsbn("1111111111");
        verify(mockDAO, atLeast(1)).getAll();
    }

    @Test
    void validationWithSpecialCharactersTest() throws Exception {
        Libro libro = new Libro("Libro con àccènti é symbols €$", "Autòre Spécial", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    @Test
    void edgeCaseEmptyAuthorTest() throws Exception {
        Libro libro = new Libro("Test Book", "", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertDoesNotThrow(() -> service.aggiungiLibro(libro));
        verify(mockDAO).add(libro);
    }

    @Test
    void concurrentAccessSimulationTest() throws Exception {
        // Simula accesso concorrente al singleton
        LibroService service1 = LibroService.getInstance();
        LibroService service2 = LibroService.getInstance();
        LibroService service3 = LibroService.getInstance();

        assertSame(service1, service2);
        assertSame(service2, service3);
        assertSame(service1, service3);

        // Tutti dovrebbero usare lo stesso DAO
        LibroDAO testDAO = mock(LibroDAO.class);
        service1.setLibroDAO(testDAO);

        when(testDAO.getAll()).thenReturn(testBooks);

        // Tutte le istanze dovrebbero vedere gli stessi dati
        assertEquals(service1.getAllLibri(), service2.getAllLibri());
        assertEquals(service2.getAllLibri(), service3.getAllLibri());
    }

}
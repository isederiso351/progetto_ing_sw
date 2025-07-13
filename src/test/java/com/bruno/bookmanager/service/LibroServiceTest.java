package com.bruno.bookmanager.service;

import com.bruno.bookmanager.dao.DAOType;
import com.bruno.bookmanager.dao.LibroDAO;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.exception.*;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LibroServiceTest {

    @Mock
    private LibroDAO mockDAO;

    private LibroService service;
    private List<Libro> testBooks;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service = LibroService.getInstance();
        service.setLibroDAO(mockDAO);

        // Setup test data
        testBooks = List.of(new Libro("1984", "George Orwell", "1234567890", Genere.DISTOPIA, 5, StatoLettura.LETTO),
                new Libro("Dune", "Frank Herbert", "0987654321", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA),
                new Libro("Neuromante", "William Gibson", "1111111111", Genere.FANTASCIENZA, 3,
                        StatoLettura.DA_LEGGERE));
    }

    @Test
    void validateTitoloVuotoTest() {
        Libro libro = new Libro("", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
    }

    @Test
    void validateTitoloTroppoLungoTest() {
        String titoloLungo = "a".repeat(256); // 256 caratteri
        Libro libro = new Libro(titoloLungo, "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
    }

    @Test
    void validateAutoreTroppoLungoTest() {
        String autoreLungo = "a".repeat(256); // 256 caratteri
        Libro libro = new Libro("Test Book", autoreLungo, "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
    }

    @Test
    void validateIsbnVuotoTest() {
        Libro libro = new Libro("Test Book", "Test Author", "", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
    }

    @Test
    void validateIsbnFormatoInvalidoTest() {
        // ISBN troppo corto
        Libro libro1 = new Libro("Test Book", "Test Author", "123456789", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro1));

        // ISBN troppo lungo
        Libro libro2 = new Libro("Test Book", "Test Author", "12345678901234", Genere.ROMANZO, 4,
                StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro2));

        // ISBN con caratteri invalidi
        Libro libro3 = new Libro("Test Book", "Test Author", "123456789A", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro3));
    }

    @Test
    void validateIsbnValidoTest() throws Exception {
        // ISBN-10 valido
        Libro libro1 = new Libro("Test Book 1", "Test Author", "123456789X", Genere.ROMANZO, 4,
                StatoLettura.DA_LEGGERE);
        assertDoesNotThrow(() -> service.aggiungiLibro(libro1));

        // ISBN-13 valido
        Libro libro2 = new Libro("Test Book 2", "Test Author", "1234567890123", Genere.ROMANZO, 4,
                StatoLettura.DA_LEGGERE);
        assertDoesNotThrow(() -> service.aggiungiLibro(libro2));
    }

    @Test
    void validateValutazioneFuoriRangeTest() {
        Libro libro1 = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, -1, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro1));

        Libro libro2 = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 6, StatoLettura.DA_LEGGERE);
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro2));
    }

    @Test
    void validateStatoLetturaNullTest() {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, null);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libro));
    }

    @Test
    void validateIsbnNullTest() {
        assertThrows(ValidationException.class, () -> service.trovaLibroPerIsbn(null));
        assertThrows(ValidationException.class, () -> service.rimuoviLibro(null));
    }

    @Test
    void daoExceptionWrappingTest() throws Exception {
        // Test che le DAOException vengano wrappate in BookManagerException
        when(mockDAO.getAll()).thenThrow(new DAOException("Database error"));

        BookManagerException exception = assertThrows(BookManagerException.class, () -> service.getAllLibri());
        assertInstanceOf(DAOException.class, exception.getCause());
    }

    @Test
    void multipleOperationsTest() throws Exception {
        Libro libro1 = new Libro("Book 1", "Author 1", "1111111111", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        Libro libro2 = new Libro("Book 2", "Author 2", "2222222222", Genere.FANTASCIENZA, 5, StatoLettura.LETTO);

        when(mockDAO.getAll()).thenReturn(List.of(libro1, libro2));

        service.aggiungiLibro(libro1);
        service.aggiungiLibro(libro2);

        int count = service.contaLibri();
        assertEquals(2, count);


        // Rimuovi un libro
        service.rimuoviLibro("1111111111");

        verify(mockDAO, times(2)).add(any(Libro.class));
        verify(mockDAO).removeByIsbn("1111111111");
    }

    @Test
    void singletonTest() {
        LibroService service1 = LibroService.getInstance();
        LibroService service2 = LibroService.getInstance();

        assertSame(service1, service2);
    }

    @Test
    void setLibroDAOTest() {
        LibroDAO newDAO = mock(LibroDAO.class);

        service.setLibroDAO(newDAO);

        //Controlliamo se viene effettivamente chiamato newDAO
        assertDoesNotThrow(() -> {
            when(newDAO.getAll()).thenReturn(new ArrayList<>());
            service.getAllLibri();
            verify(newDAO).getAll();
        });
    }

    @Test
    void aggiungiLibroValidationFailTest() {
        Libro libroInvalido = new Libro(null, "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);

        assertThrows(ValidationException.class, () -> service.aggiungiLibro(libroInvalido));
        verifyNoInteractions(mockDAO);
    }

    @Test
    void aggiungiLibroAlreadyExistsTest() throws Exception {
        Libro libro = new Libro("Test Book", "Test Author", "1234567890", Genere.ROMANZO, 4, StatoLettura.DA_LEGGERE);
        doThrow(new LibroAlreadyExistsException("1234567890")).when(mockDAO).add(libro);

        assertThrows(LibroAlreadyExistsException.class, () -> service.aggiungiLibro(libro));
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
    }

    @Test
    void aggiornaLibroSuccessTest() throws Exception {
        Libro libro = new Libro("Updated Book", "Test Author", "1234567890", Genere.ROMANZO, 5, StatoLettura.LETTO);

        service.aggiornaLibro(libro);

        verify(mockDAO).update(libro);
    }

    @Test
    void contaLibriTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        int count = service.contaLibri();

        assertEquals(3, count);
    }

    @Test
    void getValutazioneMediaTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(testBooks);

        double media = service.getValutazioneMedia();

        assertEquals(4.0, media, 0.01); // (5+4+3)/3 = 4.0
    }

    @Test
    void getValutazioneMediaLibriVuotiTest() throws Exception {
        when(mockDAO.getAll()).thenReturn(new ArrayList<>());

        double media = service.getValutazioneMedia();

        assertEquals(0.0, media);
    }

    @Test
    void validateLibroNullTest() {
        assertThrows(ValidationException.class, () -> service.aggiungiLibro(null));
    }

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
    }
}
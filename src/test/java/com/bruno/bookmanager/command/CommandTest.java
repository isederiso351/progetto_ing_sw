package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import com.bruno.bookmanager.service.DAOType;
import com.bruno.bookmanager.service.LibroService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandTest {

    @Mock
    private LibroService mockService;

    private Libro testLibro;
    private Libro updatedLibro;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        LibroService.getInstance().setDAO(DAOType.JSON, "libri_test.json");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testLibro = new Libro("1984", "George Orwell", "1234567890", Genere.DISTOPIA, 5, StatoLettura.LETTO);
        updatedLibro = new Libro("1984 Updated", "George Orwell", "1234567890", Genere.DISTOPIA, 4,
                StatoLettura.IN_LETTURA);
    }

    @Test
    void addLibroCommandExecuteTest() throws Exception {
        AddLibroCommand command = new AddLibroCommand(mockService, testLibro);

        command.execute();

        verify(mockService).aggiungiLibro(testLibro);
        assertEquals("Aggiunta libro: 1984 (ISBN: 1234567890)", command.getDescription());
        assertTrue(command.canUndo());
    }

    @Test
    void addLibroCommandUndoTest() throws Exception {
        AddLibroCommand command = new AddLibroCommand(mockService, testLibro);

        command.execute();
        command.undo();

        verify(mockService).aggiungiLibro(testLibro);
        verify(mockService).rimuoviLibro("1234567890");
    }

    @Test
    void addLibroCommandExecuteFailureTest() throws Exception {
        AddLibroCommand command = new AddLibroCommand(mockService, testLibro);
        doThrow(new LibroAlreadyExistsException("1234567890")).when(mockService).aggiungiLibro(testLibro);

        assertThrows(LibroAlreadyExistsException.class, command::execute);
        verify(mockService).aggiungiLibro(testLibro);
    }

    @Test
    void removeLibroCommandExecuteTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();

        verify(mockService).rimuoviLibro("1234567890");
        assertEquals("Rimozione libro: 1984 (ISBN: 1234567890)", command.getDescription());
    }

    @Test
    void removeLibroCommandUndoTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();
        command.undo();

        verify(mockService).rimuoviLibro("1234567890");
        verify(mockService).aggiungiLibro(testLibro);
    }

    @Test
    void removeLibroCommandUndoWithoutExecuteTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");

        assertThrows(BookManagerException.class, command::undo);
        verify(mockService, never()).aggiungiLibro(any());
    }

    @Test
    void updateLibroCommandExecuteTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();

        verify(mockService).aggiornaLibro(updatedLibro);
        assertEquals("Aggiornamento libro: 1984 Updated (ISBN: 1234567890)", command.getDescription());
    }

    @Test
    void updateLibroCommandUndoTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();
        command.undo();

        verify(mockService,atLeastOnce()).aggiornaLibro(updatedLibro);
    }

    @Test
    void updateLibroCommandExecuteBookNotFoundTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, command::execute);
        verify(mockService, never()).aggiornaLibro(any());
    }

}

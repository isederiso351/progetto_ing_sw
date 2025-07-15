package com.bruno.bookmanager.command;

import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import com.bruno.bookmanager.service.LibroService;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testLibro = new Libro("1984", "George Orwell", "1234567890", Genere.DISTOPIA, 5, StatoLettura.LETTO);
        updatedLibro = new Libro("1984 Updated", "George Orwell", "1234567890", Genere.DISTOPIA, 4,
                StatoLettura.IN_LETTURA);
    }

    // ============= ADD COMMAND TESTS =============

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
    void addLibroCommandUndoFailureTest() throws Exception {
        AddLibroCommand command = new AddLibroCommand(mockService, testLibro);
        doThrow(new LibroNotFoundException("1234567890")).when(mockService).rimuoviLibro("1234567890");

        command.execute();

        assertThrows(LibroNotFoundException.class, command::undo);
        verify(mockService).rimuoviLibro("1234567890");
    }

    // ============= REMOVE COMMAND TESTS =============

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
    void removeLibroCommandExecuteBookNotFoundTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, command::execute);
        verify(mockService, never()).rimuoviLibro(anyString());
    }

    @Test
    void removeLibroCommandUndoWithoutExecuteTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");

        assertThrows(BookManagerException.class, command::undo);
        verify(mockService, never()).aggiungiLibro(any());
    }

    @Test
    void removeLibroCommandExecuteFailureTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));
        doThrow(new BookManagerException("Remove failed")).when(mockService).rimuoviLibro("1234567890");

        assertThrows(BookManagerException.class, command::execute);
    }

    @Test
    void removeLibroCommandUndoFailureTest() throws Exception {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));
        doThrow(new LibroAlreadyExistsException("1234567890")).when(mockService).aggiungiLibro(testLibro);

        command.execute();

        assertThrows(LibroAlreadyExistsException.class, command::undo);
    }

    // ============= UPDATE COMMAND TESTS =============

    @Test
    void updateLibroCommandExecuteTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();

        verify(mockService).aggiornaLibro(updatedLibro);
        assertEquals("Aggiornamento libro: 1984 Updated (ISBN: 1234567890)", command.getDescription());
    }

    @Test
    void updateLibroCommandExecuteBookNotFoundTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, command::execute);
        verify(mockService, never()).aggiornaLibro(any());
    }

    @Test
    void updateLibroCommandUndoWithoutExecuteTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);

        assertThrows(BookManagerException.class, command::undo);
        verify(mockService, never()).aggiornaLibro(any());
    }

    @Test
    void updateLibroCommandExecuteFailureTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));
        doThrow(new BookManagerException("Update failed")).when(mockService).aggiornaLibro(updatedLibro);

        assertThrows(BookManagerException.class, command::execute);
    }

    @Test
    void updateLibroCommandUndoFailureTest() throws Exception {
        UpdateLibroCommand command = new UpdateLibroCommand(mockService, updatedLibro);
        when(mockService.trovaLibroPerIsbn("1234567890")).thenReturn(Optional.of(testLibro));

        command.execute();
        doThrow(new BookManagerException("Undo failed")).when(mockService).aggiornaLibro(testLibro);

        assertThrows(BookManagerException.class, command::undo);
    }

    // ============= EDGE CASES =============

    @Test
    void commandDescriptionTest() {
        AddLibroCommand addCommand = new AddLibroCommand(mockService, testLibro);
        RemoveLibroCommand removeCommand = new RemoveLibroCommand(mockService, "1234567890");
        UpdateLibroCommand updateCommand = new UpdateLibroCommand(mockService, updatedLibro);

        assertEquals("Aggiunta libro: 1984 (ISBN: 1234567890)", addCommand.getDescription());
        assertTrue(removeCommand.getDescription().contains("1234567890"));
        assertEquals("Aggiornamento libro: 1984 Updated (ISBN: 1234567890)", updateCommand.getDescription());
    }

    @Test
    void commandCanUndoTest() {
        AddLibroCommand addCommand = new AddLibroCommand(mockService, testLibro);
        RemoveLibroCommand removeCommand = new RemoveLibroCommand(mockService, "1234567890");
        UpdateLibroCommand updateCommand = new UpdateLibroCommand(mockService, updatedLibro);

        assertTrue(addCommand.canUndo());
        assertTrue(removeCommand.canUndo());
        assertTrue(updateCommand.canUndo());
    }

    @Test
    void removeLibroCommandDescriptionBeforeExecuteTest() {
        RemoveLibroCommand command = new RemoveLibroCommand(mockService, "1234567890");

        String description = command.getDescription();

        // Prima dell'execute, il titolo dovrebbe essere "sconosciuto"
        assertTrue(description.contains("sconosciuto"));
        assertTrue(description.contains("1234567890"));
    }
}

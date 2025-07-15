package com.bruno.bookmanager.command;


import com.bruno.bookmanager.exception.BookManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class CommandHistoryTest {

    private CommandHistory history;

    @Mock
    private Command mockCommand1;

    @Mock
    private Command mockCommand2;

    @Mock
    private Command mockCommand3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Ottieni l'istanza singleton e puliscila prima di ogni test
        history = CommandHistory.getInstance();
        history.clear();

        // Setup mock behaviors
        when(mockCommand1.canUndo()).thenReturn(true);
        when(mockCommand2.canUndo()).thenReturn(true);
        when(mockCommand3.canUndo()).thenReturn(true);

        when(mockCommand1.getDescription()).thenReturn("Command 1");
        when(mockCommand2.getDescription()).thenReturn("Command 2");
        when(mockCommand3.getDescription()).thenReturn("Command 3");
    }

    @Test
    void executeCommandTest() throws Exception {
        history.executeCommand(mockCommand1);

        verify(mockCommand1).execute();
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals("Command 1", history.getLastCommandDescription());
    }

    @Test
    void undoTest() throws Exception {
        history.executeCommand(mockCommand1);
        history.executeCommand(mockCommand2);

        history.undo();

        verify(mockCommand2).undo();
        assertTrue(history.canUndo()); // Ancora cmd1 da annullare
        assertTrue(history.canRedo()); // cmd2 può essere ripetuto
        assertEquals("Command 1", history.getLastCommandDescription());
        assertEquals("Command 2", history.getNextRedoCommandDescription());
    }

    @Test
    void redoTest() throws Exception {
        history.executeCommand(mockCommand1);
        history.undo();

        history.redo();

        verify(mockCommand1, times(2)).execute(); // Una volta in executeCommand, una in redo
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoEmptyHistoryTest() {
        assertThrows(BookManagerException.class, () -> history.undo());
        assertFalse(history.canUndo());
    }

    @Test
    void redoEmptyHistoryTest() {
        assertThrows(BookManagerException.class, () -> history.redo());
        assertFalse(history.canRedo());
    }

    @Test
    void executeCommandClearsRedoStackTest() throws Exception {
        history.executeCommand(mockCommand1);
        history.executeCommand(mockCommand2);
        history.undo(); // cmd2 va in redo stack

        assertTrue(history.canRedo());

        history.executeCommand(mockCommand3); // Dovrebbe cancellare redo stack

        assertFalse(history.canRedo());
        assertEquals("Command 3", history.getLastCommandDescription());
    }

    @Test
    void undoCommandThatCannotBeUndoneTest() throws Exception {
        when(mockCommand1.canUndo()).thenReturn(false);

        history.executeCommand(mockCommand1);

        assertThrows(BookManagerException.class, () -> history.undo());
    }

    @Test
    void multipleUndoRedoTest() throws Exception {
        history.executeCommand(mockCommand1);
        history.executeCommand(mockCommand2);
        history.executeCommand(mockCommand3);

        // Undo tutti
        history.undo(); // undo cmd3
        history.undo(); // undo cmd2
        history.undo(); // undo cmd1

        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        // Redo tutti
        history.redo(); // redo cmd1
        history.redo(); // redo cmd2
        history.redo(); // redo cmd3

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals("Command 3", history.getLastCommandDescription());
    }

    @Test
    void commandExecutionFailureTest() throws Exception {
        doThrow(new BookManagerException("Execution failed")).when(mockCommand1).execute();

        assertThrows(BookManagerException.class, () -> history.executeCommand(mockCommand1));

        // Verifica che il comando non sia stato aggiunto alla cronologia
        assertFalse(history.canUndo());
        assertNull(history.getLastCommandDescription());
    }

    @Test
    void maxHistorySizeTest() throws Exception {
        // Aggiungi più di 50 comandi (il limite massimo)
        for (int i = 0; i < 60; i++) {
            Command cmd = mock(Command.class);
            when(cmd.canUndo()).thenReturn(true);
            when(cmd.getDescription()).thenReturn("Command " + i);
            history.executeCommand(cmd);
        }

        // Dovrebbe esserci un limite nella cronologia
        int undoCount = 0;
        while (history.canUndo()) {
            history.undo();
            undoCount++;
        }

        // Non dovrebbe superare il limite massimo
        assertTrue(undoCount <= 50);
    }

    @Test
    void clearHistoryTest() throws Exception {
        history.executeCommand(mockCommand1);
        history.executeCommand(mockCommand2);
        history.undo();

        assertTrue(history.canUndo());
        assertTrue(history.canRedo());

        history.clear();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertNull(history.getLastCommandDescription());
        assertNull(history.getNextRedoCommandDescription());
    }
}

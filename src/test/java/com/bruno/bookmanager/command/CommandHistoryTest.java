package com.bruno.bookmanager.command;


import com.bruno.bookmanager.exception.BookManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class CommandHistoryTest {

    private CommandHistory history;
    private Command mockCommand1;
    private Command mockCommand2;
    private Command mockCommand3;

    @BeforeEach
    void setUp() {
        history = CommandHistory.getInstance(); // Limite di 3 comandi per test

        mockCommand1 = mock(Command.class);
        mockCommand2 = mock(Command.class);
        mockCommand3 = mock(Command.class);

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
    }
}

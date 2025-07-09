package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibroDAOTest {

    private static final String TEST_FILE_PATH = "libri_test.json";
    private LibroDAO libroDAO;
    private List<Libro> libri;

    @BeforeAll
    void setup() {
        libroDAO = new LibroDAO(TEST_FILE_PATH);
        libri = new ArrayList<>();
        libri.add(new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO));
        libri.add(new Libro("Titolo 2", "Autore 2", "987654321", Genere.FANTASCIENZA, 4, StatoLettura.IN_LETTURA));
    }

    @AfterAll
    void cleanup() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) file.delete();
    }

    @Test
    void testSalvaECercaLibri() {
        libroDAO.caricaLibri(libri);

        List<Libro> scaricati = libroDAO.scaricaLibri();

        assertEquals(2, scaricati.size());

        Libro primo = scaricati.get(0);
        assertEquals("Titolo 1", primo.getTitolo());
        assertEquals("Autore 1", primo.getAutore());
        assertEquals("123456789", primo.getIsbn());
        assertEquals(Genere.ROMANZO, primo.getGenere());
        assertEquals(5, primo.getValutazione());
        assertEquals(StatoLettura.LETTO, primo.getStatoLettura());
    }

    @Test
    void testEliminaLibro(){
        libroDAO.caricaLibri(libri);

        // Elimina libro esistente
        assertTrue(libroDAO.eliminaLibro("123456789"));

        List<Libro> libriDopo = libroDAO.scaricaLibri();
        assertEquals(1, libriDopo.size());
        assertEquals("987654321", libriDopo.get(0).getIsbn());

        // Elimina libro non esistente
        assertFalse(libroDAO.eliminaLibro("000000000"));

        // Verifica che la lista non sia cambiata
        List<Libro> libriFinale = libroDAO.scaricaLibri();
        assertEquals(libriFinale, libriDopo);
    }

    @Test
    void testAggiornaLibro(){
        libroDAO.caricaLibri(libri);
        Libro libroAggiornato = new Libro("Titolo1 Aggiornato", "Autore1", "123456789", Genere.FANTASY, 5, StatoLettura.IN_LETTURA);

        boolean risultato = libroDAO.aggiornaLibro(libroAggiornato);
        assertTrue(risultato);

        List<Libro> libriDopo = libroDAO.scaricaLibri();
        assertEquals(2, libriDopo.size());

        Libro trovato = libriDopo.stream()
                .filter(l -> l.equals(libroAggiornato))
                .findFirst()
                .orElse(null);
        assertNotNull(trovato);
        assertEquals("Titolo1 Aggiornato", trovato.getTitolo());

        // Aggiorna libro inesistente
        Libro libroNonEsistente = new Libro("TitoloX", "AutoreX", "000000000", Genere.FANTASCIENZA, 2, StatoLettura.DA_LEGGERE);
        boolean risultato2 = libroDAO.aggiornaLibro(libroNonEsistente);
        assertFalse(risultato2);
    }
}

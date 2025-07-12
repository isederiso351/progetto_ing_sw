package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.dao.filters.Filter;
import com.bruno.bookmanager.exception.DAOException;
import com.bruno.bookmanager.exception.LibroAlreadyExistsException;
import com.bruno.bookmanager.exception.LibroNotFoundException;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Implementazione di {@link LibroDAO} che utilizza un database SQLite per
 * la persistenza dei dati dei libri.
 */
public class SqliteLibroDAO implements LibroDAO {

    private static final Logger logger = LoggerFactory.getLogger(SqliteLibroDAO.class);

    private final String url;

    /**
     * Costruttore che inizializza la connessione e crea la tabella se assente.
     *
     * @param url URL di connessione JDBC al database SQLite
     */
    public SqliteLibroDAO(String url) {
        this.url = url;
        try {
            initialize();
        } catch (DAOException e) {
            logger.error("Impossibile inizializzare il database SQLite", e);
            throw new RuntimeException("Inizializzazione database fallita", e);
        }
    }

    private void initialize() throws DAOException {
        String sql = """
                CREATE TABLE IF NOT EXISTS libri (
                                isbn TEXT PRIMARY KEY,
                                titolo TEXT NOT NULL,
                                autore TEXT,
                                genere TEXT,
                                valutazione INTEGER CHECK(valutazione >= 0 AND valutazione <= 5),
                                stato TEXT NOT NULL,
                            );
                """;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            logger.info("Database SQLite inizializzato correttamente: {}", url);

        } catch (SQLException e) {
            logger.error("Errore durante l'inizializzazione del database SQLite", e);
            throw new DAOException("Errore durante l'inizializzazione del database", e);
        }
    }

    private Libro mapRowToLibro(ResultSet rs) throws SQLException {
        return new Libro(rs.getString("titolo"), rs.getString("autore"), rs.getString("isbn"),
                Genere.fromString(rs.getString("genere")), rs.getInt("valutazione"),
                StatoLettura.valueOf(rs.getString("stato")));
    }

    private void setLibroParameters(PreparedStatement stmt, Libro libro) throws SQLException {
        stmt.setString(1, libro.getIsbn());
        stmt.setString(2, libro.getTitolo());
        stmt.setString(3, libro.getAutore());
        stmt.setString(4, libro.getGenereName());
        stmt.setInt(5, libro.getValutazione());
        stmt.setString(6, libro.getStatoLettura().name());
    }

    @Override
    public List<Libro> getAll() throws DAOException {
        String sql = "SELECT * FROM libri";
        List<Libro> libri = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                libri.add(mapRowToLibro(rs));
            }

            logger.debug("Caricati {} libri dal database", libri.size());
            return libri;
        } catch (SQLException e) {
            logger.error("Errore durante il caricamento dei libri", e);
            throw new DAOException("Impossibile caricare i libri dal database", e);
        }
    }

    @Override
    public void saveAll(List<Libro> libri) throws DAOException {
        String deleteSql = "DELETE FROM libri";
        String insertSql = """
                INSERT INTO libri (isbn, titolo, autore, genere, valutazione, stato)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try {
                //Cancella libri esistenti
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(deleteSql);
                }

                // Inserisci tutti i nuovi libri
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    for (Libro libro : libri) {
                        setLibroParameters(stmt, libro);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
                logger.debug("Salvati {} libri nel database: {}", libri.size(), url);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dei libri", e);
            throw new DAOException("Impossibile salvare i libri nel database", e);
        }
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) throws DAOException {
        String sql = "SELECT * FROM libri WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Libro libro = mapRowToLibro(rs);
                    logger.debug("Trovato libro con ISBN {}: {}", isbn, libro.getTitolo());
                    return Optional.of(libro);
                } else {
                    logger.debug("Nessun libro trovato con ISBN {}", isbn);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca del libro con ISBN {}", isbn, e);
            throw new DAOException("Impossibile cercare il libro con ISBN " + isbn, e);
        }
    }

    @Override
    public void add(Libro libro) throws LibroAlreadyExistsException, DAOException {
        String sql = """
                INSERT INTO libri (isbn, titolo, autore, genere, valutazione, stato)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setLibroParameters(stmt, libro);
            stmt.executeUpdate();

            logger.info("Aggiunto libro: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());

        } catch (SQLException e) {
            logger.error("Errore durante l'aggiunta del libro con ISBN {}", libro.getIsbn(), e);
            throw new DAOException("Impossibile aggiungere il libro", e);
        }
    }

    @Override
    public void removeByIsbn(String isbn) throws LibroNotFoundException, DAOException {
        String sql = "DELETE FROM libri WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            int deleted = stmt.executeUpdate();

            if (deleted == 0) {
                logger.warn("Tentativo di rimozione libro non esistente con ISBN {}", isbn);
                throw new LibroNotFoundException(isbn);
            }

            logger.info("Rimosso libro con ISBN {}", isbn);

        } catch (SQLException e) {
            logger.error("Errore durante la rimozione del libro con ISBN {}", isbn, e);
            throw new DAOException("Impossibile rimuovere il libro con ISBN " + isbn, e);
        }
    }

    @Override
    public void update(Libro libro) throws LibroNotFoundException, DAOException {
        String sql = """
                UPDATE libri SET titolo=?, autore=?, genere=?, valutazione=?, stato=?
                WHERE isbn=?
                """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitolo());
            stmt.setString(2, libro.getAutore());
            stmt.setString(3, libro.getGenereName());
            stmt.setInt(4, libro.getValutazione());
            stmt.setString(5, libro.getStatoLettura().name());
            stmt.setString(6, libro.getIsbn());

            int updated = stmt.executeUpdate();

            if (updated == 0) {
                logger.warn("Tentativo di aggiornamento libro non esistente con ISBN {}", libro.getIsbn());
                throw new LibroNotFoundException(libro.getIsbn());
            }


            logger.info("Aggiornato libro: {} (ISBN: {})", libro.getTitolo(), libro.getIsbn());
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del libro con ISBN {}", libro.getIsbn(), e);
            throw new DAOException("Impossibile aggiornare il libro", e);
        }
    }

    @Override
    public List<Libro> getByFilter(Filter<Libro> filter) throws DAOException {
        String sql = "SELECT * FROM libri WHERE " + filter.toSqlClause();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Libro> libri = new ArrayList<>();
            while (rs.next()) {
                libri.add(mapRowToLibro(rs));
            }

            logger.debug("Filtro SQL applicato: trovati {} libri", libri.size());
            return libri;
        } catch (SQLException e) {
            logger.error("Errore durante l'applicazione del filtro SQL", e);
            throw new DAOException("Impossibile applicare il filtro", e);
        }
    }
}

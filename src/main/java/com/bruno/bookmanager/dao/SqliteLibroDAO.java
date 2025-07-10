package com.bruno.bookmanager.dao;

import com.bruno.bookmanager.dao.filters.Filter;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Implementazione di {@link LibroDAO} che utilizza un database SQLite per
 * la persistenza dei dati dei libri.
 */
public class SqliteLibroDAO implements LibroDAO {

    private final String url;

    /**
     * Costruttore che inizializza la connessione e crea la tabella se assente.
     *
     * @param url URL di connessione JDBC al database SQLite
     */
    public SqliteLibroDAO(String url) {
        this.url = url;
        initialize();
    }

    private void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS libri (
                                isbn TEXT PRIMARY KEY,
                                titolo TEXT,
                                autore TEXT,
                                genere TEXT,
                                valutazione INTEGER,
                                stato TEXT
                            );
                """;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inizializzazione del DB", e);
        }
    }

    private Libro mapRowToLibro(ResultSet rs) throws SQLException {
        return new Libro(rs.getString("titolo"), rs.getString("autore"), rs.getString("isbn"), Genere.valueOf(rs.getString("genere")), rs.getInt("valutazione"), StatoLettura.valueOf(rs.getString("stato")));
    }

    private void setLibroParameters(PreparedStatement stmt, Libro libro) throws SQLException {
        stmt.setString(1, libro.getIsbn());
        stmt.setString(2, libro.getTitolo());
        stmt.setString(3, libro.getAutore());
        stmt.setString(4, libro.getGenere().name());
        stmt.setInt(5, libro.getValutazione());
        stmt.setString(6, libro.getStatoLettura().name());
    }

    @Override
    public List<Libro> getAll() {
        String sql = "SELECT * FROM libri";
        List<Libro> libri = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                libri.add(mapRowToLibro(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return libri;
    }

    @Override
    public void saveAll(List<Libro> libri) {
        String deleteSql = "DELETE FROM libri";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(deleteSql);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione", e);
        }
        for (Libro libro : libri) {
            add(libro);
        }
    }

    @Override
    public Optional<Libro> getByIsbn(String isbn) {
        String sql = "SELECT * FROM libri WHERE isbn = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void add(Libro libro) {
        String sql = """
                INSERT INTO libri (isbn, titolo, autore, genere, valutazione, stato)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setLibroParameters(stmt, libro);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiunta del libro", e);
        }
    }

    @Override
    public boolean removeByIsbn(String isbn) {
        String sql = "DELETE FROM libri WHERE isbn = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella rimozione del libro", e);
        }
    }

    @Override
    public boolean update(Libro libro) {
        String sql = """
                UPDATE libri SET titolo=?, autore=?, genere=?, valutazione=?, stato=?
                WHERE isbn=?
                """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, libro.getTitolo());
            stmt.setString(2, libro.getAutore());
            stmt.setString(3, libro.getGenere().name());
            stmt.setInt(4, libro.getValutazione());
            stmt.setString(5, libro.getStatoLettura().name());
            stmt.setString(6, libro.getIsbn());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento del libro", e);
        }
    }

    @Override
    public List<Libro> getByFilter(Filter<Libro> filter) {
        String sql = "SELECT * FROM libri WHERE " + filter.toSqlClause();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Libro> libri = new ArrayList<>();
            while (rs.next()) {
                libri.add(mapRowToLibro(rs));
            }

            return libri;
        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
    }
}

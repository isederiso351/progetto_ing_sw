package com.bruno.bookmanager.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory per la creazione di istanze LibroDAO.
 * Nasconde i dettagli di implementazione e le dipendenze specifiche dei DAO
 * dal resto dell'applicazione.
 */
public class DAOFactory {

    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

    /**
     * Crea un'istanza di LibroDAO del tipo specificato.
     *
     * @param type tipo di DAO da creare
     * @param path percorso del file o database (per JSON sarà il file .json,
     *             per SQLite sarà il nome del file .db)
     * @return istanza del DAO richiesto
     * @throws IllegalArgumentException se il tipo non è supportato
     */
    public static LibroDAO createDAO(DAOType type, String path) {

        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso non può essere null o vuoto");
        }
        LibroDAO dao = switch (type) {
            case SQLITE -> new SqliteLibroDAO("jdbc:sqlite:" + path);
            case JSON -> new JsonLibroDAO(path);
            case CACHED_JSON -> new CachedLibroDAO(new JsonLibroDAO(path));
        };
        logger.debug("Creato DAO di tipo {} con successo", type);
        return dao;
    }
}

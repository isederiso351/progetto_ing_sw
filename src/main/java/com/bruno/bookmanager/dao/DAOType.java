package com.bruno.bookmanager.dao;

/**
 * Enum che definisce i tipi di DAO disponibili per la persistenza dei libri.
 */
public enum DAOType {
    /**
     * DAO basato su file JSON.
     */
    JSON,

    /**
     * DAO basato su database SQLite.
     */
    SQLITE,

    /**
     * DAO con cache in memoria sopra un DAO JSON.
     */
    CACHED_JSON
}
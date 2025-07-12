package com.bruno.bookmanager.model;

public enum Genere {
    BIOGRAFIA, AUTOBIOGRAFIA, ROMANZO, ROMANZO_STORICO, GIALLO, THRILLER, AZIONE, FANTASCIENZA, DISTOPIA, FANTASY, HORROR, ROSA, UMORISTICO;

    public static Genere fromString(String s) {
        if (s == null) return null;
        try {
            return Genere.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

package com.bruno.bookmanager.model;

public class Libro {
    private String titolo;
    private String autore;
    private String isbn;
    private Genere genere;
    private int valutazione; // da 1 a 5
    private StatoLettura stato;

    public Libro() {
    }

    public Libro(String titolo, String autore, String isbn, Genere genere) {
        this.titolo = titolo;
        this.autore = autore;
        this.isbn = isbn;
        this.genere = genere;
    }

    public Libro(String titolo, String autore, String isbn, Genere genere, int valutazione, StatoLettura stato) {
        this.titolo = titolo;
        this.autore = autore;
        this.isbn = isbn;
        this.genere = genere;
        this.valutazione = valutazione;
        this.stato = stato;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getAutore() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Genere getGenere() {
        return genere;
    }

    public void setGenere(Genere genere) {
        this.genere = genere;
    }

    public int getValutazione() {
        return valutazione;
    }

    public void setValutazione(int valutazione) {
        this.valutazione = valutazione;
    }

    public StatoLettura getStato() {
        return stato;
    }

    public void setStato(StatoLettura stato) {
        this.stato = stato;
    }
}

package com.bruno.bookmanager.filters;


import com.bruno.bookmanager.model.Libro;

public class ISBNFilter implements Filter<Libro> {
    private final String isbn;

    public ISBNFilter(String isbn){
        this.isbn = isbn != null ? isbn.trim() : "";
    }

    @Override
    public boolean test(Libro libro) {
        return libro.getIsbn().contains(isbn);
    }

    @Override
    public String toSqlClause() {
        return "isbn LIKE '%" + isbn + "%'";
    }
}

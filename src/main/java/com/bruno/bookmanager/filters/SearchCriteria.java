package com.bruno.bookmanager.filters;

import com.bruno.bookmanager.model.Libro;

public class SearchCriteria {

    private final Filter<Libro> filter;

    private final SortField sortField;
    private final boolean sortAsc;

    private SearchCriteria(Builder builder) {
        this.filter = builder.filter;
        this.sortField = builder.sortField;
        this.sortAsc = builder.sortAsc;
    }

    // Static factory methods per casi comuni
    public static SearchCriteria all() {
        return new Builder().build();
    }

    public static SearchCriteria byTitle(String title) {
        return new Builder().filter(new TitoloFilter(title)).build();
    }

    public static SearchCriteria byAuthor(String author) {
        return new Builder().filter(new AutoreFilter(author)).build();
    }

    public static SearchCriteria byFilter(Filter<Libro> filter) {
        return new Builder().filter(filter).build();
    }

    //Helper

    public Filter<Libro> getFilter() {
        return filter;
    }

    public SortField getSortField() {
        return sortField;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public boolean hasFilter() {
        return filter != null;
    }

    public boolean hasSorting() {
        return sortField != null;
    }

    public enum SortField {
        TITOLO, AUTORE, VALUTAZIONE, GENERE, STATO, ISBN
    }

    public static class Builder {
        private Filter<Libro> filter;
        private SortField sortField = SortField.TITOLO;
        private boolean sortAsc = true;

        public Builder filter(Filter<Libro> filter) {
            this.filter = filter;
            return this;
        }

        public Builder sortBy(SortField field) {
            return sortBy(field, true);
        }

        public Builder sortBy(SortField field, boolean sortAsc) {
            this.sortField = field;
            this.sortAsc = sortAsc;
            return this;
        }

        public Builder sortByTitle(boolean ascending) {
            return sortBy(SortField.TITOLO, ascending);
        }

        public Builder sortByAuthor(boolean ascending) {
            return sortBy(SortField.AUTORE, ascending);
        }

        public Builder sortByRating(boolean ascending) {
            return sortBy(SortField.VALUTAZIONE, ascending);
        }

        public SearchCriteria build() {
            return new SearchCriteria(this);
        }
    }
}

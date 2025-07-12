package com.bruno.bookmanager.filters;

public interface Filter<T> {
    boolean test(T item);
    String toSqlClause();

    default Filter<T> and(Filter<T> f) {
        return new AndFilter<>(this, f);
    }

    default Filter<T> or(Filter<T> f) {
        return new OrFilter<>(this,f);
    }
}

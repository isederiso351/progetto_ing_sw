package com.bruno.bookmanager.dao.filters;

public class OrFilter<T> implements Filter<T>{

    private final Filter<T> left, right;

    public OrFilter(Filter<T> left, Filter<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean test(T item) {
        return left.test(item) || right.test(item);
    }

    @Override
    public String toSqlClause() {
        return "(" + left.toSqlClause() + " OR " + right.toSqlClause() + ")";
    }
}

package com.bruno.bookmanager.dao.filters;

public class AndFilter<T> implements Filter<T>{

    private final Filter<T> left, right;

    public AndFilter(Filter<T> left, Filter<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean test(T item) {
        return left.test(item) && right.test(item);
    }

    @Override
    public String toSqlClause() {
        return "(" + left.toSqlClause() + " AND " + right.toSqlClause() + ")";
    }
}

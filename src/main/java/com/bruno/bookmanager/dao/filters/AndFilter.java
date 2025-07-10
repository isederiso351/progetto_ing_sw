package com.bruno.bookmanager.dao.filters;

public class AndFilter<T> extends CompositeFilter<T>{

    private final Filter<T> left, right;

    public AndFilter(Filter<T> left, Filter<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected String getOperator() {
        return "AND";
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

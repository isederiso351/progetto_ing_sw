package com.bruno.bookmanager.dao.filters;

public class OrFilter<T> extends CompositeFilter<T>{

    private final Filter<T> left, right;

    public OrFilter(Filter<T> left, Filter<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected String getOperator() {
        return "OR";
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

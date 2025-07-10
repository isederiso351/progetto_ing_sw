package com.bruno.bookmanager.dao.filters;

public abstract class CompositeFilter<T> implements Filter<T> {

    protected abstract String getOperator();

    @Override
    public String toSqlClause() {
        throw new UnsupportedOperationException("SQL generation not supported for this filter");
    }
}

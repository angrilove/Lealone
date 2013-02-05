/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.yourbase.expression;

import java.util.ArrayList;

import com.codefollower.yourbase.command.dml.Query;
import com.codefollower.yourbase.constant.ErrorCode;
import com.codefollower.yourbase.dbobject.table.ColumnResolver;
import com.codefollower.yourbase.dbobject.table.TableFilter;
import com.codefollower.yourbase.engine.Session;
import com.codefollower.yourbase.message.DbException;
import com.codefollower.yourbase.result.ResultInterface;
import com.codefollower.yourbase.value.Value;
import com.codefollower.yourbase.value.ValueArray;
import com.codefollower.yourbase.value.ValueNull;

/**
 * A query returning a single value.
 * Subqueries are used inside other statements.
 */
public class Subquery extends Expression {

    private final Query query;
    private Expression expression;

    public Subquery(Query query) {
        this.query = query;
    }

    public Value getValue(Session session) {
        query.setSession(session);
        ResultInterface result = session.createSubqueryResult(query, 2); //query.query(2);
        try {
            int rowcount = result.getRowCount();
            if (rowcount > 1) {
                throw DbException.get(ErrorCode.SCALAR_SUBQUERY_CONTAINS_MORE_THAN_ONE_ROW);
            }
            Value v;
            if (rowcount <= 0) {
                v = ValueNull.INSTANCE;
            } else {
                result.next();
                Value[] values = result.currentRow();
                if (result.getVisibleColumnCount() == 1) {
                    v = values[0];
                } else {
                    v = ValueArray.get(values);
                }
            }
            return v;
        } finally {
            result.close();
        }
    }

    public int getType() {
        return getExpression().getType();
    }

    public void mapColumns(ColumnResolver resolver, int level) {
        query.mapColumns(resolver, level + 1);
    }

    public Expression optimize(Session session) {
        query.prepare();
        return this;
    }

    public void setEvaluatable(TableFilter tableFilter, boolean b) {
        query.setEvaluatable(tableFilter, b);
    }

    public int getScale() {
        return getExpression().getScale();
    }

    public long getPrecision() {
        return getExpression().getPrecision();
    }

    public int getDisplaySize() {
        return getExpression().getDisplaySize();
    }

    public String getSQL(boolean isDistributed) {
        return "(" + query.getPlanSQL() + ")";
    }

    public void updateAggregate(Session session) {
        query.updateAggregate(session);
    }

    private Expression getExpression() {
        if (expression == null) {
            ArrayList<Expression> expressions = query.getExpressions();
            int columnCount = query.getColumnCount();
            if (columnCount == 1) {
                expression = expressions.get(0);
            } else {
                Expression[] list = new Expression[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    list[i] = expressions.get(i);
                }
                expression = new ExpressionList(list);
            }
        }
        return expression;
    }

    public boolean isEverything(ExpressionVisitor visitor) {
        return query.isEverything(visitor);
    }

    public Query getQuery() {
        return query;
    }

    public int getCost() {
        return query.getCostAsExpression();
    }

    public Expression[] getExpressionColumns(Session session) {
        return getExpression().getExpressionColumns(session);
    }
}
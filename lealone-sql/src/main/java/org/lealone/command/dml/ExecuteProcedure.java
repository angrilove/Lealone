/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.lealone.command.dml;

import java.util.ArrayList;

import org.lealone.command.CommandInterface;
import org.lealone.command.Prepared;
import org.lealone.dbobject.Procedure;
import org.lealone.engine.Session;
import org.lealone.expression.Expression;
import org.lealone.expression.Parameter;
import org.lealone.result.ResultInterface;
import org.lealone.util.New;

/**
 * This class represents the statement
 * EXECUTE
 */
public class ExecuteProcedure extends Prepared {

    private final ArrayList<Expression> expressions = New.arrayList();
    private Procedure procedure;

    public ExecuteProcedure(Session session) {
        super(session);
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    /**
     * Set the expression at the given index.
     *
     * @param index the index (0 based)
     * @param expr the expression
     */
    public void setExpression(int index, Expression expr) {
        expressions.add(index, expr);
    }

    private void setParameters() {
        Prepared prepared = procedure.getPrepared();
        ArrayList<Parameter> params = prepared.getParameters();
        for (int i = 0; params != null && i < params.size() && i < expressions.size(); i++) {
            Expression expr = expressions.get(i);
            Parameter p = params.get(i);
            p.setValue(expr.getValue(session));
        }
    }

    @Override
    public boolean isQuery() {
        Prepared prepared = procedure.getPrepared();
        return prepared.isQuery();
    }

    @Override
    public int update() {
        setParameters();
        Prepared prepared = procedure.getPrepared();
        return prepared.update();
    }

    @Override
    public ResultInterface query(int limit) {
        setParameters();
        Prepared prepared = procedure.getPrepared();
        return prepared.query(limit);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public ResultInterface queryMeta() {
        Prepared prepared = procedure.getPrepared();
        return prepared.queryMeta();
    }

    @Override
    public int getType() {
        return CommandInterface.EXECUTE;
    }

}

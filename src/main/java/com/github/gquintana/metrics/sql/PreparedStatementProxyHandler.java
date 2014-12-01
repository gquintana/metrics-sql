package com.github.gquintana.metrics.sql;

/*
 * #%L
 * Metrics SQL
 * %%
 * Copyright (C) 2014 Open-Source
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.MethodInvocation;
import java.sql.PreparedStatement;

/**
 * JDBC proxy handler for {@link PreparedStatement}
 */
public class PreparedStatementProxyHandler extends AbstractStatementProxyHandler<PreparedStatement> {

    private final String sql;
    private final String sqlId;

    public PreparedStatementProxyHandler(PreparedStatement delegate, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext, String sql, String sqlId) {
        super(delegate, PreparedStatement.class, name, proxyFactory, lifeTimerContext);
        this.sql = sql;
        this.sqlId = sqlId;
    }

    protected final Object execute(MethodInvocation<PreparedStatement> methodInvocation) throws Throwable {
        final String lSql;
        final String lSqlId;
        if (methodInvocation.getArgCount() > 0) {
            lSql = methodInvocation.getArgAt(0, String.class);
            lSqlId = null;
        } else {
            lSql = this.sql;
            lSqlId = this.sqlId;
        }
        StatementTimerContext timerContext = proxyFactory.startPreparedStatementExecuteTimer(name, lSql, lSqlId);
        Object result = methodInvocation.proceed();
        result = stopTimer(timerContext, result);
        return result;
    }

}

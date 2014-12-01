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
import java.sql.Statement;

/**
 * JDBC Proxy handler for {@link Statement}
 */
public class StatementProxyHandler extends AbstractStatementProxyHandler<Statement> {

    public StatementProxyHandler(Statement delegate, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, Statement.class, name, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object execute(MethodInvocation<Statement> methodInvocation) throws Throwable {
        Object result;
        if (methodInvocation.getArgCount() > 0) {
            final String sql = methodInvocation.getArgAt(0, String.class);
            final StatementTimerContext timerContext = proxyFactory.startStatementExecuteTimer(name, sql);
            result = methodInvocation.proceed();
            result = stopTimer(timerContext, result);
        } else {
            result = methodInvocation.proceed();
        }
        return result;
    }

}

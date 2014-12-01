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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Base JDBC proxy handler for Statements
 * @param <T> Statement type
 */
public abstract class AbstractStatementProxyHandler<T extends Statement> extends JdbcProxyHandler<T> {

    public AbstractStatementProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, delegateType, name, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object invoke(MethodInvocation<T> delegatingMethodInvocation) throws Throwable {
        final String methodName = delegatingMethodInvocation.getMethodName();
        Object result;
        if (methodName.equals("isWrapperFor")) {
            result = isWrapperFor(delegatingMethodInvocation);
        } else if (methodName.equals("unwrap")) {
            result = unwrap(delegatingMethodInvocation);
        } else if (methodName.equals("close")) {
            result = close(delegatingMethodInvocation);
        } else if (methodName.equals("execute") || methodName.equals("executeQuery") || methodName.equals("executeUpdate")) {
            result = execute(delegatingMethodInvocation);
        } else {
            result = delegatingMethodInvocation.proceed();
        }
        return result;
    }

    protected abstract Object execute(MethodInvocation<T> delegatingMethodInvocation) throws Throwable;

    private static final InvocationFilter THIS_INVOCATION_FILTER = new MethodNamesInvocationFilter("isWrapperFor", "unwrap", "close", "execute", "executeQuery", "executeUpdate");

    @Override
    public InvocationFilter getInvocationFilter() {
        return THIS_INVOCATION_FILTER;
    }
    protected Object stopTimer(StatementTimerContext timerContext, Object result) {
        if (timerContext!=null) {
            stopTimer(timerContext.getTimerContext());
            if (result instanceof ResultSet) {
                result = proxyFactory.wrapResultSet(name, (ResultSet) result, timerContext.getSql(), timerContext.getSqlId());
            }
        }
        return result;
    }
}

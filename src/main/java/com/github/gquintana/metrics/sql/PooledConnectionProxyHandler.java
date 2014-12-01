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
import javax.sql.PooledConnection;
import java.sql.Connection;

/**
 * JDBC proxy handler for {@link PooledConnection} and its subclasses.
 */
public class PooledConnectionProxyHandler<T extends PooledConnection> extends JdbcProxyHandler<T> {

    public PooledConnectionProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, delegateType, name, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object invoke(MethodInvocation<T> methodInvocation) throws Throwable {
        final String methodName = methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getConnection")) {
            result = getConnection(methodInvocation);
        } else {
            result = methodInvocation.proceed();
        }
        return result;
    }

    private Connection getConnection(MethodInvocation<T> methodInvocation) throws Throwable {
        Connection connection = (Connection) methodInvocation.proceed();
        connection = proxyFactory.wrapConnection(name, connection);
        return connection;
    }
}

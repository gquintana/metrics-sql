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
import java.sql.*;

/**
 * JDBC Proxy handler for {@link Connection}
 */
public class ConnectionProxyHandler extends JdbcProxyHandler<Connection> {

    /**
     * Main constructor
     *
     * @param delegate Wrapped connection
     */
    public ConnectionProxyHandler(Connection delegate, String connectionFactoryName, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, Connection.class, connectionFactoryName, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object invoke(MethodInvocation<Connection> delegatingMethodInvocation) throws Throwable {
        final String methodName = delegatingMethodInvocation.getMethodName();
        Object result;
        if (methodName.equals("isWrapperFor")) {
            result = isWrapperFor(delegatingMethodInvocation);
        } else if (methodName.equals("unwrap")) {
            result = unwrap(delegatingMethodInvocation);
        } else if (methodName.equals("close")) {
            result = close(delegatingMethodInvocation);
        } else if (methodName.equals("createStatement")) {
            result = createStatement(delegatingMethodInvocation);
        } else if (methodName.equals("prepareStatement")) {
            result = prepareStatement(delegatingMethodInvocation);
        } else if (methodName.equals("prepareCall")) {
            result = prepareCall(delegatingMethodInvocation);
        } else {
            result = delegatingMethodInvocation.proceed();
        }
        return result;
    }

    private Statement createStatement(MethodInvocation<Connection> methodInvocation) throws Throwable {
        Statement result = (Statement) methodInvocation.proceed();
        result = proxyFactory.wrapStatement(name, result);
        return result;
    }

    private PreparedStatement prepareStatement(MethodInvocation<Connection> methodInvocation) throws Throwable {
        PreparedStatement result = (PreparedStatement) methodInvocation.proceed();
        result = proxyFactory.wrapPreparedStatement(name, result, methodInvocation.getArgAt(0, String.class));
        return result;
    }

    private CallableStatement prepareCall(MethodInvocation<Connection> methodInvocation) throws Throwable {
        CallableStatement result = (CallableStatement) methodInvocation.proceed();
        result = proxyFactory.wrapCallableStatement(name, result, methodInvocation.getArgAt(0, String.class));
        return result;
    }
}

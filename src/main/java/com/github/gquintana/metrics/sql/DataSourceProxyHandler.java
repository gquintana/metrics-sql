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


import com.github.gquintana.metrics.proxy.MethodInvocation;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * JDBC Proxy handler for {@link DataSource}
 */
public class DataSourceProxyHandler extends JdbcProxyHandler<DataSource> {
    public DataSourceProxyHandler(DataSource delegate, String name, JdbcProxyFactory proxyFactory) {
        super(delegate, DataSource.class, name, proxyFactory, null);
    }
    @Override
    protected Object invoke(MethodInvocation<DataSource> methodInvocation) throws Throwable {
        final String methodName=methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getConnection")) {
            result=getConnection(methodInvocation);
        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

    private Connection getConnection(MethodInvocation<DataSource> methodInvocation) throws Throwable {
        Connection connection=(Connection) methodInvocation.proceed();
        connection= proxyFactory.wrapConnection(name,connection);
        return connection;
    }
}

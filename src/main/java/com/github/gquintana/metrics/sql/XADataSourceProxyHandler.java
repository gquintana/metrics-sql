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
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;

/**
 * JDBC proxy handler for {@link XADataSource}
 */
public class XADataSourceProxyHandler extends JdbcProxyHandler<XADataSource> {

    private boolean collectBorrowMetrics = true;

    public XADataSourceProxyHandler(XADataSource delegate, String name, JdbcProxyFactory proxyFactory) {
        super(delegate, XADataSource.class, name, proxyFactory, null);
        this.collectBorrowMetrics = Boolean.valueOf(System.getProperty("com.github.gquintana.metrics.borrow-connection.enabled","true"));
    }

    @Override
    protected Object invoke(MethodInvocation<XADataSource> methodInvocation) throws Throwable {
        final String methodName = methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getXAConnection")) {
            result = getXAConnection(methodInvocation);
        } else {
            result = methodInvocation.proceed();
        }
        return result;
    }

    private XAConnection getXAConnection(MethodInvocation<XADataSource> methodInvocation) throws Throwable {


        Timer.Context failedBorrowContext = null;
        Timer.Context borrowContext = null;

        try {
            if (collectBorrowMetrics) {
                borrowContext = proxyFactory.startBorrowConnectionTimer(name);
                failedBorrowContext = proxyFactory.startFailedBorrowConnectionTimer(name);
            }

            XAConnection connection = (XAConnection) methodInvocation.proceed();

            if (collectBorrowMetrics) {
                borrowContext.close();
            }

            connection = proxyFactory.wrapXAConnection(name, connection);
            return connection;

        }catch (Throwable e){
            if (collectBorrowMetrics){
                failedBorrowContext.close();
            }
            throw e;
        }

    }
}

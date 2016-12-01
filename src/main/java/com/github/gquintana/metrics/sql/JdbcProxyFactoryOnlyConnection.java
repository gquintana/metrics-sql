package com.github.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.MethodInvocation;
import com.github.gquintana.metrics.proxy.ProxyFactory;

import java.sql.Connection;

/**
 * A {@code JdbcProxyFactory} that changes the ConnectionProxyHandler to one that does not wrap statements.
 * this is in case you want to measure only connection lifetime and not the other jdbc classes.
 *
 * Created on 4/17/16.
 */
public class JdbcProxyFactoryOnlyConnection extends JdbcProxyFactory{

    public JdbcProxyFactoryOnlyConnection(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    public JdbcProxyFactoryOnlyConnection(MetricNamingStrategy namingStrategy) {
        super(namingStrategy);
    }

    public JdbcProxyFactoryOnlyConnection(MetricNamingStrategy namingStrategy, ProxyFactory proxyFactory) {
        super(namingStrategy, proxyFactory);
    }


    @Override
    public Connection wrapConnection(String connectionFactoryName, Connection wrappedConnection) {
        Timer.Context lifeTimerContext = getMetricNamingStrategy().startConnectionTimer(connectionFactoryName);
        return newProxy(new ConnectionProxyHandlerOnlyConnection(wrappedConnection, connectionFactoryName, this, lifeTimerContext));
    }





    private static class ConnectionProxyHandlerOnlyConnection extends ConnectionProxyHandler{

        /**
         * Main constructor
         *
         * @param delegate              Wrapped connection
         * @param connectionFactoryName
         * @param proxyFactory
         * @param lifeTimerContext
         */
        public ConnectionProxyHandlerOnlyConnection(Connection delegate, String connectionFactoryName, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
            super(delegate, connectionFactoryName, proxyFactory, lifeTimerContext);
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
            } else {
                result = delegatingMethodInvocation.proceed();
            }
            return result;
        }



    }
}

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


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;

import javax.sql.*;
import javax.sql.rowset.*;
import java.sql.*;

/**
 * Factory of {@code JdbcProxyHandler} sub classes, central class of Metrics SQL.
 * It can be used to wrap any JDBC component (connection, statement,
 * result set...). 
 */
public class JdbcProxyFactory {
    /**
     * Timer manager
     */
    private final TimerStarter timerStarter;
    /**
     * Proxy factory
     */
    private final ProxyFactory proxyFactory;

    /**
     * Constructor using default {@link ReflectProxyFactory} and default {@link DefaultMetricNamingStrategy}
     * @param metricRegistry Metric registry to store metrics
     */
    public JdbcProxyFactory(MetricRegistry metricRegistry) {
        this(metricRegistry, new DefaultMetricNamingStrategy());
    }

    /**
     * Constructor
     *
     * @param registry Registry storing metrics
     * @param namingStrategy Naming strategy used to get metrics from SQL
     */
    public JdbcProxyFactory(MetricRegistry registry, MetricNamingStrategy namingStrategy) {
        this(registry, namingStrategy, new ReflectProxyFactory());
    }

    /**
     * Constructor
     *
     * @param registry Registry storing metrics
     * @param namingStrategy Naming strategy used to get metrics from SQL
     * @param proxyFactory AbstractProxyFactory to use for proxy creation
     */
    public JdbcProxyFactory(MetricRegistry registry, MetricNamingStrategy namingStrategy, ProxyFactory proxyFactory) {
        this.timerStarter = new TimerStarter(registry, namingStrategy);
        this.proxyFactory = proxyFactory;
    }
    /**
     * Create a proxy for given JDBC proxy handler
     * @param <T> Proxy type
     * @param proxyHandler Proxy handler
     * @return Proxy
     */
    private <T> T newProxy(JdbcProxyHandler<T> proxyHandler) {
        return proxyFactory.newProxy(proxyHandler, proxyHandler.getProxyClass());
    }
    
    /**
     * Wrap a data source to monitor it.
     *
     * @param wrappedDataSource Data source to wrap
     * @return Wrapped data source
     */
    public DataSource wrapDataSource(DataSource wrappedDataSource) {
        return newProxy(new DataSourceProxyHandler(wrappedDataSource, this));
    }

    /**
     * Wrap an XA data source to monitor it.
     *
     * @param wrappedDataSource XA Data source to wrap
     * @return Wrapped XA data source
     */
    public XADataSource wrapXADataSource(XADataSource wrappedDataSource) {
        return newProxy(new XADataSourceProxyHandler(wrappedDataSource, this));
    }

    /**
     * Wrap a pooled connection to monitor it.
     *
     * @param wrappedConnection Pooled connection to wrap
     * @return Wrapped pooled connection
     */
    public PooledConnection wrapPooledConnection(PooledConnection wrappedConnection) {
        Timer.Context lifeTimerContext = getTimerStarter().startConnectionTimer();
        return newProxy(new PooledConnectionProxyHandler<PooledConnection>(wrappedConnection, PooledConnection.class, this, lifeTimerContext));
    }

    /**
     * Wrap an XA connection to monitor it.
     *
     * @param wrappedConnection XA connection to wrap
     * @return XA pooled connection
     */
    public XAConnection wrapXAConnection(XAConnection wrappedConnection) {
        Timer.Context lifeTimerContext = getTimerStarter().startConnectionTimer();
        return newProxy(new PooledConnectionProxyHandler<XAConnection>(wrappedConnection, XAConnection.class, this, lifeTimerContext));
    }

    /**
     * Wrap a connection to monitor it.
     *
     * @param wrappedConnection Connection to wrap
     * @return Wrapped connection
     */
    public Connection wrapConnection(Connection wrappedConnection) {
        Timer.Context lifeTimerContext = timerStarter.startConnectionTimer();
        return newProxy(new ConnectionProxyHandler(wrappedConnection, this, lifeTimerContext));
    }
    
    /**
     * Wrap a simple statement to monitor it.
     *
     * @param statement Statement to wrap
     * @return Wrapped statement
     */
    public Statement wrapStatement(Statement statement) {
        Timer.Context lifeTimerContext = getTimerStarter().startStatementLifeTimer();
        return newProxy(new StatementProxyHandler(statement, this, lifeTimerContext));
    }

    /**
     * Wrap a prepared statement to monitor it.
     *
     * @param preparedStatement Prepared statement to wrap
     * @param sql SQL
     * @return Wrapped prepared statement
     */
    public PreparedStatement wrapPreparedStatement(PreparedStatement preparedStatement, String sql) {
        StatementTimerContext lifeTimerContext = getTimerStarter().startPreparedStatementLifeTimer(sql);
        return newProxy(new PreparedStatementProxyHandler(preparedStatement, this, lifeTimerContext));
    }

    /**
     * Wrap a callable statement to monitor it.
     *
     * @param callableStatement Prepared statement to wrap
     * @param sql SQL
     * @return Wrapped prepared statement
     */
    public CallableStatement wrapCallableStatement(CallableStatement callableStatement, String sql) {
        StatementTimerContext lifeTimerContext = getTimerStarter().startCallableStatementLifeTimer(sql);
        return newProxy(new CallableStatementProxyHandler(callableStatement, this, lifeTimerContext));
    }

    /**
     * Wrap a result set to monitor it.
     *
     * @param resultSet set to wrap
     * @param lifeTimerContext Started timer
     * @return Wrapped prepared statement
     */
    public ResultSet wrapResultSet(ResultSet resultSet, StatementTimerContext lifeTimerContext) {
        return (ResultSet) newProxy(new ResultSetProxyHandler(resultSet, getResultSetType(resultSet), this, lifeTimerContext.getTimerContext()));
    }
    /**
     * Determine the interface implemented by this result set
     *
     * @param resultSet Result set
     */
    private Class<? extends ResultSet> getResultSetType(ResultSet resultSet) {
        Class<? extends ResultSet> resultSetType;
        if (resultSet instanceof RowSet) {
            if (resultSet instanceof CachedRowSet) {
                if (resultSet instanceof WebRowSet) {
                    if (resultSet instanceof FilteredRowSet) {
                        resultSetType = FilteredRowSet.class;
                    } else if (resultSet instanceof JoinRowSet) {
                        resultSetType = JoinRowSet.class;
                    } else {
                        resultSetType = WebRowSet.class;
                    }
                } else {
                    resultSetType = CachedRowSet.class;
                }
            } else if (resultSet instanceof JdbcRowSet) {
                resultSetType = JdbcRowSet.class;
            } else {
                resultSetType = RowSet.class;
            }
        } else {
            resultSetType = ResultSet.class;
        }
        return resultSetType;
    }

    public TimerStarter getTimerStarter() {
        return timerStarter;
    }
}

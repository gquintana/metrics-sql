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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.RowSet;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.JoinRowSet;
import javax.sql.rowset.WebRowSet;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;

/**
 * Factory of {@code JdbcProxyHandler} sub classes, central class of Metrics SQL.
 * It can be used to wrap any JDBC component (connection, statement,
 * result set...). 
 */
public class JdbcProxyFactory {
    /**
     * Strategy used to get metric name
     */
    private final MetricNamingStrategy metricNamingStrategy;
    /**
     * Proxy factory
     */
    private final ProxyFactory proxyFactory;

    /**
     * Constructor using default {@link ReflectProxyFactory} and default {@link DefaultMetricNamingStrategy}
     */
    public JdbcProxyFactory(MetricRegistry metricRegistry) {
        this(new DefaultMetricNamingStrategy(metricRegistry));
    }

    /**
     * Constructor
     *
     * @param namingStrategy Naming strategy used to get metrics from SQL
     */
    public JdbcProxyFactory(MetricNamingStrategy namingStrategy) {
        this(namingStrategy, new ReflectProxyFactory());
    }

    /**
     * Constructor
     *
     * @param namingStrategy Naming strategy used to get metrics from SQL
     * @param proxyFactory AbstractProxyFactory to use for proxy creation
     */
    public JdbcProxyFactory(MetricNamingStrategy namingStrategy, ProxyFactory proxyFactory) {
        this.metricNamingStrategy = namingStrategy;
        this.proxyFactory = proxyFactory;
    }
    /**
     * Create a proxy for given JDBC proxy handler
     * @param <T> Proxy type
     * @param proxyHandler Proxy handler
     * @return Proxy
     */
    private <T> T newProxy(JdbcProxyHandler<T> proxyHandler) {
        return (T) proxyFactory.newProxy(proxyHandler, proxyHandler.getProxyClass());
    }
    
    /**
     * Wrap a data source to monitor it.
     *
     * @param connectionFactoryName Data source name
     * @param wrappedDataSource Data source to wrap
     * @return Wrapped data source
     */
    public DataSource wrapDataSource(String connectionFactoryName, DataSource wrappedDataSource) {
        return newProxy(new DataSourceProxyHandler(wrappedDataSource, connectionFactoryName, this));
    }

    /**
     * Wrap an XA data source to monitor it.
     *
     * @param connectionFactoryName Data source name
     * @param wrappedDataSource XA Data source to wrap
     * @return Wrapped XA data source
     */
    public XADataSource wrapXADataSource(String connectionFactoryName, XADataSource wrappedDataSource) {
        return newProxy(new XADataSourceProxyHandler(wrappedDataSource, connectionFactoryName, this));
    }

    /**
     * Wrap a pooled connection to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param wrappedConnection Pooled connection to wrap
     * @return Wrapped pooled connection
     */
    public PooledConnection wrapPooledConnection(String connectionFactoryName, PooledConnection wrappedConnection) {
        Timer.Context lifeTimerContext = metricNamingStrategy.startPooledConnectionTimer(connectionFactoryName);
        return newProxy(new PooledConnectionProxyHandler<PooledConnection>(wrappedConnection, PooledConnection.class, connectionFactoryName, this, lifeTimerContext));
    }

    /**
     * Wrap an XA connection to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param wrappedConnection XA connection to wrap
     * @return XA pooled connection
     */
    public XAConnection wrapXAConnection(String connectionFactoryName, XAConnection wrappedConnection) {
        Timer.Context lifeTimerContext = metricNamingStrategy.startPooledConnectionTimer(connectionFactoryName);
        return newProxy(new PooledConnectionProxyHandler<XAConnection>(wrappedConnection, XAConnection.class, connectionFactoryName, this, lifeTimerContext));
    }

    /**
     * Wrap a connection to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param wrappedConnection Connection to wrap
     * @return Wrapped connection
     */
    public Connection wrapConnection(String connectionFactoryName, Connection wrappedConnection) {
        Timer.Context lifeTimerContext = metricNamingStrategy.startConnectionTimer(connectionFactoryName);
        return newProxy(new ConnectionProxyHandler(wrappedConnection, connectionFactoryName, this, lifeTimerContext));
    }
    
    /**
     * Wrap a simple statement to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param statement Statement to wrap
     * @return Wrapped statement
     */
    public Statement wrapStatement(String connectionFactoryName, Statement statement) {
        Timer.Context lifeTimerContext = metricNamingStrategy.startStatementTimer(connectionFactoryName);
        return newProxy(new StatementProxyHandler(statement, connectionFactoryName, this, lifeTimerContext));
    }

    /**
     * Start Timer when statement is executed
     *
     * @param connectionFactoryName DataSource/Driver name
     * @param sql SQL query
     * @return Started timer context or null
     */
    public StatementTimerContext startStatementExecuteTimer(String connectionFactoryName, String sql) {
        return metricNamingStrategy.startStatementExecuteTimer(connectionFactoryName, sql);
    }

    /**
     * Wrap a prepared statement to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param preparedStatement Prepared statement to wrap
     * @param sql SQL used for creation
     * @return Wrapped prepared statement
     */
    public PreparedStatement wrapPreparedStatement(String connectionFactoryName, PreparedStatement preparedStatement, String sql) {
        StatementTimerContext lifeTimerContext = metricNamingStrategy.startPreparedStatementTimer(connectionFactoryName, sql, null);
        PreparedStatementProxyHandler proxyHandler;
        if (lifeTimerContext==null) {
            proxyHandler = new PreparedStatementProxyHandler(preparedStatement, connectionFactoryName, this, null, sql, null);
        } else {
            proxyHandler = new PreparedStatementProxyHandler(preparedStatement, connectionFactoryName, this, lifeTimerContext.getTimerContext(), lifeTimerContext.getSql(), lifeTimerContext.getSqlId());
        }
        return newProxy(proxyHandler);
    }
    /**
     * Start timer measuring {@link PreparedStatement#execute() }
     * @param connectionFactoryName Connection factory name
     * @param sql SQL query
     * @param sqlId SQL Id
     * @return Started timer context or null
     */
    public StatementTimerContext startPreparedStatementExecuteTimer(String connectionFactoryName, String sql, String sqlId) {
        return metricNamingStrategy.startPreparedStatementExecuteTimer(connectionFactoryName, sql, sqlId);
    }

    /**
     * Wrap a callable statement to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param callableStatement Prepared statement to wrap
     * @param sql SQL used for creation
     * @return Wrapped prepared statement
     */
    public CallableStatement wrapCallableStatement(String connectionFactoryName, CallableStatement callableStatement, String sql) {
        StatementTimerContext lifeTimerContext = metricNamingStrategy.startCallableStatementTimer(connectionFactoryName, sql, null);
        CallableStatementProxyHandler proxyHandler;
        if (lifeTimerContext==null) {
            proxyHandler = new CallableStatementProxyHandler(callableStatement, connectionFactoryName, this, null, sql, null);
        } else {
            proxyHandler = new CallableStatementProxyHandler(callableStatement, connectionFactoryName, this, lifeTimerContext.getTimerContext(), lifeTimerContext.getSql(), lifeTimerContext.getSqlId());
        }
        return newProxy(proxyHandler);
    }
    /**
     * Start timer measuring {@link CallableStatement#execute() }
     *
     * @param connectionFactoryName Connection factory name
     * @param sql SQL query
     * @param sqlId SQL Id
     * @return Started timer context or null
     */
    public StatementTimerContext startCallableStatementExecuteTimer(String connectionFactoryName, String sql, String sqlId) {
        return metricNamingStrategy.startCallableStatementExecuteTimer(connectionFactoryName, sql, sqlId);
    }

    /**
     * Wrap a result set to monitor it.
     *
     * @param connectionFactoryName Data source/Driver name
     * @param resultSet set to wrap
     * @param sql SQL
     * @param sqlId  SQL Id
     * @return Wrapped prepared statement
     */
    public ResultSet wrapResultSet(String connectionFactoryName, ResultSet resultSet, String sql, String sqlId) {
        Timer.Context lifeTimerContext = metricNamingStrategy.startResultSetTimer(connectionFactoryName, sql, sqlId);
        return (ResultSet) newProxy(new ResultSetProxyHandler(resultSet, getResultSetType(resultSet), connectionFactoryName, this, lifeTimerContext));
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


}

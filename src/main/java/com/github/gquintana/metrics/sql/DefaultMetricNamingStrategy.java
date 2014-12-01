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
import javax.sql.PooledConnection;
import com.github.gquintana.metrics.util.DefaultMetricRegistryHolder;
import com.github.gquintana.metrics.util.MetricRegistryHolder;

/**
 * Defaut implementation of {@link MetricNamingStrategy}
 */
public class DefaultMetricNamingStrategy implements MetricNamingStrategy {
    private final MetricRegistryHolder metricRegistryHolder;

    public DefaultMetricNamingStrategy(MetricRegistryHolder metricRegistryHolder) {
        this.metricRegistryHolder = metricRegistryHolder;
    }

    public DefaultMetricNamingStrategy(MetricRegistry metricRegistry) {
        this.metricRegistryHolder = new DefaultMetricRegistryHolder(metricRegistry);
    }

    public MetricRegistryHolder getMetricRegistryHolder() {
        return metricRegistryHolder;
    }
    
    protected MetricRegistry getMetricRegistry() {
        return metricRegistryHolder.getMetricRegistry();
    }
    /**
     * Generate SQL Id from SQL query.
     * This method can be used to normalize SQL queries, remove special characters,
     * truncate long SQL queries...
     * @return [sql]
     */
    protected String getSqlId(String sql) {
        return "["+sql.toLowerCase()+"]";
    }
    /**
     * Start Timer for given Class and names
     */
    protected Timer.Context startTimer(Class<?> clazz, String ... names) {
        return getMetricRegistry().timer(MetricRegistry.name(clazz, names)).time();
    }
    /**
     * {@inheritDoc}
     * Example: java.sql.PooledConnection.database
     */
    public Timer.Context startPooledConnectionTimer(String databaseName) {
        return startTimer(PooledConnection.class, databaseName);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Connection.database
     */
    public Timer.Context startConnectionTimer(String databaseName) {
        return startTimer(Connection.class, databaseName);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Statement.database
     */
    public Timer.Context startStatementTimer(String databaseName) {
        return startTimer(Statement.class, databaseName);
    }

    protected StatementTimerContext startStatementTimer(Class<? extends Statement> clazz, String databaseName, String sql, String sqlId) {
        final String lSqlId = sqlId == null ? getSqlId(sql) : sqlId;
        final Timer.Context timerContext = startTimer(clazz, databaseName, lSqlId);
        return new StatementTimerContext(timerContext, sql, lSqlId);
    }

    protected StatementTimerContext startStatementExecuteTimer(Class<? extends Statement> clazz, String databaseName, String sql, String sqlId) {
        final String lSqlId = sqlId == null ? getSqlId(sql) : sqlId;
        final Timer.Context timerContext = startTimer(clazz, databaseName, lSqlId, "exec");
        return new StatementTimerContext(timerContext, sql, lSqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Statement.database.[sqlId].exec
     */
    public StatementTimerContext startStatementExecuteTimer(String databaseName, String sql) {
        return startStatementExecuteTimer(Statement.class, databaseName, sql, null);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.PreparedStatement.database.[sqlId]
     */
    public StatementTimerContext startPreparedStatementTimer(String databaseName, String sql, String sqlId) {
        return startStatementTimer(PreparedStatement.class, databaseName, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.PreparedStatement.database.[sqlId].exec
     */
    public StatementTimerContext startPreparedStatementExecuteTimer(String databaseName, String sql, String sqlId) {
        return startStatementExecuteTimer(PreparedStatement.class, databaseName, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.CallableStatement.database.[sqlId]
     */
    public StatementTimerContext startCallableStatementTimer(String databaseName, String sql, String sqlId) {
        return startStatementTimer(CallableStatement.class, databaseName, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.CallableStatement.database.[sqlId].exec
     */
    public StatementTimerContext startCallableStatementExecuteTimer(String databaseName, String sql, String sqlId) {
        return startStatementExecuteTimer(CallableStatement.class, databaseName, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     */
    public Timer.Context startResultSetTimer(String databaseName, String sql, String sqlId) {
        return startTimer(ResultSet.class, databaseName, sqlId);
    }
}

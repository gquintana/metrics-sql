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

import javax.sql.PooledConnection;
import java.sql.*;

/**
 * Default implementation of {@link MetricNamingStrategy}
 */
public class DefaultMetricNamingStrategy implements MetricNamingStrategy {
    private final String databaseName;

    public DefaultMetricNamingStrategy() {
        databaseName = "";
    }

    /**
     * @param databaseName Database name
     */
    public DefaultMetricNamingStrategy(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Generate SQL Id from SQL query.
     * This method can be used to normalize SQL queries, remove special characters,
     * truncate long SQL queries...
     *
     * @param sql Input SQL
     * @return [sql]
     */
    public String getSqlId(String sql) {
        return "[" + sql.toLowerCase() + "]";
    }

    protected String getStatementTimer(Class<? extends Statement> clazz, String sql, String sqlId) {
        final String lSqlId = sqlId == null ? getSqlId(sql) : sqlId;
        return MetricRegistry.name(clazz, databaseName, lSqlId);
    }

    protected String getStatementExecuteTimer(Class<? extends Statement> clazz, String sql, String sqlId) {
        final String lSqlId = sqlId == null ? getSqlId(sql) : sqlId;
        return MetricRegistry.name(clazz, databaseName, lSqlId, "exec");
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Connection.database.get
     */
    public String getConnectionGetTimer() {
        return MetricRegistry.name(Connection.class, databaseName, "get");
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Connection.database
     */
    public String getConnectionLifeTimer() {
        return MetricRegistry.name(Connection.class, databaseName);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Statement.database
     */
    public String getStatementLifeTimer() {
        return MetricRegistry.name(Statement.class, databaseName);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.Statement.database.[sqlId].exec
     */
    public String getStatementExecuteTimer(String sql, String sqlId) {
        return getStatementExecuteTimer(Statement.class, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.PreparedStatement.database.[sqlId]
     */
    public String getPreparedStatementLifeTimer(String sql, String sqlId) {
        return getStatementTimer(PreparedStatement.class, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.PreparedStatement.database.[sqlId].exec
     */
    public String getPreparedStatementExecuteTimer(String sql, String sqlId) {
        return getStatementExecuteTimer(PreparedStatement.class, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.CallableStatement.database.[sqlId]
     */
    public String getCallableStatementLifeTimer(String sql, String sqlId) {
        return getStatementTimer(CallableStatement.class, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     * Example: java.sql.CallableStatement.database.[sqlId].exec
     */
    public String getCallableStatementExecuteTimer(String sql, String sqlId) {
        return getStatementExecuteTimer(CallableStatement.class, sql, sqlId);
    }

    /**
     * {@inheritDoc}
     */
    public String getResultSetLifeTimer(String sql, String sqlId) {
        return MetricRegistry.name(ResultSet.class, databaseName, sqlId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultSetRowMeter(String sql, String sqlId) {
        return MetricRegistry.name(ResultSet.class, databaseName, sqlId, "rows");
    }
}

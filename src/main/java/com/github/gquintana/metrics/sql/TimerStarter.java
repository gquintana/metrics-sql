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

/**
 * Start {@link com.codahale.metrics.Timer}s.
 * Internal helper class.
 */
class TimerStarter {
    private final MetricRegistry metricRegistry;
    private final MetricNamingStrategy metricNamingStrategy;

    /**
     * Constructor
     * @param metricRegistry Registry storing metrics
     * @param metricNamingStrategy Strategy to name metrics
     */
    TimerStarter(MetricRegistry metricRegistry, MetricNamingStrategy metricNamingStrategy) {
        this.metricRegistry = metricRegistry;
        this.metricNamingStrategy = metricNamingStrategy;
    }

    private Timer.Context startTimer(String name) {
        if (name == null) {
            return null;
        }
        return metricRegistry.timer(name).time();
    }

    private StatementTimerContext startStatementTimer(String name, String sql, String sqlId) {
        return new StatementTimerContext(startTimer(name), sql, sqlId);
    }

    public Timer.Context startConnectionTimer() {
        return startTimer(metricNamingStrategy.getConnectionLifeTimer());
    }

    /**
     * Start Timer when statement is created
     *
     * @return Started timer context or null
     */
    public Timer.Context startStatementLifeTimer() {
        return startTimer(metricNamingStrategy.getStatementLifeTimer());
    }
    /**
     * Start Timer when statement is executed
     *
     * @param sql SQL query
     * @return Started timer context or null
     */
    public StatementTimerContext startStatementExecuteTimer(String sql) {
        String sqlId = metricNamingStrategy.getSqlId(sql);
        String name = metricNamingStrategy.getStatementExecuteTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public StatementTimerContext startPreparedStatementLifeTimer(String sql) {
        String sqlId = metricNamingStrategy.getSqlId(sql);
        String name = metricNamingStrategy.getPreparedStatementLifeTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public StatementTimerContext startPreparedStatementExecuteTimer(String sql, String sqlId) {
        sqlId = getSqlId(sqlId, sql);
        String name = metricNamingStrategy.getPreparedStatementExecuteTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }

    private String getSqlId(String sqlId, String sql) {
        if (sqlId == null) {
            sqlId = metricNamingStrategy.getSqlId(sql);
        }
        return sqlId;
    }

    /**
     * Start Timer when callable statement is created
     *
     * @return Started timer context or null
     */
    public StatementTimerContext startCallableStatementLifeTimer(String sql) {
        String sqlId = metricNamingStrategy.getSqlId(sql);
        String name = metricNamingStrategy.getCallableStatementLifeTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public StatementTimerContext startCallableStatementExecuteTimer(String sql, String sqlId) {
        sqlId = getSqlId(sqlId, sql);
        String name = metricNamingStrategy.getCallableStatementExecuteTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }

    /**
     * Start Timer when result set is created
     *
     * @return Started timer context or null
     */
    public StatementTimerContext startResultSetLifeTimer(String sql, String sqlId) {
        sqlId = getSqlId(sqlId, sql);
        String name = metricNamingStrategy.getResultSetLifeTimer(sql, sqlId);
        return startStatementTimer(name, sql, sqlId);
    }
}

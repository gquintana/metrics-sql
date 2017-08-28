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
 * Start {@link com.codahale.metrics.Timer}s and increments {@link com.codahale.metrics.Meter}s
 * Internal helper class.
 */
class MetricHelper {
    private final MetricRegistry metricRegistry;
    private final MetricNamingStrategy metricNamingStrategy;

    /**
     * Constructor
     * @param metricRegistry Registry storing metrics
     * @param metricNamingStrategy Strategy to name metrics
     */
    MetricHelper(MetricRegistry metricRegistry, MetricNamingStrategy metricNamingStrategy) {
        this.metricRegistry = metricRegistry;
        this.metricNamingStrategy = metricNamingStrategy;
    }

    private Timer.Context startTimer(String name) {
        if (name == null) {
            return null;
        }
        return metricRegistry.timer(name).time();
    }
    private void markMeter(String name) {
        if (name == null) {
            return;
        }
        metricRegistry.meter(name).mark();
    }

    public Timer.Context startConnectionLifeTimer() {
        return startTimer(metricNamingStrategy.getConnectionLifeTimer());
    }

    public Timer.Context startConnectionGetTimer() {
        return startTimer(metricNamingStrategy.getConnectionGetTimer());
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
     * @param query SQL query
     * @return Started timer context or null
     */
    public Timer.Context startStatementExecuteTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getStatementExecuteTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
    }

    private void ensureSqlId(Query query) {
        query.ensureSqlId(metricNamingStrategy);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public Timer.Context startPreparedStatementLifeTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getPreparedStatementLifeTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public Timer.Context startPreparedStatementExecuteTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getPreparedStatementExecuteTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
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
    public Timer.Context startCallableStatementLifeTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getCallableStatementLifeTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
    }

    /**
     * Start Timer when prepared statement is created
     *
     * @return Started timer context or null
     */
    public Timer.Context startCallableStatementExecuteTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getCallableStatementExecuteTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
    }

    /**
     * Start Timer when result set is created
     *
     * @return Started timer context or null
     */
    public Timer.Context startResultSetLifeTimer(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getResultSetLifeTimer(query.getSql(), query.getSqlId());
        return startTimer(name);
    }
    /**
     * Increment when result set row is read
     */
    public void markResultSetRowMeter(Query query) {
        ensureSqlId(query);
        String name = metricNamingStrategy.getResultSetRowMeter(query.getSql(), query.getSqlId());
        markMeter(name);
    }
}

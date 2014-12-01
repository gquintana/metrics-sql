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
import com.github.gquintana.metrics.util.MetricRegistryHolder;

/**
 * {@link MetricNamingStrategy} filtering Connection, PreparedStatement, CallableStatement lifes
 */
public class FilteringMetricNamingStrategy extends DefaultMetricNamingStrategy {

    public FilteringMetricNamingStrategy(MetricRegistryHolder metricRegistryHolder) {
        super(metricRegistryHolder);
    }

    public FilteringMetricNamingStrategy(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public StatementTimerContext startPreparedStatementTimer(String connectionFactoryName, String sql, String sqlId) {
        return null;
    }

    @Override
    public StatementTimerContext startCallableStatementTimer(String connectionFactoryName, String sql, String sqlId) {
        return null;
    }

    @Override
    public Timer.Context startConnectionTimer(String connectionFactoryName) {
        return null;
    }
    

}

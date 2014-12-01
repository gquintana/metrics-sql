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
import java.sql.Connection;
import javax.sql.DataSource;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import com.github.gquintana.metrics.util.MetricRegistryHolder;

/**
 * Metrics SQL initiazing class
 */
public class MetricsSql {
    /**
     * Builder of {@link JdbcProxyFactory}
     */
    public static class Builder {
        private final MetricNamingStrategy namingStrategy;
        private ProxyFactory proxyFactory = new ReflectProxyFactory();
        private JdbcProxyFactory jdbcProxyFactory;
        public Builder(MetricNamingStrategy namingStrategy) {
            this.namingStrategy = namingStrategy;
        }
        public Builder(MetricRegistryHolder registryHolder) {
            this.namingStrategy = new DefaultMetricNamingStrategy(registryHolder);
        }
        
        public Builder(MetricRegistry registry) {
            this.namingStrategy = new DefaultMetricNamingStrategy(registry);
        }
        /**
         * Select factory of proxies
         */
        public Builder withProxyFactory(ProxyFactory proxyFactory) {
            this.proxyFactory = proxyFactory;
            return this;
        }
        public JdbcProxyFactory build() {
            if (jdbcProxyFactory == null) {
                jdbcProxyFactory = new JdbcProxyFactory(namingStrategy, proxyFactory);
            }
            return jdbcProxyFactory;
        }
        /**
         * Wrap an existing {@link DataSource} to add metrics
         */
        public DataSource wrap(String databaseName, DataSource dataSource) {
            return build().wrapDataSource(databaseName, dataSource);
        } 
        /**
         * Wrap an existing {@link Connection} to add connection
         */
        public Connection wrap(String databaseName, Connection connection) {
            return build().wrapConnection(databaseName, connection);
        } 
    }
    /**
     * Select Default naming strategy and Metric registry
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }
    /**
     * Select Default naming strategy and Metric registry holder
     */
    public static Builder forRegistryHolder(MetricRegistryHolder registryHolder) {
        return new Builder(registryHolder);
    }
    /**
     * Select Metric naming strategy and Metric registry
     */
    public static Builder withMetricNamingStrategy(MetricNamingStrategy namingStrategy) {
        return new Builder(namingStrategy);
    }
}

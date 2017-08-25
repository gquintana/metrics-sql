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
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import com.github.gquintana.metrics.util.MetricRegistryHolder;

import javax.sql.DataSource;
import java.sql.Connection;

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
        private Builder(MetricNamingStrategy namingStrategy) {
            this.namingStrategy = namingStrategy;
        }
        private Builder(MetricRegistryHolder registryHolder) {
            this.namingStrategy = new DefaultMetricNamingStrategy(registryHolder);
        }
        public Builder(MetricRegistry registry) {
            this.namingStrategy = new DefaultMetricNamingStrategy(registry);
        }
        /**
         * Select factory of proxies
         * @param proxyFactory Strategy to create proxies
         * @return Current builder
         */
        public Builder withProxyFactory(ProxyFactory proxyFactory) {
            this.proxyFactory = proxyFactory;
            return this;
        }

        /**
         * Build {@link JdbcProxyFactory}
         * @return Built {@link JdbcProxyFactory}
         */
        public JdbcProxyFactory build() {
            if (jdbcProxyFactory == null) {
                jdbcProxyFactory = new JdbcProxyFactory(namingStrategy, proxyFactory);
            }
            return jdbcProxyFactory;
        }
        /**
         * Wrap an existing {@link DataSource} to add metrics
         * @param databaseName Database name for metric naming
         * @param dataSource {@link DataSource} to wrap
         * @return Wrapped {@link DataSource}
         */
        public DataSource wrap(String databaseName, DataSource dataSource) {
            return build().wrapDataSource(databaseName, dataSource);
        } 
        /**
         * Wrap an existing {@link Connection} to add connection
         * @param databaseName Database name for metric naming
         * @param connection {@link Connection} to wrap
         * @return Wrapped {@link Connection}
         */
        public Connection wrap(String databaseName, Connection connection) {
            return build().wrapConnection(databaseName, connection);
        } 
    }
    /**
     * Select Default naming strategy and Metric registry
     * @param registry Metrics registry
     * @return Builder of {@link JdbcProxyFactory}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }
    /**
     * Select Default naming strategy and Metric registry holder
     * @param registryHolder Metrics registry provider or holder
     * @return Builder of {@link JdbcProxyFactory}
     */
    public static Builder forRegistryHolder(MetricRegistryHolder registryHolder) {
        return new Builder(registryHolder);
    }
    /**
     * Configure Metric naming strategy
     * @param namingStrategy Strategy to name metrics
     * @return Builder of {@link JdbcProxyFactory}
     */
    public static Builder withMetricNamingStrategy(MetricNamingStrategy namingStrategy) {
        return new Builder(namingStrategy);
    }
}

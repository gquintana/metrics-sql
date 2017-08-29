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
import java.sql.*;

/**
 * Metrics SQL initializing class
 */
public class MetricsSql {
    /**
     * Builder of {@link JdbcProxyFactory}
     */
    public static class Builder {
        private final MetricRegistry registry;
        private MetricNamingStrategy namingStrategy = new DefaultMetricNamingStrategy();
        private ProxyFactory proxyFactory = new ReflectProxyFactory();
        private JdbcProxyFactory jdbcProxyFactory;

        private Builder(MetricRegistryHolder registryHolder) {
            this.registry = registryHolder.getMetricRegistry();
        }

        public Builder(MetricRegistry registry) {
            this.registry = registry;
        }

        /**
         * Select naming strategy
         *
         * @param namingStrategy Strategy to name metrics
         * @return Current builder
         */
        public Builder withNamingStrategy(MetricNamingStrategy namingStrategy) {
            this.namingStrategy = namingStrategy;
            return this;
        }

        /**
         * Select default naming strategy
         *
         * @param databaseName Database name for metric naming
         * @return Current builder
         */
        public Builder withDefaultNamingStrategy(String databaseName) {
            return withNamingStrategy(new DefaultMetricNamingStrategy(databaseName));
        }

        /**
         * Select strict naming strategy
         *
         * @param databaseName Database name for metric naming
         * @return Current builder
         */
        public Builder withStrictNamingStrategy(String databaseName) {
            return withNamingStrategy(new StrictMetricNamingStrategy.Builder().withDatabaseName(databaseName).build());
        }

        /**
         * Select factory of proxies
         *
         * @param proxyFactory Strategy to create proxies
         * @return Current builder
         */
        public Builder withProxyFactory(ProxyFactory proxyFactory) {
            this.proxyFactory = proxyFactory;
            return this;
        }

        /**
         * Build {@link JdbcProxyFactory}
         *
         * @return Built {@link JdbcProxyFactory}
         */

        public JdbcProxyFactory build() {
            if (jdbcProxyFactory == null) {
                jdbcProxyFactory = new JdbcProxyFactory(registry, namingStrategy, proxyFactory);
            }
            return jdbcProxyFactory;
        }

        /**
         * Wrap an existing {@link DataSource} to add metrics
         *
         * @param dataSource   {@link DataSource} to wrap
         * @return Wrapped {@link DataSource}
         */
        public DataSource wrap(DataSource dataSource) {
            return build().wrapDataSource(dataSource);
        }

        /**
         * Wrap an existing {@link Connection} to add metrics
         *
         * @param connection   {@link Connection} to wrap
         * @return Wrapped {@link Connection}
         */
        public Connection wrap(Connection connection) {
            return build().wrapConnection(connection);
        }
        /**
         * Wrap an existing {@link Statement} to add metrics
         *
         * @param statement  {@link Statement} to wrap
         * @return Wrapped {@link Statement}
         */
        public Statement wrap(Statement statement) {
            return build().wrapStatement(statement);
        }
        /**
         * Wrap an existing {@link PreparedStatement}
         *
         * @param statement  {@link PreparedStatement} to add metrics
         * @param sql SQL
         * @return Wrapped {@link PreparedStatement}
         */
        public PreparedStatement wrap(PreparedStatement statement, String sql) {
            return build().wrapPreparedStatement(statement, sql);
        }
        /**
         * Wrap an existing {@link CallableStatement} to add metrics
         *
         * @param statement  {@link CallableStatement} to wrap
         * @param sql SQL
         * @return Wrapped {@link CallableStatement}
         */
        public CallableStatement wrap(CallableStatement statement, String sql) {
            return build().wrapCallableStatement(statement, sql);
        }
        /**
         * Wrap an existing {@link java.sql.ResultSet} to add metrics
         *
         * @param statement  {@link java.sql.ResultSet} to wrap
         * @param sql SQL
         * @return Wrapped {@link java.sql.ResultSet}
         */
        public ResultSet wrap(ResultSet statement, String sql) {
            return build().wrapResultSet(statement, sql);
        }
    }

    /**
     * Select Default naming strategy and Metric registry
     *
     * @param registry Metrics registry
     * @return Builder of {@link JdbcProxyFactory}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * Select Default naming strategy and Metric registry holder
     *
     * @param registryHolder Metrics registry provider or holder
     * @return Builder of {@link JdbcProxyFactory}
     */
    public static Builder forRegistryHolder(MetricRegistryHolder registryHolder) {
        return new Builder(registryHolder);
    }
}

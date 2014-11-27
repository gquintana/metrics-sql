/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.sql.Connection;
import javax.sql.DataSource;
import net.gquintana.metrics.proxy.ProxyFactory;
import net.gquintana.metrics.proxy.ReflectProxyFactory;
import net.gquintana.metrics.util.MetricRegistryHolder;

/**
 * Metrics SQL initiazing class
 */
public class MetricsSql {
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

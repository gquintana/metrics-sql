/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import net.gquintana.metrics.util.MetricRegistryHolder;

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

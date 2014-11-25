/*
 * Default License
 */

package net.gquintana.metrics.util;

import com.codahale.metrics.MetricRegistry;

/**
 * Singleton metric registry
 */
public class DefaultMetricRegistryHolder implements MetricRegistryHolder{
    /**
     * Singleton instance
     */
    private MetricRegistry metricRegistry;

    public DefaultMetricRegistryHolder() {
        metricRegistry = new MetricRegistry();
    }

    public DefaultMetricRegistryHolder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }


    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
    
}

/*
 * Default License
 */

package net.gquintana.metrics.util;

import com.codahale.metrics.MetricRegistry;

/**
 * Singleton metric registry
 */
public class StaticMetricRegistryHolder implements MetricRegistryHolder{
    /**
     * Singleton instance
     */
    private static MetricRegistry metricRegistry = new MetricRegistry();


    public static void setMetricRegistry(MetricRegistry metricRegistry) {
        StaticMetricRegistryHolder.metricRegistry = metricRegistry;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
    
}

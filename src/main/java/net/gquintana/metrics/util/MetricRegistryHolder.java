/*
 * Default License
 */

package net.gquintana.metrics.util;

import com.codahale.metrics.MetricRegistry;

/**
 * Metric registry locator
 */
public interface MetricRegistryHolder {
    /**
     * Get metric registry
     * @return Metric registry
     */
    MetricRegistry getMetricRegistry();
}

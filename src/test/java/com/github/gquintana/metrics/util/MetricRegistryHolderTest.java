package com.github.gquintana.metrics.util;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
public class MetricRegistryHolderTest {

    @Test
    public void testGetMetricRegistry_Default() throws Exception {
        // Given
        MetricRegistry registry = new MetricRegistry();
        MetricRegistryHolder registryHolder = new DefaultMetricRegistryHolder(registry);
        // When Then
        assertThat(registryHolder.getMetricRegistry(), sameInstance(registry));

    }

    @Test
    public void testGetMetricRegistry_Static() throws Exception {
        // Given
        MetricRegistry registry = new MetricRegistry();
        StaticMetricRegistryHolder.setMetricRegistry(registry);
        // When Then
        assertThat(new StaticMetricRegistryHolder().getMetricRegistry(), sameInstance(registry));

    }

}
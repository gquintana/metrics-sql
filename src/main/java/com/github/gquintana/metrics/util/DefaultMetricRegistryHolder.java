package com.github.gquintana.metrics.util;

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

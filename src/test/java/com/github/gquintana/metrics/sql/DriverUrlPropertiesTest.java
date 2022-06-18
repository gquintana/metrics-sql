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

import com.github.gquintana.metrics.proxy.CGLibProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class DriverUrlPropertiesTest {
    public static class CustomMetricNamingStrategy extends DefaultMetricNamingStrategy {

    }

    @Test
    public void testProperties() {
        DriverUrl driverUrl = DriverUrl.parse("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_driver=org.h2.Driver;metrics_proxy_factory=cglib;metrics_naming_strategy=" + CustomMetricNamingStrategy.class.getName() + ";metrics_database=test");
        assertThat(driverUrl.getProxyFactoryClass()).isEqualTo(CGLibProxyFactory.class);
        assertThat(driverUrl.getNamingStrategyClass()).isEqualTo(CustomMetricNamingStrategy.class);
        assertThat(driverUrl.getDatabaseName()).isEqualTo("test");
        assertThat(driverUrl.getDriverClass()).isEqualTo(org.h2.Driver.class);
    }

    @Test
    public void testPropertiesDefault() {
        DriverUrl driverUrl = DriverUrl.parse("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE");
        assertThat(driverUrl.getProxyFactoryClass()).isEqualTo(ReflectProxyFactory.class);
        assertThat(driverUrl.getNamingStrategyClass()).isEqualTo(DefaultMetricNamingStrategy.class);
        assertThat(driverUrl.getDatabaseName()).isNull();
        assertThat(driverUrl.getDriverClass()).isNull();
    }
}

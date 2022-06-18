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
import com.github.gquintana.metrics.proxy.CGLibProxyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test between {@link CGLibProxyFactory} and {@link JdbcProxyFactory}
 */
public class CGLibProxyFactoryTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    @BeforeEach
    public void setUp() {
        metricRegistry = new MetricRegistry();
        CGLibProxyFactory factory = new CGLibProxyFactory();
        proxyFactory = MetricsSql.forRegistry(metricRegistry)
                .withProxyFactory(factory)
                .build();
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection(H2DbUtil.openConnection());
        H2DbUtil.close(connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(metricRegistry.getTimers().get("java.sql.Connection")).isNotNull();
        
    }
   
}

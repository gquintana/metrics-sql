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
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.util.StaticMetricRegistryHolder;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Test Statement wrapper
 */
public class DriverTest {
    private MetricRegistry metricRegistry;
    private static final String URL = H2DbUtil.URL.replaceFirst("jdbc:h2","jdbc:metrics:h2");
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        StaticMetricRegistryHolder.setMetricRegistry(metricRegistry);
        // Load drivers
        List<java.sql.Driver> drivers = Arrays.asList(new Driver(),new org.h2.Driver());
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL, H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        H2DbUtil.close(statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(connection.getClass()));
        Timer lifeTimer = metricRegistry.timer("java.sql.Connection");
        assertNotNull(lifeTimer);
        assertThat(lifeTimer.getCount(), equalTo(1L));
        Timer getTimer = metricRegistry.timer("java.sql.Connection.get");
        assertNotNull(getTimer);
        assertThat(getTimer.getCount(), equalTo(1L));
    }
    @Test
    public void testStatementExec() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL, H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select CURRENT_DATE");
        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.Statement.[select current_date].exec"));
        
    }
    
    @Test
    public void testGetInfo() throws SQLException {
        // Act
        java.sql.Driver driver = DriverManager.getDriver(URL);
        // Assert
        assertTrue(driver instanceof Driver);
        assertEquals(3, driver.getMajorVersion());
        assertEquals(1, driver.getMinorVersion());
        Properties properties = new Properties();
        DriverPropertyInfo[] propertyInfos=driver.getPropertyInfo(URL, properties);
        assertNotNull(propertyInfos);
        String prefix = driver.getParentLogger().getName();
        assertTrue(getClass().getName().startsWith(prefix));
        assertTrue(driver.jdbcCompliant());
    }
}

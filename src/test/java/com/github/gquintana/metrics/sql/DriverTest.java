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
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Statement wrapper
 */
public class DriverTest {
    public static final String URL = H2DbUtil.URL.replaceFirst("jdbc:h2", "jdbc:metrics:h2");

    @BeforeEach
    public void setUp() throws SQLException {
        // Load drivers
        List<java.sql.Driver> drivers = Arrays.asList(new Driver(), new org.h2.Driver());
    }

    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL + ";metrics_registry=life", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        H2DbUtil.close(statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(connection.getClass())).isTrue();
        MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("life");
        Timer lifeTimer = metricRegistry.timer("java.sql.Connection");
        assertThat(lifeTimer).isNotNull();
        assertThat(lifeTimer.getCount()).isEqualTo(1L);
        Timer getTimer = metricRegistry.timer("java.sql.Connection.get");
        assertThat(getTimer).isNotNull();
        assertThat(getTimer.getCount()).isEqualTo(1L);
    }

    @Test
    public void testStatementExec() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL + ";metrics_registry=exec", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select CURRENT_DATE");

        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(resultSet.getClass())).isTrue();
        MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("exec");
        assertThat(metricRegistry.getTimers().get("java.sql.Statement.[select current_date].exec")).isNotNull();

    }

    @Test
    public void testGetInfo() throws SQLException {
        // Act
        java.sql.Driver driver = DriverManager.getDriver(URL);
        // Assert
        assertThat(driver instanceof Driver).isTrue();
        assertThat(driver.getMajorVersion()).isEqualTo(3);
        assertThat(driver.getMinorVersion()).isEqualTo(1);
        Properties properties = new Properties();
        DriverPropertyInfo[] propertyInfos = driver.getPropertyInfo(URL, properties);
        assertThat(propertyInfos).isNotNull();
        String prefix = driver.getParentLogger().getName();
        assertThat(getClass().getName().startsWith(prefix)).isTrue();
        assertThat(driver.jdbcCompliant()).isTrue();
    }
}

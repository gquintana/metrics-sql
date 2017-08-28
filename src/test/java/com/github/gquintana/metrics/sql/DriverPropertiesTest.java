package com.github.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DriverPropertiesTest {


    @Test
    public void testNamingStrategy() throws SQLException {
        // When
        Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_naming_strategy=default;metrics_registry=naming", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        // Then
        MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("naming");
        assertNotNull(metricRegistry.getTimers().get("java.sql.Connection"));
        assertThat(connection.getClass().getName().toLowerCase(), not(containsString("cglib")));
    }

    @Test
    public void testProxyFactory() throws SQLException {
        // When
        Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_registry=proxy;metrics_proxy_factory=cglib", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        // Then
        assertNotNull(SharedMetricRegistries.getOrCreate("proxy").getTimers().get("java.sql.Connection"));
        assertThat(connection.getClass().getName().toLowerCase(), containsString("cglib"));
    }

    @Test
    public void testDatabase() throws SQLException {
        // When
        Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_registry=db;metrics_database=driver", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        // Then
        assertNotNull(connection);
        assertNotNull(SharedMetricRegistries.getOrCreate("db").getTimers().get("java.sql.Connection.driver"));
    }
}

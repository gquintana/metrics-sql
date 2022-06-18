package com.github.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverPropertiesTest {


	@Test
	public void testNamingStrategy() throws SQLException {
		// When
		Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_naming_strategy=default;metrics_registry=naming", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
		// Then
		MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("naming");
		assertThat(metricRegistry.getTimers().get("java.sql.Connection")).isNotNull();
		assertThat(connection.getClass().getName().toLowerCase()).doesNotContain("cglib");
	}

	@Test
	public void testProxyFactory() throws SQLException {
		// When
		Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_registry=proxy;metrics_proxy_factory=cglib", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
		// Then
		assertThat(SharedMetricRegistries.getOrCreate("proxy").getTimers().get("java.sql.Connection")).isNotNull();
		assertThat(connection.getClass().getName().toLowerCase()).contains("cglib");
	}

	@Test
	public void testDatabase() throws SQLException {
		// When
		Connection connection = DriverManager.getConnection(DriverTest.URL + ";metrics_registry=db;metrics_database=driver", H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
		// Then
		assertThat(connection).isNotNull();
		assertThat(SharedMetricRegistries.getOrCreate("db").getTimers().get("java.sql.Connection.driver")).isNotNull();
	}
}

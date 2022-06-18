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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class FilteringMetricNamingStrategyTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @BeforeEach
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry, new FilteringMetricNamingStrategy());
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource(rawDataSource);
    }
    @AfterEach
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testPreparedStatement() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery();        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(Proxy.isProxyClass(statement.getClass())).isTrue();
        final SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertThat(timers.get("java.sql.Connection")).isNull();
        assertThat(timers.get("java.sql.PreparedStatement.[select * from metrics_test]")).isNull();
        assertThat(timers.get("java.sql.PreparedStatement.[select * from metrics_test].exec")).isNotNull();
        assertThat(timers.get("java.sql.ResultSet.[select * from metrics_test]")).isNotNull();
    }
    @Test
    public void testCallableStatement() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery();        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(Proxy.isProxyClass(statement.getClass())).isTrue();
        final SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertThat(timers.get("java.sql.Connection")).isNull();
        assertThat(timers.get("java.sql.CallableStatement.[select * from metrics_test]")).isNull();
        assertThat(timers.get("java.sql.CallableStatement.[select * from metrics_test].exec")).isNotNull();
        assertThat(timers.get("java.sql.ResultSet.[select * from metrics_test]")).isNotNull();
    }
}

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

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.sql.DataSource;

import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test Statement wrapper
 */
public class ResultSetTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
        rawDataSource = H2DbUtil.createDataSource();
        try (Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource("test", rawDataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }

    @Test
    public void testResultSetLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST");
        while (resultSet.next()) {
            int id = resultSet.getInt("ID");
            String text = resultSet.getString("TEXT");
            Timestamp timestamp = resultSet.getTimestamp("CREATED");
        }
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        Timer timer = metricRegistry.getTimers().get("java.sql.ResultSet.test.[select * from metrics_test]");
        assertNotNull(timer);
        assertEquals(1L, timer.getCount());
    }

    @Test
    public void testGetResultSetLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeQuery("select * from METRICS_TEST");
        ResultSet resultSet = statement.getResultSet();
        while (resultSet.next()) {
            int id = resultSet.getInt("ID");
            String text = resultSet.getString("TEXT");
            Timestamp timestamp = resultSet.getTimestamp("CREATED");
        }
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        Timer timer = metricRegistry.getTimers().get("java.sql.ResultSet.test.[select * from metrics_test]");
        assertNotNull(timer);
        assertEquals(1L, timer.getCount());
    }

    @Test
    public void testResultSetUnwrap() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST");

        // Assert
        assertTrue(resultSet.isWrapperFor(org.h2.jdbc.JdbcResultSet.class));
        assertTrue(resultSet.unwrap(org.h2.jdbc.JdbcResultSet.class) instanceof org.h2.jdbc.JdbcResultSet);

        H2DbUtil.close(resultSet, statement, connection);
    }
}

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.*;

import static org.junit.Assert.*;

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
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource(rawDataSource);
    }
    @After
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
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
        while(resultSet.next()) {
            int id = resultSet.getInt("ID");
            String text = resultSet.getString("TEXT");
            Timestamp timestamp = resultSet.getTimestamp("CREATED");
        }
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        Timer timer = metricRegistry.getTimers().get("java.sql.ResultSet.[select * from metrics_test]");
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

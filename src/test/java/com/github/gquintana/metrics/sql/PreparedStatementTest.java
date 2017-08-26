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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Test Statement wrapper
 */
public class PreparedStatementTest {
    private MetricRegistry metricRegistry;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = MetricsSql.forRegistry(metricRegistry).wrap(rawDataSource);
    }
    @After
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testPreparedStatementLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from METRICS_TEST");
        H2DbUtil.close(statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.PreparedStatement.[select * from metrics_test]"));
        
    }
    @Test
    public void testPreparedStatementExec() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery();        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.PreparedStatement.[select * from metrics_test].exec"));
        
    }
    @Test(expected = Exception.class)
    public void testPreparedStatementExecSide() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST order by CREATED desc");        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.PreparedStatement.[select * from metrics_test order by created desc].exec"));
        
    }

    @Test
    public void testPreparedStatementExec_Direct() throws SQLException {
        // Act
        Connection connection = rawDataSource.getConnection();
        String sql = "select * from METRICS_TEST order by ID";
        PreparedStatement statement = MetricsSql.forRegistry(metricRegistry).wrap(connection.prepareStatement(sql), sql);
        ResultSet resultSet = statement.executeQuery();
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertEquals(1, metricRegistry.getTimers().get("java.sql.PreparedStatement.[select * from metrics_test order by id].exec").getCount());
    }

}

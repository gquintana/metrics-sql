package net.gquintana.metrics.sql;

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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 */
public class FilteringMetricNamingStrategyTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(new FilteringMetricNamingStrategy(metricRegistry));
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource("test", rawDataSource);
    }
    @After
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
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        final SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertNull(timers.get("java.sql.Connection.test"));
        assertNull(timers.get("java.sql.PreparedStatement.test.[select * from metrics_test]"));
        assertNotNull(timers.get("java.sql.PreparedStatement.test.[select * from metrics_test].exec"));        
        assertNotNull(timers.get("java.sql.ResultSet.test.[select * from metrics_test]"));        
    }
    @Test
    public void testCallableStatement() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery();        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        final SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertNull(timers.get("java.sql.Connection.test"));
        assertNull(timers.get("java.sql.CallableStatement.test.[select * from metrics_test]"));
        assertNotNull(timers.get("java.sql.CallableStatement.test.[select * from metrics_test].exec"));        
        assertNotNull(timers.get("java.sql.ResultSet.test.[select * from metrics_test]"));        
    }
}

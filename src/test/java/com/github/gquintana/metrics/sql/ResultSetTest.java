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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Statement wrapper
 */
public class ResultSetTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @BeforeEach
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
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
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(resultSet.getClass())).isTrue();
        Timer timer = metricRegistry.getTimers().get("java.sql.ResultSet.[select * from metrics_test]");
        assertThat(timer).isNotNull();
        assertThat(timer.getCount()).isEqualTo(1L);
        Meter meter = metricRegistry.meter("java.sql.ResultSet.[select * from metrics_test].rows");
        assertThat(meter).isNotNull();
        assertThat(meter.getCount()).isEqualTo(11L);
    }
    @Test
    public void testResultSetUnwrap() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST");
        
        // Assert
        assertThat(resultSet.isWrapperFor(org.h2.jdbc.JdbcResultSet.class)).isTrue();
        assertThat(resultSet.unwrap(org.h2.jdbc.JdbcResultSet.class) instanceof org.h2.jdbc.JdbcResultSet).isTrue();
        
        H2DbUtil.close(resultSet, statement, connection);
    }


    @Test
    public void testResultSet_Direct() throws SQLException {
        // Act
        Connection connection = rawDataSource.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from METRICS_TEST order by ID";
        ResultSet resultSet = MetricsSql.forRegistry(metricRegistry).wrap(statement.executeQuery(sql), sql);

        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(resultSet.getClass())).isTrue();
        assertThat(metricRegistry.getTimers().get("java.sql.ResultSet.[select * from metrics_test order by id]").getCount()).isEqualTo(1);
    }

}

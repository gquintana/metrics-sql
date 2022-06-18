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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Test Statement wrapper
 */
public class StatementTest {
    private MetricRegistry metricRegistry;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @BeforeEach
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = MetricsSql.forRegistry(metricRegistry).wrap(rawDataSource);
    }
    @AfterEach
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testStatementLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        H2DbUtil.close(statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(statement.getClass())).isTrue();
        assertThat(metricRegistry.getTimers().get("java.sql.Statement")).isNotNull();
        
    }
    @Test
    public void testStatementExec() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST");
        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(resultSet.getClass())).isTrue();
        assertThat(metricRegistry.getTimers().get("java.sql.Statement.[select * from metrics_test].exec").getCount()).isEqualTo(1);
        
    }

    @Test
    public void testStatementExec_Direct() throws SQLException {
        // Act
        Connection connection = rawDataSource.getConnection();
        Statement statement = MetricsSql.forRegistry(metricRegistry).wrap(connection.createStatement());
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST order by ID");

        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(Proxy.isProxyClass(resultSet.getClass())).isTrue();
        assertThat(metricRegistry.getTimers().get("java.sql.Statement.[select * from metrics_test order by id].exec").getCount()).isEqualTo(1);
    }

    @Test
    public void testStatementExec_SQLException() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery("select * from UNKNOWN_TABLE");
            fail("SQL Exception expected");
        } catch (SQLException sQLException) {
        }
        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertThat(metricRegistry.getTimers().get("java.sql.Statement.[select * from unknown_table].exec").getCount()).isEqualTo(0);
        
    }
}

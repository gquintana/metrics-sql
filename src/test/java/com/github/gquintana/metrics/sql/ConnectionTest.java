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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test connection wrapper
 */
public class ConnectionTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    @BeforeEach
    public void setUp() {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection(H2DbUtil.openConnection());
        H2DbUtil.close(connection);
        // Assert
        assertThat(connection).isNotNull();
        assertThat(metricRegistry.getTimers().get("java.sql.Connection")).isNotNull();
        
    }
    @Test
    public void testConnectionStatement() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection(H2DbUtil.openConnection());
        Statement statement = connection.createStatement();
        PreparedStatement preparedStatement = connection.prepareCall("select 1 from dual");
        H2DbUtil.close(preparedStatement, statement, connection);
        // Assert
        assertThat(Proxy.isProxyClass(statement.getClass())).isTrue();
        assertThat(Proxy.isProxyClass(preparedStatement.getClass())).isTrue();
    }
    @Test
    public void testConnectionNotTimed() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection(H2DbUtil.openConnection());
        String dbProduct = connection.getMetaData().getDatabaseProductName();
        // Assert
        assertThat(dbProduct.toLowerCase().contains("h2")).isTrue();
    }
        @Test
    public void testResultSetUnwrap() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection(H2DbUtil.openConnection());
        
        // Assert        
        assertThat(connection.isWrapperFor(org.h2.jdbc.JdbcConnection.class)).isTrue();
        assertThat(connection.unwrap(org.h2.jdbc.JdbcConnection.class) instanceof org.h2.jdbc.JdbcConnection).isTrue();
        
        H2DbUtil.close(connection);
    }

}

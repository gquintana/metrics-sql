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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test DataSource wrapper
 */
public class DataSourceTest {
    private MetricRegistry metricRegistry;
    private DataSource dataSource;
    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        dataSource = MetricsSql.forRegistry(metricRegistry)
                .wrap(H2DbUtil.createDataSource());
    }
    @After
    public void tearDown() {
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        H2DbUtil.close(connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(connection.getClass()));
        Timer lifeTimer = metricRegistry.timer("java.sql.Connection");
        assertNotNull(lifeTimer);
        assertThat(lifeTimer.getCount(), equalTo(1L));
        Timer getTimer = metricRegistry.timer("java.sql.Connection.get");
        assertNotNull(getTimer);
        assertThat(getTimer.getCount(), equalTo(1L));
    }
    @Test
    public void testConnectionStatement() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        PreparedStatement preparedStatement = connection.prepareCall("select 1 from dual");
        H2DbUtil.close(preparedStatement, statement, connection);
        // Assert
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        assertTrue(Proxy.isProxyClass(preparedStatement.getClass()));
    }
}

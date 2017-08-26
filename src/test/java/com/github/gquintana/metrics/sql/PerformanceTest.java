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
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.AbstractProxyFactory;
import com.github.gquintana.metrics.proxy.CGLibProxyFactory;
import com.github.gquintana.metrics.proxy.CachingProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import com.github.gquintana.metrics.util.ParametersBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;

/**
 * Performance test
 */
@RunWith(Parameterized.class)
public class PerformanceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTest.class);
    private final String name;
    private final AbstractProxyFactory factory;
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;

    public PerformanceTest(String name, AbstractProxyFactory factory) {
        this.name = name;
        this.factory = factory;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return new ParametersBuilder()
                .add("raw", null)
                .add("reflect", new ReflectProxyFactory())
                .add("cglib", new CGLibProxyFactory())
                .add("caching",new CachingProxyFactory())
                .add("raw", null)
                .build();
    }
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        if (factory==null) {
            dataSource = rawDataSource;
        } else {
            proxyFactory = MetricsSql.forRegistry(metricRegistry)
                    .withProxyFactory(factory).build();
            dataSource = proxyFactory.wrapDataSource(rawDataSource);
        }
    }
    @After
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testPerformance() throws SQLException {
        Timer timer = metricRegistry.timer(MetricRegistry.name(getClass(), name));
        final int iterations = 100, inserts=10; // Increase interations
        for(int i=0;i<iterations;i++) { final
            Timer.Context context = timer.time();
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("insert into METRICS_TEST(ID, TEXT, CREATED) values (?,?,?)");
            for(int j=0;j<inserts;j++) {
                preparedStatement.setInt(1, i*inserts+j+100);
                preparedStatement.setString(2, "Performance #"+i*10+j);
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.execute();
            }
            H2DbUtil.close(preparedStatement);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from METRICS_TEST");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
            }
            H2DbUtil.close(resultSet);
            resultSet = statement.executeQuery("select * from METRICS_TEST order by ID desc limit 100");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String text = resultSet.getString("text");
                Timestamp timestamp = resultSet.getTimestamp("created");
            }
            H2DbUtil.close(resultSet, statement, connection);
            context.stop();
        }        
        final Snapshot snapshot = timer.getSnapshot();
        LOGGER.info("End name={} 98%={}, 50%={}", name, snapshot.get98thPercentile(), snapshot.getMean());
    }
}

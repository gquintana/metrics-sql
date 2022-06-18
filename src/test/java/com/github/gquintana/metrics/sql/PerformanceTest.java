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
import com.github.gquintana.metrics.proxy.CGLibProxyFactory;
import com.github.gquintana.metrics.proxy.CachingProxyFactory;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.stream.Stream;

/**
 * Performance test
 */
public class PerformanceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTest.class);
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;

    public static Stream<Arguments> getParameters() {
        return Stream.of(
                Arguments.of("raw", null),
                Arguments.of("reflect", new ReflectProxyFactory()),
                Arguments.of("cglib", new CGLibProxyFactory()),
                Arguments.of("caching",new CachingProxyFactory()),
                Arguments.of("raw", null)
                );
    }
    @BeforeEach
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
    }
    @AfterEach
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @ParameterizedTest
	@MethodSource("getParameters")
    public void testPerformance(String name, ProxyFactory factory) throws SQLException {
		if (factory==null) {
			dataSource = rawDataSource;
		} else {
			proxyFactory = MetricsSql.forRegistry(metricRegistry)
					.withProxyFactory(factory).build();
			dataSource = proxyFactory.wrapDataSource(rawDataSource);
		}
        Timer timer = metricRegistry.timer(MetricRegistry.name(getClass(), name));
        final int iterations = 100, inserts=10; // Increase iterations
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

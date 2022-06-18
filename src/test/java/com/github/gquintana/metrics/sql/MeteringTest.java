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
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Metering test
 */
public class MeteringTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeteringTest.class);
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    private Slf4jReporter metricsReporter;

    @BeforeEach
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        metricsReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LOGGER)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .build();
        rawDataSource = H2DbUtil.createDataSource();
        try (Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        proxyFactory = MetricsSql.forRegistry(metricRegistry)
                .withProxyFactory(new ReflectProxyFactory()).build();
        dataSource = proxyFactory.wrapDataSource(rawDataSource);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }

    @Test
    public void testVolume() throws SQLException {
        final int iterations = 100, inserts = 100, textSize=10; // Increase iterations
        for (int i = 0; i < iterations; i++) {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("insert into METRICS_TEST(ID, TEXT, CREATED) values (?,?,?)");
            for (int j = 0; j < inserts; j++) {
                preparedStatement.setInt(1, i * inserts + j + 100);
                preparedStatement.setString(2, randomString(textSize));
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.execute();
            }
            H2DbUtil.close(preparedStatement);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from METRICS_TEST");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                assertThat(count).isGreaterThanOrEqualTo(i*inserts);
            }
            H2DbUtil.close(resultSet);

            resultSet = statement.executeQuery("select * from METRICS_TEST order by ID asc");
            readWholeResultSet(resultSet);
            H2DbUtil.close(resultSet, statement);

            preparedStatement = connection.prepareStatement("select * from METRICS_TEST where TEXT=? order by ID asc");
            preparedStatement.setString(1, randomString(textSize));
            resultSet = preparedStatement.executeQuery();
            readWholeResultSet(resultSet);
            H2DbUtil.close(resultSet, preparedStatement, connection);
        }

        metricsReporter.report(metricRegistry.getGauges(), metricRegistry.getCounters(), metricRegistry.getHistograms(), metricRegistry.getMeters(), metricRegistry.getTimers());
        assertThat(metricRegistry.getTimers().size()).isEqualTo(
                2 // connection
                +2 // inserts
                +5 // statements
                +3 // prepared statement
                );

        // connection
        Timer timer = metricRegistry.timer("java.sql.Connection");
        assertThat(timer.getCount()).isEqualTo(iterations);
        timer = metricRegistry.timer("java.sql.Connection.get");
        assertThat(timer.getCount()).isEqualTo(iterations);

        // statement
        timer = metricRegistry.timer("java.sql.Statement");
        assertThat(timer.getCount()).isEqualTo(iterations);
        timer = metricRegistry.timer("java.sql.Statement.[select count(*) from metrics_test].exec");
        assertThat(timer.getCount()).isEqualTo(iterations);
        timer = metricRegistry.timer("java.sql.Statement.[select * from metrics_test order by id asc].exec");
        assertThat(timer.getCount()).isEqualTo(iterations);

        // prepared statement
        timer = metricRegistry.timer("java.sql.PreparedStatement.[insert into metrics_test(id, text, created) values (?,?,?)].exec");
        assertThat(timer.getCount()).isEqualTo(iterations * inserts);

        timer = metricRegistry.timer("java.sql.PreparedStatement.[insert into metrics_test(id, text, created) values (?,?,?)]");
        assertThat(timer.getCount()).isEqualTo(iterations);

        timer = metricRegistry.timer("java.sql.PreparedStatement.[select * from metrics_test where text=? order by id asc].exec");
        assertThat(timer.getCount()).isEqualTo(iterations);
        Snapshot timerSnapshot = timer.getSnapshot();
        double preparedStatementExecMean = timerSnapshot.getMean();
        assertThat(preparedStatementExecMean).isGreaterThan(0.0);
        assertThat(timerSnapshot.getMax()).isGreaterThan(0L);
        assertThat(timerSnapshot.getMax()).isGreaterThan(timerSnapshot.getMin());

        timer = metricRegistry.timer("java.sql.PreparedStatement.[select * from metrics_test where text=? order by id asc]");
        assertThat(timer.getCount()).isEqualTo(iterations);
        timerSnapshot = timer.getSnapshot();
        assertThat(timerSnapshot.getMean()).isGreaterThan(preparedStatementExecMean);

    }

    private void readWholeResultSet(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String text = resultSet.getString("text");
            Timestamp timestamp = resultSet.getTimestamp("created");
        }
    }

    private final Random random = new Random();
    private String randomString(int size) {
        return Integer.toHexString(random.nextInt(size));
    }
}

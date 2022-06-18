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

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.management.MBeanServer;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Test the integration between Metrics SQL and the CSV Reporter
 */
public class CsvReportingTest {
    private MBeanServer mBeanServer;
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    private CsvReporter csvReporter;
    @TempDir
    public Path tmpFolder;
    private Path csvFolder;
    @BeforeEach
    public void setUp() throws SQLException, IOException {
        csvFolder = tmpFolder.resolve("csv");
		Files.createDirectories(csvFolder);
        mBeanServer=ManagementFactory.getPlatformMBeanServer();
        metricRegistry = new MetricRegistry();
        csvReporter = CsvReporter.forRegistry(metricRegistry)
                .build(csvFolder.toFile());
        proxyFactory = new JdbcProxyFactory(metricRegistry, new DefaultMetricNamingStrategy("csv"));
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
    public void testJmxReporting() throws SQLException, IOException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("insert into METRICS_TEST(ID, TEXT, CREATED) values (?,?,?)");
        preparedStatement.setInt(1, 1000);
        preparedStatement.setString(2, "JMX");
        preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        preparedStatement.execute();
        Statement statement = connection.createStatement();
        statement.executeQuery("select count(*) from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST order by ID desc limit 20");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String text = resultSet.getString("text");
            Timestamp timestamp = resultSet.getTimestamp("created");
        }
        H2DbUtil.close(resultSet, statement, preparedStatement, connection);
        final SortedMap<String, Timer> timers = metricRegistry.getTimers();
        csvReporter.report(metricRegistry.getGauges(), metricRegistry.getCounters(), metricRegistry.getHistograms(), metricRegistry.getMeters(), timers);
        // Check file
        Set<Path> csvFiles = Files.list(csvFolder).filter(Files::isRegularFile).collect(Collectors.toSet());
        if (csvFiles.isEmpty()) {
            fail("CSV not generated");
        }
        for(String timerName:timers.keySet()) {
            assertThat(csvFiles).contains(csvFolder.resolve(timerName+".csv"));
        }
    }
}

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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.management.MBeanServer;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    @Rule
    public TemporaryFolder tmpFolderRule = new TemporaryFolder();
    private File csvFolder;
    @Before
    public void setUp() throws SQLException, IOException {
        csvFolder = tmpFolderRule.newFolder("csv");
        mBeanServer=ManagementFactory.getPlatformMBeanServer();
        metricRegistry = new MetricRegistry();
        csvReporter = CsvReporter.forRegistry(metricRegistry)
                .build(csvFolder);
        proxyFactory = new JdbcProxyFactory(metricRegistry);
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
    public void testJmxReporting() throws SQLException {
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
        File[] csvFiles = csvFolder.listFiles();
        if (csvFiles==null || csvFiles.length==0) {
            fail("CSV not generated");
        }
        Set<File> csvFilesSet = new HashSet<>(Arrays.asList(csvFiles));
        for(String timerName:timers.keySet()) {
            assertTrue(csvFilesSet.contains(new File(csvFolder, timerName+".csv")));
        }
    }
}

/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import java.lang.management.ManagementFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;
import javax.management.MBeanServer;
import net.gquintana.metrics.util.SqlObjectNameFactory;

/**
 * Performance test
 */
public class JmxReportingTest {
    private MBeanServer mBeanServer;
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    private JmxReporter jmxReporter;

    @Before
    public void setUp() throws SQLException {
        mBeanServer=ManagementFactory.getPlatformMBeanServer();
        metricRegistry = new MetricRegistry();
        jmxReporter = JmxReporter.forRegistry(metricRegistry)
                .registerWith(mBeanServer)
                .createsObjectNamesWith(new SqlObjectNameFactory())
                .build();
        jmxReporter.start();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource("test", rawDataSource);
    }
    @After
    public void tearDown() throws SQLException {
        jmxReporter.stop();
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
    }
}

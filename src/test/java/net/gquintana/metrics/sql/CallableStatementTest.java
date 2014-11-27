/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Statement wrapper
 */
public class CallableStatementTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
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
    public void testCallableStatementLife() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall("select * from METRICS_TEST");
        H2DbUtil.close(statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.CallableStatement.test.[select * from metrics_test]"));
        
    }
    @Test
    public void testCallableStatementExec() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery();        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.CallableStatement.test.[select * from metrics_test].exec"));
        
    }
    @Test(expected = Exception.class)
    public void testCallableStatementExecSide() throws SQLException {
        // Act
        Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall("select * from METRICS_TEST");
        ResultSet resultSet = statement.executeQuery("select * from METRICS_TEST order by CREATED desc");        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.CallableStatement.test.[select * from metrics_test order by created desc].exec"));
        
    }
}

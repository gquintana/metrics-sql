/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DataSource wrapper
 */
public class DataSourceTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource dataSource;
    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
        dataSource = proxyFactory.wrapDataSource("test", H2DbUtil.createDataSource());
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
        assertNotNull(metricRegistry.getTimers().get("java.sql.Connection.test"));
        
    }
    @Test
    public void testConnectionStatement() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection("test", H2DbUtil.openConnection());
        Statement statement = connection.createStatement();
        PreparedStatement preparedStatement = connection.prepareCall("select 1 from dual");
        H2DbUtil.close(preparedStatement, statement, connection);
        // Assert
        assertTrue(Proxy.isProxyClass(statement.getClass()));
        assertTrue(Proxy.isProxyClass(preparedStatement.getClass()));
    }
}

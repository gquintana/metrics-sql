/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
/**
 * Test connection wrapper
 */
public class ConnectionTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        proxyFactory = new JdbcProxyFactory(metricRegistry);
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection("test", H2DbUtil.openConnection());
        H2DbUtil.close(connection);
        // Assert
        assertNotNull(connection);
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
    @Test
    public void testConnectionNotTimed() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection("test", H2DbUtil.openConnection());
        String dbProduct = connection.getMetaData().getDatabaseProductName();
        // Assert
        assertTrue(dbProduct.toLowerCase().contains("h2"));
    }
        @Test
    public void testResultSetUnwrap() throws SQLException {
        // Act
        Connection connection = proxyFactory.wrapConnection("test", H2DbUtil.openConnection());
        
        // Assert        
        assertTrue(connection.isWrapperFor(org.h2.jdbc.JdbcConnection.class));
        assertTrue(connection.unwrap(org.h2.jdbc.JdbcConnection.class) instanceof org.h2.jdbc.JdbcConnection);
        
        H2DbUtil.close(connection);
    }

}

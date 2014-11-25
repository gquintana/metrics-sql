/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.gquintana.metrics.util.StaticMetricRegistryHolder;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Statement wrapper
 */
public class DriverTest {
    private MetricRegistry metricRegistry;
    private static final String URL = H2DbUtil.URL.replaceFirst("jdbc:h2","jdbc:metrics:h2");
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        StaticMetricRegistryHolder.setMetricRegistry(metricRegistry);
        // Load drivers
        List<java.sql.Driver> drivers = Arrays.asList(new Driver(),new org.h2.Driver());
    }
    @Test
    public void testConnectionLife() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL, H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        H2DbUtil.close(statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(connection.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.Connection.h2_driver"));
        
    }
    @Test
    public void testStatementExec() throws SQLException {
        // Act
        Connection connection = DriverManager.getConnection(URL, H2DbUtil.USERNAME, H2DbUtil.PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select CURRENT_DATE");
        
        H2DbUtil.close(resultSet, statement, connection);
        // Assert
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(resultSet.getClass()));
        assertNotNull(metricRegistry.getTimers().get("java.sql.Statement.h2_driver.[select current_date].exec"));
        
    }
    
    @Test
    public void testGetInfo() throws SQLException {
        // Act
        java.sql.Driver driver = DriverManager.getDriver(URL);
        // Assert
        assertTrue(driver instanceof Driver);
        assertEquals(3, driver.getMajorVersion());
        assertEquals(1, driver.getMinorVersion());
        Properties properties = new Properties();
        DriverPropertyInfo[] propertyInfos=driver.getPropertyInfo(URL, properties);
        assertNotNull(propertyInfos);
        String prefix = driver.getParentLogger().getName();
        assertTrue(getClass().getName().startsWith(prefix));
        assertTrue(driver.jdbcCompliant());
    }
}

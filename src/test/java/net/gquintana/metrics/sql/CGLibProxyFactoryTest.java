/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import java.sql.Connection;
import java.sql.SQLException;
import net.gquintana.metrics.proxy.CGLibProxyFactory;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class CGLibProxyFactoryTest {
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        CGLibProxyFactory factory = new CGLibProxyFactory();
        proxyFactory = new JdbcProxyFactory(new DefaultMetricNamingStrategy(metricRegistry), factory);
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
   
}

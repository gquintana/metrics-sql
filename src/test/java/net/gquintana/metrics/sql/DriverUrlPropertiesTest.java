/*
 * Default License
 */
package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import net.gquintana.metrics.proxy.CGLibProxyFactory;
import net.gquintana.metrics.proxy.ReflectProxyFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class DriverUrlPropertiesTest {
    public static class CustomMetricNamingStrategy extends DefaultMetricNamingStrategy {

        public CustomMetricNamingStrategy(MetricRegistry metricRegistry) {
            super(metricRegistry);
        }
        
    }
    @Test
    public void testProperties() {
        DriverUrl driverUrl = DriverUrl.parse("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_driver=org.h2.Driver;metrics_proxy_factory=cglib;metrics_naming_strategy="+CustomMetricNamingStrategy.class.getName()+";metrics_name=test");
        assertEquals(CGLibProxyFactory.class, driverUrl.getProxyFactoryClass());
        assertEquals(CustomMetricNamingStrategy.class, driverUrl.getNamingStrategyClass());
        assertEquals("test", driverUrl.getName());
        assertEquals(org.h2.Driver.class, driverUrl.getDriverClass());
    }
    @Test
    public void testPropertiesDefault() {
        DriverUrl driverUrl = DriverUrl.parse("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE");
        assertEquals(ReflectProxyFactory.class, driverUrl.getProxyFactoryClass());
        assertEquals(DefaultMetricNamingStrategy.class, driverUrl.getNamingStrategyClass());
        assertEquals("h2_driver", driverUrl.getName());
        assertNull(null, driverUrl.getDriverClass());
    }
}

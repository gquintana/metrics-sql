/*
 * Default License
 */

package net.gquintana.metrics.sql;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import net.gquintana.metrics.proxy.ProxyFactory;
import net.gquintana.metrics.util.MetricRegistryHolder;

/**
 * Metrics SQL JDBC Driver
 */
public class Driver implements java.sql.Driver {    
    private static final Driver INSTANCE = new Driver();
    private static boolean registered = false;
    private final Logger parentLogger = Logger.getLogger("net.gquintana.metrics");
    static {
        register();
    }
    private static synchronized void register() {
        try {
            if (!registered) {
                registered = true;
                DriverManager.registerDriver(INSTANCE);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
    private static <T> T newInstance(Class<T> clazz, Object ... params) throws SQLException {
        try {
            if (params == null || params.length==0) {
                return clazz.newInstance();
            } else {
                for(Constructor<?> ctor: clazz.getConstructors()) {
                    if (ctor.getParameterTypes().length==params.length) {
                        int paramIndex=0;
                        for(Class<?> paramType:ctor.getParameterTypes()) {
                            if (!paramType.isInstance(params[paramIndex])) {
                                break;
                            }
                            paramIndex++;
                        }
                        if (paramIndex==params.length) {
                            return clazz.cast(ctor.newInstance(params));
                        }
                    }
                }
                throw new SQLException("Constructor not found for "+clazz);
            }
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new SQLException(reflectiveOperationException);
        }
    }
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        DriverUrl driverUrl = DriverUrl.parse(url);
        // Force Driver loading
        Class<? extends Driver> driverClass = driverUrl.getDriverClass();
        // Open connection
        Connection rawConnection=DriverManager.getConnection(driverUrl.getCleanUrl(), info);
        // Wrap connection
        ProxyFactory factory = newInstance(driverUrl.getProxyFactoryClass());
        MetricRegistryHolder registryHolder = newInstance(driverUrl.getRegistryHolderClass());
        MetricNamingStrategy namingStrategy = newInstance(driverUrl.getNamingStrategyClass(), registryHolder);
        JdbcProxyFactory proxyFactory = new JdbcProxyFactory(namingStrategy, factory);
        return proxyFactory.wrapConnection(driverUrl.getName(), rawConnection);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(DriverUrl.URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        DriverUrl driverUrl = DriverUrl.parse(url);
        java.sql.Driver driver = DriverManager.getDriver(driverUrl.getCleanUrl());
        return driver.getPropertyInfo(driverUrl.getCleanUrl(), info);
    }

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return parentLogger;
    }
    
    
}

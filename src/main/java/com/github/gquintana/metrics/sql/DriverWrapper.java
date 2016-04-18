package com.github.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.util.MetricRegistryHolder;
import com.github.gquintana.metrics.util.ReflectionUtil;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created on 4/17/16.
 */
public class DriverWrapper implements java.sql.Driver {


    private final Logger parentLogger = Logger.getLogger("com.github.gquintana.metrics");

    private java.sql.Driver realDriver;
    private DriverUrl driverUrl;
    private JdbcProxyFactory jdbcProxyFactory;

    private java.sql.Driver getDriver(String url) throws SQLException {
        if (realDriver == null) {
            try {
                Class<? extends Driver> driverClass = getDriverUrl(url).getDriverClass();
                realDriver = driverClass.newInstance();
            } catch (Throwable e) {
                throw new SQLException(e);
            }
        }

        return realDriver;
    }


    private DriverUrl getDriverUrl(String url) {
        if (driverUrl == null) {
            driverUrl = DriverUrl.parse(url);
        }
        return driverUrl;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        JdbcProxyFactory jdbcProxyFactory = getJdbcProxyFactory(url);
        Timer.Context context = jdbcProxyFactory.startDriverConnectTimer(getDriverUrl(url).getName());
        Connection rawConnection = getDriver(url).connect(getDriverUrl(url).getCleanUrl(), info);
        context.close();

        return jdbcProxyFactory.wrapConnection(getDriverUrl(url).getName(), rawConnection);
    }



    private JdbcProxyFactory getJdbcProxyFactory(String url) throws SQLException {

        //cache those instead of reflecting for every connect method call
        if (jdbcProxyFactory == null) {
            ProxyFactory factory = ReflectionUtil.newInstance(getDriverUrl(url).getProxyFactoryClass());
            MetricRegistryHolder registryHolder = ReflectionUtil.newInstance(getDriverUrl(url).getRegistryHolderClass());
            MetricNamingStrategy namingStrategy = ReflectionUtil.newInstance(getDriverUrl(url).getNamingStrategyClass(), registryHolder);
            jdbcProxyFactory = ReflectionUtil.newInstance(getDriverUrl(url).getJdbcProxyFactoryClass(), namingStrategy, factory);
        }
        return jdbcProxyFactory;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        java.sql.Driver driver = getDriver(url);
        return url != null &&
                url.startsWith(DriverUrl.URL_PREFIX) &&
                driver.acceptsURL(driverUrl.getCleanUrl());
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        java.sql.Driver driver = getDriver(url);
        return driver.getPropertyInfo(driverUrl.getCleanUrl(), info);
    }


    @Override
    public int getMajorVersion() {
        if (realDriver != null) {
            return realDriver.getMajorVersion();
        }
        return 3;
    }

    @Override
    public int getMinorVersion() {
        if (realDriver != null) {
            return realDriver.getMinorVersion();
        }
        return 1;
    }

    @Override
    public boolean jdbcCompliant() {
        if (realDriver != null) {
            return realDriver.jdbcCompliant();
        }
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (realDriver != null) {
            return realDriver.getParentLogger();
        }
        return parentLogger;
    }


}

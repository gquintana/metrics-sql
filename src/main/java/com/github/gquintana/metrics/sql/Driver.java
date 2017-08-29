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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.ProxyFactory;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Metrics SQL JDBC Driver
 */
public class Driver implements java.sql.Driver {
    private static final Driver INSTANCE = new Driver();
    private static boolean registered = false;
    private final Logger parentLogger = Logger.getLogger("com.github.gquintana.metrics");

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

    /**
     * Instantiate a new object of type T
     *
     * @param clazz  Object class
     * @param params Constructor args
     * @param <T>    Object type
     * @return New object
     */
    private static <T> T newInstance(Class<T> clazz, Object... params) throws SQLException {
        try {
            if (params == null || params.length == 0) {
                return clazz.newInstance();
            } else {
                for (Constructor<?> ctor : clazz.getConstructors()) {
                    if (ctor.getParameterTypes().length != params.length) {
                        continue;
                    }
                    int paramIndex = 0;
                    for (Class<?> paramType : ctor.getParameterTypes()) {
                        if (!paramType.isInstance(params[paramIndex])) {
                            break;
                        }
                        paramIndex++;
                    }
                    if (paramIndex != params.length) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Constructor<T> theCtor = (Constructor<T>) ctor;
                    return theCtor.newInstance(params);
                }
                throw new SQLException("Constructor not found for " + clazz);
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
        MetricRegistry registry = getMetricRegistry(driverUrl);
        ProxyFactory factory = newInstance(driverUrl.getProxyFactoryClass());
        MetricNamingStrategy namingStrategy = getMetricNamingStrategy(driverUrl);
        JdbcProxyFactory proxyFactory = new JdbcProxyFactory(registry, namingStrategy, factory);
        // Force Driver loading
        Class<? extends Driver> driverClass = driverUrl.getDriverClass();
        // Open connection
        Timer.Context getTimerContext = proxyFactory.getMetricHelper().startConnectionGetTimer();
        Connection rawConnection = DriverManager.getConnection(driverUrl.getCleanUrl(), info);
        if (getTimerContext != null) {
            getTimerContext.stop();
        }
        // Wrap connection
        return proxyFactory.wrapConnection(rawConnection);
    }

    private MetricNamingStrategy getMetricNamingStrategy(DriverUrl driverUrl) throws SQLException {
        Class<? extends MetricNamingStrategy> namingStrategyClass = driverUrl.getNamingStrategyClass();
        String databaseName = driverUrl.getDatabaseName();
        return databaseName == null ? newInstance(namingStrategyClass) : newInstance(namingStrategyClass, databaseName);
    }

    private MetricRegistry getMetricRegistry(DriverUrl driverUrl) {
        String registryName = driverUrl.getRegistryName();
        MetricRegistry registry;
        if (registryName == null) {
            registry = SharedMetricRegistries.tryGetDefault();
            if (registry == null) {
                registry = SharedMetricRegistries.getOrCreate("default");
            }
        } else {
            registry = SharedMetricRegistries.getOrCreate(registryName);
        }
        return registry;
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

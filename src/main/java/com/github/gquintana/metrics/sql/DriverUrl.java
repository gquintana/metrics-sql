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

import com.github.gquintana.metrics.proxy.CGLibProxyFactory;
import com.github.gquintana.metrics.proxy.CachingProxyFactory;
import com.github.gquintana.metrics.proxy.ProxyFactory;
import com.github.gquintana.metrics.proxy.ReflectProxyFactory;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Driver URL.
 * Parses URL and extract properties
 */
class DriverUrl {
    public static final String URL_PREFIX = "jdbc:metrics:";
    private final String rawUrl;
    private final String cleanUrl;
    private final String databaseType;
    private final Properties properties;

    public DriverUrl(String rawUrl, String cleanUrl, String databaseType, Properties properties) {
        this.rawUrl = rawUrl;
        this.cleanUrl = cleanUrl;
        this.databaseType = databaseType;
        this.properties = properties;
    }

    private static final Pattern PATTERN = Pattern.compile("^jdbc:metrics:(([\\w]+):[^?;]+)(?:([?;])(.*))?$");

    private static Properties parseProperties(String urlProps, String propSep, StringBuilder cleanUrlBuilder) {
        Properties properties = new Properties();
        boolean first = true;
        for (String sProp : urlProps.split(propSep)) {
            if (sProp.startsWith("metrics_")) {
                String[] subProp = sProp.split("=");
                properties.put(subProp[0], subProp[1]);
            } else {
                if (first) {
                    first = false;
                } else {
                    cleanUrlBuilder.append(propSep);
                }
                cleanUrlBuilder.append(sProp);
            }
        }
        return properties;
    }

    public static DriverUrl parse(String rawUrl) {
        Matcher matcher = PATTERN.matcher(rawUrl);
        StringBuilder cleanUrlBuilder = new StringBuilder("jdbc:");
        Properties properties = null;
        String dbType;
        String cleanUrl;
        if (matcher.matches()) {
            // mysql://localhost:3306/sakila
            cleanUrlBuilder.append(matcher.group(1));
            // mysql
            dbType = matcher.group(2);
            // ? or ;
            String sep = matcher.group(3);
            String sProps = matcher.group(4);
            if (sep != null && sProps != null) {
                cleanUrlBuilder.append(sep);
                if (sep.equals("?")) {
                    properties = parseProperties(sProps, "&", cleanUrlBuilder);
                } else if (sep.equals(";")) {
                    properties = parseProperties(sProps, ";", cleanUrlBuilder);
                }
                cleanUrl = cleanUrlBuilder.toString();
                if (cleanUrl.endsWith(sep)) {
                    cleanUrl = cleanUrl.substring(0, cleanUrl.length() - sep.length());
                }
            } else {
                cleanUrl = cleanUrlBuilder.toString();
            }
        } else {
            throw new IllegalArgumentException("Missing prefix " + URL_PREFIX);
        }
        return new DriverUrl(rawUrl, cleanUrl, dbType, properties);
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public String getCleanUrl() {
        return cleanUrl;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }
        String val = properties.getProperty(key);
        if (val == null) {
            return null;
        }
        return val;
    }

    public String getProperty(String key, String def) {
        String val = getProperty(key);
        if (val == null) {
            val = def;
        }
        return val;
    }

    private static <T> Class<T> toClass(String className) {
        if (className == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) Class.forName(className);
            return clazz;
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalArgumentException("Property " + className + " is not a valid class", classNotFoundException);
        }
    }

    public Class<? extends Driver> getDriverClass() {
        return toClass(getProperty("metrics_driver"));
    }

    public Class<? extends ProxyFactory> getProxyFactoryClass() {
        Class<? extends ProxyFactory> factoryClass;
        String proxyFactoryName = getProperty("metrics_proxy_factory", "reflect");
        switch (proxyFactoryName) {
            case "reflect":
                factoryClass = ReflectProxyFactory.class;
                break;
            case "cglib":
                factoryClass = CGLibProxyFactory.class;
                break;
            case "caching":
                factoryClass = CachingProxyFactory.class;
                break;
            default:
                factoryClass = toClass(proxyFactoryName);
        }
        return factoryClass;
    }

    /**
     * @return Class extending {@link MetricNamingStrategy}
     */
    public Class<? extends MetricNamingStrategy> getNamingStrategyClass() {
        Class<DefaultMetricNamingStrategy> namingStrategy;
        String namingStrategyName = getProperty("metrics_naming_strategy", "default");
        switch (namingStrategyName) {
            case "default":
                namingStrategy = DefaultMetricNamingStrategy.class;
                break;
            default:
                namingStrategy = toClass(namingStrategyName);
        }
        return namingStrategy;
    }

    /**
     * @return Shared metric registry name
     */
    public String getRegistryName() {
        return getProperty("metrics_registry", null);
    }

    /**
     * @return Connection factory name
     */
    public String getDatabaseName() {
        return getProperty("metrics_database");
    }
}

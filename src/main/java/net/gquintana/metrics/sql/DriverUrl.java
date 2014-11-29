/*
 * Default License
 */

package net.gquintana.metrics.sql;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.gquintana.metrics.proxy.CGLibProxyFactory;
import net.gquintana.metrics.proxy.AbstractProxyFactory;
import net.gquintana.metrics.proxy.CachingProxyFactory;
import net.gquintana.metrics.proxy.ReflectProxyFactory;
import net.gquintana.metrics.util.MetricRegistryHolder;
import net.gquintana.metrics.util.StaticMetricRegistryHolder;

/**
 * Driver URL.
 * Parses URL and extract properties
 */
public class DriverUrl {
    public static final String URL_PREFIX="jdbc:metrics:";
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
        boolean first=true;
        for(String sProp:urlProps.split(propSep)) {
            if (sProp.startsWith("metrics_")) {
                String[] subProp=sProp.split("=");
                properties.put(subProp[0], subProp[1]);
            } else {
                if (first) {
                    first=false;
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
        String dbType = null;
        if (matcher.matches()) {
            // mysql://localhost:3306/sakila
            cleanUrlBuilder.append(matcher.group(1));
            // mysql
            dbType = matcher.group(2);
            // ? or ;
            String sep = matcher.group(3);
            String sProps = matcher.group(4);
            if (sep!=null && sProps!=null) {
                cleanUrlBuilder.append(sep);
                if (sep.equals("?")) {
                    properties = parseProperties(sProps, "&", cleanUrlBuilder);
                } else if (sep.equals(";")) {
                    properties = parseProperties(sProps, ";", cleanUrlBuilder);
                }
            }
        } else {
            throw new IllegalArgumentException("Missing prefix "+URL_PREFIX);
        } 
        return new DriverUrl(rawUrl, cleanUrlBuilder.toString(), dbType, properties);
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
    public <T> T getProperty(String key, Class<T> type) {
        if (properties == null) {
            return null;
        }
        String sVal = properties.getProperty(key);
        if (sVal == null) {
            return null;
        } else if (type.equals(String.class)) {
            return type.cast(sVal);
        } else if (type.equals(Class.class)) {
            try {
                return type.cast(Class.forName(sVal));
            } catch (ClassNotFoundException classNotFoundException) {
                throw new IllegalArgumentException("Property "+sVal+" is not a valid class", classNotFoundException);
            }
        } else {
            throw new IllegalArgumentException("Property type "+type+" not supported");
        }
    }
    public <T> T getProperty(String key, Class<T> type, T def) {
        T val = getProperty(key, type);
        if (val == null) {
            val = def;           
        }
        return val;
    }
    public Class<? extends Driver> getDriverClass() {
        return getProperty("metrics_driver", Class.class);
    }
    public Class<? extends AbstractProxyFactory> getProxyFactoryClass() {
        Class<? extends AbstractProxyFactory> factoryClass;
        String s = getProperty("metrics_proxy_factory", String.class);
        if (s==null || s.equals("reflect")) {
            factoryClass = ReflectProxyFactory.class;
        } else if (s.equalsIgnoreCase("cglib")) {
            factoryClass = CGLibProxyFactory.class;
        } else if (s.equalsIgnoreCase("caching")) {
            factoryClass = CachingProxyFactory.class;
        } else {
            factoryClass = getProperty("metrics_proxy_factory", Class.class);
        }
        return factoryClass;
    }
    /**
     * @return Class extending {@link MetricNamingStrategy}
     */
    public Class<? extends MetricNamingStrategy> getNamingStrategyClass() {
        return getProperty("metrics_naming_strategy", Class.class, DefaultMetricNamingStrategy.class);
    }
    /**
     * @return Class extending {@link MetricNamingStrategy}
     */
    public Class<? extends MetricRegistryHolder> getRegistryHolderClass() {
        return getProperty("metrics_registry_holder", Class.class, StaticMetricRegistryHolder.class);
    }
    /**
     * @return Connection factory name
     */
    public String getName() {
        return getProperty("metrics_name", String.class, databaseType+"_driver");
    }
}

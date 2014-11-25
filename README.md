# Metrics SQL

This a Yammer|Codahale|Dropwizard Metrics extension to measure SQL execution times.

## Setup

### DataSource level

Wrap your existing DataSource using JdbcProxyFactory:

```java
    metricRegistry = new MetricRegistry();
    dataSource = new JdbcProxyFactory(metricRegistry).wrapDataSource("test", existingDataSource);
```

### Connection level

Same as DataSource
```java
    metricRegistry = new MetricRegistry();
    connection = new JdbcProxyFactory(metricRegistry).wrapDataSource("test", existingConnection);
```

### Driver level

1. Register Metrics SQL JDBC Driver
2. Change JDBC URL prefix: jdbc:xxx becomes jdbc:metrics:xxx

Example: `jdbc:metrics:mysql://localhost:3306/sakila?profileSQL=true`

The driver supports several options:

* metrics_driver: Real JDBC Driver class
* metrics_proxy_factory: Proxy factory implementation (Default: ReflectProxyFactory; Available: reflect, cglib; Possible: any class implementing ProxyFactory)
* metrics_naming_strategy: Naming strategy implementation (Default: DefaultMetricNamingStrategy, Possible: any class implementing MetricNamingStrategy)
* metrics_registry_holder: Registry holder (Default: StaticMetricRegistryHolder; Possible: any class implementing MetricRegistryHolder)
* metrics_name: Metrics name (Default: xxx_driver, Possible: any string)

## Configuration

* *Naming strategy*:  implements `MetricNamingStrategy`, can configure:
    * Which operation should be timed (return null means not timed)
    * How the metric is named
* *Proxy factory*: implements `ProxyFactory`, can configure how JDBC elements are wrapped (simple `java.lang.reflect.Proxy` or CGLib base proxies).
* *Registry holder*: implements `MetricRegistryHolder`, can configure how the metric registry is resolved.

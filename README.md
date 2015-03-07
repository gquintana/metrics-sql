# Metrics SQL

This a Yammer|Codahale|Dropwizard Metrics extension to instrument JDBC resources 
and measure SQL execution times.

[![Build Status](https://travis-ci.org/gquintana/metrics-sql.svg)](https://travis-ci.org/gquintana/metrics-sql)

## Setup

### DataSource level

Wrap your existing DataSource using `JdbcProxyFactory` or `MetricsSql` builder class:

```java
    metricRegistry = new MetricRegistry();
    dataSource = MetricsSql.forRegistry(metricRegistry)
                    .wrap("mysql", mysqlDataSource);
```
The String *mysql* is a datasource Id used in metric names.

### Connection level

Same as DataSource

```java
    metricRegistry = new MetricRegistry();
    connection = MetricsSql.forRegistry(metricRegistry)
                    .wrap("mysql", mysqlConnection);
```

### Driver level

1. Register Metrics SQL JDBC Driver: replace the original JDBC driver by `com.github.gquintana.metrics.sql.Driver`
2. Change JDBC URL prefix: `jdbc:xxx` becomes `jdbc:metrics:xxx`

Examples:

```
jdbc:metrics:mysql://localhost:3306/sakila?profileSQL=true&metrics_name=sakila
jdbc:metrics:postgresql://localhost/demo?metrics_driver=org.postgresql.Driver&ssl=true
jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_driver=org.h2.Driver;metrics_proxy_factory=caching
```

The driver supports several options:

* `metrics_driver`: the real driver class to wrap
* `metrics_name`: the database name used in metric names: defaults to xxx_driver
* `metrics_registry_holder`: the strategy used to locate the Metric registry: class name implementing `MetricRegistryHolder`, defaults to `StaticMetricRegistryHolder`
* `metrics_naming_strategy`: the strategy used to generate what should be metered and the timer names: class name implementing `MetricNamingStrategy`
* `metrics_proxy_factory`: the strategy used to create proxies: either `reflect` (the default), `cglib` or `caching`, 

## Configuration

* *Naming strategy*:  implements `MetricNamingStrategy`, can configure:
    * Which operation should be timed (return null means not timed)
    * How the metric is named
* *Proxy factory*: implements `ProxyFactory`, can configure how JDBC elements are wrapped (simple `java.lang.reflect.Proxy` or CGLib based proxies).
* *Registry holder*: implements `MetricRegistryHolder`, can configure how the metric registry is resolved.

## Integration

### Unprepared statement with unbound parameters

Beware of using unprepared statements and unbound parameters, it will generate a lot of metrics. For instance ...:

```
Statement statement = connection.createStatement();
statement.execute("insert into METRICS(ID, NAME) values(1, 'One')");
statement.execute("insert into METRICS(ID, NAME) values(2, 'Two')");
```
... will generate 2 metrics!

There are several options:
* Use prepared statements and bound parameters
* Tune the naming strategy to filter unprepared statements
* Tune the naming strategy to make both SQL statements generate the same metric name

### JMX

The `JmxReporter` doesn't play well with `DefaultMetricNamingStrategy`, you'll have to change either the naming strategy or the object name factory. A `SqlObjectNameFactory` is provided:

```java
JmxReporter.forRegistry(metricRegistry)
    .registerWith(mBeanServer)
    .createsObjectNamesWith(new SqlObjectNameFactory())
    .build();
```

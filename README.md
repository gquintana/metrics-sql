# Metrics SQL

This a Yammer|Codahale|Dropwizard Metrics extension to instrument JDBC resources 
and measure SQL execution times.

[![CircleCI](https://circleci.com/gh/gquintana/metrics-sql.svg?style=shield)](https://circleci.com/gh/gquintana/metrics-sql)
[![Coverage Status](https://coveralls.io/repos/github/gquintana/metrics-sql/badge.svg?branch=master)](https://coveralls.io/github/gquintana/metrics-sql?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.gquintana.metrics/metrics-sql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.gquintana.metrics/metrics-sql/)

## Supported metrics

| Description                                                     | Default metric name                                         | Metric type |
|-----------------------------------------------------------------|-------------------------------------------------------------|-------------|
| Connection opening (getConnection())                            | `java.sql.Connection                                      ` | Timer       |
| Connection life (between getConnection() and close())           | `java.sql.Connection                                      ` | Timer       |
| Statement life (between createStatement() and close())          | `java.sql.Statement                                       ` | Timer       |
| Statement execution (execute(), executeQuery()...)              | `java.sql.Statement.[select * from my_table].exec         ` | Timer       |
| PreparedStatement life (between prepareStatement() and close()) | `java.sql.PreparedStatement.[select * from my_table]      ` | Timer       |
| PreparedStatement execution (execute(), executeQuery()...)      | `java.sql.PreparedStatement.[select * from my_table].exec ` | Timer       |
| CallableStatement life (between prepareCall() and close())      | `java.sql.CallableStatement.[call_something()]            ` | Timer       |
| CallableStatement execution (execute(), executeQuery()...)      | `java.sql.CallableStatement.[call_something()].exec       ` | Timer       |
| ResultSet life (between executeQuery.() and close()..)          | `java.sql.ResultSet.[select * from my_table]              ` | Timer       |
| ResultSet rows (next())                                         | `java.sql.ResultSet.[select * from my_table].rows         ` | Meter       |

Metric naming is tunable, to be more Graphite or InfluxDB compliant, see MetricNamingStrategy.
Metering can be disabled per metric, you can select which metrics you (don't) want.

## Setup

### DataSource level

Wrap your existing DataSource using `JdbcProxyFactory` or `MetricsSql` builder class:

```java
    metricRegistry = new MetricRegistry();
    dataSource = MetricsSql.forRegistry(metricRegistry)
                    .wrap(mysqlDataSource);
```
The String *mysql* is a datasource Id used in metric names.

### Connection level

Same as DataSource

```java
    metricRegistry = new MetricRegistry();
    connection = MetricsSql.forRegistry(metricRegistry)
                    .wrap(mysqlConnection);
```

### Driver level

1. Register Metrics SQL JDBC Driver: replace the original JDBC driver by `com.github.gquintana.metrics.sql.Driver`
2. Change JDBC URL prefix: `jdbc:xxx` becomes `jdbc:metrics:xxx`

Examples:

```
jdbc:metrics:mysql://localhost:3306/sakila?profileSQL=true
jdbc:metrics:postgresql://localhost/demo?metrics_driver=org.postgresql.Driver&ssl=true
jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_driver=org.h2.Driver;metrics_proxy_factory=caching
```

The driver supports several options:

* `metrics_driver`: the real driver class to wrap
* `metrics_registry`: the name of the shared metric registry to use (see `SharedMetricRegistries`)
* `metrics_naming_strategy`: the strategy used to generate what should be metered and the timer names: class name implementing `MetricNamingStrategy`
* `metrics_proxy_factory`: the strategy used to create proxies: either `reflect` (the default), `cglib` or `caching`,

## Configuration

### Naming strategy

The *Naming strategy* implements `MetricNamingStrategy` and can configure:
    * Which operation should be timed (return null means not timed)
    * How the metric is named

The `DefaultMetricNamingStrategy` generate metric names like:
```
java.sql.Statement.[select * from my_table].exec
```
When the database is set,
```java
    dataSource = MetricsSql.forRegistry(metricRegistry)
                    .withDefaultNamingStrategy("my_database")
                    .wrap(mysqlDataSource);
```
It will produce:
```
java.sql.Statement.my_database.[select * from my_table].exec
```
This is useful when there are multiple datasources.

There is also the `StrictMetricNamingStrategy` which removes also special chars from the SQL query:
```
java.sql.Statement.select_from_my_table.exec
java.sql.Statement.my_database.select_from_my_table.exec
```

These settings are also available as URL properties:
```
jdbc:metrics:h2;metrics_naming_strategy=default;metrics_database=my_database
```

### Proxy factory 

The *Proxy factory* implements `ProxyFactory`, can configure how JDBC elements are wrapped 

* `ReflectProxyFactory` uses reflection and simple `java.lang.reflect.Proxy`
* `CGLibProxyFactory`, requires the CGLib library on the classpath and uses CGLib based proxies.

### SharedMetricRegistries

The Driver uses the `SharedMetricRegistries` singleton to lookup (and register) the `MetricRegistry`:

```
connection = DriverManager.getConnection("jdbc:metrics:h2;metrics_registry=my_registry", "sa", "");
metryRegistry = SharedMetricRegistries.getOrCreate("my_registry");
```

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

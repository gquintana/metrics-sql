package com.github.gquintana.metrics.sql;

import com.github.gquintana.metrics.sql.DefaultMetricNamingStrategy;
import com.github.gquintana.metrics.sql.StrictMetricNamingStrategy;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DefaultMetricNamingStrategyTest {

    @Test
    public void testDefault() throws Exception {
        // Given
        DefaultMetricNamingStrategy namingStrategy = new DefaultMetricNamingStrategy();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId, equalTo("[select * from metrics_test order by id]"));
        assertThat(connectionLifeTimer, equalTo("java.sql.Connection"));
    }

    @Test
    public void testDatabase() throws Exception {
        // Given
        DefaultMetricNamingStrategy namingStrategy = DefaultMetricNamingStrategy.builder()
                .withDatabaseName("test")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId, equalTo("[select * from metrics_test order by id]"));
        assertThat(connectionLifeTimer, equalTo("java.sql.Connection.test"));
    }
}
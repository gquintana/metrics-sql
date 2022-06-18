package com.github.gquintana.metrics.sql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultMetricNamingStrategyTest {

    @Test
    public void testDefault()  {
        // Given
        DefaultMetricNamingStrategy namingStrategy = new DefaultMetricNamingStrategy();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId).isEqualTo("[select * from metrics_test order by id]");
        assertThat(connectionLifeTimer).isEqualTo("java.sql.Connection");
    }

    @Test
    public void testDatabase()  {
        // Given
        DefaultMetricNamingStrategy namingStrategy = DefaultMetricNamingStrategy.builder()
                .withDatabaseName("test")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId).isEqualTo("[select * from metrics_test order by id]");
        assertThat(connectionLifeTimer).isEqualTo("java.sql.Connection.test");
    }
}

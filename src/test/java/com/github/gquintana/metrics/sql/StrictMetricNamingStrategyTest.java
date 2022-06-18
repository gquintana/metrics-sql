package com.github.gquintana.metrics.sql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class StrictMetricNamingStrategyTest {

    @Test
    public void getDefault() {
        // Given
        StrictMetricNamingStrategy namingStrategy = new StrictMetricNamingStrategy();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId).isEqualTo("select_from_metrics_test_order_by_id");
        assertThat(connectionLifeTimer).isEqualTo("java.sql.Connection");
    }

    @Test
    public void testReplaceCustom() {
        // Given
        StrictMetricNamingStrategy namingStrategy = StrictMetricNamingStrategy.builder()
                .withDatabaseName("test")
                .withReplacer("[*.]+", " ")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId).isEqualTo("select   from metrics_test order by id");
        assertThat(connectionLifeTimer).isEqualTo("java.sql.Connection.test");
    }
    @Test
    public void testMultiReplaceCustom() {
        // Given
        StrictMetricNamingStrategy namingStrategy = StrictMetricNamingStrategy.builder()
                .withReplacer("[*.]+", " ")
                .withReplacer("[\\s]+", " ")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        // Then
        assertThat(sqlId).isEqualTo("select from metrics_test order by id");
    }
}

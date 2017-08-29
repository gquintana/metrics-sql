package com.github.gquintana.metrics.sql;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class StrictMetricNamingStrategyTest {

    @Test
    public void getDefault() throws Exception {
        // Given
        StrictMetricNamingStrategy namingStrategy = new StrictMetricNamingStrategy();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId, equalTo("select_from_metrics_test_order_by_id"));
        assertThat(connectionLifeTimer, equalTo("java.sql.Connection"));
    }

    @Test
    public void testReplaceCustom() throws Exception {
        // Given
        StrictMetricNamingStrategy namingStrategy = StrictMetricNamingStrategy.builder()
                .withDatabaseName("test")
                .withReplacer("[*.]+", " ")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        String connectionLifeTimer = namingStrategy.getConnectionLifeTimer();
        // Then
        assertThat(sqlId, equalTo("select   from metrics_test order by id"));
        assertThat(connectionLifeTimer, equalTo("java.sql.Connection.test"));
    }
    @Test
    public void testMultiReplaceCustom() throws Exception {
        // Given
        StrictMetricNamingStrategy namingStrategy = StrictMetricNamingStrategy.builder()
                .withReplacer("[*.]+", " ")
                .withReplacer("[\\s]+", " ")
                .build();
        // When
        String sqlId = namingStrategy.getSqlId("select * from METRICS_TEST order by ID");
        // Then
        assertThat(sqlId, equalTo("select from metrics_test order by id"));
    }
}
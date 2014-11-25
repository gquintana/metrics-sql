/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;

/**
 *
 */
public interface MetricNamingStrategy {

    public Timer.Context startPooledConnectionTimer(String connectionFactoryName);

    public Timer.Context startConnectionTimer(String connectionFactoryName);

    public Timer.Context startStatementTimer(String connectionFactoryName);

    public StatementTimerContext startStatementExecuteTimer(String connectionFactoryName, String sql);

    public StatementTimerContext startPreparedStatementTimer(String connectionFactoryName, String sql, String sqlId);

    public StatementTimerContext startPreparedStatementExecuteTimer(String connectionFactoryName, String sql, String sqlId);

    public StatementTimerContext startCallableStatementTimer(String connectionFactoryName, String sql, String sqlId);

    public StatementTimerContext startCallableStatementExecuteTimer(String connectionFactoryName, String sql, String sqlId);

    public Timer.Context startResultSetTimer(String connectionFactoryName, String sql, String sqlId);
}

/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.PooledConnection;

/**
 * Strategy used to tell what should be timed and what should be the name of the timer
 */
public interface MetricNamingStrategy {
    /**
     * Start timer for {@link PooledConnection} life
     * @param databaseName  Name of the database
     * @return Started Timer context or null
     */
    Timer.Context startPooledConnectionTimer(String databaseName);

    /**
     * Start timer for {@link Connection} life
     * @param databaseName  Name of the database
     * @return Started Timer context or null
     */
    Timer.Context startConnectionTimer(String databaseName);

    /**
     * Start timer for {@link Statement} life
     * @param databaseName  Name of the database
     * @return Started Timer context or null
     */
    Timer.Context startStatementTimer(String databaseName);

    /**
     * Start timer for {@link Statement} execution
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @return Started Timer context or null
     */
    StatementTimerContext startStatementExecuteTimer(String databaseName, String sql);

    /**
     * Start timer for {@link PreparedStatement} life
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Started Timer context or null
     */
    StatementTimerContext startPreparedStatementTimer(String databaseName, String sql, String sqlId);

    /**
     * Start timer for {@link PreparedStatement} execution
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Started Timer context or null
     */
    StatementTimerContext startPreparedStatementExecuteTimer(String databaseName, String sql, String sqlId);

    /**
     * Start timer for {@link CallableStatement} life
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Started Timer context or null
     */
    StatementTimerContext startCallableStatementTimer(String databaseName, String sql, String sqlId);

    /**
     * Start timer for {@link CallableStatement} execution
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Started Timer context or null
     */
    StatementTimerContext startCallableStatementExecuteTimer(String databaseName, String sql, String sqlId);

    /**
     * Start timer for {@link ResultSet} execution
     * @param databaseName  Name of the database
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Started Timer context or null
     */
    Timer.Context startResultSetTimer(String databaseName, String sql, String sqlId);
}

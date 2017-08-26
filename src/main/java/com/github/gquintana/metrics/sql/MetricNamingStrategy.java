package com.github.gquintana.metrics.sql;

/*
 * #%L
 * Metrics SQL
 * %%
 * Copyright (C) 2014 Open-Source
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.sql.PooledConnection;
import java.sql.*;

/**
 * Strategy used to tell what should be timed and what should be the name of the timer
 */
public interface MetricNamingStrategy {
    /**
     * Get timer name for {@link PooledConnection} life
     * @return Timer name or null
     */
    String getPooledConnectionLifeTimer();

    /**
     * Get timer name for {@link Connection} life
     * @return Timer name or null
     */
    String getConnectionLifeTimer();

    /**
     * Get timer name for {@link Statement} life
     * @return Timer name or null
     */
    String getStatementLifeTimer();

    /**
     * Get timer name for {@link Statement} execution
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getStatementExecuteTimer(String sql, String sqlId);

    /**
     * Get timer name for {@link PreparedStatement} life
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getPreparedStatementLifeTimer(String sql, String sqlId);

    /**
     * Get timer name for {@link PreparedStatement} execution
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getPreparedStatementExecuteTimer(String sql, String sqlId);

    /**
     * Get timer name for {@link CallableStatement} life
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getCallableStatementLifeTimer(String sql, String sqlId);

    /**
     * Get timer name for {@link CallableStatement} execution
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getCallableStatementExecuteTimer(String sql, String sqlId);

    /**
     * Get timer name for {@link ResultSet} life
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getResultSetLifeTimer(String sql, String sqlId);

    /**
     * Clean, simplify, hash... SQL query to use it as an SQL identifier in metric name
     * @param sql SQL Query
     * @return sqlId SQL Id generated from query
     */
    String getSqlId(String sql);

    /**
     * Get meter name for {@link ResultSet} iteration
     * @param sql SQL Query
     * @param sqlId SQL Id generated from query or null
     * @return Timer name or null
     */
    String getResultSetRowMeter(String sql, String sqlId);
}

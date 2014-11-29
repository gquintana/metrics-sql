/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;

/**
 * {@link Timer.Context} and SQL Id couple
 */
public class StatementTimerContext {
    private final Timer.Context timerContext;
    private final String sql;
    private final String sqlId;

    public StatementTimerContext(Timer.Context timerContext, String sql, String sqlId) {
        this.timerContext = timerContext;
        this.sql = sql;
        this.sqlId = sqlId;
    }

    public Timer.Context getTimerContext() {
        return timerContext;
    }

    public String getSql() {
        return sql;
    }

    public String getSqlId() {
        return sqlId;
    }
    
}

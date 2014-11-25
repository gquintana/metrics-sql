package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import java.sql.PreparedStatement;

/**
 * JDBC proxy handler for {@link PreparedStatement}
 */
public class PreparedStatementProxyHandler extends AbstractStatementProxyHandler<PreparedStatement> {

    private final String sql;
    private final String sqlId;

    public PreparedStatementProxyHandler(PreparedStatement delegate, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext, String sql, String sqlId) {
        super(delegate, PreparedStatement.class, name, proxyFactory, lifeTimerContext);
        this.sql = sql;
        this.sqlId = sqlId;
    }

    protected final Object execute(MethodInvocation<PreparedStatement> methodInvocation) throws Throwable {
        final String lSql;
        final String lSqlId;
        if (methodInvocation.getArgCount() > 0) {
            lSql = methodInvocation.getArgAt(0, String.class);
            lSqlId = null;
        } else {
            lSql = this.sql;
            lSqlId = this.sqlId;
        }
        StatementTimerContext timerContext = proxyFactory.startPreparedStatementExecuteTimer(name, lSql, lSqlId);
        Object result = methodInvocation.proceed();
        result = stopTimer(timerContext, result);
        return result;
    }

}

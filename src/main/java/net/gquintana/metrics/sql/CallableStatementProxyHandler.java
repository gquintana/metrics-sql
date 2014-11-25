package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import java.sql.CallableStatement;

/**
 * JDBC Proxy handler for {@link CallableStatement}
 */
public class CallableStatementProxyHandler extends AbstractStatementProxyHandler<CallableStatement> {

    private final String sql;
    private final String sqlId;

    public CallableStatementProxyHandler(CallableStatement delegate, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext, String sql, String sqlId) {
        super(delegate, CallableStatement.class, name, proxyFactory, lifeTimerContext);
        this.sql = sql;
        this.sqlId = sqlId;
    }

    protected final Object execute(MethodInvocation<CallableStatement> methodInvocation) throws Throwable {
        final String lSql;
        final String lSqlId;
        if (methodInvocation.getArgCount() > 0) {
            lSql = methodInvocation.getArgAt(0, String.class);
            lSqlId = null;
        } else {
            lSql = this.sql;
            lSqlId = this.sqlId;
        }
        StatementTimerContext timerContext = proxyFactory.startCallableStatementExecuteTimer(name, lSql, lSqlId);
        Object result = methodInvocation.proceed();
        result = stopTimer(timerContext, result);
        return result;
    }

}

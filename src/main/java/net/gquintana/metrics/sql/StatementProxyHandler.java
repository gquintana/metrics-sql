package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import java.sql.Statement;

/**
 * JDBC Proxy handler for {@link Statement}
 */
public class StatementProxyHandler extends AbstractStatementProxyHandler<Statement> {

    public StatementProxyHandler(Statement delegate, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, Statement.class, name, proxyFactory, lifeTimerContext);
    }

    protected Object execute(MethodInvocation<Statement> methodInvocation) throws Throwable {
        Object result;
        if (methodInvocation.getArgCount() > 0) {
            final String sql = methodInvocation.getArgAt(0, String.class);
            final StatementTimerContext timerContext = proxyFactory.startStatementExecuteTimer(name, sql);
            result = methodInvocation.proceed();
            result = stopTimer(timerContext, result);
        } else {
            result = methodInvocation.proceed();
        }
        return result;
    }

}

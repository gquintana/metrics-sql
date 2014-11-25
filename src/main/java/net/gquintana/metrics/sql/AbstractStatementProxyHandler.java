package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Base JDBC proxy handler for Statements
 * @param <T> Statement type
 */
public abstract class AbstractStatementProxyHandler<T extends Statement> extends JdbcProxyHandler<T> {

    public AbstractStatementProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, delegateType, name, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object invoke(MethodInvocation<T> delegatingMethodInvocation) throws Throwable {
        final String methodName = delegatingMethodInvocation.getMethodName();
        Object result;
        if (methodName.equals("isWrapperFor")) {
            result = isWrapperFor(delegatingMethodInvocation);
        } else if (methodName.equals("unwrap")) {
            result = unwrap(delegatingMethodInvocation);
        } else if (methodName.equals("close")) {
            result = close(delegatingMethodInvocation);
        } else if (methodName.equals("execute") || methodName.equals("executeQuery") || methodName.equals("executeUpdate")) {
            result = execute(delegatingMethodInvocation);
        } else {
            result = delegatingMethodInvocation.proceed();
        }
        return result;
    }

    protected abstract Object execute(MethodInvocation<T> delegatingMethodInvocation) throws Throwable;

    private static final InvocationFilter THIS_INVOCATION_FILTER = new MethodNamesInvocationFilter("isWrapperFor", "unwrap", "close", "execute", "executeQuery", "executeUpdate");

    @Override
    public InvocationFilter getInvocationFilter() {
        return THIS_INVOCATION_FILTER;
    }
    protected Object stopTimer(StatementTimerContext timerContext, Object result) {
        if (timerContext!=null) {
            stopTimer(timerContext.getTimerContext());
            if (result instanceof ResultSet) {
                result = proxyFactory.wrapResultSet(name, (ResultSet) result, timerContext.getSql(), timerContext.getSqlId());
            }
        }
        return result;
    }
}

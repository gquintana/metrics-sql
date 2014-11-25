package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import java.sql.ResultSet;

/**
 * JDBC proxy handler for {@link ResultSet} and its subclasses.
 * 
 * @param <T> Proxied ResultSet type
 */
public class ResultSetProxyHandler<T extends ResultSet> extends JdbcProxyHandler<T> {

    public ResultSetProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, delegateType, name, proxyFactory, lifeTimerContext);
    }

    private static final InvocationFilter THIS_INVOCATION_FILTER = new MethodNamesInvocationFilter("isWrapperFor", "unwrap", "close");

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
        } else {
            result = delegatingMethodInvocation.proceed();
        }
        return result;
    }

    @Override
    public InvocationFilter getInvocationFilter() {
        return THIS_INVOCATION_FILTER;
    }
}

package net.gquintana.metrics.sql;

import com.codahale.metrics.Timer;
import net.gquintana.metrics.proxy.MethodInvocation;
import javax.sql.PooledConnection;
import java.sql.Connection;

/**
 * JDBC proxy handler for {@link PooledConnection} and its subclasses.
 */
public class PooledConnectionProxyHandler<T extends PooledConnection> extends JdbcProxyHandler<T> {

    public PooledConnectionProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate, delegateType, name, proxyFactory, lifeTimerContext);
    }

    @Override
    protected Object invoke(MethodInvocation<T> methodInvocation) throws Throwable {
        final String methodName = methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getConnection")) {
            result = getConnection(methodInvocation);
        } else {
            result = methodInvocation.proceed();
        }
        return result;
    }

    private Connection getConnection(MethodInvocation<T> methodInvocation) throws Throwable {
        Connection connection = (Connection) methodInvocation.proceed();
        connection = proxyFactory.wrapConnection(name, connection);
        return connection;
    }
}

package net.gquintana.metrics.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Proxy method invocation
 *
 * @author gquintana
 */
public final class MethodInvocation<T> {

    /**
     * Target (real) object
     */
    private final T delegate;
    /**
     * Proxy
     */
    private final Object proxy;
    /**
     * Method
     */
    private final Method method;
    /**
     * Invocation arguments
     */
    private final Object[] args;

    public MethodInvocation(T target, Object proxy, Method method, Object... args) {
        this.delegate = target;
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public int getArgCount() {
        return args == null ? 0 : args.length;
    }

    public Object getArgAt(int argIndex) {
        return args[argIndex];
    }

    public <R> R getArgAt(int argIndex, Class<R> argType) {
        return argType.cast(getArgAt(argIndex));
    }

    public String getMethodName() {
        return method.getName();
    }

    public Object proceed() throws Throwable {
        return method.invoke(delegate, args);
    }

}

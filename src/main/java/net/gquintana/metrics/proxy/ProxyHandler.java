package net.gquintana.metrics.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Produces proxy that wrap and existing class using
 * {@link java.lang.reflect.Proxy} class. 
 * This class is used to do lightweight AOP.
 *
 * @param <T> Type of the wrapped class
 */
public class ProxyHandler<T> implements InvocationHandler {

    /**
     * Wrapped class and concrete implementation
     */
    protected final T delegate;

    /**
     * Main constructor
     *
     * @param delegate Wrapped class and concrete implementation
     */
    public ProxyHandler(T delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc }
     */
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke(new MethodInvocation<T>(delegate, proxy, method, args));
    }

    /**
     * Method to override in child classes
     *
     * @param delegatingMethodInvocation Method invocation (arguments, method
     * name, etc.)
     * @return Method invocation results
     * @throws Throwable Method invocation raised exception
     */
    protected Object invoke(MethodInvocation<T> delegatingMethodInvocation) throws Throwable {
        return delegatingMethodInvocation.proceed();
    }

    /**
     * Return Wrapped class and concrete implementation.
     */
    public T getDelegate() {
        return delegate;
    }

    /**
     * Interface to tell which methods are intercepted by the proxy
     */
    public interface InvocationFilter {

        /**
         * Returns true if proxy is intecepting the method, or false is a direct
         * call is done
         *
         * @param method Method
         */
        boolean isIntercepted(Method method);
    }

    /**
     * Invocation filter which doesn't filter anything
     */
    protected static final InvocationFilter ALL_INVOCATION_FILTER = new InvocationFilter() {
        @Override
        public boolean isIntercepted(Method method) {
            return true;
        }
    };

    /**
     * Invocation filter which filters depending on method name
     */
    protected static final class MethodNamesInvocationFilter implements InvocationFilter {

        private final String[] methodNames;

        public MethodNamesInvocationFilter(String... methodNames) {
            this.methodNames = methodNames;
            Arrays.sort(this.methodNames);
        }

        @Override
        public boolean isIntercepted(Method method) {
            return Arrays.binarySearch(methodNames, method.getName()) >= 0;
        }
    }

    public InvocationFilter getInvocationFilter() {
        return ALL_INVOCATION_FILTER;
    }

}

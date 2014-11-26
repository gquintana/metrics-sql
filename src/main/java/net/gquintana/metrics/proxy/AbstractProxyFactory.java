package net.gquintana.metrics.proxy;

/**
 * Proxy factory is used to produce proxies of any class
 */
public abstract class AbstractProxyFactory implements ProxyFactory {


    /**
     * Create a proxy using given classloader and interfaces
     *
     * @param proxyHandler Proxy invocation handler
     * @param classLoader Class loader
     * @param interfaces Interfaces to implement
     * @return Proxy
     */
    public <X> X newProxy(ProxyHandler<X> proxyHandler, ClassLoader classLoader, Class<?>... interfaces) {
        return newProxy(proxyHandler, new ProxyClass(classLoader, interfaces));
    }

    /**
     * Create a proxy using given classloader and interfaces. Current thread
     * class loaded is used as default classload.
     *
     * @param interfaces Interfaces to implement
     * @return Proxy
     */
    public <X> X newProxy(ProxyHandler<X> proxyHandler, Class<?>... interfaces) {
        return newProxy(proxyHandler, Thread.currentThread().getContextClassLoader(), interfaces);
    }

    /**
     * Create a proxy using given classloader and interfaces
     *
     * @param interfaces Interface to implement
     * @return Proxy
     */
    public <X> X newProxy(ProxyHandler<?> proxyHandler, Class<X> interfaces) {
        return (X) newProxy(proxyHandler, new Class[]{interfaces});
    }
}

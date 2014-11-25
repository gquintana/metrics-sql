/*
 * Default License
 */

package net.gquintana.metrics.proxy;

/**
 *
 */
public interface ProxyFactory {
    /**
     * Create a proxy using given classloader and interfaces
     *
     * @param proxyHandler Proxy invocation handler
     * @param proxyClass Class loader + Interfaces to implement
     * @return Proxy
     */
    <T> T newProxy(ProxyHandler<T> proxyHandler, ProxyClass proxyClass);

}

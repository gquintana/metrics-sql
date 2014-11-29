/*
 * Default License
 */

package net.gquintana.metrics.proxy;

/**
 * Strategy used to create proxies
 */
public interface ProxyFactory {
    /**
     * Create a proxy using given classloader and interfaces
     *
     * @param proxyHandler Proxy invocation handler
     * @param proxyClass Class loader + Interfaces to implement
     * @param <T> Proxy type
     * @return Proxy
     */
    <T> T newProxy(ProxyHandler<T> proxyHandler, ProxyClass proxyClass);

}

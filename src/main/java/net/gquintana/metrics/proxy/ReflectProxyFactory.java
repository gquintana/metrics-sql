package net.gquintana.metrics.proxy;

import java.lang.reflect.Proxy;

/**
 * Factory of proxies producing proxies based on Java reflection
 */
public class ReflectProxyFactory extends AbstractProxyFactory {
	/**
	 * {@inheritDoc}
	 */
        @Override
	public <T> T newProxy(ProxyHandler<T> proxyHandler, ProxyClass proxyClass) {
		return (T) Proxy.newProxyInstance(proxyClass.getClassLoader(), proxyClass.getInterfaces(), proxyHandler);
	}
}

/*
 * Default License
 */

package net.gquintana.metrics.proxy;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ProxyFactory} using reflection, but caching proxy constructors.
 * Performance of proxy instantiation is improved (even faster than CGLib).
 * But it may lead to classloader memory leaks.
 */
public class CachingProxyFactory extends AbstractProxyFactory {
    private final ConcurrentHashMap<ProxyClass, Constructor<?>> constructorCache = new ConcurrentHashMap<>();
    @Override
    public <T> T newProxy(ProxyHandler<T> proxyHandler, ProxyClass proxyClass) {
        Constructor constructor = constructorCache.get(proxyClass);
        if (constructor == null) {
            constructor = proxyClass.createConstructor();
            final Constructor oldConstructor = constructorCache.putIfAbsent(proxyClass, constructor);
            constructor = oldConstructor == null ? constructor : oldConstructor;
        }
        try {
            return (T) constructor.newInstance(proxyHandler);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new ProxyException(reflectiveOperationException);
        }
    }
    /**
     * Clears the constructor cache
     */
    public void clearCache() {
        constructorCache.clear();
    }
}

package com.github.gquintana.metrics.proxy;

/*
 * #%L
 * Metrics SQL
 * %%
 * Copyright (C) 2014 Open-Source
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ProxyFactory} using reflection, but caching proxy constructors.
 * Performance of proxy instantiation is improved (even faster than CGLib).
 * But it may lead to classloader memory leaks.
 */
public class CachingProxyFactory implements ProxyFactory {
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

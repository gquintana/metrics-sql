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


import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy factory based on CGLib
 */
public class CGLibProxyFactory extends AbstractProxyFactory {

    private final Map<ProxyClass, Class> proxyClasses = new ConcurrentHashMap<ProxyClass, Class>();
    private static final Class[] ADAPTER_CALLBACK_TYPES = new Class[]{
        AdapterMethodInterceptor.class,
        AdapterLazyLoader.class
    };

    private static class AdapterCallbackFilter implements CallbackFilter {

        private final ProxyHandler.InvocationFilter invocationFilter;

        private AdapterCallbackFilter(ProxyHandler.InvocationFilter invocationFilter) {
            this.invocationFilter = invocationFilter;
        }

        @Override
        public int accept(Method method) {
            // 0 is AdapterMethodInterceptor
            // 1 is AdapterLazyLoader
            return invocationFilter.isIntercepted(method) ? 0 : 1;
        }
    }

    private static class AdapterMethodInterceptor implements MethodInterceptor {

        private final ProxyHandler proxyHandler;

        private AdapterMethodInterceptor(ProxyHandler proxyHandler) {
            this.proxyHandler = proxyHandler;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
            return proxyHandler.invoke(proxy, method, arguments);
        }
    }

    private static class AdapterLazyLoader<T> implements LazyLoader {

        private final T delegate;

        private AdapterLazyLoader(T delegate) {
            this.delegate = delegate;
        }

        @Override
        public T loadObject() {
            return delegate;
        }
    }

    private Class getProxyClass(ProxyHandler<?> proxyHandler, ProxyClass proxyClass) {
        Class clazz = proxyClasses.get(proxyClass);
        if (clazz == null) {
            Enhancer enhancer = new Enhancer();
            enhancer.setCallbackFilter(new AdapterCallbackFilter(proxyHandler.getInvocationFilter()));
            enhancer.setCallbackTypes(ADAPTER_CALLBACK_TYPES);
            enhancer.setClassLoader(proxyClass.getClassLoader());
            enhancer.setInterfaces(proxyClass.getInterfaces());
            clazz = enhancer.createClass();
            proxyClasses.put(proxyClass, clazz);
        }
        return clazz;
    }

    @Override
    public <T> T newProxy(ProxyHandler<T> proxyHandler, ProxyClass proxyClass) {
        try {
            Object proxy = getProxyClass(proxyHandler, proxyClass).newInstance();
            ((Factory) proxy).setCallbacks(new Callback[]{
                new AdapterMethodInterceptor(proxyHandler),
                new AdapterLazyLoader<Object>(proxyHandler.getDelegate())
            });
            return (T) proxy;
        } catch (ReflectiveOperationException e) {
            throw new ProxyException(e);
        }
    }
}

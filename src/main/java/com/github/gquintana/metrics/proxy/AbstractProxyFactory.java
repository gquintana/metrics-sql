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

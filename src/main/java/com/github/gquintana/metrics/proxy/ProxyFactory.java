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

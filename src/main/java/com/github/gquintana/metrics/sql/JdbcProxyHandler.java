package com.github.gquintana.metrics.sql;

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


import com.codahale.metrics.Timer;
import com.github.gquintana.metrics.proxy.ProxyHandler;
import com.github.gquintana.metrics.proxy.MethodInvocation;
import java.sql.SQLException;
import java.sql.Wrapper;
import com.github.gquintana.metrics.proxy.ProxyClass;

/**
 * Base class for all JDBC Proxy handlers.
 * 
 * @param <T> Proxied type
 */
public abstract class JdbcProxyHandler<T> extends ProxyHandler<T> {

    /**
     * JDBC Interface class
     */
    private final Class<T> delegateType;
    /**
     * Timer measuring this proxy lifetime
     */
    private final Timer.Context lifeTimerContext;
    /**
     * Proxy name
     */
    protected final String name;
    /**
     * Parent factory of proxy factories
     */
    protected final JdbcProxyFactory proxyFactory;

    /**
     * Main constructor
     *
     * @param delegate Wrapped JDBC object
     * @param delegateType JDBC object interface
     * @param name Proxy name
     * @param proxyFactory Parent factory
     * @param lifeTimerContext Proxy life timer context
     */
    protected JdbcProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Timer.Context lifeTimerContext) {
        super(delegate);
        this.delegateType = delegateType;
        this.proxyFactory = proxyFactory;
        this.name = name;
        this.lifeTimerContext = lifeTimerContext;
    }

    private boolean isDelegateType(Class<?> iface) {
        return this.delegateType.equals(iface);
    }

    private Class getClassArg(MethodInvocation methodInvocation) {
        return (Class) methodInvocation.getArgAt(0, Class.class);
    }

    protected Object isWrapperFor(MethodInvocation methodInvocation) throws Throwable {
        final Class iface = getClassArg(methodInvocation);
        return isDelegateType(iface) ? true : methodInvocation.proceed();
    }

    protected Object close(MethodInvocation methodInvocation) throws Throwable {
        stopTimer(lifeTimerContext);
        return methodInvocation.proceed();
    }

    protected final void stopTimer(Timer.Context timerContext) {
        if (timerContext != null) {
            timerContext.stop();
        }
    }

    protected Object unwrap(MethodInvocation<T> methodInvocation) throws SQLException {
        final Class iface = getClassArg(methodInvocation);
        final Wrapper delegateWrapper = (Wrapper) delegate;
        Object result;
        if (isDelegateType(iface)) {
            result = delegateWrapper.isWrapperFor(iface) ? delegateWrapper.unwrap(iface) : iface.cast(delegateWrapper);
        } else {
            result = delegateWrapper.unwrap(iface);
        }
        return result;
    }

    public ProxyClass getProxyClass() {
        return new ProxyClass(delegate.getClass().getClassLoader(), delegateType);
    }
}

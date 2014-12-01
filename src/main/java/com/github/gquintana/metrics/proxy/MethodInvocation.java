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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Proxy method invocation
 *
 * @author gquintana
 */
public final class MethodInvocation<T> {

    /**
     * Target (real) object
     */
    private final T delegate;
    /**
     * Proxy
     */
    private final Object proxy;
    /**
     * Method
     */
    private final Method method;
    /**
     * Invocation arguments
     */
    private final Object[] args;

    public MethodInvocation(T target, Object proxy, Method method, Object... args) {
        this.delegate = target;
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public int getArgCount() {
        return args == null ? 0 : args.length;
    }

    public Object getArgAt(int argIndex) {
        return args[argIndex];
    }

    public <R> R getArgAt(int argIndex, Class<R> argType) {
        return argType.cast(getArgAt(argIndex));
    }

    public String getMethodName() {
        return method.getName();
    }

    public Object proceed() throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}

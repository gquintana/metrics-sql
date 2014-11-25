/*
 * Default License
 */

package net.gquintana.metrics.proxy;

import java.util.Arrays;
import java.util.Objects;

/**
 * Proxy class key
 */
public final class ProxyClass {
    private final ClassLoader classLoader;
    private final Class<?>[] interfaces;
    private final int hashCode;

    public ProxyClass(ClassLoader classLoader, Class<?>... interfaces) {
        this.classLoader = classLoader;
        this.interfaces = interfaces;
        // Generate hascode once for all
        final Object[] hashValue = new Object[interfaces.length + 1];
        hashValue[0] = classLoader;
        System.arraycopy(interfaces, 0, hashValue, 1, interfaces.length);
        this.hashCode = Objects.hash(hashValue);
    } // Generate hascode once for all

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Class<?>[] getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProxyClass that = (ProxyClass) o;
        return (this.hashCode == that.hashCode) && Objects.equals(this.classLoader, that.classLoader) && Arrays.equals(this.interfaces, that.interfaces);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}

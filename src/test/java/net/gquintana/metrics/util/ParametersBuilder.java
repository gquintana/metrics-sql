/*
 * Default License
 */

package net.gquintana.metrics.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class ParametersBuilder {
    private final Collection<Object[]> parameters = new ArrayList<>();
    public ParametersBuilder add(Object ... parameter) {
        parameters.add(parameter);
        return this;
    }
    public Collection<Object[]> build() {
        return parameters;
    }
}

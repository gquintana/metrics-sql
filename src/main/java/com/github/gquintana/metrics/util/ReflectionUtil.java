package com.github.gquintana.metrics.util;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

/**
 * Created on 4/17/16.
 */
public class ReflectionUtil {

    public static <T> T newInstance(Class<T> clazz, Object ... params) throws SQLException {
        try {
            if (params == null || params.length==0) {
                return clazz.newInstance();
            } else {
                for(Constructor<?> ctor: clazz.getConstructors()) {
                    if (ctor.getParameterTypes().length==params.length) {
                        int paramIndex=0;
                        for(Class<?> paramType:ctor.getParameterTypes()) {
                            if (!paramType.isInstance(params[paramIndex])) {
                                break;
                            }
                            paramIndex++;
                        }
                        if (paramIndex==params.length) {
                            return clazz.cast(ctor.newInstance(params));
                        }
                    }
                }
                throw new SQLException("Constructor not found for "+clazz);
            }
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new SQLException(reflectiveOperationException);
        }
    }


}

package com.github.gquintana.metrics.util;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.management.ObjectName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Parameterized.class)
public class SqlObjectNameFactoryTest {
    private final String timerName;
    private final Class<?> clazz;
    private final String database;
    private final boolean sql;
    private final String event;
    private final SqlObjectNameFactory objectNameFactory = new SqlObjectNameFactory();
    
    public SqlObjectNameFactoryTest(String timerName, Class<?> clazz, String database, boolean sql, String event) {
        this.timerName = timerName;
        this.clazz = clazz;
        this.database = database;
        this.sql = sql;
        this.event = event;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return new ParametersBuilder()
                .add("java.sql.Connection.test", Connection.class, "test", false, null)
                .add("java.sql.PreparedStatement.test.[insert into metrics_test(id, text, created) values (?,?,?)]", PreparedStatement.class, "test", true, null)
                .add("java.sql.Statement.test.[select count(*) from metrics_test].exec", Statement.class, "test", true, "exec")
                .build();
    }
    @Test
    public void testGetObjectName() {
        // When
        ObjectName objectName = objectNameFactory.createName("Timer", "com.github.gquintana.metrics", timerName);
        // Then
        assertEquals(clazz.getName(), objectName.getKeyProperty("class"));
        assertEquals(database, objectName.getKeyProperty("database"));
        if (sql) {
            assertNotNull(objectName.getKeyProperty("sql"));
        }
        if (event!=null) {
            assertEquals(event, objectName.getKeyProperty("event"));
        }
    }
    
}

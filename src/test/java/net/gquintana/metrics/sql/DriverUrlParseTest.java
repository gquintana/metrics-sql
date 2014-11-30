package net.gquintana.metrics.sql;

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

import net.gquintana.metrics.util.ParametersBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test driver URL parsing
 */
@RunWith(Parameterized.class)
public class DriverUrlParseTest {
    private final String rawUrl;
    private final String cleanUrl;
    private final String databaseType;
    private final Properties properties;

    public DriverUrlParseTest(String rawUrl, String cleanUrl, String databaseType, Properties properties) {
        this.rawUrl = rawUrl;
        this.cleanUrl = cleanUrl;
        this.databaseType = databaseType;
        this.properties = properties;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        Properties properties1 = new Properties();
        properties1.setProperty("metrics_key", "val");
        return new ParametersBuilder()
                .add("jdbc:metrics:oracle:thin:192.168.2.1:1521:X01A","jdbc:oracle:thin:192.168.2.1:1521:X01A", "oracle", null)
                .add("jdbc:metrics:mysql://localhost:3306/sakila?profileSQL=true&metrics_key=val","jdbc:mysql://localhost:3306/sakila?profileSQL=true", "mysql", properties1)
                .add("jdbc:metrics:postgresql://localhost/test?metrics_key=val&user=fred&password=secret&ssl=true","jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true", "postgresql", properties1)
                .add("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_key=val","jdbc:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE", "h2", properties1)
                .add("jdbc:metrics:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true;metrics_key=val;","jdbc:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true", "sqlserver", properties1)
                .build();
    }
    @Test
    public void testParse() {
        DriverUrl driverUrl= DriverUrl.parse(rawUrl);
        assertEquals(rawUrl, driverUrl.getRawUrl());
        assertEquals(cleanUrl, driverUrl.getCleanUrl());
        assertEquals(databaseType, driverUrl.getDatabaseType());
        String prop=driverUrl.getProperty("metrics_key", String.class);
        assertTrue(prop==null || prop.equals("val"));
    }
    
}

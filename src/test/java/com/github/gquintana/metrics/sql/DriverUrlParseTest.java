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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test driver URL parsing
 */
public class DriverUrlParseTest {
    public static Stream<Arguments> getParameters() {
        Properties properties1 = new Properties();
        properties1.setProperty("metrics_key", "val");
        return Stream.of(
                Arguments.of("jdbc:metrics:oracle:thin:192.168.2.1:1521:X01A",
						"jdbc:oracle:thin:192.168.2.1:1521:X01A", "oracle", null),
                Arguments.of("jdbc:metrics:mysql://localhost:3306/sakila?profileSQL=true&metrics_key=val",
						"jdbc:mysql://localhost:3306/sakila?profileSQL=true", "mysql", properties1),
                Arguments.of("jdbc:metrics:postgresql://localhost/test?metrics_key=val&user=fred&password=secret&ssl=true",
						"jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true", "postgresql", properties1),
                Arguments.of("jdbc:metrics:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE;metrics_key=val",
						"jdbc:h2:~/test;AUTO_SERVER=TRUE;;AUTO_RECONNECT=TRUE", "h2", properties1),
                Arguments.of("jdbc:metrics:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true;metrics_key=val;",
						"jdbc:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true", "sqlserver", properties1),
				Arguments.of("jdbc:metrics:oracle:oci:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=cluster_alias)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=service_name)))?metrics_key=val",
						"jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=cluster_alias)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=service_name)))","oracle", properties1)
                );
    }
    @ParameterizedTest
	@MethodSource("getParameters")
    public void testParse(String rawUrl, String cleanUrl, String databaseType, Properties properties) {
        DriverUrl driverUrl= DriverUrl.parse(rawUrl);
        assertThat(driverUrl.getRawUrl()).isEqualTo(rawUrl);
        assertThat(driverUrl.getCleanUrl()).isEqualTo(cleanUrl);
        assertThat(driverUrl.getDatabaseType()).isEqualTo(databaseType);
        String prop=driverUrl.getProperty("metrics_key");
        assertThat(prop==null || prop.equals("val")).isTrue();
    }
    
}

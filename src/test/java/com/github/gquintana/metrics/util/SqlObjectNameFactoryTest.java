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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.management.ObjectName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class SqlObjectNameFactoryTest {
	private final SqlObjectNameFactory objectNameFactory = new SqlObjectNameFactory();

	/*
    private final String timerName;
    private final Class<?> clazz;
    private final String database;
    private final boolean sql;
    private final String event;
    private final SqlObjectNameFactory objectNameFactory = new SqlObjectNameFactory();
    */
	public static Stream<Arguments> getParameters() {
		return Stream.of(
				Arguments.of("java.sql.Connection.test", Connection.class, "test", false, null),
				Arguments.of("java.sql.PreparedStatement.test.[insert into metrics_test(id, text, created) values (?,?,?)]", PreparedStatement.class, "test", true, null),
				Arguments.of("java.sql.Statement.test.[select count(*) from metrics_test].exec", Statement.class, "test", true, "exec")
		);
	}

	@ParameterizedTest
	@MethodSource("getParameters")
	public void testGetObjectName(String timerName, Class<?> clazz, String database, boolean sql, String event) {
		// When
		ObjectName objectName = objectNameFactory.createName("Timer", "com.github.gquintana.metrics", timerName);
		// Then
		assertThat(objectName.getKeyProperty("class")).isEqualTo(clazz.getName());
		assertThat(objectName.getKeyProperty("database")).isEqualTo(database);
		if (sql) {
			assertThat(objectName.getKeyProperty("sql")).isNotNull();
		}
		if (event != null) {
			assertThat(objectName.getKeyProperty("event")).isEqualTo(event);
		}
	}

}

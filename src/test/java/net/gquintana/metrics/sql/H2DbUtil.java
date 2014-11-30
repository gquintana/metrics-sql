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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class H2DbUtil {
    public static final Class<? extends java.sql.Driver> DRIVER_CLASS= org.h2.Driver.class;
    public static final String URL="jdbc:h2:mem:metrics";
    public static final String USERNAME="sa";
    public static final String PASSWORD=null;
    private static final Logger LOGGER = LoggerFactory.getLogger(H2DbUtil.class);
    public static Connection openConnection() throws SQLException {
        try {
            DriverManager.registerDriver(DRIVER_CLASS.newInstance());
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new SQLException(reflectiveOperationException);
        }
        return DriverManager.getConnection(URL,USERNAME,PASSWORD);
    }
    public static void close(Object ... closeables) {
        for(Object closeable:closeables) {
            try {
                if (closeable instanceof AutoCloseable) {
                    ((AutoCloseable) closeable).close();
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to close "+closeable, exception);
            }
        }
    }
    public static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER_CLASS.getName());
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(3);
        
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
    public static void initTable(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()) {
            statement.execute("create table METRICS_TEST(ID int primary key, TEXT varchar(255), CREATED timestamp)");
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("insert into METRICS_TEST(ID, TEXT, CREATED) values(?,?,?)")) {
            for(int i=0; i<10; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "Text "+i);
                preparedStatement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                preparedStatement.execute();
            }
        }
        connection.commit();
    }
    public static void dropTable(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()) {
            statement.execute("drop table METRICS_TEST");
        }
    }
}

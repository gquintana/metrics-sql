/*
 * Default License
 */
package net.gquintana.metrics.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import javax.management.ObjectName;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
        ObjectName objectName = objectNameFactory.createName("Timer", "net.gquintana.metrics", timerName);
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

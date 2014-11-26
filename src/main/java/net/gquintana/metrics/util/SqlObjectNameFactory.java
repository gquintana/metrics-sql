/*
 * Default License
 */

package net.gquintana.metrics.util;

import com.codahale.metrics.DefaultObjectNameFactory;
import com.codahale.metrics.ObjectNameFactory;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Generates JMX {@link ObjectName} using JMX
 */
public class SqlObjectNameFactory implements ObjectNameFactory {
    private final ObjectNameFactory defaultObjectFactory;

    public SqlObjectNameFactory(ObjectNameFactory defaultObjectNameFactory) {
        this.defaultObjectFactory = defaultObjectNameFactory;
    }

    public SqlObjectNameFactory() {
        this.defaultObjectFactory = new DefaultObjectNameFactory();
    }
    
    private static final Pattern PATTERN = Pattern.compile(
            "(java\\.sql"           // Package
            + "\\.[A-Z]\\w+)"          // Class
            + "\\.(\\w+)"              // Database
            + "(?:\\.\\[([^]]*)\\])?"  // SQL Query
            + "(?:\\.(\\w+))?");       // Event
    @Override
    public ObjectName createName(String type, String domain, String name) {
        Matcher matcher = PATTERN.matcher(name);
        ObjectName objectName = null;
        if (matcher.matches()) {
            String className = matcher.group(1);
            String database  = matcher.group(2);
            String sql       = matcher.group(3);
            String event     = matcher.group(4);
            Hashtable<String, String> props = new Hashtable<>();
            props.put("class", className);
            props.put("database", database);
            if (sql!=null) {
                // , and \ are not allowed
                props.put("sql", sql.replaceAll("[:=*?,\\n\\\\]", " ").replaceAll("[\\s]+", " "));
            }
            if (event!=null) {
                props.put("event", event);
            }
            if (type!=null) {
                props.put("metricType", type);
            }
            try {
                objectName = new ObjectName(domain, props);
            } catch (MalformedObjectNameException malformedObjectNameException) {
            }
        } 
        if (objectName == null) {
            objectName = defaultObjectFactory.createName(type, domain, name);
        }
        return objectName;
    }

}

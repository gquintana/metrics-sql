/*
 * Default License
 */

package net.gquintana.metrics.sql;

import com.codahale.metrics.MetricRegistry;
import net.gquintana.metrics.proxy.AbstractProxyFactory;
import net.gquintana.metrics.proxy.CGLibProxyFactory;
import net.gquintana.metrics.proxy.ReflectProxyFactory;
import net.gquintana.metrics.util.ParametersBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import net.gquintana.metrics.proxy.CachingProxyFactory;

/**
 * Performance test
 */
@RunWith(Parameterized.class)
public class PerformanceTest {
    private final AbstractProxyFactory factory;
    private MetricRegistry metricRegistry;
    private JdbcProxyFactory proxyFactory;
    private DataSource rawDataSource;
    private DataSource dataSource;

    public PerformanceTest(AbstractProxyFactory factory) {
        this.factory = factory;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return new ParametersBuilder()
                .add(new ReflectProxyFactory())
                .add(new CGLibProxyFactory())
                .add(new CachingProxyFactory())
                .build();
    }
    @Before
    public void setUp() throws SQLException {
        metricRegistry = new MetricRegistry();
        proxyFactory = MetricsSql.forRegistry(metricRegistry)
                .withProxyFactory(factory).build();
        rawDataSource = H2DbUtil.createDataSource();
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.initTable(connection);
        }
        dataSource = proxyFactory.wrapDataSource("test", rawDataSource);
    }
    @After
    public void tearDown() throws SQLException {
        try(Connection connection = rawDataSource.getConnection()) {
            H2DbUtil.dropTable(connection);
        }
        H2DbUtil.close(dataSource);
    }
    @Test
    public void testPerformance() throws SQLException {
        for(int i=0;i<10;i++) { // Increase interations
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("insert into METRICS_TEST(ID, TEXT, CREATED) values (?,?,?)");
            for(int j=0;j<10;j++) {
                preparedStatement.setInt(1, i*10+j+100);
                preparedStatement.setString(2, "Performance #"+i*10+j);
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.execute();
            }
            H2DbUtil.close(preparedStatement);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from METRICS_TEST");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
            }
            H2DbUtil.close(resultSet);
            resultSet = statement.executeQuery("select * from METRICS_TEST order by ID desc limit 20");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String text = resultSet.getString("text");
                Timestamp timestamp = resultSet.getTimestamp("created");
            }
            H2DbUtil.close(resultSet, statement, connection);
        }        
    }
}

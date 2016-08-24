/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperLoaderDbTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderDbTest("atestPooledConnection"));
  }

  /**
   * 
   * @param name
   */
  public GrouperLoaderDbTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFindGrouperConnection() {
    //the name "hibernate" is a special term, which could be in the grouper-loader.properties, 
    //but defaults to grouper.hibernate.properties
    Properties properties = GrouperHibernateConfig.retrieveConfig().properties();
    
    String user = properties.getProperty("hibernate.connection.username");
    String url = properties.getProperty("hibernate.connection.url");

    GrouperStartup.startup();
    
    DataSource dataSource = GrouperLoaderDb.retrieveDataSourceFromC3P0(url, user);
    assertNotNull(dataSource);

  }
  
  /**
   * 
   */
  public void testFindConfigName() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.abc.user", "someUser");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.abc.url", "this@some/url");

    String configName = GrouperLoaderDb.retrieveConfigName("this@some/url", "someUser");
    
    assertEquals("abc", configName);
  }
  
  /**
   * to run this test, make sure you have a mysql database, user, table (test), and column (test)
   */
  public void atestPooledConnection() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouperLoader.db.connections.pool", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.abc.user", "test");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.abc.pass", "test1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.abc.url", "jdbc:mysql://localhost:3306/test?useSSL=false");

    GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile("abc");
    
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;

    try {
      connection = grouperLoaderDb.connection();

      try {      
        // create and execute a SELECT
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select * from test");

        while (resultSet.next()) {
          System.out.println(resultSet.getString(1));
        }
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
      
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
      
    }
  }
  
}

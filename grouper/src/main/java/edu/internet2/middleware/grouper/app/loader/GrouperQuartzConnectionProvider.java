package edu.internet2.middleware.grouper.app.loader;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.misc.GrouperStartup;


public class GrouperQuartzConnectionProvider implements ConnectionProvider {

  
  private GrouperLoaderDb grouperLoaderDb = null;
  
  public GrouperQuartzConnectionProvider() {
    grouperLoaderDb = new GrouperLoaderDb("grouper");
  }

  //private static long connNumber = 0;
  
  @Override
  public Connection getConnection() throws SQLException {
    //Sy stem.out.pri ntln("quartz connection " + connNumber++);
    Connection connection = this.grouperLoaderDb.connection();
    GrouperQuartzConnection grouperQuartzConnection = new GrouperQuartzConnection(connection);
    return grouperQuartzConnection;
  }

  @Override
  public void shutdown() throws SQLException {
    // nada
  }

  @Override
  public void initialize() throws SQLException {
    GrouperStartup.startup();
  }

}

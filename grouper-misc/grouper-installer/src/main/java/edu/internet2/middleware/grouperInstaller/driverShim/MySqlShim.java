package edu.internet2.middleware.grouperInstaller.driverShim;

/**
 * mysql driver in right classloader
 * @author mchyzer
 *
 */
public class MySqlShim extends DatabaseShimBase {

  /**
   * 
   * @return driver class name
   */
  @Override
  public String getDriverClassName() {
    return "com.mysql.jdbc.Driver";
  }

}

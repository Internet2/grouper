package edu.internet2.middleware.grouperInstaller.driverShim;

/**
 * sql server driver in right classloader
 * @author mchyzer
 *
 */
public class SqlServerShim extends DatabaseShimBase {

  /**
   * 
   * @return driver class name
   */
  @Override
  public String getDriverClassName() {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  }

}

package edu.internet2.middleware.grouperInstaller.driverShim;

/**
 * hsql driver in right classloader
 * @author mchyzer
 *
 */
public class HsqlShim extends DatabaseShimBase {

  /**
   * 
   * @return driver class name
   */
  @Override
  public String getDriverClassName() {
    return "org.hsqldb.jdbcDriver";
  }

}

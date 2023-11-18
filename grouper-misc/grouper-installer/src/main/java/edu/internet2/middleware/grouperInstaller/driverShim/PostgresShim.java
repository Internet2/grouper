package edu.internet2.middleware.grouperInstaller.driverShim;

/**
 * postgres driver in right classloader
 * @author mchyzer
 *
 */
public class PostgresShim extends DatabaseShimBase {

  /**
   * 
   * @return driver class name
   */
  @Override
  public String getDriverClassName() {
    return "org.postgresql.Driver";
  }

}

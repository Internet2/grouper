package edu.internet2.middleware.grouperInstaller.driverShim;

/**
 * oracle driver in right classloader
 * @author mchyzer
 *
 */
public class OracleShim extends DatabaseShimBase {

  /**
   * 
   * @return driver class name
   */
  @Override
  public String getDriverClassName() {
    return "oracle.jdbc.driver.OracleDriver";
  }

}

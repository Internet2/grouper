package edu.internet2.middleware.grouperInstaller.driverShim;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * driver in right classloader
 * @author mchyzer
 *
 */
public abstract class DatabaseShimBase implements Driver {
  
  /**
   * 
   */
  private static File jarFile = null;

  /**
   * 
   * @param theJarFile
   */
  public static void init(File theJarFile) {
    jarFile = theJarFile;
  }

  /**
   * return the driver classname
   * @return classname
   */
  public abstract String getDriverClassName();
  
  /**
   * construct
   */
  public DatabaseShimBase() {
    
    try {
      
      URL u = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/");
      
      String classname = this.getDriverClassName();
      URLClassLoader ucl = new URLClassLoader(new URL[] { u });
      this.driver = (Driver)Class.forName(classname, true, ucl).newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Problem loading driver from: " + (jarFile == null ? null : jarFile.getAbsolutePath()), e);
    }
  }
  
  /**
   * driver
   */
  private Driver driver;
  
  /**
   * 
   */
  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    return this.driver.connect(url, info);
  }

  /**
   * 
   */
  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return this.driver.acceptsURL(url);
  }

  /**
   * 
   */
  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
      throws SQLException {
    return this.driver.getPropertyInfo(url, info);
  }

  /**
   * 
   */
  @Override
  public int getMajorVersion() {
    return this.driver.getMajorVersion();
  }

  /**
   * 
   */
  @Override
  public int getMinorVersion() {
    return this.driver.getMinorVersion();
  }

  /**
   * 
   */
  @Override
  public boolean jdbcCompliant() {
    return this.driver.jdbcCompliant();
  }

  // in JDK7 is an @override, due to new method added to CommonDataSource interface
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    try {
      return (Logger) Driver.class.getDeclaredMethod("getParentLogger").invoke(this.driver);
    } catch (Throwable e) {
      return null;
    }
  }

}

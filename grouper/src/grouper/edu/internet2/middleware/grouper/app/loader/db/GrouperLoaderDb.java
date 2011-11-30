/*
 * @author mchyzer
 * $Id: GrouperLoaderDb.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Connection;
import java.sql.DriverManager;

import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;


/**
 * db profile from grouper.properties (or possibly grouper.hibernate.properties)
 */
public class GrouperLoaderDb {
  
  /** user to login to db */
  private String user;
  
  /** pass to login to db */
  private String pass;
  
  /** url of the db to login to */
  private String url;
  
  /** db driver to use to login */
  private String driver;

  /**
   * get a connection from the db
   * @return the connection
   */
  public Connection connection() {
    try {
      Class.forName (this.driver);
  
      // connect
      Connection connection = DriverManager.getConnection(this.url,this.user, this.pass);
      return connection;
    } catch (Exception e) {
      throw new RuntimeException("Problems with db: " + this, e);
    }
    
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() { 
    return "DB: user: " + this.user + ", url: " + this.url + ", driver: " + this.driver;
  }
  
  /**
   * user to login to db
   * @return the user
   */
  public String getUser() {
    return this.user;
  }

  
  /**
   * user to login to db
   * @param user1 the user to set
   */
  public void setUser(String user1) {
    this.user = user1;
  }

  
  /**
   * pass to login to db
   * @return the pass
   */
  public String getPass() {
    return this.pass;
  }

  
  /**
   * pass to login to db
   * @param pass1 the pass to set
   */
  public void setPass(String pass1) {
    this.pass = pass1;
  }

  
  /**
   * url of the db to login to
   * @return the url
   */
  public String getUrl() {
    return this.url;
  }

  
  /**
   * url of the db to login to
   * @param url1 the url to set
   */
  public void setUrl(String url1) {
    this.url = url1;
  }

  
  /**
   * db driver to use to login
   * @return the driver
   */
  public String getDriver() {
    return this.driver;
  }

  
  /**
   * db driver to use to login
   * @param driver1 the driver to set
   */
  public void setDriver(String driver1) {
    this.driver = driver1;
  }

  /**
   * empty constructor
   */
  public GrouperLoaderDb() {
    //empty  
  }
  
  /**
   * construct with all fields
   * @param user1
   * @param pass1
   * @param url1
   * @param driver1
   */
  public GrouperLoaderDb(String user1, String pass1, String url1, String driver1) {
    super();
    this.user = user1;
    this.pass = pass1;
    this.url = url1;
    this.driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(url1, driver1);
  }

  
}

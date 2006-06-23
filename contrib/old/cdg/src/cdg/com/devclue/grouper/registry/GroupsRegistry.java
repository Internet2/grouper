/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.registry;

import  edu.internet2.middleware.grouper.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;

/**
 * Provides access to the Groups Registry outside of the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupsRegistry.java,v 1.1 2006-06-23 17:30:10 blair Exp $
 */
public class GroupsRegistry {

  // PRIVATE CLASS CONSTANTS //
  private static final String CF = GrouperConfig.HIBERNATE_CF;


  // PRIVATE INSTANCE VARIABLES //
  private Connection  conn;                           // JDBC connection
  private String      driver;                         // JDBC driver
  private Properties  properties  = new Properties(); // JDBC properties


  // CONSTRUCTORS //

  /**
   * Create a Registry object for interacting with the Groups Registry
   * outside of the Grouper API.
   * <pre class="eg">
   * GroupsRegistry gr = new GroupsRegistry();
   * </pre>
   */
  public GroupsRegistry() {
    properties = new Properties();
    this._readConfig();
    this._connect();
  } // public GroupsRegistry()


  // PUBLIC INSTANCE METHODS //

  /**
   * Return a shared JDBC connection to the Groups Registry.
   * <pre class="eg">
   * GroupsRegistry gr = new GroupsRegistry();
   * try {
   *   Connection conn = gr.getConnection();
   * }
   * catch (RuntimeException e) {
   *   // Failed to get connection
   * }
   * </pre>
   * @return  A JDBC connection.
   */
  public Connection getConnection() {
    try {
      if ( (this.conn !=null) && (!this.conn.isReadOnly()) ) {
        return this.conn;
      }
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to return JDBC connection: " + e.getMessage()
      );
    }
    throw new RuntimeException("Unable to return JDBC connection"); 
  } // public Connection getConnection()

  /**
   * Resets Groups Registry to pristine state.
   * <p>
   * Removes all stems, groups, memberships, privileges, members and 
   * subjects that have been aded to the Groups Registry.  The 
   * <i>GrouperSystem</i> subject and schema information are left
   * untouched.
   * </p>
   * <p>
   * Subjects are not reliably removed at this point, at least when
   * using HSQLDB.
   * </p>
   * <pre class="eg">
   * // Reset the Groups Registry to an almost pristine state.
   * GroupsRegistry gr = new GroupsRegistry();
   * try {
   *   gr.reset();
   * }
   * catch (RuntimeException e) {
   *   // Error performing reset
   * }
   * </pre>
   */
  public void reset() {
    try {
      RegistryReset.reset();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public void reset()

  /**
   * Close connection to the Groups Registry.
   * <pre class="eg">
   * // Close the connection to the Groups Registry.
   * GroupsRegistry gr = new GroupsRegistry();
   * gr.stop();
   * </pre>
  `*/
  public void stop() {
    try {
      this.conn.close();
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to close connection: " + e.getMessage()
      );
    }
  } // public void stop()


  // PRIVATE INSTANCE METHODS //

  // Connect to the Groups Registry via JDBC.
  private void _connect() {
    String klass  = properties.getProperty(
      "hibernate.connection.driver_class"
    );
    String pw     = properties.getProperty(
      "hibernate.connection.password"
    );
    String url    = properties.getProperty(
      "hibernate.connection.url"
    );
    String user   = properties.getProperty(
      "hibernate.connection.username"
    );
    try {
     Class.forName(klass);
    } 
    catch (ClassNotFoundException e) {
      throw new RuntimeException(
        "Unable to load driver " + klass + ": " + e.getMessage()
      );
    }
    try {
     this.conn = DriverManager.getConnection(url, user, pw);
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to create connection to " + url + ":" + e.getMessage()
      );
    }
  } // private void _connect()

  // Read Grouper's Hibernate configuration
  private void _readConfig() {
    InputStream in = GroupsRegistry.class
                                   .getResourceAsStream("/" + CF);
    try {
      this.properties.load(in);
    } 
    catch (IOException e) {
      throw new RuntimeException(
        "Error loading " + CF + ": " + e.getMessage()
      );
    }
  } // private void _readConfig()

}


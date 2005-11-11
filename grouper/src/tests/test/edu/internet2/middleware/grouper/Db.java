/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package test.edu.internet2.middleware.grouper;

import  java.io.*;
import  java.sql.*;
import  java.util.*;

/**
 * Refresh the Groups Registry database and schema.
 * <p>
 * Hibernate's SchemaUpdate, of course, failed me here, just like
 * DbUnit did earlier.  Oh well.
 * </p>
 * @author  blair christensen.
 * @version $Id: Db.java,v 1.2 2005-11-11 18:39:35 blair Exp $
 */
class Db {

  // Private Class Constants
  private static final String CF = "hibernate.properties";

  
  // Private Class Variables
  private static Connection  conn;
  private static String      driver;
  private static Properties  properties = new Properties();


  // Protected Class methods
  
  protected static void refreshDb() {
    _readConfig();
    _connect();
    try {
      conn.setAutoCommit(false);
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Error disabling autocommit: " + e.getMessage()
      );
    }
    _emptyTable("grouper_memberships");
    _emptyTable("grouper_sessions");
    _emptyTable("grouper_attributes");
    _emptyTable("grouper_groups");
    _emptyTable("grouper_stems");
    _emptyTable("grouper_factors");
    _emptyTable("grouper_members");
    try {
      conn.commit();
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Error committing changes: " + e.getMessage()
      );
    }
    _stop();
  } // protected static void refreshDB()


  // Private Class Methods

  private static void _connect() {
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
      conn = DriverManager.getConnection(url, user, pw);
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to create connection to " + url + ":" + e.getMessage()
      );
    }
  } // private static void _connect()

  private static void _emptyTable(String table) {
    try {
      PreparedStatement del = conn.prepareStatement(
        "DELETE FROM " + table
      );
      try {
        del.executeUpdate();
      } 
      catch (SQLException e) {
        throw new RuntimeException(
          "Error emptying table " + table + ": " + e.getMessage()
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException(
        "Error preparing statement for " + table + ": "  + e.getMessage()
      );
    }
  } // private static void _emptyTable(table)

  private static void _readConfig() {
    InputStream in = Db.class
                       .getResourceAsStream("/" + CF);
    try {
      properties.load(in);
    } 
    catch (IOException e) {
      throw new RuntimeException(
        "Error loading " + CF + ": " + e.getMessage()
      );
    }
  } // private static void _readConfig()

  private static void _stop() {
    try {
      conn.close();
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to close connection: " + e.getMessage()
      );
    }
  } // private static void _stop()

}


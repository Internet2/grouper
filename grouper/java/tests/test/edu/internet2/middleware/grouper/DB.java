/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  java.io.*;
import  java.sql.*;
import  java.util.*;


/**
 * A poor man's DbUnit.
 */
public class DB {

  /*
   * PRIVATE CLASS CONSTANTS
   */
  private static final String CF = "hibernate.properties";


  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private Connection  conn;
  private String      driver;
  private Properties  properties;


  /*
   * CONSTRUCTORS
   */

  public DB() {
    properties = new Properties();
    this._readConfig();
    this._connect();
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public void emptyTables() {
    try {
      this.conn.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException("Error disabling autocommit: " + e);
    }
    this._emptyTable("grouper_attribute");
    this._emptyTable("grouper_group");
    this._emptyTable("grouper_list");
    this._emptyTable("grouper_member");
    this._emptyTable("grouper_memberVia");
    this._emptyTable("grouper_schema");
    this._emptyTable("grouper_session");
    try {
      this.conn.commit();
    } catch (SQLException e) {
      throw new RuntimeException("Error committing changes: " + e);
    }
  }

  public void stop() {
    try {
      this.conn.close();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to close connection: " + e);
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

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
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
                  "Unable to load driver " + klass + ": " + e
                );
    }
    try {
     this.conn = DriverManager.getConnection(url, user, pw);
    } catch (SQLException e) {
      throw new RuntimeException(
                  "Unable to create connection to " + url + ":" + e
                );
    }
  }

  private void _emptyTable(String table) {
    try {
      PreparedStatement del = this.conn.prepareStatement(
                                "DELETE FROM " + table
                              );
      try {
        del.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(
                    "Error emptying table " + table + ": " + e
                  );
      }
    } catch (SQLException e) {
      throw new RuntimeException(
                  "Error preparing statement for " + table + ": "  + e
                );
    }
  }

  private void _readConfig() {
    InputStream in = DB.class
                       .getResourceAsStream("/" + CF);
    try {
      this.properties.load(in);
    } catch (IOException e) {
      throw new RuntimeException("Error loading " + CF + ": " + e);
    }
  }

}


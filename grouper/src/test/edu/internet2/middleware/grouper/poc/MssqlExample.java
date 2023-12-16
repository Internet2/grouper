/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;


/**
 *
 */
public class MssqlExample {

  private static Driver globalDriver = null;
  
  private static Connection globalConnection = null;
  
  private static PreparedStatement globalPreparedStatement = null;
  
  //  runQueryRaw: 1392ms
  //  runQueryConnection: 16ms
  //  runQueryConnectionStatement: 11ms
  private static String type = "runQueryConnection";
  
  
  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws Exception {

    globalDriver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
    
    globalConnection = getConnection();
    globalPreparedStatement = globalConnection.prepareStatement("select count(1) as the_count from grouper_groups");
    
    runQueries();

    globalPreparedStatement.close();
    globalConnection.close();

  }

  private static Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:postgresql://localhost:5432/grouper_v2_6?currentSchema=public", "grouper", "pass");
  }

  public static void runQueries() throws Exception {
    for (String theType : new String[] {"runQueryRaw", "runQueryConnection", "runQueryConnectionStatement", "runQueryGrouper", "runQueryGrouperRaw"}) {
      type = theType;
      runQuery();

      //  runQueryRaw: 1364ms
      //  runQueryConnection: 17ms
      //  runQueryConnectionStatement: 11ms
      //  runQueryGrouper: 29ms
      //  runQueryGrouperRaw: 33ms    
      
      // Test
      //  runQueryRaw: 7508ms
      //  runQueryConnection: 980ms
      //  runQueryConnectionStatement: 940ms
      //  runQueryGrouper: 1661ms
      //  runQueryGrouperRaw: 1155ms

      long startedNanos = System.nanoTime();
      for (int i=0;i<100;i++) {
        runQuery();
      }

      System.out.println(type + ": " + ((System.nanoTime() - startedNanos) / 1000000) + "ms" );
      
    }

  }
  
  public static void runQuery() throws Exception {
    if ("runQueryRaw".equals(type)) {
      runQueryRaw();
    } else if ("runQueryConnection".equals(type)) {
      runQueryConnection(globalConnection);
    } else if ("runQueryConnectionStatement".equals(type)) {
      runQueryConnectionStatement(globalConnection, globalPreparedStatement);
    } else if ("runQueryGrouper".equals(type)) {
      runQueryGrouper();
    } else if ("runQueryGrouperRaw".equals(type)) {
      runQueryGrouperRaw();
    }
  }
  
  public static void runQueryConnectionStatement(Connection connection, PreparedStatement statement) throws Exception {
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
        String col = resultSet.getString("the_count");
        //System.out.println(col);
    }
    resultSet.close();

  }
  
  public static void runQueryRaw() throws Exception {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select count(1) as the_count from grouper_groups");
    while (resultSet.next()) {
        String col = resultSet.getString("the_count");
        //System.out.println(col);
    }
    resultSet.close();
    statement.close();
    connection.close();

  }

  public static void runQueryConnection(Connection connection) throws Exception {
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select count(1) as the_count from grouper_groups");
    while (resultSet.next()) {
        String col = resultSet.getString("the_count");
        //System.out.println(col);
    }
    resultSet.close();
    statement.close();

  }

  public static void runQueryGrouper() throws Exception {
    new GcDbAccess().sql("select count(1) as the_count from grouper_groups").select(int.class);
  }

  public static void runQueryGrouperRaw() throws Exception {
    GrouperLoaderDb grouperLoaderDb = new GrouperLoaderDb("grouper");
    Connection connection = grouperLoaderDb.connection();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select count(1) as the_count from grouper_groups");
    while (resultSet.next()) {
        String col = resultSet.getString("the_count");
        //System.out.println(col);
    }
    resultSet.close();
    statement.close();
    connection.close();
    
  }

}

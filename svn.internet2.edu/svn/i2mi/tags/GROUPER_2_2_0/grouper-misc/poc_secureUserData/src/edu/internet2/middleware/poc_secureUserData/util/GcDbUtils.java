/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.driver.OracleDriver;

import org.apache.log4j.Logger;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GcDbUtils {

  /** 
   * oracle types
   */
  public static enum DbType {

    /** string type */
    STRING {

      /**
       * 
       * @see edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType#processResultSet(java.sql.ResultSet, int)
       */
      @Override
      public Object processResultSet(ResultSet resultSet, int indexZeroIndexed) {
        try {
          return resultSet.getString(indexZeroIndexed+1);
        } catch (SQLException e) {
          throw new RuntimeException("Error reading col (zero indexed) " + indexZeroIndexed, e);
        }
      }

      /**
       * @see edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType#attachParam(PreparedStatement, Object, int)
       */
      @Override
      public void attachParam(PreparedStatement preparedStatement, Object arg,
          int indexZeroIndexed) {
        String argString = GrouperClientUtils.stringValue(arg);
        try {
          preparedStatement.setString(indexZeroIndexed+1, argString);
        } catch (SQLException e) {
          throw new RuntimeException("Error setting param (zero indexed) " + indexZeroIndexed + ": " + argString, e);
        }
      }
    };

    /**
     * get the data
     * @param resultSet
     * @param indexZeroIndexed
     * @return the object or null
     */
    public abstract Object processResultSet(ResultSet resultSet, int indexZeroIndexed);
    
    /**
     * attach a param to a prepared statement
     * @param preparedStatement
     * @param arg
     * @param indexZeroIndexed
     */
    public abstract void attachParam(PreparedStatement preparedStatement, Object arg, int indexZeroIndexed);
    
    /**
     * convert an object arg to a dbtype
     * @param object
     * @return dbtype
     */
    public static DbType fromObject(Object object) {
      if (object == null || object instanceof String) {
        return DbType.STRING;
      }
      throw new RuntimeException("Unsupported type: " + GrouperClientUtils.className(object) + ", " + object);
    }
    
  }
  
  /**
   * get a connection to the oracle DB
   * @return a connection
   */
  public static Connection connection() {
    
    String oracleUrl = null;
    
    try {
      Class.forName(OracleDriver.class.getName());
  
      oracleUrl = GrouperClientUtils.propertiesValue("oracle.sud.url", true);
      String oracleUser = GrouperClientUtils.propertiesValue("oracle.sud.user", true);
      String oraclePass = GrouperClientUtils.propertiesValue("oracle.sud.pass", true);
      
      Connection conn = DriverManager.getConnection(oracleUrl, oracleUser, oraclePass);
      
      return conn;
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to: " + oracleUrl, e);
    }
  }

  /**
   * run a query and get rows back
   * @param rowType type of each row returned, e.g. String.class or Object[]
   * @param <T> generic type
   * @param query
   * @param returnColTypes
   * @return the list of objects
   */
  public static <T> List<T> listSelect(Class<T> rowType, String query, List<DbType> returnColTypes) {
    return listSelect(rowType, query, returnColTypes, null);
  }

  /**
   * run a query and get rows back
   * @param rowType type of each row returned, e.g. String.class or Object[]
   * @param <T> generic type
   * @param query
   * @param returnColTypes
   * @param args preparedstatement arguments
   * @return the list of objects
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> listSelect(Class<T> rowType, String query, List<DbType> returnColTypes, List<Object> args) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<T> results = new ArrayList<T>();
    
    try {
      
      int returnColTypesLength = GrouperClientUtils.length(returnColTypes);
      if (returnColTypesLength==0) {
        throw new RuntimeException("Why is returnColTypesLength == 0???");
      }
      
      connection = GcDbUtils.connection();
      preparedStatement = connection.prepareStatement(query);
      
      //attach arguments
      int argsLength = GrouperClientUtils.length(args);

      for (int i=0; i<argsLength;i++) {
        Object arg = args.get(i);
        DbType dbType = DbType.fromObject(arg);
        dbType.attachParam(preparedStatement, arg, i);
      }
      
      resultSet = preparedStatement.executeQuery();

      if (rowType.isArray() != returnColTypesLength>1) {
        throw new RuntimeException("If returnColTypesLength > 1 (" + returnColTypesLength + ") then you must pass in an array as the return type");
      }
      
      while (resultSet.next()) {
        
        //if array of cols
        if (rowType.isArray()) {
        
          Object[] result = new Object[returnColTypesLength];
          results.add((T)result);
          
          for (int i=0; i<returnColTypesLength;i++) {
            result[i] = returnColTypes.get(i).processResultSet(resultSet, i);
          }
        } else {
          //if one col
          results.add((T)returnColTypes.get(0).processResultSet(resultSet, 0));
        }
        
      }
    } catch (Exception e) {
      throw new RuntimeException("Error with query: " + query, e);
    } 
    finally {
      rollbackQuietly(connection);
      GrouperClientUtils.closeQuietly(resultSet);
      GrouperClientUtils.closeQuietly(preparedStatement);
      GrouperClientUtils.closeQuietly(connection);
    }
    return results;

  }
  
  /**
   * rollback quiently
   * @param connection
   */
  public static void rollbackQuietly(Connection connection) {
    try {
      if (connection != null) {
        connection.rollback();
      }
    } catch (Exception e) {
      LOG.error("Problem rolling back", e);
      //probably should rethrow but dont want to mess up the other closes...
    }

  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @param arg preparedstatement argument
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
   */
  public static int executeUpdate(String query, Object arg) {
    
    List<Object> args = GrouperClientUtils.toList(arg);
    return executeUpdate(query, args);
    
  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
   *         or (2) 0 for SQL statements that return nothing
   */
  public static int executeUpdate(String query) {
    
    return executeUpdate(query, null);
    
  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @param args preparedstatement arguments
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
   */
  public static int executeUpdate(String query, List<Object> args) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {
      connection = GcDbUtils.connection();
      preparedStatement = connection.prepareStatement(query);
      
      //attach arguments
      int argsLength = GrouperClientUtils.length(args);

      for (int i=0; i<argsLength;i++) {
        Object arg = args.get(i);
        DbType dbType = DbType.fromObject(arg);
        dbType.attachParam(preparedStatement, arg, i);
      }
      
      int result = preparedStatement.executeUpdate();
      
      connection.commit();
      
      return result;
      
    } catch (Exception e) {
      rollbackQuietly(connection);
      throw new RuntimeException("Error with query: " + query, e);
    } 
    finally {
      GrouperClientUtils.closeQuietly(preparedStatement);
      GrouperClientUtils.closeQuietly(connection);
    }

  }

  /**
   * 
   */
  private static Logger LOG = Logger.getLogger(GcDbUtils.class);
}

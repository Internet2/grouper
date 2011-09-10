/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class SudOracleUtils {

  /** 
   * oracle types
   */
  public static enum DbType {
    
    /** string type */
    STRING {

      /**
       * 
       * @see edu.internet2.middleware.poc_secureUserData.SudOracleUtils.DbType#processResultSet(java.sql.ResultSet, int)
       */
      @Override
      public Object processResultSet(ResultSet resultSet, int indexZeroIndexed) {
        try {
          return resultSet.getString(indexZeroIndexed+1);
        } catch (Exception e) {
          throw new RuntimeException("Error reading col (zero indexed) " + indexZeroIndexed, e);
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
  }
  
  /**
   * get a connection to the oracle DB
   * @return a connection
   */
  public static Connection connection() {
    
    String oracleUrl = null;
    
    try {
      Class.forName("oracle.jdbc.OracleDriver");
  
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
   * @param query
   * @param returnColTypes
   * @return the list of objects
   */
  public static List<Object[]> retrieveRows(String query, List<DbType> returnColTypes) {
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    List<Object[]> results = new ArrayList<Object[]>();
    
    try {
      connection = SudOracleUtils.connection();
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      int returnColTypesLength = GrouperClientUtils.length(returnColTypes);

      while (resultSet.next()) {
        Object[] result = new Object[returnColTypesLength];
        results.add(result);
        
        for (int i=0; i>returnColTypesLength;i++) {
          result[i] = returnColTypes.get(i).processResultSet(resultSet, i);
        }
        
      }
    } catch (Exception e) {
      throw new RuntimeException("Error with query: " + query, e);
    } 
    finally {
      GrouperClientUtils.closeQuietly(resultSet);
      GrouperClientUtils.closeQuietly(statement);
      GrouperClientUtils.closeQuietly(connection);
    }
    return results;

  }
  
}

/*
 * @author mchyzer
 * $Id: GrouperLoaderUtils.java,v 1.1 2008-04-28 06:40:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.Group;


/**
 * utility methods for grouper loader
 */
public class GrouperLoaderUtils {

  /**
   * close a connection null safe and dont throw exception
   * @param connection
   */
  public static void closeQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
  /**
   * rollback a connection quietly
   * @param connection
   */
  public static void rollbackQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
  /**
   * close a statement null safe and dont throw exception
   * @param statement
   */
  public static void closeQuietly(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
  /**
   * close a resultSet null safe and dont throw exception
   * @param resultSet
   */
  public static void closeQuietly(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
  /**
   * get a group attribute without worrying about exceptions.  return null if not there
   * @param group
   * @param attributeName
   * @return the attribute or null if not there
   */
  public static String groupGetAttribute(Group group, String attributeName) {
    try {
      return group.getAttribute(attributeName);
    } catch (AttributeNotFoundException anfe) {
      return null;
    }
  }

}

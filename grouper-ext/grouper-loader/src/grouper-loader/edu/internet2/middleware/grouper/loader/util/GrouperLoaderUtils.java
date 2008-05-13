/*
 * @author mchyzer
 * $Id: GrouperLoaderUtils.java,v 1.3 2008-05-13 07:11:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.util;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * utility methods for grouper loader
 */
public class GrouperLoaderUtils {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoaderUtils.class);


  /** cache the hostname, it wont change */
  private static String hostname = null;

  /**
   * get the hostname of this machine
   * @return the hostname
   */
  public static String hostname() {

    if (StringUtils.isBlank(hostname)) {

      //get the hostname
      hostname = "unknown";
      try {
        InetAddress addr = InetAddress.getLocalHost();

        // Get hostname
        hostname = addr.getHostName();
      } catch (Exception e) {
        LOG.error("Cant find servers hostname: ", e);
      }
    }

    return hostname;
  }

  
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
   * rollback a transaction quietly
   * @param transaction
   */
  public static void rollbackQuietly(Transaction transaction) {
    if (transaction != null && transaction.isActive()) {
      try {
        transaction.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
  /**
   * close a session null safe and dont throw exception
   * @param session
   */
  public static void closeQuietly(Session session) {
    if (session != null) {
      try {
        session.close();
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

  /**
   * find the length of ascii chars (non ascii are counted as two)
   * @param input is the string to operate on
   * @param requiredLength length we need the string to be
   * @return the length of ascii chars
   */
  public static String truncateAscii(String input, int requiredLength) {
    if (input == null) {
      return input;
    }
    //see what real length is
    int utfLength = input.length();
    
    //see if not worth checking
    if (utfLength * 2 < requiredLength) {
      return input;
    }
    
    //count how many non asciis
    int asciiLength = 0;
    for (int i=0;i<utfLength;i++) {
      
      asciiLength++;
      
      //keep count of non ascii chars
      if (!isAscii(input.charAt(i))) {
        asciiLength++;
      }
      
      //see if we are over 
      if (asciiLength > requiredLength) {
        //do not include the current char
        return input.substring(0,i);
      }
    }
    //must have fit
    return input;
  }

  /**
   * find the length of ascii chars (non ascii are counted as two)
   * @param input
   * @return the length of ascii chars
   */
  public static int lengthAscii(String input) {
    if (input == null) {
      return 0;
    }
    //see what real length is
    int utfLength = input.length();
    //count how many non asciis
    int extras = 0;
    for (int i=0;i<utfLength;i++) {
      //keep count of non ascii chars
      if (!isAscii(input.charAt(i))) {
        extras++;
      }
    }
    return utfLength + extras;
  }

  /**
   * is ascii char
   * @param input
   * @return true if ascii
   */
  public static boolean isAscii(char input) {
    return input < 128;
  }

  /**
   * convert an object to a list of objects
   * @param object
   * @return the list of objects
   */
  @SuppressWarnings("unchecked")
  public static List<Object> listObject(Object object) {
    
    //if its already a list then we are all good
    if (object instanceof List) {
      return (List<Object>)object; 
    }
    
    return GrouperUtil.toList(object);

  }
  
  /**
   * convert a subject to string safely
   * @param subject
   * @return the string value of subject (might be null)
   */
  public static String subjectToString(Subject subject) {
    if (subject == null) {
      return null;
    }
    return "Subject id: " + subject.getId() + ", sourceId: " + subject.getSource().getId();
  }
  
}

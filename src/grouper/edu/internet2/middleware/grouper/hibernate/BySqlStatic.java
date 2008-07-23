/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * for simple HQL, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
 * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class BySqlStatic {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(BySqlStatic.class);

  
  /**
   * constructor
   *
   */
  BySqlStatic() {}


  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public static int executeSql(final String sql, final List<Object> params) {
  
    int result = (Integer)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession)
          throws GrouperDAOException {
        PreparedStatement preparedStatement = null;
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = hibernateSession.getSession().connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params);
          
          int result = preparedStatement.executeUpdate();
          
          return result;

        } catch (Exception e) {
          throw new RuntimeException("Problem with query: " + sql, e);
        } finally {
          GrouperUtil.closeQuietly(preparedStatement);
        }
      }
      
    });
    return result;
  
  }


  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public static void attachParams(PreparedStatement statement, Object params)
      throws HibernateException, SQLException {
    if (GrouperUtil.length(params) == 0) {
      return;
    }
    List<Object> paramList = GrouperUtil.toList(params);
    List<Type> typeList = HibUtils.hibernateTypes(paramList);
    attachParams(statement, paramList, typeList);
  }


  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @param types either null, Type, Type[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   */
  public static void attachParams(PreparedStatement statement, Object params, Object types)
      throws HibernateException, SQLException {
    int paramLength = GrouperUtil.length(params);
    int typeLength = GrouperUtil.length(types);
    
    //nothing to do if nothing to do
    if (paramLength == 0 && typeLength == 0) {
      return;
    }
  
      if (paramLength != typeLength) {
      throw new RuntimeException("The params length must equal the types length and params " +
      "and types must either both or neither be null");
      }
  
  
    List paramList = GrouperUtil.toList(params);
    List typeList = GrouperUtil.toList(types);
  
  
      //loop through, set the params
      Type currentType = null;
      for (int i = 0; i < paramLength; i++) {
        //not sure why the session implementer is null, if this ever fails for a type, 
        //might want to not use hibernate and brute force it
        currentType = (Type) typeList.get(i);
        currentType.nullSafeSet(statement, paramList.get(i), i + 1, null);
      }
  
  }
  
  
  
}

/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class HibUtils {

  
  /**
   * find the property index based on property name
   * @param propertyNames
   * @param propertyName e.g. userId
   * @return the index (0 based) in the data arrays where the object is
   */
  public static int propertyIndex(String[] propertyNames, String propertyName) {
    int propertiesSize = GrouperUtil.length(propertyNames);
    for (int i=0;i<propertiesSize;i++) {
      if (StringUtils.equals(propertyNames[i], propertyName)) {
        return i;
      }
    }
    throw new RuntimeException("Cant find property: " + propertyName 
        + " in list: " + GrouperUtil.toStringForLog(propertyNames));
  }

  /**
   * assign a property in hibernates arrays of states
   * @param state
   * @param propertyNames
   * @param propertyName
   * @param propertyValue
   */
  public static void assignProperty(Object[] state, String[] propertyNames, 
      String propertyName, Object propertyValue) {
    //first find which index
    int propertyIndex = propertyIndex(propertyNames, propertyName);
    //next assign the value
    state[propertyIndex] = propertyValue;
  }
  
  /**
   * find a property value in hibernates arrays of states
   * @param state
   * @param propertyNames
   * @param propertyName
   * @return the object
   */
  public static Object propertyValue(Object[] state, String[] propertyNames, 
      String propertyName) {
    //first find which index
    int propertyIndex = propertyIndex(propertyNames, propertyName);
    //next get the value
    return state[propertyIndex];
  }
  
  /**
   * close a prepared statement
   * @param preparedStatement
   */
  public static void closeQuietly(PreparedStatement preparedStatement) {
    try {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
    } catch (Exception e) {
      //forget about it
    }
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession, can be null if not known
   * @param object to evict that was just retrieved, can be list or array
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession,
      Object object, boolean onlyEvictIfNotNew) {
    if (object instanceof List) {
      HibUtils.evict(hibernateSession, (List)object, onlyEvictIfNotNew);
      return;
    }
    
    //dont worry about it if new and only evicting if not new
    if (hibernateSession != null && hibernateSession.isNewHibernateSession() && onlyEvictIfNotNew) {
      return;
    }
    
    //if array, loop through
    if (object != null && object.getClass().isArray()) {
      for (int i=0;i<Array.getLength(object);i++) {
        HibUtils.evict(hibernateSession, Array.get(object, i), onlyEvictIfNotNew);
      }
      return;
    }
    
    //not sure it could ever be null...
    if (object != null) {
      hibernateSession.getSession().evict(object);
    }
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession
   * @param list of objects from hibernate to evict
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession,
      List<Object> list, boolean onlyEvictIfNotNew) {
    if (list == null) {
      return;
    }
    for (Object object : list) {
      evict(hibernateSession, object, onlyEvictIfNotNew);
    }
  }

  /** 
   * Logger
   */
  //private static FastLogger log = new FastLogger(HibUtils.class);
  
  /**
   * make a list of criterions.  e.g. listCrit(crit1, crit2, etc).  will AND them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCrit(Criterion... criterions) {
    return listCritHelper(Restrictions.conjunction(), criterions);
  }

  /**
   * make a list of criterions.  e.g. listCrit(critList).  will AND them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCrit(List<Criterion> criterions) {
    return listCritHelper(Restrictions.conjunction(), GrouperUtil.toArray(criterions, Criterion.class));
  }

  /**
   * make a list of criterions.  e.g. listCrit(crit1, crit2, etc).  will AND or OR them together
   * this is null and empty safe
   * @param junction either conjunction or disjunction
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  private static Criterion listCritHelper(Junction junction, Criterion... criterions) {
    int criterionsLength = GrouperUtil.length(criterions);
    if (criterionsLength == 0) {
      return null;
    }
    
    //if one no need for junction
    if (criterionsLength == 1 && criterions[0] != null) {
      return criterions[0];
    }
    
    //count to see if any added
    int resultsCount = 0;
    for (int i=0;i<criterionsLength;i++) {
      Criterion current = criterions[i];
      if (current != null) {
        resultsCount++;
        junction.add(current);
      }
    }
    if (resultsCount == 0) {
      return null;
    }
    return junction;
    
  }
  
  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @return the number of rows affected or 0 for ddl
   */
  public static int executeSql(final String sql) {
    return executeSql(sql, null);
  }

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
        
        //we dont close this connection or anything since could be pooled
        Connection connection = hibernateSession.getSession().connection();
        PreparedStatement preparedStatement = null;

        try {
          preparedStatement = connection.prepareStatement(sql);
  
          attachParams(preparedStatement, params);
          
          int result = preparedStatement.executeUpdate();
          
          hibernateSession.commit(GrouperCommitType.COMMIT_IF_NEW_TRANSACTION);
          return result;
        } catch (Exception e) {
          throw new  RuntimeException ("Problem with sql: " + sql, e);
        } finally {
          closeQuietly(preparedStatement);
        }
      }
      
    });
    return result;
     
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
    List<Object> paramList = listObject(params);
    List<Type> typeList = hibernateTypes(paramList);
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
  static void attachParams(PreparedStatement statement, Object params, Object types)
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
    
      List paramList = listObject(params);
      List typeList = listObject(types);
  
      //loop through, set the params
      Type currentType = null;
      for (int i = 0; i < paramLength; i++) {
        //not sure why the session implementer is null, if this ever fails for a type, 
        //might want to not use hibernate and brute force it
        currentType = (Type) typeList.get(i);
        currentType.nullSafeSet(statement, paramList.get(i), i + 1, null);
      }
  
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
   * Returns a Hibernate Type for the given java type. Handles both primitives and Objects.
   * Will throw an exception if the given object is null or if a type cannot be found for it.
   * @param o is the object to find the Type for.
   * @return the Type.
   */
  public static Type hibernateType(Object o) {
    if (o == null) {
      //its possible to bind null (e.g. for an update), so just use object to do this
      return Hibernate.OBJECT;
    }
    Class clazz = o.getClass();
  
    if (clazz == int.class || o instanceof Integer) {
      return Hibernate.INTEGER;
    } else if (clazz == double.class || clazz == Double.class) {
      return Hibernate.DOUBLE;
    } else if (clazz == long.class || clazz == Long.class) {
      return Hibernate.LONG;
    } else if (clazz == float.class || clazz == Float.class) {
      return Hibernate.FLOAT;
    } else if (clazz == byte.class || clazz == Byte.class) {
      return Hibernate.BYTE;
    } else if (clazz == boolean.class || clazz == Boolean.class) {
      return Hibernate.TRUE_FALSE;
    } else if (clazz == char.class || clazz == Character.class) {
      return Hibernate.CHARACTER;
    } else if (clazz == short.class || clazz == Short.class) {
      return Hibernate.SHORT;
    } else if (clazz == java.util.Date.class || clazz == java.sql.Date.class) {
      //return Hibernate.TIMESTAMP;
      return Hibernate.DATE;
    } else if (clazz == Timestamp.class) {
      return Hibernate.TIMESTAMP;
    } else if (clazz == String.class) {
      return Hibernate.STRING;
    }
    throw new RuntimeException(
        "Cannot find a hibernate type to associate with java type " + clazz);
  }
  
  /**
   * Returns a list of Hibernate types corresponding to the given params.
   * @param params are the objects to get the types for. Can be list, Object, or array.
   * @return the corresponding types.
   */
  public static List<Type> hibernateTypes(List<Object> params) {
  
    int length = GrouperUtil.length(params);
  
    //if null, make sure the same (or exception later)
    if (length == 0) {
      return null;
    }
  
    // Get the types.
    // Create a list of hibernate types.
    List<Type> types = new ArrayList<Type>();
  
    for (int i = 0; i < length; i++) {
      Object o = params.get(i);
      types.add(hibernateType(o));
    }
    return types;
  }

}

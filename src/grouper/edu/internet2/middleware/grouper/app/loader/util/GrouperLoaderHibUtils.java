/*
 * @author mchyzer
 * $Id: GrouperLoaderHibUtils.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperLoaderHibUtils {

  /**
   * store an object to hibernate
   * @param object
   */
  public static void store(Object object) {

    Session session = null;
    Transaction transaction = null;

    try {
      session = GrouperDdlUtils.session();
      transaction = session.beginTransaction();
      
      session.saveOrUpdate(object);
      
      transaction.commit();
      
    } catch (Exception e) {
      GrouperLoaderUtils.rollbackQuietly(transaction);
      throw new RuntimeException("Problem storing object of type: " + GrouperUtil.className(object), e);
    } finally {
      GrouperLoaderUtils.closeQuietly(session);
    }

  }

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public static int executeSql(String sql, List<Object> params) {

    Session session = null;
    Transaction transaction = null;
    PreparedStatement preparedStatement = null;
    try {
      session = GrouperDdlUtils.session();
      transaction = session.beginTransaction();
      
      //we dont close this connection or anything since could be pooled
      Connection connection = session.connection();
      preparedStatement = connection.prepareStatement(sql);

      attachParams(preparedStatement, params);
      
      int result = preparedStatement.executeUpdate();
     
      transaction.commit();
      
      return result;
      
    } catch (Exception e) {
      GrouperLoaderUtils.rollbackQuietly(transaction);
      throw new RuntimeException("Problem with query: " + sql, e);
    } finally {
      GrouperLoaderUtils.closeQuietly(preparedStatement);
      GrouperLoaderUtils.closeQuietly(session);
    }

  }

  /**
   * delete an object to hibernate
   * @param object
   */
  public static void delete(Object object) {

    Session session = null;
    Transaction transaction = null;
    
    try {
      session = GrouperDdlUtils.session();
      transaction = session.beginTransaction();
      
      session.delete(object);
      
      transaction.commit();
      
    } catch (Exception e) {
      GrouperLoaderUtils.rollbackQuietly(transaction);
      throw new RuntimeException("Problem deleting object of type: " + GrouperUtil.className(object), e);
    } finally {
      GrouperLoaderUtils.closeQuietly(session);
    }

  }


  /**
   * select by hql
   * @param theType 
   * @param hql 
   * @param <T> 
   * @return the object or null if none found
   */
  @SuppressWarnings("unchecked")
  public static <T> T select(Class<T> theType, String hql) {

    Session session = null;
    
    try {
      session = GrouperDdlUtils.session();
      Query query = session.createQuery(hql);
      List<T> theList = query.list();
      if (theList == null || theList.size() == 0) {
        return null;
      }
      if (theList.size() == 1) {
        return theList.get(0);
      }
      throw new RuntimeException("Expected 1 or 0 results with query but retrieved: " 
          + theList.size() + ", '" + hql + "'");
       
    } finally {
      GrouperLoaderUtils.closeQuietly(session);
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
    List<Object> paramList = GrouperLoaderUtils.listObject(params);
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


    List paramList = GrouperLoaderUtils.listObject(params);
    List typeList = GrouperLoaderUtils.listObject(types);


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


}

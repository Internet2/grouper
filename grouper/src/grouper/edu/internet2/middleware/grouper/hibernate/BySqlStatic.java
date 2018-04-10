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
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
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
  private static final Log LOG = GrouperUtil.getLog(BySqlStatic.class);

  
  /**
   * constructor
   *
   */
  BySqlStatic() {}

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @return the number of rows affected or 0 for ddl
   */
  public int executeSql(final String sql) {
    return executeSql(sql, null, null);
  }

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   * @deprecated doesnt work with postgres, need to pass in param types explicitly since cant determine them if null
   */
  @Deprecated
  public int executeSql(final String sql, final List<Object> params) {
    return executeSql(sql, params, convertParamsToTypes(params));
  }

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @param types is the types of the params
   * @return the number of rows affected or 0 for ddl
   */
  public int executeSql(final String sql, final List<Object> params, final List<Type> types) {
  
    int result = (Integer)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        //lets flush before the query
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        hibernateSession.misc().flush();
        
        PreparedStatement preparedStatement = null;
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params, types);
          
          GrouperContext.incrementQueryCount();
          int result = preparedStatement.executeUpdate();
          
          return result;

        } catch (Exception e) {
          throw new RuntimeException("Problem with query in bysqlstatic: " + sql, e);
        } finally {
          GrouperUtil.closeQuietly(preparedStatement);
        }
      }
      
    });
    return result;
  
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @return the number of rows affected or 0 for ddl
   */
  public <T> T select(final Class<T> returnClassType, final String sql) {
    return select(returnClassType, sql, null, null);
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   * @deprecated doesnt work with postgres, need to pass in param types explicitly since cant determine them if null
   */
  @Deprecated
  public <T> T select(final Class<T> returnClassType, final String sql, final List<Object> params) {
    return select(returnClassType, sql, params, convertParamsToTypes(params));
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @param types types of params
   * @return the number of rows affected or 0 for ddl
   */
  public <T> T select(final Class<T> returnClassType, final String sql, final List<Object> params, final List<Type> types) {
  
    //TODO incorporate this with the listSelect
    T theResult = (T)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params);
          
          GrouperContext.incrementQueryCount();
          resultSet = preparedStatement.executeQuery();
          
          boolean hasResults = resultSet.next();
          
          if (!hasResults) {
            throw new RuntimeException("Expected 1 row but received none");
          }
          
          T result = null;
          boolean isInt = int.class.equals(returnClassType);
          boolean isLong = long.class.equals(returnClassType);
          boolean isPrimitive = isInt || isLong;
          if (isInt || Integer.class.equals(returnClassType)) {
            BigDecimal bigDecimal = resultSet.getBigDecimal(1);
            if (bigDecimal != null) {
              result = (T)(Object)bigDecimal.intValue();
            }
          } else if (isLong || Long.class.equals(returnClassType)) {
            BigDecimal bigDecimal = resultSet.getBigDecimal(1);
            if (bigDecimal != null) {
              result = (T)(Object)bigDecimal.longValue();
            }
          } else if (String.class.equals(returnClassType)) {
            result = (T)resultSet.getString(1);
          } else {
            throw new RuntimeException("Unexpected type: " + returnClassType);
          }
          
          if (result == null && isPrimitive) {
            throw new NullPointerException("expecting primitive (" + returnClassType.getSimpleName() 
                + "), but received null");
          }
          
          if (resultSet.next()) {
            throw new RuntimeException("Expected 1 row but received multiple");
          }
          
          return result;
  
        } catch (Exception e) {
          throw new RuntimeException("Problem with query in select: " + sql, e);
        } finally {
          GrouperUtil.closeQuietly(preparedStatement);
        }
      }
    });
    return theResult;
  
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param query can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   * @deprecated doesnt work with postgres, need to pass in param types explicitly since cant determine them if null
   */
  @SuppressWarnings("deprecation")
  public <T> List<T> listSelectHiberateMapped(final Class<T> returnClassType, final String query, final List<Object> params) {
    return listSelectHiberateMapped(returnClassType, query, params, convertParamsToTypes(params));
  }
  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param query can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @param types types of params
   * @return the number of rows affected or 0 for ddl
   */
  public <T> List<T> listSelectHiberateMapped(final Class<T> returnClassType, final String query, final List<Object> params, final List<Type> types) {
    //Get the alias and the types.  First see if already there
    final String alias = HibUtils.parseAlias(query, false);
    
    //if (StringUtils.isEmpty(alias)) {
      //if not there, massage the query
    //  query = HibUtils.massageQuery(query);
      //might be standard alias, might be a custom one
    //  alias = HibUtils.parseAlias(query);
    //}
    
    
    List<T> theResult = (List<T>)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      /**
       * 
       * @see edu.internet2.middleware.grouper.hibernate.HibernateHandler#callback(edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean)
       */
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        return hibernateHandlerBean.getHibernateSession()
            .retrieveListBySql(query, alias, returnClassType, params, types);
        
      }
    });
    return theResult;

  }
  
  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   * @deprecated doesnt work with postgres, need to pass in param types explicitly since cant determine them if null
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public <T> List<T> listSelect(final Class<T> returnClassType, final String sql, final List<Object> params) {
    return listSelect(returnClassType, sql, params, convertParamsToTypes(params));
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @param types
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public <T> List<T> listSelect(final Class<T> returnClassType, final String sql, final List<Object> params, final List<Type> types) {

    boolean isPrimitiveClass = HibUtils.handleAsPrimitive(returnClassType);

    if (!isPrimitiveClass) {
      return listSelectHiberateMapped(returnClassType, sql, params, types);
    }

    List<T> theResult = (List<T>)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      /**
       * 
       * @see edu.internet2.middleware.grouper.hibernate.HibernateHandler#callback(edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean)
       */
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Object> resultList = new ArrayList<Object>();
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params, types);
          
          resultSet = preparedStatement.executeQuery();
          
          int columnCount = resultSet.getMetaData().getColumnCount();
          
          while (resultSet.next()) {
          
            T result = null;
            
            if (returnClassType.isArray()) {
              result = (T)Array.newInstance(String.class, columnCount);
              for (int i=0;i<columnCount;i++) {
                Array.set(result, i, resultSet.getString(1+i));
              }
            } else {
            
              boolean isInt = int.class.equals(returnClassType);
              boolean isPrimitive = isInt;
              if (isInt || Integer.class.equals(returnClassType)) {
                BigDecimal bigDecimal = resultSet.getBigDecimal(1);
                if (bigDecimal != null) {
                  result = (T)(Object)bigDecimal.intValue();
                }
              } else if (String.class.equals(returnClassType)) {
                result = (T)resultSet.getString(1);
              } else {
                throw new RuntimeException("Unexpected type: " + returnClassType);
              }
              if (result == null && isPrimitive) {
                throw new NullPointerException("expecting primitive (" + returnClassType.getSimpleName() 
                    + "), but received null");
              }
            }
            
            resultList.add(result);
          }          
          return resultList;

        } catch (Exception e) {
          throw new RuntimeException("Problem with query in listSelect: " + sql, e);
        } finally {
          GrouperUtil.closeQuietly(preparedStatement);
        }
      }
    });
    return theResult;
  
  }

  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   * @deprecated doesnt work with postgres, pass in types explicitly
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public static void attachParams(PreparedStatement statement, Object params)
      throws HibernateException, SQLException {
    attachParams(statement, params, convertParamsToTypes(params));
  }

  /**
   * convert params to types
   * @param params
   * @return the types
   * @deprecated doesnt work with postgres.  pass in types explicitly
   */
  @Deprecated
  public static List<Type> convertParamsToTypes(Object params) {
    List<Type> typeList = null;
    if (GrouperUtil.length(params) > 0) {
      List<Object> paramList = GrouperUtil.toList(params);
      typeList = HibUtils.hibernateTypes(paramList);
    }
    return typeList;
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
      currentType.nullSafeSet(statement, paramList.get(i), i + 1, (SessionImpl)HibernateSession._internal_hibernateSession().getSession());
    }
  
  }
  
  
  
}

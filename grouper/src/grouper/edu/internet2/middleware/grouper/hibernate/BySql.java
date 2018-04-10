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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.internal.SessionImpl;
import org.hibernate.type.Type;

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
public class BySql extends HibernateDelegate {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(BySql.class);

  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  

  /**
   * assign a different grouperTransactionType (e.g. for autonomous transactions)
   * @param theGrouperTransactionType
   * @return the same object for chaining
   * @TODO remove in future grouper version 2.4
   */
  public BySql setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    return this;
  }
  
  /**
   * string value for error handling
   * @return the string value
   * @TODO remove in future grouper release 2.4
   */
  @Override
  public String toString() {
    return super.toString();
  }

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   * @TODO remove in future grouper release 2.4
   */
  public BySql createQuery(String theHqlQuery) {
    return this;
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
  

    return executeSql(sql, params, BySqlStatic.convertParamsToTypes(params));
  }

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @param types
   * @return the number of rows affected or 0 for ddl
   */
  public int executeSql(final String sql, final List<Object> params, final List<Type> types) {
  
    HibernateSession hibernateSession = this.getHibernateSession();
    hibernateSession.misc().flush();
    
    PreparedStatement preparedStatement = null;
    try {
      
      //we dont close this connection or anything since could be pooled
      Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
      preparedStatement = connection.prepareStatement(sql);
  
      BySqlStatic.attachParams(preparedStatement, params, types);
      
      GrouperContext.incrementQueryCount();
      int result = preparedStatement.executeUpdate();
      
      return result;

    } catch (Exception e) {
      throw new RuntimeException("Problem with query in bysqlstatic: " + sql, e);
    } finally {
      GrouperUtil.closeQuietly(preparedStatement);
    }
  
  }


  /**
   * @param theHibernateSession
   */
  public BySql(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }
  
  
  
}

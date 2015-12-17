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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.internal.SessionImpl;

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

  /** assign a transaction type, default use the transaction modes
   * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
   * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private GrouperTransactionType grouperTransactionType = null;
  
  /**
   * assign a different grouperTransactionType (e.g. for autonomous transactions)
   * @param theGrouperTransactionType
   * @return the same object for chaining
   */
  public BySql setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    this.grouperTransactionType = theGrouperTransactionType;
    return this;
  }
  
  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("BySql, query: '");
    result.append(this.query);
    result.append(", tx type: ").append(this.grouperTransactionType);
    //dont use bindVars() method so it doesnt lazy load
    if (this.bindVars != null) {
      int index = 0;
      int size = this.bindVars().size();
      for (Object object : this.bindVars()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(GrouperUtil.toStringForLog(object, 50));
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<Object> bindVars = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public BySql createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<Object> bindVars() {
    if (this.bindVars == null) {
      this.bindVars = new ArrayList<Object>();
    }
    return this.bindVars;
  }
  
  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  public int executeSql(final String sql, final List<Object> params) {
  
    HibernateSession hibernateSession = this.getHibernateSession();
    hibernateSession.misc().flush();
    
    PreparedStatement preparedStatement = null;
    try {
      
      //we dont close this connection or anything since could be pooled
      Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
      preparedStatement = connection.prepareStatement(sql);
  
      BySqlStatic.attachParams(preparedStatement, params);
      
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


  
  /**
   * @param bindVars1 the bindVars to set
   */
  void setBindVars(List<Object> bindVars1) {
    this.bindVars = bindVars1;
  }


  
  /**
   * @param query1 the query to set
   */
  void setQuery(String query1) {
    this.query = query1;
  }
  
  
  
}

/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

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
public class ByHqlStatic {
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(ByHqlStatic.class);

  /** assign a transaction type, default use the transaction modes
   * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
   * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private GrouperTransactionType grouperTransactionType = null;
  
  /**
   * assign if this query is cacheable or not.
   */
  private Boolean cacheable = null;
  
  /**
   * assign a different grouperTransactionType (e.g. for autonomous transactions)
   * @param theGrouperTransactionType
   * @return the same object for chaining
   */
  public ByHqlStatic setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    this.grouperTransactionType = theGrouperTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable the cacheable to set
   * @return this object for chaining
   */
  public ByHqlStatic setCacheable(Boolean cacheable) {
    this.cacheable = cacheable;
    return this;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByHqlStatic, query: '");
    result.append(this.query).append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", tx type: ").append(this.grouperTransactionType);
    //dont use bindVarParams() method so it doesnt lazy load
    if (this.bindVarNameParams != null) {
      int index = 0;
      int size = this.bindVarNameParams().size();
      for (HibernateParam hibernateParam : this.bindVarNameParams()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(hibernateParam);
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<HibernateParam> bindVarNameParams = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public ByHqlStatic createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<HibernateParam> bindVarNameParams() {
    if (this.bindVarNameParams == null) {
      this.bindVarNameParams = new ArrayList<HibernateParam>();
    }
    return this.bindVarNameParams;
  }
  
  /**
   * cache region for cache
   * @param cacheRegion the cacheRegion to set
   * @return this object for chaining
   */
  public ByHqlStatic setCacheRegion(String cacheRegion) {
    this.cacheRegion = cacheRegion;
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value
   * @return this object for chaining
   */
  public ByHqlStatic setString(String bindVarName, String value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, String.class));
    return this;
  }
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHqlStatic setLong(String bindVarName, long value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Long.class));
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHqlStatic setInteger(String bindVarName, int value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Integer.class));
    return this;
  }

  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * Hib3GroupDAO hib3GroupDAO = HibernateSession.byHqlStatic()
   * .createQuery("from Hib3GroupDAO as g where g.uuid = :uuid")
   *  .setCacheable(false)
   *  .setCacheRegion(KLASS + ".Exists")
   *  .setString("uuid", uuid).uniqueResult(Hib3GroupDAO.class);
   * 
   * </pre>
   * @param returnType type of the result (in future can use this for typecasting)
   * @param <T> is the template
   * @return the object or null if none found
   * @throws GrouperDAOException
   */
  public <T> T uniqueResult(Class<T> returnType) throws GrouperDAOException {
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READONLY_OR_USE_EXISTING);
      
      T result = (T)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              Session session  = hibernateSession.getSession();
              Query query = ByHqlStatic.this.attachQueryInfo(session);
              return query.uniqueResult();
            }
        
      });
      
      return result;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in uniqueResult: (" + returnType + "), " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in uniqueResult: " + this, e);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param <T> is the template
   * @return the list or the empty list if not found (never null)
   * @throws GrouperDAOException
   */
  public <T> List<T> list(Class<T> returnType) throws GrouperDAOException {
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READONLY_OR_USE_EXISTING);
      
      List<T> result = (List<T>)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              Session session  = hibernateSession.getSession();
              Query query = ByHqlStatic.this.attachQueryInfo(session);
              //not sure this can ever be null, but make sure not to make iterating results easier
              return GrouperUtil.nonNull(query.list());
            }
        
      });
      
      return result;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in list: (" + returnType + "), " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in list: (" + returnType + "), " + this, e);
      throw e;
    }
    
  }

  /**
   * prepare query nased on fields
   * @param session hib session
   * @return the query
   */
  private Query attachQueryInfo(Session session) {
    Query   query = session.createQuery(ByHqlStatic.this.query);
    if (ByHqlStatic.this.cacheable != null) {
      query.setCacheable(false);
    }
    if (ByHqlStatic.this.cacheRegion != null) {
      query.setCacheRegion(ByHqlStatic.this.cacheRegion);
    }
    //note, dont call the method bindVarNameParams() so it doesnt lazyload...
    if (ByHqlStatic.this.bindVarNameParams != null) {
      for (HibernateParam hibernateParam : ByHqlStatic.this.bindVarNameParams()) {
        
        if (String.class.equals(hibernateParam.getType())) {
          query.setString(hibernateParam.getName(), (String)hibernateParam.getValue());
        } else if (Long.class.equals(hibernateParam.getType())) {
          query.setLong(hibernateParam.getName(), (Long)hibernateParam.getValue());
        } else if (Integer.class.equals(hibernateParam.getType())) {
          query.setInteger(hibernateParam.getName(), (Integer)hibernateParam.getValue());
        } else {
          throw new RuntimeException("Invalid bind var type: " 
              + hibernateParam );
        }
      }
    }
    return query;
    
  }
  
  /**
   * constructor
   *
   */
  ByHqlStatic() {}
  
  
  
}

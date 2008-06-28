/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * for simple object queries, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
 * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByObjectStatic {
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(ByObjectStatic.class);

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
  public ByObjectStatic setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    this.grouperTransactionType = theGrouperTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable the cacheable to set
   * @return this object for chaining
   */
  public ByObjectStatic setCacheable(Boolean cacheable) {
    this.cacheable = cacheable;
    return this;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByObjectStatic, query: ', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", tx type: ").append(this.grouperTransactionType);
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * cache region for cache
   * @param cacheRegion the cacheRegion to set
   * @return this object for chaining
   */
  public ByObjectStatic setCacheRegion(String cacheRegion) {
    this.cacheRegion = cacheRegion;
    return this;
  }

  /**
   * <pre>
   * call hibernate method "update" on a list of objects
   * 
   * HibernateSession.byObjectStatic().update(collection);
   * 
   * </pre>
   * @param collection is collection of objects to update in one transaction.  If null or empty just ignore
   * @throws GrouperDAOException
   */
  public void update(final Collection<?> collection) throws GrouperDAOException {
    if (collection == null) {
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().update(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in update: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in update: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    }
    
  }

  /**
   * call hibernate "update" method on an object
   * @param object to update
   * @throws GrouperDAOException
   */
  public void update(final Object object) throws GrouperDAOException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      update((Collection)object);
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().update(object);
              
              return null;
            }
        
      });
      
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in update: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in update: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    }
    
  }
  
  /**
   * call hibernate "load" method on an object
   * @param <T> 
   * @param theClass to load
   * @param id 
   * @return the object
   * @throws GrouperDAOException
   */
  public <T> T load(final Class<T> theClass, final Serializable id) throws GrouperDAOException {
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      T result = (T)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              T theResult = hibernateSession.byObject().load(theClass, id);
              
              return theResult;
            }
        
      });
      return result;
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in update: " + theClass + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in update: " + theClass + ", " + this, e);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "saveOrUpdate" on a list of objects
   * 
   * HibernateSession.byObjectStatic().saveOrUpdate(collection);
   * 
   * </pre>
   * @param collection is collection of objects to saveOrUpdate in one transaction.  If null or empty just ignore
   * @throws GrouperDAOException
   */
  public void saveOrUpdate(final Collection<?> collection) throws GrouperDAOException {
    if (collection == null) {
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().saveOrUpdate(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    }
    
  }

  /**
   * call hibernate "saveOrUpdate" method on an object
   * @param object to update
   * @throws GrouperDAOException
   */
  public void saveOrUpdate(final Object object) throws GrouperDAOException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      saveOrUpdate((Collection)object);
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().saveOrUpdate(object);
              
              return null;
            }
        
      });
      
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "save" on a list of objects
   * 
   * HibernateSession.byObjectStatic().save(collection);
   * 
   * </pre>
   * @param collection is collection of objects to save in one transaction.  If null or empty just ignore
   * @throws GrouperDAOException
   */
  public void save(final Collection<?> collection) throws GrouperDAOException {
    if (collection == null) {
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().save(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in save: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in save: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    }
    
  }
  

  /**
   * <pre>
   * call hibernate method "save" on an object
   * 
   * HibernateSession.byObjectStatic().save(dao);
   * 
   * </pre>
   * @param object to save
   * @return the id
   * @throws GrouperDAOException
   */
  public Serializable save(final Object object) throws GrouperDAOException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      save((Collection)object);
      return null;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      Serializable result = (Serializable)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              return hibernateSession.byObject().save(object);
              
            }
        
      });
      return result;
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in save: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in save: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(collection);
   * 
   * </pre>
   * @param collection is collection of objects to delete in one transaction.  If null or empty just ignore
   * @throws GrouperDAOException
   */
  public void delete(final Collection<?> collection) throws GrouperDAOException {
    if (collection == null) {
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().delete(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(Rosetta.getDAO(_f));
   * 
   * </pre>
   * @param object is an object (if collection will still work), if null, will probably throw exception
   * @throws GrouperDAOException
   */
  public void delete(final Object object) throws GrouperDAOException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      delete((Collection)object);
      return;
    }
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {

              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              hibernateSession.byObject().delete(object);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(object) + ", " + this, e);
      throw e;
    }
    
  }
  
  
  /**
   * constructor
   *
   */
  ByObjectStatic() {}
  
  
  
}

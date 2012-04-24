/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
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
public class ByObjectStatic extends ByQueryBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ByObjectStatic.class);

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
   * assign if this query is cacheable or not.
   */
  private String entityName = null;
  
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
    result.append(", entityName: ").append(this.entityName);
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
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByObjectStatic setEntityName(String theEntityName) {
    this.entityName = theEntityName;
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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.update(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + GrouperUtil.classNameCollection(collection) + ", " + this;

      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, 
                  "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, 
                  "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.update(object);
              
              return null;
            }
        
      });
      
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + GrouperUtil.className(object) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      T result = (T)HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              T theResult = byObject.load(theClass, id);
              
              return theResult;
            }
        
      });
      return result;
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + theClass + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveOrUpdate(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in saveOrUpdate: " + GrouperUtil.classNameCollection(collection) + ", " + this;

      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      
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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveOrUpdate(object);
              
              return null;
            }
        
      });
      
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in saveOrUpdate: " + GrouperUtil.className(object) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.save(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + GrouperUtil.classNameCollection(collection) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      Serializable result = (Serializable)HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              return byObject.save(object);
              
            }
        
      });
      return result;
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (MembershipAlreadyExistsException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + GrouperUtil.className(object) + ", " + this;

      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "save" on a collection of objects in batch
   * 
   * </pre>
   * @param collection of objects
   * @throws GrouperDAOException
   */
  public void saveBatch(final Collection<?> collection) throws GrouperDAOException {
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveBatch(collection);
              return null;
            }
        
      });

    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (MembershipAlreadyExistsException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + GrouperUtil.classNameCollection(collection) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.delete(collection);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in delete: " + GrouperUtil.classNameCollection(collection) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * copy field to, better by a ByObject...
   */
  @Override
  protected void copyFieldsTo(ByQueryBase byQueryBase) {
    super.copyFieldsTo(byQueryBase);
    ((ByObject)byQueryBase).setEntityName(this.entityName);
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
      
      HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              GrouperUtil.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              GrouperUtil.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.delete(object);
              return null;
            }
        
      });
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in delete: " + GrouperUtil.classNameCollection(object) + ", " + this;
      
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  
  /**
   * constructor
   *
   */
  ByObjectStatic() {}
  
  /**
   * @see edu.internet2.middleware.grouper.hibernate.ByQueryBase#setIgnoreHooks(boolean)
   */
  @Override
  public ByObjectStatic setIgnoreHooks(boolean theIgnoreHooks) {
    return (ByObjectStatic)super.setIgnoreHooks(theIgnoreHooks);
  }

  
}

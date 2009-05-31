package edu.internet2.middleware.grouper.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * @version $Id: ByObject.java,v 1.11 2009-05-31 02:27:31 mchyzer Exp $
 * @author harveycg
 */
public class ByObject extends HibernateDelegate {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ByObject.class);
  /**
   * assign if this query is cacheable or not.
   */
  private String entityName = null;

  /**
   * @param theHibernateSession
   */
  ByObject(HibernateSession theHibernateSession) {
    super(theHibernateSession);
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
      for (Object object : collection) {
        delete(object);
      }
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
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
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPreDelete(hibernateSession);
      }
      GrouperContext.incrementQueryCount();

      if (StringUtils.isBlank(this.entityName)) {
        session.delete(object);
      } else {
        session.delete(this.entityName, object);
      }
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostDelete(hibernateSession);
      }

    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in delete: " + GrouperUtil.classNameCollection(object) + ", " + this, e);
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
      for (Object object : collection) {
        save(object);
      }
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
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
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();

      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPreSave(hibernateSession);
      }

      GrouperContext.incrementQueryCount();
      
      Serializable id = null;
      if (StringUtils.isBlank(this.entityName)) {

        id = session.save(object);
      } else {
        id = session.save(this.entityName, object);
      }

      session.flush();
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostSave(hibernateSession);
      }
      return id;
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
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
      for (Object object : collection) {
        saveOrUpdate(object);
      }
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in saveOrUpdate: " + GrouperUtil.classNameCollection(collection) + ", " + this, e);
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
   * @throws GrouperDAOException
   */
  public void saveOrUpdate(final Object object) throws GrouperDAOException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      saveOrUpdate((Collection)object);
      return;
    }
    try {
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();

      Boolean isInsert = null;
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        isInsert = HibUtilsMapping.isInsert(object);
        if (isInsert) {
          ((HibGrouperLifecycle)object).onPreSave(hibernateSession);
        } else {
          ((HibGrouperLifecycle)object).onPreUpdate(hibernateSession);
        }
      }

      GrouperContext.incrementQueryCount();

      if (StringUtils.isBlank(this.entityName)) {
        session.saveOrUpdate(object);
      } else {
        session.saveOrUpdate(this.entityName, object);
      }

      try {
        session.flush(); //TODO remove
      } catch (RuntimeException re) {
        throw re;
      }
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        if (isInsert) {
          ((HibGrouperLifecycle)object).onPostSave(hibernateSession);
        } else {
          ((HibGrouperLifecycle)object).onPostUpdate(hibernateSession);
        }
      }
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
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
   * call hibernate method "load" on an object
   * 
   * </pre>
   * @param <T> 
   * @param theClass to load
   * @param id to find in db
   * @return the result
   * @throws GrouperDAOException
   */
  public <T> T load(final Class<T> theClass, Serializable id) throws GrouperDAOException {
    try {
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      GrouperContext.incrementQueryCount();
      T result = null;
      if (StringUtils.isBlank(this.entityName)) {
        result = (T)session.load(theClass, id);
      } else {
        result = (T)session.load(this.entityName, id);
      }
      
      return result;

    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in load: " + theClass + ", " 
          + id + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in load: " + theClass + ", " 
          + id + ", " + this, e);
      throw e;
    }
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
      for (Object object : collection) {
        update(object);
      }
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
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
    try {

      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPreUpdate(hibernateSession);
      }

      GrouperContext.incrementQueryCount();

      if (StringUtils.isBlank(this.entityName)) {

        session.update(object);
      } else {
        session.update(this.entityName, object);
      }
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostUpdate(hibernateSession);
      }
      
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in update: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in update: " + GrouperUtil.className(object) + ", " + this, e);
      throw e;
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.ByQueryBase#setIgnoreHooks(boolean)
   */
  @Override
  public ByObject setIgnoreHooks(boolean theIgnoreHooks) {
    return (ByObject)super.setIgnoreHooks(theIgnoreHooks);
  }

  /**
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByObject setEntityName(String theEntityName) {
    this.entityName = theEntityName;
    return this;
  }
}

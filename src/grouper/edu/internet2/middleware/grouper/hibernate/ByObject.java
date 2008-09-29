package edu.internet2.middleware.grouper.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * @version $Id: ByObject.java,v 1.7 2008-09-29 03:38:30 mchyzer Exp $
 * @author harveycg
 */
public class ByObject extends HibernateDelegate {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ByObject.class);

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

      session.delete(object);
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostDelete(hibernateSession);
      }

    } catch (HookVeto hookVeto) {
      //just throw, this is ok
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

      Serializable id = session.save(object);
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostSave(hibernateSession);
      }
      return id;
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
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
        isInsert = HibUtilsMapping.isInsert(hibernateSession, object);
        if (isInsert) {
          ((HibGrouperLifecycle)object).onPreSave(hibernateSession);
        } else {
          ((HibGrouperLifecycle)object).onPreUpdate(hibernateSession);
        }
      }
      
      session.saveOrUpdate(object);

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

      T result = (T)session.load(theClass, id);
      
      return result;

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

      session.update(object);
      
      if (!this.isIgnoreHooks() && object instanceof HibGrouperLifecycle) {
        ((HibGrouperLifecycle)object).onPostUpdate(hibernateSession);
      }
      
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
   * @see edu.internet2.middleware.grouper.hibernate.ByQueryBase#setIgnoreHooks(boolean)
   */
  @Override
  public ByObject setIgnoreHooks(boolean theIgnoreHooks) {
    return (ByObject)super.setIgnoreHooks(theIgnoreHooks);
  }
}

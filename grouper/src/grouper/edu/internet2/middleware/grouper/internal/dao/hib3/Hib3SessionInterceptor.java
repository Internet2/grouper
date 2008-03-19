/*
 * @author mchyzer
 * $Id: Hib3SessionInterceptor.java,v 1.1.2.1 2008-03-19 18:46:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * hibernate should be able to tell if an assigned key and version
 * are insert or update, but it cant, so tell it
 */
public class Hib3SessionInterceptor extends EmptyInterceptor implements Serializable {

  /**
   * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    if (entity instanceof Hib3HibernateVersioned) {
      //find the current version
      long newVersion = (Long)HibUtils.propertyValue(currentState, propertyNames, 
          Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION) + 1;
      //increment by one, granted this should always by 0
      HibUtils.assignProperty(currentState, propertyNames, 
          Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION, newVersion);
      //assign back to object since might not flush in time
      GrouperUtil.assignField(entity, Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state,
      String[] propertyNames, Type[] types) {
    if (entity instanceof Hib3HibernateVersioned) {
      //find the current version
      long newVersion = (Long)HibUtils.propertyValue(state, propertyNames, 
          Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION) + 1;
      //increment by one, granted this should always by 0
      HibUtils.assignProperty(state, propertyNames, 
          Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION, newVersion);
      //assign back to object since might not flush in time
      GrouperUtil.assignField(entity, Hib3HibernateVersioned.FIELD_HIBERNATE_VERSION, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see org.hibernate.EmptyInterceptor#isTransient(java.lang.Object)
   */
  @Override
  public Boolean isTransient(Object entity) {
    if (entity instanceof Hib3HibernateVersioned) {
      return ((Hib3HibernateVersioned)entity).getHibernateVersion() < 0;
    }
    return super.isTransient(entity);
  }

}

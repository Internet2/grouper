/*--
  $Id: HousekeepingInterceptor.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.type.Type;

class HousekeepingInterceptor implements Interceptor, Serializable
{
  private String dbAccount;
  
  public HousekeepingInterceptor
  	(String dbAccount)
  {
    this.dbAccount = dbAccount;
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
   */
  public boolean onLoad
  	(Object 			entity,
  	 Serializable id,
  	 Object[] 		state,
  	 String[] 		propertyNames,
  	 Type[] 			types)
  throws CallbackException
  {
    // Do nothing, let the operation proceed.
    return false;
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
   */
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException
  {
    // Do nothing, let the operation proceed.
    return false;
  }

  /**
   * @return true if any property of the to-be-saved entity was changed,
   * 		false otherwise.
   */
  public boolean onSave
  	(Object 			entity,
  	 Serializable id, 
  	 Object[] 		state, 
  	 String[] 		propertyNames, 
  	 Type[] 			types)
  throws CallbackException
  {
    Date createDateTime;
    
    if (entity instanceof EntityImpl)
    {
      createDateTime = ((EntityImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof PermissionImpl)
    {
      createDateTime = ((PermissionImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof TreeImpl)
    {
      createDateTime = ((TreeImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof PrivilegedSubjectImpl)
    {
      createDateTime = ((PrivilegedSubjectImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof AssignmentImpl)
    {
      createDateTime = ((AssignmentImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof SubjectTypeImpl)
    {
      createDateTime = ((SubjectTypeImpl)entity).getCreateDateTime();
    }
    else if (entity instanceof TreeTypeImpl)
    {
      createDateTime = ((TreeTypeImpl)entity).getCreateDateTime();
    }
    else
    {
      return false;
    }
    
    if (createDateTime == null)
    {
      setPropertyState
      	(state, propertyNames, "createDateTime", new Date());
      setPropertyState
    		(state, propertyNames, "createDbAccount", this.dbAccount);
    }
      
    setPropertyState
			(state, propertyNames, "modifyDbAccount", this.dbAccount);
      
    return true;
  }
  
  private void setPropertyState
  	(Object[] state, 
  	 String[] propertyNames,
  	 String		propertyName,
  	 Object		newState)
  {
    boolean propertyFound = false;
    
    for (int i = 0; i < propertyNames.length; i++)
    {
      if (propertyName.equals(propertyNames[i]))
      {
        state[i] = newState;
        propertyFound = true;
        break;
      }
    }
    
    if (propertyFound == false)
    {
      throw new SignetRuntimeException
      	("At database-persistence time, the Signet interceptor attempted to"
      	 + " set the '"
      	 + propertyName
      	 + "' property to the value '"
      	 + newState
      	 + "'. Signet was unable to find that property in the supplied"
      	 + " object.");
    }
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
   */
  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#preFlush(java.util.Iterator)
   */
  public void preFlush(Iterator entities) throws CallbackException
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#postFlush(java.util.Iterator)
   */
  public void postFlush(Iterator entities) throws CallbackException
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#isUnsaved(java.lang.Object)
   */
  public Boolean isUnsaved(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#findDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
   */
  public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, java.io.Serializable)
   */
  public Object instantiate(Class clazz, Serializable id) throws CallbackException
  {
    // TODO Auto-generated method stub
    return null;
  }

}

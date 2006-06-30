/*--
$Id: HousekeepingInterceptor.java,v 1.1 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $
 
Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.dbpersist;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import edu.internet2.middleware.signet.SignetRuntimeException;


import net.sf.hibernate.CallbackException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

class HousekeepingInterceptor implements Interceptor, Serializable
{
  private String					dbAccount;
  private SessionFactory 	sessionFactory;
  private Connection			connection;
  
  public HousekeepingInterceptor
  	(String dbAccount)
  {
    this.dbAccount = dbAccount;
  }
  
  void setSessionFactory(SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }
  
  void setConnection(Connection connection)
  {
    this.connection = connection;
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
  public boolean onFlushDirty
  	(Object 			entity,
  	 Serializable id,
  	 Object[] 		currentState,
  	 Object[] 		previousState,
  	 String[] 		propertyNames,
  	 Type[] 			types)
  throws CallbackException
  {
    // Do nothing, let the operation proceed.
    return false;
  }
  
  /**
   * @return true if any property of the to-be-saved entity was changed,
   * 		false otherwise.
   */
  public boolean onSave
   (Object				entity,
    Serializable 	id, 
    Object[] 			state, 
    String[] 			propertyNames, 
    Type[] 				types)
  throws CallbackException
  {
    return false;
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
    // Do nothing, let the operation proceed.
  }
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#preFlush(java.util.Iterator)
   */
  public void preFlush(Iterator entities) throws CallbackException
  {
    // Do nothing, let the operation proceed.
  }
  
  private Transaction startXact(Session session)
  throws CallbackException
  {
    Transaction tx;
    
    try
    {
      tx = session.beginTransaction();
    }
    catch (HibernateException he)
    {
      throw new CallbackException(he);
    }
    
    return tx;
  }
  
//  private void saveInitialHistoryRecord
//    (Session       session,
//     GrantableImpl grantableInstance)
//  throws CallbackException
//  {
//    History historyRecord;
//    
//    if (grantableInstance instanceof Assignment)
//    {
//      historyRecord
//        = new AssignmentHistoryImpl((AssignmentImpl)grantableInstance);
//    }
//    else if (grantableInstance instanceof Proxy)
//    {
//      historyRecord = new ProxyHistoryImpl((ProxyImpl)grantableInstance);
//    }
//    else
//    {
//      throw new CallbackException
//        ("HousekeepingInterceptor.saveInitialHistoryRecord() received"
//         + " a Grantable instance which was neither an Assignment nor a"
//         + " Proxy.");
//    }
//    
//    Set historySet = new HashSet(1);
//    historySet.add(historyRecord);
//    grantableInstance.setHistory(historySet);
//    
//    try
//    {
//      session.save(historyRecord);
//    }
//    catch (HibernateException e)
//    {
//      throw new CallbackException(e);
//    }
//  }
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#postFlush(java.util.Iterator)
   */
  public void postFlush(Iterator entities)
  throws CallbackException
  {
//    Session     tempSession;
//    Transaction tx;
//    
//    while (entities.hasNext())
//    {
//      Object entity = entities.next();
//      
//      if (entity instanceof Grantable)
//      {
//        GrantableImpl grantableInstance = (GrantableImpl)entity;
//
//        tempSession = this.sessionFactory.openSession(this.connection);
//        tx = startXact(tempSession);
//
//        if (grantableInstance.getHistory() == null)
//        {
//          saveInitialHistoryRecord(tempSession, grantableInstance);
//        }
//        
//        try
//        {
//          tx.commit();
//          tempSession.flush();
//          tempSession.close();
//        }
//        catch (HibernateException he)
//        {
//          throw new CallbackException(he);
//        }
//      }
//    }
  }
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#isUnsaved(java.lang.Object)
   */
  public Boolean isUnsaved(Object entity)
  {
    // Do nothing, let the operation proceed.
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#findDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
   */
  public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
  {
    // Do nothing, let the operation proceed.
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, java.io.Serializable)
   */
  public Object instantiate(Class clazz, Serializable id) throws CallbackException
  {
    // Do nothing, let the operation proceed.
    return null;
  }
  
}

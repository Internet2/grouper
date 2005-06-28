/*--
 $Id: HousekeepingInterceptor.java,v 1.11 2005-06-28 19:41:57 acohen Exp $
 $Date: 2005-06-28 19:41:57 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


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
  
  /* (non-Javadoc)
   * @see net.sf.hibernate.Interceptor#postFlush(java.util.Iterator)
   */
  public void postFlush(Iterator entities) throws CallbackException
  {
    Transaction tx;
    Session     tempSession;
    
    while (entities.hasNext())
    {
      Object entity = entities.next();
      
      if (entity instanceof AssignmentImpl)
      {
        AssignmentImpl assignment = (AssignmentImpl)entity;
        
        if (assignment.hasUnsavedLimitValues
            || assignment.needsInitialHistoryRecord
            || assignment.historyRecords.size() != 0)
        {
          tempSession = this.sessionFactory.openSession(this.connection);
          
          try
          {
            tx = tempSession.beginTransaction();
          }
          catch (HibernateException he)
          {
            throw new CallbackException(he);
          }
        
          if (assignment.hasUnsavedLimitValues)
          {
            Set limitValues = assignment.getLimitValues();
            Iterator limitValuesIterator = limitValues.iterator();
        
            while (limitValuesIterator.hasNext())
            {
              LimitValue limitValue = (LimitValue)(limitValuesIterator.next());

              try
              {
                /**
                 * Note that the AssignmentLimitValue constructor takes a
                 * Subsystem ID. That Subsystem ID is actually the ID of the
                 * Subsystem that's associated with this particlar Limit in case
                 * (someday in the future) that Limit comes from another
                 * Subsystem, or the shared "signet" subsystem.
                 * 
                 * For now, the subsystemID of the assignment can be safely
                 * placed there.
                 */
                AssignmentLimitValue alv
              	  = new AssignmentLimitValue
           					  (assignment.getId().intValue(),
           					   assignment.getFunction().getSubsystem().getId(),
           					   limitValue.getLimit().getId(),
           					   limitValue.getValue());

                tempSession.save(alv);
              }
              catch (Exception e)
              {
                throw new CallbackException(e);
              }
              
              assignment.hasUnsavedLimitValues = false;
            }
          }
          
          if (assignment.needsInitialHistoryRecord)
          {
            AssignmentHistory initialHistoryRecord
              = new AssignmentHistory(assignment);
            
            try
            {
              tempSession.save(initialHistoryRecord);
              recordLimitValuesHistory(tempSession, initialHistoryRecord);
            }
            catch (Exception e)
            {
              throw new CallbackException(e);
            }
            
            assignment.needsInitialHistoryRecord = false;
          }
          
          if (assignment.historyRecords.size() != 0)
          {
            Iterator historyRecordsIterator
              = assignment.historyRecords.iterator();
            while (historyRecordsIterator.hasNext())
            {
              AssignmentHistory assignmentHistory
                = (AssignmentHistory)(historyRecordsIterator.next());
              
              try
              {
                tempSession.save(assignmentHistory);
                recordLimitValuesHistory(tempSession, assignmentHistory);
              }
              catch (Exception e)
              {
                throw new CallbackException(e);
              }
            }
            
            assignment.historyRecords = new HashSet();
          }
        
          try
          {
            tx.commit();
            tempSession.flush();
            tempSession.close();
          }
          catch (HibernateException he)
          {
            throw new CallbackException(he);
          }
        }
      }
    }
  }
  
  private void recordLimitValuesHistory
    (Session            session,
     AssignmentHistory  assignmentHistory)
  throws HibernateException
  {
    Iterator limitValuesIterator
      = assignmentHistory.getLimitValues().iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      LimitValueHistory limitValueHistory
        = new LimitValueHistory(assignmentHistory, limitValue);
      session.save(limitValueHistory);
    }
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

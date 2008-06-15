/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;

import  java.io.InputStream;
import java.io.Serializable;
import  java.util.Properties;
import  org.hibernate.*;
import  org.hibernate.cfg.*;
import org.hibernate.classic.Lifecycle;

/**
 * Base Hibernate DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.4.2.3 2008-06-15 04:29:56 mchyzer Exp $
 * @since   @HEAD@
 */
abstract class Hib3DAO implements HibGrouperLifecycle, Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final Configuration  CFG;
  /**
   * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
   */
  public boolean onDelete(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onLoad(org.hibernate.Session, java.io.Serializable)
   */
  public void onLoad(Session s, Serializable id) {
    this.dbVersionReset();
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onUpdate(org.hibernate.Session)
   */
  public boolean onUpdate(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }

  private static final SessionFactory FACTORY;

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostSave(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreSave(HibernateSession hibernateSession) {
    
  }
  
  /**
   * get the type of the hooks (which tells all methods and stuff)
   * @return the type of the hooks
   */
  GrouperHookType grouperHooksType() {
    return null;
  }
  

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreUpdate(HibernateSession hibernateSession) {
  }


  // STATIC //
  static {
    try {
      // Find the custom configuration file
      if (Hib3DAO.class.getResource(GrouperConfig.HIBERNATE_CF) == null) {
        throw new RuntimeException("Cant find resource " + GrouperConfig.HIBERNATE_CF + ", make sure it is on the classpath.");
      }
      InputStream in  = Hib3DAO.class.getResourceAsStream(GrouperConfig.HIBERNATE_CF);  
      Properties  p   = new Properties();
      p.load(in);
      // And now load all configuration information
      CFG = new Configuration()
        .addProperties(p)
        .addClass(Hib3AttributeDAO.class)
        .addClass(Hib3CompositeDAO.class)
        .addClass(Hib3FieldDAO.class)
        .addClass(Hib3GroupDAO.class)
        .addClass(Hib3GroupTypeDAO.class)
        .addClass(Hib3GroupTypeTupleDAO.class)
        .addClass(Hib3GrouperSessionDAO.class)
        .addClass(Hib3MemberDAO.class)
        .addClass(Hib3MembershipDAO.class)
        .addClass(Hib3RegistrySubjectDAO.class)
        .addClass(Hib3RegistrySubjectAttributeDAO.class)
        .addClass(Hib3StemDAO.class)
        ;
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      ErrorLog.fatal(Hib3DAO.class, msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  public static Configuration getConfiguration()
    throws  HibernateException
  {
    return CFG;
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
	public static Session session()
    throws  HibernateException
  {
		return FACTORY.openSession();
	} 


  // PROTECTED ABSTRACT METHODS //

  // @since   @HEAD@
  protected abstract String getId();

  /**
   * take a snapshot of the data since this is what is in the db
   */
  void dbVersionReset() {
  }
  
  /**
   * see if the state of this object has changed compared to the DB state (last known)
   * @return true if changed, false if not
   */
  boolean stateDifferentThanDb() {
    throw new RuntimeException("Not implemented");
  }
} 


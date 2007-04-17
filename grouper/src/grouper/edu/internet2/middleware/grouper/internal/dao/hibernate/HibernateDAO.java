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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.GrouperConfig;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.io.InputStream;
import  java.util.ArrayList;
import  java.util.Collection;
import  java.util.Iterator;
import  java.util.Properties;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;

/**
 * Stub Hibernate DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateDAO.java,v 1.2 2007-04-17 17:13:26 blair Exp $
 * @since   1.2.0
 */
public abstract class HibernateDAO {
  // FIXME 20070416 revisit visibility - including methods

  // PRIVATE CLASS CONSTANTS //
  private static final Configuration  CFG;
  private static final SessionFactory FACTORY;


  // STATIC //
  static {
    try {
      // Find the custom configuration file
      InputStream in  = HibernateDAO.class.getResourceAsStream(GrouperConfig.HIBERNATE_CF);  
      Properties  p   = new Properties();
      p.load(in);
      // And now load all configuration information
      CFG = new Configuration()
        .addProperties(p)
        .addClass(HibernateAttributeDAO.class)
        .addClass(HibernateCompositeDAO.class)
        .addClass(HibernateFieldDAO.class)
        .addClass(HibernateGroupDAO.class)
        .addClass(HibernateGroupTypeDAO.class)
        .addClass(HibernateGroupTypeTupleDAO.class)
        .addClass(HibernateGrouperSessionDAO.class)
        .addClass(HibernateMemberDAO.class)
        .addClass(HibernateMembershipDAO.class)
        .addClass(HibernateRegistrySubjectDAO.class)
        .addClass(HibernateRegistrySubjectAttributeDAO.class)
        .addClass(HibernateStemDAO.class)
        ;
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      ErrorLog.fatal(HibernateDAO.class, msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // FIXME 20070416 revisit + visiblity
  public static String create(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(obj);
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  // @since   1.2.0
  // FIXME 20070416 revisit + visiblity
  public static void delete(Collection c) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = c.iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  // @since   1.2.0
  // FIXME 20070416 revisit + visiblity
  public static void delete(Object obj) 
    throws  GrouperDAOException 
  {
    Collection c = new ArrayList();
    c.add(obj);
    delete(c);
  } 

  // @since   1.2.0
  // FIXME 20070416 visibility
	public static Session getSession()
    throws HibernateException
  {
		return FACTORY.openSession();
	} 

  // @since   1.2.0
  // FIXME 20070416 revisit + visiblity
  public static void update(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update( Rosetta.getDAO(obj) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 


  // PROTECTED ABSTRACT METHODS //

  // @since   1.2.0
  // FIXME 20070416 visibility
  public abstract String getId();

} 


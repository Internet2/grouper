/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;


import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;
import  org.apache.commons.logging.*;


/**
 * Hibernate utility helper class.
 * <p/>
 * This code was initially derived from code in the book <i>Hibernate In
 * Action</i>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateHelper.java,v 1.10.2.3 2006-04-13 00:31:02 blair Exp $
 */
class HibernateHelper {

  // Private Class Constants
  private static final String         ERR_GP    = "attempt to delete transient object ";
  private static final String         ERR_INIT  = "unable to initialize hibernate: ";
  private static final SessionFactory FACTORY;
  private static final Log            LOG       = LogFactory.getLog(HibernateHelper.class);


  // Create the static session FACTORY 
  static {
    try {
      FACTORY = new Configuration()
        .addClass(Attribute.class)
        .addClass(Field.class)
        .addClass(GrouperSession.class)
        .addClass(GroupType.class)
        .addClass(HibernateSubject.class)
        .addClass(HibernateSubjectAttribute.class)
        .addClass(Member.class)
        .addClass(Membership.class)
        .addClass(Owner.class)
        .buildSessionFactory()
        ;
    } 
    catch (Throwable e) {
      // Catch *all* the errors
      String err = ERR_INIT + e.getMessage();
      LOG.fatal(err);
      throw new ExceptionInInitializerError(err);
    }
  } // static


  // Protected Class Methods

  protected static void delete(Object o) 
    throws HibernateException
  {
    Set objects = new LinkedHashSet();
    objects.add(o);
    HibernateHelper.delete(objects);
  } // protected static void delete(o)

  // Delete multiple objects in one transaction
  // @throws  HibernateException
  protected static void delete(Set objects)
    throws HibernateException
  { 
    Object  err = null;
    String  msg = "delete";
    LOG.debug(msg + ": will delete " + objects.size());
    try {
      Session     hs = HibernateHelper.getSession();
      Transaction tx = hs.beginTransaction();
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          Object o = iter.next();
          err = o;
          LOG.debug(msg + ": deleting " + o);
          hs.delete( _getPersistent(hs, o) );
          LOG.debug(msg + ": deleted " + o);
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        msg += ": unable to delete " + err + ": " + eH.getMessage();
        tx.rollback();
        throw new HibernateException(msg);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      LOG.error(eH.getMessage());
      throw new HibernateException(eH.getMessage());
    }
    LOG.info(msg + ": deleted " + objects.size());
  } // protected static void delete(objects)

  // @return  A Hibernate session 
	protected static Session getSession()
    throws HibernateException
  {
		return FACTORY.openSession();
	} // protected static Session getSession()

  // Save an object
  // @throws  HibernateException
  protected static void save(Object o) 
    throws HibernateException
  { 
    Set objects = new LinkedHashSet();
    objects.add(o);
    HibernateHelper.save(objects);
  } // protected static void save(o)

  // Save multiple objects in one transaction
  // @throws  HibernateException
  protected static void save(Set objects)
    throws HibernateException
  { 
    Object err = null;
    String  msg = "save";
    LOG.debug(msg + ": will save " + objects.size());
    try {
      Session     hs = HibernateHelper.getSession();
      Transaction tx = hs.beginTransaction();
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          Object o = iter.next();
          err = o;
          LOG.debug(msg + ": saving " + o);
          hs.saveOrUpdate(o);
          LOG.debug(msg + ": saved " + o);
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        msg += ": unable to save " + err + ": " + eH.getMessage();
        tx.rollback();
        throw new HibernateException(msg);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      LOG.error(eH.getMessage());
      throw new HibernateException(eH.getMessage());
    }
    LOG.info(msg + ": saved " + objects.size());
  } // protected static void save(objects)

  protected static void saveAndDelete(Set saves, Set deletes)
    throws HibernateException
  { 
    Object err = null;
    try {
      Session     hs    = HibernateHelper.getSession();
      Transaction tx    = hs.beginTransaction();
      Iterator    iterD = deletes.iterator();
      Iterator    iterS = saves.iterator();
      try {
        while (iterD.hasNext()) {
          Object o = iterD.next();
          err = o;
          try {
            hs.delete( _getPersistent(hs, o) );
          }
          catch (HibernateException eH) {
            String msg = "unable to delete " + o + ": " + eH.getMessage();
            throw new HibernateException(msg);
          }
        }
        while (iterS.hasNext()) {
          Object o = iterS.next();
          err = o;
          try {
            hs.saveOrUpdate(o);
          }
          catch (HibernateException eH) {
            String msg = "unable to save " + o + ": " + eH.getMessage();
            throw new HibernateException(msg);
          }
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw new HibernateException(eH.getMessage());
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      LOG.error(eH.getMessage());
      throw new HibernateException(eH.getMessage());
    }
    LOG.info("saved: " + saves.size() + " deleted: " + deletes.size());
  } // protected static void saveAndDelete(saves, deletes)


  // Private Class Methods
  private static Object _getPersistent(Session hs, Object o) {
    boolean persistent  = false;
    if (hs.contains(o)) {
      persistent = true;
    }
    else {
      try {
        hs.update(o);
        if (hs.contains(o)) {
          persistent = true;       
        }
      }
      catch (HibernateException eH) {
        // ignore
      }
    }
    if (persistent == false) {
      try {
        throw new RuntimeException();
      }
      catch (RuntimeException eR) {
        String err = ERR_GP + o + ":" + eR.getMessage();
        LOG.fatal(err);
        eR.printStackTrace();
        throw new RuntimeException(err);
      }
    }
    return o;
  } // private static Object _getPersistent(hs, o)

}


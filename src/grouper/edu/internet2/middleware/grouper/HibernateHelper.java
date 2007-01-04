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

package edu.internet2.middleware.grouper;
import  java.io.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;

/**
 * Hibernate utility helper class.
 * <p/>
 * This code was initially derived from code in the book <i>Hibernate In
 * Action</i>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateHelper.java,v 1.28 2007-01-04 17:17:45 blair Exp $
 */
class HibernateHelper {

  // PRIVATE CLASS CONSTANTS //
  private static final Configuration  CFG;
  private static final SessionFactory FACTORY;


  // STATIC //
  static {
    try {
      // Find the custom configuration file
      InputStream in  = HibernateHelper.class.getResourceAsStream(GrouperConfig.HIBERNATE_CF);  
      Properties  p   = new Properties();
      p.load(in);
      // And now load all configuration information
      CFG     = new Configuration()
        .addProperties(p)
        .addClass(Attribute.class)
        .addClass(Field.class)
        .addClass(GrouperSession.class)
        .addClass(GroupType.class)
        .addClass(HibernateSubject.class)
        .addClass(HibernateSubjectAttribute.class)
        .addClass(Member.class)
        .addClass(Membership.class)
        .addClass(Owner.class)
        .addClass(Settings.class)
        ;
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      // Catch *all* the errors
      String msg = E.HIBERNATE_INIT + t.getMessage();
      ErrorLog.fatal(HibernateHelper.class, msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  // PROTECTED CLASS METHODS //

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
    DebugLog.info(HibernateHelper.class, msg + ": will delete " + objects.size());
    try {
      Session     hs    = HibernateHelper.getSession();
      Transaction tx    = hs.beginTransaction();
      Object      o ;
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          o = iter.next();
          err = o;
          DebugLog.info(HibernateHelper.class, msg + ": deleting " + o);
          hs.delete( _getPersistent(hs, o) );
          DebugLog.info(HibernateHelper.class, msg + ": deleted " + o);
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        msg += ": unable to delete " + err + ": " + eH.getMessage();
        tx.rollback();
        throw new HibernateException(msg, eH);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      msg = E.HIBERNATE + eH.getMessage();
      ErrorLog.error(HibernateHelper.class, msg);
      throw new HibernateException(msg, eH);
    }
    DebugLog.info(HibernateHelper.class, msg + ": deleted " + objects.size());
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
    DebugLog.info(HibernateHelper.class, msg + ": will save " + objects.size());
    try {
      Session     hs    = HibernateHelper.getSession();
      Transaction tx    = hs.beginTransaction();
      Object      o;
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          o = iter.next();
          err = o;
          DebugLog.info(HibernateHelper.class, msg + ": saving " + o);
          hs.saveOrUpdate(o);
          DebugLog.info(HibernateHelper.class, msg + ": saved " + o);
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        msg += ": unable to save " + err + ": " + eH.getMessage();
        tx.rollback();
        throw new HibernateException(msg, eH);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      msg = E.HIBERNATE + eH.getMessage();
      ErrorLog.error(HibernateHelper.class, msg);
      throw new HibernateException(msg, eH);
    }
    DebugLog.info(HibernateHelper.class, msg + ": saved " + objects.size());
  } // protected static void save(objects)

  protected static void saveAndDelete(Set saves, Set deletes)
    throws HibernateException
  { 
    try {
      Session     hs    = HibernateHelper.getSession();
      Transaction tx    = hs.beginTransaction();
      Object      oD;
      Object      oS;
      Iterator    iterD = deletes.iterator();
      Iterator    iterS = saves.iterator();
      try {
        while (iterD.hasNext()) {
          oD = iterD.next();
          try {
            hs.delete( _getPersistent(hs, oD) );
          }
          catch (HibernateException eH) {
            String msg = E.HIBERNATE + "unable to delete " + oD + ": " + eH.getMessage();
            throw new HibernateException(msg, eH);
          }
        }
        while (iterS.hasNext()) {
          oS = iterS.next();
          try {
            hs.saveOrUpdate(oS);
          }
          catch (HibernateException eH) {
            String msg = E.HIBERNATE + "unable to save " + oS + ": " + eH.getMessage();
            throw new HibernateException(msg, eH);
          }
        }
        try {
          tx.commit();
        }
        catch (HibernateException eH) {
          String msg = E.HIBERNATE_COMMIT + eH.getMessage();
          throw new HibernateException(msg, eH);
        }
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw new HibernateException(eH.getMessage(), eH);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      ErrorLog.error(HibernateHelper.class, msg);
      throw new HibernateException(msg, eH);
    }
    DebugLog.info(HibernateHelper.class, "saved: " + saves.size() + " deleted: " + deletes.size());
  } // protected static void saveAndDelete(saves, deletes)


  // PRIVATE CLASS METHODS //
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
        ErrorLog.error(HibernateHelper.class, E.HH_GETPERSISTENT + eH.getMessage());
      }
    }
    if (persistent == false) {
      // I think this was done in an effort to try and get an actual stacktrace
      // but does it actually work?
      try {
        throw new GrouperRuntimeException();
      }
      catch (GrouperRuntimeException eGR) {
        String msg = E.HIBERNATE_GETPERSISTENT + o + ":" + eGR.getMessage();
        ErrorLog.fatal(HibernateHelper.class, msg);
        eGR.printStackTrace();
        throw new GrouperRuntimeException(msg, eGR);
      }
    }
    return o;
  } // private static Object _getPersistent(hs, o)

}


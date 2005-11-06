/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * This code was initially derived code in the book <i>Hibernate In
 * Action</i>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateHelper.java,v 1.1.2.2 2005-11-06 17:52:26 blair Exp $
 */
class HibernateHelper {

  // Private Class Constants
	private static final SessionFactory factory;


  // Private Class Variables
	private static Log log = LogFactory.getLog(HibernateHelper.class);


  // Create the static session factory 
	static {
		try {
      factory = new Configuration()
        .addClass(Attribute.class)
        .addClass(Field.class)
        .addClass(Group.class)
        .addClass(GrouperSession.class)
        .addClass(GroupType.class)
        .addClass(Member.class)
        .addClass(Membership.class)
        .addClass(Privilege.class)
        .addClass(Stem.class)
        .buildSessionFactory()
        ;
		} 
    catch (Throwable e) {
      // Catch *all* the errors
      log.fatal(
        "Unable to build HibernateSessionFactory: " + e.getMessage()
      );
			throw new ExceptionInInitializerError(e.getMessage());
		}
	}


  // Protected Class Methods

  protected static void delete(Object o) 
    throws HibernateException
  {
    Set objects = new HashSet();
    objects.add(o);
    HibernateHelper.delete(objects);
  } // protected static void delete(o)

  // Delete multiple objects in one transaction
  // @throws  HibernateException
  protected static void delete(Set objects)
    throws HibernateException
  { 
    try {
      Session     hs = HibernateHelper.getSession();
      Transaction tx = hs.beginTransaction();
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          Object o = iter.next();
          hs.delete(o); 
        }
        tx.commit();
      }
      catch (HibernateException e) {
        tx.rollback();
        throw new HibernateException(e.getMessage());
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException e) {
      throw new HibernateException(e.getMessage());
    }
  } // protected static void delete(objects)

  // @return  A Hibernate session 
	protected static Session getSession()
    throws HibernateException
  {
		return factory.openSession();
	} // protected static Session getSession()

  // Save an object
  // @throws  HibernateException
  protected static void save(Object o) 
    throws HibernateException
  { 
    Set objects = new HashSet();
    objects.add(o);
    HibernateHelper.save(objects);
  } // protected static void save(o)

  // Save multiple objects in one transaction
  // @throws  HibernateException
  protected static void save(Set objects)
    throws HibernateException
  { 
    try {
      Session     hs = HibernateHelper.getSession();
      Transaction tx = hs.beginTransaction();
      Iterator    iter  = objects.iterator();
      try {
        while (iter.hasNext()) {
          Object o = iter.next();
          hs.saveOrUpdate(o);
        }
        tx.commit();
      }
      catch (HibernateException e) {
        tx.rollback();
        throw new HibernateException(e.getMessage());
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException e) {
      throw new HibernateException(e.getMessage());
    }
  } // protected static void save(objects)

}


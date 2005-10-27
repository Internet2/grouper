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

import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;
import  org.apache.commons.logging.*;

/**
 * Hibernate utility helper class.
 * <p/>
 * This code is derived from code in the book <i>Hibernate In
 * Action</i>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateUtil.java,v 1.1.2.6 2005-10-27 18:09:44 blair Exp $
 */
class HibernateUtil {

  // Private Class Constants
	private static final SessionFactory factory;


  // Private Class Variables
	private static Log log = LogFactory.getLog(HibernateUtil.class);


  // Create the static session factory 
	static {
		try {
      factory = new Configuration()
        .addClass(GrouperSession.class)
        .addClass(Member.class)
        .buildSessionFactory()
        ;
		} 
    catch (Throwable e) {
      // Catch *all* the errors
      log.fatal(
        "Unable to build HibernateSessionFactory: " + e.getMessage()
      );
			throw new ExceptionInInitializerError(e);
		}
	}


  // Protected Class Methods

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
    try {
      Session     hs = HibernateUtil.getSession();
      Transaction tx = hs.beginTransaction();
      try {
        hs.save(o);
        tx.commit();
      }
      catch (HibernateException e) {
        tx.rollback();
        throw new HibernateException(e);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException e) {
      throw new HibernateException(e);
    }
  } // protected static void save(o)

}


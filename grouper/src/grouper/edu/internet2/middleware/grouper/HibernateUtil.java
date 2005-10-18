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
 * @version $Id: HibernateUtil.java,v 1.1.2.3 2005-10-18 16:09:32 blair Exp $
 */

class HibernateUtil {

  // PRIVATE CLASS CONSTANTS
	private static final SessionFactory factory;

  // PRIVATE CLASS VARIABLES
	private static        Log           log = LogFactory.getLog(HibernateUtil.class);

  // Create the static session factory 
	static {
		try {
			factory = new Configuration().configure().buildSessionFactory();
		} 
    catch (Throwable e) {
      // Catch *all* the errors
      log.fatal(
        "Unable to build HibernateSessionFactory: " + e.getMessage()
      );
			throw new ExceptionInInitializerError(e);
		}
	}


  // PUBLIC CLASS METHODS
 
  // @return  A Hibernate session 
	public static Session getSession()
    throws HibernateException
  {
		return factory.openSession();
	} // public static Session getSession()

}


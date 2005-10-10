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
 * @version $Id: HibernateUtil.java,v 1.1.2.1 2005-10-10 20:20:08 blair Exp $
 */

public class HibernateUtil {

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


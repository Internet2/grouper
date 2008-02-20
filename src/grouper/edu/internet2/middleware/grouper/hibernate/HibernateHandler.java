/*
 * Created on Jun 21, 2005
 *
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the hibernate session object
 * @version $Id: HibernateHandler.java,v 1.2 2008-02-20 08:41:45 mchyzer Exp $
 * @author mchyzer
 */

public interface HibernateHandler {

  /**
   * This method will be called with the hibernate session object to do 
   * what you wish.  Note, HibernateException or RuntimeExceptions can be
   * thrown by this method... others should be handled somehow..
   * @param hibernateSession is the hibernate session, note, this hibernateSession 
   * will be the same as passed in if it existed, else a new one
   * @return the return value to be passed to return value of callback method
   * @throws GrouperDAOException if there is a problem
   */
  public Object callback(HibernateSession hibernateSession) throws GrouperDAOException;
}

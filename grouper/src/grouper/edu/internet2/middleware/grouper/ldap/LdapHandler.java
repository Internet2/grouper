/*
 * Created on Jun 21, 2005
 *
 */
package edu.internet2.middleware.grouper.ldap;

import javax.naming.NamingException;

/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the ldap session object
 * @version $Id: HibernateHandler.java,v 1.3 2009-02-06 16:33:18 mchyzer Exp $
 * @author mchyzer
 */

public interface LdapHandler {

  /**
   * This method will be called with the hibernate session object to do 
   * what you wish.  Note, HibernateException or RuntimeExceptions can be
   * thrown by this method... others should be handled somehow..
   * @param ldapHandlerBean holds the ldap object to do operations on
   * @return the return value to be passed to return value of callback method
   * @throws NamingException if there is a problem
   */
  public Object callback(LdapHandlerBean ldapHandlerBean) throws NamingException;
}

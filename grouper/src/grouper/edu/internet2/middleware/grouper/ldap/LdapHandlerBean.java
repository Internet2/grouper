/*
 * @author mchyzer
 * $Id: HibernateHandlerBean.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ldap;

import edu.vt.middleware.ldap.Ldap;



/**
 * bean with reference to the ldap object
 */
public class LdapHandlerBean {
    
  /** ldap object */
  private Ldap ldap;

  /**
   * ldap object
   * @return ldap object
   */
  public Ldap getLdap() {
    return this.ldap;
  }

  /**
   * ldap object
   * @param ldap1
   */
  public void setLdap(Ldap ldap1) {
    this.ldap = ldap1;
  }



}

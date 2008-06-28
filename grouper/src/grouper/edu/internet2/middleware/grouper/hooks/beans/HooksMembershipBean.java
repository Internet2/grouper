/*
 * @author mchyzer
 * $Id: HooksMembershipBean.java,v 1.2 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;



/**
 * bean to hold objects for membership low level hooks
 */
public class HooksMembershipBean extends HooksBean {
  
  /** object being affected */
  private Membership membership = null;
  
  /**
   * @param theMembership
   */
  public HooksMembershipBean(Membership theMembership) {
    this.membership = theMembership;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public Membership getMembership() {
    return this.membership;
  }

}

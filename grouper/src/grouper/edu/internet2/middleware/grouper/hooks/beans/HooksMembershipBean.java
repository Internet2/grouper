/*
 * @author mchyzer
 * $Id: HooksMembershipBean.java,v 1.1 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;



/**
 *
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

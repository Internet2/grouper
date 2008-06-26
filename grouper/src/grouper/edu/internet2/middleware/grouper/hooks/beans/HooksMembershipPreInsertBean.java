/*
 * @author mchyzer
 * $Id: HooksMembershipPreInsertBean.java,v 1.4 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * pre insert bean
 */
public class HooksMembershipPreInsertBean extends HooksBean {

  /** object being inserted */
  private Membership membership = null;
  
  /**
   * @param theMembership
   */
  public HooksMembershipPreInsertBean(Membership theMembership) {
    this.membership = theMembership;
  }
  
  /**
   * object being inserted
   * @return the MembershipDAO
   */
  public Membership getMembership() {
    return this.membership;
  }

  
  
}

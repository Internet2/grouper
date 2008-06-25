/*
 * @author mchyzer
 * $Id: HooksMembershipPreInsertBean.java,v 1.3 2008-06-25 05:46:06 mchyzer Exp $
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
   * @param theHooksContext
   * @param theMembership
   */
  public HooksMembershipPreInsertBean(HooksContext theHooksContext, Membership theMembership) {
    super(theHooksContext);
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

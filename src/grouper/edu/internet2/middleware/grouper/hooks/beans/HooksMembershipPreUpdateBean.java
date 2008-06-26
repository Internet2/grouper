/*
 * @author mchyzer
 * $Id: HooksMembershipPreUpdateBean.java,v 1.4 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * pre update bean
 */
public class HooksMembershipPreUpdateBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPreUpdateBean(Membership theMembership) {
    super(theMembership);
  }

  
  
}

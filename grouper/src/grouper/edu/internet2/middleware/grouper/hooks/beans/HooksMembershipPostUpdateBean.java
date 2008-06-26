/*
 * @author mchyzer
 * $Id: HooksMembershipPostUpdateBean.java,v 1.4 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * post update bean
 */
public class HooksMembershipPostUpdateBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPostUpdateBean(Membership theMembership) {
    super(theMembership);
  }

  
}

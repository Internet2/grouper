/*
 * @author mchyzer
 * $Id: HooksMembershipPreDeleteBean.java,v 1.4 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * pre delete bean
 */
public class HooksMembershipPreDeleteBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPreDeleteBean(Membership theMembership) {
    super(theMembership);
  }

  
}

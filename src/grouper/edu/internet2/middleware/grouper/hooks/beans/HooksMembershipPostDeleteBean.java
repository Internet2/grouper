/*
 * @author mchyzer
 * $Id: HooksMembershipPostDeleteBean.java,v 1.4 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * post delete bean
 */
public class HooksMembershipPostDeleteBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPostDeleteBean(Membership theMembership) {
    super(theMembership);
  }

  
}

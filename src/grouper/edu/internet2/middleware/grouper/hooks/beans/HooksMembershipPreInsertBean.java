/*
 * @author mchyzer
 * $Id: HooksMembershipPreInsertBean.java,v 1.5 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * pre insert bean
 */
public class HooksMembershipPreInsertBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPreInsertBean(Membership theMembership) {
    super(theMembership);
  }

  
}

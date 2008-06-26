/*
 * @author mchyzer
 * $Id: HooksMembershipPostInsertBean.java,v 1.4 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Membership;


/**
 * post insert bean
 */
public class HooksMembershipPostInsertBean extends HooksMembershipBean {

  /**
   * @param theMembership
   */
  public HooksMembershipPostInsertBean(Membership theMembership) {
    super(theMembership);
  }

  
}

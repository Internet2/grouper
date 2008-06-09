/**
 * @author mchyzer
 * $Id: HooksMembershipPreUpdateHighLevelBean.java,v 1.1.2.1 2008-06-09 19:26:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.DefaultMemberOf;


/**
 * pre update bean for high level membership change (the main change, not
 * the side effects like adding the member to the groups where the group
 * to be added to is a member)
 */
public class HooksMembershipPreUpdateHighLevelBean extends HooksBean {

  /** object being inserted */
  private DefaultMemberOf defaultMemberOf = null;
  
  /**
   * @param theHooksContext
   * @param theDefaultMemberOf 
   */
  public HooksMembershipPreUpdateHighLevelBean(HooksContext theHooksContext, 
      DefaultMemberOf theDefaultMemberOf) {
    super(theHooksContext);
    this.defaultMemberOf = theDefaultMemberOf;
  }
  
  /**
   * @return the defaultMemberOf
   */
  public DefaultMemberOf getDefaultMemberOf() {
    return this.defaultMemberOf;
  }
  
}

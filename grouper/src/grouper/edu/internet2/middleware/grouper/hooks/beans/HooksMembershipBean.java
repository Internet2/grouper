/*
 * @author mchyzer
 * $Id: HooksMembershipBean.java,v 1.3 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean to hold objects for membership low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksMembershipBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: membership */
  public static final String FIELD_MEMBERSHIP = "membership";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_MEMBERSHIP);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Membership membership = null;
  
  /**
   * 
   */
  public HooksMembershipBean() {
    super();
  }

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

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMembershipBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}

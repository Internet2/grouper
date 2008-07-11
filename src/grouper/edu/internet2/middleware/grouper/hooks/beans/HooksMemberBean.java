/*
 * @author mchyzer
 * $Id: HooksMemberBean.java,v 1.3 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for member low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksMemberBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: member */
  public static final String FIELD_MEMBER = "member";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_MEMBER);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Member member = null;
  
  /**
   * 
   */
  public HooksMemberBean() {
    super();
  }

  /**
   * @param theMember
   */
  public HooksMemberBean(Member theMember) {
    this.member = theMember;
  }
  
  /**
   * object being inserted
   * @return the Member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMemberBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}

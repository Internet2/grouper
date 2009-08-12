/**
 * @author mchyzer
 * $Id: HooksMembershipChangeBean.java,v 1.4 2009-08-12 12:44:45 shilen Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * pre/post update bean for high level membership change (the main change, not
 * the side effects like adding the member to the groups where the group
 * to be added to is a member)
 */
@GrouperIgnoreDbVersion
public class HooksMembershipChangeBean extends HooksBean {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet();

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  Membership ms = null;
  
  /**
   * 
   */
  public HooksMembershipChangeBean() {
    super();
  }

  /**
   * @param ms 
   */
  public HooksMembershipChangeBean(Membership ms) {
    this.ms = ms;
  }

  /**
   * composite if applicable
   * @return the composite
   */
  public Composite getComposite() {
    return this.ms.getViaComposite();
  }

  /**
   * field for membership
   * @return the field
   */
  public Field getField() {
    return this.ms.getList();
  }

  /**
   * group for membership
   * @return the group
   */
  public Group getGroup() {
    return this.ms.getGroup();
  }

  /**
   * member being assigned
   * @return the member
   */
  public Member getMember() {
    return this.ms.getMember();
  }

  /**
   * membership dto
   * @return the membership
   */
  public Membership getMembership() {
    return this.ms;
  }

  /**
   * stem
   * @return the stem
   */
  public Stem getStem() {
    return this.ms.getStem();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMembershipChangeBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}

/**
 * @author mchyzer
 * $Id: HooksMembershipPostAddMemberBean.java,v 1.4 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;


/**
 * post update bean for high level membership change (the main change, not
 * the side effects like adding the member to the groups where the group
 * to be added to is a member)
 */
public class HooksMembershipPostAddMemberBean extends HooksBean {

  /** object being inserted */
  private DefaultMemberOf defaultMemberOf = null;
  /**
   * composite if applicable 
   */
  private Composite composite;
  /**
   * field for membership 
   */
  private Field field;
  /**
   * group for membership 
   */
  private Group group;
  /**
   * member being assigned 
   */
  private Member member;
  /**
   * membership dto 
   */
  private Membership membership;
  /**
   * stem 
   */
  private Stem stem;
  
  /**
   * @param theHooksContext
   * @param theDefaultMemberOf 
   */
  public HooksMembershipPostAddMemberBean(
      DefaultMemberOf theDefaultMemberOf) {
    this.defaultMemberOf = theDefaultMemberOf;
    this.composite = theDefaultMemberOf.getComposite();
    this.field = theDefaultMemberOf.getField();
    this.group = theDefaultMemberOf.getGroup();
    this.member = theDefaultMemberOf.getMember();
    this.membership = theDefaultMemberOf.getMembership();
    this.stem = theDefaultMemberOf.getStem();
  }
  
  /**
   * @return the defaultMemberOf
   */
  public DefaultMemberOf getDefaultMemberOf() {
    return this.defaultMemberOf;
  }

  /**
   * composite if applicable
   * @return the composite
   */
  public Composite getComposite() {
    return this.composite;
  }

  /**
   * field for membership
   * @return the field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * group for membership
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * member being assigned
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * membership dto
   * @return the membership
   */
  public Membership getMembership() {
    return this.membership;
  }

  /**
   * stem
   * @return the stem
   */
  public Stem getStem() {
    return this.stem;
  }
  
}

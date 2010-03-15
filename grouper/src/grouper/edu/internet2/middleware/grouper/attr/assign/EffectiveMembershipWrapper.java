/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;


/**
 * wraps an effective membership which may or may not exist
 */
public class EffectiveMembershipWrapper implements AttributeAssignable {

  /**
   * group
   */
  private Group group;
  
  /**
   * member
   */
  private Member member;

  /**
   * 
   */
  public EffectiveMembershipWrapper() {
    
  }
  
  /**
   * @param group1
   * @param member1
   */
  public EffectiveMembershipWrapper(Group group1, Member member1) {
    super();
    this.group = group1;
    this.member = member1;
  }

  /**
   * group
   * @return group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * group
   * @param group1
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }

  /**
   * member
   * @return member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * member
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignable#getAttributeDelegate()
   */
  public AttributeAssignBaseDelegate getAttributeDelegate() {
    return this.group.getAttributeDelegateEffMship(this.member);
  }
  
}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * bean for membership rules
 */
public class RulesMembershipBean extends RulesBean {

  /**
   * 
   */
  public RulesMembershipBean() {
    super();
    
  }

  /**
   * @param membership1
   * @param group1
   * @param subject1
   */
  public RulesMembershipBean(Membership membership1, Group group1, Subject subject1) {
    super();
    this.membership = membership1;
    this.group = group1;
    this.subject = subject1;
  }

  /**
   * @param member1
   * @param group1
   * @param subject1
   */
  public RulesMembershipBean(Member member1, Group group1, Subject subject1) {
    super();
    this.member = member1;
    this.group = group1;
    this.subject = subject1;
  }

  /** membership */
  private Membership membership;

  /** member */
  private Member member;

  /** group */
  private Group group;
  
  /** subject */
  private Subject subject;

  
  /**
   * membership
   * @return the membership
   */
  public Membership getMembership() {
    return this.membership;
  }
  
  /**
   * membership
   * @param membership1 the membership to set
   */
  public void setMembership(Membership membership1) {
    this.membership = membership1;
  }
  
  /**
   * group
   * @return the group
   */
  @Override
  public Group getGroup() {
    return this.group;
  }
  
  /**
   * group
   * @param group1 the group to set
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }
  
  /**
   * subject
   * @return the subject
   */
  @Override
  public Subject getSubject() {
    return this.subject;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.rules.beans.RulesBean#getMemberId()
   */
  @Override
  public String getMemberId() {
    if (this.membership == null) {
      return this.member == null ? null : this.member.getUuid();
    }
    return this.membership == null ? null : this.membership.getMemberUuid();
  }
  
  /**
   * subject
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.group != null) {
      result.append("group: ").append(this.group.getName()).append(", ");
    }
    if (this.membership != null) {
      result.append("membership: ").append(this.membership.toString()).append(", ");
    }
    if (this.subject != null) {
      result.append("subject: ").append(GrouperUtil.subjectToString(this.subject)).append(", ");
    }
    if (this.member != null) {
      result.append("member: ").append(this.member).append(", ");
    }
    return result.toString();
  }
  

}

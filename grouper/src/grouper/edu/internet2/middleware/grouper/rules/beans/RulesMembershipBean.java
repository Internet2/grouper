/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
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

  /** membership */
  private Membership membership;

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
    return result.toString();
  }
  

}

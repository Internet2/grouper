/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
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
   * @param membership
   * @param group
   * @param subject
   */
  public RulesMembershipBean(Membership membership, Group group, Subject subject) {
    super();
    this.membership = membership;
    this.group = group;
    this.subject = subject;
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
   * @param membership the membership to set
   */
  public void setMembership(Membership membership) {
    this.membership = membership;
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
   * @param group the group to set
   */
  public void setGroup(Group group) {
    this.group = group;
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
   * @param subject the subject to set
   */
  public void setSubject(Subject subject) {
    this.subject = subject;
  }
}

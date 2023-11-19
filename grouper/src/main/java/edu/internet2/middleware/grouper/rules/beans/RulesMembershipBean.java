/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
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

  /** stem */
  private Stem stem;
  
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
   * subject source id
   * @return the subject
   */
  @Override
  public String getSubjectSourceId() {
    return this.subject == null ? null : this.subject.getSourceId();
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

  /**
   * @see edu.internet2.middleware.grouper.rules.beans.RulesBean#getStem()
   */
  @Override
  public Stem getStem() {
    if (this.stem == null) {
      Group theGroup = this.getGroup();
      this.stem = theGroup == null ? null : theGroup.getParentStem();
    }
    return this.stem;
  }
  
}

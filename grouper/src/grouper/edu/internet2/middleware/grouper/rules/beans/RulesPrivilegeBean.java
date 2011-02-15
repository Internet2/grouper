/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * bean for membership rules
 */
public class RulesPrivilegeBean extends RulesBean {

  /**
   * 
   */
  public RulesPrivilegeBean() {
    super();
    
  }

  /**
   * @param group1
   * @param subject1
   * @param privilege1 
   */
  public RulesPrivilegeBean(Group group1, Subject subject1, Privilege privilege1) {
    super();
    this.group = group1;
    this.subject = subject1;
    this.privilege = privilege1;
  }

  /**
   * @param stem1
   * @param subject1
   * @param privilege1 
   */
  public RulesPrivilegeBean(Stem stem1, Subject subject1, Privilege privilege1) {
    super();
    this.stem = stem1;
    this.subject = subject1;
    this.privilege = privilege1;
  }

  /**
   * @param attributeDef1
   * @param subject1
   * @param privilege1 
   */
  public RulesPrivilegeBean(AttributeDef attributeDef1, Subject subject1, Privilege privilege1) {
    super();
    this.attributeDef = attributeDef1;
    this.subject = subject1;
    this.privilege = privilege1;
  }

  /** member */
  private Member member;

  /** attributeDef */
  private AttributeDef attributeDef;

  /** group */
  private Group group;
  
  /** subject */
  private Subject subject;

  /** stem */
  private Stem stem;
  
  /** privilege being affected */
  private Privilege privilege;
  
  
  /**
   * privilege being affected
   * @return the privilege
   */
  public Privilege getPrivilege() {
    return this.privilege;
  }

  
  /**
   * privilege being affected
   * @param privilege1 the privilege to set
   */
  public void setPrivilege(Privilege privilege1) {
    this.privilege = privilege1;
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
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  
  /**
   * @param member the member to set
   */
  public void setMember(Member member) {
    this.member = member;
  }

  
  /**
   * @return the attributeDef
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  
  /**
   * @param attributeDef1 the attributeDef to set
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  
  /**
   * @param stem1 the stem to set
   */
  public void setStem(Stem stem1) {
    this.stem = stem1;
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
    return this.member == null ? null : this.member.getUuid();
  }
  
  /**
   * subject
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }
  
  /**
   * subject source id
   * @return the subject
   */
  @Override
  public String getSubjectSourceId() {
    if (this.subject != null) {
      return this.subject.getSourceId();
    }
    if (this.member != null) {
      return this.member.getSubjectSourceId();
    }
    return null;
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
    if (this.privilege != null) {
      result.append("privilege: ").append(this.privilege.toString()).append(", ");
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
      if (theGroup != null) {
        this.stem = theGroup.getParentStem();
      }
    }
    if (this.stem == null) {
      AttributeDef theAttributeDef = this.getAttributeDef();
      if (theAttributeDef != null) {
        this.stem = theAttributeDef.getParentStem();
      }
    }
    return this.stem;
  }
  
}

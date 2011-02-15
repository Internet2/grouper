/**
 * @author mchyzer
 * $Id: RulesMembershipBean.java 6957 2010-08-31 21:19:28Z mchyzer $
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;


/**
 * bean for permission rules
 */
public class RulesPermissionBean extends RulesBean {

  /**
   * 
   */
  public RulesPermissionBean() {
    super();
    
  }

  /**
   * 
   * @param attributeAssign1
   * @param role1
   * @param member1
   * @param attributeDefName1
   * @param attributeDef1
   * @param action1
   */
  public RulesPermissionBean(AttributeAssign attributeAssign1, Role role1, Member member1, 
      AttributeDefName attributeDefName1, AttributeDef attributeDef1, String action1) {
    super();
    this.attributeAssign = attributeAssign1;
    this.role = role1;
    this.member = member1;
    this.attributeDefName = attributeDefName1;
    this.attributeDef = attributeDef1;
    this.role = role1;
    this.action = action1;
  }
  
  /** member */
  private Member member;

  /** role */
  private Role role;
  
  /** attributeAssign */
  private AttributeAssign attributeAssign;
  
  /** attributeDefName */
  private AttributeDefName attributeDefName;
  
  /** attributeDef */
  private AttributeDef attributeDef;

  /** action */
  private String action;
  
  /** stem */
  private Stem stem;
  
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
   * role
   * @return role
   */
  public Role getRole() {
    return this.role;
  }

  /**
   * role
   * @param role1
   */
  public void setRole(Role role1) {
    this.role = role1;
  }

  /**
   * attributeAssign
   * @return attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
  }

  /**
   * attribute assign 
   * @param attributeAssign1
   */
  public void setAttributeAssign(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }

  /**
   * attributeDefName
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * @see RulesBean#hasAttributeDefName()
   */
  @Override
  public boolean hasAttributeDefName() {
    return true;
  }

  /**
   * @see RulesBean#hasAttributeDef()
   */
  @Override
  public boolean hasAttributeDef() {
    return true;
  }

  /**
   * attributeDefName
   * @param attributeDefName1
   */
  public void setAttributeDefName(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * attributeDef
   * @return attributeDef
   */
  @Override
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * attributeDef
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  /**
   * action
   * @return action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * action
   * @param action1
   */
  public void setAction(String action1) {
    this.action = action1;
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
   * subject source id
   * @return the subject
   */
  @Override
  public String getSubjectSourceId() {
    return this.member == null ? null : this.member.getSubjectSourceId();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    if (this.action != null) {
      result.append("action: ").append(this.action).append(", ");
    }
    if (this.attributeAssign != null) {
      result.append("attributeAssign: ").append(this.attributeAssign.toString()).append(", ");
    }
    if (this.attributeDef != null) {
      result.append("attributeDef: ").append(this.attributeDef).append(", ");
    }
    if (this.attributeDefName != null) {
      result.append("attributeDefName: ").append(this.attributeDefName).append(", ");
    }
    if (this.member != null) {
      result.append("member: ").append(this.member).append(", ");
    }
    if (this.role != null) {
      result.append("role: ").append(this.role).append(", ");
    }
    return result.toString();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.rules.beans.RulesBean#getStem()
   */
  @Override
  public Stem getStem() {
    if (this.stem == null) {
      AttributeDefName theAttributeDefName = this.getAttributeDefName();
      String stemId = theAttributeDefName == null ? null : theAttributeDefName.getStemId();
      this.stem = stemId == null ? null 
          : GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, true) ;
    }
    return this.stem;
  }
  


}

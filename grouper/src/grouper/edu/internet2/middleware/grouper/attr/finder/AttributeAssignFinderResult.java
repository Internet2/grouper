/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;


/**
 * one result of attribute assign query
 */
public class AttributeAssignFinderResult {

  /**
   * attribute definition
   */
  private AttributeDef attributeDef;
  
  /**
   * attribute definition name
   */
  private AttributeDefName attributeDefName;
  
  /**
   * 
   * @return attr def
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * 
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  /**
   * 
   * @return attr def name
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * attr def name
   * @param attributeDefName1
   */
  public void setAttributeDefName(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }


  /**
   * attribute assignment on an assignment
   */
  private AttributeAssign attributeAssignOnAssign;

  /**
   * attribute assignment on an assignment
   * @return the assign
   */
  public AttributeAssign getAttributeAssignOnAssign() {
    return this.attributeAssignOnAssign;
  }

  /**
   * attribute assignment on an assignment
   * @param attributeAssignOnAssign1
   */
  public void setAttributeAssignOnAssign(AttributeAssign attributeAssignOnAssign1) {
    this.attributeAssignOnAssign = attributeAssignOnAssign1;
  }

  /**
   * 
   */
  public AttributeAssignFinderResult() {
  }

  
  /**
   * owner attribute assign
   */
  private AttributeAssign ownerAttributeAssign;
  
  
  
  /**
   * owner attribute assign
   * @return owner attribute assign
   */
  public AttributeAssign getOwnerAttributeAssign() {
    return ownerAttributeAssign;
  }
  
  /**
   * 
   * @param ownerAttributeAssign1
   */
  public void setOwnerAttributeAssign(AttributeAssign ownerAttributeAssign1) {
    this.ownerAttributeAssign = ownerAttributeAssign1;
  }

  /**
   * attr def
   */
  private AttributeDef ownerAttributeDef;

  
  /**
   * attr def
   * @return the ownerAttributeDef
   */
  public AttributeDef getOwnerAttributeDef() {
    return this.ownerAttributeDef;
  }

  
  /**
   * attr def
   * @param ownerAttributeDef1 the ownerAttributeDef to set
   */
  public void setOwnerAttributeDef(AttributeDef ownerAttributeDef1) {
    this.ownerAttributeDef = ownerAttributeDef1;
  }


  /**
   * stem
   */
  private Stem ownerStem;
  
  /**
   * owner membership
   */
  private Membership ownerMembership;
  
  /**
   * owner membership
   * @return the ownerMembership
   */
  public Membership getOwnerMembership() {
    return this.ownerMembership;
  }
  
  /**
   * owner membership
   * @param ownerMembership1 the ownerMembership to set
   */
  public void setOwnerMembership(Membership ownerMembership1) {
    this.ownerMembership = ownerMembership1;
  }


  /**
   * member
   */
  private Member ownerMember;
  
  
  /**
   * member
   * @return the ownerMember
   */
  public Member getOwnerMember() {
    return this.ownerMember;
  }

  
  /**
   * member
   * @param ownerMember1 the ownerMember to set
   */
  public void setOwnerMember(Member ownerMember1) {
    this.ownerMember = ownerMember1;
  }

  /**
   * stem
   * @return the ownerStem
   */
  public Stem getOwnerStem() {
    return this.ownerStem;
  }
  
  /**
   * @param ownerStem1 the ownerStem to set
   */
  public void setOwnerStem(Stem ownerStem1) {
    this.ownerStem = ownerStem1;
  }


  /**
   * group
   */
  private Group ownerGroup;
  
  /**
   * group
   * @return the group
   */
  public Group getOwnerGroup() {
    return this.ownerGroup;
  }
  
  /**
   * group
   * @param ownerGroup1 the group to set
   */
  public void setOwnerGroup(Group ownerGroup1) {
    this.ownerGroup = ownerGroup1;
  }
  
  /**
   * attribute assign
   */
  private AttributeAssign attributeAssign;

  
  /**
   * attribute assign
   * @return the attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
  }

  
  /**
   * attribute assign
   * @param attributeAssign1 the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }
  
  /**
   * if getting values, this is the value
   */
  private Set<AttributeAssignValue> attributeAssignValues;

  /**
   * if getting values, this is the value
   * @return the value
   */
  public Set<AttributeAssignValue> getAttributeAssignValues() {
    return this.attributeAssignValues;
  }

  /**
   * if getting values, this is the value
   * @param attributeAssignValue1
   */
  public void setAttributeAssignValues(Set<AttributeAssignValue> attributeAssignValue1) {
    this.attributeAssignValues = attributeAssignValue1;
  }
  
}

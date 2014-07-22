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
package edu.internet2.middleware.grouper.membership;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.CompositeType;

/**
 * membership path node including both end nodes
 * @author mchyzer
 *
 */
public class MembershipPathNode implements Comparable<MembershipPathNode> {

  /**
   * default constructor
   */
  public MembershipPathNode() {
    
  }
  
  /**
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() {
    MembershipPathNode membershipPathNode = new MembershipPathNode();
    membershipPathNode.setComposite(this.composite);
    membershipPathNode.setCompositeType(this.compositeType);
    membershipPathNode.setLeftCompositeFactor(this.leftCompositeFactor);
    membershipPathNode.setMembershipOwnerType(this.membershipOwnerType);
    membershipPathNode.setOwnerAttributeDef(this.ownerAttributeDef);
    membershipPathNode.setOwnerGroup(this.ownerGroup);
    membershipPathNode.setOwnerStem(this.ownerStem);
    membershipPathNode.setRightCompositeFactor(this.rightCompositeFactor);
    return membershipPathNode;
  }



  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    
    return new HashCodeBuilder().append(this.composite).append(this.compositeType).append(this.ownerAttributeDef)
      .append(this.ownerGroup).append(this.ownerStem).append(this.membershipOwnerType).toHashCode();
    
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MembershipPathNode)) {
      return false;
    }
    MembershipPathNode that = (MembershipPathNode)obj;
    return new EqualsBuilder().append(this.composite, that.composite)
      .append(this.compositeType, that.compositeType).append(this.membershipOwnerType, that.membershipOwnerType)
      .append(this.ownerAttributeDef, that.ownerAttributeDef).append(this.ownerGroup, that.ownerGroup)
      .append(this.ownerStem, that.ownerStem).isEquals();
  }

  /**
   * constructor for group path code
   * @param field
   * @param theOwnerGroup
   */
  public MembershipPathNode(Field field, Group theOwnerGroup) {
    if (field.isGroupListField()) {
      this.setMembershipOwnerType(MembershipOwnerType.list);
    } else if (field.isGroupAccessField()) {
      this.setMembershipOwnerType(MembershipOwnerType.groupPrivilege);
    } else {
      throw new RuntimeException("Not expecting field type: " + field);
    }
    this.setOwnerGroup(theOwnerGroup);
  }
  
  /**
   * construct a composite group node
   * @param field
   * @param ownerGroup
   * @param compositeType
   * @param theLeftCompositeFactor
   * @param theRightCompositeFactor
   * @param theOtherFactor
   */
  public MembershipPathNode(Field field, Group ownerGroup,
      CompositeType compositeType,
      Group theLeftCompositeFactor, Group theRightCompositeFactor, Group theOtherFactor) {
    this(field, ownerGroup);
    this.composite = true;
    this.compositeType = compositeType;
    this.leftCompositeFactor = theLeftCompositeFactor;
    this.rightCompositeFactor = theRightCompositeFactor;
    this.otherFactor = theOtherFactor;
  }

  /**
   * constructor for stem path code
   * @param field
   * @param theOwnerStem
   */
  public MembershipPathNode(Field field, Stem theOwnerStem) {
    if (field.isStemListField()) {
      this.setMembershipOwnerType(MembershipOwnerType.stemPrivilege);
    } else {
      throw new RuntimeException("Not expecting field type: " + field);
    }
    this.setOwnerStem(theOwnerStem);
  }

  /**
   * constructor for attributeDef path code
   * @param field
   * @param theOwnerAttributeDef
   */
  public MembershipPathNode(Field field, AttributeDef theOwnerAttributeDef) {
    if (field.isAttributeDefListField()) {
      this.setMembershipOwnerType(MembershipOwnerType.attributeDefPrivilege);
    } else {
      throw new RuntimeException("Not expecting field type: " + field);
    }
    this.setOwnerAttributeDef(theOwnerAttributeDef);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    switch (this.membershipOwnerType) {
      case list:
      case groupPrivilege:
        if (this.composite) {
          switch (this.compositeType) {
            case COMPLEMENT:
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " with " + this.leftCompositeFactor.getName() + " and not in " + this.rightCompositeFactor.getName() + ")";
            case INTERSECTION:
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " of " + this.leftCompositeFactor.getName() + " and in " + this.rightCompositeFactor.getName() + ")";
            case UNION:
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " of " + this.leftCompositeFactor.getName() + " or in " + this.rightCompositeFactor.getName() + ")";
            default:
              throw new RuntimeException("Not expecting compositeType: " + this.compositeType);  
          }
        }
        return this.ownerGroup.getName();
      case stemPrivilege:
        return this.ownerStem.getName();
      case attributeDefPrivilege:
        return this.ownerAttributeDef.getName();
      default:
        throw new RuntimeException("Not expecting owner type: " + this.membershipOwnerType);    
    }
  }
  
  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   */
  private AttributeDef ownerAttributeDef;
  /**
   * if this is a list or group privilege, this is the owner group
   */
  private Group ownerGroup;
  /**
   * if this is a stem privilege, this is the owner stem
   */
  private Stem ownerStem;
  
  /**
   * type of composite, INTERSECTION, COMPLEMENT, UNION
   */
  private CompositeType compositeType;
  
  /**
   * if composite, this is the right composite factor
   * @return composite
   */
  public CompositeType getCompositeType() {
    return this.compositeType;
  }

  /**
   * if composite, this is the right composite factor
   * @param compositeType1
   */
  public void setCompositeType(CompositeType compositeType1) {
    this.compositeType = compositeType1;
  }

  /**
   * if this is a composite group
   */
  private boolean composite;

  /**
   * if composite, this is the right composite factor
   */
  private Group leftCompositeFactor;
  
  /**
   * if composite, this is the right composite factor
   */
  private Group rightCompositeFactor;

  /**
   * this is the factor not in the path
   */
  private Group otherFactor;
  
  /**
   * this is the factor not in the path
   * @return the otherFactor
   */
  public Group getOtherFactor() {
    return this.otherFactor;
  }
  
  /**
   * this is the factor not in the path
   * @param otherFactor1 the otherFactor to set
   */
  public void setOtherFactor(Group otherFactor1) {
    this.otherFactor = otherFactor1;
  }

  /**
   * if this is a composite group
   * @return composite
   */
  public boolean isComposite() {
    return this.composite;
  }

  /**
   * if this is a composite group
   * @param composite1
   */
  public void setComposite(boolean composite1) {
    this.composite = composite1;
  }

  /**
   * if composite, this is the right composite factor
   * @return factor
   */
  public Group getLeftCompositeFactor() {
    return this.leftCompositeFactor;
  }

  /**
   * if composite, this is the right composite factor
   * @param leftCompositeFactor1
   */
  public void setLeftCompositeFactor(Group leftCompositeFactor1) {
    this.leftCompositeFactor = leftCompositeFactor1;
  }

  /**
   * if composite, this is the right composite factor
   * @return right composite factor
   */
  public Group getRightCompositeFactor() {
    return this.rightCompositeFactor;
  }

  /**
   * if composite, this is the right composite factor
   * @param rightCompositeFactor1
   */
  public void setRightCompositeFactor(Group rightCompositeFactor1) {
    this.rightCompositeFactor = rightCompositeFactor1;
  }

  /**
   * what type e.g. list, or stemPrivilege
   */
  private MembershipOwnerType membershipOwnerType;

  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   * @return attribute def
   */
  public AttributeDef getOwnerAttributeDef() {
    return this.ownerAttributeDef;
  }

  /**
   * if this is a list or group privilege, this is the owner group
   * @return group
   */
  public Group getOwnerGroup() {
    return this.ownerGroup;
  }

  /**
   * if this is a stem privilege, this is the owner stem
   * @return owner stem
   */
  public Stem getOwnerStem() {
    return this.ownerStem;
  }

  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   * @param ownerAttributeDef1
   */
  public void setOwnerAttributeDef(AttributeDef ownerAttributeDef1) {
    this.ownerAttributeDef = ownerAttributeDef1;
  }

  /**
   * if this is a list or group privilege, this is the owner group
   * @param ownerGroup1
   */
  public void setOwnerGroup(Group ownerGroup1) {
    this.ownerGroup = ownerGroup1;
  }

  /**
   * if this is a stem privilege, this is the owner stem
   * @param ownerStem1
   */
  public void setOwnerStem(Stem ownerStem1) {
    this.ownerStem = ownerStem1;
  }

  /**
   * what type e.g. list, or stemPrivilege
   * @return owner type
   */
  public MembershipOwnerType getMembershipOwnerType() {
    return this.membershipOwnerType;
  }

  /**
   * what type e.g. list, or stemPrivilege
   * @param membershipOwnerType1
   */
  public void setMembershipOwnerType(MembershipOwnerType membershipOwnerType1) {
    this.membershipOwnerType = membershipOwnerType1;
  }

  /**
   * @see Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(MembershipPathNode membershipPathNode) {
    
    if (this.membershipOwnerType != membershipPathNode.membershipOwnerType) {
      if (this.membershipOwnerType == null) {
        return -1;
      }
      if (membershipPathNode.membershipOwnerType == null) {
        return -1;
      }
      return this.membershipOwnerType.name().compareTo(membershipPathNode.membershipOwnerType.name());
    }
    switch(this.membershipOwnerType) {
      case groupPrivilege:
      case list:
        if (this.ownerGroup == null && membershipPathNode.ownerGroup != null) {
          return -1;
        }
        return this.ownerGroup.compareTo(membershipPathNode.ownerGroup);
      case stemPrivilege:
        if (this.ownerStem == null && membershipPathNode.ownerStem != null) {
          return -1;
        }
        return this.ownerStem.compareTo(membershipPathNode.ownerStem);
        
      case attributeDefPrivilege:
        if (this.ownerAttributeDef == null && membershipPathNode.ownerAttributeDef != null) {
          return -1;
        }
        return this.ownerAttributeDef.compareTo(membershipPathNode.ownerAttributeDef);
        
      default:
        throw new RuntimeException("Not expecting membershipOwnerType: " + this.membershipOwnerType);
    }
  }

  
  
}

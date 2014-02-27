package edu.internet2.middleware.grouper.membership;

import org.apache.commons.lang.builder.EqualsBuilder;

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
   * @see Object#equals(Object)
   */
  public boolean equals(Object other) {
    if (!(other instanceof MembershipPathNode)) {
      return false;
    }
    MembershipPathNode that = (MembershipPathNode)other;
    return new EqualsBuilder()
      .append(this.composite, that.composite)
      .append(this.compositeType, that.compositeType)
      .append(this.leftCompositeFactor, that.leftCompositeFactor)
      .append(this.membershipOwnerType, that.membershipOwnerType)
      .append(this.ownerAttributeDef, that.ownerAttributeDef)
      .append(this.ownerGroup, that.ownerGroup)
      .append(this.ownerStem, that.ownerStem)
      .isEquals();
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
   */
  public MembershipPathNode(Field field, Group ownerGroup,
      CompositeType compositeType,
      Group theLeftCompositeFactor, Group theRightCompositeFactor) {
    this(field, ownerGroup);
    this.composite = true;
    this.compositeType = compositeType;
    this.leftCompositeFactor = theLeftCompositeFactor;
    this.rightCompositeFactor = theRightCompositeFactor;
  }

  /**
   * constructor for stem path code
   * @param field
   * @param theOwnerGroup
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
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " in " + this.leftCompositeFactor.getName() + " and not in " + this.rightCompositeFactor.getName() + ")";
            case INTERSECTION:
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " in " + this.leftCompositeFactor.getName() + " and in " + this.rightCompositeFactor.getName() + ")";
            case UNION:
              return this.ownerGroup.getName() + " (composite " + this.compositeType + " in " + this.leftCompositeFactor.getName() + " or in " + this.rightCompositeFactor.getName() + ")";
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

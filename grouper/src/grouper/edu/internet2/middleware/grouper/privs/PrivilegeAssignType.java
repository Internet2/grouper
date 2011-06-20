/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.privs;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.membership.MembershipType;


/**
 * how this subject has this privilege
 */
public enum PrivilegeAssignType {

  /**
   * immediately assigned
   */
  IMMEDIATE {

    @Override
    public boolean isImmediate() {
      return true;
    }
  },
  
  /**
   * effectively assigned
   */
  EFFECTIVE {

    @Override
    public boolean isImmediate() {
      return false;
    }
  },
  
  /**
   * has immediate and effective memberships
   */
  IMMEDIATE_AND_EFFECTIVE {

    @Override
    public boolean isImmediate() {
      return true;
    }
  };
  
  /**
   * if immediate
   * @return true/false
   */
  public abstract boolean isImmediate();
  
  /**
   * if allowed
   * @return true/false
   */
  public boolean isAllowed() {
    return true;
  }
  
  /**
   * name as javabean property
   * @return name
   */
  public String getName() {
    return this.name();
  }
  
  /**
   * convert a privilege to a type
   * @param privilegeAssignType
   * @param membership
   * @return the type
   */
  public static PrivilegeAssignType convertMembership(PrivilegeAssignType privilegeAssignType, Membership membership) {

    PrivilegeAssignType membershipAssignType = convertMembership(membership);
    return convert(privilegeAssignType, membershipAssignType);
  }
  
  /**
   * convert a privilege to a type
   * @param privilegeAssignType
   * @param anotherAssignType
   * @return the type
   */
  public static PrivilegeAssignType convert(PrivilegeAssignType privilegeAssignType, PrivilegeAssignType anotherAssignType) {

    if (privilegeAssignType == IMMEDIATE_AND_EFFECTIVE || anotherAssignType == IMMEDIATE_AND_EFFECTIVE) {
      return IMMEDIATE_AND_EFFECTIVE;
    }
    if (privilegeAssignType == null) {
      return anotherAssignType;
    }
    if (privilegeAssignType == IMMEDIATE && anotherAssignType == IMMEDIATE) {
      return IMMEDIATE;
    }
    
    if (privilegeAssignType == EFFECTIVE && anotherAssignType == EFFECTIVE) {
      return EFFECTIVE;
    }
    return IMMEDIATE_AND_EFFECTIVE;
  }
  
  /**
   * convert a privilege to a type
   * @param membership
   * @return the type
   */
  public static PrivilegeAssignType convertMembership(Membership membership) {
    if (membership.getTypeEnum() == MembershipType.IMMEDIATE) {
      return IMMEDIATE;
    }
    return EFFECTIVE;
  }
  
}

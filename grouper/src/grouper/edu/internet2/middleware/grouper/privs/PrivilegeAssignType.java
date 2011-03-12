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
   * convert a privilege to a type
   * @param privilegeAssignType
   * @param membership
   * @return the type
   */
  public static PrivilegeAssignType convertMembership(PrivilegeAssignType privilegeAssignType, Membership membership) {

    if (privilegeAssignType == IMMEDIATE_AND_EFFECTIVE ) {
      return IMMEDIATE_AND_EFFECTIVE;
    }
    PrivilegeAssignType membershipAssignType = convertMembership(membership);
    if (privilegeAssignType == null) {
      return membershipAssignType;
    }
    if (privilegeAssignType == IMMEDIATE && membershipAssignType == IMMEDIATE) {
      return IMMEDIATE;
    }
    
    if (privilegeAssignType == EFFECTIVE && membershipAssignType == EFFECTIVE) {
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

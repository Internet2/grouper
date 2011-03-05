package edu.internet2.middleware.grouper.privs;



/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class PrivilegeContainerImpl implements PrivilegeContainer {

  /**
   * 
   * @param privilege
   * @param privilegeAssignType
   */
  public PrivilegeContainerImpl(Privilege privilege,
      PrivilegeAssignType privilegeAssignType) {
    super();
    this.privilege = privilege;
    this.privilegeAssignType = privilegeAssignType;
  }

  /**
   * 
   */
  public PrivilegeContainerImpl() {
  }

  /** privilege */
  private Privilege privilege;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeContainer#getPrivilege()
   */
  public Privilege getPrivilege() {
    return this.privilege;
  }

  /** privilege assign type */
  private PrivilegeAssignType privilegeAssignType;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeContainer#getPrivilegeAssignType()
   */
  public PrivilegeAssignType getPrivilegeAssignType() {
    return this.privilegeAssignType;
  }

  
  /**
   * @param privilege1 the privilege to set
   */
  public void setPrivilege(Privilege privilege1) {
    this.privilege = privilege1;
  }

  
  /**
   * @param privilegeAssignType1 the privilegeAssignType to set
   */
  public void setPrivilegeAssignType(PrivilegeAssignType privilegeAssignType1) {
    this.privilegeAssignType = privilegeAssignType1;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Privilege: ");
    if (this.privilege == null) {
      result.append("null");
    } else {
      result.append(privilege.getName());
    }
    result.append(", type: ");
    if (this.privilegeAssignType == null) {
      result.append("null");
    } else {
      result.append(this.privilegeAssignType.name());
    }
    return result.toString();
  }

}

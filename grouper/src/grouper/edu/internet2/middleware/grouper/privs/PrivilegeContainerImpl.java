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
   * @param thePrivilegeName
   * @param privilegeAssignType
   */
  public PrivilegeContainerImpl(String thePrivilegeName,
      PrivilegeAssignType privilegeAssignType) {
    super();
    this.privilegeName = thePrivilegeName;
    this.privilegeAssignType = privilegeAssignType;
  }

  /**
   * 
   */
  public PrivilegeContainerImpl() {
  }

  /** privilege name */
  private String privilegeName;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeContainer#getPrivilegeName()
   */
  public String getPrivilegeName() {
    return this.privilegeName;
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
  public void setPrivilegeName(String privilege1) {
    this.privilegeName = privilege1;
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
    if (this.privilegeName == null) {
      result.append("null");
    } else {
      result.append(this.privilegeName);
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

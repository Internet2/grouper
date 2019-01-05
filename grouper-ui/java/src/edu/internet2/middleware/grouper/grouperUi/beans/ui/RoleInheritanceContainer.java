package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.permissions.role.Role;

public class RoleInheritanceContainer {
  
  /**
   * list of roles that implies the current role
   */
  private List<Role> rolesThatImplyThis = new ArrayList<Role>();
  
  /**
   * list of roles that immediately imply the current role
   */
  private List<Role> rolesThatImplyThisImmediate = new ArrayList<Role>();
  
  /**
   * list of roles that are implied by the current role
   */
  private List<Role> rolesImpliedByThis = new ArrayList<Role>();
  
  /**
   * list of roles that are immediately implied by the current role
   */
  private List<Role> rolesImpliedByThisImmediate = new ArrayList<Role>();

  /**
   * @return list of roles that implies the current role
   */
  public List<Role> getRolesThatImplyThis() {
    return rolesThatImplyThis;
  }

  /**
   * list of roles that implies the current role
   * @param rolesThatImplyThis
   */
  public void setRolesThatImplyThis(List<Role> rolesThatImplyThis) {
    this.rolesThatImplyThis = rolesThatImplyThis;
  }

  /**
   * @return list of roles that immediately imply the current role
   */
  public List<Role> getRolesThatImplyThisImmediate() {
    return rolesThatImplyThisImmediate;
  }

  /**
   * list of roles that immediately imply the current role
   * @param rolesThatImplyThisImmediate
   */
  public void setRolesThatImplyThisImmediate(List<Role> rolesThatImplyThisImmediate) {
    this.rolesThatImplyThisImmediate = rolesThatImplyThisImmediate;
  }

  /**
   * @return list of roles that are implied by the current role
   */
  public List<Role> getRolesImpliedByThis() {
    return rolesImpliedByThis;
  }

  /**
   * list of roles that are implied by the current role
   * @param rolesImpliedByThis
   */
  public void setRolesImpliedByThis(List<Role> rolesImpliedByThis) {
    this.rolesImpliedByThis = rolesImpliedByThis;
  }

  /**
   * @return list of roles that are immediately implied by the current role
   */
  public List<Role> getRolesImpliedByThisImmediate() {
    return rolesImpliedByThisImmediate;
  }

  /**
   * list of roles that are immediately implied by the current role
   * @param rolesImpliedByThisImmediate
   */
  public void setRolesImpliedByThisImmediate(List<Role> rolesImpliedByThisImmediate) {
    this.rolesImpliedByThisImmediate = rolesImpliedByThisImmediate;
  }
  
}

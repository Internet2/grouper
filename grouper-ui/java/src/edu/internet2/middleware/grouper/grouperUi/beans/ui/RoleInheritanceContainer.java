package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.permissions.role.Role;

public class RoleInheritanceContainer {
  
  private Role role;
  
  private List<Role> allRoles = new ArrayList<Role>();
  
  private List<Role> rolesThatImplyThis = new ArrayList<Role>();
  
  private List<Role> rolesThatImplyThisImmediate = new ArrayList<Role>();
  
  private List<Role> rolesImpliedByThis = new ArrayList<Role>();
  
  private List<Role> rolesImpliedByThisImmediate = new ArrayList<Role>();

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public List<Role> getAllRoles() {
    return allRoles;
  }

  public void setAllRoles(List<Role> allRoles) {
    this.allRoles = allRoles;
  }

  public List<Role> getRolesThatImplyThis() {
    return rolesThatImplyThis;
  }

  public void setRolesThatImplyThis(List<Role> rolesThatImplyThis) {
    this.rolesThatImplyThis = rolesThatImplyThis;
  }

  public List<Role> getRolesThatImplyThisImmediate() {
    return rolesThatImplyThisImmediate;
  }

  public void setRolesThatImplyThisImmediate(List<Role> rolesThatImplyThisImmediate) {
    this.rolesThatImplyThisImmediate = rolesThatImplyThisImmediate;
  }

  public List<Role> getRolesImpliedByThis() {
    return rolesImpliedByThis;
  }

  public void setRolesImpliedByThis(List<Role> rolesImpliedByThis) {
    this.rolesImpliedByThis = rolesImpliedByThis;
  }

  public List<Role> getRolesImpliedByThisImmediate() {
    return rolesImpliedByThisImmediate;
  }

  public void setRolesImpliedByThisImmediate(List<Role> rolesImpliedByThisImmediate) {
    this.rolesImpliedByThisImmediate = rolesImpliedByThisImmediate;
  }
  
}

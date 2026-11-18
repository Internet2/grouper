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
/**
 * @author mchyzer
 * $Id: RoleInheritanceDelegate.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions.role;

import java.io.Serializable;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.audit.PermissionInheritanceAudit;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate the role inheritance (role set) operations of a role.
 * <p>
 * Adding or removing an inheritance link changes which permissions the members of a role get,
 * so the public add and remove methods require the subject of the static grouper session to
 * have ADMIN on BOTH roles of the link (wheel and root are allowed).  Before this check, a
 * subject with ADMIN on one role could link it to any role it could merely VIEW, and so take
 * that role's permissions (UI role inheritance screen, GSH or API).
 * </p>
 * <p>
 * internal_addRoleToInheritFromThis is deliberately NOT checked: its only caller other than
 * addRoleToInheritFromThis is the XML import (RoleSet.xmlSaveBusinessProperties), which runs
 * as root.
 * </p>
 */
@SuppressWarnings("serial")
public class RoleInheritanceDelegate implements Serializable {

  /** keep a reference to the group */
  private Group group;
  
  /**
   * 
   * @param group1
   */
  public RoleInheritanceDelegate(Group group1) {
    this.group = group1;
    assertIsRole(group1);

  }

  /**
   * permissions in this role are inherited by other roles in the set returned (immediate or effective)
   * so if this role is loanAdministrator, then returned set has senior loan administrator
   * @return set of roles not including this role, or empty set if none available
   */
  public Set<Role> getRolesInheritPermissionsFromThis() {
    return GrouperDAOFactory.getFactory().getRoleSet().rolesInheritPermissionsFromThis(this.group.getId());
  }

  /**
   * permissions in this role are inherited by other roles in the set returned (immediate only)
   * so if this role is loanAdministrator, then returned set has senior loan administrator
   * @return set of roles not including this role, or empty set if none available
   */
  public Set<Role> getRolesInheritPermissionsFromThisImmediate() {
    return GrouperDAOFactory.getFactory().getRoleSet().rolesInheritPermissionsFromThisImmediate(this.group.getId());
  }

  /**
   * permissions in these returned roles inherit to this role (immediate or effective)
   * so if this role is senior loan administrator, then returned set has loanAdministrator
   * @return set of roles not including this role, or empty set if none available
   */
  public Set<Role> getRolesInheritPermissionsToThis() {
    return GrouperDAOFactory.getFactory().getRoleSet().rolesInheritPermissionsToThis(this.group.getId());
  }

  /**
   * permissions in these returned roles inherit to this role (immediate only)
   * so if this role is senior loan administrator, then returned set has loanAdministrator
   * @return set of roles not including this role, or empty set if none available
   */
  public Set<Role> getRolesInheritPermissionsToThisImmediate() {
    return GrouperDAOFactory.getFactory().getRoleSet().rolesInheritPermissionsToThisImmediate(this.group.getId());
  }

  /**
   * if a user has this role, then he also inherits permissions from the roleToAdd
   * for isntance this would be senior admin, and the argument would be admin.
   * The subject of the static grouper session needs ADMIN on this role and on roleToAdd
   * (or must be wheel or root).
   * @param roleToAdd
   * @return true if added, false if already there
   * @throws InsufficientPrivilegeException if the session subject cannot ADMIN both roles
   */
  public boolean addRoleToInheritFromThis(final Role roleToAdd) {
    assertIsRole(roleToAdd);
    // check privileges before anything is written (the internal method does not check)
    this.assertCanAdminBothRoles(roleToAdd);
    // audit who added the link: it gives every member of this role the other role's permissions
    return PermissionInheritanceAudit.roleInheritance(true, this.group, roleToAdd, 
        new PermissionInheritanceAudit.SetChange() {
      
      public boolean change() {
        return RoleInheritanceDelegate.this.internal_addRoleToInheritFromThis(roleToAdd, null);
      }
    });
  }

  /**
   * if a user has this role, then he also inherits permissions from the roleToAdd
   * @param roleToAdd
   * @param uuid is id or null if assigned
   * @return true if added, false if already there
   */
  public boolean internal_addRoleToInheritFromThis(Role roleToAdd, String uuid) {
    assertIsRole(roleToAdd);
    return GrouperSetEnum.ROLE_SET.addToGrouperSet(this.group, roleToAdd, uuid);
  }

  
  /**
   * if a user has this role, and he had inheriated permissions from roleToRemove directly, then 
   * remove that relationship.
   * The subject of the static grouper session needs ADMIN on this role and on roleToRemove
   * (or must be wheel or root).  Note: converting a role back to a group (Group update hook)
   * calls this for each link, so a non-wheel subject can only do that conversion if it can
   * ADMIN every role linked to it.
   * @param roleToRemove
   * @return true if removed, false if already not there
   * @throws InsufficientPrivilegeException if the session subject cannot ADMIN both roles
   */
  public boolean removeRoleFromInheritFromThis(final Role roleToRemove) {
    assertIsRole(roleToRemove);
    // removing a link can lift deny (disallow) assignments or take permissions away from the
    // members of the other role, so it needs the same privileges as adding one
    this.assertCanAdminBothRoles(roleToRemove);
    // audit who removed the link
    return PermissionInheritanceAudit.roleInheritance(false, this.group, roleToRemove, 
        new PermissionInheritanceAudit.SetChange() {
      
      public boolean change() {
        return GrouperSetEnum.ROLE_SET.removeFromGrouperSet(RoleInheritanceDelegate.this.group, roleToRemove);
      }
    });
  }
  
  /**
   * assert that the subject of the static grouper session can ADMIN this role and the other
   * role of an inheritance link.  Wheel and root can always.  ADMIN (not UPDATE) is required
   * because the link changes the permissions of the role's members, which is a privilege
   * change, not a membership change.
   * @param otherRole the other end of the link, already asserted to be a role group
   */
  private void assertCanAdminBothRoles(Role otherRole) {
    // throws if there is no static session, so a caller with no session fails closed
    Subject subject = GrouperSession.staticGrouperSession().getSubject();
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }
    for (Group role : new Group[] {this.group, (Group)otherRole}) {
      // secure=false: the check is about the session subject itself, which is allowed
      // to ask about its own privileges
      if (!role.canHavePrivilege(subject, AccessPrivilege.ADMIN.getName(), false)) {
        throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject)
            + " cannot ADMIN role " + role.getName()
            + ", ADMIN is required on both roles to change role inheritance");
      }
    }
  }

  /**
   * assert that this is a role
   * @param object 
   */
  private static void assertIsRole(Object object) {
    if (!(object instanceof Group)) {
      throw new RuntimeException("Expecting Group object, was: " + GrouperUtil.className(object));
    }
    Group group = (Group)object;
    if (!TypeOfGroup.role.equals(group.getTypeOfGroup())) {
      throw new RuntimeException("Requires this group to be of type 'role', but" +
          " instead is of type: " + group.getTypeOfGroup() + ": " + group.getName());
    }
  } 

  
}

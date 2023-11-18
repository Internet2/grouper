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
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * delegate the role
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
   * for isntance this would be senior admin, and the argument would be admin
   * @param roleToAdd
   * @return true if added, false if already there
   */
  public boolean addRoleToInheritFromThis(Role roleToAdd) {
    return this.internal_addRoleToInheritFromThis(roleToAdd, null);
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
   * remove that relationship
   * @param roleToRemove
   * @return true if removed, false if already not there
   */
  public boolean removeRoleFromInheritFromThis(Role roleToRemove) {
    assertIsRole(roleToRemove);
    return GrouperSetEnum.ROLE_SET.removeFromGrouperSet(this.group, roleToRemove);
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

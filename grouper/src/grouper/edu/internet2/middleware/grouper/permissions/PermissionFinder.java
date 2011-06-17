/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionFinder {

  /**
   * 
   * @param subject
   * @param permissionName
   * @param action
   * @return true if the subject has this permission in any role
   */
  public static boolean hasPermission(Subject subject, AttributeDefName permissionName, String action) {
    return hasPermission(subject, (Set<Role>)null, permissionName, action);
  }

  /**
   * 
   * @param subject
   * @param roles
   * @param permissionName
   * @param action
   * @return true if the subject has this permission in any role specified
   */
  public static boolean hasPermission(Subject subject, Set<Role> roles, AttributeDefName permissionName, String action) {
    Set<String> roleIds = null;
    
    if (subject == null) {
      throw new RuntimeException("Subject is required");
    }
    
    if (permissionName == null) {
      throw new RuntimeException("PermissionName is required");
    }
    
    if (StringUtils.isBlank(action)) {
      throw new RuntimeException("Action is required");
    }
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
    
    if (member == null) {
      //subject isnt a member, thus there are no permissions
      return false;
    }
    
    if (GrouperUtil.length(roles) > 0) {
      roleIds = new HashSet<String>();
      for (Role role : roles) {
        roleIds.add(role.getId());
      }
    }
    
    //get all the permissions for this user in these roles
    Set<PermissionEntry> permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        null, GrouperUtil.toSet(permissionName.getId()), roleIds, GrouperUtil.toSet(action), true, GrouperUtil.toSet(member.getUuid()));
    
    //we have the permissions, was anything returned?
    return permissionEntriesSet.size() > 0;
    
    //TODO filter out the disallows
    
    
  }

  /**
   * 
   * @param subject
   * @param role
   * @param permissionName
   * @param action
   * @return true if the subject has this permission in the specified role
   */
  public static boolean hasPermission(Subject subject, Role role, AttributeDefName permissionName, String action) {
    return hasPermission(subject, GrouperUtil.toSet(role), permissionName, action);
  }

}

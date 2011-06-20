/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.Collection;
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
   * find a list of permissions
   * @param subject
   * @param roles
   * @param permissionName
   * @param action
   * @param permissionProcessor maybe process the results
   * @return the set of permissions never null
   */
  public static Set<PermissionEntry> findPermissions(Subject subject, Set<Role> roles, AttributeDefName permissionName, String action, PermissionProcessor permissionProcessor) {
    
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
      return new HashSet<PermissionEntry>();
    }
    
    Set<String> roleIds = null;

    if (GrouperUtil.length(roles) > 0) {
      roleIds = new HashSet<String>();
      for (Role role : roles) {
        roleIds.add(role.getId());
      }
    }
    
    Set<PermissionEntry> permissions = findPermissions(null, 
        GrouperUtil.toSet(permissionName.getId()), roleIds, 
        GrouperUtil.toSet(action), true, GrouperUtil.toSet(member.getUuid()), permissionProcessor);

    return permissions;
  }

  /**
   * find a list of permissions
   * @param attributeDefIds 
   * @param attributeDefNameIds 
   * @param roleIds 
   * @param actions 
   * @param enabled 
   * @param memberIds 
   * @param permissionProcessor if picking the best one or something
   * @return the set of permissions never null
   */
  public static Set<PermissionEntry> findPermissions(
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> roleIds, 
      Collection<String> actions, 
      Boolean enabled,
      Collection<String> memberIds, PermissionProcessor permissionProcessor) {

    if (permissionProcessor != null && (enabled == null || !enabled)) {
      throw new RuntimeException("You cannot process the permissions " +
      		"(FILTER_REDUNDANT_PERMISSIONS || FILTER_REUNDANT_PERMISSIONS_AND_ROLES) " +
      		"without looking for enabled permissions only");
    }

    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        attributeDefIds, attributeDefNameIds, roleIds, actions, enabled, memberIds);

    //if size is one, there arent redundancies to process
    if (permissionProcessor != null) {
      permissionProcessor.processPermissions(permissionEntries);
    }
    return permissionEntries;
    
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
    Set<PermissionEntry> permissionEntriesSet = findPermissions(
        null, GrouperUtil.toSet(permissionName.getId()), roleIds, GrouperUtil.toSet(action), true, GrouperUtil.toSet(member.getUuid()), PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES);
    
    //we have the permissions, was anything returned?
    return permissionEntriesSet.size() == 0 ? false : !permissionEntriesSet.iterator().next().isDisallowed();
    
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

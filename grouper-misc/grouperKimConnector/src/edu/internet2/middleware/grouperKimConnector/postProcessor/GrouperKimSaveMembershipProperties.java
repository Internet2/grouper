/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.postProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * <pre>
 * properties about a post processor which adds a member to a group
 * 
 * kuali.edoclite.saveMembership.docTypeName.0 = sampleProvisionGroup.doctype
 * kuali.edoclite.saveMembership.groupRegex.0 = ^temp:[^:]+rovisionGroup$
 * kuali.edoclite.saveMembership.addMembershipToGroups.0 = temp:provisionGroup
 * kuali.edoclite.saveMembership.removeMembershipFromGroups.0 = temp:anotherProvisionGroup
 * kuali.edoclite.saveMembership.emailAdmins.0 = mchyzer@isc.upenn.edu
 * </pre>
 */
public class GrouperKimSaveMembershipProperties {

  /** doctype name that this applies to */
  private String docTypeName;
  
  /** regex of group allowed to assign to, extra layer of security, optional */
  private String groupRegex;
  
  /** groups (comma separated) id or name which the initiator will be assigned to when the document is final */
  private String addMembershipToGroups;

  /** groups (comma separated) id or name which the initiator will be unassigned from when the document is final */
  private String removeMembershipFromGroups;
  
  /** email addresses for sending a message to admins */
  private String emailAdmins;

  /** this will be prefixed to the entered group name so the whole stem doesnt have to be put on screen (also helps sandbox out the security) */
  private String enteredGroupNamePrefix;
  
  
  /** role to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions) */
  private String roleForPermissions;

  /** role to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with roleForPermissions) */
  private String edocliteFieldRoleForPermissions;
  
  /** allowed roles (e.g. from edoclite form) or empty if not validating */
  private String allowedRolesForPermissions;
  
  /** operation of assign|remove permissions (mutually exclusive with edocliteFieldOperationForPermissions) */
  private String operationForPermissions;

  /** operation to assign|remove permissions (read from edoclite) or empty if not doing permissions (mutually exclusive with operationForPermissions) */
  private String edocliteFieldOperationForPermissions;

  /** allowed operations (e.g. from edoclite form) or empty if not validating */
  private String allowedOperationsForPermissions;

  /** actions to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions) */
  private String actionsForPermissions;

  /** 
   * actions to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with actionsForPermissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   */
  private String edocliteFieldPrefixActionsForPermissions;

  /**
   * allowed actions (e.g. from edoclite form) or empty if not validating
   */
  private String allowedActionsForPermissions;

  /** permissions to assign or null if not doing permissions (mutually exclusive with edocliteFieldPrefixForPermissions) */
  private String permissions;

  /** 
   * permissions to assign (read from edoclite) or empty if not doing permissions (mutually exclusive with permissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   */
  private String edocliteFieldPrefixForPermissions;

  /**
   * allowed permissions (e.g. from edoclite form) or empty if not validating
   */
  private String allowedPermissions;

  /**
   * regex of permissions allowed to assign, extra layer of security, optional
   */
  private String permissionsRegex;

  /**
   * this will be prefixed to the entered permission name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   */
  private String enteredPermissionNamePrefix;

  /** names of attribute defs which are affected by a permissions replace */
  private String attributeDefNamesToReplace;

  /** actions which are affected by a permissions replace */
  private String actionsToReplace;
  
  /** 
   * this will be prefixed to the entered role name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   */
  private String enteredRoleNamePrefix;
  
  
  /**
   * this will be prefixed to the entered role name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   * @return the enteredRoleNamePrefix
   */
  public String getEnteredRoleNamePrefix() {
    return this.enteredRoleNamePrefix;
  }

  
  /**
   * this will be prefixed to the entered role name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   * @param enteredRoleNamePrefix1 the enteredRoleNamePrefix to set
   */
  public void setEnteredRoleNamePrefix(String enteredRoleNamePrefix1) {
    this.enteredRoleNamePrefix = enteredRoleNamePrefix1;
  }

  /**
   * names of attribute defs which are affected by a permissions replace
   * @return the attributeDefNamesToReplace
   */
  public String getAttributeDefNamesToReplace() {
    return this.attributeDefNamesToReplace;
  }
  
  /**
   * names of attribute defs which are affected by a permissions replace
   * @param attributeDefNamesToReplace1 the attributeDefNamesToReplace to set
   */
  public void setAttributeDefNamesToReplace(String attributeDefNamesToReplace1) {
    this.attributeDefNamesToReplace = attributeDefNamesToReplace1;
  }
  
  /**
   * actions which are affected by a permissions replace
   * @return the actionsToReplace
   */
  public String getActionsToReplace() {
    return this.actionsToReplace;
  }
  
  /**
   * actions which are affected by a permissions replace
   * @param actionsToReplace1 the actionsToReplace to set
   */
  public void setActionsToReplace(String actionsToReplace1) {
    this.actionsToReplace = actionsToReplace1;
  }

  /**
   * role to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
   * @return the roleForPermissions
   */
  public String getRoleForPermissions() {
    return this.roleForPermissions;
  }

  
  /**
   * role to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
   * @param roleForPermissions1 the roleForPermissions to set
   */
  public void setRoleForPermissions(String roleForPermissions1) {
    this.roleForPermissions = roleForPermissions1;
  }

  
  /**
   * role to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with roleForPermissions)
   * @return the edocliteFieldRoleForPermissions
   */
  public String getEdocliteFieldRoleForPermissions() {
    return this.edocliteFieldRoleForPermissions;
  }

  
  /**
   * role to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with roleForPermissions)
   * @param edocliteFieldRoleForPermissions1 the edocliteFieldRoleForPermissions to set
   */
  public void setEdocliteFieldRoleForPermissions(String edocliteFieldRoleForPermissions1) {
    this.edocliteFieldRoleForPermissions = edocliteFieldRoleForPermissions1;
  }

  
  /**
   * allowed roles (e.g. from edoclite form) or empty if not validating
   * @return the allowedRolesForPermissions
   */
  public String getAllowedRolesForPermissions() {
    return this.allowedRolesForPermissions;
  }

  
  /**
   * allowed roles (e.g. from edoclite form) or empty if not validating
   * @param allowedRolesForPermissions1 the allowedRolesForPermissions to set
   */
  public void setAllowedRolesForPermissions(String allowedRolesForPermissions1) {
    this.allowedRolesForPermissions = allowedRolesForPermissions1;
  }

  
  /**
   * operation of assign|remove permissions (mutually exclusive with edocliteFieldOperationForPermissions)
   * @return the operationForPermissions
   */
  public String getOperationForPermissions() {
    return this.operationForPermissions;
  }

  
  /**
   * operation of assign|remove permissions (mutually exclusive with edocliteFieldOperationForPermissions)
   * @param operationForPermissions1 the operationForPermissions to set
   */
  public void setOperationForPermissions(String operationForPermissions1) {
    this.operationForPermissions = operationForPermissions1;
  }

  
  /**
   * operation to assign|remove permissions (read from edoclite) or empty if not doing permissions (mutually exclusive with operationForPermissions)
   * @return the edocliteFieldOperationForPermissions
   */
  public String getEdocliteFieldOperationForPermissions() {
    return this.edocliteFieldOperationForPermissions;
  }

  
  /**
   * operation to assign|remove permissions (read from edoclite) or empty if not doing permissions (mutually exclusive with operationForPermissions)
   * @param edocliteFieldOperationForPermissions1 the edocliteFieldOperationForPermissions to set
   */
  public void setEdocliteFieldOperationForPermissions(
      String edocliteFieldOperationForPermissions1) {
    this.edocliteFieldOperationForPermissions = edocliteFieldOperationForPermissions1;
  }

  
  /**
   * allowed operations (e.g. from edoclite form) or empty if not validating
   * @return the allowedOperationsForPermissions
   */
  public String getAllowedOperationsForPermissions() {
    return this.allowedOperationsForPermissions;
  }

  
  /**
   * allowed operations (e.g. from edoclite form) or empty if not validating
   * @param allowedOperationsForPermissions1 the allowedOperationsForPermissions to set
   */
  public void setAllowedOperationsForPermissions(String allowedOperationsForPermissions1) {
    this.allowedOperationsForPermissions = allowedOperationsForPermissions1;
  }

  
  /**
   * actions to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
   * @return the actionsForPermissions
   */
  public String getActionsForPermissions() {
    return this.actionsForPermissions;
  }

  
  /**
   * actions to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
   * @param actionsForPermissions1 the actionsForPermissions to set
   */
  public void setActionsForPermissions(String actionsForPermissions1) {
    this.actionsForPermissions = actionsForPermissions1;
  }

  
  /**
   * actions to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with actionsForPermissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   * @return the edocliteFieldPrefixActionsForPermissions
   */
  public String getEdocliteFieldPrefixActionsForPermissions() {
    return this.edocliteFieldPrefixActionsForPermissions;
  }

  
  /**
   * actions to assign permissions to (read from edoclite) or empty if not doing permissions (mutually exclusive with actionsForPermissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   * @param edocliteFieldPrefixActionsForPermissions1 the edocliteFieldPrefixActionsForPermissions to set
   */
  public void setEdocliteFieldPrefixActionsForPermissions(
      String edocliteFieldPrefixActionsForPermissions1) {
    this.edocliteFieldPrefixActionsForPermissions = edocliteFieldPrefixActionsForPermissions1;
  }

  
  /**
   * allowed actions (e.g. from edoclite form) or empty if not validating
   * @return the allowedActionsForPermissions
   */
  public String getAllowedActionsForPermissions() {
    return this.allowedActionsForPermissions;
  }

  
  /**
   * allowed actions (e.g. from edoclite form) or empty if not validating
   * @param allowedActionsForPermissions1 the allowedActionsForPermissions to set
   */
  public void setAllowedActionsForPermissions(String allowedActionsForPermissions1) {
    this.allowedActionsForPermissions = allowedActionsForPermissions1;
  }

  
  /**
   * permissions to assign or null if not doing permissions (mutually exclusive with edocliteFieldPrefixForPermissions)
   * @return the permissions
   */
  public String getPermissions() {
    return this.permissions;
  }

  
  /**
   * permissions to assign or null if not doing permissions (mutually exclusive with edocliteFieldPrefixForPermissions)
   * @param permissions1 the permissions to set
   */
  public void setPermissions(String permissions1) {
    this.permissions = permissions1;
  }

  
  /**
   * permissions to assign (read from edoclite) or empty if not doing permissions (mutually exclusive with permissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   * @return the edocliteFieldPrefixForPermissions
   */
  public String getEdocliteFieldPrefixForPermissions() {
    return this.edocliteFieldPrefixForPermissions;
  }

  
  /**
   * permissions to assign (read from edoclite) or empty if not doing permissions (mutually exclusive with permissions)
   * this is the prefix, appending 0,1,2 etc on the end.  so the fields would be someEdocliteFieldName0, someEdocliteFieldName1, etc
   * @param edocliteFieldPrefixForPermissions1 the edocliteFieldPrefixForPermissions to set
   */
  public void setEdocliteFieldPrefixForPermissions(String edocliteFieldPrefixForPermissions1) {
    this.edocliteFieldPrefixForPermissions = edocliteFieldPrefixForPermissions1;
  }

  
  /**
   * allowed permissions (e.g. from edoclite form) or empty if not validating
   * @return the allowedPermissions
   */
  public String getAllowedPermissions() {
    return this.allowedPermissions;
  }

  
  /**
   * allowed permissions (e.g. from edoclite form) or empty if not validating
   * @param allowedPermissions1 the allowedPermissions to set
   */
  public void setAllowedPermissions(String allowedPermissions1) {
    this.allowedPermissions = allowedPermissions1;
  }

  
  /**
   * regex of permissions allowed to assign, extra layer of security, optional
   * @return the permissionsRegex
   */
  public String getPermissionsRegex() {
    return this.permissionsRegex;
  }

  
  /**
   * regex of permissions allowed to assign, extra layer of security, optional
   * @param permissionsRegex1 the permissionsRegex to set
   */
  public void setPermissionsRegex(String permissionsRegex1) {
    this.permissionsRegex = permissionsRegex1;
  }

  
  /**
   * this will be prefixed to the entered permission name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   * @return the enteredPermissionNamePrefix
   */
  public String getEnteredPermissionNamePrefix() {
    return this.enteredPermissionNamePrefix;
  }

  
  /**
   * this will be prefixed to the entered permission name so the whole stem doesnt 
   * have to be put on screen (also helps sandbox out the security)
   * @param enteredPermissionNamePrefix1 the enteredPermissionNamePrefix to set
   */
  public void setEnteredPermissionNamePrefix(String enteredPermissionNamePrefix1) {
    this.enteredPermissionNamePrefix = enteredPermissionNamePrefix1;
  }

  /**
   * this will be prefixed to the entered group name so the whole stem doesnt have to be put on screen (also helps sandbox out the security)
   * @return the enteredGroupNamePrefix
   */
  public String getEnteredGroupNamePrefix() {
    return this.enteredGroupNamePrefix;
  }

  /**
   * this will be prefixed to the entered group name so the whole 
   * stem doesnt have to be put on screen (also helps sandbox out the security)
   * @param enteredGroupNamePrefix1 the enteredGroupNamePrefix to set
   */
  public void setEnteredGroupNamePrefix(String enteredGroupNamePrefix1) {
    this.enteredGroupNamePrefix = enteredGroupNamePrefix1;
  }




  /** 
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   */
  private String edocliteFieldPrefix;
  
  /** 
   * if there is a disabled date, that is here.  Note, if doing permissions, the permissions will
   * disable here too
   */
  private String edocliteFieldGroupDisabledDate;
  
  /** 
   * if there is a enabled date, that is here.  Note, if doing permissions, the permissions will
   * enable here too
   */
  private String edocliteFieldGroupEnabledDate;
  
  /** 
   * if there is a disabled date, that is here.  Note, this is just for the permissions, not for the
   * role
   */
  private String edocliteFieldPermissionDisabledDate;
  
  /** 
   * if there is a enabled date, that is here.  Note, this is just for permissions, and not for the 
   * role
   */
  private String edocliteFieldPermissionEnabledDate;
  
  /**
   * blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   */
  private String permissionsDelegatable;
  
  /**
   * edoclite field for blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   */
  private String edocliteFieldPermissionsDelegatable;
  
  /**
   * blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   * @return delegatable
   */
  public String getPermissionsDelegatable() {
    return this.permissionsDelegatable;
  }


  /**
   * blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   * @param permissionsDelegatable1
   */
  public void setPermissionsDelegatable(String permissionsDelegatable1) {
    this.permissionsDelegatable = permissionsDelegatable1;
  }


  /**
   * edoclite field for blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   * @return field name
   */
  public String getEdocliteFieldPermissionsDelegatable() {
    return this.edocliteFieldPermissionsDelegatable;
  }


  /**
   * edoclite field for blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
   * @param edocliteFieldPermissionsDelegatable1
   */
  public void setEdocliteFieldPermissionsDelegatable(
      String edocliteFieldPermissionsDelegatable1) {
    this.edocliteFieldPermissionsDelegatable = edocliteFieldPermissionsDelegatable1;
  }


  /**
   * if there is a disabled date, that is here.  Note, if doing permissions, the permissions will
   * disable here too
   * @return disabled date
   */
  public String getEdocliteFieldGroupDisabledDate() {
    return this.edocliteFieldGroupDisabledDate;
  }


  /**
   * if there is a disabled date, that is here.  Note, if doing permissions, the permissions will
   * disable here too
   * @param edocliteFieldGroupDisabledDate1
   */
  public void setEdocliteFieldGroupDisabledDate(String edocliteFieldGroupDisabledDate1) {
    this.edocliteFieldGroupDisabledDate = edocliteFieldGroupDisabledDate1;
  }


  /**
   * if there is a enabled date, that is here.  Note, if doing permissions, the permissions will
   * enable here too
   * @return enabled date
   */
  public String getEdocliteFieldGroupEnabledDate() {
    return this.edocliteFieldGroupEnabledDate;
  }


  /**
   * if there is a enabled date, that is here.  Note, if doing permissions, the permissions will
   * enable here too
   * @param edocliteFieldGroupEnabledDate1
   */
  public void setEdocliteFieldGroupEnabledDate(String edocliteFieldGroupEnabledDate1) {
    this.edocliteFieldGroupEnabledDate = edocliteFieldGroupEnabledDate1;
  }


  /**
   * if there is a disabled date, that is here.  Note, this is just for the permissions, not for the
   * role
   * @return disabled date
   */
  public String getEdocliteFieldPermissionDisabledDate() {
    return this.edocliteFieldPermissionDisabledDate;
  }


  /**
   * if there is a disabled date, that is here.  Note, this is just for the permissions, not for the
   * role
   * @param edocliteFieldMembershipDisabledDate1
   */
  public void setEdocliteFieldPermissionDisabledDate(
      String edocliteFieldMembershipDisabledDate1) {
    this.edocliteFieldPermissionDisabledDate = edocliteFieldMembershipDisabledDate1;
  }


  /**
   * if there is a enabled date, that is here.  Note, this is just for permissions, and not for the 
   * role
   * @return enabled date
   */
  public String getEdocliteFieldPermissionEnabledDate() {
    return this.edocliteFieldPermissionEnabledDate;
  }


  /**
   * if there is a enabled date, that is here.  Note, this is just for permissions, and not for the 
   * role
   * @param edocliteFieldMembershipEnabledDate1
   */
  public void setEdocliteFieldPermissionEnabledDate(
      String edocliteFieldMembershipEnabledDate1) {
    this.edocliteFieldPermissionEnabledDate = edocliteFieldMembershipEnabledDate1;
  }


  /**
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   * @return the edocliteFieldPrefix
   */
  public String getEdocliteFieldPrefix() {
    return this.edocliteFieldPrefix;
  }



  
  /**
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   * @param edocliteFieldPrefix1 the edocliteFieldPrefix to set
   */
  public void setEdocliteFieldPrefix(String edocliteFieldPrefix1) {
    this.edocliteFieldPrefix = edocliteFieldPrefix1;
  }



  /** set of strings of groups allowed to be used (if empty, then allow all) */
  private Set<String> allowedGroups = new HashSet<String>();
  
  /**
   * set of strings of groups allowed to be used (if empty, then allow all)
   * @return the groups
   */
  public Set<String> getAllowedGroups() {
    return this.allowedGroups;
  }



  /**
   * email addresses for sending a message to admins
   * @return the emailAdmins
   */
  public String getEmailAdmins() {
    return this.emailAdmins;
  }

  /**
   * if allowed to access this group by name
   * @param groupName
   * @return true if allowed to access group by name
   */
  public boolean allowedToAccessGroup(String groupName) {
    
    //see if fails the regex (if there is a regex)
    if (!GrouperClientUtils.isBlank(this.groupRegex)) {
      if (!groupName.matches(this.groupRegex)) {
        return false;
      }
    }
    
    //see if not in the list, if there is a list
    if (this.getAllowedGroups().size() > 0) {
      if (!this.getAllowedGroups().contains(groupName)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * email addresses for sending a message to admins
   * @param emailAdmins1 the emailAdmins to set
   */
  public void setEmailAdmins(String emailAdmins1) {
    this.emailAdmins = emailAdmins1;
  }


  /**
   * doctype name that this applies to
   * @return the docTypeName
   */
  public String getDocTypeName() {
    return this.docTypeName;
  }

  
  /**
   * doctype name that this applies to
   * @param docTypeName1 the docTypeName to set
   */
  public void setDocTypeName(String docTypeName1) {
    this.docTypeName = docTypeName1;
  }

  
  /**
   * regex of group allowed to assign to, extra layer of security, optional
   * @return the groupRegex
   */
  public String getGroupRegex() {
    return this.groupRegex;
  }

  
  /**
   * regex of group allowed to assign to, extra layer of security, optional
   * @param groupRegex1 the groupRegex to set
   */
  public void setGroupRegex(String groupRegex1) {
    this.groupRegex = groupRegex1;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be assigned to when the document is final
   * @return the addMembershipToGroups
   */
  public String getAddMembershipToGroups() {
    return this.addMembershipToGroups;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be assigned to when the document is final
   * @param addMembershipToGroups1 the addMembershipToGroups to set
   */
  public void setAddMembershipToGroups(String addMembershipToGroups1) {
    this.addMembershipToGroups = addMembershipToGroups1;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be unassigned from when the document is final
   * @return the removeMembershipFromGroups
   */
  public String getRemoveMembershipFromGroups() {
    return this.removeMembershipFromGroups;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be unassigned from when the document is final
   * @param removeMembershipFromGroups1 the removeMembershipFromGroups to set
   */
  public void setRemoveMembershipFromGroups(String removeMembershipFromGroups1) {
    this.removeMembershipFromGroups = removeMembershipFromGroups1;
  }

  

  /**
   * cache of grouper source configs
   */
  private static ExpirableCache<String, GrouperKimSaveMembershipProperties> grouperKimSaveMembershipPropertiesCache 
    = new ExpirableCache<String, GrouperKimSaveMembershipProperties>(5);

  /**
   * get the source properties for source name (current source name)
   * @param docTypeName
   * @return properties for source and app name
   */
  public static GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties(String docTypeName) {
    
  //###############################
  //# configure postprocessor actions on document types.  The string "sampleProvisioning" ties the configs
  //# together, change that label for multiple
  //
  //###### MISC
  //# email addresses (comma separated) that should get an admin email that this was done (or errors)
  //kuali.edoclite.saveMembership.sampleProvisioning.emailAdmins = mchyzer@isc.upenn.edu
  //
  //# doctype name that this applies to
  //kuali.edoclite.saveMembership.sampleProvisioning.docTypeName = sampleProvisioning.doctype
  //
  //###### GROUPS
  //# regex of group allowed to assign to, extra layer of security, optional
  //kuali.edoclite.saveMembership.sampleProvisioning.groupRegex = ^temp:[^:]+rovisionGroup$
  //
  //# list of allowed to assign to (comma separate), extra layer of security, optional, 
  //#generally mutually exclusive with the groupRegex
  //kuali.edoclite.saveMembership.sampleProvisioning.allowedGroups = a:b:c, d:e:f:G
  //
  //# edocliteFieldPrefix if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
  //#so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
  //#the value of the field is the group to add to
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPrefix = provisionGroup
  //
  //#this will be prefixed to the entered group name so the whole stem doesnt 
  //#have to be put on screen (also helps sandbox out the security)
  //kuali.edoclite.saveMembership.sampleProvisioning.enteredGroupNamePrefix = school:some:prefix:
  //
  //# groups (comma separated) id or name which the initiator will be assigned to when the document is final
  //kuali.edoclite.saveMembership.sampleProvisioning.addMembershipToGroups = temp:provisionGroup
  //
  //# groups (comma separated) id or name which the initiator will be unassigned from when the document is final
  //kuali.edoclite.saveMembership.sampleProvisioning.removeMembershipFromGroups = temp:anotherProvisionGroup
  //
  //# delete date: yyyy/mm/dd or dd-Mon-yyyy
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldGroupDisabledDate = someFieldName
  //
  //# enable date: yyyy/mm/dd or dd-Mon-yyyy
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldGroupEnabledDate = someFieldName
  //
  //###### PERMISSIONS ROLES
  //# role to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.roleForPermissions = some:role
  //
  //# role to assign permissions to (read from edoclite) or empty if not doing permissions 
  //# (mutually exclusive with roleForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldRoleForPermissions = someEdocliteFieldName
  //
  //# this will be prefixed to the entered role name so the whole stem doesnt 
  //# have to be put on screen (also helps sandbox out the security)
  //kuali.edoclite.saveMembership.sampleProvisionPermissions.enteredRoleNamePrefix = a:b:
  //
  //# allowed roles (e.g. from edoclite form) or empty if not validating
  //kuali.edoclite.saveMembership.sampleProvisioning.allowedRolesForPermissions = some:role1, some:role2
  //
  //###### PERMISSIONS OPERATIONS
  //# operation of assign_permission|remove_permission|replace_permissions permissions 
  //# (mutually exclusive with edocliteFieldOperationForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.operationForPermissions = assign_permission
  //
  //# operation to assign|remove permissions (read from edoclite) or empty if not doing 
  //# permissions (mutually exclusive with operationForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldOperationForPermissions = someEdocliteFieldName
  //
  //# allowed operations (e.g. from edoclite form) or empty if not validating
  //kuali.edoclite.saveMembership.sampleProvisioning.allowedOperationsForPermissions = assign_permission, remove_permission
  //
  //###### ACTIONS
  //# actions to assign permissions to or null if not doing permissions (mutually exclusive with edocliteFieldRoleForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.actionsForPermissions = read,write
  //
  //# actions to assign permissions to (read from edoclite) or empty if not doing permissions 
  //# (mutually exclusive with actionsForPermissions)
  //# this is the prefix, appending 0,1,2 etc on the end.  so the fields would be 
  //# someEdocliteFieldName0, someEdocliteFieldName1, etc
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPrefixActionsForPermissions = someEdocliteFieldName
  //
  //# allowed actions (e.g. from edoclite form) or empty if not validating
  //kuali.edoclite.saveMembership.sampleProvisioning.allowedActionsForPermissions = read, write
  //
  //###### PERMISSIONS
  //# permissions to assign or null if not doing permissions (mutually exclusive with edocliteFieldPrefixForPermissions)
  //kuali.edoclite.saveMembership.sampleProvisioning.permissions = a:b, b:c
  //
  //# permissions to assign (read from edoclite) or empty if not doing permissions (mutually exclusive with permissions)
  //# this is the prefix, appending 0,1,2 etc on the end.  so the fields would be 
  //# someEdocliteFieldName0, someEdocliteFieldName1, etc
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPrefixForPermissions = someEdocliteFieldName
  //
  //# allowed permissions (e.g. from edoclite form) or empty if not validating
  //kuali.edoclite.saveMembership.sampleProvisioning.allowedPermissions = a:b, b:c
  //
  //# regex of permissions allowed to assign, extra layer of security, optional
  //kuali.edoclite.saveMembership.sampleProvisioning.permissionsRegex = ^temp:[^:]+rovisionGroup$
  //
  //#this will be prefixed to the entered permission name so the whole stem doesnt 
  //#have to be put on screen (also helps sandbox out the security)
  //kuali.edoclite.saveMembership.sampleProvisioning.enteredPermissionNamePrefix = school:some:prefix:
  //
  //# delete date: yyyy/mm/dd or dd-Mon-yyyy
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPermissionDisabledDate = someFieldName
  //
  //# enable date: yyyy/mm/dd or dd-Mon-yyyy
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPermissionEnabledDate = someFieldName
  //
  //# field name which has blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
  //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPermissionsDelegatable = someFieldName
  //
  //# blank or FALSE, TRUE, or GRANT for if the user can delegate the permissions to others
  //kuali.edoclite.saveMembership.sampleProvisioning.permissionsDelegatable = FALSE|TRUE|GRANT
  //
  //###### REPLACE PERMISSIONS SETTINGS
  //# if replacing permissions, then these are the names of attribute defs that are affected (or blank for all)
  //kuali.edoclite.saveMembership.sampleProvisioning.attributeDefNamesToReplace = a:b, b:c
  //
  //# if replacing permissions, then these are the actions that are affected (or blank for all)
  //kuali.edoclite.saveMembership.sampleProvisioning.actionsToReplace = read, write
  //##################################################
    
    
    GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties = 
      grouperKimSaveMembershipPropertiesCache.get(docTypeName);
    if (grouperKimSaveMembershipProperties == null) {
      grouperKimSaveMembershipProperties = new GrouperKimSaveMembershipProperties();
      grouperKimSaveMembershipProperties.setDocTypeName(docTypeName);
      
      //loop through and find this config
      
      Pattern pattern = Pattern.compile("^kuali\\.edoclite\\.saveMembership\\.(.+)\\.docTypeName$");
      
      Properties properties = GrouperClientUtils.grouperClientProperties();
      for (Object keyObject : properties.keySet()) {
        String key = (String)keyObject;
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
          String configName = matcher.group(1);
          
          String currentDocTypeName = GrouperClientUtils.propertiesValue(key, true);
  
          
          if (GrouperClientUtils.equals(docTypeName, currentDocTypeName)) {
            
            //we found it
            {
              String groupRegex = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".groupRegex", false);
              grouperKimSaveMembershipProperties.setGroupRegex(groupRegex);
            }
            
            {
              String groupsString = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedGroups", false);
              if (!GrouperClientUtils.isBlank(groupsString)) {
                List<String> groupsList = GrouperClientUtils.splitTrimToList(groupsString, ",");
                grouperKimSaveMembershipProperties.getAllowedGroups().addAll(groupsList);
              }
            }
            
            {
              String edocliteFieldPrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPrefix", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPrefix(edocliteFieldPrefix);
            }
            {
              String enteredGroupNamePrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".enteredGroupNamePrefix", false);
              grouperKimSaveMembershipProperties.setEnteredGroupNamePrefix(enteredGroupNamePrefix);
            }
                        
            {
              String addMembershipToGroups = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".addMembershipToGroups", false);
              grouperKimSaveMembershipProperties.setAddMembershipToGroups(addMembershipToGroups);
            }
            
            {
              String removeMembershipFromGroups = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".removeMembershipFromGroups", false);
              grouperKimSaveMembershipProperties.setRemoveMembershipFromGroups(removeMembershipFromGroups);
            }
            
            {
              String edocliteFieldGroupDisabledDate = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldGroupDisabledDate", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldGroupDisabledDate(edocliteFieldGroupDisabledDate);
            }

            {
              String edocliteFieldGroupEnabledDate = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldGroupEnabledDate", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldGroupEnabledDate(edocliteFieldGroupEnabledDate);
            }
            
            {
              String emailAdmins = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".emailAdmins", false);
              grouperKimSaveMembershipProperties.setEmailAdmins(emailAdmins);
            }
            
            {
              String roleForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".roleForPermissions", false);
              grouperKimSaveMembershipProperties.setRoleForPermissions(roleForPermissions);
            }
            
            {
              String edocliteFieldRoleForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldRoleForPermissions", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldRoleForPermissions(edocliteFieldRoleForPermissions);
            }

            {
              String enteredRoleNamePrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".enteredRoleNamePrefix", false);
              grouperKimSaveMembershipProperties.setEnteredRoleNamePrefix(enteredRoleNamePrefix);
            }
            
            {
              String allowedRolesForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedRolesForPermissions", false);
              grouperKimSaveMembershipProperties.setAllowedRolesForPermissions(allowedRolesForPermissions);
            }

            {
              String operationForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".operationForPermissions", false);
              grouperKimSaveMembershipProperties.setOperationForPermissions(operationForPermissions);
            }

            {
              String edocliteFieldOperationForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldOperationForPermissions", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldOperationForPermissions(edocliteFieldOperationForPermissions);
            }

            {
              String allowedOperationsForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedOperationsForPermissions", false);
              grouperKimSaveMembershipProperties.setAllowedOperationsForPermissions(allowedOperationsForPermissions);
            }

            {
              String actionsForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".actionsForPermissions", false);
              grouperKimSaveMembershipProperties.setActionsForPermissions(actionsForPermissions);
            }

            {
              String edocliteFieldPrefixActionsForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPrefixActionsForPermissions", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPrefixActionsForPermissions(edocliteFieldPrefixActionsForPermissions);
            }

            {
              String allowedActionsForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedActionsForPermissions", false);
              grouperKimSaveMembershipProperties.setAllowedActionsForPermissions(allowedActionsForPermissions);
            }

            {
              String permissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".permissions", false);
              grouperKimSaveMembershipProperties.setPermissions(permissions);
            }

            {
              String edocliteFieldPrefixForPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPrefixForPermissions", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPrefixForPermissions(edocliteFieldPrefixForPermissions);
            }
            
            {
              String allowedPermissions = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedPermissions", false);
              grouperKimSaveMembershipProperties.setAllowedPermissions(allowedPermissions);
            }
            
            {
              String permissionsRegex = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".permissionsRegex", false);
              grouperKimSaveMembershipProperties.setPermissionsRegex(permissionsRegex);
            }

            {
              String enteredPermissionNamePrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".enteredPermissionNamePrefix", false);
              grouperKimSaveMembershipProperties.setEnteredPermissionNamePrefix(enteredPermissionNamePrefix);
            }
            
            {
              String edocliteFieldPermissionDisabledDate = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPermissionDisabledDate", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPermissionDisabledDate(edocliteFieldPermissionDisabledDate);
            }

            {
              String edocliteFieldPermissionEnabledDate = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPermissionEnabledDate", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPermissionEnabledDate(edocliteFieldPermissionEnabledDate);
            }

            {
              String edocliteFieldPermissionsDelegatable = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPermissionsDelegatable", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPermissionsDelegatable(edocliteFieldPermissionsDelegatable);
            }

            {
              String permissionsDelegatable = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".permissionsDelegatable", false);
              grouperKimSaveMembershipProperties.setPermissionsDelegatable(permissionsDelegatable);
            }
            
            {
              String attributeDefNamesToReplace = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".attributeDefNamesToReplace", false);
              grouperKimSaveMembershipProperties.setAttributeDefNamesToReplace(attributeDefNamesToReplace);
            }
            
            {
              String actionsToReplace = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".actionsToReplace", false);
              grouperKimSaveMembershipProperties.setActionsToReplace(actionsToReplace);
            }
            
            break;
          }
        }        
      }
      grouperKimSaveMembershipPropertiesCache.put(docTypeName, grouperKimSaveMembershipProperties);
    }
    return grouperKimSaveMembershipProperties;
  }
  
}

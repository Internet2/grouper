/**
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
 */
package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public interface PermissionEntry extends Comparable<PermissionEntry> {

  /**
   * type of permission, either assigned to role, or assigned to role and user combined
   */
  public static enum PermissionType {

    /** permission assigned to role */
    role {

      @Override
      public AttributeAssignType convertToAttributeAssignType() {
        return AttributeAssignType.group;
      }
    },
    
    /** permission assigned to role and user combined */
    role_subject {

      @Override
      public AttributeAssignType convertToAttributeAssignType() {
        return AttributeAssignType.any_mem;
      }
    };
    
    /**
     * do a case-insensitive matching
     * 
     * @param theString
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static PermissionType valueOfIgnoreCase(String theString, boolean exceptionOnNull) {
      return GrouperUtil.enumValueOfIgnoreCase(PermissionType.class, 
          theString, exceptionOnNull);

    }

    /**
     * name for javabean
     * @return the attribute assign type
     */
    public String getName() {
      return this.name();
    }
    

    /**
     * convert to attribute assign type
     * @return type
     */
    public abstract AttributeAssignType convertToAttributeAssignType();
    
  }
  
  /**
   * this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @return true if allowed overall
   */
  public boolean isAllowedOverall();
  
  /**
   * this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @param allowedOverall1
   */
  public void setAllowedOverall(boolean allowedOverall1);
  
  /**
   * notes on the assignment of privilege
   * @return notes
   */
  public String getAssignmentNotes();
  
  /**
   * notes on the assignment of privilege
   * @param assignmentNotes1
   */
  public void setAssignmentNotes(String assignmentNotes1);
  
  /**
   * when the underlying membership was enabled
   * @return time
   */
  public Long getImmediateMshipEnabledTimeDb();
  
  /**
   * when the underlying membership was enabled
   * @return time
   */
  public Timestamp getImmediateMshipEnabledTime();
  
  /**
   * when the underlying membership was enabled
   * @param immediateMshipEnabledTimeDb1
   */
  public void setImmediateMshipEnabledTimeDb(Long immediateMshipEnabledTimeDb1);

  /**
   * when the underlying membership was enabled
   * @param immediateMshipEnabledTimeDb1
   */
  public void setImmediateMshipEnabledTime(Timestamp immediateMshipEnabledTimeDb1);

  /**
   * when the underlying membership will be disabled
   * @return time
   */
  public Long getImmediateMshipDisabledTimeDb();

  /**
   * when the underlying membership was enabled
   * @param immediateMshipDisabledTimeDb1
   */
  public void setImmediateMshipDisabledTimeDb(Long immediateMshipDisabledTimeDb1);

  /**
   * when the underlying membership will be disabled
   * @return time
   */
  public Timestamp getImmediateMshipDisabledTime();

  /**
   * when the underlying membership was enabled
   * @param immediateMshipDisabledTimeDb1
   */
  public void setImmediateMshipDisabledTimeDb(Timestamp immediateMshipDisabledTimeDb1);

  /**
   * action on the permission (e.g. read, write, assign (default), etc
   * @return action
   */
  public String getActionId();

  /**
   * action on the permission (e.g. read, write, assign (default), etc
   * @param actionId1
   */
  public void setActionId(String actionId1);
  
  /**
   * cache the weighting of this assignment
   * @return the permission heuristic
   */
  public PermissionHeuristics getPermissionHeuristics();

  /**
   * cache the weighting of this assignment
   * @param permissionHeuristics1
   */
  public void setPermissionHeuristics(PermissionHeuristics permissionHeuristics1);

  /**
   * depth of memberships, 0 means immediate
   * @return depth
   */
  public int getMembershipDepth();


  /**
   * depth of memberships, 0 means immediate
   * @param membershipDepth1
   */
  public void setMembershipDepth(int membershipDepth1);


  /**
   * depth of role hierarchy, 0 means immediate, -1 means no role set involved
   * @return depth
   */
  public int getRoleSetDepth();


  /**
   * depth of role hierarchy, 0 means immediate, -1 means no role set involved
   * @param roleSetDepth1
   */
  public void setRoleSetDepth(int roleSetDepth1);


  /**
   * depth of attributeDefName set hierarchy, 0 means immediate
   * @return depth
   */
  public int getAttributeDefNameSetDepth();


  /**
   * depth of attributeDefName set hierarchy, 0 means immediate
   * @param attributeDefNameSetDepth1
   */
  public void setAttributeDefNameSetDepth(int attributeDefNameSetDepth1);


  /**
   * depth of action hierarchy, 0 means immediate
   * @return depth
   */
  public int getAttributeAssignActionSetDepth();


  /**
   * depth of action hierarchy, 0 means immediate
   * @param attributeAssignActionSetDepth1
   */
  public void setAttributeAssignActionSetDepth(int attributeAssignActionSetDepth1);


  /**
   * role which has the permission or which the subject must be in to have the permission
   * @return the roleName
   */
  public String getRoleName();

  
  /**
   * role which has the permission or which the subject must be in to have the permission
   * @param roleName1 the roleName to set
   */
  public void setRoleName(String roleName1);

  
  /**
   * source id of the subject which has the permissions
   * @return the subjectSourceId
   */
  public String getSubjectSourceId();

  
  /**
   * source id of the subject which has the permissions
   * @param subjectSourceId1 the subjectSourceId to set
   */
  public void setSubjectSourceId(String subjectSourceId1);

  
  /**
   * subject id of the subject which has the permissions
   * @return the subjectId
   */
  public String getSubjectId();

  
  /**
   * subject id of the subject which has the permissions
   * @param subjectId1 the subjectId to set
   */
  public void setSubjectId(String subjectId1);
  
  /**
   * action on the permission (e.g. read, write, assign (default), etc
   * @return the action
   */
  public String getAction();
  
  /**
   * action on the permission (e.g. read, write, assign (default), etc
   * @param action1 the action to set
   */
  public void setAction(String action1);
  
  /**
   * name of the attribute def name which is the permission assigned to the role or subject
   * @return the attributeDefNameName
   */
  public String getAttributeDefNameName();

  
  /**
   * name of the attribute def name which is the permission assigned to the role or subject
   * @param attributeDefNameName1 the attributeDefNameName to set
   */
  public void setAttributeDefNameName(String attributeDefNameName1);

  
  /**
   * display name of the attribute def name which is the permission assigned to the role or subject
   * @return the attributeDefNameDispName
   */
  public String getAttributeDefNameDispName();

  
  /**
   * display name of the attribute def name which is the permission assigned to the role or subject
   * @param attributeDefNameDispName1 the attributeDefNameDispName to set
   */
  public void setAttributeDefNameDispName(String attributeDefNameDispName1);

  
  /**
   * display name of the role which the subject is in to have the permission
   * @return the roleDisplayName
   */
  public String getRoleDisplayName();

  
  /**
   * display name of the role which the subject is in to have the permission
   * @param roleDisplayName1 the roleDisplayName to set
   */
  public void setRoleDisplayName(String roleDisplayName1);

  
  /**
   * id of the role which the subject is in to get the permission
   * @return the roleId
   */
  public String getRoleId();
  
  /**
   * owner role
   * @return the ownerRole
   */
  public Role getRole();

  /**
   * get attribute def name
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName();
  
  /**
   * get attribute assign
   * @return attributeAssign
   */
  public AttributeAssign getAttributeAssign();
  
  /**
   * id of the role which the subject is in to get the permission
   * @param roleId1 the roleId to set
   */
  public void setRoleId(String roleId1);

  
  /**
   * id of the attributeDef
   * @return the attributeDefId
   */
  public String getAttributeDefId();

  /**
   * 
   * @return attributeDef
   */
  public AttributeDef getAttributeDef();
  
  /**
   * id of the attributeDef
   * @param attributeDefId1 the attributeDefId to set
   */
  public void setAttributeDefId(String attributeDefId1);

  
  /**
   * id of the member that has the permission
   * @return the memberId
   */
  public String getMemberId();

  /**
   * get the member
   * @return the member
   */
  public Member getMember();

  
  /**
   * id of the member that has the permission
   * @param memberId1 the memberId to set
   */
  public void setMemberId(String memberId1);
  
  /**
   * id of the attribute def name which is the permission
   * @return the attributeDefNameId
   */
  public String getAttributeDefNameId();

  
  /**
   * id of the attribute def name which is the permission
   * @param attributeDefNameId1 the attributeDefNameId to set
   */
  public void setAttributeDefNameId(String attributeDefNameId1);
  
  /**
   * get the enum for delegatable, do not return null
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignDelegatable getAttributeAssignDelegatable();


  /**
   * internal method for hibernate to persist this enum
   * @return the string value (enum name)
   */
  public String getAttributeAssignDelegatableDb();


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Timestamp getDisabledTime();


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Long getDisabledTimeDb();


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public String getEnabledDb();
  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Timestamp getEnabledTime();

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Long getEnabledTimeDb();


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public boolean isEnabled();

  /**
   * @param attributeAssignDelegatable1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignDelegatable(AttributeAssignDelegatable attributeAssignDelegatable1);

  /**
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignDelegatableDb
   */
  public void setAttributeAssignDelegatableDb(String theAttributeAssignDelegatableDb);


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTime(Timestamp disabledTimeDb1);

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTimeDb(Long disabledTimeDb1);

  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabled(boolean enabled1);

  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabledDb(String enabled1);


  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTime(Timestamp enabledTimeDb1);

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTimeDb(Long enabledTimeDb1);
  
  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @return type of permission
   */
  public String getPermissionTypeDb();

  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @param permissionTypeDb1
   */
  public void setPermissionTypeDb(String permissionTypeDb1);
  
  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @return permission type
   */
  public PermissionType getPermissionType();
  

  /**
   * id of the membership row
   * @return id of the membership row
   */
  public String getMembershipId();

  /**
   * 
   * @param thePermissionType
   * @return if immediate, considering which permission type we are looking at
   */
  public boolean isImmediate(PermissionType thePermissionType);
  
  /**
   * see if the membership is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediateMembership();
  
  /**
   * see if the permission is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediatePermission();
  
  /**
   * id of the membership row
   * @param membershipId1
   */
  public void setMembershipId(String membershipId1);

  /**
   * id of the attribute assign row, either to the role, or to the role member pair
   * @return id
   */
  public String getAttributeAssignId();

  /**
   * id of the attribute assign row, either to the role, or to the role member pair
   * @param attributeAssignId1
   */
  public void setAttributeAssignId(String attributeAssignId1);
  

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @return the allowed
   */
  public String getDisallowedDb();
  
  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @return if allowed
   */
  public boolean isDisallowed();

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @param disallowed1 the allowed to set
   */
  public void setDisallowed(boolean disallowed1);

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @param disallowed1 the allowed to set
   */
  public void setDisallowedDb(String disallowed1);
  
  /**
   * The end time for this permission entry.  This is for point in time.
   * @return end time
   */
  public Timestamp getEndTime();
  
  /**
   * The start time for this permission entry.  This is for point in time.
   * @return start time
   */
  public Timestamp getStartTime();
  
  /**
   * Whether this permission entry currently exists.  If the object is not from point in time, this is always true.
   * @return true if active
   */
  public boolean isActive();
}

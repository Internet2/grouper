package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public abstract class PermissionEntryBase extends GrouperAPI implements PermissionEntry {

  /** cache the weighting of this assignment */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private PermissionHeuristics permissionHeuristics;

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   */
  private boolean disallowed = false;
  
  /**
   * action on the permission (e.g. read, write, assign (default), etc
   */
  private String actionId;
  
  /** id of the role which the subject is in to get the permission */
  private String roleId;
  
  /** id of the member that has the permission */
  private String memberId;
  
  /** id of the attribute def name which is the permission */
  private String attributeDefNameId;
  
  /** id of the membership row */
  private String membershipId;
  
  /** id of the attribute assign row, either to the role, or to the role member pair */
  private String attributeAssignId;
  
  /** source id of the subject which has the permissions */
  private String subjectSourceId;

  /** subject id of the subject which has the permissions */
  private String subjectId;

  /** action on the permission (e.g. read, write, assign (default), etc */
  private String action;

  /** role which has the permission or which the subject must be in to have the permission */
  private String roleName;
  
  /** name of the attribute def name which is the permission assigned to the role or subject */
  private String attributeDefNameName;
  
  /** id of the attributeDef */
  private String attributeDefId;

  /** depth of memberships, 0 means immediate */
  private int membershipDepth = -2;
  
  /** depth of role hierarchy, 0 means immediate, -1 means no role set involved */
  private int roleSetDepth = -2;
  
  /** depth of attributeDefName set hierarchy, 0 means immediate */
  private int attributeDefNameSetDepth = -2;

  /** depth of action hierarchy, 0 means immediate */
  private int attributeAssignActionSetDepth = -2;
  
  /** type of permission, either assigned to role, or assigned to role and user combined: role_subject */
  private PermissionType permissionType;
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getPermissionHeuristics()
   */
  public PermissionHeuristics getPermissionHeuristics() {
    return this.permissionHeuristics;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setPermissionHeuristics(edu.internet2.middleware.grouper.permissions.PermissionHeuristics)
   */
  public void setPermissionHeuristics(PermissionHeuristics permissionHeuristics1) {
    this.permissionHeuristics = permissionHeuristics1;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getDisallowedDb()
   */
  public String getDisallowedDb() {
    return this.disallowed ? "T" : "F";
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isDisallowed()
   */
  public boolean isDisallowed() {
    return this.disallowed;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setDisallowed(boolean)
   */
  public void setDisallowed(boolean disallowed1) {
    this.disallowed = disallowed1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setDisallowedDb(java.lang.String)
   */
  public void setDisallowedDb(String disallowed1) {
    this.disallowed = GrouperUtil.booleanValue(disallowed1, false);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAssignmentNotes()
   */
  public String getAssignmentNotes() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeAssign()
   */
  public AttributeAssign getAttributeAssign() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeAssignDelegatable()
   */
  public AttributeAssignDelegatable getAttributeAssignDelegatable() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeAssignDelegatableDb()
   */
  public String getAttributeAssignDelegatableDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDef()
   */
  public AttributeDef getAttributeDef() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefName()
   */
  public AttributeDefName getAttributeDefName() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefNameDispName()
   */
  public String getAttributeDefNameDispName() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getDisabledTime()
   */
  public Timestamp getDisabledTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getDisabledTimeDb()
   */
  public Long getDisabledTimeDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getEnabledDb()
   */
  public String getEnabledDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getEnabledTime()
   */
  public Timestamp getEnabledTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getEnabledTimeDb()
   */
  public Long getEnabledTimeDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getImmediateMshipDisabledTime()
   */
  public Timestamp getImmediateMshipDisabledTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getImmediateMshipDisabledTimeDb()
   */
  public Long getImmediateMshipDisabledTimeDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getImmediateMshipEnabledTime()
   */
  public Timestamp getImmediateMshipEnabledTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getImmediateMshipEnabledTimeDb()
   */
  public Long getImmediateMshipEnabledTimeDb() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getMember()
   */
  public Member getMember() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getRole()
   */
  public Role getRole() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getRoleDisplayName()
   */
  public String getRoleDisplayName() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isAllowedOverall()
   */
  public boolean isAllowedOverall() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isEnabled()
   */
  public boolean isEnabled() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isImmediate(edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType)
   */
  public boolean isImmediate(PermissionType thePermissionType) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isImmediateMembership()
   */
  public boolean isImmediateMembership() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isImmediatePermission()
   */
  public boolean isImmediatePermission() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAllowedOverall(boolean)
   */
  public void setAllowedOverall(boolean allowedOverall1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAssignmentNotes(java.lang.String)
   */
  public void setAssignmentNotes(String assignmentNotes1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeAssignDelegatable(edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable)
   */
  public void setAttributeAssignDelegatable(AttributeAssignDelegatable attributeAssignDelegatable1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeAssignDelegatableDb(java.lang.String)
   */
  public void setAttributeAssignDelegatableDb(String theAttributeAssignDelegatableDb) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeDefNameDispName(java.lang.String)
   */
  public void setAttributeDefNameDispName(String attributeDefNameDispName1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setDisabledTime(java.sql.Timestamp)
   */
  public void setDisabledTime(Timestamp disabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setDisabledTimeDb(java.lang.Long)
   */
  public void setDisabledTimeDb(Long disabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setEnabledDb(java.lang.String)
   */
  public void setEnabledDb(String enabled1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setEnabledTime(java.sql.Timestamp)
   */
  public void setEnabledTime(Timestamp enabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setEnabledTimeDb(java.lang.Long)
   */
  public void setEnabledTimeDb(Long enabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setImmediateMshipDisabledTimeDb(java.lang.Long)
   */
  public void setImmediateMshipDisabledTimeDb(Long immediateMshipDisabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setImmediateMshipDisabledTimeDb(java.sql.Timestamp)
   */
  public void setImmediateMshipDisabledTimeDb(Timestamp immediateMshipDisabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setImmediateMshipEnabledTime(java.sql.Timestamp)
   */
  public void setImmediateMshipEnabledTime(Timestamp immediateMshipEnabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setImmediateMshipEnabledTimeDb(java.lang.Long)
   */
  public void setImmediateMshipEnabledTimeDb(Long immediateMshipEnabledTimeDb1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setRoleDisplayName(java.lang.String)
   */
  public void setRoleDisplayName(String roleDisplayName1) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PermissionEntry o) {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getEndTime()
   */
  public Timestamp getEndTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getStartTime()
   */
  public Timestamp getStartTime() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getMembershipDepth()
   */
  public int getMembershipDepth() {
    return this.membershipDepth;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setMembershipDepth(int)
   */
  public void setMembershipDepth(int membershipDepth1) {
    this.membershipDepth = membershipDepth1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getRoleSetDepth()
   */
  public int getRoleSetDepth() {
    return this.roleSetDepth;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setRoleSetDepth(int)
   */
  public void setRoleSetDepth(int roleSetDepth1) {
    this.roleSetDepth = roleSetDepth1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefNameSetDepth()
   */
  public int getAttributeDefNameSetDepth() {
    return this.attributeDefNameSetDepth;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeDefNameSetDepth(int)
   */
  public void setAttributeDefNameSetDepth(int attributeDefNameSetDepth1) {
    this.attributeDefNameSetDepth = attributeDefNameSetDepth1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeAssignActionSetDepth()
   */
  public int getAttributeAssignActionSetDepth() {
    return this.attributeAssignActionSetDepth;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeAssignActionSetDepth(int)
   */
  public void setAttributeAssignActionSetDepth(int attributeAssignActionSetDepth1) {
    this.attributeAssignActionSetDepth = attributeAssignActionSetDepth1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getRoleName()
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setRoleName(java.lang.String)
   */
  public void setRoleName(String roleName1) {
    this.roleName = roleName1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getSubjectSourceId()
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setSubjectSourceId(java.lang.String)
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getSubjectId()
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setSubjectId(java.lang.String)
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAction()
   */
  public String getAction() {
    return this.action;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAction(java.lang.String)
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefNameName()
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**@see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeDefNameName(java.lang.String)
   * 
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getActionId()
   */
  public String getActionId() {
    return this.actionId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setActionId(java.lang.String)
   */
  public void setActionId(String actionId1) {
    this.actionId = actionId1;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getRoleId()
   */
  public String getRoleId() {
    return this.roleId;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setRoleId(java.lang.String)
   */
  public void setRoleId(String roleId1) {
    this.roleId = roleId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefId()
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeDefId(java.lang.String)
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getMemberId()
   */
  public String getMemberId() {
    return this.memberId;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setMemberId(java.lang.String)
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeDefNameId()
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeDefNameId(java.lang.String)
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getMembershipId()
   */
  public String getMembershipId() {
    return this.membershipId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setMembershipId(java.lang.String)
   */
  public void setMembershipId(String membershipId1) {
    this.membershipId = membershipId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getAttributeAssignId()
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setAttributeAssignId(java.lang.String)
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getPermissionTypeDb()
   */
  public String getPermissionTypeDb() {
    return this.permissionType == null ? null : this.permissionType.name();
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#setPermissionTypeDb(java.lang.String)
   */
  public void setPermissionTypeDb(String permissionTypeDb1) {
    this.permissionType = PermissionType.valueOfIgnoreCase(permissionTypeDb1, false);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#getPermissionType()
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }
}

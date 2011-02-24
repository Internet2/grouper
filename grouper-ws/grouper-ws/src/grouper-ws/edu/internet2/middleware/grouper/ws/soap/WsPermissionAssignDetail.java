/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;


/**
 * result of permission entry query represents an assignment in the DB
 */
public class WsPermissionAssignDetail {

  /** depth of role set hierarchy, 0 means immediate */
  private String roleSetDepth;
  
  /**
   * depth of role set hierarchy, 0 means immediate
   * @return depth
   */
  public String getRoleSetDepth() {
    return this.roleSetDepth;
  }

  /**
   * depth of role set hierarchy, 0 means immediate
   * @param roleSetDepth1
   */
  public void setRoleSetDepth(String roleSetDepth1) {
    this.roleSetDepth = roleSetDepth1;
  }


  /** depth of membership (number of hops in hierarchy), 0 is immediate */
  private String membershipDepth;
  
  /**
   * depth of membership (number of hops in hierarchy), 0 is immediate
   * @return depth
   */
  public String getMembershipDepth() {
    return this.membershipDepth;
  }

  /**
   * depth of membership (number of hops in hierarchy), 0 is immediate
   * @param membershipDepth1
   */
  public void setMembershipDepth(String membershipDepth1) {
    this.membershipDepth = membershipDepth1;
  }


  /** depth of attribute def name set (number of hops in hierarchy), 0 is immediate */ 
  private String attributeDefNameSetDepth;
  
  /**
   * depth of attribute def name set (number of hops in hierarchy), 0 is immediate
   * @return depth
   */
  public String getAttributeDefNameSetDepth() {
    return this.attributeDefNameSetDepth;
  }

  /**
   * depth of attribute def name set (number of hops in hierarchy), 0 is immediate
   * @param attributeDefNameSetDepth1
   */
  public void setAttributeDefNameSetDepth(String attributeDefNameSetDepth1) {
    this.attributeDefNameSetDepth = attributeDefNameSetDepth1;
  }


  /** depth of action (number of hops in hierarchy), 0 is immediate */
  private String actionDepth;  
  
  /**
   * depth of action (number of hops in hierarchy), 0 is immediate
   * @return depth of action
   */
  public String getActionDepth() {
    return this.actionDepth;
  }

  /**
   * depth of action (number of hops in hierarchy), 0 is immediate
   * @param actionDepth1
   */
  public void setActionDepth(String actionDepth1) {
    this.actionDepth = actionDepth1;
  }


  /** AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT */
  private String permissionDelegatable;
  
  /**
   * AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT
   * @return delegatable
   */
  public String getPermissionDelegatable() {
    return this.permissionDelegatable;
  }


  /**
   * AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT
   * @param attributeAssignDelegatable1
   */
  public void setPermissionDelegatable(String attributeAssignDelegatable1) {
    this.permissionDelegatable = attributeAssignDelegatable1;
  }


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String disabledTime;

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String enabledTime;

  /**
   * notes about this assignment, free-form text
   */
  private String assignmentNotes;
  
  /** if this is a member attribute, this is the foreign key */
  private String memberId;

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String actionId;
  
  /** T or F if the membership is immediate to the role */
  private String immediateMembership;

  /** T or F if this permission is immediate to the role or subject */
  private String immediatePermission;
  
  /**
   * T or F if the membership is immediate to the role
   * @return T or F
   */
  public String getImmediateMembership() {
    return this.immediateMembership;
  }

  /**
   * T or F if the membership is immediate to the role
   * @param immediateMembership1
   */
  public void setImmediateMembership(String immediateMembership1) {
    this.immediateMembership = immediateMembership1;
  }

  /**
   * T or F if this permission is immediate to the role or subject
   * @return T or F
   */
  public String getImmediatePermission() {
    return this.immediatePermission;
  }

  /**
   * T or F if this permission is immediate to the role or subject
   * @param immediatePermission1
   */
  public void setImmediatePermission(String immediatePermission1) {
    this.immediatePermission = immediatePermission1;
  }

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return the disabled time
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return enabled time
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /**
   * notes about this assignment, free-form text
   * @return notes
   */
  public String getAssignmentNotes() {
    return this.assignmentNotes;
  }

  /**
   * notes about this assignment, free-form text
   * @param notes1
   */
  public void setAssignmentNotes(String notes1) {
    this.assignmentNotes = notes1;
  }

  /**
   * if this is a member attribute, this is the foreign key
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * if this is a member attribute, this is the foreign key
   * @param ownerMemberId1
   */
  public void setMemberId(String ownerMemberId1) {
    this.memberId = ownerMemberId1;
  }

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @return id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  public String getActionId() {
    return this.actionId;
  }

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @param attributeAssignActionId1
   */
  public void setActionId(String attributeAssignActionId1) {
    this.actionId = attributeAssignActionId1;
  }

  /**
   * 
   */
  public WsPermissionAssignDetail() {
    //default constructor
  }
  
  /**
   * construct with permission entry to set internal fields
   * 
   * @param permissionEntry
   */
  public WsPermissionAssignDetail(PermissionEntry permissionEntry) {
    
    this.actionDepth = Integer.toString(permissionEntry.getAttributeAssignActionSetDepth());
    this.actionId = permissionEntry.getActionId();
    this.assignmentNotes = permissionEntry.getAssignmentNotes();
    this.attributeDefNameSetDepth = Integer.toString(permissionEntry.getAttributeDefNameSetDepth());
    this.disabledTime = GrouperServiceUtils.dateToString(permissionEntry.getDisabledTime());
    this.enabledTime = GrouperServiceUtils.dateToString(permissionEntry.getEnabledTime());
    this.immediateMembership = permissionEntry.isImmediateMembership() ? "T" : "F";
    this.immediatePermission = permissionEntry.isImmediatePermission() ? "T" : "F";
    this.memberId = permissionEntry.getMemberId();
    this.membershipDepth = Integer.toString(permissionEntry.getMembershipDepth());
    this.permissionDelegatable = permissionEntry.getAttributeAssignDelegatableDb();
    this.roleSetDepth = Integer.toString(permissionEntry.getRoleSetDepth());
    
  }
  
  /**
   * construct with permission entry to set internal fields
   * 
   * @param pitPermissionEntry
   */
  public WsPermissionAssignDetail(PITPermissionAllView pitPermissionEntry) {
    
    this.actionDepth = Integer.toString(pitPermissionEntry.getAttributeAssignActionSetDepth());
    this.actionId = pitPermissionEntry.getActionId();
    this.attributeDefNameSetDepth = Integer.toString(pitPermissionEntry.getAttributeDefNameSetDepth());
    this.memberId = pitPermissionEntry.getMemberId();
    this.membershipDepth = Integer.toString(pitPermissionEntry.getMembershipDepth());
    this.roleSetDepth = Integer.toString(pitPermissionEntry.getRoleSetDepth());
    
  }
}

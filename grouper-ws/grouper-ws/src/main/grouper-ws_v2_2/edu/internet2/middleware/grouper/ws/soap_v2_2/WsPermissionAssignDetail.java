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
package edu.internet2.middleware.grouper.ws.soap_v2_2;



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
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score, note, this is applicable
   * to rank two similar permissions (type, resource, action, role, and if applicable, member)
   */
  private String heuristicFriendlyScore;
  
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
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score, note, this is applicable
   * to rank two similar permissions (type, resource, action, role, and if applicable, member)
   * @return score
   */
  public String getHeuristicFriendlyScore() {
    return this.heuristicFriendlyScore;
  }

  /**
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score, note, this is applicable
   * to rank two similar permissions (type, resource, action, role, and if applicable, member)
   * @param heuristicFriendlyScore1
   */
  public void setHeuristicFriendlyScore(String heuristicFriendlyScore1) {
    this.heuristicFriendlyScore = heuristicFriendlyScore1;
  }

  /**
   * 
   */
  public WsPermissionAssignDetail() {
    //default constructor
  }

}

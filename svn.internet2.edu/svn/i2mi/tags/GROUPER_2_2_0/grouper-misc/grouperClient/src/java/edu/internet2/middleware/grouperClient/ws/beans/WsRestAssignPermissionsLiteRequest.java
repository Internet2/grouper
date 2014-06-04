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
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean in body of rest request
 */
public class WsRestAssignPermissionsLiteRequest implements WsRequestBean {

  /** disabled time, or null for not disabled */
  private String assignmentDisabledTime;

  /** really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT */
  private String delegatable;
  
  /**
   * disabled time, or null for not disabled
   * @return disabled time
   */
  public String getAssignmentDisabledTime() {
    return this.assignmentDisabledTime;
  }

  /**
   * disabled time, or null for not disabled
   * @param assignmentDisabledTime1
   */
  public void setAssignmentDisabledTime(String assignmentDisabledTime1) {
    this.assignmentDisabledTime = assignmentDisabledTime1;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @return delegatable
   */
  public String getDelegatable() {
    return this.delegatable;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param delegatable1
   */
  public void setDelegatable(String delegatable1) {
    this.delegatable = delegatable1;
  }

  /**  to assign, or "assign" is the default if blank */
  private String action;

  /**  enabled time, or null for enabled now */
  private String assignmentEnabledTime;
  
  
  
  /**
   *  enabled time, or null for enabled now
   * @return enabled time
   */
  public String getAssignmentEnabledTime() {
    return this.assignmentEnabledTime;
  }

  /**
   *  enabled time, or null for enabled now
   * @param assignmentEnabledTime1
   */
  public void setAssignmentEnabledTime(String assignmentEnabledTime1) {
    this.assignmentEnabledTime = assignmentEnabledTime1;
  }


  /** notes on the assignment (optional) */
  private String  assignmentNotes;
  
  
  
  /**
   * notes on the assignment (optional)
   * @return notes
   */
  public String getAssignmentNotes() {
    return this.assignmentNotes;
  }

  /**
   * notes on the assignment (optional)
   * @param assignmentNotes1
   */
  public void setAssignmentNotes(String assignmentNotes1) {
    this.assignmentNotes = assignmentNotes1;
  }


  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   */
  private String permissionAssignOperation;
  
  
  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   * @return operation
   */
  public String getPermissionAssignOperation() {
    return this.permissionAssignOperation;
  }

  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   * @param attributeAssignOperation1
   */
  public void setPermissionAssignOperation(String attributeAssignOperation1) {
    this.permissionAssignOperation = attributeAssignOperation1;
  }

  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return client version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** is role or role_subject from the PermissionType enum */
  private String permissionType;

  
  
  /**
   * is role or role_subject from the PermissionType enum
   * @return attribute assign type
   */
  public String getPermissionType() {
    return this.permissionType;
  }
  
  /**
   * is role or role_subject from the PermissionType enum
   * @param attributeAssignType1
   */
  public void setPermissionType(String attributeAssignType1) {
    this.permissionType = attributeAssignType1;
  }
  
  /** attributeAssignId if you know the assign id you want, put it here */
  private String attributeAssignId;
  
  
  
  /**
   * attributeAssignId if you know the assign id you want, put it here
   * @return attributeAssignId
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * attributeAssignId if you know the assign id you want, put it here
   * @param attributeAssignId1
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }
  
  /** assign this attribute def name (optional) */
  private String permissionDefNameName;
  
  /** assign this attribute def name (optional) */
  private String permissionDefNameId;
  
  
  
  /**
   * assign this attribute def name (optional)
   * @return attribute def name name
   */
  public String getPermissionDefNameName() {
    return this.permissionDefNameName;
  }
  
  /**
   * assign this attribute def name (optional)
   * @param wsAttributeDefNameName1
   */
  public void setPermissionDefNameName(String wsAttributeDefNameName1) {
    this.permissionDefNameName = wsAttributeDefNameName1;
  }
  
  /**
   * assign this attribute def name (optional)
   * @return attribute def name id
   */
  public String getPermissionDefNameId() {
    return this.permissionDefNameId;
  }
  
  /**
   * assign this attribute def name (optional)
   * @param wsAttributeDefNameId1
   */
  public void setPermissionDefNameId(String wsAttributeDefNameId1) {
    this.permissionDefNameId = wsAttributeDefNameId1;
  }

  /** is role name to assign */
  private String roleName;
  
  /** is role id to assign */
  private String roleId;
  
  
  
  /**
   * is role name to assign
   * @return group name
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * is role name to assign
   * @param roleName1
   */
  public void setRoleName(String roleName1) {
    this.roleName = roleName1;
  }

  /**
   * is role id to assign
   * @return group id
   */
  public String getRoleId() {
    return this.roleId;
  }

  /**
   * is role id to assign
   * @param roleId1
   */
  public void setRoleId(String roleId1) {
    this.roleId = roleId1;
  }

  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String subjectRoleName;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String subjectRoleId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String subjectRoleSubjectId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String subjectRoleSubjectSourceId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String subjectRoleSubjectIdentifier;

  
  
  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return owner membership
   */
  public String getSubjectRoleName() {
    return this.subjectRoleName;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupName1
   */
  public void setSubjectRoleName(String wsOwnerMembershipAnyGroupName1) {
    this.subjectRoleName = wsOwnerMembershipAnyGroupName1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getSubjectRoleId() {
    return this.subjectRoleId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId1
   */
  public void setSubjectRoleId(String wsOwnerMembershipAnyGroupId1) {
    this.subjectRoleId = wsOwnerMembershipAnyGroupId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getSubjectRoleSubjectId() {
    return this.subjectRoleSubjectId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId1
   */
  public void setSubjectRoleSubjectId(String wsOwnerMembershipAnySubjectId1) {
    this.subjectRoleSubjectId = wsOwnerMembershipAnySubjectId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getSubjectRoleSubjectSourceId() {
    return this.subjectRoleSubjectSourceId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectSourceId1
   */
  public void setSubjectRoleSubjectSourceId(
      String wsOwnerMembershipAnySubjectSourceId1) {
    this.subjectRoleSubjectSourceId = wsOwnerMembershipAnySubjectSourceId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getSubjectRoleSubjectIdentifier() {
    return this.subjectRoleSubjectIdentifier;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectIdentifier1
   */
  public void setSubjectRoleSubjectIdentifier(
      String wsOwnerMembershipAnySubjectIdentifier1) {
    this.subjectRoleSubjectIdentifier = wsOwnerMembershipAnySubjectIdentifier1;
  }

  /**
   * action to query, or none to query all actions
   * @return action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * action to query, or none to query all actions
   * @param action1
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /** if acting as another user */
  private String actAsSubjectId; 

  /** if acting as another user */
  private String actAsSubjectSourceId;
  
  /** if acting as another user */
  private String actAsSubjectIdentifier; 

  /**
   * if acting as another user
   * @return id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * if acting as another user
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * if acting as another user
   * @return source id 
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * if acting as another user
   * @param actAsSubjectSourceId1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }

  /**
   * if acting as another user
   * @return subject identifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * if acting as another user
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }


  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   */
  private String includeSubjectDetail;
  
  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @return T|F
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @param includeSubjectDetail1
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  /**
   * are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent   
   */
  private String subjectAttributeNames;
  
  /**
   * are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent   
   * @return subject attribute names
   */
  public String getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent   
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }


  /** T or F as to if the group detail should be returned */
  private String includeGroupDetail;
  
  /**
   * T or F as to if the group detail should be returned
   * @return T|F
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * T or F as to if the group detail should be returned
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  /** reserved for future use */
  private String paramName0;
  
  /** reserved for future use */
  private String paramValue0;
  
  /** reserved for future use */
  private String paramName1; 

  /** reserved for future use */
  private String paramValue1;

  /** T or F (defaults to F), if this permission assignment is disallowed */
  private String disallowed; 

  /**
   * reserved for future use
   * @return param name 0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * reserved for future use
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * reserved for future use
   * @return param value 0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * reserved for future use
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * reserved for future use
   * @return paramname1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * reserved for future use
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * reserved for future use
   * @return param value 1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  /**
   * T or F (defaults to F), if this permission assignment is disallowed
   * @return T or F (defaults to F), if this permission assignment is disallowed
   */
  public String getDisallowed() {
    return this.disallowed;
  }

  /**
   * T or F (defaults to F), if this permission assignment is disallowed
   * @param disallowed1
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }
  
  
  
}

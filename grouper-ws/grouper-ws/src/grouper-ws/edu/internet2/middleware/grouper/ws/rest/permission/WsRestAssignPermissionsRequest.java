/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.permission;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;


/**
 * request bean in body of rest request
 */
public class WsRestAssignPermissionsRequest implements WsRequestBean {

  /** T or F (defaults to F), if this permission assignment is disallowed */
  private String disallowed;
  
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

  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   */
  private String permissionAssignOperation;
  
  /** notes on the assignment (optional) */
  private String assignmentNotes;
  
  
  
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
   * enabled time, or null for enabled now
   * @return enabled time
   */
  public String getAssignmentEnabledTime() {
    return this.assignmentEnabledTime;
  }

  /**
   * enabled time, or null for enabled now
   * @param assignmentEnabledTime1
   */
  public void setAssignmentEnabledTime(String assignmentEnabledTime1) {
    this.assignmentEnabledTime = assignmentEnabledTime1;
  }

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

  /**
   * enabled time, or null for enabled now
   */
  private String assignmentEnabledTime;
  
  /**
   * disabled time, or null for not disabled
   */
  private String assignmentDisabledTime;
  
  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   */
  private String delegatable;

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

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
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
  
  /** if you know the assign ids you want, put them here */
  private WsAttributeAssignLookup[] wsAttributeAssignLookups;
  
  /**
   * if you know the assign ids you want, put them here
   * @return attribute assign lookups
   */
  public WsAttributeAssignLookup[] getWsAttributeAssignLookups() {
    return this.wsAttributeAssignLookups;
  }

  /**
   * if you know the assign ids you want, put them here
   * @param wsAttributeAssignLookups1
   */
  public void setWsAttributeAssignLookups(WsAttributeAssignLookup[] wsAttributeAssignLookups1) {
    this.wsAttributeAssignLookups = wsAttributeAssignLookups1;
  }

  /**
   * find assignments in these attribute def names (optional)
   */
  private WsAttributeDefNameLookup[] permissionDefNameLookups;
  
  /**
   *  find assignments in these attribute def names (optional)
   *  @return def name lookups
   */
  public WsAttributeDefNameLookup[] getPermissionDefNameLookups() {
    return this.permissionDefNameLookups;
  }

  /**
   * find assignments in these attribute def names (optional)
   * @param wsAttributeDefNameLookups1
   */
  public void setPermissionDefNameLookups(
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups1) {
    this.permissionDefNameLookups = wsAttributeDefNameLookups1;
  }
  
  /** roleLookups are roles to look in */
  private WsGroupLookup[] roleLookups;
  
  /**
   * roleLookups are roles to look in
   * @return role lookups
   */
  public WsGroupLookup[] getRoleLookups() {
    return this.roleLookups;
  }

  /**
   * roleLookups are roles to look in
   * @param roleLookups1
   */
  public void setRoleLookups(WsGroupLookup[] roleLookups1) {
    this.roleLookups = roleLookups1;
  }

  /** to query attributes in "any" memberships which are on immediate or effective memberships */
  private WsMembershipAnyLookup[] subjectRoleLookups;
  
  
  
  /**
   * to query attributes in "any" memberships which are on immediate or effective memberships
   * @return any memberships
   */
  public WsMembershipAnyLookup[] getSubjectRoleLookups() {
    return this.subjectRoleLookups;
  }

  /**
   * to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerMembershipAnyLookups1
   */
  public void setSubjectRoleLookups(
      WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups1) {
    this.subjectRoleLookups = wsOwnerMembershipAnyLookups1;
  }
  
  /**
   * actions to query, or none to query all actions
   */
  private String[] actions; 
  
  /**
   * actions to query, or none to query all actions
   * @return actions
   */
  public String[] getActions() {
    return this.actions;
  }

  /**
   * actions to query, or none to query all actions
   * @param actions1
   */
  public void setActions(String[] actions1) {
    this.actions = actions1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;
  
  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
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

  /** are the additional subject attributes (data) to return. 
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   */
  private String[] subjectAttributeNames;

  
  
  /**
   * are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @return subject attribute names
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
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

  /** optional: reserved for future use */
  private  WsParam[] params;

  /**
   * if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   */
  private String[] actionsToReplace;

  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   */
  private WsAttributeDefLookup[] attributeDefsToReplace;

  
  
  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @return the actionsToReplace
   */
  public String[] getActionsToReplace() {
    return this.actionsToReplace;
  }

  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @return the attributeDefsToReplace
   */
  public WsAttributeDefLookup[] getAttributeDefsToReplace() {
    return this.attributeDefsToReplace;
  }

  /**
   * if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @param actionsToReplace1 the actionsToReplace to set
   */
  public void setActionsToReplace(String[] actionsToReplace1) {
    this.actionsToReplace = actionsToReplace1;
  }

  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @param attributeDefsToReplace1 the attributeDefsToReplace to set
   */
  public void setAttributeDefsToReplace(WsAttributeDefLookup[] attributeDefsToReplace1) {
    this.attributeDefsToReplace = attributeDefsToReplace1;
  }

  


}

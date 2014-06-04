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
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;


/**
 * request bean in body of rest request
 */
public class WsRestAssignAttributesRequest implements WsRequestBean {

  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   */
  private String attributeAssignOperation;
  
  /** notes on the assignment (optional) */
  private String assignmentNotes;
  
  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   */
  private WsAttributeDefLookup[] attributeDefsToReplace;
  
  /**
   * if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   */
  private String[] actionsToReplace;
  
  /**
   * if replacing attributeDefNames, then these are the
   * related attributeDefTypes, if blank, then just do all
   */
  private String[] attributeDefTypesToReplace;
  
  
  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @return the attributeDefsToReplace
   */
  public WsAttributeDefLookup[] getAttributeDefsToReplace() {
    return this.attributeDefsToReplace;
  }

  
  /**
   * if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @param attributeDefsToReplace1 the attributeDefsToReplace to set
   */
  public void setAttributeDefsToReplace(WsAttributeDefLookup[] attributeDefsToReplace1) {
    this.attributeDefsToReplace = attributeDefsToReplace1;
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
   * if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @param actionsToReplace1 the actionsToReplace to set
   */
  public void setActionsToReplace(String[] actionsToReplace1) {
    this.actionsToReplace = actionsToReplace1;
  }

  
  /**
   * if replacing attributeDefNames, then these are the
   * related attributeDefTypes, if blank, then just do all
   * @return the attributeDefTypesToReplace
   */
  public String[] getAttributeDefTypesToReplace() {
    return this.attributeDefTypesToReplace;
  }

  
  /**
   * if replacing attributeDefNames, then these are the
   * related attributeDefTypes, if blank, then just do all
   * @param attributeDefTypesToReplace1 the attributeDefTypesToReplace to set
   */
  public void setAttributeDefTypesToReplace(String[] attributeDefTypesToReplace1) {
    this.attributeDefTypesToReplace = attributeDefTypesToReplace1;
  }

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
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @return operation
   */
  public String getAttributeAssignValueOperation() {
    return this.attributeAssignValueOperation;
  }

  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param attributeAssignValueOperation1
   */
  public void setAttributeAssignValueOperation(String attributeAssignValueOperation1) {
    this.attributeAssignValueOperation = attributeAssignValueOperation1;
  }

  /**
   * for assignment on assignment
   * @return assignment lookup
   */
  public WsAttributeAssignLookup[] getWsOwnerAttributeAssignLookups() {
    return this.wsOwnerAttributeAssignLookups;
  }

  /**
   * for assignment on assignment
   * @param wsOwnerAttributeAssignLookups1
   */
  public void setWsOwnerAttributeAssignLookups(
      WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups1) {
    this.wsOwnerAttributeAssignLookups = wsOwnerAttributeAssignLookups1;
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
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   */
  private String attributeAssignValueOperation;

  /**
   * for assignment on assignment
   */
  private WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups;
  
  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @return operation
   */
  public String getAttributeAssignOperation() {
    return this.attributeAssignOperation;
  }

  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param attributeAssignOperation1
   */
  public void setAttributeAssignOperation(String attributeAssignOperation1) {
    this.attributeAssignOperation = attributeAssignOperation1;
  }

  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   */
  private WsAttributeAssignValue[] values;
  
  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @return values
   */
  public WsAttributeAssignValue[] getValues() {
    return this.values;
  }

  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @param values1
   */
  public void setValues(WsAttributeAssignValue[] values1) {
    this.values = values1;
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

  /** is the attribute assign type we are looking for */
  private String attributeAssignType;
  
  /**
   * is the attribute assign type we are looking for
   * @return attribute assign type
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * is the attribute assign type we are looking for
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
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
   * assign these attribute def names (optional)
   */
  private WsAttributeDefNameLookup[] wsAttributeDefNameLookups;
  
  /**
   *  find assignments in these attribute def names (optional)
   *  @return def name lookups
   */
  public WsAttributeDefNameLookup[] getWsAttributeDefNameLookups() {
    return this.wsAttributeDefNameLookups;
  }

  /**
   * find assignments in these attribute def names (optional)
   * @param wsAttributeDefNameLookups1
   */
  public void setWsAttributeDefNameLookups(
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups1) {
    this.wsAttributeDefNameLookups = wsAttributeDefNameLookups1;
  }
  
  /** wsOwnerGroupLookups are groups to assign */
  private WsGroupLookup[] wsOwnerGroupLookups;
  
  /**
   * wsOwnerGroupLookups are groups to assign
   * @return owner group lookups
   */
  public WsGroupLookup[] getWsOwnerGroupLookups() {
    return this.wsOwnerGroupLookups;
  }

  /**
   * wsOwnerGroupLookups are groups to assign
   * @param wsOwnerGroupLookups1
   */
  public void setWsOwnerGroupLookups(WsGroupLookup[] wsOwnerGroupLookups1) {
    this.wsOwnerGroupLookups = wsOwnerGroupLookups1;
  }

  /** are stems to assign */
  private WsStemLookup[] wsOwnerStemLookups;
  
  /**
   * are stems to assign
   * @return are stems to assign
   */
  public WsStemLookup[] getWsOwnerStemLookups() {
    return this.wsOwnerStemLookups;
  }

  /**
   * are stems to assign
   * @param wsOwnerStemLookups1
   */
  public void setWsOwnerStemLookups(WsStemLookup[] wsOwnerStemLookups1) {
    this.wsOwnerStemLookups = wsOwnerStemLookups1;
  }

  /** are subjects to assign */
  private WsSubjectLookup[] wsOwnerSubjectLookups;
  
  
  
  /**
   * are subjects to assign
   * @return subject
   */
  public WsSubjectLookup[] getWsOwnerSubjectLookups() {
    return this.wsOwnerSubjectLookups;
  }

  /**
   * are subjects to assign
   * @param wsOwnerSubjectLookups1
   */
  public void setWsOwnerSubjectLookups(WsSubjectLookup[] wsOwnerSubjectLookups1) {
    this.wsOwnerSubjectLookups = wsOwnerSubjectLookups1;
  }
  
  /** to query attributes on immediate memberships */
  private WsMembershipLookup[] wsOwnerMembershipLookups;
  
  /** to query attributes in "any" memberships which are on immediate or effective memberships */
  private WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups;
  
  
  
  /**
   * to query attributes on immediate memberships
   * @return owner memberships
   */
  public WsMembershipLookup[] getWsOwnerMembershipLookups() {
    return this.wsOwnerMembershipLookups;
  }

  /**
   * to query attributes on immediate memberships
   * @param wsOwnerMembershipLookups1
   */
  public void setWsOwnerMembershipLookups(WsMembershipLookup[] wsOwnerMembershipLookups1) {
    this.wsOwnerMembershipLookups = wsOwnerMembershipLookups1;
  }

  /**
   * to query attributes in "any" memberships which are on immediate or effective memberships
   * @return any memberships
   */
  public WsMembershipAnyLookup[] getWsOwnerMembershipAnyLookups() {
    return this.wsOwnerMembershipAnyLookups;
  }

  /**
   * to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerMembershipAnyLookups1
   */
  public void setWsOwnerMembershipAnyLookups(
      WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups1) {
    this.wsOwnerMembershipAnyLookups = wsOwnerMembershipAnyLookups1;
  }
  
  /**
   * to query attributes assigned on attribute defs
   */
  private WsAttributeDefLookup[] wsOwnerAttributeDefLookups;
  
  /**
   * to query attributes assigned on attribute defs
   * @return attribute def
   */
  public WsAttributeDefLookup[] getWsOwnerAttributeDefLookups() {
    return this.wsOwnerAttributeDefLookups;
  }

  /**
   * to query attributes assigned on attribute defs
   * @param wsOwnerAttributeDefLookups1
   */
  public void setWsOwnerAttributeDefLookups(
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups1) {
    this.wsOwnerAttributeDefLookups = wsOwnerAttributeDefLookups1;
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

  


}

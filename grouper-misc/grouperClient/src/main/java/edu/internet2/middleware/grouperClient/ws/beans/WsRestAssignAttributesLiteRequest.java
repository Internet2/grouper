/**
 * Copyright 2014 Internet2
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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * request bean in body of rest request
 */
public class WsRestAssignAttributesLiteRequest implements WsRequestBean {

  /** disabled time, or null for not disabled */
  private String assignmentDisabledTime;

  /** really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT */
  private String delegatable;
  
  /** 
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values  
   */
  private String attributeAssignValueOperation;

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
   * @return id
   */
  public String getWsOwnerAttributeAssignId() {
    return this.wsOwnerAttributeAssignId;
  }

  /**
   * for assignment on assignment
   * @param wsOwnerAttributeAssignId1
   */
  public void setWsOwnerAttributeAssignId(String wsOwnerAttributeAssignId1) {
    this.wsOwnerAttributeAssignId = wsOwnerAttributeAssignId1;
  }


  /**  for assignment on assignment */
  private String wsOwnerAttributeAssignId;

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


  /** is value to add, assign, remove, etc though not implemented yet */
  private String valueFormatted; 
  
  
  
  /**
   * is value to add, assign, remove, etc though not implemented yet
   * @return value
   */
  public String getValueFormatted() {
    return this.valueFormatted;
  }

  /**
   * is value to add, assign, remove, etc though not implemented yet
   * @param valueFormatted1
   */
  public void setValueFormatted(String valueFormatted1) {
    this.valueFormatted = valueFormatted1;
  }


  /** is value to add, assign, remove, etc */
  private String valueSystem;
  
  
  
  /**
   * is value to add, assign, remove, etc
   * @return value system
   */
  public String getValueSystem() {
    return this.valueSystem;
  }

  /**
   * is value to add, assign, remove, etc
   * @param valueSystem1
   */
  public void setValueSystem(String valueSystem1) {
    this.valueSystem = valueSystem1;
  }


  /**
   * If removing, and id is specified, will
   * only remove values with that id.
   */
  private String valueId; 
  
  /**
   * If removing, and id is specified, will
   * only remove values with that id.
   * @return value id
   */
  public String getValueId() {
    return this.valueId;
  }

  /**
   * If removing, and id is specified, will
   * only remove values with that id.
   * @param valueId1
   */
  public void setValueId(String valueId1) {
    this.valueId = valueId1;
  }


  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   */
  private String attributeAssignOperation;
  
  
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
  
  /** find assignments in this attribute def name (optional) */
  private String wsAttributeDefNameName;
  
  /** find assignments in this attribute def name (optional) */
  private String wsAttributeDefNameId;
  
  
  
  /**
   * find assignments in this attribute def name (optional)
   * @return attribute def name name
   */
  public String getWsAttributeDefNameName() {
    return this.wsAttributeDefNameName;
  }
  
  /**
   * find assignments in this attribute def name (optional)
   * @param wsAttributeDefNameName1
   */
  public void setWsAttributeDefNameName(String wsAttributeDefNameName1) {
    this.wsAttributeDefNameName = wsAttributeDefNameName1;
  }
  
  /**
   * find assignments in this attribute def name (optional)
   * @return attribute def name id
   */
  public String getWsAttributeDefNameId() {
    return this.wsAttributeDefNameId;
  }
  
  /**
   * find assignments in this attribute def name (optional)
   * @param wsAttributeDefNameId1
   */
  public void setWsAttributeDefNameId(String wsAttributeDefNameId1) {
    this.wsAttributeDefNameId = wsAttributeDefNameId1;
  }

  /** is group name to look in */
  private String wsOwnerGroupName;
  
  /** is group id to look in */
  private String wsOwnerGroupId;
  
  
  
  /**
   * is group name to look in
   * @return group name
   */
  public String getWsOwnerGroupName() {
    return this.wsOwnerGroupName;
  }

  /**
   * is group name to look in
   * @param wsOwnerGroupName1
   */
  public void setWsOwnerGroupName(String wsOwnerGroupName1) {
    this.wsOwnerGroupName = wsOwnerGroupName1;
  }

  /**
   * is group id to look in
   * @return group id
   */
  public String getWsOwnerGroupId() {
    return this.wsOwnerGroupId;
  }

  /**
   * is group id to look in
   * @param wsOwnerGroupId1
   */
  public void setWsOwnerGroupId(String wsOwnerGroupId1) {
    this.wsOwnerGroupId = wsOwnerGroupId1;
  }

  /** is stem to look in */
  private String wsOwnerStemName;
  
  /** is stem to look in */
  private String wsOwnerStemId; 
  
  
  /**
   * is stem to look in
   * @return stem
   */
  public String getWsOwnerStemName() {
    return this.wsOwnerStemName;
  }

  /**
   * is stem to look in
   * @param wsOwnerStemName1
   */
  public void setWsOwnerStemName(String wsOwnerStemName1) {
    this.wsOwnerStemName = wsOwnerStemName1;
  }

  /**
   * is stem to look in
   * @return stem
   */
  public String getWsOwnerStemId() {
    return this.wsOwnerStemId;
  }

  /**
   * is stem to look in
   * @param wsOwnerStemId1
   */
  public void setWsOwnerStemId(String wsOwnerStemId1) {
    this.wsOwnerStemId = wsOwnerStemId1;
  }

  /** is subject to look in */
  private String wsOwnerSubjectId;

  /** is subject to look in */
  private String wsOwnerSubjectSourceId;
  
  /** is subject to look in */
  private String wsOwnerSubjectIdentifier;
  
  
  
  /**
   * is subject to look in
   * @return subject
   */
  public String getWsOwnerSubjectId() {
    return this.wsOwnerSubjectId;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectId1
   */
  public void setWsOwnerSubjectId(String wsOwnerSubjectId1) {
    this.wsOwnerSubjectId = wsOwnerSubjectId1;
  }

  /**
   * is subject to look in
   * @return subject
   */
  public String getWsOwnerSubjectSourceId() {
    return this.wsOwnerSubjectSourceId;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectSourceId1
   */
  public void setWsOwnerSubjectSourceId(String wsOwnerSubjectSourceId1) {
    this.wsOwnerSubjectSourceId = wsOwnerSubjectSourceId1;
  }

  /**
   * is subject to look in
   * @return subject
   */
  public String getWsOwnerSubjectIdentifier() {
    return this.wsOwnerSubjectIdentifier;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectIdentifier1
   */
  public void setWsOwnerSubjectIdentifier(String wsOwnerSubjectIdentifier1) {
    this.wsOwnerSubjectIdentifier = wsOwnerSubjectIdentifier1;
  }

  /**
   * to query attributes on immediate membership
   */
  private String wsOwnerMembershipId;
  
  /**
   * to query attributes on immediate membership
   * @return membership id
   */
  public String getWsOwnerMembershipId() {
    return this.wsOwnerMembershipId;
  }

  /**
   * to query attributes on immediate membership
   * @param wsOwnerMembershipId1
   */
  public void setWsOwnerMembershipId(String wsOwnerMembershipId1) {
    this.wsOwnerMembershipId = wsOwnerMembershipId1;
  }

  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String wsOwnerMembershipAnyGroupName;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String wsOwnerMembershipAnyGroupId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String wsOwnerMembershipAnySubjectId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String wsOwnerMembershipAnySubjectSourceId;
  
  /** to query attributes in "any" membership which is on immediate or effective membership */
  private String wsOwnerMembershipAnySubjectIdentifier;

  
  
  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return owner membership
   */
  public String getWsOwnerMembershipAnyGroupName() {
    return this.wsOwnerMembershipAnyGroupName;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupName1
   */
  public void setWsOwnerMembershipAnyGroupName(String wsOwnerMembershipAnyGroupName1) {
    this.wsOwnerMembershipAnyGroupName = wsOwnerMembershipAnyGroupName1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getWsOwnerMembershipAnyGroupId() {
    return this.wsOwnerMembershipAnyGroupId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId1
   */
  public void setWsOwnerMembershipAnyGroupId(String wsOwnerMembershipAnyGroupId1) {
    this.wsOwnerMembershipAnyGroupId = wsOwnerMembershipAnyGroupId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getWsOwnerMembershipAnySubjectId() {
    return this.wsOwnerMembershipAnySubjectId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId1
   */
  public void setWsOwnerMembershipAnySubjectId(String wsOwnerMembershipAnySubjectId1) {
    this.wsOwnerMembershipAnySubjectId = wsOwnerMembershipAnySubjectId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getWsOwnerMembershipAnySubjectSourceId() {
    return this.wsOwnerMembershipAnySubjectSourceId;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectSourceId1
   */
  public void setWsOwnerMembershipAnySubjectSourceId(
      String wsOwnerMembershipAnySubjectSourceId1) {
    this.wsOwnerMembershipAnySubjectSourceId = wsOwnerMembershipAnySubjectSourceId1;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @return any membership
   */
  public String getWsOwnerMembershipAnySubjectIdentifier() {
    return this.wsOwnerMembershipAnySubjectIdentifier;
  }

  /**
   * to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectIdentifier1
   */
  public void setWsOwnerMembershipAnySubjectIdentifier(
      String wsOwnerMembershipAnySubjectIdentifier1) {
    this.wsOwnerMembershipAnySubjectIdentifier = wsOwnerMembershipAnySubjectIdentifier1;
  }

  /**  to query attributes assigned on attribute def */
  private String wsOwnerAttributeDefName;
  
  /**  to query attributes assigned on attribute def */
  private String wsOwnerAttributeDefId;
  
  
  
  /**
   *  to query attributes assigned on attribute def
   * @return attr def
   */
  public String getWsOwnerAttributeDefName() {
    return this.wsOwnerAttributeDefName;
  }

  /**
   *  to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefName1
   */
  public void setWsOwnerAttributeDefName(String wsOwnerAttributeDefName1) {
    this.wsOwnerAttributeDefName = wsOwnerAttributeDefName1;
  }

  /**
   *  to query attributes assigned on attribute def
   * @return attr def
   */
  public String getWsOwnerAttributeDefId() {
    return this.wsOwnerAttributeDefId;
  }

  /**
   *  to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefId1
   */
  public void setWsOwnerAttributeDefId(String wsOwnerAttributeDefId1) {
    this.wsOwnerAttributeDefId = wsOwnerAttributeDefId1;
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
  
  
  
}

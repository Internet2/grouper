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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Bean for rest request to get attributes
 */
@ApiModel(description = "bean that will be the data from rest request for getting attribute assignments<br /><br /><b>actAsSubjectLookup</b>: If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user to act as here<br />"
    + "<br /><br /><b>params</b>: optional params for this request<br />"
    + "<br /><br /><b>wsAttributeAssignLookups</b>: if you know the assign ids you want, put them here<br />"
    + "<br /><br /><b>wsAttributeDefLookups</b>: find assignments in these attribute defs (optional)<br />"
    + "<br /><br />wsAttributeDefNameLookups<b></b>: find assignments in these attribute def names (optional)<br />"
    + "<br /><br /><b>wsOwnerGroupLookups</b>: wsOwnerGroupLookups are groups to look in<br />"
    + "<br /><br /><b>wsOwnerStemLookups</b>: are stems to look in<br />"
    + "<br /><br /><b>wsOwnerSubjectLookups</b>: are subjects to look in<br />"
    + "<br /><br /><b>wsOwnerMembershipLookups</b>: to query attributes on immediate memberships<br />"
    + "<br /><br /><b>wsOwnerMembershipAnyLookups</b>: to query attributes in \"any\" memberships which are on immediate or effective memberships<br />")
public class WsRestGetAttributeAssignmentsRequest implements WsRequestBean {

  /**
   * required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
   */
  private String attributeDefValueType;
  
  /**
   * required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
   * @return attributeDefValueType
   */
  @ApiModelProperty(value = "required if sending theValue", example = "floating, integer, memberId, string, timestamp")
  public String getAttributeDefValueType() {
    return this.attributeDefValueType;
  }

  /**
   * required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
   * @param attributeDefValueType1
   */
  public void setAttributeDefValueType(String attributeDefValueType1) {
    this.attributeDefValueType = attributeDefValueType1;
  }

  /**
   * null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   */
  private String attributeDefType;
  
  /**
   * null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   * @return attributeDefValueType
   */
  @ApiModelProperty(value = "null for all, or specify an AttributeDefType", example = "attr, limit, service, type, limit, permp")
  public String getAttributeDefType() {
    return this.attributeDefType;
  }

  /**
   * null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   * @param attributeDefType1
   */
  public void setAttributeDefType(String attributeDefType1) {
    this.attributeDefType = attributeDefType1;
  }

  /**
	 * value if you are passing in one attributeDefNameLookup
	 */
  private String theValue;

  /**
   * T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   */
  private String includeAssignmentsFromAssignments;

  /**
   * value if you are passing in one attributeDefNameLookup
   * @return value
   */
  @ApiModelProperty(value = "value assigned to an attribute that you are searching for if you are passing in one attributeDefNameLookup", example = "myValue")
  public String getTheValue() {
    return this.theValue;
  }

  /**
   * value if you are passing in one attributeDefNameLookup
   * @param theValue1
   */
  public void setTheValue(String theValue1) {
    this.theValue = theValue1;
  }

  /**
   * T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   * @return if include assignments from assignments
   */
  @ApiModelProperty(value = "T|F if you are finding an assignment that is an assignmentOnAssignment, then get the assignment which tells you the owner as well", example = "T|F")
  public String getIncludeAssignmentsFromAssignments() {
    return this.includeAssignmentsFromAssignments;
  }

  /**
   * T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   * @param includeAssignmentsFromAssignments1
   */
  public void setIncludeAssignmentsFromAssignments(String includeAssignmentsFromAssignments1) {
    this.includeAssignmentsFromAssignments = includeAssignmentsFromAssignments1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
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
  @ApiModelProperty(value = " is the attribute assign type we are looking for", example = "group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn,stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn")
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
   * find assignments in these attribute defs (optional)
   */
  private WsAttributeDefLookup[] wsAttributeDefLookups;
  
  /**
   * find assignments in these attribute defs (optional)
   * @return defs
   */
  public WsAttributeDefLookup[] getWsAttributeDefLookups() {
    return this.wsAttributeDefLookups;
  }

  /**
   * find assignments in these attribute defs (optional)
   * @param wsAttributeDefLookups1
   */
  public void setWsAttributeDefLookups(WsAttributeDefLookup[] wsAttributeDefLookups1) {
    this.wsAttributeDefLookups = wsAttributeDefLookups1;
  }
  
  /**
   * find assignments in these attribute def names (optional)
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
  
  /** wsOwnerGroupLookups are groups to look in */
  private WsGroupLookup[] wsOwnerGroupLookups;
  
  /**
   * wsOwnerGroupLookups are groups to look in
   * @return owner group lookups
   */
  public WsGroupLookup[] getWsOwnerGroupLookups() {
    return this.wsOwnerGroupLookups;
  }

  /**
   * wsOwnerGroupLookups are groups to look in
   * @param wsOwnerGroupLookups1
   */
  public void setWsOwnerGroupLookups(WsGroupLookup[] wsOwnerGroupLookups1) {
    this.wsOwnerGroupLookups = wsOwnerGroupLookups1;
  }

  /** are stems to look in */
  private WsStemLookup[] wsOwnerStemLookups;
  
  /**
   * are stems to look in
   * @return are stems to look in
   */
  public WsStemLookup[] getWsOwnerStemLookups() {
    return this.wsOwnerStemLookups;
  }

  /**
   * are stems to look in
   * @param wsOwnerStemLookups1
   */
  public void setWsOwnerStemLookups(WsStemLookup[] wsOwnerStemLookups1) {
    this.wsOwnerStemLookups = wsOwnerStemLookups1;
  }

  /** are subjects to look in */
  private WsSubjectLookup[] wsOwnerSubjectLookups;
  
  
  
  /**
   * are subjects to look in
   * @return subject
   */
  public WsSubjectLookup[] getWsOwnerSubjectLookups() {
    return this.wsOwnerSubjectLookups;
  }

  /**
   * are subjects to look in
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
  @ApiModelProperty(value = "actions to query, or none to query all actions", example = "assign")
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

  /**
   * if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   */
  private String includeAssignmentsOnAssignments;

  
  
  /**
   * if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @return if include assignments on assignments
   */
  @ApiModelProperty(value = "if this is not querying assignments on assignments directly, but the assignments and assignments on those assignments should be returned, enter true.  default to false", example = "T|F")
  public String getIncludeAssignmentsOnAssignments() {
    return this.includeAssignmentsOnAssignments;
  }

  /**
   * if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param includeAssignmentsOnAssignments1
   */
  public void setIncludeAssignmentsOnAssignments(String includeAssignmentsOnAssignments1) {
    this.includeAssignmentsOnAssignments = includeAssignmentsOnAssignments1;
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
  @ApiModelProperty(value = "T|F, for if the extended subject information should be returned (anything more than just the id)", example = "T|F")
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
  @ApiModelProperty(value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent", example = "LastName")
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
  @ApiModelProperty(value = "T or F as to if the group detail should be returned", example = "T|F")
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

  /** A for all, T or null for enabled only, F for disabled  */
  private String enabled;

  /**
   * A for all, T or null for enabled only, F for disabled 
   * @return enabled
   */
  @ApiModelProperty(value = "A for all, T or null for enabled only, F for disabled ", example = "A, T, F")
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * A for all, T or null for enabled only, F for disabled 
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * if looking for assignments on assignments, this is the assignment the assignment is assigned to
   */
  private WsAttributeAssignLookup[] wsAssignAssignOwnerAttributeAssignLookups;
  
  /**
   * if looking for assignments on assignments, this is the assignment the assignment is assigned to
   * @return results
   */
  public WsAttributeAssignLookup[] getWsAssignAssignOwnerAttributeAssignLookups() {
    return this.wsAssignAssignOwnerAttributeAssignLookups;
  }

  /**
   * if looking for assignments on assignments, this is the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAttributeAssignLookups1
   */
  public void setWsAssignAssignOwnerAttributeAssignLookups(
      WsAttributeAssignLookup[] wsAssignAssignOwnerAttributeAssignLookups1) {
    this.wsAssignAssignOwnerAttributeAssignLookups = wsAssignAssignOwnerAttributeAssignLookups1;
  }

  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   */
  private WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups;

  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   * @return results
   */
  public WsAttributeDefLookup[] getWsAssignAssignOwnerAttributeDefLookups() {
    return this.wsAssignAssignOwnerAttributeDefLookups;
  }

  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAttributeDefLookups1
   */
  public void setWsAssignAssignOwnerAttributeDefLookups(
      WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups1) {
    this.wsAssignAssignOwnerAttributeDefLookups = wsAssignAssignOwnerAttributeDefLookups1;
  }

  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   */
  private WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups;
  
  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   * @return result
   */
  public WsAttributeDefNameLookup[] getWsAssignAssignOwnerAttributeDefNameLookups() {
    return this.wsAssignAssignOwnerAttributeDefNameLookups;
  }

  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAttributeDefNameLookups1
   */
  public void setWsAssignAssignOwnerAttributeDefNameLookups(
      WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups1) {
    this.wsAssignAssignOwnerAttributeDefNameLookups = wsAssignAssignOwnerAttributeDefNameLookups1;
  }

  /**
   * if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to
   */
  
  private String[] wsAssignAssignOwnerActions;

  /**
   * if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to
   * @return actions
   */
  @ApiModelProperty(value = "if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to")
  public String[] getWsAssignAssignOwnerActions() {
    return this.wsAssignAssignOwnerActions;
  }

  /**
   * if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerActions1
   */
  public void setWsAssignAssignOwnerActions(String[] wsAssignAssignOwnerActions1) {
    this.wsAssignAssignOwnerActions = wsAssignAssignOwnerActions1;
  }

}

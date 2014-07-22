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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * Bean for rest request to get permissions
 */
public class WsRestGetPermissionAssignmentsRequest implements WsRequestBean {

  /**
   * includeLimits T or F (default to F) for if limits should be returned with the results.
   * Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists
   */
  private String includeLimits;
  
  /**
   * includeLimits T or F (default to F) for if limits should be returned with the results.
   * Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists
   * @return the includeLimits
   */
  public String getIncludeLimits() {
    return this.includeLimits;
  }
  
  /**
   * includeLimits T or F (default to F) for if limits should be returned with the results.
   * Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists
   * @param includeLimits1 the includeLimits to set
   */
  public void setIncludeLimits(String includeLimits1) {
    this.includeLimits = includeLimits1;
  }

 /**
  * immediateOnly T of F (defaults to F) if we should filter out non immediate permissions
  */
 private String immediateOnly;
 
 
 /**
  * immediateOnly T of F (defaults to F) if we should filter out non immediate permissions
  * @return the immediateOnly
  */
 public String getImmediateOnly() {
   return this.immediateOnly;
 }

 
 /**
  * immediateOnly T of F (defaults to F) if we should filter out non immediate permissions
  * @param immediateOnly1 the immediateOnly to set
  */
 public void setImmediateOnly(String immediateOnly1) {
   this.immediateOnly = immediateOnly1;
 }
 
 /**
  * are we looking for role permissions or subject permissions?  from
  * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
  * @return the permissionType
  */
 public String getPermissionType() {
   return this.permissionType;
 }
 
 /**
  * are we looking for role permissions or subject permissions?  from
  * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
  * @param permissionType1 the permissionType to set
  */
 public void setPermissionType(String permissionType1) {
   this.permissionType = permissionType1;
 }
 
 /**
  * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
  * name of the variable, and the value is the value.  Note, you can typecast the
  * values by putting a valid type in parens in front of the param name.  e.g.
  * name: (int)amount, value: 50
  * @return the limitEnvVars
  */
 public WsPermissionEnvVar[] getLimitEnvVars() {
   return this.limitEnvVars;
 }
 
 /**
  * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
  * name of the variable, and the value is the value.  Note, you can typecast the
  * values by putting a valid type in parens in front of the param name.  e.g.
  * name: (int)amount, value: 50
  * @param limitEnvVars1 the limitEnvVars to set
  */
 public void setLimitEnvVars(WsPermissionEnvVar[] limitEnvVars1) {
   this.limitEnvVars = limitEnvVars1;
 }

 /**
  * are we looking for role permissions or subject permissions?  from
  * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
  */
 private String permissionType;

 /**
  * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
  * name of the variable, and the value is the value.  Note, you can typecast the
  * values by putting a valid type in parens in front of the param name.  e.g.
  * name: (int)amount, value: 50
  */
 private WsPermissionEnvVar[] limitEnvVars;

  /** T or F for if attributeDefName objects should be returned */
  private String includeAttributeDefNames;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   */
  private String pointInTimeFrom;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS   
   */
  private String pointInTimeTo;
  
  /**
   * T or F for if attributeDefName objects should be returned
   * @return the attributeDefName
   */
  public String getIncludeAttributeDefNames() {
    return this.includeAttributeDefNames;
  }

  /**
   * T or F for if attributeDefName objects should be returned
   * @param includeAttributeDefNames1
   */
  public void setIncludeAttributeDefNames(String includeAttributeDefNames1) {
    this.includeAttributeDefNames = includeAttributeDefNames1;
  }

  /** T or F for if the permission details should be returned */
  private String includePermissionAssignDetail;
  
  
  
  /**
   * T or F for if the permission details should be returned
   * @return T or F
   */
  public String getIncludePermissionAssignDetail() {
    return this.includePermissionAssignDetail;
  }

  /**
   * T or F for if the permission details should be returned
   * @param includePermissionAssignDetail1
   */
  public void setIncludePermissionAssignDetail(String includePermissionAssignDetail1) {
    this.includePermissionAssignDetail = includePermissionAssignDetail1;
  }

  /** T or F for it attribute assignments should be returned */
  private String includeAttributeAssignments;
  
  /**
   * T or F for it attribute assignments should be returned
   * @return include attribute assignments
   */
  public String getIncludeAttributeAssignments() {
    return this.includeAttributeAssignments;
  }

  /**
   * T or F for it attribute assignments should be returned
   * @param includeAttributeAssignments1
   */
  public void setIncludeAttributeAssignments(String includeAttributeAssignments1) {
    this.includeAttributeAssignments = includeAttributeAssignments1;
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
  
  /** are roles to look in */
  private WsGroupLookup[] roleLookups;
  
  /**
   * are roles to look in
   * @return owner group lookups
   */
  public WsGroupLookup[] getRoleLookups() {
    return this.roleLookups;
  }

  /**
   * are roles to look in
   * @param wsOwnerGroupLookups1
   */
  public void setRoleLookups(WsGroupLookup[] wsOwnerGroupLookups1) {
    this.roleLookups = wsOwnerGroupLookups1;
  }

  /** are subjects to look in */
  private WsSubjectLookup[] wsSubjectLookups;
  
  
  
  /**
   * are subjects to look in
   * @return subject
   */
  public WsSubjectLookup[] getWsSubjectLookups() {
    return this.wsSubjectLookups;
  }

  /**
   * are subjects to look in
   * @param wsOwnerSubjectLookups1
   */
  public void setWsSubjectLookups(WsSubjectLookup[] wsOwnerSubjectLookups1) {
    this.wsSubjectLookups = wsOwnerSubjectLookups1;
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

  /** A for all, T or null for enabled only, F for disabled  */
  private String enabled;

  /** 
   * if processing permissions, you can filter out either redundant permissions (find best in set),
   * or do that and filter out redundant roles (if flattening roles) (find best in set).  This is the
   * PermissionProcessor enum.  e.g. FILTER_REDUNDANT_PERMISSIONS, FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS,
   * PROCESS_LIMITS.  If null, then just get all permissions and process on the client.
   */
  private String permissionProcessor;

  /**
   * A for all, T or null for enabled only, F for disabled 
   * @return enabled
   */
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
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @return the pointInTimeFrom
   */
  public String getPointInTimeFrom() {
    return this.pointInTimeFrom;
  }

  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeFrom1 the pointInTimeFrom to set
   */
  public void setPointInTimeFrom(String pointInTimeFrom1) {
    this.pointInTimeFrom = pointInTimeFrom1;
  }

  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS 
   * @return the pointInTimeTo
   */
  public String getPointInTimeTo() {
    return this.pointInTimeTo;
  }

  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS 
   * @param pointInTimeTo1 the pointInTimeTo to set
   */
  public void setPointInTimeTo(String pointInTimeTo1) {
    this.pointInTimeTo = pointInTimeTo1;
  }

  /**
   * if processing permissions, you can filter out either redundant permissions (find best in set),
   * or do that and filter out redundant roles (if flattening roles) (find best in set).  This is the
   * PermissionProcessor enum.  e.g. FILTER_REDUNDANT_PERMISSIONS, FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS,
   * PROCESS_LIMITS.  If null, then just get all permissions and process on the client.
   * @return processor
   */
  public String getPermissionProcessor() {
    return this.permissionProcessor;
  }

  /**
   * if processing permissions, you can filter out either redundant permissions (find best in set),
   * or do that and filter out redundant roles (if flattening roles) (find best in set).  This is the
   * PermissionProcessor enum.  e.g. FILTER_REDUNDANT_PERMISSIONS, FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS,
   * PROCESS_LIMITS.  If null, then just get all permissions and process on the client.
   * @param permissionProcessor1
   */
  public void setPermissionProcessor(String permissionProcessor1) {
    this.permissionProcessor = permissionProcessor1;
  }


}

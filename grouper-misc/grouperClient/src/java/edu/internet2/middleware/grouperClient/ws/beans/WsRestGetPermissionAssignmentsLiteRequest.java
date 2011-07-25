/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * Bean for rest request to get permissions lite
 */
public class WsRestGetPermissionAssignmentsLiteRequest implements WsRequestBean {

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
   * are we looking for role permissions or subject permissions?  from
   * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
   */
  private String permissionType;

  /**
   * first env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarName0;
  
  
  /**
   * first env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarName0
   */
  public String getLimitEnvVarName0() {
    return this.limitEnvVarName0;
  }

  
  /**
   * first env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param _limitEnvVarName0 the limitEnvVarName0 to set
   */
  public void setLimitEnvVarName0(String _limitEnvVarName0) {
    this.limitEnvVarName0 = _limitEnvVarName0;
  }

  /**
   * first env var value
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarValue0;
  
  /**
   * first env var value
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarValue0
   */
  public String getLimitEnvVarValue0() {
    return this.limitEnvVarValue0;
  }

  /**
   * first env var value
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param _limitEnvVarValue0 the limitEnvVarValue0 to set
   */
  public void setLimitEnvVarValue0(String _limitEnvVarValue0) {
    this.limitEnvVarValue0 = _limitEnvVarValue0;
  }

  /**
   * second env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarName1
   */
  public String getLimitEnvVarName1() {
    return this.limitEnvVarName1;
  }

  
  /**
   * second env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param _limitEnvVarName1 the limitEnvVarName1 to set
   */
  public void setLimitEnvVarName1(String _limitEnvVarName1) {
    this.limitEnvVarName1 = _limitEnvVarName1;
  }

  /**
   * first env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarType0;
  
  /**
   * first env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarType0
   */
  public String getLimitEnvVarType0() {
    return this.limitEnvVarType0;
  }

  
  /**
   * first env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param _limitEnvVarType0 the limitEnvVarType0 to set
   */
  public void setLimitEnvVarType0(String _limitEnvVarType0) {
    this.limitEnvVarType0 = _limitEnvVarType0;
  }

  /**
   * second env var name
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarName1;
  
  /**
   * second env var value
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarValue1;
  
  /**
   * second env var value
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarValue1
   */
  public String getLimitEnvVarValue1() {
    return this.limitEnvVarValue1;
  }

  /**
   * @param _limitEnvVarValue1 the limitEnvVarValue1 to set
   */
  public void setLimitEnvVarValue1(String _limitEnvVarValue1) {
    this.limitEnvVarValue1 = _limitEnvVarValue1;
  }

  /**
   * second env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private String limitEnvVarType1;
  
  /**
   * second env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @return the limitEnvVarType1
   */
  public String getLimitEnvVarType1() {
    return this.limitEnvVarType1;
  }
  
  /**
   * second env var type
   * limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param _limitEnvVarType1 the limitEnvVarType1 to set
   */
  public void setLimitEnvVarType1(String _limitEnvVarType1) {
    this.limitEnvVarType1 = _limitEnvVarType1;
  }

  /** 
   * if processing permissions, you can filter out either redundant permissions (find best in set),
   * or do that and filter out redundant roles (if flattening roles) (find best in set).  This is the
   * PermissionProcessor enum.  e.g. FILTER_REDUNDANT_PERMISSIONS, FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS,
   * PROCESS_LIMITS.  If null, then just get all permissions and process on the client.
   */
  private String permissionProcessor;
  
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

  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
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

  /** find assignments in this attribute def (optional) */
  private String wsAttributeDefName;
  
  
  
  /**
   * find assignments in this attribute def (optional)
   * @return attribute def name
   */
  public String getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }
  
  /**
   * find assignments in this attribute def (optional)
   * @param wsAttributeDefName1
   */
  public void setWsAttributeDefName(String wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * find assignments in this attribute def (optional)
   * @return attribute def
   */
  public String getWsAttributeDefId() {
    return this.wsAttributeDefId;
  }

  /**
   * find assignments in this attribute def (optional)
   * @param wsAttributeDefId1
   */
  public void setWsAttributeDefId(String wsAttributeDefId1) {
    this.wsAttributeDefId = wsAttributeDefId1;
  }
  
  /** find assignments in this attribute def (optional) */
  private String wsAttributeDefId;
  
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

  /** is role to look in */
  private String roleName;
  
  /** is role to look in */
  private String roleId;
  
  
  
  /**
   * is role to look in
   * @return group name
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * is role to look in
   * @param wsOwnerGroupName1
   */
  public void setRoleName(String wsOwnerGroupName1) {
    this.roleName = wsOwnerGroupName1;
  }

  /**
   * is role to look in
   * @return group id
   */
  public String getRoleId() {
    return this.roleId;
  }

  /**
   * is role to look in
   * @param wsOwnerGroupId1
   */
  public void setRoleId(String wsOwnerGroupId1) {
    this.roleId = wsOwnerGroupId1;
  }

  /** is subject to look in */
  private String wsSubjectId;

  /** is subject to look in */
  private String wsSubjectSourceId;
  
  /** is subject to look in */
  private String wsSubjectIdentifier;
  
  
  
  /**
   * is subject to look in
   * @return subject
   */
  public String getWsSubjectId() {
    return this.wsSubjectId;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectId1
   */
  public void setWsSubjectId(String wsOwnerSubjectId1) {
    this.wsSubjectId = wsOwnerSubjectId1;
  }

  /**
   * is subject to look in
   * @return subject
   */
  public String getWsSubjectSourceId() {
    return this.wsSubjectSourceId;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectSourceId1
   */
  public void setWsSubjectSourceId(String wsOwnerSubjectSourceId1) {
    this.wsSubjectSourceId = wsOwnerSubjectSourceId1;
  }

  /**
   * is subject to look in
   * @return subject
   */
  public String getWsSubjectIdentifier() {
    return this.wsSubjectIdentifier;
  }

  /**
   * is subject to look in
   * @param wsOwnerSubjectIdentifier1
   */
  public void setWsSubjectIdentifier(String wsOwnerSubjectIdentifier1) {
    this.wsSubjectIdentifier = wsOwnerSubjectIdentifier1;
  }

  /** action to query, or none to query all actions */
  private String action;
  
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

  /** if this is not querying assignments on assignments directly, but the assignments  
   * and assignments on those assignments should be returned, enter true.  default to false.*/
  private String includeAssignmentsOnAssignments;
  
  /**
   * if this is not querying assignments on assignments directly, but the assignments
   *  and assignments on those assignments should be returned, enter true.  default to false.
   * @return include assignment
   */
  public String getIncludeAssignmentsOnAssignments() {
    return this.includeAssignmentsOnAssignments;
  }

  /**
   * if this is not querying assignments on assignments directly, but the assignments
   *  and assignments on those assignments should be returned, enter true.  default to false.
   * @param includeAssignmentsOnAssignments1
   */
  public void setIncludeAssignmentsOnAssignments(String includeAssignmentsOnAssignments1) {
    this.includeAssignmentsOnAssignments = includeAssignmentsOnAssignments1;
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

  /** is A for all, T or null for enabled only, F for disabled  */
  private String enabled;

  /** T or F for it attribute assignments should be returned */
  private String includeAttributeAssignments;

  /** T or F for if attributeDefName objects should be returned */
  private String includeAttributeDefNames;

  /** T or F for if the permission details should be returned */
  private String includePermissionAssignDetail;

  /**
   * is A for all, T or null for enabled only, F for disabled 
   * @return enabled
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * is A for all, T or null for enabled only, F for disabled 
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * T or F for it attribute assignments should be returned
   * @return include attribute assignments
   */
  public String getIncludeAttributeAssignments() {
    return this.includeAttributeAssignments;
  }

  /**
   * T or F for if attributeDefName objects should be returned
   * @return the attributeDefName
   */
  public String getIncludeAttributeDefNames() {
    return this.includeAttributeDefNames;
  }

  /**
   * T or F for if the permission details should be returned
   * @return T or F
   */
  public String getIncludePermissionAssignDetail() {
    return this.includePermissionAssignDetail;
  }

  /**
   * T or F for it attribute assignments should be returned
   * @param includeAttributeAssignments1
   */
  public void setIncludeAttributeAssignments(String includeAttributeAssignments1) {
    this.includeAttributeAssignments = includeAttributeAssignments1;
  }

  /**
   * T or F for if attributeDefName objects should be returned
   * @param includeAttributeDefNames1
   */
  public void setIncludeAttributeDefNames(String includeAttributeDefNames1) {
    this.includeAttributeDefNames = includeAttributeDefNames1;
  }

  /**
   * T or F for if the permission details should be returned
   * @param includePermissionAssignDetail1
   */
  public void setIncludePermissionAssignDetail(String includePermissionAssignDetail1) {
    this.includePermissionAssignDetail = includePermissionAssignDetail1;
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

  
}

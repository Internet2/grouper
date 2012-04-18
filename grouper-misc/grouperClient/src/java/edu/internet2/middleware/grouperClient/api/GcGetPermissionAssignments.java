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
/*
 * @author mchyzer
 * $Id: GcGetMemberships.java,v 1.1 2009-12-19 21:38:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionEnvVar;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get permission assignments web service call
 */
public class GcGetPermissionAssignments {

  /**
   * T of F (defaults to F) if we should filter out non immediate permissions
   */
  private boolean immediateOnly;
  
  /**
   * T of F (defaults to F) if we should filter out non immediate permissions
   * @param theImmediateOnly
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignImmediateOnly(boolean theImmediateOnly) {
    this.immediateOnly = theImmediateOnly;
    return this;
  }
  
  /**
   * are we looking for role permissions or subject permissions?  from
   * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
   */
  private String permissionType;
  
  /**
   * are we looking for role permissions or subject permissions?  from
   * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
   * @param thePermissionType
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignPermissionType(String thePermissionType) {
    this.permissionType = thePermissionType;
    return this;
  }
  
  /**
   * if we should find the best answer, or process limits, etc.  From the enum
   * PermissionProcessor.  example values are: FILTER_REDUNDANT_PERMISSIONS, 
   * FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS, PROCESS_LIMITS
   */
  private String permissionProcessor;
  
  /**
   * if we should find the best answer, or process limits, etc.  From the enum
   * PermissionProcessor.  example values are: FILTER_REDUNDANT_PERMISSIONS, 
   * FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS, PROCESS_LIMITS
   * @param thePermissionProcessor
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignPermissionProcessor(String thePermissionProcessor) {
    this.permissionProcessor = thePermissionProcessor;
    return this;
  }

  /**
   * limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private List<WsPermissionEnvVar> permissionEnvVars = new ArrayList<WsPermissionEnvVar>();

  /**
   * limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param wsPermissionEnvVar
   * @return this for chaining
   */
  public GcGetPermissionAssignments addPermissionEnvVar(WsPermissionEnvVar wsPermissionEnvVar) {
    this.permissionEnvVars.add(wsPermissionEnvVar);
    return this;
  }
  
  /**
   * limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param envVarName
   * @param envVarValue
   * @param envVarType
   * @return this for chaining
   */
  public GcGetPermissionAssignments addPermissionEnvVar(String envVarName, String envVarValue, String envVarType) {
    this.permissionEnvVars.add(new WsPermissionEnvVar(envVarName, envVarValue, envVarType));
    return this;
  }

  /** A for all, T or null for enabled only, F for disabled only */
  private String enabled;
  
  /** ws subject lookups to find memberships about */
  private Set<WsSubjectLookup> subjectLookups = new LinkedHashSet<WsSubjectLookup>();
  
  /** client version */
  private String clientVersion;

  /**
   * if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.   
   */
  private Boolean includeAssignmentsOnAssignments;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   */
  private Timestamp pointInTimeFrom;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   */
  private Timestamp pointInTimeTo;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * @param pointInTimeFrom
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignPointInTimeFrom(Timestamp pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
    return this;
  }
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   * @param pointInTimeTo
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * 
   * @param theIncludeAssignmentsOnAssignments
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludeAssignmentsOnAssignments(Boolean theIncludeAssignmentsOnAssignments) {
    this.includeAssignmentsOnAssignments = theIncludeAssignmentsOnAssignments;
    return this;
  }
  
  /** to query, or none to query all actions */
  private Set<String> actions = new LinkedHashSet<String>();
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcGetPermissionAssignments addAction(String action) {
    this.actions.add(action);
    return this;
  }
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group names to query */
  private Set<String> roleNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> roleUuids = new LinkedHashSet<String>();
  
  /**
   * set the role name
   * @param theRoleName
   * @return this for chaining
   */
  public GcGetPermissionAssignments addRoleName(String theRoleName) {
    this.roleNames.add(theRoleName);
    return this;
  }
  
  /**
   * set the subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetPermissionAssignments addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /**
   * set the role uuid
   * @param theRoleUuid
   * @return this for chaining
   */
  public GcGetPermissionAssignments addRoleUuid(String theRoleUuid) {
    this.roleUuids.add(theRoleUuid);
    return this;
  }
  
  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetPermissionAssignments addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetPermissionAssignments addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    
    if (pointInTimeFrom != null || pointInTimeTo != null) {
      if (this.includeGroupDetail != null && this.includeGroupDetail) {
        throw new RuntimeException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (this.enabled != null && !this.enabled.equals("T")) {
        throw new RuntimeException("Cannot search for disabled memberships for point in time queries.");
      }
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** attributeDef names to query */
  private Set<String> attributeDefNames = new LinkedHashSet<String>();

  /** attributeDef uuids to query */
  private Set<String> attributeDefUuids = new LinkedHashSet<String>();

  /** attributeDefName names to query */
  private Set<String> attributeDefNameNames = new LinkedHashSet<String>();

  /** attributeDefName uuids to query */
  private Set<String> attributeDefNameUuids = new LinkedHashSet<String>();

  /** T or F for it attribute assignments should be returned */
  private Boolean includeAttributeAssignments;

  /**
   * T or F for it attribute assignments should be returned
   * @param theIncludeAttributeAssignments
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludeAttributeAssignments(Boolean theIncludeAttributeAssignments) {
    this.includeAttributeAssignments = theIncludeAttributeAssignments;
    return this;
  }
  

  /** T or F for if attributeDefName objects should be returned */
  private Boolean includeAttributeDefNames;

  /**
   * T or F for if attributeDefName objects should be returned
   * @param theIncludeAttributeDefNames
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludeAttributeDefNames(Boolean theIncludeAttributeDefNames) {
    this.includeAttributeDefNames = theIncludeAttributeDefNames;
    return this;
  }
  


  /** T or F for if the permission details should be returned */
  private Boolean includePermissionAssignDetail;
  
  /**
   * T or F for if the permission details should be returned
   * @param theIncludePermissionAssignDetail
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludePermissionAssignDetail(Boolean theIncludePermissionAssignDetail) {
    this.includePermissionAssignDetail = theIncludePermissionAssignDetail;
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetPermissionAssignments addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetPermissionAssignmentsResults execute() {
    this.validate();
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetPermissionAssignmentsRequest getPermissionAssignments = new WsRestGetPermissionAssignmentsRequest();

      getPermissionAssignments.setActAsSubjectLookup(this.actAsSubject);

      getPermissionAssignments.setEnabled(this.enabled);
      
      //########### ATTRIBUTE DEFS
      List<WsAttributeDefLookup> attributeDefLookups = new ArrayList<WsAttributeDefLookup>();
      //add names and/or uuids
      for (String attributeDefName : this.attributeDefNames) {
        attributeDefLookups.add(new WsAttributeDefLookup(attributeDefName, null));
      }
      for (String attributeDefUuid : this.attributeDefUuids) {
        attributeDefLookups.add(new WsAttributeDefLookup(null, attributeDefUuid));
      }
      if (GrouperClientUtils.length(attributeDefLookups) > 0) {
        getPermissionAssignments.setWsAttributeDefLookups(GrouperClientUtils.toArray(attributeDefLookups, WsAttributeDefLookup.class));
      }

      //########### ATTRIBUTE DEF NAMES
      List<WsAttributeDefNameLookup> attributeDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();
      //add names and/or uuids
      for (String attributeDefNameName : this.attributeDefNameNames) {
        attributeDefNameLookups.add(new WsAttributeDefNameLookup(attributeDefNameName, null));
      }
      for (String attributeDefNameUuid : this.attributeDefNameUuids) {
        attributeDefNameLookups.add(new WsAttributeDefNameLookup(null, attributeDefNameUuid));
      }
      if (GrouperClientUtils.length(attributeDefNameLookups) > 0) {
        getPermissionAssignments.setWsAttributeDefNameLookups(GrouperClientUtils.toArray(attributeDefNameLookups, WsAttributeDefNameLookup.class));
      }

      //########### GROUPS
      List<WsGroupLookup> roleLookups = new ArrayList<WsGroupLookup>();
      //add names and/or uuids
      for (String ownerGroupName : this.roleNames) {
        roleLookups.add(new WsGroupLookup(ownerGroupName, null));
      }
      for (String ownerGroupUuid : this.roleUuids) {
        roleLookups.add(new WsGroupLookup(null, ownerGroupUuid));
      }
      if (GrouperClientUtils.length(roleLookups) > 0) {
        getPermissionAssignments.setRoleLookups(GrouperClientUtils.toArray(roleLookups, WsGroupLookup.class));
      }


      //############# SUBJECTS
      if (GrouperClientUtils.length(this.subjectLookups) > 0) {
        getPermissionAssignments.setWsSubjectLookups(GrouperClientUtils.toArray(this.subjectLookups, WsSubjectLookup.class));
      }
      
      
      if (this.includeAssignmentsOnAssignments != null) {
        getPermissionAssignments.setIncludeAssignmentsOnAssignments(this.includeAssignmentsOnAssignments ? "T" : "F");
      }
      
      if (this.includeAttributeAssignments != null) {
        getPermissionAssignments.setIncludeAttributeAssignments(this.includeAttributeAssignments ? "T" : "F");
      }

      if (this.includeAttributeDefNames != null) {
        getPermissionAssignments.setIncludeAttributeDefNames(this.includeAttributeDefNames ? "T" : "F");
      }

      if (this.includeGroupDetail != null) {
        getPermissionAssignments.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includePermissionAssignDetail != null) {
        getPermissionAssignments.setIncludePermissionAssignDetail(this.includePermissionAssignDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getPermissionAssignments.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      
      //add params if there are any
      if (this.params.size() > 0) {
        getPermissionAssignments.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getPermissionAssignments.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      if (GrouperClientUtils.length(this.actions) > 0) {
        getPermissionAssignments.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      }
      
      getPermissionAssignments.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      getPermissionAssignments.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      if (this.immediateOnly) {
        getPermissionAssignments.setImmediateOnly(this.immediateOnly ? "T" : "F");
      }
      
      if (!GrouperClientUtils.isBlank(this.permissionType)) {
        getPermissionAssignments.setPermissionType(this.permissionType);
      }
      
      if (!GrouperClientUtils.isBlank(this.permissionProcessor)) {
        getPermissionAssignments.setPermissionProcessor(this.permissionProcessor);
      }

      if (this.permissionEnvVars.size() > 0) {
        getPermissionAssignments.setLimitEnvVars(GrouperClientUtils.toArray(this.permissionEnvVars, WsPermissionEnvVar.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetPermissionAssignmentsResults = (WsGetPermissionAssignmentsResults)
        grouperClientWs.executeService("permissionAssignments", 
            getPermissionAssignments, "getPermissionAssignments", this.clientVersion, true);
      
      String resultMessage = wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetPermissionAssignmentsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetPermissionAssignmentsResults;
    
  }

  /**
   * assign A for all, T or null for enabled only, F for disabled only
   * @param theEnabled
   * @return this for chaining
   */
  public GcGetPermissionAssignments assignEnabled(String theEnabled) {
    this.enabled = theEnabled;
    return this;
  }


  /**
   * set the attributedef name
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcGetPermissionAssignments addAttributeDefName(String theAttributeDefName) {
    this.attributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attributedef uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcGetPermissionAssignments addAttributeDefUuid(String theAttributeDefUuid) {
    this.attributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the attributeDefName name
   * @param theAttributeDefNameName
   * @return this for chaining
   */
  public GcGetPermissionAssignments addAttributeDefNameName(String theAttributeDefNameName) {
    this.attributeDefNameNames.add(theAttributeDefNameName);
    return this;
  }

  /**
   * set the attributeDefName uuid
   * @param theAttributeDefNameUuid
   * @return this for chaining
   */
  public GcGetPermissionAssignments addAttributeDefNameUuid(String theAttributeDefNameUuid) {
    this.attributeDefNameUuids.add(theAttributeDefNameUuid);
    return this;
  }
  
}

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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get attribute assignments web service call
 */
public class GcGetAttributeAssignments {

  /**
   * required if sending theValue, can be:
   * floating|integer|memberId|string|timestamp
   */
  private String attributeDefValueType; 
   
  /**
   * value if you are passing in one attributeDefNameLookup
   */
  private String value; 
    
  /**
   * T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   */
  private Boolean includeAssignmentsFromAssignments;
   
  /**
   * null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   */
  private String attributeDefType;
   
  /**
   * if looking for assignments on assignments, this is the assignment the assignment is assigned to
   */
  private Set<WsAttributeAssignLookup> assignAssignOwnerAttributeAssignLookups = new LinkedHashSet<WsAttributeAssignLookup>();
   
  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   */
  private Set<String> assignAssignOwnerNamesOfAttributeDefs = new LinkedHashSet<String>();
   
  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   */
  private Set<String> assignAssignOwnerUuidsOfAttributeDefs = new LinkedHashSet<String>();
  
  /**
   * if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   */
  private Set<Long> assignAssignOwnerIdIndexesOfAttributeDefs = new LinkedHashSet<Long>();
  
  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   */
  private Set<String> assignAssignOwnerNamesOfAttributeDefNames = new LinkedHashSet<String>();
   
  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   */
  private Set<String> assignAssignOwnerUuidsOfAttributeDefNames = new LinkedHashSet<String>();
   
  /**
   * if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   */
  private Set<Long> assignAssignOwnerIdIndexesOfAttributeDefNames = new LinkedHashSet<Long>();
   
  /**
   * if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to
   */
  private Set<String> assignAssignOwnerActions = new LinkedHashSet<String>();
                                                                                                                                                                                                                                                                                                                                                                                                                                                           
  
  /** A for all, T or null for enabled only, F for disabled only */
  private String enabled;
  
  /** ws subject lookups to find memberships about */
  private Set<WsSubjectLookup> ownerSubjectLookups = new LinkedHashSet<WsSubjectLookup>();
  
  /** client version */
  private String clientVersion;

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   */
  private String attributeAssignType;
  
  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignAttributeAssignType(String theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
//  * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
//  * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)

  
  

  /**
   * if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.   
   */
  private Boolean includeAssignmentsOnAssignments;
  
  /**
   * 
   * @param theIncludeAssignmentsOnAssignments
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignIncludeAssignmentsOnAssignments(Boolean theIncludeAssignmentsOnAssignments) {
    this.includeAssignmentsOnAssignments = theIncludeAssignmentsOnAssignments;
    return this;
  }
  
  /**
   * 
   * @param theIncludeAssignmentsFromAssignments
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignIncludeAssignmentsFromAssignments(Boolean theIncludeAssignmentsFromAssignments) {
    this.includeAssignmentsFromAssignments = theIncludeAssignmentsFromAssignments;
    return this;
  }
  
  /**
   * 
   * @param theAttributeDefType
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignAttributeDefType(String theAttributeDefType) {
    this.attributeDefType = theAttributeDefType;
    return this;
  }
  
  /**
   * 
   * @param theAttributeDefValueType
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignAttributeDefValueType(String theAttributeDefValueType) {
    this.attributeDefValueType = theAttributeDefValueType;
    return this;
  }
  
  /**
   * 
   * @param theValue
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignValue(Object theValue) {
    this.value = GrouperClientUtils.stringValue(theValue);
    return this;
  }
  
  /** to query, or none to query all actions */
  private Set<String> actions = new LinkedHashSet<String>();
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAction(String action) {
    this.actions.add(action);
    return this;
  }
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerAction(String action) {
    this.assignAssignOwnerActions.add(action);
    return this;
  }
  

  
  /**
   * 
   * @param assignAssignOwnerAttributeAssignLookup
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerAttributeAssignLookup(WsAttributeAssignLookup assignAssignOwnerAttributeAssignLookup) {
    this.assignAssignOwnerAttributeAssignLookups.add(assignAssignOwnerAttributeAssignLookup);
    return this;
  }
  
  /**
   * add a assign assign owner attribute assign id lookup
   * @param attributeAssignId id
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerAttributeAssignId(String attributeAssignId) {
    WsAttributeAssignLookup wsAttributeAssignLookup = new WsAttributeAssignLookup();
    wsAttributeAssignLookup.setUuid(attributeAssignId);
    this.assignAssignOwnerAttributeAssignLookups.add(wsAttributeAssignLookup);
    return this;
  }

  /**
   * 
   * @param assignAssignOwnerNamesOfAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerNameOfAttributeDefName(String assignAssignOwnerNamesOfAttributeDefName) {
    this.assignAssignOwnerNamesOfAttributeDefNames.add(assignAssignOwnerNamesOfAttributeDefName);
    return this;
  }
  
  /**
   * 
   * @param assignAssignOwnerNamesOfAttributeDefs
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerNameOfAttributeDef(String assignAssignOwnerNamesOfAttributeDef) {
    this.assignAssignOwnerNamesOfAttributeDefs.add(assignAssignOwnerNamesOfAttributeDef);
    return this;
  }
  
  /**
   * 
   * @param assignAssignOwnerUuidsOfAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerUuidOfAttributeDefName(String assignAssignOwnerUuidsOfAttributeDefName) {
    this.assignAssignOwnerUuidsOfAttributeDefNames.add(assignAssignOwnerUuidsOfAttributeDefName);
    return this;
  }
  
  /**
   * 
   * @param assignAssignOwnerUuidsOfAttributeDef
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerUuidOfAttributeDef(String assignAssignOwnerUuidsOfAttributeDef) {
    this.assignAssignOwnerUuidsOfAttributeDefs.add(assignAssignOwnerUuidsOfAttributeDef);
    return this;
  }
  
  /**
   * 
   * @param assignAssignOwnerIdIndexOfAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerIdIndexOfAttributeDefName(Long assignAssignOwnerIdIndexOfAttributeDefName) {
    this.assignAssignOwnerIdIndexesOfAttributeDefNames.add(assignAssignOwnerIdIndexOfAttributeDefName);
    return this;
  }
  
  /**
   * 
   * @param assignAssignOwnerIdIndexOfAttributeDef
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAssignAssignOwnerIdIndexOfAttributeDef(Long assignAssignOwnerIdIndexOfAttributeDef) {
    this.assignAssignOwnerIdIndexesOfAttributeDefs.add(assignAssignOwnerIdIndexOfAttributeDef);
    return this;
  }
  
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group names to query */
  private Set<String> ownerGroupNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> ownerGroupUuids = new LinkedHashSet<String>();
  
  /** group idIndexes to query */
  private Set<Long> ownerGroupIdIndexes = new LinkedHashSet<Long>();
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerGroupName(String theGroupName) {
    this.ownerGroupNames.add(theGroupName);
    return this;
  }
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerGroupIdIndex(Long theGroupIdIndex) {
    this.ownerGroupIdIndexes.add(theGroupIdIndex);
    return this;
  }
  
  /**
   * set the subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.ownerSubjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerGroupUuid(String theGroupUuid) {
    this.ownerGroupUuids.add(theGroupUuid);
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
  public GcGetAttributeAssignments addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetAttributeAssignments addParam(WsParam wsParam) {
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
  public GcGetAttributeAssignments assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.attributeAssignType)) {
      throw new RuntimeException("attributeAssignType is required: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** stem names to query */
  private Set<String> ownerStemNames = new LinkedHashSet<String>();

  /** stem uuids to query */
  private Set<String> ownerStemUuids = new LinkedHashSet<String>();

  /** stem id indexes to query */
  private Set<Long> ownerStemIdIndexes = new LinkedHashSet<Long>();

  /** attribute def names to query */
  private Set<String> ownerAttributeDefNames = new LinkedHashSet<String>();

  /** attribute def uuids to query */
  private Set<String> ownerAttributeDefUuids = new LinkedHashSet<String>();

  /** attribute def id indexes to query */
  private Set<Long> ownerAttributeDefIdIndexes = new LinkedHashSet<Long>();

  /** owner membership any lookup */
  private Set<WsMembershipAnyLookup> ownerMembershipAnyLookups = new LinkedHashSet<WsMembershipAnyLookup>();
  
  /** owner membership lookup */
  private Set<WsMembershipLookup> ownerMembershipLookups = new LinkedHashSet<WsMembershipLookup>();
  
  /** owner membership lookup */
  private Set<WsAttributeAssignLookup> attributeAssignLookups = new LinkedHashSet<WsAttributeAssignLookup>();

  /** attributeDef names to query */
  private Set<String> attributeDefNames = new LinkedHashSet<String>();

  /** attributeDef uuids to query */
  private Set<String> attributeDefUuids = new LinkedHashSet<String>();

  /** attributeDef id indexes to query */
  private Set<Long> attributeDefIdIndexes = new LinkedHashSet<Long>();

  /** attributeDefName names to query */
  private Set<String> attributeDefNameNames = new LinkedHashSet<String>();

  /** attributeDefName uuids to query */
  private Set<String> attributeDefNameUuids = new LinkedHashSet<String>();
  
  /** attributeDefName id indexes to query */
  private Set<Long> attributeDefNameIdIndexes = new LinkedHashSet<Long>();
  
  
  
  /**
   * add a membership any lookup
   * @param wsMembershipAnyLookup
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerMembershipAnyLookup(WsMembershipAnyLookup wsMembershipAnyLookup) {
    this.ownerMembershipAnyLookups.add(wsMembershipAnyLookup);
    return this;
  }
  
  /**
   * add a membership id lookup for owner
   * @param membershipId id (uuid or immediate)
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerMembershipId(String membershipId) {
    WsMembershipLookup wsMembershipLookup = new WsMembershipLookup();
    wsMembershipLookup.setUuid(membershipId);
    this.ownerMembershipLookups.add(wsMembershipLookup);
    return this;
  }
  
  /**
   * add a attribute assign id lookup
   * @param attributeAssignId id
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeAssignId(String attributeAssignId) {
    WsAttributeAssignLookup wsAttributeAssignLookup = new WsAttributeAssignLookup();
    wsAttributeAssignLookup.setUuid(attributeAssignId);
    this.attributeAssignLookups.add(wsAttributeAssignLookup);
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetAttributeAssignmentsResults execute() {
    this.validate();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetAttributeAssignmentsRequest getAttributeAssignments = new WsRestGetAttributeAssignmentsRequest();

      getAttributeAssignments.setActAsSubjectLookup(this.actAsSubject);

      getAttributeAssignments.setEnabled(this.enabled);
      
      {
        //########### ATTRIBUTE DEFS
        List<WsAttributeDefLookup> attributeDefLookups = new ArrayList<WsAttributeDefLookup>();
        //add names and/or uuids
        for (String attributeDefName : this.attributeDefNames) {
          attributeDefLookups.add(new WsAttributeDefLookup(attributeDefName, null));
        }
        for (String attributeDefUuid : this.attributeDefUuids) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, attributeDefUuid));
        }
        for (Long attributeDefIdIndex : this.attributeDefIdIndexes) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, null, attributeDefIdIndex.toString()));
        }
        if (GrouperClientUtils.length(attributeDefLookups) > 0) {
          getAttributeAssignments.setWsAttributeDefLookups(GrouperClientUtils.toArray(attributeDefLookups, WsAttributeDefLookup.class));
        }
      }
      
      {
        //########### ATTRIBUTE DEF NAMES
        List<WsAttributeDefNameLookup> attributeDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();
        //add names and/or uuids
        for (String attributeDefNameName : this.attributeDefNameNames) {
          attributeDefNameLookups.add(new WsAttributeDefNameLookup(attributeDefNameName, null));
        }
        for (String attributeDefNameUuid : this.attributeDefNameUuids) {
          attributeDefNameLookups.add(new WsAttributeDefNameLookup(null, attributeDefNameUuid));
        }
        for (Long attributeDefNameIdIndex : this.attributeDefNameIdIndexes) {
          attributeDefNameLookups.add(new WsAttributeDefNameLookup(null, null, attributeDefNameIdIndex.toString()));
        }
        if (GrouperClientUtils.length(attributeDefNameLookups) > 0) {
          getAttributeAssignments.setWsAttributeDefNameLookups(GrouperClientUtils.toArray(attributeDefNameLookups, WsAttributeDefNameLookup.class));
        }
      }
      
      //########### GROUPS
      List<WsGroupLookup> ownerGroupLookups = new ArrayList<WsGroupLookup>();
      //add names and/or uuids
      for (String ownerGroupName : this.ownerGroupNames) {
        ownerGroupLookups.add(new WsGroupLookup(ownerGroupName, null));
      }
      for (String ownerGroupUuid : this.ownerGroupUuids) {
        ownerGroupLookups.add(new WsGroupLookup(null, ownerGroupUuid));
      }
      for (Long ownerGroupIdIndex : this.ownerGroupIdIndexes) {
        ownerGroupLookups.add(new WsGroupLookup(null, null, ownerGroupIdIndex.toString()));
      }
      if (GrouperClientUtils.length(ownerGroupLookups) > 0) {
        getAttributeAssignments.setWsOwnerGroupLookups(GrouperClientUtils.toArray(ownerGroupLookups, WsGroupLookup.class));
      }

      //############# STEMS
      List<WsStemLookup> ownerStemLookups = new ArrayList<WsStemLookup>();
      //add names and/or uuids
      for (String ownerStemName : this.ownerStemNames) {
        ownerStemLookups.add(new WsStemLookup(ownerStemName, null));
      }
      for (String ownerStemUuid : this.ownerStemUuids) {
        ownerStemLookups.add(new WsStemLookup(null, ownerStemUuid));
      }
      for (Long ownerStemIdIndex : this.ownerStemIdIndexes) {
        ownerStemLookups.add(new WsStemLookup(null, null, ownerStemIdIndex.toString()));
      }
      if (GrouperClientUtils.length(ownerStemLookups) > 0) {
        getAttributeAssignments.setWsOwnerStemLookups(GrouperClientUtils.toArray(ownerStemLookups, WsStemLookup.class));
      }

      //############# SUBJECTS
      if (GrouperClientUtils.length(this.ownerSubjectLookups) > 0) {
        getAttributeAssignments.setWsOwnerSubjectLookups(GrouperClientUtils.toArray(this.ownerSubjectLookups, WsSubjectLookup.class));
      }
      
      //############# MEMBERSHIP ANY LOOKUPS
      if (GrouperClientUtils.length(this.ownerMembershipAnyLookups) > 0) {
        getAttributeAssignments.setWsOwnerMembershipAnyLookups(GrouperClientUtils.toArray(this.ownerMembershipAnyLookups, WsMembershipAnyLookup.class));
      }
      
      //############# MEMBERSHIPS
      if (GrouperClientUtils.length(this.ownerMembershipLookups) > 0) {
        getAttributeAssignments.setWsOwnerMembershipLookups(GrouperClientUtils.toArray(this.ownerMembershipLookups, WsMembershipLookup.class));
      }
      
      //############# ATTRIBUTE DEFS
      List<WsAttributeDefLookup> ownerAttributeDefLookups = new ArrayList<WsAttributeDefLookup>();
      //add names and/or uuids
      for (String ownerAttributeDefName : this.ownerAttributeDefNames) {
        ownerAttributeDefLookups.add(new WsAttributeDefLookup(ownerAttributeDefName, null));
      }
      for (String ownerAttributeDefUuid : this.ownerAttributeDefUuids) {
        ownerAttributeDefLookups.add(new WsAttributeDefLookup(null, ownerAttributeDefUuid));
      }
      for (Long ownerAttributeDefIdIndex : this.ownerAttributeDefIdIndexes) {
        ownerAttributeDefLookups.add(new WsAttributeDefLookup(null, null, ownerAttributeDefIdIndex.toString()));
      }
      if (GrouperClientUtils.length(ownerAttributeDefLookups) > 0) {
        getAttributeAssignments.setWsOwnerAttributeDefLookups(GrouperClientUtils.toArray(ownerAttributeDefLookups, WsAttributeDefLookup.class));
      }
      
      if (this.includeAssignmentsOnAssignments != null) {
        getAttributeAssignments.setIncludeAssignmentsOnAssignments(this.includeAssignmentsOnAssignments ? "T" : "F");
        
      }
      
      if (this.includeGroupDetail != null) {
        getAttributeAssignments.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getAttributeAssignments.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (GrouperClientUtils.length(this.attributeAssignLookups) > 0) {
        getAttributeAssignments.setWsAttributeAssignLookups(GrouperClientUtils.toArray(
            this.attributeAssignLookups, WsAttributeAssignLookup.class));
      }

      
      getAttributeAssignments.setAttributeAssignType(this.attributeAssignType);
      
      if (!GrouperClientUtils.isBlank(this.attributeDefValueType)) {
        getAttributeAssignments.setAttributeDefValueType(this.attributeDefValueType);
      }
       
      if (!GrouperClientUtils.isBlank(this.value)) {
        getAttributeAssignments.setTheValue(this.value);
      }
       
      if (this.includeAssignmentsFromAssignments != null) {
        getAttributeAssignments.setIncludeAssignmentsFromAssignments(this.includeAssignmentsFromAssignments ? "T" : "F");
      }
      
      if (!GrouperClientUtils.isBlank(this.attributeDefType)) {
        getAttributeAssignments.setAttributeDefType(this.attributeDefType);
      }
      
      if (this.assignAssignOwnerAttributeAssignLookups.size() > 0) {
        getAttributeAssignments.setWsAssignAssignOwnerAttributeAssignLookups(GrouperClientUtils.toArray(
            this.assignAssignOwnerAttributeAssignLookups, WsAttributeAssignLookup.class));
      }
      
      {
        //############# ASSIGN ASSIGN OWNER ATTRIBUTE DEFS
        List<WsAttributeDefLookup> ownerAssignAssignOwnerAttributeDefLookups = new ArrayList<WsAttributeDefLookup>();
        //add names and/or uuids
        for (String assignAssignOwnerNameOfAttributeDef : this.assignAssignOwnerNamesOfAttributeDefs) {
          ownerAssignAssignOwnerAttributeDefLookups.add(new WsAttributeDefLookup(assignAssignOwnerNameOfAttributeDef, null));
        }
        for (String assignAssignOwnerUuidOfAttributeDef : this.assignAssignOwnerUuidsOfAttributeDefs) {
          ownerAssignAssignOwnerAttributeDefLookups.add(new WsAttributeDefLookup(null, assignAssignOwnerUuidOfAttributeDef));
        }
        for (Long assignAssignOwnerIdIndexOfAttributeDef : this.assignAssignOwnerIdIndexesOfAttributeDefs) {
          ownerAssignAssignOwnerAttributeDefLookups.add(new WsAttributeDefLookup(null, null, assignAssignOwnerIdIndexOfAttributeDef.toString()));
        }
        if (GrouperClientUtils.length(ownerAssignAssignOwnerAttributeDefLookups) > 0) {
          getAttributeAssignments.setWsAssignAssignOwnerAttributeDefLookups(GrouperClientUtils.toArray(ownerAssignAssignOwnerAttributeDefLookups, WsAttributeDefLookup.class));
        }
      }
      
      if (this.assignAssignOwnerActions.size() > 0) {
        getAttributeAssignments.setWsAssignAssignOwnerActions(GrouperClientUtils.toArray(this.assignAssignOwnerActions, String.class));
      }

      {
        //############# ASSIGN ASSIGN OWNER ATTRIBUTE DEF NAMES
        List<WsAttributeDefNameLookup> ownerAssignAssignOwnerAttributeDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();
        //add names and/or uuids
        for (String assignAssignOwnerNameOfAttributeDefName : this.assignAssignOwnerNamesOfAttributeDefNames) {
          ownerAssignAssignOwnerAttributeDefNameLookups.add(new WsAttributeDefNameLookup(assignAssignOwnerNameOfAttributeDefName, null));
        }
        for (String assignAssignOwnerUuidOfAttributeDefName : this.assignAssignOwnerUuidsOfAttributeDefNames) {
          ownerAssignAssignOwnerAttributeDefNameLookups.add(new WsAttributeDefNameLookup(null, assignAssignOwnerUuidOfAttributeDefName));
        }
        for (Long assignAssignOwnerIdIndexOfAttributeDefName : this.assignAssignOwnerIdIndexesOfAttributeDefNames) {
          ownerAssignAssignOwnerAttributeDefNameLookups.add(new WsAttributeDefNameLookup(null, null, assignAssignOwnerIdIndexOfAttributeDefName.toString()));
        }
        if (GrouperClientUtils.length(ownerAssignAssignOwnerAttributeDefNameLookups) > 0) {
          getAttributeAssignments.setWsAssignAssignOwnerAttributeDefNameLookups(GrouperClientUtils.toArray(ownerAssignAssignOwnerAttributeDefNameLookups, WsAttributeDefNameLookup.class));
        }
      }
       

      
      //add params if there are any
      if (this.params.size() > 0) {
        getAttributeAssignments.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getAttributeAssignments.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      if (GrouperClientUtils.length(this.actions) > 0) {
        getAttributeAssignments.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetAttributeAssignmentsResults = (WsGetAttributeAssignmentsResults)
        grouperClientWs.executeService("attributeAssignments", 
            getAttributeAssignments, "getAttributeAssignments", this.clientVersion, true);
      
      String resultMessage = wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetAttributeAssignmentsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetAttributeAssignmentsResults;
    
  }

  /**
   * assign A for all, T or null for enabled only, F for disabled only
   * @param theEnabled
   * @return this for chaining
   */
  public GcGetAttributeAssignments assignEnabled(String theEnabled) {
    this.enabled = theEnabled;
    return this;
  }

  /**
   * set the stem name
   * @param theStemName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerStemName(String theStemName) {
    this.ownerStemNames.add(theStemName);
    return this;
  }

  /**
   * set the stem uuid
   * @param theStemUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerStemUuid(String theStemUuid) {
    this.ownerStemUuids.add(theStemUuid);
    return this;
  }

  /**
   * set the stem id index
   * @param theStemIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerStemIdIndex(Long theStemIdIndex) {
    this.ownerStemIdIndexes.add(theStemIdIndex);
    return this;
  }

  /**
   * set the attribute def name
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerAttributeDefName(String theAttributeDefName) {
    this.ownerAttributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attribute def uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerAttributeDefUuid(String theAttributeDefUuid) {
    this.ownerAttributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the attribute def uuid
   * @param theAttributeDefIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignments addOwnerAttributeDefIdIndex(Long theAttributeDefIdIndex) {
    this.ownerAttributeDefIdIndexes.add(theAttributeDefIdIndex);
    return this;
  }

  /**
   * set the attributedef name
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefName(String theAttributeDefName) {
    this.attributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attributedef uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefUuid(String theAttributeDefUuid) {
    this.attributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the attributedef id index
   * @param theAttributeDefIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefIdIndex(Long theAttributeDefIdIndex) {
    this.attributeDefIdIndexes.add(theAttributeDefIdIndex);
    return this;
  }

  /**
   * set the attributeDefName name
   * @param theAttributeDefNameName
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefNameName(String theAttributeDefNameName) {
    this.attributeDefNameNames.add(theAttributeDefNameName);
    return this;
  }

  /**
   * set the attributeDefName uuid
   * @param theAttributeDefNameUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefNameUuid(String theAttributeDefNameUuid) {
    this.attributeDefNameUuids.add(theAttributeDefNameUuid);
    return this;
  }
  

  /**
   * set the attributeDefName id index
   * @param theAttributeDefNameIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignments addAttributeDefNameIdIndex(Long theAttributeDefNameIdIndex) {
    this.attributeDefNameIdIndexes.add(theAttributeDefNameIdIndex);
    return this;
  }
}

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
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run an assign attributes web service call
 */
public class GcAssignAttributes {

  /** disabled time, or null for not disabled */
  private Timestamp assignmentDisabledTime;
  
  /** enabled time, or null enabled */
  private Timestamp assignmentEnabledTime;
  
  /** notes on the assignment (optional) */
  private String assignmentNotes;
  
  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   */
  private String attributeAssignOperation;
  
  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   */
  private String attributeAssignValueOperation;
  
  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   */
  private String delegatable;
  
  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   */
  private List<WsAttributeAssignValue> values = new ArrayList<WsAttributeAssignValue>();
  
  /**
   * for assignment on assignment
   */
  private Set<WsAttributeAssignLookup> ownerAttributeAssignLookups = new LinkedHashSet<WsAttributeAssignLookup>();
  
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
  public GcAssignAttributes assignAttributeAssignType(String theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
//  * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
//  * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)

  
  

  /** to query, or none to query all actions */
  private Set<String> actions = new LinkedHashSet<String>();
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcAssignAttributes addAction(String action) {
    this.actions.add(action);
    return this;
  }
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignAttributes assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group names to query */
  private Set<String> ownerGroupNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> ownerGroupUuids = new LinkedHashSet<String>();
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerGroupName(String theGroupName) {
    this.ownerGroupNames.add(theGroupName);
    return this;
  }
  
  /**
   * set the subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.ownerSubjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerGroupUuid(String theGroupUuid) {
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
  public GcAssignAttributes addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignAttributes addParam(WsParam wsParam) {
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
  public GcAssignAttributes assignActAsSubject(WsSubjectLookup theActAsSubject) {
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

  /** attribute def names to query */
  private Set<String> ownerAttributeDefNames = new LinkedHashSet<String>();

  /** attribute def uuids to query */
  private Set<String> ownerAttributeDefUuids = new LinkedHashSet<String>();

  /** owner membership any lookup */
  private Set<WsMembershipAnyLookup> ownerMembershipAnyLookups = new LinkedHashSet<WsMembershipAnyLookup>();
  
  /** owner membership lookup */
  private Set<WsMembershipLookup> ownerMembershipLookups = new LinkedHashSet<WsMembershipLookup>();
  
  /** owner membership lookup */
  private Set<WsAttributeAssignLookup> attributeAssignLookups = new LinkedHashSet<WsAttributeAssignLookup>();

  /** attributeDefName names to query */
  private Set<String> attributeDefNameNames = new LinkedHashSet<String>();

  /** attributeDefName uuids to query */
  private Set<String> attributeDefNameUuids = new LinkedHashSet<String>();
  
  
  /** attributeDef names to replace */
  private Set<String> attributeDefNamesToReplace = new LinkedHashSet<String>();

  /** attributeDef uuids to replace */
  private Set<String> attributeDefUuidsToReplace = new LinkedHashSet<String>();

  //  * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
  //  * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)
  
    
    
  
    /** to replace only certain actions */
    private Set<String> actionsToReplace = new LinkedHashSet<String>();
  
  
  /**
   * add a membership any lookup
   * @param wsMembershipAnyLookup
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerMembershipAnyLookup(WsMembershipAnyLookup wsMembershipAnyLookup) {
    this.ownerMembershipAnyLookups.add(wsMembershipAnyLookup);
    return this;
  }
  
  /**
   * add a membership id lookup for owner
   * @param membershipId id (uuid or immediate)
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerMembershipId(String membershipId) {
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
  public GcAssignAttributes addAttributeAssignId(String attributeAssignId) {
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
  public GcAssignAttributes addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAssignAttributes assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAssignAttributes assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAssignAttributesResults execute() {
    this.validate();
    WsAssignAttributesResults wsAssignAttributesResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignAttributesRequest assignAttributes = new WsRestAssignAttributesRequest();

      assignAttributes.setActAsSubjectLookup(this.actAsSubject);

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
        assignAttributes.setWsAttributeDefNameLookups(GrouperClientUtils.toArray(attributeDefNameLookups, WsAttributeDefNameLookup.class));
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
      if (GrouperClientUtils.length(ownerGroupLookups) > 0) {
        assignAttributes.setWsOwnerGroupLookups(GrouperClientUtils.toArray(ownerGroupLookups, WsGroupLookup.class));
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
      if (GrouperClientUtils.length(ownerStemLookups) > 0) {
        assignAttributes.setWsOwnerStemLookups(GrouperClientUtils.toArray(ownerStemLookups, WsStemLookup.class));
      }

      //############# SUBJECTS
      if (GrouperClientUtils.length(this.ownerSubjectLookups) > 0) {
        assignAttributes.setWsOwnerSubjectLookups(GrouperClientUtils.toArray(this.ownerSubjectLookups, WsSubjectLookup.class));
      }
      
      //############# MEMBERSHIP ANY LOOKUPS
      if (GrouperClientUtils.length(this.ownerMembershipAnyLookups) > 0) {
        assignAttributes.setWsOwnerMembershipAnyLookups(GrouperClientUtils.toArray(this.ownerMembershipAnyLookups, WsMembershipAnyLookup.class));
      }
      
      //############# MEMBERSHIPS
      if (GrouperClientUtils.length(this.ownerMembershipLookups) > 0) {
        assignAttributes.setWsOwnerMembershipLookups(GrouperClientUtils.toArray(this.ownerMembershipLookups, WsMembershipLookup.class));
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
      if (GrouperClientUtils.length(ownerAttributeDefLookups) > 0) {
        assignAttributes.setWsOwnerAttributeDefLookups(GrouperClientUtils.toArray(ownerAttributeDefLookups, WsAttributeDefLookup.class));
      }

      //############# VALUES
      if (GrouperClientUtils.length(this.values) > 0) {
        assignAttributes.setValues(GrouperClientUtils.toArray(this.values, WsAttributeAssignValue.class));
      }

      //############# VALUES
      if (GrouperClientUtils.length(this.ownerAttributeAssignLookups) > 0) {
        assignAttributes.setWsOwnerAttributeAssignLookups(GrouperClientUtils.toArray(this.ownerAttributeAssignLookups, WsAttributeAssignLookup.class));
      }

      //############# REPLACE STUFF
      if (GrouperClientUtils.length(this.actionsToReplace) > 0) {
        assignAttributes.setActionsToReplace(GrouperClientUtils.toArray(this.actionsToReplace, String.class));
      }
      if (GrouperClientUtils.length(this.attributeDefTypesToReplace) > 0) {
        assignAttributes.setAttributeDefTypesToReplace(GrouperClientUtils.toArray(this.attributeDefTypesToReplace, String.class));
      }
      List<WsAttributeDefLookup> attributeDefLookupsToReplace = new ArrayList<WsAttributeDefLookup>();
      //add names and/or uuids
      for (String attributeDefNameToReplace : this.attributeDefNamesToReplace) {
        attributeDefLookupsToReplace.add(new WsAttributeDefLookup(attributeDefNameToReplace, null));
      }
      for (String attributeDefUuidToReplace : this.attributeDefUuidsToReplace) {
        attributeDefLookupsToReplace.add(new WsAttributeDefLookup(null, attributeDefUuidToReplace));
      }
      if (GrouperClientUtils.length(attributeDefLookupsToReplace) > 0) {
        assignAttributes.setAttributeDefsToReplace(GrouperClientUtils.toArray(attributeDefLookupsToReplace, WsAttributeDefLookup.class));
      }
      
      
      if (this.includeGroupDetail != null) {
        assignAttributes.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        assignAttributes.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.assignmentDisabledTime != null) {
        String disabledTime = GrouperClientUtils.dateToString(this.assignmentDisabledTime);
        assignAttributes.setAssignmentDisabledTime(disabledTime);
      }

      if (this.assignmentEnabledTime != null) {
        String enabledTime = GrouperClientUtils.dateToString(this.assignmentEnabledTime);
        assignAttributes.setAssignmentEnabledTime(enabledTime);
      }

      assignAttributes.setAssignmentNotes(this.assignmentNotes);
      assignAttributes.setAttributeAssignOperation(this.attributeAssignOperation);
      assignAttributes.setAttributeAssignValueOperation(this.attributeAssignValueOperation);
      assignAttributes.setDelegatable(this.delegatable);
      
      if (GrouperClientUtils.length(this.attributeAssignLookups) > 0) {
        assignAttributes.setWsAttributeAssignLookups(GrouperClientUtils.toArray(
            this.attributeAssignLookups, WsAttributeAssignLookup.class));
      }

      
      assignAttributes.setAttributeAssignType(this.attributeAssignType);
      
      //add params if there are any
      if (this.params.size() > 0) {
        assignAttributes.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        assignAttributes.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      if (GrouperClientUtils.length(this.actions) > 0) {
        assignAttributes.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsAssignAttributesResults = (WsAssignAttributesResults)
        grouperClientWs.executeService("attributeAssignments", assignAttributes, "assignAttributes", this.clientVersion, false);
      
      String resultMessage = wsAssignAttributesResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAssignAttributesResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignAttributesResults;
    
  }

  /**
   * set the stem name
   * @param theStemName
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerStemName(String theStemName) {
    this.ownerStemNames.add(theStemName);
    return this;
  }

  /**
   * set the stem uuid
   * @param theStemUuid
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerStemUuid(String theStemUuid) {
    this.ownerStemUuids.add(theStemUuid);
    return this;
  }

  /**
   * set the attribute def name
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerAttributeDefName(String theAttributeDefName) {
    this.ownerAttributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attribute def uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerAttributeDefUuid(String theAttributeDefUuid) {
    this.ownerAttributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the attributeDefName name
   * @param theAttributeDefNameName
   * @return this for chaining
   */
  public GcAssignAttributes addAttributeDefNameName(String theAttributeDefNameName) {
    this.attributeDefNameNames.add(theAttributeDefNameName);
    return this;
  }

  /**
   * set the attributeDefName uuid
   * @param theAttributeDefNameUuid
   * @return this for chaining
   */
  public GcAssignAttributes addAttributeDefNameUuid(String theAttributeDefNameUuid) {
    this.attributeDefNameUuids.add(theAttributeDefNameUuid);
    return this;
  }

  /**
   * disabled time, or null for not disabled
   * @param theDisabledTime
   * @return this for chaining
   */
  public GcAssignAttributes assignDisabledTime(Timestamp theDisabledTime) {
    this.assignmentDisabledTime = theDisabledTime;
    return this;
  }

  /**
   * enabled time, or null for enabled
   * @param theEnabledTime
   * @return this for chaining
   */
  public GcAssignAttributes assignEnabledTime(Timestamp theEnabledTime) {
    this.assignmentEnabledTime = theEnabledTime;
    return this;
  }

  /**
   * notes on the assignment (optional)
   * @param theAssignmentNotes
   * @return this for chaining
   */
  public GcAssignAttributes assignAssignmentNotes(String theAssignmentNotes) {
    this.assignmentNotes = theAssignmentNotes;
    return this;
  }

  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param theAttributeAssignOperation
   * @return this for chaining
   */
  public GcAssignAttributes assignAttributeAssignOperation(String theAttributeAssignOperation) {
    this.attributeAssignOperation = theAttributeAssignOperation;
    return this;
  }

  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param theAttributeAssignValueOperation
   * @return this for chaining
   */
  public GcAssignAttributes assignAttributeAssignValueOperation(String theAttributeAssignValueOperation) {
    this.attributeAssignValueOperation = theAttributeAssignValueOperation;
    return this;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param theDelegatable
   * @return this for chaining
   */
  public GcAssignAttributes assignDelegatable(String theDelegatable) {
    this.delegatable = theDelegatable;
    return this;
  }

  /**
   * add a membership any lookup
   * @param wsAttributeAssignValue
   * @return this for chaining
   */
  public GcAssignAttributes addValue(WsAttributeAssignValue wsAttributeAssignValue) {
    this.values.add(wsAttributeAssignValue);
    return this;
  }

  /**
   * for assignments on assignments
   * @param wsAttributeAssignLookup
   * @return this for chaining
   */
  public GcAssignAttributes addOwnerAttributeAssignLookup(WsAttributeAssignLookup wsAttributeAssignLookup) {
    this.ownerAttributeAssignLookups.add(wsAttributeAssignLookup);
    return this;
  }

  /**
   * set the attributeDef name to replace
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcAssignAttributes addAttributeDefNameToReplace(String theAttributeDefName) {
    this.attributeDefNamesToReplace.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attributeDef uuid to replace
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcAssignAttributes addAttributeDefUuidToReplace(String theAttributeDefUuid) {
    this.attributeDefUuidsToReplace.add(theAttributeDefUuid);
    return this;
  }

  /**
   * actions to replace
   * @param action
   * @return this for chaining
   */
  public GcAssignAttributes addActionToReplace(String action) {
    this.actionsToReplace.add(action);
    return this;
  }

  /** attribute def types to replace */
  private Set<String> attributeDefTypesToReplace = new LinkedHashSet<String>();
  
  /**
   * attribute def types to replace
   * @param attributeDefTypeToReplace
   * @return this for chaining
   */
  public GcAssignAttributes addAttributeDefTypeToReplace(String attributeDefTypeToReplace) {
    this.attributeDefTypesToReplace.add(attributeDefTypeToReplace);
    return this;
  }
}

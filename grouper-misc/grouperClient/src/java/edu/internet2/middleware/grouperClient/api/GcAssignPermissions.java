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
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run an assign permissions web service call
 */
public class GcAssignPermissions {

  /** disabled time, or null for not disabled */
  private Timestamp assignmentDisabledTime;
  
  /** enabled time, or null enabled */
  private Timestamp assignmentEnabledTime;
  
  /** notes on the assignment (optional) */
  private String assignmentNotes;
  
  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   */
  private String permissionAssignOperation;
  
  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   */
  private String delegatable;
  
  /**
   * really only for permissions, if the assignment is a disallow to override an allow in a wider inherited permission resource
   */
  private Boolean disallowed;
  
  /** client version */
  private String clientVersion;

  /**
   * is role or role_subject from the PermissionType enum
   */
  private String permissionType;
  
  /**
   * is role or role_subject from the PermissionType enum
   * @param thePermissionType
   * @return this for chaining
   */
  public GcAssignPermissions assignPermissionType(String thePermissionType) {
    this.permissionType = thePermissionType;
    return this;
  }
  
  /** to query, or none to query all actions */
  private Set<String> actions = new LinkedHashSet<String>();
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcAssignPermissions addAction(String action) {
    this.actions.add(action);
    return this;
  }
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignPermissions assignClientVersion(String theClientVersion) {
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
  public GcAssignPermissions addRoleName(String theRoleName) {
    this.roleNames.add(theRoleName);
    return this;
  }
  
  /**
   * set the role uuid
   * @param theRoleUuid
   * @return this for chaining
   */
  public GcAssignPermissions addRoleUuid(String theRoleUuid) {
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
  public GcAssignPermissions addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignPermissions addParam(WsParam wsParam) {
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
  public GcAssignPermissions assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.permissionType)) {
      throw new RuntimeException("attributeAssignType is required: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** subjectRole lookup */
  private Set<WsMembershipAnyLookup> subjectRoleLookups = new LinkedHashSet<WsMembershipAnyLookup>();
  
  /** owner membership lookup */
  private Set<WsAttributeAssignLookup> attributeAssignLookups = new LinkedHashSet<WsAttributeAssignLookup>();

  /** attributeDefName names to assign */
  private Set<String> permissionDefNameNames = new LinkedHashSet<String>();

  /** attributeDefName uuids to assign */
  private Set<String> permissionDefNameUuids = new LinkedHashSet<String>();

  //  * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
  //  * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)
  
    
    
  
    /** to replace only certain actions */
    private Set<String> actionsToReplace = new LinkedHashSet<String>();

  /** attributeDef names to replace */
  private Set<String> attributeDefNamesToReplace = new LinkedHashSet<String>();

  /** attributeDef uuids to replace */
  private Set<String> attributeDefUuidsToReplace = new LinkedHashSet<String>();
  
  
  
  /**
   * add a membership any lookup
   * @param subjectRoleLookup
   * @return this for chaining
   */
  public GcAssignPermissions addSubjectRoleLookup(WsMembershipAnyLookup subjectRoleLookup) {
    this.subjectRoleLookups.add(subjectRoleLookup);
    return this;
  }
  
  /**
   * add a attribute assign id lookup
   * @param attributeAssignId id
   * @return this for chaining
   */
  public GcAssignPermissions addAttributeAssignId(String attributeAssignId) {
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
  public GcAssignPermissions addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAssignPermissions assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAssignPermissions assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAssignPermissionsResults execute() {
    this.validate();
    WsAssignPermissionsResults wsAssignPermissionsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignPermissionsRequest assignPermissions = new WsRestAssignPermissionsRequest();

      assignPermissions.setActAsSubjectLookup(this.actAsSubject);

      //########### ATTRIBUTE DEF NAMES
      List<WsAttributeDefNameLookup> permissionDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();
      //add names and/or uuids
      for (String attributeDefNameName : this.permissionDefNameNames) {
        permissionDefNameLookups.add(new WsAttributeDefNameLookup(attributeDefNameName, null));
      }
      for (String attributeDefNameUuid : this.permissionDefNameUuids) {
        permissionDefNameLookups.add(new WsAttributeDefNameLookup(null, attributeDefNameUuid));
      }
      if (GrouperClientUtils.length(permissionDefNameLookups) > 0) {
        assignPermissions.setPermissionDefNameLookups(GrouperClientUtils.toArray(permissionDefNameLookups, WsAttributeDefNameLookup.class));
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
        assignPermissions.setRoleLookups(GrouperClientUtils.toArray(roleLookups, WsGroupLookup.class));
      }

      //############# MEMBERSHIP ANY LOOKUPS
      if (GrouperClientUtils.length(this.subjectRoleLookups) > 0) {
        assignPermissions.setSubjectRoleLookups(GrouperClientUtils.toArray(this.subjectRoleLookups, WsMembershipAnyLookup.class));
      }
      
      //############# REPLACE STUFF
      if (GrouperClientUtils.length(this.actionsToReplace) > 0) {
        assignPermissions.setActionsToReplace(GrouperClientUtils.toArray(this.actionsToReplace, String.class));
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
        assignPermissions.setAttributeDefsToReplace(GrouperClientUtils.toArray(attributeDefLookupsToReplace, WsAttributeDefLookup.class));
      }

      
      if (this.includeGroupDetail != null) {
        assignPermissions.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        assignPermissions.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.assignmentDisabledTime != null) {
        String disabledTime = GrouperClientUtils.dateToString(this.assignmentDisabledTime);
        assignPermissions.setAssignmentDisabledTime(disabledTime);
      }

      if (this.assignmentEnabledTime != null) {
        String enabledTime = GrouperClientUtils.dateToString(this.assignmentEnabledTime);
        assignPermissions.setAssignmentEnabledTime(enabledTime);
      }

      assignPermissions.setAssignmentNotes(this.assignmentNotes);
      assignPermissions.setPermissionAssignOperation(this.permissionAssignOperation);
      assignPermissions.setDelegatable(this.delegatable);

      if (this.disallowed != null) {
        assignPermissions.setDisallowed(this.disallowed ? "T" : "F");
      }

      if (GrouperClientUtils.length(this.attributeAssignLookups) > 0) {
        assignPermissions.setWsAttributeAssignLookups(GrouperClientUtils.toArray(
            this.attributeAssignLookups, WsAttributeAssignLookup.class));
      }

      assignPermissions.setPermissionType(this.permissionType);

      //add params if there are any
      if (this.params.size() > 0) {
        assignPermissions.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      if (this.subjectAttributeNames.size() > 0) {
        assignPermissions.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }

      if (GrouperClientUtils.length(this.actions) > 0) {
        assignPermissions.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAssignPermissionsResults = (WsAssignPermissionsResults)
        grouperClientWs.executeService("permissionAssignments", 
            assignPermissions, "getPermissionAssignments", this.clientVersion, false);

      String resultMessage = wsAssignPermissionsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAssignPermissionsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignPermissionsResults;
    
  }

  /**
   * set the permissionDefName name
   * @param thePermissionDefNameName
   * @return this for chaining
   */
  public GcAssignPermissions addPermissionDefNameName(String thePermissionDefNameName) {
    this.permissionDefNameNames.add(thePermissionDefNameName);
    return this;
  }

  /**
   * set the permissionDefName uuid
   * @param thePermissionDefNameUuid
   * @return this for chaining
   */
  public GcAssignPermissions addPermissionDefNameUuid(String thePermissionDefNameUuid) {
    this.permissionDefNameUuids.add(thePermissionDefNameUuid);
    return this;
  }

  /**
   * disabled time, or null for not disabled
   * @param theDisabledTime
   * @return this for chaining
   */
  public GcAssignPermissions assignDisabledTime(Timestamp theDisabledTime) {
    this.assignmentDisabledTime = theDisabledTime;
    return this;
  }

  /**
   * enabled time, or null for enabled
   * @param theEnabledTime
   * @return this for chaining
   */
  public GcAssignPermissions assignEnabledTime(Timestamp theEnabledTime) {
    this.assignmentEnabledTime = theEnabledTime;
    return this;
  }

  /**
   * notes on the assignment (optional)
   * @param theAssignmentNotes
   * @return this for chaining
   */
  public GcAssignPermissions assignAssignmentNotes(String theAssignmentNotes) {
    this.assignmentNotes = theAssignmentNotes;
    return this;
  }

  /**
   * operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   * @param thePermissionAssignOperation
   * @return this for chaining
   */
  public GcAssignPermissions assignPermissionAssignOperation(String thePermissionAssignOperation) {
    this.permissionAssignOperation = thePermissionAssignOperation;
    return this;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param theDelegatable
   * @return this for chaining
   */
  public GcAssignPermissions assignDelegatable(String theDelegatable) {
    this.delegatable = theDelegatable;
    return this;
  }

  /**
   * really only for permissions, if the assignment is a disallow to override an allow in a wider inherited permission resource
   * @param theDisallowed
   * @return this for chaining
   */
  public GcAssignPermissions assignDisallowed(Boolean theDisallowed) {
    this.disallowed = theDisallowed;
    return this;
  }

  /**
   * actions to replace
   * @param action
   * @return this for chaining
   */
  public GcAssignPermissions addActionToReplace(String action) {
    this.actionsToReplace.add(action);
    return this;
  }

  /**
   * set the attributeDef name to replace
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcAssignPermissions addAttributeDefNameToReplace(String theAttributeDefName) {
    this.attributeDefNamesToReplace.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attributeDef uuid to replace
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcAssignPermissions addAttributeDefUuidToReplace(String theAttributeDefUuid) {
    this.attributeDefUuidsToReplace.add(theAttributeDefUuid);
    return this;
  }
  
}

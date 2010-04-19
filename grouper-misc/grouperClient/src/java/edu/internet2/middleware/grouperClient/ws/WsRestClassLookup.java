/*
 * @author mchyzer
 * $Id: WsRestClassLookup.java,v 1.9 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValuesResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDef;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubjectResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssignDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsResponseMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignPermissionsLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindStemsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetMembersRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetPermissionAssignmentsLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestHasMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestStemSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 *
 */
public class WsRestClassLookup {

  /** map of aliases to classes */
  static Map<String, Class<?>> aliasClassMap = Collections
      .synchronizedMap(new HashMap<String, Class<?>>());

  /** add a bunch of xstream aliases */
  static {
    addAliasClass(WsAddMemberResult.class);
    addAliasClass(WsAddMemberResults.class);
    
    addAliasClass(WsAssignAttributeResult.class);
    addAliasClass(WsAssignAttributesLiteResults.class);
    addAliasClass(WsAssignAttributesResults.class);
    addAliasClass(WsRestAssignAttributesRequest.class);
    addAliasClass(WsRestAssignAttributesLiteRequest.class);
    addAliasClass(WsAttributeAssignValueResult.class);
    addAliasClass(WsAttributeAssignValuesResult.class);
    addAliasClass(WsAttributeAssign.class);
    addAliasClass(WsAttributeAssignLookup.class);
    addAliasClass(WsAttributeAssignValue.class);
    addAliasClass(WsAttributeDef.class);
    addAliasClass(WsAttributeDefLookup.class);
    addAliasClass(WsAttributeDefName.class);
    addAliasClass(WsAttributeDefNameLookup.class);
    addAliasClass(WsAssignGrouperPrivilegesLiteResult.class);
    addAliasClass(WsDeleteMemberResult.class);
    addAliasClass(WsDeleteMemberResults.class);

    
    addAliasClass(WsFindGroupsResults.class);
    addAliasClass(WsFindStemsResults.class);
    addAliasClass(WsGetAttributeAssignmentsResults.class);
    addAliasClass(WsGetGrouperPrivilegesLiteResult.class);

    addAliasClass(WsGetGroupsResult.class);
    addAliasClass(WsGetGroupsResults.class);

    addAliasClass(WsGetMembersResult.class);
    addAliasClass(WsGetMembersResults.class);

    addAliasClass(WsGetMembershipsResults.class);

    addAliasClass(WsGroup.class);
    addAliasClass(WsGroupDeleteResult.class);
    addAliasClass(WsGroupDeleteResults.class);
    
    addAliasClass(WsGroupDetail.class);
    addAliasClass(WsGrouperPrivilegeResult.class);
    addAliasClass(WsGroupLookup.class);
    
    addAliasClass(WsGroupSaveResult.class);
    addAliasClass(WsGroupSaveResults.class);
    addAliasClass(WsGroupToSave.class);
    
    addAliasClass(WsHasMemberResult.class);
    addAliasClass(WsHasMemberResults.class);

    addAliasClass(WsMemberChangeSubject.class);
    addAliasClass(WsMemberChangeSubjectResult.class);
    addAliasClass(WsMemberChangeSubjectResults.class);

    addAliasClass(WsMembership.class);
    addAliasClass(WsMembershipAnyLookup.class);
    addAliasClass(WsMembershipLookup.class);
    
    addAliasClass(WsParam.class);
    addAliasClass(WsQueryFilter.class);

    addAliasClass(WsResponseMeta.class);
    addAliasClass(WsRestAddMemberRequest.class);
    addAliasClass(WsRestAssignGrouperPrivilegesLiteRequest.class);
    addAliasClass(WsRestAssignGrouperPrivilegesRequest.class);
    addAliasClass(WsRestGetAttributeAssignmentsRequest.class);
    addAliasClass(WsAssignGrouperPrivilegesResult.class);
    addAliasClass(WsAssignGrouperPrivilegesResults.class);

    addAliasClass(WsRestGetPermissionAssignmentsLiteRequest.class);
    addAliasClass(WsRestGetPermissionAssignmentsRequest.class);
    addAliasClass(WsGetPermissionAssignmentsResults.class);
    addAliasClass(WsPermissionAssign.class);
    addAliasClass(WsPermissionAssignDetail.class);

    addAliasClass(WsRestAssignPermissionsLiteRequest.class);
    addAliasClass(WsRestAssignPermissionsRequest.class);
    addAliasClass(WsAssignPermissionsLiteResults.class);
    addAliasClass(WsAssignPermissionsResults.class);
    addAliasClass(WsAssignPermissionResult.class);

    addAliasClass(WsRestDeleteMemberRequest.class);
    addAliasClass(WsRestFindGroupsRequest.class);
    addAliasClass(WsRestFindStemsRequest.class);
    
    addAliasClass(WsRestGetGrouperPrivilegesLiteRequest.class);

    addAliasClass(WsRestGetGroupsRequest.class);
    addAliasClass(WsRestGetMembersRequest.class);
    addAliasClass(WsRestGetMembershipsRequest.class);

    addAliasClass(WsRestGetSubjectsRequest.class);
    addAliasClass(WsRestGetSubjectsLiteRequest.class);
    addAliasClass(WsGetSubjectsResults.class);

    addAliasClass(WsRestGroupDeleteRequest.class);
    addAliasClass(WsRestGroupSaveRequest.class);
    addAliasClass(WsRestHasMemberRequest.class);

    addAliasClass(WsRestMemberChangeSubjectRequest.class);
    addAliasClass(WsRestResultProblem.class);

    addAliasClass(WsRestStemDeleteRequest.class);
    addAliasClass(WsRestStemSaveRequest.class);
    
    addAliasClass(WsResultMeta.class);
    
    addAliasClass(WsStem.class);
    addAliasClass(WsStemDeleteResult.class);
    addAliasClass(WsStemDeleteResults.class);
    addAliasClass(WsStemLookup.class);
    addAliasClass(WsStemQueryFilter.class);
    addAliasClass(WsStemSaveResult.class);
    addAliasClass(WsStemSaveResults.class);
    addAliasClass(WsStemToSave.class);
    
    addAliasClass(WsSubject.class);
    addAliasClass(WsSubjectLookup.class);
  }
  
  /**
   * add an alias by class simple name
   * @param theClass
   */
  public static void addAliasClass(Class<?> theClass) {
    synchronized (aliasClassMap) {
      aliasClassMap.put(theClass.getSimpleName(), theClass);
    }
  }

  /**
   * find a class object based on simple name
   * @param simpleClassName
   * @return the class object or null if blank
   */
  public static Class<?> retrieveClassBySimpleName(String simpleClassName) {
    //blank is ok
    if (GrouperClientUtils.isBlank(simpleClassName)) {
      return null;
    }
    Class<?> theClass = aliasClassMap.get(simpleClassName);
    if (theClass != null) {
      return theClass;
    }
    //make a good exception.
    StringBuilder error = new StringBuilder("Cant find class from simple name: '").append(simpleClassName);
    error.append("', expecting one of: ");
    for (String simpleName : aliasClassMap.keySet()) {
      error.append(simpleName).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

  /**
   * map of aliases to classes
   * @return the alias to class map
   */
  public static Map<String, Class<?>> getAliasClassMap() {
    return aliasClassMap;
  }

}

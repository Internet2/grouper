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
 * $Id: WsRestClassLookup.java,v 1.9 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.*;

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
    addAliasClass(WsAssignAttributesBatchResults.class);
    addAliasClass(WsAssignAttributeBatchResult.class);
    addAliasClass(WsAssignAttributeBatchEntry.class);
    addAliasClass(WsAssignAttributesLiteResults.class);
    addAliasClass(WsAssignAttributesResults.class);
    addAliasClass(WsRestAssignAttributesRequest.class);
    addAliasClass(WsRestAssignAttributesBatchRequest.class);
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

    addAliasClass(WsAssignAttributeDefNameInheritanceResults.class);
    addAliasClass(WsAttributeDefNameDeleteResult.class);
    addAliasClass(WsAttributeDefNameDeleteResults.class);
    addAliasClass(WsAttributeDefNameSaveResult.class);
    addAliasClass(WsAttributeDefNameSaveResults.class);
    addAliasClass(WsAttributeDefNameToSave.class);
    addAliasClass(WsFindAttributeDefNamesResults.class);
    addAliasClass(WsRestAssignAttributeDefNameInheritanceRequest.class);
    addAliasClass(WsRestAttributeDefNameDeleteRequest.class);
    addAliasClass(WsRestAttributeDefNameSaveRequest.class);
    addAliasClass(WsRestFindAttributeDefNamesRequest.class);
    
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

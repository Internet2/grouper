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
 * $Id: WsRestClassLookup.java,v 1.12 2009/12/29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.coresoap.*;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefActionsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesBatchRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestExternalSubjectDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestExternalSubjectSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestFindExternalSubjectsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestAcknowledgeMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestReceiveMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestSendMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest;

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
    addAliasClass(WsAddMemberLiteResult.class);
    addAliasClass(WsAttribute.class);
    addAliasClass(WsAttributeAssign.class);
    addAliasClass(WsAttributeAssignLookup.class);
    addAliasClass(WsAttributeAssignValue.class);
    addAliasClass(WsAttributeAssignValueResult.class);
    addAliasClass(WsAttributeDef.class);
    addAliasClass(WsAttributeDefLookup.class);
    addAliasClass(WsAttributeDefName.class);
    addAliasClass(WsAttributeDefNameLookup.class);
    addAliasClass(WsAttributeDefNameToSave.class);
    addAliasClass(WsAttributeDefToSave.class);
    addAliasClass(WsAttributeEdit.class);
    addAliasClass(WsDeleteMemberResult.class);
    addAliasClass(WsDeleteMemberResults.class);
    addAliasClass(WsFindGroupsResults.class);
    addAliasClass(WsFindStemsResults.class);
    addAliasClass(WsGetAttributeAssignmentsResults.class);
    addAliasClass(WsGetGroupsResult.class);
    addAliasClass(WsGetGroupsResults.class);
    addAliasClass(WsGetMembershipsResults.class);
    addAliasClass(WsGetMembersResult.class);
    addAliasClass(WsGetMembersResults.class);
    addAliasClass(WsGetSubjectsResults.class);
    addAliasClass(WsGroup.class);
    addAliasClass(WsGroupDeleteResult.class);
    addAliasClass(WsGroupDeleteLiteResult.class);
    addAliasClass(WsGroupDeleteResults.class);
    addAliasClass(WsGroupDetail.class);
    addAliasClass(WsGroupLookup.class);
    addAliasClass(WsGroupSaveResult.class);
    addAliasClass(WsGroupSaveLiteResult.class);
    addAliasClass(WsGroupSaveResults.class);
    addAliasClass(WsGroupToSave.class);
    addAliasClass(WsHasMemberResult.class);
    addAliasClass(WsHasMemberResults.class);
    addAliasClass(WsMembership.class);
    addAliasClass(WsMembershipAnyLookup.class);
    addAliasClass(WsMembershipLookup.class);

    addAliasClass(WsMemberChangeSubject.class);
    addAliasClass(WsMemberChangeSubjectLiteResult.class);
    addAliasClass(WsMemberChangeSubjectResult.class);
    addAliasClass(WsMemberChangeSubjectResults.class);
    addAliasClass(WsRestGetAttributeAssignmentsRequest.class);
    addAliasClass(WsRestGetAttributeAssignmentsLiteRequest.class);
    addAliasClass(WsRestMemberChangeSubjectRequest.class);
    addAliasClass(WsRestMemberChangeSubjectLiteRequest.class);

    addAliasClass(WsRestFindAttributeDefNamesRequest.class);
    addAliasClass(WsRestFindAttributeDefNamesLiteRequest.class);
    addAliasClass(WsFindAttributeDefNamesResults.class);

    addAliasClass(WsRestAssignAttributeDefNameInheritanceLiteRequest.class);
    addAliasClass(WsRestAssignAttributeDefNameInheritanceRequest.class);
    addAliasClass(WsRestAssignAttributeDefNameInheritanceRequest.class);
    addAliasClass(WsAssignAttributeDefNameInheritanceResults.class);

    addAliasClass(WsRestAttributeDefNameSaveRequest.class);
    addAliasClass(WsRestAttributeDefNameSaveLiteRequest.class);
    addAliasClass(WsAttributeDefNameSaveResults.class);
    addAliasClass(WsAttributeDefNameSaveResult.class);
    addAliasClass(WsAttributeDefNameSaveLiteResult.class);
    
    addAliasClass(WsRestAttributeDefNameDeleteRequest.class);
    addAliasClass(WsRestAttributeDefNameDeleteLiteRequest.class);
    addAliasClass(WsAttributeDefNameDeleteResults.class);
    addAliasClass(WsAttributeDefNameDeleteResult.class);
    addAliasClass(WsAttributeDefNameDeleteLiteResult.class);
    
    addAliasClass(WsRestGetPermissionAssignmentsLiteRequest.class);
    addAliasClass(WsRestGetPermissionAssignmentsRequest.class);
    addAliasClass(WsGetPermissionAssignmentsResults.class);
    addAliasClass(WsPermissionAssign.class);
    addAliasClass(WsPermissionAssignDetail.class);
    
    addAliasClass(WsParam.class);
    addAliasClass(WsGrouperPrivilegeResult.class);
    addAliasClass(WsQueryFilter.class);
    addAliasClass(WsResponseMeta.class);
    addAliasClass(WsResultMeta.class);
    addAliasClass(WsStem.class);
    addAliasClass(WsStemDeleteResult.class);
    addAliasClass(WsStemDeleteResults.class);
    addAliasClass(WsStemDeleteLiteResult.class);
    addAliasClass(WsStemLookup.class);
    addAliasClass(WsStemQueryFilter.class);
    addAliasClass(WsStemSaveResults.class);
    addAliasClass(WsStemSaveResult.class);
    addAliasClass(WsStemSaveLiteResult.class);
    addAliasClass(WsStemToSave.class);
    addAliasClass(WsSubject.class);
    addAliasClass(WsSubjectLookup.class);

    addAliasClass(WsRestAssignAttributesLiteRequest.class);
    addAliasClass(WsRestAssignAttributesBatchRequest.class);
    addAliasClass(WsAssignAttributeBatchEntry.class);
    addAliasClass(WsRestAssignAttributesRequest.class);
    addAliasClass(WsAssignAttributesLiteResults.class);
    addAliasClass(WsAssignAttributesResults.class);
    addAliasClass(WsAssignAttributesBatchResults.class);
    addAliasClass(WsAssignAttributeBatchResult.class);
    addAliasClass(WsAssignAttributeResult.class);
    
    addAliasClass(WsRestAssignPermissionsLiteRequest.class);
    addAliasClass(WsRestAssignPermissionsRequest.class);
    addAliasClass(WsAssignPermissionsLiteResults.class);
    addAliasClass(WsAssignPermissionsResults.class);
    addAliasClass(WsAssignPermissionResult.class);
    
    addAliasClass(WsRestGetGrouperPrivilegesLiteRequest.class);
    addAliasClass(WsRestAssignGrouperPrivilegesLiteRequest.class);
    addAliasClass(WsGetGrouperPrivilegesLiteResult.class);
    addAliasClass(WsAssignGrouperPrivilegesLiteResult.class);
    
    addAliasClass(WsRestAssignGrouperPrivilegesRequest.class);
    addAliasClass(WsAssignGrouperPrivilegesResults.class);
    addAliasClass(WsAssignGrouperPrivilegesResult.class);
    
    addAliasClass(WsRestGetMembersLiteRequest.class);
    addAliasClass(WsRestAddMemberRequest.class);
    addAliasClass(WsRestAddMemberLiteRequest.class);
    addAliasClass(WsRestResultProblem.class);
    
    addAliasClass(WsRestDeleteMemberLiteRequest.class);
    addAliasClass(WsRestDeleteMemberRequest.class);
    addAliasClass(WsDeleteMemberLiteResult.class);

    addAliasClass(WsRestHasMemberLiteRequest.class);
    addAliasClass(WsRestHasMemberRequest.class);
    addAliasClass(WsHasMemberLiteResult.class);

    addAliasClass(WsRestGetGroupsLiteRequest.class);
    addAliasClass(WsRestGetGroupsRequest.class);
    addAliasClass(WsGetGroupsLiteResult.class);

    addAliasClass(WsRestGetMembersLiteRequest.class);
    addAliasClass(WsRestGetMembersRequest.class);
    addAliasClass(WsGetMembersLiteResult.class);
  
    addAliasClass(WsRestGetMembershipsLiteRequest.class);
    addAliasClass(WsRestGetMembershipsRequest.class);
    addAliasClass(WsGetMembershipsResults.class);
  
    addAliasClass(WsRestGetSubjectsLiteRequest.class);
    addAliasClass(WsRestGetSubjectsRequest.class);

    addAliasClass(WsRestFindGroupsLiteRequest.class);
    addAliasClass(WsRestFindGroupsRequest.class);

    addAliasClass(WsRestFindStemsLiteRequest.class);
    addAliasClass(WsRestFindStemsRequest.class);
    
    addAliasClass(WsRestGroupDeleteLiteRequest.class);
    addAliasClass(WsRestGroupDeleteRequest.class);

    addAliasClass(WsRestStemDeleteLiteRequest.class);
    addAliasClass(WsRestStemDeleteRequest.class);

    addAliasClass(WsRestStemSaveLiteRequest.class);
    addAliasClass(WsRestStemSaveRequest.class);

    addAliasClass(WsRestGroupSaveLiteRequest.class);
    addAliasClass(WsRestGroupSaveRequest.class);
    
    addAliasClass(WsRestGetAttributeAssignActionsLiteRequest.class);
    addAliasClass(WsRestGetAttributeAssignActionsRequest.class);
    addAliasClass(WsGetAttributeAssignActionsResults.class);
    addAliasClass(WsAttributeAssignActionTuple.class);
    
    addAliasClass(WsRestAssignAttributeDefActionsRequest.class);
    addAliasClass(WsAttributeDefAssignActionResults.class);
    addAliasClass(WsAttributeDefActionOperationPerformed.class);
    
    addAliasClass(WsRestAttributeDefDeleteRequest.class);
    addAliasClass(WsRestAttributeDefDeleteLiteRequest.class);
    addAliasClass(WsAttributeDefDeleteResult.class);
    addAliasClass(WsAttributeDefDeleteResults.class);
    addAliasClass(WsAttributeDefDeleteLiteResult.class);

    addAliasClass(WsRestFindAttributeDefsRequest.class);
    addAliasClass(WsRestFindAttributeDefsLiteRequest.class);
    addAliasClass(WsFindAttributeDefsResults.class);
    
    addAliasClass(WsRestAttributeDefSaveRequest.class);
    addAliasClass(WsRestAttributeDefSaveLiteRequest.class);
    addAliasClass(WsAttributeDefSaveResults.class);
    addAliasClass(WsAttributeDefSaveResult.class);
    addAliasClass(WsAttributeDefSaveLiteResult.class);
    
    addAliasClass(WsRestSendMessageRequest.class);
    addAliasClass(WsRestReceiveMessageRequest.class);
    addAliasClass(WsRestAcknowledgeMessageRequest.class);
    addAliasClass(WsMessage.class);
    addAliasClass(WsMessageResults.class);
    addAliasClass(WsMessageAcknowledgeResults.class);

    addAliasClass(WsExternalSubject.class);
    addAliasClass(WsExternalSubjectAttribute.class);
    addAliasClass(WsExternalSubjectDeleteResult.class);
    addAliasClass(WsExternalSubjectDeleteResults.class);
    addAliasClass(WsExternalSubjectLookup.class);
    addAliasClass(WsExternalSubjectSaveResult.class);
    addAliasClass(WsExternalSubjectSaveResults.class);
    addAliasClass(WsExternalSubjectToSave.class);
    addAliasClass(WsFindExternalSubjectsResults.class);
    addAliasClass(WsRestFindExternalSubjectsRequest.class);
    addAliasClass(WsRestExternalSubjectDeleteRequest.class);
    addAliasClass(WsRestExternalSubjectSaveRequest.class);

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
   * find a class object based on simple name, but put any errors to a warnings stringbuilder
   * @param simpleClassName
   * @param warnings is where to add error message instead of exception
   * @return the class object or null if blank
   * @throws WsInvalidQueryException if there is an invalid entry
   */
  public static Class<?> retrieveClassBySimpleName(String simpleClassName, 
      StringBuilder warnings) {
    try {
      return retrieveClassBySimpleName(simpleClassName);
    } catch (WsInvalidQueryException wsiq) {
      warnings.append(wsiq.getMessage());
      return null;
    }
  }
  
  /**
   * find a class object based on simple name
   * @param simpleClassName
   * @return the class object or null if blank
   * @throws WsInvalidQueryException if there is an invalid entry
   */
  public static Class<?> retrieveClassBySimpleName(String simpleClassName) {
    //blank is ok
    if (StringUtils.isBlank(simpleClassName)) {
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
    throw new GrouperRestInvalidRequest(error.toString());
  }

  /**
   * map of aliases to classes
   * @return the alias to class map
   */
  public static Map<String, Class<?>> getAliasClassMap() {
    return aliasClassMap;
  }

}

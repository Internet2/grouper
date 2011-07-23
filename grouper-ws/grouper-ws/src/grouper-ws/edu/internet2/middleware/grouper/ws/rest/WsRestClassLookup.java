/*
 * @author mchyzer
 * $Id: WsRestClassLookup.java,v 1.12 2009/12/29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;
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
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttribute;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeDef;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubjectResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMembership;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsPermissionAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsPermissionAssignDetail;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStem;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemSaveResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup;

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
    addAliasClass(WsRestAssignAttributesRequest.class);
    addAliasClass(WsAssignAttributesLiteResults.class);
    addAliasClass(WsAssignAttributesResults.class);
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

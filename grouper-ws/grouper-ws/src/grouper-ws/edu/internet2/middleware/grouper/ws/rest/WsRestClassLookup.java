/*
 * @author mchyzer
 * $Id: WsRestClassLookup.java,v 1.12 2009/12/29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttribute;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDef;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembership;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsPermissionAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsPermissionAssignDetail;
import edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.coresoap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.coresoap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.coresoap.WsStem;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesRequest;
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

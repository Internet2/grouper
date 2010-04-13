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
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAttribute;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDef;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResult;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.soap.WsMembership;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap.WsStem;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap.WsSubject;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;

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

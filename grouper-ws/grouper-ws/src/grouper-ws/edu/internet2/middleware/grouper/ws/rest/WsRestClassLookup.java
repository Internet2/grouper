/*
 * @author mchyzer
 * $Id: WsRestClassLookup.java,v 1.5 2008-03-29 10:50:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAttribute;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsMembership;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsPrivilege;
import edu.internet2.middleware.grouper.ws.soap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap.WsStem;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap.WsSubject;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResult;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditPrivilegesResults;


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
    addAliasClass(WsAttributeEdit.class);
    addAliasClass(WsDeleteMemberResult.class);
    addAliasClass(WsDeleteMemberResults.class);
    addAliasClass(WsFindGroupsResults.class);
    addAliasClass(WsFindStemsResults.class);
    addAliasClass(WsGetGroupsResult.class);
    addAliasClass(WsGetGroupsResults.class);
    addAliasClass(WsGetMembershipsResults.class);
    addAliasClass(WsGetMembersResult.class);
    addAliasClass(WsGetMembersResults.class);
    addAliasClass(WsGroup.class);
    addAliasClass(WsGroupDeleteResult.class);
    addAliasClass(WsGroupDeleteResults.class);
    addAliasClass(WsGroupDetail.class);
    addAliasClass(WsGroupLookup.class);
    addAliasClass(WsGroupSaveResult.class);
    addAliasClass(WsGroupSaveResults.class);
    addAliasClass(WsGroupToSave.class);
    addAliasClass(WsHasMemberResult.class);
    addAliasClass(WsHasMemberResults.class);
    addAliasClass(WsMembership.class);
    addAliasClass(WsParam.class);
    addAliasClass(WsPrivilege.class);
    addAliasClass(WsQueryFilter.class);
    addAliasClass(WsResponseMeta.class);
    addAliasClass(WsResultMeta.class);
    addAliasClass(WsStem.class);
    addAliasClass(WsStemDeleteResult.class);
    addAliasClass(WsStemDeleteResults.class);
    addAliasClass(WsStemQueryFilter.class);
    addAliasClass(WsStemToSave.class);
    addAliasClass(WsSubject.class);
    addAliasClass(WsSubjectLookup.class);
    addAliasClass(WsViewOrEditAttributesResult.class);
    addAliasClass(WsViewOrEditAttributesResults.class);
    addAliasClass(WsViewOrEditPrivilegesResult.class);
    addAliasClass(WsViewOrEditPrivilegesResults.class);

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
  
    addAliasClass(WsRestFindGroupsLiteRequest.class);
    addAliasClass(WsRestFindGroupsRequest.class);
    addAliasClass(WsRestFindStemsLiteRequest.class);
    addAliasClass(WsRestFindStemsRequest.class);
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

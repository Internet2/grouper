/*
 * @author mchyzer
 * $Id: WsLiteClassLookup.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteAddMemberRequest;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteAddMemberSimpleRequest;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteGetMembersSimpleRequest;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberSimpleResult;
import edu.internet2.middleware.grouper.ws.soap.WsAttribute;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
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
public class WsLiteClassLookup {

  /** map of aliases to classes */
  static Map<String, Class<?>> aliasClassMap = Collections
      .synchronizedMap(new HashMap<String, Class<?>>());

  /** add a bunch of xstream aliases */
  static {
    addAliasClass(WsAddMemberResult.class);
    addAliasClass(WsAddMemberResults.class);
    addAliasClass(WsAddMemberSimpleResult.class);
    addAliasClass(WsAttribute.class);
    addAliasClass(WsAttributeEdit.class);
    addAliasClass(WsDeleteMemberResult.class);
    addAliasClass(WsDeleteMemberResults.class);
    addAliasClass(WsFindGroupsResults.class);
    addAliasClass(WsFindStemsResults.class);
    addAliasClass(WsGetGroupsResults.class);
    addAliasClass(WsGetMembershipsResults.class);
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
    addAliasClass(WsStemToSave.class);
    addAliasClass(WsSubject.class);
    addAliasClass(WsSubjectLookup.class);
    addAliasClass(WsViewOrEditAttributesResult.class);
    addAliasClass(WsViewOrEditAttributesResults.class);
    addAliasClass(WsViewOrEditPrivilegesResult.class);
    addAliasClass(WsViewOrEditPrivilegesResults.class);

    addAliasClass(WsLiteGetMembersSimpleRequest.class);
    addAliasClass(WsLiteAddMemberRequest.class);
    addAliasClass(WsLiteAddMemberSimpleRequest.class);
    addAliasClass(WsLiteResultProblem.class);
    
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
    throw new GrouperLiteInvalidRequest(error.toString());
  }

  /**
   * map of aliases to classes
   * @return the alias to class map
   */
  public static Map<String, Class<?>> getAliasClassMap() {
    return aliasClassMap;
  }

}

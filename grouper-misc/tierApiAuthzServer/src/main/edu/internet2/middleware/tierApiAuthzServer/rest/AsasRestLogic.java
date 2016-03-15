/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.tierApiAuthzServer.config.TaasWsClientConfig;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasFolderDeleteResponse;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasFolderSaveRequest;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasFolderSaveResponse;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasGroupSearchContainer;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasGroupsMemberSearchContainer;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiFolderInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupsMemberInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasSaveMode;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolder;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiMembershipTypeParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.tierApiAuthzServer.j2ee.AsasHttpServletRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;


/**
 * logic for rest calls
 * @author mchyzer
 *
 */
public class AsasRestLogic {

  /**
   * groups list or search
   * @param params
   * @return the result container
   */
  public static AsasGroupSearchContainer getGroups(Map<String, String> params) {
    
    //lets get the groups interface
    AsasApiGroupInterface asasApiGroupInterface = StandardApiServerUtils.interfaceGroupInstance();
    AsasApiEntityLookup authenticatedSubject = AsasApiEntityLookup.retrieveLoggedInUser();
    
    AsasApiGroupsSearchParam asasApiGroupsSearchParam = new AsasApiGroupsSearchParam();
    
    AsasApiQueryParams asasApiQueryParams = AsasApiQueryParams.convertFromQueryString();
    
    Boolean pagingEnabled = AsasHttpServletRequest.retrieve().getParameterBoolean("pagingEnabled");
    boolean pagingDisabled = pagingEnabled != null && !pagingEnabled;
    
    //lets setup the default
    if (!pagingDisabled && asasApiQueryParams.getLimit() == null) {
      //# if there is no limit set for a group search, this is the limit that will be applied, -1 to not have a limit
      //tierApiAuthzServer.groupsSearch.defaultLimit = 100
      int defaultLimit = StandardApiServerConfig.retrieveConfig().propertyValueInt("tierApiAuthzServer.groupsSearch.defaultLimit", 100);
      if (defaultLimit != -1) {
        asasApiQueryParams.setLimit(Long.valueOf(defaultLimit));
        asasApiQueryParams.setOffset(0L);
      }
    }
    
    //# max number of groups returned from the search.  -1 to not have a limit
    //tierApiAuthzServer.groupsSearch.maxLimit = 1000
    int maxLimit = StandardApiServerConfig.retrieveConfig().propertyValueInt("tierApiAuthzServer.groupsSearch.maxLimit", 1000);

    //lets setup the max limit
    if (maxLimit != -1) {
      if (pagingDisabled || (asasApiQueryParams.getLimit() != null && asasApiQueryParams.getLimit() > maxLimit)) {
        
        asasApiQueryParams.setLimit(Long.valueOf(maxLimit));
        asasApiQueryParams.setOffset(0L);
      }
    }

    if (StandardApiServerUtils.isBlank(asasApiQueryParams.getSortField())) {
      String defaultSortField = StandardApiServerConfig
          .retrieveConfig().propertyValueString("tierApiAuthzServer.groupsSearch.defaultSortField", "name");
      
      asasApiQueryParams.setSortField(defaultSortField);
    }
    
    asasApiGroupsSearchParam.setQueryParams(asasApiQueryParams);
    
    AsasApiGroupsSearchResult asasApiGroupsSearchResult = asasApiGroupInterface.search(authenticatedSubject, asasApiGroupsSearchParam);
    
    AsasGroupSearchContainer asasGroupSearchContainer = new AsasGroupSearchContainer();

    asasGroupSearchContainer.setGroups(AsasApiGroup.convertToArray(asasApiGroupsSearchResult.getGroups()));
    
    //merge in the meta
    AsasApiQueryParams.convertTo(asasApiGroupsSearchResult.getQueryParams(), asasGroupSearchContainer.getMeta());
    
    return asasGroupSearchContainer;
  }

  /**
   * groups list or search
   * @param groupUri 
   * @param entityUri 
   * @param params
   * @return the result container
   */
  public static AsasGroupsMemberSearchContainer getGroupsMember(String groupUri, String entityUri, Map<String, String> params) {

    //lets get the groups interface
    AsasApiGroupsMemberInterface asasApiGroupsMemberInterface = StandardApiServerUtils.interfaceGroupsMemberInstance();
    AsasApiEntityLookup authenticatedSubject = AsasApiEntityLookup.retrieveLoggedInUser();
    
    AsasApiGroupsMemberSearchParam asasApiGroupsMemberSearchParam = new AsasApiGroupsMemberSearchParam();

    AsasHttpServletRequest asasHttpServletRequest = AsasHttpServletRequest.retrieve();
    
    {
      String membershipTypeString = asasHttpServletRequest.getParameter("membershipType");
      AsasApiMembershipTypeParam asasApiMembershipTypeParam = AsasApiMembershipTypeParam.all;
      if (!StringUtils.isBlank(membershipTypeString)) {
        asasApiMembershipTypeParam = AsasApiMembershipTypeParam.valueOfIgnoreCase(membershipTypeString, true);
      }
      asasApiGroupsMemberSearchParam.setMembershipType(asasApiMembershipTypeParam);
    }
    
    AsasApiQueryParams asasApiQueryParams = AsasApiQueryParams.convertFromQueryString();
    
    asasApiGroupsMemberSearchParam.setQueryParams(asasApiQueryParams);
    
    AsasApiGroupLookup asasApiGroupLookup = StandardApiServerUtils.groupConvertUriToLookup(groupUri);
    AsasApiEntityLookup asasApiEntityLookup = StandardApiServerUtils.entityConvertUriToLookup(entityUri);

    asasApiGroupsMemberSearchParam.setAsasApiGroupLookup(asasApiGroupLookup);
    asasApiGroupsMemberSearchParam.setAsasApiEntityLookup(asasApiEntityLookup);
    
    AsasApiGroupsMemberSearchResult asasApiGroupsMemberSearchResult = asasApiGroupsMemberInterface.search(authenticatedSubject, asasApiGroupsMemberSearchParam);

    AsasGroupsMemberSearchContainer asasGroupsMemberSearchContainer = AsasApiGroupsMemberSearchResult
        .convertTo(asasApiGroupsMemberSearchResult);
    
    boolean scimClient = TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean("tierClient.scim", false);
    if (asasApiGroupsMemberSearchResult.getAsasApiUser() != null) {
      asasGroupsMemberSearchContainer.getMeta().setResourceType("User");
      List<String> schemaList = new ArrayList<String>();
      schemaList.add("urn:ietf:params:scim:schemas:core:2.0:User");
      if (!scimClient) {
        schemaList.add("urn:edu:internet2:tier:2.0:User");
      }
      asasGroupsMemberSearchContainer.setSchemas(StandardApiServerUtils.toArray(schemaList, String.class));
    }
    
    if (asasApiGroupsMemberSearchResult.getAsasApiGroup() != null) {
      asasGroupsMemberSearchContainer.getMeta().setResourceType("Group");
      List<String> schemaList = new ArrayList<String>();
      schemaList.add("urn:ietf:params:scim:schemas:core:2.0:Group");
      if (!scimClient) {
        schemaList.add("urn:edu:internet2:tier:2.0:Group");
      }
      asasGroupsMemberSearchContainer.setSchemas(StandardApiServerUtils.toArray(schemaList, String.class));
    }
    
    if (StringUtils.isBlank(asasGroupsMemberSearchContainer.getMeta().getResourceType())) {
      asasGroupsMemberSearchContainer.getMeta().setResourceType("GroupMember");
    }
    
    //merge in the meta
    AsasApiQueryParams.convertTo(asasApiGroupsMemberSearchResult.getQueryParams(), asasGroupsMemberSearchContainer.getMeta());
    
    return asasGroupsMemberSearchContainer;
  }

  /**
   * folder save
   * @param asasFolderSaveRequest 
   * @param folderUri
   * @param params
   * @param putVsPost true for put or false for post
   * @return the result container
   */
  public static AsasFolderSaveResponse folderSave(AsasFolderSaveRequest asasFolderSaveRequest, 
      String folderUri, Map<String, String> params, boolean putVsPost) {

    if (StandardApiServerUtils.isBlank(folderUri)) {
      throw new NullPointerException("Why is folderUri blank?");
    }
    
    //lets get the folders interface
    AsasApiFolderInterface asasApiFolderInterface = StandardApiServerUtils.interfaceFolderInstance();
    AsasApiEntityLookup authenticatedSubject = AsasApiEntityLookup.retrieveLoggedInUser();
    
    AsasApiFolderSaveParam asasApiFolderSaveParam = new AsasApiFolderSaveParam();

    AsasApiFolderLookup asasApiFolderLookup = StandardApiServerUtils.folderConvertUriToLookup(folderUri);
    
    asasApiFolderSaveParam.setFolderLookup(asasApiFolderLookup);

    Boolean createParentFoldersIfNotExist = AsasHttpServletRequest.retrieve().getParameterBoolean("createParentFoldersIfNotExist");

    asasApiFolderSaveParam.setCreateParentFoldersIfNotExist(createParentFoldersIfNotExist);
    
    //setup the insert / update / insert_or_update
    if (putVsPost) {
      String saveMode = AsasHttpServletRequest.retrieve().getParameter("saveMode");
      if (!StandardApiServerUtils.isBlank(saveMode)) {
        if (StandardApiServerUtils.equals("update", saveMode)) {
          asasApiFolderSaveParam.setSaveMode(AsasSaveMode.UPDATE);
        } else {
          throw new AsasRestInvalidRequest("Invalid saveMode, expecting 'update', '" + saveMode + "'", "400", "ERROR_INVALID_PARAM");
        }
      } else {
        asasApiFolderSaveParam.setSaveMode(AsasSaveMode.INSERT_OR_UPDATE);
      }
    } else {
      asasApiFolderSaveParam.setSaveMode(AsasSaveMode.INSERT);
    }
    
    if (asasFolderSaveRequest != null && asasFolderSaveRequest.getFolder() != null) {
      AsasApiFolder asasApiFolder = AsasApiFolder.convertToAsasApiFolder(asasFolderSaveRequest.getFolder());
      asasApiFolderSaveParam.setFolder(asasApiFolder);
    }
    
    //call the logic
    AsasApiFolderSaveResult asasApiFolderSaveResult = asasApiFolderInterface.save(authenticatedSubject, asasApiFolderSaveParam);
    
    AsasFolderSaveResponse asasFolderSaveResponse = new AsasFolderSaveResponse();
    
    //if parent folder doent exist
    if (asasApiFolderSaveResult.getParentFolderDoesntExist() != null && asasApiFolderSaveResult.getParentFolderDoesntExist()) {
      asasFolderSaveResponse.getMeta().setTierHttpStatusCode(400);
      asasFolderSaveResponse.getMeta().setTierResultCode("PARENT_FOLDER_NOT_EXIST");
      asasFolderSaveResponse.getMeta().setTierSuccess(true);
      return asasFolderSaveResponse;
    }
    
    //if insert already there
    if (asasApiFolderSaveResult.getInsertAlreadyExists() != null && asasApiFolderSaveResult.getInsertAlreadyExists()) {
      asasFolderSaveResponse.getMeta().setTierHttpStatusCode(409);
      asasFolderSaveResponse.getMeta().setTierResultCode("FOLDER_EXISTS");
      asasFolderSaveResponse.getMeta().setTierSuccess(false);
      return asasFolderSaveResponse;
    }
    
    //if update not there
    if (asasApiFolderSaveResult.getUpdateDoesntExist() != null && asasApiFolderSaveResult.getUpdateDoesntExist()) {
      asasFolderSaveResponse.getMeta().setTierHttpStatusCode(409);
      asasFolderSaveResponse.getMeta().setTierResultCode("FOLDER_NOT_EXIST");
      asasFolderSaveResponse.getMeta().setTierSuccess(false);
      return asasFolderSaveResponse;
    }
    
    asasFolderSaveResponse.setFolder(AsasApiFolder.convertToAsasFolder(asasApiFolderSaveResult.getFolder()));
    
    asasFolderSaveResponse.setCreated(asasApiFolderSaveResult.getCreated());
    asasFolderSaveResponse.setUpdated(asasApiFolderSaveResult.getUpdated());
    
    if (asasApiFolderSaveResult.getCreated() == null) {
      throw new RuntimeException("Why is the 'created' null in folder save result???");
    }
    
    if (asasFolderSaveResponse.getCreated()) {
      asasFolderSaveResponse.getMeta().setTierHttpStatusCode(201);
      asasFolderSaveResponse.getMeta().setTierResultCode("FOLDER_CREATED");
    } else {
      asasFolderSaveResponse.getMeta().setTierResultCode("FOLDER_UPDATED");
    }
    
    return asasFolderSaveResponse;
  }

  /**
   * folder delete
   * @param folderUri
   * @param params
   * @return the result container
   */
  public static AsasFolderDeleteResponse folderDelete(String folderUri, Map<String, String> params) {

    if (StandardApiServerUtils.isBlank(folderUri)) {
      throw new NullPointerException("Why is folderUri blank?");
    }

    //lets get the folders interface
    AsasApiFolderInterface asasApiFolderInterface = StandardApiServerUtils.interfaceFolderInstance();
    AsasApiEntityLookup authenticatedSubject = AsasApiEntityLookup.retrieveLoggedInUser();
    
    AsasApiFolderDeleteParam asasApiFolderDeleteParam = new AsasApiFolderDeleteParam();

    AsasApiFolderLookup asasApiFolderLookup = StandardApiServerUtils.folderConvertUriToLookup(folderUri);
    
    asasApiFolderDeleteParam.setFolderLookup(asasApiFolderLookup);
    
    Boolean recursive = AsasHttpServletRequest.retrieve().getParameterBoolean("recursive");

    asasApiFolderDeleteParam.setRecursive(recursive);
    
    AsasApiFolderDeleteResult asasApiFolderDeleteResult = asasApiFolderInterface.delete(authenticatedSubject, asasApiFolderDeleteParam);
    
    AsasFolderDeleteResponse asasFolderDeleteResponse = new AsasFolderDeleteResponse();
    
    asasFolderDeleteResponse.setDeleted(asasApiFolderDeleteResult.getDeleted());
    
    asasFolderDeleteResponse.setParentFolderExists(asasApiFolderDeleteResult.getParentFolderExists());
    
    if (asasFolderDeleteResponse.getDeleted() == null) {
      throw new RuntimeException("Why is the 'deleted' null in folder delete result???");
    }
    
    if (asasFolderDeleteResponse.getDeleted()) {
      asasFolderDeleteResponse.getMeta().setTierResultCode("FOLDER_DELETED");
    } else {
      asasFolderDeleteResponse.getMeta().setTierHttpStatusCode(404);
      asasFolderDeleteResponse.getMeta().setTierResultCode("FOLDER_NOT_EXIST");
    }
    
    return asasFolderDeleteResponse;
  }
  
}

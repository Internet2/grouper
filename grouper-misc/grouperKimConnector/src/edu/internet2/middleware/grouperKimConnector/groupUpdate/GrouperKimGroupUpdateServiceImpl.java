/**
 * @author mchyzer
 * $Id: GrouperKimGroupUpdateServiceImpl.java,v 1.6 2009-12-20 18:03:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.groupUpdate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.GroupUpdateService;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * Implementation of the group update service which maps to grouper
 * https://test.kuali.org/rice/rice-api-1.0-javadocs/index.html?org/kuali/rice/kim/service/GroupUpdateService.html
 */
public class GrouperKimGroupUpdateServiceImpl implements GroupUpdateService {

  /**
   * logger
   */
  private static final Logger LOG = Logger.getLogger(GrouperKimGroupUpdateServiceImpl.class);
  
  /**
   * @see org.kuali.rice.kim.service.GroupUpdateService#addGroupToGroup(java.lang.String, java.lang.String)
   * boolean addGroupToGroup(java.lang.String childId,
   *                     java.lang.String parentId)
   *                     throws java.lang.UnsupportedOperationException
   *
   * Adds the group with the id supplied in childId as a member of the group with the id supplied in parentId. 
   */
  public boolean addGroupToGroup(String childId, String parentId)
      throws UnsupportedOperationException {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("childId", childId);
    debugMap.put("parentId", parentId);
    debugMap.put("operation", "addGroupToGroup");
    
    return addMemberHelper(childId, parentId, "g:gsa", debugMap);
  }

  /**
   * add a member to a group
   * @param subjectId
   * @param groupId
   * @param sourceId if applicable, null if not
   * @param debugMap
   * @return true if happened, or false if already existed
   */
  private boolean addMemberHelper(String subjectId, String groupId, String sourceId,
      Map<String, Object> debugMap) {
    boolean hadException = false;
    
    try {
      debugMap.put("subjectId", subjectId);

      subjectId = GrouperKimUtils.translatePrincipalId(subjectId);

      debugMap.put("subjectIdTranslated", subjectId);

      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      wsSubjectLookup.setSubjectId(subjectId);
      if (!GrouperClientUtils.isBlank(sourceId)) {
        wsSubjectLookup.setSubjectSourceId(sourceId);
      } else {
        wsSubjectLookup.setSubjectSourceId(GrouperKimUtils.separateSourceId(subjectId));
      }
      debugMap.put("sourceId", sourceId);

      WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupUuid(groupId)
        .addSubjectLookup(wsSubjectLookup).execute();
      
      //we did one assignment, we have one result
      WsAddMemberResult wsAddMemberResult = wsAddMemberResults.getResults()[0];
      
      String resultCode = wsAddMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was made
      if (GrouperClientUtils.equals("SUCCESS", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //assignment was already made
      if (GrouperClientUtils.equals("SUCCESS_ALREADY_EXISTED", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * boolean addPrincipalToGroup(java.lang.String principalId,
   *                          java.lang.String groupId)
   *                         throws java.lang.UnsupportedOperationException
   *
   *  Add the principal with the given principalId as a member of the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#addPrincipalToGroup(java.lang.String, java.lang.String)
   */
  public boolean addPrincipalToGroup(String principalId, String groupId)
      throws UnsupportedOperationException {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("principalId", principalId);
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "addPrincipalToGroup");

    //lets see if there is a source to use
    String sourceId = GrouperKimUtils.subjectSourceId();
    
    return addMemberHelper(principalId, groupId, sourceId, debugMap);

  }

  /**
   * GroupInfo createGroup(GroupInfo groupInfo)
   *                   throws java.lang.UnsupportedOperationException
   *
   * Creates a new group using the given GroupInfo. 
   * 
   * @see org.kuali.rice.kim.service.GroupUpdateService#createGroup(org.kuali.rice.kim.bo.group.dto.GroupInfo)
   */
  public GroupInfo createGroup(GroupInfo groupInfo) throws UnsupportedOperationException {
    
    return saveGroupHelper(groupInfo, null, "createGroup", "INSERT");
  }

  /**
   * create or update group
   * @param groupInfo
   * @param groupIdToLookup add group id to lookup
   * @param operation is for logging
   * @param saveMode should be a valid SaveMode class (from grouper ws): INSERT, UPDATE, INSERT_OR_UPDATE
   * @return the group info
   * @throws UnsupportedOperationException
   */
  private GroupInfo saveGroupHelper(GroupInfo groupInfo, String groupIdToLookup, String operation, String saveMode) throws UnsupportedOperationException {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    boolean hadException = false;
    
    try {
      
      debugMap.put("operation", operation);
      debugMap.put("groupInfo.isActive", groupInfo.isActive());
      debugMap.put("groupInfo.getGroupDescription", groupInfo.getGroupDescription());
      debugMap.put("groupInfo.getGroupIdOrig", groupInfo.getGroupId());
      debugMap.put("groupInfo.getGroupName", groupInfo.getGroupName());
      debugMap.put("groupInfo.getKimTypeId", groupInfo.getKimTypeId());
      debugMap.put("groupInfo.getNamespaceCode", groupInfo.getNamespaceCode());
      AttributeSet attributeSet = groupInfo.getAttributes();

      GcGroupSave gcGroupSave = new GcGroupSave();

      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode(saveMode);
      
      if (!GrouperClientUtils.isBlank(groupIdToLookup)) {
        WsGroupLookup wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setUuid(groupIdToLookup);
        wsGroupToSave.setWsGroupLookup(wsGroupLookup);
      }

      gcGroupSave.addGroupToSave(wsGroupToSave);
      
      WsGroup wsGroup = new WsGroup();
      wsGroupToSave.setWsGroup(wsGroup);
      
      //assign group types and attributes
      String[] groupTypesOfKimGroups = GrouperKimUtils.grouperTypesOfKimGroups();
      boolean hasGroupTypes = GrouperClientUtils.length(groupTypesOfKimGroups) > 0;
      
      Set<String> attributeNameSet = attributeSet == null ? null : attributeSet.keySet();
      
      for (String key : GrouperClientUtils.nonNull(attributeNameSet)) {
        String value = attributeSet.get(key);
        //store in debug map for debugging or errors
        debugMap.put("groupInfo.attribute." + key, value);
      }
      
      if (!groupInfo.isActive()) {
        throw new UnsupportedOperationException("Cannot save a group which is not active to grouper");
      }
      
      wsGroup.setDescription(GrouperClientUtils.abbreviate(groupInfo.getGroupDescription(), 1024));
      
      //not sure why it would add a group and specify the group id... but we can try to support that
      if (!GrouperClientUtils.isBlank(groupInfo.getGroupId())) {
        wsGroup.setUuid(groupInfo.getGroupId());
      }
        
      String stemName = GrouperKimUtils.kimStem();
      if (!GrouperClientUtils.isBlank(groupInfo.getNamespaceCode())) {
        stemName += ":" + groupInfo.getNamespaceCode();
      }
      String grouperGroupName = stemName + ":" + groupInfo.getGroupName();
      debugMap.put("grouperGroupName", grouperGroupName);
      
      wsGroup.setName(grouperGroupName);

      //use the group name as display extension too
      wsGroup.setDisplayExtension(groupInfo.getGroupName());
            
      if (hasGroupTypes) {
        for (int i=0;i<groupTypesOfKimGroups.length;i++) {
          debugMap.put("groupTypesOfKimGroups." + i, groupTypesOfKimGroups[i]);
        }
        gcGroupSave.assignIncludeGroupDetail(true);
        WsGroupDetail wsGroupDetail = new WsGroupDetail();
        wsGroupDetail.setTypeNames(groupTypesOfKimGroups);
        wsGroup.setDetail(wsGroupDetail);
        
        //assign attributes
        int attributeSize = GrouperClientUtils.length(attributeSet);
        if (attributeSize > 0) {
          String[] attributeNames = new String[attributeSize];
          wsGroupDetail.setAttributeNames(attributeNames);
          String[] attributeValues = new String[attributeSize];
          wsGroupDetail.setAttributeValues(attributeValues);
          
          int index = 0;
          for (String key : attributeNameSet) {
            String value = attributeSet.get(key);
            attributeNames[index] = key;
            attributeValues[index] = value;
            index++;
          }
        }
      } else {
        debugMap.put("groupTypesOfKimGroups", null);
        //if no types, then cannot have attributes, so dont assign them
      }
      
      wsGroupToSave.setCreateParentStemsIfNotExist("T");
      
      WsGroupSaveResults wsGroupSaveResults = gcGroupSave.execute();
      
      //we did one assignment, we have one result
      WsGroupSaveResult wsGroupSaveResult = wsGroupSaveResults.getResults()[0];
      
      String resultCode = wsGroupSaveResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      String uuid = wsGroupSaveResult.getWsGroup().getUuid();
      debugMap.put("newGroupId", uuid);
      groupInfo.setGroupId(uuid);
      
      return groupInfo;
      
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
    
  }

  
  /**
   * void removeAllGroupMembers(java.lang.String groupId)
   *                        throws java.lang.UnsupportedOperationException
   *
   * Removes all members from the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removeAllGroupMembers(java.lang.String)
   */
  public void removeAllGroupMembers(String groupId) throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "removeAllGroupMembers");
    
    boolean hadException = false;
    
    try {
      
      //to remove all, we are adding nothing, and replacing the existing list with nothing, that removes them
      WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupUuid(groupId)
        .assignReplaceAllExisting(true).execute();
      
      //here we dont have an individual result, just an overall result, and if we got this far, we are done
      debugMap.put("overallResultCode", wsAddMemberResults.getResultMetadata().getResultCode());

    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
    
  }

  /**
   * boolean removeGroupFromGroup(java.lang.String childId,
   *                         java.lang.String parentId)
   *                         throws java.lang.UnsupportedOperationException
   *
   * Removes the group with the id supplied in childId from the group with the id supplied in parentId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removeGroupFromGroup(java.lang.String, java.lang.String)
   */
  public boolean removeGroupFromGroup(String childId, String parentId)
      throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("childId", childId);
    debugMap.put("parentId", parentId);
    debugMap.put("operation", "removeGroupFromGroup");
    
    return deleteMemberHelper(childId, parentId, "g:gsa", debugMap);
    
  }

  /**
   * delete a member or group from a group
   * @param subjectId
   * @param groupId
   * @param sourceId sourceId or null if none
   * @param debugMap
   * @return if the unassignment happened, or false if didnt need to happen
   */
  private boolean deleteMemberHelper(String subjectId, String groupId, String sourceId,
      Map<String, Object> debugMap) {
    boolean hadException = false;
    
    try {

      debugMap.put("subjectId", subjectId);

      subjectId = GrouperKimUtils.translatePrincipalId(subjectId);

      debugMap.put("subjectIdTranslated", subjectId);

      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      wsSubjectLookup.setSubjectId(subjectId);
      if (!GrouperClientUtils.isBlank(sourceId)) {
        wsSubjectLookup.setSubjectSourceId(sourceId);
      } else {
        wsSubjectLookup.setSubjectSourceId(GrouperKimUtils.separateSourceId(subjectId));
      }
      debugMap.put("sourceId", sourceId);

      WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember()
        .addSubjectLookup(wsSubjectLookup).assignGroupUuid(groupId).execute();
      
      //we did one assignment, we have one result
      WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
      
      String resultCode = wsDeleteMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was deleted
      if (GrouperClientUtils.equals("SUCCESS", resultCode) 
          || GrouperClientUtils.equals("SUCCESS_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //immediate assignment didnt exist
      if (GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE", resultCode)
          || GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * boolean removePrincipalFromGroup(java.lang.String principalId,
   *                              java.lang.String groupId)
   *                              throws java.lang.UnsupportedOperationException
   *
   * Removes the member principal with the given principalId from the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removePrincipalFromGroup(java.lang.String, java.lang.String)
   */
  public boolean removePrincipalFromGroup(String principalId, String groupId)
      throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("principalId", principalId);
    
    principalId = GrouperKimUtils.translatePrincipalId(principalId);

    debugMap.put("principalIdTranslated", principalId);
    
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "removePrincipalFromGroup");
    
    //lets see if there is a source to use
    String sourceId = GrouperKimUtils.subjectSourceId();

    return deleteMemberHelper(principalId, groupId, sourceId, debugMap);

  }

  /**
   * GroupInfo updateGroup(java.lang.String groupId,
   *                   GroupInfo groupInfo)
   *                   throws java.lang.UnsupportedOperationException
   *
   * Updates the group with the given groupId using the supplied GroupInfo. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#updateGroup(java.lang.String, org.kuali.rice.kim.bo.group.dto.GroupInfo)
   */
  public GroupInfo updateGroup(String groupId, GroupInfo groupInfo)
      throws UnsupportedOperationException {
    return saveGroupHelper(groupInfo, groupId, "updateGroup", "UPDATE");
  }

}

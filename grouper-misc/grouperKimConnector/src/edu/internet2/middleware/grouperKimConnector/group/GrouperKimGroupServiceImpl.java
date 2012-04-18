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
/**
 * @author mchyzer
 * $Id: GrouperKimGroupServiceImpl.java,v 1.8 2009/12/21 06:15:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.group.dto.GroupMembershipInfo;
import org.kuali.rice.kim.service.GroupService;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.groupUpdate.GrouperKimGroupUpdateServiceImpl;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * <pre>
 * Implement the Kim group service to delegate to grouper.  This is readonly group queries.  
 * https://test.kuali.org/rice/rice-api-1.0-javadocs/org/kuali/rice/kim/service/GroupService.html
 * </pre>
 */
public class GrouperKimGroupServiceImpl extends GrouperKimGroupUpdateServiceImpl implements GroupService {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GrouperKimGroupServiceImpl.class);

  /**
   * <pre>
   * java.util.List<java.lang.String> getDirectGroupIdsForPrincipal(java.lang.String principalId)
   *
   * Get the groupIds in which the principal has direct membership only. 
   * @see org.kuali.rice.kim.service.GroupService#getDirectGroupIdsForPrincipal(java.lang.String)
   * 
   * Note: this will only return group ids that are in the Kim stem in groups (since if you try to 
   * pull groups from outside, it wont work, Kim is sandboxed off in Grouper)
   * </pre>
   */
  public List<String> getDirectGroupIdsForPrincipal(String principalId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDirectGroupIdsForPrincipal");
    debugMap.put("principalId", principalId);

    String sourceId = GrouperKimUtils.subjectSourceId();

    String stemName = GrouperKimUtils.kimStem();

    List<GroupInfo> groupInfos = getGroupsHelper(principalId, sourceId, null, stemName, 
        StemScope.ALL_IN_SUBTREE, WsMemberFilter.Immediate, debugMap);
    
    return GrouperKimUtils.convertGroupInfosToGroupIds(groupInfos);
  }

  /**
   * <pre>
   * getDirectMemberGroupIds
   *
   * java.util.List<java.lang.String> getDirectMemberGroupIds(java.lang.String groupId)
   *
   * Get all the groups which are direct members of the given group. 
   * 
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberGroupIds(java.lang.String)
   * 
   * Note: this will only return group ids that are in the Kim stem in groups (since if you try to 
   * pull groups from outside, it wont work, Kim is sandboxed off in Grouper)
   * 
   * </pre>
   */
  public List<String> getDirectMemberGroupIds(String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDirectMemberGroupIds");

    return getMemberIdsHelper(groupId, new String[]{"g:gsa"}, 
        WsMemberFilter.Immediate, debugMap);
  }

  /**
   * <pre>
   * getDirectMemberPrincipalIds
   *
   * java.util.List<java.lang.String> getDirectMemberPrincipalIds(java.lang.String groupId)
   *
   * Get all the principals directly assigned to the given group.
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberPrincipalIds(java.lang.String)
   * 
   * Note: only subjects in sources in grouper.client.properties: grouper.kim.plugin.subjectSourceIds
   * will be returned.
   * </pre>
   */
  public List<String> getDirectMemberPrincipalIds(String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDirectMemberPrincipalIds");

    return getMemberIdsHelper(groupId, GrouperKimUtils.subjectSourceIds(), 
        WsMemberFilter.Immediate, debugMap);
  }

  /**
   * <pre>
   * java.util.List<java.lang.String> getParentGroupIds(java.lang.String groupId)
   *
   * Get the groups which are parents of the given group.
   *
   * This will recurse into groups above the given group and build a complete list of all groups included above this group. 
   * @see org.kuali.rice.kim.service.GroupService#getDirectParentGroupIds(java.lang.String)
   * 
   * Note: this will only return group ids that are in the Kim stem in groups (since if you try to 
   * pull groups from outside, it wont work, Kim is sandboxed off in Grouper)
   * </pre>
   */
  public List<String> getDirectParentGroupIds(String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDirectParentGroupIds");

    String stemName = GrouperKimUtils.kimStem();

    List<GroupInfo> groupInfos = getGroupsHelper(groupId, "g:gsa", null, stemName, 
        StemScope.ALL_IN_SUBTREE, WsMemberFilter.Immediate, debugMap);
    
    return GrouperKimUtils.convertGroupInfosToGroupIds(groupInfos);
  }

  /**
   * <pre>
   * getGroupAttributes
   *
   * java.util.Map<java.lang.String,java.lang.String> getGroupAttributes(java.lang.String groupId)
   *
   * Get all the attributes of the given group. 
   * 
   * @see org.kuali.rice.kim.service.GroupService#getGroupAttributes(java.lang.String)
   * </pre>
   */
  public Map<String, String> getGroupAttributes(String groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupAttributes");
    
    Map<String, GroupInfo> resultMap = getGroupInfosHelper(GrouperClientUtils.toList(groupId), debugMap, true);
    
    if (GrouperClientUtils.length(resultMap) == 0) {
      return null;
    }

    if (resultMap.size() > 1) {
      throw new RuntimeException("Why is there more than one result when searching for " + groupId);
    }
    
    GroupInfo groupInfo = resultMap.get(groupId);
    return groupInfo.getAttributes();
    
  }

  /**
   * <pre>
   * getGroupIdsForPrincipal
   *
   * java.util.List<java.lang.String> getGroupIdsForPrincipal(java.lang.String principalId)
   *
   * Get all the groups for the given principal. Recurses into parent groups to provide a comprehensive list. 
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipal(java.lang.String)
   * </pre>
   */
  public List<String> getGroupIdsForPrincipal(String principalId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupIdsForPrincipal");
    debugMap.put("principalId", principalId);

    String sourceId = GrouperKimUtils.subjectSourceId();

    String stemName = GrouperKimUtils.kimStem();

    List<GroupInfo> groupInfos = getGroupsHelper(principalId, sourceId, null, 
        stemName, StemScope.ALL_IN_SUBTREE, null, debugMap);
    
    return GrouperKimUtils.convertGroupInfosToGroupIds(groupInfos);
  }

  /**
   * <pre>
   * getGroupIdsForPrincipalByNamespace
   *
   * java.util.List<java.lang.String> getGroupIdsForPrincipalByNamespace(java.lang.String principalId,
   *                                                                 java.lang.String namespaceCode)
   *
   * Get all the groups for the given principal in the given namespace. Recurses into parent groups to provide a comprehensive list. 
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipalByNamespace(java.lang.String, java.lang.String)
   * </pre>
   */
  public List<String> getGroupIdsForPrincipalByNamespace(String principalId, String namespaceCode) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupIdsForPrincipalByNamespace");
    debugMap.put("principalId", principalId);
    debugMap.put("namespaceCode", namespaceCode);

    String sourceId = GrouperKimUtils.subjectSourceId();

    String stemName = GrouperKimUtils.kimStem() + ":" + namespaceCode;

    List<GroupInfo> groupInfos = getGroupsHelper(principalId, sourceId, null, 
        stemName, StemScope.ONE_LEVEL, null, debugMap);
    
    return GrouperKimUtils.convertGroupInfosToGroupIds(groupInfos);
  }

  /**
   * getGroupInfo
   *
   * GroupInfo getGroupInfo(java.lang.String groupId)
   *
   * Get the group by the given id. 
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfo(java.lang.String)
   */
  public GroupInfo getGroupInfo(String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupInfo");
    
    Map<String, GroupInfo> resultMap = getGroupInfosHelper(GrouperClientUtils.toList(groupId), debugMap, true);
    
    if (GrouperClientUtils.length(resultMap) == 0) {
      return null;
    }
    
    if (resultMap.size() > 1) {
      throw new RuntimeException("Why is there more than one result when searching for " + groupId);
    }

    //return the group;
    return resultMap.values().iterator().next();
  }

  /**
   * getGroupInfoByName
   *
   * GroupInfo getGroupInfoByName(java.lang.String namespaceCode,
   *                          java.lang.String groupName)
   *
   * Get the group by the given namesapce code and name. 
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfoByName(java.lang.String, java.lang.String)
   */
  public GroupInfo getGroupInfoByName(String namespaceCode, String groupName) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupInfoByName");
    debugMap.put("namespaceCode", namespaceCode);
    debugMap.put("groupName", groupName);
    
    boolean hadException = false;
    
    try {
      
      GcFindGroups gcFindGroups = new GcFindGroups();
      gcFindGroups.assignIncludeGroupDetail(true);
      gcFindGroups.addGroupName(GrouperKimUtils.kimStem() + ":" + namespaceCode + ":" + groupName);
      
      WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
      
      //we did one assignment, we have one result
      WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
      
      int numberOfGroups = GrouperClientUtils.length(wsGroups);
      
      debugMap.put("resultNumberOfGroups", numberOfGroups);
      
      if (numberOfGroups == 0) {
        return null;
      }
      
      if (numberOfGroups > 1) {
        throw new RuntimeException("Why is there more than 1 group returned?");
      }

      return GrouperKimUtils.convertWsGroupToGroupInfo(wsGroups[0]);
      
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
   * java.util.Map<java.lang.String,GroupInfo> getGroupInfos(java.util.Collection<java.lang.String> groupIds)
   *
   * Gets all groups for the given collection of group ids.
   *
   * The result is a Map containing the group id as the key and the group info as the value. 
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfos(java.util.Collection)
   */
  public Map<String, GroupInfo> getGroupInfos(Collection<String> groupIds) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupInfos");
    
    return getGroupInfosHelper(groupIds, debugMap, true);
  }

  /**
   * get group info on a bunch of group ids
   * @param groupIds
   * @param debugMap 
   * @param retrieveGroupDetail
   * @return the map of id to group
   */
  private Map<String, GroupInfo> getGroupInfosHelper(Collection<String> groupIds, Map<String, Object> debugMap, boolean retrieveGroupDetail) {
    int groupIdsSize = GrouperClientUtils.length(groupIds);
    debugMap.put("groupIds.size", groupIdsSize);
    Map<String, GroupInfo> result = new LinkedHashMap<String, GroupInfo>();
    boolean hadException = false;
    
    try {
      
      if (groupIdsSize == 0) {
        return result;
      }

      int index = 0;
      
      //log some of these
      for (String groupId : groupIds) {
        
        //dont log all...
        if (index > 20) {
          break;
        }
        
        debugMap.put("groupIds." + index, groupId);
        
        String groupIdOriginal = groupId;
        groupId = GrouperKimUtils.translateGroupId(groupId);
        debugMap.put("grouperGroupIds." + index, groupId);
        
        String groupName = null;
        
        if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
          //see if different name to override group
          groupName = GrouperKimUtils.translateGroupName(groupId);
          if (!GrouperClientUtils.equals(groupName, groupId)) {
            groupId = null;
          } else {
            groupName = null;
          }
        }
        
        debugMap.put("grouperGroupId." + index, groupId);
        debugMap.put("grouperGroupName." + index, groupName);

        index++;
      }

      GcFindGroups gcFindGroups = new GcFindGroups();
      
      if (retrieveGroupDetail) {
        gcFindGroups.assignIncludeGroupDetail(retrieveGroupDetail);
      }
      
      Map<String, String> nameToOldIdMap = new HashMap<String, String>();
      
      for (String groupId : groupIds) {
        
        String groupIdOriginal = groupId;
        groupId = GrouperKimUtils.translateGroupId(groupId);
        
        String groupName = null;
        
        if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
          //see if different name to override group
          groupName = GrouperKimUtils.translateGroupName(groupId);
          if (!GrouperClientUtils.equals(groupName, groupId)) {
            nameToOldIdMap.put(groupName, groupId);
            gcFindGroups.addGroupName(groupName);
          } else {
            gcFindGroups.addGroupUuid(groupId);
          }
        }

      }
      
      WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
      
      //we did one assignment, we have one result
      WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
      
      debugMap.put("resultNumberOfGroups", GrouperClientUtils.length(wsGroups));
      
      index = 0;
      for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
        
        if (index < 20) {
          debugMap.put("groupResult." + index, wsGroup.getUuid() + ", " + wsGroup.getName());
        }
        
        GroupInfo groupInfo = GrouperKimUtils.convertWsGroupToGroupInfo(wsGroup);
        
        //might need to translate back to the ID before the concifg file interfered
        String groupId = groupInfo.getGroupId();
        if (nameToOldIdMap.containsKey(wsGroup.getName())) {
          groupId = nameToOldIdMap.get(wsGroup.getName());
        }
        
        result.put(groupId, groupInfo);
        index++;
      }
      
      return result;
      
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
   * getGroupMembers
   *
   * java.util.Collection<GroupMembershipInfo> getGroupMembers(java.util.List<java.lang.String> groupIds)
   *
   * Get the membership info for the members of all the groups with the given group ids.
   *
   * The collection of GroupMembershipInfo will contain members for all the groups in no defined order. The values returned may or may not be grouped by group id. 
   * 
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembers(java.util.List)
   */
  public Collection<GroupMembershipInfo> getGroupMembers(List<String> groupIds) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupMembers");
    
    int groupIdsSize = GrouperClientUtils.length(groupIds);
    debugMap.put("groupIds.size", groupIdsSize);

    List<GroupMembershipInfo> results = new ArrayList<GroupMembershipInfo>();
    if (groupIdsSize == 0) {
      return results;
    }
    boolean hadException = false;
    
    try {
      
      int index = 0;
      
      //log some of these
      for (String groupId : groupIds) {
        
        //dont log all...
        if (index > 20) {
          break;
        }

        debugMap.put("groupIds." + index, groupId);
        
        String groupIdOriginal = groupId;
        groupId = GrouperKimUtils.translateGroupId(groupId);
        debugMap.put("grouperGroupIds." + index, groupId);
        
        String groupName = null;
        
        if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
          //see if different name to override group
          groupName = GrouperKimUtils.translateGroupName(groupId);
          if (!GrouperClientUtils.equals(groupName, groupId)) {
            groupId = null;
          } else {
            groupName = null;
          }
        }
        
        debugMap.put("grouperGroupId." + index, groupId);
        debugMap.put("grouperGroupName." + index, groupName);

        
        index++;
      }
      
      GcGetMemberships gcGetMemberships = new GcGetMemberships();
      
      Map<String, String> nameToOldIdMap = new HashMap<String, String>();

      for (String groupId : groupIds) {

        String groupIdOriginal = groupId;
        groupId = GrouperKimUtils.translateGroupId(groupId);
        
        String groupName = null;
        
        if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
          //see if different name to override group
          groupName = GrouperKimUtils.translateGroupName(groupId);
          if (!GrouperClientUtils.equals(groupName, groupId)) {
            nameToOldIdMap.put(groupName, groupId);
            gcGetMemberships.addGroupName(groupName);
          } else {
            gcGetMemberships.addGroupUuid(groupId);
          }
        }

      }
      
      //only get subjects from the right sources
      String sourceIdsCommaSeparated = GrouperClientUtils.propertiesValue("grouper.kim.plugin.subjectSourceIds", true);
      String[] sourceIds = GrouperClientUtils.splitTrim(sourceIdsCommaSeparated, ",");
      for (String sourceId : sourceIds) {
        gcGetMemberships.addSourceId(sourceId);
      }
      
      //get groups too?
      gcGetMemberships.addSourceId("g:gsa");
      
      WsGetMembershipsResults wsGetMembershipsResults = gcGetMemberships.execute();
      
      //we did one assignment, we have one result
      WsMembership[] wsMemberships = wsGetMembershipsResults.getWsMemberships();
      
      if (GrouperClientUtils.length(wsMemberships) == 0) {
        return null;
      }
      
      List<WsMembership> wsMembershipsList = GrouperClientUtils.toList(wsMemberships);

      GrouperKimUtils.filterMembershipGroupsNotInKimStem(wsMembershipsList);
      
      debugMap.put("resultNumberOfMemberships", GrouperClientUtils.length(wsMembershipsList));
      
      index = 0;

      for (WsMembership wsMembership : GrouperClientUtils.nonNull(wsMembershipsList)) {
        
        if (index < 20) {
          debugMap.put("membershipResult." + index, wsMembership.getMembershipId() + ", " + wsMembership.getGroupName() + ", " + wsMembership.getSubjectId());
        }
        
        String groupId = wsMembership.getGroupId();
        
        if (nameToOldIdMap.containsKey(wsMembership.getGroupName())) {
          groupId = nameToOldIdMap.get(wsMembership.getGroupName());
        }
        
        String groupMemberId = wsMembership.getMembershipId();
        String memberId = wsMembership.getSubjectId();
        String memberTypeCode = null;
        String enabledDateString = wsMembership.getEnabledTime();
        Date enabledDate = GrouperClientCommonUtils.dateValue(enabledDateString);
        java.sql.Date enabledSqlDate = GrouperClientCommonUtils.toSqlDate(enabledDate);
        String disabledDateString = wsMembership.getDisabledTime();
        Date disabledDate = GrouperClientCommonUtils.dateValue(disabledDateString);
        java.sql.Date disabledSqlDate = GrouperClientCommonUtils.toSqlDate(disabledDate);
        
        GroupMembershipInfo groupMembershipInfo = new GroupMembershipInfo(groupId, groupMemberId, memberId, memberTypeCode, enabledSqlDate, disabledSqlDate);
        results.add(groupMembershipInfo);
        index++;
      }
      
      return results;
      
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
   * getGroupMembersOfGroup
   *
   * java.util.Collection<GroupMembershipInfo> getGroupMembersOfGroup(java.lang.String groupId)
   *
   * Get the membership info for the members of the group with the given id.
   *
   * Only GroupMembershipInfo for direct group members is returned.
   * 
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembersOfGroup(java.lang.String)
   */
  public Collection<GroupMembershipInfo> getGroupMembersOfGroup(String groupId) {
    
    return getGroupMembers(GrouperClientUtils.toList(groupId));
    
  }

  /**
   * getGroupsForPrincipal
   *
   * java.util.List<GroupInfo> getGroupsForPrincipal(java.lang.String principalId)
   *
   * Get all the groups for a given principal.
   *
   * This will include all groups directly assigned as well as those inferred by the fact that they are members of higher level groups. 
   *    
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipal(java.lang.String)
   */
  public List<GroupInfo> getGroupsForPrincipal(String principalId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupsForPrincipal");
    debugMap.put("principalId", principalId);

    String sourceId = GrouperKimUtils.subjectSourceId();

    String stemName = GrouperKimUtils.kimStem();

    return getGroupsHelper(principalId, sourceId, null, 
        stemName, StemScope.ALL_IN_SUBTREE, null, debugMap);

  }

  /**
   * get groups for a subject
   * @param subjectId
   * @param sourceId or null to not specify
   * @param subjectIdentifier
   * @param stemName to search in (required)
   * @param stemScope scope in stem
   * @param wsMemberFilter is if all, immediate, effective, etc  null means all
   * @param debugMap
   * @return the group infos
   */
  private List<GroupInfo> getGroupsHelper(String subjectId, String sourceId, 
      String subjectIdentifier,
      String stemName, 
      StemScope stemScope, WsMemberFilter wsMemberFilter, Map<String, Object> debugMap) {
    boolean hadException = false;
    
    try {
      
      int index = 0;
      
      GcGetGroups gcGetGroups = new GcGetGroups();
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();

      debugMap.put("subjectId", subjectId);
      debugMap.put("subjectIdentifier", subjectIdentifier);
      
      subjectId = GrouperKimUtils.translatePrincipalId(subjectId);
      
      //lets see if we should move the subjectId to subjectIdentifier...
      
      if (GrouperClientUtils.equals(sourceId, "g:gsa")) {
        debugMap.put("groupId", subjectId);
        
        String groupIdOriginal = subjectId;
        String groupName = null;
        
        subjectId = GrouperKimUtils.translateGroupId(subjectId);
        if (GrouperClientUtils.equals(groupIdOriginal, subjectId)) {
          //see if different name to override group
          groupName = GrouperKimUtils.translateGroupName(subjectId);
          if (!GrouperClientUtils.equals(groupName, subjectId)) {
            subjectId = null;
          } else {
            groupName = null;
          }
        }
        
        debugMap.put("grouperGroupId", subjectId);
        debugMap.put("grouperGroupName", groupName);
        subjectIdentifier = groupName;
        
      }
      
      
      debugMap.put("subjectIdentifierTranslated", subjectIdentifier);
      
      debugMap.put("subjectIdTranslated", subjectId);

      wsSubjectLookup.setSubjectId(subjectId);
      wsSubjectLookup.setSubjectIdentifier(subjectIdentifier);
      gcGetGroups.addSubjectLookup(wsSubjectLookup);
      
      debugMap.put("sourceId", sourceId);

      if (!GrouperClientUtils.isBlank(GrouperKimUtils.separateSourceId(subjectId))) {
        wsSubjectLookup.setSubjectSourceId(GrouperKimUtils.separateSourceId(subjectId));
      } else if (!GrouperClientUtils.isBlank(sourceId)) {
        wsSubjectLookup.setSubjectSourceId(sourceId);
      }

      debugMap.put("stemName", stemName);
      debugMap.put("stemScope", stemScope == null ? null : stemScope.name());
      
      WsStemLookup wsStemLookup = new WsStemLookup(stemName, null);
      
      gcGetGroups.assignWsStemLookup(wsStemLookup);
      gcGetGroups.assignStemScope(stemScope);
      
      debugMap.put("wsMemberFilter", wsMemberFilter == null ? null : wsMemberFilter.name());
      gcGetGroups.assignMemberFilter(wsMemberFilter);
      
      WsGetGroupsResults wsGetGroupsResults = gcGetGroups.execute();
      WsGetGroupsResult[] wsGetGroupsResultArray = wsGetGroupsResults.getResults();
      
      int resultsSize = GrouperClientUtils.length(wsGetGroupsResultArray);
      
      debugMap.put("resultsArraySize", resultsSize);
      
      //not sure why this would ever be 0...
      if (resultsSize == 0) {
        return null;
      }
      
      if (resultsSize > 1) {
        throw new RuntimeException("Why is result array size more than 1?");
      }
      
      WsGetGroupsResult wsGetGroupsResult = wsGetGroupsResultArray[0];
      
      WsGroup[] wsGroups = wsGetGroupsResult.getWsGroups();
      resultsSize = GrouperClientUtils.length(wsGroups);
      debugMap.put("resultSize", resultsSize);
      List<GroupInfo> results = new ArrayList<GroupInfo>();
      
      index = 0;
      
      for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
        
        if (index < 20) {
          
          debugMap.put("result." + index, wsGroup.getUuid() + ", " + wsGroup.getName());
          
        }
        
        GroupInfo groupInfo = GrouperKimUtils.convertWsGroupToGroupInfo(wsGroup);
        results.add(groupInfo);
        
        
        index++;
      }
      
      return results;
      
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
   * <pre>
   * getGroupsForPrincipalByNamespace
   *
   * java.util.List<GroupInfo> getGroupsForPrincipalByNamespace(java.lang.String principalId,
   *                                                       java.lang.String namespaceCode)
   *
   * Get all the groups within a namespace for a given principal.
   *
   * This is the same as the getGroupsForPrincipal(String) method except that the results will be filtered by namespace after retrieval.
   * </pre> 
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipalByNamespace(java.lang.String, java.lang.String)
   */
  public List<GroupInfo> getGroupsForPrincipalByNamespace(String principalId, String namespaceCode) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getGroupsForPrincipalByNamespace");
    debugMap.put("principalId", principalId);
    debugMap.put("namespaceCode", namespaceCode);

    String sourceId = GrouperKimUtils.subjectSourceId();

    String stemName = GrouperKimUtils.kimStem() + ":" + namespaceCode;

    return getGroupsHelper(principalId, sourceId, null, stemName, StemScope.ONE_LEVEL, null, debugMap);
  }

  /**
   * <pre>
   * @see org.kuali.rice.kim.service.GroupService#getMemberGroupIds(java.lang.String)
   * </pre>
   */
  public List<String> getMemberGroupIds(String groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getMemberPrincipalIds");
    
    return getMemberIdsHelper(groupId, new String[]{"g:gsa"}, null, debugMap);

    
  }

  /**
   * <pre>
   * getMemberPrincipalIds
   * java.util.List<java.lang.String> getMemberPrincipalIds(java.lang.String groupId)
   *
   * Get all the principals of the given group. Recurses into contained groups to provide a comprehensive list. 
   * @see org.kuali.rice.kim.service.GroupService#getMemberPrincipalIds(java.lang.String)
   * </pre>
   */
  public List<String> getMemberPrincipalIds(String groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getMemberPrincipalIds");
    
    return getMemberIdsHelper(groupId, GrouperKimUtils.subjectSourceIds(), null, debugMap);
    
  }

  /**
   * get member ids from a group
   * @param groupId to search by groupId, mutually exclusive with groupName
   * @param sourceIds 
   * @param wsMemberFilter null for all, or immediate, or nonimmediate
   * @param debugMap 
   * @return the member ids
   */
  private List<String> getMemberIdsHelper(String groupId,
      String[] sourceIds, WsMemberFilter wsMemberFilter, 
      Map<String, Object> debugMap ) {
    
    boolean hadException = false;
    
    try {
      
      int index = 0;
      
      GcGetMembers gcGetMembers = new GcGetMembers();
            
      debugMap.put("groupId", groupId);
      
      String groupIdOriginal = groupId;
      String groupName = null;
      
      groupId = GrouperKimUtils.translateGroupId(groupId);
      if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
        //see if different name to override group
        groupName = GrouperKimUtils.translateGroupName(groupId);
        if (!GrouperClientUtils.equals(groupName, groupId)) {
          groupId = null;
        } else {
          groupName = null;
        }
      }
      
      debugMap.put("grouperGroupId", groupId);
      debugMap.put("grouperGroupName", groupName);
      
      if (!GrouperClientUtils.isBlank(groupId)) {
        gcGetMembers.addGroupUuid(groupId);
      } else if (!GrouperClientUtils.isBlank(groupName)) {
        gcGetMembers.addGroupName(groupName);
      }
      
      int sourceIdsLength = GrouperClientUtils.length(sourceIds);
      
      debugMap.put("sourceIds.length", sourceIdsLength);
      
      for (int i=0;i<sourceIdsLength;i++) {
        
        gcGetMembers.addSourceId(sourceIds[i]);
        
      }
      
      debugMap.put("wsMemberFilter", wsMemberFilter == null ? null : wsMemberFilter.name());

      gcGetMembers.assignMemberFilter(wsMemberFilter);
      
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
      WsGetMembersResult[] wsGetMembersResultArray = wsGetMembersResults.getResults();
      
      int resultsSize = GrouperClientUtils.length(wsGetMembersResultArray);
      
      debugMap.put("resultsArraySize", resultsSize);
      
      //not sure why this would ever be 0...
      if (resultsSize == 0) {
        throw new RuntimeException("Why is result size not 1?");
      }
      
      if (resultsSize > 1) {
        throw new RuntimeException("Why is result array size more than 1?");
      }
      
      WsGetMembersResult wsGetMembersResult = wsGetMembersResultArray[0];
      
      WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
      
      int wsSubjectsLength = GrouperClientUtils.length(wsSubjects);
      
      debugMap.put("wsSubjectsLength", wsSubjectsLength);

      if (wsSubjectsLength == 0) {
        return null;
      }

      List<WsSubject> wsSubjectList = GrouperClientUtils.toList(wsSubjects);
      GrouperKimUtils.filterGroupsNotInKimStem(wsSubjectList);
            
      List<String> results = new ArrayList<String>();
      
      for (int i=0;i<GrouperClientUtils.length(wsSubjectList);i++) {

        WsSubject wsSubject = wsSubjectList.get(i);
        if (i < 20) {
          debugMap.put("result." + index, wsSubject);
        }

        String currentSubjectId = wsSubject.getId();
        
        String translatedCurrentSubjectId = GrouperKimUtils.untranslatePrincipalId(
            wsSubject.getSourceId(), currentSubjectId);
        
        results.add(translatedCurrentSubjectId);
      }
      
      return results;
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
   * <pre>
   * java.util.List<java.lang.String> getParentGroupIds(java.lang.String groupId)
   *
   * Get the groups which are parents of the given group.
   * 
   * This will recurse into groups above the given group and build a complete list of all groups included above this group. 
   * @param groupId 
   * @return the list of group ids
   * @see org.kuali.rice.kim.service.GroupService#getParentGroupIds(java.lang.String)
   </pre>
   */
  public List<String> getParentGroupIds(String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getParentGroupIds");

    String stemName = GrouperKimUtils.kimStem();

    List<GroupInfo> groupInfos = getGroupsHelper(groupId, "g:gsa", null, stemName, 
        StemScope.ALL_IN_SUBTREE, null, debugMap);
    
    return GrouperKimUtils.convertGroupInfosToGroupIds(groupInfos);
  }

  /**
   * <pre>
   * isDirectMemberOfGroup
   *
   * boolean isDirectMemberOfGroup(java.lang.String principalId,
   *                           java.lang.String groupId)
   *
   * Check whether the give principal is a member of the group.
   *
   * This will not recurse into contained groups. 
   * @see org.kuali.rice.kim.service.GroupService#isDirectMemberOfGroup(java.lang.String, java.lang.String)
   * </pre>
   */
  public boolean isDirectMemberOfGroup(String principalId, String groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "isDirectMemberOfGroup");
    debugMap.put("principalId", principalId);
    debugMap.put("groupId", groupId);

    String sourceId = GrouperKimUtils.subjectSourceId();
    
    return isMemberHelper(principalId, sourceId, null, groupId, WsMemberFilter.Immediate, debugMap);
    
  }

  /**
   * 
   * @param subjectId
   * @param sourceId or null to search all
   * @param subjectIdentifier 
   * @param groupId
   * @param wsMemberFilter null for all, or immediate, etc
   * @param debugMap 
   * @return if has member
   */
  private boolean isMemberHelper(String subjectId, String sourceId, String subjectIdentifier, String groupId, WsMemberFilter wsMemberFilter, Map<String, Object> debugMap) {
    boolean hadException = false;
    
    debugMap.put("subjectId", subjectId);
    debugMap.put("subjectIdentifier", subjectIdentifier);
    String subjectIdOriginal = subjectId;
    subjectId = GrouperKimUtils.translatePrincipalId(subjectId);

    debugMap.put("subjectIdTranslated", subjectId);

    debugMap.put("sourceId", sourceId);
    debugMap.put("groupId", groupId);
    debugMap.put("wsMemberFilter", wsMemberFilter == null ? null : wsMemberFilter.name());
    
    try {
    
      String groupIdOriginal = groupId;
      String groupName = null;
      
      groupId = GrouperKimUtils.translateGroupId(groupId);
      if (GrouperClientUtils.equals(groupIdOriginal, groupId)) {
        //see if different name to override group
        groupName = GrouperKimUtils.translateGroupName(groupId);
        if (!GrouperClientUtils.equals(groupName, groupId)) {
          groupId = null;
        } else {
          groupName = null;
        }
      }
      
      debugMap.put("grouperGroupId", groupId);
      debugMap.put("grouperGroupName", groupName);

      GcHasMember gcHasMember = new GcHasMember();
      
      if (!GrouperClientUtils.isBlank(groupId)) {
        gcHasMember.assignGroupUuid(groupId);
      } else if (!GrouperClientUtils.isBlank(groupName)) {
        gcHasMember.assignGroupName(groupName);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      
      
      if (!GrouperClientUtils.isBlank(GrouperKimUtils.separateSourceId(subjectIdOriginal))) {
        wsSubjectLookup.setSubjectSourceId(GrouperKimUtils.separateSourceId(subjectIdOriginal));
      } else if (!GrouperClientUtils.isBlank(sourceId)) {
        wsSubjectLookup.setSubjectSourceId(sourceId);
      }

      if (GrouperClientUtils.equals("g:gsa", wsSubjectLookup.getSubjectSourceId())) {
        
        subjectIdOriginal = subjectId;
        
        subjectId = GrouperKimUtils.translateGroupId(subjectId);
        
        if (GrouperClientUtils.equals(subjectIdOriginal, subjectId)) {
          //see if different name to override group
          subjectId = GrouperKimUtils.translateGroupName(subjectId);
          if (!GrouperClientUtils.equals(subjectId, subjectIdOriginal)) {
            subjectIdentifier = subjectId;
            subjectId = null;
          } 
        }
      }
      
      wsSubjectLookup.setSubjectId(subjectId);
      
      if (GrouperClientUtils.isBlank(subjectId)) {
                
        subjectIdentifier = GrouperKimUtils.separateSourceIdSuffix(subjectIdentifier);
        debugMap.put("subjectIdentifier", subjectIdentifier);
        
        wsSubjectLookup.setSubjectIdentifier(subjectIdentifier);
      }

      
      gcHasMember.addSubjectLookup(wsSubjectLookup);
      
      gcHasMember.assignMemberFilter(wsMemberFilter);
      
      WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
      WsHasMemberResult[] wsHasMemberResultArray = wsHasMemberResults.getResults();
      
      int resultsSize = GrouperClientUtils.length(wsHasMemberResultArray);
      
      debugMap.put("resultsArraySize", resultsSize);
      
      //not sure why this would ever be 0...
      if (resultsSize == 0) {
        throw new RuntimeException("Why would this not return an answer???");
      }
      
      if (resultsSize > 1) {
        throw new RuntimeException("Why is result array size more than 1?");
      }
      
      WsHasMemberResult wsHasMemberResult = wsHasMemberResultArray[0];
      
      String resultCode = wsHasMemberResult.getResultMetadata().getResultCode();
      debugMap.put("resultCode", resultCode);

      if (GrouperClientUtils.equals("IS_MEMBER", resultCode)) {
        return true;
      }
      if (GrouperClientUtils.equals("IS_NOT_MEMBER", resultCode)) {
        return false;
      }

      throw new RuntimeException("Not expecting result code: " + resultCode);

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
   * boolean isGroupActive(java.lang.String groupId)
   *
   * Checks if the group with the given id is active. Returns true if it is, false otherwise. 
   * @see org.kuali.rice.kim.service.GroupService#isGroupActive(java.lang.String)
   */
  public boolean isGroupActive(String groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "isGroupActive");
    
    Map<String, GroupInfo> resultMap = getGroupInfosHelper(GrouperClientUtils.toList(groupId), debugMap, false);
    
    if (GrouperClientUtils.length(resultMap) == 0) {
      return false;
    }

    if (resultMap.size() > 1) {
      throw new RuntimeException("Why is there more than one result when searching for " + groupId);
    }
    
    //if there is one, then we found it, it is active
    return true;
  }

  /**
   * <pre>
   * isGroupMemberOfGroup
   *
   * boolean isGroupMemberOfGroup(java.lang.String groupMemberId,
   *                          java.lang.String groupId)
   *
   * Check whether the group identified by groupMemberId is a member of the group identified by groupId. This will recurse through all groups. 
   * 
   * @see org.kuali.rice.kim.service.GroupService#isGroupMemberOfGroup(java.lang.String, java.lang.String)
   * </pre>
   */
  public boolean isGroupMemberOfGroup(String groupMemberId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "isGroupMemberOfGroup");
    debugMap.put("groupMemberId", groupMemberId);
    debugMap.put("groupId", groupId);

    return isMemberHelper(groupMemberId, "g:gsa", null, groupId, null, debugMap);
  }

  /**
   * <pre>
   * isMemberOfGroup
   *
   * boolean isMemberOfGroup(java.lang.String principalId,
   *                     java.lang.String groupId)
   *
   * Check whether the give principal is a member of the group.
   *
   * This will also return true if the principal is a member of a groups assigned to this group. 
   * 
   * @see org.kuali.rice.kim.service.GroupService#isMemberOfGroup(java.lang.String, java.lang.String)
   * </pre>
   */
  public boolean isMemberOfGroup(String principalId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "isGroupMemberOfGroup");
    debugMap.put("principalId", principalId);
    debugMap.put("groupId", groupId);
    String sourceId = GrouperKimUtils.subjectSourceId();

    return isMemberHelper(principalId, sourceId, null, groupId, null, debugMap);
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroupIds(java.util.Map)
   */
  public List<String> lookupGroupIds(Map<String, String> arg0) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroups(java.util.Map)
   */
  public List<? extends Group> lookupGroups(Map<String, String> arg0) {
    throw new RuntimeException("Not implemented");
  }

}

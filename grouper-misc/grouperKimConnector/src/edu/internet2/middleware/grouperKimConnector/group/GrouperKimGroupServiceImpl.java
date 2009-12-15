/**
 * @author mchyzer
 * $Id: GrouperKimGroupServiceImpl.java,v 1.3 2009-12-15 17:07:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.group;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.group.dto.GroupMembershipInfo;
import org.kuali.rice.kim.service.GroupService;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 *
 */
public class GrouperKimGroupServiceImpl implements GroupService {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GrouperKimGroupServiceImpl.class);

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectGroupIdsForPrincipal(java.lang.String)
   */
  public List<String> getDirectGroupIdsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberGroupIds(java.lang.String)
   */
  
  public List<String> getDirectMemberGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberPrincipalIds(java.lang.String)
   */
  
  public List<String> getDirectMemberPrincipalIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectParentGroupIds(java.lang.String)
   */
  
  public List<String> getDirectParentGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupAttributes(java.lang.String)
   */
  
  public Map<String, String> getGroupAttributes(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipal(java.lang.String)
   */
  
  public List<String> getGroupIdsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipalByNamespace(java.lang.String, java.lang.String)
   */
  
  public List<String> getGroupIdsForPrincipalByNamespace(String arg0, String arg1) {
    return null;
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
    
    Map<String, GroupInfo> resultMap = getGroupInfosHelper(GrouperClientUtils.toList(groupId), debugMap);
    
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
    
    return getGroupInfosHelper(groupIds, debugMap);
  }

  /**
   * get group info on a bunch of group ids
   * @param groupIds
   * @param debugMap 
   * @return the map of id to group
   */
  private Map<String, GroupInfo> getGroupInfosHelper(Collection<String> groupIds, Map<String, Object> debugMap) {
    int groupIdsSize = GrouperClientUtils.length(groupIds);
    debugMap.put("groupIds.size", groupIdsSize);
    Map<String, GroupInfo> result = new LinkedHashMap<String, GroupInfo>();
    if (groupIdsSize == 0) {
      return result;
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
        
        index++;
      }

      GcFindGroups gcFindGroups = new GcFindGroups();
      
      for (String groupId : groupIds) {
        gcFindGroups.addGroupUuid(groupId);
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
        result.put(groupInfo.getGroupId(), groupInfo);
        index++;
      }
      
      return result;
      
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }

  }
  
  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembers(java.util.List)
   */
  
  public Collection<GroupMembershipInfo> getGroupMembers(List<String> arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembersOfGroup(java.lang.String)
   */
  
  public Collection<GroupMembershipInfo> getGroupMembersOfGroup(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipal(java.lang.String)
   */
  
  public List<GroupInfo> getGroupsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipalByNamespace(java.lang.String, java.lang.String)
   */
  
  public List<GroupInfo> getGroupsForPrincipalByNamespace(String arg0, String arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getMemberGroupIds(java.lang.String)
   */
  
  public List<String> getMemberGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getMemberPrincipalIds(java.lang.String)
   */
  
  public List<String> getMemberPrincipalIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getParentGroupIds(java.lang.String)
   */
  
  public List<String> getParentGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isDirectMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isDirectMemberOfGroup(String arg0, String arg1) {
    return false;
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
    
    Map<String, GroupInfo> resultMap = getGroupInfosHelper(GrouperClientUtils.toList(groupId), debugMap);
    
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
   * @see org.kuali.rice.kim.service.GroupService#isGroupMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isGroupMemberOfGroup(String arg0, String arg1) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isMemberOfGroup(String arg0, String arg1) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroupIds(java.util.Map)
   */
  
  public List<String> lookupGroupIds(Map<String, String> arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroups(java.util.Map)
   */
  
  public List<? extends Group> lookupGroups(Map<String, String> arg0) {
    return null;
  }

}

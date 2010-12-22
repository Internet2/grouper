/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.AccessProvider;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAtlassianConfig.GrouperAtlassianAutoaddConfig;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * implement the opensymphony interface that Atlassian uses for products like jira/confluence
 */
@SuppressWarnings("serial")
public class GrouperAccessProvider implements AccessProvider {

  /**
   * bean which holds stuff to cache
   *
   */
  private static class GrouperAccessCacheBean {

    /** list of group names in atlassian */
    private List<String> groupNames;

    /** list of users in group */
    private Map<String, List<String>> listUsersInGroup;

    /** list of groups for a user */
    private Map<String, List<String>> listGroupsContainingUser;

    /**
     * list of group names in atlassian
     * @return gruop names
     */
    public List<String> getGroupNames() {
      return this.groupNames;
    }

    /**
     * list of group names in atlassian
     * @param groupNames1
     */
    public void setGroupNames(List<String> groupNames1) {
      this.groupNames = groupNames1;
    }

    /**
     * list of users in group
     * @return users in group
     */
    public Map<String, List<String>> getListUsersInGroup() {
      return this.listUsersInGroup;
    }

    /**
     * list of users in group
     * @param listUsersInGroup1
     */
    public void setListUsersInGroup(Map<String, List<String>> listUsersInGroup1) {
      this.listUsersInGroup = listUsersInGroup1;
    }

    /**
     * list of groups for a user
     * @return list of groups for a user
     */
    public Map<String, List<String>> getListGroupsContainingUser() {
      return this.listGroupsContainingUser;
    }

    /**
     * list of groups for a user
     * @param listGroupsContainingUser1
     */
    public void setListGroupsContainingUser(Map<String, List<String>> listGroupsContainingUser1) {
      this.listGroupsContainingUser = listGroupsContainingUser1;
    }
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperAccessProvider.class);

  /**
   * count the cache hits for testing
   */
  static long cacheHits = 0;
  /**
   * count the cache misses for unit testing
   */
  static long cacheMisses = 0;
  
  /**
   * @see com.opensymphony.user.provider.AccessProvider#addToGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean addToGroup(String username, String groupname) {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "addToGroup");
    debugMap.put("username", username);
    debugMap.put("groupname", groupname);
    
    Boolean result = null;

    try {

      cacheMisses++;
      
      GcAddMember gcAddMember = new GcAddMember();
      String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);

      gcAddMember.assignGroupName(grouperGroupName);
      
      WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(username, debugMap);
      
      gcAddMember.addSubjectLookup(wsSubjectLookup);

      WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
      
      WsAddMemberResult wsAddMemberResult = wsAddMemberResults.getResults()[0];

      GrouperAtlassianUtils.addToDebugMap(wsAddMemberResult.getResultMetadata(), debugMap, false);
      
      String resultCode = wsAddMemberResult.getResultMetadata().getResultCode();
      
      result = !GrouperClientUtils.equals("SUCCESS_ALREADY_EXISTED", resultCode);
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      //lets clear some caches
      flushCaches();
      
      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsAddMemberResults) {
          WsAddMemberResults wsAddMemberResults = (WsAddMemberResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsAddMemberResults != null && GrouperClientUtils.length(wsAddMemberResults.getResults()) > 0) {
            WsAddMemberResult wsAddMemberResult = wsAddMemberResults.getResults()[0];
            if (wsAddMemberResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsAddMemberResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /** cache for list groups, key is TRUE */
  private static ExpirableCache<Boolean, GrouperAccessCacheBean> grouperAccessCacheBeanCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<Boolean, GrouperAccessCacheBean> grouperAccessCacheBeanCache() {
    if (grouperAccessCacheBeanCache == null) {
      grouperAccessCacheBeanCache = new ExpirableCache<Boolean, GrouperAccessCacheBean>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return grouperAccessCacheBeanCache;
  }
  
  /**
   * 
   * @return the cache
   */
  private static GrouperAccessCacheBean grouperAccessCacheBean() {
    GrouperAccessCacheBean grouperAccessCacheBean = grouperAccessCacheBeanCache().get(Boolean.TRUE);
    if (grouperAccessCacheBean == null) {
     
      synchronized(GrouperAccessProvider.class) {
        
        //try again to see if another thread did this in the meantime
        grouperAccessCacheBean = grouperAccessCacheBeanCache().get(Boolean.TRUE);
        if (grouperAccessCacheBean == null) {
          
          cacheMisses++;
          
          GrouperAccessCacheBean tempGrouperAccessCacheBean = new GrouperAccessCacheBean();

          List<String> groupNames = listGroupsFromGrouper();
          tempGrouperAccessCacheBean.setGroupNames(groupNames);
          
          Map<String, List<String>> listUsersInGroupFromGrouper = listUsersInGroupFromGrouper(groupNames);
          tempGrouperAccessCacheBean.setListUsersInGroup(listUsersInGroupFromGrouper);
          tempGrouperAccessCacheBean.setListGroupsContainingUser(listGroupsContainingUser(listUsersInGroupFromGrouper));
          
          grouperAccessCacheBean = tempGrouperAccessCacheBean;
          //add to cache
          grouperAccessCacheBeanCache().put(Boolean.TRUE, grouperAccessCacheBean);
          
        } else {
          cacheHits++;
        }
        
      }
      
    } else {
      cacheHits++;
    }
    return grouperAccessCacheBean;
  }
  
  /**
   * list all groups
   * @return the group names (atlassian format)
   */
  private static List<String> listGroupsFromGrouper() {
    
    Map<String, Object> debugMap = new HashMap<String, Object>();
    debugMap.put("operation", "listGroupsFromGrouper");
    
    long startNanos = System.nanoTime();

    try {

      GcFindGroups gcFindGroups = new GcFindGroups();
      
      String folderName = GrouperAtlassianConfig.grouperAtlassianConfig().getRootFolder();
      
      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.setStemName(folderName);
      wsQueryFilter.setStemNameScope("ALL_IN_SUBTREE");
      
      wsQueryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
      
      gcFindGroups.assignQueryFilter(wsQueryFilter);
      
      WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
      
      WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
      
      List<String> atlassianGroups = GrouperAtlassianUtils.convertToAtlassianGroups(wsGroups);
      
      GrouperAtlassianUtils.addToDebugMap(wsFindGroupsResults.getResultMetadata(), debugMap, false);
      
      debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsGroups));
      
      debugMap.put("listSizeAfterSourceFiltering", GrouperClientUtils.length(atlassianGroups));

      //add in auto groups
      for (String autoaddGroup : GrouperAtlassianConfig.grouperAtlassianConfig().getAutoaddConfigGroupToUsers().keySet()) {
        atlassianGroups.add(autoaddGroup);
      }
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, atlassianGroups, "groupsResultList");
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      atlassianGroups= Collections.unmodifiableList(atlassianGroups);
      return atlassianGroups;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsFindGroupsResults) {
          WsFindGroupsResults wsFindGroupsResults = (WsFindGroupsResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsFindGroupsResults != null) {
            GrouperAtlassianUtils.addToDebugMap(wsFindGroupsResults.getResultMetadata(), 
                debugMap, true);
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }

  }
  
  /**
   * list users in group
   * @param groupNames group names to check
   * @return the group names (atlassian format)
   */
  private static Map<String, List<String>> listGroupsContainingUser(Map<String, List<String>> listUsersInGroupFromGrouper) {
    
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    
    for (String groupName : listUsersInGroupFromGrouper.keySet()) {
      
      for (String userName : listUsersInGroupFromGrouper.get(groupName)) {
        
        List<String> groups = result.get(userName);
        
        //see if we have to init it
        if (groups == null) {
          groups = new ArrayList<String>();
          result.put(userName, groups);
        }
        
        groups.add(groupName);
      }
      
    }
    for (String userName : result.keySet()) {
      List<String> newList = Collections.unmodifiableList(result.get(userName));
      result.put(userName, newList);
    }
    result = Collections.unmodifiableMap(result);
    return result;
  }
    
  /**
   * list users in group
   * @param groupNames group names to check
   * @return the group names (atlassian format)
   */
  private static Map<String, List<String>> listUsersInGroupFromGrouper(List<String> groupNames) {
    
    Map<String, Object> debugMap = new HashMap<String, Object>();
    debugMap.put("operation", "listUsersInGroupFromGrouper");
    debugMap.put("groupNameSize", GrouperClientUtils.length(groupNames));
    
    long startNanos = System.nanoTime();

    Map<String, List<String>> result = new HashMap<String, List<String>>();
    if (GrouperClientUtils.length(groupNames) == 0) {
      return result;
    }
    
    try {

      GcGetMembers gcGetMembers = new GcGetMembers();
      gcGetMembers.assignFieldName("members");
      for (String groupName : groupNames) {

        String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupName, debugMap);
        
        gcGetMembers.addGroupName(grouperGroupName);
        
      }
      for (String subjectAttributeName : GrouperAtlassianUtils.subjectAttributeNames(false)) {
        gcGetMembers.addSubjectAttributeName(subjectAttributeName);
      }
      
      for (String sourceId : GrouperAtlassianUtils.sourceIdsToSearch()) {
        gcGetMembers.addSourceId(sourceId);
      }
      
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
      
      GrouperAtlassianUtils.addToDebugMap(wsGetMembersResults.getResultMetadata(), debugMap, false);
      
      int recordsFromGrouper = 0;
      int filteredRecords = 0;
      int autoaddUsers = 0;
      
      Map<String, GrouperAtlassianAutoaddConfig> autoaddConfigGroupToUsers = GrouperAtlassianConfig.grouperAtlassianConfig().getAutoaddConfigGroupToUsers();
      for (int i=0;i<GrouperClientUtils.length(wsGetMembersResults.getResults());i++) {
        
        WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[i];
        recordsFromGrouper += GrouperClientUtils.length(wsGetMembersResult.getWsSubjects());
        
        String grouperGroupName = wsGetMembersResult.getWsGroup().getName();
        String atlassianGroupName = GrouperAtlassianUtils.atlassianGroupName(grouperGroupName);
        List<String> returnedUsers = GrouperAtlassianUtils.convertToAtlassianUsers(wsGetMembersResults.getSubjectAttributeNames(), wsGetMembersResult.getWsSubjects());

        filteredRecords += GrouperClientUtils.length(returnedUsers);
        
        //add autoadd users
        GrouperAtlassianAutoaddConfig grouperAtlassianAutoaddConfig = autoaddConfigGroupToUsers.get(atlassianGroupName);
        
        if (grouperAtlassianAutoaddConfig != null) {
          List<String> users = grouperAtlassianAutoaddConfig.getUsernames();
          for (String newUser : users) {
            if (!returnedUsers.contains(newUser)) {
              autoaddUsers++;
              returnedUsers.add(newUser);
            }
          }
        }
        
        result.put(atlassianGroupName, Collections.unmodifiableList(returnedUsers));
      }
      
      //see if any groups arent there
      for (String atlassianGroupName : autoaddConfigGroupToUsers.keySet()) {
        if (!result.containsKey(atlassianGroupName)) {
          result.put(atlassianGroupName, autoaddConfigGroupToUsers.get(atlassianGroupName).getUsernames());
        }
      }
      
      debugMap.put("listSizeReturnedFromGrouper", recordsFromGrouper);
      
      debugMap.put("autoaddUsers", autoaddUsers);

      debugMap.put("listSizeAfterSourceFiltering", filteredRecords);

      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      result = Collections.unmodifiableMap(result);

      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsGetMembersResults) {
          WsGetMembersResults wsGetMembersResults = (WsGetMembersResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsGetMembersResults != null && GrouperClientUtils.length(wsGetMembersResults.getResults()) > 0) {
            WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
            if (wsGetMembersResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsGetMembersResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }

  }
  
  /**
   * @see com.opensymphony.user.provider.AccessProvider#inGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean inGroup(String username, String groupname) {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "inGroup");
    debugMap.put("username", username);
    debugMap.put("groupname", groupname);

    Boolean result = null;

    
    
    try {

      List<String> users = grouperAccessCacheBean().getListUsersInGroup().get(groupname);
      result = GrouperClientUtils.nonNull(users).contains(username);
      
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return result;
    } catch(RuntimeException re) {

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listGroupsContainingUser(java.lang.String)
   */
  @Override
  public List<String> listGroupsContainingUser(String username) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "listGroupsContainingUser");
    debugMap.put("username", username);

    List<String> resultList = null;

    try {
      
      resultList = GrouperClientUtils.nonNull(grouperAccessCacheBean().getListGroupsContainingUser()).get(username);
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return resultList;
    } catch(RuntimeException re) {

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listUsersInGroup(java.lang.String)
   */
  @Override
  public List<String> listUsersInGroup(String groupname) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "listUsersInGroup");
    debugMap.put("groupname", groupname);

    List<String> resultList = grouperAccessCacheBean().getListUsersInGroup().get(groupname);
    resultList = GrouperClientUtils.nonNull(resultList);
    GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
      
    return resultList;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#removeFromGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean removeFromGroup(String username, String groupname) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "removeFromGroup");
    debugMap.put("username", username);
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      cacheMisses++;

      GcDeleteMember gcDeleteMember = new GcDeleteMember();
      String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);

      gcDeleteMember.assignGroupName(grouperGroupName);
      
      WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(username, debugMap);
      
      gcDeleteMember.addSubjectLookup(wsSubjectLookup);

      WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
      
      WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];

      GrouperAtlassianUtils.addToDebugMap(wsDeleteMemberResult.getResultMetadata(), debugMap, false);
      
      String resultCode = wsDeleteMemberResult.getResultMetadata().getResultCode();
      
      //note, could be an effective member and not get removed... hmmm
      result = !GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE", resultCode);
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      //lets clear some caches
      flushCaches();

      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsDeleteMemberResults) {
          WsDeleteMemberResults wsDeleteMemberResults = (WsDeleteMemberResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsDeleteMemberResults != null && GrouperClientUtils.length(wsDeleteMemberResults.getResults()) > 0) {
            WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
            if (wsDeleteMemberResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsDeleteMemberResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String groupname) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "create");
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      //see if exists
      if (load(groupname, null)) {
        debugMap.put("groupExists", "true");
        result = false;
        cacheHits++;

      } else {
      
        cacheMisses++;

        GcGroupSave gcGroupSave = new GcGroupSave();
        String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);
        
        WsGroupToSave wsGroupToSave = new WsGroupToSave();
        gcGroupSave.addGroupToSave(wsGroupToSave);
        
        wsGroupToSave.setSaveMode("INSERT");
        
        WsGroupLookup wsGroupLookup = new WsGroupLookup(grouperGroupName, null);
        wsGroupToSave.setWsGroupLookup(wsGroupLookup);
        WsGroup wsGroup = new WsGroup();
        wsGroupToSave.setWsGroup(wsGroup);
        wsGroup.setName(grouperGroupName);
        wsGroup.setExtension(groupname);
        wsGroup.setDisplayExtension(groupname);
        wsGroup.setDescription("Automatically created group from Atlassian");
        
        WsGroupSaveResults wsGroupSaveResults = gcGroupSave.execute();
        
        WsGroupSaveResult wsGroupSaveResult = wsGroupSaveResults.getResults()[0];

        GrouperAtlassianUtils.addToDebugMap(wsGroupSaveResult.getResultMetadata(), debugMap, false);
        
        result = true;
        
        {
          List<String> admins = GrouperAtlassianConfig.grouperAtlassianConfig().getAutoAddPrivilegeAdmins();
          String privilegeName = "ADMIN";
          assignPrivilegeToGroup(debugMap, wsGroupLookup, admins, privilegeName);
        }        
        {
          List<String> readers = GrouperAtlassianConfig.grouperAtlassianConfig().getAutoAddPrivilegeReaders();
          String privilegeName = "READ";
          assignPrivilegeToGroup(debugMap, wsGroupLookup, readers, privilegeName);
        }        
        {
          List<String> updaters = GrouperAtlassianConfig.grouperAtlassianConfig().getAutoAddPrivilegeUpdaters();
          String privilegeName = "UPDATE";
          assignPrivilegeToGroup(debugMap, wsGroupLookup, updaters, privilegeName);
        }        

        //lets clear some caches (since not in group list)
        flushCaches();
        
      }

      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }

      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsGroupSaveResults) {
          WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsGroupSaveResults != null && GrouperClientUtils.length(wsGroupSaveResults.getResults()) > 0) {
            WsGroupSaveResult wsGroupSaveResult = wsGroupSaveResults.getResults()[0];
            if (wsGroupSaveResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsGroupSaveResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @param debugMap
   * @param wsGroupLookup
   * @param groupNames
   * @param privilegeName
   */
  private void assignPrivilegeToGroup(Map<String, Object> debugMap,
      WsGroupLookup wsGroupLookup, List<String> groupNames, String privilegeName) {
    if (GrouperClientUtils.length(groupNames) > 0) {
      GcAssignGrouperPrivileges gcAssignGrouperPrivileges = new GcAssignGrouperPrivileges()
        .assignGroupLookup(wsGroupLookup).addPrivilegeName(privilegeName);
      for (String admin : groupNames) {
        gcAssignGrouperPrivileges.addSubjectLookup(new WsSubjectLookup(null, "g:gsa", admin));
      }
      GrouperAtlassianUtils.addToDebugMap(debugMap, groupNames, "privilegeAdd_" + privilegeName);
      gcAssignGrouperPrivileges.assignAllowed(true);

      gcAssignGrouperPrivileges.execute();
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#flushCaches()
   */
  @Override
  public void flushCaches() {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "flushCaches");
    debugMap.put("grouperAccessCacheBeanCacheSize", grouperAccessCacheBeanCache().size(true));

    grouperAccessCacheBeanCache().clear();

    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }

  }

  /**
   * this should return true if group/user exists, false if not
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String name) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "handles");
    debugMap.put("name", name);

    Boolean result = null;

    if (grouperAccessCacheBean().getGroupNames().contains(name)) {
      debugMap.put("fromGroupNameInCache", "true");
      result = true;
    }
    if (result == null && grouperAccessCacheBean().getListGroupsContainingUser().containsKey(name)) {
      debugMap.put("fromUsersInCache", "true");
      result = true;
    }
    //see if this is hardcoded in the config
    if(result == null && GrouperAtlassianConfig.grouperAtlassianConfig().getAutoaddConfigUserToGroups().keySet().contains(name)) {
      debugMap.put("fromAutoAddUsers", "true");
      result = true;
    }
    //see if this is hardcoded in the config
    if(result == null && GrouperAtlassianConfig.grouperAtlassianConfig().getWsUsersToIgnore().contains(name)) {
      debugMap.put("fromWsUsersToIgnore", "true");
      result = true;
    }
    //not sure why atlassian does this, but users are checked too, so see if it is a user
    if (result == null && new GrouperProfileProvider().handles(name)) {
      debugMap.put("fromProfile", "true");
      result = true;
    }
    if (result == null) {
      result = false;
    }
    debugMap.put("result", result);
      
    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
      
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#init(java.util.Properties)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean init(Properties properties) {

        //nothing to do here
    Boolean result = true;
    if (LOG.isDebugEnabled()) {
      StringBuilder logMessage = new StringBuilder("init, properties: ");
      if (properties == null) {
        logMessage.append("null");
      } else {
        for (String propertyName : (Set<String>)(Object)properties.keySet()) {
          logMessage.append(propertyName).append(": ").append(properties.get(propertyName)).append(", ");
        }
      }
      logMessage.append(", result: ").append(result);
      LOG.debug(logMessage);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#list()
   */
  @Override
  public List<String> list() {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "list");

    List<String> resultList = grouperAccessCacheBean().getGroupNames();
    
    GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
    
    return resultList;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#load(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean load(String groupname, Accessor accessor) {
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "load");
    debugMap.put("groupname", groupname);

    boolean result = grouperAccessCacheBean().getGroupNames().contains(groupname);

    debugMap.put("result", result);
    
    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
    
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#remove(java.lang.String)
   */
  @Override
  public boolean remove(String groupname) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "remove");
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      //see if exists
      if (!load(groupname, null)) {
        debugMap.put("groupExists", "false");
        result = false;
        cacheHits++;

      } else {
      
        cacheMisses++;

        GcGroupDelete gcGroupDelete = new GcGroupDelete();
        String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);
        
        gcGroupDelete.addGroupLookup(new WsGroupLookup(grouperGroupName, null));
        
        WsGroupDeleteResults wsGroupDeleteResults = gcGroupDelete.execute();
        
        WsGroupDeleteResult wsGroupDeleteResult = wsGroupDeleteResults.getResults()[0];

        GrouperAtlassianUtils.addToDebugMap(wsGroupDeleteResult.getResultMetadata(), debugMap, false);
        
        result = true;

        //lets clear some caches (since not in group list)
        flushCaches();
      }

      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }

      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsGroupDeleteResults) {
          WsGroupDeleteResults wsGroupDeleteResults = (WsGroupDeleteResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsGroupDeleteResults != null && GrouperClientUtils.length(wsGroupDeleteResults.getResults()) > 0) {
            WsGroupDeleteResult wsGroupDeleteResult = wsGroupDeleteResults.getResults()[0];
            if (wsGroupDeleteResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsGroupDeleteResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#store(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean store(String name, Accessor accessor) {
    //I dont know what this does...
    if (LOG.isDebugEnabled()) {
      LOG.debug("Operation is 'store' '" + name + "' which is a no-op");
    }
    return true;
  }

}

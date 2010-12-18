/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * implement the opensymphony interface that Atlassian uses for products like jira/confluence
 */
public class GrouperAccessProvider implements AccessProvider {

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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /** cache for inGroup, multikey is username and groupname */
  private static ExpirableCache<MultiKey, Boolean> inGroupCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<MultiKey, Boolean> inGroupCache() {
    if (inGroupCache == null) {
      inGroupCache = new ExpirableCache<MultiKey, Boolean>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return inGroupCache;
  }
  
  /** cache for list groups, key is TRUE */
  private static ExpirableCache<Boolean, List<String>> listGroupsCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<Boolean, List<String>> listGroupsCache() {
    if (listGroupsCache == null) {
      listGroupsCache = new ExpirableCache<Boolean, List<String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return listGroupsCache;
  }
  
  /** cache for load group, by atlassian group name */
  private static ExpirableCache<String, Boolean> loadGroupCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<String, Boolean> loadGroupCache() {
    if (loadGroupCache == null) {
      loadGroupCache = new ExpirableCache<String, Boolean>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return loadGroupCache;
  }
  
  /** cache for handles group/subject, by atlassian group name or subject name */
  private static ExpirableCache<String, Boolean> handlesCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<String, Boolean> handlesCache() {
    if (handlesCache == null) {
      handlesCache = new ExpirableCache<String, Boolean>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return handlesCache;
  }
  
  /**
   * @see com.opensymphony.user.provider.AccessProvider#inGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean inGroup(String username, String groupname) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "inGroup");
    debugMap.put("username", username);
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      //lets check the overrides
      GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
      if (GrouperClientUtils.nonNull(grouperAtlassianConfig.getAutoaddConfigUserToGroups().get(username)).contains(groupname)) {
        result = true;
      
        debugMap.put("overrideFromConfig", true);

      } else {

        //check cache
        MultiKey multiKey = new MultiKey(username, groupname);
        Boolean cacheResult = inGroupCache().get(multiKey);
        List<String> groups = listGroupsContainingUserCache().get(username);
        List<String> users = listUsersInGroupCache().get(groupname);
        if (cacheResult != null || groups != null || users != null) {
          if (cacheResult != null) {
            debugMap.put("retrievedFromCache", true);
            result = cacheResult;
          } else if (groups != null) {
            debugMap.put("retrievedFromGroupCache", true);
            result = groups.contains(groupname);
          } else if (users != null) {
            debugMap.put("retrievedFromUserCache", true);
            result = users.contains(username);
          } else {
            throw new RuntimeException("Why are we here?");
          }
          cacheHits++;

        } else {
          if (GrouperAtlassianConfig.grouperAtlassianConfig()
              .getWsUsersToIgnore().contains(username)) {
            result = false;
            debugMap.put("wsUserIsIgnoredViaConfig", true);
          } else {
            cacheMisses++;

            GcHasMember gcHasMember = new GcHasMember();
            String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);
      
            gcHasMember.assignGroupName(grouperGroupName);
            
            WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(username, debugMap);
            
            gcHasMember.addSubjectLookup(wsSubjectLookup);
      
            WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
            
            WsHasMemberResult wsHasMemberResult = wsHasMemberResults.getResults()[0];
      
            GrouperAtlassianUtils.addToDebugMap(wsHasMemberResult.getResultMetadata(), debugMap, false);
            
            String resultCode = wsHasMemberResult.getResultMetadata().getResultCode();
            
            if (GrouperClientUtils.equals("IS_NOT_MEMBER", resultCode)) {
              result = false;
            } else if (GrouperClientUtils.equals("IS_MEMBER", resultCode)) {
              result = true;
            } else {
              throw new RuntimeException("Not expecting resultCode: " + resultCode);
            }
            //lets add to cache
            inGroupCache().put(multiKey, result);
          }
        }
        
      }
      
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return result;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsHasMemberResults) {
          WsHasMemberResults wsHasMemberResults = (WsHasMemberResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsHasMemberResults != null && GrouperClientUtils.length(wsHasMemberResults.getResults()) > 0) {
            WsHasMemberResult wsHasMemberResult = wsHasMemberResults.getResults()[0];
            if (wsHasMemberResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsHasMemberResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /** cache for inGroup, multikey is username and groupname */
  private static ExpirableCache<String, List<String>> listGroupsContainingUserCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<String, List<String>> listGroupsContainingUserCache() {
    if (listGroupsContainingUserCache == null) {
      listGroupsContainingUserCache = new ExpirableCache<String, List<String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return listGroupsContainingUserCache;
  }
  
  /** cache for inGroup, multikey is username and groupname */
  private static ExpirableCache<String, List<String>> listUsersInGroupCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<String, List<String>> listUsersInGroupCache() {
    if (listUsersInGroupCache == null) {
      listUsersInGroupCache = new ExpirableCache<String, List<String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return listUsersInGroupCache;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listGroupsContainingUser(java.lang.String)
   */
  @Override
  public List<String> listGroupsContainingUser(String username) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "listGroupsContainingUser");
    debugMap.put("username", username);

    List<String> resultList = new ArrayList<String>();

    try {

      //check cache
      List<String> groups = listGroupsContainingUserCache().get(username);
      if (groups != null) {
        debugMap.put("retrievedFromGroupCache", true);
        resultList.addAll(groups);
        cacheHits++;

      } else {

        Set<String> resultSet = new LinkedHashSet<String>();
        //lets check the overrides
        GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
        {
          List<String> overrides = GrouperClientUtils.nonNull(grouperAtlassianConfig.getAutoaddConfigUserToGroups().get(username));
          if (GrouperClientUtils.length(overrides) > 0) {
            resultSet.addAll(overrides);
            debugMap.put("overrideFromConfigSize", true);
          }
        }
        if (GrouperAtlassianConfig.grouperAtlassianConfig()
            .getWsUsersToIgnore().contains(username)) {
          debugMap.put("wsUserIsIgnoredViaConfig", true);
        } else {
          cacheMisses++;

          GcGetGroups gcGetGroups = new GcGetGroups();
    
          WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(username, debugMap);
          
          gcGetGroups.addSubjectLookup(wsSubjectLookup);
    
          //constrain to a stem
          String folderRoot = GrouperAtlassianConfig.grouperAtlassianConfig().getRootFolder();
          //uh... I guess we could support groups with a colon in them... hmmm
          gcGetGroups.assignStemScope(StemScope.ALL_IN_SUBTREE);
          gcGetGroups.assignWsStemLookup(new WsStemLookup(folderRoot, null));
          
          WsGetGroupsResults wsGetGroupsResults = gcGetGroups.execute();
          
          WsGetGroupsResult wsGetGroupsResult = wsGetGroupsResults.getResults()[0];
    
          GrouperAtlassianUtils.addToDebugMap(wsGetGroupsResult.getResultMetadata(), debugMap, false);
          
          List<String> returnedGroups = GrouperAtlassianUtils.convertToAtlassianGroups(wsGetGroupsResult.getWsGroups());
          resultSet.addAll(returnedGroups);
        }
        resultList.addAll(resultSet);
        //lets add to cache
        listGroupsContainingUserCache().put(username, Collections.unmodifiableList(resultList));

      }
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return resultList;
    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsGetGroupsResults) {
          WsGetGroupsResults wsGetGroupsResults = (WsGetGroupsResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsGetGroupsResults != null && GrouperClientUtils.length(wsGetGroupsResults.getResults()) > 0) {
            WsGetGroupsResult wsGetGroupsResult = wsGetGroupsResults.getResults()[0];
            if (wsGetGroupsResult != null) {
              GrouperAtlassianUtils.addToDebugMap(wsGetGroupsResult.getResultMetadata(), 
                  debugMap, true);
            }
          }
        }
      }

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listUsersInGroup(java.lang.String)
   */
  @Override
  public List<String> listUsersInGroup(String groupname) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "listUsersInGroup");
    debugMap.put("groupname", groupname);

    List<String> resultList = new ArrayList<String>();

    try {

      //check cache
      List<String> users = listUsersInGroupCache().get(groupname);
      if (users != null) {
        debugMap.put("retrievedFromGroupCache", true);
        resultList.addAll(users);
        cacheHits++;

      } else {

        Set<String> resultSet = new LinkedHashSet<String>();
        //lets check the overrides
        GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
        {
          GrouperAtlassianAutoaddConfig grouperAtlassianAutoaddConfig = grouperAtlassianConfig.getAutoaddConfigGroupToUsers().get(groupname);
          if (grouperAtlassianAutoaddConfig != null) {
            resultSet.addAll(grouperAtlassianAutoaddConfig.getUsernames());
            debugMap.put("overrideFromConfigSize", true);
          }
        }
        cacheMisses++;

        GcGetMembers gcGetMembers = new GcGetMembers();
          
        String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);
        
        gcGetMembers.addGroupName(grouperGroupName);
        
        for (String subjectAttributeName : GrouperAtlassianUtils.subjectAttributeNames(false)) {
          gcGetMembers.addSubjectAttributeName(subjectAttributeName);
        }
        
        for (String sourceId : GrouperAtlassianUtils.sourceIdsToSearch()) {
          gcGetMembers.addSourceId(sourceId);
        }
        
        WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
        
        WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
  
        GrouperAtlassianUtils.addToDebugMap(wsGetMembersResult.getResultMetadata(), debugMap, false);
        
        debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsGetMembersResult.getWsSubjects()));
        
        List<String> returnedUsers = GrouperAtlassianUtils.convertToAtlassianUsers(wsGetMembersResults.getSubjectAttributeNames(), wsGetMembersResult.getWsSubjects());

        debugMap.put("listSizeAfterSourceFiltering", GrouperClientUtils.length(returnedUsers));

        resultSet.addAll(returnedUsers);
        resultList.addAll(resultSet);
        //lets add to cache
        listUsersInGroupCache().put(groupname, Collections.unmodifiableList(resultList));

      }
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return resultList;
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#removeFromGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean removeFromGroup(String username, String groupname) {
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String groupname) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "create");
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      //see if exists
      if (loadHelper(groupname, false)) {
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @param debugMap
   * @param wsGroupLookup
   * @param admins
   * @param privilegeName
   */
  private void assignPrivilegeToGroup(Map<String, Object> debugMap,
      WsGroupLookup wsGroupLookup, List<String> admins, String privilegeName) {
    if (GrouperClientUtils.length(admins) > 0) {
      GcAssignGrouperPrivileges gcAssignGrouperPrivileges = new GcAssignGrouperPrivileges()
        .assignGroupLookup(wsGroupLookup).addPrivilegeName(privilegeName);
      for (String admin : admins) {
        gcAssignGrouperPrivileges.addSubjectLookup(new WsSubjectLookup(null, "g:gsa", admin));
      }
      GrouperAtlassianUtils.addToDebugMap(debugMap, admins, "privilegeAdd_" + privilegeName);
      gcAssignGrouperPrivileges.assignAllowed(true);

      gcAssignGrouperPrivileges.execute();
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#flushCaches()
   */
  @Override
  public void flushCaches() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("flush caches, sizes: " + inGroupCache().size(true) + ", " 
          + listGroupsContainingUserCache().size(true) + ", " + listUsersInGroupCache().size(true));
    }
    inGroupCache().clear();
    listGroupsContainingUserCache().clear();
    listUsersInGroupCache().clear();
    loadGroupCache().clear();
    listGroupsCache().clear();
    handlesCache().clear();
  }

  /**
   * this should return true if group/user exists, false if not
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String name) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "handles");

    Boolean result = null;

    try {

      Boolean handlesBoolean = handlesCache().get(name);
      if (handlesBoolean != null) {
        result = handlesBoolean;
        debugMap.put("fromCache", "true");
        cacheHits++;
      }
      //check if it is in the group cache...
      if (result == null) {
        //try to load a group
        if (loadHelper(name, true)) {
          debugMap.put("foundGroup", "true");
          result = true;
        }
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
      
      if (result == null) {
        
        cacheMisses++;

        GcGetSubjects gcGetSubjects = new GcGetSubjects();
        
        for (String sourceId : GrouperAtlassianUtils.sourceIdsToSearch()) {
          gcGetSubjects.addSourceId(sourceId);
        }
        
        WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(name, debugMap);

        gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
        
        for (String subjectAttributeName : GrouperAtlassianUtils.subjectAttributeNames(false)) {
          gcGetSubjects.addSubjectAttributeName(subjectAttributeName);
        }
        
        WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
        
        WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
        
        GrouperAtlassianUtils.addToDebugMap(wsGetSubjectsResults.getResultMetadata(), debugMap, false);
        
        debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsSubjects));
        
        String[] subjectAttributeNames = wsGetSubjectsResults.getSubjectAttributeNames();
        
        List<String> atlassianUsers = GrouperAtlassianUtils.convertToAtlassianUsers(subjectAttributeNames, wsSubjects);
        
        debugMap.put("listSizeAfterSourceFiltering", GrouperClientUtils.length(atlassianUsers));
        
        result = atlassianUsers.contains(name);
        
        //lets add to cache
        handlesCache().put(name, result);
      }
      
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return result;
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }

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

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "list");

    List<String> resultList = new ArrayList<String>();

    try {

      //check cache
      List<String> listGroups = listGroupsCache().get(Boolean.TRUE);
      if (listGroups != null) {
        debugMap.put("retrievedFromGroupCache", true);
        resultList.addAll(listGroups);
        cacheHits++;

      } else {

        cacheMisses++;

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
        
        resultList.addAll(atlassianGroups);
        
        //lets add to cache
        listGroupsCache().put(Boolean.TRUE, Collections.unmodifiableList(resultList));

      }
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return resultList;
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#load(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean load(String groupname, Accessor accessor) {
    return loadHelper(groupname, true);
  }

  /**
   * load a group, if it is not there, then return false, else true
   * @param groupname 
   * @param useCache 
   * @return true if group is there, false if not
   */
  private boolean loadHelper(String groupname, boolean useCache) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "loadHelper");
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      if (useCache) {
        
        Boolean loadGroupBoolean = loadGroupCache().get(groupname);
        if (loadGroupBoolean != null) {
          result = loadGroupBoolean;
          cacheHits++;
          debugMap.put("loadGroupCacheHit", loadGroupBoolean);

        } else {
          List<String> groupList = listGroupsCache().get(Boolean.TRUE);
          if (groupList != null) {
            cacheHits++;
            debugMap.put("listGroupsCacheHit", loadGroupBoolean);
            result = groupList.contains(groupname);
          }
        }
      }
      if (result == null) {
        
        cacheMisses++;

        GcFindGroups gcFindGroups = new GcFindGroups();
        
        String grouperGroupName = GrouperAtlassianUtils.grouperGroupName(groupname, debugMap);

        gcFindGroups.addGroupName(grouperGroupName);
        
        WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
        
        WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
        
        List<String> atlassianGroups = GrouperAtlassianUtils.convertToAtlassianGroups(wsGroups);
        
        GrouperAtlassianUtils.addToDebugMap(wsFindGroupsResults.getResultMetadata(), debugMap, false);
        
        debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsGroups));
        
        debugMap.put("listSizeAfterSourceFiltering", GrouperClientUtils.length(atlassianGroups));
        
        result = atlassianGroups.contains(groupname);
        
        //lets add to cache
        loadGroupCache().put(groupname, result);
      }
      
      debugMap.put("result", result);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return result;
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

      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }

  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#remove(java.lang.String)
   */
  @Override
  public boolean remove(String groupname) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "remove");
    debugMap.put("groupname", groupname);

    Boolean result = null;

    try {

      //see if exists
      if (!loadHelper(groupname, false)) {
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
      LOG.debug("Operation is 'store' which is a no-op");
    }
    return true;
  }

}

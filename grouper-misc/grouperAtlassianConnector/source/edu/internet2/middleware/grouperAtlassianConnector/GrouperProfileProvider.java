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
 * $Id: GrouperAccessProvider.java 7084 2010-12-18 13:17:59Z mchyzer $
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAtlassianConfig.GrouperAtlassianAutoaddUserConfig;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * implement the opensymphony interface that Atlassian uses for products like jira/confluence
 */
@SuppressWarnings("serial")
public class GrouperProfileProvider {

  /**
   * if we should fail on grouper for failsafe cache
   */
  static boolean failOnGrouperForTestingFailsafeCache = false;

  /** users that were created by atlassian since they are referenced in the DB, but not in the IdM */
  private static Set<String> createdUsers = new HashSet<String>();

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperProfileProvider.class);

  /**
   * count the cache hits for testing
   */
  static long cacheHits = 0;
  /**
   * count the cache misses for unit testing
   */
  static long cacheMisses = 0;
  
  /** cache for list users, key is TRUE */
  private static ExpirableCache<Boolean, List<String>> listUsersCache = null;
  
  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<Boolean, List<String>> listUsersCache() {
    if (listUsersCache == null) {
      listUsersCache = new ExpirableCache<Boolean, List<String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheProfileMinutes());
    }
    return listUsersCache;
  }
  
  /** 
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   */
  private static ExpirableCache<String, Map<String, String>> propertySetCache = null;
  
  /**
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   * @return the cache
   */
  private static ExpirableCache<String, Map<String, String>> propertySetCache() {
    if (propertySetCache == null) {
      propertySetCache = new ExpirableCache<String, Map<String, String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheProfileMinutes());
    }
    return propertySetCache;
  }

  /** cache for list users, key is TRUE */
  private static ExpirableCache<Boolean, List<String>> listUsersFailsafeCache = null;

  /** 
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   */
  private static ExpirableCache<String, Map<String, String>> propertySetFailsafeCache = null;

  /**
   * count the failsafe cache hits for testing
   */
  static long cacheFailsafeHits = 0;

  /**
   * 
   * @param name
   * @return true or false
   */
  public boolean create(String name) {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "create");
    debugMap.put("name", name);

    Boolean result = null;

    try {
      //lets see if it exists...
      if (!this.handles(name)) {
        
        //add it to the created users map and the createdUsers cache
        createdUsers.add(name);
        
        Map<String, String> propertySet = GrouperAtlassianUtils.propertySet(name, name, null);
        
        propertySetCache().put(name, propertySet);
        propertySetFailsafeCache().put(name, propertySet);
        
        List<String> list = new ArrayList<String>(list());
        list.add(name);
        list = Collections.unmodifiableList(list);
        listUsersCache().put(Boolean.TRUE, list);
        listUsersFailsafeCache().put(Boolean.TRUE, list);
        flushCaches();
        result = true;
      } else {
        debugMap.put("foundInHandlesNoNeedToCreate", "true");
        
        result = false;
      }
      
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
   * 
   */
  public void flushCaches() {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "flushCaches");
    debugMap.put("propertySetCacheSize", propertySetCache().size(true));
    debugMap.put("listUsersCacheSize", listUsersCache().size(true));

    propertySetCache().clear();
    listUsersCache().clear();
    
    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
  }

  /**
   * this should return true if user exists, false if not
   * @param theUsername 
   * @return true or false
   */
  public boolean handles(String theUsername) {
    
    return getPropertySetHelper(theUsername, "handles") != null ;

  }

  /**
   * 
   * @param properties
   * @return true or false
   */
  @SuppressWarnings("unchecked")
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
   * 
   * @return the list
   */
  public List<String> list() {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "list");

    List<String> resultList = new ArrayList<String>();
    Set<String> resultSet = new TreeSet<String>(createdUsers);

    try {

      //check cache
      List<String> listUsers = listUsersCache().get(Boolean.TRUE);
      if (listUsers != null) {
        debugMap.put("retrievedFromUserCache", true);
        resultList.addAll(listUsers);
        cacheHits++;

      } else {

        //lets synchronize so we dont have a lot of people doing this at once...
        synchronized(GrouperProfileProvider.class) {
          
          //maybe someone has done this in the meantime...
          listUsers = listUsersCache().get(Boolean.TRUE);
          if (listUsers != null) {
            debugMap.put("retrievedFromUserCache", true);
            resultList.addAll(listUsers);
            cacheHits++;

          } else {
            cacheMisses++;

            //lets check the overrides
            GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
            ExpirableCache<String, Map<String, String>> thePropertySetCache = propertySetCache();
            ExpirableCache<String, Map<String, String>> thePropertySetFailsafeCache = propertySetFailsafeCache();
            {
              Set<String> autoaddUsers = grouperAtlassianConfig.getAutoaddConfigUsers().keySet();
              resultSet.addAll(autoaddUsers);
              
              //lets do the propertysets
              for (String autoaddUserId : autoaddUsers) {
                
                GrouperAtlassianAutoaddUserConfig grouperAtlassianAutoaddUserConfig = grouperAtlassianConfig.getAutoaddConfigUsers().get(autoaddUserId);
                
                String name = grouperAtlassianAutoaddUserConfig.getUserName();
                String email = grouperAtlassianAutoaddUserConfig.getEmail();
                Map<String, String> propertySet = GrouperAtlassianUtils.propertySet(autoaddUserId, name, email);
                
                thePropertySetCache.put(autoaddUserId, propertySet);
                thePropertySetFailsafeCache.put(autoaddUserId, propertySet);
              }
              
            }

            try {
              GcGetMembers gcGetMembers = new GcGetMembers();
                
              String grouperGroupName = grouperAtlassianConfig.getGrouperAllUsersGroup();
              if (GrouperClientUtils.isBlank(grouperGroupName)) {
                grouperGroupName = grouperAtlassianConfig.getRootFolder() 
                  + ":"+ grouperAtlassianConfig.getAtlassianUsersGroupName();
              }
              
              gcGetMembers.addGroupName(grouperGroupName);
              
              for (String subjectAttributeName : GrouperAtlassianUtils.subjectAttributeNames(true)) {
                gcGetMembers.addSubjectAttributeName(subjectAttributeName);
              }
              
              for (String sourceId : GrouperAtlassianUtils.sourceIdsToSearch()) {
                gcGetMembers.addSourceId(sourceId);
              }

              if (failOnGrouperForTestingFailsafeCache) {
                throw new RuntimeException("failOnGrouperForTestingFailsafeCache");
              }

              WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
              
              WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
        
              GrouperAtlassianUtils.addToDebugMap(wsGetMembersResult.getResultMetadata(), debugMap, false);
              
              WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
              debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsSubjects));
              
              String[] subjectAttributeNames = wsGetMembersResults.getSubjectAttributeNames();
              List<String> returnedUsers = GrouperAtlassianUtils.convertToAtlassianUsers(subjectAttributeNames, wsSubjects);
  
              debugMap.put("listSizeAfterSourceFiltering", GrouperClientUtils.length(returnedUsers));
  
              resultSet.addAll(returnedUsers);
              resultList.addAll(resultSet);
              
              resultList = Collections.unmodifiableList(resultList);

              //lets add to cache
              listUsersFailsafeCache().put(Boolean.TRUE, resultList);
              
              Map<String, Map<String, String>> propertySetMap = GrouperAtlassianUtils.convertToAtlassianPropertySets(subjectAttributeNames, wsSubjects);
              
              //add that to cache too
              for (String userId: propertySetMap.keySet()) {
                thePropertySetCache.put(userId, propertySetMap.get(userId));
                thePropertySetFailsafeCache.put(userId, propertySetMap.get(userId));
              }

              
            } catch (RuntimeException re) {
              
              resultList = listUsersFailsafeCache().get(Boolean.TRUE);
              if (resultList == null) {
                throw re;
              }
              LOG.error("Error from grouper", re);
              cacheFailsafeHits++;

            }
            //lets add to cache
            listUsersCache().put(Boolean.TRUE, resultList);


            {
              //add created people
              for (String principalName : createdUsers) {
                Map<String, String> propertySet = GrouperAtlassianUtils.propertySet(principalName, principalName, null);
                
                if (thePropertySetCache.get(principalName) == null) {
                  thePropertySetCache.put(principalName, propertySet);
                }
                if (thePropertySetFailsafeCache.get(principalName) == null) {
                  thePropertySetFailsafeCache.put(principalName, propertySet);
                }
              }
            }
            

          }
        }
      }
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
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

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * 
   * @param username
   * @return true or false
   */
  public boolean load(String username) {
    return getPropertySetHelper(username, "load") != null;
  }

  /**
   * 
   * @param username
   * @return the map
   */
  public Map<String, String> getPropertySet(String username) {
    return this.getPropertySetHelper(username, "getPropertySet");
  }
  
  /**
   * @param username 
   * @param operation 
   * @return the proeprty set or null
   */
  private Map<String, String> getPropertySetHelper(String username, String operation) {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", operation);
    debugMap.put("username", username);

    //lets get from cache first
    this.list();
    
    Map<String, String> propertySet = propertySetCache().get(username);
    boolean fromFailsafeCache = false;
    try {
      
      //check if null, but not the null property set
      if (propertySet != null) {
        
        debugMap.put("retrievedFromMap<String, String>Cache", true);
        cacheHits++;
  
      } else {

        cacheMisses++;
        
        GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
        GrouperAtlassianAutoaddUserConfig grouperAtlassianAutoaddUserConfig = grouperAtlassianConfig
          .getAutoaddConfigUsers().get(username);
        
        if (grouperAtlassianAutoaddUserConfig != null) {
          String name = grouperAtlassianAutoaddUserConfig.getUserName();
          String email = grouperAtlassianAutoaddUserConfig.getEmail();
          propertySet = GrouperAtlassianUtils.propertySet(username, name, email);
        } else {
          
          try {
            GcGetSubjects gcGetSubjects = new GcGetSubjects();
            
            for (String sourceId : GrouperAtlassianUtils.sourceIdsToSearch()) {
              gcGetSubjects.addSourceId(sourceId);
            }
            
            WsSubjectLookup wsSubjectLookup = GrouperAtlassianUtils.wsSubjectLookup(username, debugMap);
    
            gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
            
            for (String subjectAttributeName : GrouperAtlassianUtils.subjectAttributeNames(true)) {
              gcGetSubjects.addSubjectAttributeName(subjectAttributeName);
            }
  
            //see if we are restricting by group name
            if (grouperAtlassianConfig.isRequireGrouperAllUsersGroupForLookups()) {
              String groupName = grouperAtlassianConfig.getGrouperAllUsersGroup();
              if (GrouperClientUtils.isBlank(groupName)) {
                groupName = grouperAtlassianConfig.getRootFolder() 
                  + ":"+ grouperAtlassianConfig.getAtlassianUsersGroupName();
              }
              gcGetSubjects.assignGroupLookup(new WsGroupLookup(groupName, null));
            }
            
            if (failOnGrouperForTestingFailsafeCache) {
              throw new RuntimeException("failOnGrouperForTestingFailsafeCache");
            }
            
            WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
            
            WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
            
            GrouperAtlassianUtils.addToDebugMap(wsGetSubjectsResults.getResultMetadata(), debugMap, false);
            
            debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsSubjects));
            
            String[] subjectAttributeNames = wsGetSubjectsResults.getSubjectAttributeNames();
            
            if (GrouperClientUtils.length(wsGetSubjectsResults.getWsSubjects()) == 1) {
              propertySet = GrouperAtlassianUtils.convertToAtlassianPropertySet(username, subjectAttributeNames, 
                  wsGetSubjectsResults.getWsSubjects()[0], GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs());
            } else if (createdUsers.contains(username)) {
              //remember we dont have to keep looking for a while
              propertySet = GrouperAtlassianUtils.propertySet(username, username, null);
              
            }
          } catch (RuntimeException re) {
            propertySet = propertySetFailsafeCache().get(username);
            if (propertySet == null) {
              throw re;
            }
            fromFailsafeCache = true;
            LOG.error("Error from grouper", re);
            cacheFailsafeHits++;

          }
        }
        
        //add to cache, but with special value if null so we dont have to keep looking...
        Map<String, String> cachePropertySet = GrouperClientUtils.defaultIfNull(propertySet, new HashMap<String, String>());
        propertySetCache().put(username, cachePropertySet);
        if (!fromFailsafeCache) {
          propertySetFailsafeCache().put(username, cachePropertySet);
        }
      }
      
      //if it is null, then it does not exist, I wonder if we should return a null one... hmmm
      if (propertySet == null || propertySet.size() == 0) {
        debugMap.put("propertySetIsNull", true);
        propertySet = null;
      } else {
        debugMap.put("fullName", propertySet.get("fullName"));
        debugMap.put("email", propertySet.get("email"));
      }
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return propertySet;

    } catch(RuntimeException re) {

      if (re instanceof GcWebServiceError) {
        if (((GcWebServiceError)re).getContainerResponseObject() instanceof WsGetSubjectsResults) {
          WsGetSubjectsResults wsGetSubjectsResults = (WsGetSubjectsResults)((GcWebServiceError)re).getContainerResponseObject();
          if (wsGetSubjectsResults != null) {
            GrouperAtlassianUtils.addToDebugMap(wsGetSubjectsResults.getResultMetadata(), 
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
   * 
   * @return the cache
   */
  private static ExpirableCache<Boolean, List<String>> listUsersFailsafeCache() {
    if (listUsersFailsafeCache == null) {
      listUsersFailsafeCache = new ExpirableCache<Boolean, List<String>>(
          GrouperAtlassianConfig.grouperAtlassianConfig().getCacheFailsafeMinutes());
    }
    return listUsersFailsafeCache;
  }

  /**
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   * @return the cache
   */
  private static ExpirableCache<String, Map<String, String>> propertySetFailsafeCache() {
    if (propertySetFailsafeCache == null) {
      propertySetFailsafeCache = new ExpirableCache<String, Map<String, String>>(
          GrouperAtlassianConfig.grouperAtlassianConfig().getCacheFailsafeMinutes());
    }
    return propertySetFailsafeCache;
  }

}

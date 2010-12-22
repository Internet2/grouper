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

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.ProfileProvider;

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
public class GrouperCredentialsProvider implements ProfileProvider {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperCredentialsProvider.class);

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
      listUsersCache = new ExpirableCache<Boolean, List<String>>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return listUsersCache;
  }
  
  /** 
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   */
  private static ExpirableCache<String, PropertySet> propertySetCache = null;
  
  /**
   * cache for list users, key is atlassian username, value is the map property set of name and email 
   * @return the cache
   */
  private static ExpirableCache<String, PropertySet> propertySetCache() {
    if (propertySetCache == null) {
      propertySetCache = new ExpirableCache<String, PropertySet>(GrouperAtlassianConfig.grouperAtlassianConfig().getCacheMinutes());
    }
    return propertySetCache;
  }
  
  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String name) {
    LOG.error("You cannot create here '" + name + "', information is read from the source system via Grouper: " + name);
    throw new RuntimeException("You cannot create here, information is read from the source system via Grouper");
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#flushCaches()
   */
  @Override
  public void flushCaches() {
    
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "flushCaches");
    debugMap.put("propertySetCache", propertySetCache().size(true));

    propertySetCache().clear();
    listUsersCache().clear();

    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
  }

  /**
   * this should return true if user exists, false if not
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String username) {
    
    return getPropertySetHelper(username, "handles") != null;

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

    List<String> resultList = new ArrayList<String>();

    try {

      //check cache
      List<String> listUsers = listUsersCache().get(Boolean.TRUE);
      if (listUsers != null) {
        debugMap.put("retrievedFromUserCache", true);
        resultList.addAll(listUsers);
        cacheHits++;

      } else {

        //lets synchronize so we dont have a lot of people doing this at once...
        synchronized(GrouperCredentialsProvider.class) {
          
          //maybe someone has done this in the meantime...
          listUsers = listUsersCache().get(Boolean.TRUE);
          if (listUsers != null) {
            debugMap.put("retrievedFromUserCache", true);
            resultList.addAll(listUsers);
            cacheHits++;

          } else {
            cacheMisses++;

            Set<String> resultSet = new LinkedHashSet<String>();
            
            //lets check the overrides
            GrouperAtlassianConfig grouperAtlassianConfig = GrouperAtlassianConfig.grouperAtlassianConfig();
            ExpirableCache<String, PropertySet> thePropertySetCache = propertySetCache();
            {
              Set<String> autoaddUsers = grouperAtlassianConfig.getAutoaddConfigUsers().keySet();
              resultSet.addAll(autoaddUsers);
              
              //lets do the propertysets
              for (String autoaddUserId : autoaddUsers) {
                
                GrouperAtlassianAutoaddUserConfig grouperAtlassianAutoaddUserConfig = grouperAtlassianConfig.getAutoaddConfigUsers().get(autoaddUserId);
                
                String name = grouperAtlassianAutoaddUserConfig.getUserName();
                String email = grouperAtlassianAutoaddUserConfig.getEmail();
                PropertySet propertySet = GrouperAtlassianUtils.propertySet(autoaddUserId, name, email);
                
                thePropertySetCache.put(autoaddUserId, propertySet);
                
              }
              
            }

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
            
            //lets add to cache
            listUsersCache().put(Boolean.TRUE, Collections.unmodifiableList(resultList));

            Map<String, PropertySet> propertySetMap = GrouperAtlassianUtils.convertToAtlassianPropertySets(subjectAttributeNames, wsSubjects);
            
            //add that to cache too
            for (String userId: propertySetMap.keySet()) {
              thePropertySetCache.put(userId, propertySetMap.get(userId));
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
   * @see com.opensymphony.user.provider.UserProvider#load(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean load(String username, Accessor accessor) {
    return getPropertySetHelper(username, "load") != null;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#remove(java.lang.String)
   */
  @Override
  public boolean remove(String name) {
    LOG.error("You cannot remove here: '" + name + "', information is read from the source system via Grouper: " + name);
    throw new RuntimeException("You cannot remove here, information is read from the source system via Grouper");
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#store(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean store(String name, Accessor accessor) {

    LOG.error("You cannot store here: '" + name + "', information is read from the source system via Grouper");
    throw new RuntimeException("You cannot store here, information is read from the source system via Grouper: " + name);

  }

  /**
   * @see com.opensymphony.user.provider.ProfileProvider#getPropertySet(String)
   */
  @Override
  public PropertySet getPropertySet(String username) {
    return this.getPropertySetHelper(username, "getPropertySet");
  }
  
  /**
   * @param username 
   * @param operation 
   * @return the proeprty set or null
   */
  private PropertySet getPropertySetHelper(String username, String operation) {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", operation);
    debugMap.put("username", username);

    //lets get from cache first
    this.list();
    
    PropertySet propertySet = propertySetCache().get(username);

    try {
      
      //check if null, but not the null property set
      if (propertySet != null) {
        
        debugMap.put("retrievedFromPropertySetCache", true);
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
          
          
          WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
          
          WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
          
          GrouperAtlassianUtils.addToDebugMap(wsGetSubjectsResults.getResultMetadata(), debugMap, false);
          
          debugMap.put("listSizeReturnedFromGrouper", GrouperClientUtils.length(wsSubjects));
          
          String[] subjectAttributeNames = wsGetSubjectsResults.getSubjectAttributeNames();
          
          if (GrouperClientUtils.length(wsGetSubjectsResults.getWsSubjects()) == 1) {
            propertySet = GrouperAtlassianUtils.convertToAtlassianPropertySet(username, subjectAttributeNames, 
                wsGetSubjectsResults.getWsSubjects()[0], GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs());
          }
          
        }
        
        //add to cache, but with special value if null so we dont have to keep looking...
        PropertySet cachePropertySet = GrouperClientUtils.defaultIfNull(propertySet, GrouperAtlassianUtils.NULL_PROPERTY_SET);
        propertySetCache().put(username, cachePropertySet);
        
        
      }
      
      //if it is null, then it does not exist, I wonder if we should return a null one... hmmm
      if (propertySet == null || propertySet == GrouperAtlassianUtils.NULL_PROPERTY_SET) {
        debugMap.put("propertySetIsNull", true);
        propertySet = null;
      } else {
        debugMap.put("fullName", propertySet.getString("fullName"));
        debugMap.put("email", propertySet.getString("email"));
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

}

package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class TeamDynamixApiCommands {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TeamDynamixApiCommands.class);


  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix, int[] returnCode) {

    return executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404, 429), returnCode, null);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(TeamDynamixMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(TeamDynamixMockServiceHandler.doNotLogParameters);

    String bearerToken = TeamDynamixExternalSystem
        .retrieveBearerTokenForTeamDynamixConfigId(debugMap, configId);
    
    String url = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.teamDynamix." + configId + ".url");
    
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    // in a nextLink, url is specified, so it might not have a prefix of the resourceEndpoint
    if (!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    
    String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.teamDynamix." + configId + ".proxyUrl");
    String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.teamDynamix." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);

    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
    grouperHttpCall.assignBody(body);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssuesSleepMillis(70*1000L); // 1 min and 10 secs
    
    grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {
      
      @Override
      public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
        
        String responseBody = httpClient.getResponseBody();
        boolean isThrottle = false;
        //sometimes it throttles but it also has a response so we're going to use the response if it has
        if (StringUtils.isNotBlank(responseBody) && 
            StringUtils.contains(responseBody, "Please try again later.") &&
            !StringUtils.contains(responseBody, "Please try again later.[{")) {
          isThrottle = true;
        }
        if (isThrottle) {                
          GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
        }
        return isThrottle;
      }
    });
    
    grouperHttpCall.executeRequest();
    
    int code = -1;
    String json = null;

    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
      
      if (StringUtils.isNotBlank(json) && json.contains("Please try again later.[{")) {
        int startOfJson = json.indexOf("[{");
        json =  json.substring(startOfJson);
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
      return rootNode;
    } catch (Exception e) {
//      System.out.println("Error parsing response: '" + json + "'");
//      return null;
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  
  /**
   * 
   * @param oneResponse
   * @param statusCode
   * @param bodyNode
   * @return
   */
  private static StringBuilder buildError(JsonNode oneResponse, Integer statusCode, JsonNode bodyNode) {
    StringBuilder error = new StringBuilder("statusCode = "+statusCode);
    
    if (bodyNode != null) {
      JsonNode errorNode = GrouperUtil.jsonJacksonGetNode(bodyNode, "error");
      if (errorNode != null) {
        String errorCode = GrouperUtil.jsonJacksonGetString(errorNode, "code");
        if (StringUtils.isNotBlank(errorCode)) {
          error.append(", errorCode = "+errorCode);
        }
        
        String errorMessage = GrouperUtil.jsonJacksonGetString(errorNode, "message");
        if (StringUtils.isNotBlank(errorMessage)) {
          error.append(", errorMessage = "+errorMessage);
        }
      }
    }
    return error;
  }
  
  
  /**
   * create a membership
   * @param grouperTeamDynamixGroup
   * @return the result
   */
  public static void createTeamDynamixMemberships(String configId,
      String groupId, Collection<String> userIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    Map<MultiKey, Exception> groupIdUserIdToException = new HashMap<>();

    debugMap.put("method", "createTeamDynamixMemberships");

    //api/groups/{id}/members?isPrimary={isPrimary}&isNotified={isNotified}&isManager={isManager} 
    
    
    long startTime = System.nanoTime();

    try {

      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      for (String userId: userIds) {
        arrayNode.add(userId);
      }
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNode);
      
      String urlSuffix = "api/groups/"+groupId+"/members";

      executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
    
  }
  
  
  public static List<TeamDynamixGroup> retrieveTeamDynamixGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<TeamDynamixGroup> results = new ArrayList<TeamDynamixGroup>();

    debugMap.put("method", "retrieveTeamDynamixGroups");

    long startTime = System.nanoTime();
    
    try {
      
      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("IsActive", true);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/groups/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode groupsArray = (ArrayNode) jsonNode;
      
      for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
        JsonNode groupNode = groupsArray.get(i);
        TeamDynamixGroup grouperTeamDynamixGroup = TeamDynamixGroup.fromJson(groupNode);
        if (grouperTeamDynamixGroup != null) {
          results.add(grouperTeamDynamixGroup);
        }
      }


    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
    debugMap.put("size", GrouperClientUtils.length(results));

    return results;
  }

  /**
   * delete memberships
   */
  public static void deleteTeamDynamixMemberships(String configId, List<ProvisioningMembership> membershipsToDelete) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteTeamDynamixMemberships");

    long startTime = System.nanoTime();
    
    try {
      
      Map<String, Set<String>> groupToMembers = new HashMap<>();
      for (ProvisioningMembership provisioningMembership: membershipsToDelete) {
        
        String groupId = provisioningMembership.getProvisioningGroupId();
        if (groupToMembers.containsKey(groupId)) {
          groupToMembers.get(groupId).add(provisioningMembership.getProvisioningEntityId());
        } else {
          
          Set<String> members = new HashSet<>();
          members.add(provisioningMembership.getProvisioningEntityId());
          groupToMembers.put(provisioningMembership.getProvisioningGroupId(), members);
          
        }
        
      }
      
      for (String groupId: groupToMembers.keySet()) {
        
        ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();

        for (String userId: groupToMembers.get(groupId)) {
          arrayNode.add(userId);
        }
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNode);
        
        String urlSuffix = "api/groups/"+groupId+"/members";

        executeMethod(debugMap, "DELETE", configId, urlSuffix, GrouperUtil.toSet(200), 
            new int[] { -1 }, jsonStringToSend);
        
      }
     

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
  
  }

  private static int retrieveSecondsToSleep(JsonNode oneResponse) {
    int secondsToSleep = 155;
    
    if (oneResponse != null) {
      return secondsToSleep;
    }
    try {
      
      JsonNode headers = GrouperUtil.jsonJacksonGetNode(oneResponse, "headers");
      String retryAfter = GrouperUtil.jsonJacksonGetString(headers, "Retry-After");
      secondsToSleep = GrouperUtil.intValue(retryAfter, 155);
      
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Problem with: "+oneResponse, e);
      }
    }
    
    return secondsToSleep;
  }
  
  public static void updateTeamDynamixUserStatus(String configId, 
      String userId, boolean newStatus) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateTeamDynamixUserStatus");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "PUT", configId, "/api/people/" + userId+"/isactive?status="+newStatus,
          GrouperUtil.toSet(200), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
  }
  
  /**
   * create a user
   * @param TeamDynamixUser
   * @return the result
   */
  public static String createTeamDynamixUser(String configId,
      TeamDynamixUser teamDynamixUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createTeamDynamixUser");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = teamDynamixUser.toJson(null);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      int[] returnCode = new int[] { -1 };
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/api/people",
          GrouperUtil.toSet(200, 201, 400), returnCode, jsonStringToSend);
      
      if (returnCode[0] == 400 && jsonNode != null && jsonNode.has("Message")
          && StringUtils.contains(GrouperUtil.jsonJacksonGetString(jsonNode, "Message"), "Username must be unique")) {
       
        TeamDynamixUser teamDynamixUserBySearchTerm = retrieveTeamDynamixUserBySearchTerm(configId, "username", teamDynamixUser.getUserName(), false);
        if (teamDynamixUserBySearchTerm != null) {
          updateTeamDynamixUserStatus(configId, teamDynamixUserBySearchTerm.getId(), true);
          return teamDynamixUserBySearchTerm.getId();
        } 
        
        throw new RuntimeException("How is it possible to get username already exists but then not find the user when searching");
        
      }
      
      TeamDynamixUser teamDynamixUserResult = TeamDynamixUser.fromJson(jsonNode);
      return teamDynamixUserResult.getId();
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  
  /**
   * create a group
   * @param teamDynamixGroup
   * @return the result
   */
  public static TeamDynamixGroup createTeamDynamixGroup(String configId,
      TeamDynamixGroup teamDynamixGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {
      
      //maybe the group already exists with the same name
      TeamDynamixGroup groupByName = retrieveTeamDynamixGroupByName(configId,
          teamDynamixGroup.getName(), null);
      
      if (groupByName == null) {
        JsonNode jsonToSend = teamDynamixGroup.toJson(null);
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
        
        JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/groups", GrouperUtil.toSet(201), 
            new int[] { -1 }, jsonStringToSend);

        TeamDynamixGroup teamDynamixGroupResult = TeamDynamixGroup.fromJson(jsonNode);
        
        return teamDynamixGroupResult;
      }
      
      if (groupByName.getActive() == null || groupByName.getActive() == false) {
        
        groupByName.setActive(true);
        groupByName.setDescription(teamDynamixGroup.getDescription());
        
        TeamDynamixGroup teamDynamixGroupResult = updateTeamDynamixGroup(configId, groupByName, null);
        return teamDynamixGroupResult;
      }
      
      return groupByName;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  public static TeamDynamixUser retrieveTeamDynamixUser(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/api/people/" + id;

      int[] returnCode = new int[] { -1 };
      
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null);
      
      if (returnCode[0] == 404) {
        return null;
      }
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (userNode == null) {
        return null;
      }
      TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromJson(userNode);

      return teamDynamixUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param searchTerm
   * @return
   */
  public static TeamDynamixUser retrieveTeamDynamixUserBySearchTerm(String configId,
      String fieldName,
      String searchTerm, Boolean isActive) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUserByExternalId");

    long startTime = System.nanoTime();
    
    if (StringUtils.isBlank(searchTerm)) {
      return null;
    }

    try {

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      if (isActive != null) {
        jsonJacksonNode.put("IsActive", isActive);
      }
      jsonJacksonNode.put("SearchText", searchTerm);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/people/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode usersArray = (ArrayNode) jsonNode;
      
      if (usersArray == null || usersArray.size() == 0) {
        return null;
      } 
      Iterator<JsonNode> iterator = usersArray.iterator();
      List<TeamDynamixUser> users = new ArrayList<>();
      while (iterator.hasNext()) {
        JsonNode userNode = iterator.next();
        TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromJson(userNode);
        
        if (StringUtils.equals(fieldName, "externalId")) {
          if (StringUtils.equals(teamDynamixUser.getExternalId(), searchTerm)) {
            users.add(teamDynamixUser);
          }
        } else if (StringUtils.equals(fieldName, "username")) {
          if (StringUtils.equals(teamDynamixUser.getUserName(), searchTerm)) {
            users.add(teamDynamixUser);
          }
        } else {
          throw new RuntimeException("Invalid field name.");
        }
        
        
        
      }
      if (users.size() > 1) {
        throw new RuntimeException("How can there be more than one user with the same value in TeamDynamix?? '" + searchTerm + "'");
      }
      return users.get(0);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param externalId
   * @return
   */
  public static List<TeamDynamixUser> retrieveTeamDynamixUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUsers");

    long startTime = System.nanoTime();
    
    try {

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("IsActive", true);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/people/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode usersArray = (ArrayNode) jsonNode;
      
      List<TeamDynamixUser> results = new ArrayList<TeamDynamixUser>();
      
      for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
        JsonNode groupNode = usersArray.get(i);
        TeamDynamixUser grouperTeamDynamixGroup = TeamDynamixUser.fromJson(groupNode);
        if (grouperTeamDynamixGroup != null) {
          results.add(grouperTeamDynamixGroup);
        }
      }
      
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param group id
   * @return the user
   */
  public static TeamDynamixGroup retrieveTeamDynamixGroup(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/groups/" + id;

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null);
      
      if (returnCode[0] == 404) {
        return null;
      }
      
      //lets get the group node
      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (groupNode == null) {
        return null;
      }
      TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromJson(groupNode);

      return teamDynamixGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param groupName
   * @param isActive
   * @return
   */
  public static TeamDynamixGroup retrieveTeamDynamixGroupByName(String configId, 
      String groupName, Boolean isActive) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixGroupByName");

    long startTime = System.nanoTime();

    try {

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      if (isActive != null) {
        jsonJacksonNode.put("IsActive", isActive);
      }
      jsonJacksonNode.put("NameLike", groupName);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/groups/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode groupsArray = (ArrayNode) jsonNode;
      
      if (groupsArray == null || groupsArray.size() == 0) {
        return null;
      } 
      Iterator<JsonNode> iterator = groupsArray.iterator();
      List<TeamDynamixGroup> groups = new ArrayList<>();
      while (iterator.hasNext()) {
        JsonNode groupNode = iterator.next();
        TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromJson(groupNode);
        if (StringUtils.equals(teamDynamixGroup.getName(), groupName)) {
          groups.add(teamDynamixGroup);
        }
      }
      if (groups.size() > 1) {
        throw new RuntimeException("How can there be more than one group with the same name in TeamDynamix?? '" + groupName + "'");
      }
      return groups.get(0);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  public static List<TeamDynamixGroup> retrieveTeamDynamixGroupsByUser(String configId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixGroupsByUser");

    long startTime = System.nanoTime();

    try {
      
      List<TeamDynamixGroup> results = new ArrayList<>();

      String urlSuffix = "/api/people/"+userId+"/groups/";

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);
      
      ArrayNode groupsArray = (ArrayNode)jsonNode;
      
      for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
        JsonNode groupNode = groupsArray.get(i);
        TeamDynamixGroup grouperTeamDynamixGroup = TeamDynamixGroup.fromJson(groupNode);
        if (grouperTeamDynamixGroup != null) {
          results.add(grouperTeamDynamixGroup);
        }
      }
      
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  public static List<TeamDynamixUser> retrieveTeamDynamixUsersByGroup(String configId, String groupId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUsersByGroup");

    long startTime = System.nanoTime();

    try {
      
      List<TeamDynamixUser> results = new ArrayList<>();

      String urlSuffix = "api/groups/"+groupId+"/members";

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);
      
      ArrayNode usersArray = (ArrayNode)jsonNode;
      
      for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
        JsonNode groupNode = usersArray.get(i);
        TeamDynamixUser grouperTeamDynamixGroup = TeamDynamixUser.fromJson(groupNode);
        if (grouperTeamDynamixGroup != null) {
          results.add(grouperTeamDynamixGroup);
        }
      }
      
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  public static void patchTeamDynamixUser(String configId,
      TeamDynamixUser teamDynamixUser, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "patchTeamDynamixUser");

    long startTime = System.nanoTime();

    try {

//      [
//       {"op": "add", "path": "/title", "value": "Updated Title"},
//       {"op": "add", "path": "/accountid", "value": 47},
//       {"op": "add", "path": "/attributes/1234", "value": "New Attribute Value"},
//       {"op": "remove", "path": "/attributes/5678"}
//      ]

      ArrayNode operationsNode = GrouperUtil.jsonJacksonArrayNode();
      
//      if (fieldsToUpdate.containsKey("active")) {
//        throw new UnsupportedOperationException("active field cannnot be modified");
//      }

      for (String fieldToUpdate : fieldsToUpdate.keySet()) {
        ProvisioningObjectChangeAction provisioningObjectChangeAction = fieldsToUpdate.get(fieldToUpdate);
        
        ObjectNode operationNode = GrouperUtil.jsonJacksonNode();
        
        String newValue = null;
        
        if (StringUtils.equals(fieldToUpdate, "FirstName")) {
          newValue = teamDynamixUser.getFirstName();
        } else if (StringUtils.equals(fieldToUpdate, "LastName")) {
          newValue = teamDynamixUser.getLastName();
        } else if (StringUtils.equals(fieldToUpdate, "Company")) {
          newValue = teamDynamixUser.getCompany();
        } else if (StringUtils.equals(fieldToUpdate, "ExternalId")) {
          newValue = teamDynamixUser.getExternalId();
        } else if (StringUtils.equals(fieldToUpdate, "PrimaryEmail")) {
          newValue = teamDynamixUser.getPrimaryEmail();
        } else if (StringUtils.equals(fieldToUpdate, "SecurityRoleId")) {
          newValue = teamDynamixUser.getSecurityRoleId();
        } else if (StringUtils.equals(fieldToUpdate, "UserName")) {
          newValue = teamDynamixUser.getUserName();
        }
        
        switch (provisioningObjectChangeAction) {
          case insert:
            operationNode.put("op", "add");
            operationNode.put("value", newValue);
            break;
          case update:
            operationNode.put("op", "replace");
            operationNode.put("value", newValue);
            break;
          case delete:
            operationNode.put("op", "remove");
            break;
          default:
            throw new RuntimeException("Not expecting object change: " + provisioningObjectChangeAction);
        }
        
        operationNode.put("path", fieldToUpdate);
        operationsNode.add(operationNode);
        
      }
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(operationsNode);

      executeMethod(debugMap, "PATCH", configId, "/api/people/"+ GrouperUtil.escapeUrlEncode(teamDynamixUser.getId()),
          GrouperUtil.toSet(200, 204), new int[] { -1 }, jsonStringToSend);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  
  /**
   * update a group
   * @param configId 
   * @param teamDynamixGroup
   * @param fieldsToUpdate 
   * @return the result
   */
  public static TeamDynamixGroup updateTeamDynamixGroup(String configId,
      TeamDynamixGroup teamDynamixGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {

      String id = teamDynamixGroup.getId();
      
      JsonNode jsonToSend = teamDynamixGroup.toJson(fieldsToUpdate);
      
      TeamDynamixGroup updatedTeamDynamixGroup = null;
      
      if (jsonToSend.size() > 0) {
        String urlSuffix = "/api/groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

        updatedTeamDynamixGroup = TeamDynamixGroup.fromJson(jsonNode);
      }

      return updatedTeamDynamixGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
 

}

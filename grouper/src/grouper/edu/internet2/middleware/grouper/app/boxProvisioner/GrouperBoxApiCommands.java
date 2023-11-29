package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.google.GrouperGoogleLog;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GrouperBoxApiCommands {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperBoxApiCommands.class);

  public static void main(String[] args) {

//    BoxMockServiceHandler.dropBoxMockTables();
//    BoxMockServiceHandler.ensureBoxMockTables();
    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.boxConnector.box1.loginEndpoint",
//        "http://localhost/f3/login.microsoftonline.com/");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.boxConnector.box1.resourceEndpoint",
//        "http://localhost/f3/graph.microsoft.com/v1.0/");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
      "grouper.boxConnector.box1.loginEndpoint",
      "http://localhost:8400/grouper/mockServices/box/auth");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
      "grouper.boxConnector.box1.resourceEndpoint",
      "http://localhost:8400/grouper/mockServices/box");

    //GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("BoxProvA");
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = BoxGrouperExternalSystem.retrieveApiConnectionForProvisioning("box1");

    //  List<GrouperBoxGroup> grouperBoxGroups = retrieveBoxGroups("box1");
    //  
    //  for (GrouperBoxGroup grouperBoxGroup : grouperBoxGroups) {
    //    System.out.println(grouperBoxGroup);
    //  }
    
    //  GrouperBoxGroup grouperBoxGroup = retrieveBoxGroup("box1", "id", "1153755cfa554297a29cfc332e1bef9f");
    //  GrouperBoxGroup grouperBoxGroup = retrieveBoxGroup("box1", "displayName", "myDisplayName2");
    //  System.out.println(grouperBoxGroup);

//    for (int i=0;i<5;i++) {
//      {
//        GrouperBoxUser grouperBoxUser = new GrouperBoxUser();
//        grouperBoxUser.setAccountEnabled(true);
//        grouperBoxUser.setDisplayName("myDispName" + i);
//        grouperBoxUser.setId(GrouperUuid.getUuid());
//        grouperBoxUser.setMailNickname("a" + i + "@b.c");
//        grouperBoxUser.setOnPremisesImmutableId((12345678+i) + "");
//        grouperBoxUser.setUserPrincipalName("jsmith" + 1);
//        HibernateSession.byObjectStatic().save(grouperBoxUser);
//        createBoxMembership("box1", "dcba5d8d7986432db23a0342887e8fba", grouperBoxUser.getId());
//      }
//      
//    }
    
    //  Set<String> groupIds = retrieveBoxUserGroups("box1", "84ec56bad4da4430ae5f2998ea283dfc");
    //  for (String groupId : groupIds) {
    //    System.out.println(groupId);
    //  }

    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("boxGetMembershipPagingSize", "2");
    //
    //    Set<String> userIds = retrieveBoxGroupMembers("box1", "dcba5d8d7986432db23a0342887e8fba");
    //    for (String userId : userIds) {
    //      System.out.println(userId);
    //    }
        
    
    //  {
    //    GrouperBoxUser grouperBoxUser = new GrouperBoxUser();
    //    grouperBoxUser.setAccountEnabled(true);
    //    grouperBoxUser.setDisplayName("myDispName2");
    //    grouperBoxUser.setId(GrouperUuid.getUuid());
    //    grouperBoxUser.setMailNickname("a@b.d");
    //    grouperBoxUser.setOnPremisesImmutableId("12345679");
    //    grouperBoxUser.setUserPrincipalName("kjohnson");
    //    HibernateSession.byObjectStatic().save(grouperBoxUser);
    //  }
    
    //  List<GrouperBoxUser> grouperBoxUsers = retrieveBoxUsers("box1");
    //
    //  for (GrouperBoxUser grouperBoxUser : grouperBoxUsers) {
    //    System.out.println(grouperBoxUser);
    //  }
    
    //GrouperBoxUser grouperBoxUser = retrieveBoxUser("box1", "userPrincipalName", "jsmith");
    //GrouperBoxUser grouperBoxUser = retrieveBoxUser("box1", "displayName", "myDispName");
    //System.out.println(grouperBoxUser);
    
    //  createBoxMembership("box1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //createBoxMemberships("box1", "dcba5d8d7986432db23a0342887e8fba", GrouperUtil.toSet("1db63cda166a4640b9ef1a0808f90873", "b1dda78d8d42461a93f8b471f26b682e"));
    
    //  deleteBoxMembership("box1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup();
    grouperBoxGroup.setDescription("myDescription3");
//    grouperBoxGroup.setDisplayName("myDisplayName3");
//    grouperBoxGroup.setMailNickname("myMailNick3");
//    grouperBoxGroup.setGroupTypeUnified(true); 
    
    Map<GrouperBoxGroup, Set<String>> map = new HashMap<>();
    map.put(grouperBoxGroup, null);
//    createBoxGroups("box1", map);

    //deleteBoxGroup("box1", "fa356bb8ddb14600be7994cd7b5239a7");
    
//    GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup();
//    grouperBoxGroup.setId("dcba5d8d7986432db23a0342887e8fba");
//    grouperBoxGroup.setDisplayName("myDisplayName4");
//    grouperBoxGroup.setMailNickname("whatever");
//    updateBoxGroup("box1", grouperBoxGroup, GrouperUtil.toSet("displayName"));
  }

  

  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix, int[] returnCode) {

    return executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404, 429), returnCode, null);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(BoxMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(BoxMockServiceHandler.doNotLogParameters);

    String bearerToken = BoxGrouperExternalSystem.retrieveAccessTokenForBoxConfigId(debugMap, configId);
    
    String baseUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".baseUrl");
    
//    String baseUrl = "https://api.box.com/2.0";
    String url = baseUrl;
    
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
    
    String proxyHost = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyHost");
    String proxyType = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyType");
    String proxyPort = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyPort");
    
    String proxyUrl  = null;
    
    if(StringUtils.isNotBlank(proxyHost)) {
      proxyUrl = proxyHost;
      if (StringUtils.isNotBlank(proxyPort)) {
        proxyUrl = proxyUrl + ":"+ proxyPort;
      }
    }
    
    if (StringUtils.isNotBlank(proxyUrl)) {
      grouperHttpCall.assignProxyUrl(proxyUrl);
    }
    
    if (StringUtils.isNotBlank(proxyType)) {
      grouperHttpCall.assignProxyType(proxyType);
    }
    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
    grouperHttpCall.assignBody(body);
    grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {
      
      @Override
      public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
        String body = httpClient.getResponseBody();
        try {
          if (StringUtils.isNotBlank(body) && body.contains("error") && body.contains("429")) {
            
            // {"type":"error","status":429,"code":"rate_limit_exceeded",
            // "help_url":"http://developers.box.com/docs/#errors",
            // "message":"Request rate limit exceeded, please try again later",
            // "request_id":"j09ok2hkbixiyeko"}
            JsonNode node = GrouperUtil.jsonJacksonNode(body);
            Integer status = GrouperUtil.jsonJacksonGetInteger(node, "status");
            boolean isThrottle = status != null && status == 429;
            if (isThrottle) {                
              GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
              return isThrottle;
            }
            
          }
        } catch(Exception e) {
          LOG.error("Error: " + debugMap.get("url") + ", " + grouperHttpCall.getResponseCode() + ", " + body, e);
        }
      
        boolean isThrottle = grouperHttpCall.getResponseCode() == 429;
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
      
      String type = GrouperUtil.jsonJacksonGetString(rootNode, "type");
      if (StringUtils.equals(type, "error")) {
        throw new RuntimeException(
            "Error, http response code: " + code
                + ", url: '" + debugMap.get("url") + "', " + json);
      }
      
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  
  /**
   * create a group
   * @param configId
   * @param grouperBoxGroup
   * @return the result
   */
  public static GrouperBoxGroup createBoxGroup(String configId,
      GrouperBoxGroup grouperBoxGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createBoxGroup");

    long startTime = System.nanoTime();

    try {

      //TODO pass in field
      JsonNode jsonToSend = grouperBoxGroup.toJson(null);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      int[] returnCode = new int[] {-1};
      
      JsonNode groupNode = executeMethod(debugMap, "POST", configId, "/groups",
          GrouperUtil.toSet(201, 429), returnCode, jsonStringToSend);
      
      GrouperBoxGroup grouperBoxGroupResult = GrouperBoxGroup.fromJson(groupNode);

      return grouperBoxGroupResult; 
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }
  
  /**
   * create a user
   * @param configId
   * @param grouperBoxUser
   * @return the result
   */
  public static GrouperBoxUser createBoxUser(String configId,
      GrouperBoxUser grouperBoxUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createBoxUser");

    long startTime = System.nanoTime();

    try {

      //TODO pass in field
      JsonNode jsonToSend = grouperBoxUser.toJson(null);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      JsonNode userNode = executeMethod(debugMap, "POST", configId, "/users",
          GrouperUtil.toSet(201, 429), new int[] { -1 }, jsonStringToSend);
      
      GrouperBoxUser grouperBoxUserResult = GrouperBoxUser.fromJson(userNode);

      return grouperBoxUserResult; 
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }
  
  /**
   * 
   * @param configId
   * @param groupId
   */
  public static void deleteBoxGroup(String configId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteBoxGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/groups/" + groupId,
          GrouperUtil.toSet(200, 204, 404, 429), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  }
  
  /**
   * 
   * @param configId
   * @param userId
   */
  public static void deleteBoxUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteBoxUser");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/users/" + userId,
          GrouperUtil.toSet(200, 404, 204, 429), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  }

  /**
   * create a membership
   * @param configId
   * @param groupId
   * @param userId
   * @return the result
   */
  public static void createBoxMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createBoxMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      
      ObjectNode userNode = GrouperUtil.jsonJacksonNode();
      userNode.put("id", userId);
      
      ObjectNode groupNode = GrouperUtil.jsonJacksonNode();
      groupNode.put("id", groupId);
      
      objectNode.set("user", userNode);
      objectNode.set("group", groupNode);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //String url = "https://api.box.com/2.0/group_memberships";
      String urlSuffix = "/group_memberships";
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200, 201, 429), 
          new int[] { -1 }, jsonStringToSend);
      
      if (jsonNode == null) {
        throw new RuntimeException("error creating box membership for groupId "+groupId+" userId "+userId);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }
  
  /**
   * delete membership
   * @param configId
   * @param groupId
   * @param userId
   * @return the result
   */
  public static void deleteBoxMembership(String configId, String groupMembershipId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteBoxMembership");

    long startTime = System.nanoTime();

    try {
  
//      String url = "https://api.box.com/2.0/group_memberships/:group_membership_id";
      
      executeMethod(debugMap, "DELETE", configId, "/group_memberships/"+groupMembershipId,
          GrouperUtil.toSet(200, 204, 404, 429), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  
  }

  
  /**
   * update a user
   * @param configId
   * @param grouperBoxUser
   * @param fieldsToUpdate
   * @return the result
   */
  public static GrouperBoxUser updateBoxUser(String configId,
      GrouperBoxUser grouperBoxUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateBoxUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperBoxUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperBoxUser);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperBoxUser + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
//      String url = "https://api.box.com/2.0/users/:user_id"
      
      JsonNode jsonToSend = grouperBoxUser.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "/users/"+id,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

      GrouperBoxUser grouperBoxUserResult = GrouperBoxUser.fromJson(jsonNode);

      return grouperBoxUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }
  
  /**
   * update a group
   * @param configId
   * @param grouperBoxGroup
   * @param fieldsToUpdate
   * @return the result
   */
  public static GrouperBoxGroup updateBoxGroup(String configId,
      GrouperBoxGroup grouperBoxGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateBoxGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperBoxGroup.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperBoxGroup);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperBoxGroup + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
//      String url = "https://api.box.com/2.0/groups/:group_id"
      
      JsonNode jsonToSend = grouperBoxGroup.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "/groups/"+id,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

      GrouperBoxGroup grouperBoxGroupResult = GrouperBoxGroup.fromJson(jsonNode);

      return grouperBoxGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * 
   * @param configId
   * @param filterTerm
   * @param attributesToRetrieve
   * @return
   */
  public static List<GrouperBoxGroup> retrieveBoxGroups(String configId, String filterTerm, Set<String> attributesToRetrieve) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<GrouperBoxGroup> results = new ArrayList<GrouperBoxGroup>();

    debugMap.put("method", "retrieveBoxGroups");

    long startTime = System.nanoTime();
    
    int limit = 1000;
    
    Set<String> fieldsToSelect = new HashSet<>();
    for (String attributeToRetrieve: attributesToRetrieve) {
      String boxField = GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.get(attributeToRetrieve);
      if (StringUtils.isNotBlank(boxField)) {
        fieldsToSelect.add(boxField);
      }
    }
    
    String fieldsToSelectSingleString = GrouperUtil.join(fieldsToSelect.iterator(), ",");
    
    String requestUrl =  "/groups?limit="+limit+"&fields=" + fieldsToSelectSingleString;
    
    if (!StringUtils.isBlank(filterTerm)) {
      requestUrl += "&filter_term=" + GrouperUtil.escapeUrlEncode(filterTerm);
    }

    try {
      
      boolean allGroupsFetched = false;
      
      while (allGroupsFetched == false) {
        
        int[] returnCode = new int[] { -1 };
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, requestUrl, returnCode);
        
        if (!jsonNode.has("total_count")) {
          throw new RuntimeException("Invalid response, requestUri: " + requestUrl + "  : http response code: " + returnCode[0] + ", " + GrouperUtil.jsonJacksonToString(jsonNode));
        }

        ArrayNode groupsArray = (ArrayNode) jsonNode.get("entries");
        
        int groupsArraySize = groupsArray == null ? 0 : groupsArray.size();
        for (int i = 0; i < groupsArraySize; i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromJson(groupNode);
          if (grouperBoxGroup != null) {
            results.add(grouperBoxGroup);
          }
        }

        long totalGroups = GrouperUtil.jsonJacksonGetLong(jsonNode, "total_count", 0L);
//        long offset = GrouperUtil.jsonJacksonGetLong(jsonNode, "offset");
        long newOffset = results.size();
        if (Long.valueOf(results.size()).compareTo(totalGroups) == 0 || groupsArraySize == 0) {
          allGroupsFetched = true;
        } else {
          requestUrl =  "/groups?offset="+newOffset+"&limit="+limit+"&fields=" + fieldsToSelectSingleString;
          if (!StringUtils.isBlank(filterTerm)) {
            requestUrl += "&filter_term=" + GrouperUtil.escapeUrlEncode(filterTerm);
          }
        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    debugMap.put("size", GrouperClientUtils.length(results));

    return results;
  }
  
  /**
   * @param configId
   * @param id is the group id
   * @param attributesToRetrieve
   * @return the box group
   */
  public static GrouperBoxGroup retrieveBoxGroup(String configId, String id, Set<String> attributesToRetrieve) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveBoxGroup");

    long startTime = System.nanoTime();

    try {
      
      Set<String> fieldsToSelect = new HashSet<>();
      for (String attributeToRetrieve: attributesToRetrieve) {
        String boxField = GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.get(attributeToRetrieve);
        if (StringUtils.isNotBlank(boxField)) {
          fieldsToSelect.add(boxField);
        }
      }
      
      String fieldsToSelectSingleString = GrouperUtil.join(fieldsToSelect.iterator(), ",");

//      String url = "https://api.box.com/2.0/groups/:group_id"
      
      String urlSuffix = "/groups/"+id+"?fields="+fieldsToSelectSingleString;
      
      int[] returnCode = new int[] {-1};
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, returnCode);
      
      if (returnCode[0] == 404) {
        return null;
      }
      
      if (jsonNode == null) {
        return null;
      }
      
      GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromJson(jsonNode);
      
      return grouperBoxGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * 
   * @param configId
   * @param filterTerm
   * @param attributesToRetrieve
   * @return
   */
  public static List<GrouperBoxUser> retrieveBoxUsers(String configId, String filterTerm, Set<String> attributesToRetrieve) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<GrouperBoxUser> results = new ArrayList<GrouperBoxUser>();

    debugMap.put("method", "retrieveBoxUsers");

    long startTime = System.nanoTime();
    
    int limit = 1000;
    
    Set<String> fieldsToSelect = new HashSet<>();
    for (String attributeToRetrieve: attributesToRetrieve) {
      String boxField = GrouperBoxUser.grouperBoxUserToBoxSpecificAttributeNames.get(attributeToRetrieve);
      if (StringUtils.isNotBlank(boxField)) {
        fieldsToSelect.add(boxField);
      }
    }
    
    String fieldsToSelectSingleString = GrouperUtil.join(fieldsToSelect.iterator(), ",");
    
    String requestUrl =  "/users?limit="+limit+"&fields=" + fieldsToSelectSingleString;
    
    if (!StringUtils.isBlank(filterTerm)) {
      requestUrl += "&filter_term=" + GrouperUtil.escapeUrlEncode(filterTerm);
    }

    try {
      
      boolean allUsersFetched = false;
      
      while (allUsersFetched == false) {
        
        int[] returnCode = new int[] { -1 };
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, requestUrl, returnCode);
        
        if (!jsonNode.has("total_count")) {
          throw new RuntimeException("Invalid response, requestUri: " + requestUrl + "  : http response code: " + returnCode[0] + ", " + GrouperUtil.jsonJacksonToString(jsonNode));
        }
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("entries");
        
        int usersArraySize = usersArray == null ? 0 : usersArray.size();
        for (int i = 0; i < usersArraySize; i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromJson(userNode);
          if (grouperBoxUser != null) {
            results.add(grouperBoxUser);
          }
        }

        long totalUsers = GrouperUtil.jsonJacksonGetLong(jsonNode, "total_count", 0L);
//        long offset = GrouperUtil.jsonJacksonGetLong(jsonNode, "offset");
        long newOffset = results.size();
        if (Long.valueOf(results.size()).compareTo(totalUsers) == 0 || usersArraySize == 0) {
          allUsersFetched = true;
        } else {
          requestUrl =  "/users?offset="+newOffset+"&limit="+limit+"&fields=" + fieldsToSelectSingleString;
          if (!StringUtils.isBlank(filterTerm)) {
            requestUrl += "&filter_term=" + GrouperUtil.escapeUrlEncode(filterTerm);
          }

        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    debugMap.put("size", GrouperClientUtils.length(results));

    return results;

  }
  
  /**
   * @param configId
   * @param id is the user id
   * @param attributesToRetrieve
   * @return the box user
   */
  public static GrouperBoxUser retrieveBoxUser(String configId, String id, Set<String> attributesToRetrieve) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveBoxUser");

    long startTime = System.nanoTime();

    try {

      Set<String> fieldsToSelect = new HashSet<>();
      for (String attributeToRetrieve: attributesToRetrieve) {
        String boxField = GrouperBoxUser.grouperBoxUserToBoxSpecificAttributeNames.get(attributeToRetrieve);
        if (StringUtils.isNotBlank(boxField)) {
          fieldsToSelect.add(boxField);
        }
      }
      
      String fieldsToSelectSingleString = GrouperUtil.join(fieldsToSelect.iterator(), ",");
      
//      String url = "https://api.box.com/2.0/users/:user_id?fields=id,type,name"
      
      int[] returnCode = new int[] { -1 };
      
      String urlSuffix = "/users/"+id+"?fields="+fieldsToSelectSingleString;
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, returnCode);
      
      if (returnCode[0] == 404) {
        return null;
      }
      
      if (jsonNode == null) {
        return null;
      }
      
      GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromJson(jsonNode);
      
      return grouperBoxUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user id to membership id map
   */
  public static Map<String, String> retrieveBoxGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    // https://api.box.com/2.0/groups/:group_id/memberships
    debugMap.put("method", "retrieveBoxGroupMembers");

    long startTime = System.nanoTime();
    try {

      Map<String, String> memberIdToMembershipId = new HashMap<String, String>();
      
      boolean allMembersFetched = false;
      
      String urlSuffix = "/groups/"+groupId+"/memberships?limit=1000&offset=0";
      
      while (allMembersFetched == false) {
        
        int[] returnCode = new int[] { -1 };
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, returnCode);
        
        if (!jsonNode.has("total_count")) {
          throw new RuntimeException("Invalid response, requestUri: " + urlSuffix + "  : http response code: " + returnCode[0] + ", " + GrouperUtil.jsonJacksonToString(jsonNode));
        }

        ArrayNode entries = (ArrayNode) jsonNode.get("entries");
        
        int entriesSize = entries == null ? 0 : entries.size();
        for (int i = 0; i < entriesSize; i++) {
          JsonNode singleEntry = entries.get(i);
          
          String membershipId = GrouperUtil.jsonJacksonGetString(singleEntry, "id");
          JsonNode userNode  = GrouperUtil.jsonJacksonGetNode(singleEntry, "user");
          String userId = GrouperUtil.jsonJacksonGetString(userNode, "id");
          memberIdToMembershipId.put(userId, membershipId);
        }

        long totalMembers = GrouperUtil.jsonJacksonGetLong(jsonNode, "total_count", 0L);
//        long offset = GrouperUtil.jsonJacksonGetLong(jsonNode, "offset");
        long newOffset = memberIdToMembershipId.size();
        if (Long.valueOf(memberIdToMembershipId.size()).compareTo(totalMembers) == 0 || entriesSize == 0) {
          allMembersFetched = true;
        } else {
          urlSuffix =  "/groups/"+groupId+"/memberships?limit=1000&offset="+newOffset;
        }
      }
      
      debugMap.put("size", GrouperClientUtils.length(memberIdToMembershipId));

      return memberIdToMembershipId;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  }

}

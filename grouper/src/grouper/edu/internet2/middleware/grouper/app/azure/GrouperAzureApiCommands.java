package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GrouperAzureApiCommands {

  public static void main(String[] args) {

//    AzureMockServiceHandler.dropAzureMockTables();
//    AzureMockServiceHandler.ensureAzureMockTables();
    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.azureConnector.azure1.loginEndpoint",
//        "http://localhost/f3/login.microsoftonline.com/");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.azureConnector.azure1.resourceEndpoint",
//        "http://localhost/f3/graph.microsoft.com/v1.0/");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
    "grouper.azureConnector.azure1.loginEndpoint",
    "http://localhost:8400/grouper/mockServices/azure/auth");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
    "grouper.azureConnector.azure1.resourceEndpoint",
    "http://localhost:8400/grouper/mockServices/azure");

    //GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("AzureProvA");
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning("azure1");

    //  List<GrouperAzureGroup> grouperAzureGroups = retrieveAzureGroups("azure1");
    //  
    //  for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
    //    System.out.println(grouperAzureGroup);
    //  }
    
    //  GrouperAzureGroup grouperAzureGroup = retrieveAzureGroup("azure1", "id", "1153755cfa554297a29cfc332e1bef9f");
    //  GrouperAzureGroup grouperAzureGroup = retrieveAzureGroup("azure1", "displayName", "myDisplayName2");
    //  System.out.println(grouperAzureGroup);

//    for (int i=0;i<5;i++) {
//      {
//        GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
//        grouperAzureUser.setAccountEnabled(true);
//        grouperAzureUser.setDisplayName("myDispName" + i);
//        grouperAzureUser.setId(GrouperUuid.getUuid());
//        grouperAzureUser.setMailNickname("a" + i + "@b.c");
//        grouperAzureUser.setOnPremisesImmutableId((12345678+i) + "");
//        grouperAzureUser.setUserPrincipalName("jsmith" + 1);
//        HibernateSession.byObjectStatic().save(grouperAzureUser);
//        createAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", grouperAzureUser.getId());
//      }
//      
//    }
    
    //  Set<String> groupIds = retrieveAzureUserGroups("azure1", "84ec56bad4da4430ae5f2998ea283dfc");
    //  for (String groupId : groupIds) {
    //    System.out.println(groupId);
    //  }

    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("azureGetMembershipPagingSize", "2");
    //
    //    Set<String> userIds = retrieveAzureGroupMembers("azure1", "dcba5d8d7986432db23a0342887e8fba");
    //    for (String userId : userIds) {
    //      System.out.println(userId);
    //    }
        
    
    //  {
    //    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    //    grouperAzureUser.setAccountEnabled(true);
    //    grouperAzureUser.setDisplayName("myDispName2");
    //    grouperAzureUser.setId(GrouperUuid.getUuid());
    //    grouperAzureUser.setMailNickname("a@b.d");
    //    grouperAzureUser.setOnPremisesImmutableId("12345679");
    //    grouperAzureUser.setUserPrincipalName("kjohnson");
    //    HibernateSession.byObjectStatic().save(grouperAzureUser);
    //  }
    
    //  List<GrouperAzureUser> grouperAzureUsers = retrieveAzureUsers("azure1");
    //
    //  for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
    //    System.out.println(grouperAzureUser);
    //  }
    
    //GrouperAzureUser grouperAzureUser = retrieveAzureUser("azure1", "userPrincipalName", "jsmith");
    //GrouperAzureUser grouperAzureUser = retrieveAzureUser("azure1", "displayName", "myDispName");
    //System.out.println(grouperAzureUser);
    
    //  createAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //createAzureMemberships("azure1", "dcba5d8d7986432db23a0342887e8fba", GrouperUtil.toSet("1db63cda166a4640b9ef1a0808f90873", "b1dda78d8d42461a93f8b471f26b682e"));
    
    //  deleteAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //  GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    //  grouperAzureGroup.setDescription("myDescription2");
    //  grouperAzureGroup.setDisplayName("myDisplayName2");
    //  grouperAzureGroup.setMailNickname("myMailNick");
    //  grouperAzureGroup.setGroupTypeUnified(true);
    //  grouperAzureGroup.setVisibility(AzureVisibility.Public);
    //  createAzureGroup("azure1", grouperAzureGroup, null);

    //deleteAzureGroup("azure1", "fa356bb8ddb14600be7994cd7b5239a7");
    
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    grouperAzureGroup.setId("dcba5d8d7986432db23a0342887e8fba");
    grouperAzureGroup.setDisplayName("myDisplayName3");
    grouperAzureGroup.setMailNickname("whatever");
    updateAzureGroup("azure1", grouperAzureGroup, GrouperUtil.toSet("displayName"));
  }

  

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static GetMethod httpGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (GetMethod) httpMethod(debugMap, configId, urlSuffix, "GET");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static PostMethod httpPostMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (PostMethod) httpMethod(debugMap, configId, urlSuffix, "POST");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @param httpMethodName is GET, POST, DELETE, PUT
   * @return
   */
  private static HttpMethodBase httpMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix, String httpMethodName) {
    String bearerToken = AzureGrouperExternalSystem
        .retrieveBearerTokenForAzureConfigId(debugMap, configId);
    String graphEndpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.azureConnector." + configId + ".resourceEndpoint");
    String url = graphEndpoint;
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

    HttpMethodBase method = null;
    if (StringUtils.equals("GET", httpMethodName)) {
      method = new GetMethod(url);
    } else if (StringUtils.equals("POST", httpMethodName)) {
      method = new PostMethod(url);
    } else if (StringUtils.equals("DELETE", httpMethodName)) {
      method = new DeleteMethod(url);
    } else if (StringUtils.equals("PUT", httpMethodName)) {
      method = new PutMethod(url);
    } else if (StringUtils.equals("PATCH", httpMethodName)) {

      method = new PostMethod(url) {
        @Override public String getName() { return "PATCH"; }
      };
    } else {
      throw new RuntimeException("Not expecting type: '" + httpMethodName + "'");
    }
    method.addRequestHeader("Content-Type", "application/json");
    method.addRequestHeader("Authorization", "Bearer " + bearerToken);
    return method;
  }

  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {

    return executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    HttpMethodBase httpMethod = httpMethod(debugMap, configId, urlSuffix, httpMethodName);

    HttpClient httpClient = new HttpClient();

    if (!StringUtils.isBlank(body)) {
      if (httpMethod instanceof EntityEnclosingMethod) {
        try {
          StringRequestEntity entity = new StringRequestEntity(body, "application/json",
              "UTF-8");
          ((EntityEnclosingMethod) httpMethod).setRequestEntity(entity);
        } catch (Exception e) {
          throw new RuntimeException("error", e);
        }
      } else {
        throw new RuntimeException("Cant attach a body if in method: " + httpMethodName);
      }
    }

    int code = -1;
    String json = null;

    try {
      code = httpClient.executeMethod(httpMethod);
      returnCode[0] = code;
      json = httpMethod.getResponseBodyAsString();
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

    JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
    return rootNode;
  }

  /**
   * create a group
   * @param grouperAzureGroup
   * @return the result
   */
  public static GrouperAzureGroup createAzureGroup(String configId,
      GrouperAzureGroup grouperAzureGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperAzureGroup.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups",
          GrouperUtil.toSet(201), new int[] { -1 }, jsonStringToSend);

      GrouperAzureGroup grouperAzureGroupResult = GrouperAzureGroup.fromJson(jsonNode);

      return grouperAzureGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * create a membership
   * @param grouperAzureGroup
   * @return the result
   */
  public static void createAzureMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

      String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
          "grouper.azureConnector.azure1.resourceEndpoint");
      
      objectNode.put("@odata.id", GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/" + GrouperUtil.escapeUrlEncode(userId));
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);
      
      executeMethod(debugMap, "POST", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members/$ref",
          GrouperUtil.toSet(204, 400), new int[] { -1 }, jsonStringToSend);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * create a membership
   * @param grouperAzureGroup
   * @return the result
   */
  public static void createAzureMemberships(String configId,
      String groupId, Collection<String> userIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureMemberships");

    //  PATCH https://graph.microsoft.com/v1.0/groups/{group-id}
    //  Content-type: application/json
    //  Content-length: 30
    //
    //  {
    //    "members@odata.bind": [
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}"
    //      ]
    //  }
    
    long startTime = System.nanoTime();

    try {

      int batchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureMembershipPagingSize", 20);
      List<String> userIdsList = new ArrayList<String>(userIds);
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(userIdsList, batchSize);
      debugMap.put("numberOfBatches", numberOfBatches);
      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        debugMap.put("batchIndex", batchIndex);
        List<String> batchOfUserIds = GrouperUtil.batchList(userIdsList, batchSize, batchIndex);

        ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
        
        String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "grouper.azureConnector.azure1.resourceEndpoint");
        
        for (int i=0;i<GrouperUtil.length(batchOfUserIds);i++) {
          String userId = batchOfUserIds.get(i);
          arrayNode.add(GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/" + GrouperUtil.escapeUrlEncode(userId));
        }
        
        ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

        objectNode.set("members@odata.bind", arrayNode);
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);
        try {
          executeMethod(debugMap, "PATCH", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId),
              GrouperUtil.toSet(204), new int[] { -1 }, jsonStringToSend);
        } catch (Exception e) {

          debugMap.put("innerException", GrouperClientUtils.getFullStackTrace(e));

          // if this fails, try individually
          for (int i=0;i<GrouperUtil.length(batchOfUserIds);i++) {
            String userId = batchOfUserIds.get(i);
            createAzureMembership(configId, groupId, userId);
          }
        }

      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * update a group
   * @param grouperAzureGroup
   * @return the result
   */
  public static GrouperAzureGroup updateAzureGroup(String configId,
      GrouperAzureGroup grouperAzureGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateAzureGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperAzureGroup.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperAzureGroup);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperAzureGroup + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
      JsonNode jsonToSend = grouperAzureGroup.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PATCH", configId, "/groups/" + GrouperUtil.escapeUrlEncode(id),
          GrouperUtil.toSet(204), new int[] { -1 }, jsonStringToSend);

      GrouperAzureGroup grouperAzureGroupResult = GrouperAzureGroup.fromJson(jsonNode);

      return grouperAzureGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  public static void deleteAzureGroup(String configId,
      String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAzureGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId),
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
  }


  public static List<GrouperAzureGroup> retrieveAzureGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperAzureGroup> results = new ArrayList<GrouperAzureGroup>();

      JsonNode jsonNode = executeGetMethod(debugMap, configId,
          "/groups?$select=" + GrouperAzureGroup.fieldsToSelect);

      ArrayNode groupsArray = (ArrayNode) jsonNode.get("value");

      for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
        JsonNode groupNode = groupsArray.get(i);
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(groupNode);
        results.add(grouperAzureGroup);
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  public static List<GrouperAzureUser> retrieveAzureUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperAzureUser> results = new ArrayList<GrouperAzureUser>();

      JsonNode jsonNode = executeGetMethod(debugMap, configId,
          "/users?$select=" + GrouperAzureUser.fieldsToSelect);

      ArrayNode usersArray = (ArrayNode) jsonNode.get("value");

      for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
        JsonNode userNode = usersArray.get(i);
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(userNode);
        results.add(grouperAzureUser);
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param fieldName id or userPrincipalName
   * @param fieldValue is value of id or userPrincipalName
   * @return
   */
  public static GrouperAzureUser retrieveAzureUser(String configId, String fieldName,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")
          || StringUtils.equals(fieldName, "userPrincipalName")) {
        urlSuffix = "/users/" + GrouperUtil.escapeUrlEncode(fieldValue) + "?$select="
            + GrouperAzureUser.fieldsToSelect;
      } else {
        urlSuffix = "/users?$filter=" + GrouperUtil.escapeUrlEncode(fieldName)
            + "%20eq%20'" + GrouperUtil.escapeUrlEncode(StringUtils.replace(fieldValue, "'", "''")) + "'&$select="
            + GrouperAzureUser.fieldsToSelect;

      }

      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      //lets get the group node
      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        if (value.size() == 1) {
          jsonNode = value.get(0);
        } else {
          throw new RuntimeException("Query returned multiple results: " + urlSuffix);
        }
      }

      debugMap.put("found", jsonNode != null);

      if (jsonNode == null) {
        return null;
      }
      GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(jsonNode);

      return grouperAzureUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * return user ids in the group
   * @param configId
   * @param userId
   * @return group ids
   */
  public static Set<String> retrieveAzureUserGroups(String configId, String userId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUserGroups");

    long startTime = System.nanoTime();
    
    Set<String> result = new LinkedHashSet<String>();

    try {

      String urlSuffix = "/users/" + GrouperUtil.escapeUrlEncode(userId) + "/getMemberGroups";

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200), new int[] {-1}, null);

      //lets get the group node

      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        
        int azureGetUserGroupsMax = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetUserGroupsMax", 2046);
        if (value.size() == azureGetUserGroupsMax) {
          throw new RuntimeException("Too many groups! " + value.size());
        }
        
        for (int i=0;i<value.size();i++) {
          String groupId = value.get(i).asText();
          result.add(groupId);
        }
      }

      return result;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
  }

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveAzureGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroupMembers");

    int calls = 0;
    long startTime = System.nanoTime();
    
    Set<String> result = new LinkedHashSet<String>();

    try {

      int azureGetMembershipPagingSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetMembershipPagingSize", 100);
      
      String urlSuffix = "/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members?$select=id&$top=" + azureGetMembershipPagingSize;

      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      //lets get the group node
      retrieveAzureGroupMembersHelper(result, jsonNode);
      debugMap.put("calls", ++calls);
      
      for (int i=0;i<1000000;i++) {
        String nextLink = GrouperUtil.jsonJacksonGetString(jsonNode, "@odata.nextLink");
        if (StringUtils.isBlank(nextLink)) {
          break;
        }
        String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
            "grouper.azureConnector.azure1.resourceEndpoint");
        if (!nextLink.startsWith(resourceEndpoint)) {
          if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperAzureAllowNextLinkPrefixMismatch", false)) {
            throw new RuntimeException("@odata.nextLink is going to a different URL! '" + nextLink + "', '" + resourceEndpoint + "'");
          }
        } else {
          urlSuffix = nextLink.substring(resourceEndpoint.length(), nextLink.length());
        }
        jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        retrieveAzureGroupMembersHelper(result, jsonNode);
        debugMap.put("calls", ++calls);
      }

      return result;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
  }

  private static void retrieveAzureGroupMembersHelper(Set<String> result, JsonNode jsonNode) {
    ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
    if (value != null && value.size() > 0) {
      for (int i=0;i<value.size();i++) {
        JsonNode membership = value.get(i);
        result.add(GrouperUtil.jsonJacksonGetString(membership, "id"));
      }
    }
  }

  /**
   * @param configId
   * @param fieldName is id or displayName
   * @param fieldValue is value of id or displayName
   * @return the user
   */
  public static GrouperAzureGroup retrieveAzureGroup(String configId, String fieldName,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")) {
        urlSuffix = "/groups/" + GrouperUtil.escapeUrlEncode(fieldValue) + "?$select="
            + GrouperAzureGroup.fieldsToSelect;
      } else {
        urlSuffix = "/groups?$filter=" + GrouperUtil.escapeUrlEncode(fieldName)
            + "%20eq%20'" + GrouperUtil.escapeUrlEncode(StringUtils.replace(fieldValue, "'", "''")) + "'&$select="
            + GrouperAzureGroup.fieldsToSelect;
      }

      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      //lets get the group node
      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        if (value.size() == 1) {
          jsonNode = value.get(0);
        } else {
          throw new RuntimeException("Query returned multiple results: " + urlSuffix);
        }
      }

      if (jsonNode == null) {
        return null;
      }
      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(jsonNode);

      return grouperAzureGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }



  /**
   * delete membership
   * @param grouperAzureGroup
   * @return the result
   */
  public static void deleteAzureMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAzureMembership");

    long startTime = System.nanoTime();

    try {
  
      executeMethod(debugMap, "DELETE", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members/" + GrouperUtil.escapeUrlEncode(userId) + "/$ref",
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
  
  }

}

package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GrouperAzureApiCommands {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAzureApiCommands.class);

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
    
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    grouperAzureGroup.setDescription("myDescription3");
    grouperAzureGroup.setDisplayName("myDisplayName3");
    grouperAzureGroup.setMailNickname("myMailNick3");
    grouperAzureGroup.setGroupTypeUnified(true);
    grouperAzureGroup.setVisibility(AzureVisibility.Public);
    
    Map<GrouperAzureGroup, Set<String>> map = new HashMap<>();
    map.put(grouperAzureGroup, null);
    createAzureGroups("azure1", map);

    //deleteAzureGroup("azure1", "fa356bb8ddb14600be7994cd7b5239a7");
    
//    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
//    grouperAzureGroup.setId("dcba5d8d7986432db23a0342887e8fba");
//    grouperAzureGroup.setDisplayName("myDisplayName4");
//    grouperAzureGroup.setMailNickname("whatever");
//    updateAzureGroup("azure1", grouperAzureGroup, GrouperUtil.toSet("displayName"));
  }

  

  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {

    return executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(AzureMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(AzureMockServiceHandler.doNotLogParameters);

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

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    
    String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".proxyUrl");
    String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);

    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
    grouperHttpCall.assignBody(body);
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
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  
  
  /**
   * create groups
   * @return the result
   */
  public static Map<GrouperAzureGroup, Exception> createAzureGroups(String configId, Map<GrouperAzureGroup, Set<String>> groupToFieldNamesToInsert) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureGroups");

    long startTime = System.nanoTime();
    
    Map<GrouperAzureGroup, Exception> groupToMayBeException = new HashMap<>();

    List<GrouperAzureGroup> groupsToInsert = new ArrayList<>(groupToFieldNamesToInsert.keySet());
    
    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(groupsToInsert, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureGroup> groupsInOneHttpRequest = GrouperUtil.batchList(groupsToInsert, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureGroup singleGroupToBeCreated: groupsInOneHttpRequest) {
          
          JsonNode jsonToSend = singleGroupToBeCreated.toJson(groupToFieldNamesToInsert.get(singleGroupToBeCreated));
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/groups");
          innerRequestNode.put("method", "POST");
          innerRequestNode.set("body", jsonToSend);
          
          ObjectNode headersNode  = GrouperUtil.jsonJacksonNode();
          headersNode.put("Content-Type", "application/json");
          innerRequestNode.set("headers", headersNode);
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int groupIndex = GrouperUtil.intValue(id);
            
            GrouperAzureGroup grouperAzureGroup = groupsInOneHttpRequest.get(groupIndex);
            
            if (statusCode != 201) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              groupToMayBeException.put(grouperAzureGroup, new RuntimeException(error.toString()));
              
            } else {
            
              GrouperAzureGroup grouperAzureGroupResult = GrouperAzureGroup.fromJson(bodyNode);
              
              grouperAzureGroup.setId(grouperAzureGroupResult.getId());
              
              groupToMayBeException.put(grouperAzureGroup, null);
            }
          }
        }
        
        
      }

      return groupToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
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
  
  
  //https://www.baeldung.com/java-generate-secure-password
  private static String generateRandomPassword() {
    String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
    String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
    String numbers = RandomStringUtils.randomNumeric(2);
    String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
    String totalChars = RandomStringUtils.randomAlphanumeric(2);
    String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
      .concat(numbers)
      .concat(specialChar)
      .concat(totalChars);
    List<Character> pwdChars = combinedChars.chars()
      .mapToObj(c -> (char) c)
      .collect(Collectors.toList());
    Collections.shuffle(pwdChars);
    String password = pwdChars.stream()
      .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
      .toString();
    return password;
  }
  
  
  /**
   * create users
   * @param configId
   * @param grouperAzureUser
   * @return
   */
  public static Map<GrouperAzureUser, Exception> createAzureUsers(String configId, List<GrouperAzureUser> grouperAzureUsers, Set<String> fieldsToCreate) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureUsers");

    long startTime = System.nanoTime();
    
    Map<GrouperAzureUser, Exception> userToMayBeException = new HashMap<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(grouperAzureUsers, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureUser> usersInOneHttpRequest = GrouperUtil.batchList(grouperAzureUsers, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureUser singleUserToBeCreated: usersInOneHttpRequest) {
          
          String password = generateRandomPassword();
          singleUserToBeCreated.setPassword(password);
          
          JsonNode jsonToSend = singleUserToBeCreated.toJson(fieldsToCreate);
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/users");
          innerRequestNode.put("method", "POST");
          innerRequestNode.set("body", jsonToSend);
          
          ObjectNode headersNode  = GrouperUtil.jsonJacksonNode();
          headersNode.put("Content-Type", "application/json");
          innerRequestNode.set("headers", headersNode);
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            GrouperAzureUser grouperAzureUser = usersInOneHttpRequest.get(userIndex);
            
            if (statusCode != 201) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              userToMayBeException.put(grouperAzureUser, new RuntimeException(error.toString()));
              
            } else {
            
              GrouperAzureUser grouperAzureUserResult = GrouperAzureUser.fromJson(bodyNode);
              
              grouperAzureUser.setId(grouperAzureUserResult.getId());
              
              userToMayBeException.put(grouperAzureUser, null);
            }
          }
        }
        
        
      }

      return userToMayBeException;
      
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
  private static void createAzureMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAzureMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

      String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
          "grouper.azureConnector."+configId+".resourceEndpoint");
      
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
  public static Map<MultiKey, Exception> createAzureMemberships(String configId,
      String groupId, Collection<String> userIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    Map<MultiKey, Exception> groupIdUserIdToException = new HashMap<>();

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
    
    
    // new way - https://docs.microsoft.com/en-us/graph/json-batching#first-json-batch-request
    long startTime = System.nanoTime();

    try {

      List<String> userIdsList = new ArrayList<String>(userIds);
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(userIdsList, 400, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<String> userIdsInOneHttpRequest = GrouperUtil.batchList(userIdsList, 400, httpRequestIndex);
        
        int batchSize = 20;
        
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(userIdsInOneHttpRequest, batchSize, false);
        debugMap.put("numberOfBatches", numberOfBatches);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        
        
        for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
          debugMap.put("batchIndex", batchIndex);
          List<String> batchOfUserIds = GrouperUtil.batchList(userIdsInOneHttpRequest, batchSize, batchIndex);

          ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
          
          String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
              "grouper.azureConnector."+configId+".resourceEndpoint");
          
          for (int i=0;i<GrouperUtil.length(batchOfUserIds);i++) {
            String userId = batchOfUserIds.get(i);
            arrayNode.add(GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/" + GrouperUtil.escapeUrlEncode(userId));
          }
          
          ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

          objectNode.set("members@odata.bind", arrayNode);
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(batchIndex));
          innerRequestNode.put("url", "/groups/" + GrouperUtil.escapeUrlEncode(groupId));
          innerRequestNode.put("method", "PATCH");
          innerRequestNode.set("body", objectNode);
          
          ObjectNode headersNode  = GrouperUtil.jsonJacksonNode();
          headersNode.put("Content-Type", "application/json");
          innerRequestNode.set("headers", headersNode);
          
          requestsArrayNode.add(innerRequestNode);

        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            if (statusCode != 204 && statusCode != 400) {
              String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
              
              int batchIndexWithProblem = GrouperUtil.intValue(id);
              
              List<String> batchOfUserIdsWithProblem = GrouperUtil.batchList(userIdsInOneHttpRequest, batchSize, batchIndexWithProblem);
              
              for (String userId: batchOfUserIdsWithProblem) {
                try {
                  createAzureMembership(configId, groupId, userId);
                } catch (Exception e) {
                  groupIdUserIdToException.put(new MultiKey(groupId, userId), e);
                }
              }
              
            }
          }
        }
        
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
    
    return groupIdUserIdToException;

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
  
  
  /**
   * update groups
   * @param configId
   * @param azureGroupToFieldNamesToUpdate
   * @return the result
   */
  public static Map<GrouperAzureGroup, Exception> updateAzureGroups(String configId, Map<GrouperAzureGroup, Set<String>> azureGroupToFieldNamesToUpdate) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "updateAzureGroups");
    
    long startTime = System.nanoTime();
    
    Map<GrouperAzureGroup, Exception> groupToMayBeException = new HashMap<>();
    
    List<GrouperAzureGroup> groupsToUpdate = new ArrayList<>(azureGroupToFieldNamesToUpdate.keySet());

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(groupsToUpdate, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureGroup> groupsInOneHttpRequest = GrouperUtil.batchList(groupsToUpdate, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureGroup singleGroupToBeUpdated: groupsInOneHttpRequest) {
          
          String id = singleGroupToBeUpdated.getId();
          
          JsonNode jsonToSend = singleGroupToBeUpdated.toJson(azureGroupToFieldNamesToUpdate.get(singleGroupToBeUpdated));
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/groups/" + GrouperUtil.escapeUrlEncode(id));
          innerRequestNode.put("method", "PATCH");
          innerRequestNode.set("body", jsonToSend);
          
          ObjectNode headersNode  = GrouperUtil.jsonJacksonNode();
          headersNode.put("Content-Type", "application/json");
          innerRequestNode.set("headers", headersNode);
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            GrouperAzureGroup grouperAzureGroup = groupsInOneHttpRequest.get(userIndex);
            
            if (statusCode != 204) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              groupToMayBeException.put(grouperAzureGroup, new RuntimeException(error.toString()));
              
            } else {
              groupToMayBeException.put(grouperAzureGroup, null);
            }
          }
        }
        
      }

      return groupToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
    
  }
  
  
  /**
   * update users
   * @param configId
   * @param azureUserToFieldNamesToUpdate
   * @return the result
   */
  public static Map<GrouperAzureUser, Exception> updateAzureUsers(String configId, Map<GrouperAzureUser, Set<String>> azureUserToFieldNamesToUpdate) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "updateAzureUsers");
    
    long startTime = System.nanoTime();
    
    Map<GrouperAzureUser, Exception> userToMayBeException = new HashMap<>();
    
    List<GrouperAzureUser> usersToUpdate = new ArrayList<>(azureUserToFieldNamesToUpdate.keySet());

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(usersToUpdate, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureUser> usersInOneHttpRequest = GrouperUtil.batchList(usersToUpdate, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureUser singleUserToBeUpdated: usersInOneHttpRequest) {
          
          String id = singleUserToBeUpdated.getId();
          
          JsonNode jsonToSend = singleUserToBeUpdated.toJson(azureUserToFieldNamesToUpdate.get(singleUserToBeUpdated));
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/users/" + GrouperUtil.escapeUrlEncode(id));
          innerRequestNode.put("method", "PATCH");
          innerRequestNode.set("body", jsonToSend);
          
          ObjectNode headersNode  = GrouperUtil.jsonJacksonNode();
          headersNode.put("Content-Type", "application/json");
          innerRequestNode.set("headers", headersNode);
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            GrouperAzureUser grouperAzureUser = usersInOneHttpRequest.get(userIndex);
            
            if (statusCode != 204) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              userToMayBeException.put(grouperAzureUser, new RuntimeException(error.toString()));
              
            } else {
            
              userToMayBeException.put(grouperAzureUser, null);
            }
          }
        }
        
        
      }

      return userToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
    
  }


  public static Map<GrouperAzureGroup, Exception> deleteAzureGroups(String configId, List<GrouperAzureGroup> grouperAzureGroups) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAzureGroups");

    long startTime = System.nanoTime();
    
    Map<GrouperAzureGroup, Exception> groupToMayBeException = new HashMap<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(grouperAzureGroups, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureGroup> groupsInOneHttpRequest = GrouperUtil.batchList(grouperAzureGroups, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureGroup singleGroupToBeDeleted: groupsInOneHttpRequest) {
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/groups/" + GrouperUtil.escapeUrlEncode(singleGroupToBeDeleted.getId()));
          innerRequestNode.put("method", "DELETE");
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            GrouperAzureGroup grouperAzureGroup = groupsInOneHttpRequest.get(userIndex);
            
            if (statusCode != 204 && statusCode != 404) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              groupToMayBeException.put(grouperAzureGroup, new RuntimeException(error.toString()));
              
            } else {
            
              groupToMayBeException.put(grouperAzureGroup, null);
            }
          }
        }
        
        
      }

      return groupToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
    
  }


  public static List<GrouperAzureGroup> retrieveAzureGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<GrouperAzureGroup> results = new ArrayList<GrouperAzureGroup>();

    debugMap.put("method", "retrieveAzureGroups");

    long startTime = System.nanoTime();
    
    String nextLink =  "/groups?$top=999&$select=" + GrouperAzureGroup.fieldsToSelect;

    // dont endless loop
    int j = -1;
    int maxPages = 10000;
    for (j=0;j<maxPages;j++) {

      try {
  
        JsonNode jsonNode = executeGetMethod(debugMap, configId, nextLink);
  
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("value");
  
        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(groupNode);
          if (grouperAzureGroup != null) {
            results.add(grouperAzureGroup);
          }
        }
  
        nextLink = GrouperUtil.jsonJacksonGetString(jsonNode, "@odata.nextLink");
        
        if (StringUtils.isBlank(nextLink)) {
          break;
        }
  
      } catch (RuntimeException re) {
        debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
        throw re;
      } finally {
        GrouperAzureLog.azureLog(debugMap, startTime);
      }
    }
    GrouperUtil.assertion(j<maxPages, "Too many groups! " + GrouperClientUtils.length(results));
    debugMap.put("size", GrouperClientUtils.length(results));

    return results;
  }

  public static List<GrouperAzureUser> retrieveAzureUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUsers");

    long startTime = System.nanoTime();

    List<GrouperAzureUser> results = new ArrayList<GrouperAzureUser>();
    String nextLink = "/users?$top=999&$select=" + GrouperAzureUser.fieldsToSelect;

    // dont endless loop
    int j = -1;
    int maxPages = 10000;
    for (j=0;j<maxPages;j++) {

      try {

        JsonNode jsonNode = executeGetMethod(debugMap, configId, nextLink);
  
        ArrayNode usersArray = (ArrayNode) jsonNode.get("value");
  
        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(userNode);
          if (grouperAzureUser != null) {
            results.add(grouperAzureUser);
          }
        }
        nextLink = GrouperUtil.jsonJacksonGetString(jsonNode, "@odata.nextLink");
        
        if (StringUtils.isBlank(nextLink)) {
          break;
        }
      } catch (RuntimeException re) {
        debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
        throw re;
      } finally {
        GrouperAzureLog.azureLog(debugMap, startTime);
      }
      
    }

    GrouperUtil.assertion(j<maxPages, "Too many users! " + GrouperClientUtils.length(results));
    debugMap.put("size", GrouperClientUtils.length(results));

    return results;

  }

  
  /**
   * @param configId
   * @param fieldValues
   * @param fieldName id or userPrincipalName
   * @return
   */
  public static List<GrouperAzureUser> retrieveAzureUsers(String configId, List<String> fieldValues, String fieldName) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUsers");

    long startTime = System.nanoTime();
    
    List<GrouperAzureUser> result = new ArrayList<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(fieldValues, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<String> fieldValuesInOneHttpRequest = GrouperUtil.batchList(fieldValues, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (String fieldValue : fieldValuesInOneHttpRequest) {
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          
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
          
          innerRequestNode.put("url", urlSuffix);
          innerRequestNode.put("method", "GET");
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int index1 = GrouperUtil.intValue(id);
            
            String fieldValue = fieldValuesInOneHttpRequest.get(index1);
            
            if (statusCode != 200 && statusCode != 404) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              LOG.error(error.toString());
              
            } else {
            
              GrouperAzureUser grouperAzureUser = null;
              JsonNode userNode = bodyNode;
              
              ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(bodyNode, "value");

              boolean hasError = false;
              if (value != null && value.size() > 0) {
                if (value.size() == 1) {
                  userNode = value.get(0);
                } else {
                  hasError = true;
                  LOG.error("Query returned multiple results for field name: "+fieldName +" and fieldValue: "+fieldValue);
                }
              }
              if (!hasError) {
                grouperAzureUser = GrouperAzureUser.fromJson(userNode);
                if (grouperAzureUser != null) {
                  result.add(grouperAzureUser);
                }
              }
            }
          }
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

      for (boolean securityEnabledOnly: new boolean[] {true, false}) { 

        JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200), new int[] {-1}, "{\"securityEnabledOnly\": " + securityEnabledOnly + "}");

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

      int azureGetMembershipPagingSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetMembershipPagingSize", 999);
      
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
            "grouper.azureConnector."+configId+".resourceEndpoint");
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
   * @return
   */
  public static List<GrouperAzureGroup> retrieveAzureGroups(String configId, List<String> fieldValues, String fieldName) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroups");

    long startTime = System.nanoTime();
    
    List<GrouperAzureGroup> result = new ArrayList<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(fieldValues, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<String> fieldValuesInOneHttpRequest = GrouperUtil.batchList(fieldValues, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (String fieldValue: fieldValuesInOneHttpRequest) {
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          
          String urlSuffix = null;
          if (StringUtils.equals(fieldName, "id")) {
            urlSuffix = "/groups/" + GrouperUtil.escapeUrlEncode(fieldValue) + "?$select="
                + GrouperAzureGroup.fieldsToSelect;
          } else {
            urlSuffix = "/groups?$filter=" + GrouperUtil.escapeUrlEncode(fieldName)
                + "%20eq%20'" + GrouperUtil.escapeUrlEncode(StringUtils.replace(fieldValue, "'", "''")) + "'&$select="
                + GrouperAzureGroup.fieldsToSelect;
          }
          
          innerRequestNode.put("url", urlSuffix);
          innerRequestNode.put("method", "GET");
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int index1 = GrouperUtil.intValue(id);
            
            String fieldValue = fieldValuesInOneHttpRequest.get(index1);
            
            if (statusCode != 200 && statusCode != 404) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              LOG.error(error.toString());
              
            } else {
            
              GrouperAzureGroup grouperAzureGroup = null;
              JsonNode groupNode = bodyNode;
              boolean hasError = false;
              
              ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(bodyNode, "value");

              if (value != null && value.size() > 0) {
                if (value.size() == 1) {
                  groupNode = value.get(0);
                } else {
                  hasError = true;
                  LOG.error("Query returned multiple results for field name: "+fieldName +" and fieldValue: "+fieldValue);
                }
              }
              
              if (!hasError) {
                grouperAzureGroup = GrouperAzureGroup.fromJson(groupNode);
                if (grouperAzureGroup != null) {
                  result.add(grouperAzureGroup);
                }
              }
            }
          }
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
   * delete memberships
   */
  public static Map<ProvisioningMembership, Exception> deleteAzureMemberships(String configId, List<ProvisioningMembership> membershipsToDelete) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAzureMemberships");

    long startTime = System.nanoTime();
    
    Map<ProvisioningMembership, Exception> membershipToMayBeException = new HashMap<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(membershipsToDelete, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<ProvisioningMembership> membershipsInOneHttpRequest = GrouperUtil.batchList(membershipsToDelete, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (ProvisioningMembership singleMembershipToBeDeleted: membershipsInOneHttpRequest) {
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/groups/" + 
              GrouperUtil.escapeUrlEncode(singleMembershipToBeDeleted.getProvisioningGroupId()) + "/members/" +
              GrouperUtil.escapeUrlEncode(singleMembershipToBeDeleted.getProvisioningEntityId()) + "/$ref");
          innerRequestNode.put("method", "DELETE");
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            ProvisioningMembership provisioningMembership = membershipsInOneHttpRequest.get(userIndex);
            
            if (statusCode != 204 && statusCode != 404) {
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              membershipToMayBeException.put(provisioningMembership, new RuntimeException(error.toString()));
              
            } else {
              membershipToMayBeException.put(provisioningMembership, null);
            }
          }
        }
        
        
      }

      return membershipToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
  
  }


  public static Map<GrouperAzureUser, Exception> deleteAzureUsers(String configId, List<GrouperAzureUser> grouperAzureUsers) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteAzureUsers");
  
    long startTime = System.nanoTime();
    
    Map<GrouperAzureUser, Exception> userToMayBeException = new HashMap<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(grouperAzureUsers, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<GrouperAzureUser> usersInOneHttpRequest = GrouperUtil.batchList(grouperAzureUsers, 20, httpRequestIndex);
        
        ObjectNode mainRequestsNode  = GrouperUtil.jsonJacksonNode();
        ArrayNode requestsArrayNode = GrouperUtil.jsonJacksonArrayNode();
        mainRequestsNode.set("requests", requestsArrayNode);
        
        int index = 0;
        for (GrouperAzureUser singleUserToBeDeleted: usersInOneHttpRequest) {
          
          ObjectNode innerRequestNode  = GrouperUtil.jsonJacksonNode();
          innerRequestNode.put("id", String.valueOf(index));
          innerRequestNode.put("url", "/users/" + GrouperUtil.escapeUrlEncode(singleUserToBeDeleted.getId()));
          innerRequestNode.put("method", "DELETE");
          
          requestsArrayNode.add(innerRequestNode);
          
          index++;
          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(mainRequestsNode);
        JsonNode mainResponseNode = executeMethod(debugMap, "POST", configId, "/$batch/",
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
        
        if (mainResponseNode != null) {
          ArrayNode responses = (ArrayNode) GrouperUtil.jsonJacksonGetNode(mainResponseNode, "responses");
          
          for (int i = 0; i < (responses == null ? 0 : responses.size()); i++) {
            JsonNode oneResponse = responses.get(i);
            Integer statusCode = GrouperUtil.jsonJacksonGetInteger(oneResponse, "status");
            String id = GrouperUtil.jsonJacksonGetString(oneResponse, "id");
            JsonNode bodyNode = GrouperUtil.jsonJacksonGetNode(oneResponse, "body");
            
            int userIndex = GrouperUtil.intValue(id);
            
            GrouperAzureUser grouperAzureUser = usersInOneHttpRequest.get(userIndex);
            
            if (statusCode != 204 && statusCode != 404) {
              
              StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
              
              userToMayBeException.put(grouperAzureUser, new RuntimeException(error.toString()));
              
            } else {
            
              userToMayBeException.put(grouperAzureUser, null);
            }
          }
        }
        
        
      }

      return userToMayBeException;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }
   
    
    
  }

}

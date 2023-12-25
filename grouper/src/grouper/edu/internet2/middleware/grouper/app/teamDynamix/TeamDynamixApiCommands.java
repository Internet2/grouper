package edu.internet2.middleware.grouper.app.teamDynamix;

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

import edu.internet2.middleware.grouper.app.duo.GrouperDuoLog;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleLog;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Log;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class TeamDynamixApiCommands {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TeamDynamixApiCommands.class);

  public static void main(String[] args) {

//    TeamDynamixMockServiceHandler.dropTeamDynamixMockTables();
//    TeamDynamixMockServiceHandler.ensureTeamDynamixMockTables();
    
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

    //GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("TeamDynamixProvA");
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = TeamDynamixGrouperExternalSystem.retrieveApiConnectionForProvisioning("azure1");

    //  List<TeamDynamixGroup> grouperTeamDynamixGroups = retrieveTeamDynamixGroups("azure1");
    //  
    //  for (TeamDynamixGroup grouperTeamDynamixGroup : grouperTeamDynamixGroups) {
    //    System.out.println(grouperTeamDynamixGroup);
    //  }
    
    //  TeamDynamixGroup grouperTeamDynamixGroup = retrieveTeamDynamixGroup("azure1", "id", "1153755cfa554297a29cfc332e1bef9f");
    //  TeamDynamixGroup grouperTeamDynamixGroup = retrieveTeamDynamixGroup("azure1", "displayName", "myDisplayName2");
    //  System.out.println(grouperTeamDynamixGroup);

//    for (int i=0;i<5;i++) {
//      {
//        TeamDynamixUser grouperTeamDynamixUser = new TeamDynamixUser();
//        grouperTeamDynamixUser.setAccountEnabled(true);
//        grouperTeamDynamixUser.setDisplayName("myDispName" + i);
//        grouperTeamDynamixUser.setId(GrouperUuid.getUuid());
//        grouperTeamDynamixUser.setMailNickname("a" + i + "@b.c");
//        grouperTeamDynamixUser.setOnPremisesImmutableId((12345678+i) + "");
//        grouperTeamDynamixUser.setUserPrincipalName("jsmith" + 1);
//        HibernateSession.byObjectStatic().save(grouperTeamDynamixUser);
//        createTeamDynamixMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", grouperTeamDynamixUser.getId());
//      }
//      
//    }
    
    //  Set<String> groupIds = retrieveTeamDynamixUserGroups("azure1", "84ec56bad4da4430ae5f2998ea283dfc");
    //  for (String groupId : groupIds) {
    //    System.out.println(groupId);
    //  }

    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("azureGetMembershipPagingSize", "2");
    //
    //    Set<String> userIds = retrieveTeamDynamixGroupMembers("azure1", "dcba5d8d7986432db23a0342887e8fba");
    //    for (String userId : userIds) {
    //      System.out.println(userId);
    //    }
        
    
    //  {
    //    TeamDynamixUser grouperTeamDynamixUser = new TeamDynamixUser();
    //    grouperTeamDynamixUser.setAccountEnabled(true);
    //    grouperTeamDynamixUser.setDisplayName("myDispName2");
    //    grouperTeamDynamixUser.setId(GrouperUuid.getUuid());
    //    grouperTeamDynamixUser.setMailNickname("a@b.d");
    //    grouperTeamDynamixUser.setOnPremisesImmutableId("12345679");
    //    grouperTeamDynamixUser.setUserPrincipalName("kjohnson");
    //    HibernateSession.byObjectStatic().save(grouperTeamDynamixUser);
    //  }
    
    //  List<TeamDynamixUser> grouperTeamDynamixUsers = retrieveTeamDynamixUsers("azure1");
    //
    //  for (TeamDynamixUser grouperTeamDynamixUser : grouperTeamDynamixUsers) {
    //    System.out.println(grouperTeamDynamixUser);
    //  }
    
    //TeamDynamixUser grouperTeamDynamixUser = retrieveTeamDynamixUser("azure1", "userPrincipalName", "jsmith");
    //TeamDynamixUser grouperTeamDynamixUser = retrieveTeamDynamixUser("azure1", "displayName", "myDispName");
    //System.out.println(grouperTeamDynamixUser);
    
    //  createTeamDynamixMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //createTeamDynamixMemberships("azure1", "dcba5d8d7986432db23a0342887e8fba", GrouperUtil.toSet("1db63cda166a4640b9ef1a0808f90873", "b1dda78d8d42461a93f8b471f26b682e"));
    
    //  deleteTeamDynamixMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
//    TeamDynamixGroup grouperTeamDynamixGroup = new TeamDynamixGroup();
//    grouperTeamDynamixGroup.setDescription("myDescription3");
//    grouperTeamDynamixGroup.setName("myName3");
//    Map<TeamDynamixGroup, Set<String>> map = new HashMap<>();
//    map.put(grouperTeamDynamixGroup, null);
//    createTeamDynamixGroups("azure1", map);

    //deleteTeamDynamixGroup("azure1", "fa356bb8ddb14600be7994cd7b5239a7");
    
//    TeamDynamixGroup grouperTeamDynamixGroup = new TeamDynamixGroup();
//    grouperTeamDynamixGroup.setId("dcba5d8d7986432db23a0342887e8fba");
//    grouperTeamDynamixGroup.setDisplayName("myDisplayName4");
//    grouperTeamDynamixGroup.setMailNickname("whatever");
//    updateTeamDynamixGroup("azure1", grouperTeamDynamixGroup, GrouperUtil.toSet("displayName"));
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
   * create a membership
   * @param grouperTeamDynamixGroup
   * @return the result
   */
  private static void createTeamDynamixMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createTeamDynamixMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

      String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
          "grouper.azureConnector."+configId+".resourceEndpoint");
      
      objectNode.put("@odata.id", GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/" + GrouperUtil.escapeUrlEncode(userId));
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);
      
      int[] returnCode = new int[] { -1 };
      
      if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
        GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureMemberhipErrorCount", 1);
      }
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members/$ref",
          GrouperUtil.toSet(204, 400, 429), returnCode, jsonStringToSend);
      
      if (returnCode[0] == 429) {
        int secondsToSleep = retrieveSecondsToSleep(null);
        GrouperUtil.sleep(secondsToSleep * 1000);
        
        GrouperUtil.mapAddValue(debugMap, "azureThrottleCount", 1);
        if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
          GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleCount", 1);
        }
        
        GrouperUtil.mapAddValue(debugMap, "azureThrottleSleepSeconds", secondsToSleep);
        if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
          GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleSleepSeconds", secondsToSleep);
        }
        
        createTeamDynamixMembership(configId, groupId, userId);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

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

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend);
      
      if (jsonNode == null) {
        throw new RuntimeException("error creating team dyamix membership for groupId "+groupId);
      }

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
      jsonJacksonNode.put("isActive", true);
      
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
  
  public static List<TeamDynamixUser> retrieveTeamDynamixUsers(String configId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<TeamDynamixUser> results = new ArrayList<TeamDynamixUser>();
    
    debugMap.put("method", "retrieveTeamDynamixUsers");
    
    long startTime = System.nanoTime();
    
    try {
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, "api/people/userlist?isActive=true",
           new int[] { -1 });
      
      ArrayNode usersArray = (ArrayNode) jsonNode;
      
      for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
        JsonNode groupNode = usersArray.get(i);
        TeamDynamixUser grouperTeamDynamixGroup = TeamDynamixUser.fromJson(groupNode);
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

  
  private static void retrieveUserGroupsHelper(String configId, Map<String, Object> debugMap, 
      String urlSuffix, boolean securityEnabledOnly, Set<String> result) {
    
    int[] returnCode = new int[] {-1};
    JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200, 429), returnCode,
        "{\"securityEnabledOnly\": " + securityEnabledOnly + "}");

    if (returnCode[0] == 429) {
      int secondsToSleep = retrieveSecondsToSleep(null);
      GrouperUtil.sleep(secondsToSleep * 1000);
      
      GrouperUtil.mapAddValue(debugMap, "azureThrottleCount", 1);
      if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
        GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleCount", 1);
      }
      
      GrouperUtil.mapAddValue(debugMap, "azureThrottleSleepSeconds", secondsToSleep);
      if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
        GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleSleepSeconds", secondsToSleep);
      }
      
      retrieveUserGroupsHelper(configId, debugMap, urlSuffix, securityEnabledOnly, result);
    } else {
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

  }
  
  
  /**
   * return user ids in the group
   * @param configId
   * @param userId
   * @return group ids
   */
  public static Set<String> retrieveTeamDynamixUserGroups(String configId, String userId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUserGroups");

    long startTime = System.nanoTime();
    
    Set<String> result = new LinkedHashSet<String>();

    try {

      String urlSuffix = "/users/" + GrouperUtil.escapeUrlEncode(userId) + "/getMemberGroups";

      for (boolean securityEnabledOnly: new boolean[] {true, false}) { 

        retrieveUserGroupsHelper(configId, debugMap, urlSuffix, securityEnabledOnly, result);
        
      }
      
      return result;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
  }

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveTeamDynamixGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixGroupMembers");

    int calls = 0;
    long startTime = System.nanoTime();
    
    Set<String> result = new LinkedHashSet<String>();

    try {

      int azureGetMembershipPagingSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetMembershipPagingSize", 999);
      
      String urlSuffix = "/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members?$select=id&$top=" + azureGetMembershipPagingSize;

      int[] returnCode = new int[] { -1 };
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, returnCode);
      
      if (returnCode[0] == 429) {
        int secondsToSleep = retrieveSecondsToSleep(null);
        GrouperUtil.sleep(secondsToSleep * 1000);
        
        GrouperUtil.mapAddValue(debugMap, "azureThrottleCount", 1);
        if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
          GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleCount", 1);
        }
        
        GrouperUtil.mapAddValue(debugMap, "azureThrottleSleepSeconds", secondsToSleep);
        if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
          GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleSleepSeconds", secondsToSleep);
        }
        
        return retrieveTeamDynamixGroupMembers(configId, groupId);
      }
      
      //lets get the group node
      retrieveTeamDynamixGroupMembersHelper(result, jsonNode);
      debugMap.put("calls", ++calls);
      
      String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
          "grouper.azureConnector."+configId+".resourceEndpoint");
      
      for (int i=0; i<1000000; i++) {
        String nextLink = GrouperUtil.jsonJacksonGetString(jsonNode, "@odata.nextLink");
        if (StringUtils.isBlank(nextLink)) {
          break;
        }
        
        if (!nextLink.startsWith(resourceEndpoint)) {
          if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperTeamDynamixAllowNextLinkPrefixMismatch", false)) {
            throw new RuntimeException("@odata.nextLink is going to a different URL! '" + nextLink + "', '" + resourceEndpoint + "'");
          }
        } else {
          urlSuffix = nextLink.substring(resourceEndpoint.length(), nextLink.length());
        }
        JsonNode localJsonNode = executeGetMethod(debugMap, configId, urlSuffix, returnCode);
        
        if (returnCode[0] == 429) {
          int secondsToSleep = retrieveSecondsToSleep(null);
          GrouperUtil.sleep(secondsToSleep * 1000);
          GrouperUtil.mapAddValue(debugMap, "azureThrottleCount", 1);
          if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
            GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleCount", 1);
          }
          
          GrouperUtil.mapAddValue(debugMap, "azureThrottleSleepSeconds", secondsToSleep);
          if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
            GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleSleepSeconds", secondsToSleep);
          }
          
          continue;
        }
        
        jsonNode = localJsonNode;
        
        retrieveTeamDynamixGroupMembersHelper(result, jsonNode);
        debugMap.put("calls", ++calls);
      }

      return result;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }
  }

  private static void retrieveTeamDynamixGroupMembersHelper(Set<String> result, JsonNode jsonNode) {
    ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
    if (value != null && value.size() > 0) {
      for (int i=0;i<value.size();i++) {
        JsonNode membership = value.get(i);
        result.add(GrouperUtil.jsonJacksonGetString(membership, "id"));
      }
    }
  }

  private static void deleteMembershipsHelper(String configId, Map<String, Object> debugMap, 
      List<ProvisioningMembership> membershipsInOneHttpRequest, Map<ProvisioningMembership, Exception> membershipToMayBeException) {
    
    List<ProvisioningMembership> throttledMembershipsToDelete = new ArrayList<>();
    
    int secondsToSleep = -1;
    
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
        if (statusCode == 429) {
          throttledMembershipsToDelete.add(provisioningMembership);
          
          int localSecondsToSleep = retrieveSecondsToSleep(oneResponse);
          secondsToSleep = Math.max(localSecondsToSleep, secondsToSleep);
          
        } else if (statusCode != 204 && statusCode != 404) {
          StringBuilder error = buildError(oneResponse, statusCode, bodyNode);
          membershipToMayBeException.put(provisioningMembership, new RuntimeException(error.toString()));
          
        } else {
          membershipToMayBeException.put(provisioningMembership, null);
        }
      }
    }
    
    if (throttledMembershipsToDelete.size() > 0) {
      if (secondsToSleep < 0) {
        secondsToSleep = 155;
      }
      
      GrouperUtil.sleep(secondsToSleep * 1000);
      
      GrouperUtil.mapAddValue(debugMap, "azureThrottleCount", 1);
      if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
        GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleCount", 1);
      }
      
      GrouperUtil.mapAddValue(debugMap, "azureThrottleSleepSeconds", secondsToSleep);
      if (GrouperProvisioner.retrieveCurrentGrouperProvisioner() != null) {
        GrouperUtil.mapAddValue(GrouperProvisioner.retrieveCurrentGrouperProvisioner().getDebugMap(), "azureThrottleSleepSeconds", secondsToSleep);
      }
      deleteMembershipsHelper(configId, debugMap, throttledMembershipsToDelete, membershipToMayBeException);
      
    }
    
  }

  /**
   * delete memberships
   */
  public static Map<ProvisioningMembership, Exception> deleteTeamDynamixMemberships(String configId, List<ProvisioningMembership> membershipsToDelete) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteTeamDynamixMemberships");

    long startTime = System.nanoTime();
    
    Map<ProvisioningMembership, Exception> membershipToMayBeException = new HashMap<>();

    try {
      
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(membershipsToDelete, 20, false);
      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);
      
      for (int httpRequestIndex=0; httpRequestIndex<numberOfHttpRequests; httpRequestIndex++) {
        
        List<ProvisioningMembership> membershipsInOneHttpRequest = GrouperUtil.batchList(membershipsToDelete, 20, httpRequestIndex);
        
        deleteMembershipsHelper(configId, debugMap, membershipsInOneHttpRequest, membershipToMayBeException);
        
      }

      return membershipToMayBeException;
      
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
  
  public static void deleteTeamDynamixUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteTeamDynamixUser");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/users/" + userId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }
  
  public static void deleteTeamDynamixGroup(String configId,
      String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/groups/" + groupId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * create a user
   * @param TeamDynamixUser
   * @return the result
   */
  public static TeamDynamixUser createTeamDynamixUser(String configId,
      TeamDynamixUser grouperDuoUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createTeamDynamixUser");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperDuoUser.toJson(null);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/users",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      TeamDynamixUser teamDynamixUserResult = TeamDynamixUser.fromJson(jsonNode);

      return teamDynamixUserResult;
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
      TeamDynamixGroup grouperGoogleGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperGoogleGroup.toJson(null);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/groups", GrouperUtil.toSet(201), 
          new int[] { -1 }, jsonStringToSend);

      TeamDynamixGroup teamDynamixGroupResult = TeamDynamixGroup.fromJson(jsonNode);
      
      return teamDynamixGroupResult;
      
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

      String urlSuffix = "/users/" + id;

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
      TeamDynamixUser grouperDuoUser = TeamDynamixUser.fromJson(userNode);

      return grouperDuoUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param externalId
   * @return
   */
  public static TeamDynamixUser retrieveTeamDynamixUserByExternalId(String configId, String externalId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixUserByExternalId");

    long startTime = System.nanoTime();

    try {

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("IsActive", true);
      jsonJacksonNode.put("SearchText", externalId);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/people/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode usersArray = (ArrayNode) jsonNode.get("response");
      
      if (usersArray == null || usersArray.size() == 0) {
        return null;
      } else if (usersArray.size() > 1) {
        throw new RuntimeException("How can there be more than one user with the same external id in team dynamix?? '" + externalId + "'");
      } else {
        JsonNode userNode = usersArray.get(0);
        TeamDynamixUser grouperDuoUser = TeamDynamixUser.fromJson(userNode);
        return grouperDuoUser;
      }
      
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
      TeamDynamixGroup grouperDuoGroup = TeamDynamixGroup.fromJson(groupNode);

      return grouperDuoGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TeamDynamixLog.teamDynamixLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param username
   * @return
   */
  public static TeamDynamixGroup retrieveTeamDynamixGroupByName(String configId, String username) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveTeamDynamixGroupByName");

    long startTime = System.nanoTime();

    try {

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("isActive", true);
      jsonJacksonNode.put("NameLike", username);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/groups/search",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      ArrayNode groupsArray = (ArrayNode) jsonNode;
      
      if (groupsArray == null || groupsArray.size() == 0) {
        return null;
      } else if (groupsArray.size() > 1) {
        throw new RuntimeException("How can there be more than one group with the same name in TeamDynamix?? '" + username + "'");
      } else {
        JsonNode userNode = groupsArray.get(0);
        TeamDynamixGroup grouperDuoUser = TeamDynamixGroup.fromJson(userNode);
        return grouperDuoUser;
      }
      
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
      GrouperDuoLog.duoLog(debugMap, startTime);
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
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static void patchTeamDynamixUser(String configId,
      TeamDynamixUser grouperScim2User, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {

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
        
        switch (provisioningObjectChangeAction) {
          case insert:
            operationNode.put("op", "add");
            operationNode.put("value", GrouperUtil.stringValue(GrouperUtil.fieldValue(grouperScim2User, fieldToUpdate)));
            break;
          case update:
            operationNode.put("op", "replace");
            Object resolvedObject = GrouperUtil.fieldValue(grouperScim2User, fieldToUpdate);
            if (resolvedObject != null && resolvedObject instanceof Boolean) {
              operationNode.put("value", GrouperUtil.booleanValue(resolvedObject));
            } else {
              operationNode.put("value", GrouperUtil.stringValue(GrouperUtil.fieldValue(grouperScim2User, fieldToUpdate)));
            }
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

      executeMethod(debugMap, "PATCH", configId, "/api/people/"+ GrouperUtil.escapeUrlEncode(grouperScim2User.getUid()),
          GrouperUtil.toSet(200, 204), new int[] { -1 }, jsonStringToSend);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }

  }
  
  
  /**
   * update a group
   * @param grouperGoogleGroup
   * @return the result
   */
  public static TeamDynamixGroup updateTeamDynamixGroup(String configId,
      TeamDynamixGroup grouperGoogleGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateTeamDynamixGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperGoogleGroup.getId();
      
      JsonNode jsonToSend = grouperGoogleGroup.toJson(fieldsToUpdate);
      
      TeamDynamixGroup updatedGoogleGroup = null;
      
      if (jsonToSend.size() > 0) {
        String urlSuffix = "/api/groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

        updatedGoogleGroup = TeamDynamixGroup.fromJson(jsonNode);
      }

      return updatedGoogleGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }
 

}

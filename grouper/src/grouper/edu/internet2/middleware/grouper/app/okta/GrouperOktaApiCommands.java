package edu.internet2.middleware.grouper.app.okta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoLog;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperOktaApiCommands {
  
  public static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId, String urlSuffix) {

    int[] returnCode = new int[] { -1 };
    JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), returnCode, null);
    
    if (returnCode[0] == 404) {
      return null;
    }
    
    return jsonNode;
  }

  public static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    grouperHttpCall.assignDebugMap(debugMap);
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
    String tenantDomain = grouperLoaderConfig.propertyValueStringRequired("grouper.wsBearerToken." + configId + ".serviceUrl");

    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpCall, configId, grouperLoaderConfig, debugMap);
    
    String proxyUrl = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + configId + ".proxyUrl");
    String proxyType = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);
    
    String url = "";
    if (urlSuffix.startsWith("https://")) { // for pagination, we are passing the full url instead of url suffix
      url = urlSuffix;
    } else {      
      url = tenantDomain + (tenantDomain.endsWith("/") ? "" : "/") +  urlSuffix;
    }
    
    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.assignBody(body);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssuesSleepMillis(60*1000L); // 1min
    
    grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {
      
      @Override
      public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
        boolean isThrottle = httpClient.getResponseCode() == 403 
            || httpClient.getResponseCode() == 429 || httpClient.getResponseCode() == 503;
        if (isThrottle) {                
          GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
        }
        return isThrottle;
      }
    });
    
    grouperHttpCall.executeRequest();
    
    int code = -1;
    String json = null;

    Map<String,String> prevNextHeader = new HashMap<String, String>();
    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
      Map<String,String> responseHeaders = grouperHttpCall.getResponseHeaders();
      if (responseHeaders.containsKey("link")) {
        prevNextHeader = parseLinkHeader(responseHeaders.get("link"));
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
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      JsonNode dataNode = GrouperUtil.jsonJacksonNode(json);
      resultNode.set("data", dataNode);
      if (prevNextHeader.containsKey("prev") || prevNextHeader.containsKey("next")) {
        ObjectNode paginationNode = GrouperUtil.jsonJacksonNode();
        resultNode.set("pagination", paginationNode);
        if (prevNextHeader.containsKey("prev")) {      
          GrouperUtil.jsonJacksonAssignString(paginationNode, "prev", prevNextHeader.get("prev"));
        }
        if (prevNextHeader.containsKey("next")) { 
          GrouperUtil.jsonJacksonAssignString(paginationNode, "next", prevNextHeader.get("next"));
        }
        
      }
      return resultNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  
  private static Map<String, String> parseLinkHeader(String header) {
    
    Map<String, String> links = new HashMap<>();
    
    if (StringUtils.isBlank(header)) {
      return links;
    }

    // Split the header into individual links
    String[] parts = header.split(", ");
    for (String part : parts) {
        // Match the URL and rel value
        String[] sections = part.split("; ");
        if (sections.length == 2) {
            String url = sections[0].trim();
            String rel = sections[1].trim();

            // Remove the angle brackets from the URL
            if (url.startsWith("<") && url.endsWith(">")) {
                url = url.substring(1, url.length() - 1);
            }

            // Extract the rel value
            if (rel.startsWith("rel=\"") && rel.endsWith("\"")) {
                rel = rel.substring(5, rel.length() - 1);
            }

            // Add to the map
            links.put(rel, url);
        }
    }

    return links;
  }

  /**
   * create a group
   * @param grouperOktaGroup
   * @return the result
   */
  public static GrouperOktaGroup createOktaGroup(String configId,
      GrouperOktaGroup grouperOktaGroup, Set<String> fieldsToInsert) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperOktaGroup.toJsonGroupOnly(fieldsToInsert);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "groups", GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend);
      
      GrouperOktaGroup grouperOktaGroupResult = null;
      if (jsonNode != null && jsonNode.has("data")) {        
        grouperOktaGroupResult = GrouperOktaGroup.fromJson(jsonNode.get("data"));
      }

      return grouperOktaGroupResult;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

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
   * create a user
   * @param configId
   * @param grouperOktaUser
   * @return
   */
  public static GrouperOktaUser createOktaUser(String configId, GrouperOktaUser grouperOktaUser) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaUser");

    long startTime = System.nanoTime();

    try {
      
      JsonNode jsonToSend = grouperOktaUser.toJson();
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "users",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      GrouperOktaUser grouperOktaUserResult = null;
      if (jsonNode != null && jsonNode.has("data")) {
        grouperOktaUserResult = GrouperOktaUser.fromJson(jsonNode.get("data"));
      }
      
      return grouperOktaUserResult;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
    
    
  }

  /**
   * create a membership
   *
   * @param configId
   * @param groupId
   * @param userId
   */
  public static void createOktaMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      objectNode.put("id", userId);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697
      String urlSuffix = "groups/"+groupId+"/users/"+userId;

      executeMethod(debugMap, "PUT", configId, urlSuffix, GrouperUtil.toSet(204), 
          new int[] { -1 }, jsonStringToSend);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static void deleteOktaUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaUser");
    
    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
      
      //users/00umxoh7cgDm3zD9v697
      int[] returnCode = new int[1];
      //first delete marks the user as deactivated and the second one actually deletes it
      executeMethod(debugMap, "DELETE", configId, "users/"+userId,
          GrouperUtil.toSet(200, 204, 404), returnCode, null);
      
      if (returnCode[0] == 200 || returnCode[0] == 204) {
        executeMethod(debugMap, "DELETE", configId, "users/"+userId,
            GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * update a group except the managers and owners of the group
   * @param grouperOktaGroup
   * @return the result
   */
  public static GrouperOktaGroup updateOktaGroup(String configId,
      GrouperOktaGroup grouperOktaGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateOktaGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperOktaGroup.getId();
      
      JsonNode jsonToSend = grouperOktaGroup.toJsonGroupOnly(null);
      
      GrouperOktaGroup updatedOktaGroup = null;
      
      if (jsonToSend.size() > 0) {
        //groups/00gmxp8w6iYQLHtEN697
        String urlSuffix = "groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

        updatedOktaGroup = GrouperOktaGroup.fromJson(jsonNode);
      }
      
      return updatedOktaGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperOktaUser
   * @return the result
   */
  public static GrouperOktaUser updateOktaUser(String configId,
      GrouperOktaUser grouperOktaUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateOktaUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperOktaUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperOktaUser);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperOktaUser + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
      JsonNode jsonToSend = grouperOktaUser.toJson();
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "users/"+id,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

      GrouperOktaUser grouperOktaUserResult = GrouperOktaUser.fromJson(jsonNode.get("data"));

      return grouperOktaUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static void deleteOktaGroup(String configId,String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "groups/"+groupId,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }

  public static List<GrouperOktaGroup> retrieveOktaGroups(String configId, String fieldToSearchFor,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperOktaGroup> results = new ArrayList<GrouperOktaGroup>();

      String nextPageUrl = null;
      String previousPageUrl = null;
      boolean firstRequest = true;
      
      String urlSuffixConstant = "groups";
      
      if (StringUtils.isNotBlank(fieldToSearchFor)) {
        
        String filterValue = fieldToSearchFor + " eq "+ "\"" + fieldValue + "\"";
        String urlEncodedFilter = GrouperUtil.escapeUrlEncode(filterValue);
        urlSuffixConstant = urlSuffixConstant + "?search="+urlEncodedFilter;
      }
      
      int maxCalls = 10000;
      int numberOfCalls = 0;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("data");
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }
        
        if (groupsArray == null || groupsArray.size() == 0) {
          break;
        }

        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromJson(groupNode);
          
          results.add(grouperOktaGroup);
        }
        
        if (StringUtils.isNotBlank(previousPageUrl) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageUrl, nextPageUrl)) {
          break;
        }
        previousPageUrl = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static List<GrouperOktaUser> retrieveOktaUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperOktaUser> results = new ArrayList<GrouperOktaUser>();
      
      String nextPageUrl = null;
      boolean firstRequest = true;
      
      String urlSuffixConstant = "users";
      
      int maxCalls = 1000000;
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", numberOfCalls: " + numberOfCalls);
        }

        firstRequest = false;
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("data");
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }
        
        if (usersArray == null || usersArray.size() == 0) {
          break;
        }

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(userNode);
          results.add(grouperOktaUser);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageToken, nextPageUrl)) {
          break;
        }
        previousPageToken = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param id of the user
   * @return okta user
   */
  public static GrouperOktaUser retrieveOktaUser(String configId, String fieldName, String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaUser");

    long startTime = System.nanoTime();

    try {
      //TODO review commands classes and make sure we are url encoding field names and values like below
      String urlSuffix = "users?search="+GrouperUtil.escapeUrlEncode(fieldName)+"+eq+%22"+GrouperUtil.escapeUrlEncode(fieldValue)+"%22";
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
      
      if (jsonNode == null || jsonNode.get("data") == null) {
        return null;
      }
      
      ArrayNode users = (ArrayNode)jsonNode.get("data");
      if (users.size() == 0 || users.size() > 1) {
        return null;
      }
      
      GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(users.get(0));
      return grouperOktaUser;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }
  
  public static void main(String[] args) {
    GrouperSession.startRootSession();
    
    List<GrouperOktaUser> oktaUsers = retrieveOktaUsers("oktaExternalSystemWs");
    
    System.exit(0);
    
  }
  

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveOktaGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroupMembers");

    long startTime = System.nanoTime();

    try {

      Set<String> memberIds = new HashSet<String>();
      
      String nextPageUrl = null;
      boolean firstRequest = true;
      
      //groups/00gmvgcs9mpZKSfAX697/users
      String urlSuffixConstant = "groups/"+groupId+"/skinny_users";

      int maxCalls = 10000000;
      int numberOfCalls = 0;
      String previousPageUrl = null;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + memberIds.size()
             + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode membersArray = (ArrayNode)jsonNode.get("data");
        
        if (membersArray == null || membersArray.size() == 0) {
          break;
        }
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }

        for (int i = 0; i < (membersArray == null ? 0 : membersArray.size()); i++) {
          JsonNode memberNode = membersArray.get(i);
          
          String memberId = GrouperUtil.jsonJacksonGetString(memberNode, "id");
          
          memberIds.add(memberId);
        }
        
        if (StringUtils.isNotBlank(previousPageUrl) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageUrl, nextPageUrl)) {
          break;
        }
        previousPageUrl = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(memberIds));

      return memberIds;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }
  
  /**
   * @param configId
   * @param id is the group id
   * @return the okta group
   */
  public static GrouperOktaGroup retrieveOktaGroup(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "groups/"+id;
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
      
      if (jsonNode == null || jsonNode.get("data") == null) {
        return null;
      }
      
      GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromJson(jsonNode.get("data"));
      
      return grouperOktaGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }



  /**
   * delete membership
   * @param grouperOktaGroup
   * @return the result
   */
  public static void deleteOktaMembership(String configId, String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaMembership");

    long startTime = System.nanoTime();

    try {
     // groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697
      executeMethod(debugMap, "DELETE", configId, "groups/"+groupId+"/users/"+userId,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }

}

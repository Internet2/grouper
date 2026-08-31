package edu.internet2.middleware.grouper.app.adobe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAdobeApiCommands {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperAdobeApiCommands.class);
  


  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
//    String configId = "adobeTest";
//    List<GrouperAdobeGroup> adobeGroups = retrieveAdobeGroups(configId);
//    System.out.println("adobe groups size = "+adobeGroups.size());
//    
//    List<GrouperAdobeUser> adobeUsers = retrieveAdobeUsers(configId, true);
//    System.out.println("adobe users size = "+adobeUsers.size());
//    
//    for (GrouperAdobeUser grouperAdobeUser: adobeUsers) {
//      List<GrouperAdobeGroup> groupsByUser = retrieveAdobeGroupsByUser(configId, grouperAdobeUser.getId());
//      System.out.println("for user: "+grouperAdobeUser.getUserName()+ " found: "+groupsByUser.size()+ " groups");
//    }
//    
//    GrouperAdobeUser userByName = retrieveAdobeUserByName(configId, "mchyzer");
//    System.out.println("userByName: "+userByName);
    
//    associateUserToGroup("adobe1", "DUP0LW3MHLGSFMGGQAV3", "DGCXPKWT7MJ7WLQT7CMQ");
    
//    GrouperAdobeUser grouperAdobeUser = retrieveAdobeUser("adobe", "mplatt@pennmedicine.upenn.edu", false, "5DE9B008561EC4997F000101@AdobeOrg");
//    System.out.println(grouperAdobeUser);

    
    System.exit(0);
        
  }

  public static void main1(String[] args) {

  }
  
  
  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization", "x-api-key");

  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String bodyParam, String[] returnBody) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(doNotLogHeaders).assignDoNotLogParameters(doNotLogParameters);

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpCall, configId, grouperLoaderConfig, debugMap);

    String url = grouperLoaderConfig.propertyValueStringRequired("grouper.wsBearerToken." + configId + ".endpoint");


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
    
    String proxyUrl = grouperLoaderConfig.propertyValueString("grouperClient.wsBearerToken." + configId + ".proxyUrl");
    String proxyType = grouperLoaderConfig.propertyValueString("grouperClient.wsBearerToken." + configId + ".proxyType");
    
    if (StringUtils.isNotBlank(proxyUrl)) {
      grouperHttpCall.assignProxyUrl(proxyUrl);
    }
    
    if (StringUtils.isNotBlank(proxyType)) {
      grouperHttpCall.assignProxyType(proxyType);
    }
    
    grouperHttpCall.addHeader("Content-Type", "application/json");

    grouperHttpCall.assignBody(bodyParam);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssuesBackOffMillis(2*60*1000);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssuesSleepMillis(2*60*1000);
    
    grouperHttpCall.assignRetryForThrottlingIsMinutes(true);
    
    grouperHttpCall.assignRetryForThrottlingUseRetryAfter(false);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssues(30);

    
    grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {
      
      @Override
      public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
        String throttlingBody = StringUtils.trim(httpClient.getResponseBody());
        try {
          if (StringUtils.isNotBlank(throttlingBody) && throttlingBody.contains("error_code") && throttlingBody.contains("\"429")) {
//            JsonNode node = GrouperUtil.jsonJacksonNode(body);
//            String errorCode = GrouperUtil.jsonJacksonGetString(node, "error_code");
//            boolean isThrottle = errorCode != null && errorCode.startsWith("429");
//            if (isThrottle) {                
              GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
              return true;
//              return isThrottle;
//            }
            
          }
        } catch(Exception e) {
          LOG.error("Error: " + debugMap.get("url") + ", " + grouperHttpCall.getResponseCode() + ", " + throttlingBody, e);
        }
      
        boolean isThrottle = grouperHttpCall.getResponseCode() == 429;
        if (isThrottle) {                
          GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
        }
        return isThrottle;
        }
    });
    long httpCallStartMillis = System.currentTimeMillis();
    try {
      grouperHttpCall.executeRequest();
    } finally {
      GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
          System.currentTimeMillis() - httpCallStartMillis);
    }

    int code = -1;
    String json = null;

    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
      if (returnBody != null && returnBody.length > 0) {
        returnBody[0] = json;
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
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  

  /**
   * encode URL param
   * @param param
   * @return the value
   */
  public static String escapeUrlEncode(String param) {
    return GrouperUtil.escapeUrlEncode(param).replace("+", "%20").replace(":", "%3A");
  }

  /**
   * create a group
   * @param grouperAdobeGroup
   * @return the result
   */
  public static void createAdobeGroup(String configId,
      GrouperAdobeGroup grouperAdobeGroup, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAdobeGroup");

    long startTime = System.nanoTime();

    try {
      /**
       * [
        {
          "usergroup": "myTestGroup",
          "do": [
                  {
                    "createUserGroup": {
                      "name": "myTestGroup",
                      "option": "ignoreIfAlreadyExists"
                    }
                  }
               ]
        }
      ]
       */
      
      ObjectNode innerNode = GrouperUtil.jsonJacksonNode();
      innerNode.put("name", grouperAdobeGroup.getName());
      innerNode.put("option", "ignoreIfAlreadyExists");
      
      ObjectNode createUserGroupNode = GrouperUtil.jsonJacksonNode();
      createUserGroupNode.set("createUserGroup", innerNode);
      
      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      arrayNode.add(createUserGroupNode);
      
      ObjectNode outerNode = GrouperUtil.jsonJacksonNode();
      outerNode.set("do", arrayNode);
      outerNode.put("usergroup", grouperAdobeGroup.getName());
      
      ArrayNode finalArrayNode = GrouperUtil.jsonJacksonArrayNode();
      finalArrayNode.add(outerNode);
      
      String bodyToSend = GrouperUtil.jsonJacksonToString(finalArrayNode);

      JsonNode jsonNode = executeMethod(debugMap, "createAdobeGroup", "POST", configId, "/action/" + orgId,
          GrouperUtil.toSet(200), new int[] { -1 }, bodyToSend, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }

  /**
   * update a group
   */
  public static void updateAdobeGroup(String configId,
      String oldGroupName, String newGroupName, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateAdobeGroup");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(oldGroupName) || StringUtils.isBlank(newGroupName)) {
        throw new RuntimeException("oldGroupName or newGroupName is blank");
      }
      
      /**
       * [
          {
            "usergroup": "OLD_GROUP_NAME",
            "do": [
                    {
                      "updateUserGroup": {
                        "name": "NEW_GROUP_NAME"
                      }
                    }
                 ]
          }
        ]
       */
      
        
      ObjectNode innerMostNode = GrouperUtil.jsonJacksonNode();
      innerMostNode.put("name", newGroupName);
      ObjectNode updateUserGroupNode = GrouperUtil.jsonJacksonNode();
      updateUserGroupNode.set("updateUserGroup", innerMostNode);
      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      arrayNode.add(updateUserGroupNode);
      ObjectNode outerNode = GrouperUtil.jsonJacksonNode();
      outerNode.set("do", arrayNode);
      outerNode.put("usergroup", oldGroupName);
      ArrayNode finalArrayNode = GrouperUtil.jsonJacksonArrayNode();
      finalArrayNode.add(outerNode);
      
      String bodyToSend = GrouperUtil.jsonJacksonToString(finalArrayNode);

      JsonNode jsonNode = executeMethod(debugMap, "updateAdobeGroup", "POST", configId, "/action/" + orgId,
          GrouperUtil.toSet(200), new int[] { -1 }, bodyToSend, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }

  public static void deleteAdobeGroup(String configId, String groupName, String orgId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAdobeGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupName)) {
        throw new RuntimeException("groupName is null");
      }
      
      /**
       * [
          {
            "usergroup": "GROUP_NAME",
            "do": [
                    {
                      "deleteUserGroup": {
                      }
                    }
                 ]
          }
        ]
       */
      
      ObjectNode emptyNode = GrouperUtil.jsonJacksonNode();
      ObjectNode deleteUserGroupNode = GrouperUtil.jsonJacksonNode();
      deleteUserGroupNode.set("deleteUserGroup", emptyNode);
      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      arrayNode.add(deleteUserGroupNode);
      ObjectNode outerNode = GrouperUtil.jsonJacksonNode();
      outerNode.put("usergroup", groupName);
      outerNode.set("do", arrayNode);
      ArrayNode finalArrayNode = GrouperUtil.jsonJacksonArrayNode();
      finalArrayNode.add(outerNode);
      
      String bodyToSend = GrouperUtil.jsonJacksonToString(finalArrayNode);

      executeMethod(debugMap, "deleteAdobeGroup", "POST", configId, "/action/" + orgId,GrouperUtil.toSet(200, 404), new int[] { -1 }, bodyToSend, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }
  }


  public static List<GrouperAdobeGroup> retrieveAdobeGroups(String configId, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAdobeGroups");

    long startTime = System.nanoTime();
    
    try {
      
      List<GrouperAdobeGroup> results = new ArrayList<GrouperAdobeGroup>();
      
      boolean lastPage = false;
      int maxLoops = 0;

      while (lastPage != true && maxLoops < 100000) { //max groups should not be 100,000 * results per page

        JsonNode jsonNode = executeMethod(debugMap, "retrieveAdobeGroups", "GET", configId, "/groups/"+orgId+"/"+maxLoops,
            GrouperUtil.toSet(200), new int[] { -1 }, null, null);
        
        maxLoops = maxLoops + 1;
        
        String result = GrouperUtil.jsonJacksonGetString(jsonNode, "result");
        if (StringUtils.equals(result, "success")) {
          ArrayNode groupsArray = (ArrayNode) jsonNode.get("groups");

          for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
            JsonNode groupNode = groupsArray.get(i);
            GrouperAdobeGroup grouperAdobeGroup = GrouperAdobeGroup.fromJson(groupNode);
            results.add(grouperAdobeGroup);
          }
          
          lastPage = GrouperUtil.jsonJacksonGetBoolean(jsonNode, "lastPage");
        } else if (StringUtils.equals(result, "Not found")) {
          lastPage = true;
        } else {
          throw new RuntimeException("Received invalid result value: "+result);
        }
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
//  public static List<GrouperAdobeGroup> retrieveAdobeGroupsByUser(String configId, String userId, String orgId) {
//
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "retrieveAdobeGroupsByUser");
//
//    long startTime = System.nanoTime();
//
//    try {
//      
//      
//      GrouperAdobeUser adobeUser = retrieveAdobeUser(configId, userId, true, orgId);
//      
//      Set<String> groups = adobeUser.getGroups();
//      
//      for (String group: groups) {
//        retrieveAdobeGroup(configId, orgId);
//      }
//
//      String urlSuffix = "/users/" + userId;
//
//      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
//          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null, "v1");
//      
//      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
//      if (userNode == null) {
//        return new ArrayList<GrouperAdobeGroup>();
//      }
//      GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromJson(userNode, false);
//
//      return null;
//      //return new ArrayList<GrouperAdobeGroup>(grouperAdobeUser.getGroups());
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperAdobeLog.adobeLog(debugMap, startTime);
//    }
//
//  }
  
//  public static List<GrouperAdobeUser> retrieveAdobeUserIdsUserNamesByGroup(String configId, String groupId) {
//    
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "rertrieveAdobeUserIdsUserNamesByGroup");
//
//    long startTime = System.nanoTime();
//
//    try {
//      
//      List<GrouperAdobeUser> results = new ArrayList<GrouperAdobeUser>();
//      
//      int limit = 100;
//      int offset = 0;
//      
//      while (offset >= 0) {
//
//        Map<String, String> params = GrouperUtil.toMap("limit", String.valueOf(limit), "offset", String.valueOf(offset));
//        
//        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "/groups/"+groupId+"/users",
//            GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null, "v2");
//        
//        ArrayNode usersArray = (ArrayNode) jsonNode.get("response");
//
//        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
//          JsonNode userNode = usersArray.get(i);
//          GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromJson(userNode, false);
//          results.add(grouperAdobeUser);
//        }
//        
//        JsonNode metadata = jsonNode.get("metadata");
//        
//        if (metadata != null && metadata.get("next_offset") != null && usersArray.size() >= limit) {
//          offset = metadata.get("next_offset").asInt();
//        } else {
//          offset = -1;
//        }
//        
//      }
//      
//      debugMap.put("size", GrouperClientUtils.length(results));
//      
//      return results;
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperAdobeLog.adobeLog(debugMap, startTime);
//    }
//    
//  }

  /**
   * @param configId
   * @param group id
   * @return the user
   */
//  public static GrouperAdobeGroup retrieveAdobeGroup(String configId, String id) {
//
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "retrieveAdobeGroup");
//
//    long startTime = System.nanoTime();
//
//    try {
//
//      String urlSuffix = "/groups/" + id;
//
//      int[] returnCode = new int[] { -1 };
//      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
//          GrouperUtil.toSet(200, 404), returnCode, null, null, "v1");
//      
//      if (returnCode[0] == 404) {
//        return null;
//      }
//      
//      //lets get the group node
//      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
//      if (groupNode == null) {
//        return null;
//      }
//      GrouperAdobeGroup grouperAdobeGroup = GrouperAdobeGroup.fromJson(groupNode);
//
//      return grouperAdobeGroup;
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperAdobeLog.adobeLog(debugMap, startTime);
//    }
//
//  }
  
  /**
   * create a user
   * @param grouperAdobeUser
   * @return the result
   */
  public static Map<String, GrouperAdobeUser> createAdobeUsers(String configId,
      List<GrouperAdobeUser> grouperAdobeUsers, String userTypeOnCreate, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAdobeUser");

    long startTime = System.nanoTime();

    try {

     /**
      * [
        {
          "user": "abc@school.edu",
          "do": [
            {
              "addAdobeID|createFederatedID|createEnterpriseID": {
                "email": "abc@school.edu",
                "country": "US",
                "firstname": "AbcTest",
                "lastname": "AbcTest"
              }
            }
          ]
        }
      ]
      */

      String errorMessage = null;
      
      int batchSize = 10;
      Map<String, GrouperAdobeUser> resultsEmailToUser = new HashMap<String, GrouperAdobeUser>();
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperAdobeUsers, batchSize, false);
      for (int i=0;i<numberOfBatches;i++) {
        
        List<GrouperAdobeUser> grouperAdobeUsersBatch = GrouperUtil.batchList(grouperAdobeUsers, batchSize, i);

        ArrayNode arrayNodeToSend = GrouperUtil.jsonJacksonArrayNode();

        for (GrouperAdobeUser grouperAdobeUser: grouperAdobeUsersBatch) {
          
          // if email is blank in user object, use userName
          if (StringUtils.isBlank(grouperAdobeUser.getEmail())) {
            grouperAdobeUser.setEmail(grouperAdobeUser.getUserName());
          }

          ObjectNode userNode = grouperAdobeUser.toJson(GrouperUtil.toSet("email", "country", "firstname", "lastname"));
          
          ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
          
          if (StringUtils.equals(userTypeOnCreate, "AdobeID")) {
            objectNode.set("addAdobeID", userNode);
          } else if (StringUtils.equals(userTypeOnCreate, "EnterpriseID")) {
            objectNode.set("createEnterpriseID", userNode);
          } else if (StringUtils.equals(userTypeOnCreate, "FederatedID")) {
            objectNode.set("createFederatedID", userNode);
          }
          
          ArrayNode doArrayNode = GrouperUtil.jsonJacksonArrayNode();
          doArrayNode.add(objectNode);
          
          ObjectNode objectNodeOuter = GrouperUtil.jsonJacksonNode();
          objectNodeOuter.set("do", doArrayNode);
          
          String userName = GrouperUtil.defaultIfBlank(grouperAdobeUser.getUserName(), grouperAdobeUser.getEmail());
          
          
          GrouperUtil.jsonJacksonAssignString(objectNodeOuter, "user", userName);
          
          
          arrayNodeToSend.add(objectNodeOuter);

          
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNodeToSend);

        JsonNode jsonNode = executeMethod(debugMap, "createAdobeUser", "POST", configId, "/action/"+orgId,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, null);
        
        // {"completed":0,"notCompleted":1,"completedInTestMode":0,"result":"error","errors":[{"index":0,"step":0,"message":"Domain for email is not claimed","errorCode":"error.domain.trust.nonexistent","user":"pgtest1@upenn.edu"}]}
        
        int completed = GrouperUtil.jsonJacksonGetInteger(jsonNode, "completed");
        
        // if we have mor than 1 created then thats ok i guess
        if (completed == 0) {
          errorMessage = GrouperUtil.jsonJacksonToString(jsonNode);
        }
        
        if (grouperAdobeUsers.size() < 200) {
          // retrieve from server
          for (GrouperAdobeUser grouperAdobeUser: grouperAdobeUsersBatch) {
            GrouperAdobeUser adobeUserAfterInsert = retrieveAdobeUser(configId, grouperAdobeUser.getEmail(), true, orgId);
            // add if not null
            if (adobeUserAfterInsert != null) {
              resultsEmailToUser.put(adobeUserAfterInsert.getEmail(), adobeUserAfterInsert);
            }
          }
        }        
      }
      
      // if we have more than 200, then retrieve all and match up
      if (grouperAdobeUsers.size() >= 200) {
        
        // retrieve all from server
        List<GrouperAdobeUser> allGrouperAdobeUsers = retrieveAdobeUsers(configId, true, orgId);
        Map<String, GrouperAdobeUser> emailToGrouperAdobeUser = new HashMap<String, GrouperAdobeUser>();
        
        // map by email
        for (GrouperAdobeUser grouperAdobeUser : allGrouperAdobeUsers) {
          emailToGrouperAdobeUser.put(grouperAdobeUser.getEmail(), grouperAdobeUser);
        }
        
        // go through the ones we inserted
        for (GrouperAdobeUser grouperAdobeUser: grouperAdobeUsers) {
          GrouperAdobeUser adobeUserAfterInsert = emailToGrouperAdobeUser.get(grouperAdobeUser.getEmail());
          // add if not null
          if (adobeUserAfterInsert != null) {
            resultsEmailToUser.put(adobeUserAfterInsert.getEmail(), adobeUserAfterInsert);
          }
        }
        
      }

      if (resultsEmailToUser.size() == 0 && !StringUtils.isBlank(errorMessage)) {
        throw new RuntimeException("Did not create any users, here is a sample error message: " + errorMessage);
      }

      return resultsEmailToUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperAdobeUser
   * @param fieldsToUpdate
   * @return the result
   */
  public static void updateAdobeUser(String configId,
      GrouperAdobeUser grouperAdobeUser, Set<String> fieldsToUpdate, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateAdobeUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperAdobeUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperAdobeUser);
      }
      
      Map<String, String> params = GrouperUtil.toMap();
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("firstname")) {
        params.put("firstname", StringUtils.defaultString(grouperAdobeUser.getFirstName()));
      }
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("lastname")) {
        params.put("lastname", StringUtils.defaultString(grouperAdobeUser.getLastName()));
      }
      
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("country")) {
        params.put("country", StringUtils.defaultString(grouperAdobeUser.getCountry()));
      }
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("email")) {
        params.put("email", StringUtils.defaultString(grouperAdobeUser.getEmail()));
      }

      /**
       * [
          {
            "user": "abc@upenn.edu",
            "do": [
              {
                "update": {
                  "lastname": "AbcTest1"
                }
              }
            ]
          }
        ]
       */
      
      
      ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
      
      ObjectNode valuesThatNeedUpdates = GrouperUtil.jsonConvertFromObjectToObjectNode(params);
      
      // add attribute:  "option": "ignoreIfAlreadyExists"
      GrouperUtil.jsonJacksonAssignString(valuesThatNeedUpdates, "option", "ignoreIfAlreadyExists");
      
      objectNode.set("update", valuesThatNeedUpdates);
      
      ArrayNode doArrayNode = GrouperUtil.jsonJacksonArrayNode();
      doArrayNode.add(objectNode);
      
      ObjectNode objectNodeOuter = GrouperUtil.jsonJacksonNode();
      objectNodeOuter.set("do", doArrayNode);
      GrouperUtil.jsonJacksonAssignString(objectNodeOuter, "user", grouperAdobeUser.getEmail());
      
      
      ArrayNode arrayNodeToSend = GrouperUtil.jsonJacksonArrayNode();
      arrayNodeToSend.add(objectNodeOuter);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNodeToSend);

      JsonNode jsonNode = executeMethod(debugMap, "updateAdobeUser", "POST", configId, "/action/"+orgId,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, null);
      

//      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
//      
//      GrouperAdobeUser grouperAdobeUserResult = GrouperAdobeUser.fromJson(groupNode, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
  public static List<GrouperAdobeUser> retrieveAdobeUsers(String configId, boolean includeLoadedFields, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAdobeUsers");

    long startTime = System.nanoTime();

    try {
      
      List<GrouperAdobeUser> results = new ArrayList<GrouperAdobeUser>();
      
      boolean lastPage = false;
      int maxLoops = 0;
      
      while (lastPage != true && maxLoops < 100000) { //max users should not be 100,000 * results per page  
        
        int[] returnCode = new int[] { -1 };
        String[] returnBody = new String[1];
        JsonNode jsonNode = executeMethod(debugMap, "retrieveAdobeUsers", "GET", configId, "/users/"+orgId+"/"+maxLoops,
            GrouperUtil.toSet(200), returnCode, null, returnBody);
        
        maxLoops = maxLoops + 1;
        
        String result = GrouperUtil.jsonJacksonGetString(jsonNode, "result");
        if (StringUtils.equals(result, "success")) {
          ArrayNode usersArray = (ArrayNode) jsonNode.get("users");

          for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
            JsonNode userNode = usersArray.get(i);
            GrouperAdobeUser grouperAdobeGroup = GrouperAdobeUser.fromJson(userNode, includeLoadedFields);
            results.add(grouperAdobeGroup);
          }
          
          lastPage = GrouperUtil.jsonJacksonGetBoolean(jsonNode, "lastPage");
        } else if (StringUtils.equals(result, "Not found")) {
          lastPage = true;
        } else {
          throw new RuntimeException("Received invalid result value: "+result + ", " + returnBody[0]);
        }
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
  public static GrouperAdobeUser retrieveAdobeUser(String configId, String email, boolean includeLoadedFields, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAdobeUser");

    long startTime = System.nanoTime();

    try {
      //organizations/$ORG_ID$/users/email@address.edu
      String urlSuffix = "/organizations/"+orgId+"/users/" + email;

      int[] returnCode = new int[] { -1 };

      JsonNode jsonNode = executeMethod(debugMap, "retrieveAdobeUser", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null);
      
      if (returnCode[0] == 404) {
        return null;
      }

      String result = GrouperUtil.jsonJacksonGetString(jsonNode, "result");
      if (StringUtils.equals("success", result)) {
        JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "user");
        if (userNode == null) {
          return null;
        }
        GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromJson(userNode, includeLoadedFields);

        return grouperAdobeUser;
      } else {
        throw new RuntimeException("Did not receive success for result field instead received: "+result);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param username
   * @return
   */
//  public static GrouperAdobeUser retrieveAdobeUserByName(String configId, String username) {
//
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "retrieveAdobeUserByName");
//
//    long startTime = System.nanoTime();
//
//    try {
//
//      String urlSuffix = "/users";
//      
//      Map<String, String> params = GrouperUtil.toMap("username", StringUtils.defaultString(username));
//
//      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
//          GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null, "v1");
//      
//      ArrayNode usersArray = (ArrayNode) jsonNode.get("response");
//      
//      if (usersArray == null || usersArray.size() == 0) {
//        return null;
//      } else if (usersArray.size() > 1) {
//        throw new RuntimeException("How can there be more than one user with the same username in adobe?? '" + username + "'");
//      } else {
//        JsonNode userNode = usersArray.get(0);
//        GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromJson(userNode, false);
//        return grouperAdobeUser;
//      }
//      
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperAdobeLog.adobeLog(debugMap, startTime);
//    }
//
//  }
  
  public static void deleteAdobeUser(String configId, String email, boolean isDeleteAccount, String orgId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAdobeUser");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is blank/null");
      }
      
      /**
       * [
        {
          "user": "abc@upenn.edu",
          "do": [
            {
              "removeFromOrg": {
                "deleteAccount": true/false
              }
            }
          ]
        }
      ]
       */
    
      ObjectNode deleteAccountNode = GrouperUtil.jsonJacksonNode();
      deleteAccountNode.put("deleteAccount", isDeleteAccount);
      
      ObjectNode removeFromOrgNode = GrouperUtil.jsonJacksonNode();
      removeFromOrgNode.set("removeFromOrg", deleteAccountNode);
      
      ArrayNode doArray = GrouperUtil.jsonJacksonArrayNode();
      doArray.add(removeFromOrgNode);
      
      ObjectNode outerObjectNode = GrouperUtil.jsonJacksonNode();
      outerObjectNode.set("do", doArray);
      outerObjectNode.put("user", email);
      
      ArrayNode arrayNodeToSend = GrouperUtil.jsonJacksonArrayNode();
      arrayNodeToSend.add(outerObjectNode);
      
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNodeToSend);

      JsonNode jsonNode = executeMethod(debugMap, "deleteAdobeUser", "POST", configId, "/action/"+orgId,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }
  }
  
  public static void associateUsersToGroup(String configId, List<String> emails, String groupName, String orgId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "associateUsersToGroup");

    long startTime = System.nanoTime();

    try {
            
      /**
       * used to be single
       * [
          {
            "user": "abc@upenn.edu",
            "do": [
              {
                "add": {
                  "group": [
                    "HireIT ISC - CCE Pro - Acrobat Pro DC"
                  ]
                }
              }
            ]
          }
        ]
       */

      /**
       * now is multiples
       * [{
            "user": "jdoe@claimed-domain1.com",
            "requestID": "action_1",
            "do": [{
              "add": {
                "group": [ 
                  "group_name1"
                ]
              }
            }]
          }]
       */

      // batch up usernames with max batch size 10
      int batchSize = 10;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(emails, batchSize, false);
      
      boolean success = true;
      String exampleErrorMessage = null;
      
      for (int i=0;i<numberOfBatches;i++) {
        
        List<String> emailsBatch = GrouperUtil.batchList(emails, batchSize, i);
        
        ArrayNode arrayNodeToSend = GrouperUtil.jsonJacksonArrayNode();
        int indexInBatch = 0;
        for (String email: emailsBatch) {
          
          ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();
          groupsArray.add(groupName);
          
          ObjectNode addNode = GrouperUtil.jsonJacksonNode();
          addNode.set("group", groupsArray);
          
          ObjectNode doActionItemNode = GrouperUtil.jsonJacksonNode();
          doActionItemNode.set("add", addNode);
          
          ArrayNode doArray = GrouperUtil.jsonJacksonArrayNode();
          doArray.add(doActionItemNode);
          
          ObjectNode outerObjectNode = GrouperUtil.jsonJacksonNode();
          outerObjectNode.set("do", doArray);
          outerObjectNode.put("user", email);
          outerObjectNode.put("requestID", "action_" + indexInBatch);
          
          arrayNodeToSend.add(outerObjectNode);
          
          indexInBatch++;
        }
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNodeToSend);

        JsonNode jsonNode = executeMethod(debugMap, "associateUsersToGroup", "POST", configId, "/action/"+orgId,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, null);

        // {"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"error","errors":[{"index":0,"step":0,"message":"User is already a member of the specified group","errorCode":"error.usergroup.user.alreadyMember","user":"
        int notCompleted = GrouperUtil.jsonJacksonGetInteger(jsonNode, "notCompleted");
        // should not have any events not completed
        if (notCompleted != 0) {
          success = false;
          exampleErrorMessage = GrouperUtil.jsonJacksonToString(jsonNode);
        }
        // {"code": 40004, "message": "Operation failed", "message_detail": "User is already a member of the specified group", "stat": "FAIL"}
        
      }

      if (!success) {
        throw new RuntimeException("Did not add all users to group, see a sample error message: " + exampleErrorMessage);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }

  }
  
  public static void disassociateUsersFromGroup(String configId, List<String> emails, String groupName, String orgId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "disassociateUsersFromGroup");

    long startTime = System.nanoTime();

    try {
    
      if (GrouperUtil.length(emails) == 0) {
        throw new RuntimeException("emails are required");
      }
      
      if (StringUtils.isBlank(groupName)) {
        throw new RuntimeException("groupName is blank/null");
      }

      boolean success = true;
      String exampleErrorMessage = null;

      // batch up usernames with max batch size 10
      int batchSize = 10;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(emails, batchSize, false);
      for (int i=0;i<numberOfBatches;i++) {
        
        List<String> emailsBatch = GrouperUtil.batchList(emails, batchSize, i);

        
        /**
         * now is
         * [{
              "user": "jdoe@claimed-domain1.com",
              "requestID": "action_1",
              "do": [{
                "remove": {
                  "group": [ 
                    "group_name1"
                  ]
                }
              }]
            }]
         */
        
        ArrayNode arrayNodeToSend = GrouperUtil.jsonJacksonArrayNode();
        
        
        int indexInBatch = 0;
        
        for (String email: emailsBatch) {
          
          ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();
          groupsArray.add(groupName);
          
          ObjectNode removeNode = GrouperUtil.jsonJacksonNode();
          removeNode.set("group", groupsArray);
          
          ObjectNode doActionItemNode = GrouperUtil.jsonJacksonNode();
          doActionItemNode.set("remove", removeNode);
          
          ArrayNode doArray = GrouperUtil.jsonJacksonArrayNode();
          doArray.add(doActionItemNode);
          
          ObjectNode outerObjectNode = GrouperUtil.jsonJacksonNode();
          outerObjectNode.set("do", doArray);
          outerObjectNode.put("user", email);
          outerObjectNode.put("requestID", "action_" + indexInBatch);
          
          arrayNodeToSend.add(outerObjectNode);
          
          indexInBatch++;
        }
        
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(arrayNodeToSend);

        JsonNode jsonNode = executeMethod(debugMap, "disassociateUsersFromGroup", "POST", configId, "/action/"+orgId,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, null);
        
        // {"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"error","errors":[{"index":0,"step":0,"message":"User is already a member of the specified group","errorCode":"error.usergroup.user.alreadyMember","user":"
        int notCompleted = GrouperUtil.jsonJacksonGetInteger(jsonNode, "notCompleted");
        // should not have any events not completed
        if (notCompleted != 0) {
          success = false;
          exampleErrorMessage = GrouperUtil.jsonJacksonToString(jsonNode);
        }
        // {"code": 40004, "message": "Operation failed", "message_detail": "User is already a member of the specified group", "stat": "FAIL"}
        
      }

      if (!success) {
        throw new RuntimeException("Did not add all users to group, see a sample error message: " + exampleErrorMessage);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAdobeLog.adobeLog(debugMap, startTime);
    }
  }

}

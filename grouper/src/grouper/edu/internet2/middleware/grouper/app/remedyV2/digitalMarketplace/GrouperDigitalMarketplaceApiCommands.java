package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.exception.ExceptionUtils;

public class GrouperDigitalMarketplaceApiCommands {
  
  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @return remedy login id to user never null
   */
  public static Map<String, GrouperDigitalMarketplaceUser> retrieveDigitalMarketplaceUsers(String digitalMarketplaceExternalSystemConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDigitalMarketplaceUsers");

    long startTime = System.nanoTime();

    Map<String, GrouperDigitalMarketplaceUser> results = new LinkedHashMap<String, GrouperDigitalMarketplaceUser>();
    
    try {

      int pageSize = 2000;
      
      for (int i=0;i<6000;i++) {
      
        Map<String, GrouperDigitalMarketplaceUser> localResults = retrieveDigitalMarketplaceUsersHelper(digitalMarketplaceExternalSystemConfigId, pageSize, 0+results.size());
        
        if (localResults.size() == 0) {
          break;
        }
        
        results.putAll(localResults);
        
      }

      // jsonObject.getString("totalSize")
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
    
    
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param pageSize 
   * @param startIndex 
   * @return remedy login id to user never null
   */
  private static Map<String, GrouperDigitalMarketplaceUser> retrieveDigitalMarketplaceUsersHelper(
      String digitalMarketplaceExternalSystemConfigId,
      int pageSize, int startIndex) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveDigitalMarketplaceUsersHelper");

    long startTime = System.nanoTime();

    try {
    
      Map<String, String> paramMap = new HashMap<String, String>();
  
      paramMap.put("dataPageType", "com.bmc.arsys.rx.application.user.datapage.UserDataPageQuery");
      paramMap.put("pageSize", "" + pageSize);
      paramMap.put("startIndex", "" + startIndex);
      
      JsonNode jsonObject = executeGetMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/datapage", paramMap);
      
      Map<String, GrouperDigitalMarketplaceUser> results = convertMarketplaceUsersFromJson(jsonObject);
      debugMap.put("totalSize", GrouperUtil.jsonJacksonGetInteger(jsonObject, "totalSize")); 
      
      List<String> ids = new ArrayList<String>(results.keySet());

      if (ids.size() > 0) {
        debugMap.put("first", ids.get(0));
        debugMap.put("last", ids.get(ids.size()-1));
      }      
  
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }

  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param loginid
   * @return the user based on loginid
   */
  public static GrouperDigitalMarketplaceUser retrieveDigitalMarketplaceUser(String digitalMarketplaceExternalSystemConfigId, String loginid) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDigitalMarketplaceUser");
    debugMap.put("loginid", loginid);

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("dataPageType", "com.bmc.arsys.rx.application.user.datapage.UserDataPageQuery");
      paramMap.put("pageSize", "1");
      paramMap.put("startIndex", "0");
      paramMap.put("loginName", loginid);

      JsonNode jsonObject = executeGetMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/datapage", paramMap);

      Map<String, GrouperDigitalMarketplaceUser> results = convertMarketplaceUsersFromJson(jsonObject);

      debugMap.put("size", GrouperClientUtils.length(results));

      if (GrouperClientUtils.length(results) == 0) {
        
        return null;
        
      }
      if (GrouperClientUtils.length(results) == 1) {
        return results.values().iterator().next();
      }
      throw new RuntimeException("Found multiple results for loginid '" + loginid + "', results: " + GrouperClientUtils.length(results));
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
    
  }
  
  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param groupName
   * @return the group based on group name
   */
  public static GrouperDigitalMarketplaceGroup retrieveDigitalMarketplaceGroup(String digitalMarketplaceExternalSystemConfigId, String groupName) {
    
    Map<String, GrouperDigitalMarketplaceGroup> results = new TreeMap<String, GrouperDigitalMarketplaceGroup>();
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDigitalMarketplaceGroup");
    debugMap.put("groupName", groupName);

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("dataPageType", "com.bmc.arsys.rx.application.group.datapage.GroupDataPageQuery");
      paramMap.put("pageSize", "1");
      paramMap.put("startIndex", "0");
      paramMap.put("groupName", groupName);

      JsonNode jsonObject = executeGetMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/datapage", paramMap);

      JsonNode jsonObjectEntries = jsonObject.get("data");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JsonNode jsonObjectGroup = jsonObjectEntries.get(i);
        
        GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = new GrouperDigitalMarketplaceGroup();
  
        {
          String groupName1 = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "groupName");
          grouperDigitalMarketplaceGroup.setGroupName(groupName1);
        }
        
        {
          String comments = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "comments");
          grouperDigitalMarketplaceGroup.setComments(comments);
        }
        
        {
          String groupType = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "groupType");
          grouperDigitalMarketplaceGroup.setGroupType(groupType);
        }

        {
          String longGroupName = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "longGroupName");
          grouperDigitalMarketplaceGroup.setLongGroupName(longGroupName);
        }

        {
          // note: this might be blank: com.bmc.arsys.rx.services.group.domain.RegularGroup
          if (jsonObjectGroup.has("resourceType")) {
            String resourceType = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "resourceType");
            grouperDigitalMarketplaceGroup.setResourceType(resourceType);
          }
        }

        results.put(grouperDigitalMarketplaceGroup.getGroupName(), grouperDigitalMarketplaceGroup);
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      if (GrouperClientUtils.length(results) == 0) {
        
        return null;
        
      }
      if (GrouperClientUtils.length(results) == 1) {
        return results.values().iterator().next();
      }
      throw new RuntimeException("Found multiple results for groupName '" + groupName + "', results: " + GrouperClientUtils.length(results));
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
    
  }

  /**
   * execute a GET method
   * @param digitalMarketplaceExternalSystemConfigId
   * @param debugMap
   * @param path
   * @param paramMap
   * @return the json object
   */
  private static JsonNode executeGetMethod(String digitalMarketplaceExternalSystemConfigId, Map<String, Object> debugMap, String path, Map<String, String> paramMap) {
  
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
    String jwtToken = retrieveJwtToken(digitalMarketplaceExternalSystemConfigId, debugMap);
  
    String fullUrl = calculateUrl(digitalMarketplaceExternalSystemConfigId, path, paramMap);
    grouperHttpClient.assignUrl(fullUrl);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    
    grouperHttpClient.addHeader("authorization", "AR-JWT " + jwtToken);
    
    int responseCodeInt = -1;
    String body = null;
    long startTime = System.nanoTime();
    try {
      grouperHttpClient.executeRequest();
      responseCodeInt = grouperHttpClient.getResponseCode();
      
      try {
        body = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        debugMap.put("getResponseBody", ExceptionUtils.getStackTrace(e));
      }
      
    } catch (Exception e) {
      throw new RuntimeException("error in authn", e);
    } finally {
      debugMap.put("getMillis", ((System.nanoTime() - startTime) / 1000000) + "ms");
    }
    
    if (responseCodeInt != 200) {
      throw new RuntimeException("get didnt return 200, it returned: " + responseCodeInt + ", " + body);
    }
  
    // hmmm, no body
    if (GrouperClientUtils.isBlank(body)) {
      return null;
    }
    
    JsonNode rootNode = GrouperUtil.jsonJacksonNode(body);

    return rootNode;
  }

  /**
   * cache tokens
   */
  private static ExpirableCache<Boolean, String> retrieveJwtTokenCache = new ExpirableCache<Boolean, String>(5);

  /**
   * get the login token
   * @param digitalMarketplaceExternalSystemConfigId
   * @param debugMap
   * @return the login token
   */
  private static String retrieveJwtToken(String digitalMarketplaceExternalSystemConfigId, Map<String, Object> debugMap) {
    
    String jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);

    if (GrouperClientUtils.isBlank(jwtToken)) {
      
      synchronized (retrieveJwtTokenCache) {
        jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);
        if (GrouperClientUtils.isBlank(jwtToken)) {
          
          String username = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".username");
          
          String password = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".password");
      
          //login and get a token
          String loginUrl = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".tokenUrl");
        
          //login and get a token
          String url = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".url");
        
          url = GrouperClientUtils.stripEnd(url, "/");
          
//          String loginUrl = url + "/api/myit-sb/users/login";
          
          
          GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
          
          //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
          //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
          grouperHttpClient.assignUrl(loginUrl);
          grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
        
          //no keep alive so response is easier to indent for tests
          grouperHttpClient.addHeader("Connection", "close");
          grouperHttpClient.addHeader("Content-Type", "application/json");
          grouperHttpClient.addHeader("X-Requested-By", username);
          
          ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
          
          jsonObject.put("id", username);
          jsonObject.put("password", password);
          
          String postBody = jsonObject.toString();

          grouperHttpClient.assignBody(postBody);

          int responseCodeInt = -1;

          long startTime = System.nanoTime();
          try {
            grouperHttpClient.executeRequest();
            responseCodeInt = grouperHttpClient.getResponseCode();
            
            try {
              jwtToken = grouperHttpClient.getResponseBody();
            } catch (Exception e) {
              debugMap.put("authnGetResponseAsStringException", ExceptionUtils.getStackTrace(e));
            }
            
          } catch (Exception e) {
            throw new RuntimeException("error in authn", e);
          } finally {
            debugMap.put("authnMillis", ((System.nanoTime() - startTime) / 1000000) + "ms");
          }
          
          if (responseCodeInt != 200) {
            debugMap.put("authnResponseCodeInt", responseCodeInt);
            // note jwt token in this case is not valid and is an error message
            throw new RuntimeException("authn didnt return 200, it returned: " + responseCodeInt + ", " + jwtToken);
          }
          
          retrieveJwtTokenCache.put(Boolean.TRUE, jwtToken);
      
        }
      }
    }
    return jwtToken;
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param path
   * @param paramMap
   * @return the url
   */
  private static String calculateUrl(String digitalMarketplaceExternalSystemConfigId,  String path, Map<String, String> paramMap) {
    
    String url = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".url");
    
    url = GrouperClientUtils.stripEnd(url, "/");
    
    StringBuilder fullUrlBuilder = new StringBuilder(url).append(path);
  
    if (GrouperClientUtils.length(paramMap) > 0) {
      GrouperClientUtils.length(paramMap);
      int index = 0;
      
      for (String keyname : GrouperClientUtils.nonNull(paramMap).keySet()) {
        
        if (index == 0) {
          fullUrlBuilder.append("?");
        } else {
          fullUrlBuilder.append("&");
        }
        
        fullUrlBuilder.append(keyname).append("=").append(paramMap.get(keyname));
        
        index++;
      }
    }
    
    String fullUrl = fullUrlBuilder.toString();
    return fullUrl;
  }

  /**
   * @param jsonObject
   * @return the map from netId to user object
   */
  private static Map<String, GrouperDigitalMarketplaceUser> convertMarketplaceUsersFromJson(JsonNode jsonObject) {
    Map<String, GrouperDigitalMarketplaceUser> results = new TreeMap<String, GrouperDigitalMarketplaceUser>();
  
    //    { 
    //      "totalSize":44027,
    //      "data":[ 
    //         { 
    //            "fullName":"Hannah Admin",
    //            "loginName":"hannah_admin",
    //            "password":"**************************",
    //            "userId":"AGGADGJWHI4IZAPC012HPB3UI41XTN",
    //            "emailAddress":"",
    //            "groups":[ 
    //               "Administrator",
    //               "sbe-catalog-admins",
    //               "University of Pennsylvania",
    //               "General Access",
    //               "sbe-internal-suppliers"
    //            ],
    //            "computedGroups":[ 
    //               "CMDB SC Admin Group",
    //               "CMDB SC User Group",
    //               "sbe-public-computed",
    //               "sbe-internal-suppliers-computed",
    //               "sbe-catalog-admins-computed",
    //               "CMDB Data Change Group",
    //               "CMDB Data View Group",
    //               "CMDB Console User Group",
    //               "CMDB Console Admin Group",
    //               "CMDB RE User Group",
    //               "CMDB Definitions Viewer Group",
    //               "CMDB Definitions Admin Group",
    //               "AI Computed Group"
    //            ], ...
    //         }, 

    JsonNode jsonObjectEntries = jsonObject.get("data");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JsonNode jsonObjectUser = jsonObjectEntries.get(i);
      
      GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = new GrouperDigitalMarketplaceUser();

      // store this so we can post it back later...
      grouperDigitalMarketplaceUser.setJsonObject(jsonObjectUser);
      
      {
        String userId = GrouperUtil.jsonJacksonGetString(jsonObjectUser, "userId");
  
        // not sure why this would happen
        if (GrouperClientUtils.isBlank(userId)) {
          continue;
        }
        
        grouperDigitalMarketplaceUser.setUserId(userId);
      }
      
      {
        String loginName = GrouperUtil.jsonJacksonGetString(jsonObjectUser, "loginName");
        // not sure why this would happen
        if (GrouperClientUtils.isBlank(loginName)) {
          continue;
        }

        grouperDigitalMarketplaceUser.setLoginName(loginName);
        
        results.put(loginName, grouperDigitalMarketplaceUser);
      }
      
      {
        JsonNode groupsArray = jsonObjectUser.get("groups");
        for (int j=0; j < groupsArray.size(); j++) {
          JsonNode jsonNode = groupsArray.get(j);
          String groupExtension = jsonNode.asText();
          grouperDigitalMarketplaceUser.getGroups().add(groupExtension);
        }
      }
      
    }
    return results;
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @return the name of group extension mapped to group
   */
  public static Map<String, GrouperDigitalMarketplaceGroup> retrieveDigitalMarketplaceGroups(String digitalMarketplaceExternalSystemConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveDigitalMarketplaceGroups");
  
    long startTime = System.nanoTime();
  
    try {
  
      Map<String, GrouperDigitalMarketplaceGroup> results = new TreeMap<String, GrouperDigitalMarketplaceGroup>();
      
      Map<String, String> paramMap = new HashMap<String, String>();
      paramMap.put("dataPageType", "com.bmc.arsys.rx.application.group.datapage.GroupDataPageQuery");
      paramMap.put("pageSize", "-1");
      paramMap.put("startIndex", "0");
      
      //doesnt work since the url shouldnt be encoded
      
      JsonNode jsonObject = executeGetMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/datapage", paramMap);
      
      //  { 
      //    "totalSize":555,
      //    "data":[ 
      //       { 
      //          "groupName":"Administrator",
      //          "groupId":1,
      //          "longGroupName":"superuser to the AR System",
      //          "groupType":"Change",
      //          "parentGroup":null,
      //          "status":"Current",
      //          "comments":"Members have full and unlimited access to AR System, and thus can perform all operations\non all objects and data.",
      //          "createdBy":"AR System",
      //          "tags":null,
      //          "permittedGroupsBySecurityLabels":{ 
      //  
      //          },
      //          "permittedUsersBySecurityLabels":{ 
      //  
      //          },
      //          "floatingLicenses":0,
      //          "applicationFloatingLicenses":null,
      //          "groupCategory":"com.bmc.arsys.rx.services.group.domain.RegularGroup"
      //       }
      //    ]
      // } 
      
      JsonNode jsonObjectEntries = jsonObject.get("data");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JsonNode jsonObjectGroup = jsonObjectEntries.get(i);
        
        GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = new GrouperDigitalMarketplaceGroup();
  
        {
          String groupName = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "groupName");
          grouperDigitalMarketplaceGroup.setGroupName(groupName);
        }
        
        {
          String comments = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "comments");
          grouperDigitalMarketplaceGroup.setComments(comments);
        }
        
        {
          String groupType = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "groupType");
          grouperDigitalMarketplaceGroup.setGroupType(groupType);
        }

        {
          String longGroupName = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "longGroupName");
          grouperDigitalMarketplaceGroup.setLongGroupName(longGroupName);
        }

        {
          // note: this might be blank: com.bmc.arsys.rx.services.group.domain.RegularGroup
          if (jsonObjectGroup.has("resourceType")) {
            String resourceType = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "resourceType");
            grouperDigitalMarketplaceGroup.setResourceType(resourceType);
          }
        }

        results.put(grouperDigitalMarketplaceGroup.getGroupName(), grouperDigitalMarketplaceGroup);
      }
      
      
      debugMap.put("size", GrouperClientUtils.length(results));
  
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
  
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param grouperDigitalMarketplaceUser
   * @param grouperDigitalMarketplaceGroup
   * @param isIncremental
   * @return true if added, false if already exists
   */
  public static Boolean assignUserToDigitalMarketplaceGroup(String digitalMarketplaceExternalSystemConfigId, GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToDigitalMarketplaceGroup");
    debugMap.put("loginName", grouperDigitalMarketplaceUser.getLoginName());
    debugMap.put("groupName", grouperDigitalMarketplaceGroup.getGroupName());

    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser(
          digitalMarketplaceExternalSystemConfigId,
          grouperDigitalMarketplaceUser.getLoginName());
      
      // restart timer
      startTime = System.nanoTime();
      
      if (!GrouperClientUtils.nonNull(grouperDigitalMarketplaceUser.getGroups()).contains(grouperDigitalMarketplaceGroup.getGroupName())) {
        debugMap.put("foundExistingMembership", false);
        JsonNode jsonObject = grouperDigitalMarketplaceUser.getJsonObject();
        
//        {  
//          "groups":[
//             "sbe-myit-users",
//             "University of Pennsylvania",
//             "University of Pennsylvania - 91-Information Systems and Computing",
//             "University of Pennsylvania - 91-Information Systems and Computing - 9166-ISC-Tech Services-Network Operations"
//          ]
//       }
        
        ArrayNode groups = (ArrayNode)jsonObject.get("groups");

//        removeNonExistentGroups(groups, debugMap);

        ObjectNode userWithGroupsJson = GrouperUtil.jsonJacksonNode();
        
        ArrayNode groupsJson = GrouperUtil.jsonJacksonArrayNode();

        for (int i=0;i<groups.size();i++) {
          groupsJson.add(groups.get(i).asText());
        }
        groupsJson.add(grouperDigitalMarketplaceGroup.getGroupName());
        
        userWithGroupsJson.set("groups", groupsJson);
        
        String userJson = userWithGroupsJson.toString();
        
        // /api/rx/application/user/Allen
        executePutPostMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/user/" + grouperDigitalMarketplaceUser.getLoginName(), null, userJson, true);
        
        return true;
      } 
        
      debugMap.put("foundExistingMembership", true);
    
      return false;
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
  }

  /**
   * execute a GET method
   * @param digitalMarketplaceExternalSystemConfigId
   * @param debugMap
   * @param path
   * @param paramMap
   * @param requestBody 
   * @param isPutNotPost 
   * @return the json object
   */
  private static JsonNode executePutPostMethod(String digitalMarketplaceExternalSystemConfigId, Map<String, Object> debugMap, String path, Map<String, String> paramMap, String requestBody, boolean isPutNotPost) {
  
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
    String jwtToken = retrieveJwtToken(digitalMarketplaceExternalSystemConfigId, debugMap);
  
    String fullUrl = calculateUrl(digitalMarketplaceExternalSystemConfigId, path, paramMap);
    grouperHttpClient.assignUrl(fullUrl);
    if (isPutNotPost) {
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.put);
    } else {
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    }
    
    debugMap.put(isPutNotPost ? "put" : "post", true);
    //debugMap.put("requestBody", requestBody);
    grouperHttpClient.addHeader("authorization", "AR-JWT " + jwtToken);
    grouperHttpClient.addHeader("Content-Type", "application/json");
    
    String xRequestedBy = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".xRequestedByHeader");
    grouperHttpClient.addHeader("X-Requested-By", xRequestedBy);
    if (!GrouperClientUtils.isBlank(requestBody)) {
      grouperHttpClient.assignBody(requestBody);
    }
    
    int responseCodeInt = -1;
    String responseBody = null;
    long startTime = System.nanoTime();
    try {
      grouperHttpClient.executeRequest();
      responseCodeInt = grouperHttpClient.getResponseCode();
      
      try {
        responseBody = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        debugMap.put("getResponseBodyException", ExceptionUtils.getStackTrace(e));
      }
      
    } catch (Exception e) {
      throw new RuntimeException("error in authn", e);
    } finally {
      debugMap.put("getMillis", ((System.nanoTime() - startTime) / 1000000) + "ms");
    }
    
    debugMap.put("responseCodeInt", responseCodeInt);
    if (responseCodeInt != 200 && responseCodeInt != 201 && responseCodeInt != 204) {
      throw new RuntimeException("get didnt return 200, it returned: " + responseCodeInt + "," + responseBody);
    }
  
    // hmmm, no body
    if (GrouperClientUtils.isBlank(responseBody)) {
      return null;
    }
    
    JsonNode jsonObject = GrouperUtil.jsonJacksonNode( responseBody );     
  
    return jsonObject;
  }

//  /**
//   * 
//   * @param groups
//   * @param debugMap 
//   * @return the new node with removed groups
//   */
//  private static void removeNonExistentGroups(ArrayNode groups, Map<String, Object> debugMap) {
//
//    if (groups == null || groups.size() == 0) {
//      return;
//    }
//
//    //get all groupNames
//    Set<String> groupNames = new LinkedHashSet<String>();
//    for (int i=0;i<groups.size();i++) {
//      String groupName = groups.get(i).asText();
//      groupNames.add(groupName);
//    }
//
//    List<String> originalGroupNames = new ArrayList<String>(groupNames);
//
//    for (int i=0;i<originalGroupNames.size();i++) {
//      String groupName = originalGroupNames.get(i);
//      if (!GrouperDigitalMarketplaceGroup.retrieveGroups().containsKey(groupName)) {
//        groups.remove(i);
//        debugMap.put("removeGroup_" + groupName, true);
//      }
//    }
//  }
  
  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param grouperDigitalMarketplaceUser
   * @param grouperDigitalMarketplaceGroup
   * @return true if removed, false if not in there
   */
  public static Boolean removeUserFromDigitalMarketplaceGroup(String digitalMarketplaceExternalSystemConfigId, GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, 
      GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "removeUserFromDigitalMarketplaceGroup");
    debugMap.put("loginName", grouperDigitalMarketplaceUser.getLoginName());
    debugMap.put("groupName", grouperDigitalMarketplaceGroup.getGroupName());
  
    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser(digitalMarketplaceExternalSystemConfigId, grouperDigitalMarketplaceUser.getLoginName());
      
      // restart timer
      startTime = System.nanoTime();
      
      JsonNode jsonObject = grouperDigitalMarketplaceUser.getJsonObject();
      ArrayNode groups = (ArrayNode)jsonObject.get("groups");
      
//      removeNonExistentGroups(groups, debugMap);
      
      int groupIndex = -1;
      
      for (int i=0;i<groups.size();i++) {
        if (GrouperClientUtils.equals(grouperDigitalMarketplaceGroup.getGroupName(), groups.get(i).asText())) {
          groupIndex = i;
          break;
        }
      }
      
      if (groupIndex != -1) {
        
        groups.remove(groupIndex);
        
        ObjectNode userWithGroupsJson = GrouperUtil.jsonJacksonNode();
        
        ArrayNode groupsJson = GrouperUtil.jsonJacksonArrayNode();

        for (int i=0;i<groups.size();i++) {
          groupsJson.add(groups.get(i));
        }
        
        userWithGroupsJson.set("groups", groupsJson);
        
        String userJson = userWithGroupsJson.toString();
        
        debugMap.put("foundExistingMembership", true);
        
        // /api/rx/application/user/Allen
        executePutPostMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/user/" + grouperDigitalMarketplaceUser.getLoginName(), null, userJson, true);
        
        return true;
      } 
        
      debugMap.put("foundExistingMembership", false);
    
      return false;
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param groupName 
   * @param longGroupName 
   * @param comments 
   * @param groupType
   * @return true if added, false if already exists
   */
  public static Boolean createDigitalMarketplaceGroup(String digitalMarketplaceExternalSystemConfigId, String groupName, String longGroupName, String comments, String groupType) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "createDigitalMarketplaceGroup");
    debugMap.put("groupName", groupName);

    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroupExisting = retrieveDigitalMarketplaceGroups(digitalMarketplaceExternalSystemConfigId).get(groupName);
      
      // restart timer
      startTime = System.nanoTime();
      
      if (grouperDigitalMarketplaceGroupExisting == null) {
        debugMap.put("foundExistingGroup", false);
        
        //  {
        //    "resourceType": "com.bmc.arsys.rx.services.group.domain.RegularGroup",
        //    "groupName": "chris-hyzer-test",
        //    "longGroupName": "chris-hyzer-test",
        //    "groupType": "Change",
        //    "comments": "chris-hyzer-test comments",
        //    "status": "Current", "tags" : ["virtualmarketplace"]
        //  }          
        
        
        ObjectNode groupJsonObject = GrouperUtil.jsonJacksonNode();
        
        groupJsonObject.put("resourceType", "com.bmc.arsys.rx.services.group.domain.RegularGroup");
        groupJsonObject.put("groupName", groupName);
        if (GrouperClientUtils.isBlank(longGroupName)) {
          longGroupName = groupName;
        }
        groupJsonObject.put("longGroupName", longGroupName);
        groupType = StringUtils.defaultIfBlank(groupType, "Change");
        groupJsonObject.put("groupType", groupType);
        if (!GrouperClientUtils.isBlank(comments)) {
          groupJsonObject.put("comments", comments);
        }
        groupJsonObject.put("status", "Current");
        ArrayNode tagsArray = GrouperUtil.jsonJacksonArrayNode();
        tagsArray.add("virtualmarketplace");
        groupJsonObject.put("tags", tagsArray );
        
        String groupJson = groupJsonObject.toString();
        
        // /api/rx/application/user/Allen
        executePutPostMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/group/", null, groupJson, false);

        return true;
      } 
        
      debugMap.put("foundExistingGroup", true);
    
      return false;
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
  }

  /**
   * @param digitalMarketplaceExternalSystemConfigId
   * @param groupName 
   * @return true if added, false if already exists
   */
  public static Boolean deleteDigitalMarketplaceGroup(String digitalMarketplaceExternalSystemConfigId, String groupName) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteDigitalMarketplaceGroup");
    debugMap.put("groupName", groupName);
  
    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroupExisting = retrieveDigitalMarketplaceGroups(digitalMarketplaceExternalSystemConfigId).get(groupName);
      
      // restart timer
      startTime = System.nanoTime();
      
      if (grouperDigitalMarketplaceGroupExisting != null) {
        debugMap.put("foundExistingGroup", true);
        
        // /api/rx/application/user/Allen
        executeDeleteMethod(digitalMarketplaceExternalSystemConfigId, debugMap, "/api/rx/application/group/" + groupName, null);

        return true;
      } 
        
      debugMap.put("foundExistingGroup", true);
    
      return false;
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTime);
    }
  }

  /**
   * execute a DELETE method
   * @param digitalMarketplaceExternalSystemConfigId
   * @param debugMap
   * @param path
   * @param paramMap
   * @return the json object
   */
  private static JsonNode executeDeleteMethod(String digitalMarketplaceExternalSystemConfigId, Map<String, Object> debugMap, String path, Map<String, String> paramMap) {
  
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
  
    String jwtToken = retrieveJwtToken(digitalMarketplaceExternalSystemConfigId, debugMap);
  
    String fullUrl = calculateUrl(digitalMarketplaceExternalSystemConfigId, path, paramMap);
    
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
    grouperHttpClient.assignUrl(fullUrl);
    
    debugMap.put("delete", true);

    //debugMap.put("requestBody", requestBody);
    grouperHttpClient.addHeader("authorization", "AR-JWT " + jwtToken);
    grouperHttpClient.addHeader("Content-Type", "application/json");
    String xRequestedBy = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+digitalMarketplaceExternalSystemConfigId+".xRequestedByHeader");
    grouperHttpClient.addHeader("X-Requested-By", xRequestedBy);
    
    int responseCodeInt = -1;
    String responseBody = null;
    long startTime = System.nanoTime();
    try {
      grouperHttpClient.executeRequest();
      responseCodeInt = grouperHttpClient.getResponseCode();
      
      try {
        responseBody = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        debugMap.put("getResponseBodyException", ExceptionUtils.getStackTrace(e));
      }
      
    } catch (Exception e) {
      throw new RuntimeException("error in authn", e);
    } finally {
      debugMap.put("getMillis", ((System.nanoTime() - startTime) / 1000000) + "ms");
    }
    
    debugMap.put("responseCodeInt", responseCodeInt);
    if (responseCodeInt != 200 && responseCodeInt != 201 && responseCodeInt != 204) {
      throw new RuntimeException("get didnt return 204, it returned: " + responseCodeInt + "," + responseBody);
    }
  
    // hmmm, no body
    if (GrouperClientUtils.isBlank(responseBody)) {
      return null;
    }
    
    JsonNode jsonObject = GrouperUtil.jsonJacksonNode( responseBody );     
  
    return jsonObject;
  }

}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.azure.AzureMockServiceHandler;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;


/**
 *
 */
public class GrouperZoomCommands {

  public static void main(String[] args) {
    GrouperStartup.startup();
//    System.out.println(retrieveBearerTokenFromCacheOrFresh("pennZoomProd"));
//    System.out.println(retrieveBearerTokenFromCacheOrFresh("pennZoomProd"));
//    System.out.println(retrieveBearerTokenFromCacheOrFresh("pennZoomProd"));
    
    boolean first = true;
    final String configId = "pennZoomProd";
//    
//    Map<String, Map<String, Object>> accounts = retrieveAccounts(configId);
//    
//    for (Map<String, Object> account : GrouperUtil.nonNull(accounts).values()) {
//      String accountId = (String)account.get("id");
//      String accountName = (String)account.get("account_name");
//      System.out.println(accountId + ": " + accountName);
//      
//      if (first == true) {
//        Map<String, Map<String, Object>> users = retrieveSubaccountUsers(configId, accountId);
//        
//        System.out.println(accountName + ": " + GrouperUtil.length(users));
//        for (String email : GrouperUtil.nonNull(users).keySet()) {
//          System.out.println("    - " + email);
//        }
//      }
//      first = false;
//    }

    
//    test_printUsersPerSubaccount(configId);
    
//    Map<String, Map<String, Object>> groups = retrieveGroups(configId);
//    for (Map<String, Object> group: groups.values()) {
//      System.out.println("Group: " + group.get("id") + ", " + group.get("name") + ", " + group.get("total_members"));
//      if (first == true) {
//        List<Map<String, Object>> members = retrieveGroupMemberships(configId, (String)group.get("id"));
//        for (Map<String, Object> member: members) {
//          System.out.println("Member: " + member.get("id") + ", " + member.get("email") + ", " + member.get("first_name")
//              + ", " + member.get("last_name") + ", " + member.get("type") + ", " + member.get("primary_group"));
//        }
//      }
//      first = false;
//    }

//    Map<String, Map<String, Object>> roles = retrieveRoles(configId);
//    for (Map<String, Object> role: roles.values()) {
//      System.out.println("Role: " + role.get("id") + ", name: " + role.get("name") + ", description: " + role.get("description") + ", " + role.get("total_members"));
//    }

    
//    removeRoleMembership(configId, "2", "OvqgrwGqS1idoyTQnHg6bQ");
//    addRoleMembership(configId, "RFxtuFQxVRT65srojB-sgGA", "OvqgrwGqS1idoyTQnHg6bQ");
//    removeRoleMembership(configId, "RFxtuFQxVRT65srojB-sgGA", "OvqgrwGqS1idoyTQnHg6bQ");
//    addRoleMembership(configId, "2", "OvqgrwGqS1idoyTQnHg6bQ");
  
    GrouperZoomCommands.userChangeStatus(configId, "kwilso@upenn.edu", false);
    GrouperZoomCommands.userChangeStatus(configId, "kwilso@upenn.edu", true);
    
//    Map<String, Map<String, Object>> roles = retrieveRoles(configId);
//    for (Map<String, Object> role: roles.values()) {
//      System.out.println("Role: " + role.get("id") + ", name: " + role.get("name") + ", description: " + role.get("description") + ", " + role.get("total_members"));
//
//      if (first) {
//        List<Map<String, Object>> members = retrieveRoleMemberships(configId, (String)role.get("id"));
//        for (Map<String, Object> member: members) {
//          System.out.println("Member: " + member.get("id") + ", " + member.get("email") + ", " + member.get("first_name")
//              + ", " + member.get("last_name") + ", " + member.get("type") + ", " + member.get("department"));
//        }
//      }
//      first = false;
//    }

//    Map<String, Object> group = createGroup(configId, "Test6");
//    System.out.println("Group: " + group.get("id") + ", " + group.get("name") + ", " + group.get("total_members"));
//    addGroupMembership(configId, (String)group.get("id"), "ZelEDQlNRSWau5tOzYZQYA");
//    removeGroupMembership(configId, (String)group.get("id2"), "ZelEDQlNRSWau5tOzYZQYA");
//    deleteGroup(configId, (String)group.get("id"));


//  * @return map with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
//  * personal_meeting_url(string), timezone(string), verified(int e.g. 1), group_ids (array[string]), account_id(string), status(string e.g. active)

//    Map<String, Object> createUser = createUser(configId, "abc123@upenn.edu", "ABC", "123", 2);
//    deleteUser(configId, "abc123@upenn.edu");
    
//    
//    System.out.println("Member: " + createUser.get("id") + ", " + createUser.get("email") + ", " + createUser.get("first_name")
//      + ", " + createUser.get("last_name") + ", " + createUser.get("type"));
    
    
//    Map<String, Object> user = retrieveUser(configId, "mchyzer@upenn.edu");
//    System.out.println("Member: " + user.get("id") + ", " + user.get("email") + ", " + user.get("first_name")
//      + ", " + user.get("last_name") + ", " + user.get("type") + ", " + user.get("role_name") + ", " + user.get("personal_meeting_url")
//      + ", " + user.get("timezone") + ", " + user.get("verified") + ", " + GrouperUtil.toStringForLog(user.get("group_ids"))
//      + ", accountId: " + user.get("account_id") + ", status: " + user.get("status"));
//    

//      Map<String, Map<String, Object>> accounts = retrieveAccounts(configId);
//      for (Map<String, Object> account: accounts.values()) {
//        System.out.println(GrouperUtil.mapToString(account));
//      }

    System.out.println("Done");
    System.exit(0);
  }

  /**
   * @param configId
   */
  public static void test_printUsersPerSubaccount(final String configId) {
    Map<String, Map<String, Object>> userList = retrieveUsers(configId);
    
    Map<String, Set<String>> subaccountNameToEmails = new TreeMap<String, Set<String>>();
    
    Map<String, Map<String, Object>> accounts = retrieveAccounts(configId);
    
    Map<String, String> accountIdToName = new HashMap<String, String>();

    for (Map<String, Object> account : GrouperUtil.nonNull(accounts).values()) {
      String accountId = (String)account.get("id");
      String accountName = (String)account.get("account_name");
      accountIdToName.put(accountId, accountName);
    }
    
    for (Map<String, Object> user : GrouperUtil.nonNull(userList).values()) {
      
      String accountId = (String)user.get("account_id");
      
      String accountName = accountIdToName.get(accountId);
      
      if (accountName == null) {
        accountName = accountId;
      }
      
      if (accountName == null) {
        accountName = "null";
      }
      
      Set<String> emails = subaccountNameToEmails.get(accountName);
      
      if (emails == null) {
        emails = new TreeSet<String>();
        subaccountNameToEmails.put(accountName, emails);
      }
      
      emails.add((String)user.get("email"));
      
    }

    for (String accountName : subaccountNameToEmails.keySet()) {
      
      System.out.println(accountName + ": " + GrouperUtil.length(subaccountNameToEmails.get(accountName)));
      
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperZoomCommands.class);
  
  /**
   * 
   */
  public GrouperZoomCommands() {
  }

  /**
   * store an encrypted bearer token
   */
  private static ExpirableCache<Boolean, String> bearerTokenCache = null;
  
  /**
   * get a cached or fresh bearer token
   * @param configId 
   * @return the bearer token
   */
  public static String retrieveBearerTokenFromCacheOrFresh(String configId) {
    ExpirableCache<Boolean, String> theBearerTokenCache = bearerTokenCache();
    
    if (theBearerTokenCache != null) {
      String bearerToken = theBearerTokenCache.get(Boolean.TRUE);
      if (bearerToken != null) {
        return bearerToken;
      }
    }

    String jwt = retrieveBearerTokenFresh(configId);
    
    if (theBearerTokenCache != null) {
      theBearerTokenCache.put(Boolean.TRUE, jwt);
    }
    return jwt;

  }
  
  /**
   * get a cached or fresh bearer token
   * @param configId 
   * @return the bearer token
   */
  public static String retrieveBearerTokenFresh(String configId) {

    String zoomAuthenticationType = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".zoomAuthenticationType");

    if (StringUtils.equals(zoomAuthenticationType, "oauth2")) {
      
      String zoomOauth2AccountId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".zoomOauth2AccountId");
      String zoomOauth2ClientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".zoomOauth2ClientId");
      String zoomOauth2ClientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".zoomOauth2ClientSecret");

      // we need to get another one
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignDoNotLogHeaders(AzureMockServiceHandler.doNotLogHeaders);
      grouperHttpClient.assignDoNotLogResponseBody(true);
      
      String endpoint = endpointOauth2(configId);
      String url = endpoint + "oauth/token?grant_type=account_credentials&account_id=" + zoomOauth2AccountId;
    
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      
      grouperHttpClient.assignUser(zoomOauth2ClientId);
      grouperHttpClient.assignPassword(zoomOauth2ClientSecret);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();

        json = grouperHttpClient.getResponseBody();

      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      if (code != 200) {
        throw new RuntimeException("Expecting 200 but received: " + code + ", from: " + url + ", " + json );
      }
      
      json = grouperHttpClient.getResponseBody();
      
      //  {"access_token":"eyJzdiI6IjAwMDAwMSIsImFsZyI6Ik",
      //    "token_type":"bearer",
      //    "expires_in":3599,"scope":"user:write:admin user:read:admin role:master user:master"}
      JsonNode jsonJacksonNode = GrouperUtil.jsonJacksonNode(json);
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonJacksonNode, "access_token");
      
      return accessToken;

    }
    // lets get a new bearer token
    String apiSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".jwtApiSecretPassword");
    apiSecret = Morph.decryptIfFile(apiSecret);
    Algorithm algorithmHS = Algorithm.HMAC256(apiSecret);
    String apiKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".jwtApiKey");
    
    int cacheJwtForMinutes = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".cacheJwtForMinutes", 30);
    if (cacheJwtForMinutes == 1) {
      cacheJwtForMinutes = 2;
    }
    if (cacheJwtForMinutes <= 0) {
      cacheJwtForMinutes = 2;
    }
    
    int keyExpiresMillis = cacheJwtForMinutes*60*1000;
    String jwt = JWT.create().withIssuer(apiKey)
      .withExpiresAt(new Date(System.currentTimeMillis() + keyExpiresMillis))
      .sign(algorithmHS);
    return jwt;

  }
  
  /**
   * get the cache based on how long to cache.  Note this is used for oauth which expires in an hour... we can adjust this in future
   * @return the cache
   */
  public static ExpirableCache<Boolean, String> bearerTokenCache() {
    int cacheJwtForMinutes = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.zoom.cacheJwtForMinutes", 30);
    
    if (cacheJwtForMinutes == 1) {
      cacheJwtForMinutes = 2;
    }
    if (cacheJwtForMinutes <= 0) {
      return null;
    }
    // cache for the number of minutes minus 1 so it doesnt expire
    bearerTokenCache = new ExpirableCache<Boolean, String>(cacheJwtForMinutes-1);
    return bearerTokenCache;
  }
  
  private static String endpointOauth2(String configId) {
    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".zoomOauth2endpoint", "https://zoom.us");
    if (!endpoint.endsWith("/")) {
      endpoint+= "/";
    }
    return endpoint;
  }
  
  private static String endpoint(String configId) {
    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".endpoint", "https://api.zoom.us/v2");
    if (!endpoint.endsWith("/")) {
      endpoint+= "/";
    }
    return endpoint;
  }
  
  /**
   * 
   * @param configId
   * @param email 
   * @return map with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
   * personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), account_id(string), status(string e.g. active)
   * or null if not found
   */
  public static Map<String, Object> retrieveUser(String configId, String email) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveUser");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is required!");
      }
      if (email.contains("/")) {
        throw new RuntimeException("Invalid email: " + email);
      }
      String url = endpoint + "users/" + email;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      debugMap.put("httpCode", code);

      if (code == 404) {
        return null;
      }

      if (code != 200) {
        throw new RuntimeException("Cant get user from '" + url + "' " + json);
      }
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);

      Map<String, Object> result = retrieveUserFromJsonObject(jsonObject);
      
      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  private static GrouperHttpClient grouperHttpClient(String configId) {
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".proxyUrl");
    String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".proxyType");
    
    grouperHttpClient.assignProxyUrl(proxyUrl);
    grouperHttpClient.assignProxyType(proxyType);

    return grouperHttpClient;
  }

  /**
   * @param jsonObject
   * @return map
   */
  public static Map<String, Object> retrieveUserFromJsonObject(JsonNode jsonObject) {
    
    //  {
    //    "id": "z8dsdsdsdsdCfp8uQ",
    //    "first_name": "Harry",
    //    "last_name": "Grande",
    //    "email": "harryg@dfkjdslfjkdsfjkdsf.fsdfdfd",
    //    "type": 2,
    //    "role_name": "Owner",
    //    "pmi": 000000000,
    //    "use_pmi": false,
    //    "personal_meeting_url": "https://zoom.us/j/6352635623323434343443",
    //    "timezone": "America/Los_Angeles",
    //    "verified": 1,
    //    "dept": "",
    //    "created_at": "2018-11-15T01:10:08Z",
    //    "last_login_time": "2019-09-13T21:08:52Z",
    //    "last_client_version": "4.4.55383.0716(android)",
    //    "pic_url": "https://lh4.googleusercontent.com/-hsgfhdgsfghdsfghfd-photo.jpg",
    //    "host_key": "0000",
    //    "jid": "hghghfghdfghdfhgh@xmpp.zoom.us",
    //    "group_ids": [],
    //    "im_group_ids": [
    //        "CcSAAAAAAABBBVoQ"
    //    ],
    //    "account_id": "EAAAAAbbbbbCCCCHMA",
    //    "language": "en-US",
    //    "phone_country": "USA",
    //    "phone_number": "00000000",
    //    "status": "active"
    //}      
    Map<String, Object> result = new HashMap<String, Object>();
    
    if (jsonObject.has("id")) {
      result.put("id", GrouperUtil.jsonJacksonGetString(jsonObject, "id"));
    }
    if (jsonObject.has("first_name")) {
      result.put("first_name", GrouperUtil.jsonJacksonGetString(jsonObject, "first_name"));
    }
    if (jsonObject.has("last_name")) {
      result.put("last_name", GrouperUtil.jsonJacksonGetString(jsonObject, "last_name"));
    }
    if (jsonObject.has("email")) {
      result.put("email", GrouperUtil.jsonJacksonGetString(jsonObject, "email"));
    }
    if (jsonObject.has("type")) {
      result.put("type", GrouperUtil.jsonJacksonGetInteger(jsonObject, "type"));
    }
    if (jsonObject.has("role_name")) {
      result.put("role_name", GrouperUtil.jsonJacksonGetString(jsonObject, "role_name"));
    }
    if (jsonObject.has("personal_meeting_url")) {
      result.put("personal_meeting_url", GrouperUtil.jsonJacksonGetString(jsonObject, "personal_meeting_url"));
    }
    if (jsonObject.has("timezone")) {
      result.put("timezone", GrouperUtil.jsonJacksonGetString(jsonObject, "timezone"));
    }
    if (jsonObject.has("verified")) {
      result.put("verified", GrouperUtil.jsonJacksonGetInteger(jsonObject, "verified"));
    }
    if (jsonObject.has("created_at")) {
      result.put("created_at", GrouperUtil.jsonJacksonGetString(jsonObject, "created_at"));
    }
    if (jsonObject.has("last_login_time")) {
      result.put("last_login_time", GrouperUtil.jsonJacksonGetString(jsonObject, "last_login_time"));
    }
    ArrayNode groupIdsJsonArray = jsonObject.has("group_ids") ? (ArrayNode)jsonObject.get("group_ids") : null;
    String[] groupIdsArray = new String[groupIdsJsonArray == null ? 0 : groupIdsJsonArray.size()];
    for (int i=0;i<(groupIdsJsonArray == null ? 0 : groupIdsJsonArray.size());i++) {
      groupIdsArray[i] = groupIdsJsonArray.get(i).asText();
    }
    result.put("group_ids", groupIdsArray);
    if (jsonObject.has("account_id")) {
      result.put("account_id", GrouperUtil.jsonJacksonGetString(jsonObject, "account_id"));
    }
    if (jsonObject.has("status")) {
      result.put("status", GrouperUtil.jsonJacksonGetString(jsonObject, "status"));
    }
    return result;
  }

  /**
   * 
   * @param configId
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
   * personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), account_id(string), status(string e.g. active)
   * or null if not found
   */
  public static Map<String, Map<String, Object>> retrieveUsers(String configId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveUsers");
    try {
      debugMap.put("configId", configId);
      
      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
      Map<String, Map<String, Object>> resultInactive = new HashMap<String, Map<String, Object>>();
      Map<String, Map<String, Object>> resultPending = new HashMap<String, Map<String, Object>>();

      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeUsers", 300);

      // Get all users with the default active status
      for (int i=0;i<10000;i++) {
        Map<String, Map<String, Object>> tempResult = retrieveUsersHelper(configId, i+1, "users?");
        
        result.putAll(tempResult);

        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }

      // Get all users with status = inactive
      for (int i=0;i<10000;i++) {
        Map<String, Map<String, Object>> tempResult = retrieveUsersHelper(configId, i+1, "users?status=inactive&");
        
        resultInactive.putAll(tempResult);

        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }

      // Get all users with status = pending
      for (int i=0;i<10000;i++) {
        Map<String, Map<String, Object>> tempResult = retrieveUsersHelper(configId, i+1, "users?status=pending&");
        
        resultPending.putAll(tempResult);

        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }
      
      result.putAll(resultInactive);
      result.putAll(resultPending);

      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param endpointString
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string)
   * or null if not found
   */
  public static Map<String, Map<String, Object>> retrievePhoneUsers(String configId, String endpointString) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrievePhoneUsers");
    try {
      debugMap.put("configId", configId);
      
      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();

      result = retrievePhoneUsersHelper(configId, endpointString);
      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * 
   * @param configId
   * @return map from group name to map with id(string), name(string), and total_members(int)
   */
  public static Map<String, Map<String, Object>> retrieveRoles(String configId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveRoles");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "roles";
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get roles from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "total_records": 3,
      //    "roles": [
      //      {
      //        "id": "0",
      //        "name": "Owner",
      //        "description": "Account owner has full privileges to access and manage a Zoom account.",
      //        "total_members": 1
      //      },
      //      {
      //        "id": "1",
      //        "name": "Admin",
      //        "description": "Admins have wide range privileges to access and manage a Zoom account.",
      //        "total_members": 0
      //      },
      //      {
      //        "id": "2",
      //        "name": "Member",
      //        "description": "Members have access to basic Zoom video meeting functions but no account management privileges.",
      //        "total_members": 1
      //      }
      //    ]
      //  }
      
      Map<String, Map<String, Object>> result = new TreeMap<String, Map<String, Object>>();
      
      ArrayNode jsonArray = jsonObject.has("roles") ? (ArrayNode)jsonObject.get("roles") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectGroup = (JsonNode)jsonArray.get(i);
          Map<String, Object> groupMap = new HashMap<String, Object>();
          groupMap.put("id", GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "id"));
          final String name = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "name");
          groupMap.put("name", name);
          final String description = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "description");
          groupMap.put("description", description);
          groupMap.put("total_members", GrouperUtil.jsonJacksonGetInteger(jsonObjectGroup, "total_members"));
          result.put(name, groupMap);
        }
      }
      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  
  /**
   * 
   * @param configId
   * @param groupId 
   * @return list of maps with id(string), first_name(string), last_name(string), email(string), type(int), primary_group(boolean)
   */
  public static List<Map<String, Object>> retrieveGroupMemberships(String configId, String groupId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGroupMemberships");
    try {
      debugMap.put("configId", configId);
      
      Set<String> idsSeen = new HashSet<String>();
      
      List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeMemberships", 300);

      for (int i=0;i<10000;i++) {
        List<Map<String, Object>> tempResult = retrieveGroupMembershipsHelper(configId, groupId, i+1);
        
        for (Map<String, Object> member : tempResult) {
          
          String id = (String)member.get("id");
          
          if (idsSeen.contains(id)) {
            continue;
          }
          
          result.add(member);
          idsSeen.add(id);
        }
        
        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }
      
      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }

  /**
   * 
   * @param configId
   * @param groupId 
   * @param pageNumberOneIndexed 
   * @return list of maps with id(string), first_name(string), last_name(string), email(string), type(int), primary_group(boolean)
   */
  private static List<Map<String, Object>> retrieveGroupMembershipsHelper(String configId, String groupId, int pageNumberOneIndexed) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGroupMembershipsHelper");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      if (groupId.contains("/")) {
        throw new RuntimeException("Invalid group: " + groupId);
      }
      //    zoom.myConfigId.pageSizeMemberships = 300
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeMemberships", 300);
      debugMap.put("pageSize", pageSize);
      //page_size, page_number

      String url = endpoint + "groups/" + groupId + "/members?page_size=" + pageSize + "&page_number=" + pageNumberOneIndexed;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }


      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get group memberships from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "members": [
      //      {
      //        "email": "",
      //        "first_name": "Ram",
      //        "id": "3542342",
      //        "last_name": "Ghale",
      //        "type": 1,
      //        "primary_group":true
      //      }
      //    ],
      //    "page_count": 1,
      //    "page_number": 1,
      //    "page_size": 1,
      //    "total_records": 1
      //  }
      
      List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      
      int pageNumberReturned = GrouperUtil.jsonJacksonGetInteger(jsonObject, "page_number");
      int totalRecords = GrouperUtil.jsonJacksonGetInteger(jsonObject, "total_records");
      debugMap.put("totalRecords", totalRecords);
      
      // if we are over the number of pages, then dont return any members to notify caller we are done paging
      if (pageNumberOneIndexed != pageNumberReturned) {
        debugMap.put("count", 0);

        return result;
      }
      
      ArrayNode jsonArray = jsonObject.has("members") ? (ArrayNode)jsonObject.get("members") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectMember = (JsonNode)jsonArray.get(i);
          Map<String, Object> memberMap = new HashMap<String, Object>();
          memberMap.put("id", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "id"));
          memberMap.put("email", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "email"));
          memberMap.put("first_name", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "first_name"));
          memberMap.put("last_name", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "last_name"));
          memberMap.put("type", GrouperUtil.jsonJacksonGetInteger(jsonObjectMember, "type"));
          memberMap.put("primary_group", GrouperUtil.jsonJacksonGetBoolean(jsonObjectMember, "primary_group"));
          result.add(memberMap);
        }
      }
      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param name is group name which is generally just the extension
   * @return map with id(string), name(string), and total_members(int)
   */
  public static Map<String, Object> createGroup(String configId, String name) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "createGroup");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "groups";
      debugMap.put("url", url);

      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "name": "myawesomegroup"
      //  }
    
      JsonNode jsonNode = null;
      {
        ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
        GrouperUtil.jsonJacksonAssignString(jsonObject, "name", name);
        String jsonRequest = GrouperUtil.jsonJacksonToString(jsonObject);
        grouperHttpClient.assignBody(jsonRequest);        
  
        int code = -1;
        String json = null;
    
        try {
          grouperHttpClient.executeRequest();
          code = grouperHttpClient.getResponseCode();
          // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
          
          json = grouperHttpClient.getResponseBody();
  
        
        } catch (Exception e) {
          throw new RuntimeException("Error connecting to '" + url + "'", e);
        }
    
        debugMap.put("httpCode", code);
        
        if (code != 201) {
          throw new RuntimeException("Cant create group '" + url + "', '" + name + "' " + json);
        }
        
        jsonNode = GrouperUtil.jsonJacksonNode(json);
      }      
      //  {
      //    "id": "string",
      //    "name": "string",
      //    "total_members": "integer"
      //  }

      Map<String, Object> result = new HashMap<String, Object>();
      
      result.put("id", GrouperUtil.jsonJacksonGetString(jsonNode, "id"));
      result.put("name", GrouperUtil.jsonJacksonGetString(jsonNode, "name"));
      result.put("total_members", GrouperUtil.jsonJacksonGetInteger(jsonNode, "total_members"));
      debugMap.put("id", result.size());

      return result;
  
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param groupId 
   * @param memberId 
   */
  public static void addGroupMembership(String configId, String groupId, String memberId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "addGroupMembership");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "groups";
      if (groupId.contains("/")) {
        throw new RuntimeException("Invalid group: " + groupId);
      }
      url += "/" + groupId + "/members";
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "members": [
      //      {
      //        "id": "36565387",
      //        "email": "memberemail@somecompany.com"
      //      }
      //    ]
      //  }  
      
      ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
      ArrayNode jsonArray = GrouperUtil.jsonJacksonArrayNode();
      ObjectNode member = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(member, "id", memberId);
      jsonArray.add(member);

      objectNode.set("members", jsonArray);
      String jsonRequest = GrouperUtil.jsonJacksonToString(objectNode);
      
      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      if (code != 201) {
        throw new RuntimeException("Cant add member '" + url + "', '" + memberId + "' " + json);
      }
        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param groupId is group id
   */
  public static void deleteGroup(String configId, String groupId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "deleteGroup");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required!");
      }
      if (groupId.contains("/")) {
        throw new RuntimeException("Invalid groupId: " + groupId);
      }

      String url = endpoint + "groups/" + groupId;
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      if (code != 204) {
        throw new RuntimeException("Cant delete group '" + url +"'");
      }
        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param groupId 
   * @param memberId 
   */
  public static void removeGroupMembership(String configId, String groupId, String memberId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "removeGroupMembership");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "groups";
      if (groupId.contains("/")) {
        throw new RuntimeException("Invalid group: " + groupId);
      }
      url += "/" + groupId + "/members";
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("memberId is required!");
      }
      if (memberId.contains("/")) {
        throw new RuntimeException("Invalid memberId: " + memberId);
      }
      url += "/" + memberId;
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      // 400 if not in group, 204 if removed
      if (code != 204 && code != 400) {
        throw new RuntimeException("Cant remove member '" + url + "', '" + memberId + "' " + json);
      }
        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param email 
   * @param firstName 
   * @param lastName 
   * @param type 1 basic, 2 licensed, 3, on-prem
   * @return map with id(string), name(string), and total_members(int)
   */
  public static Map<String, Object> createUser(String configId, String email, String firstName, String lastName, int type) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "createUser");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "users";
      debugMap.put("url", url);

      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
            
      //  {
      //    "name": "myawesomegroup"
      //  }
  
      ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
      jsonObject.put("action", "create");
      ObjectNode userInfoJsonObject = GrouperUtil.jsonJacksonNode();
      userInfoJsonObject.put("email", email);
      userInfoJsonObject.put("type", type);
      userInfoJsonObject.put("first_name", firstName);
      userInfoJsonObject.put("last_name", lastName);
      jsonObject.set("user_info", userInfoJsonObject);
      
      String jsonRequest = GrouperUtil.jsonJacksonToString(jsonObject);
      
      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      if (code != 201) {
        throw new RuntimeException("Cant create user '" + url + "', '" + email + "' " + json);
      }
      
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "id": "string",
      //    "first_name": "string",
      //    "last_name": "string",
      //    "email": "string",
      //    "type": "integer"
      //  }
  
      Map<String, Object> result = new HashMap<String, Object>();
      
      result.put("id", GrouperUtil.jsonJacksonGetString(jsonNode, "id"));
      result.put("first_name", GrouperUtil.jsonJacksonGetString(jsonNode, "first_name"));
      result.put("last_name", GrouperUtil.jsonJacksonGetString(jsonNode, "last_name"));
      result.put("email", GrouperUtil.jsonJacksonGetString(jsonNode, "email"));
      result.put("type", GrouperUtil.jsonJacksonGetInteger(jsonNode, "type"));
      debugMap.put("size", result.size());
  
      return result;
  
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param roleId 
   * @return list of maps with id(string), first_name(string), last_name(string), email(string), type(int), department(string)
   */
  public static List<Map<String, Object>> retrieveRoleMemberships(String configId, String roleId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveRoleMemberships");
    try {
      debugMap.put("configId", configId);
      
      Set<String> idsSeen = new HashSet<String>();
      
      List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeRoleMemberships", 300);
  
      for (int i=0;i<10000;i++) {
        List<Map<String, Object>> tempResult = retrieveRoleMembershipsHelper(configId, roleId, i+1);
        
        for (Map<String, Object> member : tempResult) {
          
          String id = (String)member.get("id");
          
          if (idsSeen.contains(id)) {
            continue;
          }
          
          result.add(member);
          idsSeen.add(id);
        }
        
        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }
      
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  
  }

  /**
   * 
   * @param configId
   * @param roleId 
   * @param pageNumberOneIndexed 
   * @return list of maps with id(string), first_name(string), last_name(string), email(string), type(int), department(string)
   */
  private static List<Map<String, Object>> retrieveRoleMembershipsHelper(String configId, String roleId, int pageNumberOneIndexed) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveRoleMembershipsHelper");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      if (roleId.contains("/")) {
        throw new RuntimeException("Invalid role: " + roleId);
      }
      //    zoom.myConfigId.pageSizeMemberships = 300
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeRoleMemberships", 300);
      debugMap.put("pageSize", pageSize);
      //page_size, page_number
  
      String url = endpoint + "roles/" + roleId + "/members?page_size=" + pageSize + "&page_number=" + pageNumberOneIndexed;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
  
      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get roles from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "members": [
      //      {
      //        "email": "",
      //        "first_name": "Ram",
      //        "id": "3542342",
      //        "last_name": "Ghale",
      //        "type": 1,
      //        "department":"math"
      //      }
      //    ],
      //    "page_count": 1,
      //    "page_number": 1,
      //    "page_size": 1,
      //    "total_records": 1
      //  }
      
      List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      
      int pageNumberReturned = GrouperUtil.jsonJacksonGetInteger(jsonObject, "page_number");
      int totalRecords = GrouperUtil.jsonJacksonGetInteger(jsonObject, "total_records");
      debugMap.put("totalRecords", totalRecords);
      
      // if we are over the number of pages, then dont return any members to notify caller we are done paging
      if (pageNumberOneIndexed != pageNumberReturned) {
        debugMap.put("count", 0);
  
        return result;
      }
      
      ArrayNode jsonArray = jsonObject.has("members") ? (ArrayNode)jsonObject.get("members") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectMember = (JsonNode)jsonArray.get(i);
          Map<String, Object> memberMap = new HashMap<String, Object>();
          memberMap.put("id", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "id"));
          memberMap.put("email", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "email"));
          if (jsonObjectMember.has("first_name")) {
            memberMap.put("first_name", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "first_name"));
          }
          if (jsonObjectMember.has("last_name")) {
            memberMap.put("last_name", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "last_name"));
          }
          if (jsonObjectMember.has("type")) {
            memberMap.put("type", GrouperUtil.jsonJacksonGetInteger(jsonObjectMember, "type"));
          }
          if (jsonObjectMember.has("department")) {
            memberMap.put("department", GrouperUtil.jsonJacksonGetString(jsonObjectMember, "department"));
          }
          result.add(memberMap);
        }
      }
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param pageNumberOneIndexed 
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
   * personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), account_id(string), status(string e.g. active)
   * or null if not found
   */
  private static Map<String, Map<String, Object>> retrieveUsersHelper(String configId, int pageNumberOneIndexed, String endpointString) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveUsersHelper");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);

      //    zoom.myConfigId.pageSizeMemberships = 300
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeUsers", 300);
      debugMap.put("pageSize", pageSize);
      //page_size, page_number
  
      String url = endpoint + endpointString + "page_size=" + pageSize + "&page_number=" + pageNumberOneIndexed;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
  
      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get users from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "users": [
      //      {
      //        "id": "z8yAAAAA8bbbQ",
      //        "first_name": "Melina",
      //        "last_name": "Ghimire",
      //        "email": "mel@jfggdhfhdfj.djfhdsfh",
      //        "type": 2,
      //        "pmi": 581111112,
      //        "timezone": "America/Los_Angeles",
      //        "verified": 1,
      //        "dept": "",
      //        "created_at": "2018-11-15T01:10:08Z",
      //        "last_login_time": "2019-09-13T21:08:52Z",
      //        "last_client_version": "4.4.55383.0716(android)",
      //        "pic_url": "https://lh4.googleusercontent.com/-someurl/photo.jpg",
      //        "im_group_ids": [
      //          "Abdsjkfhdhfj"
      //        ],
      //        "status": "active"
      //      }
      //    ],
      //    "page_count": 1,
      //    "page_number": 1,
      //    "page_size": 1,
      //    "total_records": 1
      //  }
      
      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
      
      int pageNumberReturned = GrouperUtil.jsonJacksonGetInteger(jsonObject, "page_number");
      int totalRecords = GrouperUtil.jsonJacksonGetInteger(jsonObject, "total_records");
      debugMap.put("totalRecords", totalRecords);
      
      // if we are over the number of pages, then dont return any members to notify caller we are done paging
      if (pageNumberOneIndexed != pageNumberReturned) {
        debugMap.put("count", 0);
  
        return result;
      }
      
      ArrayNode jsonArray = jsonObject.has("users") ? (ArrayNode)jsonObject.get("users") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectUser = (JsonNode)jsonArray.get(i);
          Map<String, Object> userMap = retrieveUserFromJsonObject(jsonObjectUser);
          String email = (String)userMap.get("email");
          if (!StringUtils.isBlank(email)) {
            result.put(email, userMap);
          }
        }
      }
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }


  /**
   * 
   * @param configId
   * @param endpointString
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string)
   * or null if not found
   */
  private static Map<String, Map<String, Object>> retrievePhoneUsersHelper(String configId, String endpointString) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    String nextPageToken = "";
    
    debugMap.put("method", "retrievePhoneUsersHelper");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);

      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeUsers", 300);
      debugMap.put("pageSize", pageSize);

      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();

      // as long as we get a value for nextPageToken, keep looping through pages
      do {
        String url = endpoint + endpointString + "?page_size=" + pageSize + "&next_page_token=" + nextPageToken;
        debugMap.put("url", url);

        GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
        grouperHttpClient.assignUrl(url);
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
        
        grouperHttpClient.addHeader("Content-Type", "application/json");
        grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
        
        int code = -1;
        String json = null;
    
        try {
          grouperHttpClient.executeRequest();
          code = grouperHttpClient.getResponseCode();
          json = grouperHttpClient.getResponseBody();
        } catch (Exception e) {
          throw new RuntimeException("Error connecting to '" + url + "'", e);
        }
        debugMap.put("httpCode", code);
        
        if (code != 200) {
          throw new RuntimeException("Cant get phone users from '" + url + "' " + json);
        }
        
        JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
        Map<String, Map<String, Object>> tempResult = new HashMap<String, Map<String, Object>>();
        
        int totalRecords = GrouperUtil.jsonJacksonGetInteger(jsonObject, "total_records");
        nextPageToken = GrouperUtil.jsonJacksonGetString(jsonObject, "next_page_token");
        debugMap.put("totalRecords", totalRecords);
        
        ArrayNode jsonArray = jsonObject.has("users") ? (ArrayNode)jsonObject.get("users") : null;
        if (jsonArray != null && jsonArray.size() >= 1) {
          for (int i=0;i<jsonArray.size();i++) {
            JsonNode jsonObjectUser = (JsonNode)jsonArray.get(i);
            Map<String, Object> userMap = retrieveUserFromJsonObject(jsonObjectUser);
            String email = (String)userMap.get("email");
            if (!StringUtils.isBlank(email)) {
              tempResult.put(email, userMap);
            }
          }
        }
        debugMap.put("count", tempResult.size());
        
        result.putAll(tempResult);

      } while (nextPageToken != "");

      return result;
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * 
   * @param configId
   * @return map from group name to map with id(string), name(string), and total_members(int)
   */
  public static Map<String, Map<String, Object>> retrieveGroups(String configId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGroups");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "groups";
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get groups from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //    {
      //      "total_records":34,
      //      "groups":[
      //         {
      //            "id":"O__bs3GDQkmbwUgnd41MCA",
      //            "name":"Annenberg Center",
      //            "total_members":1
      //         
      //         }
      //      ]
      //   }
      
      Map<String, Map<String, Object>> result = new TreeMap<String, Map<String, Object>>();
      
      ArrayNode jsonArray = jsonObject.has("groups") ? (ArrayNode)jsonObject.get("groups") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectGroup = (JsonNode)jsonArray.get(i);
          Map<String, Object> groupMap = new HashMap<String, Object>();
          groupMap.put("id", GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "id"));
          final String name = GrouperUtil.jsonJacksonGetString(jsonObjectGroup, "name");
          groupMap.put("name", name);
          groupMap.put("total_members", GrouperUtil.jsonJacksonGetInteger(jsonObjectGroup, "total_members"));
          result.put(name, groupMap);
        }
      }
      debugMap.put("count", result.size());

      return result;

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param userIdOrEmail is user id or email
   */
  public static void deleteUser(String configId, String userIdOrEmail) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "deleteUser");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      if (StringUtils.isBlank(userIdOrEmail)) {
        throw new RuntimeException("userIdOrEmail is required!");
      }
      if (userIdOrEmail.contains("/")) {
        throw new RuntimeException("Invalid userId: " + userIdOrEmail);
      }

      String url = endpoint + "users/" + userIdOrEmail + "?action=delete";
      debugMap.put("url", url);

      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;

      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUserDeletesInsteadOfDeleting", false)) {
        
        debugMap.put("logUserDeletesInsteadOfDeleting", true);

      } else {
        try {
          grouperHttpClient.executeRequest();
          code = grouperHttpClient.getResponseCode();
          // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
          
        } catch (Exception e) {
          throw new RuntimeException("Error connecting to '" + url + "'", e);
        }
    
        debugMap.put("httpCode", code);
        
        if (code != 204) {
          throw new RuntimeException("Cant delete user '" + url +"'");
        }
      }        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param roleId 
   * @param memberId 
   */
  public static void addRoleMembership(String configId, String roleId, String memberId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "addRoleMembership");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "roles";
      if (roleId.contains("/")) {
        throw new RuntimeException("Invalid role: " + roleId);
      }
      url += "/" + roleId + "/members";
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
            
      //  {
      //    "members": [
      //      {
      //        "id": "36565387"
      //      }
      //    ]
      //  }  
      
      ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
      ArrayNode jsonArray = GrouperUtil.jsonJacksonArrayNode();
      ObjectNode member = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(member, "id", memberId);
      jsonArray.add(member);

      objectNode.set("members", jsonArray);
      String jsonRequest = GrouperUtil.jsonJacksonToString(objectNode);

      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      // sometimes this returns a 400...  maybe if the user already has the role?
      if (code != 201) {
        throw new RuntimeException("Cant add member '" + url + "', '" + memberId + "' " + json + ", " + code);
      }
        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param groupId 
   * @param memberId 
   */
  public static void removeRoleMembership(String configId, String roleId, String memberId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "removeRoleMembership");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "roles";
      if (roleId.contains("/")) {
        throw new RuntimeException("Invalid role: " + roleId);
      }
      url += "/" + roleId + "/members";
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("memberId is required!");
      }
      if (memberId.contains("/")) {
        throw new RuntimeException("Invalid memberId: " + memberId);
      }
      url += "/" + memberId;
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      // 400 if not in group, 204 if removed
      if (code != 204 && code != 400) {
        throw new RuntimeException("Cant remove member '" + url + "', '" + memberId + "' " + json);
      }
        
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * @param configId
   * @param email
   * @param activate true to activate, false to deactivate
   */
  public static void userChangeStatus(String configId, String email, boolean activate) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "userChangeStatus");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "users";
      if (email.contains("/")) {
        throw new RuntimeException("Invalid email: " + email);
      }
      url += "/" + email + "/status";
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.put);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "action": "activate"
      //  }  
      
      ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
      jsonObject.put("action", activate ? "activate" : "deactivate");
      String jsonRequest = GrouperUtil.jsonJacksonToString(jsonObject);
      
      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      if (!activate && GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUserDeactivatesInsteadOfDeactivating", false)) {
        
        debugMap.put("logUserDeactivateInsteadOfDeactivating", true);

      } else {

        try {
          grouperHttpClient.executeRequest();
          code = grouperHttpClient.getResponseCode();
          // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
          
        } catch (Exception e) {
          throw new RuntimeException("Error connecting to '" + url + "'", e);
        }
    
        debugMap.put("httpCode", code);
        
        if (code != 204) {
          throw new RuntimeException("Cant update user status '" + url + "', '" + activate + "' " + code);
        }
      }
      
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    
  }

  /**
   * @param configId
   * @param email
   * @param type 1 = basic, 2 = licensed, 3 on prem, 99 none
   */
  public static void userChangeType(String configId, String email, int type) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "userChangeType");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "users";
      if (email.contains("/")) {
        throw new RuntimeException("Invalid email: " + email);
      }
      url += "/" + email;
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.patch);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "action": "activate"
      //  }  
      
      ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
      jsonObject.put("type", type);
      String jsonRequest = GrouperUtil.jsonJacksonToString(jsonObject);
      
      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      if (type == 1 && GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUserDelicencesInsteadOfDelicensing", false)) {
        
        debugMap.put("logUserDelicencesInsteadOfDelicensing", true);

      } else {

        try {
          grouperHttpClient.executeRequest();
          code = grouperHttpClient.getResponseCode();
          // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
          
        } catch (Exception e) {
          throw new RuntimeException("Error connecting to '" + url + "'", e);
        }
    
        debugMap.put("httpCode", code);
        
        if (code != 204) {
          throw new RuntimeException("Cant update user type '" + url + "', '" + type + "' " + code);
        }
      }
      
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    
  }

  /**
   * @param configId
   * @param email
   * @param activate
   */
  public static void userChangePhoneLicense(String configId, String email, Boolean activate) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "userChangePhoneLicense");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
      String url = endpoint + "users";
      if (email.contains("/")) {
        throw new RuntimeException("Invalid email: " + email);
      }
      url += "/" + email + "/settings";
      debugMap.put("url", url);
  
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.patch);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);

      ObjectNode jsonObject = GrouperUtil.jsonJacksonNode();
      ObjectNode jsonObjectInside = GrouperUtil.jsonJacksonNode();
      jsonObjectInside.put("zoom_phone", activate);
      jsonObject.set("feature", jsonObjectInside);
      String jsonRequest = GrouperUtil.jsonJacksonToString(jsonObject);
      
      grouperHttpClient.assignBody(jsonRequest);
      
      int code = -1;
      grouperHttpClient.executeRequest();
      code = grouperHttpClient.getResponseCode();
      debugMap.put("httpCode", code);

      if (code != 204) {
          throw new RuntimeException("Cant update user type '" + url + "', '" + code);
      }
      
    } catch (Exception e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * 
   * @param configId
   * @return map key is id, and value with id(string), account_name(string), owner_email(string), account_type(string), seats(int), subscription_start_time(string), 
   * subscription_end_time(string), created_at(string)
   */
  public static Map<String, Map<String, Object>> retrieveAccounts(String configId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveAccounts");
    String json = null;
    
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
  
      int pageSize = 300;
      debugMap.put("pageSize", pageSize);
      //page_size, page_number
  
      String url = endpoint + "accounts?page_size=" + pageSize;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
  
      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get accounts from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "page_count": "integer",
      //    "page_number": "integer",
      //    "page_size": "integer",
      //    "total_records": "integer",
      //    "accounts": [
      //      {
      //        "id": "string [uuid]",
      //        "account_name": "string",
      //        "owner_email": "string",
      //        "account_type": "string",
      //        "seats": "integer",
      //        "subscription_start_time": "string [date-time]",
      //        "subscription_end_time": "string [date-time]",
      //        "created_at": "string [date-time]"
      //      }
      //    ]
      //  }      

      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
      
      ArrayNode jsonArray = jsonObject.has("accounts") ? (ArrayNode)jsonObject.get("accounts") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectAccount = (JsonNode)jsonArray.get(i);
          Map<String, Object> accountMap = new HashMap<String, Object>();
          
          if (jsonObjectAccount.has("id")) {
            accountMap.put("id", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "id"));
          }
          if (jsonObjectAccount.has("account_name")) {
            accountMap.put("account_name", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "account_name"));
          }
          if (jsonObjectAccount.has("owner_email")) {
            accountMap.put("owner_email", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "owner_email"));
          }
          if (jsonObjectAccount.has("account_type")) {
            accountMap.put("account_type", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "account_type"));
          }
          if (jsonObjectAccount.has("seats")) {
            accountMap.put("seats", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "seats"));
          }
          if (jsonObjectAccount.has("subscription_start_time")) {
            accountMap.put("subscription_start_time", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "subscription_start_time"));
          }
          if (jsonObjectAccount.has("subscription_end_time")) {
            accountMap.put("subscription_end_time", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "subscription_end_time"));
          }
          if (jsonObjectAccount.has("created_at")) {
            accountMap.put("created_at", GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "created_at"));
          }
              
          result.put(GrouperUtil.jsonJacksonGetString(jsonObjectAccount, "id"), accountMap);
        }
      }
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("json", json);
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param subaccountId 
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
   * personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), account_id(string), status(string e.g. active)
   * or null if not found
   */
  public static Map<String, Map<String, Object>> retrieveSubaccountUsers(String configId, String subaccountId) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveSubaccountUsers");
    try {
      debugMap.put("configId", configId);
      
      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
      
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeUsers", 300);
  
      for (int i=0;i<10000;i++) {
        Map<String, Map<String, Object>> tempResult = retrieveSubaccountUsersHelper(configId, subaccountId, i+1);
        
        result.putAll(tempResult);
  
        // we are done when there are no reults or its less than the page size
        if (tempResult.size() < pageSize) {
          break;
        }
      }
      
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param configId
   * @param subaccountId 
   * @param pageNumberOneIndexed 
   * @return map key is email, and value with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
   * personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), account_id(string), status(string e.g. active)
   * or null if not found
   */
  private static Map<String, Map<String, Object>> retrieveSubaccountUsersHelper(String configId, String subaccountId, int pageNumberOneIndexed) {
    
    long startedNanos = System.nanoTime();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    String json = null;
    
    debugMap.put("method", "retrieveSubaccountUsersHelper");
    try {
      String jwt = retrieveBearerTokenFromCacheOrFresh(configId);
      String endpoint = endpoint(configId);
      debugMap.put("configId", configId);
  
      //    zoom.myConfigId.pageSizeMemberships = 300
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeUsers", 300);
      debugMap.put("pageSize", pageSize);
      //page_size, page_number
      
      String url = endpoint + "accounts/" + subaccountId + "/users?page_size=" + pageSize + "&page_number=" + pageNumberOneIndexed;
      debugMap.put("url", url);
    
      GrouperHttpClient grouperHttpClient = grouperHttpClient(configId);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
  
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.addHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
  
      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get users from '" + url + "' " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      
      //  {
      //    "users": [
      //      {
      //        "id": "z8yAAAAA8bbbQ",
      //        "first_name": "Melina",
      //        "last_name": "Ghimire",
      //        "email": "mel@jfggdhfhdfj.djfhdsfh",
      //        "type": 2,
      //        "pmi": 581111112,
      //        "timezone": "America/Los_Angeles",
      //        "verified": 1,
      //        "dept": "",
      //        "created_at": "2018-11-15T01:10:08Z",
      //        "last_login_time": "2019-09-13T21:08:52Z",
      //        "last_client_version": "4.4.55383.0716(android)",
      //        "pic_url": "https://lh4.googleusercontent.com/-someurl/photo.jpg",
      //        "im_group_ids": [
      //          "Abdsjkfhdhfj"
      //        ],
      //        "status": "active"
      //      }
      //    ],
      //    "page_count": 1,
      //    "page_number": 1,
      //    "page_size": 1,
      //    "total_records": 1
      //  }
      
      Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
      
      int pageNumberReturned = GrouperUtil.jsonJacksonGetInteger(jsonObject, "page_number");
      int totalRecords = GrouperUtil.jsonJacksonGetInteger(jsonObject, "total_records");
      debugMap.put("totalRecords", totalRecords);
      
      // if we are over the number of pages, then dont return any members to notify caller we are done paging
      if (pageNumberOneIndexed != pageNumberReturned) {
        debugMap.put("count", 0);
  
        return result;
      }
      
      ArrayNode jsonArray = jsonObject.has("users") ? (ArrayNode)jsonObject.get("users") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JsonNode jsonObjectUser = (JsonNode)jsonArray.get(i);
          Map<String, Object> userMap = retrieveUserFromJsonObject(jsonObjectUser);
          String email = (String)userMap.get("email");
          if (!StringUtils.isBlank(email)) {
            result.put(email, userMap);
          }
        }
      }
      debugMap.put("count", result.size());
  
      return result;
  
    } catch (RuntimeException e) {
      debugMap.put("json", json);
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

}

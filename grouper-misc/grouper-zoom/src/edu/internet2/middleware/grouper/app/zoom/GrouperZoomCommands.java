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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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
    
    final String configId = "pennZoomProd";
//    List<Map<String, Object>> groups = retrieveGroups(configId);
//    for (Map<String, Object> group: groups) {
//      System.out.println("Group: " + group.get("id") + ", " + group.get("name") + ", " + group.get("total_members"));
//      
//      List<Map<String, Object>> members = retrieveGroupMemberships(configId, (String)group.get("id"));
//      for (Map<String, Object> member: members) {
//        System.out.println("Member: " + member.get("id") + ", " + member.get("email") + ", " + member.get("first_name")
//            + ", " + member.get("last_name") + ", " + member.get("type") + ", " + member.get("primary_group"));
//      }
//    }

//    Map<String, Object> group = createGroup(configId, "test");
//    System.out.println("Group: " + group.get("id") + ", " + group.get("name") + ", " + group.get("total_members"));

//    deleteGroup(configId, "j1FjzLFpS2Cd2KWzGehyNQ");

//    addGroupMembership(configId, "pE3vVI9RQeeImJL3bbmSNA", "ZelEDQlNRSWau5tOzYZQYA");

    removeGroupMembership(configId, "pE3vVI9RQeeImJL3bbmSNA", "ZelEDQlNRSWau5tOzYZQYA");

//  * @return map with id(string), first_name(string), last_name(string), email(string), type(int), role_name(string), 
//  * personal_meeting_url(string), timezone(string), verified(int e.g. 1), group_ids (array[string]), account_id(string), status(string e.g. active)

    
//    Map<String, Object> user = retrieveUser(configId, "mchyzer@upenn.edu");
//    System.out.println("Member: " + user.get("id") + ", " + user.get("email") + ", " + user.get("first_name")
//      + ", " + user.get("last_name") + ", " + user.get("type") + ", " + user.get("role_name") + ", " + user.get("personal_meeting_url")
//      + ", " + user.get("timezone") + ", " + user.get("verified") + ", " + GrouperUtil.toStringForLog(user.get("group_ids"))
//      + ", " + user.get("account_id") + ", " + user.get("status"));
    
    
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
   * get the cache based on how long to cache
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
  
  private static String endpoint(String configId) {
    String endpoint = GrouperConfig.retrieveConfig().propertyValueString("zoom." + configId + ".endpoint", "https://api.zoom.us/v2");
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
    
      GetMethod getMethod = new GetMethod(url);
      HttpClient httpClient = new HttpClient();
  
      getMethod.addRequestHeader("Content-Type", "application/json");
      getMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(getMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = getMethod.getResponseBodyAsString();
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
      
      JSONObject jsonObject = JSONObject.fromObject(json);
      
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
      
      result.put("id", jsonObject.getString("id"));
      result.put("first_name", jsonObject.getString("first_name"));
      result.put("last_name", jsonObject.getString("last_name"));
      result.put("email", jsonObject.getString("email"));
      result.put("type", jsonObject.getInt("type"));
      result.put("role_name", jsonObject.getString("role_name"));
      result.put("personal_meeting_url", jsonObject.getString("personal_meeting_url"));
      result.put("timezone", jsonObject.getString("timezone"));
      result.put("verified", jsonObject.getInt("verified"));
      JSONArray groupIdsJsonArray = jsonObject.containsKey("group_ids") ? jsonObject.getJSONArray("group_ids") : null;
      String[] groupIdsArray = new String[groupIdsJsonArray == null ? 0 : groupIdsJsonArray.size()];
      for (int i=0;i<(groupIdsJsonArray == null ? 0 : groupIdsJsonArray.size());i++) {
        groupIdsArray[i] = groupIdsJsonArray.getString(i);
      }
      result.put("group_ids", groupIdsArray);
      result.put("account_id", jsonObject.getString("account_id"));
      result.put("status", jsonObject.getString("status"));
      
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
    
      GetMethod getMethod = new GetMethod(url);
      HttpClient httpClient = new HttpClient();
  
      getMethod.addRequestHeader("Content-Type", "application/json");
      getMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(getMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = getMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get groups from '" + url + "' " + json);
      }
      
      JSONObject jsonObject = JSONObject.fromObject(json);
      
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
      
      JSONArray jsonArray = jsonObject.has("groups") ? jsonObject.getJSONArray("groups") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JSONObject jsonObjectGroup = (JSONObject)jsonArray.get(i);
          Map<String, Object> groupMap = new HashMap<String, Object>();
          groupMap.put("id", jsonObjectGroup.getString("id"));
          final String name = jsonObjectGroup.getString("name");
          groupMap.put("name", name);
          groupMap.put("total_members", jsonObjectGroup.getInt("total_members"));
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
      
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeMemberships", 10);

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
      int pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".pageSizeMemberships", 10);
      debugMap.put("pageSize", pageSize);
      //page_size, page_number

      String url = endpoint + "groups/" + groupId + "/members?page_size=" + pageSize + "&page_number=" + pageNumberOneIndexed;
      debugMap.put("url", url);
    
      GetMethod getMethod = new GetMethod(url);
      HttpClient httpClient = new HttpClient();
  
      getMethod.addRequestHeader("Content-Type", "application/json");
      getMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(getMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = getMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }


      debugMap.put("httpCode", code);
      
      if (code != 200) {
        throw new RuntimeException("Cant get groups from '" + url + "' " + json);
      }
      
      JSONObject jsonObject = JSONObject.fromObject(json);
      
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
      
      int pageNumberReturned = jsonObject.getInt("page_number");
      int totalRecords = jsonObject.getInt("total_records");
      debugMap.put("totalRecords", totalRecords);
      
      // if we are over the number of pages, then dont return any members to notify caller we are done paging
      if (pageNumberOneIndexed != pageNumberReturned) {
        debugMap.put("count", 0);

        return result;
      }
      
      JSONArray jsonArray = jsonObject.has("members") ? jsonObject.getJSONArray("members") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        for (int i=0;i<jsonArray.size();i++) {
          JSONObject jsonObjectMember = (JSONObject)jsonArray.get(i);
          Map<String, Object> memberMap = new HashMap<String, Object>();
          memberMap.put("id", jsonObjectMember.getString("id"));
          memberMap.put("email", jsonObjectMember.getString("email"));
          memberMap.put("first_name", jsonObjectMember.getString("first_name"));
          memberMap.put("last_name", jsonObjectMember.getString("last_name"));
          memberMap.put("type", jsonObjectMember.getInt("type"));
          memberMap.put("primary_group", jsonObjectMember.getBoolean("primary_group"));
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

      PostMethod postMethod = new PostMethod(url);
      HttpClient httpClient = new HttpClient();
  
      postMethod.addRequestHeader("Content-Type", "application/json");
      postMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "name": "myawesomegroup"
      //  }

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("name", name);
      String jsonRequest = jsonObject.toString();
      
      postMethod.setRequestEntity(new StringRequestEntity(jsonRequest, "application/json", "UTF-8"));
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(postMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = postMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      debugMap.put("httpCode", code);
      
      if (code != 201) {
        throw new RuntimeException("Cant create group '" + url + "', '" + name + "' " + json);
      }
      
      jsonObject = JSONObject.fromObject(json);
      
      //  {
      //    "id": "string",
      //    "name": "string",
      //    "total_members": "integer"
      //  }

      Map<String, Object> result = new HashMap<String, Object>();
      
      result.put("id", jsonObject.getString("id"));
      result.put("name", jsonObject.getString("name"));
      result.put("total_members", jsonObject.getInt("total_members"));
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
  
      PostMethod postMethod = new PostMethod(url);
      HttpClient httpClient = new HttpClient();
  
      postMethod.addRequestHeader("Content-Type", "application/json");
      postMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "members": [
      //      {
      //        "id": "36565387",
      //        "email": "memberemail@somecompany.com"
      //      }
      //    ]
      //  }  
      
      JSONObject jsonObject = new JSONObject();
      JSONArray jsonArray = new JSONArray();
      JSONObject member = new JSONObject();
      member.put("id", memberId);
      jsonArray.add(member);
      jsonObject.put("members", jsonArray);
      String jsonRequest = jsonObject.toString();
      
      postMethod.setRequestEntity(new StringRequestEntity(jsonRequest, "application/json", "UTF-8"));
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(postMethod);
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
  
      DeleteMethod deleteMethod = new DeleteMethod(url);
      HttpClient httpClient = new HttpClient();
  
      deleteMethod.addRequestHeader("Content-Type", "application/json");
      deleteMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      //  {
      //    "name": "myawesomegroup"
      //  }
  
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("groupId", groupId);
      
      int code = -1;
  
      try {
        code = httpClient.executeMethod(deleteMethod);
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
  
      DeleteMethod deleteMethod = new DeleteMethod(url);
      HttpClient httpClient = new HttpClient();
  
      deleteMethod.addRequestHeader("Content-Type", "application/json");
      deleteMethod.addRequestHeader("Authorization", "Bearer " + jwt);
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(deleteMethod);
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
  
  
}

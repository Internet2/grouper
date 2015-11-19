/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.duosecurity.client.Http;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDuoCommands {

  /**
   * 
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    
//    for (GrouperDuoGroup grouperDuoGroup : retrieveGroups().values()) {
//      System.out.println(grouperDuoGroup);
//    }

//    createDuoGroup("test2", "testDesc", true);
//    updateDuoGroup("DG6LYPHI53Y8K50JJZYQ", "testDesc2", true);



    // mchyzer DU71ZRNO1W6507WQMJIP
//    System.out.println(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"));

      String username = "mchyzer";
      String groupName = "test2";
//      assignUserToGroupIfNotInGroup(retrieveUserIdFromUsername(username), retrieveGroupIdFromGroupName(groupName), false);
//    removeUserFromGroup(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"), retrieveGroups().get("test2").getId(), false);
//    System.out.println(userInGroup(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"), retrieveGroups().get("test2").getId(), false));
  
//      deleteDuoGroup(retrieveGroupIdFromGroupName(groupName), false);
      
      deleteDuoGroup("DGVWQ4JEQIUE390MJLDD", false);
      
//      for (GrouperDuoUser grouperDuoUser : retrieveUsersForGroup(retrieveGroupIdFromGroupName(groupName)).values()) {
//        System.out.println(grouperDuoUser);
//      }

  }

  /**
   * @param groupName
   * @return the groupName
   */
  public static String retrieveGroupIdFromGroupName(String groupName) {
    GrouperDuoGroup grouperDuoGroup = retrieveGroups().get(groupName);
    return grouperDuoGroup == null ? null : grouperDuoGroup.getId();
  }

  /**
   * @param username
   * @return the userId
   */
  public static String retrieveUserIdFromUsername(String username) {
    JSONObject duoUser = retrieveDuoUserByIdOrUsername(username, false, null);
    return (duoUser == null || !duoUser.has("user_id")) ? null : duoUser.getString("user_id");
  }

  /**
   * 
   */
  public GrouperDuoCommands() {
  }

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @return the http
   */
  private static Http httpAdmin(String method, String path) {
    return httpAdmin(method, path, null);
  }

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @param timeoutSeconds
   * @return the http
   */
  private static Http httpAdmin(String method, String path, Integer timeoutSeconds) {
    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.adminDomainName");
    
    Http request = (timeoutSeconds != null && timeoutSeconds > 0) ? 
        new Http(method, domain, path, timeoutSeconds) : new Http(method, domain, path);

    return request;
  }

  /**
   * execute response raw without checked exception
   * @param request
   * @return the string
   */
  private static String executeRequestRaw(Http request) {
    try {
      return request.executeRequestRaw();
    } catch (Exception e) {
      throw new RuntimeException("Problem with duo", e);
    }
  }
  
  /**
   * sign the http request
   * @param request
   */
  private static void signHttpAdmin(Http request) {
    String integrationKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.adminIntegrationKey");
    String secretKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.adminSecretKey");
    try {
      request.signRequest(integrationKey,
          secretKey);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Error signing request", uee);
    }
    
  }

  /**
   * retrieve duo user
   * @param theId 
   * @param isDuoUuid true if id, false if username
   * @param timeoutSeconds null if no timeout
   * @return the json object
   */
  public static JSONObject retrieveDuoUserByIdOrUsername(String theId, boolean isDuoUuid, Integer timeoutSeconds) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoUserByIdOrUsername");
    if (isDuoUuid) {
      debugMap.put("userId", theId);
    } else {
      debugMap.put("username", theId);
    }
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(theId)) {
        throw new RuntimeException("Why is netId blank?");
      }
      
      //retrieve user
      String path = "/admin/v1/users" + (isDuoUuid ? ("/" + theId) : "");
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path, timeoutSeconds);
      
      if (!isDuoUuid) {
        request.addParam("username", theId);
      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":[
      //      {
      //        "desktoptokens":[
      //          
      //        ],
      //        "email":"",
      //        "groups":[
      //          
      //        ],
      //        "last_login":null,
      //        "notes":"",
      //        "phones":[
      //          
      //        ],
      //        "realname":"",
      //        "status":"active",
      //        "tokens":[
      //          
      //        ],
      //        "user_id":"DUXEK2QS0MSI7TV3TEN1",
      //        "username":"harveycg"
      //      }
      //    ],
      //    "stat":"OK"
      //  }    
      
      // {"code": 40401, "message": "Resource not found", "stat": "FAIL"}
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      
      if (jsonObject.has("code") && jsonObject.getInt("code") == 40401) {
        debugMap.put("code", 40401);
        return null;
      }
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result + ", " + theId);
      }
      Object response = jsonObject.get("response");
      JSONObject duoUser = null;
      if (response instanceof JSONObject) {
        duoUser = (JSONObject)response;
      } else {
        JSONArray responseArray = (JSONArray)response;
        if (responseArray.size() > 0) {
          if (responseArray.size() > 1) {
            throw new RuntimeException("Why more than 1 user found? " + responseArray.size() + ", " + result);
          }
          duoUser = (JSONObject)responseArray.get(0);
        }
      }
      if (duoUser != null) {
        debugMap.put("returnedUserId", duoUser.getString("user_id"));
        debugMap.put("returnedUsername", duoUser.getString("username"));
      }
      return duoUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * @param groupId
   * @param groupDescription
   * @param isIncremental
   * @return the json object
   */
  public static JSONObject updateDuoGroup(String groupId, String groupDescription, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateDuoGroup");
    debugMap.put("id", groupId);
    debugMap.put("desc", groupDescription);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //create user
      String path = "/admin/v1/groups/" + groupId;
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("desc", groupDescription);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "response": {
      //      "desc": "Group description",
      //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
      //      "name": "Group Name",
      //      "push_enabled": true,
      //      "sms_enabled": true,
      //      "status": "active",
      //      "voice_enabled": true,
      //      "mobile_otp_enabled": true
      //    },
      //    "stat": "OK"
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        
        // {
        //    "code": 40003, 
        //    "message": "Duplicate resource", 
        //    "stat": "FAIL"
        // }
        
        debugMap.put("error", true);
          debugMap.put("result", result);
          throw new RuntimeException("Bad response from Duo: " + result);
      }

      jsonObject = (JSONObject)jsonObject.get("response");
        
      String groupName = jsonObject.getString("name");

      debugMap.put("groupName", groupName);
      
      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * @param userId
   * @param groupId
   * @param isIncremental
   * @return the json object
   */
  public static JSONObject assignUserToGroup(String userId, String groupId, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "assignUserToGroup");
    debugMap.put("userId", userId);
    debugMap.put("groupId", groupId);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
      
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("Why is userId blank?");
      }
      
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //assign group to user
      // POST /admin/v1/users/[user_id]/groups
      String path = "/admin/v1/users/" + userId + "/groups";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("group_id", groupId);

      signHttpAdmin(request);

      String result = executeRequestRaw(request);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {

        //  {
        //    "stat": "OK",
        //    "response": ""
        //  }
        
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * @param userId
   * @param groupId
   * @param isIncremental
   * @return the json object
   */
  public static boolean userInGroup(String userId, String groupId, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "userInGroup");
    debugMap.put("userId", userId);
    debugMap.put("groupId", groupId);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
      
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("Why is userId blank?");
      }
      
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //retrieve groups for users
      // GET /admin/v1/users/[user_id]/groups
      String path = "/admin/v1/users/" + userId + "/groups";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);

      signHttpAdmin(request);

      String result = executeRequestRaw(request);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {

        //  {
        //    "response": [{
        //      "desc": "This is group A",
        //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
        //      "name": "Group A"
        //    },
        //    {
        //      "desc": "This is group B",
        //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
        //      "name": "Group B"
        //    }],
        //    "stat": "OK"
        //  }
        
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      JSONArray resultArray = (JSONArray)jsonObject.get("response");
      
      Map<String, GrouperDuoGroup> results = convertJsonArrayToGroups(resultArray);

      for (GrouperDuoGroup grouperDuoGroup : results.values()) {
        if (StringUtils.equals(groupId, grouperDuoGroup.getId())) {
          return true;
        }
      }
      
      return false;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * @param userId
   * @param groupId
   * @param isIncremental
   * @return the json object
   */
  public static JSONObject removeUserFromGroup(String userId, String groupId, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "removeUserFromGroup");
    debugMap.put("userId", userId);
    debugMap.put("groupId", groupId);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
  
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("Why is userId blank?");
      }

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //assign group to user
      // DELETE /admin/v1/users/[user_id]/groups/[group_id]
      String path = "/admin/v1/users/" + userId + "/groups/" + groupId;
      debugMap.put("DELETE", path);
      Http request = httpAdmin("DELETE", path);

      signHttpAdmin(request);

      String result = executeRequestRaw(request);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {

        //  {
        //    "stat": "OK",
        //    "response": ""
        //  }
        
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * @param groupId
   * @return the map from username to grouper user object
   */
  public static Map<String, GrouperDuoUser> retrieveUsersForGroup(String groupId) {
  
    JSONObject response = retrieveGroupInfoHelper(groupId, "retrieveUsersForGroup", false);
    return retrieveUsersForGroup(response);

  }

  
  /**
   * @param response
   * @return the map from username to grouper user object
   */
  public static Map<String, GrouperDuoUser> retrieveUsersForGroup(JSONObject response) {
    
    JSONArray resultArray = (JSONArray)response.get("users");

    Map<String, GrouperDuoUser> results = new LinkedHashMap<String, GrouperDuoUser>();

    for (int i=0;i<resultArray.size();i++) {
      JSONObject user = resultArray.getJSONObject(i);
      GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
      grouperDuoUser.setUserId(user.getString("user_id"));
      grouperDuoUser.setUsername(user.getString("username"));
      results.put(grouperDuoUser.getUsername(), grouperDuoUser);
    }
    return results;

  }
  
  /**
   * @param groupId
   * @param isIncremental
   * @return the response object for 
   */
  public static JSONObject retrieveGroupInfo(String groupId, boolean isIncremental) {
    return retrieveGroupInfoHelper(groupId, "retrieveGroupInfo", isIncremental);
  }
  
  /**
   * @param groupId
   * @param methodNameForLog
   * @param isIncremental
   * @return the response object for 
   */
  private static JSONObject retrieveGroupInfoHelper(String groupId, String methodNameForLog, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", methodNameForLog);
    debugMap.put("groupId", groupId);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
  
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //Retrieve information about a group.
      //GET /admin/v1/groups/[group_id]
      String path = "/admin/v1/groups/" + groupId;
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);

      signHttpAdmin(request);

      String result = executeRequestRaw(request);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {

        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      //  {
      //    "response": {
      //      "desc": "Group description",
      //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
      //      "name": "Group Name",
      //      "push_enabled": true,
      //      "sms_enabled": true,
      //      "status": "active",
      //      "users": [{
      //        "user_id": "DUXXXXXXXXXXXXXXXXXX",
      //        "username": "User A"
      //      },
      //      {
      //        "user_id": "DUXXXXXXXXXXXXXXXXXX",
      //        "username": "User B"
      //      }],
      //      "voice_enabled": true,
      //      "mobile_otp_enabled": true
      //    },
      //    "stat": "OK"
      //  }

      JSONObject response = (JSONObject)jsonObject.get("response");

      return response;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * @return the name of group mapped to group
   */
  public static Map<String, GrouperDuoGroup> retrieveGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGroups");

    long startTime = System.nanoTime();
    
    try {

      //create user
      String path = "/admin/v1/groups";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "response": [{
      //      "desc": "This is group A",
      //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
      //      "name": "Group A",
      //      "push_enabled": true,
      //      "sms_enabled": true,
      //      "status": "active",
      //      "voice_enabled": true,
      //      "mobile_otp_enabled": true
      //    },
      //    {
      //      "desc": "This is group B",
      //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
      //      "name": "Group B",
      //      "push_enabled": true,
      //      "sms_enabled": true,
      //      "status": "active",
      //      "voice_enabled": true,
      //      "mobile_otp_enabled": true
      //    }],
      //    "stat": "OK"
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        
        // {
        //    "code": 40003, 
        //    "message": "Duplicate resource", 
        //    "stat": "FAIL"
        // }
        
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      JSONArray resultArray = (JSONArray)jsonObject.get("response");
      
      Map<String, GrouperDuoGroup> results = convertJsonArrayToGroups(resultArray);

      debugMap.put("numberOfGroups", GrouperUtil.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * @param resultArray
   * @return the map
   */
  private static Map<String, GrouperDuoGroup> convertJsonArrayToGroups(JSONArray resultArray) {
    Map<String, GrouperDuoGroup> results = new LinkedHashMap<String, GrouperDuoGroup>();

    for (int i=0;i<resultArray.size();i++) {
      JSONObject group = resultArray.getJSONObject(i);
      GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
      grouperDuoGroup.setId(group.getString("group_id"));
      grouperDuoGroup.setName(group.getString("name"));
      grouperDuoGroup.setDescription(group.getString("desc"));
      results.put(grouperDuoGroup.getName(), grouperDuoGroup);
    }
    return results;
  }
  
  /**
   * create duo group
   * @param groupName 
   * @param groupDescription 
   * @param isIncremental incremental or full (for logging)
   * @return the json object
   */
  public static JSONObject createDuoGroup(String groupName, String groupDescription, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoGroup");
    debugMap.put("name", groupName);
    debugMap.put("desc", groupDescription);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(groupName)) {
        throw new RuntimeException("Why is groupName blank?");
      }
      
      //create user
      String path = "/admin/v1/groups";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("name", groupName);
      request.addParam("desc", StringUtils.defaultString(groupDescription));
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "response": {
      //      "desc": "This is an example group",
      //      "group_id": "DGXXXXXXXXXXXXXXXXXX",
      //      "name": "Example Group",
      //      "push_enabled": true,
      //      "sms_enabled": false,
      //      "status": "active",
      //      "voice_enabled": true,
      //      "mobile_otp_enabled": true
      //    },
      //    "stat": "OK"
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      boolean alreadyExisted = false;
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        
        // {
        //    "code": 40003, 
        //    "message": "Duplicate resource", 
        //    "stat": "FAIL"
        // }
        
        if (alreadyExisted(jsonObject)) {
          
          debugMap.put("alreadyExisted", true);
          
        } else {
        
          alreadyExisted = true;
          debugMap.put("error", true);
            debugMap.put("result", result);
            throw new RuntimeException("Bad response from Duo: " + result);
        }
      }

      if (!alreadyExisted) {
        jsonObject = (JSONObject)jsonObject.get("response");
          
        String groupId = jsonObject.getString("group_id");
  
        debugMap.put("groupId", groupId);
      }
      
      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
    
  }

  /**
   * delete duo group
   * @param groupId
   * @param isIncremental incremental or full (for logging)
   * @return the json object
   */
  public static JSONObject deleteDuoGroup(String groupId, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoGroup");
    debugMap.put("groupId", groupId);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Why is groupId blank?");
      }
      
      //create user
      //  Delete a group.
      //
      //  DELETE /admin/v1/groups/[group_id]
      //  PARAMETERS
      //
      String path = "/admin/v1/groups/" + groupId;
      debugMap.put("DELETE", path);
      Http request = httpAdmin("DELETE", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "response": "",
      //    "stat": "OK"
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
    
  }

  /**
   * 
   * @param jsonObject
   * @return true if already exists
   */
  public static boolean alreadyExisted(JSONObject jsonObject) {
    return jsonObject.has("code") && StringUtils.equals(jsonObject.getString("code"), "40003");
  }

  /**
   * @param userId
   * @param groupId
   * @param isIncremental
   * @return the json object
   */
  public static JSONObject assignUserToGroupIfNotInGroup(String userId, String groupId, boolean isIncremental) {
    
    if (!userInGroup(userId, groupId, isIncremental)) {
      return assignUserToGroup(userId, groupId, isIncremental);
    }
    
    return null;
  }
  
}

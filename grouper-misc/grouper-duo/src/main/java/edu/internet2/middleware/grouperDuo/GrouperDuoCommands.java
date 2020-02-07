/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import com.duosecurity.client.Http;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


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
    
    String groupId = retrieveGroupIdFromGroupName("test1");
//    String userId = retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id");
//    for (GrouperDuoGroup grouperDuoGroup : retrieveGroupsForUser(userId, false).values()) {
//      System.out.println(grouperDuoGroup);
//    }
  
//      for (GrouperDuoUser grouperDuoUser : retrieveUsersForGroup(groupId).values()) {
//        System.out.println(grouperDuoUser);
//      }

    for (String id : new String[]{"harveycg", "admorten", "mchyzer", "campeau", "couch", "dak", "danalane", "ghamlin", "harris2", "isobel"} ) {

      String name = "my name is " + id;
      RegistrySubject registrySubject = new RegistrySubject();
      registrySubject.setId(id);
      registrySubject.setName(name);
      registrySubject.setTypeString("person");
      
      registrySubject.getAttributes(false).put("name", GrouperUtil.toSet("name." + id));
      registrySubject.getAttributes(false).put("loginid", GrouperUtil.toSet("id." + id));
      registrySubject.getAttributes(false).put("description", GrouperUtil.toSet("description." + id));
      registrySubject.getAttributes(false).put("email", GrouperUtil.toSet(id + "@somewhere.someSchool.edu"));
      try {
        GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "registrySubject: " + registrySubject.getId());
        throw re;
      }
      
    }


//    System.out.println(userInGroup(userId, groupId, true));
    
//    createDuoGroup("test2", "testDesc", true);
//    updateDuoGroup("DG6LYPHI53Y8K50JJZYQ", "testDesc2", true);

//    System.out.println(userInGroup(retrieveUserIdFromUsername("mchyzer"), "test1", true));

    // mchyzer DU71ZRNO1W6507WQMJIP
//    System.out.println(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"));

//      String username = "mchyzer";
//      String groupName = "test2";
//      assignUserToGroupIfNotInGroup(retrieveUserIdFromUsername(username), retrieveGroupIdFromGroupName(groupName), false);
//    removeUserFromGroup(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"), retrieveGroups().get("test2").getId(), false);
//    System.out.println(userInGroup(retrieveDuoUserByIdOrUsername("mchyzer", false, null).getString("user_id"), retrieveGroups().get("test2").getId(), false));
  
//      deleteDuoGroup(retrieveGroupIdFromGroupName(groupName), false);
      
//      deleteDuoGroup("DGVWQ4JEQIUE390MJLDD", false);
      
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
    
    Map<String, GrouperDuoGroup> results = retrieveGroupsForUser(userId, isIncremental);

    for (GrouperDuoGroup grouperDuoGroup : results.values()) {
      if (StringUtils.equals(groupId, grouperDuoGroup.getId())) {
        return true;
      }
    }
    return false; 
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
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveUsersForGroup");

    long startTime = System.nanoTime();
    
    try {
      int[] totalObjects = new int[] {-1};
      int[] nextOffset = new int[] {-1};
      Map<String, GrouperDuoUser> allResults = new LinkedHashMap<String, GrouperDuoUser>();
      int offset = 0;
      
      for (int i=0;i<4000;i++) {

        debugMap.put("numberOfCalls", i+1);

        totalObjects[0] = -1;
        nextOffset[0] = -1;
        
        Map<String, GrouperDuoUser> pageResult = retrieveUsersForGroupHelper(groupId, offset, totalObjects, nextOffset);
        allResults.putAll(pageResult);

        debugMap.put("totalObjects", totalObjects[0]);
        
        if (nextOffset[0] == -1) {
          break;
        }

        offset = nextOffset[0];
      }

      debugMap.put("numberOfUsers", GrouperUtil.length(allResults));

      return allResults;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }


  }
  

  /**
   * get all groups, loop through pages
   * @return the name of group mapped to group
   */
  public static Map<String, GrouperDuoGroup> retrieveGroups() {
  
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGroups");

    long startTime = System.nanoTime();
    
    try {
      int[] totalObjects = new int[] {-1};
      int[] nextOffset = new int[] {-1};
      Map<String, GrouperDuoGroup> allResults = new LinkedHashMap<String, GrouperDuoGroup>();
      int offset = 0;
      
      for (int i=0;i<4000;i++) {

        debugMap.put("numberOfCalls", i+1);

        totalObjects[0] = -1;
        nextOffset[0] = -1;
        
        Map<String, GrouperDuoGroup> pageResult = retrieveGroupsHelper(offset, totalObjects, nextOffset);
        allResults.putAll(pageResult);

        debugMap.put("totalObjects", totalObjects[0]);
        
        if (nextOffset[0] == -1) {
          break;
        }

        offset = nextOffset[0];
      }

      debugMap.put("numberOfGroups", GrouperUtil.length(allResults));

      return allResults;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

    
  }
  
  /**
   * get one page of the groups
   * @param offset first zero based index to get in paging
   * @param totalObjects pass back how many total object
   * @param nextOffset pass back the next index to get.  if -1, we done
   * @return the name of group mapped to group
   */
  private static Map<String, GrouperDuoGroup> retrieveGroupsHelper(int offset, int[] totalObjects, int[] nextOffset) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGroupsHelper");

    long startTime = System.nanoTime();
    
    try {

      //create user
      String path = "/admin/v1/groups";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);

      // the max is 300, but make it 100, doesnt hurt
      request.addParam("limit", "1000");
      debugMap.put("limit", 1000);

      request.addParam("offset", "" + offset);
      debugMap.put("offset", offset);

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
      
      //  {
      //    "metadata": {
      //        "next_offset": 100,
      //        "prev_offset": 0,
      //        "total_objects": 951
      //    }
      //  }
      if (jsonObject.containsKey("metadata")) {
        JSONObject metadataJsonObject = (JSONObject)jsonObject.get("metadata");
        if (metadataJsonObject.containsKey("next_offset")) {
          nextOffset[0] = metadataJsonObject.getInt("next_offset");
          debugMap.put("next_offset", nextOffset[0]);
        }
        if (metadataJsonObject.containsKey("total_objects")) {
          totalObjects[0] = metadataJsonObject.getInt("total_objects");
          debugMap.put("total_objects", totalObjects[0]);
        }
        
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
   * @param resultArray
   * @return the map
   */
  private static Map<String, GrouperDuoUser> convertJsonArrayToUsers(JSONArray resultArray) {
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

  private static GrouperDuoAdministrator applyJsonObjectToAdministrator(GrouperDuoAdministrator admin, JSONObject responseObject) {
    GrouperDuoLog.duoLog("applyJsonObjectToAdministrator: " + responseObject.toString());

    admin.setActive(responseObject.getString("status").equals("Active"));
    admin.setAdminId(responseObject.getString("admin_id"));
    admin.setLastLogin(responseObject.optLong("last_login", 0));
    admin.setName(responseObject.getString("name"));
    admin.setPasswordChangeRequired(responseObject.getBoolean("password_change_required"));
    admin.setPhone(responseObject.getString("phone"));
    admin.setRestrictedByAdminUnits(responseObject.getBoolean("restricted_by_admin_units"));
    admin.setRole(responseObject.getString("role"));
    admin.setEmail(responseObject.getString("email"));
    
    return admin;
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

  /**
   * @param userId
   * @param isIncremental
   * @return the map of groups for the user
   */
  public static Map<String, GrouperDuoGroup> retrieveGroupsForUser(String userId, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGroupsForUser");

    long startTime = System.nanoTime();
    
    try {
      int[] totalObjects = new int[] {-1};
      int[] nextOffset = new int[] {-1};
      Map<String, GrouperDuoGroup> allResults = new LinkedHashMap<String, GrouperDuoGroup>();
      int offset = 0;
      
      for (int i=0;i<4000;i++) {

        debugMap.put("numberOfCalls", i+1);

        totalObjects[0] = -1;
        nextOffset[0] = -1;
        
        Map<String, GrouperDuoGroup> pageResult = retrieveGroupsForUserHelper(
            userId, isIncremental, offset, totalObjects, nextOffset);
        allResults.putAll(pageResult);

        debugMap.put("totalObjects", totalObjects[0]);
        
        if (nextOffset[0] == -1) {
          break;
        }

        offset = nextOffset[0];
      }

      debugMap.put("numberOfGroups", GrouperUtil.length(allResults));

      return allResults;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  
  }

  /**
   * get one page of the groups
   * @param userId
   * @param isIncremental
   * @param offset first zero based index to get in paging
   * @param totalObjects pass back how many total object
   * @param nextOffset pass back the next index to get.  if -1, we done
   * @return the name of group mapped to group
   */
  private static Map<String, GrouperDuoGroup> retrieveGroupsForUserHelper(String userId, boolean isIncremental, int offset, int[] totalObjects, int[] nextOffset) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveGroupsForUserHelper");
  
    long startTime = System.nanoTime();
    
    try {
  
      //create user
      // GET /admin/v1/users/[user_id]/groups
      String path = "/admin/v1/users/" + userId + "/groups";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);
  
      // the max is 300, but make it 100, doesnt hurt
      request.addParam("limit", "1000");
      debugMap.put("limit", 1000);
  
      request.addParam("offset", "" + offset);
      debugMap.put("offset", offset);
  
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
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
      
      //  {
      //    "metadata": {
      //        "next_offset": 100,
      //        "prev_offset": 0,
      //        "total_objects": 951
      //    }
      //  }
      if (jsonObject.containsKey("metadata")) {
        JSONObject metadataJsonObject = (JSONObject)jsonObject.get("metadata");
        if (metadataJsonObject.containsKey("next_offset")) {
          nextOffset[0] = metadataJsonObject.getInt("next_offset");
          debugMap.put("next_offset", nextOffset[0]);
        }
        if (metadataJsonObject.containsKey("total_objects")) {
          totalObjects[0] = metadataJsonObject.getInt("total_objects");
          debugMap.put("total_objects", totalObjects[0]);
        }
        
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
   * get one page of the users in a group
   * @param groupId
   * @param offset first zero based index to get in paging
   * @param totalObjects pass back how many total object
   * @param nextOffset pass back the next index to get.  if -1, we done
   * @return the username of user mapped to user
   */
  private static Map<String, GrouperDuoUser> retrieveUsersForGroupHelper(String groupId, int offset, int[] totalObjects, int[] nextOffset) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveUsersForGroupHelper");
  
    long startTime = System.nanoTime();
    
    try {
  
      //create user
      // /admin/v2/groups/[group_id]/users
      String path = "/admin/v2/groups/" + groupId + "/users";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);
  
      // the max is 300, but make it 100, doesnt hurt
      request.addParam("limit", "1000");
      debugMap.put("limit", 1000);
  
      request.addParam("offset", "" + offset);
      debugMap.put("offset", offset);
  
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "metadata": {
      //        "total_objects": 4
      //    },
      //    "response": [{
      //        {
      //            "user_id": "DUXXXXXXXXXXXXXXXXXX",
      //            "username": "user1"
      //        },
      //        {
      //            "user_id": "DUXXXXXXXXXXXXXXXXXX",
      //            "username": "user2"
      //        },
      //        {
      //            "user_id": "DUXXXXXXXXXXXXXXXXXX",
      //            "username": "user3"
      //        },
      //    ],
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
      
      //  {
      //    "metadata": {
      //        "next_offset": 100,
      //        "prev_offset": 0,
      //        "total_objects": 951
      //    }
      //  }
      if (jsonObject.containsKey("metadata")) {
        JSONObject metadataJsonObject = (JSONObject)jsonObject.get("metadata");
        if (metadataJsonObject.containsKey("next_offset")) {
          nextOffset[0] = metadataJsonObject.getInt("next_offset");
          debugMap.put("next_offset", nextOffset[0]);
        }
        if (metadataJsonObject.containsKey("total_objects")) {
          totalObjects[0] = metadataJsonObject.getInt("total_objects");
          debugMap.put("total_objects", totalObjects[0]);
        }
        
      }
      
      JSONArray resultArray = (JSONArray)jsonObject.get("response");
      
      Map<String, GrouperDuoUser> results = convertJsonArrayToUsers(resultArray);
  
      debugMap.put("numberOfUsers", GrouperUtil.length(results));
  
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  
  }

  /**
   * @throws RuntimeException
   * @param name
   * @param emailAddress
   * @param password
   * @param phoneNumber
   * @param role
   * @param requirePasswordChange
   * @param isActive
   * @return
   */
  public static GrouperDuoAdministrator createNewAdminAccount(String name, String emailAddress, String password, String phoneNumber, String role, boolean requirePasswordChange, boolean isActive) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createNewAdminAccount");
    debugMap.put("name", name);
    debugMap.put("emailAddress", emailAddress);
    debugMap.put("password", "is " + (password == null ? "null" : "not null"));
    debugMap.put("phoneNumber", phoneNumber);
    debugMap.put("role", role);
    debugMap.put("requirePasswordChange", requirePasswordChange);
    debugMap.put("isActive", isActive);

    long startTime = System.nanoTime();

    try {
      Http request = httpAdmin("POST", "/admin/v1/admins");

      request.addParam("email", emailAddress);
      request.addParam("password", password);
      request.addParam("name", name);
      request.addParam("phone", phoneNumber);
      request.addParam("password_change_required", String.valueOf(requirePasswordChange));
      request.addParam("status", isActive ? "Active" : "Disabled");
      request.addParam("role", role);
      
      signHttpAdmin(request);
      String result = executeRequestRaw(request);
      debugMap.put("result", result);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(result);

      if (!jsonObject.getString("stat").equals("OK")) {
        throw new RuntimeException("Received an error response from duo: " + result);
      }

      GrouperDuoAdministrator admin = applyJsonObjectToAdministrator(new GrouperDuoAdministrator(), jsonObject.getJSONObject("response"));

      debugMap.put("createdAdminId", admin.getAdminId());

      return admin;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

  public static Map<String, GrouperDuoAdministrator> retrieveAdminAccounts() {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();

    debugMap.put("method", "retrieveAdminAccounts");

    boolean hasNextPage;
    int limit = 500;
    int page = 0;

    Map<String, GrouperDuoAdministrator> administrators = new HashMap<String, GrouperDuoAdministrator>();

    try {
      do {
        Http request = httpAdmin("GET", "/admin/v1/admins");

        debugMap.put("limit-" + page, String.valueOf(limit));
        request.addParam("limit", String.valueOf(limit));

        debugMap.put("offset-" + page, String.valueOf(page * limit));
        request.addParam("offset", String.valueOf(page * limit));

        signHttpAdmin(request);
        String result = executeRequestRaw(request);
        debugMap.put("result-" + page, result);

        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(result);

        if (!jsonObject.getString("stat").equals("OK")) {
          throw new RuntimeException("Received an error response from duo: " + result);
        }

        JSONArray objects = jsonObject.getJSONArray("response");
        for (int i = 0; i < objects.size(); i++) {
          GrouperDuoAdministrator admin = applyJsonObjectToAdministrator(new GrouperDuoAdministrator(), objects.getJSONObject(i));

          administrators.put(admin.getAdminId(), admin);
        }

        hasNextPage = false;

        page++;
      } while (hasNextPage);
    }catch (RuntimeException e) {
      throw e;
    }finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

    return administrators;
  }

  /**
   * Retrieve a GrouperDuoAdministrator object from the API.
   *
   * @param adminId
   * @throws RuntimeException when an unexpected response is received.
   * @return GrouperDuoAdministrator object, or null if no administrator was found
   */
  public static GrouperDuoAdministrator retrieveAdminAccount(String adminId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();

    debugMap.put("method", "retrieveAdminAccount");
    debugMap.put("adminId", adminId);

    GrouperDuoAdministrator administrator = null;

    try {
      Http request = httpAdmin("GET", "/admin/v1/admins/" + adminId);

      signHttpAdmin(request);
      String result = executeRequestRaw(request);
      debugMap.put("result", result);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(result);

      if (jsonObject.containsKey("code") && jsonObject.getInt("code") == 40401) {
        // Log that we got a '40401' code, 'Resource not found'
        // This is different than a 'stat != "OK"' error, we should just return null.
        debugMap.put("response-code", 40401);
      }else if (!jsonObject.getString("stat").equals("OK")) {
        throw new RuntimeException("Received an error response from duo: " + result);
      }else {
          JSONObject response = jsonObject.getJSONObject("response");
        administrator = applyJsonObjectToAdministrator(new GrouperDuoAdministrator(), response);
        GrouperDuoLog.duoLog("JSONOBJECT" + jsonObject.toString());
        GrouperDuoLog.duoLog("RESPONSE" + response.toString());
      }
    }finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

    return administrator;
  }

  public static Http startAdminUpdateRequest(GrouperDuoAdministrator admin) {
    Http request = httpAdmin("POST", "/admin/v1/admins/" + admin.getAdminId());

    return request;
  }

  public static void updateAdminStatus(Http request, boolean isActive) {
    request.addParam("status", isActive ? "Active" : "Disabled");
  }

  public static void updateAdminRole(Http request, String role) {
    request.addParam("role", role == null ? "Read-only" : role);
  }

  public static void updateAdminName(Http request, String name) {
    request.addParam("name", name);
  }

  public static void executeAdminUpdateRequest(GrouperDuoAdministrator admin, Http request) {
    signHttpAdmin(request);
    String result = executeRequestRaw(request);

    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(result);

    if (!jsonObject.getString("stat").equals("OK")) {
      throw new RuntimeException(jsonObject.getString("message"));
    }

    // Update the admin object with the new properties from the API.
    applyJsonObjectToAdministrator(admin, jsonObject.getJSONObject("response"));
  }

  public static void deleteAdminAccount(String adminId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();

    debugMap.put("method", "deleteAdminAccount");
    debugMap.put("adminId", adminId);

    GrouperDuoAdministrator administrator = null;

    try {
      Http request = httpAdmin("DELETE", "/admin/v1/admins/" + adminId);

      signHttpAdmin(request);
      String result = executeRequestRaw(request);
      debugMap.put("result", result);

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(result);

      if (!jsonObject.getString("stat").equals("OK")) {
        throw new RuntimeException("Received an error response from duo: " + result);
      }
    }catch (Exception e) {
    	GrouperDuoLog.logError("Failed to delete Duo Administrator", e);
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

}

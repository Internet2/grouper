package edu.internet2.middleware.grouperRemedy;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.GetMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PutMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * commands against the box api
 */
public class GrouperRemedyCommands {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    Map<Long, GrouperRemedyGroup> allGroupsMap = retrieveRemedyGroups();
    for (Long name : allGroupsMap.keySet()) {
      
      GrouperRemedyGroup grouperRemedyGroup = allGroupsMap.get(name);
      
      System.out.println(name + ": " + grouperRemedyGroup.getPermissionGroup() + ", " + grouperRemedyGroup.getPermissionGroupId());
      for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyGroup.getMemberships()) {
        System.out.println(" - " + grouperRemedyMembership.getRemedyLoginId() + " enabled? " + grouperRemedyMembership.isEnabled());
      }
    }

//    GrouperRemedyGroup grouperRemedyGroup = allGroupsMap.get(2000000001L);
//
//    GrouperRemedyUser grouperBoxUser = retrieveRemedyUser("avekker");
//    
//    System.out.println("assign user to group");
//    
//    grouperRemedyGroup.assignUserToGroup(grouperBoxUser, false);
//
//    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyGroup.getMemberships()) {
//      System.out.println(" - " + grouperRemedyMembership.getRemedyLoginId() + " enabled? " + grouperRemedyMembership.isEnabled());
//    }
//
//    System.out.println("remove user to group");
//
//    grouperRemedyGroup.removeUserFromGroup(grouperBoxUser, false);
//
//    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyGroup.getMemberships()) {
//      System.out.println(" - " + grouperRemedyMembership.getRemedyLoginId() + " enabled? " + grouperRemedyMembership.isEnabled());
//    }
//    
//    System.out.println("add user to group");
//
//    grouperRemedyGroup.assignUserToGroup(grouperBoxUser, false);
//
//    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyGroup.getMemberships()) {
//      System.out.println(" - " + grouperRemedyMembership.getRemedyLoginId() + " enabled? " + grouperRemedyMembership.isEnabled());
//    }
//
//    System.out.println("remove user to group");
//
//    grouperRemedyGroup.removeUserFromGroup(grouperBoxUser, false);
//
//    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyGroup.getMemberships()) {
//      System.out.println(" - " + grouperRemedyMembership.getRemedyLoginId() + " enabled? " + grouperRemedyMembership.isEnabled());
//    }
    
    
//    Map<String, GrouperRemedyUser> allUsersMap = retrieveRemedyUsers();
//    for (String remedyLoginId : allUsersMap.keySet()) {
//      GrouperRemedyUser theGrouperRemedyUser = allUsersMap.get(remedyLoginId);
//      System.out.println(theGrouperRemedyUser.getRemedyLoginId() + ": " + theGrouperRemedyUser.getPersonId());
//    }

//    Map<String, GrouperBoxGroup> allGroupsMap = retrieveBoxGroups();
//    Map<String, GrouperBoxUser> allUsersMap = retrieveUsers();
//    //testGroup, testGroup2, mchyzer@gmail.com, mchyzer@yahoo.com    
//    GrouperBoxGroup grouperBoxGroup = allGroupsMap.get("testGroup3");
//    GrouperBoxUser grouperBoxUser = allUsersMap.get("mchyzer@gmail.com");
//
    
//    System.out.println(BoxUser.getCurrentUser(retrieveBoxApiConnection()));
    
//    grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
//    grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
//    
//    grouperBoxGroup.removeUserFromGroup(grouperBoxUser, false);
//    grouperBoxGroup.removeUserFromGroup(grouperBoxUser, false);

//    createBoxGroup("testGroup3", false);
//    createBoxGroup("testGroup3", false);
//
//    Map<String, GrouperBoxGroup> allGroupsMap = retrieveBoxGroups();
//    GrouperBoxGroup grouperBoxGroup = allGroupsMap.get("testGroup3");
//
//    deleteBoxGroup(grouperBoxGroup, false);
//    deleteBoxGroup(grouperBoxGroup, false);

  }

  /**
   * @return the name of group mapped to group
   */
  public static Map<Long, GrouperRemedyGroup> retrieveRemedyGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyGroups");

    long startTime = System.nanoTime();

    try {

      Map<Long, GrouperRemedyGroup> results = new TreeMap<Long, GrouperRemedyGroup>();
      
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      //paramMap.put("fields", "values(Person ID,Remedy Login ID)");
      
      //JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?fields=values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Status)", paramMap);
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps?fields=values(Status,Permission%20Group,Permission%20Group%20ID)", paramMap);
      
      //  {
      //    "entries": [
      //      {
      //        "values": {
      //          "Status": "Enabled",
      //          "Permission Group": "2000000001",
      //          "Permission Group ID": 2000000001
      //        },
      //        "_links": {
      //          "self": [
      //            {
      //              "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/2000000001"
      //            }
      //          ]
      //        }
      //      },
          
      JSONArray jsonObjectEntries = jsonObject.getJSONArray("entries");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JSONObject jsonObjectUser = jsonObjectEntries.getJSONObject(i);
        
        JSONObject jsonObjectUserValues = jsonObjectUser.getJSONObject("values");
        
        GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();

        {
          String permissionGroup = jsonObjectUserValues.getString("Permission Group");
          grouperRemedyGroup.setPermissionGroup(permissionGroup);
        }
        
        {
          String status = jsonObjectUserValues.getString("Status");
          grouperRemedyGroup.setStatusString(status);
        }
        
        {
          Long permissionGroupId = jsonObjectUserValues.getLong("Permission Group ID");
          grouperRemedyGroup.setPermissionGroupId(permissionGroupId);
        }
                
        results.put(grouperRemedyGroup.getPermissionGroupId(), grouperRemedyGroup);
      }
      
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }

  }


  /**
   * http client
   * @param debugMap
   * @return the http client
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  private static HttpClient httpClient(Map<String, Object> debugMap) {
    
    //see if invalid SSL
    String httpsSocketFactoryName = GrouperClientConfig.retrieveConfig().propertyValueString("remedyGrouperClient.https.customSocketFactory");
    
    //is there overhead here?  should only do this once?
    //perhaps give a custom factory
    if (!GrouperClientUtils.isBlank(httpsSocketFactoryName)) {
      Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = GrouperClientUtils.forName(httpsSocketFactoryName);
      SecureProtocolSocketFactory httpsSocketFactoryInstance = GrouperClientUtils.newInstance(httpsSocketFactoryClass);
      Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
      Protocol.registerProtocol("https", easyhttps);
    }
    
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    int soTimeoutMillis = GrouperClientConfig.retrieveConfig().propertyValueIntRequired(
        "grouperClient.webService.httpSocketTimeoutMillis");
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
    int connectionManagerMillis = GrouperClientConfig.retrieveConfig().propertyValueIntRequired(
        "grouperClient.webService.httpConnectionManagerTimeoutMillis");
    
    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);
    return httpClient;
  }

  /**
   * execute a GET method
   * @param debugMap
   * @param path
   * @param paramMap
   * @return the json object
   */
  private static JSONObject executeGetMethod(Map<String, Object> debugMap, String path, Map<String, String> paramMap) {

    HttpClient httpClient = httpClient(debugMap);

    String jwtToken = retrieveJwtToken(debugMap, httpClient);

    String fullUrl = calculateUrl(path, paramMap);
    GetMethod getMethod = new GetMethod(fullUrl);
    
    getMethod.addRequestHeader("authorization", "AR-JWT " + jwtToken);
    
    int responseCodeInt = -1;
    String body = null;
    long startTime = System.nanoTime();
    try {
      responseCodeInt = httpClient.executeMethod(getMethod);
      
      try {
        body = getMethod.getResponseBodyAsString();
      } catch (Exception e) {
        debugMap.put("getResponseAsStringException", ExceptionUtils.getStackTrace(e));
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
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( body );     

    return jsonObject;
  }
  
  /**
   * get the login token
   * @param debugMap
   * @param httpClient
   * @return the login token
   */
  private static String retrieveJwtToken(Map<String, Object> debugMap, HttpClient httpClient) {
    String username = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("remedyGrouperClient.webService.username");

    boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");
    
    //lets lookup if file
    String wsPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("remedyGrouperClient.webService.password");
    String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

    if (!GrouperClientUtils.equals(wsPass, wsPassFromFile)) {

      String encryptKey = GrouperClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    }

    //login and get a token
    String url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("remedyGrouperClient.webService.url");

    url = GrouperClientUtils.stripEnd(url, "/");
    
    String loginUrl = url + "/api/jwt/login";
    
    //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    PostMethod postMethod = new PostMethod(loginUrl);

    //no keep alive so response is easier to indent for tests
    postMethod.setRequestHeader("Connection", "close");
    
    postMethod.addParameter("username", username);
    postMethod.addParameter("password", wsPass);
    
    int responseCodeInt = -1;
    String jwtToken = null;
    long startTime = System.nanoTime();
    try {
      responseCodeInt = httpClient.executeMethod(postMethod);
      
      try {
        jwtToken = postMethod.getResponseBodyAsString();
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
    
    return jwtToken;
  }
  
  /**
   * @return remedy login id to user never null
   */
  public static Map<MultiKey, GrouperRemedyMembership> retrieveRemedyMemberships() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyMemberships");

    long startTime = System.nanoTime();

    try {

      Map<String, String> paramMap = new HashMap<String, String>();

      paramMap.put("fields", "values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)");
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
      Map<MultiKey, GrouperRemedyMembership> results = convertRemedyMembershipsFromJson(jsonObject);
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  }

  /**
   * @param jsonObject
   * @return map of memberships
   */
  private static Map<MultiKey, GrouperRemedyMembership> convertRemedyMembershipsFromJson(JSONObject jsonObject) {
    
    Map<MultiKey, GrouperRemedyMembership> results = new LinkedHashMap<MultiKey, GrouperRemedyMembership>();
    
    //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "People Permission Group ID": "EPG000000000101",
    //          "Permission Group": "2000000001",
    //          "Permission Group ID": 2000000001,
    //          "Person ID": "PPL000000000616",
    //          "Remedy Login ID": "benoff",
    //          "Status": "Enabled"
    //        },
        
    JSONArray jsonObjectEntries = jsonObject.getJSONArray("entries");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JSONObject jsonObjectUser = jsonObjectEntries.getJSONObject(i);
      
      JSONObject jsonObjectUserValues = jsonObjectUser.getJSONObject("values");
      
      GrouperRemedyMembership grouperRemedyMembership = new GrouperRemedyMembership();

      {
        String personId = jsonObjectUserValues.getString("Person ID");
        grouperRemedyMembership.setPersonId(personId);
      }
      
      {
        String remedyLoginId = jsonObjectUserValues.getString("Remedy Login ID");
        grouperRemedyMembership.setRemedyLoginId(remedyLoginId);
      }
      
      {
        String status = jsonObjectUserValues.getString("Status");
        grouperRemedyMembership.setStatusString(status);
      }

      {
        String peoplePermissionGroupId = jsonObjectUserValues.getString("People Permission Group ID");
        grouperRemedyMembership.setPeoplePermissionGroupId(peoplePermissionGroupId);
      }
      
      {
        String permissionGroup = jsonObjectUserValues.getString("Permission Group");
        grouperRemedyMembership.setPermissionGroup(permissionGroup);
      }
      
      {
        Long permissionGroupId = jsonObjectUserValues.getLong("Permission Group ID");
        grouperRemedyMembership.setPermissionGroupId(permissionGroupId);
      }
      
      MultiKey multiKey = new MultiKey(grouperRemedyMembership.getPeoplePermissionGroupId(), grouperRemedyMembership.getRemedyLoginId());
      
      results.put(multiKey, grouperRemedyMembership);
    }
    
    return results;
  }
    
  /**
   * @return remedy login id to user never null
   */
  public static Map<String, GrouperRemedyUser> retrieveRemedyUsers() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyUsers");

    long startTime = System.nanoTime();

    try {

      Map<String, String> paramMap = new HashMap<String, String>();

      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/CTM:People", paramMap);
      
      Map<String, GrouperRemedyUser> results = convertRemedyUsersFromJson(jsonObject);
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }

  }

  /**
   * @param jsonObject
   * @return the map
   */
  private static Map<String, GrouperRemedyUser> convertRemedyUsersFromJson(JSONObject jsonObject) {
    Map<String, GrouperRemedyUser> results = new TreeMap<String, GrouperRemedyUser>();

    //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "Person ID": "PPL000000000306",
    //          "Remedy Login ID": "foundationdataadmin"
    //        },
    //        "_links": {
    //          "self": [
    //            {
    //              "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306"
    //            }
    //          ]
    //        }
    //      }
    //    ],
    //    "_links": {
    //      "self": [
    //        {
    //          "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People"
    //        }
    //      ]
    //    }
    //  }      

    JSONArray jsonObjectEntries = jsonObject.getJSONArray("entries");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JSONObject jsonObjectUser = jsonObjectEntries.getJSONObject(i);
      
      JSONObject jsonObjectUserValues = jsonObjectUser.getJSONObject("values");
      
      GrouperRemedyUser grouperRemedyUser = new GrouperRemedyUser();

      {
        String personId = jsonObjectUserValues.getString("Person ID");
        grouperRemedyUser.setPersonId(personId);
        
      }
      
      {
        String remedyLoginId = jsonObjectUserValues.getString("Remedy Login ID");

        // not sure why this would happen
        if (GrouperClientUtils.isBlank(remedyLoginId)) {
          continue;
        }
        
        grouperRemedyUser.setRemedyLoginId(remedyLoginId);
        results.put(remedyLoginId, grouperRemedyUser);
      }
      
    }
    return results;
  }

  /**
   * @param loginid
   * @return the user based on loginid
   */
  public static GrouperRemedyUser retrieveRemedyUser(String loginid) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyUser");
    debugMap.put("loginid", loginid);

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Remedy Login ID' = \"" + loginid + "\""));
      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/CTM:People", paramMap);
      
      Map<String, GrouperRemedyUser> results = convertRemedyUsersFromJson(jsonObject);
      
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
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
    
  }

  /**
   * @param grouperRemedyGroup
   * @return the map from username to grouper user object
   */
  public static List<GrouperRemedyMembership> retrieveRemedyMembershipsForGroup(GrouperRemedyGroup grouperRemedyGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveMembershipsForRemedyGroup");
    debugMap.put("group", grouperRemedyGroup.getPermissionGroupId());

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Permission Group ID' = \"" + grouperRemedyGroup.getPermissionGroupId() + "\""));
      paramMap.put("fields", "values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)");
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
      Map<MultiKey, GrouperRemedyMembership> results = GrouperClientUtils.nonNull(convertRemedyMembershipsFromJson(jsonObject));
      
      debugMap.put("size", GrouperClientUtils.length(results));
      
      return new ArrayList<GrouperRemedyMembership>(results.values());
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  }

  /**
   * 
   * @param permissionGroupId 
   * @param netId 
   * @param jsonObjectReturn pass in to get the json object, or null if you dont care
   * @return the membership object if available
   */
  public static GrouperRemedyMembership retrieveRemedyMembership(String permissionGroupId, String netId, JSONObject[] jsonObjectReturn) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveMembershipsForRemedyGroup");
    debugMap.put("groupId", permissionGroupId);
    debugMap.put("netId", netId);

    long startTime = System.nanoTime();

    try {

      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      //https://upenn-dev-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?q=%27Permission+Group+ID%27+%3D+%222000000001%22+and+%27Remedy+Login+ID%27+%3D+%22benoff%22
      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Permission Group ID' = \"" + permissionGroupId + "\""
          + " and 'Remedy Login ID' = \"" + netId + "\""));
      
      paramMap.put("fields", "values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)");
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
      Map<MultiKey, GrouperRemedyMembership> results = GrouperClientUtils.nonNull(convertRemedyMembershipsFromJson(jsonObject));
      
      debugMap.put("size", GrouperClientUtils.length(results));

      if (GrouperClientUtils.length(results) == 0) {
        
        return null;
        
      }
      if (GrouperClientUtils.length(results) == 1) {
        
        if (jsonObjectReturn != null && jsonObjectReturn.length == 1) {
          //  {
          //    "entries": [
          //      {
          //        "values": {
          //          "People Permission Group ID": "EPG000000000101",
          //          "Permission Group": "2000000001",
          //          "Permission Group ID": 2000000001,
          //          "Person ID": "PPL000000000616",
          //          "Remedy Login ID": "benoff",
          //          "Status": "Enabled"
          //        },
          JSONArray jsonObjectEntries = jsonObject.getJSONArray("entries");
          jsonObjectReturn[0] = jsonObjectEntries.getJSONObject(0);
        }
        
        return results.values().iterator().next();
      }
      throw new RuntimeException("Found multiple membership results for permissionGroupId '" + permissionGroupId 
          + "' and loginid '" + netId + "', results: " + GrouperClientUtils.length(results));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }

  }

  /**
   * @param grouperRemedyUser
   * @param grouperRemedyGroup
   * @param isIncremental
   * @return true if added, false if already exists, null if enabled a past disabled memberships
   */
  public static Boolean assignUserToRemedyGroup(GrouperRemedyUser grouperRemedyUser, GrouperRemedyGroup grouperRemedyGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToRemedyGroup");
    debugMap.put("userLoginId", grouperRemedyUser.getRemedyLoginId());
    debugMap.put("permissionGroupId", grouperRemedyGroup.getPermissionGroupId());
    debugMap.put("permissionGroup", grouperRemedyGroup.getPermissionGroup());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    
    JSONObject[] grouperRemedyMembershipJsonObject = new JSONObject[1];
    
    GrouperRemedyMembership grouperRemedyMembership = retrieveRemedyMembership(Long.toString(grouperRemedyGroup.getPermissionGroupId()), grouperRemedyUser.getRemedyLoginId(), grouperRemedyMembershipJsonObject);

    debugMap.put("foundExistingMembership", grouperRemedyMembership != null ? true : false);
    
    long startTime = System.nanoTime();
    try {
      
      if (grouperRemedyMembership != null) {
        
        debugMap.put("existingMembershipEnabled", grouperRemedyMembership.isEnabled());
        
        if (grouperRemedyMembership.isEnabled()) {
          
          return false;
          
        }

        //  PUT /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000101
        //  {
        //    "values": {
        //      "Permission Group ID": 2000000001,
        //      "Permission Group": "2000000001",
        //      "Person ID": "PPL000000000616",
        //      "Remedy Login ID": "benoff",
        //      "Status": "Enabled"
        //    }
        //  }

        //put it back
        grouperRemedyMembershipJsonObject[0].getJSONObject("values").put("Status", "Enabled");
        String peoplePermissionGroupId = grouperRemedyMembershipJsonObject[0].getJSONObject("values").getString("People Permission Group ID");
        
        JSONObject newContainer = new JSONObject();
        newContainer.put("values", grouperRemedyMembershipJsonObject[0].getJSONObject("values"));

        
        debugMap.put("peoplePermissionGroupId", peoplePermissionGroupId);
        executePutPostMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/" + peoplePermissionGroupId, null, newContainer.toString(), true);
        
        return null;
      }
      
      //put a new one
      //  POST /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups
      //  {
      //    "values": {
      //      "Permission Group ID": 2000000001,
      //      "Permission Group": "2000000001",
      //      "Person ID": "PPL000000000616",
      //      "Remedy Login ID": "benoff",
      //      "Status": "Enabled"
      //    }
      //  }

      //put it back
      JSONObject jsonObject = new JSONObject();
      JSONObject valuesObject = new JSONObject();
      valuesObject.put("Permission Group ID", grouperRemedyGroup.getPermissionGroupId());
      valuesObject.put("Permission Group", grouperRemedyGroup.getPermissionGroup());
      valuesObject.put("Person ID", grouperRemedyUser.getPersonId());
      valuesObject.put("Remedy Login ID", grouperRemedyUser.getRemedyLoginId());
      valuesObject.put("Status", "Enabled");
      jsonObject.put("values", valuesObject);
      executePutPostMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", null, jsonObject.toString(), false);

      return true;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  
  }

  /**
   * @param grouperRemedyUser
   * @param grouperRemedyGroup
   * @param isIncremental
   * @return true if disabled, false if already disabled, null if membership never existed
   */
  public static Boolean removeUserFromRemedyGroup(GrouperRemedyUser grouperRemedyUser, GrouperRemedyGroup grouperRemedyGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "removeUserFromRemedyGroup");
    debugMap.put("userLoginId", grouperRemedyUser.getRemedyLoginId());
    debugMap.put("permissionGroupId", grouperRemedyGroup.getPermissionGroupId());
    debugMap.put("permissionGroup", grouperRemedyGroup.getPermissionGroup());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    
    JSONObject[] grouperRemedyMembershipJsonObject = new JSONObject[1];
    
    GrouperRemedyMembership grouperRemedyMembership = retrieveRemedyMembership(Long.toString(grouperRemedyGroup.getPermissionGroupId()), grouperRemedyUser.getRemedyLoginId(), grouperRemedyMembershipJsonObject);

    debugMap.put("foundExistingMembership", grouperRemedyMembership != null ? true : false);
    
    long startTime = System.nanoTime();
    try {
      
      if (grouperRemedyMembership != null) {
        
        debugMap.put("existingMembershipEnabled", grouperRemedyMembership.isEnabled());
        
        if (!grouperRemedyMembership.isEnabled()) {
          
          return false;
          
        }

        //  PUT /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000101
        //  {
        //    "values": {
        //      "Permission Group ID": 2000000001,
        //      "Permission Group": "2000000001",
        //      "Person ID": "PPL000000000616",
        //      "Remedy Login ID": "benoff",
        //      "Status": "Delete"
        //    }
        //  }

        //put it back
        grouperRemedyMembershipJsonObject[0].getJSONObject("values").put("Status", "Delete");
        String peoplePermissionGroupId = grouperRemedyMembershipJsonObject[0].getJSONObject("values").getString("People Permission Group ID");
        debugMap.put("peoplePermissionGroupId", peoplePermissionGroupId);
        JSONObject newContainer = new JSONObject();
        newContainer.put("values", grouperRemedyMembershipJsonObject[0].getJSONObject("values"));
        executePutPostMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/" + peoplePermissionGroupId, null, newContainer.toString(), true);
        
        return true;
      }
      
      // didnt exist
      return null;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  
  }

  /**
   * execute a GET method
   * @param debugMap
   * @param path
   * @param paramMap
   * @param requestBody 
   * @param isPutNotPost 
   * @return the json object
   */
  private static JSONObject executePutPostMethod(Map<String, Object> debugMap, String path, Map<String, String> paramMap, String requestBody, boolean isPutNotPost) {
  
    HttpClient httpClient = httpClient(debugMap);
  
    String jwtToken = retrieveJwtToken(debugMap, httpClient);
  
    String fullUrl = calculateUrl(path, paramMap);
    EntityEnclosingMethod putPostMethod = isPutNotPost ? new PutMethod(fullUrl) : new PostMethod(fullUrl);
    
    debugMap.put(isPutNotPost ? "put" : "post", true);
    debugMap.put("requestBody", requestBody);
    putPostMethod.addRequestHeader("authorization", "AR-JWT " + jwtToken);
    
    if (!GrouperClientUtils.isBlank(requestBody)) {
      String contentType = "application/json";
      String charset = "utf-8";
      try {
        putPostMethod.setRequestEntity(new StringRequestEntity(requestBody, contentType, charset));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(contentType + ", " + charset, e);
      }
    }
    
    int responseCodeInt = -1;
    String responseBody = null;
    long startTime = System.nanoTime();
    try {
      responseCodeInt = httpClient.executeMethod(putPostMethod);
      
      try {
        responseBody = putPostMethod.getResponseBodyAsString();
      } catch (Exception e) {
        debugMap.put("getResponseAsStringException", ExceptionUtils.getStackTrace(e));
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
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( responseBody );     
  
    return jsonObject;
  }

  /**
   * @param path
   * @param paramMap
   * @return the url
   */
  private static String calculateUrl(String path, Map<String, String> paramMap) {
    String url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("remedyGrouperClient.webService.url");
  
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

}

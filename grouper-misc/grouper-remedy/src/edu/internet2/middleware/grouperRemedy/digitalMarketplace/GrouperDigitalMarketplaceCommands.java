package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.DeleteMethod;
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
public class GrouperDigitalMarketplaceCommands {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
//    deleteDigitalMarketplaceGroup("pg_ISC_staff", false);
//    deleteDigitalMarketplaceGroup("pg_IT_staff", false);
//    deleteDigitalMarketplaceGroup("pg_data_center_hosting_clients", false);
//    deleteDigitalMarketplaceGroup("pg_hire_IT_clients", false);
//    deleteDigitalMarketplaceGroup("pg_penn_community_admins", false);
//    deleteDigitalMarketplaceGroup("pg_telephone_support_providers", false);
//    deleteDigitalMarketplaceGroup("pg_test2", false);
//    deleteDigitalMarketplaceGroup("pg_test", false);
    
    
//    Map<String, GrouperDigitalMarketplaceGroup> mapNameToGroup = retrieveDigitalMarketplaceGroups();
//    for (GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup : mapNameToGroup.values()) {
//      if (grouperDigitalMarketplaceGroup.getGroupName().startsWith("pg_")) {
//        System.out.println(grouperDigitalMarketplaceGroup.getGroupName() + " #### " + grouperDigitalMarketplaceGroup.getLongGroupName());
//        for (GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser : grouperDigitalMarketplaceGroup.getMemberUsers().values()) {
//          System.out.println(grouperDigitalMarketplaceGroup.getGroupName() + " #### " + grouperDigitalMarketplaceUser.getLoginName());
//        }
//      }
//    }

//    Map<String, GrouperDigitalMarketplaceGroup> mapNameToGroup = retrieveDigitalMarketplaceGroups();
//    for (GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup : mapNameToGroup.values()) {
//      System.out.println(grouperDigitalMarketplaceGroup.getGroupName() + " #### " + grouperDigitalMarketplaceGroup.getLongGroupName());
//    }

    
//    createDigitalMarketplaceGroup("pg_test2", "pg_test2", "comments", false);
    
    Map<String, GrouperDigitalMarketplaceUser> mapNameToUser = retrieveDigitalMarketplaceUsers();
    for (GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser : mapNameToUser.values()) {
      System.out.println(grouperDigitalMarketplaceUser);
    }
    
//    GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser("cipollad");
//    System.out.println(grouperDigitalMarketplaceUser.getLoginName());
//    for (String groupName : grouperDigitalMarketplaceUser.getGroups()) {
//      System.out.println(groupName);
//    }

//    GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser("adrianj");
//    System.out.println(grouperDigitalMarketplaceUser.getLoginName());
//    for (String groupName : grouperDigitalMarketplaceUser.getGroups()) {
//      System.out.println(groupName);
//    }

//    Map<String, GrouperDigitalMarketplaceGroup> mapNameToGroup = retrieveDigitalMarketplaceGroups();
//    GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = mapNameToGroup.get("sbe-asset-managers");
//    GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser("adrianj");
//    assignUserToDigitalMarketplaceGroup(grouperDigitalMarketplaceUser, grouperDigitalMarketplaceGroup, true);
//    removeUserFromDigitalMarketplaceGroup(grouperDigitalMarketplaceUser, grouperDigitalMarketplaceGroup, true);
    
  }

  /**
   * @return remedy login id to user never null
   */
  public static Map<String, GrouperDigitalMarketplaceUser> retrieveDigitalMarketplaceUsers() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDigitalMarketplaceUsers");

    long startTime = System.nanoTime();

    Map<String, GrouperDigitalMarketplaceUser> results = new LinkedHashMap<String, GrouperDigitalMarketplaceUser>();
    
    try {

      int pageSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperDigitalMarketplace.usersPageSize", 2000);
      
      for (int i=0;i<6000;i++) {
      
        Map<String, GrouperDigitalMarketplaceUser> localResults = retrieveDigitalMarketplaceUsersHelper(pageSize, 0+results.size());
        
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
   * @param pageSize 
   * @param startIndex 
   * @return remedy login id to user never null
   */
  private static Map<String, GrouperDigitalMarketplaceUser> retrieveDigitalMarketplaceUsersHelper(int pageSize, int startIndex) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveDigitalMarketplaceUsersHelper");

    long startTime = System.nanoTime();

    try {
    
      Map<String, String> paramMap = new HashMap<String, String>();
  
      paramMap.put("dataPageType", "com.bmc.arsys.rx.application.user.datapage.UserDataPageQuery");
      paramMap.put("pageSize", "" + pageSize);
      paramMap.put("startIndex", "" + startIndex);
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/rx/application/datapage", paramMap);
      
      Map<String, GrouperDigitalMarketplaceUser> results = convertMarketplaceUsersFromJson(jsonObject);
      debugMap.put("totalSize", jsonObject.getString("totalSize"));
      
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
   * @param loginid
   * @return the user based on loginid
   */
  public static GrouperDigitalMarketplaceUser retrieveDigitalMarketplaceUser(String loginid) {
    
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

      JSONObject jsonObject = executeGetMethod(debugMap, "/api/rx/application/datapage", paramMap);

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
   * cache tokens
   */
  private static ExpirableCache<Boolean, String> retrieveJwtTokenCache = new ExpirableCache<Boolean, String>(5);

  /**
   * get the login token
   * @param debugMap
   * @param httpClient
   * @return the login token
   */
  private static String retrieveJwtToken(Map<String, Object> debugMap, HttpClient httpClient) {
    
    String jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);

    if (GrouperClientUtils.isBlank(jwtToken)) {
      
      synchronized (retrieveJwtTokenCache) {
        jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);
        if (GrouperClientUtils.isBlank(jwtToken)) {
          String username = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperDigitalMarketplace.user");
        
          boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
              "encrypt.disableExternalFileLookup");
          
          //lets lookup if file
          String wsPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperDigitalMarketplace.pass");
          String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);
        
          if (!GrouperClientUtils.equals(wsPass, wsPassFromFile)) {
        
            String encryptKey = GrouperClientUtils.encryptKey();
            wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
            
          }
        
          //login and get a token
          String url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperDigitalMarketplace.url");
        
          url = GrouperClientUtils.stripEnd(url, "/");
          
          String loginUrl = url + "/api/myit-sb/users/login";
          
          //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
          //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
          PostMethod postMethod = new PostMethod(loginUrl);
        
          //no keep alive so response is easier to indent for tests
          postMethod.setRequestHeader("Connection", "close");
          postMethod.setRequestHeader("Content-Type", "application/json");
          postMethod.setRequestHeader("X-Requested-By", username);
          
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("id", username);
          jsonObject.put("password", wsPass);
          
          String postBody = jsonObject.toString();
          
          String contentType = "application/json";
          String charset = "utf-8";
          try {
            postMethod.setRequestEntity(new StringRequestEntity(postBody, contentType, charset));
          } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(contentType + ", " + charset, e);
          }

          int responseCodeInt = -1;

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
          
          retrieveJwtTokenCache.put(Boolean.TRUE, jwtToken);
      
        }
      }
    }
    return jwtToken;
  }

  /**
   * @param path
   * @param paramMap
   * @return the url
   */
  private static String calculateUrl(String path, Map<String, String> paramMap) {
    String url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperDigitalMarketplace.url");
  
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
  private static Map<String, GrouperDigitalMarketplaceUser> convertMarketplaceUsersFromJson(JSONObject jsonObject) {
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

    JSONArray jsonObjectEntries = jsonObject.getJSONArray("data");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JSONObject jsonObjectUser = jsonObjectEntries.getJSONObject(i);
      
      GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = new GrouperDigitalMarketplaceUser();

      // store this so we can post it back later...
      grouperDigitalMarketplaceUser.setJsonObject(jsonObjectUser);
      
      {
        String userId = jsonObjectUser.getString("userId");
  
        // not sure why this would happen
        if (GrouperClientUtils.isBlank(userId)) {
          continue;
        }
        
        grouperDigitalMarketplaceUser.setUserId(userId);
      }
      
      {
        String loginName = jsonObjectUser.getString("loginName");
        // not sure why this would happen
        if (GrouperClientUtils.isBlank(loginName)) {
          continue;
        }

        grouperDigitalMarketplaceUser.setLoginName(loginName);
        
        results.put(loginName, grouperDigitalMarketplaceUser);
      }
      
      {
        JSONArray groupsArray = jsonObjectUser.getJSONArray("groups");
        for (int j=0; j < groupsArray.size(); j++) {
          String groupExtension = groupsArray.getString(j);
          grouperDigitalMarketplaceUser.getGroups().add(groupExtension);
        }
      }
      
    }
    return results;
  }

  /**
   * @return the name of group extension mapped to group
   */
  public static Map<String, GrouperDigitalMarketplaceGroup> retrieveDigitalMarketplaceGroups() {
    
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
      
      JSONObject jsonObject = executeGetMethod(debugMap, "/api/rx/application/datapage", paramMap);
      
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
      
      JSONArray jsonObjectEntries = jsonObject.getJSONArray("data");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JSONObject jsonObjectGroup = jsonObjectEntries.getJSONObject(i);
        
        GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = new GrouperDigitalMarketplaceGroup();
  
        {
          String groupName = jsonObjectGroup.getString("groupName");
          grouperDigitalMarketplaceGroup.setGroupName(groupName);
        }
        
        {
          String comments = jsonObjectGroup.getString("comments");
          grouperDigitalMarketplaceGroup.setComments(comments);
        }
        
        {
          String groupType = jsonObjectGroup.getString("groupType");
          grouperDigitalMarketplaceGroup.setGroupType(groupType);
        }

        {
          String longGroupName = jsonObjectGroup.getString("longGroupName");
          grouperDigitalMarketplaceGroup.setLongGroupName(longGroupName);
        }

        {
          // note: this might be blank: com.bmc.arsys.rx.services.group.domain.RegularGroup
          if (jsonObjectGroup.has("resourceType")) {
            String resourceType = jsonObjectGroup.getString("resourceType");
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
   * @param grouperDigitalMarketplaceUser
   * @param grouperDigitalMarketplaceGroup
   * @param isIncremental
   * @return true if added, false if already exists
   */
  public static Boolean assignUserToDigitalMarketplaceGroup(GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToDigitalMarketplaceGroup");
    debugMap.put("loginName", grouperDigitalMarketplaceUser.getLoginName());
    debugMap.put("groupName", grouperDigitalMarketplaceGroup.getGroupName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");

    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser(grouperDigitalMarketplaceUser.getLoginName());
      
      // restart timer
      startTime = System.nanoTime();
      
      if (!GrouperClientUtils.nonNull(grouperDigitalMarketplaceUser.getGroups()).contains(grouperDigitalMarketplaceGroup.getGroupName())) {
        debugMap.put("foundExistingMembership", false);
        JSONObject jsonObject = grouperDigitalMarketplaceUser.getJsonObject();
        
//        {  
//          "groups":[
//             "sbe-myit-users",
//             "University of Pennsylvania",
//             "University of Pennsylvania - 91-Information Systems and Computing",
//             "University of Pennsylvania - 91-Information Systems and Computing - 9166-ISC-Tech Services-Network Operations"
//          ]
//       }
        
        JSONArray groups = jsonObject.getJSONArray("groups");

        removeNonExistentGroups(groups, debugMap);

        JSONObject userWithGroupsJson = new JSONObject();
        JSONArray groupsJson = new JSONArray();

        for (int i=0;i<groups.size();i++) {
          groupsJson.add(groups.get(i));
        }
        groupsJson.add(grouperDigitalMarketplaceGroup.getGroupName());
        
        userWithGroupsJson.put("groups", groupsJson);
        
        String userJson = userWithGroupsJson.toString();
        
        // /api/rx/application/user/Allen
        executePutPostMethod(debugMap, "/api/rx/application/user/" + grouperDigitalMarketplaceUser.getLoginName(), null, userJson, true);
        
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
    //debugMap.put("requestBody", requestBody);
    putPostMethod.addRequestHeader("authorization", "AR-JWT " + jwtToken);
    putPostMethod.addRequestHeader("Content-Type", "application/json");
    putPostMethod.addRequestHeader("X-Requested-By", "hannah_admin@upenn-qa-mtvip.onbmc.com");
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
   * 
   * @param groups
   * @param debugMap 
   */
  private static void removeNonExistentGroups(JSONArray groups, Map<String, Object> debugMap) {

    if (groups == null || groups.size() == 0) {
      return;
    }

    //get all groupNames
    Set<String> groupNames = new LinkedHashSet<String>();
    for (int i=0;i<groups.size();i++) {
      String groupName = groups.getString(i);
      groupNames.add(groupName);
    }
    
    for (String groupName : groupNames) {
      if (!GrouperDigitalMarketplaceGroup.retrieveGroups().containsKey(groupName)) {
        groups.remove(groupName);
        debugMap.put("removeGroup_" + groupName, true);
      }
    }
  }
  
  /**
   * @param grouperDigitalMarketplaceUser
   * @param grouperDigitalMarketplaceGroup
   * @param isIncremental
   * @return true if removed, false if not in there
   */
  public static Boolean removeUserFromDigitalMarketplaceGroup(GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "removeUserFromDigitalMarketplaceGroup");
    debugMap.put("loginName", grouperDigitalMarketplaceUser.getLoginName());
    debugMap.put("groupName", grouperDigitalMarketplaceGroup.getGroupName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
  
    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      grouperDigitalMarketplaceUser = retrieveDigitalMarketplaceUser(grouperDigitalMarketplaceUser.getLoginName());
      
      // restart timer
      startTime = System.nanoTime();
      
      JSONObject jsonObject = grouperDigitalMarketplaceUser.getJsonObject();
      JSONArray groups = jsonObject.getJSONArray("groups");
      
      removeNonExistentGroups(groups, debugMap);
      
      int groupIndex = -1;
      
      for (int i=0;i<groups.size();i++) {
        if (GrouperClientUtils.equals(grouperDigitalMarketplaceGroup.getGroupName(), groups.getString(i))) {
          groupIndex = i;
          break;
        }
      }
      
      if (groupIndex != -1) {
        
        groups.remove(groupIndex);

        JSONObject userWithGroupsJson = new JSONObject();
        JSONArray groupsJson = new JSONArray();

        for (int i=0;i<groups.size();i++) {
          groupsJson.add(groups.get(i));
        }
        
        userWithGroupsJson.put("groups", groupsJson);
        
        String userJson = userWithGroupsJson.toString();
        
        debugMap.put("foundExistingMembership", true);
        
        // /api/rx/application/user/Allen
        executePutPostMethod(debugMap, "/api/rx/application/user/" + grouperDigitalMarketplaceUser.getLoginName(), null, userJson, true);
        
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
   * @param groupName 
   * @param longGroupName 
   * @param comments 
   * @param isIncremental
   * @return true if added, false if already exists
   */
  public static Boolean createDigitalMarketplaceGroup(String groupName, String longGroupName, String comments, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "createDigitalMarketplaceGroup");
    debugMap.put("groupName", groupName);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");

    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroupExisting = retrieveDigitalMarketplaceGroups().get(groupName);
      
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
        
        JSONObject groupJsonObject = new JSONObject();

        groupJsonObject.put("resourceType", "com.bmc.arsys.rx.services.group.domain.RegularGroup");
        groupJsonObject.put("groupName", groupName);
        if (GrouperClientUtils.isBlank(longGroupName)) {
          longGroupName = groupName;
        }
        groupJsonObject.put("longGroupName", longGroupName);
        groupJsonObject.put("groupType", "Change");
        if (!GrouperClientUtils.isBlank(comments)) {
          groupJsonObject.put("comments", comments);
        }
        groupJsonObject.put("status", "Current");
        JSONArray tagsArray = new JSONArray();
        tagsArray.add("virtualmarketplace");
        groupJsonObject.put("tags", tagsArray );
        
        String groupJson = groupJsonObject.toString();
        
        // /api/rx/application/user/Allen
        executePutPostMethod(debugMap, "/api/rx/application/group/", null, groupJson, false);

        GrouperDigitalMarketplaceGroup.clearGroupCache();

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
   * @param groupName 
   * @param isIncremental
   * @return true if added, false if already exists
   */
  public static Boolean deleteDigitalMarketplaceGroup(String groupName, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteDigitalMarketplaceGroup");
    debugMap.put("groupName", groupName);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
  
    long startTime = System.nanoTime();
    
    try {
  
      // refresh the user object
      GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroupExisting = retrieveDigitalMarketplaceGroups().get(groupName);
      
      // restart timer
      startTime = System.nanoTime();
      
      if (grouperDigitalMarketplaceGroupExisting != null) {
        debugMap.put("foundExistingGroup", true);
        
        // /api/rx/application/user/Allen
        executeDeleteMethod(debugMap, "/api/rx/application/group/" + groupName, null);

        GrouperDigitalMarketplaceGroup.clearGroupCache();

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
   * @param debugMap
   * @param path
   * @param paramMap
   * @return the json object
   */
  private static JSONObject executeDeleteMethod(Map<String, Object> debugMap, String path, Map<String, String> paramMap) {
  
    HttpClient httpClient = httpClient(debugMap);
  
    String jwtToken = retrieveJwtToken(debugMap, httpClient);
  
    String fullUrl = calculateUrl(path, paramMap);
    DeleteMethod deleteMethod = new DeleteMethod(fullUrl);
    
    debugMap.put("delete", true);

    //debugMap.put("requestBody", requestBody);
    deleteMethod.addRequestHeader("authorization", "AR-JWT " + jwtToken);
    deleteMethod.addRequestHeader("Content-Type", "application/json");
    deleteMethod.addRequestHeader("X-Requested-By", "hannah_admin@upenn-qa-mtvip.onbmc.com");
    
    int responseCodeInt = -1;
    String responseBody = null;
    long startTime = System.nanoTime();
    try {
      responseCodeInt = httpClient.executeMethod(deleteMethod);
      
      try {
        responseBody = deleteMethod.getResponseBodyAsString();
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
      throw new RuntimeException("get didnt return 204, it returned: " + responseCodeInt + "," + responseBody);
    }
  
    // hmmm, no body
    if (GrouperClientUtils.isBlank(responseBody)) {
      return null;
    }
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( responseBody );     
  
    return jsonObject;
  }

}

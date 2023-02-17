package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.StringUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.exception.ExceptionUtils;

public class GrouperRemedyApiCommands {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperRemedyApiCommands.class);
  
  
  /**
   * @param remedyExternalSystemConfigId
   * @return the name of group mapped to group
   */
  public static Map<Long, GrouperRemedyGroup> retrieveRemedyGroups(String remedyExternalSystemConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyGroups");

    long startTime = System.nanoTime();

    try {

      Map<Long, GrouperRemedyGroup> results = new TreeMap<Long, GrouperRemedyGroup>();
      
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      //paramMap.put("fields", "values(Person ID,Remedy Login ID)");
      
      //JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?fields=values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Status)", paramMap);
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps?fields=values(Status,Permission%20Group,Permission%20Group%20ID)", paramMap);
      
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
          
      ArrayNode jsonObjectEntries = (ArrayNode)jsonObject.get("entries");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JsonNode jsonObjectUser = jsonObjectEntries.get(i);
        
        JsonNode jsonObjectUserValues = jsonObjectUser.get("values");
        
        GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();

        {
          String permissionGroup = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Permission Group");
          grouperRemedyGroup.setPermissionGroup(permissionGroup);
        }
        
        {
          String status = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Status");
          if (!StringUtils.equals("Enabled", status)) {
            continue;
          }
        }
        
        {
          Long permissionGroupId = GrouperUtil.longObjectValue(GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Permission Group ID"), true);
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
   * execute a GET method
   * @param debugMap
   * @param remedyExternalSystemConfigId
   * @param path
   * @param paramMap
   * @return the json object
   */
  private static JsonNode executeGetMethod(Map<String, Object> debugMap, 
      String remedyExternalSystemConfigId,
      String path, Map<String, String> paramMap) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    String jwtToken = retrieveJwtToken(remedyExternalSystemConfigId, debugMap);

    String fullUrl = calculateUrl(remedyExternalSystemConfigId, path, paramMap);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    grouperHttpClient.assignUrl(fullUrl);
    
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
        debugMap.put("getResponseBodyException", ExceptionUtils.getStackTrace(e));
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
   * @param remedyExternalSystemConfigId
   * @param debugMap
   * @return the login token
   */
  private static String retrieveJwtToken(String remedyExternalSystemConfigId, Map<String, Object> debugMap) {
    
    String jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);

    if (GrouperClientUtils.isBlank(jwtToken)) {
      
      synchronized (retrieveJwtTokenCache) {
        jwtToken = retrieveJwtTokenCache.get(Boolean.TRUE);
        if (GrouperClientUtils.isBlank(jwtToken)) {
          String username = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+remedyExternalSystemConfigId+".username");
          
          String password = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+remedyExternalSystemConfigId+".password");
      
          //login and get a token
          String loginUrl = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+remedyExternalSystemConfigId+".tokenUrl");
      
          GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
          
          //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
          //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
          grouperHttpClient.assignUrl(loginUrl);
          grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      
          //no keep alive so response is easier to indent for tests
          grouperHttpClient.addHeader("Connection", "close");
          
          grouperHttpClient.addBodyParameter("username", username);
          grouperHttpClient.addBodyParameter("password", password);
          
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
   * @param remedyExternalSystemConfigId
   * @return remedy login id to user never null
   */
  public static Map<MultiKey, GrouperRemedyMembership> retrieveRemedyMemberships(String remedyExternalSystemConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyMemberships");

    long startTime = System.nanoTime();

    try {

      Map<String, String> paramMap = new HashMap<String, String>();

      paramMap.put("fields", "values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)");
      
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
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
  private static Map<MultiKey, GrouperRemedyMembership> convertRemedyMembershipsFromJson(JsonNode jsonObject) {
    
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
        
    ArrayNode jsonObjectEntries = (ArrayNode)jsonObject.get("entries");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JsonNode jsonObjectUser = jsonObjectEntries.get(i);
      
      JsonNode jsonObjectUserValues = jsonObjectUser.get("values");
      
      GrouperRemedyMembership grouperRemedyMembership = new GrouperRemedyMembership();

      {
        String personId = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Person ID");
        grouperRemedyMembership.setPersonId(personId);
      }
      
      {
        String remedyLoginId = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Remedy Login ID");
        grouperRemedyMembership.setRemedyLoginId(remedyLoginId);
      }
      
      {
        String status = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Status");
        grouperRemedyMembership.setStatus(status);
      }

      {
        String peoplePermissionGroupId = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "People Permission Group ID");
        grouperRemedyMembership.setPeoplePermissionGroupId(peoplePermissionGroupId);
      }
      
      {
        String permissionGroup = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Permission Group");
        grouperRemedyMembership.setPermissionGroup(permissionGroup);
      }
      
      {
        Long permissionGroupId = GrouperUtil.longObjectValue(GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Permission Group ID"), true);
        grouperRemedyMembership.setPermissionGroupId(permissionGroupId);
      }
      
      MultiKey multiKey = new MultiKey(grouperRemedyMembership.getPeoplePermissionGroupId(), grouperRemedyMembership.getRemedyLoginId());
      
      results.put(multiKey, grouperRemedyMembership);
    }
    
    return results;
  }
    
  /**
   * @param remedyExternalSystemConfigId
   * @return remedy login id to user never null
   */
  public static Map<String, GrouperRemedyUser> retrieveRemedyUsers(String remedyExternalSystemConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyUsers");

    long startTime = System.nanoTime();

    try {

      Map<String, String> paramMap = new HashMap<String, String>();

      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
      
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/CTM:People", paramMap);
      
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
  private static Map<String, GrouperRemedyUser> convertRemedyUsersFromJson(JsonNode jsonObject) {
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

    ArrayNode jsonObjectEntries = (ArrayNode)jsonObject.get("entries");
    for (int i=0; i < jsonObjectEntries.size(); i++) {
      
      JsonNode jsonObjectUser = jsonObjectEntries.get(i);
      
      JsonNode jsonObjectUserValues = jsonObjectUser.get("values");
      
      GrouperRemedyUser grouperRemedyUser = new GrouperRemedyUser();

      {
        String personId = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Person ID"); 
        grouperRemedyUser.setPersonId(personId);
        
      }
      
      {
        String remedyLoginId = GrouperUtil.jsonJacksonGetString(jsonObjectUserValues, "Remedy Login ID");

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
   * @param remedyExternalSystemConfigId
   * @param loginid
   * @return the user based on loginid
   */
  public static GrouperRemedyUser retrieveRemedyUser(String remedyExternalSystemConfigId, String loginid) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyUser");
    debugMap.put("loginid", loginid);

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Remedy Login ID' = \"" + loginid + "\""));
      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
      
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/CTM:People", paramMap);
      
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
   * @param remedyExternalSystemConfigId
   * @param permissionGroupId
   * @return the group based on permission group id
   */
  public static GrouperRemedyGroup retrieveRemedyGroup(String remedyExternalSystemConfigId, Long permissionGroupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRemedyGroup");
    debugMap.put("permissionGroupId", permissionGroupId);

    long startTime = System.nanoTime();

    Map<Long, GrouperRemedyGroup> results = new TreeMap<Long, GrouperRemedyGroup>();
    
    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
//      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Remedy Login ID' = \"" + permissionGroupId + "\""));
//      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
      // https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/2000000001
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/"+permissionGroupId, paramMap);
      
      ArrayNode jsonObjectEntries = (ArrayNode)jsonObject.get("entries");
      for (int i=0; i < jsonObjectEntries.size(); i++) {
        
        JsonNode jsonObjectGroup = jsonObjectEntries.get(i);
        
        JsonNode jsonObjectGroupValues = jsonObjectGroup.get("values");
        
        GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();

        {
          String permissionGroup = GrouperUtil.jsonJacksonGetString(jsonObjectGroupValues, "Permission Group");
          grouperRemedyGroup.setPermissionGroup(permissionGroup);
        }
        
        {
          String status = GrouperUtil.jsonJacksonGetString(jsonObjectGroupValues, "Status");
          if (!StringUtils.equals(status, "Enabled")) {
            continue;
          }
        }
        
        {
          Long permissionGroupId1 = GrouperUtil.longObjectValue(GrouperUtil.jsonJacksonGetString(jsonObjectGroupValues, "Permission Group ID"), true);
          grouperRemedyGroup.setPermissionGroupId(permissionGroupId1);
        }
                
        results.put(grouperRemedyGroup.getPermissionGroupId(), grouperRemedyGroup);
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      if (GrouperClientUtils.length(results) == 0) {
        
        return null;
        
      }
      if (GrouperClientUtils.length(results) == 1) {
        return results.values().iterator().next();
      }
      throw new RuntimeException("Found multiple results for loginid '" + permissionGroupId + "', results: " + GrouperClientUtils.length(results));
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
    
  }

  /**
   * @param remedyExternalSystemConfigId
   * @param grouperRemedyGroup
   * @return the map from username to grouper user object
   */
  public static List<GrouperRemedyMembership> retrieveRemedyMembershipsForGroup(
      String remedyExternalSystemConfigId,
      GrouperRemedyGroup grouperRemedyGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveMembershipsForRemedyGroup");
    debugMap.put("group", grouperRemedyGroup.getPermissionGroupId());

    long startTime = System.nanoTime();

    try {
  
      Map<String, String> paramMap = new HashMap<String, String>();

      //doesnt work since the url shouldnt be encoded
      paramMap.put("q", GrouperRemedyUtils.escapeUrlEncode("'Permission Group ID' = \"" + grouperRemedyGroup.getPermissionGroupId() + "\""));
      paramMap.put("fields", "values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)");
      
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
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
   * @param remedyExternalSystemConfigId
   * @param permissionGroupId 
   * @param netId 
   * @param jsonObjectReturn pass in to get the json object, or null if you dont care
   * @return the membership object if available
   */
  public static GrouperRemedyMembership retrieveRemedyMembership(String remedyExternalSystemConfigId, String permissionGroupId, String netId, JsonNode[] jsonObjectReturn) {

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
      
      JsonNode jsonObject = executeGetMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", paramMap);
      
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
          ArrayNode jsonObjectEntries = (ArrayNode)jsonObject.get("entries");
          jsonObjectReturn[0] = jsonObjectEntries.get(0);
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
   * @param remedyExternalSystemConfigId
   * @param grouperRemedyUser
   * @param grouperRemedyGroup
   * @return true if added, false if already exists, null if enabled a past disabled memberships
   */
  public static Boolean assignUserToRemedyGroup(String remedyExternalSystemConfigId,
      String remedyLoginId, String personId, String permissionGroup, Long permissionGroupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToRemedyGroup");
    debugMap.put("remedyLoginId", remedyLoginId);
    debugMap.put("permissionGroupId", permissionGroupId);
    debugMap.put("permissionGroup", permissionGroup);
    debugMap.put("personId", personId);
    
    JsonNode[] grouperRemedyMembershipJsonObject = new JsonNode[1];
    
    GrouperRemedyMembership grouperRemedyMembership = retrieveRemedyMembership(remedyExternalSystemConfigId,
        String.valueOf(permissionGroupId),
        remedyLoginId, 
        grouperRemedyMembershipJsonObject);

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
        ObjectNode jsonNode = (ObjectNode)grouperRemedyMembershipJsonObject[0];
        JsonNode valuesJsonNode = jsonNode.get("values");
        ((ObjectNode)valuesJsonNode).put("Status", "Enabled");
        String peoplePermissionGroupId = GrouperUtil.jsonJacksonGetString(valuesJsonNode, "People Permission Group ID");
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        ObjectNode newContainer = objectMapper.createObjectNode();
        newContainer.set("values", valuesJsonNode);

        debugMap.put("peoplePermissionGroupId", peoplePermissionGroupId);
        executePutPostMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/" + peoplePermissionGroupId, null, newContainer.toString(), true);
        
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
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode jsonObject = objectMapper.createObjectNode();
      ObjectNode valuesObject = objectMapper.createObjectNode();
      valuesObject.put("Permission Group ID", permissionGroupId);
      valuesObject.put("Permission Group", permissionGroup);
      valuesObject.put("Person ID", personId);
      valuesObject.put("Remedy Login ID", remedyLoginId);
      valuesObject.put("Status", "Enabled");
      jsonObject.set("values", valuesObject);
      executePutPostMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups", null, jsonObject.toString(), false);

      return true;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  
  }

  /**
   * @param remedyExternalSystemConfigId
   * @param grouperRemedyUser
   * @param grouperRemedyGroup
   * @return true if disabled, false if already disabled, null if membership never existed
   */
  public static Boolean removeUserFromRemedyGroup(
      String remedyExternalSystemConfigId,
      GrouperRemedyUser grouperRemedyUser, GrouperRemedyGroup grouperRemedyGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "removeUserFromRemedyGroup");
    debugMap.put("userLoginId", grouperRemedyUser.getRemedyLoginId());
    debugMap.put("permissionGroupId", grouperRemedyGroup.getPermissionGroupId());
    debugMap.put("permissionGroup", grouperRemedyGroup.getPermissionGroup());
    
    JsonNode[] grouperRemedyMembershipJsonObject = new JsonNode[1];
    
    GrouperRemedyMembership grouperRemedyMembership = retrieveRemedyMembership(
        remedyExternalSystemConfigId, 
        Long.toString(grouperRemedyGroup.getPermissionGroupId()), grouperRemedyUser.getRemedyLoginId(), grouperRemedyMembershipJsonObject);

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
        JsonNode valuesJsonNode = grouperRemedyMembershipJsonObject[0].get("values");
        ((ObjectNode)valuesJsonNode).put("Status", "Delete");
        String peoplePermissionGroupId = GrouperUtil.jsonJacksonGetString(valuesJsonNode, "People Permission Group ID");
        debugMap.put("peoplePermissionGroupId", peoplePermissionGroupId);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode newContainer = objectMapper.createObjectNode();
        newContainer.set("values", valuesJsonNode);
        executePutPostMethod(debugMap, remedyExternalSystemConfigId, "/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/" + peoplePermissionGroupId, null, newContainer.toString(), true);
        
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
   * execute a PUT|POST method
   * @param debugMap
   * @param remedyExternalSystemConfigId
   * @param path
   * @param paramMap
   * @param requestBody 
   * @param isPutNotPost 
   * @return the json object
   */
  private static JsonNode executePutPostMethod(Map<String, Object> debugMap, String remedyExternalSystemConfigId, String path, Map<String, String> paramMap, String requestBody, boolean isPutNotPost) {
  
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
  
    String jwtToken = retrieveJwtToken(remedyExternalSystemConfigId, debugMap);
  
    String fullUrl = calculateUrl(remedyExternalSystemConfigId, path, paramMap);
    
    if (isPutNotPost) {
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.put);
    } else {
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    }
    grouperHttpClient.assignUrl(fullUrl);
    
    debugMap.put(isPutNotPost ? "put" : "post", true);
    debugMap.put("requestBody", requestBody);
    grouperHttpClient.addHeader("authorization", "AR-JWT " + jwtToken);
    
    if (!GrouperClientUtils.isBlank(requestBody)) {
      grouperHttpClient.addHeader("Content-type", "application/json");
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
    
    JsonNode jsonObject = GrouperUtil.jsonJacksonNode(responseBody);

    return jsonObject;
  }

  /**
   * @param remedyExternalSystemConfigId
   * @param path
   * @param paramMap
   * @return the url
   */
  private static String calculateUrl(
      String remedyExternalSystemConfigId,
      String path, Map<String, String> paramMap) {
    
    String url = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+remedyExternalSystemConfigId+".url");
    
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

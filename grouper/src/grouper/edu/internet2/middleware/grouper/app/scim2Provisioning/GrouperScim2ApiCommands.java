package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GrouperScim2ApiCommands {

  public static void main(String[] args) {

    GrouperStartup.startup();

//    for (int i=3;i<10;i++) {
//      GrouperScim2User grouperScimUser = new GrouperScim2User();
//      grouperScimUser.setActive(true);
//      grouperScimUser.setCostCenter("costCent" + i);
//      grouperScimUser.setDisplayName("dispName" + i);
//      grouperScimUser.setEmailType("emailTy" + i);
//      grouperScimUser.setEmailValue("emailVal" + i);
//      grouperScimUser.setEmployeeNumber("123456" + i);
//      grouperScimUser.setExternalId("extId" + i);
//      grouperScimUser.setFamilyName("famName" + i);
//      grouperScimUser.setFormattedName("formName" + i);
//      grouperScimUser.setGivenName("givName" + i);
//      grouperScimUser.setMiddleName("midName" + i);
//      grouperScimUser.setUserName("userNam" + i);
//      grouperScimUser.setUserType("userTyp" + i);
//  
//      createScimUser("awsReal", grouperScimUser, null);
//    }

//    for (int i=3;i<10;i++) {
//      GrouperScim2Group grouperScimGroup = new GrouperScim2Group();
//      grouperScimGroup.setDisplayName("dispName" + i);
//    
//      createScimGroup("awsLocal", grouperScimGroup, null);
//    }

//    List<GrouperScimUser> grouperScimUsers = retrieveScimUsers("awsLocal");
//    for (GrouperScimUser grouperScimUser : grouperScimUsers) {
//      System.out.println(grouperScimUser);
//    }

//    GrouperScim2Group grouperScimGroup = retrieveScimGroup("awsLocal", "id", "ce8ef11ccae741d394f37b6b78d92735");
    GrouperScim2Group grouperScimGroup = retrieveScimGroup("awsLocal", "displayName", "dispName4");
    System.out.println(grouperScimGroup);

//    GrouperScimUser grouperScimUser = retrieveScimUser("awsLocal", "displayName", "dispName5");
//    System.out.println(grouperScimUser);

//    deleteScimUser("awsLocal", "4489fb364b7f4689bd2acc7a3c56441f");
//    deleteScimGroup("awsLocal", "75975fdb5bde4f2f9d89271d1f5ed6ec");
    
  }


  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static GetMethod httpGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (GetMethod) httpMethod(debugMap, configId, urlSuffix, "GET");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static PostMethod httpPostMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (PostMethod) httpMethod(debugMap, configId, urlSuffix, "POST");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @param httpMethodName is GET, POST, DELETE, PUT
   * @return
   */
  private static HttpMethodBase httpMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix, String httpMethodName) {
    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + configId + ".endpoint");
    String bearerToken = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + configId + ".accessTokenPassword");
    String url = GrouperUtil.stripLastSlashIfExists(endpoint);

    // in a nextLink, url is specified, so it might not have a prefix of the resourceEndpoint
    if (!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    debugMap.put("url", url);

    HttpMethodBase method = null;
    if (StringUtils.equals("GET", httpMethodName)) {
      method = new GetMethod(url);
    } else if (StringUtils.equals("POST", httpMethodName)) {
      method = new PostMethod(url);
    } else if (StringUtils.equals("DELETE", httpMethodName)) {
      method = new DeleteMethod(url);
    } else if (StringUtils.equals("PUT", httpMethodName)) {
      method = new PutMethod(url);
    } else if (StringUtils.equals("PATCH", httpMethodName)) {

      method = new PostMethod(url) {
        @Override public String getName() { return "PATCH"; }
      };
    } else {
      throw new RuntimeException("Not expecting type: '" + httpMethodName + "'");
    }
    method.addRequestHeader("Authorization", "Bearer " + bearerToken);
    return method;
  }

  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {

    return executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    HttpMethodBase httpMethod = httpMethod(debugMap, configId, urlSuffix, httpMethodName);

    HttpClient httpClient = new HttpClient();

    if (!StringUtils.isBlank(body)) {
      if (httpMethod instanceof EntityEnclosingMethod) {
        try {
          StringRequestEntity entity = new StringRequestEntity(body, "application/json",
              "UTF-8");
          ((EntityEnclosingMethod) httpMethod).setRequestEntity(entity);
        } catch (Exception e) {
          throw new RuntimeException("error", e);
        }
      } else {
        throw new RuntimeException("Cant attach a body if in method: " + httpMethodName);
      }
    }

    int code = -1;
    String json = null;

    try {
      code = httpClient.executeMethod(httpMethod);
      returnCode[0] = code;
      json = httpMethod.getResponseBodyAsString();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + httpMethodName + "' '" + debugMap.get("url") + "'", e);
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
   * create a user
   * @param grouperScimUser
   * @return the result
   */
  public static GrouperScim2User createScimUser(String configId,
      GrouperScim2User grouperScimUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createScimUser");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperScimUser.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/Users",
          GrouperUtil.toSet(201), new int[] { -1 }, jsonStringToSend);

      GrouperScim2User grouperScimUserResult = GrouperScim2User.fromJson(jsonNode);

      return grouperScimUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param fieldName id or userPrincipalName
   * @param fieldValue is value of id or userPrincipalName
   * @return
   */
  public static GrouperScim2User retrieveScimUser(String configId, String fieldName,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveScimUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")) {
        urlSuffix = "/Users/" + GrouperUtil.escapeUrlEncode(fieldValue);
      } else {
        urlSuffix = "/Users?filter=" + GrouperUtil.escapeUrlEncode(fieldName)
            + "%20eq%20" + GrouperUtil.escapeUrlEncode("\"" + StringEscapeUtils.escapeJson(fieldValue) + "\"");
      }
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      if (StringUtils.equals(fieldName, "id")) {
        GrouperScim2User grouperScimUser = GrouperScim2User.fromJson(jsonNode);
        debugMap.put("found", grouperScimUser != null);
        return grouperScimUser;
      }

      if (!jsonNode.has("Resources")) {
        debugMap.put("found", false);
        return null;
      }
      
      ArrayNode resourcesNode = (ArrayNode)jsonNode.get("Resources");

      if (resourcesNode.size() == 0) {
        debugMap.put("found", false);
        return null;
      }
      
      if (resourcesNode.size() != 1) {
        throw new RuntimeException("Why is resourcesNode size " + resourcesNode.size() + " and not 1???? " + fieldName + ", " +  fieldValue);
      }
      
      JsonNode userNode = resourcesNode.get(0);
      GrouperScim2User grouperScimUser = GrouperScim2User.fromJson(userNode);
      debugMap.put("found", grouperScimUser != null);
      return grouperScimUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }

  }


  /**
   * retrieve all users
   * @return the results
   */
  public static List<GrouperScim2User> retrieveScimUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveScimUsers");

    long startTime = System.nanoTime();

    List<GrouperScim2User> results = new ArrayList<GrouperScim2User>();
    
    try {

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "/Users",
          GrouperUtil.toSet(200), new int[] { -1 }, null);

      int totalResults = GrouperUtil.jsonJacksonGetInteger(jsonNode, "totalResults");
      int itemsPerPage = GrouperUtil.jsonJacksonGetInteger(jsonNode, "itemsPerPage");
      int startIndex = GrouperUtil.jsonJacksonGetInteger(jsonNode, "startIndex");

      if (totalResults > itemsPerPage) {
        throw new RuntimeException("Total results " + totalResults + " is greater than items per page " + itemsPerPage);
      }

      if (totalResults == 0) {
        return results;
      }
      
      ArrayNode resourcesNode = (ArrayNode)jsonNode.get("Resources");
 
      for (int i=0;i<resourcesNode.size();i++) {
        JsonNode userNode = resourcesNode.get(i);
        GrouperScim2User grouperScimUser = GrouperScim2User.fromJson(userNode);
        results.add(grouperScimUser);
      }
      
      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }

  }



  public static void deleteScimUser(String configId,
      String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteScimUser");
  
    long startTime = System.nanoTime();
  
    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/Users/" + GrouperUtil.escapeUrlEncode(userId),
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }
  }


  /**
   * create a group
   * @param grouperScimGroup
   * @return the result
   */
  public static GrouperScim2Group createScimGroup(String configId,
      GrouperScim2Group grouperScimGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createScimGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperScimGroup.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/Groups",
          GrouperUtil.toSet(201), new int[] { -1 }, jsonStringToSend);
  
      GrouperScim2Group grouperScimGroupResult = GrouperScim2Group.fromJson(jsonNode);

      return grouperScimGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }
  
  }


  public static void deleteScimGroup(String configId,
      String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteScimGroup");
  
    long startTime = System.nanoTime();
  
    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/Groups/" + GrouperUtil.escapeUrlEncode(userId),
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }
  }


  /**
   * @param configId
   * @param fieldName id or userPrincipalName
   * @param fieldValue is value of id or userPrincipalName
   * @return
   */
  public static GrouperScim2Group retrieveScimGroup(String configId, String fieldName,
      String fieldValue) {
  
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveScimGroup");
  
    long startTime = System.nanoTime();
  
    try {
  
      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")) {
        urlSuffix = "/Groups/" + GrouperUtil.escapeUrlEncode(fieldValue);
      } else {
        urlSuffix = "/Groups?filter=" + GrouperUtil.escapeUrlEncode(fieldName)
            + "%20eq%20" + GrouperUtil.escapeUrlEncode("\"" + StringEscapeUtils.escapeJson(fieldValue) + "\"");
      }
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
  
      if (StringUtils.equals(fieldName, "id")) {
        GrouperScim2Group grouperScimGroup = GrouperScim2Group.fromJson(jsonNode);
        debugMap.put("found", grouperScimGroup != null);
        return grouperScimGroup;
      }
  
      if (!jsonNode.has("Resources")) {
        debugMap.put("found", false);
        return null;
      }
      
      ArrayNode resourcesNode = (ArrayNode)jsonNode.get("Resources");
  
      if (resourcesNode.size() == 0) {
        debugMap.put("found", false);
        return null;
      }
      
      if (resourcesNode.size() != 1) {
        throw new RuntimeException("Why is resourcesNode size " + resourcesNode.size() + " and not 1???? " + fieldName + ", " +  fieldValue);
      }
      
      JsonNode groupNode = resourcesNode.get(0);
      GrouperScim2Group grouperScimGroup = GrouperScim2Group.fromJson(groupNode);
      debugMap.put("found", grouperScimGroup != null);
      return grouperScimGroup;
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperScim2Log.scimLog(debugMap, startTime);
    }
  
  }
  
//  public void updateScimUser(String configId,
//      GrouperScim2User grouperScimUser, Set<String> fieldsToUpdate) {
//    
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "updateScimUser");
//
//    long startTime = System.nanoTime();
//
//    try {
//
//      
//      
//      JsonNode jsonToSend = grouperScimUser.toJson(fieldsToUpdate);
//      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
//
//      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/Users",
//          GrouperUtil.toSet(201), new int[] { -1 }, jsonStringToSend);
//
//      GrouperScim2User grouperScimUserResult = GrouperScim2User.fromJson(jsonNode);
//
//      return grouperScimUserResult;
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperScim2Log.scimLog(debugMap, startTime);
//    }
//
//
//    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
//      return;
//    }
//    
//    String id = mockServiceRequest.getPostMockNamePaths()[1];
//    
//    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
//  
//    GrouperScim2User grouperScimUser = HibernateSession.byHqlStatic()
//        .createQuery("from GrouperScimUser where id = :theValue").setString("theValue", id)
//        .uniqueResult(GrouperScim2User.class);
//
//    if (grouperScimUser == null) {
//      mockServiceResponse.setResponseCode(404);
//      mockServiceRequest.getDebugMap().put("foundUser", false);
//      return;
//    }
//        
//    mockServiceResponse.setContentType("application/json");
//    
//    //  {
//    //    "schemas": [
//    //        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
//    //    ],
//    //    "Operations": [
//    //        {
//    //            "op": "replace",
//    //            "path": "active",
//    //            "value": "false"
//    //        }
//    //    ]
//    //  }
//    
//    String requestBodyString = mockServiceRequest.getRequestBody();
//    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBodyString);
//
//    ArrayNode schemasNode = (ArrayNode)requestNode.get("schemas");
//
//    GrouperUtil.assertion(schemasNode.size() == 1, "schema is required");
//    GrouperUtil.assertion("urn:ietf:params:scim:api:messages:2.0:PatchOp".equals(schemasNode.get(0).asText()), "schema is required");
//
//    ArrayNode operationsNode = (ArrayNode)requestNode.get("Operations");
//
//    GrouperUtil.assertion(operationsNode.size() > 0, "must send operations");
//
//    for (int i=0;i<operationsNode.size();i++) {
//      
//      JsonNode operation = operationsNode.get(i);
//      
//      //            "op": "replace",
//      //            "path": "active",
//      //            "value": "false"
//
//      // replace, add, remove
//      String op = GrouperUtil.jsonJacksonGetString(operation, "op");
//      boolean opAdd = "add".equals(op);
//      boolean opReplace = "replace".equals(op);
//      boolean opRemove = "remove".equals(op);
//      if (!opAdd && !opRemove && !opReplace) {
//        throw new RuntimeException("Invalid op, expecting add, replace, remove, but received: '" + op + "'");
//      }
//      String path = GrouperUtil.jsonJacksonGetString(operation, "path");
//      
//      //  {
//      //    "active":true,
//      //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
//      //       "employeeNumber":"12345",
//      //       "costCenter":"costCent"   e.g. urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter
//      //    },
//      //    "id":"i",
//      //    "displayName":"dispName",
//      //    "emails":[
//      //       {
//      //          "value":"emailVal", emails.value eq "emailVal" or emails[value eq "emailVal"]
//      //          "primary":true,
//      //          "type":"emailTy"  emails.type eq "work" or emails[type eq "work"]
//      //       }
//      //    ],
//      //    "name":{
//      //       "formatted":"formName",    e.g. name.formatted
//      //       "familyName":"famName",
//      //       "givenName":"givName",
//      //       "middleName":"midName"
//      //    },
//      //    "externalId":"extId",
//      //    "userName":"userNam",
//      //    "userType":"userTyp"
//      // }
//      
//      GrouperUtil.assertion(!"id".equals(path), "cannot patch id");
//
//      //  costCenter : String
//      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter".equals(path)) {
//        path = "costCenter";
//      }
//      //  employeeNumber : String
//      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.employeeNumber".equals(path)) {
//        path = "employeeNumber";
//      }
//
//      //  familyName : String
//      if ("name.familyName".equals(path)) {
//        path = "familyName";
//      }
//      //  formattedName : String
//      if ("name.formattedName".equals(path)) {
//        path = "formattedName";
//      }
//      //  givenName : String
//      if ("name.givenName".equals(path)) {
//        path = "givenName";
//      }
//      //  middleName : String
//      if ("name.middleName".equals(path)) {
//        path = "middleName";
//      }
//      
//      if (path.startsWith("emails")) {
//        // emailType : String
//        // emailValue : String
//        // emails[0]['value'] or emails.value eq "emailVal" or emails[value eq "emailVal"]
//        
//        JsonNode newEmailNode = operation.get("value");
//        
//        // validate the email
//        if (opAdd) {
//          
//          // if theres an existing, thats bad
//          if (!StringUtils.isBlank(grouperScimUser.getEmailValue()) || !StringUtils.isBlank(grouperScimUser.getEmailType())) {
//            
//            throw new RuntimeException("Adding email but already exists! " + grouperScimUser);
//            
//          }
//
//          if (newEmailNode.has("type")) {
//            grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
//          }
//          if (newEmailNode.has("value")) {
//            grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
//          }
//          
//        } else {
//          grouperScimUser.validateEmail(path);
//
//          if (StringUtils.isBlank(grouperScimUser.getEmailValue()) && StringUtils.isBlank(grouperScimUser.getEmailType())) {
//            
//            throw new RuntimeException(op + " email but not there! " + grouperScimUser);
//            
//          }
//
//          if (opRemove) {
//            
//            grouperScimUser.setEmailType(null);
//            grouperScimUser.setEmailValue(null);
//            
//          } else {
//            
//            //replace
//            GrouperUtil.assertion(opReplace, "expecting replace");
//
//            if (newEmailNode.isArray()) {
//              GrouperUtil.assertion(newEmailNode.size() == 1, "expecting size 1 but was " + newEmailNode.size());
//              newEmailNode = ((ArrayNode)newEmailNode).get(0);
//            }
//            if (newEmailNode.has("type")) {
//              grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
//            }
//            if (newEmailNode.has("value")) {
//              grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
//            }
//            
//            
//          }
//          
//        }
//        
//      } else {
//        
//        Object newValue = "active".equals(path) ? GrouperUtil.jsonJacksonGetBoolean(operation, "value") : GrouperUtil.jsonJacksonGetString(operation, "value");
//        Object oldValue = GrouperUtil.fieldValue(grouperScimUser, path);
//        
//        // validate the email
//        if (opAdd) {
//          
//          GrouperUtil.assertion(GrouperUtil.isBlank(oldValue), "add op already has value! " + path + ", '" + oldValue + "' " + grouperScimUser);
//          
//          GrouperUtil.assignField(grouperScimUser, path, newValue);
//          
//        } else {
//
//          GrouperUtil.assertion(!GrouperUtil.isBlank(oldValue), "add op doesnt have value! " + path + ", '" + oldValue + "' " + grouperScimUser);
//
//          if (opRemove) {
//            
//            GrouperUtil.assertion(newValue == null, "remove op should not have a value! " + path + ", '" + newValue + "' " + grouperScimUser);
//          }
//
//          GrouperUtil.assignField(grouperScimUser, path, newValue);
//        }
//        
//      }
//      
//    }
//    HibernateSession.byObjectStatic().saveOrUpdate(grouperScimUser);
//    
//    ObjectNode objectNode = grouperScimUser.toJson(null);
//    mockServiceResponse.setResponseCode(204);
//    mockServiceResponse.setContentType("application/json");
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
//    
//    
//  }

}

package edu.internet2.middleware.grouper.app.azure;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.morphString.Morph;

public class AzureMockServiceHandler extends MockServiceHandler {

  public AzureMockServiceHandler() {
  }

  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  private String configId;
  /**
   * params to not log all of
   */
  @Override
  public Set<String> doNotLogParameters() {
    
    return doNotLogParameters;
  }

  /**
   * headers to not log all of
   */
  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  /**
   * 
   */
  public static void ensureAzureMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_azure_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_azure_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_azure_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_azure_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperAzureGroup.createTableAzureGroup(ddlVersionBean, database);
          GrouperAzureAuth.createTableAzureAuth(ddlVersionBean, database);
          GrouperAzureUser.createTableAzureUser(ddlVersionBean, database);
          GrouperAzureMembership.createTableAzureMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropAzureMockTables() {
    MockServiceServlet.dropMockTable("mock_azure_membership");
    MockServiceServlet.dropMockTable("mock_azure_user");
    MockServiceServlet.dropMockTable("mock_azure_group");
    MockServiceServlet.dropMockTable("mock_azure_auth");
  }
  
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureAzureMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    this.configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.azure.mock.configId");
    if (StringUtils.isBlank(configId)) {
      this.configId = "myAzure";
    }

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
        getGroupMembers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        deleteGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        deleteUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 5 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2]) && "$ref".equals(mockServiceRequest.getPostMockNamePaths()[4])) {
        deleteMembership(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("auth".equals(mockServiceRequest.getPostMockNamePaths()[0])) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        postUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 4 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2]) && "$ref".equals(mockServiceRequest.getPostMockNamePaths()[3])) {
        postMembership(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
          && "getMemberGroups".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
        postUserGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("$batch".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        postBatch(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("PATCH", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        patchGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
    }    

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
  }

  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    
    List<GrouperAzureAuth> grouperAzureAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperAzureAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperAzureAuth.class);
    
    if (GrouperUtil.length(grouperAzureAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found! " + StringUtils.abbreviate(authorizationToken, 5));
    }
    
    GrouperAzureAuth grouperAzureAuth = grouperAzureAuths.get(0);    

    if (grouperAzureAuth.getExpiresOnSeconds() < System.currentTimeMillis()/1000) {
      throw new RuntimeException("Invalid access token, expired!");
    }

    // all good
  }

  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")
            && !StringUtils.startsWith(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json;")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }
  
  public void postBatch(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    /**
     * {"requests":[{"id":"0","url":"/groups","method":"POST","body":{"displayName":"test:testGroup","mailEnabled":false,
     * "mailNickname":"testGroup","securityEnabled":true,"resourceBehaviorOptions":["AllowOnlyMembersToPost","WelcomeEmailDisabled"],
     * "resourceProvisioningOptions":["Team"]},"headers":{"Content-Type":"application/json"}}]}
     */
    
    /**
     * {"requests":[{"id":"0","url":"/groups?$filter=displayName%20eq%20'test:testGroup'&$select=isAssignableToRole,description,
     * displayName,groupTypes,id,mailEnabled,mailNickname,
     * securityEnabled,visibility,resourceBehaviorOptions,resourceProvisioningOptions","method":"GET"}]}
     */
    
    /**
     * {"requests":[{"id":"0","url":"/users?$filter=displayName%20eq%20'my+name+is+test.subject.0'&$select=accountEnabled,
     * displayName,id,mailNickname,onPremisesImmutableId,userPrincipalName","method":"GET"},{"id":"1",
     * "url":"/users?$filter=displayName%20eq%20'my+name+is+test.subject.1'&$select=accountEnabled,displayName,
     * id,mailNickname,onPremisesImmutableId,userPrincipalName","method":"GET"}]}
     */
    
    /**
     * {"requests":[{"id":"0","url":"/groups/45b476a6e3084aa390dba03f9e667382",
     * "method":"PATCH","body":{"members@odata.bind":["http://localhost:8080/grouper/mockServices/azure/directoryObjects/78a8e33a584745debd4c4aeac8592c40",
     * "http://localhost:8080/grouper/mockServices/azure/directoryObjects/7801a0b0bcf94c45a641e612e4e8e5b9"]},
     * "headers":{"Content-Type":"application/json"}}]}
     */
    
    /**
     * {"requests":[{"id":"0","url":"/groups/2427842379df43eb871ecda17e9fca53/members/24a13a5a0e7e4d7586a530f0f7818761/$ref",
     * "method":"DELETE"}]}
     */
    
    /**
     * {"requests":[{"id":"0","url":"/groups/f1fdeacd5c834733a7d26556969571f8","method":"DELETE"}]}
     */
    
    checkAuthorization(mockServiceRequest);
    
    String batchJsonString = mockServiceRequest.getRequestBody();
    JsonNode batchJsonNode = GrouperUtil.jsonJacksonNode(batchJsonString);
    
    ArrayNode requestsArrayNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(batchJsonNode, "requests");
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode responsesNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.set("responses", responsesNode);
    
    for (int i=0; i<requestsArrayNode.size(); i++) {
      
      JsonNode singleRequestNode = requestsArrayNode.get(i);
      
      String httpMethod = GrouperUtil.jsonJacksonGetString(singleRequestNode, "method");
      String url = GrouperUtil.jsonJacksonGetString(singleRequestNode, "url");
      String id = GrouperUtil.jsonJacksonGetString(singleRequestNode, "id");
      
      if (StringUtils.equalsIgnoreCase(httpMethod, "get")) {
        
        String[] urlParts = url.split("/");
        List<String> urlPartsList = new ArrayList<String>(Arrays.asList(urlParts));
        urlPartsList.removeAll(Arrays.asList("", null));
        
        if ( urlPartsList.size() == 1 && StringUtils.startsWith(urlPartsList.get(0), "groups?")) {
          
          String groupsFilter = urlPartsList.get(0);
          String[] beforeAfterGroups = groupsFilter.split("groups\\?");
          
          List<NameValuePair> queryParams = URLEncodedUtils.parse(beforeAfterGroups[1], Charset.defaultCharset());
          
          Map<String, String> keyValue = new HashMap<>();
          
          for (NameValuePair nameValuePair: queryParams) {
            keyValue.put(nameValuePair.getName(), nameValuePair.getValue());
          }
          
          MultiKey getGroupsResult = getGroups(keyValue.get("$filter"), keyValue.get("$select"));
          
          ObjectNode getGroupsResponse = GrouperUtil.jsonJacksonNode();
          getGroupsResponse.put("id", id);
          getGroupsResponse.put("status", (Integer)getGroupsResult.getKey(0));
          if (getGroupsResult.getKey(1) != null) {
            getGroupsResponse.set("body", (JsonNode)getGroupsResult.getKey(1));
          }
          
          responsesNode.add(getGroupsResponse);
        } else if (StringUtils.equals(urlParts[0], "/groups") && urlParts.length == 2 ) { // get a particular group
          
//          MultiKey getGroupResult = getGroup1(urlParts[1], keyValue.get("$select"));
//          ObjectNode getGroupResponse = GrouperUtil.jsonJacksonNode();
//          getGroupResponse.put("id", id);
//          getGroupResponse.put("status", (Integer)getGroupResult.getKey(0));
//          if (getGroupResult.getKey(1) != null) {
//            getGroupResponse.set("body", (JsonNode)getGroupResult.getKey(1));
//          }
//          
//          responsesNode.add(getGroupResponse);
        } else if ( urlPartsList.size() == 1 && StringUtils.startsWith(urlPartsList.get(0), "users?")) {
          
          String groupsFilter = urlPartsList.get(0);
          String[] beforeAfterGroups = groupsFilter.split("users\\?");
          
          List<NameValuePair> queryParams = URLEncodedUtils.parse(beforeAfterGroups[1], Charset.defaultCharset());
          
          Map<String, String> keyValue = new HashMap<>();
          
          for (NameValuePair nameValuePair: queryParams) {
            keyValue.put(nameValuePair.getName(), nameValuePair.getValue());
          }
          
          MultiKey getUsersResult = getUsers(keyValue.get("$filter"), keyValue.get("$select"));
          
          ObjectNode getUsersResponse = GrouperUtil.jsonJacksonNode();
          getUsersResponse.put("id", id);
          getUsersResponse.put("status", (Integer)getUsersResult.getKey(0));
          if (getUsersResult.getKey(1) != null) {
            getUsersResponse.set("body", (JsonNode)getUsersResult.getKey(1));
          }
          
          responsesNode.add(getUsersResponse);
        } else if (urlPartsList.size() == 2 && "users".equals(urlPartsList.get(0))) {

        // /users/upn@domain.com?$select=accountEnabled,displayName,id,mailNickname,onPremisesImmutableId,userPrincipalName
        

          String filter = urlPartsList.get(1);
          String[] beforeAfterUser = filter.split("\\?\\$select=");

          MultiKey getUsersResult = null;
          try {
            getUsersResult = getUsers("userPrincipalName eq '" + URLDecoder.decode(beforeAfterUser[0],"UTF-8") + "'", beforeAfterUser[1]);
          } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
          }

          ObjectNode getUsersResponse = GrouperUtil.jsonJacksonNode();
          getUsersResponse.put("id", id);
          getUsersResponse.put("status", (Integer)getUsersResult.getKey(0));
          if (getUsersResult.getKey(1) != null) {
            getUsersResponse.set("body", (JsonNode)getUsersResult.getKey(1));
          }

          responsesNode.add(getUsersResponse);
        } else {
          throw new RuntimeException("Not expecting get url: " + url);
        }
      } else if (StringUtils.equalsIgnoreCase(httpMethod, "post")) {
        
        JsonNode body = GrouperUtil.jsonJacksonGetNode(singleRequestNode, "body");
        String[] urlParts = url.split("/");

        if ("/groups".equals(url)) {
          
          MultiKey postGroupsResult = postGroups(body);
          
          ObjectNode postGroupResponse = GrouperUtil.jsonJacksonNode();
          postGroupResponse.put("id", id);
          postGroupResponse.put("status", (Integer)postGroupsResult.getKey(0));
          if (postGroupsResult.getKey(1) != null) {
            postGroupResponse.set("body", (JsonNode)postGroupsResult.getKey(1));
          }
          
          responsesNode.add(postGroupResponse);
          
        } else if ("/users".equals(url)) {
          MultiKey postUsersResult = postUsers(body);
          
          ObjectNode postUserResponse = GrouperUtil.jsonJacksonNode();
          postUserResponse.put("id", id);
          postUserResponse.put("status", (Integer)postUsersResult.getKey(0));
          if (postUsersResult.getKey(1) != null) {
            postUserResponse.set("body", (JsonNode)postUsersResult.getKey(1));
          }
          
          responsesNode.add(postUserResponse);
        } else if (urlParts.length == 4 && StringUtils.equals(urlParts[0], "groups") 
            && StringUtils.equals(urlParts[2], "members") && StringUtils.equals(urlParts[3], "$ref")) {
          
          
          MultiKey postMembershipResult = postMembership(body, urlParts[1]);
          ObjectNode postUserResponse = GrouperUtil.jsonJacksonNode();
          postUserResponse.put("id", id);
          postUserResponse.put("status", (Integer)postMembershipResult.getKey(0));
          if (postMembershipResult.getKey(1) != null) {
            postUserResponse.set("body", (JsonNode)postMembershipResult.getKey(1));
          }
          
          responsesNode.add(postUserResponse);
          
        } else if (urlParts.length == 3 && StringUtils.equals(urlParts[0], "users") 
            && StringUtils.equals(urlParts[2], "getMemberGroups")) {
          
          
          MultiKey postMembershipResult = postUserGroups(urlParts[1]);
          ObjectNode postUserResponse = GrouperUtil.jsonJacksonNode();
          postUserResponse.put("id", id);
          postUserResponse.put("status", (Integer)postMembershipResult.getKey(0));
          if (postMembershipResult.getKey(1) != null) {
            postUserResponse.set("body", (JsonNode)postMembershipResult.getKey(1));
          }
          
          responsesNode.add(postUserResponse);
          
        } else if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
            && "getMemberGroups".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
          postUserGroups(mockServiceRequest, mockServiceResponse);
          return;
        } else {
          throw new RuntimeException("Not expecting post URL: " + url);
        }
        
      } else if (StringUtils.equalsIgnoreCase(httpMethod, "patch")) {
        
        String[] urlParts = url.split("/");
        
        JsonNode body = GrouperUtil.jsonJacksonGetNode(singleRequestNode, "body");
        
        if (urlParts.length == 3 && StringUtils.equals(urlParts[1], "groups")) {
          
          MultiKey postMembershipResult = patchGroups(body, urlParts[2]);
          ObjectNode postUserResponse = GrouperUtil.jsonJacksonNode();
          postUserResponse.put("id", id);
          postUserResponse.put("status", (Integer)postMembershipResult.getKey(0));
          if (postMembershipResult.getKey(1) != null) {
            postUserResponse.set("body", (JsonNode)postMembershipResult.getKey(1));
          }
          
          responsesNode.add(postUserResponse);
          
        } else {
          throw new RuntimeException("Not expecting patch URL: " + url);
        }
        
      } else if (StringUtils.equalsIgnoreCase(httpMethod, "delete")) {
        
        /**
         * {"requests":[{"id":"0","url":"/groups/2427842379df43eb871ecda17e9fca53/members/24a13a5a0e7e4d7586a530f0f7818761/$ref",
         * "method":"DELETE"}]}
         */
        String[] urlParts = url.split("/");
        if (urlParts.length == 6 && StringUtils.equals(urlParts[1], "groups") 
            && StringUtils.equals(urlParts[3], "members")) {
          
          MultiKey result = deleteMembership(urlParts[4], urlParts[2]);
          ObjectNode response = GrouperUtil.jsonJacksonNode();
          response.put("id", id);
          response.put("status", (Integer)result.getKey(0));
          if (result.getKey(1) != null) {
            response.set("body", (JsonNode)result.getKey(1));
          }
          responsesNode.add(response);
          
        } else if (urlParts.length == 3 && StringUtils.equals(urlParts[1], "groups")) {
        
        /**
         * {"requests":[{"id":"0","url":"/groups/f1fdeacd5c834733a7d26556969571f8","method":"DELETE"}]}
         */
        
          
          MultiKey result = deleteGroups(urlParts[2]);
          ObjectNode response = GrouperUtil.jsonJacksonNode();
          response.put("id", id);
          response.put("status", (Integer)result.getKey(0));
          if (result.getKey(1) != null) {
            response.set("body", (JsonNode)result.getKey(1));
          }
          responsesNode.add(response);
          
        } else if (urlParts.length == 3 && StringUtils.equals(urlParts[1], "users")) {
        
        /**
         * {"requests":[{"id":"0","url":"/users/f1fdeacd5c834733a7d26556969571f8","method":"DELETE"}]}
         */
        
          
          MultiKey result = deleteUsers(urlParts[2]);
          ObjectNode response = GrouperUtil.jsonJacksonNode();
          response.put("id", id);
          response.put("status", (Integer)result.getKey(0));
          if (result.getKey(1) != null) {
            response.set("body", (JsonNode)result.getKey(1));
          }
          responsesNode.add(response);
          
        } else {
          throw new RuntimeException("Not expecting patch URL: " + url);
        }
        
        
      } else {
        throw new RuntimeException("Not expecting method: " + httpMethod + ", " + url);
      }
      
      
      
      
      
      
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    
  }
   
  
  public MultiKey postGroups(JsonNode body) {
    
    JsonNode groupJsonNode = body;

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "displayName")) > 0, "displayName is required");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "displayName")) <= 256, "displayName must be less than 256");
    
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "description")) <= 1024, "description must be less than 1024");

    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(groupJsonNode, "mailEnabled") != null, "mailEnabled is required");

    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "mailNickname")) <= 64, "mailNickname must be less than 64");

    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(groupJsonNode, "securityEnabled") != null, "securityEnabled is required");

    String visibility = GrouperUtil.jsonJacksonGetString(groupJsonNode, "visibility");

    if (visibility != null) {
      GrouperUtil.assertion(GrouperUtil.toSet("Private", "Public", "HiddenMembership", "Public").contains(visibility), "visibility must be one of: 'Private', 'Public', 'HiddenMembership', 'Public', but was: '" + visibility + "'");
    }

    //GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "id")) == 0, "id is forbidden");

    GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(groupJsonNode);
    grouperAzureGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperAzureGroup);
    
    JsonNode resultNode = grouperAzureGroup.toJson(null);
    
    return new MultiKey(201, resultNode);

//    mockServiceResponse.setResponseCode(201);
//    mockServiceResponse.setContentType("application/json");
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    //  {
    //    "description": "Self help community for library",
    //    "displayName": "Library Assist",
    //    "groupTypes": [
    //      "Unified"
    //    ],
    //    "mailEnabled": true,
    //    "mailNickname": "library",
    //    "securityEnabled": false
    //  }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "displayName")) > 0, "displayName is required");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "displayName")) <= 256, "displayName must be less than 256");
    
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "description")) <= 1024, "description must be less than 1024");

    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(groupJsonNode, "mailEnabled") != null, "mailEnabled is required");

    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "mailNickname")) <= 64, "mailNickname must be less than 64");

    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(groupJsonNode, "securityEnabled") != null, "securityEnabled is required");

    String visibility = GrouperUtil.jsonJacksonGetString(groupJsonNode, "visibility");

    if (visibility != null) {
      GrouperUtil.assertion(GrouperUtil.toSet("Private", "Public", "HiddenMembership", "Public").contains(visibility), "visibility must be one of: 'Private', 'Public', 'HiddenMembership', 'Public', but was: '" + visibility + "'");
    }

    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "id")) == 0, "id is forbidden");

    GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(groupJsonNode);
    grouperAzureGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperAzureGroup);
    
    JsonNode resultNode = grouperAzureGroup.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    
  }
  
  public MultiKey postUsers(JsonNode body) {
    
    JsonNode userJsonNode = body;

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "displayName")) > 0, "displayName is required");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "displayName")) <= 256, "displayName must be less than 256");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "mailNickname")) <= 64, "mailNickname must be less than 64");
    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "accountEnabled") != null, "accountEnabled is required");
    
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "userPrincipalName")) > 0, "userPrincipalName is required");


    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "id")) == 0, "id is forbidden");

    GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(userJsonNode);
    grouperAzureUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperAzureUser);
    
    JsonNode resultNode = grouperAzureUser.toJson(null);
    
    return new MultiKey(201, resultNode);
  }
  
  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

//    {
//      "accountEnabled": true,
//      "displayName": "Adele Vance1",
//      "mailNickname": "AdeleV1",
//      "userPrincipalName": "Adele1V@erviveksachdevaoutlook.onmicrosoft.com",
//      "passwordProfile" : {
//        "forceChangePasswordNextSignIn": true,
//        "password": "xWwvJ]6NMw+bWH-d1"
//      }
//    }
    
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "displayName")) > 0, "displayName is required");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "displayName")) <= 256, "displayName must be less than 256");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "mailNickname")) <= 64, "mailNickname must be less than 64");
    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "accountEnabled") != null, "accountEnabled is required");
    
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "userPrincipalName")) > 0, "userPrincipalName is required");


    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "id")) == 0, "id is forbidden");

    GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(userJsonNode);
    grouperAzureUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperAzureUser);
    
    JsonNode resultNode = grouperAzureUser.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  public MultiKey getGroups(String filter, String fieldsToRetrieveString) {
    
    List<GrouperAzureGroup> grouperAzureGroups = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20'" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq 'something'
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+'(.+)'$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup where " + field + " = :theValue").setString("theValue", value).list(GrouperAzureGroup.class);
    }
    
    //  {
    //    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#groups",
    //    "value": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@contoso.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint");

    resultNode.put("@odata.context", GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/$metadata#groups");
    
    Set<String> fieldsToRetrieve = null;
//    String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
    if (!StringUtils.isBlank(fieldsToRetrieveString)) {
      fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
    }
    
    for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
      valueNode.add(grouperAzureGroup.toJson(fieldsToRetrieve));
    }
    
    resultNode.set("value", valueNode);
    
    return new MultiKey(200, resultNode);
    
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String filter = mockServiceRequest.getHttpServletRequest().getParameter("$filter");
    
    
    List<GrouperAzureGroup> grouperAzureGroups = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20'" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq 'something'
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+'(.+)'$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup where " + field + " = :theValue").setString("theValue", value).list(GrouperAzureGroup.class);
    }
    
    //  {
    //    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#groups",
    //    "value": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@contoso.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint");

    resultNode.put("@odata.context", GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/$metadata#groups");
    
    Set<String> fieldsToRetrieve = null;
    String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
    if (!StringUtils.isBlank(fieldsToRetrieveString)) {
      fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
    }
    
    for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
      valueNode.add(grouperAzureGroup.toJson(fieldsToRetrieve));
    }
    
    resultNode.set("value", valueNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }
  
  public MultiKey getGroup(String groupId, String fieldsToRetrieveString) {
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "id is required");
    
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);

    if (GrouperUtil.length(grouperAzureGroups) == 1) {
//      mockServiceResponse.setResponseCode(200);

      //  {
      //    "id": "11111111-2222-3333-4444-555555555555",
      //    "mail": "group1@contoso.com",
      //    "mailEnabled": true,
      //    "mailNickname": "ContosoGroup1",
      //    "securityEnabled": true
      //  }
      
      Set<String> fieldsToRetrieve = null;
//      String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
      if (!StringUtils.isBlank(fieldsToRetrieveString)) {
        fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
      }

//      mockServiceResponse.setContentType("application/json");

      ObjectNode objectNode = grouperAzureGroups.get(0).toJson(fieldsToRetrieve);
//      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
      
      return new MultiKey(200, objectNode);

    } else if (GrouperUtil.length(grouperAzureGroups) == 0) {
//      mockServiceResponse.setResponseCode(404);
      return new MultiKey(404, null);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperAzureGroups) + ", id: " + groupId);
    }
    
    
  }
  
  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
    
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup where id = :theId")
        .setString("theId", id).list(GrouperAzureGroup.class);

    if (GrouperUtil.length(grouperAzureGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      //  {
      //    "id": "11111111-2222-3333-4444-555555555555",
      //    "mail": "group1@contoso.com",
      //    "mailEnabled": true,
      //    "mailNickname": "ContosoGroup1",
      //    "securityEnabled": true
      //  }
      
      Set<String> fieldsToRetrieve = null;
      String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
      if (!StringUtils.isBlank(fieldsToRetrieveString)) {
        fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
      }

      mockServiceResponse.setContentType("application/json");

      ObjectNode objectNode = grouperAzureGroups.get(0).toJson(fieldsToRetrieve);
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperAzureGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperAzureGroups) + ", id: " + id);
    }

  }
  
  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String clientId = mockServiceRequest.getHttpServletRequest().getParameter("client_id");
    if (StringUtils.isBlank(clientId)) {
      throw new RuntimeException("client_id is required!");
    }

    String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + this.configId + ".clientSecret");
    clientSecret = Morph.decryptIfFile(clientSecret);
    if (!StringUtils.equals(clientSecret, mockServiceRequest.getHttpServletRequest().getParameter("client_secret"))) {
      // let config propagate
      GrouperUtil.sleep(10000);
      
      clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + this.configId + ".clientSecret");
      clientSecret = Morph.decryptIfFile(clientSecret);
      if (!StringUtils.equals(clientSecret, mockServiceRequest.getHttpServletRequest().getParameter("client_secret"))) {
        throw new RuntimeException("Cant find client secret!");
      }
    }
    
    String tenantId = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + this.configId + ".tenantId");
    if (StringUtils.isBlank(tenantId)) {
      tenantId = "myTenant";
    }
    
    if (4 != mockServiceRequest.getPostMockNamePaths().length
        || !StringUtils.equals(tenantId, mockServiceRequest.getPostMockNamePaths()[1])
        || !StringUtils.equals("oauth2", mockServiceRequest.getPostMockNamePaths()[2])
        || !StringUtils.equals("token", mockServiceRequest.getPostMockNamePaths()[3])
        ) {
      throw new RuntimeException("Invalid request! expecting: auth/<tenantId>/oauth2/token");
    }
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    if (!StringUtils.equals("client_credentials", grantType)) {
      throw new RuntimeException("Invalid request! client_credentials must equal 'grant_type'");
    }
    String resourceConfig = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + this.configId + ".resource");
    String resourceHttp =  mockServiceRequest.getHttpServletRequest().getParameter("resource");
    if (StringUtils.isBlank(resourceConfig) || !StringUtils.equals(resourceConfig, resourceHttp)) {
      throw new RuntimeException("Invalid request! resource: '" + resourceHttp + "' must equal '" + resourceConfig + "'");
    }

    mockServiceResponse.setResponseCode(200);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    //expires in a minute
    long expiresOnSeconds = System.currentTimeMillis()/1000 + 60;
    
    resultNode.put("expires_on", expiresOnSeconds);
    
    String accessToken = GrouperUuid.getUuid();
    
    GrouperAzureAuth grouperAzureAuth = new GrouperAzureAuth();
    grouperAzureAuth.setConfigId(this.configId);
    grouperAzureAuth.setAccessToken(accessToken);
    grouperAzureAuth.setExpiresOnSeconds(expiresOnSeconds);
    HibernateSession.byObjectStatic().save(grouperAzureAuth);
    
    resultNode.put("access_token", accessToken);
    
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    //delete if its been a while
    if (System.currentTimeMillis() - lastDeleteMillis > 1000*60*60) {
      lastDeleteMillis = System.currentTimeMillis();
      
      long secondsToDelete = System.currentTimeMillis()/1000 - 60*60;
      
      int accessTokensDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureAuth where expiresOnSeconds < :theExpiresOnSeconds")
        .setLong("theExpiresOnSeconds", secondsToDelete).executeUpdateInt();
      
      if (accessTokensDeleted > 0) {
        mockServiceRequest.getDebugMap().put("accessTokensDeleted", accessTokensDeleted);
      }
    }
    
  }

  
  public MultiKey deleteGroups(String groupId) {
    
    String id = groupId;
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    int membershipsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureMembership where groupId = :theId")
        .setString("theId", id).executeUpdateInt();
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureGroup where id = :theId")
        .setString("theId", id).executeUpdateInt();

    if (groupsDeleted == 1) {
      return new MultiKey(204, null);
    } else if (groupsDeleted == 0) {
      return new MultiKey(404, null);
    } else {
      throw new RuntimeException("groupsDeleted: " + groupsDeleted);
    }
  }
  
  public MultiKey deleteUsers(String userId) {
    
    String id = userId;
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    int membershipsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureMembership where userId = :theId")
        .setString("theId", id).executeUpdateInt();
    
    int usersDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureUser where id = :theId")
        .setString("theId", id).executeUpdateInt();

    if (usersDeleted == 1) {
      return new MultiKey(204, null);
    } else if (usersDeleted == 0) {
      return new MultiKey(404, null);
    } else {
      throw new RuntimeException("usersDeleted: " + usersDeleted);
    }
  }
  
  
  public void deleteGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    int membershipsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureMembership where groupId = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("membershipsDeleted", membershipsDeleted);
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureGroup where id = :theId")
        .setString("theId", id).executeUpdateInt();

    if (groupsDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (groupsDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsDeleted: " + groupsDeleted);
    }
        
  }



  public void deleteUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    int membershipsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureMembership where userId = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("membershipsDeleted", membershipsDeleted);
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAzureUser where id = :theId")
        .setString("theId", id).executeUpdateInt();

    if (groupsDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (groupsDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersDeleted: " + groupsDeleted);
    }
        
  }

  public MultiKey getUsers(String filter, String fieldsToRetrieveString) {
    
    List<GrouperAzureUser> grouperAzureUsers = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20'" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq 'something'
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+'(.+)'$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser where " + field + " = :theValue").setString("theValue", value).list(GrouperAzureUser.class);
    }
    
    //  {
    //    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#groups",
    //    "value": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@contoso.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    Set<String> fieldsToRetrieve = null;
//    String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
    if (!StringUtils.isBlank(fieldsToRetrieveString)) {
      fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
    }
    
    for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
      valueNode.add(grouperAzureUser.toJson(fieldsToRetrieve));
    }
    
    resultNode.set("value", valueNode);
    
//    mockServiceResponse.setResponseCode(200);
//    mockServiceResponse.setContentType("application/json");
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    return new MultiKey(200, resultNode);
    
  }
  
  
  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String filter = mockServiceRequest.getHttpServletRequest().getParameter("$filter");
    
    
    List<GrouperAzureUser> grouperAzureUsers = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20'" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq 'something'
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+'(.+)'$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser where " + field + " = :theValue").setString("theValue", value).list(GrouperAzureUser.class);
    }
    
    //  {
    //    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#groups",
    //    "value": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@contoso.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    Set<String> fieldsToRetrieve = null;
    String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
    if (!StringUtils.isBlank(fieldsToRetrieveString)) {
      fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
    }
    
    for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
      valueNode.add(grouperAzureUser.toJson(fieldsToRetrieve));
    }
    
    resultNode.set("value", valueNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser where id = :theId or userPrincipalName = :theId2")
        .setString("theId", id).setString("theId2", id).list(GrouperAzureUser.class);
  
    if (GrouperUtil.length(grouperAzureUsers) == 1) {
      mockServiceResponse.setResponseCode(200);
  
      //  {
      //    "id": "11111111-2222-3333-4444-555555555555",
      //    "userPrincipalName": "whatever",
      //    etc
      //  }
      
      Set<String> fieldsToRetrieve = null;
      String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
      if (!StringUtils.isBlank(fieldsToRetrieveString)) {
        fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
      }
  
      mockServiceResponse.setContentType("application/json");
  
      ObjectNode objectNode = grouperAzureUsers.get(0).toJson(fieldsToRetrieve);
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
    } else if (GrouperUtil.length(grouperAzureUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(grouperAzureUsers) + ", id: " + id);
    }
  
  }
  
  public MultiKey patchGroups(JsonNode requestJsonNode, String groupId) {
    
    if (requestJsonNode.has("members@odata.bind")) {
      return patchMemberships(groupId, requestJsonNode);
    }
    
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      return new MultiKey(404, null);
    }
    if (GrouperUtil.length(grouperAzureGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperAzureGroups));
    }
    GrouperAzureGroup grouperAzureGroup = grouperAzureGroups.get(0);

    // only update fields if they are in the patch
    if (requestJsonNode.has("description")) {
      grouperAzureGroup.setDescription(GrouperUtil.jsonJacksonGetString(requestJsonNode, "description"));
    }
    if (requestJsonNode.has("displayName")) {
      grouperAzureGroup.setDisplayName(GrouperUtil.jsonJacksonGetString(requestJsonNode, "displayName"));
    }
    if (requestJsonNode.has("groupTypes")) {
      ArrayNode groupTypesArrayNode = (ArrayNode)requestJsonNode.get("groupTypes");
      Set<String> groupTypesSet = new HashSet<String>();
      for (int i=0;i<groupTypesArrayNode.size();i++) {
        String groupType = groupTypesArrayNode.get(i).asText();
        groupTypesSet.add(groupType);
      }
     
      grouperAzureGroup.setGroupTypeUnified(groupTypesSet.contains("Unified"));
      grouperAzureGroup.setGroupTypeDynamic(groupTypesSet.contains("Dynamic"));
    }
    if (requestJsonNode.has("id")) {
      throw new RuntimeException("Cant update the id field!");
    }
    if (requestJsonNode.has("mailEnabled")) {
      grouperAzureGroup.setMailEnabled(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "mailEnabled"));
    }
    if (requestJsonNode.has("isAssignableToRole")) {
      grouperAzureGroup.setAssignableToRole(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "isAssignableToRole"));
    }
    if (requestJsonNode.has("mailNickname")) {
      grouperAzureGroup.setMailNickname(GrouperUtil.jsonJacksonGetString(requestJsonNode, "mailNickname"));
    }
    if (requestJsonNode.has("securityEnabled")) {
      grouperAzureGroup.setSecurityEnabled(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "securityEnabled"));
    }
    if (requestJsonNode.has("visibility")) {
      grouperAzureGroup.setVisibilityDb(GrouperUtil.jsonJacksonGetString(requestJsonNode, "visibility"));
    }
    HibernateSession.byObjectStatic().saveOrUpdate(grouperAzureGroup);
    
    return new MultiKey(204, null);
  }

  public void patchGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String requestJsonString = mockServiceRequest.getRequestBody();
    JsonNode requestJsonNode = GrouperUtil.jsonJacksonNode(requestJsonString);
    
    if (requestJsonNode.has("members@odata.bind")) {
      patchMemberships(mockServiceRequest, mockServiceResponse, requestJsonNode);
      return;
    }
    
    // patch a group
    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);

    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperAzureGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperAzureGroups));
    }
    GrouperAzureGroup grouperAzureGroup = grouperAzureGroups.get(0);

    // only update fields if they are in the patch
    if (requestJsonNode.has("description")) {
      grouperAzureGroup.setDescription(GrouperUtil.jsonJacksonGetString(requestJsonNode, "description"));
    }
    if (requestJsonNode.has("displayName")) {
      grouperAzureGroup.setDisplayName(GrouperUtil.jsonJacksonGetString(requestJsonNode, "displayName"));
    }
    if (requestJsonNode.has("groupTypes")) {
      ArrayNode groupTypesArrayNode = (ArrayNode)requestJsonNode.get("groupTypes");
      Set<String> groupTypesSet = new HashSet<String>();
      for (int i=0;i<groupTypesArrayNode.size();i++) {
        String groupType = groupTypesArrayNode.get(i).asText();
        groupTypesSet.add(groupType);
      }
     
      grouperAzureGroup.setGroupTypeUnified(groupTypesSet.contains("Unified"));
      grouperAzureGroup.setGroupTypeDynamic(groupTypesSet.contains("Dynamic"));
    }
    if (requestJsonNode.has("id")) {
      throw new RuntimeException("Cant update the id field!");
    }
    if (requestJsonNode.has("mailEnabled")) {
      grouperAzureGroup.setMailEnabled(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "mailEnabled"));
    }
    if (requestJsonNode.has("isAssignableToRole")) {
      grouperAzureGroup.setAssignableToRole(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "isAssignableToRole"));
    }
    if (requestJsonNode.has("mailNickname")) {
      grouperAzureGroup.setMailNickname(GrouperUtil.jsonJacksonGetString(requestJsonNode, "mailNickname"));
    }
    if (requestJsonNode.has("securityEnabled")) {
      grouperAzureGroup.setSecurityEnabled(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "securityEnabled"));
    }
    if (requestJsonNode.has("visibility")) {
      grouperAzureGroup.setVisibilityDb(GrouperUtil.jsonJacksonGetString(requestJsonNode, "visibility"));
    }
    HibernateSession.byObjectStatic().saveOrUpdate(grouperAzureGroup);
    
    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");

    
  }
  
  public MultiKey patchMemberships(String groupId, JsonNode odataJsonNode) {
    
    GrouperUtil.assertion(odataJsonNode.has("members@odata.bind"), "members@odata.bind is required");
    
    ArrayNode membersNode = (ArrayNode)odataJsonNode.get("members@odata.bind");

    GrouperUtil.assertion(membersNode.size() > 0, "members@odata.bind needs elements");

    int maxSize = Math.min(20, GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureMembershipPagingSize", 20));
    
    GrouperUtil.assertion(membersNode.size() <= maxSize, "members@odata.bind cannot be more than " + maxSize);

    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      return new MultiKey(404, null) ;
    }
    
    int responseCode = 204;
    
    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint");
    
    for (int i=0;i<membersNode.size();i++) {

      String url = membersNode.get(i).asText();
      GrouperUtil.assertion(url.startsWith(GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/"), "@odata.id must start with " + GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/");
      String userId = GrouperUtil.prefixOrSuffix(url, GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/", false);
      
      List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery(
          "from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
          .setString("theGroupId", groupId).setString("theUserId", userId).list(GrouperAzureMembership.class);

      if (GrouperUtil.length(grouperAzureMemberships) > 0) {
        responseCode = 400;
        continue;
      }

      List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
          "from GrouperAzureUser where id = :theId")
          .setString("theId", userId).list(GrouperAzureUser.class);
      
      if (GrouperUtil.length(grouperAzureUsers) == 0) {
        responseCode = 404;
        continue;
      }

      GrouperAzureMembership grouperAzureMembership = new GrouperAzureMembership();
      grouperAzureMembership.setId(GrouperUuid.getUuid());
      grouperAzureMembership.setGroupId(groupId);
      grouperAzureMembership.setUserId(userId);
      HibernateSession.byObjectStatic().save(grouperAzureMembership);
      
    }

    return new MultiKey(responseCode, null);
  }
     
  public void patchMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, JsonNode odataJsonNode) {
  
    //  PATCH https://graph.microsoft.com/v1.0/groups/{group-id}
    //  Content-type: application/json
    //  Content-length: 30
    //
    //  {
    //    "members@odata.bind": [
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}"
    //      ]
    //  }
      
    //check require args
    GrouperUtil.assertion(odataJsonNode.has("members@odata.bind"), "members@odata.bind is required");
    
    ArrayNode membersNode = (ArrayNode)odataJsonNode.get("members@odata.bind");

    GrouperUtil.assertion(membersNode.size() > 0, "members@odata.bind needs elements");

    mockServiceRequest.getDebugMap().put("members", membersNode.size());

    int maxSize = Math.min(20, GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureMembershipPagingSize", 20));
    
    GrouperUtil.assertion(membersNode.size() <= maxSize, "members@odata.bind cannot be more than " + maxSize);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);

    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    int responseCode = 204;
    
    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint");
    
    for (int i=0;i<membersNode.size();i++) {

      String url = membersNode.get(i).asText();
      GrouperUtil.assertion(url.startsWith(GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/"), "@odata.id must start with " + GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/");
      String userId = GrouperUtil.prefixOrSuffix(url, GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/", false);
      
      List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery(
          "from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
          .setString("theGroupId", groupId).setString("theUserId", userId).list(GrouperAzureMembership.class);

      if (GrouperUtil.length(grouperAzureMemberships) > 0) {
        responseCode = 400;
        continue;
      }

      List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
          "from GrouperAzureUser where id = :theId")
          .setString("theId", userId).list(GrouperAzureUser.class);
      
      if (GrouperUtil.length(grouperAzureUsers) == 0) {
        mockServiceRequest.getDebugMap().put("cantFindUser", true);
        responseCode = 404;
        continue;
      }

      GrouperAzureMembership grouperAzureMembership = new GrouperAzureMembership();
      grouperAzureMembership.setId(GrouperUuid.getUuid());
      grouperAzureMembership.setGroupId(groupId);
      grouperAzureMembership.setUserId(userId);
      HibernateSession.byObjectStatic().save(grouperAzureMembership);
      
    }

    mockServiceResponse.setResponseCode(responseCode);
  
    
  }

  public MultiKey postMembership(JsonNode body, String groupId) {
    
    JsonNode odataJsonNode = body;
    
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id")) > 0, "@odata.id is required");

    String resourceEndpointDirectoryObjects = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint")) + "/directoryObjects/";
    
    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id").startsWith(resourceEndpointDirectoryObjects), "@odata.id must start with " + resourceEndpointDirectoryObjects);

    String userId = GrouperUtil.prefixOrSuffix(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id"), resourceEndpointDirectoryObjects, false);

    List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", groupId).setString("theUserId", userId).list(GrouperAzureMembership.class);

    if (GrouperUtil.length(grouperAzureMemberships) > 0) {
      return new MultiKey(400, null) ;
    }

    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      return new MultiKey(404, null) ;
    }
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureUser where id = :theId")
        .setString("theId", userId).list(GrouperAzureUser.class);
    
    if (GrouperUtil.length(grouperAzureUsers) == 0) {
      return new MultiKey(404, null) ;
    }

    GrouperAzureMembership grouperAzureMembership = new GrouperAzureMembership();
    grouperAzureMembership.setId(GrouperUuid.getUuid());
    grouperAzureMembership.setGroupId(groupId);
    grouperAzureMembership.setUserId(userId);
    HibernateSession.byObjectStatic().save(grouperAzureMembership);
    
    return new MultiKey(204, null) ;
    
  }
  
  public void postMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    //  {
    //    "@odata.id": "https://graph.microsoft.com/v1.0/directoryObjects/<someUserId>"
    //  }
    
    String odataJsonString = mockServiceRequest.getRequestBody();
    JsonNode odataJsonNode = GrouperUtil.jsonJacksonNode(odataJsonString);
  
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id")) > 0, "@odata.id is required");

    String resourceEndpointDirectoryObjects = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint")) + "/directoryObjects/";
    
    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id").startsWith(resourceEndpointDirectoryObjects), "@odata.id must start with " + resourceEndpointDirectoryObjects);

    String userId = GrouperUtil.prefixOrSuffix(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id"), resourceEndpointDirectoryObjects, false);

    mockServiceRequest.getDebugMap().put("userId", userId);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);
    
    List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", groupId).setString("theUserId", userId).list(GrouperAzureMembership.class);

    if (GrouperUtil.length(grouperAzureMemberships) > 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }

    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureUser where id = :theId")
        .setString("theId", userId).list(GrouperAzureUser.class);
    
    if (GrouperUtil.length(grouperAzureUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }

    GrouperAzureMembership grouperAzureMembership = new GrouperAzureMembership();
    grouperAzureMembership.setId(GrouperUuid.getUuid());
    grouperAzureMembership.setGroupId(groupId);
    grouperAzureMembership.setUserId(userId);
    HibernateSession.byObjectStatic().save(grouperAzureMembership);
    
    mockServiceResponse.setResponseCode(204);
  
    
  }
  
  public MultiKey deleteMembership(String userId, String groupId) {
    
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
  
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    int deletedCount = HibernateSession.byHqlStatic().createQuery(
        "delete from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", groupId).setString("theUserId", userId).executeUpdateInt();
  
    if (deletedCount > 0) {
      return new MultiKey(204, null);
    }
  
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
    }
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureUser where id = :theId")
        .setString("theId", userId).list(GrouperAzureUser.class);
    
    if (GrouperUtil.length(grouperAzureUsers) == 0) {
    }

    return new MultiKey(404, null) ;
    
  }

  public void deleteMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);
  
    String userId = mockServiceRequest.getPostMockNamePaths()[3];
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
  
    mockServiceRequest.getDebugMap().put("userId", userId);
  
    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "userId is required");
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);
    
    int deletedCount = HibernateSession.byHqlStatic().createQuery(
        "delete from GrouperAzureMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", groupId).setString("theUserId", userId).executeUpdateInt();
  
    if (deletedCount > 0) {
      mockServiceResponse.setResponseCode(204);
      return;
    }
  
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureGroup where id = :theId")
        .setString("theId", groupId).list(GrouperAzureGroup.class);
    
    if (GrouperUtil.length(grouperAzureGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
    }
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAzureUser where id = :theId")
        .setString("theId", userId).list(GrouperAzureUser.class);
    
    if (GrouperUtil.length(grouperAzureUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
    }

    mockServiceRequest.getDebugMap().put("cantFindMembership", true);
    mockServiceResponse.setResponseCode(404);
  }

  public void getGroupMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "id is required");
    GrouperUtil.assertion("id".equals(mockServiceRequest.getHttpServletRequest().getParameter("$select")), "$select must equal 'id'");

    // GET /groups/{id}/members
    //  $top=5&$skiptoken=X%274453707 ... 6633B900000000
    int pageSize = Math.min(GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("$top"), 100), 100);
    pageSize = Math.min(pageSize, GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetMembershipPagingSize", 100));

    mockServiceRequest.getDebugMap().put("pageSize", pageSize);

    String skipToken = mockServiceRequest.getHttpServletRequest().getParameter("$skiptoken");
    mockServiceRequest.getDebugMap().put("skipToken", skipToken);

    List<GrouperAzureMembership> grouperAzureMemberships = null;
    
    // get one more than page size to see if there is more data :)
    if (StringUtils.isBlank(skipToken)) {
      grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership where groupId = :theGroupId")
          .setString("theGroupId", groupId).options(QueryOptions.create("userId", true, 1, pageSize+1)).list(GrouperAzureMembership.class);
    } else {
      grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership where groupId = :theGroupId and userId > :skipToken")
          .setString("theGroupId", groupId).setString("skipToken", skipToken).options(QueryOptions.create("userId", true, 1, pageSize+1)).list(GrouperAzureMembership.class);
    }

    mockServiceRequest.getDebugMap().put("resultSize", GrouperUtil.length(grouperAzureMemberships));

    mockServiceResponse.setContentType("application/json");

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();

    // if theres more, send the nextLink
    if (GrouperUtil.length(grouperAzureMemberships) == pageSize + 1) {
      
      // take off the last one
      grouperAzureMemberships.remove(grouperAzureMemberships.size()-1);
      
      // e.g. http://localhost:8400/grouper/mockServices/azure
      String azureLink = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + this.configId + ".resourceEndpoint"));

      String odataNextLink = azureLink + "/groups/" + GrouperUtil.escapeUrlEncode(groupId) 
        + "/members?$skiptoken=" + GrouperUtil.escapeUrlEncode(grouperAzureMemberships.get(grouperAzureMemberships.size()-1).getUserId())
        + "&$top=" + pageSize + "&$select=id";
      
      mockServiceRequest.getDebugMap().put("odataNextLink", odataNextLink);
      
      resultNode.put("@odata.nextLink", odataNextLink);
      
      
    }

    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector." + this.configId + ".resourceEndpoint");

    resultNode.put("@odata.context", GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/$metadata#directoryObjects");

    mockServiceResponse.setResponseCode(200);

    if (GrouperUtil.length(grouperAzureMemberships) > 0) {
    
      ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();


      //  {
      //    "id": "11111111-2222-3333-4444-555555555555"
      //  }

      for (GrouperAzureMembership grouperAzureMembership : grouperAzureMemberships) {
        ObjectNode membershipNode = GrouperUtil.jsonJacksonNode();
        membershipNode.put("id", grouperAzureMembership.getUserId());
        valueNode.add(membershipNode);
      }
      
      resultNode.set("value", valueNode);

    }
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  
  }
  
  public MultiKey postUserGroups(String userId) {
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser where id = :theId or userPrincipalName = :theId2")
        .setString("theId", userId).setString("theId2", userId).list(GrouperAzureUser.class);
  
    if (GrouperUtil.length(grouperAzureUsers) == 1) {
      //  {
      //    "value": [
      //      "11111111-2222-3333-4444-555555555555",
      //      "12334-3452-43345-352345345-345345345"]
      //  }

      int azureGetUserGroupsMax = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetUserGroupsMax", 2046);

      List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership where userId = :theUserId")
        .setString("theUserId", userId).options(QueryOptions.create("userId", true, 1, azureGetUserGroupsMax)).list(GrouperAzureMembership.class);
        
  
      ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
      if (GrouperUtil.length(grouperAzureMemberships) > 0) {
        ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
        for (GrouperAzureMembership grouperAzureMembership : grouperAzureMemberships) {
          valueNode.add(grouperAzureMembership.getGroupId());
        }
        objectNode.set("value", valueNode);
      }
      
      return new MultiKey(200, objectNode);
  
    } else if (GrouperUtil.length(grouperAzureUsers) == 0) {
      return new MultiKey(404, null);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(grouperAzureUsers) + ", id: " + userId);
    }
    
  }

  /**
   * get groups for a user
   * @param mockServiceRequest
   * @param mockServiceResponse
   */
  public void postUserGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    checkAuthorization(mockServiceRequest);
    
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<GrouperAzureUser> grouperAzureUsers = HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser where id = :theId or userPrincipalName = :theId2")
        .setString("theId", userId).setString("theId2", userId).list(GrouperAzureUser.class);
  
    if (GrouperUtil.length(grouperAzureUsers) == 1) {
      
      mockServiceResponse.setResponseCode(200);
      
      //  {
      //    "value": [
      //      "11111111-2222-3333-4444-555555555555",
      //      "12334-3452-43345-352345345-345345345"]
      //  }

      int azureGetUserGroupsMax = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetUserGroupsMax", 2046);

      List<GrouperAzureMembership> grouperAzureMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership where userId = :theUserId")
        .setString("theUserId", userId).options(QueryOptions.create("userId", true, 1, azureGetUserGroupsMax)).list(GrouperAzureMembership.class);
        
      mockServiceResponse.setContentType("application/json");
  
      ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
      if (GrouperUtil.length(grouperAzureMemberships) > 0) {
        ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
        for (GrouperAzureMembership grouperAzureMembership : grouperAzureMemberships) {
          valueNode.add(grouperAzureMembership.getGroupId());
        }
        objectNode.set("value", valueNode);
      }
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
    } else if (GrouperUtil.length(grouperAzureUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(grouperAzureUsers) + ", id: " + userId);
    }
  
  }
}

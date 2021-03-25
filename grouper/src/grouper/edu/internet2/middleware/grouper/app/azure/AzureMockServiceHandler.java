package edu.internet2.middleware.grouper.app.azure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.morphString.Morph;

public class AzureMockServiceHandler extends MockServiceHandler {

  public AzureMockServiceHandler() {
  }

  /**
   * 
   */
  private static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  private static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

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
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
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
      throw new RuntimeException("Invalid access token, not found!");
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

    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "mailNickname")) > 0, "mailNickname is required");
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "mailNickname")) <= 64, "displayName must be less than 64");

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
        "grouper.azureConnector.azure1.resourceEndpoint");

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
    
    Pattern clientIdPattern = Pattern.compile("^grouper\\.azureConnector\\.([^.]+)\\.clientId$");
    String configId = null;
    for (String propertyName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
      
      Matcher matcher = clientIdPattern.matcher(propertyName);
      if (matcher.matches()) {
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(propertyName), clientId)) {
          configId = matcher.group(1);
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("Cant find client id!");
    }

    String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientSecret");
    clientSecret = Morph.decryptIfFile(clientSecret);
    if (!StringUtils.equals(clientSecret, mockServiceRequest.getHttpServletRequest().getParameter("client_secret"))) {
      throw new RuntimeException("Cant find client secret!");
    }
    
    String tenantId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".tenantId");
    
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
    String resourceConfig = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".resource");
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
    grouperAzureAuth.setConfigId(configId);
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
      grouperAzureGroup.setGroupTypeMailEnabled(groupTypesSet.contains("MailEnabled"));
      grouperAzureGroup.setGroupTypeMailEnabledSecurity(groupTypesSet.contains("MailEnabledSecurity"));
      grouperAzureGroup.setGroupTypeSecurity(groupTypesSet.contains("Security"));
      grouperAzureGroup.setGroupTypeUnified(groupTypesSet.contains("Unified"));
    }
    if (requestJsonNode.has("id")) {
      throw new RuntimeException("Cant update the id field!");
    }
    if (requestJsonNode.has("mailEnabled")) {
      grouperAzureGroup.setMailEnabled(GrouperUtil.jsonJacksonGetBoolean(requestJsonNode, "mailEnabled"));
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
        "grouper.azureConnector.azure1.resourceEndpoint");
    
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
        "grouper.azureConnector.azure1.resourceEndpoint")) + "/directoryObjects/";
    
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
      String azureLink = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.azure1.resourceEndpoint"));

      String odataNextLink = azureLink + "/groups/" + GrouperUtil.escapeUrlEncode(groupId) 
        + "/members?$skiptoken=" + GrouperUtil.escapeUrlEncode(grouperAzureMemberships.get(grouperAzureMemberships.size()-1).getUserId())
        + "&$top=" + pageSize + "&$select=id";
      
      mockServiceRequest.getDebugMap().put("odataNextLink", odataNextLink);
      
      resultNode.put("@odata.nextLink", odataNextLink);
      
      
    }

    String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.azureConnector.azure1.resourceEndpoint");

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

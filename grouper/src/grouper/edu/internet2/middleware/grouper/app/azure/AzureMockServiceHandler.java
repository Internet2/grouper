package edu.internet2.middleware.grouper.app.azure;

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
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.morphString.Morph;

public class AzureMockServiceHandler implements MockServiceHandler {

  public AzureMockServiceHandler() {
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
  
  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }

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
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
    
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
    resultNode.put("@odata.context", "https://graph.microsoft.com/v1.0/$metadata#groups");
    
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
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
    
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
      throw new RuntimeException("Cant invalid client secret!");
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
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }

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
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
    
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
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
    
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

  public void postMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
  
    //  {
    //    "@odata.id": "https://graph.microsoft.com/v1.0/directoryObjects/<someUserId>"
    //  }
    
    String odataJsonString = mockServiceRequest.getRequestBody();
    JsonNode odataJsonNode = GrouperUtil.jsonJacksonNode(odataJsonString);
  
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id")) > 0, "@odata.id is required");
    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id").startsWith("https://graph.microsoft.com/v1.0/directoryObjects/"), "@odata.id must start with https://graph.microsoft.com/v1.0/directoryObjects/");

    String userId = GrouperUtil.prefixOrSuffix(GrouperUtil.jsonJacksonGetString(odataJsonNode, "@odata.id"), "https://graph.microsoft.com/v1.0/directoryObjects/", false);

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
}

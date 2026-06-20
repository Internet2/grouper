package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GenericScim2MockServiceHandler extends MockServiceHandler {

  /**
   * Read a sync-back test knob fresh from the DB config. Clears the GrouperConfig cache (files +
   * DB overlay) first, because this mock runs in a long-lived Tomcat JVM that otherwise only
   * re-checks the DB every grouper.config.secondsBetweenUpdateChecksToDb (~30s) -- far longer than
   * a test runs, so a cached read would miss a knob the test just stored. Returns false if unset.
   * @param configKey the grouper.properties key the test stored via GrouperDbConfig
   * @return the knob value, false if absent or blank
   */
  private static boolean mockTestKnobBoolean(String configKey) {
    ConfigPropertiesCascadeBase.clearCache();
    return GrouperConfig.retrieveConfig().propertyValueBoolean(configKey, false);
  }

  public GenericScim2MockServiceHandler() {
  }

  /**
   * matches the pathEmailsQualified patch path, e.g. emails[type eq "work"].value
   */
  private static final Pattern EMAILS_QUALIFIED_PATTERN = Pattern.compile("^emails\\[type\\s+eq\\s+\"([^\"]+)\"\\]\\.value$");

  /**
   * 
   */
  private static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

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
  public static void ensureScimMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_scim_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_scim_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_scim_membership").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_scim_capture").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperScim2User.createTableScimUser(ddlVersionBean, database);
          GrouperScim2Group.createTableScimGroup(ddlVersionBean, database);
          GrouperScim2Membership.createTableScimMembership(ddlVersionBean, database);
          createTableScimCapture(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * capture table that records the last outgoing request the mock saw, so tests running in a
   * different JVM than the (separate process) Tomcat mock can assert the exact outgoing request.
   * the database (grouper connection) is the only channel shared between the two JVMs.
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableScimCapture(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_scim_capture";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "capture_key", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "capture_value", Types.VARCHAR, "4000", false, false);
    }
  }

  /**
   * record the last outgoing request the mock saw for a given key (overwrites previous value).
   * best effort - never break the primary mock behavior if capture fails.
   * @param key
   * @param value
   */
  public static void recordCapture(String key, String value) {
    try {
      new GcDbAccess().sql("delete from mock_scim_capture where capture_key = ?").addBindVar(key).executeSql();
      new GcDbAccess().sql("insert into mock_scim_capture (capture_key, capture_value) values (?, ?)")
          .addBindVar(key).addBindVar(StringUtils.abbreviate(value, 4000)).executeSql();
    } catch (Exception e) {
      // best effort capture only
    }
  }

  /**
   * 
   */
  public static void dropScimMockTables() {
    MockServiceServlet.dropMockTable("mock_scim_capture");
    MockServiceServlet.dropMockTable("mock_scim_membership");
    MockServiceServlet.dropMockTable("mock_scim_group");
    MockServiceServlet.dropMockTable("mock_scim_user");
  }
  
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureScimMockTables();
    }
    mockTablesThere = true;
    
    
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
   
    GrouperUtil.assertion(mockNamePaths.size() >= 1, "Must start with v2/");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "v2"), "");
    
    mockNamePaths = mockNamePaths.subList(1, mockNamePaths.size());
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);
    
    try {
      String externalSystemConfigId = WsBearerTokenExternalSystem.authenticateMockUser(mockServiceRequest.getHttpServletRequest());
      
      Pattern pattern = Pattern.compile("^provisioner\\.([^\\.]+)\\.bearerTokenExternalSystemConfigId$");
      Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
      String provisionerConfigId = null;
      for (String configId: configIds) {
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner."+configId+".bearerTokenExternalSystemConfigId"), externalSystemConfigId)) {
          GrouperUtil.assertion(provisionerConfigId == null, "Multiple provisioners match this bearer token external system: "+externalSystemConfigId);
          provisionerConfigId = configId;
        }
      }
      GrouperUtil.assertion(provisionerConfigId != null, "Cannot find a provisioner that uses bearer token external system: "+externalSystemConfigId);
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      if (grouperProvisioner != null) {
        GrouperProvisioner.assignCurrentGrouperProvisioner(grouperProvisioner);
        grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
      }

      if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
        if ("ServiceProviderConfig".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
          getServiceProviderConfig(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
          getUsers(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          getUser(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
          getGroups(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          getGroup(mockServiceRequest, mockServiceResponse);
          return;
        }
      }
      if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
        if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          deleteUser(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          deleteGroup(mockServiceRequest, mockServiceResponse);
          return;
        }
      }
      if (StringUtils.equals("PATCH", mockServiceRequest.getHttpServletRequest().getMethod())) {
        if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          patchUser(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
          patchGroup(mockServiceRequest, mockServiceResponse);
          return;
        }
      }
      if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
        if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
          postUsers(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
          postGroups(mockServiceRequest, mockServiceResponse);
          return;
        }
      }
  
      throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
          + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    } finally {
      GrouperProvisioner.removeCurrentGrouperProvisioner();
    }
  }

  public boolean checkAuthorization(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    String configId = WsBearerTokenExternalSystem.authenticateMockUser(mockServiceRequest.getHttpServletRequest());
    
    if (StringUtils.isBlank(configId)) {
      mockServiceRequest.getDebugMap().put("authnError", "Cant find client id!  WS bearer token external system not configured or invalid secret!");
      mockServiceResponse.setResponseCode(401);
      return false;
    }

    // all good
    return true;
  }
  
  public void getServiceProviderConfig(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "schemas":[
    //       "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"
    //    ],
    //    "documentationUri":"https://docs.aws.amazon.com/singlesignon/latest/userguide/manage-your-identity-source-idp.html",
    //    "authenticationSchemes":[
    //       {
    //          "type":"oauthbearertoken",
    //          "name":"OAuth Bearer Token",
    //          "description":"Authentication scheme using the OAuth Bearer Token Standard",
    //          "specUri":"https://www.rfc-editor.org/info/rfc6750",
    //          "documentationUri":"https://docs.aws.amazon.com/singlesignon/latest/userguide/provision-automatically.html",
    //          "primary":true
    //       }
    //    ],
    //    "patch":{
    //       "supported":true
    //    },
    //    "bulk":{
    //       "supported":false,
    //       "maxOperations":1,
    //       "maxPayloadSize":1048576
    //    },
    //    "filter":{
    //       "supported":true,
    //       "maxResults":50
    //    },
    //    "changePassword":{
    //       "supported":false
    //    },
    //    "sort":{
    //       "supported":false
    //    },
    //    "etag":{
    //       "supported":false
    //    }
    // }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig");
      resultNode.set("schemas", schemasNode);
    }
    
    resultNode.put("documentationUri", "https://docs.aws.amazon.com/singlesignon/latest/userguide/manage-your-identity-source-idp.html");
    
    {
      ObjectNode authenticationSchemeNode = GrouperUtil.jsonJacksonNode();
      authenticationSchemeNode.put("type", "oauthbearertoken");
      authenticationSchemeNode.put("name", "OAuth Bearer Token");
      authenticationSchemeNode.put("description", "Authentication scheme using the OAuth Bearer Token Standard");
      authenticationSchemeNode.put("specUri", "https://www.rfc-editor.org/info/rfc6750");
      authenticationSchemeNode.put("documentationUri", "https://docs.aws.amazon.com/singlesignon/latest/userguide/provision-automatically.html");
      authenticationSchemeNode.put("primary", true);
      ArrayNode authenticationSchemesArray = GrouperUtil.jsonJacksonArrayNode();
      authenticationSchemesArray.add(authenticationSchemeNode);
      resultNode.set("authenticationSchemes", authenticationSchemesArray);
      
    }

    {
      ObjectNode patchNode = GrouperUtil.jsonJacksonNode();
      patchNode.put("supported", true);
      resultNode.set("patch", patchNode);
    }
    {
      ObjectNode bulkNode = GrouperUtil.jsonJacksonNode();
      bulkNode.put("supported", false);
      bulkNode.put("maxOperations", 1);
      bulkNode.put("maxPayloadSize", 1048576);
      resultNode.set("bulk", bulkNode);
    }
    {
      ObjectNode filterNode = GrouperUtil.jsonJacksonNode();
      filterNode.put("supported", true);
      filterNode.put("maxResults", 50);
      resultNode.set("filter", filterNode);
    }
    {
      ObjectNode changePasswordNode = GrouperUtil.jsonJacksonNode();
      changePasswordNode.put("supported", false);
      resultNode.set("changePassword", changePasswordNode);
    }
    {
      ObjectNode sortNode = GrouperUtil.jsonJacksonNode();
      sortNode.put("supported", false);
      resultNode.set("sort", sortNode);
    }
    {
      ObjectNode etagNode = GrouperUtil.jsonJacksonNode();
      etagNode.put("supported", false);
      resultNode.set("etag", etagNode);
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "active":true,
    //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
    //       "employeeNumber":"12345",
    //       "costCenter":"costCent"
    //    },
    //    "id":"i",
    //    "displayName":"dispName",
    //    "emails":[
    //       {
    //          "value":"emailVal",
    //          "primary":true,
    //          "type":"emailTy"
    //       }
    //    ],
    //    "name":{
    //       "formatted":"formName",
    //       "familyName":"famName",
    //       "givenName":"givName",
    //       "middleName":"midName"
    //    },
    //    "externalId":"extId",
    //    "userName":"userNam",
    //    "userType":"userTyp"
    // }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
  
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "id")) == 0, "id is forbidden");
  
    GrouperScim2User grouperScimUser = GrouperScim2User.fromJson(userJsonNode);

    // GRP-7062: real SCIM targets reject a create that collides with an existing resource using
    // HTTP 409 with scimType "uniqueness".  Mirror that here so the create-conflict recovery path
    // can be exercised: if a user with the same externalId or userName already exists, return 409
    // rather than silently creating a duplicate.
    GrouperScim2User existingConflict = findExistingUserForUniqueness(grouperScimUser);
    if (existingConflict != null) {
      ObjectNode errorNode = GrouperUtil.jsonJacksonNode();
      ArrayNode errorSchemasNode = GrouperUtil.jsonJacksonArrayNode();
      errorSchemasNode.add("urn:ietf:params:scim:api:messages:2.0:Error");
      errorNode.set("schemas", errorSchemasNode);
      errorNode.put("scimType", "uniqueness");
      errorNode.put("detail", "User already exists");
      errorNode.put("status", "409");

      mockServiceResponse.setResponseCode(409);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(errorNode));
      return;
    }

    grouperScimUser.setId(GrouperUuid.getUuid());

    HibernateSession.byObjectStatic().save(grouperScimUser);

    JsonNode resultNode = grouperScimUser.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));


  }

  /**
   * GRP-7062: find an already-existing mock user that a create would collide with, matching on
   * externalId first (stable across userName renames) and then userName.  Returns null if there is
   * no collision (the normal create case).
   * @param grouperScimUser the user being created
   * @return the existing colliding user, or null
   */
  private GrouperScim2User findExistingUserForUniqueness(GrouperScim2User grouperScimUser) {

    String externalId = grouperScimUser.getExternalId();
    if (StringUtils.isNotBlank(externalId)) {
      List<GrouperScim2User> matches = HibernateSession.byHqlStatic()
          .createQuery("from GrouperScim2User where externalId = :theValue")
          .setString("theValue", externalId).list(GrouperScim2User.class);
      if (GrouperUtil.length(matches) > 0) {
        return matches.get(0);
      }
    }

    String userName = grouperScimUser.getUserName();
    if (StringUtils.isNotBlank(userName)) {
      List<GrouperScim2User> matches = HibernateSession.byHqlStatic()
          .createQuery("from GrouperScim2User where userName = :theValue")
          .setString("theValue", userName).list(GrouperScim2User.class);
      if (GrouperUtil.length(matches) > 0) {
        return matches.get(0);
      }
    }

    return null;
  }

  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String filter = mockServiceRequest.getHttpServletRequest().getParameter("filter");
    
    // capture the exact outgoing filter so tests (in a different JVM) can assert it
    recordCapture("lastUsersFilter", filter);
    
    List<GrouperScim2User> grouperScimUsers = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperScimUsers = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class);
    } else {
      String field = null;
      String value = null;

      // which email-filter format the incoming filter represents (null if it is not an email
      // filter, e.g. a userName lookup); used to enforce grouperTest.scim2.mock.emailFilterStrategy.mode
      String emailFilterFormat = null;

      // Parse the SCIM filter into a field name and value.
      // Different SCIM clients send email filters in different formats depending on
      // the scimEmailFilterStrategy config. We need to handle all of them.

      // SCIM 2.0 bracket notation without type qualifier, e.g.: emails[value eq "someone@example.com"]
      // Captures the email value inside the brackets. Maps to HQL field "emailValue".
      Pattern bracketValuePattern = Pattern.compile("^emails\\[value\\s+eq\\s+\"(.+)\"\\]$");

      // SCIM 2.0 bracket notation with type qualifier, e.g.: emails[type eq "work" and value eq "someone@example.com"]
      // Ignores the type value (e.g. "work") and captures only the email value. Maps to HQL field "emailValue".
      Pattern bracketTypeValuePattern = Pattern.compile("^emails\\[type\\s+eq\\s+\"[^\"]+\"\\s+and\\s+value\\s+eq\\s+\"(.+)\"\\]$");

      // Simple SCIM filter format, e.g.: userName eq "jsmith" or email eq "someone@example.com"
      // Group 1 = field name, Group 2 = value.
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+\"(.+)\"$");

      // Try bracket patterns first since the simple pattern would also partially match bracket syntax
      Matcher matcher = bracketValuePattern.matcher(filter);
      if (matcher.matches()) {
        field = "emailValue";
        value = matcher.group(1);
        emailFilterFormat = "emails[value]";
      } else {
        matcher = bracketTypeValuePattern.matcher(filter);
        if (matcher.matches()) {
          field = "emailValue";
          value = matcher.group(1);
          emailFilterFormat = "emails[typeWork and value]";
        } else {
          // Fall back to simple "field eq value" format
          matcher = fieldPattern.matcher(filter);
          GrouperUtil.assertion(matcher.matches(), "SCIM filter doesnt match any expected format: '" + filter + "'");
          field = matcher.group(1);
          value = matcher.group(2);
          // Validate field name to prevent HQL injection — allow alphanumeric and dots (for emails.value)
          GrouperUtil.assertion(field.matches("^[a-zA-Z0-9.]+$"), "SCIM filter field name must be alphanumeric or dot notation: '" + field + "'");
          // Map non-standard email filter field names to the HQL column "emailValue" on GrouperScim2User.
          // "email" is non-standard but used by Qlik; "emails.value" is standard SCIM 2.0 dot notation.
          // Other fields like "userName" pass through directly since they match the HQL column names.
          if (StringUtils.equals(field, "email") || StringUtils.equals(field, "emails.value")) {
            emailFilterFormat = field;
            field = "emailValue";
          }
        }
      }
      value = StringEscapeUtils.unescapeJson(value);

      // strict-mode simulation: the mock honors exactly one email-filter strategy and rejects the
      // rest (returns no results), so diagnostics can discover which strategy the simulated SCIM
      // server actually supports. when grouperTest.scim2.mock.emailFilterStrategy.mode is not set,
      // the mock falls back to the config default "email" (grouper-loader.base.properties
      // scimEmailFilterStrategy.defaultValue) rather than accepting all formats. mode "none" rejects
      // every email filter (simulating e.g. GitHub, which does not support email filtering).
      // non-email lookups (e.g. userName) are never gated.
      String emailFilterMode = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.emailFilterStrategy.mode");
      if (StringUtils.isBlank(emailFilterMode)) {
        emailFilterMode = "email";
      }
      if (emailFilterFormat != null && !StringUtils.equals(emailFilterFormat, emailFilterMode)) {
        grouperScimUsers = new ArrayList<GrouperScim2User>();
      } else {
        grouperScimUsers = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User where " + field + " = :theValue").setString("theValue", value).list(GrouperScim2User.class);
      }
    }
    
    //  {
    //    "totalResults": 5,
    //    "Resources": [
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
    
    resultNode.put("totalResults", GrouperUtil.length(grouperScimUsers));
    resultNode.put("itemsPerPage", GrouperUtil.length(grouperScimUsers));
    resultNode.put("startIndex", 1);

    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
      resultNode.set("schemas", schemasNode);
    }
    
    ArrayNode resourcesNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2User grouperScimUser : grouperScimUsers) {
      ObjectNode userNode = grouperScimUser.toJson(null);
      resourcesNode.add(userNode);
      String membershipStrategy = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.membershipStrategy.mode");
      if (StringUtils.equals(membershipStrategy, "membershipsInUserObjectsWhenRetrievingAllUsers")) {
        attachGroupsToUser(userNode, grouperScimUser.getId());
      }
    }
    
    resultNode.set("Resources", resourcesNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperScim2User grouperScimUser = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2User.class);

    if (grouperScimUser == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    ObjectNode objectNode = grouperScimUser.toJson(null);
    String membershipStrategy = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.membershipStrategy.mode");
    if (StringUtils.equals(membershipStrategy, "membershipsInUserObjectsWhenRetrievingIndividualUsers")) {      
      attachGroupsToUser(objectNode, grouperScimUser.getId());
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
  }

  public void deleteUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
    
    if (mockTestKnobBoolean("grouperTest.scim2.mock.deleteUsersReturnSuccessButDoNotDelete")) {
      // sync-back test knob: ack the delete (204) but do NOT remove the row -- a broken SCIM
      // target. The record stays unchanged (still active), so the drain's re-read finds it
      // still there and the mirror keeps it rather than assuming the delete worked.
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseCode(204);
      return;
    }

    int membershipsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperScim2Membership where userId = :userId")
        .setString("userId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("membershipsDeleted", membershipsDeleted);
  
    int usersDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperScim2User where id = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("usersDeleted", usersDeleted);
    
    // not sure why but they set this content type even though no json in response
    mockServiceResponse.setContentType("application/json");

    if (usersDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (usersDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersDeleted: " + usersDeleted);
    }
        
  }

  public void patchUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    GrouperScim2User grouperScimUser = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2User.class);

    if (grouperScimUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceRequest.getDebugMap().put("foundUser", false);
      return;
    }
        
    mockServiceResponse.setContentType("application/json");
    
    //  {
    //    "schemas": [
    //        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
    //    ],
    //    "Operations": [
    //        {
    //            "op": "replace",
    //            "path": "active",
    //            "value": "false"
    //        }
    //    ]
    //  }
    
    String requestBodyString = mockServiceRequest.getRequestBody();
    
    // capture the exact outgoing patch body so tests (in a different JVM) can assert it
    recordCapture("lastUserPatchBody", requestBodyString);
    
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBodyString);

    ArrayNode schemasNode = (ArrayNode)requestNode.get("schemas");

    GrouperUtil.assertion(schemasNode.size() == 1, "schema is required");
    GrouperUtil.assertion("urn:ietf:params:scim:api:messages:2.0:PatchOp".equals(schemasNode.get(0).asText()), "schema is required");

    ArrayNode operationsNode = (ArrayNode)requestNode.get("Operations");

    GrouperUtil.assertion(operationsNode.size() > 0, "must send operations");

    // pathEmailsQualified can send one op per email address; track which slot to fill
    int qualifiedEmailOpCount = 0;

    // strict-mode simulation: the mock accepts exactly one strategy per dimension and rejects the
    // others, so diagnostics can discover which strategy the simulated SCIM server actually supports.
    // when grouperTest.scim2.mock.*Strategy.mode is not set, the mock falls back to the config
    // defaults (namePatch "nonqualified", emailPatch "pathEmails", per grouper-loader.base.properties)
    // rather than accepting all shapes.
    String namePatchMode = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.namePatchStrategy.mode");
    if (StringUtils.isBlank(namePatchMode)) {
      namePatchMode = "nonqualified";
    }
    String emailPatchMode = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.emailPatchStrategy.mode");
    if (StringUtils.isBlank(emailPatchMode)) {
      emailPatchMode = "pathEmails";
    }

    for (int i=0;i<operationsNode.size();i++) {
      
      JsonNode operation = operationsNode.get(i);
      
      //            "op": "replace",
      //            "path": "active",
      //            "value": "false"

      // replace, add, remove
      String op = GrouperUtil.jsonJacksonGetString(operation, "op");
      boolean opAdd = "add".equals(op);
      boolean opReplace = "replace".equals(op);
      boolean opRemove = "remove".equals(op);
      if (!opAdd && !opRemove && !opReplace) {
        throw new RuntimeException("Invalid op, expecting add, replace, remove, but received: '" + op + "'");
      }
      String path = GrouperUtil.jsonJacksonGetString(operation, "path");
      
      //  {
      //    "active":true,
      //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
      //       "employeeNumber":"12345",
      //       "costCenter":"costCent"   e.g. urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter
      //    },
      //    "id":"i",
      //    "displayName":"dispName",
      //    "emails":[
      //       {
      //          "value":"emailVal", emails.value eq "emailVal" or emails[value eq "emailVal"]
      //          "primary":true,
      //          "type":"emailTy"  emails.type eq "work" or emails[type eq "work"]
      //       }
      //    ],
      //    "name":{
      //       "formatted":"formName",    e.g. name.formatted
      //       "familyName":"famName",
      //       "givenName":"givName",
      //       "middleName":"midName"
      //    },
      //    "externalId":"extId",
      //    "userName":"userNam",
      //    "userType":"userTyp"
      // }
      
      GrouperUtil.assertion(!"id".equals(path), "cannot patch id");

      // strict-mode simulation: reject patch shapes that don't match the mock's configured
      // supported strategy, so diagnostics can detect which strategy actually works.
      {
        String namePatchVariant = null;
        if (opReplace && "name".equals(path)) {
          namePatchVariant = "nested";
        } else if (path != null && (path.equals("name.givenName") || path.equals("name.familyName")
            || path.equals("name.middleName") || path.equals("name.formatted") || path.equals("name.formattedName"))) {
          namePatchVariant = "qualified";
        } else if (path != null && (path.equals("givenName") || path.equals("familyName")
            || path.equals("middleName") || path.equals("formatted"))) {
          namePatchVariant = "nonqualified";
        }
        if (namePatchVariant != null && !StringUtils.equals(namePatchVariant, namePatchMode)) {
          throw new RuntimeException("Mock SCIM server (grouperTest.scim2.mock.namePatchStrategy.mode=" + namePatchMode
              + ") does not support name patch strategy '" + namePatchVariant + "', path: '" + path + "'");
        }

        String emailPatchVariant = null;
        JsonNode opValueNode = operation.get("value");
        if (opReplace && StringUtils.isBlank(path) && opValueNode != null && opValueNode.has("emails")) {
          emailPatchVariant = "noPath";
        } else if (path != null && EMAILS_QUALIFIED_PATTERN.matcher(path).matches()) {
          emailPatchVariant = "pathEmailsQualified";
        } else if (StringUtils.equals(path, "emails")) {
          emailPatchVariant = "pathEmails";
        }
        if (emailPatchVariant != null && !StringUtils.equals(emailPatchVariant, emailPatchMode)) {
          throw new RuntimeException("Mock SCIM server (grouperTest.scim2.mock.emailPatchStrategy.mode=" + emailPatchMode
              + ") does not support email patch strategy '" + emailPatchVariant + "', path: '" + path + "'");
        }
      }

      // nested name patch strategy (scimNamePatchStrategy=nested):
      //   { "op":"replace", "path":"name", "value":{ "givenName":..., "familyName":..., "middleName":..., "formatted":... } }
      if (opReplace && "name".equals(path)) {
        JsonNode nameValueNode = operation.get("value");
        GrouperUtil.assertion(nameValueNode != null && nameValueNode.isObject(), "expecting an object value for nested name patch");
        if (nameValueNode.has("formatted")) {
          grouperScimUser.setFormattedName(GrouperUtil.jsonJacksonGetString(nameValueNode, "formatted"));
        }
        if (nameValueNode.has("givenName")) {
          grouperScimUser.setGivenName(GrouperUtil.jsonJacksonGetString(nameValueNode, "givenName"));
        }
        if (nameValueNode.has("familyName")) {
          grouperScimUser.setFamilyName(GrouperUtil.jsonJacksonGetString(nameValueNode, "familyName"));
        }
        if (nameValueNode.has("middleName")) {
          grouperScimUser.setMiddleName(GrouperUtil.jsonJacksonGetString(nameValueNode, "middleName"));
        }
        continue;
      }

      // email noPath patch strategy (scimEmailPatchStrategy=noPath):
      //   { "op":"replace", "value":{ "emails":[ { "primary":true, "value":..., "type":... } ] } }  (no path)
      if (opReplace && StringUtils.isBlank(path)) {
        JsonNode valueNode = operation.get("value");
        if (valueNode != null && valueNode.has("emails")) {
          ArrayNode emailsArrayNode = (ArrayNode) valueNode.get("emails");
          grouperScimUser.setEmailType(null);
          grouperScimUser.setEmailValue(null);
          grouperScimUser.setEmailType2(null);
          grouperScimUser.setEmailValue2(null);
          for (int emailIndex = 0; emailIndex < emailsArrayNode.size(); emailIndex++) {
            JsonNode emailNode = emailsArrayNode.get(emailIndex);
            String emailValue = GrouperUtil.jsonJacksonGetString(emailNode, "value");
            String emailType = GrouperUtil.jsonJacksonGetString(emailNode, "type");
            if (emailIndex == 0) {
              grouperScimUser.setEmailValue(emailValue);
              grouperScimUser.setEmailType(emailType);
            } else {
              grouperScimUser.setEmailValue2(emailValue);
              grouperScimUser.setEmailType2(emailType);
            }
          }
          continue;
        }
      }

      // email pathEmailsQualified patch strategy (scimEmailPatchStrategy=pathEmailsQualified):
      //   { "op":"replace", "path":"emails[type eq \"work\"].value", "value":"someone@example.com" }
      Matcher emailsQualifiedMatcher = path == null ? null : EMAILS_QUALIFIED_PATTERN.matcher(path);
      if (opReplace && emailsQualifiedMatcher != null && emailsQualifiedMatcher.matches()) {
        String emailType = emailsQualifiedMatcher.group(1);
        String emailValue = GrouperUtil.jsonJacksonGetString(operation, "value");
        qualifiedEmailOpCount++;
        if (qualifiedEmailOpCount == 1) {
          grouperScimUser.setEmailValue(emailValue);
          grouperScimUser.setEmailType(emailType);
        } else {
          grouperScimUser.setEmailValue2(emailValue);
          grouperScimUser.setEmailType2(emailType);
        }
        continue;
      }

      // email pathEmails patch strategy (scimEmailPatchStrategy=pathEmails, the default):
      //   { "op":"replace", "path":"emails", "value":[ { "primary":true, "value":..., "type":... } ] }
      // the entire emails collection is replaced, so (unlike the qualified path) this does not
      // require a pre-existing email to be present
      if ((opReplace || opAdd) && StringUtils.equals(path, "emails")) {
        JsonNode valueNode = operation.get("value");
        if (valueNode != null && valueNode.isArray()) {
          ArrayNode emailsArrayNode = (ArrayNode) valueNode;
          grouperScimUser.setEmailType(null);
          grouperScimUser.setEmailValue(null);
          grouperScimUser.setEmailType2(null);
          grouperScimUser.setEmailValue2(null);
          for (int emailIndex = 0; emailIndex < emailsArrayNode.size(); emailIndex++) {
            JsonNode emailNode = emailsArrayNode.get(emailIndex);
            String emailValue = GrouperUtil.jsonJacksonGetString(emailNode, "value");
            String emailType = GrouperUtil.jsonJacksonGetString(emailNode, "type");
            if (emailIndex == 0) {
              grouperScimUser.setEmailValue(emailValue);
              grouperScimUser.setEmailType(emailType);
            } else {
              grouperScimUser.setEmailValue2(emailValue);
              grouperScimUser.setEmailType2(emailType);
            }
          }
          continue;
        }
      }

      //  costCenter : String
      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter".equals(path)) {
        path = "costCenter";
      }
      //  employeeNumber : String
      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.employeeNumber".equals(path)) {
        path = "employeeNumber";
      }

      //  familyName : String
      if ("name.familyName".equals(path)) {
        path = "familyName";
      }
      //  formattedName : String
      if ("name.formattedName".equals(path)) {
        path = "formattedName";
      }
      //  formattedName : String (scimNamePatchStrategy=qualified sends name.formatted)
      if ("name.formatted".equals(path)) {
        path = "formattedName";
      }
      //  formattedName : String (scimNamePatchStrategy=nonqualified sends the bare SCIM
      //  sub-attribute name 'formatted', which maps to the Java field 'formattedName')
      if ("formatted".equals(path)) {
        path = "formattedName";
      }
      //  givenName : String
      if ("name.givenName".equals(path)) {
        path = "givenName";
      }
      //  middleName : String
      if ("name.middleName".equals(path)) {
        path = "middleName";
      }
      
      if (path.startsWith("emails")) {
        // emailType : String
        // emailValue : String
        // emails[0]['value'] or emails.value eq "emailVal" or emails[value eq "emailVal"]
        
        JsonNode newEmailNode = operation.get("value");
        
        // validate the email
        if (opAdd) {
          
          // if theres an existing, thats bad
          if (!StringUtils.isBlank(grouperScimUser.getEmailValue()) || !StringUtils.isBlank(grouperScimUser.getEmailType())) {
            
            throw new RuntimeException("Adding email but already exists! " + grouperScimUser);
            
          }

          if (newEmailNode.has("type")) {
            grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
          }
          if (newEmailNode.has("value")) {
            grouperScimUser.setEmailValue(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
          }
          
        } else {
          grouperScimUser.validateEmail(path);

          if (StringUtils.isBlank(grouperScimUser.getEmailValue()) && StringUtils.isBlank(grouperScimUser.getEmailType())) {
            
            throw new RuntimeException(op + " email but not there! " + grouperScimUser);
            
          }

          if (opRemove) {
            
            grouperScimUser.setEmailType(null);
            grouperScimUser.setEmailValue(null);
            
          } else {
            
            //replace
            GrouperUtil.assertion(opReplace, "expecting replace");

            if (newEmailNode.isArray()) {
              GrouperUtil.assertion(newEmailNode.size() == 1, "expecting size 1 but was " + newEmailNode.size());
              newEmailNode = ((ArrayNode)newEmailNode).get(0);
            }
            if (newEmailNode.has("type")) {
              grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
            }
            if (newEmailNode.has("value")) {
              grouperScimUser.setEmailValue(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
            }
            
            
          }
          
        }
        
      } else {
        
        Object newValue = "active".equals(path) ? GrouperUtil.jsonJacksonGetBoolean(operation, "value") : GrouperUtil.jsonJacksonGetString(operation, "value");
        Object oldValue = GrouperUtil.fieldValue(grouperScimUser, path);
        
        // validate the email
        if (opAdd) {
          
          GrouperUtil.assertion(GrouperUtil.isBlank(oldValue), "add op already has value! " + path + ", '" + oldValue + "' " + grouperScimUser);
          
          GrouperUtil.assignField(grouperScimUser, path, newValue);
          
        } else if (opRemove) {

          GrouperUtil.assertion(!GrouperUtil.isBlank(oldValue), "remove op doesnt have value! " + path + ", '" + oldValue + "' " + grouperScimUser);
          GrouperUtil.assertion(newValue == null, "remove op should not have a value! " + path + ", '" + newValue + "' " + grouperScimUser);

          GrouperUtil.assignField(grouperScimUser, path, newValue);

        } else {

          // replace: per SCIM (RFC 7644 3.5.2.1) a replace on a single-valued attribute sets the
          // value whether or not one previously existed, so do not require a pre-existing value
          GrouperUtil.assignField(grouperScimUser, path, newValue);
        }
        
      }
      
    }
    HibernateSession.byObjectStatic().saveOrUpdate(grouperScimUser);

    if (mockTestKnobBoolean("grouperTest.scim2.mock.patchUsersReturnNoBody")) {
      // sync-back test knob: 204 No Content, no body -> the update has no representation, so
      // the write hook marks the id and the end-of-run drain re-reads it.
      mockServiceResponse.setResponseCode(204);
    } else {
      ObjectNode objectNode = grouperScimUser.toJson(null);
      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
    }
    
    
  }

  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "meta": {
    //        "resourceType": "Group",
    //        "created": "2020-04-06T16:48:19Z",
    //        "lastModified": "2020-04-06T16:48:19Z"
    //    },
    //    "schemas": [
    //        "urn:ietf:params:scim:schemas:core:2.0:Group"
    //    ],
    //    "displayName": "Group Bar"
    //  }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "id")) == 0, "id is forbidden");

    GrouperScim2Group grouperScimGroup = GrouperScim2Group.fromJson(groupJsonNode);
    grouperScimGroup.setId(GrouperUuid.getUuid());
    grouperScimGroup.setCreated(new Timestamp(System.currentTimeMillis()));
    grouperScimGroup.setLastModified(new Timestamp(System.currentTimeMillis()));

    HibernateSession.byObjectStatic().save(grouperScimGroup);
    
    if (grouperScimGroup.getCustomAttributes() != null && grouperScimGroup.getCustomAttributes().containsKey("custom_description")) {
      String serviceNowEmpNum = GrouperUtil.stringValue(grouperScimGroup.getCustomAttributes().get("custom_description"));
      String sql = "update mock_scim_group set description = ? where id = ?";
      new GcDbAccess().sql(sql).addBindVar(serviceNowEmpNum).addBindVar(grouperScimGroup.getId()).executeSql();
    }

    JsonNode resultNode = grouperScimGroup.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }

  public void deleteGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    int membershipsDeleted = HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperScim2Membership where groupId = :groupId")
    .setString("groupId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("membershipsDeleted", membershipsDeleted);
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperScim2Group where id = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("groupsDeleted", groupsDeleted);
    
    // not sure why but they set this content type even though no json in response
    mockServiceResponse.setContentType("application/json");

    if (groupsDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (groupsDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsDeleted: " + groupsDeleted);
    }
        
  }

  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperScim2Group grouperScimGroup = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2Group where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2Group.class);
  
    if (grouperScimGroup == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    String customDescription = new GcDbAccess().sql("select description from mock_scim_group where id = ? ").addBindVar(grouperScimGroup.getId()).select(String.class);
    if (customDescription != null) {        
      Map<String, Object> customAttributes = new HashMap<>();
      customAttributes.put("custom_description", customDescription);
      grouperScimGroup.setCustomAttributes(customAttributes);
      
      Map<String, String> customAttributeJsonPointers = new HashMap<>();
      customAttributeJsonPointers.put("custom_description", "/urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group/description");
      grouperScimGroup.setCustomAttributeNameToJsonPointer(customAttributeJsonPointers);
    }
    
    ObjectNode objectNode = grouperScimGroup.toJson(null);
    String membershipStrategy = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.membershipStrategy.mode");
    if (StringUtils.equals(membershipStrategy, "fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups")) {      
      attachMembersToGroup(objectNode, grouperScimGroup.getId());
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
  }
  
  private void attachMembersToGroup(ObjectNode groupNode, String groupId) {
      
    // select memberships
    List<GrouperScim2Membership> grouperScimMemberships =  HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership where groupId = :theGroupId")
        .setString("theGroupId", groupId).list(GrouperScim2Membership.class);
    
    ArrayNode membersArrayNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2Membership grouperScim2Membership: grouperScimMemberships) {
      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("value", grouperScim2Membership.getUserId());
      membersArrayNode.add(jsonJacksonNode);
    }
    
    groupNode.set("members", membersArrayNode);
      
    
  }
  
  private void attachGroupsToUser(ObjectNode userNode, String userId) {
      
    List<GrouperScim2Membership> grouperScimMemberships =  HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership where userId = :theUserId")
        .setString("theUserId", userId).list(GrouperScim2Membership.class);
    
    ArrayNode membersArrayNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2Membership grouperScim2Membership: grouperScimMemberships) {
      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("value", grouperScim2Membership.getGroupId());
      membersArrayNode.add(jsonJacksonNode);
    }
    
    userNode.set("groups", membersArrayNode);
      
  } 

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String filter = mockServiceRequest.getHttpServletRequest().getParameter("filter");
    
    List<GrouperScim2Group> grouperScimGroups = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperScimGroups = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20\"" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq "something"
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+\"(.+)\"$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      value = StringEscapeUtils.unescapeJson(value);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperScimGroups = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group where " + field + " = :theValue").setString("theValue", value).list(GrouperScim2Group.class);
    }
    
    //  {
    //    "totalResults": 5,
    //    "Resources": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "displayName": "my group"
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("totalResults", GrouperUtil.length(grouperScimGroups));
    resultNode.put("itemsPerPage", GrouperUtil.length(grouperScimGroups));
    resultNode.put("startIndex", 1);
  
    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
      resultNode.set("schemas", schemasNode);
    }
    
    ArrayNode resourcesNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2Group grouperScimGroup : grouperScimGroups) {
      
      String customDescription = new GcDbAccess().sql("select description from mock_scim_group where id = ? ").addBindVar(grouperScimGroup.getId()).select(String.class);
      if (customDescription != null) {        
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("custom_description", customDescription);
        grouperScimGroup.setCustomAttributes(customAttributes);
        
        Map<String, String> customAttributeJsonPointers = new HashMap<>();
        customAttributeJsonPointers.put("custom_description", "/urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group/description");
        grouperScimGroup.setCustomAttributeNameToJsonPointer(customAttributeJsonPointers);
      }
      ObjectNode groupNode = grouperScimGroup.toJson(null);
      resourcesNode.add(groupNode);
      
      String membershipStrategy = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.scim2.mock.membershipStrategy.mode");
      if (StringUtils.equals(membershipStrategy, "fullGroupMembershipsInGroupObjectsWhenRetrievingAllGroups")) {
        attachMembersToGroup(groupNode, grouperScimGroup.getId());
      }
    }
    
    resultNode.set("Resources", resourcesNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  
  }

  public void patchGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    GrouperScim2Group grouperScimGroup = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2Group where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2Group.class);

    if (grouperScimGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceRequest.getDebugMap().put("foundGroup", false);
      return;
    }
        
    mockServiceResponse.setContentType("application/json");
    
    //  {
    //    "schemas": [
    //        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
    //    ],
    //    "Operations": [
    //        {
    //            "op": "replace",
    //            "path": "active",
    //            "value": "false"
    //        }
    //    ]
    //  }
    
    String requestBodyString = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBodyString);
  
    ArrayNode schemasNode = (ArrayNode)requestNode.get("schemas");
  
    GrouperUtil.assertion(schemasNode.size() == 1, "schema is required");
    GrouperUtil.assertion("urn:ietf:params:scim:api:messages:2.0:PatchOp".equals(schemasNode.get(0).asText()), "schema is required");
  
    ArrayNode operationsNode = (ArrayNode)requestNode.get("Operations");

    GrouperUtil.assertion(operationsNode.size() > 0, "must send operations");

    int adds = 0;
    int removes = 0;
    int replaces = 0;
    
    for (int i=0;i<operationsNode.size();i++) {
      boolean isDescription = false;
      JsonNode operation = operationsNode.get(i);
      if (operation.has("value")) {
        JsonNode valueNode = operation.get("value");
        if (valueNode.has("urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group")) {
          JsonNode groupNode = valueNode.get("urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group");
          isDescription = groupNode.has("description");
        }
      }

      //            "op": "replace",
      //            "path": "active",
      //            "value": "false"

      // replace, add, remove
      String op = GrouperUtil.jsonJacksonGetString(operation, "op");
      boolean opAdd = "add".equals(op);
      boolean opReplace = "replace".equals(op);
      boolean opRemove = "remove".equals(op);
      if (!opAdd && !opRemove && !opReplace) {
        throw new RuntimeException("Invalid op, expecting add, replace, remove, but received: '" + op + "'");
      }
      String path = GrouperUtil.jsonJacksonGetString(operation, "path");

      Set<String> userIdsToAdd = new HashSet<String>();
      Set<String> userIdsToRemove = new HashSet<String>();

      if (!isDescription) {
        if (opRemove) {
          
          String userId = grouperScimGroup.validateMembersPath(path);
          
          if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("userId is blank: " + requestBodyString);
          }

          userIdsToRemove.add(userId);
          
        } else {
          
          GrouperUtil.assertion("members".equals(path), "'members' is only path acceptable, not '" + path + "'");
          
          JsonNode valueNode = GrouperUtil.jsonJacksonGetNode(operation, "value");
          
          if (!valueNode.isArray()) {
            ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
            arrayNode.add(valueNode);
            valueNode = arrayNode;
          }
          for (int j=0;j<valueNode.size();j++) {
            
            JsonNode theValue = valueNode.get(j);
            
            String value = GrouperUtil.jsonJacksonGetString(theValue, "value");
            GrouperUtil.assertion(!StringUtils.isBlank(value), "'members' is only path acceptable, not '" + path + "'");
            userIdsToAdd.add(value);
          }
          
          if (opReplace) {
           
            userIdsToRemove.addAll(HibernateSession.byHqlStatic()
                .createQuery("select userId from GrouperScim2Membership where groupId = :theGroupId")
                .setString("theGroupId", id).listSet(String.class));

            Set<String> replaceWithIds = new HashSet<String>(userIdsToAdd);
            
            userIdsToAdd.removeAll(userIdsToRemove);
            userIdsToRemove.removeAll(replaceWithIds);
            replaces++;
          }
        }
        
        for (String userId : userIdsToRemove) {
          GrouperScim2Membership grouperScim2Membership = HibernateSession.byHqlStatic()
              .createQuery("select membership from GrouperScim2Membership membership where userId = :theUserId and groupId = :theGroupId")
              .setString("theGroupId", id).setString("theUserId", userId).uniqueResult(GrouperScim2Membership.class);
          
          if (grouperScim2Membership != null) {
            HibernateSession.byObjectStatic().delete(grouperScim2Membership);
            removes++;
          }

        }

        for (String userId : userIdsToAdd) {
          GrouperScim2Membership grouperScim2Membership = new GrouperScim2Membership();
          grouperScim2Membership.setId(GrouperUuid.getUuid());
          grouperScim2Membership.setUserId(userId);
          grouperScim2Membership.setGroupId(id);
          HibernateSession.byObjectStatic().save(grouperScim2Membership);
          adds++;
        }
      } else {
        String description = GrouperUtil.jsonJacksonGetStringFromJsonPointer(operation, "/value/urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group/description");
        if (opRemove) {
          description = null;
        }
        String sql = "update mock_scim_group set description = ? where id = ?";
        new GcDbAccess().sql(sql).addBindVar(description).addBindVar(grouperScimGroup.getId()).executeSql();
      }

    }

    mockServiceRequest.getDebugMap().put("adds", adds);
    mockServiceRequest.getDebugMap().put("removes", removes);
    mockServiceRequest.getDebugMap().put("replaces", replaces);
    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
    
  }
  
}

/*******************************************************************************
 * Copyright 2024 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.azure.AzureMockServiceHandler;
import edu.internet2.middleware.grouper.app.azure.AzureProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxMockServiceHandler;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.duo.DuoMockServiceHandler;
import edu.internet2.middleware.grouper.app.duo.DuoProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterMockServiceHandler;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.google.GoogleMockServiceHandler;
import edu.internet2.middleware.grouper.app.google.GoogleProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.remedyV2.RemedyMockServiceHandler;
import edu.internet2.middleware.grouper.app.remedyV2.RemedyProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.DigitalMarketplaceMockServiceHandler;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.DigitalMarketplaceProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixMockServiceHandler;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisionerTestUtils;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpAdminExternalSystemGet (admin_external_system_get MCP tool).
 * Tests that require the mock server (testGetUserAzure, testGetUserDuo, testGetUserBox,
 * testGetUserGoogle, testGetUserFreshserviceRequesters, testGetUserRemedy,
 * testGetUserRemedyDigitalMarketplace, testGetUserTeamDynamix) need Tomcat running with MockServiceServlet deployed.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminExternalSystemGetTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpAdminExternalSystemGetTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpAdminExternalSystemGetTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpAdminExternalSystemGetTest("testToolDefinition"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpAdminExternalSystemGet.toolDefinition();

    assertEquals("admin_external_system_get", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("action"));
    assertNotNull(properties.get("externalSystemConfigId"));
    assertNotNull(properties.get("subjectIdOrIdentifier"));
    assertNotNull(properties.get("subjectSourceId"));

    // action should have an enum with 2 values
    JsonNode actionEnum = properties.get("action").get("enum");
    assertNotNull(actionEnum);
    assertTrue(actionEnum.isArray());
    assertEquals(2, actionEnum.size());

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("action", required.get(0).asText());
  }

  /**
   * test missing action returns an error
   */
  public void testMissingAction() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for missing action", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("action is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test null arguments returns an error
   */
  public void testNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("action is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test unknown action returns an error
   */
  public void testUnknownAction() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "bogusAction");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for unknown action", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Unknown action"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listExternalSystems with no config returns empty list
   */
  public void testListExternalSystemsEmpty() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listExternalSystems");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(0, responseNode.get("externalSystemCount").asInt());
        JsonNode systems = responseNode.get("externalSystems");
        assertNotNull(systems);
        assertTrue(systems.isArray());
        assertEquals(0, systems.size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listExternalSystems returns configured systems
   */
  public void testListExternalSystemsConfigured() {

    // configure MCP external system properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.externalSystemLookupField",
        "userPrincipalName");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.documentationForAiClient",
        "Azure AD for campus users");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.duo1.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.duo1.externalSystemLookupField",
        "username");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listExternalSystems");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(2, responseNode.get("externalSystemCount").asInt());

        JsonNode systems = responseNode.get("externalSystems");
        assertNotNull(systems);
        assertTrue(systems.isArray());

        // find myAzure
        boolean foundAzure = false;
        boolean foundDuo = false;
        for (int i = 0; i < systems.size(); i++) {
          String configId = systems.get(i).get("configId").asText();
          if ("myAzure".equals(configId)) {
            foundAzure = true;
            assertEquals("userPrincipalName", systems.get(i).get("lookupField").asText());
            assertEquals("Azure AD for campus users", systems.get(i).get("documentation").asText());
          }
          if ("duo1".equals(configId)) {
            foundDuo = true;
            assertEquals("username", systems.get(i).get("lookupField").asText());
            assertNull("duo1 should not have documentation", systems.get(i).get("documentation"));
          }
        }
        assertTrue("Should find myAzure in external systems", foundAzure);
        assertTrue("Should find duo1 in external systems", foundDuo);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with missing externalSystemConfigId returns error
   */
  public void testGetUserMissingConfigId() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for missing configId", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("externalSystemConfigId is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with missing subjectIdOrIdentifier returns error
   */
  public void testGetUserMissingSubject() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myAzure");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for missing subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("subjectIdOrIdentifier is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with unconfigured JEXL returns error
   */
  public void testGetUserMissingJexl() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "nonExistentSystem");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for missing JEXL", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("subjectIdTranslationJexl"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with missing lookup field returns error
   */
  public void testGetUserMissingLookupField() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.testSystem.subjectIdTranslationJexl",
        "${subject.getId()}");
    // intentionally not setting externalSystemLookupField

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "testSystem");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for missing lookup field", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("externalSystemLookupField"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with nonexistent subject returns error
   */
  public void testGetUserSubjectNotFound() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.externalSystemLookupField",
        "userPrincipalName");

    // set up Azure external system so type detection works
    AzureProvisionerTestUtils.setupAzureExternalSystem(false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myAzure");
      arguments.put("subjectIdOrIdentifier", "totallyBogusSubjectThatDoesNotExist99999");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertTrue("Expected error for nonexistent subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Subject not found"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Azure mock server.
   * requires Tomcat with MockServiceServlet running.
   * verifies basic fields, extended attributes (mail, userType, etc.), and license details.
   */
  public void testGetUserAzure() {

    // set up Azure external system pointing at mock server
    AzureProvisionerTestUtils.setupAzureExternalSystem(false);

    // ensure mock tables exist (includes extended columns and license table)
    AzureMockServiceHandler.ensureAzureMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_license").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_user").executeSql();

    // insert a mock Azure user via SQL with extended attributes
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_azure_user (id, display_name, user_principal_name, mail_nickname, account_enabled, "
        + "mail, user_type, on_premises_sam_account_name, on_premises_last_sync_date_time, "
        + "proxy_addresses, show_in_address_list) "
        + "values ('azure-user-id-123', 'Test Subject 0', 'test.subject.0', 'testsubj0', 'T', "
        + "'test0@example.edu', 'Member', 'testsubj0', '2025-01-15T10:30:00Z', "
        + "'SMTP:test0@example.edu,smtp:test0@mail.example.edu', 'T')").executeSql();

    // insert mock license details for the user
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_azure_license (id, user_id, sku_id, sku_part_number) "
        + "values ('license-1', 'azure-user-id-123', 'sku-aaa-111', 'ENTERPRISEPACK')").executeSql();
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_azure_license (id, user_id, sku_id, sku_part_number) "
        + "values ('license-2', 'azure-user-id-123', 'sku-bbb-222', 'EMS')").executeSql();

    // configure MCP external system properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.externalSystemLookupField",
        "userPrincipalName");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myAzure");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("myAzure", responseNode.get("externalSystemConfigId").asText());
        assertEquals("azure", responseNode.get("externalSystemType").asText());
        assertEquals("userPrincipalName", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
        assertTrue(responseNode.get("userFound").asBoolean());

        JsonNode user = responseNode.get("user");
        assertNotNull(user);
        assertEquals("azure-user-id-123", user.get("id").asText());
        assertEquals("Test Subject 0", user.get("displayName").asText());
        assertEquals("test.subject.0", user.get("userPrincipalName").asText());

        // verify extended attributes from direct Graph API call
        assertEquals("test0@example.edu", user.get("mail").asText());
        assertEquals("Member", user.get("userType").asText());
        assertEquals("testsubj0", user.get("onPremisesSamAccountName").asText());
        assertEquals("2025-01-15T10:30:00Z", user.get("onPremisesLastSyncDateTime").asText());

        // verify license details from /users/{id}/licenseDetails call
        JsonNode licenseDetails = user.get("licenseDetails");
        assertNotNull("licenseDetails should be present", licenseDetails);
        assertTrue("licenseDetails should be an array", licenseDetails.isArray());
        assertEquals(2, licenseDetails.size());

        // find ENTERPRISEPACK license
        boolean foundEnterprise = false;
        boolean foundEms = false;
        for (int i = 0; i < licenseDetails.size(); i++) {
          String skuPartNumber = licenseDetails.get(i).get("skuPartNumber").asText();
          if ("ENTERPRISEPACK".equals(skuPartNumber)) {
            foundEnterprise = true;
            assertEquals("sku-aaa-111", licenseDetails.get(i).get("skuId").asText());
          }
          if ("EMS".equals(skuPartNumber)) {
            foundEms = true;
            assertEquals("sku-bbb-222", licenseDetails.get(i).get("skuId").asText());
          }
        }
        assertTrue("Should find ENTERPRISEPACK license", foundEnterprise);
        assertTrue("Should find EMS license", foundEms);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Duo mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserDuo() {

    // set up Duo external system pointing at mock server
    DuoProvisionerTestUtils.setupDuoExternalSystem();

    // ensure mock tables exist
    DuoMockServiceHandler.ensureDuoMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_user").executeSql();

    // insert a mock Duo user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_duo_user (user_id, user_name, email, first_name, last_name, real_name) "
        + "values ('duo-user-id-456', 'test.subject.0', 'test0@example.com', 'Test', 'Subject0', 'Test Subject 0')").executeSql();

    // configure MCP external system properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.duo1.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.duo1.externalSystemLookupField",
        "username");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "duo1");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("duo1", responseNode.get("externalSystemConfigId").asText());
        assertEquals("duo", responseNode.get("externalSystemType").asText());
        assertEquals("username", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
        assertTrue(responseNode.get("userFound").asBoolean());

        JsonNode user = responseNode.get("user");
        assertNotNull(user);
        assertEquals("duo-user-id-456", user.get("user_id").asText());
        assertEquals("test.subject.0", user.get("username").asText());
        assertEquals("test0@example.com", user.get("email").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Azure mock server when user is not found
   */
  public void testGetUserAzureNotFound() {

    // set up Azure external system pointing at mock server
    AzureProvisionerTestUtils.setupAzureExternalSystem(false);

    // ensure mock tables exist
    AzureMockServiceHandler.ensureAzureMockTables();

    // clean up mock data (no users inserted)
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_license").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_user").executeSql();

    // configure MCP external system properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myAzure.externalSystemLookupField",
        "userPrincipalName");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myAzure");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success (not found is not an error), got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertFalse(responseNode.get("userFound").asBoolean());
        assertFalse(responseNode.has("user"));
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Box mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserBox() {

    // set up Box external system pointing at mock server
    BoxProvisionerTestUtils.setupBoxExternalSystem();

    // ensure mock tables exist
    BoxMockServiceHandler.ensureBoxMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

    // insert a mock Box user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_box_user (id, login, name, status, role) "
        + "values ('box-user-id-456', 'test.subject.0@example.edu', 'Test Subject 0', 'active', 'user')").executeSql();

    // configure MCP external system properties (configId must match "localBox" from BoxProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.localBox.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.localBox.externalSystemLookupField",
        "login");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "localBox");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("localBox", responseNode.get("externalSystemConfigId").asText());
        assertEquals("box", responseNode.get("externalSystemType").asText());
        assertEquals("login", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with FreshService Requesters mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserFreshserviceRequesters() {

    // set up FreshService external system pointing at mock server
    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // ensure mock tables exist
    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    // insert a mock FreshService requester user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_freshreq_user (id, email, first_name, last_name, active) "
        + "values (12345, 'test.subject.0@example.edu', 'Test', 'Subject 0', 'T')").executeSql();

    // configure MCP external system properties (configId must match "freshServiceDev" from FreshRequesterProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.freshServiceDev.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.freshServiceDev.externalSystemLookupField",
        "email");
    // explicit type required since WsBearerToken is also used for SCIM
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.freshServiceDev.externalSystemType",
        "freshserviceRequesters");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "freshServiceDev");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("freshServiceDev", responseNode.get("externalSystemConfigId").asText());
        assertEquals("freshserviceRequesters", responseNode.get("externalSystemType").asText());
        assertEquals("email", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Google mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserGoogle() {

    // set up Google external system pointing at mock server
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();

    // ensure mock tables exist
    GoogleMockServiceHandler.ensureGoogleMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

    // insert a mock Google user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_google_user (id, primary_email, given_name, family_name, org_unit_path) "
        + "values ('google-user-id-789', 'test.subject.0@example.edu', 'Test', 'Subject0', '/')").executeSql();

    // configure MCP external system properties (configId must match "myGoogle" from GoogleProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myGoogle.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myGoogle.externalSystemLookupField",
        "id");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myGoogle");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("myGoogle", responseNode.get("externalSystemConfigId").asText());
        assertEquals("google", responseNode.get("externalSystemType").asText());
        assertEquals("id", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Remedy mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserRemedy() {

    // set up Remedy external system pointing at mock server
    RemedyProvisionerTestUtils.setupRemedyExternalSystem();

    // ensure mock tables exist
    RemedyMockServiceHandler.ensureRemedyMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_user").executeSql();

    // insert a mock Remedy user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_remedy_user (person_id, remedy_login_id) "
        + "values ('remedy-person-123', 'test.subject.0')").executeSql();

    // configure MCP external system properties (configId must match "myRemedy" from RemedyProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myRemedy.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myRemedy.externalSystemLookupField",
        "remedyLoginId");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myRemedy");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("myRemedy", responseNode.get("externalSystemConfigId").asText());
        assertEquals("remedy", responseNode.get("externalSystemType").asText());
        assertEquals("remedyLoginId", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with Remedy Digital Marketplace mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserRemedyDigitalMarketplace() {

    // set up Digital Marketplace external system pointing at mock server
    DigitalMarketplaceProvisionerTestUtils.setupDigitalMarketplaceExternalSystem();

    // ensure mock tables exist
    DigitalMarketplaceMockServiceHandler.ensureDigitalMarketplaceMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_mp_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_marketplace_user").executeSql();

    // insert a mock Digital Marketplace user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_digital_marketplace_user (user_id, login_name) "
        + "values ('dm-user-123', 'test.subject.0')").executeSql();

    // configure MCP external system properties (configId must match "myDigitalMarketplace" from DigitalMarketplaceProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myDigitalMarketplace.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.myDigitalMarketplace.externalSystemLookupField",
        "loginName");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "myDigitalMarketplace");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("myDigitalMarketplace", responseNode.get("externalSystemConfigId").asText());
        assertEquals("remedyDigitalMarketplace", responseNode.get("externalSystemType").asText());
        assertEquals("loginName", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getUser with TeamDynamix mock server.
   * requires Tomcat with MockServiceServlet running.
   */
  public void testGetUserTeamDynamix() {

    // set up TeamDynamix external system pointing at mock server
    TeamDynamixProvisionerTestUtils.setupTeamDynamixExternalSystem();

    // ensure mock tables exist
    TeamDynamixMockServiceHandler.ensureTeamDynamixMockTables();

    // clean up mock data
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

    // insert a mock TeamDynamix user via SQL (not a Hibernate entity)
    new GcDbAccess().connectionName("grouper").sql(
        "insert into mock_teamdynamix_user (id, first_name, last_name, primary_email, company, security_role_id, user_name, external_id, active) "
        + "values ('tdx-user-123', 'Test', 'Subject0', 'test0@example.edu', 'TestCo', 'role-123', 'test.subject.0', 'test.subject.0', 'T')").executeSql();

    // configure MCP external system properties (configId must match "teamdx" from TeamDynamixProvisionerTestUtils)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.teamdx.subjectIdTranslationJexl",
        "${subject.getId()}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.adminExternalSystem.teamdx.externalSystemLookupField",
        "externalId");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "getUser");
      arguments.put("externalSystemConfigId", "teamdx");
      arguments.put("subjectIdOrIdentifier", "test.subject.0");

      ObjectNode result = GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("teamdx", responseNode.get("externalSystemConfigId").asText());
        assertEquals("teamDynamix", responseNode.get("externalSystemType").asText());
        assertEquals("externalId", responseNode.get("lookupField").asText());
        assertEquals("test.subject.0", responseNode.get("translatedLookupValue").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

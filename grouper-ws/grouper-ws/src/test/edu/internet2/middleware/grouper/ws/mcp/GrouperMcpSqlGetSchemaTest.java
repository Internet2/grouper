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
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpSqlGetSchema (sql_get_schema MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpSqlGetSchemaTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpSqlGetSchemaTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpSqlGetSchemaTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpSqlGetSchemaTest("testListTablesGrouper"));
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

    ObjectNode toolDef = GrouperMcpSqlGetSchema.toolDefinition();

    assertEquals("sql_get_schema", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("action"));
    assertNotNull(properties.get("externalSystemId"));
    assertNotNull(properties.get("tableName"));

    // action should have an enum with 3 values
    JsonNode actionEnum = properties.get("action").get("enum");
    assertNotNull(actionEnum);
    assertTrue(actionEnum.isArray());
    assertEquals(3, actionEnum.size());

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

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

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

      ObjectNode result = GrouperMcpSqlGetSchema.execute(null, authUser);

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

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertTrue("Expected error for unknown action", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Unknown action"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listExternalSystems returns at least "grouper"
   */
  public void testListExternalSystems() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listExternalSystems");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode systems = responseNode.get("externalSystems");
        assertNotNull(systems);
        assertTrue(systems.isArray());
        assertTrue("Should have at least 'grouper'", systems.size() >= 1);

        // first entry should be "grouper"
        assertEquals("grouper", systems.get(0).get("id").asText());
        assertTrue(systems.get(0).get("isGrouperDb").asBoolean());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listExternalSystems includes configured external systems
   */
  public void testListExternalSystemsWithConfigured() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.hrDb.sqlTablesViews", "hr_employees, hr_departments");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listExternalSystems");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode systems = responseNode.get("externalSystems");

        // find "hrDb" in the list
        boolean foundHrDb = false;
        for (int i = 0; i < systems.size(); i++) {
          if ("hrDb".equals(systems.get(i).get("id").asText())) {
            foundHrDb = true;
            assertFalse(systems.get(i).get("isGrouperDb").asBoolean());
          }
        }
        assertTrue("Should find 'hrDb' in external systems", foundHrDb);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listTables for the grouper database returns tables and views from DDL
   */
  public void testListTablesGrouper() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listTables");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);

        int tableCount = responseNode.get("tableCount").asInt();
        int viewCount = responseNode.get("viewCount").asInt();
        assertTrue("Should have tables", tableCount > 0);

        JsonNode tables = responseNode.get("tables");
        assertNotNull(tables);
        assertTrue(tables.isArray());

        // grouper_groups should be in the tables
        boolean foundGrouperGroups = false;
        for (int i = 0; i < tables.size(); i++) {
          if ("grouper_groups".equals(tables.get(i).asText())) {
            foundGrouperGroups = true;
            break;
          }
        }
        assertTrue("Should contain grouper_groups table", foundGrouperGroups);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test tableInfo for a known grouper table returns DDL
   */
  public void testTableInfoGrouperGroups() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "tableInfo");
      arguments.put("tableName", "grouper_groups");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      // DDL for grouper_groups should contain CREATE TABLE
      assertTrue("Should contain CREATE TABLE", text.toUpperCase().contains("CREATE TABLE"));
      assertTrue("Should contain grouper_groups", text.toLowerCase().contains("grouper_groups"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test tableInfo for a non-existent table returns error
   */
  public void testTableInfoNotFound() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "tableInfo");
      arguments.put("tableName", "bogus_nonexistent_table_99999");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertTrue("Expected error for non-existent table", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not found"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test tableInfo with missing tableName returns error
   */
  public void testTableInfoMissingTableName() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "tableInfo");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertTrue("Expected error for missing tableName", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("tableName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listTables with unconfigured external system returns error
   */
  public void testListTablesUnconfiguredExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listTables");
      arguments.put("externalSystemId", "bogusExternalSystem99999");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertTrue("Expected error for unconfigured external system",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test tableInfo with unconfigured external system returns error
   */
  public void testTableInfoUnconfiguredExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "tableInfo");
      arguments.put("tableName", "some_table");
      arguments.put("externalSystemId", "bogusExternalSystem99999");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertTrue("Expected error for unconfigured external system",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listTables with explicit "grouper" externalSystemId works
   */
  public void testListTablesExplicitGrouper() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listTables");
      arguments.put("externalSystemId", "grouper");

      ObjectNode result = GrouperMcpSqlGetSchema.execute(arguments, authUser);

      assertFalse("Expected success with explicit 'grouper', got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertTrue("Should have tables", responseNode.get("tableCount").asInt() > 0);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

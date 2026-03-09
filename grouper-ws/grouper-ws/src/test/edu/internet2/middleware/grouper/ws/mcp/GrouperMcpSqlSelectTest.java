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

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpSqlSelect (sql_select MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpSqlSelectTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpSqlSelectTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpSqlSelectTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpSqlSelectTest("testSqlSelectBasic"));
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
   * test a basic SELECT query that returns rows
   */
  public void testSqlSelectBasic() {

    // create a group so there is data in grouper_groups
    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpSqlSelectGroup1")
        .assignName("test:mcpSqlSelectGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("sql select test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name, gg.description FROM grouper_groups gg "
          + "WHERE gg.name = 'test:mcpSqlSelectGroup1'");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("rowCount").asInt());
        assertEquals(1, responseNode.get("pageNumber").asInt());
        JsonNode rows = responseNode.get("rows");
        assertNotNull(rows);
        assertTrue(rows.isArray());
        assertEquals(1, rows.size());
        assertEquals("test:mcpSqlSelectGroup1", rows.get(0).get("name").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that an empty result set returns 0 rows
   */
  public void testSqlSelectNoResults() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'bogus:nonExistentGroup99999'");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(0, responseNode.get("rowCount").asInt());
        assertEquals(0, responseNode.get("rows").size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test paging with custom page size
   */
  public void testSqlSelectPaging() {

    // create 3 groups
    for (int i = 0; i < 3; i++) {
      new GroupSave(GrouperSession.staticGrouperSession())
          .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:mcpSqlPage:group" + i)
          .assignName("test:mcpSqlPage:group" + i)
          .assignCreateParentStemsIfNotExist(true)
          .assignDescription("sql page test " + i).save();
    }

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // page 1, size 2
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name LIKE 'test:mcpSqlPage:group%' ORDER BY gg.name");
      arguments.put("pageSize", 2);
      arguments.put("pageNumber", 1);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(2, responseNode.get("pageSize").asInt());
        assertEquals(1, responseNode.get("pageNumber").asInt());
        assertEquals(2, responseNode.get("rows").size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }

      // page 2, size 2
      arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name LIKE 'test:mcpSqlPage:group%' ORDER BY gg.name");
      arguments.put("pageSize", 2);
      arguments.put("pageNumber", 2);

      result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("rows").size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that paging beyond page 1 without ORDER BY returns an error
   */
  public void testSqlSelectPagingRequiresOrderBy() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg");
      arguments.put("pageNumber", 2);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for missing ORDER BY", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("ORDER BY"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that page 1 without ORDER BY succeeds
   */
  public void testSqlSelectPage1NoOrderByOk() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg WHERE 1=0");
      arguments.put("pageNumber", 1);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success for page 1 without ORDER BY, got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // SQL validation tests
  // ========================================================================

  /**
   * test that missing sql returns an error
   */
  public void testSqlSelectMissingSql() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("sql is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with null arguments
   */
  public void testSqlSelectNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpSqlSelect.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("sql is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that INSERT is rejected
   */
  public void testSqlSelectRejectsInsert() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "INSERT INTO grouper_groups (name) VALUES ('bad')");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for INSERT", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Only SELECT"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that UPDATE is rejected
   */
  public void testSqlSelectRejectsUpdate() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "UPDATE grouper_groups SET name = 'bad'");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for UPDATE", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Only SELECT"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that DELETE is rejected
   */
  public void testSqlSelectRejectsDelete() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "DELETE FROM grouper_groups");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for DELETE", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Only SELECT"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that DROP is rejected even inside a SELECT
   */
  public void testSqlSelectRejectsDrop() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT 1; DROP TABLE grouper_groups");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for semicolon/DROP", result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that semicolons are rejected
   */
  public void testSqlSelectRejectsSemicolon() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT 1;");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for semicolon", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("semicolon"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // countOnly tests
  // ========================================================================

  /**
   * test countOnly returns the correct count
   */
  public void testSqlSelectCountOnly() {

    // create 3 groups
    for (int i = 0; i < 3; i++) {
      new GroupSave(GrouperSession.staticGrouperSession())
          .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:mcpSqlCount:group" + i)
          .assignName("test:mcpSqlCount:group" + i)
          .assignCreateParentStemsIfNotExist(true)
          .assignDescription("sql count test " + i).save();
    }

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name LIKE 'test:mcpSqlCount:group%'");
      arguments.put("countOnly", true);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(3, responseNode.get("count").asInt());
        // countOnly should not have rows or pageSize
        assertNull(responseNode.get("rows"));
        assertNull(responseNode.get("pageSize"));
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test countOnly with zero results
   */
  public void testSqlSelectCountOnlyZero() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'bogus:nonExistentGroup99999'");
      arguments.put("countOnly", true);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(0, responseNode.get("count").asInt());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test countOnly still validates SQL (rejects INSERT)
   */
  public void testSqlSelectCountOnlyRejectsInsert() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "INSERT INTO grouper_groups (name) VALUES ('bad')");
      arguments.put("countOnly", true);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for INSERT with countOnly", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Only SELECT"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test countOnly with missing sql returns error
   */
  public void testSqlSelectCountOnlyMissingSql() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("countOnly", true);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("sql is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test countOnly ignores paging parameters
   */
  public void testSqlSelectCountOnlyIgnoresPaging() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpSqlCountPaging1")
        .assignName("test:mcpSqlCountPaging1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("count paging test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // countOnly with pageNumber > 1 and no ORDER BY should still succeed
      // because paging is ignored for count
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'test:mcpSqlCountPaging1'");
      arguments.put("countOnly", true);
      arguments.put("pageNumber", 2);
      arguments.put("pageSize", 1);

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success (countOnly ignores paging), got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("count").asInt());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // externalSystemId tests
  // ========================================================================

  /**
   * test that the default externalSystemId (null/blank) uses the grouper connection
   */
  public void testSqlSelectDefaultExternalSystem() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpSqlExtSys1")
        .assignName("test:mcpSqlExtSys1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("ext sys test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // no externalSystemId - should default to grouper and work
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'test:mcpSqlExtSys1'");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success with default external system, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("rowCount").asInt());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that passing "grouper" explicitly as externalSystemId works the same as default
   */
  public void testSqlSelectExplicitGrouper() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpSqlExtSysGrouper1")
        .assignName("test:mcpSqlExtSysGrouper1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("explicit grouper test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // explicitly pass "grouper" as externalSystemId - should work the same as default
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'test:mcpSqlExtSysGrouper1'");
      arguments.put("externalSystemId", "grouper");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success with explicit 'grouper' external system, got: "
          + result.toString(), result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("rowCount").asInt());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that an unconfigured externalSystemId returns an error
   */
  public void testSqlSelectUnconfiguredExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT 1 FROM grouper_groups WHERE 1=0");
      arguments.put("externalSystemId", "bogusExternalSystem99999");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for unconfigured external system",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected message about external system not configured",
          text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a configured externalSystemId (with sqlTablesViews) is allowed
   */
  public void testSqlSelectConfiguredExternalSystem() {

    // configure a custom external system with sqlTablesViews
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.testExtDb.sqlTablesViews", "grouper_groups, grouper_stems");

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpSqlExtSysConfig1")
        .assignName("test:mcpSqlExtSysConfig1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("configured ext sys test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // testExtDb is configured but isn't a real DB connection,
      // so it should pass the validation check but may fail on actual query execution.
      // We just verify the external system validation passes (no "not configured" error).
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg WHERE 1=0");
      arguments.put("externalSystemId", "testExtDb");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      // the query will either succeed (if testExtDb resolves to grouper) or fail with
      // a connection error, but NOT with "not configured"
      String text = result.get("content").get(0).get("text").asText();
      assertFalse("Should not get 'not configured' error for configured external system",
          text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that countOnly also works with externalSystemId
   */
  public void testSqlSelectCountOnlyWithUnconfiguredExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT 1 FROM grouper_groups WHERE 1=0");
      arguments.put("countOnly", true);
      arguments.put("externalSystemId", "bogusExternalSystem99999");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error for unconfigured external system with countOnly",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the resolveConnectionName method directly
   */
  public void testResolveConnectionName() {

    // null/blank should return the default (from grouper.mcp.sqlGrouperExternalSystem config)
    String resolved = GrouperMcpSqlSelect.resolveConnectionName(null);
    assertNotNull(resolved);

    resolved = GrouperMcpSqlSelect.resolveConnectionName("");
    assertNotNull(resolved);

    // "grouper" should also return the configured default
    resolved = GrouperMcpSqlSelect.resolveConnectionName("grouper");
    assertNotNull(resolved);

    // if sqlGrouperExternalSystem is set to a custom value, "grouper" should resolve to it
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sqlGrouperExternalSystem", "grouperReadOnly");
    try {
      resolved = GrouperMcpSqlSelect.resolveConnectionName(null);
      assertEquals("grouperReadOnly", resolved);

      resolved = GrouperMcpSqlSelect.resolveConnectionName("grouper");
      assertEquals("grouperReadOnly", resolved);

      resolved = GrouperMcpSqlSelect.resolveConnectionName("");
      assertEquals("grouperReadOnly", resolved);
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
          "grouper.mcp.sqlGrouperExternalSystem");
    }

    // explicit non-grouper value should be returned as-is (trimmed)
    resolved = GrouperMcpSqlSelect.resolveConnectionName("myDb");
    assertEquals("myDb", resolved);

    resolved = GrouperMcpSqlSelect.resolveConnectionName("  myDb  ");
    assertEquals("myDb", resolved);
  }

  /**
   * test the isGrouperDb method directly
   */
  public void testIsGrouperDb() {

    assertTrue("null should be grouper db", GrouperMcpSqlSelect.isGrouperDb(null));
    assertTrue("blank should be grouper db", GrouperMcpSqlSelect.isGrouperDb(""));
    assertTrue("'grouper' should be grouper db", GrouperMcpSqlSelect.isGrouperDb("grouper"));
    assertTrue("' grouper ' should be grouper db", GrouperMcpSqlSelect.isGrouperDb(" grouper "));
    assertFalse("'hr_db' should not be grouper db", GrouperMcpSqlSelect.isGrouperDb("hr_db"));
    assertFalse("'grouperReadOnly' should not be grouper db",
        GrouperMcpSqlSelect.isGrouperDb("grouperReadOnly"));
  }

  /**
   * test the validateExternalSystemAllowed method directly
   */
  public void testValidateExternalSystemAllowed() {

    // null (grouper db) should always be allowed
    assertNull("null should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed(null));

    // blank (grouper db) should always be allowed
    assertNull("blank should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed(""));

    // "grouper" should always be allowed
    assertNull("'grouper' should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("grouper"));

    // unconfigured system should be denied
    String error = GrouperMcpSqlSelect.validateExternalSystemAllowed("unconfiguredDb12345");
    assertNotNull("Unconfigured system should be denied", error);
    assertTrue(error.contains("not configured"));

    // system with sqlTablesViews configured should be allowed
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.allowedDb.sqlTablesViews", "some_table");
    assertNull("System with sqlTablesViews should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("allowedDb"));

    // system with sqlTablesViewsQuery configured should be allowed
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.allowedDb2.sqlTablesViewsQuery",
        "SELECT table_name FROM information_schema.tables");
    assertNull("System with sqlTablesViewsQuery should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("allowedDb2"));
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpSqlSelect.toolDefinition();

    assertEquals("sql_select", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("sql"));
    assertNotNull(properties.get("countOnly"));
    assertNotNull(properties.get("pageSize"));
    assertNotNull(properties.get("pageNumber"));
    assertNotNull(properties.get("externalSystemId"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("sql", required.get(0).asText());
  }

  /**
   * test the validateReadOnlySql method directly
   */
  public void testValidateReadOnlySql() {

    // valid SELECT
    assertNull(GrouperMcpSqlSelect.validateReadOnlySql(
        "SELECT gg.name FROM grouper_groups gg"));

    // valid SELECT with subquery
    assertNull(GrouperMcpSqlSelect.validateReadOnlySql(
        "SELECT * FROM (SELECT name FROM grouper_groups) sub"));

    // blank sql
    assertNotNull(GrouperMcpSqlSelect.validateReadOnlySql(""));
    assertNotNull(GrouperMcpSqlSelect.validateReadOnlySql(null));

    // INSERT
    String error = GrouperMcpSqlSelect.validateReadOnlySql(
        "INSERT INTO grouper_groups (name) VALUES ('bad')");
    assertNotNull(error);
    assertTrue(error.contains("Only SELECT"));

    // SELECT with embedded INSERT keyword
    error = GrouperMcpSqlSelect.validateReadOnlySql(
        "SELECT 1 FROM grouper_groups WHERE 1=0 UNION ALL INSERT INTO foo VALUES (1)");
    assertNotNull(error);
    assertTrue(error.contains("INSERT"));

    // TRUNCATE
    error = GrouperMcpSqlSelect.validateReadOnlySql("TRUNCATE TABLE grouper_groups");
    assertNotNull(error);

    // semicolon
    error = GrouperMcpSqlSelect.validateReadOnlySql("SELECT 1;");
    assertNotNull(error);
    assertTrue(error.contains("semicolon"));
  }
}

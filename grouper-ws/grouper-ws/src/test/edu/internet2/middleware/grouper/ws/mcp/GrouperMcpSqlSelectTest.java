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

import java.util.ArrayList;
import java.util.List;

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
    TestRunner.run(GrouperMcpSqlSelectTest.class);
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

    // no database is available to the MCP SQL tools unless the administrator configures it,
    // so make the Grouper database available to these tests.  the external system ID is
    // the database connection name
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.grouper.grouperDatabase", "true");

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
   * remove any grouper.mcp.sql.* config overrides so that no database is available
   * to the MCP SQL tools, which is the out of the box state
   */
  private static void removeAllSqlExternalSystemConfigs() {
    List<String> keys = new ArrayList<String>(
        GrouperConfig.retrieveConfig().propertiesOverrideMap().keySet());
    for (String key : keys) {
      if (key.startsWith("grouper.mcp.sql.")) {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
      }
    }
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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
      arguments.put("externalSystemId", "grouper");

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
   * test that a missing externalSystemId is an error since there is no default database
   */
  public void testSqlSelectMissingExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // no externalSystemId, and there is no default
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg WHERE 1=0");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error when externalSystemId is not passed, got: "
          + result.toString(), result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected message that externalSystemId is required, got: " + text,
          text.contains("externalSystemId is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that no database at all is available when the administrator configured none
   */
  public void testSqlSelectNoExternalSystemsConfigured() {

    removeAllSqlExternalSystemConfigs();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg WHERE 1=0");
      arguments.put("externalSystemId", "grouper");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertTrue("Expected error when no external system is configured, got: "
          + result.toString(), result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected message that no databases are configured, got: " + text,
          text.contains("No databases are configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a configured Grouper database external system can be queried
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

      // "grouper" is configured as a Grouper database external system in setUp
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("sql", "SELECT gg.name FROM grouper_groups gg "
          + "WHERE gg.name = 'test:mcpSqlExtSysGrouper1'");
      arguments.put("externalSystemId", "grouper");

      ObjectNode result = GrouperMcpSqlSelect.execute(arguments, authUser);

      assertFalse("Expected success with the 'grouper' external system, got: "
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
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
          "grouper.mcp.sql.testExtDb.sqlTablesViews");
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
   * test the isGrouperDatabase method directly
   */
  public void testIsGrouperDatabase() {

    // setUp configured grouper.mcp.sql.grouper.grouperDatabase = true
    assertTrue("'grouper' should be a grouper database",
        GrouperMcpSqlSelect.isGrouperDatabase("grouper"));
    assertTrue("' grouper ' should be a grouper database",
        GrouperMcpSqlSelect.isGrouperDatabase(" grouper "));

    // not a grouper database unless flagged as one, even if the ID looks like it
    assertFalse("'hr_db' should not be a grouper database",
        GrouperMcpSqlSelect.isGrouperDatabase("hr_db"));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.grouperReadOnly.sqlTablesViews", "some_table");
    try {
      assertFalse("'grouperReadOnly' should not be a grouper database unless flagged",
          GrouperMcpSqlSelect.isGrouperDatabase("grouperReadOnly"));
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
          "grouper.mcp.sql.grouperReadOnly.sqlTablesViews");
    }
  }

  /**
   * test the validateExternalSystemAllowed method directly
   */
  public void testValidateExternalSystemAllowed() {

    // the grouper database is allowed since setUp configured it
    assertNull("'grouper' should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("grouper"));

    // there is no default, so null and blank are errors
    String error = GrouperMcpSqlSelect.validateExternalSystemAllowed(null);
    assertNotNull("null should not be allowed", error);
    assertTrue(error.contains("externalSystemId is required"));

    error = GrouperMcpSqlSelect.validateExternalSystemAllowed("");
    assertNotNull("blank should not be allowed", error);
    assertTrue(error.contains("externalSystemId is required"));

    // unconfigured system should be denied
    error = GrouperMcpSqlSelect.validateExternalSystemAllowed("unconfiguredDb12345");
    assertNotNull("Unconfigured system should be denied", error);
    assertTrue(error.contains("not configured"));

    // system with grouperDatabase configured should be allowed
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.allowedDb.grouperDatabase", "true");
    assertNull("System with grouperDatabase should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("allowedDb"));

    // system with sqlTablesViews configured should be allowed
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.allowedDb2.sqlTablesViews", "some_table");
    assertNull("System with sqlTablesViews should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("allowedDb2"));

    // system with sqlTablesViewsQuery configured should be allowed
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.sql.allowedDb3.sqlTablesViewsQuery",
        "SELECT table_name FROM information_schema.tables");
    assertNull("System with sqlTablesViewsQuery should be allowed",
        GrouperMcpSqlSelect.validateExternalSystemAllowed("allowedDb3"));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        "grouper.mcp.sql.allowedDb.grouperDatabase");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        "grouper.mcp.sql.allowedDb2.sqlTablesViews");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        "grouper.mcp.sql.allowedDb3.sqlTablesViewsQuery");
  }

  /**
   * test that no external systems are available when nothing is configured
   */
  public void testExternalSystemIdsNoneConfigured() {

    removeAllSqlExternalSystemConfigs();

    assertTrue("No external systems should be configured by default",
        GrouperMcpSqlSelect.externalSystemIds().isEmpty());
    assertFalse("anyConfigured should be false", GrouperMcpSqlSelect.anyConfigured());

    String error = GrouperMcpSqlSelect.validateExternalSystemAllowed("grouper");
    assertNotNull("The grouper database should not be available by default", error);
    assertTrue(error.contains("No databases are configured"));
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
    assertEquals(2, required.size());
    assertEquals("sql", required.get(0).asText());
    assertEquals("externalSystemId", required.get(1).asText());
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

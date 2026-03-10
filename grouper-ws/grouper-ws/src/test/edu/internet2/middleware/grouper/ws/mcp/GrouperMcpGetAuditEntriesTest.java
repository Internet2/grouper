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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpGetAuditEntries (audit_get MCP tool).
 * Now uses UserAuditQuery directly with privilege checks on groups/stems.
 *
 * @author mchyzer
 */
public class GrouperMcpGetAuditEntriesTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpGetAuditEntriesTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpGetAuditEntriesTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpGetAuditEntriesTest("testGetAuditEntriesByGroup"));
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
   * test getting audit entries filtered by group.
   * creating a group generates a group/addGroup audit entry.
   * user with admin on the group should be able to see audit entries.
   */
  public void testGetAuditEntriesByGroup() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditGroup1")
        .assignName("test:mcpAuditGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit test group").save();

    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("auditType", "group");
      arguments.put("groupName", "test:mcpAuditGroup1");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertNotNull(text);
      assertTrue("Expected audit entries in response", text.length() > 10);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getting audit entries filtered by stem.
   * creating groups in a stem generates audit entries.
   * user with stemAdmin on the stem should be able to see audit entries.
   */
  public void testGetAuditEntriesByStem() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditStem:group1")
        .assignName("test:mcpAuditStem:group1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit stem test 1").save();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditStem:group2")
        .assignName("test:mcpAuditStem:group2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit stem test 2").save();

    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(),
        "test:mcpAuditStem", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("auditType", "group");
      arguments.put("stemName", "test:mcpAuditStem");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultsNode = objectMapper.readTree(text);
        assertTrue("Expected array of audit entries", resultsNode.isArray());
        assertTrue("Expected at least 2 audit entries for 2 groups created",
            resultsNode.size() >= 2);

        // verify each entry has expected fields
        for (int i = 0; i < resultsNode.size(); i++) {
          assertNotNull(resultsNode.get(i).get("id"));
          assertNotNull(resultsNode.get(i).get("auditCategory"));
          assertNotNull(resultsNode.get(i).get("timestamp"));
        }
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that groupName alone works without auditType (gets all audit types for the group)
   */
  public void testGetAuditEntriesGroupNoAuditType() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditDefaultGroup")
        .assignName("test:mcpAuditDefaultGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit default test").save();

    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // no auditType specified, should still work since groupName is provided
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpAuditDefaultGroup");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that subjectId works when looking at own audit entries.
   * a user should be able to see audit entries about themselves.
   */
  public void testGetAuditEntriesOwnSubject() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditSubjGroup")
        .assignName("test:mcpAuditSubjGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit subject test").save();

    group1.addMember(SubjectTestHelper.SUBJ0);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertFalse("Expected success for own subject audits, got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with custom page size
   */
  public void testGetAuditEntriesWithPageSize() {

    // create multiple groups to generate audit entries
    for (int i = 0; i < 3; i++) {
      new GroupSave(GrouperSession.staticGrouperSession())
          .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:mcpAuditPage:group" + i)
          .assignName("test:mcpAuditPage:group" + i)
          .assignCreateParentStemsIfNotExist(true)
          .assignDescription("audit page test " + i).save();
    }

    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(),
        "test:mcpAuditPage", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("auditType", "group");
      arguments.put("stemName", "test:mcpAuditPage");
      arguments.put("pageSize", 2);

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultsNode = objectMapper.readTree(text);
        assertTrue("Expected array", resultsNode.isArray());
        assertTrue("Expected at most 2 entries with pageSize 2",
            resultsNode.size() <= 2);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that no filter at all returns an error
   */
  public void testGetAuditEntriesNoFilter() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("At least one filter is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with null arguments
   */
  public void testGetAuditEntriesNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpGetAuditEntries.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("At least one filter is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with a group that does not exist returns an error
   */
  public void testGetAuditEntriesGroupNotFound() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:bogusGroupThatDoesNotExist99999");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error for nonexistent group", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected group not found message",
          text.contains("Group not found"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a non-admin user without group admin gets access denied for group audits
   */
  public void testGetAuditEntriesGroupAccessDenied() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditDenied")
        .assignName("test:mcpAuditDenied")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit denied test").save();

    // grant VIEW so group is findable, but NOT admin
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpAuditDenied");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error for non-admin user", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected access denied message",
          text.contains("Access denied"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a non-admin user without stemAdmin gets access denied for stem audits
   */
  public void testGetAuditEntriesStemAccessDenied() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditStemDenied:group1")
        .assignName("test:mcpAuditStemDenied:group1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit stem denied test").save();

    // grant STEM_ATTR_READ so stem is findable, but NOT stemAdmin
    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(),
        "test:mcpAuditStemDenied", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "test:mcpAuditStemDenied");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error for non-stemAdmin user", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected access denied message",
          text.contains("Access denied"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a non-admin user cannot look at another subject's audit entries
   */
  public void testGetAuditEntriesSubjectAccessDenied() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // SUBJ0 trying to look at SUBJ1's audit entries
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectId", SubjectTestHelper.SUBJ1.getId());

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error for different subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected access denied message",
          text.contains("Access denied"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that auditType alone requires admin privilege
   */
  public void testGetAuditEntriesAuditTypeOnlyAccessDenied() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("auditType", "group");

      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

      assertTrue("Expected error for non-admin user with auditType only",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected access denied message",
          text.contains("Access denied"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that root user can query by auditType alone
   */
  public void testGetAuditEntriesAuditTypeOnlyAsRoot() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpAuditRootOnly")
        .assignName("test:mcpAuditRootOnly")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("audit root test").save();

    GrouperSession.startRootSession();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(
        SubjectFinder.findRootSubject());

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("auditType", "group");

    ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, authUser);

    assertFalse("Expected success for root user, got: " + result.toString(),
        result.get("isError").asBoolean());
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpGetAuditEntries.toolDefinition();

    assertEquals("audit_get", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("auditType"));
    assertNotNull(properties.get("groupName"));
    assertNotNull(properties.get("stemName"));
    assertNotNull(properties.get("subjectId"));
    assertNotNull(properties.get("subjectSourceId"));
    assertNotNull(properties.get("actionsPerformedBySubjectId"));
    assertNotNull(properties.get("actionsPerformedBySubjectSourceId"));
    assertNotNull(properties.get("fromDate"));
    assertNotNull(properties.get("toDate"));
    assertNotNull(properties.get("pageSize"));

    // no required fields for this tool
    assertNull(toolDef.get("inputSchema").get("required"));
  }
}

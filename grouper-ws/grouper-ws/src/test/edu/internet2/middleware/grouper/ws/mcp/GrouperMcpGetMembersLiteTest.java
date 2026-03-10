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
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpGetMembersLite (group_get_members MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpGetMembersLiteTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpGetMembersLiteTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpGetMembersLiteTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpGetMembersLiteTest("testGetMembersBasic"));
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
   * test getting members of a group with two members
   */
  public void testGetMembersBasic() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup1")
        .assignName("test:mcpGetMembersGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp getMembers").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup1");

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(50, responseNode.get("pageSize").asInt());
        assertEquals(1, responseNode.get("pageNumber").asInt());
        JsonNode subjects = responseNode.get("subjects");
        assertNotNull(subjects);
        assertTrue(subjects.isArray());
        assertEquals(2, subjects.size());

        // verify subject fields are present
        for (int i = 0; i < subjects.size(); i++) {
          assertNotNull(subjects.get(i).get("subjectId"));
          assertNotNull(subjects.get(i).get("sourceId"));
        }
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test getting members of an empty group
   */
  public void testGetMembersEmptyGroup() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup2")
        .assignName("test:mcpGetMembersGroup2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("empty test group").save();

    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup2");

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode subjects = responseNode.get("subjects");
        assertNotNull(subjects);
        assertTrue(subjects.isArray());
        assertEquals(0, subjects.size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with Immediate member filter
   */
  public void testGetMembersImmediateFilter() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup3")
        .assignName("test:mcpGetMembersGroup3")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp getMembers").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup3");
      arguments.put("memberFilter", "Immediate");

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode subjects = responseNode.get("subjects");
        assertEquals(1, subjects.size());
        assertEquals(SubjectTestHelper.SUBJ0.getId(),
            subjects.get(0).get("subjectId").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test paging with custom page size and page number
   */
  public void testGetMembersPaging() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup4")
        .assignName("test:mcpGetMembersGroup4")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp getMembers paging").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);
    group1.addMember(SubjectTestHelper.SUBJ2);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // page 1, size 2
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup4");
      arguments.put("pageSize", 2);
      arguments.put("pageNumber", 1);

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(2, responseNode.get("pageSize").asInt());
        assertEquals(1, responseNode.get("pageNumber").asInt());
        JsonNode subjects = responseNode.get("subjects");
        assertEquals(2, subjects.size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }

      // page 2, size 2
      arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup4");
      arguments.put("pageSize", 2);
      arguments.put("pageNumber", 2);

      result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(2, responseNode.get("pageSize").asInt());
        assertEquals(2, responseNode.get("pageNumber").asInt());
        JsonNode subjects = responseNode.get("subjects");
        assertEquals(1, subjects.size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with sourceIds filter
   */
  public void testGetMembersWithSourceIds() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup5")
        .assignName("test:mcpGetMembersGroup5")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp getMembers").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup5");
      arguments.put("sourceIds", "jdbc");

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode subjects = responseNode.get("subjects");
        assertTrue(subjects.size() >= 1);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that missing groupName returns an error
   */
  public void testGetMembersMissingGroupName() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("groupName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with null arguments
   */
  public void testGetMembersNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpGetMembersLite.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("groupName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpGetMembersLite.toolDefinition();

    assertEquals("group_get_members", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("groupName"));
    assertNotNull(properties.get("memberFilter"));
    assertNotNull(properties.get("privilegeListName"));
    assertNotNull(properties.get("sourceIds"));
    assertNotNull(properties.get("pageSize"));
    assertNotNull(properties.get("pageNumber"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("groupName", required.get(0).asText());
  }

  /**
   * test with NonImmediate member filter on a group with only immediate members
   */
  public void testGetMembersNonImmediateFilter() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpGetMembersGroup6")
        .assignName("test:mcpGetMembersGroup6")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp getMembers").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpGetMembersGroup6");
      arguments.put("memberFilter", "NonImmediate");

      ObjectNode result = GrouperMcpGetMembersLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode subjects = responseNode.get("subjects");
        assertEquals("Expected no non-immediate members", 0, subjects.size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

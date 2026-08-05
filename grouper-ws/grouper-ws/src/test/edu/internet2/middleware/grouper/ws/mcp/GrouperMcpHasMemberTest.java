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
import com.fasterxml.jackson.databind.node.ArrayNode;
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
 * unit tests for GrouperMcpHasMember (group_has_member MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpHasMemberTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpHasMemberTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpHasMemberTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpHasMemberTest("testHasMemberIsMember"));
    TestRunner.run(GrouperMcpHasMemberTest.class);
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
   * helper to build the subjects array with a single subject by subjectId
   * @param groupName the group name
   * @param subjectId the subject ID
   * @return arguments with subjects array
   */
  private ObjectNode buildSingleSubjectArgs(String groupName, String subjectId) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", groupName);
    ArrayNode subjects = arguments.putArray("subjects");
    ObjectNode subjectNode = subjects.addObject();
    subjectNode.put("subjectIdOrIdentifier", subjectId);
    return arguments;
  }

  /**
   * test that a subject that is a member returns IS_MEMBER
   */
  public void testHasMemberIsMember() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpHasMemberGroup1")
        .assignName("test:mcpHasMemberGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp hasMember").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = buildSingleSubjectArgs(
          "test:mcpHasMemberGroup1", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultNode = objectMapper.readTree(text);
        assertEquals("IS_MEMBER", resultNode.get("resultCode").asText());
        assertTrue(resultNode.get("isMember").asBoolean());
        assertEquals(SubjectTestHelper.SUBJ0.getId(), resultNode.get("subjectId").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a subject that is not a member returns IS_NOT_MEMBER
   */
  public void testHasMemberIsNotMember() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpHasMemberGroup2")
        .assignName("test:mcpHasMemberGroup2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp hasMember").save();

    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // SUBJ1 is NOT added to the group
      ObjectNode arguments = buildSingleSubjectArgs(
          "test:mcpHasMemberGroup2", SubjectTestHelper.SUBJ1.getId());

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultNode = objectMapper.readTree(text);
        assertEquals("IS_NOT_MEMBER", resultNode.get("resultCode").asText());
        assertFalse(resultNode.get("isMember").asBoolean());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test checking multiple subjects at once
   */
  public void testHasMemberMultipleSubjects() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpHasMemberGroup3")
        .assignName("test:mcpHasMemberGroup3")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp hasMember").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    // SUBJ1 is not a member
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpHasMemberGroup3");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subj0Node = subjects.addObject();
      subj0Node.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());
      ObjectNode subj1Node = subjects.addObject();
      subj1Node.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ1.getId());

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultsNode = objectMapper.readTree(text);
        assertTrue("Expected array of results", resultsNode.isArray());
        assertEquals(2, resultsNode.size());

        // find each subject's result
        boolean foundMember = false;
        boolean foundNonMember = false;
        for (int i = 0; i < resultsNode.size(); i++) {
          JsonNode node = resultsNode.get(i);
          if (SubjectTestHelper.SUBJ0.getId().equals(node.get("subjectId").asText())) {
            assertTrue(node.get("isMember").asBoolean());
            foundMember = true;
          } else if (SubjectTestHelper.SUBJ1.getId().equals(node.get("subjectId").asText())) {
            assertFalse(node.get("isMember").asBoolean());
            foundNonMember = true;
          }
        }
        assertTrue("Expected to find SUBJ0 as member", foundMember);
        assertTrue("Expected to find SUBJ1 as non-member", foundNonMember);
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
  public void testHasMemberWithMemberFilter() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpHasMemberGroup4")
        .assignName("test:mcpHasMemberGroup4")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp hasMember").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = buildSingleSubjectArgs(
          "test:mcpHasMemberGroup4", SubjectTestHelper.SUBJ0.getId());
      arguments.put("memberFilter", "Immediate");

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultNode = objectMapper.readTree(text);
        assertTrue(resultNode.get("isMember").asBoolean());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with subject identified by subjectIdentifier instead of subjectId
   */
  public void testHasMemberBySubjectIdentifier() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpHasMemberGroup5")
        .assignName("test:mcpHasMemberGroup5")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp hasMember").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:mcpHasMemberGroup5");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectIdOrIdentifier", "id.test.subject.0");

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultNode = objectMapper.readTree(text);
        assertTrue(resultNode.get("isMember").asBoolean());
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
  public void testHasMemberMissingGroupName() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("groupName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that missing subjects array returns an error
   */
  public void testHasMemberMissingSubjects() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:someGroup");

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("subjects array is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that empty subjects array returns an error
   */
  public void testHasMemberEmptySubjects() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:someGroup");
      arguments.putArray("subjects");

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("subjects array is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a subject with neither subjectId nor subjectIdentifier returns an error
   */
  public void testHasMemberSubjectMissingIdAndIdentifier() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:someGroup");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("sourceId", "jdbc");

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("must have subjectIdOrIdentifier"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a subject with both subjectId and subjectIdentifier returns an error
   */
  public void testHasMemberSubjectBothIdAndIdentifier() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:someGroup");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpHasMember.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not both"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpHasMember.toolDefinition();

    assertEquals("group_has_member", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("groupName"));
    assertNotNull(properties.get("subjects"));
    assertNotNull(properties.get("memberFilter"));
    assertNotNull(properties.get("privilegeListName"));
    assertNotNull(properties.get("pointInTimeFrom"));
    assertNotNull(properties.get("pointInTimeTo"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(2, required.size());
  }

  /**
   * test with null arguments
   */
  public void testHasMemberNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpHasMember.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("groupName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

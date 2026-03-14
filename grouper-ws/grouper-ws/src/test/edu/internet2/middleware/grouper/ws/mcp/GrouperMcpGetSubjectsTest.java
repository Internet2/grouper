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
 * unit tests for GrouperMcpGetSubjects (entity_get MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpGetSubjectsTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpGetSubjectsTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpGetSubjectsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpGetSubjectsTest("testGetSubjectBySubjectId"));
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
   * test looking up a subject by subjectId
   */
  public void testGetSubjectBySubjectId() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertNotNull(text);

      // parse the returned JSON and verify subject fields
      try {
        JsonNode subjectNode = objectMapper.readTree(text);
        assertEquals(SubjectTestHelper.SUBJ0.getId(), subjectNode.get("subjectId").asText());
        assertNotNull(subjectNode.get("name"));
        assertNotNull(subjectNode.get("sourceId"));
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test looking up a subject by subjectIdentifier
   */
  public void testGetSubjectBySubjectIdentifier() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      // test subjects have identifiers like "id.test.subject.0"
      arguments.put("subjectIdOrIdentifier", "id.test.subject.0");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode subjectNode = objectMapper.readTree(text);
        assertEquals(SubjectTestHelper.SUBJ0.getId(), subjectNode.get("subjectId").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test searching for subjects by search string
   */
  public void testGetSubjectsBySearchString() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("searchString", "test.");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultsNode = objectMapper.readTree(text);
        // search for "test." should return multiple test subjects
        assertTrue("Expected array of subjects", resultsNode.isArray());
        assertTrue("Expected multiple subjects, got: " + resultsNode.size(),
            resultsNode.size() >= 2);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that providing no lookup parameter returns an error
   */
  public void testGetSubjectsNoLookupParam() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected error about missing param",
          text.contains("One of subjectIdOrIdentifier or searchString is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that providing multiple lookup parameters returns an error
   */
  public void testGetSubjectsMultipleLookupParams() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());
      arguments.put("searchString", "test.");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected error about multiple params",
          text.contains("Only one of subjectIdOrIdentifier or searchString may be provided"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test looking up a subject that does not exist.
   * the WS layer returns SUCCESS with a subject that has a SUBJECT_NOT_FOUND result code,
   * and the MCP tool wraps that as a non-error result with the subject JSON.
   */
  public void testGetSubjectNotFound() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", "bogusSubjectIdThatDoesNotExist12345");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      // the WS layer returns a subject entry even for not-found subjects,
      // so the MCP tool returns it as a non-error result
      assertNotNull(result);
      assertNotNull(result.get("content"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filtering subjects by group membership
   */
  public void testGetSubjectsFilterByGroup() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpTestGroup1")
        .assignName("test:mcpTestGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // search with group filter
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("searchString", "test.");
      arguments.put("groupName", "test:mcpTestGroup1");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode resultsNode = objectMapper.readTree(text);
        // should only get subjects that are in the group and match search
        if (resultsNode.isArray()) {
          assertEquals("Expected 2 subjects in group", 2, resultsNode.size());
        }
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filtering subjects by group membership with member filter
   */
  public void testGetSubjectsFilterByGroupAndMemberFilter() {

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpTestGroup2")
        .assignName("test:mcpTestGroup2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("searchString", "test.");
      arguments.put("groupName", "test:mcpTestGroup2");
      arguments.put("memberFilter", "Immediate");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode subjectNode = objectMapper.readTree(text);
        // single immediate member matching search
        assertEquals(SubjectTestHelper.SUBJ0.getId(), subjectNode.get("subjectId").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with includeSubjectDetail
   */
  public void testGetSubjectWithSubjectDetail() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());
      arguments.put("includeSubjectDetail", true);

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode subjectNode = objectMapper.readTree(text);
        assertEquals(SubjectTestHelper.SUBJ0.getId(), subjectNode.get("subjectId").asText());
        // with includeSubjectDetail, we may get attributes
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpGetSubjects.toolDefinition();

    assertEquals("entity_get", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("subjectIdOrIdentifier"));
    assertNotNull(properties.get("searchString"));
    assertNotNull(properties.get("sourceIds"));
    assertNotNull(properties.get("groupName"));
    assertNotNull(properties.get("memberFilter"));
    assertNotNull(properties.get("privilegeListName"));
    assertNotNull(properties.get("includeSubjectDetail"));
    assertNotNull(properties.get("subjectAttributeNames"));
  }

  /**
   * test with null arguments
   */
  public void testGetSubjectsNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpGetSubjects.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("One of subjectId, subjectIdentifier, or searchString is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with sourceIds filter
   */
  public void testGetSubjectWithSourceIds() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0.getId());
      arguments.putArray("sourceIds").add("jdbc");

      ObjectNode result = GrouperMcpGetSubjects.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode subjectNode = objectMapper.readTree(text);
        assertEquals(SubjectTestHelper.SUBJ0.getId(), subjectNode.get("subjectId").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

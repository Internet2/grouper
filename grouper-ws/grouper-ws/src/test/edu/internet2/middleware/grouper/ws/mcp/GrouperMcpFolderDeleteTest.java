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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpFolderDelete (folder_delete MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpFolderDeleteTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpFolderDeleteTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpFolderDeleteTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpFolderDeleteTest("testDeleteFolderSuccess"));
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
   * test successfully deleting a folder
   */
  public void testDeleteFolderSuccess() {

    Stem stem1 = new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpDeleteFolder1")
        .assignName("test:mcpDeleteFolder1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test folder for mcp delete").save();

    String uuid = stem1.getUuid();

    stem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "test:mcpDeleteFolder1");

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("DELETE", responseNode.get("resultCode").asText());
        assertTrue(responseNode.get("success").asBoolean());
        assertEquals("test:mcpDeleteFolder1", responseNode.get("name").asText());
        assertEquals(uuid, responseNode.get("uuid").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }

      // verify the stem no longer exists
      Stem deletedStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpDeleteFolder1", false);
      assertNull("Stem should be deleted", deletedStem);

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test deleting a folder that does not exist
   */
  public void testDeleteFolderNotFound() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "test:bogusFolderThatDoesNotExist99999");

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Stem not found"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that missing stemName returns an error
   */
  public void testDeleteFolderMissingStemName() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("stemName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test with null arguments
   */
  public void testDeleteFolderNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpFolderDelete.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("stemName is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpFolderDelete.toolDefinition();

    assertEquals("folder_delete", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("stemName"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("stemName", required.get(0).asText());
  }

  /**
   * test that a stem with child groups cannot be deleted
   */
  public void testDeleteNonEmptyStemWithChildGroup() {

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpDeleteNonEmpty")
        .assignName("test:mcpDeleteNonEmpty")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test non-empty folder for mcp delete").save();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpDeleteNonEmpty:childGroup")
        .assignName("test:mcpDeleteNonEmpty:childGroup")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpDeleteNonEmpty", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "test:mcpDeleteNonEmpty");

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertTrue("Expected error for non-empty stem", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Should mention child groups",
          text.contains("child groups") || text.contains("child stems"));

      // verify the stem still exists
      Stem stillExists = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpDeleteNonEmpty", false);
      assertNotNull("Stem should still exist", stillExists);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that protected system stems cannot be deleted
   */
  public void testDeleteProtectedStem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "etc:someSystemStem");

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertTrue("Expected error for protected stem", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Should mention protected", text.contains("protected") || text.contains("built-in"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

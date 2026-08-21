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
import edu.internet2.middleware.grouper.GroupFinder;
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
 * unit tests for GrouperMcpGroupSave (group_save MCP tool), renameGroup action
 *
 * @author mchyzer
 */
public class GrouperMcpGroupSaveTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpGroupSaveTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpGroupSaveTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpGroupSaveTest("testRenameGroupSuccess"));
    TestRunner.run(GrouperMcpGroupSaveTest.class);
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
   * create a group owned by SUBJ0 to rename in a test
   * @param groupName
   * @param displayExtension
   * @param description
   * @return the group
   */
  private Group createGroupForSubj0(String groupName, String displayExtension, String description) {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit(groupName)
        .assignName(groupName)
        .assignCreateParentStemsIfNotExist(true)
        .assignDisplayExtension(displayExtension)
        .assignDescription(description).save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    return group;
  }

  /**
   * run the group_save tool as SUBJ0
   * @param arguments
   * @return the tool result
   */
  private ObjectNode executeAsSubj0(ObjectNode arguments) {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      return GrouperMcpGroupSave.execute(arguments, authUser);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * get the text of the first content entry of a tool result
   * @param result
   * @return the text
   */
  private String resultText(ObjectNode result) {
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * test renaming a group keeps its uuid, description, display extension, and memberships,
   * and by default does not keep the old name as an alternate ID path
   */
  public void testRenameGroupSuccess() {

    Group group1 = createGroupForSubj0("test:mcpRenameGroup1", "Mcp Rename Group 1",
        "test group for mcp rename");

    String uuid = group1.getUuid();

    group1.addMember(SubjectTestHelper.SUBJ1, false);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup1");
    arguments.put("newExtension", "mcpRenameGroup1b");

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    try {
      JsonNode responseNode = objectMapper.readTree(resultText(result));
      assertEquals("renameGroup", responseNode.get("action").asText());
      assertEquals("UPDATE", responseNode.get("resultCode").asText());
      assertTrue(responseNode.get("success").asBoolean());
      assertEquals("test:mcpRenameGroup1", responseNode.get("previousName").asText());
      assertEquals("test:mcpRenameGroup1b", responseNode.get("name").asText());
      assertEquals(uuid, responseNode.get("uuid").asText());

      // replaceAllSettings is false, so a rename leaves these alone
      assertEquals("Mcp Rename Group 1", responseNode.get("displayExtension").asText());
      assertEquals("test group for mcp rename", responseNode.get("description").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }

    // the group is the same object under the new name
    Group renamedGroup = GroupFinder.findByName("test:mcpRenameGroup1b", true);
    assertEquals(uuid, renamedGroup.getUuid());
    assertTrue("Membership should survive a rename",
        renamedGroup.hasMember(SubjectTestHelper.SUBJ1));

    // the old name is gone as a name
    assertNull("Old name should no longer be a group name",
        GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpRenameGroup1",
            false));

    // and it is not kept as an alternate ID path, matching the UI's unchecked
    // "Update alternate ID path" box
    Group byAlternateName = GroupFinder.findByAlternateName(
        GrouperSession.staticGrouperSession(), "test:mcpRenameGroup1", false);
    assertNull("Old name should not be kept as an alternate ID path by default", byAlternateName);
  }

  /**
   * test that setAlternateNameIfRename=true keeps the old name as the alternate ID path, so
   * lookups by the old name still resolve
   */
  public void testRenameGroupWithAlternateName() {

    Group group2 = createGroupForSubj0("test:mcpRenameGroup2", "Mcp Rename Group 2", null);

    String uuid = group2.getUuid();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup2");
    arguments.put("newExtension", "mcpRenameGroup2b");
    arguments.put("setAlternateNameIfRename", true);

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    assertNotNull(GroupFinder.findByName("test:mcpRenameGroup2b", false));

    Group byAlternateName = GroupFinder.findByAlternateName(
        GrouperSession.staticGrouperSession(), "test:mcpRenameGroup2", false);
    assertNotNull("Old name should be kept as an alternate ID path", byAlternateName);
    assertEquals(uuid, byAlternateName.getUuid());
  }

  /**
   * test renaming and changing the display extension in one call
   */
  public void testRenameGroupWithDisplayExtension() {

    createGroupForSubj0("test:mcpRenameGroup3", "Mcp Rename Group 3", null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup3");
    arguments.put("newExtension", "mcpRenameGroup3b");
    arguments.put("displayExtension", "Mcp Rename Group 3b");

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    Group renamedGroup = GroupFinder.findByName("test:mcpRenameGroup3b", true);
    assertEquals("Mcp Rename Group 3b", renamedGroup.getDisplayExtension());
  }

  /**
   * test that a fully qualified newExtension is rejected with a message about moving,
   * rather than failing somewhere inside GroupSave
   */
  public void testRenameGroupRejectsQualifiedName() {

    createGroupForSubj0("test:mcpRenameGroup4", "Mcp Rename Group 4", null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup4");
    arguments.put("newExtension", "test2:mcpRenameGroup4b");

    ObjectNode result = executeAsSubj0(arguments);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = resultText(result);
    assertTrue("Should explain that a group cannot be moved: " + text,
        text.contains("moved to a different folder"));

    // nothing happened
    assertNotNull(GroupFinder.findByName("test:mcpRenameGroup4", false));
  }

  /**
   * test that newExtension is required
   */
  public void testRenameGroupMissingNewExtension() {

    createGroupForSubj0("test:mcpRenameGroup5", "Mcp Rename Group 5", null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup5");

    ObjectNode result = executeAsSubj0(arguments);

    assertTrue("Expected error", result.get("isError").asBoolean());
    assertTrue(resultText(result).contains("newExtension is required"));
  }

  /**
   * test that a group cannot be renamed onto an existing group name
   */
  public void testRenameGroupOntoExistingName() {

    createGroupForSubj0("test:mcpRenameGroup6", "Mcp Rename Group 6", null);
    createGroupForSubj0("test:mcpRenameGroup6b", "Mcp Rename Group 6b", null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup6");
    arguments.put("newExtension", "mcpRenameGroup6b");

    ObjectNode result = executeAsSubj0(arguments);

    assertTrue("Expected error", result.get("isError").asBoolean());
    assertTrue("Should say the group already exists: " + resultText(result),
        resultText(result).contains("already exists"));

    // both groups are still there under their own names
    assertNotNull(GroupFinder.findByName("test:mcpRenameGroup6", false));
    assertNotNull(GroupFinder.findByName("test:mcpRenameGroup6b", false));
  }

  /**
   * test that a group in the protected stem cannot be renamed
   */
  public void testRenameProtectedGroup() {

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "etc:someSystemGroup");
    arguments.put("newExtension", "someSystemGroupRenamed");

    ObjectNode result = executeAsSubj0(arguments);

    assertTrue("Expected error for protected group", result.get("isError").asBoolean());
    String text = resultText(result);
    assertTrue("Should mention protected: " + text,
        text.contains("protected") || text.contains("built-in"));
  }

  /**
   * test that a rename which does not change anything is reported as no change
   */
  public void testRenameGroupSameExtension() {

    createGroupForSubj0("test:mcpRenameGroup7", "Mcp Rename Group 7", null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameGroup");
    arguments.put("groupName", "test:mcpRenameGroup7");
    arguments.put("newExtension", "mcpRenameGroup7");

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    try {
      JsonNode responseNode = objectMapper.readTree(resultText(result));
      assertEquals("NO_CHANGE", responseNode.get("resultCode").asText());
      assertEquals("test:mcpRenameGroup7", responseNode.get("name").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that renameGroup is advertised in the tool definition along with its parameters
   */
  public void testToolDefinitionHasRenameGroup() {

    ObjectNode toolDef = GrouperMcpGroupSave.toolDefinition();

    assertEquals("group_save", toolDef.get("name").asText());
    assertTrue("Description should list renameGroup",
        toolDef.get("description").asText().contains("renameGroup"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("newExtension"));
    assertNotNull(properties.get("setAlternateNameIfRename"));
    assertEquals("boolean", properties.get("setAlternateNameIfRename").get("type").asText());

    JsonNode actionEnum = properties.get("action").get("enum");
    assertNotNull(actionEnum);
    boolean foundRenameGroup = false;
    for (int i = 0; i < actionEnum.size(); i++) {
      if ("renameGroup".equals(actionEnum.get(i).asText())) {
        foundRenameGroup = true;
        break;
      }
    }
    assertTrue("renameGroup should be in the action enum", foundRenameGroup);
  }
}

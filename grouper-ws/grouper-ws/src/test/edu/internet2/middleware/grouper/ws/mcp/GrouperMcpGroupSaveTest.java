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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.abac.GrouperAbacScriptedGroup;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpGroupMembership;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpGroupSave (group_save MCP tool), renameGroup and moveGroup actions, and
 * the scripted group actions setJexlScript, removeJexlScript and analyzeJexlScript
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
   * create a group owned by SUBJ0 to rename or move in a test
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
   * get the resultCode of a successful tool result
   * @param result
   * @return the result code
   */
  private String resultCode(ObjectNode result) {
    try {
      return objectMapper.readTree(resultText(result)).get("resultCode").asText();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse result JSON: " + resultText(result), e);
    }
  }

  /**
   * removeComposite removes the composite even though SUBJ0 cannot see the factor groups, and
   * removing it again reports NO_CHANGE.  it used to fail with "leftFactorGroup is required"
   */
  public void testRemoveComposite() {

    createGroupForSubj0("test:mcpRemoveCompositeOwner", "Mcp Remove Composite Owner", null);

    // factor groups SUBJ0 cannot see, since removing a composite needs only UPDATE on the owner
    Group left = new GroupSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpRemoveCompositeLeft").assignCreateParentStemsIfNotExist(true).save();
    Group right = new GroupSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpRemoveCompositeRight").assignCreateParentStemsIfNotExist(true).save();

    GroupFinder.findByName("test:mcpRemoveCompositeOwner", true)
        .addCompositeMember(CompositeType.INTERSECTION, left, right);
    assertTrue(GroupFinder.findByName("test:mcpRemoveCompositeOwner", true).isHasComposite());

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "removeComposite");
    arguments.put("groupName", "test:mcpRemoveCompositeOwner");

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("DELETE", resultCode(result));
    assertFalse("the composite should be gone",
        GroupFinder.findByName("test:mcpRemoveCompositeOwner", true).isHasComposite());

    // nothing left to remove
    result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("NO_CHANGE", resultCode(result));
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
   * create a folder SUBJ0 can create groups in, to move a group to in a test
   * @param folderName
   * @return the folder
   */
  private Stem createFolderForSubj0(String folderName) {
    Stem folder = new StemSave(GrouperSession.staticGrouperSession())
        .assignName(folderName).assignCreateParentStemsIfNotExist(true).save();
    folder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    return folder;
  }

  /**
   * @param groupName group to move
   * @param newFolderName folder to move it to
   * @return moveGroup arguments
   */
  private ObjectNode moveArguments(String groupName, String newFolderName) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "moveGroup");
    arguments.put("groupName", groupName);
    if (newFolderName != null) {
      arguments.put("newFolderName", newFolderName);
    }
    return arguments;
  }

  /**
   * test moving a group keeps its uuid, group ID, display extension, description, and
   * memberships, and by default does not keep the old name as an alternate ID path
   */
  public void testMoveGroupSuccess() {

    Group group = createGroupForSubj0("test:mcpMoveFrom1:mcpMoveGroup1", "Mcp Move Group 1",
        "test group for mcp move");
    group.addMember(SubjectTestHelper.SUBJ1, false);
    String uuid = group.getUuid();
    createFolderForSubj0("test:mcpMoveTo1");

    ObjectNode result = executeAsSubj0(moveArguments("test:mcpMoveFrom1:mcpMoveGroup1", "test:mcpMoveTo1"));

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());

    try {
      JsonNode responseNode = objectMapper.readTree(resultText(result));
      assertEquals("moveGroup", responseNode.get("action").asText());
      assertEquals("UPDATE", responseNode.get("resultCode").asText());
      assertTrue(responseNode.get("success").asBoolean());
      assertEquals("test:mcpMoveFrom1:mcpMoveGroup1", responseNode.get("previousName").asText());
      assertEquals("test:mcpMoveTo1:mcpMoveGroup1", responseNode.get("name").asText());
      assertEquals(uuid, responseNode.get("uuid").asText());
      assertEquals("Mcp Move Group 1", responseNode.get("displayExtension").asText());
      assertEquals("test group for mcp move", responseNode.get("description").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }

    Group movedGroup = GroupFinder.findByName("test:mcpMoveTo1:mcpMoveGroup1", true);
    assertEquals(uuid, movedGroup.getUuid());
    assertTrue("Membership should survive a move", movedGroup.hasMember(SubjectTestHelper.SUBJ1));

    assertNull("Old name should no longer be a group name",
        GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpMoveFrom1:mcpMoveGroup1",
            false));
    assertNull("Old name should not be kept as an alternate ID path by default",
        GroupFinder.findByAlternateName(GrouperSession.staticGrouperSession(),
            "test:mcpMoveFrom1:mcpMoveGroup1", false));
  }

  /**
   * test that setAlternateNameIfMove=true keeps the old name as the alternate ID path
   */
  public void testMoveGroupWithAlternateName() {

    Group group = createGroupForSubj0("test:mcpMoveFrom2:mcpMoveGroup2", "Mcp Move Group 2", null);
    createFolderForSubj0("test:mcpMoveTo2");

    ObjectNode arguments = moveArguments("test:mcpMoveFrom2:mcpMoveGroup2", "test:mcpMoveTo2");
    arguments.put("setAlternateNameIfMove", true);

    ObjectNode result = executeAsSubj0(arguments);

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());

    Group byAlternateName = GroupFinder.findByAlternateName(GrouperSession.staticGrouperSession(),
        "test:mcpMoveFrom2:mcpMoveGroup2", false);
    assertNotNull("Old name should be kept as an alternate ID path", byAlternateName);
    assertEquals(group.getUuid(), byAlternateName.getUuid());
  }

  /**
   * test the moves which are refused before anything is changed: no folder given, the folder
   * the group is already in, a folder which does not exist, a folder SUBJ0 cannot create groups
   * in (which is not found, the same as one which does not exist), the root folder, a protected
   * folder, a group SUBJ0 is not an admin of, and a group SUBJ0 cannot see
   */
  public void testMoveGroupRefused() {

    // SUBJ0 can create in the group's own folder, so the same-folder case finds the folder
    createFolderForSubj0("test:mcpMoveFrom3");
    createGroupForSubj0("test:mcpMoveFrom3:mcpMoveGroup3", "Mcp Move Group 3", null);

    ObjectNode result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", null));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("newFolderName is required"));

    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "test:mcpMoveFrom3"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("already in folder"));

    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "test:mcpMoveNoSuchFolder"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("was not found, or you cannot create groups"));

    // a folder which exists but SUBJ0 cannot create groups in is not found
    Stem noCreate = new StemSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpMoveNoCreate").assignCreateParentStemsIfNotExist(true).save();
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "test:mcpMoveNoCreate"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("was not found, or you cannot create groups"));

    // the folder is looked up by name only, so the root folder's uuid, the only way to name a
    // folder whose name is blank, finds nothing.  SUBJ0 is given CREATE on the root to show that
    // even then it is not found
    Stem root = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    root.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", root.getUuid()));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("was not found, or you cannot create groups"));

    // the same folder in a different case: not found on a case sensitive database, and told its
    // real name on a case insensitive one.  either way it is refused
    createFolderForSubj0("test:mcpMoveCase3");
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "test:MCPMOVECASE3"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result),
        resultText(result).contains("was not found, or you cannot create groups")
        || resultText(result).contains("is named 'test:mcpMoveCase3'"));
    assertNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(),
        "test:mcpMoveCase3:mcpMoveGroup3", false));

    // a folder SUBJ0 can create in under the built-in objects folder gets as far as the
    // protection check
    createFolderForSubj0("etc:mcpMoveTarget3");
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "etc:mcpMoveTarget3"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result),
        resultText(result).contains("protected") || resultText(result).contains("built-in"));

    // SUBJ0 can read this group but is not an admin of it
    Group notAdmin = new GroupSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpMoveFrom3:mcpMoveNotAdmin").assignCreateParentStemsIfNotExist(true).save();
    notAdmin.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    createFolderForSubj0("test:mcpMoveTo3");
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveNotAdmin", "test:mcpMoveTo3"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("not an admin"));

    // SUBJ0 cannot see this group at all, since the test's setUp turns off granting view to all
    new GroupSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpMoveFrom3:mcpMoveHidden").assignCreateParentStemsIfNotExist(true).save();
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveHidden", "test:mcpMoveTo3"));
    assertTrue(result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("was not found, or you cannot see it"));

    // nothing moved
    assertNotNull(GroupFinder.findByName("test:mcpMoveFrom3:mcpMoveGroup3", false));
    assertNotNull(GroupFinder.findByName("test:mcpMoveFrom3:mcpMoveNotAdmin", false));
    assertNotNull(GroupFinder.findByName("test:mcpMoveFrom3:mcpMoveHidden", false));

    // the folder which was not found above is found once SUBJ0 can create groups in it, so it
    // was the privilege which kept it out
    noCreate.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    result = executeAsSubj0(moveArguments("test:mcpMoveFrom3:mcpMoveGroup3", "test:mcpMoveNoCreate"));
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertNotNull(GroupFinder.findByName("test:mcpMoveNoCreate:mcpMoveGroup3", false));
  }

  /**
   * an OAuth caller whose readwrite scope covers the folder a group is in, but not the folder it
   * would move to, is refused, since a move puts the group at a name the caller was not granted
   */
  public void testMoveGroupOutsideReadwriteScope() {

    createGroupForSubj0("test:mcpMoveFrom4:mcpMoveGroup4", "Mcp Move Group 4", null);
    createFolderForSubj0("test:mcpMoveTo4");

    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveFrom4:mcpMoveGroup4", "test:mcpMoveTo4"),
        oauthSubj0WithFolders("test:mcpMoveFrom4"));

    assertTrue("Expected error, got: " + result.toString(), result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("test:mcpMoveTo4:mcpMoveGroup4"));
    assertTrue(resultText(result), resultText(result).contains("outside your consented read-write scope"));
    assertNotNull(GroupFinder.findByName("test:mcpMoveFrom4:mcpMoveGroup4", false));
  }

  /**
   * an OAuth caller whose readwrite scope consents to the group itself, by uuid, but to no folder,
   * may change the group but not move it into another folder: the consent covers the group where
   * it is, and a new name needs a consented folder, the same as creating a group there
   */
  public void testMoveGroupConsentedGroupOnly() {

    Group group = createGroupForSubj0("test:mcpMoveFrom5:mcpMoveGroup5", "Mcp Move Group 5", null);
    createFolderForSubj0("test:mcpMoveTo5");

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    List<String> groups = new ArrayList<String>();
    groups.add(group.getUuid());
    authUser.setConsentReadwriteGroups(groups);

    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveFrom5:mcpMoveGroup5", "test:mcpMoveTo5"), authUser);

    assertTrue("Expected error, got: " + result.toString(), result.get("isError").asBoolean());
    assertTrue(resultText(result), resultText(result).contains("test:mcpMoveTo5:mcpMoveGroup5"));
    assertTrue(resultText(result), resultText(result).contains("outside your consented read-write scope"));
    assertNotNull(GroupFinder.findByName("test:mcpMoveFrom5:mcpMoveGroup5", false));
  }

  /**
   * @param folders consented readwrite folders
   * @return an OAuth auth user for SUBJ0 whose readwrite scope is those folders
   */
  private GrouperMcpAuthUser oauthSubj0WithFolders(String... folders) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    List<String> folderList = new ArrayList<String>();
    for (String folder : folders) {
      folderList.add(folder);
    }
    authUser.setConsentReadwriteFolders(folderList);
    return authUser;
  }

  /**
   * @param arguments tool arguments
   * @param authUser who is calling, as SUBJ0
   * @return the tool result
   */
  private ObjectNode executeAsSubj0(ObjectNode arguments, GrouperMcpAuthUser authUser) {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpGroupSave.execute(arguments, authUser);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * make a folder whose real name is newParentName plus its extension, and whose alternate name
   * is oldName, the way moving a folder in the UI leaves it
   * @param oldName the folder's name before the move, kept as its alternate name
   * @param newParentName the folder it is moved into
   * @return the folder, under its real name
   */
  private Stem createMovedFolder(String oldName, String newParentName) {
    Stem folder = new StemSave(GrouperSession.staticGrouperSession())
        .assignName(oldName).assignCreateParentStemsIfNotExist(true).save();
    Stem newParent = new StemSave(GrouperSession.staticGrouperSession())
        .assignName(newParentName).assignCreateParentStemsIfNotExist(true).save();
    new StemMove(folder, newParent).assignAlternateName(true).save();
    Stem moved = StemFinder.findByName(GrouperSession.staticGrouperSession(), oldName, true);
    assertEquals("the old name should find the folder under its new name",
        newParentName + ":" + GrouperUtil.extensionFromName(oldName), moved.getName());
    return moved;
  }

  /**
   * a folder given by its alternate (old) name is refused with its current name, since the UI's
   * approval box shows the folder as given and the user has to approve where the group really
   * goes.  given by its current name, the same move works
   */
  public void testMoveGroupToFolderByAlternateName() {

    createGroupForSubj0("test:mcpMoveSandbox6:mcpMoveGroup6", "Mcp Move Group 6", null);
    Stem payroll = createMovedFolder("test:mcpMoveSandbox6:payroll", "test:mcpMoveHr6");
    payroll.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);

    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveSandbox6:mcpMoveGroup6", "test:mcpMoveSandbox6:payroll"));

    assertTrue("Expected error, got: " + result.toString(), result.get("isError").asBoolean());
    assertTrue(resultText(result),
        resultText(result).contains("is named 'test:mcpMoveHr6:payroll'"));
    assertNotNull(GroupFinder.findByName("test:mcpMoveSandbox6:mcpMoveGroup6", false));

    result = executeAsSubj0(
        moveArguments("test:mcpMoveSandbox6:mcpMoveGroup6", "test:mcpMoveHr6:payroll"));

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertNotNull(GroupFinder.findByName("test:mcpMoveHr6:payroll:mcpMoveGroup6", false));
  }

  /**
   * a group in the built-in objects folder cannot be moved out by an alternate (old) name it has
   * elsewhere.  execute() checks only the name as given, so this is caught by the check on the
   * group's real name
   */
  public void testMoveGroupOutOfEtcByAlternateName() {

    Group group = createGroupForSubj0("test:mcpMoveOld7:mcpMoveGroup7", "Mcp Move Group 7", null);
    Stem etcFolder = new StemSave(GrouperSession.staticGrouperSession())
        .assignName("etc:mcpMoveEtc7").assignCreateParentStemsIfNotExist(true).save();
    new GroupMove(group, etcFolder).assignAlternateName(true).save();
    createFolderForSubj0("test:mcpMoveTo7");

    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveOld7:mcpMoveGroup7", "test:mcpMoveTo7"));

    assertTrue("Expected error, got: " + result.toString(), result.get("isError").asBoolean());
    String text = resultText(result);
    assertTrue(text, text.contains("protected") || text.contains("built-in"));
    assertNotNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(),
        "etc:mcpMoveEtc7:mcpMoveGroup7", false));
  }

  /**
   * an OAuth consent for a folder does not let a group elsewhere be moved by an alternate (old)
   * name it has under that folder: the scope check is on the group's real name too
   */
  public void testMoveGroupByAlternateNameOutsideScope() {

    Group group = createGroupForSubj0("test:mcpMoveSandbox8:mcpMoveGroup8", "Mcp Move Group 8", null);
    Stem hr = new StemSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpMoveHr8").assignCreateParentStemsIfNotExist(true).save();
    new GroupMove(group, hr).assignAlternateName(true).save();
    createFolderForSubj0("test:mcpMoveTo8");

    // the destination is consented, so only the group's real name is out of scope
    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveSandbox8:mcpMoveGroup8", "test:mcpMoveTo8"),
        oauthSubj0WithFolders("test:mcpMoveSandbox8", "test:mcpMoveTo8"));

    assertTrue("Expected error, got: " + result.toString(), result.get("isError").asBoolean());
    String text = resultText(result);
    assertTrue(text, text.contains("test:mcpMoveHr8:mcpMoveGroup8"));
    assertTrue(text, text.contains("outside your consented read-write scope"));
    assertNotNull(GroupFinder.findByName("test:mcpMoveHr8:mcpMoveGroup8", false));
  }

  /**
   * an OAuth caller with both folders consented can move a group, so the scope checks do not
   * refuse a move they should allow
   */
  public void testMoveGroupInsideReadwriteScope() {

    createGroupForSubj0("test:mcpMoveFrom9:mcpMoveGroup9", "Mcp Move Group 9", null);
    createFolderForSubj0("test:mcpMoveTo9");

    ObjectNode result = executeAsSubj0(
        moveArguments("test:mcpMoveFrom9:mcpMoveGroup9", "test:mcpMoveTo9"),
        oauthSubj0WithFolders("test:mcpMoveFrom9", "test:mcpMoveTo9"));

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertNotNull(GroupFinder.findByName("test:mcpMoveTo9:mcpMoveGroup9", false));
  }

  /**
   * moving a group back to the name it kept as its alternate name works with the default
   * setAlternateNameIfMove=false.  Grouper refuses a group whose name and alternate name are the
   * same, so the alternate name is dropped rather than the move failing with "already exists"
   */
  public void testMoveGroupBackToAlternateName() {

    createFolderForSubj0("test:mcpMoveFrom10");
    createGroupForSubj0("test:mcpMoveFrom10:mcpMoveGroup10", "Mcp Move Group 10", null);
    createFolderForSubj0("test:mcpMoveTo10");

    ObjectNode arguments = moveArguments("test:mcpMoveFrom10:mcpMoveGroup10", "test:mcpMoveTo10");
    arguments.put("setAlternateNameIfMove", true);
    ObjectNode result = executeAsSubj0(arguments);
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("test:mcpMoveFrom10:mcpMoveGroup10",
        GroupFinder.findByName("test:mcpMoveTo10:mcpMoveGroup10", true).getAlternateName());

    result = executeAsSubj0(moveArguments("test:mcpMoveTo10:mcpMoveGroup10", "test:mcpMoveFrom10"));

    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    Group movedBack = GroupFinder.findByName("test:mcpMoveFrom10:mcpMoveGroup10", true);
    assertEquals("test:mcpMoveFrom10:mcpMoveGroup10", movedBack.getName());
    assertNull("the alternate name was the new name, so it is dropped", movedBack.getAlternateName());
  }

  /**
   * setAlternateNameIfMove=false leaves an alternate name the group already has, when it is not
   * the name being moved to, and true replaces it with the name the group had before this move
   */
  public void testMoveGroupAlternateNameKeptOrReplaced() {

    createFolderForSubj0("test:mcpMoveA11");
    createGroupForSubj0("test:mcpMoveA11:mcpMoveGroup11", "Mcp Move Group 11", null);
    createFolderForSubj0("test:mcpMoveB11");
    createFolderForSubj0("test:mcpMoveC11");

    ObjectNode arguments = moveArguments("test:mcpMoveA11:mcpMoveGroup11", "test:mcpMoveB11");
    arguments.put("setAlternateNameIfMove", true);
    ObjectNode result = executeAsSubj0(arguments);
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("test:mcpMoveA11:mcpMoveGroup11",
        GroupFinder.findByName("test:mcpMoveB11:mcpMoveGroup11", true).getAlternateName());

    // false: the alternate name from the first move stays
    result = executeAsSubj0(moveArguments("test:mcpMoveB11:mcpMoveGroup11", "test:mcpMoveC11"));
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("test:mcpMoveA11:mcpMoveGroup11",
        GroupFinder.findByName("test:mcpMoveC11:mcpMoveGroup11", true).getAlternateName());

    // true: replaced by the name the group had before this move
    arguments = moveArguments("test:mcpMoveC11:mcpMoveGroup11", "test:mcpMoveB11");
    arguments.put("setAlternateNameIfMove", true);
    result = executeAsSubj0(arguments);
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    assertEquals("test:mcpMoveC11:mcpMoveGroup11",
        GroupFinder.findByName("test:mcpMoveB11:mcpMoveGroup11", true).getAlternateName());
  }

  /** a script for the scripted group tests: SUBJ1 is in a and not in b */
  private static final String JEXL_SCRIPT = "entity.memberOf('test:mcpJexl:a') && !entity.memberOf('test:mcpJexl:b')";

  /**
   * groups for the scripted group tests: test:mcpJexl:target, which SUBJ0 is an admin of and which
   * has SUBJ3 as a direct member, and the groups the script uses, test:mcpJexl:a with SUBJ1 and
   * SUBJ2 and test:mcpJexl:b with SUBJ2, which SUBJ0 can read.  SUBJ0 has MCP readwrite.  the sql
   * cache is synced, since that is what a script reads
   * @return the target group
   */
  private Group createScriptedGroupTestGroups() {

    Group target = createGroupForSubj0("test:mcpJexl:target", "target", "scripted group test");
    target.addMember(SubjectTestHelper.SUBJ3, false);
    // can see the group, but is not an admin of it
    target.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);

    Group groupA = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:mcpJexl:a")
        .assignCreateParentStemsIfNotExist(true).save();
    groupA.addMember(SubjectTestHelper.SUBJ1, false);
    groupA.addMember(SubjectTestHelper.SUBJ2, false);
    groupA.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    Group groupB = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:mcpJexl:b")
        .assignCreateParentStemsIfNotExist(true).save();
    groupB.addMember(SubjectTestHelper.SUBJ2, false);
    groupB.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    // SUBJ0 cannot see this one.  SUBJ1 is in it, so add and remove counts built from its real
    // members would differ from ones which treat it as empty
    Group secret = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:mcpJexl:secret")
        .assignCreateParentStemsIfNotExist(true).save();
    secret.addMember(SubjectTestHelper.SUBJ4, false);
    secret.addMember(SubjectTestHelper.SUBJ1, false);

    Group readwriteGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpJexlReadwriteUsers").assignCreateParentStemsIfNotExist(true).save();
    readwriteGroup.addMember(SubjectTestHelper.SUBJ0, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperToolAccess.GROUP_READWRITE,
        "test:mcpJexlReadwriteUsers");
    GrouperMcpGroupMembership.clearCache();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheFullSync");

    return target;
  }

  /**
   * undo the MCP readwrite setting createScriptedGroupTestGroups made
   */
  private void removeScriptedGroupTestAccess() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(GrouperToolAccess.GROUP_READWRITE);
    GrouperMcpGroupMembership.clearCache();
  }

  /**
   * @param action setJexlScript, removeJexlScript or analyzeJexlScript
   * @param script the script, or null to leave it out
   * @return group_save arguments for the target group
   */
  private ObjectNode jexlScriptArguments(String action, String script) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", action);
    arguments.put("groupName", "test:mcpJexl:target");
    if (script != null) {
      arguments.put("script", script);
    }
    return arguments;
  }

  /**
   * @param result a successful tool result
   * @return its JSON
   */
  private JsonNode resultJson(ObjectNode result) {
    assertFalse("Expected success, got: " + result.toString(), result.get("isError").asBoolean());
    try {
      return objectMapper.readTree(resultText(result));
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse result JSON: " + resultText(result), e);
    }
  }

  /**
   * preview, save, save again, preview the saved script, and remove.  the preview counts the whole
   * script, what the sync would add and remove, and one person; a save changes no members; removing
   * leaves the members
   */
  public void testJexlScriptAnalyzeSetRemove() {

    Group target = createScriptedGroupTestGroups();
    try {

      ObjectNode arguments = jexlScriptArguments("analyzeJexlScript", JEXL_SCRIPT);
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ1.getId());
      arguments.put("sourceId", "jdbc");
      arguments.put("partCounts", true);
      JsonNode analysis = resultJson(executeAsSubj0(arguments));
      assertEquals(1, analysis.get("populationCount").asInt());
      // SUBJ1 is added, SUBJ3 is removed
      assertEquals(1, analysis.get("wouldAdd").asInt());
      assertEquals(1, analysis.get("wouldRemove").asInt());
      assertTrue(analysis.get("subject").get("selectedByScript").asBoolean());
      assertTrue(analysis.get("parts").size() > 0);
      assertNull(analysis.get("warnings"));
      assertNull(analysis.get("cannotBeSaved"));

      // without partCounts, only the whole script is counted
      analysis = resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript", JEXL_SCRIPT)));
      assertEquals(1, analysis.get("populationCount").asInt());
      assertNull(analysis.get("parts"));
      assertNotNull(analysis.get("wouldAdd"));

      arguments = jexlScriptArguments("analyzeJexlScript", JEXL_SCRIPT);
      arguments.put("membershipChanges", false);
      analysis = resultJson(executeAsSubj0(arguments));
      assertNull(analysis.get("wouldAdd"));

      JsonNode saved = resultJson(executeAsSubj0(jexlScriptArguments("setJexlScript", JEXL_SCRIPT)));
      assertEquals("INSERT", saved.get("resultCode").asText());
      assertEquals(1, saved.get("populationCount").asInt());
      assertEquals(JEXL_SCRIPT, GrouperAbacScriptedGroup.internal_retrieveScript(target));
      assertNull(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target));
      // the sync changes members, the save does not
      assertTrue(target.hasImmediateMember(SubjectTestHelper.SUBJ3));
      assertFalse(target.hasMember(SubjectTestHelper.SUBJ1));

      saved = resultJson(executeAsSubj0(jexlScriptArguments("setJexlScript", JEXL_SCRIPT)));
      assertEquals("NO_CHANGE", saved.get("resultCode").asText());

      // the saved script is previewed when none is given
      analysis = resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript", null)));
      assertEquals(JEXL_SCRIPT, analysis.get("script").asText());

      // but a blank one given is refused, not swapped for the saved script
      for (String blankScript : new String[] {"", "   "}) {
        ObjectNode blankResult = executeAsSubj0(jexlScriptArguments("analyzeJexlScript", blankScript));
        assertTrue(blankResult.toString(), blankResult.get("isError").asBoolean());
        assertTrue(resultText(blankResult), resultText(blankResult).contains("script is required"));
      }

      String changedScript = "entity.memberOf('test:mcpJexl:a')";
      saved = resultJson(executeAsSubj0(jexlScriptArguments("setJexlScript", changedScript)));
      assertEquals("UPDATE", saved.get("resultCode").asText());
      assertEquals(changedScript, GrouperAbacScriptedGroup.internal_retrieveScript(target));

      JsonNode removed = resultJson(executeAsSubj0(jexlScriptArguments("removeJexlScript", null)));
      assertEquals("DELETE", removed.get("resultCode").asText());
      assertNull(GrouperAbacScriptedGroup.internal_retrieveScript(target));
      assertTrue(target.hasImmediateMember(SubjectTestHelper.SUBJ3));

      removed = resultJson(executeAsSubj0(jexlScriptArguments("removeJexlScript", null)));
      assertEquals("NO_CHANGE", removed.get("resultCode").asText());

    } finally {
      removeScriptedGroupTestAccess();
    }
  }

  /**
   * what saving or previewing a script is refused for, and that a refusal saves nothing
   */
  public void testJexlScriptRefused() {

    Group target = createScriptedGroupTestGroups();
    try {

      // the group itself
      ObjectNode result = executeAsSubj0(jexlScriptArguments("setJexlScript",
          "entity.memberOf('test:mcpJexl:target')"));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("cannot refer to the group itself"));
      assertTrue(resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript",
          "entity.memberOf('test:mcpJexl:target')"))).get("cannotBeSaved").asBoolean());

      // a group SUBJ0 cannot even see is refused with the same message as one which does not exist,
      // and the preview counts nothing from it.  its member is made unresolvable, so the
      // unresolvable subjects warning would list it if that check used the group's real members
      new GcDbAccess().sql("update grouper_members set subject_resolution_resolvable = 'F' "
          + "where subject_id = ? and subject_source = 'jdbc'")
          .addBindVar(SubjectTestHelper.SUBJ4.getId()).executeSql();
      result = executeAsSubj0(jexlScriptArguments("setJexlScript", "entity.memberOf('test:mcpJexl:secret')"));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("test:mcpJexl:secret"));
      assertTrue(resultText(result), resultText(result).contains("which you can read"));
      JsonNode secretPreview = resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript",
          "entity.memberOf('test:mcpJexl:secret')")));
      assertEquals(0, secretPreview.get("populationCount").asInt());
      assertNull(secretPreview.get("wouldAdd"));
      assertTrue(secretPreview.get("warnings").toString().contains("which you can read"));
      assertFalse(secretPreview.toString(), secretPreview.toString().contains("unresolvable"));
      assertFalse(secretPreview.toString(), secretPreview.toString().contains(SubjectTestHelper.SUBJ4.getId()));

      // a composite cannot be a scripted group, and the preview says so
      Group composite = createGroupForSubj0("test:mcpJexl:composite", "composite", null);
      composite.addCompositeMember(CompositeType.COMPLEMENT,
          GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpJexl:a", true),
          GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:mcpJexl:b", true));
      ObjectNode compositeArguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      compositeArguments.put("groupName", "test:mcpJexl:composite");
      result = executeAsSubj0(compositeArguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("composite"));
      compositeArguments.put("action", "analyzeJexlScript");
      assertTrue(resultJson(executeAsSubj0(compositeArguments)).get("warnings").toString().contains("composite"));

      // a group with another kind of loader is left alone
      Group loaderGroup = createGroupForSubj0("test:mcpJexl:loaderGroup", "loaderGroup", null);
      loaderGroup.getAttributeDelegate().assignAttributeByName(
          GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
          + ":legacy:attribute:legacyGroupType_grouperLoader");
      ObjectNode loaderArguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      loaderArguments.put("groupName", "test:mcpJexl:loaderGroup");
      result = executeAsSubj0(loaderArguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("SQL loader"));
      assertNull(GrouperAbacScriptedGroup.internal_retrieveScript(loaderGroup));

      // the analysis says it cannot be saved there, but still counts what switching would do
      loaderArguments.put("action", "analyzeJexlScript");
      JsonNode loaderPreview = resultJson(executeAsSubj0(loaderArguments));
      assertTrue(loaderPreview.toString(), loaderPreview.get("cannotBeSaved").asBoolean());
      assertTrue(loaderPreview.get("warnings").toString().contains("SQL loader"));
      assertTrue(loaderPreview.toString(), loaderPreview.has("populationCount"));

      // too long to save is refused before it is parsed or counted, so a script which would not
      // even parse gets the length message, not the parse error
      String tooLong = text("grouperLoaderEditJexlScriptTooLong");
      result = executeAsSubj0(jexlScriptArguments("analyzeJexlScript",
          "entity.memberOf(" + StringUtils.repeat(" ", GrouperAbacScriptedGroup.MAX_SCRIPT_BYTES)));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains(tooLong));

      // a group SUBJ0 can see but not read is refused as not readable, since they know it exists
      Group viewOnly = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:mcpJexl:viewOnly")
          .assignCreateParentStemsIfNotExist(true).save();
      viewOnly.addMember(SubjectTestHelper.SUBJ4, false);
      viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheFullSync");
      result = executeAsSubj0(jexlScriptArguments("analyzeJexlScript", "entity.memberOf('test:mcpJexl:viewOnly')"));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("test:mcpJexl:viewOnly"));
      assertFalse(resultText(result), resultText(result).contains("which you can read"));

      // a group which does not exist yet, which could later be made in a folder SUBJ0 cannot see
      result = executeAsSubj0(jexlScriptArguments("setJexlScript", "entity.memberOf('test:mcpJexl:notYet')"));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("test:mcpJexl:notYet"));
      assertTrue(resultText(result), resultText(result).contains("which you can read"));
      JsonNode preview = resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript",
          "entity.memberOf('test:mcpJexl:notYet')")));
      assertTrue(preview.toString(), preview.get("warnings").toString().contains("test:mcpJexl:notYet"));

      // a group named by an old name it no longer has
      Group renamed = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:mcpJexl:oldName")
          .assignCreateParentStemsIfNotExist(true).save();
      renamed.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      new GroupSave(GrouperSession.staticGrouperSession()).assignGroupNameToEdit("test:mcpJexl:oldName")
          .assignName("test:mcpJexl:newName").assignSetAlternateNameIfRename(true)
          .assignReplaceAllSettings(false).assignSaveMode(SaveMode.UPDATE).save();
      result = executeAsSubj0(jexlScriptArguments("setJexlScript", "entity.memberOf('test:mcpJexl:oldName')"));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("test:mcpJexl:oldName"));

      // a script which cannot be parsed, for a save and for a preview
      String invalid = text("grouperLoaderEditJexlScriptInvalid");
      result = executeAsSubj0(jexlScriptArguments("setJexlScript", "entity.memberOf("));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains(invalid));
      result = executeAsSubj0(jexlScriptArguments("analyzeJexlScript", "entity.memberOf("));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains(invalid));
      assertFalse(resultText(result), resultText(result).contains("GrouperLoaderJexlScriptFullSync"));

      // longer than the column holds
      result = executeAsSubj0(jexlScriptArguments("setJexlScript",
          JEXL_SCRIPT + StringUtils.repeat(" ", GrouperAbacScriptedGroup.MAX_SCRIPT_BYTES)));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains(tooLong));

      // this Grouper does not let the sources be chosen
      ObjectNode arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds").add("jdbc");
      result = executeAsSubj0(arguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("subjectSourceIds"));

      // a group which does not exist yet
      arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.put("groupName", "test:mcpJexl:notThere");
      result = executeAsSubj0(arguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("create it with createGroup"));

      // SUBJ1 is not an admin of the group
      GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
      try {
        result = GrouperMcpGroupSave.execute(jexlScriptArguments("setJexlScript", JEXL_SCRIPT),
            new GrouperMcpAuthUser(SubjectTestHelper.SUBJ1));
      } finally {
        GrouperSession.stopQuietly(session);
      }
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("not allowed to edit the script"));

      // outside the consented folders: the save is refused and the preview, a read, is not, but it
      // says the save would be refused
      GrouperMcpAuthUser outsideScope = oauthSubj0WithFolders("test:mcpJexlOther");
      result = executeAsSubj0(jexlScriptArguments("setJexlScript", JEXL_SCRIPT), outsideScope);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      JsonNode outsidePreview = resultJson(executeAsSubj0(jexlScriptArguments("analyzeJexlScript", JEXL_SCRIPT), outsideScope));
      assertTrue(outsidePreview.toString(), outsidePreview.get("cannotBeSaved").asBoolean());
      assertTrue(outsidePreview.toString(), outsidePreview.has("populationCount"));

      // nothing above was saved
      assertNull(GrouperAbacScriptedGroup.internal_retrieveScript(target));

      // the preview needs readwrite access, though it is a read
      removeScriptedGroupTestAccess();
      result = executeAsSubj0(jexlScriptArguments("analyzeJexlScript", JEXL_SCRIPT));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("needs the same access as saving"));

    } finally {
      removeScriptedGroupTestAccess();
    }
  }

  /**
   * @param key a text key the checks use
   * @return its text, which has to be there, or a comparison with a missing message would pass
   */
  private static String text(String key) {
    String text = GrouperTextContainer.textOrNull(key);
    assertNotNull(key, text);
    return text;
  }

  /**
   * a group cannot have its script set or removed by an alternate (old) name it has in a consented
   * folder, or outside the built-in objects folder: execute() checks only the name as given, so these
   * are caught by the checks on the group's real name
   */
  public void testJexlScriptByAlternateName() {

    createScriptedGroupTestGroups();
    try {

      // its real name is outside the consented folder, its old name inside it
      Group moved = createGroupForSubj0("test:mcpJexlSandbox:moved", "moved", null);
      Stem hr = new StemSave(GrouperSession.staticGrouperSession())
          .assignName("test:mcpJexlHr").assignCreateParentStemsIfNotExist(true).save();
      new GroupMove(moved, hr).assignAlternateName(true).save();

      GrouperMcpAuthUser sandboxScope = oauthSubj0WithFolders("test:mcpJexlSandbox");
      for (String action : new String[] {"setJexlScript", "removeJexlScript"}) {
        ObjectNode arguments = jexlScriptArguments(action, JEXL_SCRIPT);
        arguments.put("groupName", "test:mcpJexlSandbox:moved");
        ObjectNode result = executeAsSubj0(arguments, sandboxScope);
        assertTrue(action + ": " + result.toString(), result.get("isError").asBoolean());
        assertTrue(resultText(result), resultText(result).contains("test:mcpJexlHr:moved"));
        assertTrue(resultText(result), resultText(result).contains("outside your consented read-write scope"));
      }

      // its real name is in the built-in objects folder
      Group inEtc = createGroupForSubj0("test:mcpJexlOld:inEtc", "inEtc", null);
      Stem etcFolder = new StemSave(GrouperSession.staticGrouperSession())
          .assignName("etc:mcpJexlEtc").assignCreateParentStemsIfNotExist(true).save();
      new GroupMove(inEtc, etcFolder).assignAlternateName(true).save();

      for (String action : new String[] {"setJexlScript", "removeJexlScript"}) {
        ObjectNode arguments = jexlScriptArguments(action, JEXL_SCRIPT);
        arguments.put("groupName", "test:mcpJexlOld:inEtc");
        ObjectNode result = executeAsSubj0(arguments);
        assertTrue(action + ": " + result.toString(), result.get("isError").asBoolean());
        assertTrue(resultText(result), resultText(result).contains("etc:mcpJexlEtc:inEtc"));
      }

      assertNull(GrouperAbacScriptedGroup.internal_retrieveScript(moved));
      assertNull(GrouperAbacScriptedGroup.internal_retrieveScript(inEtc));

    } finally {
      removeScriptedGroupTestAccess();
    }
  }

  /**
   * where Grouper lets the subject sources be chosen: chosen sources are kept for the group, left
   * out they stay as they are, the defaults or an empty array remove them, and a source which
   * cannot be chosen is refused
   */
  public void testJexlScriptSubjectSourceIds() {

    Group target = createScriptedGroupTestGroups();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.allowUserOverrideSubjectSourceIds", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.availableSubjectSourceIds", "jdbc, grouperEntities");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.globalDefaultSubjectSourceIds", "jdbc");
    GrouperAbac.clearCaches();
    try {

      Set<String> chosen = new HashSet<String>();
      chosen.add("jdbc");
      chosen.add("grouperEntities");

      ObjectNode arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds").add("jdbc").add("grouperEntities");
      JsonNode saved = resultJson(executeAsSubj0(arguments));
      assertEquals("INSERT", saved.get("resultCode").asText());
      assertEquals(chosen, new HashSet<String>(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target)));

      // left out, the group keeps its sources
      saved = resultJson(executeAsSubj0(jexlScriptArguments("setJexlScript", JEXL_SCRIPT)));
      assertEquals("NO_CHANGE", saved.get("resultCode").asText());
      assertEquals(chosen, new HashSet<String>(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target)));

      // the same as the defaults, so nothing is kept for the group
      arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds").add("jdbc");
      saved = resultJson(executeAsSubj0(arguments));
      assertEquals("UPDATE", saved.get("resultCode").asText());
      assertNull(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target));

      // chosen again, then an empty array goes back to the defaults
      arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds").add("grouperEntities").add("jdbc");
      resultJson(executeAsSubj0(arguments));
      assertEquals(chosen, new HashSet<String>(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target)));
      arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds");
      saved = resultJson(executeAsSubj0(arguments));
      assertEquals("UPDATE", saved.get("resultCode").asText());
      assertNull(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target));

      // not one of the sources which can be chosen
      arguments = jexlScriptArguments("setJexlScript", JEXL_SCRIPT);
      arguments.putArray("subjectSourceIds").add("jdbc").add("g:isa");
      ObjectNode result = executeAsSubj0(arguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("cannot be chosen"));
      assertNull(GrouperAbacScriptedGroup.internal_retrieveSourceIds(target));

    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.allowUserOverrideSubjectSourceIds");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.availableSubjectSourceIds");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.globalDefaultSubjectSourceIds");
      GrouperAbac.clearCaches();
      removeScriptedGroupTestAccess();
    }
  }

  /**
   * a JSON null script or sourceId is not given, rather than the text "null"
   */
  public void testJexlScriptNullInputs() {

    createScriptedGroupTestGroups();
    try {

      ObjectNode arguments = jexlScriptArguments("setJexlScript", null);
      arguments.putNull("script");
      ObjectNode result = executeAsSubj0(arguments);
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(resultText(result), resultText(result).contains("script is required"));

      resultJson(executeAsSubj0(jexlScriptArguments("setJexlScript", JEXL_SCRIPT)));

      // the group's own script is previewed, and every source is searched for the person
      arguments = jexlScriptArguments("analyzeJexlScript", null);
      arguments.putNull("script");
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ1.getId());
      arguments.putNull("sourceId");
      JsonNode analysis = resultJson(executeAsSubj0(arguments));
      assertEquals(JEXL_SCRIPT, analysis.get("script").asText());
      assertTrue(analysis.get("subject").get("selectedByScript").asBoolean());

    } finally {
      removeScriptedGroupTestAccess();
    }
  }

  /**
   * the scripted group actions and their inputs are advertised
   */
  public void testToolDefinitionHasJexlScript() {

    ObjectNode toolDef = GrouperMcpGroupSave.toolDefinition();
    assertTrue(toolDef.get("description").asText().contains("analyzeJexlScript"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertEquals("string", properties.get("script").get("type").asText());
    assertEquals("array", properties.get("subjectSourceIds").get("type").asText());
    assertEquals("boolean", properties.get("partCounts").get("type").asText());
    assertEquals("boolean", properties.get("membershipChanges").get("type").asText());
    assertNotNull(properties.get("subjectIdOrIdentifier"));

    String actions = properties.get("action").get("enum").toString();
    assertTrue(actions, actions.contains("\"setJexlScript\""));
    assertTrue(actions, actions.contains("\"removeJexlScript\""));
    assertTrue(actions, actions.contains("\"analyzeJexlScript\""));
  }

  /**
   * test that moveGroup is advertised in the tool definition along with its parameters
   */
  public void testToolDefinitionHasMoveGroup() {

    ObjectNode toolDef = GrouperMcpGroupSave.toolDefinition();

    assertTrue(toolDef.get("description").asText().contains("moveGroup"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("newFolderName"));
    assertEquals("boolean", properties.get("setAlternateNameIfMove").get("type").asText());

    JsonNode actionEnum = properties.get("action").get("enum");
    boolean foundMoveGroup = false;
    for (int i = 0; i < actionEnum.size(); i++) {
      if ("moveGroup".equals(actionEnum.get(i).asText())) {
        foundMoveGroup = true;
        break;
      }
    }
    assertTrue("moveGroup should be in the action enum", foundMoveGroup);
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

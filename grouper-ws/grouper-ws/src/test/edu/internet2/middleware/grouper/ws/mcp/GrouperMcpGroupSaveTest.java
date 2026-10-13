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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpGroupSave (group_save MCP tool), renameGroup and moveGroup actions
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

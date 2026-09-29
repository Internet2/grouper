/*******************************************************************************
 * Copyright 2026 Internet2
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for folders protected from MCP writes via grouper.mcp.protectedFolders (GRP-7383).
 * the tools run as a sysadmin (GrouperSystem) on purpose: the lock down applies to everyone
 *
 * @author mchyzer
 */
public class GrouperMcpProtectedFoldersTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpProtectedFoldersTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpProtectedFoldersTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpProtectedFoldersTest("testAlternateNameProtected"));
    TestRunner.run(GrouperMcpProtectedFoldersTest.class);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** the folder protected in these tests */
  private static final String PROTECTED_FOLDER = "test:payroll";

  /** a group in the protected folder */
  private Group protectedGroup = null;

  /** a group in a folder whose name starts with the protected folder name, not protected */
  private Group similarNameGroup = null;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    // trailing colon on purpose, it should be tolerated
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG, "app:somethingElse, " + PROTECTED_FOLDER + ":");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    this.protectedGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(PROTECTED_FOLDER + ":sub:payrollGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    this.protectedGroup.addMember(SubjectTestHelper.SUBJ1, false);

    this.similarNameGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(PROTECTED_FOLDER + "X:otherGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    this.similarNameGroup.addMember(SubjectTestHelper.SUBJ1, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG);
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * auth user for the sysadmin the tools run as
   * @return the auth user
   */
  private static GrouperMcpAuthUser rootAuthUser() {
    return new GrouperMcpAuthUser(GrouperSession.staticGrouperSession().getSubject());
  }

  /**
   * a subjects array with one subject in it, as add/delete member take it
   * @param subjectId the subject id
   * @return the array
   */
  private static ArrayNode subjects(String subjectId) {
    ArrayNode subjects = objectMapper.createArrayNode();
    ObjectNode subject = objectMapper.createObjectNode();
    subject.put("subjectIdOrIdentifier", subjectId);
    subjects.add(subject);
    return subjects;
  }

  /**
   * assert the tool refused the write because of the protected folder, and the message
   * names the folder and points to the UI
   * @param result the tool result
   */
  private static void assertProtectedFolderError(ObjectNode result) {
    assertTrue("Expected error, got: " + result, result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text, text.contains("'" + PROTECTED_FOLDER + "'"));
    assertTrue(text, text.contains("Grouper UI"));
  }

  /**
   * the matching is on whole folder names, and the config is parsed
   */
  public void testProtectedResourcesMatching() {
    assertEquals(PROTECTED_FOLDER, GrouperMcpProtectedResources.protectedFolderForStemName(PROTECTED_FOLDER));
    assertEquals(PROTECTED_FOLDER, GrouperMcpProtectedResources.protectedFolderForStemName(PROTECTED_FOLDER + ":a:b"));
    assertEquals(PROTECTED_FOLDER, GrouperMcpProtectedResources.protectedFolderForGroupName(PROTECTED_FOLDER + ":a:notThereYet"));
    assertNull(GrouperMcpProtectedResources.protectedFolderForStemName(PROTECTED_FOLDER + "X"));
    assertNull(GrouperMcpProtectedResources.protectedFolderForGroupName(PROTECTED_FOLDER + "X:otherGroup"));
    assertNull(GrouperMcpProtectedResources.protectedFolderForStemName("test"));
    assertTrue(GrouperMcpProtectedResources.isProtectedGroupName(this.protectedGroup.getName()));
    assertFalse(GrouperMcpProtectedResources.isProtectedGroupName(this.similarNameGroup.getName()));
  }

  /**
   * nothing configured means nothing extra is protected
   */
  public void testNoFoldersConfigured() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG, "");

    assertEquals(0, GrouperMcpProtectedResources.protectedFolderNames().size());
    assertFalse(GrouperMcpProtectedResources.isProtectedGroupName(this.protectedGroup.getName()));

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.protectedGroup.getName());
    arguments.set("subjects", subjects(SubjectTestHelper.SUBJ2.getId()));
    ObjectNode result = GrouperMcpAddMember.execute(arguments, rootAuthUser());
    assertFalse("Expected success, got: " + result, result.get("isError").asBoolean());
    assertTrue(this.protectedGroup.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * group_delete is refused in the protected folder, allowed in the similar named one
   */
  public void testGroupDelete() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.protectedGroup.getName());
    assertProtectedFolderError(GrouperMcpGroupDelete.execute(arguments, rootAuthUser()));
    assertNotNull(GroupFinder.findByName(this.protectedGroup.getName(), false));

    arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.similarNameGroup.getName());
    ObjectNode result = GrouperMcpGroupDelete.execute(arguments, rootAuthUser());
    assertFalse("Expected success, got: " + result, result.get("isError").asBoolean());
    assertNull(GroupFinder.findByName(this.similarNameGroup.getName(), false));
  }

  /**
   * add member, replace all members, and delete member are refused
   */
  public void testMemberships() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.protectedGroup.getName());
    arguments.set("subjects", subjects(SubjectTestHelper.SUBJ2.getId()));
    assertProtectedFolderError(GrouperMcpAddMember.execute(arguments, rootAuthUser()));

    arguments.put("replaceAllExisting", true);
    assertProtectedFolderError(GrouperMcpAddMember.execute(arguments, rootAuthUser()));

    arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.protectedGroup.getName());
    arguments.set("subjects", subjects(SubjectTestHelper.SUBJ1.getId()));
    assertProtectedFolderError(GrouperMcpDeleteMember.execute(arguments, rootAuthUser()));

    assertTrue(this.protectedGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(this.protectedGroup.hasMember(SubjectTestHelper.SUBJ2));

    // the similar named folder is not affected
    arguments = objectMapper.createObjectNode();
    arguments.put("groupName", this.similarNameGroup.getName());
    arguments.set("subjects", subjects(SubjectTestHelper.SUBJ1.getId()));
    ObjectNode result = GrouperMcpDeleteMember.execute(arguments, rootAuthUser());
    assertFalse("Expected success, got: " + result, result.get("isError").asBoolean());
    assertFalse(this.similarNameGroup.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * group_save is refused for every action, creating a group included
   */
  public void testGroupSave() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createGroup");
    arguments.put("groupName", PROTECTED_FOLDER + ":newGroup");
    assertProtectedFolderError(GrouperMcpGroupSave.execute(arguments, rootAuthUser()));
    assertNull(GroupFinder.findByName(PROTECTED_FOLDER + ":newGroup", false));

    arguments = objectMapper.createObjectNode();
    arguments.put("action", "updateGroupPart");
    arguments.put("groupName", this.protectedGroup.getName());
    arguments.put("description", "changed by mcp");
    assertProtectedFolderError(GrouperMcpGroupSave.execute(arguments, rootAuthUser()));
  }

  /**
   * folder_delete is refused on the protected folder and folders under it
   */
  public void testFolderDelete() {
    Stem emptyStem = new StemSave(GrouperSession.staticGrouperSession())
        .assignName(PROTECTED_FOLDER + ":emptyFolder").assignCreateParentStemsIfNotExist(true).save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemName", emptyStem.getName());
    assertProtectedFolderError(GrouperMcpFolderDelete.execute(arguments, rootAuthUser()));
    assertNotNull(StemFinder.findByName(emptyStem.getName(), false));
  }

  /**
   * OAuth consent: a readwrite folder which is, or is under, a protected folder or etc is
   * refused.  a folder which only contains a protected folder, or has a similar name, is allowed
   */
  public void testConsentScopeFolders() {
    // exact match and anything under it
    assertEquals(PROTECTED_FOLDER, GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList(PROTECTED_FOLDER)));
    assertEquals(PROTECTED_FOLDER + ":sub", GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList(PROTECTED_FOLDER + ":sub")));

    // etc and anything under it
    assertEquals("etc", GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList("etc")));
    assertEquals("etc:mcp", GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList("etc:mcp")));

    // the protected one is found wherever it is in the list, and the first one is returned
    assertEquals(PROTECTED_FOLDER + ":sub", GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList("test:other", PROTECTED_FOLDER + ":sub", "etc")));

    // a folder which contains the protected folder is fine, the tools block the protected part
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeFolderName(GrouperUtil.toList("test")));

    // similar name is not protected
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList(PROTECTED_FOLDER + "X", "test:other", "etcX")));

    // nothing picked
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeFolderName(new ArrayList<String>()));
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeFolderName(null));
  }

  /**
   * OAuth consent: a readwrite group in a protected folder or etc, or a system group
   * outside etc, is refused
   */
  public void testConsentScopeGroups() {
    assertEquals(this.protectedGroup.getName(), GrouperMcpProtectedResources.firstProtectedScopeGroupName(
        GrouperUtil.toList(this.protectedGroup.getName())));
    assertEquals("etc:someGroup", GrouperMcpProtectedResources.firstProtectedScopeGroupName(
        GrouperUtil.toList("test:other:group", "etc:someGroup")));
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeGroupName(
        GrouperUtil.toList(this.similarNameGroup.getName(), "test:other:group")));
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeGroupName(null));

    // a system group which was moved out of etc is still refused.  the system group names
    // are cached for the JVM so the cache is cleared around the config change
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "test:admins:wheel");
    GrouperMcpProtectedResources.clearCache();
    try {
      assertEquals("test:admins:wheel", GrouperMcpProtectedResources.firstProtectedScopeGroupName(
          GrouperUtil.toList("test:admins:wheel")));
      assertNull(GrouperMcpProtectedResources.firstProtectedScopeGroupName(
          GrouperUtil.toList("test:admins:notWheel")));
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.group");
      GrouperMcpProtectedResources.clearCache();
    }
  }

  /**
   * OAuth consent: with no protected folders configured, the folder is allowed but etc is
   * still refused
   */
  public void testConsentScopeNoFoldersConfigured() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG, "");

    assertNull(GrouperMcpProtectedResources.firstProtectedScopeFolderName(GrouperUtil.toList(PROTECTED_FOLDER)));
    assertNull(GrouperMcpProtectedResources.firstProtectedScopeGroupName(
        GrouperUtil.toList(this.protectedGroup.getName())));
    assertEquals("etc:mcp", GrouperMcpProtectedResources.firstProtectedScopeFolderName(
        GrouperUtil.toList("etc:mcp")));
  }

  /**
   * OAuth consent: the error texts the consent screen shows name the picked folder or group
   */
  public void testConsentScopeErrorText() {
    try {
      GrouperTextContainer.assignThreadLocalVariable("scopeName", PROTECTED_FOLDER);
      String folderText = GrouperTextContainer.textOrNull("oauthConsentReadwriteFolderProtected");
      assertTrue(folderText, folderText.contains("The folder " + PROTECTED_FOLDER + " is protected"));

      GrouperTextContainer.assignThreadLocalVariable("scopeName", this.protectedGroup.getName());
      String groupText = GrouperTextContainer.textOrNull("oauthConsentReadwriteGroupProtected");
      assertTrue(groupText, groupText.contains("The group " + this.protectedGroup.getName() + " is protected"));
    } finally {
      GrouperTextContainer.resetThreadLocalVariableMap();
    }
  }

  /**
   * a group moved into a protected folder is protected under its old name too
   */
  public void testAlternateNameProtected() {
    Group movedGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:unprotected:movedGroup")
        .assignCreateParentStemsIfNotExist(true).save();

    // move sets the old name as the alternate name
    movedGroup.move(StemFinder.findByName(PROTECTED_FOLDER, true));
    assertEquals(PROTECTED_FOLDER + ":movedGroup", GroupFinder.findByName("test:unprotected:movedGroup", true).getName());

    assertEquals(PROTECTED_FOLDER, GrouperMcpProtectedResources.protectedFolderForGroupName("test:unprotected:movedGroup"));

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:unprotected:movedGroup");
    assertProtectedFolderError(GrouperMcpGroupDelete.execute(arguments, rootAuthUser()));
    assertNotNull(GroupFinder.findByName(PROTECTED_FOLDER + ":movedGroup", false));
  }
}

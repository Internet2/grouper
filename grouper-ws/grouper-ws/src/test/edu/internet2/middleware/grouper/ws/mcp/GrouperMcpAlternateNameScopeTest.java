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

import java.util.Arrays;

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
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security issue 54 (mcpAlternateNameScopeBypass): MCP write tools gate the group or folder a
 * name resolves to (a name also matches an alternate, old, name), not the typed string, and
 * write by what was checked.
 *
 * <p>A group and a folder are made under the consented folder, then moved out of it keeping the
 * old name as the alternate name.  Before the fix every call by the old name below is allowed.</p>
 */
public class GrouperMcpAlternateNameScopeTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** in the consent */
  private static final String SANDBOX = "test:altName:sandbox";

  /** not in the consent */
  private static final String FINANCE = "test:altName:finance";

  /** old (alternate) name of the moved group */
  private static final String OLD_GROUP_NAME = SANDBOX + ":g1";

  /** old (alternate) name of the moved folder */
  private static final String OLD_STEM_NAME = SANDBOX + ":f1";

  /** */
  private Group movedGroup;

  /** */
  private Stem movedStem;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpAlternateNameScopeTest("testGroupDeleteByOldNameDenied"));
  }

  /**
   * @param name
   */
  public GrouperMcpAlternateNameScopeTest(String name) {
    super(name);
  }

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

    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    Stem sandboxStem = new StemSave(rootSession).assignName(SANDBOX).assignCreateParentStemsIfNotExist(true).save();
    Stem financeStem = new StemSave(rootSession).assignName(FINANCE).assignCreateParentStemsIfNotExist(true).save();
    sandboxStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    financeStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    Group group = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(OLD_GROUP_NAME).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group.addMember(SubjectTestHelper.SUBJ2, false);
    new GroupMove(group, financeStem).assignAlternateName(true).save();
    this.movedGroup = GroupFinder.findByUuid(rootSession, group.getUuid(), true);
    assertEquals(FINANCE + ":g1", this.movedGroup.getName());
    assertEquals(OLD_GROUP_NAME, this.movedGroup.getAlternateName());

    Stem stem = new StemSave(rootSession).assignName(OLD_STEM_NAME).save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    new StemMove(stem, financeStem).assignAlternateName(true).save();
    this.movedStem = StemFinder.findByUuid(rootSession, stem.getUuid(), true);
    assertEquals(FINANCE + ":f1", this.movedStem.getName());
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
   * an OAuth caller with read-write consent only on the sandbox folder
   * @param subject who
   * @return the auth user
   */
  private static GrouperMcpAuthUser sandboxUser(Subject subject) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteFolders(Arrays.asList(SANDBOX));
    return authUser;
  }

  /**
   * @param result tool result
   * @return its text
   */
  private static String text(ObjectNode result) {
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * the write-up's reproduction: group_delete by the old name is refused, the group stays
   */
  public void testGroupDeleteByOldNameDenied() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", OLD_GROUP_NAME);
      ObjectNode result = GrouperMcpGroupDelete.execute(arguments, sandboxUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertNotNull(GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.movedGroup.getUuid(), false));
  }

  /**
   * add_member, delete_member and assign_privileges by the old name are refused
   */
  public void testMembershipAndPrivilegeByOldNameDenied() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = sandboxUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", OLD_GROUP_NAME);
      arguments.putArray("subjects").addObject().put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ1_ID);
      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());

      arguments = objectMapper.createObjectNode();
      arguments.put("groupName", OLD_GROUP_NAME);
      arguments.putArray("subjects").addObject().put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ2_ID);
      result = GrouperMcpDeleteMember.execute(arguments, authUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());

      arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ1_ID);
      arguments.put("groupName", OLD_GROUP_NAME);
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);
      result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.movedGroup.getUuid(), true);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(group.hasRead(SubjectTestHelper.SUBJ1));
  }

  /**
   * group_save updateGroupPart by the old name is refused, the description is unchanged
   */
  public void testGroupSaveByOldNameDenied() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "updateGroupPart");
      arguments.put("groupName", OLD_GROUP_NAME);
      arguments.put("description", "changed through an old name");
      ObjectNode result = GrouperMcpGroupSave.execute(arguments, sandboxUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.movedGroup.getUuid(), true);
    assertFalse("changed through an old name".equals(group.getDescription()));
  }

  /**
   * folder_delete and folder_save by the old folder name are refused
   */
  public void testFolderByOldNameDenied() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = sandboxUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "updateFolderPart");
      arguments.put("stemName", OLD_STEM_NAME);
      arguments.put("description", "changed through an old name");
      ObjectNode result = GrouperMcpFolderSave.execute(arguments, authUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());

      arguments = objectMapper.createObjectNode();
      arguments.put("stemName", OLD_STEM_NAME);
      result = GrouperMcpFolderDelete.execute(arguments, authUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.movedStem.getUuid(), false);
    assertNotNull(stem);
    assertFalse("changed through an old name".equals(stem.getDescription()));
  }

  /**
   * a group moved into etc from outside cannot be deleted through its old name, even by a wheel
   * caller with no OAuth scope (protected-resource rule on the real name)
   */
  public void testGroupMovedIntoEtcByOldNameDenied() {
    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    Group group = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:altName:toEtc").save();
    Stem etcTarget = new StemSave(rootSession).assignName("etc:altNameMoved").assignCreateParentStemsIfNotExist(true).save();
    new GroupMove(group, etcTarget).assignAlternateName(true).save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:altName:toEtc");
    ObjectNode result = GrouperMcpGroupDelete.execute(arguments, new GrouperMcpAuthUser(SubjectFinder.findRootSubject()));
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertNotNull(GroupFinder.findByUuid(rootSession, group.getUuid(), false));
  }

  /**
   * a caller who cannot see the moved group is not told where it went
   */
  public void testDenialDoesNotNameHiddenGroup() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", OLD_GROUP_NAME);
      ObjectNode result = GrouperMcpGroupDelete.execute(arguments, sandboxUser(SubjectTestHelper.SUBJ1));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertFalse(text(result), text(result).contains(FINANCE));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * the template input check judges a group input on what it resolves to
   */
  public void testTemplateInputScopeUsesResolvedName() {
    GrouperMcpAuthUser authUser = sandboxUser(SubjectTestHelper.SUBJ0);
    assertFalse(GrouperMcpResolvedTargetGate.isGroupNameInReadwriteScope(authUser, OLD_GROUP_NAME));
    assertFalse(GrouperMcpResolvedTargetGate.isStemNameInReadwriteScope(authUser, OLD_STEM_NAME));
    // a name that does not exist is judged on the string, as before
    assertTrue(GrouperMcpResolvedTargetGate.isGroupNameInReadwriteScope(authUser, SANDBOX + ":notCreatedYet"));
  }

  /**
   * controls: a group under its current, in-scope name can still be changed, and a new in-scope
   * group can be created
   */
  public void testInScopeCurrentNameAllowed() {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(SANDBOX + ":g2").save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = sandboxUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "createGroup");
      arguments.put("groupName", SANDBOX + ":g3");
      ObjectNode result = GrouperMcpGroupSave.execute(arguments, authUser);
      assertFalse(result.toString(), result.get("isError").asBoolean());

      arguments = objectMapper.createObjectNode();
      arguments.put("groupName", SANDBOX + ":g2");
      result = GrouperMcpGroupDelete.execute(arguments, authUser);
      assertFalse(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

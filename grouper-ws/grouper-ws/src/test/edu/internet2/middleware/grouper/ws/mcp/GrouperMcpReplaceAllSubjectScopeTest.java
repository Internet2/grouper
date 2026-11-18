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
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
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
 * Security issue 80 (mcpReplaceAllSubjectScope): group_add_member with replaceAllExisting also
 * removes every current member not in the request, so when the OAuth subject scope is limited
 * each of those removals has to be in scope, checked before anything changes.
 *
 * <p>Group test:replaceAll has members SUBJ0, SUBJ1, SUBJ2; SUBJ0 is ADMIN.  Before the fix the
 * first case removes SUBJ1 and SUBJ2.</p>
 */
public class GrouperMcpReplaceAllSubjectScopeTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** the group */
  private static final String GROUP_NAME = "test:replaceAll";

  /** */
  private Group group;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpReplaceAllSubjectScopeTest("testReplaceAllRemovingOutOfScopeMembersDenied"));
  }

  /**
   * @param name
   */
  public GrouperMcpReplaceAllSubjectScopeTest(String name) {
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

    this.group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.group.addMember(SubjectTestHelper.SUBJ0, false);
    this.group.addMember(SubjectTestHelper.SUBJ1, false);
    this.group.addMember(SubjectTestHelper.SUBJ2, false);
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
   * @param subjectId a jdbc test subject id
   * @return its scope key
   */
  private static String key(String subjectId) {
    return "jdbc||" + subjectId;
  }

  /**
   * SUBJ0 as an OAuth caller, as the servlet builds it from a token
   * @param subjects the subject keys, or null for no subject restriction
   * @return the auth user
   */
  private static GrouperMcpAuthUser oauthUser(List<String> subjects) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteGroups(Arrays.asList(GROUP_NAME));
    authUser.setConsentReadwriteSubjects(subjects);
    return authUser;
  }

  /**
   * replace all with the given subjects
   * @param authUser caller
   * @param fieldName field or null
   * @param subjectIds subjects to keep
   * @return result
   */
  private static ObjectNode replaceAll(GrouperMcpAuthUser authUser, String fieldName, String... subjectIds) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", GROUP_NAME);
    arguments.put("replaceAllExisting", true);
    if (fieldName != null) {
      arguments.put("fieldName", fieldName);
    }
    ArrayNode subjects = arguments.putArray("subjects");
    for (String subjectId : subjectIds) {
      subjects.addObject().put("subjectIdOrIdentifier", subjectId);
    }
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpAddMember.execute(arguments, authUser);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * the reproduction: keep the one consented subject, the two others would be removed but are
   * outside the scope, so nothing changes
   */
  public void testReplaceAllRemovingOutOfScopeMembersDenied() {
    ObjectNode result = replaceAll(oauthUser(Arrays.asList(key(SubjectTestHelper.SUBJ0_ID))), null,
        SubjectTestHelper.SUBJ0_ID);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * the same for a privilege field: admins
   */
  public void testReplaceAllAdminsOutOfScopeDenied() {
    this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    this.group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN, false);

    ObjectNode result = replaceAll(oauthUser(Arrays.asList(key(SubjectTestHelper.SUBJ0_ID))), "admins",
        SubjectTestHelper.SUBJ0_ID);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    Field admins = FieldFinder.find("admins", true);
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ1, admins));
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ2, admins));
  }

  /**
   * every subject removed and kept is in scope: allowed, the omitted one is removed
   */
  public void testReplaceAllAllInScopeAllowed() {
    ObjectNode result = replaceAll(oauthUser(Arrays.asList(key(SubjectTestHelper.SUBJ0_ID),
        key(SubjectTestHelper.SUBJ1_ID), key(SubjectTestHelper.SUBJ2_ID))), null,
        SubjectTestHelper.SUBJ0_ID, SubjectTestHelper.SUBJ1_ID);
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(this.group.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * no subject restriction (only the group dimension is scoped): behaves as before
   */
  public void testReplaceAllSubjectDimensionOpenAllowed() {
    ObjectNode result = replaceAll(oauthUser(null), null, SubjectTestHelper.SUBJ0_ID);
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.group.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(this.group.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * a submitted subject outside the scope is still refused
   */
  public void testSubmittedOutOfScopeSubjectDenied() {
    ObjectNode result = replaceAll(oauthUser(Arrays.asList(key(SubjectTestHelper.SUBJ0_ID))), null,
        SubjectTestHelper.SUBJ0_ID, SubjectTestHelper.SUBJ3_ID);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.group.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(this.group.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * a caller with no OAuth scope (web service credentials) replaces as before
   */
  public void testNonOAuthReplaceAllAllowed() {
    ObjectNode result = replaceAll(new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0), null, SubjectTestHelper.SUBJ0_ID);
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.group.hasMember(SubjectTestHelper.SUBJ1));
  }
}

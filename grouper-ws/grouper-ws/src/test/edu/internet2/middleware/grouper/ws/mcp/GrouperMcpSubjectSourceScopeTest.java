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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSubjectScope;
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
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security issue 42 (mcpOauthSubjectSourceScope): the OAuth read-write subject scope is a list
 * of sourceId||subjectId keys, and every MCP subject gate compares the key of the subject the
 * operation really acts on.  A bare subject id (as tokens issued before the fix carry) matches
 * nothing, so it cannot act as a wildcard across sources.
 *
 * <p>Two subjects with the same subject id in different sources: a group subject (source g:gsa,
 * id = the group uuid) and the same id in the jdbc source (the consent key only, no jdbc subject
 * needs to exist).  Before the fix a bare consent value matches the g:gsa subject (the add is
 * allowed), a source-qualified consent value matches nothing, and GrouperOAuthSubjectScope does
 * not exist, so this does not compile.</p>
 */
public class GrouperMcpSubjectSourceScopeTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** the group the subjects are added to */
  private static final String TARGET_GROUP = "test:subjSrc:target";

  /** */
  private Group targetGroup;

  /** a group used as a subject, source g:gsa */
  private Group subjectGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpSubjectSourceScopeTest("testLegacyBareConsentDoesNotMatchAnySource"));
  }

  /**
   * @param name
   */
  public GrouperMcpSubjectSourceScopeTest(String name) {
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
    this.targetGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(TARGET_GROUP).assignCreateParentStemsIfNotExist(true).save();
    this.subjectGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:subjSrc:groupSubject").assignCreateParentStemsIfNotExist(true).save();

    // SUBJ0 may natively change the target, and see the group it adds as a subject
    this.targetGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.subjectGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    this.subjectGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
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
   * SUBJ0 as an OAuth caller, read-write restricted to the target group and the given subjects
   * @param subjects the grouper_readwrite_subjects claim
   * @return the auth user
   */
  private static GrouperMcpAuthUser oauthUser(List<String> subjects) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteGroups(Arrays.asList(TARGET_GROUP));
    authUser.setConsentReadwriteSubjects(subjects);
    return authUser;
  }

  /**
   * group_add_member of one subject given with its source
   * @param authUser caller
   * @param subjectId value
   * @param subjectIdType type or null
   * @param sourceId source or null
   * @return result
   */
  private static ObjectNode addMember(GrouperMcpAuthUser authUser, String subjectId, String subjectIdType,
      String sourceId) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", TARGET_GROUP);
    ArrayNode subjects = arguments.putArray("subjects");
    ObjectNode subjectNode = subjects.addObject();
    subjectNode.put("subjectIdOrIdentifier", subjectId);
    if (subjectIdType != null) {
      subjectNode.put("subjectIdType", subjectIdType);
    }
    if (sourceId != null) {
      subjectNode.put("sourceId", sourceId);
    }
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpAddMember.execute(arguments, authUser);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * key helpers: bare values are not keys, a picked value is confirmed in its source, a typed
   * value must resolve to one subject
   */
  public void testSubjectScopeKeys() {
    Subject groupSubject = this.subjectGroup.toSubject();
    String groupKey = groupSubject.getSourceId() + "||" + groupSubject.getId();

    assertFalse(GrouperOAuthSubjectScope.isSubjectKey(SubjectTestHelper.SUBJ0_ID));
    assertTrue(GrouperOAuthSubjectScope.isSubjectKey("jdbc||" + SubjectTestHelper.SUBJ0_ID));
    assertEquals(groupKey, GrouperOAuthSubjectScope.subjectKey(groupSubject));

    assertEquals("jdbc||" + SubjectTestHelper.SUBJ0_ID,
        GrouperOAuthSubjectScope.canonicalizeConsentValue("jdbc||" + SubjectTestHelper.SUBJ0_ID));
    assertEquals("jdbc||" + SubjectTestHelper.SUBJ0_ID,
        GrouperOAuthSubjectScope.canonicalizeConsentValue(SubjectTestHelper.SUBJ0_IDENTIFIER));
    assertEquals(groupKey, GrouperOAuthSubjectScope.canonicalizeConsentValue(groupKey));
    // a pick naming the wrong source does not resolve
    assertNull(GrouperOAuthSubjectScope.canonicalizeConsentValue("jdbc||" + groupSubject.getId()));
    assertNull(GrouperOAuthSubjectScope.canonicalizeConsentValue("noSuchSubjectAnywhere"));

    // a bare value in the scope matches nothing; the key matches only its own source
    assertFalse(GrouperOAuthSubjectScope.isSubjectInScope(Arrays.asList(groupSubject.getId()), groupSubject));
    assertTrue(GrouperOAuthSubjectScope.isSubjectInScope(Arrays.asList(groupKey), groupSubject));
    assertFalse(GrouperOAuthSubjectScope.isSubjectInScope(Arrays.asList("jdbc||" + groupSubject.getId()), groupSubject));
  }

  /**
   * a token with a bare subject id (issued before the fix) does not let the client act on the
   * subject with that id in another source.  Before the fix this add succeeds
   */
  public void testLegacyBareConsentDoesNotMatchAnySource() {
    String groupSubjectId = this.subjectGroup.getUuid();
    ObjectNode result = addMember(oauthUser(Arrays.asList(groupSubjectId)),
        groupSubjectId, "subjectId", "g:gsa");
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.targetGroup.hasMember(this.subjectGroup.toSubject()));
  }

  /**
   * consent for the subject with this id in jdbc does not cover the g:gsa subject with the same id
   */
  public void testConsentForOtherSourceDenied() {
    String groupSubjectId = this.subjectGroup.getUuid();
    ObjectNode result = addMember(oauthUser(Arrays.asList("jdbc||" + groupSubjectId)),
        groupSubjectId, "subjectId", "g:gsa");
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.targetGroup.hasMember(this.subjectGroup.toSubject()));
  }

  /**
   * consent for the g:gsa subject allows it.  Before the fix the key never matched
   */
  public void testSourceQualifiedConsentAllowed() {
    String groupSubjectId = this.subjectGroup.getUuid();
    ObjectNode result = addMember(oauthUser(Arrays.asList("g:gsa||" + groupSubjectId)),
        groupSubjectId, "subjectId", "g:gsa");
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertTrue(this.targetGroup.hasMember(this.subjectGroup.toSubject()));
  }

  /**
   * an identifier is resolved first and the resolved subject is compared, so the consent key
   * covers the subject by id or identifier, and another subject is still denied
   */
  public void testIdentifierResolvedThenCompared() {
    GrouperMcpAuthUser authUser = oauthUser(Arrays.asList("jdbc||" + SubjectTestHelper.SUBJ0_ID));

    ObjectNode result = addMember(authUser, SubjectTestHelper.SUBJ0_IDENTIFIER, null, null);
    assertFalse(result.toString(), result.get("isError").asBoolean());

    result = addMember(authUser, SubjectTestHelper.SUBJ1_ID, null, "jdbc");
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(this.targetGroup.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * the same rule on group_delete_member and assign_privileges
   */
  public void testDeleteMemberAndPrivilegeUseSourceAwareGate() {
    // root makes the g:gsa subject a member, then a bare-id token tries to remove it
    this.targetGroup.addMember(this.subjectGroup.toSubject(), false);
    String groupSubjectId = this.subjectGroup.getUuid();
    GrouperMcpAuthUser legacyUser = oauthUser(Arrays.asList(groupSubjectId));

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", TARGET_GROUP);
      ObjectNode subjectNode = arguments.putArray("subjects").addObject();
      subjectNode.put("subjectIdOrIdentifier", groupSubjectId);
      subjectNode.put("subjectIdType", "subjectId");
      subjectNode.put("sourceId", "g:gsa");
      ObjectNode result = GrouperMcpDeleteMember.execute(arguments, legacyUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());

      arguments = objectMapper.createObjectNode();
      arguments.put("subjectIdOrIdentifier", groupSubjectId);
      arguments.put("subjectIdType", "subjectId");
      arguments.put("sourceId", "g:gsa");
      arguments.put("groupName", TARGET_GROUP);
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);
      result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, legacyUser);
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(this.targetGroup.hasMember(this.subjectGroup.toSubject()));
  }

  /**
   * the member and subject overloads compare source and id
   */
  public void testAuthUserMemberOverload() {
    Subject groupSubject = this.subjectGroup.toSubject();
    GrouperMcpAuthUser authUser = oauthUser(Arrays.asList("g:gsa||" + groupSubject.getId()));
    assertTrue(authUser.isSubjectInReadwriteScope(groupSubject));
    assertTrue(authUser.isSubjectInReadwriteScope(
        MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), groupSubject, true)));
    assertFalse(authUser.isSubjectInReadwriteScope(SubjectTestHelper.SUBJ0));
    // the string form resolves first; a bare uuid only exists in g:gsa, so it resolves to the group
    assertTrue(authUser.isSubjectInReadwriteScope(groupSubject.getId()));
    assertFalse(authUser.isSubjectInReadwriteScope("noSuchSubjectAnywhere"));

    // no subject list and another dimension set: subjects are unscoped, so open
    GrouperMcpAuthUser openUser = oauthUser(null);
    assertTrue(openUser.isSubjectInReadwriteScope(SubjectTestHelper.SUBJ1));
  }
}

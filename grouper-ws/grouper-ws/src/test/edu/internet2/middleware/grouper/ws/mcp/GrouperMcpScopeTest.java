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
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
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
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpAuthUser readwrite scope restriction methods.
 * tests the consentReadwriteScopeRestricted boolean and the
 * isGroupInReadwriteScope, isStemInReadwriteScope, isSubjectInReadwriteScope methods.
 * also includes integration tests for each readwrite tool class verifying
 * that scope restrictions are enforced in execute().
 *
 * @author mchyzer
 */
public class GrouperMcpScopeTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpScopeTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpScopeTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpScopeTest("testGroupInScope_unrestricted"));
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
   * helper to create an auth user with no subject (scope methods don't need it)
   */
  private GrouperMcpAuthUser createAuthUser(boolean scopeRestricted,
      List<String> folders, List<String> groups, List<String> subjects) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(null);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(scopeRestricted);
    authUser.setConsentReadwriteFolders(folders);
    authUser.setConsentReadwriteGroups(groups);
    authUser.setConsentReadwriteSubjects(subjects);
    return authUser;
  }

  /**
   * build a GrouperMcpAuthUser for SUBJ0 with OAuth scope restrictions.
   * @param scopeRestricted whether scope restrictions are active
   * @param folders consented readwrite folder paths
   * @param groups consented readwrite group paths
   * @param subjects consented readwrite subject IDs
   * @return the auth user
   */
  private GrouperMcpAuthUser buildOAuthAuthUser(boolean scopeRestricted,
      List<String> folders, List<String> groups, List<String> subjects) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(scopeRestricted);
    authUser.setConsentReadwriteFolders(folders);
    authUser.setConsentReadwriteGroups(groups);
    authUser.setConsentReadwriteSubjects(subjects);
    return authUser;
  }

  // ========================================================================
  // isGroupInReadwriteScope tests (pure method tests)
  // ========================================================================

  /**
   * when consentReadwriteScopeRestricted is false and lists are empty,
   * all groups should be in scope (backward-compatible behavior)
   */
  public void testGroupInScope_unrestricted() {
    GrouperMcpAuthUser authUser = createAuthUser(false, null, null, null);
    assertTrue("null lists + unrestricted = all in scope",
        authUser.isGroupInReadwriteScope("any:group:name"));

    authUser = createAuthUser(false, new ArrayList<String>(), new ArrayList<String>(), null);
    assertTrue("empty lists + unrestricted = all in scope",
        authUser.isGroupInReadwriteScope("any:group:name"));
  }

  /**
   * when consentReadwriteScopeRestricted is true and both folder/group lists are empty,
   * no groups should be in scope
   */
  public void testGroupInScope_restricted_emptyLists() {
    GrouperMcpAuthUser authUser = createAuthUser(true, null, null, null);
    assertFalse("null lists + restricted = nothing in scope",
        authUser.isGroupInReadwriteScope("any:group:name"));

    authUser = createAuthUser(true, new ArrayList<String>(), new ArrayList<String>(), null);
    assertFalse("empty lists + restricted = nothing in scope",
        authUser.isGroupInReadwriteScope("any:group:name"));
  }

  /**
   * when consentReadwriteScopeRestricted is true with folder restrictions,
   * only groups under consented folders are in scope
   */
  public void testGroupInScope_restricted_withFolders() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"), null, null);

    assertTrue("group under consented folder is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering"));
    assertTrue("group deeply under consented folder is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering:team1"));
    assertFalse("group outside consented folder is not in scope",
        authUser.isGroupInReadwriteScope("school:clubs:chess"));
    assertFalse("group with matching prefix but not under folder is not in scope",
        authUser.isGroupInReadwriteScope("school:departmentsExtra:group1"));
  }

  /**
   * when consentReadwriteScopeRestricted is true with explicit group restrictions,
   * only matching groups are in scope
   */
  public void testGroupInScope_restricted_withGroups() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, Arrays.asList("school:departments:engineering", "school:departments:math"), null);

    assertTrue("exact match group is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering"));
    assertTrue("second exact match group is in scope",
        authUser.isGroupInReadwriteScope("school:departments:math"));
    assertFalse("non-matching group is not in scope",
        authUser.isGroupInReadwriteScope("school:departments:history"));
  }

  /**
   * when both folders and groups are specified, either match grants scope
   */
  public void testGroupInScope_restricted_withFoldersAndGroups() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"),
        Arrays.asList("school:clubs:chess"), null);

    assertTrue("group under consented folder is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering"));
    assertTrue("explicitly listed group outside folders is in scope",
        authUser.isGroupInReadwriteScope("school:clubs:chess"));
    assertFalse("group not in folder or list is not in scope",
        authUser.isGroupInReadwriteScope("school:clubs:debate"));
  }

  // ========================================================================
  // isStemInReadwriteScope tests (pure method tests)
  // ========================================================================

  /**
   * when consentReadwriteScopeRestricted is true and folders list is empty,
   * no stems should be in scope
   */
  public void testStemInScope_restricted_emptyFolders() {
    GrouperMcpAuthUser authUser = createAuthUser(true, null, null, null);
    assertFalse("null folders + restricted = nothing in scope",
        authUser.isStemInReadwriteScope("any:stem"));

    authUser = createAuthUser(true, new ArrayList<String>(), null, null);
    assertFalse("empty folders + restricted = nothing in scope",
        authUser.isStemInReadwriteScope("any:stem"));
  }

  /**
   * when consentReadwriteScopeRestricted is false and folders list is empty,
   * all stems should be in scope
   */
  public void testStemInScope_unrestricted() {
    GrouperMcpAuthUser authUser = createAuthUser(false, null, null, null);
    assertTrue("null folders + unrestricted = all in scope",
        authUser.isStemInReadwriteScope("any:stem"));
  }

  /**
   * when consentReadwriteScopeRestricted is true with folder restrictions,
   * only matching/child stems are in scope
   */
  public void testStemInScope_restricted_withFolders() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"), null, null);

    assertTrue("exact match stem is in scope",
        authUser.isStemInReadwriteScope("school:departments"));
    assertTrue("child stem under consented folder is in scope",
        authUser.isStemInReadwriteScope("school:departments:engineering"));
    assertFalse("stem outside consented folder is not in scope",
        authUser.isStemInReadwriteScope("school:clubs"));
    assertFalse("parent stem is not in scope",
        authUser.isStemInReadwriteScope("school"));
  }

  // ========================================================================
  // isSubjectInReadwriteScope tests (pure method tests)
  // ========================================================================

  /**
   * when consentReadwriteScopeRestricted is true and subjects list is empty,
   * no subjects should be in scope
   */
  public void testSubjectInScope_restricted_emptySubjects() {
    GrouperMcpAuthUser authUser = createAuthUser(true, null, null, null);
    assertFalse("null subjects + restricted = nothing in scope",
        authUser.isSubjectInReadwriteScope("anySubject"));

    authUser = createAuthUser(true, null, null, new ArrayList<String>());
    assertFalse("empty subjects + restricted = nothing in scope",
        authUser.isSubjectInReadwriteScope("anySubject"));
  }

  /**
   * when consentReadwriteScopeRestricted is false and subjects list is empty,
   * all subjects should be in scope
   */
  public void testSubjectInScope_unrestricted() {
    GrouperMcpAuthUser authUser = createAuthUser(false, null, null, null);
    assertTrue("null subjects + unrestricted = all in scope",
        authUser.isSubjectInReadwriteScope("anySubject"));
  }

  /**
   * when consentReadwriteScopeRestricted is true with subject restrictions,
   * only matching subjects are in scope
   */
  public void testSubjectInScope_restricted_withSubjects() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("jsmith", "jdoe"));

    assertTrue("matching subject is in scope",
        authUser.isSubjectInReadwriteScope("jsmith"));
    assertTrue("second matching subject is in scope",
        authUser.isSubjectInReadwriteScope("jdoe"));
    assertFalse("non-matching subject is not in scope",
        authUser.isSubjectInReadwriteScope("bwilson"));
  }

  // ========================================================================
  // mixed scenarios (pure method tests)
  // ========================================================================

  /**
   * when scope restricted with only subjects, groups and stems should be allowed
   * (the group/folder dimension is unscoped, so it is open)
   */
  public void testMixedScope_onlySubjects() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("jsmith"));

    // subject is allowed
    assertTrue("matching subject is in scope",
        authUser.isSubjectInReadwriteScope("jsmith"));

    // non-matching subject is blocked
    assertFalse("non-matching subject should be blocked",
        authUser.isSubjectInReadwriteScope("bwilson"));

    // groups and stems should be allowed because the group/folder dimension
    // is unscoped (empty) while subjects dimension has values
    assertTrue("group should be allowed when only subjects specified",
        authUser.isGroupInReadwriteScope("any:group"));
    assertTrue("stem should be allowed when only subjects specified",
        authUser.isStemInReadwriteScope("any:stem"));
  }

  /**
   * when scope restricted with only folders, subjects should be allowed
   * (the subject dimension is unscoped, so it is open)
   */
  public void testMixedScope_onlyFolders() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"), null, null);

    // group under folder is allowed
    assertTrue("group under consented folder is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering"));

    // group outside folder is blocked
    assertFalse("group outside consented folder should be blocked",
        authUser.isGroupInReadwriteScope("school:clubs:chess"));

    // subjects should be allowed because subjects list is empty
    // and at least one other dimension (folders) has values
    assertTrue("subject should be allowed when only folders specified",
        authUser.isSubjectInReadwriteScope("jsmith"));
  }

  /**
   * when scope restricted with nothing scoped (all lists empty),
   * everything should be blocked
   */
  public void testMixedScope_nothingScoped() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, null);

    assertFalse("group should be blocked when nothing scoped",
        authUser.isGroupInReadwriteScope("any:group"));
    assertFalse("stem should be blocked when nothing scoped",
        authUser.isStemInReadwriteScope("any:stem"));
    assertFalse("subject should be blocked when nothing scoped",
        authUser.isSubjectInReadwriteScope("anySubject"));

    // also with empty lists instead of null
    authUser = createAuthUser(true,
        new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());

    assertFalse("group should be blocked when nothing scoped (empty lists)",
        authUser.isGroupInReadwriteScope("any:group"));
    assertFalse("stem should be blocked when nothing scoped (empty lists)",
        authUser.isStemInReadwriteScope("any:stem"));
    assertFalse("subject should be blocked when nothing scoped (empty lists)",
        authUser.isSubjectInReadwriteScope("anySubject"));
  }

  // ========================================================================
  // Tool integration tests: verify scope enforcement in each readwrite tool
  // ========================================================================

  // ---- GrouperMcpAddMember (group_add_member) ----

  /**
   * group_add_member with a group outside the consented scope should be denied
   */
  public void testAddMember_invalidGroupScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "other:folder:someGroup");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for group",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with a subject outside the consented scope should be denied
   */
  public void testAddMember_invalidSubjectScope() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberSubj")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList("allowedSubjectOnly"));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberSubj");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertTrue("Expected scope error for subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for subject",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with group and subject both in scope should succeed
   */
  public void testAddMember_validScope() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberValid");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (scope check passed), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with group in scope but subjects unscoped (empty list)
   * should succeed - the subject dimension is open when not specified
   */
  public void testAddMember_validScope_subjectsUnscoped() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberSubjUnscoped")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // folder "test" is scoped, but subjects list is null (unscoped)
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberSubjUnscoped");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (subjects unscoped), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with subjects scoped but groups/folders unscoped (empty)
   * should succeed for the group scope check - the group dimension is open
   */
  public void testAddMember_validScope_groupsUnscoped() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberGrpUnscoped")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // no folder/group scoping, but subjects list has SUBJ0
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          null, null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberGrpUnscoped");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (groups unscoped), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---- GrouperMcpDeleteMember (group_remove_member) ----

  /**
   * group_remove_member with a group outside the consented scope should be denied
   */
  public void testDeleteMember_invalidGroupScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "other:folder:someGroup");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpDeleteMember.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for group",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_remove_member with group and subject both in scope should succeed
   */
  public void testDeleteMember_validScope() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeDeleteMemberValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.addMember(SubjectTestHelper.SUBJ0);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeDeleteMemberValid");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpDeleteMember.execute(arguments, authUser);

      assertFalse("Expected success (scope check passed), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---- GrouperMcpGroupSave (group_save) ----

  /**
   * group_save with a group outside the consented scope should be denied
   */
  public void testGroupSave_invalidGroupScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "other:folder:newGroup");
      arguments.put("action", "createGroup");

      ObjectNode result = GrouperMcpGroupSave.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for group",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_save with a group in the consented scope should succeed
   */
  public void testGroupSave_validScope() {

    // create the test stem and grant stem create privilege so SUBJ0 can create groups in test:
    edu.internet2.middleware.grouper.Stem testStem =
        new edu.internet2.middleware.grouper.StemSave(GrouperSession.staticGrouperSession())
            .assignStemNameToEdit("test")
            .assignName("test")
            .assignCreateParentStemsIfNotExist(true)
            .save();
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeGroupSaveValid");
      arguments.put("action", "createGroup");

      ObjectNode result = GrouperMcpGroupSave.execute(arguments, authUser);

      assertFalse("Expected success (scope check passed), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---- GrouperMcpAssignGrouperPrivilegesLite (privilege_assign) ----

  /**
   * privilege_assign with a group outside the consented scope should be denied
   */
  public void testPrivilegeAssign_invalidGroupScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "other:folder:someGroup");
      arguments.put("subjectId", SubjectTestHelper.SUBJ0.getId());
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);

      ObjectNode result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for group",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * privilege_assign with a subject outside the consented scope should be denied
   */
  public void testPrivilegeAssign_invalidSubjectScope() {

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopePrivSubj")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList("allowedSubjectOnly"));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopePrivSubj");
      arguments.put("subjectId", SubjectTestHelper.SUBJ0.getId());
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);

      ObjectNode result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);

      assertTrue("Expected scope error for subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for subject",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * privilege_assign with group and subject both in scope should succeed
   */
  public void testPrivilegeAssign_validScope() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopePrivValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopePrivValid");
      arguments.put("subjectId", SubjectTestHelper.SUBJ0.getId());
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);

      ObjectNode result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);

      assertFalse("Expected success (scope check passed), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---- GrouperMcpAssignAttributes (attribute_assignment_save) ----

  /**
   * attribute_assignment_save with an owner group outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidGroupScope() {

    // create attribute def and attribute def name so the arguments are valid
    AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeTestAttrDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDef)
        .assignName("test:scopeTestAttrDefName")
        .save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:scopeTestAttrDefName");
      arguments.put("ownerGroupName", "other:folder:someGroup");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for owner group",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * attribute_assignment_save with an owner stem outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidStemScope() {

    // create attribute def and attribute def name so the arguments are valid
    AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeTestAttrDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToStem(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDef)
        .assignName("test:scopeTestAttrDefName")
        .save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("allowed:folder"), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "stem");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:scopeTestAttrDefName");
      arguments.put("ownerStemName", "other:folder");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      assertTrue("Expected scope error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for owner stem",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * attribute_assignment_save with an owner subject outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidSubjectScope() {

    // create attribute def and attribute def name so the arguments are valid
    AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeTestAttrDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToMember(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDef)
        .assignName("test:scopeTestAttrDefName")
        .save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          null, null, Arrays.asList("allowedSubjectOnly"));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "member");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:scopeTestAttrDefName");
      arguments.put("ownerSubjectId", SubjectTestHelper.SUBJ0.getId());

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      assertTrue("Expected scope error for owner subject", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Expected scope denial for owner subject",
          text.contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * attribute_assignment_save with an owner group in the consented scope should succeed
   */
  public void testAssignAttributes_validScope() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAttrValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create attribute def and attribute def name so the WS can resolve them
    AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeTestAttrDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDef)
        .assignName("test:scopeTestAttrDefName")
        .save();

    // grant SUBJ0 attrUpdate privilege on the attribute def so they can assign attributes
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        edu.internet2.middleware.grouper.privs.AttributeDefPrivilege.ATTR_UPDATE, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:scopeTestAttrDefName");
      arguments.put("ownerGroupName", "test:scopeAttrValid");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      assertFalse("Expected success (scope check passed), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // UUID and multi-identifier scope matching tests (pure method tests)
  // ========================================================================

  /**
   * when scope contains a group UUID, isGroupInReadwriteScope should match
   * when the UUID is passed
   */
  public void testGroupInScope_byUuid() {
    String groupUuid = "abc-123-def-456";
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, Arrays.asList(groupUuid), null);

    // should match when UUID is passed
    assertTrue("group should be in scope when UUID matches consented group",
        authUser.isGroupInReadwriteScope(null, groupUuid));
    assertTrue("group should be in scope when UUID matches consented group (with name)",
        authUser.isGroupInReadwriteScope("some:other:name", groupUuid));

    // should not match a different UUID
    assertFalse("group should not be in scope with different UUID",
        authUser.isGroupInReadwriteScope(null, "different-uuid"));
    assertFalse("group should not be in scope with only non-matching name",
        authUser.isGroupInReadwriteScope("some:group", null));
  }

  /**
   * when scope contains a group name, isGroupInReadwriteScope should match
   * by name even when a UUID is also provided
   */
  public void testGroupInScope_byNameWithUuid() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, Arrays.asList("school:departments:engineering"), null);

    // should match by name
    assertTrue("group should match by name even when UUID is also passed",
        authUser.isGroupInReadwriteScope("school:departments:engineering", "some-uuid"));
    // should not match by UUID alone
    assertFalse("group should not match by UUID when scope contains names",
        authUser.isGroupInReadwriteScope("other:group", "some-uuid"));
  }

  /**
   * when scope contains a folder path, groups under that folder should match
   * regardless of UUID
   */
  public void testGroupInScope_folderContainmentWithUuid() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"), null, null);

    assertTrue("group under folder should be in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering", "some-uuid"));
    assertFalse("group outside folder should not be in scope even with UUID",
        authUser.isGroupInReadwriteScope("school:clubs:chess", "some-uuid"));
  }

  /**
   * when scope contains a stem UUID, isStemInReadwriteScope should match
   * when the UUID is passed
   */
  public void testStemInScope_byUuid() {
    String stemUuid = "stem-uuid-789";
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList(stemUuid), null, null);

    // should match when UUID is passed
    assertTrue("stem should be in scope when UUID matches consented folder",
        authUser.isStemInReadwriteScope(null, stemUuid));
    assertTrue("stem should be in scope when UUID matches consented folder (with name)",
        authUser.isStemInReadwriteScope("some:other:stem", stemUuid));

    // should not match a different UUID
    assertFalse("stem should not be in scope with different UUID",
        authUser.isStemInReadwriteScope(null, "different-uuid"));
  }

  /**
   * isSubjectInReadwriteScope with a list of identifiers should match
   * if any identifier is in the scope list
   */
  public void testSubjectInScope_multipleIdentifiers() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("jsmith"));

    // should match when any identifier in the list matches
    assertTrue("should match when subjectIdentifier0 matches scope",
        authUser.isSubjectInReadwriteScope(
            Arrays.asList("12345", "jsmith", "john.smith@example.com")));

    // should match if the matching identifier is not the first
    assertTrue("should match when subjectId is first but identifier matches",
        authUser.isSubjectInReadwriteScope(
            Arrays.asList("other-id", "jsmith")));

    // should not match when no identifier matches
    assertFalse("should not match when no identifier matches",
        authUser.isSubjectInReadwriteScope(
            Arrays.asList("12345", "john.smith@example.com")));
  }

  /**
   * isSubjectInReadwriteScope with a list should work when scope has subjectId
   * and the list contains the subjectId among other identifiers
   */
  public void testSubjectInScope_multipleIdentifiers_matchById() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("12345"));

    // should match when subjectId matches
    assertTrue("should match when subjectId in list matches scope",
        authUser.isSubjectInReadwriteScope(
            Arrays.asList("12345", "jsmith", "john.smith@example.com")));

    // should not match when subjectId doesn't match
    assertFalse("should not match when subjectId doesn't match",
        authUser.isSubjectInReadwriteScope(
            Arrays.asList("99999", "jsmith")));
  }

  /**
   * isSubjectInReadwriteScope with empty or null list should respect
   * the unrestricted/restricted flag
   */
  public void testSubjectInScope_multipleIdentifiers_emptyList() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("jsmith"));

    // empty list should not match
    assertFalse("empty identifier list should not match any scope",
        authUser.isSubjectInReadwriteScope(new ArrayList<String>()));
  }

  // ========================================================================
  // Integration tests: multi-identifier scope matching in tools
  // ========================================================================

  /**
   * group_add_member with a group UUID in scope should succeed when the
   * tool passes a group name that resolves to that UUID
   */
  public void testAddMember_validScope_byGroupUuid() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberUuid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for UUID scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // scope contains the group UUID, not the name
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          null, Arrays.asList(group.getUuid()),
          Arrays.asList(SubjectTestHelper.SUBJ0_ID));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberUuid");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0_ID);

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (group UUID in scope), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with subject identifier in scope should succeed
   * when the tool passes subjectId (which resolves to that identifier)
   */
  public void testAddMember_validScope_bySubjectIdentifier() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberSubjIdent")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for subject identifier scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // ensure SUBJ0 has a member record with identifiers
    MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectTestHelper.SUBJ0, true);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // scope contains the subject identifier (e.g. "id.test.subject.0"),
      // not the subject ID ("test.subject.0")
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0_IDENTIFIER));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberSubjIdent");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      // API sends subjectId, but scope has the identifier
      subjectNode.put("subjectId", SubjectTestHelper.SUBJ0_ID);

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (subject identifier in scope matches subjectId), got: "
          + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * group_add_member with subject ID in scope should succeed
   * when the tool passes subjectIdentifier (which resolves to that ID)
   */
  public void testAddMember_validScope_bySubjectIdWhenIdentifierSent() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberSubjIdReverse")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for reverse subject scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // ensure SUBJ0 has a member record with identifiers
    MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectTestHelper.SUBJ0, true);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // scope contains the subject ID ("test.subject.0")
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0_ID));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopeAddMemberSubjIdReverse");
      ArrayNode subjects = arguments.putArray("subjects");
      ObjectNode subjectNode = subjects.addObject();
      // API sends subjectIdentifier, but scope has the subjectId
      subjectNode.put("subjectIdentifier", SubjectTestHelper.SUBJ0_IDENTIFIER);

      ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

      assertFalse("Expected success (subject ID in scope matches identifier), got: "
          + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * folder_delete with stem UUID in scope should succeed when the
   * tool passes a stem name that resolves to that UUID
   */
  public void testFolderDelete_validScope_byStemUuid() {

    Stem stem = new StemSave(GrouperSession.staticGrouperSession())
        .assignStemNameToEdit("test:scopeFolderDeleteUuid")
        .assignName("test:scopeFolderDeleteUuid")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // scope contains the stem UUID, not the name
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList(stem.getUuid()), null, null);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("stemName", "test:scopeFolderDeleteUuid");

      ObjectNode result = GrouperMcpFolderDelete.execute(arguments, authUser);

      assertFalse("Expected success (stem UUID in scope), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * privilege_assign with subject identifier in scope should succeed
   * when the tool passes subjectId
   */
  public void testPrivilegeAssign_validScope_bySubjectIdentifier() {

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopePrivSubjIdent")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for priv subject identifier scope test").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // ensure SUBJ0 has a member record with identifiers
    MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectTestHelper.SUBJ0, true);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // scope contains subject identifier, not subject ID
      GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
          Arrays.asList("test"), null,
          Arrays.asList(SubjectTestHelper.SUBJ0_IDENTIFIER));

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("groupName", "test:scopePrivSubjIdent");
      // API sends subjectId, but scope has the identifier
      arguments.put("subjectId", SubjectTestHelper.SUBJ0_ID);
      arguments.put("privilegeType", "access");
      arguments.put("privilegeName", "read");
      arguments.put("allowed", true);

      ObjectNode result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);

      assertFalse("Expected success (subject identifier in scope), got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
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

    GrouperServiceUtils.testSession = GrouperSession.staticGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperServiceUtils.testSession = null;
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
   * build a GrouperMcpAuthUser for the root session with OAuth scope restrictions.
   * uses a real subject (GrouperSystem) so that tool execute() calls that
   * pass the scope check can proceed to WS logic.
   * @param scopeRestricted whether scope restrictions are active
   * @param folders consented readwrite folder paths
   * @param groups consented readwrite group paths
   * @param subjects consented readwrite subject IDs
   * @return the auth user
   */
  private GrouperMcpAuthUser buildOAuthAuthUser(boolean scopeRestricted,
      List<String> folders, List<String> groups, List<String> subjects) {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(
        SubjectFinder.findRootSubject());
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
   * when scope restricted with only subjects, groups and stems should be blocked
   */
  public void testMixedScope_onlySubjects() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        null, null, Arrays.asList("jsmith"));

    // subject is allowed
    assertTrue("matching subject is in scope",
        authUser.isSubjectInReadwriteScope("jsmith"));

    // groups and stems should be blocked because folders/groups lists are empty
    // and consentReadwriteScopeRestricted is true
    assertFalse("group should be blocked when only subjects specified",
        authUser.isGroupInReadwriteScope("any:group"));
    assertFalse("stem should be blocked when only subjects specified",
        authUser.isStemInReadwriteScope("any:stem"));
  }

  /**
   * when scope restricted with only folders, subjects should be blocked
   */
  public void testMixedScope_onlyFolders() {
    GrouperMcpAuthUser authUser = createAuthUser(true,
        Arrays.asList("school:departments"), null, null);

    // group under folder is allowed
    assertTrue("group under consented folder is in scope",
        authUser.isGroupInReadwriteScope("school:departments:engineering"));

    // subjects should be blocked because subjects list is empty
    // and consentReadwriteScopeRestricted is true
    assertFalse("subject should be blocked when only folders specified",
        authUser.isSubjectInReadwriteScope("jsmith"));
  }

  // ========================================================================
  // Tool integration tests: verify scope enforcement in each readwrite tool
  // ========================================================================

  // ---- GrouperMcpAddMember (group_add_member) ----

  /**
   * group_add_member with a group outside the consented scope should be denied
   */
  public void testAddMember_invalidGroupScope() {

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
  }

  /**
   * group_add_member with a subject outside the consented scope should be denied
   */
  public void testAddMember_invalidSubjectScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null,
        Arrays.asList("allowedSubjectOnly"));

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberSubj")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

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
  }

  /**
   * group_add_member with group and subject both in scope should succeed
   */
  public void testAddMember_validScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null,
        Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAddMemberValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:scopeAddMemberValid");
    ArrayNode subjects = arguments.putArray("subjects");
    ObjectNode subjectNode = subjects.addObject();
    subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

    ObjectNode result = GrouperMcpAddMember.execute(arguments, authUser);

    assertFalse("Expected success (scope check passed), got: " + result.toString(),
        result.get("isError").asBoolean());
  }

  // ---- GrouperMcpDeleteMember (group_remove_member) ----

  /**
   * group_remove_member with a group outside the consented scope should be denied
   */
  public void testDeleteMember_invalidGroupScope() {

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
  }

  /**
   * group_remove_member with group and subject both in scope should succeed
   */
  public void testDeleteMember_validScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null,
        Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeDeleteMemberValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    group.addMember(SubjectTestHelper.SUBJ0);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:scopeDeleteMemberValid");
    ArrayNode subjects = arguments.putArray("subjects");
    ObjectNode subjectNode = subjects.addObject();
    subjectNode.put("subjectId", SubjectTestHelper.SUBJ0.getId());

    ObjectNode result = GrouperMcpDeleteMember.execute(arguments, authUser);

    assertFalse("Expected success (scope check passed), got: " + result.toString(),
        result.get("isError").asBoolean());
  }

  // ---- GrouperMcpGroupSave (group_save) ----

  /**
   * group_save with a group outside the consented scope should be denied
   */
  public void testGroupSave_invalidGroupScope() {

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
  }

  /**
   * group_save with a group in the consented scope should succeed
   */
  public void testGroupSave_validScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null, null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:scopeGroupSaveValid");
    arguments.put("action", "createGroup");

    ObjectNode result = GrouperMcpGroupSave.execute(arguments, authUser);

    assertFalse("Expected success (scope check passed), got: " + result.toString(),
        result.get("isError").asBoolean());
  }

  // ---- GrouperMcpAssignGrouperPrivilegesLite (privilege_assign) ----

  /**
   * privilege_assign with a group outside the consented scope should be denied
   */
  public void testPrivilegeAssign_invalidGroupScope() {

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
  }

  /**
   * privilege_assign with a subject outside the consented scope should be denied
   */
  public void testPrivilegeAssign_invalidSubjectScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null,
        Arrays.asList("allowedSubjectOnly"));

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopePrivSubj")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

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
  }

  /**
   * privilege_assign with group and subject both in scope should succeed
   */
  public void testPrivilegeAssign_validScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null,
        Arrays.asList(SubjectTestHelper.SUBJ0.getId()));

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopePrivValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:scopePrivValid");
    arguments.put("subjectId", SubjectTestHelper.SUBJ0.getId());
    arguments.put("privilegeType", "access");
    arguments.put("privilegeName", "read");
    arguments.put("allowed", true);

    ObjectNode result = GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);

    assertFalse("Expected success (scope check passed), got: " + result.toString(),
        result.get("isError").asBoolean());
  }

  // ---- GrouperMcpAssignAttributes (attribute_assignment_save) ----

  /**
   * attribute_assignment_save with an owner group outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidGroupScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("allowed:folder"), null, null);

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
  }

  /**
   * attribute_assignment_save with an owner stem outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidStemScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("allowed:folder"), null, null);

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
  }

  /**
   * attribute_assignment_save with an owner subject outside the consented scope should be denied
   */
  public void testAssignAttributes_invalidSubjectScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        null, null, Arrays.asList("allowedSubjectOnly"));

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
  }

  /**
   * attribute_assignment_save with an owner group in the consented scope should succeed
   */
  public void testAssignAttributes_validScope() {

    GrouperMcpAuthUser authUser = buildOAuthAuthUser(true,
        Arrays.asList("test"), null, null);

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeAttrValid")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope test").save();

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

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("attributeAssignType", "group");
    arguments.put("attributeAssignOperation", "assign_attr");
    arguments.put("attributeDefNameName", "test:scopeTestAttrDefName");
    arguments.put("ownerGroupName", "test:scopeAttrValid");

    ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

    assertFalse("Expected success (scope check passed), got: " + result.toString(),
        result.get("isError").asBoolean());
  }
}

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * Security (GRP-7578): attribute_assignment_save must gate
 * the real owner of the assignment it changes, however the assignment is addressed (by owner
 * name, by attributeAssignId, or through ownerAttributeAssignId), fail closed when the owner
 * cannot be resolved, and not name an owner the caller cannot see.
 *
 * <p>Before the fix the attributeAssignId cases are allowed (the assignment is removed or its
 * value replaced), and GrouperMcpResolvedTargetGate does not exist, so this does not compile.</p>
 */
public class GrouperMcpAssignAttributesScopeGateTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** in the consent */
  private static final String ALLOWED_GROUP = "test:mcpScope:allowedGroup";

  /** not in the consent */
  private static final String OUTSIDE_GROUP = "test:mcpScope:outsideGroup";

  /** marker attribute (no value) on groups */
  private static final String MARKER_ATTR = "test:mcpScope:scopeTestAttr";

  /** string attribute on groups */
  private static final String VALUE_ATTR = "test:mcpScope:scopeValueAttr";

  /** string attribute on group assignments (assignment on assignment) */
  private static final String ASGN_ATTR = "test:mcpScope:scopeAsgnAttr";

  /** marker attribute on immediate memberships */
  private static final String IMM_MEM_ATTR = "test:mcpScope:scopeImmMemAttr";

  /** */
  private Group allowedGroup;

  /** */
  private Group outsideGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpAssignAttributesScopeGateTest("testRemoveByAttributeAssignIdOutOfScopeDenied"));
  }

  /**
   * @param name
   */
  public GrouperMcpAssignAttributesScopeGateTest(String name) {
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

    this.allowedGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(ALLOWED_GROUP).assignCreateParentStemsIfNotExist(true).save();
    this.outsideGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(OUTSIDE_GROUP).assignCreateParentStemsIfNotExist(true).save();

    // SUBJ0 is natively allowed on both groups: only the consent scope tells them apart
    this.allowedGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.outsideGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    AttributeDef markerDef = new AttributeDefSave(rootSession).assignName("test:mcpScope:scopeTestDef")
        .assignCreateParentStemsIfNotExist(true).assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr).save();
    new AttributeDefNameSave(rootSession, markerDef).assignName(MARKER_ATTR).save();

    AttributeDef valueDef = new AttributeDefSave(rootSession).assignName("test:mcpScope:scopeValueDef")
        .assignCreateParentStemsIfNotExist(true).assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr).assignValueType(AttributeDefValueType.string).save();
    new AttributeDefNameSave(rootSession, valueDef).assignName(VALUE_ATTR).save();

    AttributeDef asgnDef = new AttributeDefSave(rootSession).assignName("test:mcpScope:scopeAsgnDef")
        .assignCreateParentStemsIfNotExist(true).assignToGroupAssn(true)
        .assignAttributeDefType(AttributeDefType.attr).assignValueType(AttributeDefValueType.string).save();
    new AttributeDefNameSave(rootSession, asgnDef).assignName(ASGN_ATTR).save();

    AttributeDef immMemDef = new AttributeDefSave(rootSession).assignName("test:mcpScope:scopeImmMemDef")
        .assignCreateParentStemsIfNotExist(true).assignToImmMembership(true)
        .assignAttributeDefType(AttributeDefType.attr).save();
    new AttributeDefNameSave(rootSession, immMemDef).assignName(IMM_MEM_ATTR).save();

    for (AttributeDef attributeDef : new AttributeDef[] {markerDef, valueDef, asgnDef, immMemDef}) {
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    }
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
   * an OAuth caller whose read-write consent is only the allowed group
   * @param subject who
   * @return the auth user
   */
  private static GrouperMcpAuthUser oauthUser(edu.internet2.middleware.subject.Subject subject) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteGroups(Arrays.asList(ALLOWED_GROUP));
    return authUser;
  }

  /**
   * assign an attribute to a group as root
   * @param group the group
   * @param attributeDefNameName the attribute
   * @return the assignment
   */
  private static AttributeAssign assignAsRoot(Group group, String attributeDefNameName) {
    AttributeDefName attributeDefName = edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder
        .findByName(attributeDefNameName, true);
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    HibernateSession.flush();
    return attributeAssign;
  }

  /**
   * @param result tool result
   * @return its text
   */
  private static String text(ObjectNode result) {
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * @param attributeAssignId id
   * @return true if the assignment still exists (looked up as root)
   */
  private static boolean assignmentExists(String attributeAssignId) {
    // the DAO, not AttributeAssignFinder: the finder checks VIEW with the static session, and
    // callers run this after stopping their own session
    return edu.internet2.middleware.grouper.misc.GrouperDAOFactory.getFactory().getAttributeAssign()
        .findById(attributeAssignId, false) != null;
  }

  /**
   * control: remove by owner name outside the consent is denied (worked before the fix too)
   */
  public void testRemoveByOwnerNameOutOfScopeDenied() {
    AttributeAssign attributeAssign = assignAsRoot(this.outsideGroup, MARKER_ATTR);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeDefNameName", MARKER_ATTR);
      arguments.put("ownerGroupName", OUTSIDE_GROUP);

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertTrue(text(result), text(result).contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(assignmentExists(attributeAssign.getId()));
  }

  /**
   * the reproduced bug: the same assignment by attributeAssignId is denied too, and not removed
   */
  public void testRemoveByAttributeAssignIdOutOfScopeDenied() {
    AttributeAssign attributeAssign = assignAsRoot(this.outsideGroup, MARKER_ATTR);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeAssignId", attributeAssign.getId());

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      // SUBJ0 can see the group, so the denial may name it
      assertTrue(text(result), text(result).contains("outside your consented read-write scope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue("assignment must not be removed", assignmentExists(attributeAssign.getId()));
  }

  /**
   * replacing values by attributeAssignId outside the consent is denied, values unchanged
   */
  public void testAssignValuesByAttributeAssignIdOutOfScopeDenied() {
    this.outsideGroup.getAttributeValueDelegate().assignValue(VALUE_ATTR, "original");
    HibernateSession.flush();
    AttributeAssign attributeAssign = this.outsideGroup.getAttributeDelegate()
        .retrieveAssignment(null, edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder
            .findByName(VALUE_ATTR, true), false, true);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeAssignId", attributeAssign.getId());
      arguments.put("valueOperation", "replace_values");
      ArrayNode values = arguments.putArray("values");
      values.add("changedByMcp");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertEquals("original", this.outsideGroup.getAttributeValueDelegate().retrieveValueString(VALUE_ATTR));
  }

  /**
   * an assignment in the consent can still be removed by its id
   */
  public void testRemoveByAttributeAssignIdInScopeAllowed() {
    AttributeAssign attributeAssign = assignAsRoot(this.allowedGroup, MARKER_ATTR);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeAssignId", attributeAssign.getId());

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertFalse(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertFalse("assignment should be removed", assignmentExists(attributeAssign.getId()));
  }

  /**
   * an assignment on a protected group (under etc), by id, is refused even for a wheel caller
   * with no OAuth scope
   */
  public void testProtectedGroupByAttributeAssignIdDenied() {
    Group etcGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("etc:mcpScopeProtectedGroup").assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign attributeAssign = assignAsRoot(etcGroup, MARKER_ATTR);

    GrouperMcpAuthUser rootUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("attributeAssignType", "group");
    arguments.put("attributeAssignOperation", "remove_attr");
    arguments.put("attributeAssignId", attributeAssign.getId());

    ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, rootUser);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertTrue(text(result), text(result).contains("protected"));
    assertTrue(assignmentExists(attributeAssign.getId()));
  }

  /**
   * an assignment on an assignment, by id, is gated on the group the base assignment is on
   */
  public void testAssignmentOnAssignmentByIdOutOfScopeDenied() {
    AttributeAssign baseAssign = assignAsRoot(this.outsideGroup, MARKER_ATTR);
    AttributeDefName asgnAttr = edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder
        .findByName(ASGN_ATTR, true);
    AttributeAssign asgnOnAsgn = baseAssign.getAttributeDelegate().assignAttribute(asgnAttr).getAttributeAssign();
    HibernateSession.flush();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group_asgn");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeAssignId", asgnOnAsgn.getId());

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(assignmentExists(asgnOnAsgn.getId()));
  }

  /**
   * an id that matches nothing fails, without owner details or exception text
   */
  public void testUnknownAttributeAssignIdDenied() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeAssignId", "abcdef0123456789abcdef0123456789");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ0));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertEquals(GrouperMcpResolvedTargetGate.GENERIC_DENIED_MESSAGE, text(result));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a caller who cannot see the out of scope group is not told its name, by attributeAssignId
   * or by ownerAttributeAssignId.  SUBJ1 has no privileges on it
   */
  public void testDenialDoesNotNameHiddenOwner() {
    AttributeAssign attributeAssign = assignAsRoot(this.outsideGroup, MARKER_ATTR);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "remove_attr");
      arguments.put("attributeAssignId", attributeAssign.getId());

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ1));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertFalse(text(result), text(result).contains("outsideGroup"));

      arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group_asgn");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", ASGN_ATTR);
      arguments.put("ownerAttributeAssignId", attributeAssign.getId());

      result = GrouperMcpAssignAttributes.execute(arguments, oauthUser(SubjectTestHelper.SUBJ1));
      assertTrue(result.toString(), result.get("isError").asBoolean());
      assertFalse(text(result), text(result).contains("outsideGroup"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(assignmentExists(attributeAssign.getId()));
  }

  /**
   * the shared gate on immediate membership owners: out of scope group denied, in scope allowed,
   * and a membership with no owner, or no membership, is denied (fail closed)
   */
  public void testGateImmediateMembershipOwner() {
    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    this.outsideGroup.addMember(SubjectTestHelper.SUBJ2, false);
    this.allowedGroup.addMember(SubjectTestHelper.SUBJ2, false);
    AttributeDefName immMemAttr = edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder
        .findByName(IMM_MEM_ATTR, true);

    Membership outsideMembership = MembershipFinder.findImmediateMembership(rootSession, this.outsideGroup,
        SubjectTestHelper.SUBJ2, Group.getDefaultList(), true);
    Membership allowedMembership = MembershipFinder.findImmediateMembership(rootSession, this.allowedGroup,
        SubjectTestHelper.SUBJ2, Group.getDefaultList(), true);
    AttributeAssign outsideAssign = outsideMembership.getAttributeDelegate().assignAttribute(immMemAttr).getAttributeAssign();
    AttributeAssign allowedAssign = allowedMembership.getAttributeDelegate().assignAttribute(immMemAttr).getAttributeAssign();
    HibernateSession.flush();

    // a membership has a member too: the gate also checks the member against the subject scope
    // (as the any_mem owner check does), so the caller here can change SUBJ2
    GrouperMcpAuthUser authUser = oauthUser(SubjectTestHelper.SUBJ0);
    authUser.setConsentReadwriteSubjects(Arrays.asList(
        edu.internet2.middleware.grouper.authentication.GrouperOAuthSubjectScope.subjectKey(SubjectTestHelper.SUBJ2)));
    assertNotNull(GrouperMcpResolvedTargetGate.checkAttributeAssign(authUser, outsideAssign.getId()));
    assertNull(GrouperMcpResolvedTargetGate.checkAttributeAssign(authUser, allowedAssign.getId()));

    // a membership with no owner ids cannot be checked: denied, not skipped
    assertEquals(GrouperMcpResolvedTargetGate.GENERIC_DENIED_MESSAGE,
        GrouperMcpResolvedTargetGate.checkMembership(authUser, new Membership()));
    assertEquals(GrouperMcpResolvedTargetGate.GENERIC_DENIED_MESSAGE,
        GrouperMcpResolvedTargetGate.checkMembership(authUser, null));
  }
}

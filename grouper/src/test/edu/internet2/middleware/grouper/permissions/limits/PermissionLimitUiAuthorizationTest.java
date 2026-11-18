/**
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
 */
package edu.internet2.middleware.grouper.permissions.limits;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueBinding;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for PermissionLimitUiAuthorization, which the UI permission limit handlers
 * (UiV2GroupPermission / UiV2SubjectPermission limitDelete, limitValueDelete, limitValueEdit,
 * limitValueEditSubmit) call before changing anything.
 *
 * limitDelete used to delete the assignment first and check privileges afterward, so a caller who
 * could only VIEW a limit removed it (and then saw an error).  It also deleted any viewable assignment,
 * not only limits.  The limit value handlers skipped the permission definition ATTR_UPDATE check.
 */
public class PermissionLimitUiAuthorizationTest extends GrouperTest {

  /**
   *
   */
  public PermissionLimitUiAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public PermissionLimitUiAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionLimitUiAuthorizationTest("testLimitAuthorization"));
  }

  /** role */
  private Group role;

  /** permission def */
  private AttributeDef permissionDef;

  /** role level permission (group owner) */
  private AttributeAssign rolePermissionAssign;

  /** limit on the role level permission (group_asgn) */
  private AttributeAssign roleLimitAssign;

  /** role-subject permission (any_mem owner) */
  private AttributeAssign subjectPermissionAssign;

  /** limit on the role-subject permission (any_mem_asgn) */
  private AttributeAssign subjectLimitAssign;

  /** disallowed (deny) permission on the role */
  private AttributeAssign denyPermissionAssign;

  /** plain group attribute assignment */
  private AttributeAssign plainGroupAttributeAssign;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  public void setUp() {
    super.setUp();

    // no default grants to GrouperAll on new objects, so the test controls the privileges
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      this.role = new GroupSave(rootSession).assignName("test:permLimit:role")
          .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
      this.role.addMember(SubjectTestHelper.SUBJ3, false);

      this.permissionDef = new AttributeDefSave(rootSession).assignName("test:permLimit:permissionDef")
          .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true).assignToEffMembership(true).save();
      AttributeDefName permissionName = new AttributeDefNameSave(rootSession, this.permissionDef).assignName("test:permLimit:action").save();
      AttributeDefName denyName = new AttributeDefNameSave(rootSession, this.permissionDef).assignName("test:permLimit:denied").save();

      this.rolePermissionAssign = this.role.getPermissionRoleDelegate()
          .assignRolePermission(permissionName, PermissionAllowed.ALLOWED).getAttributeAssign();
      this.subjectPermissionAssign = this.role.getPermissionRoleDelegate()
          .assignSubjectRolePermission(permissionName, SubjectTestHelper.SUBJ3, PermissionAllowed.ALLOWED).getAttributeAssign();
      this.denyPermissionAssign = this.role.getPermissionRoleDelegate()
          .assignRolePermission(denyName, PermissionAllowed.DISALLOWED).getAttributeAssign();

      AttributeDefName limitName = PermissionLimitUtils.limitElAttributeDefName();
      this.rolePermissionAssign.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 50000");
      this.subjectPermissionAssign.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 10");
      this.roleLimitAssign = this.rolePermissionAssign.getAttributeDelegate().retrieveAssignments(limitName).iterator().next();
      this.subjectLimitAssign = this.subjectPermissionAssign.getAttributeDelegate().retrieveAssignments(limitName).iterator().next();

      AttributeDef plainDef = new AttributeDefSave(rootSession).assignName("test:permLimit:plainDef")
          .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
          .assignValueType(AttributeDefValueType.marker).save();
      AttributeDefName plainName = new AttributeDefNameSave(rootSession, plainDef).assignName("test:permLimit:plain").save();
      this.plainGroupAttributeAssign = this.role.getAttributeDelegate().assignAttribute(plainName).getAttributeAssign();

      AttributeDef limitDef = limitName.getAttributeDef();

      // SUBJ0: everything needed to manage the limits
      grantView(SubjectTestHelper.SUBJ0, limitDef, plainDef);
      this.role.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_UPDATE, false);
      limitDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      this.permissionDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      plainDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);

      // SUBJ1: everything except ATTR_UPDATE on the permission def (the check the value handlers skipped)
      grantView(SubjectTestHelper.SUBJ1, limitDef, plainDef);
      this.role.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_UPDATE, false);
      limitDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);

      // SUBJ2: read only, the reproduction in the security report
      grantView(SubjectTestHelper.SUBJ2, limitDef, plainDef);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * enough to view the limits (secure finder) and the role
   * @param subject
   * @param limitDef
   * @param plainDef
   */
  private void grantView(Subject subject, AttributeDef limitDef, AttributeDef plainDef) {
    this.role.grantPriv(subject, AccessPrivilege.VIEW, false);
    this.role.grantPriv(subject, AccessPrivilege.READ, false);
    this.role.grantPriv(subject, AccessPrivilege.GROUP_ATTR_READ, false);
    limitDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
    this.permissionDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
    plainDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
  }

  /**
   * whether an assignment is still in the database
   * @param attributeAssign
   * @return true if it exists
   */
  private static boolean exists(AttributeAssign attributeAssign) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssign.getId(), false) != null;
  }

  /**
   * authorize as a subject
   * @param subject
   * @param limitAssignId
   * @return the result
   */
  private static PermissionLimitUiAuthorization authorizeAs(Subject subject, String limitAssignId) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      return PermissionLimitUiAuthorization.authorize(grouperSession, subject, limitAssignId);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Read-only and partly privileged callers are refused before anything is deleted; ids that are not
   * limits are refused for everyone; a fully privileged caller can still delete a limit.
   */
  public void testLimitAuthorization() {

    // ---- read only (the reproduction): refused, nothing deleted
    PermissionLimitUiAuthorization readOnly = authorizeAs(SubjectTestHelper.SUBJ2, this.roleLimitAssign.getId());
    assertFalse(readOnly.isAuthorized());
    assertNotNull(readOnly.getLimitAssign());
    try {
      readOnly.deleteLimit();
      fail("an unauthorized limit must not be deleted");
    } catch (InsufficientPrivilegeException ipe) {
      // good
    }
    assertTrue(exists(this.roleLimitAssign));
    assertFalse(authorizeAs(SubjectTestHelper.SUBJ2, this.subjectLimitAssign.getId()).isAuthorized());

    // ---- missing only ATTR_UPDATE on the permission def
    PermissionLimitUiAuthorization noPermDef = authorizeAs(SubjectTestHelper.SUBJ1, this.roleLimitAssign.getId());
    assertEquals(PermissionLimitUiAuthorization.ERROR_CANT_EDIT_LIMIT, noPermDef.getErrorTextKey());
    assertTrue(exists(this.roleLimitAssign));

    // ---- not limits: the permission itself, a deny row, a plain group attribute, junk
    assertEquals(PermissionLimitUiAuthorization.ERROR_CANT_EDIT_LIMIT,
        authorizeAs(SubjectTestHelper.SUBJ0, this.rolePermissionAssign.getId()).getErrorTextKey());
    assertEquals(PermissionLimitUiAuthorization.ERROR_CANT_EDIT_LIMIT,
        authorizeAs(SubjectTestHelper.SUBJ0, this.denyPermissionAssign.getId()).getErrorTextKey());
    assertEquals(PermissionLimitUiAuthorization.ERROR_CANT_EDIT_LIMIT,
        authorizeAs(SubjectTestHelper.SUBJ0, this.plainGroupAttributeAssign.getId()).getErrorTextKey());
    assertEquals(PermissionLimitUiAuthorization.ERROR_CANT_EDIT_LIMIT,
        authorizeAs(SubjectTestHelper.SUBJ0, "doesNotExist").getErrorTextKey());
    assertTrue(exists(this.rolePermissionAssign));
    assertTrue(exists(this.denyPermissionAssign));
    assertTrue(exists(this.plainGroupAttributeAssign));

    // ---- limit values: a value of another limit is not reachable through this one
    GrouperSession rootSession = GrouperSession.startRootSession();
    AttributeAssignValue subjectLimitValue = this.subjectLimitAssign.getValueDelegate().retrieveValues().iterator().next();
    AttributeAssignValue roleLimitValue = this.roleLimitAssign.getValueDelegate().retrieveValues().iterator().next();
    GrouperSession.stopQuietly(rootSession);
    assertNull(AttributeAssignValueBinding.retrieveBoundValue(this.roleLimitAssign, subjectLimitValue.getId()));
    assertNotNull(AttributeAssignValueBinding.retrieveBoundValue(this.roleLimitAssign, roleLimitValue.getId()));

    // ---- fully privileged: both kinds of limit are authorized, and the role is derived from the limit
    PermissionLimitUiAuthorization subjectLimitOk = authorizeAs(SubjectTestHelper.SUBJ0, this.subjectLimitAssign.getId());
    assertTrue(subjectLimitOk.getErrorTextKey(), subjectLimitOk.isAuthorized());
    assertEquals(this.role.getId(), subjectLimitOk.getRole().getId());
    assertEquals(this.subjectPermissionAssign.getId(), subjectLimitOk.getPermissionAssign().getId());

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      PermissionLimitUiAuthorization roleLimitOk = PermissionLimitUiAuthorization.authorize(
          subj0Session, SubjectTestHelper.SUBJ0, this.roleLimitAssign.getId());
      assertTrue(roleLimitOk.getErrorTextKey(), roleLimitOk.isAuthorized());
      assertEquals(this.role.getId(), roleLimitOk.getRole().getId());
      roleLimitOk.deleteLimit();
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertFalse(exists(this.roleLimitAssign));
    assertTrue(exists(this.rolePermissionAssign));
  }
}

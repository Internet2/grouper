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
package edu.internet2.middleware.grouper.permissions.role;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory M2 (role inheritance authorization): adding or removing a role inheritance
 * link must require ADMIN on BOTH roles (wheel and root are allowed).  Before the fix,
 * RoleInheritanceDelegate did no privilege check at all, so a subject who was ADMIN of one role
 * (which anyone with CREATE on a folder can be) could link it to any role it could VIEW and so
 * receive that role's permissions, or remove links on roles it does not manage.  The UI role
 * inheritance endpoints (UiV2Role) only check ADMIN on the current role and rely on the
 * delegate for the other one.
 * <p>
 * Setup: SUBJ0 is a member and ADMIN of controlledRole, and has only VIEW and UPDATE on
 * protectedRole and protectedRole2.  protectedRole carries a permission (read on top:secret).
 * SUBJ1 is in the wheel group in the wheel test.
 * </p>
 * Before the fix the denied calls succeed, so testAddDenied and testRemoveDenied fail.
 */
public class RoleInheritanceAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RoleInheritanceAuthorizationTest("testAddDeniedWithoutAdminOnOtherRole"));
  }

  /**
   *
   */
  public RoleInheritanceAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public RoleInheritanceAuthorizationTest(String name) {
    super(name);
  }

  /** the action of the test permission */
  private static final String ACTION = "read";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** the role SUBJ0 administers and is a member of */
  private Role controlledRole;

  /** a role SUBJ0 can only VIEW and UPDATE, it carries the secret permission */
  private Role protectedRole;

  /** a second role SUBJ0 can only VIEW and UPDATE */
  private Role protectedRole2;

  /** permission resource assigned only to protectedRole */
  private AttributeDefName secret;

  /**
   * make the roles, privileges and permission as root
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(this.rootSession);
    Stem top = root.addChildStem("top", "top");

    this.controlledRole = top.addChildRole("controlledRole", "controlledRole");
    this.protectedRole = top.addChildRole("protectedRole", "protectedRole");
    this.protectedRole2 = top.addChildRole("protectedRole2", "protectedRole2");

    // SUBJ0 is a member and ADMIN of the role it controls
    ((Group)this.controlledRole).addMember(SubjectTestHelper.SUBJ0, false);
    ((Group)this.controlledRole).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // and only VIEW and UPDATE (what the UI role picker accepts) on the others: not ADMIN
    for (Role role : new Role[] {this.protectedRole, this.protectedRole2}) {
      ((Group)role).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      ((Group)role).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    }

    // a permission only protectedRole has, to show what the escalation would give
    AttributeDef permissionDef = new AttributeDefSave(this.rootSession).assignName("top:permissionDef")
        .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    permissionDef.getAttributeDefActionDelegate().configureActionList(ACTION);
    this.secret = new AttributeDefNameSave(this.rootSession, permissionDef).assignName("top:secret")
        .assignDisplayExtension("secret").save();
    this.protectedRole.getPermissionRoleDelegate().assignRolePermission(ACTION, this.secret, PermissionAllowed.ALLOWED);

    // sanity check of the setup: SUBJ0 does not have the permission yet
    assertFalse(subj0HasSecret());
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * start a session as the subject, which becomes the static session the delegate checks
   * @param subject
   * @return the session
   */
  private static GrouperSession startAs(Subject subject) {
    return GrouperSession.start(subject);
  }

  /**
   * see if there is an immediate inheritance link (looked up directly, no privileges involved)
   * @param ifHasRole
   * @param thenHasRole
   * @return true if the link exists
   */
  private static boolean linkExists(Role ifHasRole, Role thenHasRole) {
    return GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(
        ifHasRole.getId(), thenHasRole.getId(), false) != null;
  }

  /**
   * see as root if SUBJ0 effectively has the secret permission
   * @return true if so
   */
  private boolean subj0HasSecret() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      return new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
          .addPermissionName(this.secret).addAction(ACTION).hasPermission();
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---------------------------------------------------------------------------------------
  // denied
  // ---------------------------------------------------------------------------------------

  /**
   * ADMIN on controlledRole and only VIEW/UPDATE on protectedRole: neither direction of link
   * can be added, nothing is written and SUBJ0 does not get the permission
   */
  public void testAddDeniedWithoutAdminOnOtherRole() {
    GrouperSession session = startAs(SubjectTestHelper.SUBJ0);
    try {
      // the escalation (UiV2Role.addRoleImpliedBy): controlledRole inherits from protectedRole
      try {
        this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole);
        fail("SUBJ0 cannot ADMIN protectedRole, so it must not inherit its permissions");
      } catch (InsufficientPrivilegeException ipe) {
        // ok
      }
      // the other direction (UiV2Role.addRoleImplies): protectedRole inherits from controlledRole
      try {
        this.protectedRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.controlledRole);
        fail("SUBJ0 cannot ADMIN protectedRole, so it must not change what it inherits");
      } catch (InsufficientPrivilegeException ipe) {
        // ok
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }

    assertFalse(linkExists(this.controlledRole, this.protectedRole));
    assertFalse(linkExists(this.protectedRole, this.controlledRole));
    assertFalse(subj0HasSecret());
  }

  /**
   * links made by root cannot be removed by SUBJ0 in either direction (UiV2Role.deleteRoleImpliedBy
   * and deleteRoleImplies), and they stay
   */
  public void testRemoveDeniedWithoutAdminOnOtherRole() {
    GrouperSession rootSession2 = GrouperSession.startRootSession();
    try {
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
      assertTrue(this.protectedRole2.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.controlledRole));
    } finally {
      GrouperSession.stopQuietly(rootSession2);
    }
    // root's link gives SUBJ0 the permission (role inheritance works as documented)
    assertTrue(subj0HasSecret());

    GrouperSession session = startAs(SubjectTestHelper.SUBJ0);
    try {
      try {
        this.controlledRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.protectedRole);
        fail("SUBJ0 cannot ADMIN protectedRole, so it must not remove its link");
      } catch (InsufficientPrivilegeException ipe) {
        // ok
      }
      try {
        this.protectedRole2.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.controlledRole);
        fail("SUBJ0 cannot ADMIN protectedRole2, so it must not remove its link");
      } catch (InsufficientPrivilegeException ipe) {
        // ok
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }

    assertTrue(linkExists(this.controlledRole, this.protectedRole));
    assertTrue(linkExists(this.protectedRole2, this.controlledRole));
  }

  // ---------------------------------------------------------------------------------------
  // allowed
  // ---------------------------------------------------------------------------------------

  /**
   * with ADMIN on both roles, SUBJ0 can add and remove links in both directions
   */
  public void testAllowedWithAdminOnBothRoles() {
    ((Group)this.protectedRole).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    GrouperSession session = startAs(SubjectTestHelper.SUBJ0);
    try {
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
      assertFalse(this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(linkExists(this.controlledRole, this.protectedRole));
    assertTrue(subj0HasSecret());

    session = startAs(SubjectTestHelper.SUBJ0);
    try {
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.protectedRole));
      assertTrue(this.protectedRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.controlledRole));
      assertTrue(this.protectedRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.controlledRole));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertFalse(linkExists(this.controlledRole, this.protectedRole));
    assertFalse(linkExists(this.protectedRole, this.controlledRole));
    assertFalse(subj0HasSecret());
  }

  /**
   * root and a wheel member are allowed without any privilege on the roles
   */
  public void testRootAndWheelAllowed() {
    // root (the session from setUp is not the static one any more after helper calls)
    GrouperSession session = GrouperSession.startRootSession();
    try {
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.protectedRole));
    } finally {
      GrouperSession.stopQuietly(session);
    }

    // wheel: SUBJ1 has no privileges on the roles but is in the wheel group
    Group wheel = configureWheelWithSubj1();

    session = startAs(SubjectTestHelper.SUBJ1);
    try {
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
      assertTrue(this.protectedRole2.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole));
      assertTrue(this.controlledRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.protectedRole));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertFalse(linkExists(this.controlledRole, this.protectedRole));
    assertTrue(linkExists(this.protectedRole2, this.protectedRole));
    assertNotNull(wheel);
  }

  // ---------------------------------------------------------------------------------------
  // converting a role back to a group (Group update hook removes its links)
  // ---------------------------------------------------------------------------------------

  /**
   * a wheel member can still convert a linked role back to a group: its links are removed
   */
  public void testRoleToGroupConversionWheel() {
    GrouperSession rootSession2 = GrouperSession.startRootSession();
    try {
      this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole);
      this.protectedRole2.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole);
    } finally {
      GrouperSession.stopQuietly(rootSession2);
    }

    configureWheelWithSubj1();

    GrouperSession session = startAs(SubjectTestHelper.SUBJ1);
    try {
      Group protectedGroup = (Group)this.protectedRole;
      protectedGroup.setTypeOfGroup(TypeOfGroup.group);
      protectedGroup.store();
    } finally {
      GrouperSession.stopQuietly(session);
    }

    assertFalse(linkExists(this.controlledRole, this.protectedRole));
    assertFalse(linkExists(this.protectedRole2, this.protectedRole));
  }

  /**
   * a non-wheel subject who cannot ADMIN every linked role cannot convert the role (that would
   * silently remove links on roles it does not manage); the links stay.  The exception may come
   * wrapped from the update hook, so any exception is accepted.
   */
  public void testRoleToGroupConversionDeniedWithoutAdminOnLinkedRole() {
    GrouperSession rootSession2 = GrouperSession.startRootSession();
    try {
      this.controlledRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.protectedRole);
    } finally {
      GrouperSession.stopQuietly(rootSession2);
    }

    GrouperSession session = startAs(SubjectTestHelper.SUBJ0);
    try {
      Group controlledGroup = (Group)this.controlledRole;
      controlledGroup.setTypeOfGroup(TypeOfGroup.group);
      controlledGroup.store();
      fail("SUBJ0 cannot ADMIN protectedRole, so converting must not drop its link");
    } catch (RuntimeException e) {
      // ok
    } finally {
      GrouperSession.stopQuietly(session);
    }

    assertTrue(linkExists(this.controlledRole, this.protectedRole));
  }

  /**
   * make an etc:wheel group with SUBJ1 in it and turn wheel on (GrouperTest clears the override
   * map after each test)
   * @return the wheel group
   */
  private Group configureWheelWithSubj1() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Stem root = StemFinder.findRootStem(session);
      Stem etc = StemFinder.findByName(session, "etc", false);
      if (etc == null) {
        etc = root.addChildStem("etc", "etc");
      }
      Group wheel = etc.addChildGroup("testWheel", "testWheel");
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
      return wheel;
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}

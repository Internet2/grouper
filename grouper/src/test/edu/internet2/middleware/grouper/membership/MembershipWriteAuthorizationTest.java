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
package edu.internet2.middleware.grouper.membership;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for advisory M4 (SCIM membership DELETE / PUT by uuid):
 * - MembershipFinder.findByUuid must check visibility as the caller, not as root;
 * - Membership.update() must check every owner type (folder rows need STEM_ADMIN, attribute def
 *   rows ATTR_ADMIN) and deny by default, not only group rows;
 * - MembershipWriteAuthorization.deleteChecked (used by SCIM DELETE instead of Membership.delete())
 *   must enforce the normal privileges and write the normal audit entry.
 *
 * Core API only: the SCIM provider's new logic is just these calls plus the 403 mapping.
 */
public class MembershipWriteAuthorizationTest extends GrouperTest {

  /**
   *
   */
  public MembershipWriteAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public MembershipWriteAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipWriteAuthorizationTest("testUpdateStemPrivilegeRequiresStemAdmin"));
  }

  /** a time in the past */
  private static final Timestamp PAST = new Timestamp(System.currentTimeMillis() - 24L * 60L * 60L * 1000L);

  /**
   * group with GrouperAll READ and VIEW revoked (GrouperTest grants them on create), so the test
   * subjects have only what each test grants
   * @param rootSession
   * @param name
   * @return the group
   */
  private static Group privateGroup(GrouperSession rootSession, String name) {
    Group group = new GroupSave(rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    return group;
  }

  /**
   * @param subject
   * @return the member
   */
  private static Member member(Subject subject) {
    return MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
  }

  /**
   * count audit entries of this type whose description mentions the text
   * @param auditTypeBuiltin
   * @param descriptionContains
   * @return the count
   */
  private static int auditCount(AuditTypeBuiltin auditTypeBuiltin, String descriptionContains) {
    return new GcDbAccess().sql("select count(1) from grouper_audit_entry gae, grouper_audit_type gat "
        + "where gae.audit_type_id = gat.id and gat.audit_category = ? and gat.action_name = ? and gae.description like ?")
        .addBindVar(auditTypeBuiltin.getAuditCategory()).addBindVar(auditTypeBuiltin.getActionName())
        .addBindVar("%" + descriptionContains + "%").select(int.class);
  }

  /**
   * revokePriv wraps the privilege failure (Stem / Group / AttributeDef revokePriv throw
   * RevokePrivilegeException caused by InsufficientPrivilegeException), so look down the cause chain,
   * the same way the SCIM provider decides on 403
   * @param throwable
   */
  private static void assertInsufficientPrivilege(Throwable throwable) {
    Throwable current = throwable;
    int depth = 0;
    while (current != null && depth++ < 20) {
      if (current instanceof InsufficientPrivilegeException) {
        return;
      }
      current = current.getCause();
    }
    throw new RuntimeException("expected an InsufficientPrivilegeException in the cause chain", throwable);
  }

  /**
   * findByUuid used to check canViewMembership with the root session, so it authorized nothing.
   * A caller with no privilege on the group must not find the membership; with READ it does.
   */
  public void testFindByUuidChecksCaller() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = privateGroup(rootSession, "test:findByUuid");
    group.addMember(SubjectTestHelper.SUBJ1);
    Membership membership = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true);
    String uuid = membership.getImmediateMembershipId();

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertNull("no privilege: not found", MembershipFinder.findByUuid(subj0Session, uuid, false, true));
      try {
        MembershipFinder.findByUuid(subj0Session, uuid, true, true);
        fail("no privilege: exceptionIfNotFound must throw");
      } catch (MembershipNotFoundException e) {
        // expected
      }
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    GrouperSession.startRootSession();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertNotNull("READ: found", MembershipFinder.findByUuid(subj0Session, uuid, true, true));
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * Membership.update() checked only group rows.  A subject must not re-enable its own expired
   * STEM_ADMIN, and a subject without STEM_ADMIN must not cut off someone else's.  A STEM_ADMIN can.
   */
  public void testUpdateStemPrivilegeRequiresStemAdmin() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(rootSession).assignName("test:f").assignCreateParentStemsIfNotExist(true).save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN, false);
    Field stemAdmins = FieldFinder.find("stemAdmins", true);

    // expire SUBJ1's STEM_ADMIN as root
    Membership subj1Row = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        stem.getUuid(), member(SubjectTestHelper.SUBJ1).getUuid(), stemAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
    subj1Row.setDisabledTime(PAST);
    subj1Row.update();
    assertFalse("precondition: expired", stem.hasStemAdmin(SubjectTestHelper.SUBJ1));

    // (a) SUBJ1 re-enables its own expired row: denied
    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      Membership row = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          stem.getUuid(), member(SubjectTestHelper.SUBJ1).getUuid(), stemAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
      row.setDisabledTime(null);
      row.update();
      fail("re-enabling an expired STEM_ADMIN without STEM_ADMIN must be denied");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertFalse("still expired", stem.hasStemAdmin(SubjectTestHelper.SUBJ1));

    // (b) SUBJ2 (no privilege) cuts off SUBJ3's live STEM_ADMIN: denied
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Membership row = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          stem.getUuid(), member(SubjectTestHelper.SUBJ3).getUuid(), stemAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
      row.setDisabledTime(PAST);
      row.update();
      fail("disabling another subject's STEM_ADMIN without STEM_ADMIN must be denied");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertTrue("SUBJ3 unchanged", stem.hasStemAdmin(SubjectTestHelper.SUBJ3));

    // (c) SUBJ3 (STEM_ADMIN) re-enables SUBJ1: allowed
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    try {
      Membership row = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          stem.getUuid(), member(SubjectTestHelper.SUBJ1).getUuid(), stemAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
      row.setDisabledTime(null);
      row.update();
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    // the earlier "still expired" check cached false in CachingNamingResolver (static ehcache); a
    // membership row update does not flush it (only grant / revoke do), so flush before re-checking
    GrouperSession.staticGrouperSession().getNamingResolver().flushCache();
    assertTrue("re-enabled by a STEM_ADMIN", stem.hasStemAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * same as the folder case for an attribute definition: ATTR_ADMIN is required
   */
  public void testUpdateAttributeDefPrivilegeRequiresAttrAdmin() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(rootSession).assignName("test:f2").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDef = stem.addChildAttributeDef("attrDef", AttributeDefType.attr);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    Field attrAdmins = FieldFinder.find("attrAdmins", true);

    // SUBJ2 (no privilege) cuts off SUBJ1's ATTR_ADMIN: denied
    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Membership row = GrouperDAOFactory.getFactory().getMembership().findByAttrDefOwnerAndMemberAndFieldAndType(
          attributeDef.getId(), member(SubjectTestHelper.SUBJ1).getUuid(), attrAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
      row.setDisabledTime(PAST);
      row.update();
      fail("disabling another subject's ATTR_ADMIN without ATTR_ADMIN must be denied");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertTrue("SUBJ1 unchanged", attributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ1));

    // SUBJ1 (ATTR_ADMIN) may change the row
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      Membership row = GrouperDAOFactory.getFactory().getMembership().findByAttrDefOwnerAndMemberAndFieldAndType(
          attributeDef.getId(), member(SubjectTestHelper.SUBJ1).getUuid(), attrAdmins, MembershipType.IMMEDIATE.getTypeString(), true, false);
      row.setDisabledTime(new Timestamp(System.currentTimeMillis() + 10L * 24L * 60L * 60L * 1000L));
      row.update();
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
  }

  /**
   * group rows keep their behavior: READ only is denied, UPDATE is allowed
   */
  public void testUpdateGroupMembershipUnchanged() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = privateGroup(rootSession, "test:updateGroup");
    group.addMember(SubjectTestHelper.SUBJ1);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    String uuid = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true).getImmediateMembershipId();

    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Membership row = MembershipFinder.findByUuid(subjSession, uuid, true, false);
      row.setDisabledTime(PAST);
      row.update();
      fail("READ only must not change a membership");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }

    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Membership row = MembershipFinder.findByUuid(subjSession, uuid, true, false);
      row.setDisabledTime(new Timestamp(System.currentTimeMillis() + 10L * 24L * 60L * 60L * 1000L));
      row.update();
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
  }

  /**
   * deleteChecked (what SCIM DELETE now calls): READ only is denied and the membership remains;
   * UPDATE deletes it and writes the normal membership delete audit entry; OPTOUT lets a subject
   * remove itself, OPTIN alone does not; effective rows are refused.
   */
  public void testDeleteCheckedGroupMembership() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = privateGroup(rootSession, "test:deleteChecked");
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(SubjectTestHelper.SUBJ5);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.OPTOUT, false);
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.OPTIN, false);

    // (a) READ only: denied (the reproduced SCIM attack), membership remains
    Membership subj1Row = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true);
    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, subj1Row);
      fail("READ only must not delete a membership");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertTrue("still a member", group.hasImmediateMember(SubjectTestHelper.SUBJ1));

    // (b) UPDATE: deleted, and audited
    int auditsBefore = auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, group.getName());
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      assertTrue(MembershipWriteAuthorization.deleteChecked(subjSession, subj1Row));
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertFalse("deleted", group.hasImmediateMember(SubjectTestHelper.SUBJ1));
    assertEquals("delete audited", auditsBefore + 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, group.getName()));

    // (c) OPTOUT on self: allowed
    Membership subj4Row = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ4, true);
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, subj4Row);
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertFalse("opted out", group.hasImmediateMember(SubjectTestHelper.SUBJ4));

    // (d) OPTIN only on self: denied
    Membership subj5Row = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ5, true);
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, subj5Row);
      fail("OPTIN alone must not let a subject remove itself");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertTrue("still a member", group.hasImmediateMember(SubjectTestHelper.SUBJ5));

    // (e) effective rows are refused, even for root
    Group outer = privateGroup(rootSession, "test:deleteCheckedOuter");
    outer.addMember(group.toSubject());
    Membership effective = null;
    for (Membership membership : GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndMemberAndField(
        outer.getUuid(), member(SubjectTestHelper.SUBJ5).getUuid(), Group.getDefaultList(), true)) {
      if (!membership.isImmediate()) {
        effective = membership;
      }
    }
    assertNotNull("precondition: effective row", effective);
    try {
      MembershipWriteAuthorization.deleteChecked(rootSession, effective);
      fail("effective rows must be refused");
    } catch (MemberDeleteException e) {
      // expected
    }
  }

  /**
   * deleteChecked on privilege rows: a folder privilege needs STEM_ADMIN, a group privilege ADMIN
   */
  public void testDeleteCheckedPrivileges() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(rootSession).assignName("test:f3").assignCreateParentStemsIfNotExist(true).save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN, false);
    Membership stemRow = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        stem.getUuid(), member(SubjectTestHelper.SUBJ1).getUuid(), FieldFinder.find("stemAdmins", true),
        MembershipType.IMMEDIATE.getTypeString(), true, false);

    Group group = privateGroup(rootSession, "test:deleteCheckedPriv");
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    Membership adminRow = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), member(SubjectTestHelper.SUBJ1).getUuid(), FieldFinder.find("admins", true),
        MembershipType.IMMEDIATE.getTypeString(), true, false);

    // folder privilege, caller without STEM_ADMIN: denied
    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, stemRow);
      fail("deleting a folder privilege needs STEM_ADMIN");
    } catch (RuntimeException e) {
      // expected: Stem.revokePriv wraps the InsufficientPrivilegeException in a RevokePrivilegeException
      assertInsufficientPrivilege(e);
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }

    // group ADMIN row, caller with UPDATE only: denied
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, adminRow);
      fail("deleting a group ADMIN row needs ADMIN");
    } catch (RuntimeException e) {
      // expected: Group.revokePriv wraps the InsufficientPrivilegeException in a RevokePrivilegeException
      assertInsufficientPrivilege(e);
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertTrue(stem.hasStemAdmin(SubjectTestHelper.SUBJ1));
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ1));

    // folder privilege, caller with STEM_ADMIN: allowed
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    try {
      MembershipWriteAuthorization.deleteChecked(subjSession, stemRow);
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertFalse("revoked by a STEM_ADMIN", stem.hasStemAdmin(SubjectTestHelper.SUBJ1));
  }

}

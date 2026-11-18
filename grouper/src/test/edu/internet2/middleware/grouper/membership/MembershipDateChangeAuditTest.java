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

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * SECURITY tests for advisory M3: membership enabled / disabled date changes must go through the
 * checked, audited API.  Membership.update() (now MembershipWriteAuthorization.updateChecked)
 * checks privileges and writes an audit entry unless the caller audits; MembershipSave uses it
 * for date changes and the checked, audited delete for SaveMode.DELETE, and only acts on
 * immediate default list rows of the group it was given.
 *
 * The group import (UiV2GroupImport) now calls Membership.update() as the logged in user instead
 * of the DAO update; that is UI code and is checked by hand (see the md).  WS addMember calls
 * Membership.update() inside a WILL_NOT_AUDIT callback, which testUpdateInsideNotAuditCallbackAudits
 * reproduces.
 */
public class MembershipDateChangeAuditTest extends GrouperTest {

  /**
   *
   */
  public MembershipDateChangeAuditTest() {
    super();
  }

  /**
   * @param name
   */
  public MembershipDateChangeAuditTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipDateChangeAuditTest("testUpdateAuditsAndChecks"));
  }

  /** ten days from now */
  private static Timestamp future() {
    return new Timestamp(System.currentTimeMillis() + 10L * 24L * 60L * 60L * 1000L);
  }

  /**
   * group with GrouperAll READ and VIEW revoked (GrouperTest grants them on create)
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
   * Membership.update() with a new disabled time: UPDATE writes one MEMBERSHIP_GROUP_UPDATE audit
   * entry naming the change; READ only is denied and nothing changes.  Before the fix update()
   * wrote no audit entry.
   */
  public void testUpdateAuditsAndChecks() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = privateGroup(rootSession, "test:dateAudit");
    group.addMember(SubjectTestHelper.SUBJ1);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    String uuid = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true).getImmediateMembershipId();

    int before = auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName());

    // READ only: denied, no audit
    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Membership row = MembershipFinder.findByUuid(subjSession, uuid, true, false);
      row.setDisabledTime(future());
      row.update();
      fail("READ only must not change membership dates");
    } catch (InsufficientPrivilegeException e) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertNull("unchanged", MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true).getDisabledTime());
    assertEquals("no audit for a denied change", before, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName()));

    // UPDATE: allowed and audited, and the description names the dates
    subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Membership row = MembershipFinder.findByUuid(subjSession, uuid, true, false);
      row.setDisabledTime(future());
      row.update();
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }
    GrouperSession.startRootSession();
    assertEquals("date change audited", before + 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName()));
    assertEquals("description names the dates", 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName() + "%disabledTime: null -> "));
  }

  /**
   * a caller that already audits (outer WILL_AUDIT) gets no extra entry; a caller that does not
   * (outer WILL_NOT_AUDIT, which is what WS addMember does for an existing member) gets one
   */
  public void testUpdateInsideNotAuditCallbackAudits() {

    final GrouperSession rootSession = GrouperSession.startRootSession();
    final Group group = privateGroup(rootSession, "test:dateAuditCallback");
    group.addMember(SubjectTestHelper.SUBJ1);

    int before = auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName());

    // outer WILL_AUDIT: the caller writes its own audit, so update() must not
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            Membership row = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true);
            row.setDisabledTime(future());
            row.update();
            return null;
          }
        });
    assertEquals("caller audits, no extra entry", before, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName()));

    // outer WILL_NOT_AUDIT (WS addMember): update() audits
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            Membership row = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true);
            row.setDisabledTime(null);
            row.update();
            return null;
          }
        });
    assertEquals("WS style caller, audited", before + 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName()));
  }

  /**
   * MembershipSave: a date change and a delete are each audited, and a membership id that is not
   * an immediate member of this group (here another subject's ADMIN row) is not changed or deleted
   * by a caller with UPDATE only
   */
  public void testMembershipSaveAuditsAndRestrictsRows() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = privateGroup(rootSession, "test:membershipSave");
    group.addMember(SubjectTestHelper.SUBJ1);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, false);
    String adminRowId = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ3,
        FieldFinder.find("admins", true)).getImmediateMembershipId();

    int updatesBefore = auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName());
    int deletesBefore = auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, group.getName());

    GrouperSession subjSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      // date change: audited (was a direct DAO update)
      new MembershipSave().assignGroupName(group.getName()).assignSubject(SubjectTestHelper.SUBJ1)
          .assignImmediateMshipDisabledTime(future().getTime()).save();

      // another subject's ADMIN row by id, UPDATE only: must not be deleted
      try {
        new MembershipSave().assignGroupName(group.getName()).assignSubject(SubjectTestHelper.SUBJ3)
            .assignImmediateMembershipId(adminRowId).assignSaveMode(SaveMode.DELETE).save();
      } catch (RuntimeException e) {
        // expected when the row is found (refused as not a member row); with advisory M4's finder
        // change the row is not visible to an UPDATE only caller and there is nothing to delete
      }

      // delete: audited (was Membership.delete(), no audit)
      new MembershipSave().assignGroupName(group.getName()).assignSubject(SubjectTestHelper.SUBJ1)
          .assignSaveMode(SaveMode.DELETE).save();
    } finally {
      GrouperSession.stopQuietly(subjSession);
    }

    GrouperSession.startRootSession();
    assertEquals("date change audited", updatesBefore + 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, group.getName()));
    assertEquals("delete audited", deletesBefore + 1, auditCount(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, group.getName()));
    assertFalse("deleted", group.hasImmediateMember(SubjectTestHelper.SUBJ1));
    assertTrue("ADMIN row untouched", group.hasAdmin(SubjectTestHelper.SUBJ3));
  }

}

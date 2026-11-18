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
import java.text.SimpleDateFormat;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Authorization for writes to an existing membership row (a group list membership, or a group,
 * folder or attribute definition privilege), looked up for example by membership uuid.
 *
 * Why this class exists: Membership.update() used to check privileges only when the row is owned
 * by a group, so folder and attribute definition privilege rows could have their enabled and
 * disabled times changed by anyone, and Membership.delete() checks nothing at all (it is a low
 * level method with many internal callers that already authorized, or run as root).  Code that
 * acts on a membership row for a user (e.g. the SCIM Membership endpoints) needs one place that
 * checks the right privilege for the owner type, denies by default, and writes an audit entry.
 *
 * It is a separate class (not new methods on Membership or Group) so it can ship as one version
 * to every 5.x, 6.x and 7.x release: it only uses APIs that exist from 5.0.0 on.
 *
 * Rules:
 * - update (enabled / disabled time change): group row: the field's write privilege on the group
 *   (UPDATE for lists, ADMIN for access privileges); folder row: STEM_ADMIN; attribute def row:
 *   ATTR_ADMIN; anything else: denied.  Root and wheel are allowed.
 * - delete: immediate rows only, through the normal checked and audited Group / Stem /
 *   AttributeDef APIs (UPDATE or self OPTOUT for lists; ADMIN, STEM_ADMIN, ATTR_ADMIN for
 *   privileges).
 * </pre>
 */
public class MembershipWriteAuthorization {

  /**
   * no instances
   */
  private MembershipWriteAuthorization() {
  }

  // ---------------------------------------------------------------------------------------------
  // update (enabled / disabled times)
  // ---------------------------------------------------------------------------------------------

  /**
   * make sure the subject of the grouper session may change this membership row (e.g. its
   * enabled or disabled time).  Checks by owner type and denies by default.
   * @param grouperSession session of the caller (the subject being authorized)
   * @param membership row that will be updated
   * @throws InsufficientPrivilegeException if not allowed
   */
  public static void assertCanUpdate(GrouperSession grouperSession, Membership membership) {

    GrouperSession.validate(grouperSession);
    Subject subject = grouperSession.getSubject();

    if (membership == null) {
      throw new NullPointerException("membership is null");
    }

    // root and wheel can write anything, same as the privilege resolvers
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }

    Field field = FieldFinder.findById(membership.getFieldId(), true);

    if (!GrouperUtil.isBlank(membership.getOwnerGroupId())) {

      // group rows: same check Membership.update() always did.  Find the group as the caller, so a
      // caller who cannot VIEW the group gets GroupNotFoundException, as before
      Group group = GroupFinder.findByUuid(grouperSession, membership.getOwnerGroupId(), true);
      if (!group.canWriteField(subject, field)) {
        throw new InsufficientPrivilegeException("Not allowed to edit membership on group " + group.getName() + ", field: "
            + field.getName() + ", " + GrouperUtil.subjectToString(subject));
      }
      return;
    }

    if (!GrouperUtil.isBlank(membership.getOwnerStemId())) {

      // folder privilege rows (stemAdmins, creators, ...): editing a naming privilege takes STEM_ADMIN
      Stem stem = membership.getOwnerStem();
      if (!PrivilegeHelper.canStemAdmin(grouperSession, stem, subject)) {
        throw new InsufficientPrivilegeException("Not allowed to edit privilege on folder " + stem.getName() + ", field: "
            + field.getName() + ", " + GrouperUtil.subjectToString(subject));
      }
      return;
    }

    if (!GrouperUtil.isBlank(membership.getOwnerAttrDefId())) {

      // attribute def privilege rows (attrAdmins, attrUpdaters, ...): editing takes ATTR_ADMIN
      AttributeDef attributeDef = membership.getOwnerAttributeDef();
      if (!PrivilegeHelper.canAttrAdmin(grouperSession, attributeDef, subject)) {
        throw new InsufficientPrivilegeException("Not allowed to edit privilege on attribute definition " + attributeDef.getName()
            + ", field: " + field.getName() + ", " + GrouperUtil.subjectToString(subject));
      }
      return;
    }

    // deny by default: an owner type this class does not know
    throw new InsufficientPrivilegeException("Not allowed to edit membership with unknown owner type: "
        + membership.getUuid() + ", " + GrouperUtil.subjectToString(subject));
  }

  /**
   * check the caller may change this membership row, update it in the database, and write an
   * audit entry (unless the caller is already writing one).  This is what Membership.update() does.
   * @param grouperSession session of the caller (the subject being authorized)
   * @param membership row with the new enabled and disabled times set
   * @throws InsufficientPrivilegeException if not allowed
   */
  public static void updateChecked(final GrouperSession grouperSession, final Membership membership) {

    assertCanUpdate(grouperSession, membership);

    // capture the times as loaded from the database, for the audit description.  The dbVersion
    // is reset after the update, so read it first
    Membership dbVersion = membership.dbVersion();
    final Timestamp oldEnabledTime = dbVersion == null ? null : dbVersion.getEnabledTime();
    final Timestamp oldDisabledTime = dbVersion == null ? null : dbVersion.getDisabledTime();
    final boolean hadDbVersion = dbVersion != null;

    // same pattern as Group.addOrEditMember(): WILL_AUDIT, and only audit here if the caller
    // is not going to write its own audit entry
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperDAOFactory.getFactory().getMembership().update(membership);

            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              auditUpdate(grouperSession, membership, hadDbVersion, oldEnabledTime, oldDisabledTime);
            }
            return null;
          }
        });
  }

  /**
   * write the audit entry for an enabled / disabled time change.  The audit type depends on the
   * owner: group list membership, group privilege, folder privilege, attribute def privilege.
   * @param grouperSession caller
   * @param membership updated row
   * @param hadDbVersion true if the old times are known
   * @param oldEnabledTime enabled time before the change
   * @param oldDisabledTime disabled time before the change
   */
  private static void auditUpdate(GrouperSession grouperSession, Membership membership, boolean hadDbVersion,
      Timestamp oldEnabledTime, Timestamp oldDisabledTime) {

    Field field = FieldFinder.findById(membership.getFieldId(), true);
    Subject memberSubject = membership.getMember().getSubject();

    // the time change is the interesting part of this audit, so name the old and new values
    String timesDescription = ", subject: " + memberSubject.getSourceId() + "." + memberSubject.getId()
        + ", field: " + field.getName()
        + ", enabledTime: " + (hadDbVersion ? formatTime(oldEnabledTime) + " -> " : "") + formatTime(membership.getEnabledTime())
        + ", disabledTime: " + (hadDbVersion ? formatTime(oldDisabledTime) + " -> " : "") + formatTime(membership.getDisabledTime())
        + ", by: " + GrouperUtil.subjectToString(grouperSession.getSubject());

    AuditEntry auditEntry = null;

    if (!GrouperUtil.isBlank(membership.getOwnerGroupId())) {

      Group group = membership.getOwnerGroup();
      if (field.isGroupAccessField()) {
        Privilege privilege = AccessPrivilege.listToPriv(field.getName());
        auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_GROUP_UPDATE, "privilegeName",
            privilege == null ? field.getName() : privilege.getName(), "memberId", membership.getMemberUuid(),
            "privilegeType", "access", "groupId", group.getUuid(), "groupName", group.getName());
        auditEntry.setDescription("Updated privilege: group: " + group.getName() + timesDescription);
      } else {
        // same fields and wording as Group.addOrEditMember()
        auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, "id",
            membership.getUuid(), "fieldId", field.getUuid(),
            "fieldName", field.getName(), "memberId", membership.getMemberUuid(),
            "membershipType", membership.getType(),
            "groupId", group.getUuid(), "groupName", group.getName());
        auditEntry.setDescription("Updated membership: group: " + group.getName() + timesDescription);
      }

    } else if (!GrouperUtil.isBlank(membership.getOwnerStemId())) {

      Stem stem = membership.getOwnerStem();
      Privilege privilege = NamingPrivilege.listToPriv(field.getName());
      auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_STEM_UPDATE, "privilegeName",
          privilege == null ? field.getName() : privilege.getName(), "memberId", membership.getMemberUuid(),
          "privilegeType", "naming", "stemId", stem.getUuid(), "stemName", stem.getName());
      auditEntry.setDescription("Updated privilege: stem: " + stem.getName() + timesDescription);

    } else if (!GrouperUtil.isBlank(membership.getOwnerAttrDefId())) {

      AttributeDef attributeDef = membership.getOwnerAttributeDef();
      Privilege privilege = AttributeDefPrivilege.listToPriv(field.getName());
      auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_ATTRIBUTE_DEF_UPDATE, "privilegeName",
          privilege == null ? field.getName() : privilege.getName(), "memberId", membership.getMemberUuid(),
          "privilegeType", "attrDef", "attributeDefId", attributeDef.getUuid(), "attributeDefName", attributeDef.getName());
      auditEntry.setDescription("Updated privilege: attributeDef: " + attributeDef.getName() + timesDescription);
    }

    if (auditEntry != null) {
      auditEntry.saveOrUpdate(true);
    }
  }

  /**
   * format a time for an audit description
   * @param timestamp
   * @return the string or null
   */
  private static String formatTime(Timestamp timestamp) {
    // same format Group.addOrEditMember() uses in its audit description
    return timestamp == null ? null : new SimpleDateFormat("yyyy/MM/dd h:mm a").format(timestamp);
  }

  // ---------------------------------------------------------------------------------------------
  // delete
  // ---------------------------------------------------------------------------------------------

  /**
   * delete an immediate membership row as the subject of the grouper session, through the normal
   * checked and audited API for its owner and field type.  Use this instead of Membership.delete()
   * when acting for a user: Membership.delete() checks nothing and writes no audit entry.
   * @param grouperSession session of the caller (the subject being authorized)
   * @param membership immediate membership (list membership or privilege)
   * @return true if deleted, false if it was already gone
   * @throws InsufficientPrivilegeException if not allowed
   * @throws MemberDeleteException if the row is not an immediate membership
   */
  public static boolean deleteChecked(final GrouperSession grouperSession, final Membership membership) {

    GrouperSession.validate(grouperSession);

    if (membership == null) {
      throw new NullPointerException("membership is null");
    }

    // effective and composite rows are not deleted directly; the immediate row (or the
    // composite) they come from is what has to change
    if (!membership.isImmediate()) {
      throw new MemberDeleteException("Only immediate memberships can be deleted, type: " + membership.getType()
          + ", membership: " + membership.getUuid());
    }

    // the Group / Stem / AttributeDef APIs use the static session, so make sure it is the caller's
    return (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

        Field field = FieldFinder.findById(membership.getFieldId(), true);
        Subject memberSubject = membership.getMember().getSubject();

        if (!GrouperUtil.isBlank(membership.getOwnerGroupId())) {

          // find the group as the caller: a caller who cannot VIEW it gets GroupNotFoundException
          Group group = GroupFinder.findByUuid(theGrouperSession, membership.getOwnerGroupId(), true);

          if (field.isGroupAccessField()) {
            // group privilege: revokePriv checks ADMIN and audits
            return group.revokePriv(memberSubject, AccessPrivilege.listToPriv(field.getName()), false);
          }
          // list membership: deleteMember checks the field's write privilege (UPDATE), or OPTOUT
          // when the caller removes themself, and audits
          return group.deleteMember(memberSubject, field, false);
        }

        if (!GrouperUtil.isBlank(membership.getOwnerStemId())) {
          // folder privilege: revokePriv checks STEM_ADMIN and audits
          Stem stem = StemFinder.findByUuid(theGrouperSession, membership.getOwnerStemId(), true);
          return stem.revokePriv(memberSubject, NamingPrivilege.listToPriv(field.getName()), false);
        }

        if (!GrouperUtil.isBlank(membership.getOwnerAttrDefId())) {
          // attribute def privilege: revokePriv checks ATTR_ADMIN and audits
          AttributeDef attributeDef = AttributeDefFinder.findById(membership.getOwnerAttrDefId(), true);
          return attributeDef.getPrivilegeDelegate().revokePriv(memberSubject,
              AttributeDefPrivilege.listToPriv(field.getName()), false);
        }

        // deny by default: an owner type this class does not know
        throw new InsufficientPrivilegeException("Not allowed to delete membership with unknown owner type: "
            + membership.getUuid() + ", " + GrouperUtil.subjectToString(theGrouperSession.getSubject()));
      }
    });
  }

}

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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperSyncObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Binds the grouper_sync_member / grouper_sync_membership row shown on a provisioning target
 * details page to the group and subject the request was authorized for.</p>
 *
 * <p>Why: the three detail endpoints in UiV2Provisioning (viewProvisioningTargetDetailsOnSubject,
 * viewProvisioningTargetDetailsOnGroupMembership, viewProvisioningTargetDetailsOnSubjectMembership)
 * authorize the group, subject and target from the request, but then load the sync row by a
 * separate request parameter (groupSyncMemberId / groupSyncMembershipId) under a root session.  The
 * DAO by-id lookups are not scoped to the target or to that group and subject, so a caller who may
 * see one membership could substitute the id of any other row (another group, another subject or
 * another provisioner) and read its target ids, caches and error messages.</p>
 *
 * <p>Fix: re-derive the row from the authorized objects with the DAO lookups that are scoped in SQL
 * to the target (grouper_sync_id) and to the group / member ids.  The row is shown only if it is
 * the same row the request asked for; then the canonical row (and its canonical sync group and
 * sync member) replaces the one the handler loaded, so nothing from a by-id lookup reaches the JSP.
 * Otherwise the row is removed, so the page shows the empty detail, the same as a blank id, and does
 * not say whether the requested id exists.</p>
 *
 * <p>ProvisioningContainer.setGuiGrouperSyncObject calls this; only those three detail endpoints
 * call that setter (the list pages use setGuiGrouperSyncObjects).  Kept in its own class so the
 * same file works in every Grouper 5.x - 7.x release and UiV2Provisioning is not changed.</p>
 */
public class ProvisioningSyncRowBinding {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ProvisioningSyncRowBinding.class);

  /**
   * no instances
   */
  private ProvisioningSyncRowBinding() {
  }

  /**
   * bind the sync row in this object to the group and subject that the current UI request
   * authorized (GroupContainer.guiGroup set by UiV2Group.retrieveGroupHelper, SubjectContainer.guiSubject
   * set by UiV2Subject.retrieveSubjectHelper)
   * @param guiGrouperSyncObject object about to be put in the ProvisioningContainer, may be null
   */
  public static void bindToRequest(GuiGrouperSyncObject guiGrouperSyncObject) {

    // nothing loaded by id, nothing to check
    if (guiGrouperSyncObject == null
        || (guiGrouperSyncObject.getGcGrouperSyncMember() == null
          && guiGrouperSyncObject.getGcGrouperSyncMembership() == null)) {
      return;
    }

    Group group = null;
    Subject subject = null;
    String loggedInSubjectId = null;
    try {
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      GuiGroup guiGroup = grouperRequestContainer.getGroupContainer().getGuiGroup();
      group = guiGroup == null ? null : guiGroup.getGroup();
      GuiSubject guiSubject = grouperRequestContainer.getSubjectContainer().getGuiSubject();
      subject = guiSubject == null ? null : guiSubject.getSubject();
      Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      loggedInSubjectId = loggedInSubject == null ? null : loggedInSubject.getId();
    } catch (RuntimeException re) {
      // fail closed below: no group / subject means no row is shown
      LOG.debug("cant read the authorized group / subject from the request", re);
    }

    bind(guiGrouperSyncObject, group, subject, loggedInSubjectId);
  }

  /**
   * bind the sync row in this object to the given authorized group and subject.  Keeps the row
   * (replaced by the canonical row) only if it is the row for that subject (member row) or for that
   * group and subject (membership row) in the object's target, else removes it.
   * @param guiGrouperSyncObject object with the row the handler loaded by request id
   * @param group group the request was authorized for, null if none (subject pages)
   * @param subject subject the request was authorized for
   * @param loggedInSubjectId for the log message only
   */
  public static void bind(final GuiGrouperSyncObject guiGrouperSyncObject, final Group group,
      final Subject subject, final String loggedInSubjectId) {

    if (guiGrouperSyncObject == null) {
      return;
    }

    final GcGrouperSyncMember requestedSyncMember = guiGrouperSyncObject.getGcGrouperSyncMember();
    final GcGrouperSyncMembership requestedSyncMembership = guiGrouperSyncObject.getGcGrouperSyncMembership();
    if (requestedSyncMember == null && requestedSyncMembership == null) {
      return;
    }

    // remove first, so any exception below leaves nothing to render (fail closed)
    guiGrouperSyncObject.setGcGrouperSyncMember(null);
    guiGrouperSyncObject.setGcGrouperSyncMembership(null);

    final String targetName = guiGrouperSyncObject.getTargetName();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // ---------------------------------------------------------------------------------------
        // canonical objects: target sync row, member of the authorized subject
        // ---------------------------------------------------------------------------------------
        GcGrouperSync gcGrouperSync = null;
        if (targetName != null && targetName.trim().length() > 0) {
          gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName);
        }

        Member member = null;
        if (subject != null) {
          try {
            member = MemberFinder.findBySubject(rootSession, subject, false);
          } catch (RuntimeException re) {
            // not a member yet, so it has no sync rows
            LOG.debug("cant find member for subject", re);
          }
        }
        String memberUuid = member == null ? null : member.getUuid();

        // ---------------------------------------------------------------------------------------
        // subject target details: grouper_sync_member of the authorized subject in this target
        // ---------------------------------------------------------------------------------------
        if (requestedSyncMember != null) {
          GcGrouperSyncMember canonicalSyncMember = null;
          if (gcGrouperSync != null && memberUuid != null) {
            // scoped in SQL: where grouper_sync_id = ? and member_id = ?
            canonicalSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(memberUuid);
          }
          if (canonicalSyncMember != null && canonicalSyncMember.getId() != null
              && canonicalSyncMember.getId().equals(requestedSyncMember.getId())) {
            guiGrouperSyncObject.setGcGrouperSyncMember(canonicalSyncMember);
          } else {
            logMismatch("groupSyncMemberId", requestedSyncMember.getId(), targetName, loggedInSubjectId);
          }
        }

        // ---------------------------------------------------------------------------------------
        // membership target details: grouper_sync_membership of the authorized group and subject
        // ---------------------------------------------------------------------------------------
        if (requestedSyncMembership != null) {
          GcGrouperSyncMembership canonicalSyncMembership = null;
          if (gcGrouperSync != null && memberUuid != null && group != null) {
            // sync group and sync member are scoped in SQL by grouper_sync_id, the membership by
            // their ids, so the row belongs to this target, this group and this member
            canonicalSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao()
                .membershipRetrieveByGroupIdAndMemberId(group.getId(), memberUuid);
          }
          if (canonicalSyncMembership != null && canonicalSyncMembership.getId() != null
              && canonicalSyncMembership.getId().equals(requestedSyncMembership.getId())) {

            // attach the canonical sync group and member, not the ones the handler found by id
            canonicalSyncMembership.setGrouperSyncGroup(
                gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(group.getId()));
            canonicalSyncMembership.setGrouperSyncMember(
                gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(memberUuid));
            guiGrouperSyncObject.setGcGrouperSyncMembership(canonicalSyncMembership);
          } else {
            logMismatch("groupSyncMembershipId", requestedSyncMembership.getId(), targetName, loggedInSubjectId);
          }
        }
        return null;
      }
    });
  }

  /**
   * log a sync row id that does not belong to the authorized objects.  Only ids, no names of the
   * protected group or subject.
   * @param paramName request param
   * @param requestedId id the request asked for
   * @param targetName provisioner
   * @param loggedInSubjectId caller
   */
  private static void logMismatch(String paramName, String requestedId, String targetName, String loggedInSubjectId) {
    if (LOG.isWarnEnabled()) {
      LOG.warn("Provisioning details: " + paramName + " '" + requestedId + "' does not belong to the requested "
          + "group / subject in target '" + targetName + "', not shown, logged in subject: " + loggedInSubjectId);
    }
  }
}

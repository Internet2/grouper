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
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.membership.MembershipContainer;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Server side checks for the deprovisioning submits in the UI.</p>
 *
 * <p>The deprovisioning screens list rows (memberships of one subject across objects, or
 * deprovisioned subjects on one object) and the submit then removes access as root for the
 * ids that come back in the request.  The submits used to trust those ids, so any membership
 * or member uuid could be removed from any object.  This class rebuilds, at submit time, the
 * set of rows the screen offered, refuses the whole request if any submitted id is not in it,
 * applies guardrails, and does all the writes in one transaction.</p>
 *
 * <p>Deprovisioning managers are admin-like by design: they may remove access on objects they
 * cannot otherwise see, and the removal runs as root.  This class does not change that.  It
 * only binds the request to what the screen offered, and adds the guardrails:</p>
 * <ul>
 *   <li>never remove from an exclude or deny group (the right-hand factor of a complement
 *   composite, directly or through a group it is a member of): removing someone there grants
 *   access instead of removing it</li>
 *   <li>never touch g:gsa or g:isa subjects (groups as members, GrouperAll, GrouperSystem)</li>
 *   <li>removals from the sysadmin groups, the deprovisioning groups, the deprovisioning
 *   system folder, or anything under the root stem for built in objects (etc) require the
 *   caller to be wheel or root</li>
 * </ul>
 *
 * <p>Rows that hit a guardrail are skipped (not removed) and returned so the screen can list
 * them for the manager to handle by hand.  Ids that were not offered refuse the whole request.</p>
 *
 * <p>This class only uses APIs that exist in every Grouper release from 5.0.0 on, so the same
 * file ships in every patch set.  Note: no commons-lang import on purpose (5.x uses lang, 6.x
 * and later use lang3).</p>
 */
public class GrouperDeprovisioningAllowedAccess {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningAllowedAccess.class);

  // ---------------------------------------------------------------------------------------
  // reason codes (refused requests and skipped rows)
  // ---------------------------------------------------------------------------------------

  /** guardrail: the owner group is an exclude or deny group (complement right factor) */
  public static final String GUARDRAIL_EXCLUDE_OR_DENY_GROUP = "excludeOrDenyGroup";

  /** guardrail: the subject is a group or an internal subject (g:gsa or g:isa) */
  public static final String GUARDRAIL_PROTECTED_SUBJECT = "protectedSubject";

  /** guardrail: the owner is a system object and the caller is not wheel or root */
  public static final String GUARDRAIL_SYSTEM_OBJECT_REQUIRES_WHEEL = "systemObjectRequiresWheel";

  /** refused: deprovisioning is not enabled */
  public static final String REFUSED_DEPROVISIONING_DISABLED = "deprovisioningDisabled";

  /** refused: the caller is not allowed (not a manager of the affiliation, or no privilege on the object) */
  public static final String REFUSED_CALLER_NOT_ALLOWED = "callerNotAllowed";

  /** refused: the member to deprovision was not found or is not from an allowed source */
  public static final String REFUSED_MEMBER_NOT_ALLOWED = "memberNotAllowed";

  /** refused: at least one submitted id was not on the screen */
  public static final String REFUSED_ID_NOT_OFFERED = "idNotOffered";

  /** refused: nothing was submitted */
  public static final String REFUSED_NOTHING_SUBMITTED = "nothingSubmitted";

  /**
   * outcome of a deprovisioning submit
   */
  public static class Result {

    /** null if the request was allowed, else a REFUSED_* code; nothing was changed when refused */
    private String refusedReason;

    /** submitted ids that were not offered by the screen */
    private Set<String> refusedIds = new TreeSet<String>();

    /** memberships whose access was removed (deprovision user screen) */
    private List<Membership> removedMemberships = new ArrayList<Membership>();

    /** members whose access was removed (report screens) */
    private List<Member> removedMembers = new ArrayList<Member>();

    /** rows skipped because of a guardrail: description of the row and the GUARDRAIL_* code */
    private List<String> skipped = new ArrayList<String>();

    /** true if the member was newly added to the users who have been deprovisioned group */
    private boolean addedToDeprovisionedGroup = false;

    /** the member being deprovisioned (deprovision user screen) */
    private Member deprovisionedMember;

    /**
     * @return null if allowed, else a REFUSED_* code (nothing was changed)
     */
    public String getRefusedReason() {
      return this.refusedReason;
    }

    /**
     * @return true if the whole request was refused and nothing was changed
     */
    public boolean isRefused() {
      return this.refusedReason != null;
    }

    /**
     * @return submitted ids that were not offered by the screen
     */
    public Set<String> getRefusedIds() {
      return this.refusedIds;
    }

    /**
     * @return memberships whose access was removed (deprovision user screen)
     */
    public List<Membership> getRemovedMemberships() {
      return this.removedMemberships;
    }

    /**
     * @return members whose access was removed (report screens)
     */
    public List<Member> getRemovedMembers() {
      return this.removedMembers;
    }

    /**
     * @return rows skipped because of a guardrail, "description (code)"
     */
    public List<String> getSkipped() {
      return this.skipped;
    }

    /**
     * @return true if the member was newly added to the users who have been deprovisioned group
     */
    public boolean isAddedToDeprovisionedGroup() {
      return this.addedToDeprovisionedGroup;
    }

    /**
     * @return the member being deprovisioned (deprovision user screen)
     */
    public Member getDeprovisionedMember() {
      return this.deprovisionedMember;
    }

    /**
     * mark refused
     * @param reason
     * @return this
     */
    private Result refuse(String reason) {
      this.refusedReason = reason;
      return this;
    }
  }

  // ---------------------------------------------------------------------------------------
  // guardrails (shared by the deprovision user screen and the report screens)
  // ---------------------------------------------------------------------------------------

  /**
   * see if removing this subject's access on this owner would break a guardrail.  Runs as root
   * since the caller usually cannot see the objects involved; the caller is passed in explicitly
   * for the wheel check for that reason.
   * @param owner group, folder or attribute definition the access would be removed from
   * @param subject whose access would be removed
   * @param caller the logged in user doing the removal
   * @return null if allowed, else a GUARDRAIL_* code
   */
  public static String guardrailViolation(final GrouperObject owner, final Subject subject, final Subject caller) {

    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // groups as members, GrouperAll and GrouperSystem are never deprovisioned: removing a group
        // from an excludes group grants access to all its members, and GrouperAll privileges are site wide
        if (subject == null || isProtectedSourceId(subject.getSourceId())) {
          return GUARDRAIL_PROTECTED_SUBJECT;
        }

        // removing someone from the excluded side of a composite grants access instead of removing it
        if (owner instanceof Group && isExcludeOrDenyGroup((Group)owner)) {
          return GUARDRAIL_EXCLUDE_OR_DENY_GROUP;
        }

        // the groups and folders that decide who administers grouper or deprovisioning need wheel
        if (isProtectedSystemObject(owner) && (caller == null || !PrivilegeHelper.isWheelOrRoot(caller))) {
          return GUARDRAIL_SYSTEM_OBJECT_REQUIRES_WHEEL;
        }
        return null;
      }
    });
  }

  /**
   * @param sourceId
   * @return true if this is the group source or the internal source
   */
  private static boolean isProtectedSourceId(String sourceId) {
    return GrouperUtil.isBlank(sourceId)
        || GrouperUtil.equals(sourceId, GrouperSourceAdapter.groupSourceId())
        || GrouperUtil.equals(sourceId, InternalSourceAdapter.ID);
  }

  /**
   * a group is an exclude or deny group if removing one of its members could put someone INTO a group,
   * i.e. if it reaches the right-hand factor of a complement composite (e.g. the excludes side of a
   * policy group).  It walks upward from the group (2026-11-10, the first version only checked the
   * group and the groups it is a member of): through every group it is a member of (directly or
   * indirectly) and through the owner of every composite it is a factor of (left or right, any type,
   * since a union or intersection owner can itself feed a complement), with a visited set.  Call as root.
   * @param group
   * @return true if removing a member could grant access
   */
  static boolean isExcludeOrDenyGroup(Group group) {

    Set<String> visited = new HashSet<String>();
    java.util.ArrayDeque<Group> toCheck = new java.util.ArrayDeque<Group>();
    toCheck.add(group);

    while (!toCheck.isEmpty()) {
      Group candidate = toCheck.poll();
      if (candidate == null || !visited.add(candidate.getId())) {
        continue;
      }
      // a group nested (directly or indirectly) in another group passes its members up
      for (Group parent : GrouperUtil.nonNull(candidate.toMember().getGroups())) {
        if (parent != null && !visited.contains(parent.getId())) {
          toCheck.add(parent);
        }
      }
      for (Composite composite : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getComposite().findAsFactor(candidate))) {
        if (composite.getType() == CompositeType.COMPLEMENT
            && GrouperUtil.equals(composite.getRightFactorUuid(), candidate.getId())) {
          return true;
        }
        // any other factor position: the composite owner's membership follows this group, keep walking
        Group owner = GrouperDAOFactory.getFactory().getGroup().findByUuid(composite.getFactorOwnerUuid(), false);
        if (owner != null && !visited.contains(owner.getId())) {
          toCheck.add(owner);
        }
      }
    }
    return false;
  }

  /**
   * system objects: the root folder, anything at or under the root stem for built in objects
   * (etc) or the deprovisioning system folder, the sysadmin groups, the deprovisioning admin
   * group, and each affiliation's managers and deprovisioned users groups
   * @param owner
   * @return true if removing access needs wheel
   */
  static boolean isProtectedSystemObject(GrouperObject owner) {

    if (owner == null) {
      return true;
    }

    if (owner instanceof Stem && ((Stem)owner).isRootStem()) {
      return true;
    }

    String ownerName = owner.getName();
    if (GrouperUtil.isBlank(ownerName)) {
      return true;
    }

    Set<String> protectedFolders = new HashSet<String>();
    protectedFolders.add(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc"));
    protectedFolders.add(GrouperDeprovisioningSettings.deprovisioningStemName());
    for (String protectedFolder : protectedFolders) {
      if (!GrouperUtil.isBlank(protectedFolder)
          && (GrouperUtil.equals(ownerName, protectedFolder) || ownerName.startsWith(protectedFolder + ":"))) {
        return true;
      }
    }

    Set<String> protectedNames = new HashSet<String>();
    protectedNames.add(GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group"));
    protectedNames.add(GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.viewonly.group"));
    protectedNames.add(GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.readonly.group"));
    protectedNames.add(GrouperDeprovisioningSettings.retrieveDeprovisioningAdminGroupName());
    for (GrouperDeprovisioningAffiliation affiliation : GrouperUtil.nonNull(GrouperDeprovisioningAffiliation.retrieveAllAffiliations()).values()) {
      protectedNames.add(affiliation.getManagersGroupName());
      protectedNames.add(affiliation.getUsersWhoHaveBeenDeprovisionedGroupName());
    }
    return protectedNames.contains(ownerName);
  }

  /**
   * @param membership
   * @return the group, folder or attribute definition that owns the membership
   */
  private static GrouperObject ownerOf(Membership membership) {
    if (membership.getOwnerGroupId() != null) {
      return membership.getOwnerGroup();
    }
    if (membership.getOwnerStemId() != null) {
      return membership.getOwnerStem();
    }
    if (membership.getOwnerAttrDefId() != null) {
      return membership.getOwnerAttributeDef();
    }
    return null;
  }

  /**
   * @param sourceId
   * @return true if the source is one the deprovisioning screen lets managers deprovision
   */
  private static boolean isSourceAllowedToDeprovision(String sourceId) {
    if (isProtectedSourceId(sourceId)) {
      return false;
    }
    for (Source source : GrouperUtil.nonNull(GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision())) {
      if (GrouperUtil.equals(source.getId(), sourceId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * save an audit entry in the current transaction; the logged in member comes from the request
   * context (UI) or, if there is none (GSH, tests), from the caller
   * @param auditEntry
   * @param caller
   */
  private static void saveAudit(final AuditEntry auditEntry, final Subject caller) {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            GrouperContext.assignAuditEntryFields(auditEntry);
            if (GrouperUtil.isBlank(auditEntry.getLoggedInMemberId()) && caller != null) {
              Member callerMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), caller, true);
              auditEntry.setLoggedInMemberId(callerMember.getId());
            }
            auditEntry.saveOrUpdate(false);
            return null;
          }
        });
  }

  // ---------------------------------------------------------------------------------------
  // screen messages (here, not in the UI class, so each UI submit stays a small edit)
  // ---------------------------------------------------------------------------------------

  /**
   * message for a refused request.  Uses the text key deprovisioningRequestNotAllowed if the
   * text file has it (mainline), else an English default, so patch sets need no text file.
   * @param result
   * @return the message, html escaped
   */
  public static String refusedMessage(Result result) {
    String text = GrouperTextContainer.textOrNull("deprovisioningRequestNotAllowed");
    if (GrouperUtil.isBlank(text)) {
      text = "Error: the request included rows that this screen did not offer, or is not allowed. Nothing was changed. Reload the screen and try again.";
    }
    return text;
  }

  /**
   * message listing rows skipped by a guardrail.  Uses the text key
   * deprovisioningRowsSkippedByGuardrail if the text file has it (mainline), else an English
   * default.
   * @param result
   * @return the message (html escaped), or null if nothing was skipped
   */
  public static String skippedMessage(Result result) {
    if (result == null || result.getSkipped().size() == 0) {
      return null;
    }
    String text = GrouperTextContainer.textOrNull("deprovisioningRowsSkippedByGuardrail");
    if (GrouperUtil.isBlank(text)) {
      text = "These rows were not removed because a deprovisioning guardrail applies (exclude or deny group, group or internal entity, or a system object that needs a sysadmin). Handle them by hand:";
    }
    StringBuilder message = new StringBuilder(text);
    for (String skipped : result.getSkipped()) {
      message.append(" ").append(GrouperUtil.xmlEscape(skipped)).append(";");
    }
    return message.toString();
  }

  // ---------------------------------------------------------------------------------------
  // deprovision user screen (UiV2Deprovisioning.deprovisionUserDeprovisionSubmit)
  // ---------------------------------------------------------------------------------------

  /**
   * the immediate memberships the deprovision user results page offers for this member and
   * affiliation: the same listing as UiV2Deprovisioning.deprovisionUserSubmit (immediate
   * memberships and privileges of list, access, naming and attribute def fields), on objects
   * where showForRemoval is true for the affiliation, for a member from an allowed source.
   * Each row's checkbox carries one of these membership ids.
   * @param member
   * @param affiliation
   * @return membership id to membership, never null
   */
  public static Map<String, Membership> allowedMembershipsForUser(final Member member, final GrouperDeprovisioningAffiliation affiliation) {

    final Map<String, Membership> result = new LinkedHashMap<String, Membership>();

    if (member == null || affiliation == null || !isSourceAllowedToDeprovision(member.getSubjectSourceId())) {
      return result;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        Set<MembershipSubjectContainer> membershipSubjectContainers =
            MembershipFinder.findAllImmediateMemberhipSubjectContainers(rootSession, member.getSubject());

        for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(membershipSubjectContainers)) {

          GrouperObject owner = membershipSubjectContainer.getGroupOwner();
          if (owner == null) {
            owner = membershipSubjectContainer.getStemOwner();
          }
          if (owner == null) {
            owner = membershipSubjectContainer.getAttributeDefOwner();
          }
          if (owner == null) {
            continue;
          }

          // rows with showForRemoval false have no checkbox on the page
          GrouperDeprovisioningOverallConfiguration config = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(owner);
          if (!config.isShowForRemoval(affiliation.getLabel())) {
            continue;
          }

          Set<Membership> memberships = new LinkedHashSet<Membership>();
          for (MembershipContainer membershipContainer : GrouperUtil.nonNull(membershipSubjectContainer.getMembershipContainers()).values()) {
            if (membershipContainer.getImmediateMembership() != null) {
              memberships.add(membershipContainer.getImmediateMembership());
            }
          }
          for (List<Membership> membershipList : GrouperUtil.nonNull(membershipSubjectContainer.getAllMemberships()).values()) {
            memberships.addAll(GrouperUtil.nonNull(membershipList));
          }

          for (Membership membership : memberships) {
            // belt and braces: only immediate memberships of this member
            if (membership == null || GrouperUtil.isBlank(membership.getUuid()) || !membership.isImmediate()
                || !GrouperUtil.equals(membership.getMemberUuid(), member.getId())) {
              continue;
            }
            result.put(membership.getUuid(), membership);
          }
        }
        return null;
      }
    });

    return result;
  }

  /**
   * @param member
   * @param affiliation
   * @return the membership ids the deprovision user results page offers
   */
  public static Set<String> allowedMembershipIdsForUser(Member member, GrouperDeprovisioningAffiliation affiliation) {
    return new LinkedHashSet<String>(allowedMembershipsForUser(member, affiliation).keySet());
  }

  /**
   * <p>Do the deprovision user submit: check every submitted membership id against the rows the
   * results page offers for this member and affiliation, refuse the whole request if any is not
   * offered, skip rows that hit a guardrail, then in one transaction as root remove access for
   * the remaining rows, add the member to the users who have been deprovisioned group, and save
   * the MEMBER_DEPROVISIONING audit entry.  If anything fails, nothing is changed.</p>
   *
   * <p>An empty set of membership ids is allowed (deprovision a user who has no access left).</p>
   *
   * <p>The caller sends the email after this returns, from getRemovedMemberships().</p>
   *
   * @param caller logged in user (must be a manager of the affiliation, or wheel)
   * @param affiliation
   * @param memberId member id of the subject being deprovisioned (from the request)
   * @param membershipIds membership ids from the request
   * @return the result, see Result.isRefused()
   */
  public static Result deprovisionUser(final Subject caller, final GrouperDeprovisioningAffiliation affiliation,
      final String memberId, final Set<String> membershipIds) {

    final Result result = new Result();

    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return result.refuse(REFUSED_DEPROVISIONING_DISABLED);
    }
    if (caller == null || affiliation == null || !affiliation.subjectIsManager(caller)) {
      return result.refuse(REFUSED_CALLER_NOT_ALLOWED);
    }

    return (Result)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // the member comes from the request: it must exist and be from a source the screen allows
        final Member member = GrouperUtil.isBlank(memberId) ? null : MemberFinder.findByUuid(rootSession, memberId, false);
        if (member == null || !isSourceAllowedToDeprovision(member.getSubjectSourceId())) {
          LOG.warn("Deprovision user refused, member not allowed: '" + memberId + "', caller: "
              + GrouperUtil.subjectToString(caller) + ", affiliation: " + affiliation.getLabel());
          return result.refuse(REFUSED_MEMBER_NOT_ALLOWED);
        }
        result.deprovisionedMember = member;

        // rebuild what the results page offered and refuse the whole request on any other id
        Map<String, Membership> allowedMemberships = allowedMembershipsForUser(member, affiliation);
        for (String membershipId : GrouperUtil.nonNull(membershipIds)) {
          if (!allowedMemberships.containsKey(membershipId)) {
            result.refusedIds.add(membershipId);
          }
        }
        if (result.refusedIds.size() > 0) {
          LOG.warn("Deprovision user refused, " + result.refusedIds.size() + " membership id(s) not offered for member: "
              + member.getId() + ", caller: " + GrouperUtil.subjectToString(caller) + ", affiliation: " + affiliation.getLabel());
          return result.refuse(REFUSED_ID_NOT_OFFERED);
        }

        // guardrails: skip, do not refuse, so the rest of the offered rows can still be removed
        final List<Membership> membershipsToRemove = new ArrayList<Membership>();
        for (String membershipId : GrouperUtil.nonNull(membershipIds)) {
          Membership membership = allowedMemberships.get(membershipId);
          GrouperObject owner = ownerOf(membership);
          String violation = guardrailViolation(owner, member.getSubject(), caller);
          if (violation != null) {
            result.skipped.add((owner == null ? membershipId : owner.getName()) + " (" + violation + ")");
            continue;
          }
          membershipsToRemove.add(membership);
        }

        // all or nothing: removals, the deprovisioned users group, and the audit entry
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {

            for (Membership membership : membershipsToRemove) {
              GrouperDeprovisioningLogic.removeAccess(membership);
            }

            // as root: managers do not get UPDATE on this group, and the member and rows are checked above
            Group deprovisionGroup = affiliation.getUsersWhoHaveBeenDeprovisionedGroup();
            if (deprovisionGroup != null) {
              result.addedToDeprovisionedGroup = deprovisionGroup.addMember(member.getSubject(), false);
            } else {
              LOG.warn("Users who have been deprovisioned group not found for affiliation: " + affiliation.getLabel());
            }

            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBER_DEPROVISIONING,
                "memberId", member.getId(), "affiliation", affiliation.getLabel());
            auditEntry.setDescription("Deprovisioned user: " + GrouperUtil.subjectToString(member.getSubject())
                + " from affiliation: " + affiliation.getLabel() + ", by: " + GrouperUtil.subjectToString(caller)
                + ", removed: " + membershipsToRemove.size() + ", skipped: " + result.skipped.size());
            saveAudit(auditEntry, caller);

            return null;
          }
        });

        result.removedMemberships.addAll(membershipsToRemove);
        return result;
      }
    });
  }

  // ---------------------------------------------------------------------------------------
  // deprovisioning report on a group, folder or attribute definition
  // (UiV2Deprovisioning.deprovisioningOn*ReportSubmit)
  // ---------------------------------------------------------------------------------------

  /**
   * the member ids the deprovisioning report on this object lists: subjects deprovisioned for
   * an affiliation that applies to the object who still have an immediate membership or
   * privilege on it (group: access and list fields, folder: naming, attribute def: attribute
   * def privileges).  Same as UiV2Deprovisioning.deprovisioningOn*Report.
   * @param owner group, folder or attribute definition
   * @return member ids, never null
   */
  public static Set<String> reportAllowedMemberIds(final GrouperObject owner) {

    final Set<String> result = new LinkedHashSet<String>();

    if (owner == null || !GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return result;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        Set<DeprovisionedSubject> deprovisionedSubjects =
            GrouperDeprovisioningLogic.subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(owner, false);
        if (GrouperUtil.length(deprovisionedSubjects) == 0) {
          return null;
        }
        Set<Subject> subjects = DeprovisionedSubject.retrieveSubjectsFromDeprovisionedSubject(deprovisionedSubjects);

        List<MembershipResult> membershipResults = new ArrayList<MembershipResult>();
        if (owner instanceof Group) {
          membershipResults.add(new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addGroup((Group)owner)
              .assignFieldType(FieldType.ACCESS).addSubjects(subjects).findMembershipResult());
          membershipResults.add(new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addGroup((Group)owner)
              .assignFieldType(FieldType.LIST).addSubjects(subjects).findMembershipResult());
        } else if (owner instanceof Stem) {
          membershipResults.add(new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addStem((Stem)owner)
              .assignFieldType(FieldType.NAMING).addSubjects(subjects).findMembershipResult());
        } else if (owner instanceof AttributeDef) {
          membershipResults.add(new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addAttributeDef((AttributeDef)owner)
              .assignFieldType(FieldType.ATTRIBUTE_DEF).addSubjects(subjects).findMembershipResult());
        }

        for (MembershipResult membershipResult : membershipResults) {
          for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(membershipResult.getMembershipSubjectContainers())) {
            if (membershipSubjectContainer.getMember() != null) {
              result.add(membershipSubjectContainer.getMember().getId());
            }
          }
        }
        return null;
      }
    });

    return result;
  }

  /**
   * the privileges the report screens require: UPDATE and READ on a group, STEM_ADMIN on a
   * folder, ATTR_READ and ATTR_UPDATE on an attribute definition (or wheel or root)
   * @param caller
   * @param owner
   * @return true if the caller may submit the report on this object
   */
  static boolean callerCanSubmitReport(final Subject caller, final GrouperObject owner) {
    if (caller == null || owner == null) {
      return false;
    }
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        if (PrivilegeHelper.isWheelOrRoot(caller)) {
          return true;
        }
        if (owner instanceof Group) {
          Group group = (Group)owner;
          return group.canHavePrivilege(caller, AccessPrivilege.UPDATE.getName(), false)
              && group.canHavePrivilege(caller, AccessPrivilege.READ.getName(), false);
        }
        if (owner instanceof Stem) {
          return ((Stem)owner).canHavePrivilege(caller, NamingPrivilege.STEM_ADMIN.getName(), false);
        }
        if (owner instanceof AttributeDef) {
          AttributeDef attributeDef = (AttributeDef)owner;
          return attributeDef.getPrivilegeDelegate().canAttrRead(caller)
              && attributeDef.getPrivilegeDelegate().canAttrUpdate(caller);
        }
        return false;
      }
    });
  }

  /**
   * <p>Do a deprovisioning report submit on a group, folder or attribute definition: re-check
   * that deprovisioning is enabled and that the caller has the report's privileges on the
   * object, check every submitted member id against the subjects the report lists, refuse the
   * whole request if any is not listed, skip members that hit a guardrail, then remove access
   * for the rest in one transaction as root.  If anything fails, nothing is changed.</p>
   *
   * <p>The membership and privilege audit entries name the logged in user from the request
   * context, as before.</p>
   *
   * @param caller logged in user
   * @param owner group, folder or attribute definition the report is on
   * @param memberIds member ids from the request
   * @return the result, see Result.isRefused()
   */
  public static Result removeAccessFromReport(final Subject caller, final GrouperObject owner, final Set<String> memberIds) {

    final Result result = new Result();

    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return result.refuse(REFUSED_DEPROVISIONING_DISABLED);
    }
    if (GrouperUtil.length(memberIds) == 0) {
      return result.refuse(REFUSED_NOTHING_SUBMITTED);
    }
    if (!callerCanSubmitReport(caller, owner)) {
      return result.refuse(REFUSED_CALLER_NOT_ALLOWED);
    }

    return (Result)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // rebuild what the report listed and refuse the whole request on any other id
        Set<String> allowedMemberIds = reportAllowedMemberIds(owner);
        for (String memberId : memberIds) {
          if (!allowedMemberIds.contains(memberId)) {
            result.refusedIds.add(memberId);
          }
        }
        if (result.refusedIds.size() > 0) {
          LOG.warn("Deprovisioning report submit refused, " + result.refusedIds.size() + " member id(s) not on the report for: "
              + owner.getName() + ", caller: " + GrouperUtil.subjectToString(caller));
          return result.refuse(REFUSED_ID_NOT_OFFERED);
        }

        // guardrails: skip, do not refuse
        final List<Member> membersToRemove = new ArrayList<Member>();
        for (String memberId : memberIds) {
          Member member = MemberFinder.findByUuid(rootSession, memberId, true);
          String violation = guardrailViolation(owner, member.getSubject(), caller);
          if (violation != null) {
            result.skipped.add(member.getSubjectSourceId() + "::" + member.getSubjectId() + " (" + violation + ")");
            continue;
          }
          membersToRemove.add(member);
        }

        // all or nothing
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
            for (Member member : membersToRemove) {
              Subject subject = member.getSubject();
              if (owner instanceof Group) {
                GrouperDeprovisioningLogic.removeAccess((Group)owner, subject);
              } else if (owner instanceof Stem) {
                GrouperDeprovisioningLogic.removeAccess((Stem)owner, subject);
              } else if (owner instanceof AttributeDef) {
                GrouperDeprovisioningLogic.removeAccess((AttributeDef)owner, subject);
              }
            }
            return null;
          }
        });

        result.removedMembers.addAll(membersToRemove);
        return result;
      }
    });
  }

}

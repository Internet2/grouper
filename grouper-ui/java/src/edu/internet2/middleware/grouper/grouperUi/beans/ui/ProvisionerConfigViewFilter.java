package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningError;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningErrorSummary;
import edu.internet2.middleware.grouper.app.provisioning.GrouperSyncLogWithOwner;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Row-level filter for the provisioner configuration monitoring screens (activity, errors,
 * logs, provisionable groups, assignments).</p>
 *
 * <p>Why: those screens are open to anyone in a provisioner's groupAllowedToView or
 * groupAllowedToAssign group, but being allowed to view a provisioner does not grant VIEW on
 * every group it provisions, or READ on their memberships.  The service methods behind the
 * screens read the grouper_sync_* tables directly (HQL / SQL, no privilege check), so the rows
 * are filtered here, in the caller's GrouperSession, before ProvisionerConfigurationContainer
 * hands them to the JSPs:</p>
 * <ul>
 *   <li>group rows (sync group activity, group errors, group-owned sync logs): VIEW on the group</li>
 *   <li>membership rows (sync membership activity, membership errors): READ on the group</li>
 *   <li>entity (member) rows and job rows: unchanged, that is provisioner metadata the viewer
 *   role is meant to see</li>
 * </ul>
 *
 * <p>Wheel and root callers see every row (nothing is filtered, no extra queries).  A row whose
 * group cannot be found (blank id, deleted group, stale renamed name) is dropped for everyone
 * else.  With no GrouperSession every group and membership row is dropped (fail closed).</p>
 *
 * <p>This class only uses APIs that exist in every Grouper release from 5.0.0 on, so the same
 * file ships in every patch set.  GRP security fix (provisionerConfigViewsDisclosePrivateGroupsAndMemberships).</p>
 */
public class ProvisionerConfigViewFilter {

  /** object type of a group error, see GrouperProvisioningService */
  private static final String OBJECT_TYPE_GROUP = "group";

  /** object type of a membership error, see GrouperProvisioningService */
  private static final String OBJECT_TYPE_MEMBERSHIP = "membership";

  /** batch size for group lookups, keeps the IN clause small */
  private static final int BATCH_SIZE = 100;

  /**
   * no instances
   */
  private ProvisionerConfigViewFilter() {
  }

  // ---------------------------------------------------------------------------------------
  // who is the caller
  // ---------------------------------------------------------------------------------------

  /**
   * @return true if the caller is wheel or root, so rows are not filtered
   */
  public static boolean isUnfiltered() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null) {
      // no session: filter (and since no group can be looked up, every group row is dropped)
      return false;
    }
    Subject subject = grouperSession.getSubject();
    return subject != null && PrivilegeHelper.isWheelOrRoot(subject);
  }

  // ---------------------------------------------------------------------------------------
  // group lookups, in the caller's session
  // ---------------------------------------------------------------------------------------

  /**
   * @param groupIds
   * @param readPrivilege true to require READ (memberships), false for VIEW (the group)
   * @return the ids of the groups the caller can see
   */
  static Set<String> allowedGroupIds(Collection<String> groupIds, boolean readPrivilege) {
    Set<String> result = new HashSet<String>();
    for (Group group : findGroups(groupIds, true, readPrivilege)) {
      result.add(group.getId());
    }
    return result;
  }

  /**
   * @param groupNames
   * @param readPrivilege true to require READ (memberships), false for VIEW (the group)
   * @return the names of the groups the caller can see (current name only, not alternate names)
   */
  static Set<String> allowedGroupNames(Collection<String> groupNames, boolean readPrivilege) {
    Set<String> result = new HashSet<String>();
    for (Group group : findGroups(groupNames, false, readPrivilege)) {
      result.add(group.getName());
    }
    return result;
  }

  /**
   * batched GroupFinder lookup in the static session
   * @param idsOrNames
   * @param byId true for ids, false for names
   * @param readPrivilege true to require READ, false for VIEW
   * @return groups found (never null)
   */
  private static List<Group> findGroups(Collection<String> idsOrNames, boolean byId, boolean readPrivilege) {
    List<Group> result = new ArrayList<Group>();
    if (GrouperSession.staticGrouperSession(false) == null) {
      return result;
    }
    // dedupe and drop blanks; an empty lookup must never reach GroupFinder (no criteria could
    // mean "all groups" when grouper.emptySetOfLookupsReturnsNoResults is false)
    List<String> values = new ArrayList<String>();
    for (String value : new LinkedHashSet<String>(GrouperUtil.nonNull(idsOrNames))) {
      if (!GrouperUtil.isBlank(value)) {
        values.add(value);
      }
    }
    for (int start = 0; start < values.size(); start += BATCH_SIZE) {
      List<String> batch = values.subList(start, Math.min(values.size(), start + BATCH_SIZE));
      GroupFinder groupFinder = new GroupFinder();
      // VIEW is what the UI uses everywhere to decide that a group exists for the caller;
      // READ is what the membership screens require
      groupFinder.assignPrivileges(readPrivilege ? AccessPrivilege.READ_PRIVILEGES : AccessPrivilege.VIEW_PRIVILEGES);
      for (String value : batch) {
        if (byId) {
          groupFinder.addGroupId(value);
        } else {
          groupFinder.addGroupName(value);
        }
      }
      result.addAll(GrouperUtil.nonNull(groupFinder.findGroups()));
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------
  // activity screen
  // ---------------------------------------------------------------------------------------

  /**
   * keep sync group rows whose group the caller can VIEW
   * @param syncGroups not changed
   * @return a new list (or the input for wheel / root / null)
   */
  public static List<GcGrouperSyncGroup> filterSyncGroups(List<GcGrouperSyncGroup> syncGroups) {
    if (syncGroups == null || isUnfiltered()) {
      return syncGroups;
    }
    List<String> groupIds = new ArrayList<String>();
    for (GcGrouperSyncGroup syncGroup : syncGroups) {
      if (syncGroup != null) {
        groupIds.add(syncGroup.getGroupId());
      }
    }
    Set<String> allowed = allowedGroupIds(groupIds, false);
    List<GcGrouperSyncGroup> result = new ArrayList<GcGrouperSyncGroup>();
    for (GcGrouperSyncGroup syncGroup : syncGroups) {
      if (syncGroup != null && syncGroup.getGroupId() != null && allowed.contains(syncGroup.getGroupId())) {
        result.add(syncGroup);
      }
    }
    return result;
  }

  /**
   * keep sync membership rows whose group the caller can READ
   * @param syncMemberships not changed
   * @return a new list (or the input for wheel / root / null)
   */
  public static List<GcGrouperSyncMembership> filterSyncMemberships(List<GcGrouperSyncMembership> syncMemberships) {
    if (syncMemberships == null || isUnfiltered()) {
      return syncMemberships;
    }
    List<String> groupIds = new ArrayList<String>();
    for (GcGrouperSyncMembership syncMembership : syncMemberships) {
      String groupId = membershipGroupId(syncMembership);
      if (groupId != null) {
        groupIds.add(groupId);
      }
    }
    Set<String> allowed = allowedGroupIds(groupIds, true);
    List<GcGrouperSyncMembership> result = new ArrayList<GcGrouperSyncMembership>();
    for (GcGrouperSyncMembership syncMembership : syncMemberships) {
      String groupId = membershipGroupId(syncMembership);
      if (groupId != null && allowed.contains(groupId)) {
        result.add(syncMembership);
      }
    }
    return result;
  }

  /**
   * @param syncMembership
   * @return the grouper group id of a sync membership, or null
   */
  private static String membershipGroupId(GcGrouperSyncMembership syncMembership) {
    if (syncMembership == null || syncMembership.getGrouperSyncGroup() == null) {
      return null;
    }
    return syncMembership.getGrouperSyncGroup().getGroupId();
  }

  // ---------------------------------------------------------------------------------------
  // errors screen
  // ---------------------------------------------------------------------------------------

  /**
   * keep group errors whose group the caller can VIEW, membership errors whose group the
   * caller can READ, and every entity error
   * @param errors not changed
   * @return a new list (or the input for wheel / root / null)
   */
  public static List<GrouperProvisioningError> filterErrors(List<GrouperProvisioningError> errors) {
    if (errors == null || isUnfiltered()) {
      return errors;
    }
    List<String> groupErrorNames = new ArrayList<String>();
    List<String> membershipErrorNames = new ArrayList<String>();
    for (GrouperProvisioningError error : errors) {
      if (error == null) {
        continue;
      }
      if (OBJECT_TYPE_GROUP.equals(error.getObjectType())) {
        groupErrorNames.add(error.getGroupName());
      } else if (OBJECT_TYPE_MEMBERSHIP.equals(error.getObjectType())) {
        membershipErrorNames.add(error.getGroupName());
      }
    }
    Set<String> viewable = allowedGroupNames(groupErrorNames, false);
    Set<String> readable = allowedGroupNames(membershipErrorNames, true);
    List<GrouperProvisioningError> result = new ArrayList<GrouperProvisioningError>();
    for (GrouperProvisioningError error : errors) {
      if (error == null) {
        continue;
      }
      if (OBJECT_TYPE_GROUP.equals(error.getObjectType())) {
        if (error.getGroupName() != null && viewable.contains(error.getGroupName())) {
          result.add(error);
        }
      } else if (OBJECT_TYPE_MEMBERSHIP.equals(error.getObjectType())) {
        if (error.getGroupName() != null && readable.contains(error.getGroupName())) {
          result.add(error);
        }
      } else {
        // entity errors (and anything else without a group) stay
        result.add(error);
      }
    }
    return result;
  }

  /**
   * Rebuild the error summary for a filtered caller.  The group and membership counts come
   * from the filtered error rows (so the counts and the group / entity substring filters are no
   * oracle for groups the caller cannot see); entity counts are kept from the original summary
   * since entity rows are not filtered.  The error rows are capped (1000), so large counts can
   * be lower than the real ones for filtered callers.
   * @param original summary from GrouperProvisioningService (may be null)
   * @param filteredErrors errors after filterErrors (may be null)
   * @return the summary to show (the original for wheel / root)
   */
  public static GrouperProvisioningErrorSummary filterErrorSummary(GrouperProvisioningErrorSummary original,
      List<GrouperProvisioningError> filteredErrors) {
    if (original == null || isUnfiltered()) {
      return original;
    }
    long groupErrors = 0;
    long membershipErrors = 0;
    Map<String, Long> groupErrorTypeCount = new HashMap<String, Long>();
    Map<String, Long> membershipErrorTypeCount = new HashMap<String, Long>();
    for (GrouperProvisioningError error : GrouperUtil.nonNull(filteredErrors)) {
      if (error == null) {
        continue;
      }
      if (OBJECT_TYPE_GROUP.equals(error.getObjectType())) {
        groupErrors++;
        increment(groupErrorTypeCount, error.getErrorCode());
      } else if (OBJECT_TYPE_MEMBERSHIP.equals(error.getObjectType())) {
        membershipErrors++;
        increment(membershipErrorTypeCount, error.getErrorCode());
      }
    }
    GrouperProvisioningErrorSummary result = new GrouperProvisioningErrorSummary();
    result.setGroupErrorsCount(groupErrors);
    result.setGroupErrorTypeCount(groupErrorTypeCount);
    result.setMembershipErrorsCount(membershipErrors);
    result.setMembershipsErrorTypeCount(membershipErrorTypeCount);
    result.setEntityErrorsCount(original.getEntityErrorsCount());
    result.setEntityErrorTypeCount(original.getEntityErrorTypeCount());
    result.setErrorsCount(groupErrors + membershipErrors + original.getEntityErrorsCount());
    return result;
  }

  /**
   * @param counts
   * @param key
   */
  private static void increment(Map<String, Long> counts, String key) {
    Long current = counts.get(key);
    counts.put(key, current == null ? 1L : current + 1L);
  }

  // ---------------------------------------------------------------------------------------
  // logs screen
  // ---------------------------------------------------------------------------------------

  /**
   * drop group-owned log rows whose group the caller cannot VIEW (the description is the log
   * text for that group); job and member owned rows stay
   * @param logs not changed
   * @return a new list (or the input for wheel / root / null)
   */
  public static List<GuiProvisionerLog> filterLogs(List<GuiProvisionerLog> logs) {
    if (logs == null || isUnfiltered()) {
      return logs;
    }
    List<String> groupIds = new ArrayList<String>();
    for (GuiProvisionerLog log : logs) {
      GcGrouperSyncGroup syncGroup = logSyncGroup(log);
      if (syncGroup != null) {
        groupIds.add(syncGroup.getGroupId());
      }
    }
    Set<String> allowed = allowedGroupIds(groupIds, false);
    List<GuiProvisionerLog> result = new ArrayList<GuiProvisionerLog>();
    for (GuiProvisionerLog log : logs) {
      if (log == null) {
        continue;
      }
      // a row whose owner id is set but resolved to no job, group or member (the owner was deleted or
      // never synced) is dropped too: its description can be the log text for a private group (2026-11-10)
      if (isOrphanOwnedLog(log)) {
        continue;
      }
      GcGrouperSyncGroup syncGroup = logSyncGroup(log);
      if (syncGroup == null || (syncGroup.getGroupId() != null && allowed.contains(syncGroup.getGroupId()))) {
        result.add(log);
      }
    }
    return result;
  }

  /**
   * @param log
   * @return true if the row names an owner id that resolved to no job, group or member
   */
  private static boolean isOrphanOwnedLog(GuiProvisionerLog log) {
    GrouperSyncLogWithOwner withOwner = log.getGcGrouperSyncLogWithOwner();
    if (withOwner == null || withOwner.getGcGrouperSyncLog() == null) {
      return false;
    }
    if (withOwner.getGcGrouperSyncLog().getGrouperSyncOwnerId() == null
        || withOwner.getGcGrouperSyncLog().getGrouperSyncOwnerId().trim().length() == 0) {
      return false;
    }
    return withOwner.getGcGrouperSyncJob() == null && withOwner.getGcGrouperSyncGroup() == null
        && withOwner.getGcGrouperSyncMember() == null;
  }

  /**
   * @param log
   * @return the owning sync group of a log row, or null if not group owned
   */
  private static GcGrouperSyncGroup logSyncGroup(GuiProvisionerLog log) {
    if (log == null) {
      return null;
    }
    GrouperSyncLogWithOwner withOwner = log.getGcGrouperSyncLogWithOwner();
    return withOwner == null ? null : withOwner.getGcGrouperSyncGroup();
  }

  // ---------------------------------------------------------------------------------------
  // provisionable groups and assignments screens
  // ---------------------------------------------------------------------------------------

  /**
   * The handlers look the groups up with GroupFinder in the caller's session (VIEW), but wrap
   * a group that was not found (not viewable) as new GuiGroup(null), which breaks the JSP and
   * still counts the hidden group.  Drop those rows.
   * @param guiGroups not changed
   * @return a new list without empty rows (or the input if null)
   */
  public static List<GuiGroup> filterGuiGroups(List<GuiGroup> guiGroups) {
    if (guiGroups == null) {
      return null;
    }
    // the handlers look these up with an unprivileged GroupFinder, so the group is found even when the
    // caller cannot VIEW it: re-check VIEW here, the one place both screens go through (2026-11-10)
    Set<String> allowed = null;
    if (!isUnfiltered()) {
      List<String> groupIds = new ArrayList<String>();
      for (GuiGroup guiGroup : guiGroups) {
        if (guiGroup != null && guiGroup.getGroup() != null) {
          groupIds.add(guiGroup.getGroup().getId());
        }
      }
      allowed = allowedGroupIds(groupIds, false);
    }
    List<GuiGroup> result = new ArrayList<GuiGroup>();
    for (GuiGroup guiGroup : guiGroups) {
      if (guiGroup != null && guiGroup.getGroup() != null
          && (allowed == null || allowed.contains(guiGroup.getGroup().getId()))) {
        result.add(guiGroup);
      }
    }
    return result;
  }

  /**
   * @param guiObject a GuiGroup or GuiStem (assignment row)
   * @return true if it wraps nothing (the group or folder was not found for the caller)
   */
  public static boolean isEmptyGuiObject(Object guiObject) {
    if (guiObject == null) {
      return true;
    }
    if (guiObject instanceof GuiGroup) {
      Group group = ((GuiGroup)guiObject).getGroup();
      if (group == null) {
        return true;
      }
      // found by an unprivileged lookup: treat a group the caller cannot VIEW as not found (2026-11-10)
      return !isUnfiltered() && !allowedGroupIds(java.util.Collections.singleton(group.getId()), false).contains(group.getId());
    }
    if (guiObject instanceof GuiStem) {
      return ((GuiStem)guiObject).getStem() == null;
    }
    return false;
  }

}

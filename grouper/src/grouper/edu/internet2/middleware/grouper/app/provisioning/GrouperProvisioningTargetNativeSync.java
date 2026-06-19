package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * Sibling of {@link GrouperProvisioningConfiguration} / {@link GrouperProvisioningBehavior} /
 * {@link GrouperProvisioningData} that owns the runtime "generic provisioner sync back"
 * logic. Subclasses live per protocol (e.g. {@code LdapProvisioningTargetNativeSync},
 * eventually {@code GrouperScim2ProvisioningTargetNativeSync}) and add the protocol-specific
 * concerns:
 *
 * <ul>
 *   <li>Bean construction from the native protocol object (e.g. {@code LdapEntry}, {@code JsonNode}).</li>
 *   <li>Widening the protocol-specific search-attribute set so the native-attribute paths
 *       configured on {@link GrouperProvisioningConfiguration} are returned by the target query.</li>
 *   <li>Convenience capture-from-native-entry methods that pair a build with a record.</li>
 * </ul>
 *
 * <p>This base is concrete and usable as-is for the recording side; a default provisioner
 * with no protocol-specific build logic gets the generic three-method recorder.
 *
 * <p>Each {@code recordTargetNativeXxx} method is gated by the corresponding
 * {@link GrouperProvisioningBehavior} predicate
 * ({@link GrouperProvisioningBehavior#isLoadGroupsToGenericGrouperTable()} etc.) so callers
 * never need to flag-check at the call site.
 */
public class GrouperProvisioningTargetNativeSync {

  private GrouperProvisioner grouperProvisioner;

  /** reference back up to the provisioner */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /** wired by {@link GrouperProvisioner#retrieveGrouperProvisioningTargetNativeSync()} */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  /**
   * Append a native group bean to the in-memory list that drives end-of-run sync into
   * grouper_prov_group / _attr / _attr_value. No-op when reporting is disabled or the DAO
   * doesn't support retrieving groups.
   */
  public synchronized void recordTargetNativeGroup(GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup) {
    if (grouperProvisioningTargetNativeGroup == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    String targetId = grouperProvisioningTargetNativeGroup.getTargetId();
    if (targetId == null) {
      // no key to index by; skip rather than crash. real responses always have an id.
      return;
    }
    // last-write-wins on duplicate targetId — supports both read-pass capture and later
    // write-shadowing (e.g. patch-response capture for the same id) without growing dups.
    this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetGroupIdToNativeGroup()
        .put(targetId, grouperProvisioningTargetNativeGroup);
    // NOTE: do NOT clear the sync-back-read mark here. The drain cross-checks the to-read
    // set against this map: an id present in both was captured by its own write (or a read),
    // so the drain skips it; the drain owns clearing the to-read set when it runs.
  }

  /**
   * Append a native user bean to the in-memory list that drives end-of-run sync into
   * grouper_prov_user / _attr / _attr_value. No-op when reporting is disabled or the DAO
   * doesn't support retrieving entities.
   */
  public synchronized void recordTargetNativeUser(GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser) {
    if (grouperProvisioningTargetNativeUser == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    String targetId = grouperProvisioningTargetNativeUser.getTargetId();
    if (targetId == null) {
      return;
    }
    // last-write-wins; see recordTargetNativeGroup for rationale.
    this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetUserIdToNativeUser()
        .put(targetId, grouperProvisioningTargetNativeUser);
    // see recordTargetNativeGroup: the sync-back-read mark is left for the drain to
    // cross-check and clear, not cleared here.
  }

  /**
   * Effective list of native attribute configs to capture for entities (users). If the
   * provisioner config has an explicit {@code nativeAttributesEntities} list, that wins;
   * otherwise the protocol subclass's defaults from
   * {@link #getDefaultNativeAttributeConfigsEntities()} are used.
   */
  public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
    List<GrouperProvisioningNativeAttributeConfig> configured =
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getNativeAttributeConfigsEntities();
    if (configured != null && !configured.isEmpty()) {
      return configured;
    }
    return this.getDefaultNativeAttributeConfigsEntities();
  }

  /**
   * Effective list of native attribute configs to capture for groups. See
   * {@link #effectiveNativeAttributeConfigsEntities()} for the resolution rule.
   */
  public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
    List<GrouperProvisioningNativeAttributeConfig> configured =
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getNativeAttributeConfigsGroups();
    if (configured != null && !configured.isEmpty()) {
      return configured;
    }
    return this.getDefaultNativeAttributeConfigsGroups();
  }

  /**
   * Per-protocol sensible-default attribute list for entities (users) when the operator
   * hasn't configured {@code nativeAttributesEntities}. Override in protocol subclasses to
   * return a curated list (e.g. SCIM core schema fields). Default is empty — for LDAP this
   * means "no extra capture beyond what the regular target query returned," which is the
   * historical behavior.
   */
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsEntities() {
    return Collections.emptyList();
  }

  /**
   * Per-protocol sensible-default attribute list for groups. See
   * {@link #getDefaultNativeAttributeConfigsEntities()}.
   */
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    return Collections.emptyList();
  }

  /**
   * Append native memberships to the in-memory list that drives end-of-run sync into
   * grouper_prov_mship. No-op when reporting is disabled, or when the membership load flag
   * is off, or when the input list is empty.
   */
  public synchronized void recordTargetNativeMemberships(
      List<GrouperProvisioningTargetNativeMembership> grouperProvisioningTargetNativeMemberships) {
    if (grouperProvisioningTargetNativeMemberships == null || grouperProvisioningTargetNativeMemberships.isEmpty()) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isLoadMembershipsToGenericGrouperTable()) {
      return;
    }
    java.util.Map<MultiKey, GrouperProvisioningTargetNativeMembership> index =
        this.grouperProvisioner.retrieveGrouperProvisioningData()
            .getTargetGroupIdTargetUserIdToNativeMembership();
    for (GrouperProvisioningTargetNativeMembership grouperProvisioningTargetNativeMembership
        : grouperProvisioningTargetNativeMemberships) {
      if (grouperProvisioningTargetNativeMembership == null) {
        continue;
      }
      String targetGroupId = grouperProvisioningTargetNativeMembership.getTargetGroupId();
      String targetUserId = grouperProvisioningTargetNativeMembership.getTargetUserId();
      if (targetGroupId == null || targetUserId == null) {
        continue;
      }
      // last-write-wins on duplicate (gid,uid). same rationale as recordTargetNativeGroup.
      index.put(new MultiKey(targetGroupId, targetUserId),
          grouperProvisioningTargetNativeMembership);
    }
  }

  // ===================== sync-back read set (mark/clear) =====================
  // Write sites mark the affected target id with {@link #markSyncBackUserForRead} / {@link
  // #markSyncBackGroupForRead}. The end-of-run drain (GrouperProvisioningLogic
  // .syncBackDrainGroups) cross-checks each marked id against the read map: an id already in
  // the map was captured by its own write response (or a read), so it is skipped; an id NOT
  // in the map is bulk re-read. The drain then clears the whole set.
  //
  // recordTargetNativeXxx deliberately does NOT clear the mark (so the cross-check can see an
  // id in both sets); the drain is the sole clearer. Mark gates on the behavior flag (no
  // point growing the set for an axis that won't be flushed); clear/remove are unconditional
  // (cheap, idempotent).

  /**
   * Register {@code targetId} as needing a sync-back re-read. Called from write sites that
   * change an entity in the target without getting a response body to capture from.
   * No-op when reporting is off for entities or when {@code targetId} is null.
   */
  public void markSyncBackUserForRead(String targetId) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackUserNativeIdsToRead().add(targetId);
  }

  /** see {@link #markSyncBackUserForRead}; same semantics for groups */
  public void markSyncBackGroupForRead(String targetId) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackGroupNativeIdsToRead().add(targetId);
  }

  /**
   * Drop {@code targetId} from the user sync-back-read set. Called by
   * {@link #recordTargetNativeUser} whenever a fresh entry lands in the canonical map —
   * whether from a read response, a write response, or the drain.
   */
  public void clearSyncBackUserForRead(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackUserNativeIdsToRead().remove(targetId);
  }

  /** see {@link #clearSyncBackUserForRead}; same semantics for groups */
  public void clearSyncBackGroupForRead(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackGroupNativeIdsToRead().remove(targetId);
  }

  // ===================== read-map removal =====================
  // Used by the insert/update hooks (drop a stale pre-write snapshot before re-capture) and
  // by the delete path (drop a deleted object so the flush deletes its mirror row).

  /** Remove any captured native group representation for {@code targetId}. Idempotent. */
  public void removeTargetNativeGroup(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getTargetGroupIdToNativeGroup().remove(targetId);
  }

  /** see {@link #removeTargetNativeGroup}; same semantics for users */
  public void removeTargetNativeUser(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getTargetUserIdToNativeUser().remove(targetId);
  }

  // ===================== sync-back deleted set (mark/clear) =====================
  // The delete write path marks the deleted target id; the end-of-run drain / flush drops
  // it from the mirror. An insert of the same id clears the mark (re-create wins).

  /**
   * Register {@code targetId} as deleted from the target this run. No-op when group
   * reporting is off or {@code targetId} is null.
   */
  public void markSyncBackGroupForDelete(String targetId) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackGroupNativeIdsDeleted().add(targetId);
  }

  /** Drop {@code targetId} from the group deleted set (cheap, idempotent). */
  public void clearSyncBackGroupForDelete(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackGroupNativeIdsDeleted().remove(targetId);
  }

  /** see {@link #markSyncBackGroupForDelete}; same semantics for users */
  public void markSyncBackUserForDelete(String targetId) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackUserNativeIdsDeleted().add(targetId);
  }

  /** see {@link #clearSyncBackGroupForDelete}; same semantics for users */
  public void clearSyncBackUserForDelete(String targetId) {
    if (targetId == null) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningData()
        .getSyncBackUserNativeIdsDeleted().remove(targetId);
  }

  // ===================== write hooks (insert / update) =====================

  /**
   * Group-write sync-back hook, called from a provisioner's insert/update commands path
   * right after the target write (create or attribute update). Runs four steps in order:
   * <ol>
   *   <li>mark the target id for sync-back read (the drain ensures it later),</li>
   *   <li>drop the id from the deleted set (a write supersedes a pending delete),</li>
   *   <li>remove any stale read-map representation -- for an update this is the crucial step
   *       (drops the pre-write read-pass native so the drain re-reads it; an insert normally
   *       has none),</li>
   *   <li>if the write returned the native object, register it like a read so no re-read is
   *       needed -- the drain then cross-checks the read map and skips this id.</li>
   * </ol>
   * No-op when group reporting is off or {@code targetId} is null.
   *
   * @param targetId native target id of the written group
   * @param grouperProvisioningTargetNativeGroup native object from the write response, or
   *   null when the write returned no usable body (then the drain bulk-reads it)
   */
  public synchronized void recordTargetNativeGroupWrite(String targetId,
      GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    // 1. mark for sync-back read
    this.markSyncBackGroupForRead(targetId);
    // 2. a write supersedes any pending delete of the same id
    this.clearSyncBackGroupForDelete(targetId);
    // 3. drop any stale (pre-write) read-map representation so the drain re-reads it
    this.removeTargetNativeGroup(targetId);
    // 4. if the write returned the native, register it like a read (drain then skips it)
    if (grouperProvisioningTargetNativeGroup != null) {
      this.recordTargetNativeGroup(grouperProvisioningTargetNativeGroup);
    }
  }

  /**
   * Entity-write sync-back hook; the user-axis mirror of
   * {@link #recordTargetNativeGroupWrite(String, GrouperProvisioningTargetNativeGroup)}
   * (mark to-read, clear pending delete, drop stale rep, register if returned). No-op when
   * entity reporting is off or {@code targetId} is null.
   *
   * @param targetId native target id of the written entity
   * @param grouperProvisioningTargetNativeUser native object from the write response, or
   *   null when the write returned no usable body (then the drain bulk-reads it)
   */
  public synchronized void recordTargetNativeUserWrite(String targetId,
      GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser) {
    if (targetId == null) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior()
        .isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    // 1. mark for sync-back read
    this.markSyncBackUserForRead(targetId);
    // 2. a write supersedes any pending delete of the same id
    this.clearSyncBackUserForDelete(targetId);
    // 3. drop any stale (pre-write) read-map representation so the drain re-reads it
    this.removeTargetNativeUser(targetId);
    // 4. if the write returned the native, register it like a read (drain then skips it)
    if (grouperProvisioningTargetNativeUser != null) {
      this.recordTargetNativeUser(grouperProvisioningTargetNativeUser);
    }
  }

}

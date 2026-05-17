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
  public void recordTargetNativeGroup(GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup) {
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
    // a fresh entry in the canonical map means any pending sync-back-read for this id is
    // now satisfied — drop it from the dirty set so the drain doesn't re-fetch.
    this.clearSyncBackGroupForRead(targetId);
  }

  /**
   * Append a native user bean to the in-memory list that drives end-of-run sync into
   * grouper_prov_user / _attr / _attr_value. No-op when reporting is disabled or the DAO
   * doesn't support retrieving entities.
   */
  public void recordTargetNativeUser(GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser) {
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
    // satisfied any pending sync-back-read for this id; see recordTargetNativeGroup.
    this.clearSyncBackUserForRead(targetId);
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
  public void recordTargetNativeMemberships(
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
  // Write sites that don't get a response body (e.g. SCIM PATCH returning 204, LDAP modify)
  // mark the affected target id with {@link #markSyncBackUserForRead} / {@link
  // #markSyncBackGroupForRead}. A later drain phase re-reads each marked id, captures
  // through {@link #recordTargetNativeUser} / {@link #recordTargetNativeGroup}, which
  // clears the dirty entry. So the steady-state invariant at end-of-flush is: the set is
  // empty (every dirty id was either drained, or never marked because the write got a body
  // and the capture happened directly).
  //
  // Mark gates on the behavior flag — no point growing the set for an axis that won't be
  // flushed. Clear is unconditional (cheap, idempotent set.remove).
  //
  // The drain itself is not implemented in this slice — these methods only set up the
  // infrastructure so write sites and the eventual drain have a single chokepoint.

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

}

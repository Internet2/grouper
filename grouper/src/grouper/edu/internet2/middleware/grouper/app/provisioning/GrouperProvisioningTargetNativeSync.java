package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collections;
import java.util.List;

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
    this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetNativeGroups()
        .add(grouperProvisioningTargetNativeGroup);
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
    this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetNativeUsers()
        .add(grouperProvisioningTargetNativeUser);
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
    this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetNativeMemberships()
        .addAll(grouperProvisioningTargetNativeMemberships);
  }

}

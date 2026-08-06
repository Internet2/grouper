package edu.internet2.middleware.grouper.app.jamf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;

/**
 * Jamf-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting beans
 * for sync-back into the generic grouper_prov_group / grouper_prov_user / grouper_prov_mship
 * tables.
 *
 * <p>Unlike Azure (which captures from raw Graph JSON), Jamf speaks XML that is parsed into the
 * typed {@link JamfAccount} / {@link JamfAccountGroup} beans, so capture reads those beans. The
 * native attribute set is therefore the fields those beans model. Object capture is hooked at the
 * {@link JamfApiCommands} read seams (each account/role read) and membership capture at the
 * {@link JamfTargetDao} translation/write seams where target ids are resolved.</p>
 *
 * <p>Accounts are create-only (Grouper never updates account attributes), so a cache-reconstructed
 * account carrying only the attributes present at read time cannot trigger a spurious update.</p>
 */
public class JamfProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /** default captured attributes for a Jamf account (entity); id is already the target_user_id column */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("fullName"),
          attrConfig("email"),
          attrConfig("accessLevel")));

  /** default captured attributes for a Jamf account group / role (group); id is target_group_id */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("accessLevel"),
          attrConfig("privilegeSet"),
          attrConfig("site")));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    return cfg;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsEntities() {
    return DEFAULT_ENTITY_ATTRS;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    return DEFAULT_GROUP_ATTRS;
  }

  // ----- build (typed bean -> native reporting bean) -----------------------------------

  /**
   * Build a native group bean from a JamfAccountGroup. Returns null if the role has no id.
   * @param role the account group (role)
   * @return the native group bean, or null
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroup(JamfAccountGroup role) {
    if (role == null || StringUtils.isBlank(role.getId())) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(role.getId());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsGroups()) {
      Object value = groupAttributeValue(role, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  /**
   * Build a native user bean from a JamfAccount. Returns null if the account has no id.
   * @param account the account
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUser(JamfAccount account) {
    if (account == null || StringUtils.isBlank(account.getId())) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(account.getId());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsEntities()) {
      Object value = entityAttributeValue(account, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  private static Object groupAttributeValue(JamfAccountGroup role, String name) {
    if ("name".equals(name)) {
      return role.getName();
    }
    if ("accessLevel".equals(name)) {
      return role.getAccessLevel();
    }
    if ("privilegeSet".equals(name)) {
      return role.getPrivilegeSet();
    }
    if ("site".equals(name)) {
      return role.getSiteName();
    }
    return null;
  }

  private static Object entityAttributeValue(JamfAccount account, String name) {
    if ("name".equals(name)) {
      return account.getName();
    }
    if ("fullName".equals(name)) {
      return account.getFullName();
    }
    if ("email".equals(name)) {
      return account.getEmail();
    }
    if ("accessLevel".equals(name)) {
      return account.getAccessLevel();
    }
    return null;
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Jamf role. No-op when sync-back is off or the role has no id. */
  public void captureGroup(JamfAccountGroup role) {
    this.recordTargetNativeGroup(this.buildNativeGroup(role));
  }

  /** Build + record a Jamf account. No-op when sync-back is off or the account has no id. */
  public void captureAccount(JamfAccount account) {
    this.recordTargetNativeUser(this.buildNativeUser(account));
  }

  /**
   * Record (roleId, accountId) memberships for a single role, given already-resolved account ids.
   * @param targetGroupId the role id
   * @param targetUserIds the member account native ids
   */
  public void captureMembershipsForGroup(String targetGroupId, Collection<String> targetUserIds) {
    if (targetGroupId == null || targetUserIds == null || targetUserIds.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String userId : targetUserIds) {
      if (userId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(userId);
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from JamfApiCommands / JamfTargetDao) --------------

  /** Capture a role read against the current provisioner's sync. No-op outside a Jamf cycle. */
  public static void captureGroupFromCurrentProvisioner(JamfAccountGroup role) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.captureGroup(role);
    }
  }

  /** Capture an account read against the current provisioner's sync. No-op outside a Jamf cycle. */
  public static void captureAccountFromCurrentProvisioner(JamfAccount account) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.captureAccount(account);
    }
  }

  /** Record a role's full membership set (already-resolved account ids) against the current sync. */
  public static void captureMembershipsForGroupFromCurrentProvisioner(
      String targetGroupId, Collection<String> targetUserIds) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.captureMembershipsForGroup(targetGroupId, targetUserIds);
    }
  }

  /**
   * Write-track: after a successful full-members replace, set the mirror for this role to exactly
   * the given account ids.
   */
  public static void captureMembershipReplaceFromCurrentProvisioner(
      String targetGroupId, Collection<String> targetUserIds) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.recordTargetNativeMembershipReplace(targetGroupId, targetUserIds);
    }
  }

  /** Write-track a single successful membership add. */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
    }
  }

  /** Write-track a single successful membership remove. */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    JamfProvisioningTargetNativeSync jamfSync = jamfSyncForCurrentProvisioner();
    if (jamfSync != null) {
      jamfSync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
    }
  }

  private static JamfProvisioningTargetNativeSync jamfSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof JamfProvisioningTargetNativeSync) {
      return (JamfProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

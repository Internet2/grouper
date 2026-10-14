package edu.internet2.middleware.grouper.app.smartsheet;

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
 * Smartsheet-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back into the generic grouper_prov_group / grouper_prov_user / grouper_prov_mship
 * tables.
 *
 * <p>The mirror is the full view of Smartsheet. Capture reads the typed {@link SmartsheetUser} /
 * {@link SmartsheetGroup} beans at the {@link SmartsheetApiCommands} read seams, before any filtering
 * (ignored and deactivated users and ignored groups are mirrored too), and each group read captures
 * its full member list; {@link SmartsheetTargetDao} write-tracks membership adds / removes. When a
 * full sync is served from the cache, SmartsheetTargetDao's filterTarget*FromSyncBack overrides apply
 * the same rules a live read applies, so the mirror stays complete while the compare sees what a
 * live run would.</p>
 *
 * <p>The default captured attributes are every attribute the provisioner manages and compares, in
 * the same representation the DAO produces ("true" / "false" flags, normalized seatType), so a
 * cache-reconstructed user or group cannot cause a spurious update. isInternal is captured so a user
 * outside the org is still recognized (and never deactivated) when users come from the cache.</p>
 */
public class SmartsheetProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /** default captured attributes for a Smartsheet user (entity); id is already the target_user_id column */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("email"),
          attrConfig("firstName"),
          attrConfig("lastName"),
          attrConfig("status"),
          attrConfig("admin"),
          attrConfig("licensedSheetCreator"),
          attrConfig("groupAdmin"),
          attrConfig("resourceViewer"),
          attrConfig(SmartsheetUser.ATTR_SEAT_TYPE),
          attrConfig(SmartsheetUser.ATTR_IS_INTERNAL)));

  /** default captured attributes for a Smartsheet group; id is target_group_id */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("description"),
          attrConfig("owner")));

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
   * Build a native group bean from a SmartsheetGroup. Returns null if the group has no id.
   * @param group the group
   * @return the native group bean, or null
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroup(SmartsheetGroup group) {
    if (group == null || StringUtils.isBlank(group.getId())) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(group.getId());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsGroups()) {
      Object value = groupAttributeValue(group, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  /**
   * Build a native user bean from a SmartsheetUser. Returns null if the user has no id.
   * @param user the user
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUser(SmartsheetUser user) {
    if (user == null || StringUtils.isBlank(user.getId())) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(user.getId());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsEntities()) {
      Object value = entityAttributeValue(user, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  private static Object groupAttributeValue(SmartsheetGroup group, String name) {
    if ("name".equals(name)) {
      return group.getName();
    }
    if ("description".equals(name)) {
      return group.getDescription();
    }
    if ("owner".equals(name)) {
      return group.getOwner();
    }
    return null;
  }

  /**
   * The attribute value exactly as {@link SmartsheetUser#toProvisioningEntity()} produces it, so the
   * cache and a target read agree.
   */
  private static Object entityAttributeValue(SmartsheetUser user, String name) {
    if ("email".equals(name)) {
      return user.getEmail();
    }
    if ("firstName".equals(name)) {
      return user.getFirstName();
    }
    if ("lastName".equals(name)) {
      return user.getLastName();
    }
    if ("status".equals(name)) {
      return user.getStatus();
    }
    if ("admin".equals(name)) {
      return booleanString(user.getAdmin());
    }
    if ("licensedSheetCreator".equals(name)) {
      return booleanString(user.getLicensedSheetCreator());
    }
    if ("groupAdmin".equals(name)) {
      return booleanString(user.getGroupAdmin());
    }
    if ("resourceViewer".equals(name)) {
      return booleanString(user.getResourceViewer());
    }
    if (SmartsheetUser.ATTR_SEAT_TYPE.equals(name)) {
      return SmartsheetUser.normalizeSeatType(user.getSeatType());
    }
    if (SmartsheetUser.ATTR_IS_INTERNAL.equals(name)) {
      return booleanString(user.getInternal());
    }
    return null;
  }

  private static String booleanString(Boolean value) {
    return value == null ? null : value.toString();
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a group. No-op when sync-back is off or the group has no id. */
  public void captureGroup(SmartsheetGroup group) {
    this.recordTargetNativeGroup(this.buildNativeGroup(group));
  }

  /** Build + record a user. No-op when sync-back is off or the user has no id. */
  public void captureUser(SmartsheetUser user) {
    this.recordTargetNativeUser(this.buildNativeUser(user));
  }

  /**
   * Record (groupId, userId) memberships for a single group.
   * @param targetGroupId the group id
   * @param targetUserIds the member user ids
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

  // ----- static dispatchers (called from SmartsheetTargetDao) --------------------------

  /** Capture a group read against the current provisioner's sync. No-op outside a Smartsheet cycle. */
  public static void captureGroupFromCurrentProvisioner(SmartsheetGroup group) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureGroup(group);
    }
  }

  /** Capture a user read against the current provisioner's sync. No-op outside a Smartsheet cycle. */
  public static void captureUserFromCurrentProvisioner(SmartsheetUser user) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureUser(user);
    }
  }

  /** Record a group's full membership set (user ids) against the current sync. */
  public static void captureMembershipsForGroupFromCurrentProvisioner(String targetGroupId, Collection<String> targetUserIds) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureMembershipsForGroup(targetGroupId, targetUserIds);
    }
  }

  /**
   * Group write hook: after a create, record the created group (no re-read needed); after an update,
   * pass null so the end-of-run drain re-reads it.
   */
  public static void captureGroupWriteFromCurrentProvisioner(String targetGroupId, SmartsheetGroup writtenGroup) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeGroupWrite(targetGroupId, writtenGroup == null ? null : sync.buildNativeGroup(writtenGroup));
    }
  }

  /** User write hook: the drain re-reads the user (seat, reactivation and flags change server side). */
  public static void captureUserWriteFromCurrentProvisioner(String targetUserId) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeUserWrite(targetUserId, null);
    }
  }

  /** Write-track a single successful membership add. */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
    }
  }

  /** Write-track a single successful membership remove. */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    SmartsheetProvisioningTargetNativeSync sync = smartsheetSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
    }
  }

  private static SmartsheetProvisioningTargetNativeSync smartsheetSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof SmartsheetProvisioningTargetNativeSync) {
      return (SmartsheetProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

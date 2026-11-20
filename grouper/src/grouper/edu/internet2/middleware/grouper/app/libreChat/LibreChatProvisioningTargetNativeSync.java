package edu.internet2.middleware.grouper.app.libreChat;

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
 * LibreChat sync-back: builds native beans for the generic grouper_prov_user / grouper_prov_group /
 * grouper_prov_mship tables.
 *
 * <p>Users: every user read is mirrored.  Groups: every local group read is mirrored with its source and
 * its idOnTheSource exactly as LibreChat stores it (with the prefix), so the managed-group rule can be
 * applied to cached groups the same way as to a live read.  Memberships: the full member list of each
 * managed group read, plus each membership add and remove.  Captures happen at the
 * {@link LibreChatApiCommands} read seams and in {@link LibreChatTargetDao} for writes.</p>
 *
 * GRP-7595
 */
public class LibreChatProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /** captured user attributes (the id is the target_user_id column) */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(attrConfig("username"), attrConfig("name"), attrConfig("email")));

  /** captured group attributes (the id is the target_group_id column); idOnTheSource is the raw LibreChat value */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(attrConfig("name"), attrConfig("description"),
          attrConfig("source"), attrConfig("idOnTheSource")));

  /**
   * @param name attribute name
   * @return config
   */
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

  // ----- build (typed bean -> native bean) -----

  /**
   * @param group LibreChat group
   * @return native bean, or null if no id
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroup(LibreChatGroup group) {
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
   * @param user LibreChat user
   * @return native bean, or null if no id
   */
  public GrouperProvisioningTargetNativeUser buildNativeUser(LibreChatUser user) {
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

  /**
   * @param group group
   * @param name attribute
   * @return the value as LibreChat stores it
   */
  private static Object groupAttributeValue(LibreChatGroup group, String name) {
    if ("name".equals(name)) {
      return group.getName();
    }
    if ("description".equals(name)) {
      return group.getDescription();
    }
    if ("source".equals(name)) {
      return StringUtils.defaultIfBlank(group.getSource(), "local");
    }
    if ("idOnTheSource".equals(name)) {
      return group.getIdOnTheSource();
    }
    return null;
  }

  /**
   * @param user user
   * @param name attribute
   * @return the value as {@link LibreChatUser#toProvisioningEntity()} has it
   */
  private static Object entityAttributeValue(LibreChatUser user, String name) {
    if ("username".equals(name)) {
      return user.getUsername();
    }
    if ("name".equals(name)) {
      return user.getName();
    }
    if ("email".equals(name)) {
      return user.getEmail();
    }
    return null;
  }

  // ----- capture (build + record) -----

  /**
   * @param group group read
   */
  public void captureGroup(LibreChatGroup group) {
    this.recordTargetNativeGroup(this.buildNativeGroup(group));
  }

  /**
   * @param user user read
   */
  public void captureUser(LibreChatUser user) {
    this.recordTargetNativeUser(this.buildNativeUser(user));
  }

  /**
   * Record a group's full member list
   * @param targetGroupId group id
   * @param targetUserIds member user ids
   */
  public void captureMembershipsForGroup(String targetGroupId, Collection<String> targetUserIds) {
    if (targetGroupId == null || targetUserIds == null || targetUserIds.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships = new ArrayList<GrouperProvisioningTargetNativeMembership>();
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

  // ----- static dispatchers: no-ops outside a LibreChat provisioning run -----

  /** @param group group read */
  public static void captureGroupFromCurrentProvisioner(LibreChatGroup group) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureGroup(group);
    }
  }

  /** @param user user read */
  public static void captureUserFromCurrentProvisioner(LibreChatUser user) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureUser(user);
    }
  }

  /**
   * @param targetGroupId group id
   * @param targetUserIds the group's full member list
   */
  public static void captureMembershipsForGroupFromCurrentProvisioner(String targetGroupId, Collection<String> targetUserIds) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.captureMembershipsForGroup(targetGroupId, targetUserIds);
    }
  }

  /**
   * Group write: after a create pass the created group (no re-read needed); after an update pass null so
   * the end-of-run drain re-reads it
   * @param targetGroupId group id
   * @param writtenGroup created group or null
   */
  public static void captureGroupWriteFromCurrentProvisioner(String targetGroupId, LibreChatGroup writtenGroup) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeGroupWrite(targetGroupId, writtenGroup == null ? null : sync.buildNativeGroup(writtenGroup));
    }
  }

  /**
   * Group delete: drop it from the mirror at the end of the run
   * @param targetGroupId group id
   */
  public static void captureGroupDeleteFromCurrentProvisioner(String targetGroupId) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.removeTargetNativeGroup(targetGroupId);
      sync.markSyncBackGroupForDelete(targetGroupId);
    }
  }

  /**
   * @param targetGroupId group id
   * @param targetUserId user id added
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
    }
  }

  /**
   * @param targetGroupId group id
   * @param targetUserId user id removed
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    LibreChatProvisioningTargetNativeSync sync = libreChatSyncForCurrentProvisioner();
    if (sync != null) {
      sync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
    }
  }

  /**
   * @return the current provisioner's LibreChat sync, or null outside a LibreChat run
   */
  private static LibreChatProvisioningTargetNativeSync libreChatSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof LibreChatProvisioningTargetNativeSync) {
      return (LibreChatProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

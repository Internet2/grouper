package edu.internet2.middleware.grouper.app.duo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Duo-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperDuoUser} / {@link GrouperDuoGroup} typed beans
 * returned by {@code GrouperDuoApiCommands}.
 *
 * <p>Like Adobe (the reference), Duo target objects are typed Java beans, so attribute
 * capture is a small switch on attribute name -> bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored for Duo; only {@code name}
 * is meaningful.
 *
 * <p>Capture is hooked at the DAO level because the DAO is where Duo responses are
 * already converted to typed beans. Memberships come back inline with users
 * ({@link GrouperDuoUser#getGroups()} returns {@code Set<GrouperDuoGroup>}, each with its
 * own {@code group_id}), so no name->id index resolution is needed.
 */
public class GrouperDuoProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Duo users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userName"),
          attrConfig("email"),
          attrConfig("status")));

  /**
   * Default per-attribute capture list for Duo groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code group_id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name")));

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

  // ----- build (typed bean -> native-reporting bean) -----------------------------------

  /** Build a native group bean from a Duo group. {@code targetId} is the Duo group_id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromDuoGroup(GrouperDuoGroup grouperDuoGroup) {
    if (grouperDuoGroup == null || grouperDuoGroup.getGroup_id() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperDuoGroup.getGroup_id());
    populateGroupAttributes(bean.getAttributes(), grouperDuoGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a Duo user. {@code targetId} is the Duo user id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromDuoUser(GrouperDuoUser grouperDuoUser) {
    if (grouperDuoUser == null || grouperDuoUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperDuoUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperDuoUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperDuoGroup grouperDuoGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperDuoGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperDuoUser grouperDuoUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperDuoUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a Duo group bean. Unknown attribute names return
   * null (silently skipped). Bean property names map directly: "name" -> getName(), etc.
   */
  private static Object resolveGroupAttribute(GrouperDuoGroup grouperDuoGroup, String attributeName) {
    if (grouperDuoGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name": return grouperDuoGroup.getName();
      case "desc": return grouperDuoGroup.getDesc();
      default:     return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperDuoUser grouperDuoUser, String attributeName) {
    if (grouperDuoUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "userName":          return grouperDuoUser.getUserName();
      case "email":             return grouperDuoUser.getEmail();
      case "firstName":         return grouperDuoUser.getFirstName();
      case "lastName":          return grouperDuoUser.getLastName();
      case "realName":          return grouperDuoUser.getRealName();
      case "status":            return grouperDuoUser.getStatus();
      case "alias1":            return grouperDuoUser.getAlias1();
      case "alias2":            return grouperDuoUser.getAlias2();
      case "alias3":            return grouperDuoUser.getAlias3();
      case "alias4":            return grouperDuoUser.getAlias4();
      case "phones":            return grouperDuoUser.getPhones();
      case "pushEnabled":       return grouperDuoUser.getPushEnabled();
      case "aliases":           return grouperDuoUser.getAliases();
      case "enrolled":          return grouperDuoUser.getEnrolled();
      case "lastDirectorySync": return grouperDuoUser.getLastDirectorySync();
      case "notes":             return grouperDuoUser.getNotes();
      case "createdAt":         return grouperDuoUser.getCreatedAt();
      case "lastLogin":         return grouperDuoUser.getLastLogin();
      default:                  return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Duo group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperDuoGroup grouperDuoGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromDuoGroup(grouperDuoGroup));
  }

  /** Build + record a Duo user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperDuoUser grouperDuoUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromDuoUser(grouperDuoUser));
  }

  /**
   * Translate a Duo user's inline {@code groups} set ({@link GrouperDuoGroup} beans with
   * their own {@code group_id}) into native membership beans and record them. No-op if
   * reporting is off or the user has no groups.
   */
  public void captureMembershipsFromUser(GrouperDuoUser grouperDuoUser) {
    if (grouperDuoUser == null || grouperDuoUser.getId() == null) {
      return;
    }
    Set<GrouperDuoGroup> userGroups = grouperDuoUser.getGroups();
    if (userGroups == null || userGroups.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (GrouperDuoGroup duoGroup : userGroups) {
      if (duoGroup == null || duoGroup.getGroup_id() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(duoGroup.getGroup_id());
      membership.setTargetUserId(grouperDuoUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperDuoTargetDao) --------------------------

  /**
   * Capture a Duo group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't a Duo one.
   */
  public static void captureGroupFromCurrentProvisioner(GrouperDuoGroup grouperDuoGroup) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureGroup(grouperDuoGroup);
  }

  /** Capture a Duo user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperDuoUser grouperDuoUser) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureUser(grouperDuoUser);
  }

  /**
   * Translate a Duo user's inline {@code groups} set into native membership records and
   * record them on the current provisioner.
   */
  public static void captureMembershipsFromUserForCurrentProvisioner(GrouperDuoUser grouperDuoUser) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureMembershipsFromUser(grouperDuoUser);
  }

  private static GrouperDuoProvisioningTargetNativeSync duoSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperDuoProvisioningTargetNativeSync) {
      return (GrouperDuoProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

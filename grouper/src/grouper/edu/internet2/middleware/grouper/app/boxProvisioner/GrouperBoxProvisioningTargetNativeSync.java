package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Box-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperBoxUser} / {@link GrouperBoxGroup} typed beans
 * returned by {@code GrouperBoxApiCommands}.
 *
 * <p>Unlike SCIM (JSON Pointer paths) or LDAP (raw entry attributes), Box target objects
 * are typed Java beans, so attribute capture is a small switch on attribute name -> bean
 * getter. The {@code path} field on {@link GrouperProvisioningNativeAttributeConfig} is
 * ignored for Box; only {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because
 * the DAO is where Box responses are already converted to typed beans, and the seam is
 * a single function call shorter.
 */
public class GrouperBoxProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Box users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and large
   * / unstable fields.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("login"),
          attrConfig("role"),
          attrConfig("status"),
          attrConfig("type")));

  /**
   * Default per-attribute capture list for Box groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("groupType"),
          attrConfig("provenance")));

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

  // ----- build (typed bean -> native-reporting bean) ------------------------------------

  /** Build a native group bean from a Box group. {@code targetId} is the Box id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromBoxGroup(GrouperBoxGroup grouperBoxGroup) {
    if (grouperBoxGroup == null || grouperBoxGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperBoxGroup.getId());
    populateGroupAttributes(bean.getAttributes(), grouperBoxGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a Box user. {@code targetId} is the Box id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromBoxUser(GrouperBoxUser grouperBoxUser) {
    if (grouperBoxUser == null || grouperBoxUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperBoxUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperBoxUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperBoxGroup grouperBoxGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperBoxGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperBoxUser grouperBoxUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperBoxUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a Box group bean. Unknown attribute names return null
   * (silently skipped — validation already catches bad config). Bean property names map
   * directly: "name" -> getName(), etc.
   */
  private static Object resolveGroupAttribute(GrouperBoxGroup grouperBoxGroup, String attributeName) {
    if (grouperBoxGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":                     return grouperBoxGroup.getName();
      case "type":                     return grouperBoxGroup.getType();
      case "description":              return grouperBoxGroup.getDescription();
      case "externalSyncIdentifier":   return grouperBoxGroup.getExternalSyncIdentifier();
      case "groupType":                return grouperBoxGroup.getGroupType();
      case "invitabilityLevel":        return grouperBoxGroup.getInvitabilityLevel();
      case "memberViewabilityLevel":   return grouperBoxGroup.getMemberViewabilityLevel();
      case "provenance":               return grouperBoxGroup.getProvenance();
      case "canInviteAsCollaborator":  return grouperBoxGroup.isCanInviteAsCollaborator();
      case "createdAt":                return grouperBoxGroup.getCreatedAt();
      case "modifiedAt":               return grouperBoxGroup.getModifiedAt();
      default:                         return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperBoxUser grouperBoxUser, String attributeName) {
    if (grouperBoxUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "type":                          return grouperBoxUser.getType();
      case "role":                          return grouperBoxUser.getRole();
      case "maxUploadSize":                 return grouperBoxUser.getMaxUploadSize();
      case "spaceAmount":                   return grouperBoxUser.getSpaceAmount();
      case "isExemptFromDeviceLimits":      return grouperBoxUser.isExemptFromDeviceLimits();
      case "isExemptFromLoginVerification": return grouperBoxUser.isExemptFromLoginVerification();
      case "isExternalCollabRestricted":    return grouperBoxUser.isExternalCollabRestricted();
      case "isPlatformAccessOnly":          return grouperBoxUser.isPlatformAccessOnly();
      case "isSyncEnabled":                 return grouperBoxUser.isSyncEnabled();
      case "canSeeManagedUsers":            return grouperBoxUser.isCanSeeManagedUsers();
      case "createdAt":                     return grouperBoxUser.getCreatedAt();
      case "modifiedAt":                    return grouperBoxUser.getModifiedAt();
      case "login":                         return grouperBoxUser.getLogin();
      case "name":                          return grouperBoxUser.getName();
      case "spaceUsed":                     return grouperBoxUser.getSpaceUsed();
      case "status":                        return grouperBoxUser.getStatus();
      default:                              return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Box group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperBoxGroup grouperBoxGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromBoxGroup(grouperBoxGroup));
  }

  /** Build + record a Box user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperBoxUser grouperBoxUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromBoxUser(grouperBoxUser));
  }

  /**
   * Record the (targetGroupId, targetUserId) pairs as native membership beans.
   * No-op if input is empty.
   */
  public void captureMemberships(String targetGroupId, Iterable<String> targetUserIds) {
    if (targetGroupId == null || targetUserIds == null) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String targetUserId : targetUserIds) {
      if (targetUserId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(targetUserId);
      memberships.add(membership);
    }
    if (!memberships.isEmpty()) {
      this.recordTargetNativeMemberships(memberships);
    }
  }

  // ----- static dispatchers (called from GrouperBoxTargetDao) ---------------------------

  /**
   * Capture a Box group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't a Box one.
   */
  public static void captureGroupFromCurrentProvisioner(GrouperBoxGroup grouperBoxGroup) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureGroup(grouperBoxGroup);
  }

  /** Capture a Box user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperBoxUser grouperBoxUser) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureUser(grouperBoxUser);
  }

  /**
   * Record the (targetGroupId, targetUserId) pairs as native membership beans on the
   * current provisioner's sync.
   */
  public static void captureMembershipsFromCurrentProvisioner(String targetGroupId, Iterable<String> targetUserIds) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureMemberships(targetGroupId, targetUserIds);
  }

  private static GrouperBoxProvisioningTargetNativeSync boxSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperBoxProvisioningTargetNativeSync) {
      return (GrouperBoxProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

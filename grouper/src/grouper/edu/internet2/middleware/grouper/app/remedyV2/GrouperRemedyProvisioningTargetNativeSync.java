package edu.internet2.middleware.grouper.app.remedyV2;

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
 * Remedy-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperRemedyUser} / {@link GrouperRemedyGroup} /
 * {@link GrouperRemedyMembership} typed beans returned by {@code GrouperRemedyApiCommands}.
 *
 * <p>Remedy beans are typed Java beans (not JSON), so attribute capture is a small switch
 * on attribute name to bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored for Remedy; only {@code name}
 * is meaningful.
 *
 * <p>Capture is hooked at the DAO level (where Remedy responses are already converted to
 * typed beans).
 */
public class GrouperRemedyProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Remedy users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code personId} (already the target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("remedyLoginId")));

  /**
   * Default per-attribute capture list for Remedy groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code permissionGroupId} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("permissionGroup")));

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

  // ----- build (typed bean -> native-reporting bean) ----------------------------------

  /** Build a native group bean from a Remedy group. {@code targetId} is permissionGroupId. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromRemedyGroup(GrouperRemedyGroup grouperRemedyGroup) {
    if (grouperRemedyGroup == null || grouperRemedyGroup.getPermissionGroupId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperRemedyGroup.getPermissionGroupId().toString());
    populateGroupAttributes(bean.getAttributes(), grouperRemedyGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a Remedy user. {@code targetId} is personId. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromRemedyUser(GrouperRemedyUser grouperRemedyUser) {
    if (grouperRemedyUser == null || grouperRemedyUser.getPersonId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperRemedyUser.getPersonId());
    populateUserAttributes(bean.getAttributes(), grouperRemedyUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * Build a native membership bean from a Remedy membership. targetGroupId =
   * permissionGroupId, targetUserId = personId.
   */
  public GrouperProvisioningTargetNativeMembership buildNativeMembershipFromRemedyMembership(
      GrouperRemedyMembership grouperRemedyMembership) {
    if (grouperRemedyMembership == null
        || grouperRemedyMembership.getPermissionGroupId() == null
        || grouperRemedyMembership.getPersonId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeMembership bean = new GrouperProvisioningTargetNativeMembership();
    bean.setTargetGroupId(grouperRemedyMembership.getPermissionGroupId().toString());
    bean.setTargetUserId(grouperRemedyMembership.getPersonId());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperRemedyGroup grouperRemedyGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperRemedyGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperRemedyUser grouperRemedyUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperRemedyUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a Remedy group bean. Unknown attribute names return
   * null (silently skipped — validation already catches bad config).
   */
  private static Object resolveGroupAttribute(GrouperRemedyGroup grouperRemedyGroup, String attributeName) {
    if (grouperRemedyGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "permissionGroup":   return grouperRemedyGroup.getPermissionGroup();
      case "permissionGroupId": return grouperRemedyGroup.getPermissionGroupId();
      default:                  return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperRemedyUser grouperRemedyUser, String attributeName) {
    if (grouperRemedyUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "personId":      return grouperRemedyUser.getPersonId();
      case "remedyLoginId": return grouperRemedyUser.getRemedyLoginId();
      default:              return null;
    }
  }

  // ----- capture convenience (build + record) -----------------------------------------

  /** Build + record a Remedy group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperRemedyGroup grouperRemedyGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromRemedyGroup(grouperRemedyGroup));
  }

  /** Build + record a Remedy user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperRemedyUser grouperRemedyUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromRemedyUser(grouperRemedyUser));
  }

  /** Build + record a list of Remedy memberships. No-op when sync-back is off or empty. */
  public void captureMemberships(List<GrouperRemedyMembership> grouperRemedyMemberships) {
    if (grouperRemedyMemberships == null || grouperRemedyMemberships.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyMemberships) {
      GrouperProvisioningTargetNativeMembership bean =
          this.buildNativeMembershipFromRemedyMembership(grouperRemedyMembership);
      if (bean != null) {
        memberships.add(bean);
      }
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperRemedyTargetDao) ----------------------

  /** Capture a Remedy group against the current provisioner's sync. */
  public static void captureGroupFromCurrentProvisioner(GrouperRemedyGroup grouperRemedyGroup) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureGroup(grouperRemedyGroup);
  }

  /** Capture a Remedy user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperRemedyUser grouperRemedyUser) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureUser(grouperRemedyUser);
  }

  /** Capture a list of Remedy memberships against the current provisioner's sync. */
  public static void captureMembershipsFromCurrentProvisioner(
      List<GrouperRemedyMembership> grouperRemedyMemberships) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureMemberships(grouperRemedyMemberships);
  }

  private static GrouperRemedyProvisioningTargetNativeSync remedySyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperRemedyProvisioningTargetNativeSync) {
      return (GrouperRemedyProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

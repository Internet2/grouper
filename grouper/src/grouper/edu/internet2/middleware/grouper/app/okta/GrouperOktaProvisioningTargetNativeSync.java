package edu.internet2.middleware.grouper.app.okta;

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
 * Okta-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperOktaUser} / {@link GrouperOktaGroup} typed beans
 * returned by {@code GrouperOktaApiCommands}.
 *
 * <p>Like Adobe, Okta target objects are typed Java beans, so attribute capture is a small
 * switch on attribute name &rarr; bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored for Okta; only {@code name}
 * is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because
 * the DAO is where Okta responses are already converted to typed beans.
 */
public class GrouperOktaProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Okta users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("login"),
          attrConfig("email")));

  /**
   * Default per-attribute capture list for Okta groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
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

  // ----- build (typed bean -> native-reporting bean) ----------------------------------

  /** Build a native group bean from an Okta group. {@code targetId} is the Okta id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromOktaGroup(GrouperOktaGroup grouperOktaGroup) {
    if (grouperOktaGroup == null || grouperOktaGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperOktaGroup.getId());
    populateGroupAttributes(bean.getAttributes(), grouperOktaGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from an Okta user. {@code targetId} is the Okta id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromOktaUser(GrouperOktaUser grouperOktaUser) {
    if (grouperOktaUser == null || grouperOktaUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperOktaUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperOktaUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperOktaGroup grouperOktaGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperOktaGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperOktaUser grouperOktaUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperOktaUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from an Okta group bean. Unknown attribute names return
   * null (silently skipped &mdash; validation already catches bad config).
   */
  private static Object resolveGroupAttribute(GrouperOktaGroup grouperOktaGroup, String attributeName) {
    if (grouperOktaGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":        return grouperOktaGroup.getName();
      case "description": return grouperOktaGroup.getDescription();
      default:            return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperOktaUser grouperOktaUser, String attributeName) {
    if (grouperOktaUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "login":     return grouperOktaUser.getLogin();
      case "email":     return grouperOktaUser.getEmail();
      case "firstName": return grouperOktaUser.getFirstName();
      case "lastName":  return grouperOktaUser.getLastName();
      default:          return null;
    }
  }

  // ----- capture convenience (build + record) -----------------------------------------

  /** Build + record an Okta group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperOktaGroup grouperOktaGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromOktaGroup(grouperOktaGroup));
  }

  /** Build + record an Okta user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperOktaUser grouperOktaUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromOktaUser(grouperOktaUser));
  }

  /**
   * Build native membership beans for all member user ids in the supplied group, and record
   * them. No-op if reporting is off or the input is empty.
   */
  public void captureMembershipsForGroup(String targetGroupId, Iterable<String> targetUserIds) {
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
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperOktaTargetDao) ------------------------

  /**
   * Capture an Okta group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't an Okta one.
   */
  public static void captureGroupFromCurrentProvisioner(GrouperOktaGroup grouperOktaGroup) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureGroup(grouperOktaGroup);
  }

  /** Capture an Okta user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperOktaUser grouperOktaUser) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureUser(grouperOktaUser);
  }

  /**
   * Record memberships for a given target group id against the current provisioner's sync.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, Iterable<String> targetUserIds) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureMembershipsForGroup(targetGroupId, targetUserIds);
  }

  private static GrouperOktaProvisioningTargetNativeSync oktaSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperOktaProvisioningTargetNativeSync) {
      return (GrouperOktaProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

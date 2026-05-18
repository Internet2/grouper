package edu.internet2.middleware.grouper.app.teamDynamix;

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
 * TeamDynamix-specific {@link GrouperProvisioningTargetNativeSync}: builds native target
 * reporting beans directly from the {@link TeamDynamixUser} / {@link TeamDynamixGroup} typed
 * beans returned by {@code TeamDynamixApiCommands}.
 *
 * <p>TeamDynamix target objects are typed Java beans (like Adobe), so attribute capture is
 * a small switch on attribute name to bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored for TeamDynamix; only
 * {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level: native users / groups are captured as they are
 * converted from the API response, and memberships are captured at the membership-retrieve
 * seam (memberships are retrieved per-group, not inline with users).
 */
public class TeamDynamixProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for TeamDynamix users when
   * {@code nativeAttributesEntities} is not configured. Excludes {@code id} (already the
   * target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userName"),
          attrConfig("primaryEmail"),
          attrConfig("active")));

  /**
   * Default per-attribute capture list for TeamDynamix groups when
   * {@code nativeAttributesGroups} is not configured. Excludes {@code id} (already
   * target_group_id).
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

  // ----- build (typed bean to native-reporting bean) -----------------------------------

  /** Build a native group bean from a TeamDynamix group. {@code targetId} is the TD id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromTeamDynamixGroup(TeamDynamixGroup teamDynamixGroup) {
    if (teamDynamixGroup == null || teamDynamixGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(teamDynamixGroup.getId());
    populateGroupAttributes(bean.getAttributes(), teamDynamixGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a TeamDynamix user. {@code targetId} is the TD id (UID). */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromTeamDynamixUser(TeamDynamixUser teamDynamixUser) {
    if (teamDynamixUser == null || teamDynamixUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(teamDynamixUser.getId());
    populateUserAttributes(bean.getAttributes(), teamDynamixUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      TeamDynamixGroup teamDynamixGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(teamDynamixGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      TeamDynamixUser teamDynamixUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(teamDynamixUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a TeamDynamix group bean. Unknown attribute names return
   * null (silently skipped; validation already catches bad config).
   */
  private static Object resolveGroupAttribute(TeamDynamixGroup teamDynamixGroup, String attributeName) {
    if (teamDynamixGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":        return teamDynamixGroup.getName();
      case "description": return teamDynamixGroup.getDescription();
      default:            return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(TeamDynamixUser teamDynamixUser, String attributeName) {
    if (teamDynamixUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "userName":       return teamDynamixUser.getUserName();
      case "firstName":      return teamDynamixUser.getFirstName();
      case "lastName":       return teamDynamixUser.getLastName();
      case "primaryEmail":   return teamDynamixUser.getPrimaryEmail();
      case "company":        return teamDynamixUser.getCompany();
      case "securityRoleId": return teamDynamixUser.getSecurityRoleId();
      case "externalId":     return teamDynamixUser.getExternalId();
      case "active":         return teamDynamixUser.getActive();
      default:               return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a TeamDynamix group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(TeamDynamixGroup teamDynamixGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromTeamDynamixGroup(teamDynamixGroup));
  }

  /** Build + record a TeamDynamix user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(TeamDynamixUser teamDynamixUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromTeamDynamixUser(teamDynamixUser));
  }

  /**
   * Record native memberships for a TeamDynamix group, given the list of user ids that
   * belong to it. No-op if reporting is off or the input is empty.
   */
  public void captureMembershipsForGroup(String targetGroupId, List<TeamDynamixUser> teamDynamixUsers) {
    if (targetGroupId == null || teamDynamixUsers == null || teamDynamixUsers.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {
      if (teamDynamixUser == null || teamDynamixUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(teamDynamixUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from TeamDynamixTargetDao) --------------------------

  /**
   * Capture a TeamDynamix group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't a TeamDynamix one.
   */
  public static void captureGroupFromCurrentProvisioner(TeamDynamixGroup teamDynamixGroup) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroup(teamDynamixGroup);
  }

  /** Capture a TeamDynamix user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(TeamDynamixUser teamDynamixUser) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUser(teamDynamixUser);
  }

  /**
   * Record native memberships for a TeamDynamix group on the current provisioner.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, List<TeamDynamixUser> teamDynamixUsers) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMembershipsForGroup(targetGroupId, teamDynamixUsers);
  }

  private static TeamDynamixProvisioningTargetNativeSync teamDynamixSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof TeamDynamixProvisioningTargetNativeSync) {
      return (TeamDynamixProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

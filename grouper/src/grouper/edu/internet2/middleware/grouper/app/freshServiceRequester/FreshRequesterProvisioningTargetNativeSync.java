package edu.internet2.middleware.grouper.app.freshServiceRequester;

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
 * FreshServiceRequester-specific {@link GrouperProvisioningTargetNativeSync}: builds native
 * target reporting beans directly from the {@link FreshRequesterUser} / {@link FreshRequesterGroup}
 * typed beans returned by {@code FreshRequesterApiCommands}.
 *
 * <p>FreshServiceRequester target objects are typed Java beans (like Adobe), so attribute
 * capture is a small switch on attribute name to bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored; only {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level since the DAO is where FreshRequester responses are
 * already converted to typed beans.
 */
public class FreshRequesterProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for FreshRequester users when
   * {@code nativeAttributesEntities} is not configured. Excludes {@code id} (already the
   * target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("email"),
          attrConfig("active")));

  /**
   * Default per-attribute capture list for FreshRequester groups when
   * {@code nativeAttributesGroups} is not configured. Excludes {@code id} (already target_group_id).
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

  // ----- build (typed bean to native-reporting bean) ----------------------------------

  /** Build a native group bean from a FreshRequester group. {@code targetId} is the id as a string. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromFreshRequesterGroup(FreshRequesterGroup freshRequesterGroup) {
    if (freshRequesterGroup == null || freshRequesterGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(String.valueOf(freshRequesterGroup.getId()));
    populateGroupAttributes(bean.getAttributes(), freshRequesterGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a FreshRequester user. {@code targetId} is the id as a string. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromFreshRequesterUser(FreshRequesterUser freshRequesterUser) {
    if (freshRequesterUser == null || freshRequesterUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(String.valueOf(freshRequesterUser.getId()));
    populateUserAttributes(bean.getAttributes(), freshRequesterUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      FreshRequesterGroup freshRequesterGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(freshRequesterGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      FreshRequesterUser freshRequesterUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(freshRequesterUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a FreshRequester group bean. Unknown attribute names return
   * null (silently skipped — validation already catches bad config). Bean property names map
   * directly: "name" to getName(), etc.
   */
  private static Object resolveGroupAttribute(FreshRequesterGroup freshRequesterGroup, String attributeName) {
    if (freshRequesterGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":        return freshRequesterGroup.getName();
      case "description": return freshRequesterGroup.getDescription();
      default:            return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(FreshRequesterUser freshRequesterUser, String attributeName) {
    if (freshRequesterUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "email":              return freshRequesterUser.getEmail();
      case "firstName":          return freshRequesterUser.getFirstName();
      case "lastName":           return freshRequesterUser.getLastName();
      case "isAgent":            return freshRequesterUser.getIsAgent();
      case "jobTitle":           return freshRequesterUser.getJobTitle();
      case "workPhoneNumber":    return freshRequesterUser.getWorkPhoneNumber();
      case "departmentId":       return freshRequesterUser.getDepartmentId();
      case "reportingManagerId": return freshRequesterUser.getReportingManagerId();
      case "address":            return freshRequesterUser.getAddress();
      case "externalId":         return freshRequesterUser.getExternalId();
      case "active":             return freshRequesterUser.getActive();
      default:                   return null;
    }
  }

  // ----- capture convenience (build + record) -----------------------------------------

  /** Build + record a FreshRequester group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(FreshRequesterGroup freshRequesterGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromFreshRequesterGroup(freshRequesterGroup));
  }

  /** Build + record a FreshRequester user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(FreshRequesterUser freshRequesterUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromFreshRequesterUser(freshRequesterUser));
  }

  /**
   * Translate a list of FreshRequester users known to be members of {@code targetGroupId} into
   * native membership beans, and record them. No-op when sync-back is off or input is empty.
   */
  public void captureMembershipsForGroup(String targetGroupId, List<FreshRequesterUser> members) {
    if (targetGroupId == null || members == null || members.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (FreshRequesterUser freshRequesterUser : members) {
      if (freshRequesterUser == null || freshRequesterUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(String.valueOf(freshRequesterUser.getId()));
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from FreshRequesterTargetDao) ----------------------

  /**
   * Capture a FreshRequester group against the current provisioner's sync. No-op if there's
   * no current provisioner or the active provisioner isn't a FreshRequester one.
   */
  public static void captureGroupFromCurrentProvisioner(FreshRequesterGroup freshRequesterGroup) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroup(freshRequesterGroup);
  }

  /** Capture a FreshRequester user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(FreshRequesterUser freshRequesterUser) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUser(freshRequesterUser);
  }

  /**
   * Capture memberships for the given target group id against the current provisioner's sync.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, List<FreshRequesterUser> members) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMembershipsForGroup(targetGroupId, members);
  }

  private static FreshRequesterProvisioningTargetNativeSync freshRequesterSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof FreshRequesterProvisioningTargetNativeSync) {
      return (FreshRequesterProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

package edu.internet2.middleware.grouper.app.datadog;

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
 * Datadog-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link DatadogUser} / {@link DatadogGroup} typed beans returned by
 * {@code DatadogApiCommands}.
 *
 * <p>Unlike SCIM (JSON Pointer paths) or LDAP (raw entry attributes), Datadog target objects
 * are typed Java beans, so attribute capture is a small switch on attribute name -&gt; bean
 * getter. The {@code path} field on {@link GrouperProvisioningNativeAttributeConfig} is
 * ignored for Datadog; only {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because
 * the DAO is where Datadog responses are already converted to typed beans.
 */
public class DatadogProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Datadog users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and unstable
   * fields.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("handle"),
          attrConfig("email"),
          attrConfig("disabled")));

  /**
   * Default per-attribute capture list for Datadog groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("handle"),
          attrConfig("groupType")));

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

  /** Build a native group bean from a Datadog group. {@code targetId} is the Datadog id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromDatadogGroup(DatadogGroup datadogGroup) {
    if (datadogGroup == null || datadogGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(datadogGroup.getId());
    populateGroupAttributes(bean.getAttributes(), datadogGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a Datadog user. {@code targetId} is the Datadog id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromDatadogUser(DatadogUser datadogUser) {
    if (datadogUser == null || datadogUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(datadogUser.getId());
    populateUserAttributes(bean.getAttributes(), datadogUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      DatadogGroup datadogGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(datadogGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      DatadogUser datadogUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(datadogUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a Datadog group bean. Unknown attribute names return null
   * (silently skipped; validation already catches bad config).
   */
  private static Object resolveGroupAttribute(DatadogGroup datadogGroup, String attributeName) {
    if (datadogGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":        return datadogGroup.getName();
      case "handle":      return datadogGroup.getHandle();
      case "description": return datadogGroup.getDescription();
      case "groupType":   return datadogGroup.getGroupType();
      default:            return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(DatadogUser datadogUser, String attributeName) {
    if (datadogUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "email":          return datadogUser.getEmail();
      case "name":           return datadogUser.getName();
      case "title":          return datadogUser.getTitle();
      case "handle":         return datadogUser.getHandle();
      case "disabled":       return datadogUser.getDisabled();
      case "serviceAccount": return datadogUser.getServiceAccount();
      default:               return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Datadog group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(DatadogGroup datadogGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromDatadogGroup(datadogGroup));
  }

  /** Build + record a Datadog user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(DatadogUser datadogUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromDatadogUser(datadogUser));
  }

  /**
   * Translate a list of Datadog memberships for a given target group id into native membership
   * beans and record them. No-op if reporting is off or input is empty. Used for team
   * memberships (DatadogMembership carries the userId).
   */
  public void captureTeamMemberships(String targetGroupId, List<DatadogMembership> datadogMemberships) {
    if (targetGroupId == null || datadogMemberships == null || datadogMemberships.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (DatadogMembership datadogMembership : datadogMemberships) {
      if (datadogMembership == null || datadogMembership.getUserId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(datadogMembership.getUserId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  /**
   * Translate a list of Datadog users (role members) for a given target group id into native
   * membership beans and record them. Used for role memberships (the Datadog API returns the
   * user list directly).
   */
  public void captureRoleMemberships(String targetGroupId, List<DatadogUser> roleUsers) {
    if (targetGroupId == null || roleUsers == null || roleUsers.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (DatadogUser datadogUser : roleUsers) {
      if (datadogUser == null || datadogUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(datadogUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from DatadogTargetDao) ------------------------------

  /**
   * Capture a Datadog group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't a Datadog one.
   */
  public static void captureGroupFromCurrentProvisioner(DatadogGroup datadogGroup) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroup(datadogGroup);
  }

  /** Capture a Datadog user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(DatadogUser datadogUser) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUser(datadogUser);
  }

  /** Capture team memberships against the current provisioner's sync. */
  public static void captureTeamMembershipsFromCurrentProvisioner(
      String targetGroupId, List<DatadogMembership> datadogMemberships) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureTeamMemberships(targetGroupId, datadogMemberships);
  }

  /** Capture role memberships against the current provisioner's sync. */
  public static void captureRoleMembershipsFromCurrentProvisioner(
      String targetGroupId, List<DatadogUser> roleUsers) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureRoleMemberships(targetGroupId, roleUsers);
  }

  private static DatadogProvisioningTargetNativeSync datadogSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof DatadogProvisioningTargetNativeSync) {
      return (DatadogProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

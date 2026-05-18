package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * Azure-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperAzureUser} / {@link GrouperAzureGroup} typed beans
 * returned by {@code GrouperAzureApiCommands}.
 *
 * <p>Like Adobe, Azure objects are typed Java beans so attribute capture is a switch on
 * attribute name to bean getter. The {@code path} field on {@link GrouperProvisioningNativeAttributeConfig}
 * is ignored for Azure; only {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because
 * the DAO is where Azure responses are already converted to typed beans.
 */
public class GrouperAzureProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Azure users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and
   * security-sensitive fields like {@code password}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userPrincipalName"),
          attrConfig("mail"),
          attrConfig("mailNickname"),
          attrConfig("userType")));

  /**
   * Default per-attribute capture list for Azure groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("displayName"),
          attrConfig("mailNickname")));

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

  /** Build a native group bean from an Azure group. {@code targetId} is the Azure id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromAzureGroup(GrouperAzureGroup grouperAzureGroup) {
    if (grouperAzureGroup == null || grouperAzureGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperAzureGroup.getId());
    populateGroupAttributes(bean.getAttributes(), grouperAzureGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from an Azure user. {@code targetId} is the Azure id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromAzureUser(GrouperAzureUser grouperAzureUser) {
    if (grouperAzureUser == null || grouperAzureUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperAzureUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperAzureUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperAzureGroup grouperAzureGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperAzureGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperAzureUser grouperAzureUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperAzureUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from an Azure group bean. Unknown attribute names return
   * null. {@code visibility} is an enum and is captured as its {@code name()} string.
   */
  private static Object resolveGroupAttribute(GrouperAzureGroup grouperAzureGroup, String attributeName) {
    if (grouperAzureGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "displayName":   return grouperAzureGroup.getDisplayName();
      case "mailNickname":  return grouperAzureGroup.getMailNickname();
      case "description":   return grouperAzureGroup.getDescription();
      case "visibility":    return grouperAzureGroup.getVisibility() == null ? null : grouperAzureGroup.getVisibility().name();
      case "mailEnabled":             return grouperAzureGroup.getMailEnabledDb();
      case "securityEnabled":         return grouperAzureGroup.getSecurityEnabledDb();
      case "groupTypeUnified":        return grouperAzureGroup.getGroupTypeUnifiedDb();
      case "groupTypeDynamic":        return grouperAzureGroup.getGroupTypeDynamicDb();
      case "isAssignableToRole":      return grouperAzureGroup.getAssignableToRoleDb();
      case "resourceProvisioningOptionsTeam":                        return grouperAzureGroup.getResourceProvisioningOptionsTeamDb();
      case "resourceBehaviorOptionsAllowOnlyMembersToPost":          return grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb();
      case "resourceBehaviorOptionsHideGroupInOutlook":              return grouperAzureGroup.getResourceBehaviorOptionsHideGroupInOutlookDb();
      case "resourceBehaviorOptionsSubscribeNewGroupMembers":        return grouperAzureGroup.getResourceBehaviorOptionsSubscribeNewGroupMembersDb();
      case "resourceBehaviorOptionsWelcomeEmailDisabled":            return grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb();
      case "resourceBehaviorOptionsSubscribeMembersToCalendarEventsDisabled":
                                                                     return grouperAzureGroup.getResourceBehaviorOptionsSubscribeMembersToCalendarEventsDisabledDb();
      default:              return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperAzureUser grouperAzureUser, String attributeName) {
    if (grouperAzureUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "displayName":               return grouperAzureUser.getDisplayName();
      case "mailNickname":              return grouperAzureUser.getMailNickname();
      case "userPrincipalName":         return grouperAzureUser.getUserPrincipalName();
      case "mail":                      return grouperAzureUser.getMail();
      case "userType":                  return grouperAzureUser.getUserType();
      case "onPremisesImmutableId":     return grouperAzureUser.getOnPremisesImmutableId();
      case "onPremisesSamAccountName":  return grouperAzureUser.getOnPremisesSamAccountName();
      case "onPremisesLastSyncDateTime":return grouperAzureUser.getOnPremisesLastSyncDateTime();
      case "proxyAddresses":            return grouperAzureUser.getProxyAddresses();
      case "showInAddressList":         return grouperAzureUser.getShowInAddressList();
      case "accountEnabled":            return grouperAzureUser.getAccountEnabledDb();
      // password intentionally not captured
      default:                          return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record an Azure group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperAzureGroup grouperAzureGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromAzureGroup(grouperAzureGroup));
  }

  /** Build + record an Azure user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperAzureUser grouperAzureUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromAzureUser(grouperAzureUser));
  }

  /**
   * Record (targetGroupId, targetUserId) memberships for a single Azure group, given the
   * already-resolved user ids returned by {@code retrieveAzureGroupMembers}. No-op when
   * inputs are blank.
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

  // ----- static dispatchers (called from GrouperAzureTargetDao) -------------------------

  /**
   * Capture an Azure group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't an Azure one.
   */
  public static void captureGroupFromCurrentProvisioner(GrouperAzureGroup grouperAzureGroup) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.captureGroup(grouperAzureGroup);
  }

  /** Capture an Azure user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperAzureUser grouperAzureUser) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.captureUser(grouperAzureUser);
  }

  /**
   * Record group-to-user memberships against the current provisioner's sync, using
   * already-resolved Azure target ids.
   */
  public static void captureMembershipsForGroupFromCurrentProvisioner(
      String targetGroupId, Collection<String> targetUserIds) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.captureMembershipsForGroup(targetGroupId, targetUserIds);
  }

  private static GrouperAzureProvisioningTargetNativeSync azureSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperAzureProvisioningTargetNativeSync) {
      return (GrouperAzureProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

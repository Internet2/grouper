package edu.internet2.middleware.grouper.app.google;

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
 * Google-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperGoogleUser} / {@link GrouperGoogleGroup} typed beans
 * returned by {@code GrouperGoogleApiCommands}.
 *
 * <p>Like Adobe, Google target objects are typed Java beans, so attribute capture is a small
 * switch on attribute name → bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored for Google; only {@code name}
 * is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because the
 * DAO is where Google responses are already converted to typed beans.
 *
 * <p>The user {@code password} field is intentionally never captured into the sync-back
 * shadow.
 */
public class GrouperGoogleProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Google users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and
   * {@code password} (never shadowed). Given/family name are intentionally omitted from the
   * default to keep the shadow small — operators who want them can add to nativeAttributesEntities.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("primaryEmail"),
          attrConfig("orgUnitPath")));

  /**
   * Default per-attribute capture list for Google groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("email")));

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

  // ----- build (typed bean → native-reporting bean) ------------------------------------

  /** Build a native group bean from a Google group. {@code targetId} is the Google id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromGoogleGroup(GrouperGoogleGroup grouperGoogleGroup) {
    if (grouperGoogleGroup == null || grouperGoogleGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperGoogleGroup.getId());
    populateGroupAttributes(bean.getAttributes(), grouperGoogleGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a Google user. {@code targetId} is the Google id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromGoogleUser(GrouperGoogleUser grouperGoogleUser) {
    if (grouperGoogleUser == null || grouperGoogleUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperGoogleUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperGoogleUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperGoogleGroup grouperGoogleGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperGoogleGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperGoogleUser grouperGoogleUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperGoogleUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a Google group bean. Unknown attribute names return null
   * (silently skipped — validation already catches bad config).
   */
  private static Object resolveGroupAttribute(GrouperGoogleGroup grouperGoogleGroup, String attributeName) {
    if (grouperGoogleGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":                              return grouperGoogleGroup.getName();
      case "description":                       return grouperGoogleGroup.getDescription();
      case "email":                             return grouperGoogleGroup.getEmail();
      case "owners":                            return grouperGoogleGroup.getOwners();
      case "managers":                          return grouperGoogleGroup.getManagers();
      case "whoCanAdd":                         return grouperGoogleGroup.getWhoCanAdd();
      case "whoCanJoin":                        return grouperGoogleGroup.getWhoCanJoin();
      case "whoCanViewMembership":              return grouperGoogleGroup.getWhoCanViewMembership();
      case "whoCanViewGroup":                   return grouperGoogleGroup.getWhoCanViewGroup();
      case "whoCanInvite":                      return grouperGoogleGroup.getWhoCanInvite();
      case "whoCanModerateMembers":             return grouperGoogleGroup.getWhoCanModerateMembers();
      case "whoCanPostMessage":                 return grouperGoogleGroup.getWhoCanPostMessage();
      case "allowExternalMembers":              return grouperGoogleGroup.getAllowExternalMembers();
      case "allowGoogleCommunication":          return grouperGoogleGroup.getAllowGoogleCommunication();
      case "allowWebPosting":                   return grouperGoogleGroup.getAllowWebPosting();
      case "defaultMessageDenyNotificationText":return grouperGoogleGroup.getDefaultMessageDenyNotificationText();
      case "messageModerationLevel":            return grouperGoogleGroup.getMessageModerationLevel();
      case "replyTo":                           return grouperGoogleGroup.getReplyTo();
      case "spamModerationLevel":               return grouperGoogleGroup.getSpamModerationLevel();
      default:                                  return null;
    }
  }

  /**
   * see {@link #resolveGroupAttribute}. {@code password} is intentionally not exposed.
   */
  private static Object resolveUserAttribute(GrouperGoogleUser grouperGoogleUser, String attributeName) {
    if (grouperGoogleUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "primaryEmail": return grouperGoogleUser.getPrimaryEmail();
      case "givenName":    return grouperGoogleUser.getGivenName();
      case "familyName":   return grouperGoogleUser.getFamilyName();
      case "orgUnitPath":  return grouperGoogleUser.getOrgUnitPath();
      default:             return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Google group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperGoogleGroup grouperGoogleGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromGoogleGroup(grouperGoogleGroup));
  }

  /** Build + record a Google user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperGoogleUser grouperGoogleUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromGoogleUser(grouperGoogleUser));
  }

  /**
   * Translate a set of Google member user-ids for a given target group id into native
   * membership beans and record them. No-op if reporting is off or input is empty.
   */
  public void captureMembershipsForGroup(String targetGroupId, java.util.Set<String> memberUserIds) {
    if (targetGroupId == null || memberUserIds == null || memberUserIds.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String userId : memberUserIds) {
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

  // ----- static dispatchers (called from GrouperGoogleTargetDao) ------------------------

  /** Capture a Google group against the current provisioner's sync. */
  public static void captureGroupFromCurrentProvisioner(GrouperGoogleGroup grouperGoogleGroup) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.captureGroup(grouperGoogleGroup);
  }

  /** Capture a Google user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperGoogleUser grouperGoogleUser) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.captureUser(grouperGoogleUser);
  }

  /** Record (targetGroupId, userId) memberships against the current provisioner's sync. */
  public static void captureMembershipsForGroupFromCurrentProvisioner(
      String targetGroupId, java.util.Set<String> memberUserIds) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.captureMembershipsForGroup(targetGroupId, memberUserIds);
  }

  private static GrouperGoogleProvisioningTargetNativeSync googleSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperGoogleProvisioningTargetNativeSync) {
      return (GrouperGoogleProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

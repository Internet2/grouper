package edu.internet2.middleware.grouper.app.adobe;

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
 * Adobe-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans directly from the {@link GrouperAdobeUser} / {@link GrouperAdobeGroup} typed beans
 * returned by {@code GrouperAdobeApiCommands}.
 *
 * <p>Unlike SCIM (JSON Pointer paths) or LDAP (raw entry attributes), Adobe target objects
 * are typed Java beans, so attribute capture is a small switch on attribute name → bean
 * getter. The {@code path} field on {@link GrouperProvisioningNativeAttributeConfig} is
 * ignored for Adobe; only {@code name} is meaningful.
 *
 * <p>Capture is hooked at the DAO level (not at the API-commands seam like SCIM) because
 * the DAO is where Adobe responses are already converted to typed beans, and the seam is
 * a single function call shorter.
 */
public class GrouperAdobeProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Adobe users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and
   * unstable / large fields like {@code groups}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userName"),
          attrConfig("email"),
          attrConfig("status")));

  /**
   * Default per-attribute capture list for Adobe groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("productName"),
          attrConfig("type")));

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

  /** Build a native group bean from an Adobe group. {@code targetId} is the Adobe id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromAdobeGroup(GrouperAdobeGroup grouperAdobeGroup) {
    if (grouperAdobeGroup == null || grouperAdobeGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(grouperAdobeGroup.getId().toString());
    populateGroupAttributes(bean.getAttributes(), grouperAdobeGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from an Adobe user. {@code targetId} is the Adobe id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromAdobeUser(GrouperAdobeUser grouperAdobeUser) {
    if (grouperAdobeUser == null || grouperAdobeUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(grouperAdobeUser.getId());
    populateUserAttributes(bean.getAttributes(), grouperAdobeUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      GrouperAdobeGroup grouperAdobeGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(grouperAdobeGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      GrouperAdobeUser grouperAdobeUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(grouperAdobeUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from an Adobe group bean. Unknown attribute names return
   * null (silently skipped — validation already catches bad config). Bean property names
   * map directly: "name" → getName(), etc.
   */
  private static Object resolveGroupAttribute(GrouperAdobeGroup grouperAdobeGroup, String attributeName) {
    if (grouperAdobeGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":         return grouperAdobeGroup.getName();
      case "type":         return grouperAdobeGroup.getType();
      case "productName":  return grouperAdobeGroup.getProductName();
      case "memberCount":  return grouperAdobeGroup.getMemberCount();
      case "licenseQuota": return grouperAdobeGroup.getLicenseQuota();
      default:             return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(GrouperAdobeUser grouperAdobeUser, String attributeName) {
    if (grouperAdobeUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "userName":  return grouperAdobeUser.getUserName();
      case "email":     return grouperAdobeUser.getEmail();
      case "firstName": return grouperAdobeUser.getFirstName();
      case "lastName":  return grouperAdobeUser.getLastName();
      case "status":    return grouperAdobeUser.getStatus();
      case "type":      return grouperAdobeUser.getType();
      case "domain":    return grouperAdobeUser.getDomain();
      case "country":   return grouperAdobeUser.getCountry();
      default:          return null;
    }
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record an Adobe group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(GrouperAdobeGroup grouperAdobeGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromAdobeGroup(grouperAdobeGroup));
  }

  /** Build + record an Adobe user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(GrouperAdobeUser grouperAdobeUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromAdobeUser(grouperAdobeUser));
  }

  /**
   * Translate an Adobe user's {@code groups} set (group-name strings) into native
   * membership beans against the supplied {@code groupNameToTargetId} index, and record
   * them. No-op if reporting is off or the input is empty.
   */
  public void captureMembershipsFromUser(GrouperAdobeUser grouperAdobeUser, Map<String, String> groupNameToTargetId) {
    if (grouperAdobeUser == null || grouperAdobeUser.getId() == null || groupNameToTargetId == null) {
      return;
    }
    if (grouperAdobeUser.getGroups() == null || grouperAdobeUser.getGroups().isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String groupName : grouperAdobeUser.getGroups()) {
      String targetGroupId = groupNameToTargetId.get(groupName);
      if (targetGroupId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(grouperAdobeUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperAdobeTargetDao) -------------------------

  /**
   * Capture an Adobe group against the current provisioner's sync. No-op if there's no
   * current provisioner or the active provisioner isn't an Adobe one.
   */
  public static void captureGroupFromCurrentProvisioner(GrouperAdobeGroup grouperAdobeGroup) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.captureGroup(grouperAdobeGroup);
  }

  /** Capture an Adobe user against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(GrouperAdobeUser grouperAdobeUser) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.captureUser(grouperAdobeUser);
  }

  /**
   * Translate an Adobe user's group-name memberships into native records against the
   * supplied group-name → target-id index, and record them on the current provisioner.
   */
  public static void captureMembershipsFromUserForCurrentProvisioner(
      GrouperAdobeUser grouperAdobeUser, Map<String, String> groupNameToTargetId) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.captureMembershipsFromUser(grouperAdobeUser, groupNameToTargetId);
  }

  private static GrouperAdobeProvisioningTargetNativeSync adobeSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperAdobeProvisioningTargetNativeSync) {
      return (GrouperAdobeProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

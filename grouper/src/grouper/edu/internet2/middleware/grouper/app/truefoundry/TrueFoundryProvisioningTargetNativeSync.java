package edu.internet2.middleware.grouper.app.truefoundry;

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
 * TrueFoundry-specific {@link GrouperProvisioningTargetNativeSync}: builds native target
 * reporting beans directly from the {@link TrueFoundryUser} / {@link TrueFoundryGroup}
 * typed beans returned by {@code TrueFoundryApiCommands}.
 *
 * <p>Like Adobe, TrueFoundry target objects are typed Java beans, so attribute capture is
 * a small switch on attribute name to bean getter. The {@code path} field on
 * {@link GrouperProvisioningNativeAttributeConfig} is ignored; only {@code name} matters.
 *
 * <p>Capture is hooked at the DAO level, where TrueFoundry responses are already converted
 * to typed beans.
 */
public class TrueFoundryProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for TrueFoundry users when {@code nativeAttributesEntities}
   * is not configured. Excludes {@code id} (already the target_user_id column) and noisy fields.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("email"),
          attrConfig("active")));

  /**
   * Default per-attribute capture list for TrueFoundry groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id) and large/embedded fields
   * like {@code members}/{@code managers}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
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

  /** Build a native group bean from a TrueFoundryGroup. {@code targetId} is the TF id. */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromTrueFoundryGroup(TrueFoundryGroup trueFoundryGroup) {
    if (trueFoundryGroup == null || trueFoundryGroup.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(trueFoundryGroup.getId());
    populateGroupAttributes(bean.getAttributes(), trueFoundryGroup, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /** Build a native user bean from a TrueFoundryUser. {@code targetId} is the TF native user id. */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromTrueFoundryUser(TrueFoundryUser trueFoundryUser) {
    if (trueFoundryUser == null || trueFoundryUser.getId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(trueFoundryUser.getId());
    populateUserAttributes(bean.getAttributes(), trueFoundryUser, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  private static void populateGroupAttributes(
      Map<String, Object> destinationAttributes,
      TrueFoundryGroup trueFoundryGroup,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveGroupAttribute(trueFoundryGroup, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static void populateUserAttributes(
      Map<String, Object> destinationAttributes,
      TrueFoundryUser trueFoundryUser,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      Object value = resolveUserAttribute(trueFoundryUser, cfg.getName());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  /**
   * Resolve a named attribute from a TrueFoundryGroup. Unknown attribute names return null
   * (silently skipped). Bean property names map directly.
   */
  private static Object resolveGroupAttribute(TrueFoundryGroup trueFoundryGroup, String attributeName) {
    if (trueFoundryGroup == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "name":         return trueFoundryGroup.getName();
      case "displayName":  return trueFoundryGroup.getDisplayName();
      case "description":  return trueFoundryGroup.getDescription();
      case "groupType":    return trueFoundryGroup.getGroupType();
      case "resourceType": return trueFoundryGroup.getResourceType();
      case "isDefault":    return trueFoundryGroup.getIsDefault();
      default:             return null;
    }
  }

  /** see {@link #resolveGroupAttribute} */
  private static Object resolveUserAttribute(TrueFoundryUser trueFoundryUser, String attributeName) {
    if (trueFoundryUser == null || attributeName == null) {
      return null;
    }
    switch (attributeName) {
      case "email":       return trueFoundryUser.getEmail();
      case "displayName": return trueFoundryUser.getDisplayName();
      case "active":      return trueFoundryUser.getActive();
      default:            return null;
    }
  }

  // ----- capture convenience (build + record) -----------------------------------------

  /** Build + record a TrueFoundry group. No-op when sync-back is off or group is null/idless. */
  public void captureGroup(TrueFoundryGroup trueFoundryGroup) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromTrueFoundryGroup(trueFoundryGroup));
  }

  /** Build + record a TrueFoundry user. No-op when sync-back is off or user is null/idless. */
  public void captureUser(TrueFoundryUser trueFoundryUser) {
    this.recordTargetNativeUser(this.buildNativeUserFromTrueFoundryUser(trueFoundryUser));
  }

  /**
   * Translate a TrueFoundry group's {@code members} list (member email strings) into native
   * membership beans against the supplied {@code emailToTargetUserId} index, and record them.
   * No-op if reporting is off or the input is empty.
   */
  public void captureMembershipsFromGroup(TrueFoundryGroup trueFoundryGroup, Map<String, String> emailToTargetUserId) {
    if (trueFoundryGroup == null || trueFoundryGroup.getId() == null || emailToTargetUserId == null) {
      return;
    }
    List<String> members = trueFoundryGroup.getMembers();
    if (members == null || members.isEmpty()) {
      return;
    }
    String targetGroupId = trueFoundryGroup.getId();
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String memberEmail : members) {
      String targetUserId = emailToTargetUserId.get(memberEmail);
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

  // ----- static dispatchers (called from TrueFoundryTargetDao) -------------------------

  /** Capture a TrueFoundryGroup against the current provisioner's sync. */
  public static void captureGroupFromCurrentProvisioner(TrueFoundryGroup trueFoundryGroup) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroup(trueFoundryGroup);
  }

  /** Capture a TrueFoundryUser against the current provisioner's sync. */
  public static void captureUserFromCurrentProvisioner(TrueFoundryUser trueFoundryUser) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUser(trueFoundryUser);
  }

  /**
   * Translate a TrueFoundry group's member-email memberships into native records against the
   * supplied email -> target-user-id index, and record them on the current provisioner.
   */
  public static void captureMembershipsFromGroupForCurrentProvisioner(
      TrueFoundryGroup trueFoundryGroup, Map<String, String> emailToTargetUserId) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMembershipsFromGroup(trueFoundryGroup, emailToTargetUserId);
  }

  /**
   * Scoped-retrieve variant: capture a TrueFoundry group's memberships using an email ->
   * target-user-id index built on the fly from the current provisioner's already-captured
   * native users (populated by prior scoped {@code retrieveEntity} calls in the same pass).
   * No-op if reporting is off, the group has no members, or no captured users carry an email.
   */
  public static void captureMembershipsFromGroupForCurrentProvisionerUsingCapturedUsers(
      TrueFoundryGroup trueFoundryGroup) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return;
    }
    Map<String, GrouperProvisioningTargetNativeUser> targetUserIdToNativeUser =
        provisioner.retrieveGrouperProvisioningData().getTargetUserIdToNativeUser();
    if (targetUserIdToNativeUser == null || targetUserIdToNativeUser.isEmpty()) {
      return;
    }
    Map<String, String> emailToTargetUserId = new java.util.LinkedHashMap<String, String>();
    for (GrouperProvisioningTargetNativeUser nativeUser : targetUserIdToNativeUser.values()) {
      if (nativeUser == null || nativeUser.getTargetId() == null) {
        continue;
      }
      Object emailObject = nativeUser.getAttributes() == null
          ? null : nativeUser.getAttributes().get("email");
      if (emailObject == null) {
        continue;
      }
      String email = emailObject.toString();
      if (email.isEmpty()) {
        continue;
      }
      emailToTargetUserId.put(email, nativeUser.getTargetId());
    }
    if (emailToTargetUserId.isEmpty()) {
      return;
    }
    sync.captureMembershipsFromGroup(trueFoundryGroup, emailToTargetUserId);
  }

  private static TrueFoundryProvisioningTargetNativeSync syncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof TrueFoundryProvisioningTargetNativeSync) {
      return (TrueFoundryProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

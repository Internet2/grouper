package edu.internet2.middleware.grouper.app.adobe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Adobe-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Adobe JSON (JSON Pointer paths,
 * like SCIM), hooked at the API-commands seam ({@code GrouperAdobeApiCommands.retrieveAdobeGroups}
 * and {@code retrieveAdobeUsers}/{@code retrieveAdobeUser}) where the full JSON node is in scope.
 * This avoids losing any Adobe field that the {@link GrouperAdobeGroup} / {@link GrouperAdobeUser}
 * typed beans do not model; operators can capture any JSON field via {@code nativeAttributesGroups}
 * / {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>Groups additionally pair with {@code canRetrieveGroup=false}: Adobe has no by-id group
 * endpoint, so the framework always reads all groups and the commands register every group. Users
 * keep {@code canRetrieveEntity=true} (Adobe reads a single user by email), so only the capture
 * site moved -- the routing is unchanged.
 *
 * <p>Memberships are still derived from the {@link GrouperAdobeUser} typed bean's {@code groups}
 * set ({@link #captureMembershipsFromUser}); only the group/user object capture is JSON-based.
 */
public class GrouperAdobeProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Adobe users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Adobe user JSON). Excludes {@code id}
   * (already the target_user_id column) and the large {@code groups} array. Operators can capture
   * any other Adobe user JSON field (firstname, lastname, domain, country, type, ...) via
   * {@code nativeAttributesEntities} with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Adobe JSON field is "username"; store it under the friendlier key "userName"
          attrConfigWithPath("userName", "/username"),
          attrConfig("email"),
          attrConfig("status")));

  /**
   * Default per-attribute capture list for Adobe groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (the group path reads the raw Adobe JSON). Excludes
   * {@code groupId} (already the target_group_id column). Operators can configure any other Adobe
   * group JSON field via {@code nativeAttributesGroups} with a {@code name} (and optional
   * {@code path}), since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Adobe JSON field is "groupName"; store it under the friendlier key "name"
          attrConfigWithPath("name", "/groupName"),
          attrConfig("productName"),
          attrConfig("type")));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name) {
    return attrConfigWithPath(name, null);
  }

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path}. When {@code path}
   * is null the JSON path defaults to {@code "/" + name} (see {@link #populateAttributesFromJson}).
   */
  private static GrouperProvisioningNativeAttributeConfig attrConfigWithPath(String name, String path) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
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

  // ----- build (raw Adobe JSON -> native-reporting bean) -------------------------------

  /**
   * Build a native group bean from the raw Adobe group JSON. {@code targetId} is read from
   * {@code /groupId}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code groupId}.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/groupId");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw Adobe user JSON. {@code targetId} is read from
   * {@code /id}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code id}.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/id");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), userNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer ({@code path}, or {@code "/" + name})
   * against the raw Adobe JSON (group or user) and put the coerced value under {@code cfg.getName()}.
   * Missing / null nodes are skipped (no attribute row written).
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /productName, /email)
      String pointer = StringUtils.defaultIfBlank(cfg.getPath(), "/" + cfg.getName());
      JsonNode node = resourceNode.at(pointer);
      if (node == null || node.isMissingNode() || node.isNull()) {
        continue;
      }
      Object value = coerceJsonValue(node, cfg.getType());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static String resolveScalarAsString(JsonNode resourceNode, String jsonPointer) {
    if (resourceNode == null) {
      return null;
    }
    JsonNode node = resourceNode.at(jsonPointer);
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    return node.asText();
  }

  /**
   * Coerce a JsonNode to a scalar Object for storage in the attribute map. The declared type
   * ({@code "string"|"integer"|"boolean"|"timestamp"}) wins when present; otherwise the node's
   * intrinsic JSON type drives the choice.
   */
  private static Object coerceJsonValue(JsonNode node, String declaredType) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "integer")) {
      return Long.valueOf(node.asLong());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "boolean")) {
      return Boolean.valueOf(node.asBoolean());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "timestamp")) {
      // store as the source string; downstream coercion handled by the dictionary path
      return node.asText();
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "string")) {
      return node.asText();
    }
    // auto-detect by intrinsic JSON type
    if (node.isBoolean()) {
      return Boolean.valueOf(node.asBoolean());
    }
    if (node.isIntegralNumber()) {
      return Long.valueOf(node.asLong());
    }
    if (node.isNumber()) {
      return Double.valueOf(node.asDouble());
    }
    return node.asText();
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record an Adobe group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record an Adobe user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
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
   * Capture an Adobe group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Adobe one. Called from the
   * commands seam ({@code GrouperAdobeApiCommands.retrieveAdobeGroups}) for every group in the org.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.captureGroupJson(groupNode);
  }

  /**
   * Capture an Adobe user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Adobe one. Called from the
   * commands seam ({@code GrouperAdobeApiCommands.retrieveAdobeUsers}/{@code retrieveAdobeUser}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.captureUserJson(userNode);
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

  // ----- membership write-track dispatchers (called from GrouperAdobeTargetDao write sites) -----
  // Unlike groups/users (re-read via the drain to reflect the target), memberships are tracked
  // purely from our own successful add/remove writes -- never re-read -- because they are
  // high-volume. The keys are the Adobe target ids: group id (set on the ProvisioningGroup at
  // insert) and Adobe user id (set on the ProvisioningEntity at insert), which match the native
  // group/user targetIds the flush reconciles against.

  /**
   * Write-track a successful Adobe membership add ({@code associateUsersToGroup}) against the
   * current provisioner: record {@code (groupTargetId, userTargetId)} in the native membership map.
   * No-op out of cycle or for a non-Adobe provisioner.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Adobe membership remove ({@code disassociateUsersFromGroup}) against
   * the current provisioner: drop {@code (groupTargetId, userTargetId)} from the native membership
   * map so the end-of-run flush deletes its grouper_prov_mship row.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperAdobeProvisioningTargetNativeSync adobeSync = adobeSyncForCurrentProvisioner();
    if (adobeSync == null) {
      return;
    }
    adobeSync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
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

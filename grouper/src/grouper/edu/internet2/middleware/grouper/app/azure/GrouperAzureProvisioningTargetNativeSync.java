package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * Azure-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Microsoft Graph JSON (JSON
 * Pointer paths, like SCIM/Adobe/Google), hooked at the API-commands seam
 * ({@code GrouperAzureApiCommands.retrieveAzureGroups}/{@code retrieveGroupsHelper} and
 * {@code retrieveAzureUsers}/{@code retrieveUsersHelper}) where the full JSON node is in scope.
 * This avoids losing any Graph field that the {@link GrouperAzureGroup} / {@link GrouperAzureUser}
 * typed beans do not model; operators can capture any JSON field via {@code nativeAttributesGroups}
 * / {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>Unlike Adobe, Azure keeps {@code canRetrieveGroup=true} (Graph reads a single group by id or
 * displayName), so the routing is unchanged -- only the capture site moved off the typed bean.
 *
 * <p>Memberships are group-centric and still derived during DAO translation
 * ({@link #captureMembershipsForGroup}): the Graph group-members read co-locates the group id and
 * the member user ids, and group owners are written as role memberships. Owners/members are NOT
 * group attributes, so they are not folded into the group attribute node here. Only the
 * group/user object capture is JSON-based.
 */
public class GrouperAzureProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Azure users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Graph user JSON). Excludes {@code id}
   * (already the target_user_id column) and security-sensitive fields like {@code password}.
   * Each default's JSON field matches the field the old typed-bean getter returned. Operators can
   * capture any other Graph user JSON field via {@code nativeAttributesEntities} with a
   * {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userPrincipalName"),
          attrConfig("mail"),
          attrConfig("mailNickname"),
          attrConfig("userType")));

  /**
   * Default per-attribute capture list for Azure groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (the group path reads the raw Graph JSON). Excludes
   * {@code id} (already target_group_id). Each default's JSON field matches the field the old
   * typed-bean getter returned. Operators can configure any other Graph group JSON field via
   * {@code nativeAttributesGroups} with a {@code name} (and optional {@code path}), since capture
   * now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("displayName"),
          attrConfig("mailNickname")));

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

  // ----- build (raw Graph JSON -> native-reporting bean) -------------------------------

  /**
   * Build a native group bean from the raw Microsoft Graph group JSON. {@code targetId} is read
   * from {@code /id} (the same field the old {@code buildNativeGroupFromAzureGroup} took from
   * {@code GrouperAzureGroup.getId()}); the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code id}.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/id");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw Microsoft Graph user JSON. {@code targetId} is read from
   * {@code /id} (the same field the old {@code buildNativeUserFromAzureUser} took from
   * {@code GrouperAzureUser.getId()}); the attributes map is populated for each entry in
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
   * against the raw Graph JSON (group or user) and put the coerced value under {@code cfg.getName()}.
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /displayName, /mail)
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

  /** Build + record an Azure group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record an Azure user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Record (targetGroupId, targetUserId) memberships for a single Azure group, given the
   * already-resolved user ids returned by {@code retrieveAzureGroupMembers}. No-op when
   * inputs are blank.
   *
   * <p>Azure membership is group-centric and derived during DAO translation (the Graph
   * group-members read co-locates both ids), so it stays bean/id-based -- the same situation as
   * Adobe -- while object capture moves to raw JSON.
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

  // ----- static dispatchers (called from GrouperAzureApiCommands / GrouperAzureTargetDao) -----

  /**
   * Capture an Azure group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Azure one. Called from the
   * commands seam ({@code GrouperAzureApiCommands.retrieveAzureGroups}/{@code retrieveGroupsHelper})
   * for every group read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.captureGroupJson(groupNode);
  }

  /**
   * Capture an Azure user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Azure one. Called from the
   * commands seam ({@code GrouperAzureApiCommands.retrieveAzureUsers}/{@code retrieveUsersHelper}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.captureUserJson(userNode);
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

  // ----- membership write-track dispatchers (called from GrouperAzureTargetDao write sites) -----
  // These mirror single successful membership add/remove writes into grouper_prov_mship so the
  // full-sync "memberships from sync-back cache" feature stays current between full reads. The keys
  // are the Azure target ids: group id and user (entity) id, matching the native group/user
  // targetIds the flush reconciles against.

  /**
   * Write-track a successful Azure membership add against the current provisioner: record
   * {@code (groupTargetId, userTargetId)} in the native membership map. No-op out of cycle or for a
   * non-Azure provisioner (the underlying record call is itself guarded by
   * isLoadMembershipsToGenericGrouperTable()).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Azure membership remove against the current provisioner: drop
   * {@code (groupTargetId, userTargetId)} from the native membership map so the end-of-run flush
   * deletes its grouper_prov_mship row. No-op out of cycle or for a non-Azure provisioner.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperAzureProvisioningTargetNativeSync azureSync = azureSyncForCurrentProvisioner();
    if (azureSync == null) {
      return;
    }
    azureSync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
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

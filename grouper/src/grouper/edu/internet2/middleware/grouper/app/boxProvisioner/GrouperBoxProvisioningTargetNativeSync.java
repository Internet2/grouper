package edu.internet2.middleware.grouper.app.boxProvisioner;

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
 * Box-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Box JSON (JSON Pointer paths,
 * like SCIM and Adobe), hooked at the API-commands seam ({@code GrouperBoxApiCommands} read
 * methods such as {@code retrieveBoxGroups} / {@code retrieveBoxGroup} / {@code retrieveBoxUsers}
 * / {@code retrieveBoxUser}) where the per-element JSON node is in scope. This avoids losing any
 * Box field that the {@link GrouperBoxGroup} / {@link GrouperBoxUser} typed beans do not model;
 * operators can capture any JSON field via {@code nativeAttributesGroups} /
 * {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>The default keys are chosen so each default's captured value matches what the OLD typed-bean
 * getter returned. For Box every default's bean property name happens to equal its raw Box JSON
 * field name (e.g. group {@code name}/{@code group_type}/{@code provenance}; user
 * {@code login}/{@code role}/{@code status}/{@code type}), so the defaults need no explicit path
 * remapping (the JSON path defaults to {@code "/" + name}). The target ids come from {@code /id}
 * for both groups and users -- the same JSON field the old typed-bean build methods read via
 * {@code GrouperBoxGroup.getId()} / {@code GrouperBoxUser.getId()}.
 *
 * <p>Memberships are captured on BOTH the read path and the write path. On read they are derived
 * from the Box memberships read during DAO translation ({@link #captureMemberships}) -- Box's
 * membership model is group-centric (the {@code /groups/:id/memberships} read yields the member user
 * ids for a group), and the DAO records the {@code (targetGroupId, targetUserId)} pairs in the same
 * loop where they are already known. On write,
 * {@code GrouperBoxTargetDao.insertMembership}/{@code deleteMembership} call
 * {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner} (-> {@code recordTargetNativeMembershipInsert}
 * / {@code recordTargetNativeMembershipDelete}) on success, so a membership add/remove is recorded
 * into the native mirror on the write pass. Only the group/user object capture moved to JSON.
 */
public class GrouperBoxProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Box users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Box user JSON). Excludes {@code id}
   * (already the target_user_id column) and large / unstable fields. Each default's Box JSON
   * field equals its name, so the path defaults to {@code "/" + name}. Operators can capture any
   * other Box user JSON field (name, space_used, max_upload_size, ...) via
   * {@code nativeAttributesEntities} with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("login"),
          attrConfig("role"),
          attrConfig("status"),
          attrConfig("type")));

  /**
   * Default per-attribute capture list for Box groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw Box group JSON). Excludes {@code id}
   * (already the target_group_id column). Each default's Box JSON field equals its name, so the
   * path defaults to {@code "/" + name}. Operators can configure any other Box group JSON field
   * (description, external_sync_identifier, ...) via {@code nativeAttributesGroups} with a
   * {@code name} (and optional {@code path}), since capture now reads the full JSON rather than the
   * typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("group_type"),
          attrConfig("provenance")));

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

  // ----- build (raw Box JSON -> native-reporting bean) ----------------------------------

  /**
   * Build a native group bean from the raw Box group JSON. {@code targetId} is read from
   * {@code /id} (the same field the old typed-bean build read via getId()); the attributes map is
   * populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code id}.
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
   * Build a native user bean from the raw Box user JSON. {@code targetId} is read from
   * {@code /id} (the same field the old typed-bean build read via getId()); the attributes map is
   * populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code id}.
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
   * against the raw Box JSON (group or user) and put the coerced value under {@code cfg.getName()}.
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /login, /status).
      // A name with embedded slashes can address nested Box JSON via an explicit path, e.g.
      // path "/permissions/can_invite_as_collaborator".
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

  /** Build + record a Box group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Box user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Record the (targetGroupId, targetUserId) pairs as native membership beans.
   * No-op if input is empty.
   */
  public void captureMemberships(String targetGroupId, Iterable<String> targetUserIds) {
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
    if (!memberships.isEmpty()) {
      this.recordTargetNativeMemberships(memberships);
    }
  }

  // ----- static dispatchers (called from GrouperBoxApiCommands / GrouperBoxTargetDao) ----

  /**
   * Capture a Box group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Box one. Called from the
   * commands seam ({@code GrouperBoxApiCommands} read methods) for every group parsed from a read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Box user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Box one. Called from the
   * commands seam ({@code GrouperBoxApiCommands} read methods) for every user parsed from a read.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureUserJson(userNode);
  }

  /**
   * Record the (targetGroupId, targetUserId) pairs as native membership beans on the
   * current provisioner's sync. Box memberships are group-centric and derived during DAO
   * translation, so unlike groups/users (captured at the commands JSON seam) this stays a
   * typed-bean call site.
   */
  public static void captureMembershipsFromCurrentProvisioner(String targetGroupId, Iterable<String> targetUserIds) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.captureMemberships(targetGroupId, targetUserIds);
  }

  /**
   * Write-track a successful Box membership add ({@code createBoxMembership}) against the current
   * provisioner: record {@code (groupTargetId, userTargetId)} in the native membership map so the
   * generic grouper_prov_mship sync-back mirror stays current on capture-on-write. No-op out of
   * cycle, for a non-Box provisioner, or when membership sync-back is off (guarded downstream by
   * {@code recordTargetNativeMembershipInsert} -> {@code isLoadMembershipsToGenericGrouperTable()}).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Box membership remove ({@code deleteBoxMembership}) against the current
   * provisioner: drop {@code (groupTargetId, userTargetId)} from the native membership map so the
   * end-of-run flush deletes its grouper_prov_mship row. No-op out of cycle, for a non-Box
   * provisioner, or when membership sync-back is off (guarded downstream by
   * {@code recordTargetNativeMembershipDelete} -> {@code isLoadMembershipsToGenericGrouperTable()}).
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperBoxProvisioningTargetNativeSync boxSync = boxSyncForCurrentProvisioner();
    if (boxSync == null) {
      return;
    }
    boxSync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  private static GrouperBoxProvisioningTargetNativeSync boxSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperBoxProvisioningTargetNativeSync) {
      return (GrouperBoxProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

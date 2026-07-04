package edu.internet2.middleware.grouper.app.remedyV2;

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
 * Remedy-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Remedy (BMC AR System) JSON
 * (JSON Pointer paths, like SCIM/Adobe), hooked at the API-commands seam
 * ({@code GrouperRemedyApiCommands.retrieveRemedyGroups} and the user-parse helper
 * {@code convertRemedyUsersFromJson}) where the full JSON entry node is in scope. This avoids
 * losing any Remedy field that the {@link GrouperRemedyGroup} / {@link GrouperRemedyUser} typed
 * beans do not model; operators can capture any JSON field via {@code nativeAttributesGroups}
 * / {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p><b>Remedy "values" envelope:</b> a Remedy entry is shaped
 * <code>{"values":{...fields...},"_links":{...}}</code>. The real fields (and the ids) live under
 * {@code /values}, so capture is hooked at the <i>entry</i> node and every default/operator pointer
 * is rooted at {@code /values/...}. Capturing the whole entry node (rather than the inner
 * {@code values} node) keeps the {@code _links/self/href} reachable too -- a field the typed beans
 * drop entirely.
 *
 * <p>Memberships are a separate Remedy object ({@code ENT:SYS People Entitlement Groups}, read by
 * its own API call), not something derived from the group or user object. They are still captured
 * from the {@link GrouperRemedyMembership} typed beans ({@link #captureMemberships}) at the DAO
 * level; only the group/user object capture moved to JSON.
 */
public class GrouperRemedyProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Remedy users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Remedy user entry JSON, fields under the
   * {@code values} envelope). Excludes {@code Person ID} (already the target_user_id column).
   * Operators can capture any other Remedy user JSON field via {@code nativeAttributesEntities}
   * with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Remedy field is "values"/"Remedy Login ID"; store it under the friendlier key "remedyLoginId"
          attrConfigWithPath("remedyLoginId", "/values/Remedy Login ID")));

  /**
   * Default per-attribute capture list for Remedy groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw Remedy group entry JSON, fields under the
   * {@code values} envelope). Excludes {@code Permission Group ID} (already target_group_id).
   * Operators can capture any other Remedy group JSON field via {@code nativeAttributesGroups}
   * with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Remedy field is "values"/"Permission Group"; store it under the friendlier key "permissionGroup"
          attrConfigWithPath("permissionGroup", "/values/Permission Group")));

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

  // ----- build (raw Remedy JSON -> native-reporting bean) ------------------------------

  /**
   * Build a native group bean from the raw Remedy group entry JSON. {@code targetId} is read from
   * {@code /values/Permission Group ID} (the same value the old typed-bean capture took from
   * {@link GrouperRemedyGroup#getPermissionGroupId()}); the attributes map is populated for each
   * entry in {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured or default) by
   * JSON Pointer. Returns null when the JSON is missing or has no {@code /values/Permission Group ID}.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/values/Permission Group ID");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw Remedy user entry JSON. {@code targetId} is read from
   * {@code /values/Person ID} (the same value the old typed-bean capture took from
   * {@link GrouperRemedyUser#getPersonId()}); the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code /values/Person ID}.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/values/Person ID");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), userNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * Build a native membership bean from a Remedy membership. targetGroupId =
   * permissionGroupId, targetUserId = personId.
   */
  public GrouperProvisioningTargetNativeMembership buildNativeMembershipFromRemedyMembership(
      GrouperRemedyMembership grouperRemedyMembership) {
    if (grouperRemedyMembership == null
        || grouperRemedyMembership.getPermissionGroupId() == null
        || grouperRemedyMembership.getPersonId() == null) {
      return null;
    }
    GrouperProvisioningTargetNativeMembership bean = new GrouperProvisioningTargetNativeMembership();
    bean.setTargetGroupId(grouperRemedyMembership.getPermissionGroupId().toString());
    bean.setTargetUserId(grouperRemedyMembership.getPersonId());
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer ({@code path}, or {@code "/" + name})
   * against the raw Remedy JSON entry (group or user) and put the coerced value under
   * {@code cfg.getName()}. Missing / null nodes are skipped (no attribute row written).
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name. Remedy fields live under
      // the "values" envelope, so the defaults carry an explicit "/values/..." path.
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

  // ----- capture convenience (build + record) -----------------------------------------

  /** Build + record a Remedy group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Remedy user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /** Build + record a list of Remedy memberships. No-op when sync-back is off or empty. */
  public void captureMemberships(List<GrouperRemedyMembership> grouperRemedyMemberships) {
    if (grouperRemedyMemberships == null || grouperRemedyMemberships.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyMemberships) {
      GrouperProvisioningTargetNativeMembership bean =
          this.buildNativeMembershipFromRemedyMembership(grouperRemedyMembership);
      if (bean != null) {
        memberships.add(bean);
      }
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperRemedyApiCommands / GrouperRemedyTargetDao) -----

  /**
   * Capture a Remedy group (from its raw JSON entry) against the current provisioner's sync. No-op
   * if there's no current provisioner or the active provisioner isn't a Remedy one. Called from the
   * commands seam ({@code GrouperRemedyApiCommands.retrieveRemedyGroups}) for every group read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Remedy user (from its raw JSON entry) against the current provisioner's sync. No-op
   * if there's no current provisioner or the active provisioner isn't a Remedy one. Called from the
   * commands user-parse helper ({@code GrouperRemedyApiCommands.convertRemedyUsersFromJson}), which
   * backs both the retrieve-all-users and retrieve-single-user read paths.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureUserJson(userNode);
  }

  /** Capture a list of Remedy memberships against the current provisioner's sync. */
  public static void captureMembershipsFromCurrentProvisioner(
      List<GrouperRemedyMembership> grouperRemedyMemberships) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.captureMemberships(grouperRemedyMemberships);
  }

  /**
   * Write-track a successful Remedy membership add ({@code assignUserToRemedyGroup}) against the
   * current provisioner: record {@code (groupTargetId, userTargetId)} in the native membership map so
   * the end-of-run flush inserts/keeps its grouper_prov_mship row. groupTargetId is the Remedy
   * permissionGroupId and userTargetId is the Remedy personId (the same target ids
   * {@link #buildNativeMembershipFromRemedyMembership} records). No-op out of cycle, for a non-Remedy
   * provisioner, or when membership sync-back is off (guarded in {@code recordTargetNativeMembershipInsert}).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Remedy membership remove ({@code removeUserFromRemedyGroup}) against the
   * current provisioner: drop {@code (groupTargetId, userTargetId)} from the native membership map so
   * the end-of-run flush deletes its grouper_prov_mship row. groupTargetId is the Remedy
   * permissionGroupId and userTargetId is the Remedy personId. No-op out of cycle, for a non-Remedy
   * provisioner, or when membership sync-back is off (guarded in {@code recordTargetNativeMembershipDelete}).
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperRemedyProvisioningTargetNativeSync remedySync = remedySyncForCurrentProvisioner();
    if (remedySync == null) {
      return;
    }
    remedySync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  private static GrouperRemedyProvisioningTargetNativeSync remedySyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperRemedyProvisioningTargetNativeSync) {
      return (GrouperRemedyProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

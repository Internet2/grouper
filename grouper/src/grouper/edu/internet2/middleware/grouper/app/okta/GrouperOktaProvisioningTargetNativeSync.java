package edu.internet2.middleware.grouper.app.okta;

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
 * Okta-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Okta JSON (JSON Pointer paths,
 * like SCIM/Adobe), hooked at the API-commands seam ({@code GrouperOktaApiCommands.retrieveOktaGroups}
 * / {@code retrieveOktaGroup} and {@code retrieveOktaUsers} / {@code retrieveOktaUser} /
 * {@code retrieveOktaUserById}) where the full JSON node is in scope. This avoids losing any Okta
 * field that the {@link GrouperOktaGroup} / {@link GrouperOktaUser} typed beans do not model;
 * operators can capture any JSON field via {@code nativeAttributesGroups} /
 * {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p><b>Okta objects are NESTED</b>: a group's identity id lives at the top level ({@code /id}) but
 * its descriptive fields live under {@code profile} ({@code /profile/name},
 * {@code /profile/description}); likewise a user's id is at {@code /id}, its lifecycle status at
 * {@code /status}, and its descriptive fields under {@code profile} ({@code /profile/login},
 * {@code /profile/email}, {@code /profile/firstName}, ...). The default capture pointers therefore
 * reach into {@code /profile/*}, matching exactly what {@link GrouperOktaGroup#fromJson} /
 * {@link GrouperOktaUser#fromJson} read.
 *
 * <p>Group/user OBJECTS capture only on the READ path (from the raw JSON above); there is no
 * write-side capture of group/user objects. MEMBERSHIPS, by contrast, now capture on WRITE: the
 * DAO's insert/deleteMembership call {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner} on success, which record into the native
 * membership mirror ({@code recordTargetNativeMembershipInsert}/{@code Delete}) -- the same
 * membership write-track design as Adobe/SCIM/Dropbox, so a membership add/remove converges on the
 * write pass. Memberships are also still derived group-centrically from the per-group member-id
 * fetch on the read path ({@link #captureMembershipsForGroup}); Okta has no retrieve-all-memberships
 * call, so that per-group member fetch is the only read-path place both ids are co-located -- the
 * same situation as Adobe/Google.
 */
public class GrouperOktaProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Okta users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Okta user JSON). The user's descriptive
   * fields are NESTED under {@code profile}, so the pointers reach {@code /profile/*}. Excludes
   * {@code id} (already the target_user_id column). Operators can capture any other Okta user JSON
   * field (firstName, lastName, the top-level status, ...) via {@code nativeAttributesEntities}
   * with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Okta nests these under "profile"; same values GrouperOktaUser.fromJson reads
          attrConfigWithPath("login", "/profile/login"),
          attrConfigWithPath("email", "/profile/email")));

  /**
   * Default per-attribute capture list for Okta groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (the group path reads the raw Okta JSON). The group's
   * descriptive fields are NESTED under {@code profile}, so the pointers reach {@code /profile/*}.
   * Excludes {@code id} (already the target_group_id column). Operators can configure any other
   * Okta group JSON field via {@code nativeAttributesGroups} with a {@code name} (and optional
   * {@code path}), since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Okta nests these under "profile"; same values GrouperOktaGroup.fromJson reads
          attrConfigWithPath("name", "/profile/name"),
          attrConfigWithPath("description", "/profile/description")));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name) {
    return attrConfigWithPath(name, null);
  }

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path}. When {@code path}
   * is null the JSON path defaults to {@code "/" + name} (see {@link #populateAttributesFromJson}).
   * Okta fields live under {@code profile}, so most defaults supply an explicit
   * {@code /profile/...} path rather than relying on the {@code "/" + name} fallback.
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

  // ----- build (raw Okta JSON -> native-reporting bean) --------------------------------

  /**
   * Build a native group bean from the raw Okta group JSON. {@code targetId} is read from the
   * top-level {@code /id} (the same field {@link GrouperOktaGroup#fromJson} uses for the id); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer, which for the defaults reaches into
   * {@code /profile/*}. Returns null when the JSON is missing or has no {@code id}.
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
   * Build a native user bean from the raw Okta user JSON. {@code targetId} is read from the
   * top-level {@code /id} (the same field {@link GrouperOktaUser#fromJson} uses for the id); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
   * (operator-configured or default) by JSON Pointer, which for the defaults reaches into
   * {@code /profile/*}. Returns null when the JSON is missing or has no {@code id}.
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
   * against the raw Okta JSON (group or user) and put the coerced value under {@code cfg.getName()}.
   * Missing / null nodes are skipped (no attribute row written). Note the Okta defaults supply an
   * explicit {@code /profile/...} path, so the {@code "/" + name} fallback only applies to
   * operator-configured top-level fields (e.g. {@code /status}).
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /status)
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

  /** Build + record an Okta group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record an Okta user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Build native membership beans for all member user ids in the supplied group, and record
   * them. No-op if reporting is off or the input is empty. Okta membership is group-centric: the
   * member ids come from the per-group member fetch during DAO translation, not from the group
   * object's JSON (the object does not carry its members), so this path is untouched by the move
   * to raw-JSON object capture.
   */
  public void captureMembershipsForGroup(String targetGroupId, Iterable<String> targetUserIds) {
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
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperOktaApiCommands / GrouperOktaTargetDao) -----

  /**
   * Capture an Okta group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Okta one. Called from the
   * commands seam ({@code GrouperOktaApiCommands.retrieveOktaGroups} / {@code retrieveOktaGroup})
   * for every group read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureGroupJson(groupNode);
  }

  /**
   * Capture an Okta user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't an Okta one. Called from the
   * commands seam ({@code GrouperOktaApiCommands.retrieveOktaUsers} / {@code retrieveOktaUser} /
   * {@code retrieveOktaUserById}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureUserJson(userNode);
  }

  /**
   * Record memberships for a given target group id against the current provisioner's sync. Okta
   * membership is group-centric and derived during DAO translation, so this stays a typed dispatch
   * (the ids are plain strings, not JSON) -- only the group/user object capture moved to raw JSON.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, Iterable<String> targetUserIds) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.captureMembershipsForGroup(targetGroupId, targetUserIds);
  }

  // ----- membership write-track dispatchers (called from GrouperOktaTargetDao write sites) -----
  // Like groups/users are re-read via the drain to reflect the target, but memberships are tracked
  // purely from our own successful add/remove writes -- never re-read -- because they are
  // high-volume (same design as Adobe/SCIM/Dropbox). This is what keeps grouper_prov_mship current
  // during a full sync that serves memberships from the sync-back cache (fullSyncMembershipsFromSyncBack),
  // where the per-group member read that would otherwise capture them is skipped. The ids are the Okta
  // target ids (group id and user id), matching the native group/user targetIds the flush reconciles.

  /**
   * Write-track a successful Okta membership add ({@code createOktaMembership}) against the current
   * provisioner: record {@code (targetGroupId, targetUserId)} in the native membership map. No-op
   * out of cycle or for a non-Okta provisioner.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
  }

  /**
   * Write-track a successful Okta membership remove ({@code deleteOktaMembership}) against the
   * current provisioner: drop {@code (targetGroupId, targetUserId)} from the native membership map
   * so the end-of-run flush deletes its grouper_prov_mship row.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    GrouperOktaProvisioningTargetNativeSync oktaSync = oktaSyncForCurrentProvisioner();
    if (oktaSync == null) {
      return;
    }
    oktaSync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
  }

  private static GrouperOktaProvisioningTargetNativeSync oktaSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperOktaProvisioningTargetNativeSync) {
      return (GrouperOktaProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

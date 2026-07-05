package edu.internet2.middleware.grouper.app.google;

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
 * Google-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Google JSON (JSON Pointer paths,
 * like SCIM and Adobe), hooked at the API-commands seam ({@code GrouperGoogleApiCommands
 * .retrieveGoogleGroups}/{@code retrieveGoogleGroup} and {@code retrieveGoogleUsers}/
 * {@code retrieveGoogleUser}) where the per-element JSON node is in scope. This avoids losing any
 * Google field that the {@link GrouperGoogleGroup} / {@link GrouperGoogleUser} typed beans do not
 * model; operators can capture any JSON field via {@code nativeAttributesGroups} /
 * {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p><b>Google-specific caveat on group capture:</b> the group JSON node parsed at the commands
 * seam is the Directory API group resource (the {@code groups[]} elements of
 * {@code retrieveGoogleGroups}, restricted by {@code fields=...} to {@code id,email,name,
 * description}; or the single {@code retrieveGoogleGroup} resource). Google group <i>settings</i>
 * (whoCanAdd, replyTo, messageModerationLevel, ...) and the manager/owner sets arrive from
 * SEPARATE API calls and are NOT present in that node. So the default group capture (name, email)
 * and any operator-configured Directory field (id excluded, description, ...) resolve from the
 * node, but an operator who configures a group <i>settings</i> field in
 * {@code nativeAttributesGroups} will not have it captured from this seam (it is not in the JSON
 * here). This matches the Adobe approach of capturing at the per-element raw-JSON parse point.
 *
 * <p>Group and user OBJECTS capture on the read path (JSON-based, as above). MEMBERSHIPS capture on
 * the WRITE path, like Adobe/SCIM: {@code GrouperGoogleTargetDao.insertMembership} /
 * {@code deleteMembership} call {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner} (-> {@code recordTargetNativeMembershipInsert}
 * / {@code recordTargetNativeMembershipDelete}) on success, so a membership add/remove lands in the
 * native mirror on the write and converges on the write pass. The read path also still derives
 * memberships group-centrically (the group's member ids, with roles looked up separately) during DAO
 * translation ({@link #captureMembershipsForGroup}) to re-confirm the mirror idempotently. The user
 * {@code password} field is never returned by Google on a read, so it never reaches this shadow.
 */
public class GrouperGoogleProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Google users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Google user JSON). Excludes {@code id}
   * (already the target_user_id column). Given/family name (nested under {@code /name}) and
   * {@code password} (never returned by Google) are intentionally omitted from the default to keep
   * the shadow small -- operators who want the names can add them to {@code nativeAttributesEntities}
   * with a path of {@code /name/givenName} / {@code /name/familyName}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Capture the MANAGED entity attributes -- the ones Grouper writes/compares -- so a
          // cache-reconstructed user matches a live read EXACTLY and does not trigger a spurious
          // update on every from-cache run (a spurious update marks the user for the drain, which
          // drops its shadow snapshot, and the flush then deletes the cache row -- the cache eats
          // itself). givenName/familyName are nested under /name in the Google user JSON. The
          // matching attribute "email" is auto-injected by the base capture layer from the rename
          // map (email <- primaryEmail), so it is not listed here. orgUnitPath is captured for
          // parity with the live read (not Grouper-managed here, but harmless and null in practice).
          attrConfigWithPath("givenName", "/name/givenName"),
          attrConfigWithPath("familyName", "/name/familyName"),
          attrConfig("orgUnitPath")));

  /**
   * Default per-attribute capture list for Google groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw Google Directory group JSON). Excludes
   * {@code id} (already the target_group_id column). Both defaults ({@code name}, {@code email})
   * are present in the Directory group node; see the class javadoc for why group-settings fields
   * are not reachable from this seam.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("email")));

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
  protected Map<String, String> grouperToNativeNameExceptionsEntities() {
    // single source of truth for the one Google user name that differs (email <- primaryEmail);
    // the base capture layer normalizes + auto-injects renamed attributes from this
    return GrouperGoogleUser.grouperNameToNativeNameExceptions();
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    return DEFAULT_GROUP_ATTRS;
  }

  // ----- build (raw Google JSON -> native-reporting bean) ------------------------------

  /**
   * Build a native group bean from the raw Google Directory group JSON. {@code targetId} is read
   * from {@code /id} (the same field the old {@code buildNativeGroupFromGoogleGroup} took from
   * {@code GrouperGoogleGroup.getId()}, which {@code fromJson} reads from {@code id}); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
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
   * Build a native user bean from the raw Google user JSON. {@code targetId} is read from
   * {@code /id} (the same field the old {@code buildNativeUserFromGoogleUser} took from
   * {@code GrouperGoogleUser.getId()}, which {@code fromJson} reads from {@code id}); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
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
   * against the raw Google JSON (group or user) and put the coerced value under {@code cfg.getName()}.
   * Missing / null nodes are skipped (no attribute row written). Nested fields (e.g. the user's
   * {@code /name/givenName}) are reachable via an explicit JSON-Pointer {@code path}.
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /primaryEmail, /email)
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

  /** Build + record a Google group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Google user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate a set of Google member user-ids for a given target group id into native
   * membership beans and record them. No-op if reporting is off or input is empty. Google
   * membership is group-centric (a group's member ids, with manager/owner roles resolved by
   * separate calls), so -- like Adobe -- it is captured from the translated object during DAO
   * processing rather than from a single per-element JSON node.
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

  // ----- static dispatchers (called from GrouperGoogleApiCommands / GrouperGoogleTargetDao) -----

  /**
   * Capture a Google group (from its raw Directory JSON) against the current provisioner's sync.
   * No-op if there's no current provisioner or the active provisioner isn't a Google one. Called
   * from the commands seam ({@code GrouperGoogleApiCommands.retrieveGoogleGroups}/
   * {@code retrieveGoogleGroup}) for each group while the JSON node is in scope.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Google user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Google one. Called from the
   * commands seam ({@code GrouperGoogleApiCommands.retrieveGoogleUsers}/{@code retrieveGoogleUser}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.captureUserJson(userNode);
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

  /**
   * Write-track a successful Google membership add ({@code createGoogleMembership}) against the
   * current provisioner: record {@code (groupTargetId, userTargetId)} in the native membership map
   * so the generic grouper_prov_mship mirror stays current without re-reading the target. No-op out
   * of cycle, for a non-Google provisioner, or when membership sync-back is off (guarded downstream
   * by {@code isLoadMembershipsToGenericGrouperTable()}).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Google membership remove ({@code deleteGoogleMembership}) against the
   * current provisioner: drop {@code (groupTargetId, userTargetId)} from the native membership map
   * so the end-of-run flush deletes its grouper_prov_mship row. No-op out of cycle, for a non-Google
   * provisioner, or when membership sync-back is off.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperGoogleProvisioningTargetNativeSync googleSync = googleSyncForCurrentProvisioner();
    if (googleSync == null) {
      return;
    }
    googleSync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
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

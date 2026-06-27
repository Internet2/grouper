package edu.internet2.middleware.grouper.app.freshServiceRequester;

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
 * FreshServiceRequester-specific {@link GrouperProvisioningTargetNativeSync}: builds native target
 * reporting beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Freshservice JSON (JSON Pointer
 * paths, like SCIM / Adobe / Google / Duo), hooked at the API-commands seam
 * ({@code FreshRequesterApiCommands.retrieveRequesterGroups}/{@code retrieveRequesterGroup} and the
 * {@code retrieveRequesterUsers}/{@code retrieveRequesterUserById}/{@code retrieveRequesterUserByEmail}/
 * {@code retrieveRequesterUserByAttribute} read methods) where the full per-object JSON node is in
 * scope. This avoids losing any Freshservice field that the {@link FreshRequesterGroup} /
 * {@link FreshRequesterUser} typed beans do not model; operators can capture any JSON field via
 * {@code nativeAttributesGroups} / {@code nativeAttributesEntities} with a {@code name} and optional
 * JSON-Pointer {@code path}.
 *
 * <p>The captured node is the <b>inner</b> object of the Freshservice envelope -- the read methods
 * unwrap {@code {"requester_group": {...}}} / {@code {"requester": {...}}} / the elements of
 * {@code {"requester_groups":[...]}} / {@code {"requesters":[...]}} -- so the default JSON pointers
 * below are relative to that inner object (e.g. {@code /name}, {@code /primary_email}, and the
 * target-id at {@code /id}), exactly the shape {@code FreshRequesterGroup.fromJson} /
 * {@code FreshRequesterUser.fromJson} parse.
 *
 * <p>Memberships are still group-centric: derived from the per-group member listing as typed
 * {@link FreshRequesterUser} beans during DAO translation ({@link #captureMembershipsForGroup}) --
 * the same situation as Adobe -- so only the group/user object capture is JSON-based; membership
 * capture is unchanged.
 */
public class FreshRequesterProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for FreshRequester users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Freshservice requester JSON, inner of the
   * envelope). Excludes {@code id} (already the target_user_id column). Each default's JSON field
   * matches what the old {@link FreshRequesterUser} getter returned: {@code getEmail()} read the
   * Freshservice {@code primary_email} field, so the friendlier key {@code email} points at
   * {@code /primary_email}; {@code getActive()} read {@code active}. Operators can capture any other
   * Freshservice requester JSON field (first_name, last_name, job_title, department_ids, ...) via
   * {@code nativeAttributesEntities} with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Freshservice JSON field is "primary_email"; store it under the friendlier key "email"
          attrConfigWithPath("email", "/primary_email"),
          attrConfig("active")));

  /**
   * Default per-attribute capture list for FreshRequester groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (the group path reads the raw Freshservice requester-group
   * JSON, inner of the envelope). Excludes {@code id} (already the target_group_id column). The
   * Freshservice group JSON field is {@code name}, matching the old {@code getName()} getter.
   * Operators can configure any other Freshservice group JSON field (e.g. description) via
   * {@code nativeAttributesGroups} with a {@code name} (and optional {@code path}).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name")));

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

  // ----- build (raw Freshservice JSON -> native-reporting bean) ------------------------

  /**
   * Build a native group bean from the raw Freshservice requester-group JSON (inner of the
   * {@code requester_group}/{@code requester_groups} envelope). {@code targetId} is read from
   * {@code /id} -- the same field {@link FreshRequesterGroup#fromJson} used for the bean id; the
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
   * Build a native user bean from the raw Freshservice requester JSON (inner of the
   * {@code requester}/{@code requesters} envelope). {@code targetId} is read from {@code /id} -- the
   * same field {@link FreshRequesterUser#fromJson} used for the bean id; the attributes map is
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
   * against the raw Freshservice JSON (group or user) and put the coerced value under
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /name, /active)
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

  /** Build + record a FreshRequester group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a FreshRequester user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate a list of FreshRequester users known to be members of {@code targetGroupId} into
   * native membership beans, and record them. No-op when sync-back is off or input is empty.
   *
   * <p>Unchanged by the move to raw-JSON object capture: Freshservice memberships are group-centric
   * (the per-group member listing returns the member users), so they are still derived from the
   * typed {@link FreshRequesterUser} beans the DAO already has in hand -- the same situation as
   * Adobe's user-{@code groups} derivation.
   */
  public void captureMembershipsForGroup(String targetGroupId, List<FreshRequesterUser> members) {
    if (targetGroupId == null || members == null || members.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (FreshRequesterUser freshRequesterUser : members) {
      if (freshRequesterUser == null || freshRequesterUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(String.valueOf(freshRequesterUser.getId()));
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from FreshRequesterApiCommands read seams) ---------

  /**
   * Capture a FreshRequester group (from its raw inner JSON) against the current provisioner's sync.
   * No-op if there's no current provisioner or the active provisioner isn't a FreshRequester one.
   * Called from the commands read seams ({@code retrieveRequesterGroups}/{@code retrieveRequesterGroup})
   * where the per-group JSON node is in scope.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a FreshRequester user (from its raw inner JSON) against the current provisioner's sync.
   * No-op if there's no current provisioner or the active provisioner isn't a FreshRequester one.
   * Called from the commands read seams ({@code retrieveRequesterUsers}/{@code retrieveRequesterUserById}/
   * {@code retrieveRequesterUserByEmail}/{@code retrieveRequesterUserByAttribute}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
  }

  /**
   * Capture memberships for the given target group id against the current provisioner's sync.
   * Group-centric (see {@link #captureMembershipsForGroup}); unchanged by the JSON object-capture move.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, List<FreshRequesterUser> members) {
    FreshRequesterProvisioningTargetNativeSync sync = freshRequesterSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMembershipsForGroup(targetGroupId, members);
  }

  private static FreshRequesterProvisioningTargetNativeSync freshRequesterSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof FreshRequesterProvisioningTargetNativeSync) {
      return (FreshRequesterProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

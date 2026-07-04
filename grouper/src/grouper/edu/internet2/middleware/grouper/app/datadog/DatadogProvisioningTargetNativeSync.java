package edu.internet2.middleware.grouper.app.datadog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Datadog-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Datadog JSON (JSON Pointer paths,
 * like SCIM/Adobe), hooked at the API-commands seam ({@code DatadogApiCommands.retrieveRoles}/
 * {@code retrieveTeams}/{@code retrieveUsers}/{@code retrieveUserByEmail}/{@code getRoleUsers})
 * where the full JSON node is in scope. This avoids losing any Datadog field that the
 * {@link DatadogGroup} / {@link DatadogUser} typed beans do not model; operators can capture any
 * JSON field via {@code nativeAttributesGroups} / {@code nativeAttributesEntities} with a
 * {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>Datadog speaks JSON:API, so every object arrives as an <i>envelope</i>
 * <code>{ "id": ..., "type": ..., "attributes": { ... } }</code>. The capture seam hands this
 * envelope node straight through, so the default/operator pointers are nested under
 * {@code /attributes} (e.g. {@code /attributes/name}, {@code /attributes/email}) and the target id
 * is the top-level {@code /id} -- exactly where {@link DatadogUser#fromJson}/{@link DatadogGroup#fromJson}
 * read them.
 *
 * <p>{@code groupType} ("role" vs "team") is the one default that is <b>not</b> in the Datadog
 * response JSON -- the commands/DAO set it on the typed bean programmatically based on which
 * endpoint produced the object. To preserve the old behavior (the typed-bean capture wrote
 * {@code groupType}), the commands first overlay the known group type onto a shallow copy of the
 * envelope via {@link #nodeWithGroupType(JsonNode, String)}, so the default pointer
 * {@code /attributes/groupType} resolves. This mirrors the merged-JSON capture used by the Google
 * connector (which assembles a group from two reads before capturing).
 *
 * <p>Memberships are NOT captured from JSON here, but they ARE captured on the WRITE path as well
 * as the read path. On read, the team-membership and role-user beans are recorded by the DAO via
 * {@link #captureTeamMemberships} / {@link #captureRoleMemberships} (those typed-bean helpers are
 * unchanged). On write, {@code DatadogTargetDao.insertMembership}/{@code deleteMembership} record
 * the edge directly into the native membership mirror via
 * {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner} (-> {@code recordTargetNativeMembershipInsert}/
 * {@code recordTargetNativeMembershipDelete}), like Adobe/SCIM. So a membership add/remove is
 * recorded into the mirror on the write and converges on the write pass; only the group/user
 * OBJECT attributes still capture on the read path.
 */
public class DatadogProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Datadog users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Datadog user JSON:API envelope; the real
   * fields live under {@code /attributes}). Excludes {@code id} (already the target_user_id column)
   * and unstable fields. Matches what the old typed-bean capture wrote (handle/email/disabled).
   * Operators can capture any other Datadog user JSON field (name, title, service_account, ...)
   * via {@code nativeAttributesEntities} with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("handle", "/attributes/handle"),
          attrConfigWithPath("email", "/attributes/email"),
          attrConfigWithPath("disabled", "/attributes/disabled")));

  /**
   * Default per-attribute capture list for Datadog groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (the real fields live under {@code /attributes}).
   * Excludes {@code id} (already the target_group_id column). Matches what the old typed-bean
   * capture wrote (name/handle/groupType).
   *
   * <p>{@code groupType} is not in the Datadog response JSON; the commands overlay it onto the
   * envelope before capture (see {@link #nodeWithGroupType(JsonNode, String)}), so the
   * {@code /attributes/groupType} pointer resolves to that injected value.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("name", "/attributes/name"),
          attrConfigWithPath("handle", "/attributes/handle"),
          attrConfigWithPath("groupType", "/attributes/groupType")));

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

  // ----- build (raw Datadog JSON:API envelope -> native-reporting bean) -----------------

  /**
   * Build a native group bean from the raw Datadog group JSON:API envelope. {@code targetId} is
   * read from the top-level {@code /id}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured or default) by JSON
   * Pointer (defaults nested under {@code /attributes}). Returns null when the JSON is missing or
   * has no {@code id}.
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
   * Build a native user bean from the raw Datadog user JSON:API envelope. {@code targetId} is read
   * from the top-level {@code /id}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default) by JSON
   * Pointer (defaults nested under {@code /attributes}). Returns null when the JSON is missing or
   * has no {@code id}.
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
   * against the raw Datadog JSON (group or user envelope) and put the coerced value under
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /attributes/name)
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

  // ----- group-type overlay (the one default not present in the response JSON) ----------

  /**
   * Return a shallow copy of the Datadog group envelope with {@code groupType} written into its
   * {@code attributes} object, so the default {@code /attributes/groupType} pointer resolves.
   * Datadog never returns groupType in the JSON -- the commands know it from which endpoint
   * (roles vs teams) produced the object and overlay it here before capture, the same way the
   * Google connector merges two reads into one node before capturing.
   *
   * <p>Null-safe: if {@code groupNode} is null or not a JSON object, it is returned unchanged
   * (the build path will then simply skip the missing groupType). The original node is never
   * mutated.
   *
   * @param groupNode the per-element JSON:API envelope for a role or team
   * @param groupType "role" or "team", or null/blank to leave the node unchanged
   * @return a copy with attributes.groupType set, or the original node when not applicable
   */
  public static JsonNode nodeWithGroupType(JsonNode groupNode, String groupType) {
    if (groupNode == null || !groupNode.isObject() || StringUtils.isBlank(groupType)) {
      return groupNode;
    }
    ObjectNode copy = (ObjectNode) groupNode.deepCopy();
    JsonNode attributesNode = copy.get("attributes");
    ObjectNode attributesObject;
    if (attributesNode != null && attributesNode.isObject()) {
      attributesObject = (ObjectNode) attributesNode;
    } else {
      // no attributes object in the envelope (or it is null/array) -- create one so the pointer resolves
      attributesObject = GrouperUtil.jsonJacksonNode();
      copy.set("attributes", attributesObject);
    }
    attributesObject.put("groupType", groupType);
    return copy;
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Datadog group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Datadog user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate a list of Datadog memberships for a given target group id into native membership
   * beans and record them. No-op if reporting is off or input is empty. Used for team
   * memberships (DatadogMembership carries the userId). Unchanged by the raw-JSON migration:
   * Datadog has no membership-from-JSON capture; the membership edges are recorded from the
   * already-parsed beans.
   */
  public void captureTeamMemberships(String targetGroupId, List<DatadogMembership> datadogMemberships) {
    if (targetGroupId == null || datadogMemberships == null || datadogMemberships.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (DatadogMembership datadogMembership : datadogMemberships) {
      if (datadogMembership == null || datadogMembership.getUserId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(datadogMembership.getUserId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  /**
   * Translate a list of Datadog users (role members) for a given target group id into native
   * membership beans and record them. Used for role memberships (the Datadog API returns the
   * user list directly). Unchanged by the raw-JSON migration.
   */
  public void captureRoleMemberships(String targetGroupId, List<DatadogUser> roleUsers) {
    if (targetGroupId == null || roleUsers == null || roleUsers.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (DatadogUser datadogUser : roleUsers) {
      if (datadogUser == null || datadogUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(datadogUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from the DatadogApiCommands seam) -------------------

  /**
   * Capture a Datadog group (from its raw JSON:API envelope) against the current provisioner's
   * sync. No-op if there's no current provisioner or the active provisioner isn't a Datadog one.
   * Called from the commands seam ({@code retrieveRoles}/{@code retrieveTeams}/{@code retrieveGroup})
   * for every role/team read. The envelope should already carry {@code groupType} via
   * {@link #nodeWithGroupType(JsonNode, String)}.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Datadog user (from its raw JSON:API envelope) against the current provisioner's sync.
   * No-op if there's no current provisioner or the active provisioner isn't a Datadog one. Called
   * from the commands seam ({@code retrieveUsers}/{@code retrieveUserByEmail}/{@code getRoleUsers}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
  }

  /** Capture team memberships against the current provisioner's sync. */
  public static void captureTeamMembershipsFromCurrentProvisioner(
      String targetGroupId, List<DatadogMembership> datadogMemberships) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureTeamMemberships(targetGroupId, datadogMemberships);
  }

  /** Capture role memberships against the current provisioner's sync. */
  public static void captureRoleMembershipsFromCurrentProvisioner(
      String targetGroupId, List<DatadogUser> roleUsers) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureRoleMemberships(targetGroupId, roleUsers);
  }

  // ----- membership write-track dispatchers (called from DatadogTargetDao write sites) -----
  // Unlike groups/users (re-read to reflect the target), single membership add/remove writes are
  // tracked purely from our own successful DAO calls -- never re-read. The keys are the Datadog
  // target ids: the group id (role or team id) and the user id passed to the membership API call,
  // which match the native group/user targetIds the end-of-run flush reconciles against.

  /**
   * Write-track a successful Datadog membership add ({@code addUserToTeam}/{@code addUserToRole})
   * against the current provisioner: record {@code (groupTargetId, userTargetId)} in the native
   * membership map so the end-of-run flush inserts its grouper_prov_mship row. No-op out of cycle
   * or for a non-Datadog provisioner (and internally no-op when membership sync-back is off).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Datadog membership remove ({@code removeUserFromTeam}/
   * {@code removeUserFromRole}) against the current provisioner: drop
   * {@code (groupTargetId, userTargetId)} from the native membership map so the end-of-run flush
   * deletes its grouper_prov_mship row. No-op out of cycle or for a non-Datadog provisioner (and
   * internally no-op when membership sync-back is off).
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    DatadogProvisioningTargetNativeSync sync = datadogSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  private static DatadogProvisioningTargetNativeSync datadogSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof DatadogProvisioningTargetNativeSync) {
      return (DatadogProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

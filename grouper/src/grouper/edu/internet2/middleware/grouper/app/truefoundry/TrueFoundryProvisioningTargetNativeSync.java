package edu.internet2.middleware.grouper.app.truefoundry;

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
 * TrueFoundry-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw TrueFoundry JSON (JSON Pointer
 * paths, like SCIM / Adobe / Duo), hooked at the API-commands seam ({@code TrueFoundryApiCommands}
 * read methods such as {@code retrieveSubjectsData} / {@code retrieveUsers} /
 * {@code retrieveUserByEmail} for users and {@code retrieveRoles} / {@code retrieveTeams} /
 * {@code getTeamById} for groups) where the per-object JSON node is in scope. This avoids losing
 * any TrueFoundry field that the {@link TrueFoundryUser} / {@link TrueFoundryGroup} typed beans do
 * not model; operators can capture any JSON field via {@code nativeAttributesGroups} /
 * {@code nativeAttributesEntities} with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>The default keys are chosen so each default's captured value matches what the OLD typed-bean
 * getter returned:
 * <ul>
 *   <li>users: target id from {@code /id} (the same field {@code TrueFoundryUser.fromJson} reads
 *       into {@code id} / the old build read via {@code getId()}); defaults {@code email}
 *       (from {@code /email}) and {@code active} (from {@code /active}, a JSON boolean).</li>
 *   <li>groups: target id from {@code /id}; defaults {@code name} and {@code groupType}.</li>
 * </ul>
 *
 * <p><b>TrueFoundry group nodes are not uniform</b>, so the commands seam hands this class a
 * <i>normalized</i> capture node rather than the raw API node (see
 * {@code TrueFoundryApiCommands.normalizeTeamJsonForCapture} /
 * {@code normalizeRoleJsonForCapture}). Two fields the typed beans synthesize have no single raw
 * JSON field and so are injected onto that capture node before it reaches us:
 * <ul>
 *   <li>{@code groupType} is not a TrueFoundry JSON field at all -- {@code fromTeamJson} /
 *       {@code fromRoleJson} hard-code it to {@code "team"} / {@code "role"}. The seam stamps it on
 *       the capture node so the {@code /groupType} default resolves (matching the old
 *       {@code getGroupType()} value).</li>
 *   <li>{@code name} lives at {@code /teamName} for a team but at {@code /name} for a role. The
 *       seam normalizes both into a top-level {@code name} field on the capture node so the
 *       {@code /name} default resolves uniformly (matching the old {@code getName()} value).</li>
 * </ul>
 * Every default / pointer is therefore relative to that normalized node, consistently. All other
 * raw fields (e.g. {@code teamName}, {@code resourceType}, {@code isDefault}, the {@code manifest}
 * subtree) are preserved verbatim on the capture node, so an operator can capture any of them --
 * including fields the typed beans never model -- via {@code nativeAttributesGroups}.
 *
 * <p>Memberships are still derived from the {@link TrueFoundryGroup} typed bean's {@code members}
 * list during DAO translation ({@link #captureMembershipsFromGroup} and the
 * {@code ...UsingCapturedUsers} variant), because TrueFoundry membership is group-centric and
 * carried inside the team object (member emails translated to native user ids) -- the Adobe
 * situation. Only the group/user object capture is JSON-based.
 */
public class TrueFoundryProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for TrueFoundry users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw TrueFoundry user JSON). Excludes
   * {@code id} (already the target_user_id column). Operators can capture any other user JSON field
   * (e.g. {@code displayName} at {@code /metadata/displayName}) via {@code nativeAttributesEntities}
   * with a {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // matches the old GrouperProvisioningTargetNativeUser built from TrueFoundryUser.getEmail()
          attrConfig("email"),
          // matches the old build from TrueFoundryUser.getActive() (a JSON boolean at /active)
          attrConfig("active")));

  /**
   * Default per-attribute capture list for TrueFoundry groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the normalized TrueFoundry group capture node).
   * Excludes {@code id} (already the target_group_id column) and large/embedded fields like
   * {@code manifest.members} / {@code manifest.managers}. {@code name} and {@code groupType} are
   * normalized onto the capture node by the commands seam (see class javadoc). Operators can
   * configure any other group JSON field (e.g. {@code teamName}, {@code resourceType},
   * {@code isDefault}) via {@code nativeAttributesGroups} with a {@code name} and optional
   * {@code path}, since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // matches the old build from TrueFoundryGroup.getName()
          attrConfig("name"),
          // matches the old build from TrueFoundryGroup.getGroupType() ("team" / "role");
          // groupType is synthesized onto the capture node by the commands seam
          attrConfig("groupType")));

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

  // ----- build (raw TrueFoundry JSON -> native-reporting bean) -------------------------

  /**
   * Build a native group bean from the (normalized) raw TrueFoundry group JSON. {@code targetId}
   * is read from {@code /id} (the same field the old typed-bean build read via {@code getId()});
   * the attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code id}.
   * @param groupNode the normalized group capture node (see class javadoc); for teams/roles this
   *                  is the raw API node with {@code name} and {@code groupType} stamped on
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
   * Build a native user bean from the raw TrueFoundry user JSON. {@code targetId} is read from
   * {@code /id} (the same field {@code TrueFoundryUser.fromJson} reads and the old build used via
   * {@code getId()}); the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code id}.
   * @param userNode a user object as it appears inside the subjects {@code users[]} array
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
   * against the raw TrueFoundry JSON (group or user) and put the coerced value under
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /email, /name)
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

  /**
   * Build + record a TrueFoundry group from its (normalized) raw JSON. No-op when sync-back is off
   * or the node is null / id-less.
   */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /**
   * Build + record a TrueFoundry user from its raw JSON. No-op when sync-back is off or the node is
   * null / id-less.
   */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
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

  // ----- static dispatchers (called from TrueFoundryApiCommands / TrueFoundryTargetDao) -

  /**
   * Capture a TrueFoundry group (from its normalized raw JSON) against the current provisioner's
   * sync. No-op if there's no current provisioner or the active provisioner isn't a TrueFoundry
   * one. Called from the commands seam (TrueFoundryApiCommands read methods) for every team/role
   * parsed from a read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a TrueFoundry user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a TrueFoundry one. Called from
   * the commands seam (TrueFoundryApiCommands read methods) for every user parsed from a read.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
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

  // ----- membership write-track dispatchers (called from TrueFoundryTargetDao write sites) -----
  // Unlike groups/users (re-read via the drain to reflect the target), memberships are tracked
  // purely from our own successful add/remove/replace writes -- never re-read -- because they are
  // high-volume. The keys are the TrueFoundry target ids: group id (getProvisioningGroupId) and
  // user id (getProvisioningEntityId), which match the native group/user targetIds the flush
  // reconciles against. Each dispatcher is a no-op out of cycle / for a non-TrueFoundry provisioner,
  // and the underlying record* methods are themselves no-ops when membership sync-back is off.

  /**
   * Write-track a successful TrueFoundry membership add against the current provisioner: record
   * {@code (groupTargetId, userTargetId)} in the native membership map. No-op out of cycle or for a
   * non-TrueFoundry provisioner. Called per written membership from the DAO insert success path.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful TrueFoundry membership remove against the current provisioner: drop
   * {@code (groupTargetId, userTargetId)} from the native membership map so the end-of-run flush
   * deletes its grouper_prov_mship row. No-op out of cycle or for a non-TrueFoundry provisioner.
   * Called per written membership from the DAO delete success path.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful TrueFoundry replace-all-memberships against the current provisioner:
   * set the native membership map for {@code groupTargetId} to exactly {@code userTargetIds}. No-op
   * out of cycle or for a non-TrueFoundry provisioner. Called from the DAO replaceGroupMemberships
   * success path with the group's full desired user-id set.
   */
  public static void captureMembershipReplaceFromCurrentProvisioner(
      String groupTargetId, java.util.Collection<String> userTargetIds) {
    TrueFoundryProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipReplace(groupTargetId, userTargetIds);
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

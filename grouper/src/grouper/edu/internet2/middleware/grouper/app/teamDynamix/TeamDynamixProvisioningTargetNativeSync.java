package edu.internet2.middleware.grouper.app.teamDynamix;

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
 * TeamDynamix-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw TeamDynamix JSON (JSON Pointer
 * paths, like SCIM/Adobe), hooked at the API-commands seam ({@code TeamDynamixApiCommands}) where
 * the full JSON node is in scope. This avoids losing any TeamDynamix field that the
 * {@link TeamDynamixGroup} / {@link TeamDynamixUser} typed beans do not model; operators can
 * capture any JSON field via {@code nativeAttributesGroups} / {@code nativeAttributesEntities}
 * with a {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>TeamDynamix JSON uses PascalCase keys ({@code Name}, {@code PrimaryEmail}, {@code IsActive},
 * ...) and reads the object id from {@code ID} (groups) / {@code UID} (users), matching what
 * {@link TeamDynamixGroup#fromJson} / {@link TeamDynamixUser#fromJson} parse. The default capture
 * lists therefore carry explicit JSON-Pointer paths so the stored attribute keys stay friendly
 * ({@code name}, {@code userName}, {@code primaryEmail}, {@code active}) while reading the
 * PascalCase source fields.
 *
 * <p>Memberships capture on BOTH paths. On the READ path they are derived from the typed beans
 * during DAO translation ({@link #captureMembershipsForGroup}): TeamDynamix membership is
 * group-centric (the members of a group are retrieved per-group, not inline with a user object),
 * so -- as with Adobe -- only the group/user object capture moved to raw JSON; the read-side
 * membership capture is unchanged. On the WRITE path a membership add/remove is recorded into the
 * native mirror at the moment of the write: {@link TeamDynamixTargetDao#insertMemberships} /
 * {@code deleteMemberships} call {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner}, which invoke
 * {@link #recordTargetNativeMembershipInsert} / {@link #recordTargetNativeMembershipDelete}. So,
 * like Adobe/SCIM, TeamDynamix is a capture-on-write target for memberships and a membership
 * change converges on the write pass (the group/user OBJECTS still capture on the read path).
 */
public class TeamDynamixProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for TeamDynamix users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw TeamDynamix user JSON). Excludes the id
   * (TeamDynamix {@code UID}, already the target_user_id column). The stored keys match what the
   * old typed-bean capture wrote ({@code userName}, {@code primaryEmail}, {@code active}); each
   * points at the PascalCase TeamDynamix JSON field {@link TeamDynamixUser#fromJson} reads.
   * Operators can capture any other TeamDynamix user JSON field (FirstName, LastName, Company,
   * SecurityRoleID, ExternalID, ...) via {@code nativeAttributesEntities} with a {@code name} and
   * optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // TeamDynamix JSON field is "UserName"; store under the friendlier key "userName"
          attrConfigWithPath("userName", "/UserName"),
          // TeamDynamix JSON field is "PrimaryEmail"; store under "primaryEmail"
          attrConfigWithPath("primaryEmail", "/PrimaryEmail"),
          // TeamDynamix JSON field is "IsActive" (boolean); store under "active"
          attrConfigWithPath("active", "/IsActive")));

  /**
   * Default per-attribute capture list for TeamDynamix groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw TeamDynamix group JSON). Excludes the id
   * (TeamDynamix {@code ID}, already the target_group_id column). The stored key matches what the
   * old typed-bean capture wrote ({@code name}); it points at the PascalCase TeamDynamix JSON field
   * {@link TeamDynamixGroup#fromJson} reads. Operators can configure any other TeamDynamix group
   * JSON field (e.g. {@code Description}) via {@code nativeAttributesGroups} with a {@code name}
   * (and optional {@code path}), since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // TeamDynamix JSON field is "Name"; store under the friendlier key "name"
          attrConfigWithPath("name", "/Name")));

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

  // ----- build (raw TeamDynamix JSON -> native-reporting bean) --------------------------

  /**
   * Build a native group bean from the raw TeamDynamix group JSON. {@code targetId} is read from
   * {@code /ID} (the same field {@link TeamDynamixGroup#fromJson} uses for the bean id); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code ID}.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/ID");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw TeamDynamix user JSON. {@code targetId} is read from
   * {@code /UID} (the same field {@link TeamDynamixUser#fromJson} uses for the bean id); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code UID}.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/UID");
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
   * against the raw TeamDynamix JSON (group or user) and put the coerced value under
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /Description)
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

  /** Build + record a TeamDynamix group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a TeamDynamix user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Record native memberships for a TeamDynamix group, given the list of users that belong to it.
   * No-op if reporting is off or the input is empty. TeamDynamix membership is group-centric
   * (members are read per-group), so -- unlike the group/user objects, which capture from raw JSON
   * -- memberships are still derived from the typed beans during DAO translation.
   */
  public void captureMembershipsForGroup(String targetGroupId, List<TeamDynamixUser> teamDynamixUsers) {
    if (targetGroupId == null || teamDynamixUsers == null || teamDynamixUsers.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {
      if (teamDynamixUser == null || teamDynamixUser.getId() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(teamDynamixUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from TeamDynamixApiCommands / TeamDynamixTargetDao) -----

  /**
   * Capture a TeamDynamix group (from its raw JSON) against the current provisioner's sync. No-op
   * if there's no current provisioner or the active provisioner isn't a TeamDynamix one. Called
   * from the commands seam ({@code TeamDynamixApiCommands}) for every group read off the target.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a TeamDynamix user (from its raw JSON) against the current provisioner's sync. No-op
   * if there's no current provisioner or the active provisioner isn't a TeamDynamix one. Called
   * from the commands seam ({@code TeamDynamixApiCommands}) for every user read off the target.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
  }

  /**
   * Record native memberships for a TeamDynamix group on the current provisioner.
   */
  public static void captureMembershipsForGroupForCurrentProvisioner(
      String targetGroupId, List<TeamDynamixUser> teamDynamixUsers) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMembershipsForGroup(targetGroupId, teamDynamixUsers);
  }

  // ----- membership write-track dispatchers (called from TeamDynamixTargetDao write sites) -----
  // Unlike groups/users (re-read via the drain to reflect the target), memberships are tracked
  // purely from our own successful add/remove writes -- never re-read -- because they are
  // high-volume. The keys are the TeamDynamix target ids: the group id and the user id the DAO
  // passes to createTeamDynamixMemberships / deleteTeamDynamixMemberships, which match the native
  // group/user targetIds the end-of-run flush reconciles against.

  /**
   * Write-track a successful TeamDynamix membership add ({@code createTeamDynamixMemberships})
   * against the current provisioner: record {@code (groupTargetId, userTargetId)} in the native
   * membership map. No-op out of cycle or for a non-TeamDynamix provisioner.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful TeamDynamix membership remove ({@code deleteTeamDynamixMemberships})
   * against the current provisioner: drop {@code (groupTargetId, userTargetId)} from the native
   * membership map so the end-of-run flush deletes its grouper_prov_mship row. No-op out of cycle
   * or for a non-TeamDynamix provisioner.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    TeamDynamixProvisioningTargetNativeSync sync = teamDynamixSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  private static TeamDynamixProvisioningTargetNativeSync teamDynamixSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof TeamDynamixProvisioningTargetNativeSync) {
      return (TeamDynamixProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

package edu.internet2.middleware.grouper.app.dropbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Dropbox-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting beans
 * for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>This mirrors {@code TrueFoundryProvisioningTargetNativeSync}. Both <b>groups</b> and
 * <b>users</b> are captured from the raw Dropbox JSON (via JSON-Pointer paths, like SCIM / Adobe /
 * Duo / TrueFoundry), hooked at the API-commands seam ({@code DropboxApiCommands} read methods such
 * as {@code retrieveDropboxGroups} / {@code retrieveDropboxGroup} for groups and
 * {@code retrieveDropboxUsers} / {@code retrieveDropboxUser} for users) where the per-object JSON
 * node is in scope. Capturing the raw JSON -- rather than the lossy {@link DropboxGroup} /
 * {@link DropboxUser} typed beans -- means an operator can capture any Dropbox field via
 * {@code nativeAttributesGroups} / {@code nativeAttributesEntities} with a {@code name} and optional
 * JSON-Pointer {@code path}, including fields the typed beans never model.</p>
 *
 * <p><b>Dropbox JSON is uniform across endpoints</b> (unlike TrueFoundry's team-vs-role nodes), so
 * the commands seam performs only an <i>identity</i> normalization (a defensive deep copy) before
 * handing the node here -- there is no field to alias and no {@code groupType} to synthesize. Every
 * default / pointer below is therefore relative to the raw Dropbox node:</p>
 * <ul>
 *   <li>groups: target id from {@code /group_id}; default attributes {@code name}
 *       (from {@code /group_name}) and {@code externalId} (from {@code /group_external_id}, the
 *       Grouper match key).</li>
 *   <li>users: target id from {@code /profile/team_member_id} (members/list_v2 and get_info_v2 both
 *       wrap the profile); default attributes {@code email} (from {@code /profile/email}) and
 *       {@code status} (from {@code /profile/status/.tag}, a Dropbox union tag).</li>
 * </ul>
 *
 * <p>Memberships are mirrored to grouper_prov_mship via a single native membership map kept current
 * from two sources: every membership READ records "this membership exists" (via
 * {@link #captureMembershipInsertFromCurrentProvisioner}, called from the membership-read seam in
 * {@code DropboxApiCommands.retrieveDropboxGroupMemberships}) so a full sync re-populates the map each
 * pass and the end-of-run global reconcile keeps the rows; and every successful WRITE adjusts it --
 * {@link DropboxTargetDao} calls the same insert dispatcher after a groups/members/add and
 * {@link #captureMembershipDeleteFromCurrentProvisioner} after a groups/members/remove -- so an
 * incremental add/remove adjusts grouper_prov_mship precisely with no read-lag.</p>
 */
public class DropboxProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Dropbox users when {@code nativeAttributesEntities} is
   * not configured. JSON-Pointer based (reads the raw Dropbox member JSON, which wraps the profile).
   * Excludes the target id (already the target_user_id column). Operators can capture any other
   * member field (e.g. {@code /profile/external_id}, {@code /profile/account_id}) via
   * {@code nativeAttributesEntities}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // member email lives under the profile wrapper
          attrConfigWithPath("email", "/profile/email"),
          // status is a Dropbox union; the human-readable value is its ".tag"
          attrConfigWithPath("status", "/profile/status/.tag")));

  /**
   * Default per-attribute capture list for Dropbox groups when {@code nativeAttributesGroups} is not
   * configured. JSON-Pointer based (reads the raw Dropbox group JSON). Excludes the target id
   * (already the target_group_id column). Operators can capture any other group field (e.g.
   * {@code /group_management_type/.tag}, {@code /member_count}) via {@code nativeAttributesGroups}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("name", "/group_name"),
          // externalId is Grouper's match key on the group
          attrConfigWithPath("externalId", "/group_external_id")));

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path}. When {@code path} is
   * null the JSON path defaults to {@code "/" + name} (see {@link #populateAttributesFromJson}).
   * @param name attribute name stored in the captured native bean
   * @param path JSON Pointer into the raw Dropbox node, or null for {@code "/" + name}
   * @return the attribute config
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

  // ----- build (raw Dropbox JSON -> native-reporting bean) -----------------------------

  /**
   * Build a native group bean from the raw Dropbox group JSON. {@code targetId} is read from
   * {@code /group_id} (the same field {@code DropboxGroup.fromJson} reads); the attributes map is
   * populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured
   * or default) by JSON Pointer. Returns null when the JSON is missing or has no {@code group_id}.
   * @param groupNode a raw Dropbox group object
   * @return the native group bean, or null
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/group_id");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw Dropbox member JSON. {@code targetId} is read from
   * {@code /profile/team_member_id} (the profile wrapper present on members/list_v2 and
   * get_info_v2); the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default) by JSON
   * Pointer. Returns null when the JSON is missing or has no {@code team_member_id}.
   * @param memberNode a raw Dropbox member object (with a {@code profile} child)
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode memberNode) {
    if (memberNode == null || memberNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(memberNode, "/profile/team_member_id");
    if (targetId == null) {
      // tolerate a bare profile node (no wrapper) just in case
      targetId = resolveScalarAsString(memberNode, "/team_member_id");
    }
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), memberNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer ({@code path}, or {@code "/" + name})
   * against the raw Dropbox JSON (group or member) and put the coerced value under
   * {@code cfg.getName()}. Missing / null nodes are skipped (no attribute row written).
   * @param destinationAttributes the bean attributes map to populate
   * @param resourceNode the raw Dropbox node
   * @param nativeAttributeConfigs the effective attribute configs
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name
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
   * @param node the JSON node to coerce
   * @param declaredType the operator-declared type, or null to auto-detect
   * @return the coerced scalar, or null
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
   * Build + record a Dropbox group from its raw JSON. No-op when sync-back is off or the node is
   * null / id-less.
   * @param groupNode the raw Dropbox group node
   */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /**
   * Build + record a Dropbox user from its raw JSON. No-op when sync-back is off or the node is
   * null / id-less.
   * @param memberNode the raw Dropbox member node
   */
  public void captureUserJson(JsonNode memberNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(memberNode));
  }

  // ----- static dispatchers (called from DropboxApiCommands / DropboxTargetDao) --------

  /**
   * Capture a Dropbox group (from its raw JSON) against the current provisioner's sync. No-op if
   * there is no current provisioner or the active provisioner is not a Dropbox one. Called from the
   * commands seam for every group parsed from a read.
   * @param groupNode the raw Dropbox group node
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    DropboxProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Dropbox user (from its raw JSON) against the current provisioner's sync. No-op if
   * there is no current provisioner or the active provisioner is not a Dropbox one. Called from the
   * commands seam for every member parsed from a read.
   * @param memberNode the raw Dropbox member node
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode memberNode) {
    DropboxProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(memberNode);
  }

  /**
   * Record that a Dropbox group membership EXISTS: put {@code (targetGroupId, targetUserId)} in the
   * native membership map so the end-of-run flush writes its grouper_prov_mship row. No-op out of
   * cycle, for a non-Dropbox provisioner, or when membership sync-back is off. Called both from the
   * membership-read seam ({@code DropboxApiCommands.retrieveDropboxGroupMemberships}, to re-populate
   * the map on a full sync) and from {@code DropboxTargetDao.insertMemberships} after a successful add
   * (so an incremental add adjusts sync-back immediately).
   * @param targetGroupId the native group_id
   * @param targetUserId the native team_member_id
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    DropboxProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
  }

  /**
   * Write-track a successful Dropbox group-member REMOVE (groups/members/remove) against the current
   * provisioner: drop {@code (targetGroupId, targetUserId)} from the native membership map so the
   * end-of-run flush deletes its grouper_prov_mship row. No-op out of cycle, for a non-Dropbox
   * provisioner, or when membership sync-back is off. Called from
   * {@code DropboxTargetDao.deleteMemberships} after the remove succeeds.
   * @param targetGroupId the native group_id
   * @param targetUserId the native team_member_id
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    DropboxProvisioningTargetNativeSync sync = syncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
  }

  private static DropboxProvisioningTargetNativeSync syncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof DropboxProvisioningTargetNativeSync) {
      return (DropboxProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

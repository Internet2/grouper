package edu.internet2.middleware.grouper.app.duo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Duo-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Duo JSON (JSON Pointer paths,
 * like SCIM and Adobe), hooked at the API-commands seam ({@code GrouperDuoApiCommands} read
 * methods such as {@code retrieveDuoGroups} / {@code retrieveDuoUsers} / {@code retrieveDuoUser})
 * where the per-element JSON node is in scope. This avoids losing any Duo field that the
 * {@link GrouperDuoGroup} / {@link GrouperDuoUser} typed beans do not model; operators can capture
 * any JSON field via {@code nativeAttributesGroups} / {@code nativeAttributesEntities} with a
 * {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>The default keys are chosen so each default's captured value matches what the OLD typed-bean
 * getter returned: the Duo user JSON field is {@code username} (stored under the friendlier key
 * {@code userName}, matching {@code GrouperDuoUser.getUserName()}), and the target ids come from
 * {@code /user_id} (user) and {@code /group_id} (group) -- the same JSON fields the old
 * typed-bean build methods read via {@code getId()} / {@code getGroup_id()}.
 *
 * <p>Memberships are still derived from the {@link GrouperDuoUser} typed bean's inline
 * {@code groups} set ({@link #captureMembershipsFromUser}); each {@link GrouperDuoGroup} in that
 * set carries its own {@code group_id}, so no name->id index resolution is needed. Only the
 * group/user object capture is JSON-based.
 */
public class GrouperDuoProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Duo users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Duo user JSON). Excludes {@code user_id}
   * (already the target_user_id column). Operators can capture any other Duo user JSON field
   * (firstname, lastname, realname, notes, ...) via {@code nativeAttributesEntities} with a
   * {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Duo JSON field is "username"; store it under the friendlier key "userName"
          // (matches the old GrouperDuoUser.getUserName() default)
          attrConfigWithPath("userName", "/username"),
          attrConfig("email"),
          attrConfig("status")));

  /**
   * Default per-attribute capture list for Duo groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw Duo group JSON). Excludes
   * {@code group_id} (already the target_group_id column). Operators can configure any other Duo
   * group JSON field (desc, status, ...) via {@code nativeAttributesGroups} with a {@code name}
   * (and optional {@code path}), since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Duo JSON field is "name" (matches the old GrouperDuoGroup.getName() default)
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

  // ----- build (raw Duo JSON -> native-reporting bean) ---------------------------------

  /**
   * Build a native group bean from the raw Duo group JSON. {@code targetId} is read from
   * {@code /group_id} (the same field the old typed-bean build read via getGroup_id()); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code group_id}.
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
   * Build a native user bean from the raw Duo user JSON. {@code targetId} is read from
   * {@code /user_id} (the same field the old typed-bean build read via getId()); the attributes
   * map is populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code user_id}.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/user_id");
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
   * against the raw Duo JSON (group or user) and put the coerced value under {@code cfg.getName()}.
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
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /email, /status)
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

  /** Build + record a Duo group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Duo user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate a Duo user's inline {@code groups} set ({@link GrouperDuoGroup} beans with
   * their own {@code group_id}) into native membership beans and record them. No-op if
   * reporting is off or the user has no groups.
   */
  public void captureMembershipsFromUser(GrouperDuoUser grouperDuoUser) {
    if (grouperDuoUser == null || grouperDuoUser.getId() == null) {
      return;
    }
    Set<GrouperDuoGroup> userGroups = grouperDuoUser.getGroups();
    if (userGroups == null || userGroups.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (GrouperDuoGroup duoGroup : userGroups) {
      if (duoGroup == null || duoGroup.getGroup_id() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(duoGroup.getGroup_id());
      membership.setTargetUserId(grouperDuoUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperDuoApiCommands / GrouperDuoTargetDao) ----

  /**
   * Capture a Duo group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Duo one. Called from the
   * commands seam (GrouperDuoApiCommands read methods) for every group parsed from a read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Duo user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Duo one. Called from the
   * commands seam (GrouperDuoApiCommands read methods) for every user parsed from a read.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureUserJson(userNode);
  }

  /**
   * Translate a Duo user's inline {@code groups} set into native membership records and
   * record them on the current provisioner.
   */
  public static void captureMembershipsFromUserForCurrentProvisioner(GrouperDuoUser grouperDuoUser) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureMembershipsFromUser(grouperDuoUser);
  }

  private static GrouperDuoProvisioningTargetNativeSync duoSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperDuoProvisioningTargetNativeSync) {
      return (GrouperDuoProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

package edu.internet2.middleware.grouper.app.scim2Provisioning;

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
 * SCIM-specific {@link GrouperProvisioningTargetNativeSync}: builds native-target reporting
 * beans directly from the SCIM JSON resource and drains the {@link GrouperScim2MembershipCache}
 * populated as a side-effect of {@code retrieveScimGroups} / {@code retrieveScimUsers} into
 * the native memberships list.
 *
 * <p>Capture is hooked at {@link GrouperScim2ApiCommands} (the protocol-I/O seam) rather
 * than in the DAO, because SCIM API methods are axis-typed. The static
 * {@code …FromCurrentProvisioner} dispatchers absorb the ThreadLocal lookup + instanceof
 * check so call sites stay 1 line.
 *
 * <p>Attribute capture is config-driven, not "everything in the JSON":
 * <ul>
 *   <li>If the operator has set {@code nativeAttributesEntities} / {@code nativeAttributesGroups}
 *       in the provisioner config, those exact paths are captured.</li>
 *   <li>If blank, a curated SCIM core-schema default list ({@link #DEFAULT_ENTITY_ATTRS} /
 *       {@link #DEFAULT_GROUP_ATTRS}) is used. The defaults intentionally do NOT include
 *       {@code id} since that is already the {@code target_user_id} / {@code target_group_id}
 *       column on the prov_* row.</li>
 * </ul>
 */
public class GrouperScim2ProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for SCIM users when {@code nativeAttributesEntities}
   * is not configured. Five SCIM core-schema fields that operators commonly use for
   * reconciliation. Excludes {@code id} (already the target_user_id column) and
   * non-essential fields like {@code phoneNumber}, {@code title}, {@code department},
   * {@code /meta/*} (opt-in via config).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("userName", null),
          attrConfig("displayName", null),
          attrConfig("active", "boolean"),
          attrConfig("externalId", null),
          // givenName/familyName/formattedName are managed (Grouper writes/compares them) and nested
          // under /name in SCIM 2.0 core -- capture them so a cache-reconstructed user matches a live
          // read and does not trigger a spurious update every from-cache run (the same managed-
          // attribute contract Google needs). A deployment that manages OTHER attributes must add
          // them via nativeAttributesEntities; the from-cache guardrail warns about any it finds
          // missing.
          attrConfigWithPath("givenName", "/name/givenName", null),
          attrConfigWithPath("familyName", "/name/familyName", null),
          attrConfigWithPath("formattedName", "/name/formatted", null),
          // first email value/type are at /emails/0/value and /emails/0/type in SCIM 2.0 core
          attrConfigWithPath("emailValue", "/emails/0/value", null),
          attrConfigWithPath("emailType", "/emails/0/type", null)));

  /**
   * Default per-attribute capture list for SCIM groups when {@code nativeAttributesGroups}
   * is not configured. Excludes {@code id} (already target_group_id) and {@code /meta/*}
   * (opt-in via config).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("displayName", null),
          attrConfig("externalId", null)));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setType(type);
    return cfg;
  }

  private static GrouperProvisioningNativeAttributeConfig attrConfigWithPath(
      String name, String path, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
    cfg.setType(type);
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

  // ----- build (JsonNode → native-reporting bean) --------------------------------------

  /**
   * Build a native group bean from the SCIM JSON resource. {@code targetId} is read from
   * {@code /id}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsGroups()} (operator-configured or default).
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode resourceNode) {
    if (resourceNode == null || resourceNode.isMissingNode()) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    String id = resolveScalarAsString(resourceNode, "/id");
    if (id == null) {
      return null;
    }
    bean.setTargetId(id);
    populateAttributesFromJson(bean.getAttributes(), resourceNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the SCIM JSON resource. {@code targetId} is read from
   * {@code /id}; the attributes map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsEntities()} (operator-configured or default).
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode resourceNode) {
    if (resourceNode == null || resourceNode.isMissingNode()) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    String id = resolveScalarAsString(resourceNode, "/id");
    if (id == null) {
      return null;
    }
    bean.setTargetId(id);
    populateAttributesFromJson(bean.getAttributes(), resourceNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * For each config entry, resolve its JSON Pointer against the resource and put the
   * coerced value into {@code destinationAttributes} under {@code cfg.getName()}.
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
      String pointer = StringUtils.defaultIfBlank(cfg.getPath(), "/" + cfg.getName());
      // JSON Pointer per RFC 6901; "/" + name produces /userName, /externalId, etc.
      JsonNode node = resourceNode.at(pointer);
      if (node == null || node.isMissingNode() || node.isNull()) {
        continue;
      }
      Object value = coerceJsonValue(node, cfg.getType());
      if (value == null) {
        continue;
      }
      destinationAttributes.put(cfg.getName(), value);
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
   * Coerce a JsonNode to a scalar Object suitable for storage in the attribute map. The
   * declared type ({@code "string"|"integer"|"boolean"|"timestamp"}) wins when present;
   * otherwise the node's intrinsic JSON type drives the choice.
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
      // store as the source string; downstream coercion to Timestamp handled by the dictionary path
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

  /** Build a native group bean from the SCIM JSON and record it. No-op if reporting is off. */
  public void captureGroupJson(JsonNode resourceNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(resourceNode));
  }

  /**
   * Group-insert sync-back: build the native group from the create (POST) response JSON and
   * run it through the generic insert hook. SCIM echoes the created resource on POST, so this
   * registers it "like a read" (no re-read needed) and the drain skips the id.
   */
  public void recordGroupInsertJson(JsonNode resourceNode) {
    GrouperProvisioningTargetNativeGroup nativeGroup = this.buildNativeGroupFromJson(resourceNode);
    if (nativeGroup == null) {
      return;
    }
    this.recordTargetNativeGroupWrite(nativeGroup.getTargetId(), nativeGroup);
  }

  /** Build a native user bean from the SCIM JSON and record it. No-op if reporting is off. */
  public void captureEntityJson(JsonNode resourceNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(resourceNode));
  }

  /**
   * Entity-insert sync-back: build the native user from the create (POST) response JSON and
   * run it through the generic insert hook. SCIM echoes the created resource on POST, so this
   * registers it "like a read" (no re-read needed) and the drain skips the id.
   */
  public void recordUserInsertJson(JsonNode resourceNode) {
    GrouperProvisioningTargetNativeUser nativeUser = this.buildNativeUserFromJson(resourceNode);
    if (nativeUser == null) {
      return;
    }
    this.recordTargetNativeUserWrite(nativeUser.getTargetId(), nativeUser);
  }

  /**
   * Translate the {@link GrouperScim2MembershipCache}'s {@code groupId → userIds} map into
   * {@link GrouperProvisioningTargetNativeMembership} records and record them in bulk.
   * No-op if the cache is empty or reporting is off.
   */
  public void captureMembershipsFromCache(GrouperScim2MembershipCache grouperScim2MembershipCache) {
    if (grouperScim2MembershipCache == null) {
      return;
    }
    Map<String, Set<String>> groupIdToMembershipUserIds = grouperScim2MembershipCache.getGroupIdToMembershipUserIds();
    if (groupIdToMembershipUserIds == null || groupIdToMembershipUserIds.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (Map.Entry<String, Set<String>> entry : groupIdToMembershipUserIds.entrySet()) {
      String groupId = entry.getKey();
      Set<String> userIds = entry.getValue();
      if (StringUtils.isBlank(groupId) || userIds == null) {
        continue;
      }
      // Iterate a point-in-time snapshot rather than the live set. userIds is a
      // Collections.synchronizedSet (see GrouperScim2MembershipCache.addMembership) whose backing
      // HashSet is still being written by concurrent retrieve threads. A synchronizedSet only
      // guards individual method calls, not iteration, so iterating it live throws
      // ConcurrentModificationException. new ArrayList<>(userIds) copies via the set's synchronized
      // toArray(), yielding a safe snapshot to iterate.
      for (String userId : new ArrayList<String>(userIds)) {
        if (StringUtils.isBlank(userId)) {
          continue;
        }
        GrouperProvisioningTargetNativeMembership grouperProvisioningTargetNativeMembership =
            new GrouperProvisioningTargetNativeMembership();
        grouperProvisioningTargetNativeMembership.setTargetGroupId(groupId);
        grouperProvisioningTargetNativeMembership.setTargetUserId(userId);
        memberships.add(grouperProvisioningTargetNativeMembership);
      }
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from the SCIM commands class) -----------------------

  /**
   * Capture a SCIM user JSON resource against the current provisioner's sync. No-op if
   * there's no current provisioner (e.g. an out-of-cycle CLI call) or if the active
   * provisioner isn't a SCIM one (defensive — shouldn't happen in practice since these
   * dispatchers are only called from the SCIM commands class).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.captureEntityJson(resourceNode);
  }

  /** Run a SCIM user create (POST) response through the insert sync-back hook. */
  public static void captureUserInsertFromCurrentProvisioner(JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordUserInsertJson(resourceNode);
  }

  /**
   * SCIM group-update sync-back (uniform write rule): if the update response carries the
   * resource, register it like a read so the end-of-run drain skips it; if there is no body
   * (e.g. a 204), {@code buildNativeGroupFromJson} returns null and the id is only marked for
   * the drain to re-read. Either way the stale pre-update read-pass native is dropped.
   */
  public static void captureGroupUpdateFromCurrentProvisioner(String targetId, JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordTargetNativeGroupWrite(targetId, scimSync.buildNativeGroupFromJson(resourceNode));
  }

  /** see {@link #captureGroupUpdateFromCurrentProvisioner}; user axis */
  public static void captureUserUpdateFromCurrentProvisioner(String targetId, JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordTargetNativeUserWrite(targetId, scimSync.buildNativeUserFromJson(resourceNode));
  }

  /** Capture a SCIM group JSON resource against the current provisioner's sync. */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.captureGroupJson(resourceNode);
  }

  /** Run a SCIM group create (POST) response through the insert sync-back hook. */
  public static void captureGroupInsertFromCurrentProvisioner(JsonNode resourceNode) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordGroupInsertJson(resourceNode);
  }

  /** Drain a SCIM membership cache into the current provisioner's native memberships list. */
  public static void captureMembershipsFromCacheIfActive(GrouperScim2MembershipCache grouperScim2MembershipCache) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.captureMembershipsFromCache(grouperScim2MembershipCache);
  }

  /**
   * Record a successful SCIM membership add (PATCH op=add) against the current provisioner.
   * No-op out of cycle or for a non-SCIM provisioner. Memberships are tracked from writes only
   * (never re-read) -- see {@link GrouperProvisioningTargetNativeSync#recordTargetNativeMembershipInsert}.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupId, String userId) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordTargetNativeMembershipInsert(groupId, userId);
  }

  /** Record a successful SCIM membership remove (PATCH op=remove) against the current provisioner. */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupId, String userId) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordTargetNativeMembershipDelete(groupId, userId);
  }

  /**
   * Record a successful SCIM full-members replace (PATCH op=replace on members) against the
   * current provisioner: the group's mirror membership set becomes exactly {@code userIds}.
   */
  public static void captureMembershipReplaceFromCurrentProvisioner(String groupId, Set<String> userIds) {
    GrouperScim2ProvisioningTargetNativeSync scimSync = scimSyncForCurrentProvisioner();
    if (scimSync == null) {
      return;
    }
    scimSync.recordTargetNativeMembershipReplace(groupId, userIds);
  }

  private static GrouperScim2ProvisioningTargetNativeSync scimSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperScim2ProvisioningTargetNativeSync) {
      return (GrouperScim2ProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

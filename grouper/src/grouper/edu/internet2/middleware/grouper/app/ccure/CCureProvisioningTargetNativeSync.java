package edu.internet2.middleware.grouper.app.ccure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * CCure-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting beans
 * for sync-back into the generic grouper_prov_group / grouper_prov_user / grouper_prov_mship tables.
 *
 * <p><b>Object model.</b> In CCure terms a <i>group</i> is a Clearance, an <i>entity</i> is a
 * Personnel record, and a <i>membership</i> is a PersonnelClearancePair. The pair's
 * {@code ClearanceID} is the Clearance's {@code ObjectID} and its {@code PersonnelID} is the
 * Personnel record's {@code ObjectID}, so the native membership keys line up with the native group
 * and user target ids the end-of-run flush reconciles against, with no translation needed.
 *
 * <p><b>Flat JSON.</b> Unlike Datadog (JSON:API envelopes) CCure returns flat PascalCase objects,
 * so the JSON Pointer for a field is simply {@code "/" + fieldName} -- e.g. {@code /GUID},
 * {@code /Name}, {@code /Int1}. Capture is hooked at the {@link CCureApiCommands} seams where the
 * raw node is still in scope, so a CCure field that the {@code CCureUser} / {@code CCureGroup}
 * records do not model is still available to operators via {@code nativeAttributesEntities} /
 * {@code nativeAttributesGroups}.
 *
 * <p><b>DisplayProperties widening.</b> This is the one CCure-specific wrinkle. The
 * {@code /api/Objects/GetAllWithCriteria} endpoint is projection-based: it returns <i>only</i> the
 * fields named in the request's {@code DisplayProperties} array. So capturing an operator-configured
 * native attribute is not enough -- the attribute has to be asked for in the first place, or the
 * pointer resolves to a missing node and the attribute is silently dropped. {@link
 * #widenDisplayPropertiesForEntities(ArrayNode)} / {@link #widenDisplayPropertiesForGroups(ArrayNode)}
 * add the configured names to the outbound projection, the CCure analogue of the LDAP connector's
 * {@code widenLdapAttributeNamesForGroups}. The {@code /api/Objects/GetAll/Clearance} endpoint
 * returns whole objects and needs no widening.
 *
 * <p><b>Memberships.</b> On read, the clearance pairs are recorded from the already-parsed
 * {@link CCureMembership} records via {@link #captureMemberships}. On write,
 * {@code CCureTargetDao.insertMemberships}/{@code deleteMemberships} record each successful edge
 * directly into the native membership mirror (see
 * {@link #captureMembershipInsertFromCurrentProvisioner} /
 * {@link #captureMembershipDeleteFromCurrentProvisioner}), so a membership change converges on the
 * write pass rather than waiting for the next full sync. Memberships are never re-read; see the
 * base class for the rationale.
 *
 * <p><b>Objects are read-only.</b> The CCure provisioner does not create or delete Personnel or
 * Clearance records -- people and clearances are managed in CCure itself and Grouper only pairs
 * them. So there are no object write hooks here (no {@code recordTargetNativeGroupWrite} /
 * {@code recordTargetNativeUserWrite} call sites); the group and user mirrors are populated purely
 * from the read path.
 */
public class CCureProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for CCure entities (Personnel) when
   * {@code nativeAttributesEntities} is not configured. Excludes the id fields
   * ({@code PersonnelID} / {@code ObjectID}) since those are already the target_user_id column.
   * These are exactly the non-id fields the provisioner already asks for in DisplayProperties, so
   * the default costs nothing extra on the wire.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("GUID", "/GUID"),
          attrConfigWithPath("Name", "/Name"),
          attrConfigWithPath("Int1", "/Int1")));

  /**
   * Default per-attribute capture list for CCure groups (Clearances) when
   * {@code nativeAttributesGroups} is not configured. Excludes {@code ObjectID} (already the
   * target_group_id column).
   *
   * <p>NB the field is {@code PartitionID} -- that is what CCure returns, what
   * {@code CCureGroup.fromJson} reads, and what the mock's mock_ccure_clearance.partition_id column
   * holds. (The record's java field is spelled {@code partitionKey}, which is only a local naming
   * inconsistency; the wire name is PartitionID everywhere.)
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("GUID", "/GUID"),
          attrConfigWithPath("Name", "/Name"),
          attrConfigWithPath("PartitionID", "/PartitionID")));

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path}. When {@code path}
   * is null the pointer defaults to {@code "/" + name} (see {@link #populateAttributesFromJson}),
   * which for CCure's flat JSON is nearly always what you want anyway.
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

  // ----- widen (add configured attributes to the outbound DisplayProperties projection) -----

  /**
   * Add the CCure field names called for by the effective entity capture list to the outbound
   * {@code DisplayProperties} array, so {@code GetAllWithCriteria} actually returns them. Names
   * already present are not duplicated. No-op when entity sync-back is off.
   *
   * @param displayProperties the {@code DisplayProperties} array being built for the request
   */
  public void widenDisplayPropertiesForEntities(ArrayNode displayProperties) {
    if (displayProperties == null) {
      return;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior()
        .isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    widenDisplayProperties(displayProperties, this.effectiveNativeAttributeConfigsEntities());
  }

  /** see {@link #widenDisplayPropertiesForEntities(ArrayNode)}; same semantics for groups */
  public void widenDisplayPropertiesForGroups(ArrayNode displayProperties) {
    if (displayProperties == null) {
      return;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior()
        .isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    widenDisplayProperties(displayProperties, this.effectiveNativeAttributeConfigsGroups());
  }

  /**
   * Append the CCure field name for each capture config to {@code displayProperties}, skipping any
   * already there. The field name is derived from the JSON Pointer {@code path} when it is a simple
   * top-level pointer ({@code /Foo} -> {@code Foo}), else from the config {@code name}. A nested
   * pointer ({@code /a/b}) cannot be expressed as a DisplayProperties entry, so its first segment is
   * requested and the pointer resolves against whatever CCure returns under it.
   *
   * <p>Package-private rather than private so the unit test can exercise the projection logic
   * (dedup, pointer-to-field-name derivation) without standing up a provisioner for the behavior
   * gate that the public callers above apply.
   */
  static void widenDisplayProperties(ArrayNode displayProperties,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {

    // what is already asked for, so a configured attribute that duplicates a built-in is a no-op
    Set<String> existing = new LinkedHashSet<String>();
    for (JsonNode node : displayProperties) {
      if (node != null && !node.isNull()) {
        existing.add(node.asText());
      }
    }

    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      if (cfg == null || StringUtils.isBlank(cfg.getName())) {
        continue;
      }
      String ccureFieldName = ccureFieldNameForConfig(cfg);
      if (StringUtils.isBlank(ccureFieldName)) {
        continue;
      }
      if (existing.add(ccureFieldName)) {
        displayProperties.add(ccureFieldName);
      }
    }
  }

  /**
   * The CCure field to request in DisplayProperties for one capture config: the first segment of the
   * JSON Pointer {@code path} when there is one, else the config {@code name}.
   */
  private static String ccureFieldNameForConfig(GrouperProvisioningNativeAttributeConfig cfg) {
    String path = cfg.getPath();
    if (StringUtils.isBlank(path)) {
      return cfg.getName();
    }
    String pointer = StringUtils.removeStart(path, "/");
    if (StringUtils.isBlank(pointer)) {
      return cfg.getName();
    }
    return StringUtils.substringBefore(pointer, "/");
  }

  // ----- build (raw CCure JSON -> native-reporting bean) --------------------------------

  /**
   * Build a native group bean from a raw CCure Clearance JSON object. {@code targetId} is
   * {@code /ObjectID}; the attribute map is populated for each entry in
   * {@link #effectiveNativeAttributeConfigsGroups()} by JSON Pointer. Returns null when the JSON is
   * missing or has no ObjectID.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/ObjectID");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from a raw CCure Personnel JSON object. {@code targetId} is
   * {@code /PersonnelID} when present, else {@code /ObjectID} -- the same either/or that
   * {@code CCureUser.fromJson} applies, because which one comes back depends on the endpoint
   * (GetAllWithCriteria on Personnel projects ObjectID; the clearance-pair views carry PersonnelID).
   * Returns null when the JSON is missing or has neither id.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/PersonnelID");
    if (targetId == null) {
      targetId = resolveScalarAsString(userNode, "/ObjectID");
    }
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
   * against the raw CCure JSON and put the coerced value under {@code cfg.getName()}. Missing / null
   * nodes are skipped (no attribute row written) -- which is also what happens when a field was not
   * in the request's DisplayProperties, hence the widening above.
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (CCure JSON is flat)
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

  /** Build + record a CCure Clearance from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a CCure Personnel record from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate CCure clearance pairs into native membership beans and record them. The pair's
   * {@code ClearanceID} is the native group id and its {@code PersonnelID} is the native user id
   * (both are the referenced object's ObjectID), so no lookup is needed. No-op if reporting is off
   * or the input is empty.
   */
  public void captureMemberships(List<CCureMembership> ccureMemberships) {
    if (ccureMemberships == null || ccureMemberships.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (CCureMembership ccureMembership : ccureMemberships) {
      if (ccureMembership == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(String.valueOf(ccureMembership.clearanceId()));
      membership.setTargetUserId(String.valueOf(ccureMembership.personnelId()));
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from the CCureApiCommands / CCureTargetDao seams) -----

  /**
   * Capture a CCure Clearance (from its raw JSON) against the current provisioner's sync. No-op if
   * there is no current provisioner or the active provisioner is not a CCure one. Called from the
   * commands seam ({@code retrieveGroups} / {@code retrieveGroupByName} /
   * {@code retrieveGroupByObjectId}).
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureGroupJson(groupNode);
  }

  /**
   * Capture a CCure Personnel record (from its raw JSON) against the current provisioner's sync.
   * Called from the commands seam ({@code retrieveUsers} / {@code retrieveEntityByObjectId} /
   * {@code retrieveEntityByMatchField}).
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
  }

  /**
   * Capture CCure clearance pairs against the current provisioner's sync. Called from the commands
   * seam ({@code retrieveMemberships} / {@code retrieveMembershipsForUser} /
   * {@code retrieveMembershipsForGroup}).
   */
  public static void captureMembershipsFromCurrentProvisioner(List<CCureMembership> ccureMemberships) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureMemberships(ccureMemberships);
  }

  /**
   * Single-pair flavor of {@link #captureMembershipsFromCurrentProvisioner(List)}, for the paged
   * read loops that parse one clearance pair at a time. Keeps the call site to one line without
   * making each row allocate its own list.
   */
  public static void captureMembershipFromCurrentProvisioner(CCureMembership ccureMembership) {
    if (ccureMembership == null) {
      return;
    }
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(
        String.valueOf(ccureMembership.clearanceId()), String.valueOf(ccureMembership.personnelId()));
  }

  /**
   * Widen the outbound {@code DisplayProperties} for a Personnel query against the current
   * provisioner's sync, so operator-configured native attributes are actually returned. No-op out of
   * cycle or for a non-CCure provisioner.
   */
  public static void widenDisplayPropertiesForEntitiesFromCurrentProvisioner(ArrayNode displayProperties) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.widenDisplayPropertiesForEntities(displayProperties);
  }

  /** see {@link #widenDisplayPropertiesForEntitiesFromCurrentProvisioner(ArrayNode)}; groups (Clearance) */
  public static void widenDisplayPropertiesForGroupsFromCurrentProvisioner(ArrayNode displayProperties) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.widenDisplayPropertiesForGroups(displayProperties);
  }

  // ----- membership write-track dispatchers (called from CCureTargetDao write sites) -----
  // Like Datadog/Adobe/SCIM, single membership add/remove writes are tracked purely from our own
  // successful DAO calls and never re-read. The keys are the CCure ids passed to the write: the
  // ClearanceID (native group id) and the PersonnelID (native user id).

  /**
   * Write-track a successful clearance-pair insert against the current provisioner: record
   * {@code (clearanceId, personnelId)} in the native membership map so the end-of-run flush inserts
   * its grouper_prov_mship row. No-op out of cycle or for a non-CCure provisioner (and internally
   * no-op when membership sync-back is off).
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String clearanceId, String personnelId) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipInsert(clearanceId, personnelId);
  }

  /**
   * Write-track a successful clearance-pair delete against the current provisioner: drop
   * {@code (clearanceId, personnelId)} from the native membership map so the end-of-run flush
   * deletes its grouper_prov_mship row. No-op out of cycle or for a non-CCure provisioner.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String clearanceId, String personnelId) {
    CCureProvisioningTargetNativeSync sync = ccureSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeMembershipDelete(clearanceId, personnelId);
  }

  private static CCureProvisioningTargetNativeSync ccureSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof CCureProvisioningTargetNativeSync) {
      return (CCureProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * AssetSonar-specific {@link GrouperProvisioningTargetNativeSync}: captures the members read off
 * AssetSonar into the generic grouper_prov_user reporting tables, from the raw member JSON.
 *
 * <p>Entity-only, so only users are captured. Capture is hooked at the API-commands read seams
 * ({@link AssetSonarApiCommands}: every list page, active and inactive, the read by id, and the
 * lookup by email), where the full member JSON is in scope. The target_user_id is the numeric
 * member id.</p>
 *
 * <p>Writes do not return the member (create returns only an id, update only a message), so the
 * DAO records each write with no native object and the end-of-run drain re-reads it by email.</p>
 *
 * <p>The default captured attributes are exactly the ones the provisioner manages, so the cache
 * can stand in for the target with fullSyncUsersFromSyncBack (a managed attribute missing from the
 * cache would look changed on every run). role_id and status are captured as strings because that
 * is how the provisioner compares them. Operators can capture any other member field (e.g.
 * role_name, creation_source, created_by_id) via nativeAttributesEntities, but that list REPLACES
 * the defaults, so it must repeat them.</p>
 */
public class AssetSonarProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default capture list when nativeAttributesEntities is not configured: the managed attributes.
   * Excludes id (it is the target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          stringAttrConfig(AssetSonarMember.ATTR_EMAIL),
          stringAttrConfig(AssetSonarMember.ATTR_FIRST_NAME),
          stringAttrConfig(AssetSonarMember.ATTR_LAST_NAME),
          stringAttrConfig(AssetSonarMember.ATTR_EMPLOYEE_ID),
          stringAttrConfig(AssetSonarMember.ATTR_EMPLOYEE_IDENTIFICATION_NUMBER),
          stringAttrConfig(AssetSonarMember.ATTR_ROLE_ID),
          stringAttrConfig(AssetSonarMember.ATTR_STATUS)));

  /**
   * @param name the JSON field, also the stored attribute name
   * @return a string-typed capture config ("/" + name pointer)
   */
  private static GrouperProvisioningNativeAttributeConfig stringAttrConfig(String name) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath("/" + name);
    // the JSON has role_id and status as numbers; Grouper compares them as strings
    cfg.setType("string");
    return cfg;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsEntities() {
    return DEFAULT_ENTITY_ATTRS;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    // entity-only - no groups captured
    return Collections.emptyList();
  }

  /**
   * Build a native user bean from the raw member JSON. target_user_id is /id; attributes are read by
   * JSON Pointer for each configured attribute. Null when the JSON is missing or has no id.
   * @param memberNode the raw member JSON
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode memberNode) {
    if (memberNode == null || memberNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(memberNode, "/" + AssetSonarMember.ATTR_ID);
    if (StringUtils.isBlank(targetId)) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), memberNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer (path, or "/" + name) and put the coerced
   * value under the config name. Missing, null and blank values are skipped, matching how
   * {@link AssetSonarMember#toProvisioningEntity()} leaves blank attributes unassigned.
   */
  private static void populateAttributesFromJson(Map<String, Object> destinationAttributes,
      JsonNode resourceNode, List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      String pointer = StringUtils.defaultIfBlank(cfg.getPath(), "/" + cfg.getName());
      JsonNode node = resourceNode.at(pointer);
      if (node == null || node.isMissingNode() || node.isNull()) {
        continue;
      }
      Object value = coerceJsonValue(node, cfg.getType());
      if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
        continue;
      }
      destinationAttributes.put(cfg.getName(), value);
    }
  }

  private static String resolveScalarAsString(JsonNode resourceNode, String jsonPointer) {
    JsonNode node = resourceNode.at(jsonPointer);
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    return node.asText();
  }

  /**
   * Coerce a JsonNode to a scalar for storage. The declared type wins when present; otherwise the
   * node's own JSON type decides.
   */
  private static Object coerceJsonValue(JsonNode node, String declaredType) {
    if (StringUtils.equalsIgnoreCase(declaredType, "integer")) {
      return Long.valueOf(node.asLong());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "boolean")) {
      return Boolean.valueOf(node.asBoolean());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "string") || StringUtils.equalsIgnoreCase(declaredType, "timestamp")) {
      return node.asText();
    }
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

  /**
   * Capture a member (from its raw JSON) against the current provisioner's sync. No-op if there is
   * no current provisioner, it is not an AssetSonar one, or sync-back is off. Called from every
   * read seam in {@link AssetSonarApiCommands}.
   * @param memberNode the raw member JSON
   */
  public static void captureMemberJsonFromCurrentProvisioner(JsonNode memberNode) {
    AssetSonarProvisioningTargetNativeSync sync = assetSonarSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeUser(sync.buildNativeUserFromJson(memberNode));
  }

  /**
   * Record a write (create, reactivate, update, deactivate) of a member. AssetSonar writes return no
   * member body, so this passes no native object: the stale snapshot is dropped and the end-of-run
   * drain re-reads the member. A deactivation is a write, not a delete -- the member still exists.
   * @param memberId native member id
   */
  public static void recordMemberWriteFromCurrentProvisioner(String memberId) {
    AssetSonarProvisioningTargetNativeSync sync = assetSonarSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.recordTargetNativeUserWrite(memberId, null);
  }

  private static AssetSonarProvisioningTargetNativeSync assetSonarSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof AssetSonarProvisioningTargetNativeSync) {
      return (AssetSonarProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

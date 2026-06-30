package edu.internet2.middleware.grouper.app.interfolio;

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
 * Interfolio-specific {@link GrouperProvisioningTargetNativeSync}: captures the target users read off
 * Interfolio into the generic grouper_prov_user reporting tables, from the raw users/search JSON.
 *
 * This is an entity-only provisioner, so only users are captured (no groups, no memberships).  Capture
 * is hooked at the API-commands read seam ({@link GrouperInterfolioApiCommands#searchUsers}) where the
 * full user JSON node is in scope.  The target_user_id is the Interfolio pid (the provisioner key); the
 * default captured attributes are first_name, last_name, and email (the fields users/search returns).
 * Operators can capture any other users/search JSON field via nativeAttributesEntities with a name and
 * optional JSON-Pointer path.
 */
public class InterfolioProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Interfolio users when nativeAttributesEntities is not
   * configured.  JSON-Pointer based (reads the raw users/search JSON).  Excludes the id (the pid is
   * already the target_user_id column).
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfigWithPath("first_name", "/first_name"),
          attrConfigWithPath("last_name", "/last_name"),
          attrConfigWithPath("email", "/email")));

  /**
   * Build a native-attribute config with an explicit JSON Pointer path.
   * @param name stored attribute key
   * @param path JSON pointer into the user JSON
   * @return the config
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
    // entity-only - no groups captured
    return Collections.emptyList();
  }

  /**
   * Build a native user bean from the raw Interfolio users/search JSON.  target_user_id is read from
   * /pid; the attributes are populated by JSON Pointer for each configured attribute.  Returns null
   * when the JSON is missing or has no pid.
   * @param userNode the raw user JSON
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/pid");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), userNode, effectiveNativeAttributeConfigsEntities());
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer (path, or "/" + name) against the raw user
   * JSON and put the coerced value under the config name.  Missing / null nodes are skipped.
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
   * Coerce a JsonNode to a scalar for storage.  The declared type wins when present; otherwise the
   * node's intrinsic JSON type drives the choice.
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
      return node.asText();
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "string")) {
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

  /** Build + record an Interfolio user from its raw JSON.  No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Capture an Interfolio user (from its raw JSON) against the current provisioner's sync.  No-op if
   * there is no current provisioner or the active provisioner is not an Interfolio one.  Called from
   * the commands read seam for every user read off the target.
   * @param userNode the raw user JSON
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    InterfolioProvisioningTargetNativeSync sync = interfolioSyncForCurrentProvisioner();
    if (sync == null) {
      return;
    }
    sync.captureUserJson(userNode);
  }

  private static InterfolioProvisioningTargetNativeSync interfolioSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof InterfolioProvisioningTargetNativeSync) {
      return (InterfolioProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}

package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * One entry in a native-attributes JSON config (per provisioner, separate lists for
 * entities and groups). Identifies an extra target-side attribute to capture for the
 * generic provisioner reporting tables (grouper_prov_*_attr / _attr_value).
 *
 * <p>Shape per entry in the JSON array:
 * <pre>
 * { "name": "active",                                                  }
 * { "name": "displayName"                                              }
 * { "name": "lastModified", "path": "/meta/lastModified", "type": "timestamp" }
 * </pre>
 * <ul>
 *   <li>{@code name} (required, non-blank): attribute name written to grouper_prov_*_attr</li>
 *   <li>{@code path} (optional): SCIM JSON Pointer or LDAP attribute name; defaults to
 *       {@code "/" + name} for SCIM-style provisioners, {@code name} for LDAP-style</li>
 *   <li>{@code type} (optional): one of {@code string|integer|boolean|timestamp};
 *       defaults to auto-detect from the runtime value</li>
 * </ul>
 */
public class GrouperProvisioningNativeAttributeConfig {

  private static final Set<String> VALID_TYPES = new HashSet<String>();
  static {
    VALID_TYPES.add("string");
    VALID_TYPES.add("integer");
    VALID_TYPES.add("boolean");
    VALID_TYPES.add("timestamp");
  }

  private String name;

  private String path;

  private String type;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * parse and validate a native-attributes JSON string.
   *
   * @param json the JSON array, may be null/blank (returns empty list)
   * @param configLabel a human-readable label used in error messages
   *                    (e.g. "nativeAttributesJsonEntities")
   * @return parsed entries; never null
   * @throws RuntimeException if the JSON is malformed or any entry fails validation
   */
  public static List<GrouperProvisioningNativeAttributeConfig> parseAndValidate(
      String json, String configLabel) {

    List<GrouperProvisioningNativeAttributeConfig> result = new ArrayList<GrouperProvisioningNativeAttributeConfig>();

    if (StringUtils.isBlank(json)) {
      return result;
    }

    JsonNode root;
    try {
      root = GrouperUtil.jsonJacksonNode(json);
    } catch (Exception e) {
      throw new RuntimeException(configLabel + ": invalid JSON: " + e.getMessage(), e);
    }

    if (root == null || !root.isArray()) {
      throw new RuntimeException(configLabel + ": top-level value must be a JSON array");
    }

    Set<String> seenNames = new HashSet<String>();

    for (int i = 0; i < root.size(); i++) {
      JsonNode entryNode = root.get(i);
      if (entryNode == null || !entryNode.isObject()) {
        throw new RuntimeException(configLabel + ": entry " + i + " must be a JSON object");
      }

      String name = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "name"));
      if (StringUtils.isBlank(name)) {
        throw new RuntimeException(configLabel + ": entry " + i + " is missing required 'name'");
      }
      if (!seenNames.add(name)) {
        throw new RuntimeException(configLabel + ": duplicate 'name' = '" + name + "' at entry " + i);
      }

      String path = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "path"));

      String type = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "type"));
      if (type != null && !VALID_TYPES.contains(type.toLowerCase())) {
        throw new RuntimeException(configLabel + ": entry " + i + " (name='" + name
            + "') has invalid 'type' = '" + type + "'; expected one of "
            + VALID_TYPES);
      }

      GrouperProvisioningNativeAttributeConfig entry = new GrouperProvisioningNativeAttributeConfig();
      entry.name = name;
      entry.path = path;
      entry.type = type == null ? null : type.toLowerCase();
      result.add(entry);
    }

    return result;
  }

}

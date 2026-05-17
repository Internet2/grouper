package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  /**
   * Thrown by {@link #parseAndValidate} on a structural problem. Carries a stable
   * error {@link #textKey} (i18n suffix, e.g. {@code duplicateName}) plus named
   * {@link #args} so the UI validator can render an externalized message via
   * {@code provisioning.configuration.validation.nativeAttributes.<textKey>}.
   * {@link Throwable#getMessage()} carries an English fallback so the daemon log
   * stays readable when i18n lookup is unavailable.
   */
  public static class NativeAttributeConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String textKey;
    private final Map<String, String> args;

    public NativeAttributeConfigException(String textKey, Map<String, String> args,
        String englishFallbackMessage) {
      super(englishFallbackMessage);
      this.textKey = textKey;
      this.args = args == null ? new LinkedHashMap<String, String>() : args;
    }

    public NativeAttributeConfigException(String textKey, Map<String, String> args,
        String englishFallbackMessage, Throwable cause) {
      super(englishFallbackMessage, cause);
      this.textKey = textKey;
      this.args = args == null ? new LinkedHashMap<String, String>() : args;
    }

    /** i18n suffix; full key is {@code provisioning.configuration.validation.nativeAttributes.<textKey>} */
    public String getTextKey() {
      return this.textKey;
    }

    /** named substitution args; UI validator replaces {@code $$name$$} tokens in the localized text */
    public Map<String, String> getArgs() {
      return this.args;
    }
  }

  private static Map<String, String> argMap(String... namesAndValues) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    for (int i = 0; i + 1 < namesAndValues.length; i += 2) {
      map.put(namesAndValues[i], namesAndValues[i + 1] == null ? "" : namesAndValues[i + 1]);
    }
    return map;
  }

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
   * parse and validate a native-attributes config string. Accepts two forms:
   *
   * <p><b>1. Comma-separated list</b> (LDAP-style, simple attribute names only):
   * <pre>
   * sn, mail, telephoneNumber
   * </pre>
   * Each token becomes an entry with {@code name=path=token}, {@code type=null}
   * (auto-detect). Use this when each attribute is a flat name with no nested
   * path and no explicit type.
   *
   * <p><b>2. JSON array</b> (full form, supports nested paths and explicit types):
   * <pre>
   * [
   *   { "name": "active" },
   *   { "name": "displayName" },
   *   { "name": "lastModified", "path": "/meta/lastModified", "type": "timestamp" }
   * ]
   * </pre>
   *
   * <p>Detection rule: input that starts with {@code [} (after trimming) is parsed
   * as JSON; anything else is parsed as CSV.
   *
   * @param json the input (CSV or JSON array), may be null/blank (returns empty list)
   * @param configLabel a human-readable label used in error messages
   *                    (e.g. "nativeAttributesEntities")
   * @return parsed entries; never null
   * @throws RuntimeException if the input is malformed or any entry fails validation
   */
  public static List<GrouperProvisioningNativeAttributeConfig> parseAndValidate(
      String json, String configLabel) {

    List<GrouperProvisioningNativeAttributeConfig> result = new ArrayList<GrouperProvisioningNativeAttributeConfig>();

    if (StringUtils.isBlank(json)) {
      return result;
    }

    String trimmed = json.trim();

    // CSV form: bare comma-separated attribute names (LDAP-style)
    if (!trimmed.startsWith("[")) {
      Set<String> seenCsvNames = new HashSet<String>();
      for (String rawToken : trimmed.split(",")) {
        String token = StringUtils.trimToNull(rawToken);
        if (token == null) {
          continue;
        }
        if (token.startsWith("{") || token.startsWith("[") || token.contains("\"")) {
          throw new NativeAttributeConfigException("csvLooksLikeJson",
              argMap("configLabel", configLabel, "token", token),
              configLabel + ": value '" + token
                  + "' looks like JSON; either provide the whole value as a JSON array, "
                  + "or use a plain comma-separated list of attribute names");
        }
        if (!seenCsvNames.add(token)) {
          throw new NativeAttributeConfigException("csvDuplicateName",
              argMap("configLabel", configLabel, "name", token),
              configLabel + ": duplicate attribute name '" + token + "'");
        }
        GrouperProvisioningNativeAttributeConfig entry = new GrouperProvisioningNativeAttributeConfig();
        entry.name = token;
        result.add(entry);
      }
      return result;
    }

    // JSON array form
    JsonNode root;
    try {
      root = GrouperUtil.jsonJacksonNode(trimmed);
    } catch (Exception e) {
      throw new NativeAttributeConfigException("invalidJson",
          argMap("configLabel", configLabel, "detail", e.getMessage()),
          configLabel + ": invalid JSON: " + e.getMessage(), e);
    }

    if (root == null || !root.isArray()) {
      throw new NativeAttributeConfigException("notArray",
          argMap("configLabel", configLabel),
          configLabel + ": top-level value must be a JSON array");
    }

    Set<String> seenNames = new HashSet<String>();

    for (int i = 0; i < root.size(); i++) {
      JsonNode entryNode = root.get(i);
      if (entryNode == null || !entryNode.isObject()) {
        throw new NativeAttributeConfigException("entryNotObject",
            argMap("configLabel", configLabel, "index", String.valueOf(i)),
            configLabel + ": entry " + i + " must be a JSON object");
      }

      String name = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "name"));
      if (StringUtils.isBlank(name)) {
        throw new NativeAttributeConfigException("missingName",
            argMap("configLabel", configLabel, "index", String.valueOf(i)),
            configLabel + ": entry " + i + " is missing required 'name'");
      }
      if (!seenNames.add(name)) {
        throw new NativeAttributeConfigException("duplicateName",
            argMap("configLabel", configLabel, "name", name, "index", String.valueOf(i)),
            configLabel + ": duplicate 'name' = '" + name + "' at entry " + i);
      }

      String path = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "path"));

      String type = StringUtils.trimToNull(GrouperUtil.jsonJacksonGetString(entryNode, "type"));
      if (type != null && !VALID_TYPES.contains(type.toLowerCase())) {
        throw new NativeAttributeConfigException("invalidType",
            argMap("configLabel", configLabel, "index", String.valueOf(i),
                "name", name, "type", type, "validTypes", VALID_TYPES.toString()),
            configLabel + ": entry " + i + " (name='" + name
                + "') has invalid 'type' = '" + type + "'; expected one of " + VALID_TYPES);
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

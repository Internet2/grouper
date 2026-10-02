/*******************************************************************************
 * Copyright 2024 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for searching LDAP directories.
 * Supports two actions:
 * <ul>
 *   <li>{@code listExternalSystems} - list available LDAP external system IDs
 *       with their default base DN and documentation</li>
 *   <li>{@code filter} - execute an LDAP search with a filter string and return
 *       matching entries with their attributes</li>
 * </ul>
 * LDAP external systems are available for MCP if they have any
 * {@code grouper.mcp.ldap.<id>.*} configuration property.
 *
 * @author mchyzer
 */
public class GrouperMcpLdapSearch {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpLdapSearch.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** default maximum number of LDAP entries to return */
  static final int DEFAULT_MAX_ENTRIES = 2500;

  /** default maximum total attribute values across all entries */
  static final int DEFAULT_MAX_TOTAL_ATTRIBUTE_VALUES = 10000;

  /** default maximum characters in the response text */
  static final int DEFAULT_MAX_RESPONSE_CHARS = 1000000;

  /**
   * get the configured max entries limit
   * @return the max entries
   */
  static int maxEntries() {
    return GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.mcp.ldap.maxEntries", DEFAULT_MAX_ENTRIES);
  }

  /**
   * get the configured max total attribute values limit
   * @return the max total attribute values
   */
  static int maxTotalAttributeValues() {
    return GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.mcp.ldap.maxTotalAttributeValues", DEFAULT_MAX_TOTAL_ATTRIBUTE_VALUES);
  }

  /**
   * get the configured max response chars limit
   * @return the max response chars
   */
  static int maxResponseChars() {
    return GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.mcp.ldap.maxResponseChars", DEFAULT_MAX_RESPONSE_CHARS);
  }

  /**
   * pattern to match LDAP external system IDs from config keys like
   * grouper.mcp.ldap.&lt;id&gt;.baseDn or grouper.mcp.ldap.&lt;id&gt;.documentationForAiClient
   */
  private static final Pattern LDAP_CONFIG_PATTERN = Pattern.compile(
      "^grouper\\.mcp\\.ldap\\.([^.]+)\\..*$");

  /**
   * return the MCP tool definition for ldap
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "ldap");
    tool.put("description",
        "Search LDAP directories configured as Grouper external systems. "
        + "Use action 'listExternalSystems' to discover available LDAP connections "
        + "with their default base DN and documentation. "
        + "Use action 'filter' to execute an LDAP search with a filter string "
        + "and return matching entries with their attributes.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("listExternalSystems");
    actionEnum.add("filter");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The action to perform. 'listExternalSystems' returns available LDAP connections. "
        + "'filter' executes an LDAP search and returns matching entries.");
    properties.set("action", actionProp);

    ObjectNode externalSystemIdProp = objectMapper.createObjectNode();
    externalSystemIdProp.put("type", "string");
    externalSystemIdProp.put("description",
        "LDAP external system ID. Required for 'filter' action. "
        + "Must match an ldap.<id>.* connection configured in Grouper. "
        + "Use 'listExternalSystems' to discover available IDs.");
    properties.set("externalSystemId", externalSystemIdProp);

    ObjectNode baseDnProp = objectMapper.createObjectNode();
    baseDnProp.put("type", "string");
    baseDnProp.put("description",
        "Base DN for the LDAP search (e.g., 'ou=people,dc=example,dc=edu'). "
        + "Optional; if not specified, falls back to the default base DN "
        + "configured for the external system.");
    properties.set("baseDn", baseDnProp);

    ObjectNode searchScopeProp = objectMapper.createObjectNode();
    searchScopeProp.put("type", "string");
    ArrayNode scopeEnum = objectMapper.createArrayNode();
    scopeEnum.add("OBJECT_SCOPE");
    scopeEnum.add("ONELEVEL_SCOPE");
    scopeEnum.add("SUBTREE_SCOPE");
    searchScopeProp.set("enum", scopeEnum);
    searchScopeProp.put("description",
        "LDAP search scope. Defaults to SUBTREE_SCOPE. "
        + "OBJECT_SCOPE searches only the base entry, "
        + "ONELEVEL_SCOPE searches one level below the base, "
        + "SUBTREE_SCOPE searches the entire subtree.");
    properties.set("searchScope", searchScopeProp);

    ObjectNode filterProp = objectMapper.createObjectNode();
    filterProp.put("type", "string");
    filterProp.put("description",
        "LDAP filter string (e.g., '(uid=jsmith)', '(&(objectClass=person)(sn=Smith))'). "
        + "Required for 'filter' action.");
    properties.set("filter", filterProp);

    ObjectNode attributesProp = objectMapper.createObjectNode();
    attributesProp.put("type", "array");
    ObjectNode attributeItems = objectMapper.createObjectNode();
    attributeItems.put("type", "string");
    attributesProp.set("items", attributeItems);
    attributesProp.put("description",
        "Attribute names to return (e.g., ['uid', 'cn', 'mail', 'memberOf']). "
        + "If not specified, returns all attributes.");
    properties.set("attributes", attributesProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the ldap tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required. Use 'listExternalSystems' or 'filter'.");
    }

    try {
      if ("listExternalSystems".equals(action)) {
        return listExternalSystems(authUser);
      } else if ("filter".equals(action)) {
        return executeFilter(arguments, authUser);
      } else {
        return buildErrorResult("Unknown action '" + action
            + "'. Use 'listExternalSystems' or 'filter'.");
      }
    } catch (Exception e) {
      LOG.error("Error executing LDAP tool", e);
      return buildErrorResult("Error executing LDAP tool: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * list all available LDAP external system IDs by scanning config properties.
   * discovers systems from grouper.mcp.ldap.&lt;id&gt;.* config keys.  a caller who is not on the
   * all tier only sees the systems opened to them, and is not told whether others exist
   * @param authUser the caller
   * @return the MCP tool result with the list of external systems
   */
  private static ObjectNode listExternalSystems(GrouperMcpAuthUser authUser) throws Exception {

    Set<String> externalSystemIds = externalSystemIdsFor(authUser);

    if (externalSystemIds.isEmpty()) {
      if (isAllTier(authUser)) {
        return buildSuccessResult("No LDAP external systems are configured for MCP. "
            + "The administrator must add grouper.mcp.ldap.<id>.baseDn configuration "
            + "for each LDAP connection to make available.");
      }
      return buildSuccessResult(NONE_AVAILABLE_MESSAGE);
    }

    ArrayNode systemsArray = objectMapper.createArrayNode();
    for (String id : externalSystemIds) {
      ObjectNode systemNode = objectMapper.createObjectNode();
      systemNode.put("id", id);

      String baseDn = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.ldap." + id + ".baseDn", "");
      if (StringUtils.isNotBlank(baseDn)) {
        systemNode.put("baseDn", baseDn);
      }

      String documentation = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.ldap." + id + ".documentationForAiClient", "");
      if (StringUtils.isNotBlank(documentation)) {
        systemNode.put("documentation", documentation);
      }

      systemsArray.add(systemNode);
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.set("externalSystems", systemsArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * the LDAP external system IDs configured for MCP, from grouper.mcp.ldap.&lt;id&gt;.* keys
   * @return the IDs, empty if none
   */
  static Set<String> externalSystemIds() {
    Set<String> externalSystemIds = new LinkedHashSet<String>();
    Set<String> propertyNames = GrouperConfig.retrieveConfig().propertyNames();
    for (String key : propertyNames) {
      Matcher matcher = LDAP_CONFIG_PATTERN.matcher(key);
      if (matcher.matches()) {
        externalSystemIds.add(matcher.group(1));
      }
    }
    return externalSystemIds;
  }

  /**
   * the LDAP external system IDs this caller may search.  a caller on the all tier of admin
   * readonly gets every configured system.  a caller on the limited tier only gets those whose
   * grouper.mcp.ldap.&lt;id&gt;.limitedAccessGroup they are in (GRP-7415)
   * @param authUser the caller
   * @return the IDs, empty if none
   */
  static Set<String> externalSystemIdsFor(GrouperMcpAuthUser authUser) {
    Set<String> result = new LinkedHashSet<String>();
    for (String id : externalSystemIds()) {
      if (GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.admin_readonly,
          GrouperToolAccess.LDAP_CONFIG_PREFIX, id, authUser)) {
        result.add(id);
      }
    }
    return result;
  }

  /**
   * if this tool is worth offering this caller.  a caller on the all tier is always offered it,
   * as before, so they can be told what to configure.  a caller on the limited tier only if an
   * LDAP system is opened to them
   * @param authUser the caller
   * @return true if the tool is worth offering
   */
  public static boolean anyAvailableFor(GrouperMcpAuthUser authUser) {
    if (isAllTier(authUser)) {
      return true;
    }
    return !externalSystemIdsFor(authUser).isEmpty();
  }

  /** what a caller who is not on the all tier is told when no LDAP system is opened to them */
  private static final String NONE_AVAILABLE_MESSAGE = "No LDAP external systems are available "
      + "to you. Your Grouper administrator can make one available to you.";

  /**
   * whether the caller is on the all tier of admin readonly, so may use every LDAP system
   * @param authUser the caller
   * @return true if on the all tier
   */
  private static boolean isAllTier(GrouperMcpAuthUser authUser) {
    return GrouperToolAccess.isAllTier(GrouperToolCategory.admin_readonly,
        authUser == null ? null : authUser.getSubject());
  }

  /**
   * validate that an LDAP external system is allowed for MCP queries by this caller.
   * the system must have at least one grouper.mcp.ldap.&lt;id&gt;.* config property, and a
   * caller who is not on the all tier must be on the limited tier and in its limitedAccessGroup.
   * @param externalSystemId the external system ID to validate
   * @param authUser the caller
   * @return an error message if the system is not allowed, or null if allowed
   */
  static String validateExternalSystemAllowed(String externalSystemId,
      GrouperMcpAuthUser authUser) {
    if (StringUtils.isBlank(externalSystemId)) {
      return "externalSystemId is required for the 'filter' action.";
    }

    String trimmed = externalSystemId.trim();

    // a caller who is not on the all tier may only use a system opened to them, which is always
    // a configured one.  they get the same answer whether or not a system by that name exists,
    // so they cannot find out which do
    if (!isAllTier(authUser)) {
      if (externalSystemIdsFor(authUser).contains(trimmed)) {
        return null;
      }
      return "LDAP external system '" + trimmed + "' is not available to you. "
          + "Use the 'listExternalSystems' action to see the LDAP connections available to you.";
    }

    // check if any grouper.mcp.ldap.<id>.* config exists
    Set<String> propertyNames = GrouperConfig.retrieveConfig().propertyNames();
    String prefix = "grouper.mcp.ldap." + trimmed + ".";
    for (String key : propertyNames) {
      if (key.startsWith(prefix)) {
        return null; // allowed
      }
    }

    return "LDAP external system '" + trimmed + "' is not configured for MCP. "
        + "The administrator must add grouper.mcp.ldap." + trimmed
        + ".baseDn (or other config) to enable it. "
        + "Use the 'listExternalSystems' action to see available LDAP connections.";
  }

  /**
   * execute an LDAP filter search
   * @param arguments the tool arguments
   * @param authUser the caller
   * @return the MCP tool result with matching entries
   */
  private static ObjectNode executeFilter(JsonNode arguments, GrouperMcpAuthUser authUser)
      throws Exception {

    String externalSystemId = arguments.has("externalSystemId")
        ? arguments.get("externalSystemId").asText() : null;
    String baseDn = arguments.has("baseDn")
        ? arguments.get("baseDn").asText() : null;
    String searchScopeStr = arguments.has("searchScope")
        ? arguments.get("searchScope").asText() : null;
    String filter = arguments.has("filter")
        ? arguments.get("filter").asText() : null;

    // validate externalSystemId
    String validationError = validateExternalSystemAllowed(externalSystemId, authUser);
    if (validationError != null) {
      return buildErrorResult(validationError);
    }

    String trimmedId = externalSystemId.trim();

    // validate filter
    if (StringUtils.isBlank(filter)) {
      return buildErrorResult("filter is required for the 'filter' action. "
          + "Provide an LDAP filter string (e.g., '(uid=jsmith)').");
    }

    // resolve base DN: argument > config > error
    if (StringUtils.isBlank(baseDn)) {
      baseDn = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.ldap." + trimmedId + ".baseDn", "");
    }
    if (StringUtils.isBlank(baseDn)) {
      return buildErrorResult("baseDn is required. Either provide it as a parameter "
          + "or configure grouper.mcp.ldap." + trimmedId + ".baseDn as the default.");
    }

    // parse search scope
    LdapSearchScope searchScope = LdapSearchScope.SUBTREE_SCOPE;
    if (StringUtils.isNotBlank(searchScopeStr)) {
      try {
        searchScope = LdapSearchScope.valueOfIgnoreCase(searchScopeStr.trim(), true);
      } catch (Exception e) {
        return buildErrorResult("Invalid searchScope '" + searchScopeStr
            + "'. Use OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE.");
      }
    }

    // parse attributes (null means return all)
    String[] attributeNames = null;
    if (arguments.has("attributes") && arguments.get("attributes").isArray()) {
      JsonNode attrsNode = arguments.get("attributes");
      if (attrsNode.size() > 0) {
        attributeNames = new String[attrsNode.size()];
        for (int i = 0; i < attrsNode.size(); i++) {
          attributeNames[i] = attrsNode.get(i).asText();
        }
      }
    }

    int maxEntriesValue = maxEntries();
    int maxAttrValues = maxTotalAttributeValues();
    int maxChars = maxResponseChars();

    // execute LDAP search
    List<LdapEntry> entries = LdapSessionUtils.ldapSession().list(
        trimmedId, baseDn, searchScope, filter, attributeNames, maxEntriesValue);

    // build response JSON, enforcing attribute value limit
    ArrayNode entriesArray = objectMapper.createArrayNode();
    int totalAttributeValues = 0;
    boolean truncated = false;
    int entryCount = 0;

    for (LdapEntry entry : entries) {

      // check if we've hit the attribute value limit before processing this entry
      if (totalAttributeValues >= maxAttrValues) {
        truncated = true;
        break;
      }

      ObjectNode entryNode = objectMapper.createObjectNode();
      entryNode.put("dn", entry.getDn());

      ObjectNode attrsNode = objectMapper.createObjectNode();
      Collection<LdapAttribute> attributes = entry.getAttributes();
      if (attributes != null) {
        for (LdapAttribute attr : attributes) {
          Collection<String> values = attr.getStringValues();
          if (values != null && !values.isEmpty()) {
            ArrayNode valuesArray = objectMapper.createArrayNode();
            for (String value : values) {
              valuesArray.add(value);
              totalAttributeValues++;
            }
            attrsNode.set(attr.getName(), valuesArray);

            // check limit after each attribute
            if (totalAttributeValues >= maxAttrValues) {
              truncated = true;
              break;
            }
          }
        }
      }

      entryNode.set("attributes", attrsNode);
      entriesArray.add(entryNode);
      entryCount++;
    }

    // if there are more entries from LDAP than we included, mark as truncated
    if (entryCount < entries.size()) {
      truncated = true;
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("entryCount", entryCount);
    resultNode.put("totalAttributeValues", totalAttributeValues);
    resultNode.put("truncated", truncated);
    resultNode.set("entries", entriesArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);

    // if the response is too large, remove entries from the end until it fits
    while (resultText.length() > maxChars && entriesArray.size() > 0) {
      entriesArray.remove(entriesArray.size() - 1);
      entryCount = entriesArray.size();
      resultNode.put("entryCount", entryCount);
      resultNode.put("truncated", true);
      resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
    }

    return buildSuccessResult(resultText);
  }

  /**
   * build a successful MCP tool result
   */
  private static ObjectNode buildSuccessResult(String text) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", text);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", false);
    return result;
  }

  /**
   * build an error MCP tool result
   */
  private static ObjectNode buildErrorResult(String errorMessage) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", errorMessage);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", true);
    return result;
  }
}

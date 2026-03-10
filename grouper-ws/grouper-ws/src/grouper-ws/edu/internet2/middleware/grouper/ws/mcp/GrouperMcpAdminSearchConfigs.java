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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * MCP admin tool for searching configuration properties across Grouper config files.
 * Takes a regex to match config keys and returns matching key/value pairs ordered
 * by key name. Sensitive values (passwords, secrets, etc.) are masked.
 * Optionally filter by a specific config file.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminSearchConfigs {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminSearchConfigs.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum number of config entries to return */
  static final int MAX_RESULTS = 500;

  /**
   * return the MCP tool definition for admin_config_search
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_config_search");
    tool.put("description",
        "Search for Grouper configuration properties by regex. "
        + "Takes a regex pattern that is matched against config key names. "
        + "Returns matching key/value pairs ordered by key name. "
        + "Sensitive values (passwords, secrets, private keys) are masked as '"
        + GrouperConfigHibernate.ESCAPED_PASSWORD + "'. "
        + "Optionally filter by a specific config file. "
        + "Available config files: grouper.properties, grouper-loader.properties, "
        + "grouper.client.properties, grouper.cache.properties, "
        + "grouper-ui.properties, grouper-ws.properties, subject.properties. "
        + "Examples: '.*provisioner.*' finds all provisioner-related configs, "
        + "'.*mcp.*' finds all MCP-related configs, "
        + "'grouper\\.mcp\\..*' finds configs starting with 'grouper.mcp.'.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode searchRegexProp = objectMapper.createObjectNode();
    searchRegexProp.put("type", "string");
    searchRegexProp.put("description",
        "Regex pattern to match against config key names (case-insensitive). "
        + "Examples: '.*provisioner.*', '.*mcp.*', 'grouper\\.mcp\\..*'");
    properties.set("searchRegex", searchRegexProp);

    ObjectNode configFileProp = objectMapper.createObjectNode();
    configFileProp.put("type", "string");
    configFileProp.put("description",
        "Optional. Filter by a specific config file name. "
        + "Available values: grouper.properties, grouper-loader.properties, "
        + "grouper.client.properties, grouper.cache.properties, "
        + "grouper-ui.properties, grouper-ws.properties, subject.properties. "
        + "If not specified, searches across all config files.");
    properties.set("configFile", configFileProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("searchRegex");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_config_search tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String searchRegex = arguments != null && arguments.has("searchRegex")
        ? arguments.get("searchRegex").asText() : null;

    String configFile = arguments != null && arguments.has("configFile")
        ? arguments.get("configFile").asText() : null;

    if (StringUtils.isBlank(searchRegex)) {
      return buildErrorResult("searchRegex is required.");
    }

    // compile the regex pattern (case-insensitive)
    Pattern pattern;
    try {
      pattern = Pattern.compile(searchRegex, Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException e) {
      return buildErrorResult("Invalid regex pattern: " + e.getMessage());
    }

    // determine which config files to search
    ConfigFileName[] configFileNames;
    if (StringUtils.isNotBlank(configFile)) {
      ConfigFileName configFileName = null;
      try {
        configFileName = ConfigFileName.valueOfIgnoreCase(configFile.trim(), false);
      } catch (Exception e) {
        // ignore, handle below
      }
      if (configFileName == null) {
        StringBuilder availableFiles = new StringBuilder();
        for (ConfigFileName cf : ConfigFileName.values()) {
          if (availableFiles.length() > 0) {
            availableFiles.append(", ");
          }
          availableFiles.append(cf.getConfigFileName());
        }
        return buildErrorResult("Unknown config file: '" + configFile
            + "'. Available config files: " + availableFiles);
      }
      configFileNames = new ConfigFileName[]{configFileName};
    } else {
      configFileNames = ConfigFileName.values();
    }

    try {
      // collect matching configs across all (or filtered) config files
      // use TreeMap for natural key ordering
      Map<String, ObjectNode> matchingConfigs = new TreeMap<String, ObjectNode>();

      for (ConfigFileName fileName : configFileNames) {
        try {
          ConfigPropertiesCascadeBase config = fileName.getConfig();
          if (config == null) {
            continue;
          }

          Set<String> propertyNames = config.propertyNames();
          for (String key : propertyNames) {
            if (pattern.matcher(key).find()) {

              String value = config.propertyValueString(key);

              // check if this is a sensitive/password config
              boolean isSensitive = GrouperConfigHibernate.isPasswordHelper(
                  fileName, null, key, value,
                  StringUtils.isNotBlank(value), null);

              ObjectNode entry = objectMapper.createObjectNode();
              entry.put("key", key);
              if (isSensitive && StringUtils.isNotBlank(value)) {
                entry.put("value", GrouperConfigHibernate.ESCAPED_PASSWORD);
                entry.put("sensitive", true);
              } else {
                entry.put("value", value != null ? value : "");
              }
              entry.put("configFile", fileName.getConfigFileName());

              matchingConfigs.put(key + "|||" + fileName.getConfigFileName(), entry);
            }
          }
        } catch (Exception e) {
          LOG.warn("Error reading config file " + fileName.getConfigFileName()
              + " via MCP: " + e.getMessage(), e);
          // continue with other config files
        }
      }

      // truncate if too many
      boolean truncated = false;
      int totalCount = matchingConfigs.size();

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("matchCount", totalCount);

      if (totalCount > MAX_RESULTS) {
        truncated = true;
        resultNode.put("truncated", true);
        resultNode.put("message", "Results truncated to " + MAX_RESULTS
            + ". Use a more specific regex to narrow results.");
      }

      ArrayNode configsArray = objectMapper.createArrayNode();
      int count = 0;
      for (ObjectNode entry : matchingConfigs.values()) {
        if (count >= MAX_RESULTS) {
          break;
        }
        configsArray.add(entry);
        count++;
      }
      resultNode.set("configs", configsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error searching configs via MCP", e);
      return buildErrorResult("Error searching configs: " + e.getMessage());
    }
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

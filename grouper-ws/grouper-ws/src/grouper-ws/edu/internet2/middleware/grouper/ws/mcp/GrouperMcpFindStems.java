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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStem;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemQueryFilter;

/**
 * MCP tool handler for finding Grouper stems (folders).
 * Supports searching by name (exact or approximate), by parent stem,
 * and by attribute.
 *
 * @author mchyzer
 */
public class GrouperMcpFindStems {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFindStems.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for folder_find
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "folder_find");
    tool.put("description",
        "Search for Grouper stems (folders) by name, parent stem, or attribute. "
        + "Supports exact and approximate name matching, "
        + "and searching within a specific parent stem.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode stemQueryFilterTypeProp = objectMapper.createObjectNode();
    stemQueryFilterTypeProp.put("type", "string");
    ArrayNode sqftEnum = objectMapper.createArrayNode();
    sqftEnum.add("FIND_BY_STEM_NAME");
    sqftEnum.add("FIND_BY_STEM_NAME_APPROXIMATE");
    sqftEnum.add("FIND_BY_STEM_UUID");
    sqftEnum.add("FIND_BY_APPROXIMATE_ATTRIBUTE");
    stemQueryFilterTypeProp.set("enum", sqftEnum);
    stemQueryFilterTypeProp.put("description",
        "Type of search to perform. "
        + "FIND_BY_STEM_NAME = exact name match, "
        + "FIND_BY_STEM_NAME_APPROXIMATE = approximate name match (most common), "
        + "FIND_BY_STEM_UUID = find by UUID, "
        + "FIND_BY_APPROXIMATE_ATTRIBUTE = search by attribute value.");
    properties.set("stemQueryFilterType", stemQueryFilterTypeProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Stem name to search for. Used with FIND_BY_STEM_NAME "
        + "or FIND_BY_STEM_NAME_APPROXIMATE.");
    properties.set("stemName", stemNameProp);

    ObjectNode stemUuidProp = objectMapper.createObjectNode();
    stemUuidProp.put("type", "string");
    stemUuidProp.put("description",
        "Stem UUID. Used with FIND_BY_STEM_UUID.");
    properties.set("stemUuid", stemUuidProp);

    ObjectNode parentStemNameProp = objectMapper.createObjectNode();
    parentStemNameProp.put("type", "string");
    parentStemNameProp.put("description",
        "Parent stem name to search within. "
        + "Can be used with FIND_BY_STEM_NAME_APPROXIMATE to scope the search.");
    properties.set("parentStemName", parentStemNameProp);

    ObjectNode parentStemNameScopeProp = objectMapper.createObjectNode();
    parentStemNameScopeProp.put("type", "string");
    ArrayNode scopeEnum = objectMapper.createArrayNode();
    scopeEnum.add("ONE_LEVEL");
    scopeEnum.add("ALL_IN_SUBTREE");
    parentStemNameScopeProp.set("enum", scopeEnum);
    parentStemNameScopeProp.put("description",
        "Scope when searching in a parent stem. ONE_LEVEL = direct children only, "
        + "ALL_IN_SUBTREE = all descendants (default).");
    properties.set("parentStemNameScope", parentStemNameScopeProp);

    ObjectNode stemAttributeValueProp = objectMapper.createObjectNode();
    stemAttributeValueProp.put("type", "string");
    stemAttributeValueProp.put("description",
        "Attribute value to search for. Used with FIND_BY_APPROXIMATE_ATTRIBUTE.");
    properties.set("stemAttributeValue", stemAttributeValueProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("stemQueryFilterType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the folder_find tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String stemQueryFilterType = arguments != null && arguments.has("stemQueryFilterType")
        ? arguments.get("stemQueryFilterType").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String stemUuid = arguments != null && arguments.has("stemUuid")
        ? arguments.get("stemUuid").asText() : null;
    String parentStemName = arguments != null && arguments.has("parentStemName")
        ? arguments.get("parentStemName").asText() : null;
    String parentStemNameScope = arguments != null && arguments.has("parentStemNameScope")
        ? arguments.get("parentStemNameScope").asText() : null;
    String stemAttributeValue = arguments != null && arguments.has("stemAttributeValue")
        ? arguments.get("stemAttributeValue").asText() : null;

    if (StringUtils.isBlank(stemQueryFilterType)) {
      return buildErrorResult("stemQueryFilterType is required.");
    }

    try {

      WsStemQueryFilter wsStemQueryFilter = new WsStemQueryFilter();
      wsStemQueryFilter.setStemQueryFilterType(stemQueryFilterType);

      if (StringUtils.isNotBlank(stemName)) {
        wsStemQueryFilter.setStemName(stemName);
      }
      if (StringUtils.isNotBlank(stemUuid)) {
        wsStemQueryFilter.setStemUuid(stemUuid);
      }
      if (StringUtils.isNotBlank(parentStemName)) {
        wsStemQueryFilter.setParentStemName(parentStemName);
      }
      if (StringUtils.isNotBlank(parentStemNameScope)) {
        wsStemQueryFilter.setParentStemNameScope(parentStemNameScope);
      }
      if (StringUtils.isNotBlank(stemAttributeValue)) {
        wsStemQueryFilter.setStemAttributeValue(stemAttributeValue);
      }

      WsFindStemsResults wsResults = GrouperServiceLogic.findStems(
          GrouperVersion.currentVersion(),
          wsStemQueryFilter,
          null,   // actAsSubjectLookup
          null,   // params
          null    // wsStemLookups
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      ObjectNode resultNode = objectMapper.createObjectNode();
      WsStem[] stems = wsResults.getStemResults();
      int stemCount = GrouperUtil.length(stems);
      resultNode.put("totalStemsReturned", stemCount);

      ArrayNode stemsArray = objectMapper.createArrayNode();
      if (stemCount > 0) {
        for (WsStem stem : stems) {
          ObjectNode stemNode = objectMapper.createObjectNode();
          stemNode.put("name", stem.getName());
          if (StringUtils.isNotBlank(stem.getDisplayName())) {
            stemNode.put("displayName", stem.getDisplayName());
          }
          if (StringUtils.isNotBlank(stem.getExtension())) {
            stemNode.put("extension", stem.getExtension());
          }
          if (StringUtils.isNotBlank(stem.getDescription())) {
            stemNode.put("description", stem.getDescription());
          }
          if (StringUtils.isNotBlank(stem.getUuid())) {
            stemNode.put("uuid", stem.getUuid());
          }
          stemsArray.add(stemNode);
        }
      }
      resultNode.set("stems", stemsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error finding stems", e);
      return buildErrorResult("Error finding stems: " + e.getMessage());
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

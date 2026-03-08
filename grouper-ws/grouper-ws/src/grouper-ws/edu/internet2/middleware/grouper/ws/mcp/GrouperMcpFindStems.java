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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.misc.GrouperObject;
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
 * <p>Delegates to {@link GrouperServiceLogic#findStems} for the actual search,
 * then optionally enriches results with Grouper object type names (e.g., policy, ref,
 * basis, manual) via {@link GrouperObjectTypesConfiguration}.</p>
 *
 * <p>The object types are not part of the WS response, so when includeFolderTypes is true,
 * we do a second lookup: re-fetch the Stem objects by name, then batch-retrieve their
 * type attributes. This adds some overhead but avoids exposing the underlying attribute
 * framework complexity to the MCP client.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpFindStems {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFindStems.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for folder_find.
   * This builds the JSON Schema that describes the tool's input parameters
   * to the MCP client (e.g., an AI model).
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
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

    ObjectNode includeFolderTypesProp = objectMapper.createObjectNode();
    includeFolderTypesProp.put("type", "boolean");
    includeFolderTypesProp.put("description",
        "If true, include folder type names (e.g., policy, ref, basis, manual, app, org, test, service, readOnly, etc.) "
        + "for each folder in the results. Defaults to false.");
    properties.set("includeFolderTypes", includeFolderTypesProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("stemQueryFilterType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the folder_find tool by delegating to the WS service logic.
   *
   * <p>Flow:
   * 1. Parse and validate input arguments from the MCP request
   * 2. Build a WsStemQueryFilter and call GrouperServiceLogic.findStems()
   * 3. If includeFolderTypes is requested, do a secondary lookup to fetch
   *    Grouper object type attributes (policy, ref, basis, etc.) for each stem
   * 4. Build a clean JSON response with stem details and optional type info</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user (used for access control upstream)
   * @return the MCP tool result containing stem data or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    // parse all input parameters from the MCP request arguments
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
    boolean includeFolderTypes = arguments != null && arguments.has("includeFolderTypes")
        && arguments.get("includeFolderTypes").asBoolean(false);

    if (StringUtils.isBlank(stemQueryFilterType)) {
      return buildErrorResult("stemQueryFilterType is required.");
    }

    try {

      // build the WS stem query filter from the MCP arguments
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

      // Optionally look up Grouper object types (policy, ref, basis, manual, etc.)
      // for each stem. The WS findStems response doesn't include type info, so we
      // need to re-fetch the Stem objects and use the GrouperObjectTypesConfiguration
      // API to batch-retrieve the type attributes. The result is a map from stem name
      // to a list of type values, which we later add as an "objectTypes" array on each stem.
      Map<String, List<GrouperObjectTypesAttributeValue>> stemNameToTypes = null;
      if (includeFolderTypes && stemCount > 0) {

        // collect unique stem names from the WS results
        Set<String> stemNames = new HashSet<>();
        for (WsStem wsStem : stems) {
          if (StringUtils.isNotBlank(wsStem.getName())) {
            stemNames.add(wsStem.getName());
          }
        }
        if (stemNames.size() > 0) {

          // re-fetch Stem objects from the database (needed for the types API)
          Set<Stem> stemObjects = new StemFinder().assignStemNames(stemNames).findStems();

          // batch-retrieve object type attributes for all stems at once
          Map<GrouperObject, List<GrouperObjectTypesAttributeValue>> typesMap =
              GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stemObjects);

          // convert to a name-keyed map for easy lookup when building the response
          stemNameToTypes = new HashMap<>();
          for (Map.Entry<GrouperObject, List<GrouperObjectTypesAttributeValue>> entry : typesMap.entrySet()) {
            if (entry.getKey() instanceof Stem) {
              stemNameToTypes.put(((Stem) entry.getKey()).getName(), entry.getValue());
            }
          }
        }
      }

      // build the response array with each stem's details
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
          // append object type names (e.g., "policy", "ref") if types were requested and found
          if (stemNameToTypes != null && StringUtils.isNotBlank(stem.getName())) {
            List<GrouperObjectTypesAttributeValue> typeValues = stemNameToTypes.get(stem.getName());
            if (typeValues != null && typeValues.size() > 0) {
              ArrayNode typesArray = objectMapper.createArrayNode();
              for (GrouperObjectTypesAttributeValue typeValue : typeValues) {
                if (StringUtils.isNotBlank(typeValue.getObjectTypeName())) {
                  typesArray.add(typeValue.getObjectTypeName());
                }
              }
              if (typesArray.size() > 0) {
                stemNode.set("objectTypes", typesArray);
              }
            }
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
   * Build a successful MCP tool result with the standard content array format.
   * @param text the result text (typically JSON) to return to the MCP client
   * @return ObjectNode with isError=false and a content array containing the text
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
   * Build an error MCP tool result with the standard content array format.
   * @param errorMessage the error message to return to the MCP client
   * @return ObjectNode with isError=true and a content array containing the error message
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

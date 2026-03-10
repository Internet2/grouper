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

import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults;

/**
 * MCP tool handler for finding attribute definition names in Grouper.
 * Supports searching by scope (partial name match) or exact name lookup.
 * Delegates to the WS findAttributeDefNames service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpFindAttributeDefNames {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFindAttributeDefNames.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for attribute_def_name_find
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "attribute_def_name_find");
    tool.put("description",
        "Find attribute definition names in Grouper by scope (partial name match) "
        + "or exact name. Supports filtering by attribute assign type and "
        + "attribute def type. Returns matching attribute def names with "
        + "their details.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode scopeProp = objectMapper.createObjectNode();
    scopeProp.put("type", "string");
    scopeProp.put("description",
        "Search scope for partial name matching "
        + "(e.g., 'etc:attribute' to find attribute def names containing that string).");
    properties.set("scope", scopeProp);

    ObjectNode splitScopeProp = objectMapper.createObjectNode();
    splitScopeProp.put("type", "boolean");
    splitScopeProp.put("description",
        "If true, split the scope by whitespace and search each term separately.");
    properties.set("splitScope", splitScopeProp);

    ObjectNode attributeDefNameProp = objectMapper.createObjectNode();
    attributeDefNameProp.put("type", "string");
    attributeDefNameProp.put("description",
        "Exact attribute def name to look up "
        + "(e.g., 'etc:attribute:attrDefName').");
    properties.set("attributeDefName", attributeDefNameProp);

    ObjectNode attributeAssignTypeProp = objectMapper.createObjectNode();
    attributeAssignTypeProp.put("type", "string");
    ArrayNode assignTypeEnum = objectMapper.createArrayNode();
    assignTypeEnum.add("group");
    assignTypeEnum.add("stem");
    assignTypeEnum.add("member");
    assignTypeEnum.add("group_asgn");
    assignTypeEnum.add("stem_asgn");
    assignTypeEnum.add("member_asgn");
    assignTypeEnum.add("imm_mem");
    assignTypeEnum.add("imm_mem_asgn");
    assignTypeEnum.add("attr_def");
    assignTypeEnum.add("attr_def_asgn");
    assignTypeEnum.add("any_mem");
    assignTypeEnum.add("any_mem_asgn");
    attributeAssignTypeProp.set("enum", assignTypeEnum);
    attributeAssignTypeProp.put("description",
        "The attribute assign type to filter by.");
    properties.set("attributeAssignType", attributeAssignTypeProp);

    ObjectNode attributeDefTypeProp = objectMapper.createObjectNode();
    attributeDefTypeProp.put("type", "string");
    ArrayNode defTypeEnum = objectMapper.createArrayNode();
    defTypeEnum.add("attr");
    defTypeEnum.add("domain");
    defTypeEnum.add("limit");
    defTypeEnum.add("perm");
    defTypeEnum.add("service");
    defTypeEnum.add("type");
    attributeDefTypeProp.set("enum", defTypeEnum);
    attributeDefTypeProp.put("description",
        "The attribute def type to filter by.");
    properties.set("attributeDefType", attributeDefTypeProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of results per page. Default is 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number to retrieve (1-based). Default is 1.");
    pageNumberProp.put("default", 1);
    properties.set("pageNumber", pageNumberProp);

    inputSchema.set("properties", properties);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the attribute_def_name_find tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String scope = arguments != null && arguments.has("scope")
        ? arguments.get("scope").asText() : null;
    Boolean splitScope = arguments != null && arguments.has("splitScope")
        ? arguments.get("splitScope").asBoolean() : null;
    String attributeDefName = arguments != null && arguments.has("attributeDefName")
        ? arguments.get("attributeDefName").asText() : null;
    String attributeAssignTypeString = arguments != null && arguments.has("attributeAssignType")
        ? arguments.get("attributeAssignType").asText() : null;
    String attributeDefTypeString = arguments != null && arguments.has("attributeDefType")
        ? arguments.get("attributeDefType").asText() : null;
    Integer pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : 50;
    Integer pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt() : 1;

    try {

      WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
      if (StringUtils.isNotBlank(attributeDefName)) {
        wsAttributeDefNameLookups = new WsAttributeDefNameLookup[] {
            new WsAttributeDefNameLookup(attributeDefName, null)
        };
      }

      AttributeAssignType attrAssignType = null;
      if (StringUtils.isNotBlank(attributeAssignTypeString)) {
        attrAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, false);
      }

      AttributeDefType attrDefType = null;
      if (StringUtils.isNotBlank(attributeDefTypeString)) {
        attrDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, false);
      }

      WsFindAttributeDefNamesResults wsResults = GrouperServiceLogic.findAttributeDefNames(
          GrouperVersion.currentVersion(),
          scope, splitScope,
          null,   // wsAttributeDefLookup
          attrAssignType, attrDefType,
          wsAttributeDefNameLookups,
          pageSize, pageNumber,
          null, null,  // sort
          null,   // wsInheritanceSetRelation
          null,   // actAsSubjectLookup
          null,   // params
          null, null,  // wsSubjectLookup, serviceRole
          null, null, null, null  // cursor paging
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAttributeDefName[] attributeDefNames = wsResults.getAttributeDefNameResults();
      if (GrouperUtil.length(attributeDefNames) == 0) {
        return buildSuccessResult("No attribute def names found.");
      }

      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (WsAttributeDefName wsAttrDefName : attributeDefNames) {
        resultsArray.add(convertAttributeDefNameToJson(wsAttrDefName));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error finding attribute def names", e);
      return buildErrorResult("Error finding attribute def names: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * convert a WsAttributeDefName to a clean JSON object for MCP consumption.
   * @param wsAttrDefName the WS attribute def name
   * @return clean JSON object
   */
  private static ObjectNode convertAttributeDefNameToJson(WsAttributeDefName wsAttrDefName) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    resultNode.put("name", wsAttrDefName.getName());
    resultNode.put("uuid", wsAttrDefName.getUuid());
    if (StringUtils.isNotBlank(wsAttrDefName.getDescription())) {
      resultNode.put("description", wsAttrDefName.getDescription());
    }
    if (StringUtils.isNotBlank(wsAttrDefName.getDisplayName())) {
      resultNode.put("displayName", wsAttrDefName.getDisplayName());
    }
    if (StringUtils.isNotBlank(wsAttrDefName.getAttributeDefName())) {
      resultNode.put("attributeDefName", wsAttrDefName.getAttributeDefName());
    }

    return resultNode;
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

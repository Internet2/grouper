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
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter;

/**
 * MCP tool handler for finding Grouper groups.
 * Supports searching by name (exact or approximate), by stem, by attribute,
 * with paging and sorting.
 *
 * @author mchyzer
 */
public class GrouperMcpFindGroups {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFindGroups.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_find
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_find");
    tool.put("description",
        "Search for Grouper groups by name, stem, or attribute. "
        + "Supports exact and approximate name matching, "
        + "searching within a specific stem, and paging/sorting.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode queryFilterTypeProp = objectMapper.createObjectNode();
    queryFilterTypeProp.put("type", "string");
    ArrayNode qftEnum = objectMapper.createArrayNode();
    qftEnum.add("FIND_BY_GROUP_NAME_EXACT");
    qftEnum.add("FIND_BY_GROUP_NAME_APPROXIMATE");
    qftEnum.add("FIND_BY_STEM_NAME");
    qftEnum.add("FIND_BY_GROUP_UUID");
    qftEnum.add("FIND_BY_APPROXIMATE_ATTRIBUTE");
    queryFilterTypeProp.set("enum", qftEnum);
    queryFilterTypeProp.put("description",
        "Type of search to perform. "
        + "FIND_BY_GROUP_NAME_EXACT = exact name match, "
        + "FIND_BY_GROUP_NAME_APPROXIMATE = approximate name match (most common), "
        + "FIND_BY_STEM_NAME = all groups in a stem, "
        + "FIND_BY_GROUP_UUID = find by UUID, "
        + "FIND_BY_APPROXIMATE_ATTRIBUTE = search by attribute value.");
    properties.set("queryFilterType", queryFilterTypeProp);

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "Group name to search for. Used with FIND_BY_GROUP_NAME_EXACT "
        + "or FIND_BY_GROUP_NAME_APPROXIMATE.");
    properties.set("groupName", groupNameProp);

    ObjectNode groupUuidProp = objectMapper.createObjectNode();
    groupUuidProp.put("type", "string");
    groupUuidProp.put("description",
        "Group UUID. Used with FIND_BY_GROUP_UUID.");
    properties.set("groupUuid", groupUuidProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Stem name to search within. Used with FIND_BY_STEM_NAME, "
        + "or to scope a FIND_BY_GROUP_NAME_APPROXIMATE search.");
    properties.set("stemName", stemNameProp);

    ObjectNode stemNameScopeProp = objectMapper.createObjectNode();
    stemNameScopeProp.put("type", "string");
    ArrayNode scopeEnum = objectMapper.createArrayNode();
    scopeEnum.add("ONE_LEVEL");
    scopeEnum.add("ALL_IN_SUBTREE");
    stemNameScopeProp.set("enum", scopeEnum);
    stemNameScopeProp.put("description",
        "Scope when searching in a stem. ONE_LEVEL = direct children only, "
        + "ALL_IN_SUBTREE = all descendants (default).");
    properties.set("stemNameScope", stemNameScopeProp);

    ObjectNode groupAttributeValueProp = objectMapper.createObjectNode();
    groupAttributeValueProp.put("type", "string");
    groupAttributeValueProp.put("description",
        "Attribute value to search for. Used with FIND_BY_APPROXIMATE_ATTRIBUTE.");
    properties.set("groupAttributeValue", groupAttributeValueProp);

    ObjectNode typeOfGroupsProp = objectMapper.createObjectNode();
    typeOfGroupsProp.put("type", "string");
    typeOfGroupsProp.put("description",
        "Comma-separated types of groups to return: group, role, entity. "
        + "Default is all types.");
    properties.set("typeOfGroups", typeOfGroupsProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of results per page. Default is 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number (1-indexed). Default is 1.");
    pageNumberProp.put("default", 1);
    properties.set("pageNumber", pageNumberProp);

    ObjectNode sortStringProp = objectMapper.createObjectNode();
    sortStringProp.put("type", "string");
    sortStringProp.put("description",
        "Field to sort by: name, displayName, extension, displayExtension.");
    properties.set("sortString", sortStringProp);

    ObjectNode ascendingProp = objectMapper.createObjectNode();
    ascendingProp.put("type", "boolean");
    ascendingProp.put("description",
        "Sort ascending (true, default) or descending (false).");
    properties.set("ascending", ascendingProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("queryFilterType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_find tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String queryFilterType = arguments != null && arguments.has("queryFilterType")
        ? arguments.get("queryFilterType").asText() : null;
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String groupUuid = arguments != null && arguments.has("groupUuid")
        ? arguments.get("groupUuid").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String stemNameScope = arguments != null && arguments.has("stemNameScope")
        ? arguments.get("stemNameScope").asText() : null;
    String groupAttributeValue = arguments != null && arguments.has("groupAttributeValue")
        ? arguments.get("groupAttributeValue").asText() : null;
    String typeOfGroups = arguments != null && arguments.has("typeOfGroups")
        ? arguments.get("typeOfGroups").asText() : null;
    int pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt(50) : 50;
    int pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt(1) : 1;
    String sortString = arguments != null && arguments.has("sortString")
        ? arguments.get("sortString").asText() : null;
    String ascending = arguments != null && arguments.has("ascending")
        ? (arguments.get("ascending").asBoolean(true) ? "T" : "F") : null;

    if (StringUtils.isBlank(queryFilterType)) {
      return buildErrorResult("queryFilterType is required.");
    }

    try {

      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.setQueryFilterType(queryFilterType);

      if (StringUtils.isNotBlank(groupName)) {
        wsQueryFilter.setGroupName(groupName);
      }
      if (StringUtils.isNotBlank(groupUuid)) {
        wsQueryFilter.setGroupUuid(groupUuid);
      }
      if (StringUtils.isNotBlank(stemName)) {
        wsQueryFilter.setStemName(stemName);
      }
      if (StringUtils.isNotBlank(stemNameScope)) {
        wsQueryFilter.setStemNameScope(stemNameScope);
      }
      if (StringUtils.isNotBlank(groupAttributeValue)) {
        wsQueryFilter.setGroupAttributeValue(groupAttributeValue);
      }
      if (StringUtils.isNotBlank(typeOfGroups)) {
        wsQueryFilter.setTypeOfGroups(typeOfGroups);
      }
      wsQueryFilter.setPageSize(String.valueOf(pageSize));
      wsQueryFilter.setPageNumber(String.valueOf(pageNumber));
      if (StringUtils.isNotBlank(sortString)) {
        wsQueryFilter.setSortString(sortString);
      }
      if (StringUtils.isNotBlank(ascending)) {
        wsQueryFilter.setAscending(ascending);
      }

      WsFindGroupsResults wsResults = GrouperServiceLogic.findGroups(
          GrouperVersion.currentVersion(),
          wsQueryFilter,
          null,   // actAsSubjectLookup
          false,  // includeGroupDetail
          null,   // params
          null    // wsGroupLookups
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      ObjectNode resultNode = objectMapper.createObjectNode();
      WsGroup[] groups = wsResults.getGroupResults();
      int groupCount = GrouperUtil.length(groups);
      resultNode.put("totalGroupsReturned", groupCount);
      resultNode.put("pageSize", pageSize);
      resultNode.put("pageNumber", pageNumber);

      ArrayNode groupsArray = objectMapper.createArrayNode();
      if (groupCount > 0) {
        for (WsGroup group : groups) {
          ObjectNode groupNode = objectMapper.createObjectNode();
          groupNode.put("name", group.getName());
          if (StringUtils.isNotBlank(group.getDisplayName())) {
            groupNode.put("displayName", group.getDisplayName());
          }
          if (StringUtils.isNotBlank(group.getExtension())) {
            groupNode.put("extension", group.getExtension());
          }
          if (StringUtils.isNotBlank(group.getDescription())) {
            groupNode.put("description", group.getDescription());
          }
          if (StringUtils.isNotBlank(group.getUuid())) {
            groupNode.put("uuid", group.getUuid());
          }
          if (StringUtils.isNotBlank(group.getTypeOfGroup())) {
            groupNode.put("typeOfGroup", group.getTypeOfGroup());
          }
          groupsArray.add(groupNode);
        }
      }
      resultNode.set("groups", groupsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error finding groups", e);
      return buildErrorResult("Error finding groups: " + e.getMessage());
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

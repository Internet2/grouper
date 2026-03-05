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
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for finding what groups a subject belongs to.
 * Supports filtering by member filter, field, scope, stem, and paging.
 * Delegates to the WS getGroups service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetGroups {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetGroups.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for entity_get_groups
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "entity_get_groups");
    tool.put("description",
        "Find what groups a subject belongs to. "
        + "The subject is identified by subjectId or subjectIdentifier "
        + "(and optionally subjectSourceId). "
        + "Supports filtering by membership type, field, stem scope, and paging.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "The subject ID to find groups for. "
        + "Mutually exclusive with subjectIdentifier.");
    properties.set("subjectId", subjectIdProp);

    ObjectNode subjectIdentifierProp = objectMapper.createObjectNode();
    subjectIdentifierProp.put("type", "string");
    subjectIdentifierProp.put("description",
        "The subject identifier (e.g., login ID or eppn). "
        + "Mutually exclusive with subjectId.");
    properties.set("subjectIdentifier", subjectIdentifierProp);

    ObjectNode subjectSourceIdProp = objectMapper.createObjectNode();
    subjectSourceIdProp.put("type", "string");
    subjectSourceIdProp.put("description",
        "Optional source ID to restrict the subject lookup to a specific source.");
    properties.set("subjectSourceId", subjectSourceIdProp);

    ObjectNode memberFilterProp = objectMapper.createObjectNode();
    memberFilterProp.put("type", "string");
    ArrayNode memberFilterEnum = objectMapper.createArrayNode();
    memberFilterEnum.add("All");
    memberFilterEnum.add("Immediate");
    memberFilterEnum.add("Effective");
    memberFilterEnum.add("Composite");
    memberFilterEnum.add("NonImmediate");
    memberFilterProp.set("enum", memberFilterEnum);
    memberFilterProp.put("description",
        "Membership filter type. "
        + "All = all memberships (default), Immediate = direct only, "
        + "Effective = indirect only, Composite = composite memberships, "
        + "NonImmediate = non-direct memberships only.");
    properties.set("memberFilter", memberFilterProp);

    ObjectNode fieldNameProp = objectMapper.createObjectNode();
    fieldNameProp.put("type", "string");
    fieldNameProp.put("description",
        "Field (list) name for the membership. "
        + "Defaults to 'members' (the standard membership list).");
    properties.set("fieldName", fieldNameProp);

    ObjectNode scopeProp = objectMapper.createObjectNode();
    scopeProp.put("type", "string");
    scopeProp.put("description",
        "Scope string to filter groups (e.g., 'stem1:stem2:').");
    properties.set("scope", scopeProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Stem name to scope the search to (e.g., 'stem1:stem2').");
    properties.set("stemName", stemNameProp);

    ObjectNode stemScopeProp = objectMapper.createObjectNode();
    stemScopeProp.put("type", "string");
    ArrayNode stemScopeEnum = objectMapper.createArrayNode();
    stemScopeEnum.add("ONE_LEVEL");
    stemScopeEnum.add("ALL_IN_SUBTREE");
    stemScopeProp.set("enum", stemScopeEnum);
    stemScopeProp.put("description",
        "Stem scope when stemName is specified. "
        + "ONE_LEVEL = direct children only, ALL_IN_SUBTREE = all descendants.");
    properties.set("stemScope", stemScopeProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of groups to return per page.");
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number to return (1-based).");
    properties.set("pageNumber", pageNumberProp);

    inputSchema.set("properties", properties);

    // no required fields - validation is done in execute()

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the entity_get_groups tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String subjectId = arguments != null && arguments.has("subjectId")
        ? arguments.get("subjectId").asText() : null;
    String subjectIdentifier = arguments != null && arguments.has("subjectIdentifier")
        ? arguments.get("subjectIdentifier").asText() : null;
    String subjectSourceId = arguments != null && arguments.has("subjectSourceId")
        ? arguments.get("subjectSourceId").asText() : null;
    String memberFilterString = arguments != null && arguments.has("memberFilter")
        ? arguments.get("memberFilter").asText() : null;
    String fieldNameString = arguments != null && arguments.has("fieldName")
        ? arguments.get("fieldName").asText() : null;
    String scope = arguments != null && arguments.has("scope")
        ? arguments.get("scope").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String stemScopeString = arguments != null && arguments.has("stemScope")
        ? arguments.get("stemScope").asText() : null;
    Integer pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : null;
    Integer pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt() : null;

    // validate that exactly one of subjectId or subjectIdentifier is provided
    if (StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectIdentifier)) {
      return buildErrorResult(
          "One of subjectId or subjectIdentifier is required.");
    }
    if (StringUtils.isNotBlank(subjectId) && StringUtils.isNotBlank(subjectIdentifier)) {
      return buildErrorResult(
          "Only one of subjectId or subjectIdentifier may be provided, not both.");
    }

    try {

      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[] {
          new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier)
      };

      WsStemLookup wsStemLookup = null;
      if (StringUtils.isNotBlank(stemName)) {
        wsStemLookup = new WsStemLookup(stemName, null);
      }

      StemScope stemScopeEnum = null;
      if (StringUtils.isNotBlank(stemScopeString)) {
        stemScopeEnum = StemScope.valueOf(stemScopeString);
      }

      WsMemberFilter wsMemberFilter = null;
      if (StringUtils.isNotBlank(memberFilterString)) {
        wsMemberFilter = GrouperServiceUtils.convertMemberFilter(memberFilterString);
      }

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsGetGroupsResults wsResults = GrouperServiceLogic.getGroups(
          GrouperVersion.currentVersion(),
          subjectLookups,
          wsMemberFilter,
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          false,  // includeGroupDetail
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          null,   // params
          fieldNameString,
          scope,
          wsStemLookup,
          stemScopeEnum,
          null,   // enabled
          pageSize,
          pageNumber,
          null,   // sortString
          null,   // ascending
          null, null,  // pointInTime
          null, null, null, null  // cursor paging
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // get the groups from the first (and only) result
      WsGetGroupsResult[] results = wsResults.getResults();
      if (GrouperUtil.length(results) == 0) {
        return buildSuccessResult("No results returned.");
      }

      WsGroup[] wsGroups = results[0].getWsGroups();
      if (GrouperUtil.length(wsGroups) == 0) {
        return buildSuccessResult("Subject is not a member of any groups matching the criteria.");
      }

      // convert WsGroups to JSON array
      ArrayNode groupsArray = objectMapper.createArrayNode();
      for (WsGroup wsGroup : wsGroups) {
        groupsArray.add(convertWsGroupToJson(wsGroup));
      }

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(groupsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      String lookupDescription = StringUtils.isNotBlank(subjectId)
          ? subjectId : subjectIdentifier;
      LOG.error("Error getting groups for subject: " + lookupDescription, e);
      return buildErrorResult("Error getting groups for subject: " + e.getMessage());
    }
  }

  /**
   * convert a WsGroup to a clean JSON object for MCP consumption.
   * @param wsGroup the WS group
   * @return clean JSON object
   */
  private static ObjectNode convertWsGroupToJson(WsGroup wsGroup) {
    ObjectNode groupNode = objectMapper.createObjectNode();
    groupNode.put("name", wsGroup.getName());
    if (StringUtils.isNotBlank(wsGroup.getExtension())) {
      groupNode.put("extension", wsGroup.getExtension());
    }
    if (StringUtils.isNotBlank(wsGroup.getDisplayExtension())) {
      groupNode.put("displayExtension", wsGroup.getDisplayExtension());
    }
    if (StringUtils.isNotBlank(wsGroup.getDescription())) {
      groupNode.put("description", wsGroup.getDescription());
    }
    groupNode.put("uuid", wsGroup.getUuid());
    return groupNode;
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

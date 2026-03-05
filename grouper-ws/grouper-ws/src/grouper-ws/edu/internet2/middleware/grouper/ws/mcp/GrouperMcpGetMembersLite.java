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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for getting members of a Grouper group.
 * Supports filtering by member filter, field, source IDs, and paging.
 * Delegates to the WS getMembersLite service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetMembersLite {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetMembersLite.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_get_members
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_get_members");
    tool.put("description",
        "Get members of a Grouper group. "
        + "Returns subjects that are members of the specified group. "
        + "Supports filtering by membership type, field, source IDs, and paging. "
        + "Defaults to page size 50 and page number 1 to prevent returning too many results.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to get members from "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

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
        + "All = all members (default), Immediate = direct members only, "
        + "Effective = indirect members only, Composite = composite members, "
        + "NonImmediate = non-direct members only.");
    properties.set("memberFilter", memberFilterProp);

    ObjectNode fieldNameProp = objectMapper.createObjectNode();
    fieldNameProp.put("type", "string");
    fieldNameProp.put("description",
        "Field (list) name for the membership. "
        + "Defaults to 'members' (the standard membership list).");
    properties.set("fieldName", fieldNameProp);

    ObjectNode sourceIdsProp = objectMapper.createObjectNode();
    sourceIdsProp.put("type", "string");
    sourceIdsProp.put("description",
        "Comma-separated source IDs to restrict the results to specific sources.");
    properties.set("sourceIds", sourceIdsProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of members to return per page. Defaults to 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number to return (1-based). Defaults to 1.");
    pageNumberProp.put("default", 1);
    properties.set("pageNumber", pageNumberProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_get_members tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String memberFilterString = arguments != null && arguments.has("memberFilter")
        ? arguments.get("memberFilter").asText() : null;
    String fieldNameString = arguments != null && arguments.has("fieldName")
        ? arguments.get("fieldName").asText() : null;
    String sourceIdsString = arguments != null && arguments.has("sourceIds")
        ? arguments.get("sourceIds").asText() : null;
    Integer pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : 50;
    Integer pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt() : 1;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    try {

      // convert memberFilter string to WsMemberFilter
      WsMemberFilter wsMemberFilter = null;
      if (StringUtils.isNotBlank(memberFilterString)) {
        wsMemberFilter = GrouperServiceUtils.convertMemberFilter(memberFilterString);
      }

      // convert fieldName string to Field
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsGetMembersLiteResult wsResult = GrouperServiceLogic.getMembersLite(
          GrouperVersion.currentVersion(),
          groupName,
          null,   // groupUuid
          wsMemberFilter,
          null, null, null,  // actAs
          field,
          false,  // includeGroupDetail
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          null, null, null, null,  // params
          sourceIdsString,
          null, null,  // pointInTime
          pageSize,
          pageNumber,
          null, null,  // sort
          null,   // pointInTimeRetrieve
          null, null, null, null  // cursor paging
      );

      // check for overall errors
      if (wsResult.getResultMetadata() != null
          && !"T".equals(wsResult.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResult.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result with paging info
      WsSubject[] wsSubjects = wsResult.getWsSubjects();

      ObjectNode responseNode = objectMapper.createObjectNode();
      responseNode.put("pageSize", pageSize);
      responseNode.put("pageNumber", pageNumber);

      ArrayNode subjectsArray = objectMapper.createArrayNode();
      if (GrouperUtil.length(wsSubjects) > 0) {
        for (WsSubject wsSubject : wsSubjects) {
          subjectsArray.add(convertWsSubjectToJson(wsSubject));
        }
      }
      responseNode.set("subjects", subjectsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(responseNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting members of group: " + groupName, e);
      return buildErrorResult("Error getting members of group: " + e.getMessage());
    }
  }

  /**
   * convert a WsSubject to a clean JSON object for MCP consumption.
   * @param wsSubject the WS subject
   * @return clean JSON object
   */
  private static ObjectNode convertWsSubjectToJson(WsSubject wsSubject) {
    ObjectNode subjectNode = objectMapper.createObjectNode();
    subjectNode.put("subjectId", wsSubject.getId());
    if (StringUtils.isNotBlank(wsSubject.getName())) {
      subjectNode.put("name", wsSubject.getName());
    }
    subjectNode.put("sourceId", wsSubject.getSourceId());
    return subjectNode;
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

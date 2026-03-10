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

import java.util.ArrayList;
import java.util.List;

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
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for removing members from a Grouper group.
 * Supports removing one or more subjects by subject ID or identifier.
 * Delegates to the WS deleteMember service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpDeleteMember {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpDeleteMember.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_remove_member
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_remove_member");
    tool.put("description",
        "Remove one or more subjects as members of a Grouper group. "
        + "Each subject is identified by subjectId or subjectIdentifier "
        + "(and optionally sourceId).");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to remove members from "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

    ObjectNode subjectsProp = objectMapper.createObjectNode();
    subjectsProp.put("type", "array");
    ObjectNode subjectsItemsNode = objectMapper.createObjectNode();
    subjectsItemsNode.put("type", "object");
    ObjectNode subjectProperties = objectMapper.createObjectNode();

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "The subject ID. Mutually exclusive with subjectIdentifier.");
    subjectProperties.set("subjectId", subjectIdProp);

    ObjectNode subjectIdentifierProp = objectMapper.createObjectNode();
    subjectIdentifierProp.put("type", "string");
    subjectIdentifierProp.put("description",
        "The subject identifier (e.g., login ID or eppn). "
        + "Mutually exclusive with subjectId.");
    subjectProperties.set("subjectIdentifier", subjectIdentifierProp);

    ObjectNode sourceIdProp = objectMapper.createObjectNode();
    sourceIdProp.put("type", "string");
    sourceIdProp.put("description",
        "Optional source ID to restrict the subject lookup to a specific source.");
    subjectProperties.set("sourceId", sourceIdProp);

    subjectsItemsNode.set("properties", subjectProperties);
    subjectsProp.set("items", subjectsItemsNode);
    subjectsProp.put("description",
        "Array of subjects to remove from the group. Each subject must have either "
        + "subjectId or subjectIdentifier (and optionally sourceId).");
    properties.set("subjects", subjectsProp);

    ObjectNode fieldNameProp = objectMapper.createObjectNode();
    fieldNameProp.put("type", "string");
    fieldNameProp.put("description",
        "Field (list) name for the membership. "
        + "Defaults to 'members' (the standard membership list).");
    properties.set("fieldName", fieldNameProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    required.add("subjects");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_remove_member tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String fieldNameString = arguments != null && arguments.has("fieldName")
        ? arguments.get("fieldName").asText() : null;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    // block modifications to protected system groups
    if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (!authUser.isGroupInReadwriteScope(groupName)) {
        return buildErrorResult("Access denied: group '" + groupName
            + "' is outside your consented read-write scope.");
      }
    }

    // parse subjects array
    if (arguments == null || !arguments.has("subjects")
        || !arguments.get("subjects").isArray()
        || arguments.get("subjects").size() == 0) {
      return buildErrorResult("subjects array is required and must not be empty.");
    }

    JsonNode subjectsArray = arguments.get("subjects");
    List<WsSubjectLookup> subjectLookupList = new ArrayList<WsSubjectLookup>();

    for (int i = 0; i < subjectsArray.size(); i++) {
      JsonNode subjectNode = subjectsArray.get(i);
      String subjectId = subjectNode.has("subjectId")
          ? subjectNode.get("subjectId").asText() : null;
      String subjectIdentifier = subjectNode.has("subjectIdentifier")
          ? subjectNode.get("subjectIdentifier").asText() : null;
      String sourceId = subjectNode.has("sourceId")
          ? subjectNode.get("sourceId").asText() : null;

      if (StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectIdentifier)) {
        return buildErrorResult(
            "Each subject must have either subjectId or subjectIdentifier "
            + "(subject at index " + i + " has neither).");
      }
      if (StringUtils.isNotBlank(subjectId) && StringUtils.isNotBlank(subjectIdentifier)) {
        return buildErrorResult(
            "Each subject must have either subjectId or subjectIdentifier, not both "
            + "(subject at index " + i + " has both).");
      }

      // check readwrite scope restrictions on subject (OAuth only)
      if (authUser.isOAuthAuthenticated()) {
        String subjectValue = StringUtils.isNotBlank(subjectId) ? subjectId : subjectIdentifier;
        if (subjectValue != null && !authUser.isSubjectInReadwriteScope(subjectValue)) {
          return buildErrorResult("Access denied: subject '" + subjectValue
              + "' is outside your consented read-write scope.");
        }
      }

      subjectLookupList.add(new WsSubjectLookup(subjectId, sourceId, subjectIdentifier));
    }

    WsSubjectLookup[] subjectLookups =
        subjectLookupList.toArray(new WsSubjectLookup[0]);

    try {

      // build the group lookup
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName(groupName);

      // convert fieldName string to Field
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsDeleteMemberResults wsResults = GrouperServiceLogic.deleteMember(
          GrouperVersion.currentVersion(),
          wsGroupLookup,
          subjectLookups,
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          field,
          null,   // txType
          false,  // includeGroupDetail
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          null    // params
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsDeleteMemberResult[] memberResults = wsResults.getResults();
      if (GrouperUtil.length(memberResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      if (GrouperUtil.length(memberResults) == 1) {
        // single subject - return as object
        ObjectNode resultNode = convertDeleteMemberResultToJson(memberResults[0]);
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultNode);
        return buildSuccessResult(resultText);
      } else {
        // multiple subjects - return as array
        ArrayNode resultsArray = objectMapper.createArrayNode();
        for (WsDeleteMemberResult memberResult : memberResults) {
          resultsArray.add(convertDeleteMemberResultToJson(memberResult));
        }
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultsArray);
        return buildSuccessResult(resultText);
      }

    } catch (Exception e) {
      LOG.error("Error removing members from group: " + groupName, e);
      return buildErrorResult("Error removing members from group: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * convert a WsDeleteMemberResult to a clean JSON object for MCP consumption.
   * @param memberResult the WS delete member result
   * @return clean JSON object
   */
  private static ObjectNode convertDeleteMemberResultToJson(WsDeleteMemberResult memberResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    // include the result code (SUCCESS, SUCCESS_WASNT_IMMEDIATE, etc.)
    if (memberResult.getResultMetadata() != null) {
      resultNode.put("resultCode",
          memberResult.getResultMetadata().getResultCode());
      resultNode.put("success",
          "T".equals(memberResult.getResultMetadata().getSuccess()));
    }

    // include subject info
    if (memberResult.getWsSubject() != null) {
      resultNode.put("subjectId", memberResult.getWsSubject().getId());
      if (StringUtils.isNotBlank(memberResult.getWsSubject().getName())) {
        resultNode.put("name", memberResult.getWsSubject().getName());
      }
      resultNode.put("sourceId", memberResult.getWsSubject().getSourceId());
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

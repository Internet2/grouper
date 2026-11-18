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
        + "Each subject is identified by subjectIdOrIdentifier "
        + "(and optionally sourceId and subjectIdType).");

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

    GrouperMcpSubjectUtils.addSubjectArrayItemProperties(subjectProperties);

    subjectsItemsNode.set("properties", subjectProperties);
    subjectsProp.set("items", subjectsItemsNode);
    subjectsProp.put("description",
        "Array of subjects to remove from the group. Each subject must have a "
        + "subjectIdOrIdentifier (and optionally sourceId and subjectIdType).");
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

    // protected-resource rules and read-write scope on the group the name really resolves to
    // (it can be an alternate, old, name), see GrouperMcpResolvedTargetGate.  the write below
    // uses that group's uuid, so the group checked is the group changed
    GrouperMcpResolvedTargetGate.Target groupTarget =
        GrouperMcpResolvedTargetGate.checkGroupByName(authUser, groupName);
    if (groupTarget.getError() != null) {
      return buildErrorResult(groupTarget.getError());
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
      String subjectIdOrIdentifier = subjectNode.has("subjectIdOrIdentifier")
          ? subjectNode.get("subjectIdOrIdentifier").asText() : null;
      String subjectIdType = subjectNode.has("subjectIdType")
          ? subjectNode.get("subjectIdType").asText() : null;
      String sourceId = subjectNode.has("sourceId")
          ? subjectNode.get("sourceId").asText() : null;

      if (StringUtils.isBlank(subjectIdOrIdentifier)) {
        return buildErrorResult(
            "Each subject must have a subjectIdOrIdentifier "
            + "(subject at index " + i + " is missing it).");
      }

      // validate subjectIdType if provided
      String subjectIdTypeError = GrouperMcpSubjectUtils.validateSubjectIdType(subjectIdType);
      if (subjectIdTypeError != null) {
        return buildErrorResult(subjectIdTypeError + " (subject at index " + i + ").");
      }

      // the lookup the write will use.  the scope check resolves it (by value, id type and source)
      // and the lookup keeps that subject, so the subject checked is the subject removed
      WsSubjectLookup subjectLookup = GrouperMcpSubjectUtils.createSubjectLookup(
          subjectIdOrIdentifier, subjectIdType, sourceId);

      // check readwrite scope restrictions on subject, by source and id
      String subjectError = GrouperMcpResolvedTargetGate.checkSubjectLookup(
          authUser, subjectLookup, subjectIdOrIdentifier);
      if (subjectError != null) {
        return buildErrorResult(subjectError);
      }

      subjectLookupList.add(subjectLookup);
    }

    WsSubjectLookup[] subjectLookups =
        subjectLookupList.toArray(new WsSubjectLookup[0]);

    try {

      // build the group lookup: by the uuid the gate checked, else by name (not found, the web
      // service logic reports that)
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      if (groupTarget.isFound()) {
        wsGroupLookup.setUuid(groupTarget.getUuid());
      } else {
        wsGroupLookup.setGroupName(groupName);
      }

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
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
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

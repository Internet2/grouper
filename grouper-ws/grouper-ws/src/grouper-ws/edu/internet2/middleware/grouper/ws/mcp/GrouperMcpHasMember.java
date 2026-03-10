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

import java.sql.Timestamp;
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
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for checking if subjects are members of a Grouper group.
 * Supports checking one or more subjects by subject ID or identifier.
 * Delegates to the WS hasMember service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpHasMember {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpHasMember.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_has_member
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_has_member");
    tool.put("description",
        "Check if one or more subjects are members of a Grouper group. "
        + "Each subject is identified by subjectId or subjectIdentifier "
        + "(and optionally sourceId). Returns the membership status "
        + "(IS_MEMBER or IS_NOT_MEMBER) for each subject. "
        + "Supports point-in-time queries to check historical membership.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to check membership in "
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
        "Array of subjects to check membership for. Each subject must have either "
        + "subjectId or subjectIdentifier (and optionally sourceId).");
    properties.set("subjects", subjectsProp);

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

    ObjectNode privilegeListNameProp = objectMapper.createObjectNode();
    privilegeListNameProp.put("type", "string");
    ArrayNode privilegeListNameEnum = objectMapper.createArrayNode();
    privilegeListNameEnum.add(Field.FIELD_NAME_ADMINS);
    privilegeListNameEnum.add(Field.FIELD_NAME_UPDATERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_READERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_VIEWERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_OPTINS);
    privilegeListNameEnum.add(Field.FIELD_NAME_OPTOUTS);
    privilegeListNameEnum.add(Field.FIELD_NAME_GROUP_ATTR_READERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_GROUP_ATTR_UPDATERS);
    privilegeListNameProp.set("enum", privilegeListNameEnum);
    privilegeListNameProp.put("description",
        "Privilege list name to check instead of membership. "
        + "If omitted, checks the standard membership list. "
        + "Use this to check if a subject has a specific privilege on the group.");
    properties.set("privilegeListName", privilegeListNameProp);

    ObjectNode pointInTimeFromProp = objectMapper.createObjectNode();
    pointInTimeFromProp.put("type", "string");
    pointInTimeFromProp.put("description",
        "Start of point-in-time query range, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/01/01 00:00:00.000'). "
        + "Used to check historical membership. "
        + "If specified without pointInTimeTo, the range is from this time to now.");
    properties.set("pointInTimeFrom", pointInTimeFromProp);

    ObjectNode pointInTimeToProp = objectMapper.createObjectNode();
    pointInTimeToProp.put("type", "string");
    pointInTimeToProp.put("description",
        "End of point-in-time query range, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/12/31 23:59:59.000'). "
        + "Used to check historical membership. "
        + "If specified without pointInTimeFrom, the range is from the earliest "
        + "point in time to this time.");
    properties.set("pointInTimeTo", pointInTimeToProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    required.add("subjects");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_has_member tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String memberFilterString = arguments != null && arguments.has("memberFilter")
        ? arguments.get("memberFilter").asText() : "All";
    String fieldNameString = arguments != null && arguments.has("privilegeListName")
        ? arguments.get("privilegeListName").asText() : null;
    if (StringUtils.isBlank(fieldNameString) && arguments != null && arguments.has("fieldName")) {
      fieldNameString = arguments.get("fieldName").asText();
    }
    String pointInTimeFromString = arguments != null && arguments.has("pointInTimeFrom")
        ? arguments.get("pointInTimeFrom").asText() : null;
    String pointInTimeToString = arguments != null && arguments.has("pointInTimeTo")
        ? arguments.get("pointInTimeTo").asText() : null;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
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

      subjectLookupList.add(new WsSubjectLookup(subjectId, sourceId, subjectIdentifier));
    }

    WsSubjectLookup[] subjectLookups =
        subjectLookupList.toArray(new WsSubjectLookup[0]);

    try {

      // build the group lookup
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName(groupName);

      // convert memberFilter string to WsMemberFilter
      WsMemberFilter wsMemberFilter = null;
      if (StringUtils.isNotBlank(memberFilterString)) {
        wsMemberFilter = GrouperServiceUtils.convertMemberFilter(memberFilterString);
      }

      // convert fieldName string to Field
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // convert timestamp strings
      Timestamp pointInTimeFrom = GrouperServiceUtils.stringToTimestamp(pointInTimeFromString);
      Timestamp pointInTimeTo = GrouperServiceUtils.stringToTimestamp(pointInTimeToString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsHasMemberResults wsResults = GrouperServiceLogic.hasMember(
          GrouperVersion.currentVersion(),
          wsGroupLookup,
          subjectLookups,
          wsMemberFilter,
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          field,
          false,  // includeGroupDetail
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          null,   // params
          pointInTimeFrom,
          pointInTimeTo
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsHasMemberResult[] memberResults = wsResults.getResults();
      if (GrouperUtil.length(memberResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      if (GrouperUtil.length(memberResults) == 1) {
        // single subject - return as object
        ObjectNode resultNode = convertHasMemberResultToJson(memberResults[0]);
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultNode);
        return buildSuccessResult(resultText);
      } else {
        // multiple subjects - return as array
        ArrayNode resultsArray = objectMapper.createArrayNode();
        for (WsHasMemberResult memberResult : memberResults) {
          resultsArray.add(convertHasMemberResultToJson(memberResult));
        }
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultsArray);
        return buildSuccessResult(resultText);
      }

    } catch (Exception e) {
      LOG.error("Error checking membership in group: " + groupName, e);
      return buildErrorResult("Error checking membership in group: " + e.getMessage());
    }
  }

  /**
   * convert a WsHasMemberResult to a clean JSON object for MCP consumption.
   * @param memberResult the WS has member result
   * @return clean JSON object
   */
  private static ObjectNode convertHasMemberResultToJson(WsHasMemberResult memberResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    // include the result code (IS_MEMBER, IS_NOT_MEMBER, SUBJECT_NOT_FOUND, etc.)
    if (memberResult.getResultMetadata() != null) {
      String resultCode = memberResult.getResultMetadata().getResultCode();
      resultNode.put("resultCode", resultCode);
      resultNode.put("isMember", "IS_MEMBER".equals(resultCode));
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

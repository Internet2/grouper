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
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for looking up Grouper subjects.
 * Supports lookup by subject ID, subject identifier, or search string.
 * Can optionally filter by group membership.
 * Delegates to the WS getSubjects service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetSubjects {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetSubjects.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for entity_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "entity_get");
    tool.put("description",
        "Look up Grouper subjects by subject ID, subject identifier, or search string. "
        + "Provide exactly one of subjectId, subjectIdentifier, or searchString. "
        + "Can optionally filter to members of a specific group. "
        + "Returns subject details including name, source, "
        + "and optionally extended subject attributes.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "The subject ID to look up. Mutually exclusive with subjectIdentifier and searchString.");
    properties.set("subjectId", subjectIdProp);

    ObjectNode subjectIdentifierProp = objectMapper.createObjectNode();
    subjectIdentifierProp.put("type", "string");
    subjectIdentifierProp.put("description",
        "The subject identifier to look up (e.g., login ID or eppn). "
        + "Mutually exclusive with subjectId and searchString.");
    properties.set("subjectIdentifier", subjectIdentifierProp);

    ObjectNode searchStringProp = objectMapper.createObjectNode();
    searchStringProp.put("type", "string");
    searchStringProp.put("description",
        "Free-form search string to find subjects (e.g., name or partial match). "
        + "May return multiple results. Mutually exclusive with subjectId and subjectIdentifier.");
    properties.set("searchString", searchStringProp);

    ObjectNode sourceIdsProp = objectMapper.createObjectNode();
    sourceIdsProp.put("type", "array");
    ObjectNode sourceIdsItemsNode = objectMapper.createObjectNode();
    sourceIdsItemsNode.put("type", "string");
    sourceIdsProp.set("items", sourceIdsItemsNode);
    sourceIdsProp.put("description",
        "Optional source IDs to restrict the lookup to specific sources");
    properties.set("sourceIds", sourceIdsProp);

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "Optional group name to filter subjects by group membership "
        + "(e.g., 'stem1:stem2:groupName'). Only subjects who are members "
        + "of this group will be returned.");
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
        "Membership filter when groupName is specified. "
        + "All = all members (default), Immediate = direct members only, "
        + "Effective = indirect members only, Composite = composite members, "
        + "NonImmediate = non-direct members only.");
    properties.set("memberFilter", memberFilterProp);

    ObjectNode fieldNameProp = objectMapper.createObjectNode();
    fieldNameProp.put("type", "string");
    fieldNameProp.put("description",
        "Field (list) name for group membership filtering when groupName is specified. "
        + "Defaults to 'members' (the standard membership list).");
    properties.set("fieldName", fieldNameProp);

    ObjectNode includeSubjectDetailProp = objectMapper.createObjectNode();
    includeSubjectDetailProp.put("type", "boolean");
    includeSubjectDetailProp.put("description",
        "If true, return extended subject attributes");
    includeSubjectDetailProp.put("default", false);
    properties.set("includeSubjectDetail", includeSubjectDetailProp);

    ObjectNode subjectAttributeNamesProp = objectMapper.createObjectNode();
    subjectAttributeNamesProp.put("type", "array");
    ObjectNode itemsNode = objectMapper.createObjectNode();
    itemsNode.put("type", "string");
    subjectAttributeNamesProp.set("items", itemsNode);
    subjectAttributeNamesProp.put("description",
        "Specific attribute names to return (if includeSubjectDetail is true "
        + "and this is empty, all configured attributes are returned)");
    properties.set("subjectAttributeNames", subjectAttributeNamesProp);

    inputSchema.set("properties", properties);

    // no required fields - one of subjectId, subjectIdentifier, or searchString must be provided
    // but that is validated in execute()

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the entity_get tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String subjectId = arguments != null && arguments.has("subjectId")
        ? arguments.get("subjectId").asText() : null;
    String subjectIdentifier = arguments != null && arguments.has("subjectIdentifier")
        ? arguments.get("subjectIdentifier").asText() : null;
    String searchString = arguments != null && arguments.has("searchString")
        ? arguments.get("searchString").asText() : null;
    boolean includeSubjectDetail = arguments != null && arguments.has("includeSubjectDetail")
        && arguments.get("includeSubjectDetail").asBoolean(false);

    String[] subjectAttributeNames = null;
    if (arguments != null && arguments.has("subjectAttributeNames")
        && arguments.get("subjectAttributeNames").isArray()) {
      JsonNode attrArray = arguments.get("subjectAttributeNames");
      subjectAttributeNames = new String[attrArray.size()];
      for (int i = 0; i < attrArray.size(); i++) {
        subjectAttributeNames[i] = attrArray.get(i).asText();
      }
    }

    String[] sourceIds = null;
    if (arguments != null && arguments.has("sourceIds")
        && arguments.get("sourceIds").isArray()) {
      JsonNode sourceIdsArray = arguments.get("sourceIds");
      sourceIds = new String[sourceIdsArray.size()];
      for (int i = 0; i < sourceIdsArray.size(); i++) {
        sourceIds[i] = sourceIdsArray.get(i).asText();
      }
    }

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String memberFilterString = arguments != null && arguments.has("memberFilter")
        ? arguments.get("memberFilter").asText() : null;
    String fieldNameString = arguments != null && arguments.has("fieldName")
        ? arguments.get("fieldName").asText() : null;

    // validate that exactly one of subjectId, subjectIdentifier, or searchString is provided
    int lookupCount = (StringUtils.isNotBlank(subjectId) ? 1 : 0)
        + (StringUtils.isNotBlank(subjectIdentifier) ? 1 : 0)
        + (StringUtils.isNotBlank(searchString) ? 1 : 0);
    if (lookupCount == 0) {
      return buildErrorResult(
          "One of subjectId, subjectIdentifier, or searchString is required.");
    }
    if (lookupCount > 1) {
      return buildErrorResult(
          "Only one of subjectId, subjectIdentifier, or searchString may be provided.");
    }

    try {

      // build the subject lookup (for subjectId or subjectIdentifier)
      WsSubjectLookup[] wsSubjectLookups = null;
      if (StringUtils.isNotBlank(subjectId) || StringUtils.isNotBlank(subjectIdentifier)) {
        wsSubjectLookups = new WsSubjectLookup[] {
          new WsSubjectLookup(subjectId, null, subjectIdentifier)
        };
      }

      // build the group lookup if specified
      WsGroupLookup wsGroupLookup = null;
      if (StringUtils.isNotBlank(groupName)) {
        wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setGroupName(groupName);
      }

      // convert memberFilter string to WsMemberFilter
      WsMemberFilter wsMemberFilter = null;
      if (StringUtils.isNotBlank(memberFilterString)) {
        wsMemberFilter = GrouperServiceUtils.convertMemberFilter(memberFilterString);
      }

      // convert fieldName string to Field
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsGetSubjectsResults wsResults = GrouperServiceLogic.getSubjects(
          GrouperVersion.currentVersion(),
          wsSubjectLookups,
          searchString,
          includeSubjectDetail,
          subjectAttributeNames,
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          sourceIds,
          wsGroupLookup,
          wsMemberFilter,
          field,
          false,  // includeGroupDetail
          null    // params
      );

      // check for errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      WsSubject[] wsSubjects = wsResults.getWsSubjects();
      if (GrouperUtil.length(wsSubjects) == 0) {
        String lookupDescription = StringUtils.isNotBlank(subjectId) ? subjectId
            : StringUtils.isNotBlank(subjectIdentifier) ? subjectIdentifier : searchString;
        return buildErrorResult("No subjects found for: " + lookupDescription);
      }

      // convert WS results to clean MCP-friendly JSON (strip metadata)
      String[] attributeNames = wsResults.getSubjectAttributeNames();

      if (GrouperUtil.length(wsSubjects) == 1) {
        // single subject - return as object
        ObjectNode subjectNode = convertWsSubjectToJson(wsSubjects[0], attributeNames);
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(subjectNode);
        return buildSuccessResult(resultText);
      } else {
        // multiple subjects - return as array
        ArrayNode subjectsArray = objectMapper.createArrayNode();
        for (WsSubject wsSubject : wsSubjects) {
          subjectsArray.add(convertWsSubjectToJson(wsSubject, attributeNames));
        }
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(subjectsArray);
        return buildSuccessResult(resultText);
      }

    } catch (Exception e) {
      String lookupDescription = StringUtils.isNotBlank(subjectId) ? subjectId
          : StringUtils.isNotBlank(subjectIdentifier) ? subjectIdentifier : searchString;
      LOG.error("Error looking up subject: " + lookupDescription, e);
      return buildErrorResult("Error looking up subject: " + e.getMessage());
    }
  }

  /**
   * convert a WsSubject to a clean JSON object for MCP consumption.
   * strips out WS metadata (resultCode, success, identifierLookup, memberId)
   * @param wsSubject the WS subject
   * @param attributeNames the attribute names corresponding to the attributeValues array
   * @return clean JSON object
   */
  private static ObjectNode convertWsSubjectToJson(WsSubject wsSubject, String[] attributeNames) {
    ObjectNode subjectNode = objectMapper.createObjectNode();
    subjectNode.put("subjectId", wsSubject.getId());
    subjectNode.put("name", wsSubject.getName());
    subjectNode.put("sourceId", wsSubject.getSourceId());

    // include attributes as a map if present
    String[] attributeValues = wsSubject.getAttributeValues();
    if (GrouperUtil.length(attributeNames) > 0 && GrouperUtil.length(attributeValues) > 0) {
      ObjectNode attributesNode = objectMapper.createObjectNode();
      for (int i = 0; i < attributeNames.length && i < attributeValues.length; i++) {
        if (StringUtils.isNotBlank(attributeValues[i])) {
          attributesNode.put(attributeNames[i], attributeValues[i]);
        }
      }
      if (attributesNode.size() > 0) {
        subjectNode.set("attributes", attributesNode);
      }
    }

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

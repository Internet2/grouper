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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;

/**
 * MCP tool handler for assigning, adding, removing, or replacing attributes in Grouper.
 * Supports attribute operations on groups, stems, members, and other owner types.
 * Delegates to the WS assignAttributes service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpAssignAttributes {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAssignAttributes.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for attribute_assignment_save
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "attribute_assignment_save");
    tool.put("description",
        "Assign, add, remove, or replace attributes on Grouper objects. "
        + "Supports attribute operations on groups, stems, members, "
        + "and other owner types. Can include attribute values.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode attributeAssignTypeProp = objectMapper.createObjectNode();
    attributeAssignTypeProp.put("type", "string");
    ArrayNode assignTypeEnum = objectMapper.createArrayNode();
    assignTypeEnum.add("group");
    assignTypeEnum.add("stem");
    assignTypeEnum.add("member");
    assignTypeEnum.add("imm_mem");
    assignTypeEnum.add("any_mem");
    assignTypeEnum.add("attr_def");
    attributeAssignTypeProp.set("enum", assignTypeEnum);
    attributeAssignTypeProp.put("description",
        "The type of object to assign the attribute on.");
    properties.set("attributeAssignType", attributeAssignTypeProp);

    ObjectNode attributeAssignOperationProp = objectMapper.createObjectNode();
    attributeAssignOperationProp.put("type", "string");
    ArrayNode assignOpEnum = objectMapper.createArrayNode();
    assignOpEnum.add("assign_attr");
    assignOpEnum.add("add_attr");
    assignOpEnum.add("remove_attr");
    assignOpEnum.add("replace_attrs");
    attributeAssignOperationProp.set("enum", assignOpEnum);
    attributeAssignOperationProp.put("description",
        "The operation to perform: assign_attr (assign if not already assigned), "
        + "add_attr (add even if already assigned), "
        + "remove_attr (remove the assignment), "
        + "replace_attrs (replace all existing assignments with this one).");
    properties.set("attributeAssignOperation", attributeAssignOperationProp);

    ObjectNode attributeDefNameNameProp = objectMapper.createObjectNode();
    attributeDefNameNameProp.put("type", "string");
    attributeDefNameNameProp.put("description",
        "The attribute def name to assign "
        + "(e.g., 'etc:attribute:attrDefName').");
    properties.set("attributeDefNameName", attributeDefNameNameProp);

    ObjectNode ownerGroupNameProp = objectMapper.createObjectNode();
    ownerGroupNameProp.put("type", "string");
    ownerGroupNameProp.put("description",
        "The fully qualified group name to assign the attribute on "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("ownerGroupName", ownerGroupNameProp);

    ObjectNode ownerStemNameProp = objectMapper.createObjectNode();
    ownerStemNameProp.put("type", "string");
    ownerStemNameProp.put("description",
        "The fully qualified stem name to assign the attribute on "
        + "(e.g., 'stem1:stem2').");
    properties.set("ownerStemName", ownerStemNameProp);

    ObjectNode ownerSubjectIdProp = objectMapper.createObjectNode();
    ownerSubjectIdProp.put("type", "string");
    ownerSubjectIdProp.put("description",
        "The subject ID to assign the attribute on.");
    properties.set("ownerSubjectId", ownerSubjectIdProp);

    ObjectNode ownerSubjectSourceIdProp = objectMapper.createObjectNode();
    ownerSubjectSourceIdProp.put("type", "string");
    ownerSubjectSourceIdProp.put("description",
        "Optional source ID for the owner subject.");
    properties.set("ownerSubjectSourceId", ownerSubjectSourceIdProp);

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    actionProp.put("description",
        "The action name for the attribute assignment. "
        + "Defaults to 'assign' if not specified.");
    properties.set("action", actionProp);

    ObjectNode valuesProp = objectMapper.createObjectNode();
    valuesProp.put("type", "array");
    ObjectNode valuesItemsNode = objectMapper.createObjectNode();
    valuesItemsNode.put("type", "string");
    valuesProp.set("items", valuesItemsNode);
    valuesProp.put("description",
        "Array of string values to assign with the attribute.");
    properties.set("values", valuesProp);

    ObjectNode assignmentNotesProp = objectMapper.createObjectNode();
    assignmentNotesProp.put("type", "string");
    assignmentNotesProp.put("description",
        "Optional notes for the attribute assignment.");
    properties.set("assignmentNotes", assignmentNotesProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("attributeAssignType");
    required.add("attributeAssignOperation");
    required.add("attributeDefNameName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the attribute_assignment_save tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String attributeAssignTypeString = arguments != null && arguments.has("attributeAssignType")
        ? arguments.get("attributeAssignType").asText() : null;
    String attributeAssignOperationString = arguments != null && arguments.has("attributeAssignOperation")
        ? arguments.get("attributeAssignOperation").asText() : null;
    String attributeDefNameName = arguments != null && arguments.has("attributeDefNameName")
        ? arguments.get("attributeDefNameName").asText() : null;
    String ownerGroupName = arguments != null && arguments.has("ownerGroupName")
        ? arguments.get("ownerGroupName").asText() : null;
    String ownerStemName = arguments != null && arguments.has("ownerStemName")
        ? arguments.get("ownerStemName").asText() : null;
    String ownerSubjectId = arguments != null && arguments.has("ownerSubjectId")
        ? arguments.get("ownerSubjectId").asText() : null;
    String ownerSubjectSourceId = arguments != null && arguments.has("ownerSubjectSourceId")
        ? arguments.get("ownerSubjectSourceId").asText() : null;
    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;
    JsonNode valuesArray = arguments != null && arguments.has("values")
        ? arguments.get("values") : null;
    String assignmentNotes = arguments != null && arguments.has("assignmentNotes")
        ? arguments.get("assignmentNotes").asText() : null;

    if (StringUtils.isBlank(attributeAssignTypeString)) {
      return buildErrorResult("attributeAssignType is required.");
    }
    if (StringUtils.isBlank(attributeAssignOperationString)) {
      return buildErrorResult("attributeAssignOperation is required.");
    }
    if (StringUtils.isBlank(attributeDefNameName)) {
      return buildErrorResult("attributeDefNameName is required.");
    }

    // block modifications to protected system groups and stems
    if (StringUtils.isNotBlank(ownerGroupName)
        && GrouperMcpProtectedResources.isProtectedGroupName(ownerGroupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(ownerGroupName));
    }
    if (StringUtils.isNotBlank(ownerStemName)
        && GrouperMcpProtectedResources.isProtectedStemName(ownerStemName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedStemError(ownerStemName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (StringUtils.isNotBlank(ownerGroupName)
          && !authUser.isGroupInReadwriteScope(ownerGroupName)) {
        return buildErrorResult("Access denied: group '" + ownerGroupName
            + "' is outside your consented read-write scope.");
      }
      if (StringUtils.isNotBlank(ownerStemName)
          && !authUser.isStemInReadwriteScope(ownerStemName)) {
        return buildErrorResult("Access denied: folder '" + ownerStemName
            + "' is outside your consented read-write scope.");
      }
      if (StringUtils.isNotBlank(ownerSubjectId)
          && !authUser.isSubjectInReadwriteScope(ownerSubjectId)) {
        return buildErrorResult("Access denied: subject '" + ownerSubjectId
            + "' is outside your consented read-write scope.");
      }
    }

    try {

      AttributeAssignType attrAssignType = AttributeAssignType.valueOfIgnoreCase(
          attributeAssignTypeString, false);
      AttributeAssignOperation attrAssignOp = AttributeAssignOperation.valueOfIgnoreCase(
          attributeAssignOperationString, false);

      WsAttributeDefNameLookup[] wsAttributeDefNameLookups = new WsAttributeDefNameLookup[] {
          new WsAttributeDefNameLookup(attributeDefNameName, null)
      };

      WsGroupLookup[] wsOwnerGroupLookups = null;
      if (StringUtils.isNotBlank(ownerGroupName)) {
        WsGroupLookup gl = new WsGroupLookup();
        gl.setGroupName(ownerGroupName);
        wsOwnerGroupLookups = new WsGroupLookup[] { gl };
      }

      WsStemLookup[] wsOwnerStemLookups = null;
      if (StringUtils.isNotBlank(ownerStemName)) {
        wsOwnerStemLookups = new WsStemLookup[] { new WsStemLookup(ownerStemName, null) };
      }

      WsSubjectLookup[] wsOwnerSubjectLookups = null;
      if (StringUtils.isNotBlank(ownerSubjectId)) {
        wsOwnerSubjectLookups = new WsSubjectLookup[] {
            new WsSubjectLookup(ownerSubjectId, ownerSubjectSourceId, null)
        };
      }

      WsAttributeAssignValue[] wsValues = null;
      if (valuesArray != null && valuesArray.isArray() && valuesArray.size() > 0) {
        wsValues = new WsAttributeAssignValue[valuesArray.size()];
        for (int i = 0; i < valuesArray.size(); i++) {
          wsValues[i] = new WsAttributeAssignValue();
          wsValues[i].setValueSystem(valuesArray.get(i).asText());
        }
      }

      String[] actions = null;
      if (StringUtils.isNotBlank(action)) {
        actions = new String[] { action };
      }

      WsAssignAttributesResults wsResults = GrouperServiceLogic.assignAttributes(
          GrouperVersion.currentVersion(),
          attrAssignType,
          wsAttributeDefNameLookups,
          attrAssignOp,
          wsValues,
          assignmentNotes,
          null, null,  // enabledTime, disabledTime
          null,   // delegatable
          null,   // attributeAssignValueOperation
          null,   // wsAttributeAssignLookups
          wsOwnerGroupLookups,
          wsOwnerStemLookups,
          wsOwnerSubjectLookups,
          null,   // wsOwnerMembershipLookups
          null,   // wsOwnerMembershipAnyLookups
          null,   // wsOwnerAttributeDefLookups
          null,   // wsOwnerAttributeAssignLookups
          actions,
          null,   // actAsSubjectLookup
          false, null,  // includeSubjectDetail
          false,  // includeGroupDetail
          null,   // params
          null, null, null  // replace*
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAssignAttributeResult[] assignResults = wsResults.getWsAttributeAssignResults();
      if (GrouperUtil.length(assignResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (WsAssignAttributeResult assignResult : assignResults) {
        resultsArray.add(convertAssignAttributeResultToJson(assignResult));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error assigning attributes", e);
      return buildErrorResult("Error assigning attributes: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * convert a WsAssignAttributeResult to a clean JSON object for MCP consumption.
   * @param assignResult the WS assign attribute result
   * @return clean JSON object
   */
  private static ObjectNode convertAssignAttributeResultToJson(WsAssignAttributeResult assignResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    resultNode.put("changed", "T".equals(assignResult.getChanged()));

    WsAttributeAssign[] wsAttributeAssigns = assignResult.getWsAttributeAssigns();
    if (GrouperUtil.length(wsAttributeAssigns) > 0) {
      ArrayNode assignsArray = objectMapper.createArrayNode();
      for (WsAttributeAssign wsAttrAssign : wsAttributeAssigns) {
        ObjectNode assignNode = objectMapper.createObjectNode();
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignType())) {
          assignNode.put("attributeAssignType", wsAttrAssign.getAttributeAssignType());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeDefNameName())) {
          assignNode.put("attributeDefNameName", wsAttrAssign.getAttributeDefNameName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getOwnerGroupName())) {
          assignNode.put("ownerGroupName", wsAttrAssign.getOwnerGroupName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getOwnerStemName())) {
          assignNode.put("ownerStemName", wsAttrAssign.getOwnerStemName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignActionName())) {
          assignNode.put("action", wsAttrAssign.getAttributeAssignActionName());
        }
        assignsArray.add(assignNode);
      }
      resultNode.set("attributeAssigns", assignsArray);
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

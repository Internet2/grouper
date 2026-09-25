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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;

/**
 * MCP tool handler for getting attribute assignments in Grouper.
 * Supports getting assignments on groups, stems, members, and other owner types.
 * Delegates to the WS getAttributeAssignments service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetAttributeAssignmentsLite {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetAttributeAssignmentsLite.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for attribute_assignment_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "attribute_assignment_get");
    tool.put("description",
        "Get attribute assignments in Grouper. "
        + "Retrieve attribute assignments on groups, stems, members, or other "
        + "owner types. Can filter by attribute def name, owner, and action.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

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
        "The type of attribute assignment to retrieve.");
    properties.set("attributeAssignType", attributeAssignTypeProp);

    ObjectNode attributeDefNameNameProp = objectMapper.createObjectNode();
    attributeDefNameNameProp.put("type", "string");
    attributeDefNameNameProp.put("description",
        "The attribute def name to filter assignments by "
        + "(e.g., 'etc:attribute:attrDefName').");
    properties.set("attributeDefNameName", attributeDefNameNameProp);

    ObjectNode ownerGroupNameProp = objectMapper.createObjectNode();
    ownerGroupNameProp.put("type", "string");
    ownerGroupNameProp.put("description",
        "The fully qualified group name that owns the attribute assignment "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("ownerGroupName", ownerGroupNameProp);

    ObjectNode ownerStemNameProp = objectMapper.createObjectNode();
    ownerStemNameProp.put("type", "string");
    ownerStemNameProp.put("description",
        "The fully qualified stem name that owns the attribute assignment "
        + "(e.g., 'stem1:stem2').");
    properties.set("ownerStemName", ownerStemNameProp);

    ObjectNode ownerSubjectIdProp = objectMapper.createObjectNode();
    ownerSubjectIdProp.put("type", "string");
    ownerSubjectIdProp.put("description",
        "The subject ID that owns the attribute assignment.");
    properties.set("ownerSubjectId", ownerSubjectIdProp);

    ObjectNode ownerSubjectSourceIdProp = objectMapper.createObjectNode();
    ownerSubjectSourceIdProp.put("type", "string");
    ownerSubjectSourceIdProp.put("description",
        "Optional source ID for the owner subject.");
    properties.set("ownerSubjectSourceId", ownerSubjectSourceIdProp);

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    actionProp.put("description",
        "The action name to filter by (e.g., 'assign').");
    properties.set("action", actionProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("attributeAssignType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the attribute_assignment_get tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String attributeAssignTypeString = arguments != null && arguments.has("attributeAssignType")
        ? arguments.get("attributeAssignType").asText() : null;
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

    if (StringUtils.isBlank(attributeAssignTypeString)) {
      return buildErrorResult("attributeAssignType is required.");
    }

    try {

      AttributeAssignType attrAssignType = AttributeAssignType.valueOfIgnoreCase(
          attributeAssignTypeString, false);

      WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
      if (StringUtils.isNotBlank(attributeDefNameName)) {
        wsAttributeDefNameLookups = new WsAttributeDefNameLookup[] {
            new WsAttributeDefNameLookup(attributeDefNameName, null)
        };
      }

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

      String[] actions = null;
      if (StringUtils.isNotBlank(action)) {
        actions = new String[] { action };
      }

      WsGetAttributeAssignmentsResults wsResults = GrouperServiceLogic.getAttributeAssignments(
          GrouperVersion.currentVersion(),
          attrAssignType,
          null,   // wsAttributeAssignLookups
          null,   // wsAttributeDefLookups
          wsAttributeDefNameLookups,
          wsOwnerGroupLookups,
          wsOwnerStemLookups,
          wsOwnerSubjectLookups,
          null,   // wsOwnerMembershipLookups
          null,   // wsOwnerMembershipAnyLookups
          null,   // wsOwnerAttributeDefLookups
          actions,
          false,  // includeAssignmentsOnAssignments
          null,   // actAsSubjectLookup
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          false,  // includeGroupDetail
          null,   // params
          null,   // enabled
          null,   // attributeDefValueType
          null,   // theValue
          false,  // includeAssignmentsFromAssignments
          null,   // attributeDefType
          null, null, null, null  // assignAssignOwner*
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAttributeAssign[] attributeAssigns = wsResults.getWsAttributeAssigns();
      if (GrouperUtil.length(attributeAssigns) == 0) {
        return buildSuccessResult("No attribute assignments found.");
      }

      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (WsAttributeAssign wsAttrAssign : attributeAssigns) {
        resultsArray.add(convertAttributeAssignToJson(wsAttrAssign));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting attribute assignments", e);
      return buildErrorResult("Error getting attribute assignments: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * convert a WsAttributeAssign to a clean JSON object for MCP consumption.
   * @param wsAttrAssign the WS attribute assign
   * @return clean JSON object
   */
  private static ObjectNode convertAttributeAssignToJson(WsAttributeAssign wsAttrAssign) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    if (StringUtils.isNotBlank(wsAttrAssign.getId())) {
      resultNode.put("attributeAssignId", wsAttrAssign.getId());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignType())) {
      resultNode.put("attributeAssignType", wsAttrAssign.getAttributeAssignType());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getAttributeDefNameName())) {
      resultNode.put("attributeDefNameName", wsAttrAssign.getAttributeDefNameName());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getOwnerGroupName())) {
      resultNode.put("ownerGroupName", wsAttrAssign.getOwnerGroupName());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getOwnerStemName())) {
      resultNode.put("ownerStemName", wsAttrAssign.getOwnerStemName());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getOwnerMemberId())) {
      resultNode.put("ownerSubjectId", wsAttrAssign.getOwnerMemberId());
    }
    if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignActionName())) {
      resultNode.put("action", wsAttrAssign.getAttributeAssignActionName());
    }

    // include values if present
    WsAttributeAssignValue[] wsValues = wsAttrAssign.getWsAttributeAssignValues();
    if (GrouperUtil.length(wsValues) > 0) {
      ArrayNode valuesArray = objectMapper.createArrayNode();
      for (WsAttributeAssignValue wsValue : wsValues) {
        if (StringUtils.isNotBlank(wsValue.getValueSystem())) {
          valuesArray.add(wsValue.getValueSystem());
        }
      }
      if (valuesArray.size() > 0) {
        resultNode.set("values", valuesArray);
      }
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

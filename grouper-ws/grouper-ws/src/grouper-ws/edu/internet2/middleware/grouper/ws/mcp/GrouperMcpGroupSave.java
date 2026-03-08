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
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupToSave;

/**
 * MCP tool handler for creating or updating a Grouper group.
 * Supports setting group name, description, display extension, type, and save mode.
 * Delegates to the WS groupSave service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGroupSave {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGroupSave.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_save
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_save");
    tool.put("description",
        "Create or update a Grouper group. "
        + "Specify the fully qualified group name and optionally a description, "
        + "display extension, save mode, and type of group. "
        + "Supports composite groups: set compositeType and left/right factor group names "
        + "to make this group a composite, or set hasComposite to false to remove an existing composite.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to create or update "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

    ObjectNode descriptionProp = objectMapper.createObjectNode();
    descriptionProp.put("type", "string");
    descriptionProp.put("description",
        "Description of the group.");
    properties.set("description", descriptionProp);

    ObjectNode displayExtensionProp = objectMapper.createObjectNode();
    displayExtensionProp.put("type", "string");
    displayExtensionProp.put("description",
        "Display extension (friendly name) of the group. "
        + "If not provided, defaults to the extension portion of the group name.");
    properties.set("displayExtension", displayExtensionProp);

    ObjectNode saveModeProp = objectMapper.createObjectNode();
    saveModeProp.put("type", "string");
    ArrayNode saveModeEnum = objectMapper.createArrayNode();
    saveModeEnum.add("INSERT");
    saveModeEnum.add("UPDATE");
    saveModeEnum.add("INSERT_OR_UPDATE");
    saveModeProp.set("enum", saveModeEnum);
    saveModeProp.put("description",
        "Save mode. INSERT = create only (fail if exists), "
        + "UPDATE = update only (fail if not exists), "
        + "INSERT_OR_UPDATE = create or update (default).");
    properties.set("saveMode", saveModeProp);

    ObjectNode typeOfGroupProp = objectMapper.createObjectNode();
    typeOfGroupProp.put("type", "string");
    typeOfGroupProp.put("description",
        "Type of group to create, e.g., 'group', 'role', 'entity'. "
        + "Defaults to 'group'.");
    properties.set("typeOfGroup", typeOfGroupProp);

    ObjectNode compositeTypeProp = objectMapper.createObjectNode();
    compositeTypeProp.put("type", "string");
    ArrayNode compositeTypeEnum = objectMapper.createArrayNode();
    compositeTypeEnum.add("COMPLEMENT");
    compositeTypeEnum.add("INTERSECTION");
    compositeTypeProp.set("enum", compositeTypeEnum);
    compositeTypeProp.put("description",
        "Composite type. COMPLEMENT = members in left but not right, "
        + "INTERSECTION = members in both left and right. "
        + "Required when hasComposite is true.");
    properties.set("compositeType", compositeTypeProp);

    ObjectNode leftGroupNameProp = objectMapper.createObjectNode();
    leftGroupNameProp.put("type", "string");
    leftGroupNameProp.put("description",
        "Fully qualified name of the left factor group for a composite. "
        + "Required when hasComposite is true.");
    properties.set("leftGroupName", leftGroupNameProp);

    ObjectNode rightGroupNameProp = objectMapper.createObjectNode();
    rightGroupNameProp.put("type", "string");
    rightGroupNameProp.put("description",
        "Fully qualified name of the right factor group for a composite. "
        + "Required when hasComposite is true.");
    properties.set("rightGroupName", rightGroupNameProp);

    ObjectNode hasCompositeProp = objectMapper.createObjectNode();
    hasCompositeProp.put("type", "boolean");
    hasCompositeProp.put("description",
        "Set to true to make this group a composite (compositeType, leftGroupName, "
        + "and rightGroupName are required). Set to false to remove an existing "
        + "composite from this group. Omit if not changing composite status.");
    properties.set("hasComposite", hasCompositeProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_save tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String description = arguments != null && arguments.has("description")
        ? arguments.get("description").asText() : null;
    String displayExtension = arguments != null && arguments.has("displayExtension")
        ? arguments.get("displayExtension").asText() : null;
    String saveMode = arguments != null && arguments.has("saveMode")
        ? arguments.get("saveMode").asText() : null;
    String typeOfGroup = arguments != null && arguments.has("typeOfGroup")
        ? arguments.get("typeOfGroup").asText() : null;
    String compositeType = arguments != null && arguments.has("compositeType")
        ? arguments.get("compositeType").asText() : null;
    String leftGroupName = arguments != null && arguments.has("leftGroupName")
        ? arguments.get("leftGroupName").asText() : null;
    String rightGroupName = arguments != null && arguments.has("rightGroupName")
        ? arguments.get("rightGroupName").asText() : null;
    // hasComposite: null means not specified (don't change composite), true/false means set/remove
    Boolean hasComposite = arguments != null && arguments.has("hasComposite")
        ? arguments.get("hasComposite").asBoolean() : null;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    // block modifications to protected system groups and the etc stem
    if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
    }

    // validate composite parameters
    if (hasComposite != null && hasComposite) {
      if (StringUtils.isBlank(compositeType)) {
        return buildErrorResult("compositeType is required when hasComposite is true.");
      }
      if (StringUtils.isBlank(leftGroupName)) {
        return buildErrorResult("leftGroupName is required when hasComposite is true.");
      }
      if (StringUtils.isBlank(rightGroupName)) {
        return buildErrorResult("rightGroupName is required when hasComposite is true.");
      }
    }

    try {

      WsGroupToSave wsGroupToSave = new WsGroupToSave();

      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName(groupName);
      wsGroupToSave.setWsGroupLookup(wsGroupLookup);

      WsGroup wsGroup = new WsGroup();
      wsGroup.setName(groupName);
      if (StringUtils.isNotBlank(description)) {
        wsGroup.setDescription(description);
      }
      if (StringUtils.isNotBlank(displayExtension)) {
        wsGroup.setDisplayExtension(displayExtension);
      }
      if (StringUtils.isNotBlank(typeOfGroup)) {
        wsGroup.setTypeOfGroup(typeOfGroup);
      }
      wsGroupToSave.setWsGroup(wsGroup);

      if (StringUtils.isNotBlank(saveMode)) {
        wsGroupToSave.setSaveMode(saveMode);
      }
      // set composite detail if hasComposite was specified
      if (hasComposite != null) {
        WsGroupDetail wsGroupDetail = new WsGroupDetail();
        if (hasComposite) {
          wsGroupDetail.setHasComposite("T");
          wsGroupDetail.setCompositeType(compositeType);
          WsGroup leftGroup = new WsGroup();
          leftGroup.setName(leftGroupName);
          wsGroupDetail.setLeftGroup(leftGroup);
          WsGroup rightGroup = new WsGroup();
          rightGroup.setName(rightGroupName);
          wsGroupDetail.setRightGroup(rightGroup);
        } else {
          wsGroupDetail.setHasComposite("F");
        }
        wsGroup.setDetail(wsGroupDetail);
      }

      boolean includeGroupDetail = hasComposite != null;

      // actAs is null: the logged-in user (set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsGroupSaveResults wsResults = GrouperServiceLogic.groupSave(
          GrouperVersion.currentVersion(),
          new WsGroupToSave[] { wsGroupToSave },
          null,   // actAsSubjectLookup - uses REMOTE_USER
          null,   // txType
          includeGroupDetail,
          null    // params
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsGroupSaveResult[] saveResults = wsResults.getResults();
      if (GrouperUtil.length(saveResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      WsGroupSaveResult saveResult = saveResults[0];
      ObjectNode resultNode = objectMapper.createObjectNode();

      // include the result code (SUCCESS_INSERTED, SUCCESS_UPDATED, SUCCESS_NO_CHANGES_NEEDED, etc.)
      if (saveResult.getResultMetadata() != null) {
        resultNode.put("resultCode",
            saveResult.getResultMetadata().getResultCode());
        resultNode.put("success",
            "T".equals(saveResult.getResultMetadata().getSuccess()));
      }

      // include group info
      if (saveResult.getWsGroup() != null) {
        WsGroup resultGroup = saveResult.getWsGroup();
        resultNode.put("name", resultGroup.getName());
        if (StringUtils.isNotBlank(resultGroup.getDisplayExtension())) {
          resultNode.put("displayExtension", resultGroup.getDisplayExtension());
        }
        if (StringUtils.isNotBlank(resultGroup.getDescription())) {
          resultNode.put("description", resultGroup.getDescription());
        }
        resultNode.put("uuid", resultGroup.getUuid());

        // include composite info if detail is present
        if (resultGroup.getDetail() != null) {
          WsGroupDetail detail = resultGroup.getDetail();
          if ("T".equals(detail.getHasComposite())) {
            ObjectNode compositeNode = objectMapper.createObjectNode();
            compositeNode.put("compositeType", detail.getCompositeType());
            if (detail.getLeftGroup() != null) {
              compositeNode.put("leftGroupName", detail.getLeftGroup().getName());
            }
            if (detail.getRightGroup() != null) {
              compositeNode.put("rightGroupName", detail.getRightGroup().getName());
            }
            resultNode.set("composite", compositeNode);
          } else {
            resultNode.put("hasComposite", false);
          }
        }
      }

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error saving group: " + groupName, e);
      return buildErrorResult("Error saving group: " + e.getMessage());
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

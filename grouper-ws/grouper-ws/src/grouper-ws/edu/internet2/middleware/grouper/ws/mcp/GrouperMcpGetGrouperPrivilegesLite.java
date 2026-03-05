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
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGrouperPrivilegeResult;

/**
 * MCP tool handler for getting privileges on a Grouper group or stem.
 * Supports looking up privileges by subject, group/stem, and privilege type/name.
 * Delegates to the WS getGrouperPrivilegesLite service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetGrouperPrivilegesLite {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetGrouperPrivilegesLite.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for privilege_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "privilege_get");
    tool.put("description",
        "Get privileges on a Grouper group or stem. "
        + "Can filter by subject, privilege type, and privilege name. "
        + "Returns the list of matching privileges with subject and privilege details.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "The subject ID to check privileges for. "
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

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to check privileges on "
        + "(e.g., 'stem1:stem2:groupName'). "
        + "Mutually exclusive with stemName.");
    properties.set("groupName", groupNameProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "The fully qualified stem name to check privileges on "
        + "(e.g., 'stem1:stem2'). "
        + "Mutually exclusive with groupName.");
    properties.set("stemName", stemNameProp);

    ObjectNode privilegeTypeProp = objectMapper.createObjectNode();
    privilegeTypeProp.put("type", "string");
    ArrayNode privilegeTypeEnum = objectMapper.createArrayNode();
    privilegeTypeEnum.add("access");
    privilegeTypeEnum.add("naming");
    privilegeTypeProp.set("enum", privilegeTypeEnum);
    privilegeTypeProp.put("description",
        "The privilege type: 'access' for group privileges, "
        + "'naming' for stem privileges.");
    properties.set("privilegeType", privilegeTypeProp);

    ObjectNode privilegeNameProp = objectMapper.createObjectNode();
    privilegeNameProp.put("type", "string");
    privilegeNameProp.put("description",
        "The specific privilege name to filter by. "
        + "For groups (access): read, view, update, admin, optin, optout, "
        + "groupAttrRead, groupAttrUpdate. "
        + "For stems (naming): stem, create, stemAdmin, stemAttrRead, stemAttrUpdate.");
    properties.set("privilegeName", privilegeNameProp);

    inputSchema.set("properties", properties);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the privilege_get tool by delegating to the WS service logic
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
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String privilegeTypeString = arguments != null && arguments.has("privilegeType")
        ? arguments.get("privilegeType").asText() : null;
    String privilegeNameString = arguments != null && arguments.has("privilegeName")
        ? arguments.get("privilegeName").asText() : null;

    if (StringUtils.isBlank(groupName) && StringUtils.isBlank(stemName)) {
      return buildErrorResult("At least one of groupName or stemName is required.");
    }

    try {

      PrivilegeType privilegeTypeEnum = null;
      if (StringUtils.isNotBlank(privilegeTypeString)) {
        privilegeTypeEnum = PrivilegeType.valueOfIgnoreCase(privilegeTypeString);
      }
      Privilege privilegeEnum = null;
      if (StringUtils.isNotBlank(privilegeNameString) && privilegeTypeEnum != null) {
        privilegeEnum = privilegeTypeEnum.retrievePrivilege(privilegeNameString);
      }

      WsGetGrouperPrivilegesLiteResult wsResult = GrouperServiceLogic.getGrouperPrivilegesLite(
          GrouperVersion.currentVersion(),
          subjectId, subjectSourceId, subjectIdentifier,
          groupName, null,  // groupUuid
          stemName, null,   // stemUuid
          privilegeTypeEnum, privilegeEnum,
          null, null, null,  // actAs
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          false,  // includeGroupDetail
          null, null, null, null  // params
      );

      // check for overall errors
      if (wsResult.getResultMetadata() != null
          && !"T".equals(wsResult.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResult.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsGrouperPrivilegeResult[] privilegeResults = wsResult.getPrivilegeResults();
      if (GrouperUtil.length(privilegeResults) == 0) {
        return buildSuccessResult("No privileges found.");
      }

      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (WsGrouperPrivilegeResult privilegeResult : privilegeResults) {
        resultsArray.add(convertPrivilegeResultToJson(privilegeResult));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting privileges", e);
      return buildErrorResult("Error getting privileges: " + e.getMessage());
    }
  }

  /**
   * convert a WsGrouperPrivilegeResult to a clean JSON object for MCP consumption.
   * @param privilegeResult the WS privilege result
   * @return clean JSON object
   */
  private static ObjectNode convertPrivilegeResultToJson(WsGrouperPrivilegeResult privilegeResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    resultNode.put("privilegeName", privilegeResult.getPrivilegeName());
    resultNode.put("privilegeType", privilegeResult.getPrivilegeType());
    resultNode.put("allowed", privilegeResult.getAllowed());

    if (privilegeResult.getWsSubject() != null) {
      resultNode.put("subjectId", privilegeResult.getWsSubject().getId());
      if (StringUtils.isNotBlank(privilegeResult.getWsSubject().getName())) {
        resultNode.put("subjectName", privilegeResult.getWsSubject().getName());
      }
    }

    if (privilegeResult.getWsGroup() != null
        && StringUtils.isNotBlank(privilegeResult.getWsGroup().getName())) {
      resultNode.put("groupName", privilegeResult.getWsGroup().getName());
    }

    if (privilegeResult.getWsStem() != null
        && StringUtils.isNotBlank(privilegeResult.getWsStem().getName())) {
      resultNode.put("stemName", privilegeResult.getWsStem().getName());
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

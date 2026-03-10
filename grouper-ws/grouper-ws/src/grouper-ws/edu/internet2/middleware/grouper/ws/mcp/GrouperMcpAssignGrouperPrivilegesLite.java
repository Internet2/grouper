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
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult;

/**
 * MCP tool handler for assigning or removing privileges on a Grouper group or stem.
 * Supports granting or revoking a privilege for a subject on a group or stem.
 * Delegates to the WS assignGrouperPrivilegesLite service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpAssignGrouperPrivilegesLite {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAssignGrouperPrivilegesLite.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for privilege_assign
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "privilege_assign");
    tool.put("description",
        "Assign or remove a privilege on a Grouper group or stem for a subject. "
        + "Specify the subject, the group or stem, the privilege type and name, "
        + "and whether to grant (allowed=true) or revoke (allowed=false) the privilege.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "The subject ID to assign the privilege to. "
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
        "The fully qualified group name to assign the privilege on "
        + "(e.g., 'stem1:stem2:groupName'). "
        + "Mutually exclusive with stemName.");
    properties.set("groupName", groupNameProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "The fully qualified stem name to assign the privilege on "
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
        "The specific privilege name to assign or remove. "
        + "For groups (access): read, view, update, admin, optin, optout, "
        + "groupAttrRead, groupAttrUpdate. "
        + "For stems (naming): stem, create, stemAdmin, stemAttrRead, stemAttrUpdate.");
    properties.set("privilegeName", privilegeNameProp);

    ObjectNode allowedProp = objectMapper.createObjectNode();
    allowedProp.put("type", "boolean");
    allowedProp.put("description",
        "True to grant the privilege, false to revoke it.");
    properties.set("allowed", allowedProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("privilegeType");
    required.add("privilegeName");
    required.add("allowed");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the privilege_assign tool by delegating to the WS service logic
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
    boolean allowed = arguments != null && arguments.has("allowed")
        && arguments.get("allowed").asBoolean(false);

    if (StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectIdentifier)) {
      return buildErrorResult("Either subjectId or subjectIdentifier is required.");
    }
    if (StringUtils.isBlank(groupName) && StringUtils.isBlank(stemName)) {
      return buildErrorResult("Either groupName or stemName is required.");
    }
    if (StringUtils.isBlank(privilegeTypeString)) {
      return buildErrorResult("privilegeType is required.");
    }
    if (StringUtils.isBlank(privilegeNameString)) {
      return buildErrorResult("privilegeName is required.");
    }

    // block modifications to protected system groups and stems
    if (StringUtils.isNotBlank(groupName)
        && GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
    }
    if (StringUtils.isNotBlank(stemName)
        && GrouperMcpProtectedResources.isProtectedStemName(stemName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedStemError(stemName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (StringUtils.isNotBlank(groupName)
          && !authUser.isGroupInReadwriteScope(groupName)) {
        return buildErrorResult("Access denied: group '" + groupName
            + "' is outside your consented read-write scope.");
      }
      if (StringUtils.isNotBlank(stemName)
          && !authUser.isStemInReadwriteScope(stemName)) {
        return buildErrorResult("Access denied: folder '" + stemName
            + "' is outside your consented read-write scope.");
      }
      String subjectValue = StringUtils.isNotBlank(subjectId) ? subjectId : subjectIdentifier;
      if (subjectValue != null && !authUser.isSubjectInReadwriteScope(subjectValue)) {
        return buildErrorResult("Access denied: subject '" + subjectValue
            + "' is outside your consented read-write scope.");
      }
    }

    try {

      PrivilegeType privilegeTypeEnum = PrivilegeType.valueOfIgnoreCase(privilegeTypeString);
      Privilege privilegeEnum = privilegeTypeEnum.retrievePrivilege(privilegeNameString);

      WsAssignGrouperPrivilegesLiteResult wsResult = GrouperServiceLogic.assignGrouperPrivilegesLite(
          GrouperVersion.currentVersion(),
          subjectId, subjectSourceId, subjectIdentifier,
          groupName, null,  // groupUuid
          stemName, null,   // stemUuid
          privilegeTypeEnum, privilegeEnum,
          allowed,
          null, null, null,  // actAs
          false, null,  // includeSubjectDetail
          false,  // includeGroupDetail
          null, null, null, null  // params
      );

      // check for overall errors
      if (wsResult.getResultMetadata() != null
          && !"T".equals(wsResult.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResult.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      ObjectNode resultNode = objectMapper.createObjectNode();
      if (wsResult.getResultMetadata() != null) {
        resultNode.put("resultCode", wsResult.getResultMetadata().getResultCode());
        resultNode.put("success", "T".equals(wsResult.getResultMetadata().getSuccess()));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error assigning privilege", e);
      return buildErrorResult("Error assigning privilege: " + e.getMessage());
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

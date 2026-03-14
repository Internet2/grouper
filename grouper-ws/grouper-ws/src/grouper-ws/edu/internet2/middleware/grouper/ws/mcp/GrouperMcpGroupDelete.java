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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for deleting a Grouper group.
 *
 * <p>Looks up the group by name and deletes it. The calling user must have
 * ADMIN privilege on the group. Protected system groups and groups under the
 * built-in objects stem (default <code>etc</code>) cannot be deleted via MCP.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpGroupDelete {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGroupDelete.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for group_delete.
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_delete");
    tool.put("description",
        "Delete a Grouper group. The group is looked up by name and permanently deleted. "
        + "The calling user must have ADMIN privilege on the group. "
        + "System groups and groups under the built-in objects stem cannot be deleted.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to delete (e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the group_delete tool.
   *
   * <p>Flow:
   * 1. Parse and validate the groupName (required)
   * 2. Check that the group is not a protected system group
   * 3. Check OAuth scope restrictions
   * 4. Look up the group by name
   * 5. Delete the group
   * 6. Return the result</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user
   * @return the MCP tool result containing the operation result or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    // block modifications to protected system groups and the etc stem
    if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (!authUser.hasGroupOrFolderReadwriteScope()) {
        return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
      }
      if (!authUser.isGroupInReadwriteScope(groupName)) {
        return buildErrorResult(
            authUser.buildReadwriteScopeDeniedError("group", groupName));
      }
    }

    try {
      // look up the group by name (false = don't throw exception if not found)
      Group group = GroupFinder.findByName(groupName, false);

      if (group == null) {
        return buildErrorResult("Group not found: " + groupName);
      }

      String uuid = group.getUuid();
      String displayExtension = group.getDisplayExtension();

      // delete the group (this checks ADMIN privilege internally)
      group.delete();

      // build the response
      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("resultCode", "DELETE");
      resultNode.put("success", true);
      resultNode.put("name", groupName);
      if (StringUtils.isNotBlank(displayExtension)) {
        resultNode.put("displayExtension", displayExtension);
      }
      resultNode.put("uuid", uuid);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error in group_delete for group: " + groupName, e);
      return buildErrorResult("Error in group_delete: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * Build a successful MCP tool result with the standard content array format.
   * @param text the result text (typically JSON) to return to the MCP client
   * @return ObjectNode with isError=false and a content array containing the text
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
   * Build an error MCP tool result with the standard content array format.
   * @param errorMessage the error message to return to the MCP client
   * @return ObjectNode with isError=true and a content array containing the error message
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

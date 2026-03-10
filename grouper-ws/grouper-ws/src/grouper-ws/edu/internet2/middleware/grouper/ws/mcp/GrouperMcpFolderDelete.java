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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for deleting a Grouper stem (folder).
 *
 * <p>Looks up the stem by name and deletes it. The calling user must have
 * STEM_ADMIN privilege on the stem. Protected system stems and stems under the
 * built-in objects stem (default <code>etc</code>) cannot be deleted via MCP.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpFolderDelete {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFolderDelete.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for folder_delete.
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "folder_delete");
    tool.put("description",
        "Delete a Grouper stem (folder). The stem is looked up by name and permanently deleted. "
        + "The calling user must have STEM_ADMIN privilege on the stem. "
        + "System stems and stems under the built-in objects stem cannot be deleted. "
        + "Note: you cannot delete a stem that has child groups or sub-stems");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "The fully qualified stem name to delete (e.g., 'stem1:stem2').");
    properties.set("stemName", stemNameProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("stemName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the folder_delete tool.
   *
   * <p>Flow:
   * 1. Parse and validate the stemName (required)
   * 2. Check that the stem is not a protected system stem
   * 3. Check OAuth scope restrictions
   * 4. Look up the stem by name
   * 5. Delete the stem
   * 6. Return the result</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user
   * @return the MCP tool result containing the operation result or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;

    if (StringUtils.isBlank(stemName)) {
      return buildErrorResult("stemName is required.");
    }

    // block modifications to protected system stems and the etc stem
    if (GrouperMcpProtectedResources.isProtectedStemName(stemName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedStemError(stemName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (!authUser.isStemInReadwriteScope(stemName)) {
        return buildErrorResult(
            authUser.buildReadwriteScopeDeniedError("stem", stemName));
      }
    }

    try {
      // look up the stem by name (false = don't throw exception if not found)
      Stem stem = StemFinder.findByName(stemName, false);

      if (stem == null) {
        return buildErrorResult("Stem not found: " + stemName);
      }

      String uuid = stem.getUuid();
      String displayExtension = stem.getDisplayExtension();

      // delete the stem (this checks STEM_ADMIN privilege internally)
      stem.delete();

      // build the response
      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("resultCode", "DELETE");
      resultNode.put("success", true);
      resultNode.put("name", stemName);
      if (StringUtils.isNotBlank(displayExtension)) {
        resultNode.put("displayExtension", displayExtension);
      }
      resultNode.put("uuid", uuid);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error in folder_delete for stem: " + stemName, e);
      return buildErrorResult("Error in folder_delete: " + e.getMessage()
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

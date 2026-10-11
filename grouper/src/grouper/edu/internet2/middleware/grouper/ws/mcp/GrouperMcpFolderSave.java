/*******************************************************************************
 * Copyright 2026 Internet2
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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for creating and updating a Grouper stem (folder) (GRP-6806).
 *
 * <p>Like group_save, this tool is action based and calls StemSave directly rather than the
 * WS stemSave operation, so an update only changes the fields that are passed in and never
 * blanks a description or display name that the caller left out.</p>
 *
 * <p>Supported actions:</p>
 * <ul>
 *   <li><b>createFolder</b> - create a new folder (fails if it already exists)</li>
 *   <li><b>createOrUpdateFolder</b> - create a new folder, or update the display name and
 *       description of an existing one without affecting other settings</li>
 *   <li><b>updateFolderPart</b> - update the display name and/or description of an existing
 *       folder without affecting other settings</li>
 * </ul>
 *
 * <p>Renaming and moving folders are not supported here.  Those touch every group and folder
 * underneath, which is better done in the UI where the caller can see what will change.</p>
 *
 * <p>Security: StemSave checks privileges as the calling user, so creating a folder needs the
 * CREATE (stem) privilege on the parent folder, and updating one needs STEM_ADMIN on the folder.
 * Folders protected from MCP (the built-in objects folder, default <code>etc</code>, and
 * <code>grouper.mcp.protectedFolders</code>) and folders outside an OAuth readwrite scope are
 * refused.  Parent folders are only created when the caller asks for it, and each parent that
 * would be created goes through the same protected and scope checks as the folder itself, so
 * a caller scoped to <code>a:b:c</code> cannot create <code>a:b</code> as a side effect.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpFolderSave {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFolderSave.class);

  /** json mapper */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for folder_save.
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "folder_save");
    tool.put("description",
        "Create or update a Grouper stem (folder). "
        + "Use the 'action' parameter to specify which operation to perform. "
        + "Actions: createFolder, createOrUpdateFolder, updateFolderPart. "
        + "Creating a folder needs the CREATE (stem) privilege on the parent folder; updating one "
        + "needs STEM_ADMIN on the folder. Only the fields passed in are changed. "
        + "Renaming or moving a folder is not supported. "
        + "System folders and folders under the built-in objects folder cannot be changed.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    // --- action (required) ---
    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("createFolder");
    actionEnum.add("createOrUpdateFolder");
    actionEnum.add("updateFolderPart");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The operation to perform. "
        + "createFolder = create a new folder (fails if it exists). "
        + "createOrUpdateFolder = create a new folder, or update the display name and description "
        + "of an existing one without replacing other settings. "
        + "updateFolderPart = update the display name and/or description of an existing folder "
        + "without replacing other settings.");
    properties.set("action", actionProp);

    // --- stemName (required) ---
    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "The fully qualified folder name (e.g., 'app:myApp:groups'). The last part is the folder ID.");
    properties.set("stemName", stemNameProp);

    // --- displayExtension (optional) ---
    ObjectNode displayExtensionProp = objectMapper.createObjectNode();
    displayExtensionProp.put("type", "string");
    displayExtensionProp.put("description",
        "The friendly name of the folder (the last part of its display name). "
        + "When creating, defaults to the folder ID.");
    properties.set("displayExtension", displayExtensionProp);

    // --- description (optional) ---
    ObjectNode descriptionProp = objectMapper.createObjectNode();
    descriptionProp.put("type", "string");
    descriptionProp.put("description", "The description of the folder.");
    properties.set("description", descriptionProp);

    // --- createParentFoldersIfNotExist (optional) ---
    ObjectNode createParentsProp = objectMapper.createObjectNode();
    createParentsProp.put("type", "boolean");
    createParentsProp.put("description",
        "When creating, also create any parent folders that do not exist yet. Default is false, "
        + "in which case the parent folder must already exist.");
    properties.set("createParentFoldersIfNotExist", createParentsProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    required.add("stemName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the folder_save tool.
   *
   * <p>Flow:
   * 1. Parse and validate the action and stemName (required)
   * 2. Check that the folder is not protected from MCP and is in the OAuth readwrite scope
   * 3. If parent folders will be created, run the same checks on each of them
   * 4. Dispatch to the action handler, which calls StemSave as the calling user
   * 5. Return the result</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user
   * @return the MCP tool result containing the operation result or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required.");
    }
    if (StringUtils.isBlank(stemName)) {
      return buildErrorResult("stemName is required.");
    }
    stemName = stemName.trim();

    // the root folder has no name and cannot be created or changed here
    if (StringUtils.equals(stemName, ":")) {
      return buildErrorResult("The root folder cannot be changed with folder_save.");
    }

    String protectedOrScopeError = protectedOrScopeError(stemName, authUser);
    if (protectedOrScopeError != null) {
      return buildErrorResult(protectedOrScopeError);
    }

    boolean createParentFolders = arguments.has("createParentFoldersIfNotExist")
        && arguments.get("createParentFoldersIfNotExist").asBoolean(false);

    try {
      switch (action) {
        case "createFolder":
        case "createOrUpdateFolder":

          // parent folders created as a side effect get the same checks as the folder itself
          if (createParentFolders) {
            for (String missingParentName : missingParentFolderNames(stemName)) {
              String parentError = protectedOrScopeError(missingParentName, authUser);
              if (parentError != null) {
                return buildErrorResult("Cannot create parent folder '" + missingParentName + "': " + parentError);
              }
            }
          }

          if (StringUtils.equals("createFolder", action)) {
            return executeCreateFolder(arguments, stemName, createParentFolders);
          }
          return executeCreateOrUpdateFolder(arguments, stemName, createParentFolders);

        case "updateFolderPart":
          return executeUpdateFolderPart(arguments, stemName);

        default:
          return buildErrorResult("Unknown action: " + action
              + ". Valid actions: createFolder, createOrUpdateFolder, updateFolderPart.");
      }
    } catch (Exception e) {
      LOG.error("Error in folder_save action '" + action + "' for stem: " + stemName, e);
      return buildErrorResult("Error in folder_save action '" + action + "': " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * see if a folder may not be written by this caller through MCP: protected from MCP, or
   * outside the OAuth readwrite scope
   * @param stemName fully qualified folder name
   * @param authUser the authenticated user
   * @return the error message, or null if it may be written
   */
  private static String protectedOrScopeError(String stemName, GrouperMcpAuthUser authUser) {

    // block modifications to protected system folders and the etc folder
    if (GrouperMcpProtectedResources.isProtectedStemName(stemName)) {
      return GrouperMcpProtectedResources.buildProtectedStemError(stemName);
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      if (!authUser.hasGroupOrFolderReadwriteScope()) {
        return "Access denied: your OAuth scope does not include groups or folders.";
      }
      if (!authUser.isStemInReadwriteScope(stemName)) {
        return authUser.buildReadwriteScopeDeniedError("stem", stemName);
      }
    }
    return null;
  }

  /**
   * the ancestors of a folder which do not exist yet, nearest first.  stops at the first one
   * that exists, since everything above it exists too
   * @param stemName fully qualified folder name
   * @return the names of the parent folders that would be created
   */
  static List<String> missingParentFolderNames(String stemName) {
    List<String> result = new ArrayList<String>();
    String parentName = GrouperUtil.parentStemNameFromName(stemName, false);
    while (StringUtils.isNotBlank(parentName) && !StringUtils.equals(parentName, ":")) {
      if (StemFinder.findByName(parentName, false) != null) {
        break;
      }
      result.add(parentName);
      parentName = GrouperUtil.parentStemNameFromName(parentName, false);
    }
    return result;
  }

  /**
   * Create a new folder with StemSave in INSERT mode, which fails if it already exists.
   *
   * @param arguments the MCP request arguments
   * @param stemName the fully qualified folder name to create
   * @param createParentFolders if missing parent folders should be created
   * @return the MCP tool result
   * @throws Exception if the save fails
   */
  private static ObjectNode executeCreateFolder(JsonNode arguments, String stemName,
      boolean createParentFolders) throws Exception {

    StemSave stemSave = new StemSave()
        .assignName(stemName)
        .assignSaveMode(SaveMode.INSERT)
        .assignCreateParentStemsIfNotExist(createParentFolders);
    assignDisplayExtensionAndDescription(stemSave, arguments);

    Stem stem = stemSave.save();
    return buildStemResult("createFolder", stemSave.getSaveResultType(), stem);
  }

  /**
   * Create a new folder, or update an existing one without replacing settings that were not
   * passed in.  StemSave cannot insert with replaceAllSettings=false, so the two cases are split
   * here: an insert sets what was passed in, an update only changes what was passed in.
   *
   * @param arguments the MCP request arguments
   * @param stemName the fully qualified folder name to create or update
   * @param createParentFolders if missing parent folders should be created
   * @return the MCP tool result
   * @throws Exception if the save fails
   */
  private static ObjectNode executeCreateOrUpdateFolder(JsonNode arguments, String stemName,
      boolean createParentFolders) throws Exception {

    if (StemFinder.findByName(stemName, false) == null) {
      StemSave stemSave = new StemSave()
          .assignName(stemName)
          .assignSaveMode(SaveMode.INSERT)
          .assignCreateParentStemsIfNotExist(createParentFolders);
      assignDisplayExtensionAndDescription(stemSave, arguments);

      Stem stem = stemSave.save();
      return buildStemResult("createOrUpdateFolder", stemSave.getSaveResultType(), stem);
    }

    StemSave stemSave = new StemSave()
        .assignStemNameToEdit(stemName)
        .assignName(stemName)
        .assignReplaceAllSettings(false)
        .assignSaveMode(SaveMode.UPDATE);
    assignDisplayExtensionAndDescription(stemSave, arguments);

    Stem stem = stemSave.save();
    return buildStemResult("createOrUpdateFolder", stemSave.getSaveResultType(), stem);
  }

  /**
   * Update the display name and/or description of an existing folder.  Uses StemSave with
   * replaceAllSettings=false and SaveMode.UPDATE so only the fields passed in are changed.
   *
   * @param arguments the MCP request arguments
   * @param stemName the fully qualified folder name to update
   * @return the MCP tool result
   * @throws Exception if the save fails
   */
  private static ObjectNode executeUpdateFolderPart(JsonNode arguments, String stemName) throws Exception {

    if (!arguments.hasNonNull("displayExtension") && !arguments.hasNonNull("description")) {
      return buildErrorResult("updateFolderPart needs displayExtension and/or description.");
    }

    // a clear message instead of a StemNotFoundException from inside StemSave
    if (StemFinder.findByName(stemName, false) == null) {
      return buildErrorResult("Stem not found: " + stemName);
    }

    StemSave stemSave = new StemSave()
        .assignStemNameToEdit(stemName)
        .assignName(stemName)
        .assignReplaceAllSettings(false)
        .assignSaveMode(SaveMode.UPDATE);
    assignDisplayExtensionAndDescription(stemSave, arguments);

    Stem stem = stemSave.save();
    return buildStemResult("updateFolderPart", stemSave.getSaveResultType(), stem);
  }

  /**
   * assign the optional displayExtension and description from the arguments.  a description
   * that is passed in as an empty string clears it; one that is not passed in is left alone
   * @param stemSave the save to assign to
   * @param arguments the MCP request arguments
   */
  private static void assignDisplayExtensionAndDescription(StemSave stemSave, JsonNode arguments) {
    if (arguments.hasNonNull("displayExtension")
        && StringUtils.isNotBlank(arguments.get("displayExtension").asText())) {
      stemSave.assignDisplayExtension(arguments.get("displayExtension").asText());
    }
    if (arguments.hasNonNull("description")) {
      stemSave.assignDescription(arguments.get("description").asText());
    }
  }

  /**
   * build the success result for a saved folder
   * @param action the action that was run
   * @param saveResultType INSERT, UPDATE, or NO_CHANGE
   * @param stem the saved folder
   * @return the MCP tool result
   * @throws Exception if the json cannot be written
   */
  private static ObjectNode buildStemResult(String action, SaveResultType saveResultType,
      Stem stem) throws Exception {
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", action);
    resultNode.put("resultCode", saveResultType.name());
    resultNode.put("success", true);
    resultNode.put("name", stem.getName());
    resultNode.put("displayName", stem.getDisplayName());
    if (StringUtils.isNotBlank(stem.getDisplayExtension())) {
      resultNode.put("displayExtension", stem.getDisplayExtension());
    }
    if (StringUtils.isNotBlank(stem.getDescription())) {
      resultNode.put("description", stem.getDescription());
    }
    resultNode.put("uuid", stem.getUuid());

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
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

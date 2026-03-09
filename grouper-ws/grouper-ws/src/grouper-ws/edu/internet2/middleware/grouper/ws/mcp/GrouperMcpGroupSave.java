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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.CompositeSave;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeGroupSave;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireConfigBean;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireEngine;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignToGroupSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionableGroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for creating, updating, and managing Grouper groups.
 *
 * <p>This tool uses an action-based approach rather than delegating to the WS groupSave
 * operation. This prevents accidental data loss that can occur when WS groupSave
 * replaces all settings by default. Each action has its own set of applicable parameters
 * and uses the appropriate Grouper API directly.</p>
 *
 * <p>Supported actions:</p>
 * <ul>
 *   <li><b>createGroup</b> - Create a new group (fails if it already exists)</li>
 *   <li><b>createOrUpdateGroup</b> - Create a new group or update an existing one without
 *       affecting other settings</li>
 *   <li><b>updateGroupPart</b> - Update specific fields on an existing group without
 *       affecting other settings (uses GroupSave with replaceAllSettings=false)</li>
 *   <li><b>addGroupType</b> - Assign an object type (policy, ref, basis, etc.) to a group</li>
 *   <li><b>removeGroupType</b> - Remove an object type from a group</li>
 *   <li><b>addComposite</b> - Make a group a composite of two other groups</li>
 *   <li><b>updateComposite</b> - Update the composite type or factor groups on an existing composite</li>
 *   <li><b>removeComposite</b> - Remove the composite definition from a group</li>
 *   <li><b>addEligibilityRequirement</b> - Assign an eligibility requirement to a group</li>
 *   <li><b>removeEligibilityRequirement</b> - Remove an eligibility requirement from a group</li>
 *   <li><b>addProvisioner</b> - Enable provisioning of a group to a provisioning target</li>
 *   <li><b>removeProvisioner</b> - Disable provisioning of a group from a provisioning target</li>
 * </ul>
 *
 * @author mchyzer
 */
public class GrouperMcpGroupSave {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGroupSave.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for group_save.
   * The tool uses an "action" enum parameter to determine which operation to perform.
   * All parameters are included in the schema, but which ones are applicable depends
   * on the action. The description for each parameter indicates which actions it is used with.
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_save");
    tool.put("description",
        "Create or update a Grouper group, manage group types, composites, and provisioners. "
        + "Use the 'action' parameter to specify which operation to perform. "
        + "Actions: createGroup, createOrUpdateGroup, updateGroupPart, addGroupType, removeGroupType, "
        + "addComposite, updateComposite, removeComposite, "
        + "addEligibilityRequirement, removeEligibilityRequirement, "
        + "addProvisioner, removeProvisioner.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    // --- action parameter (required for all operations) ---
    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("createGroup");
    actionEnum.add("createOrUpdateGroup");
    actionEnum.add("updateGroupPart");
    actionEnum.add("addGroupType");
    actionEnum.add("removeGroupType");
    actionEnum.add("addComposite");
    actionEnum.add("updateComposite");
    actionEnum.add("removeComposite");
    actionEnum.add("addEligibilityRequirement");
    actionEnum.add("removeEligibilityRequirement");
    actionEnum.add("addProvisioner");
    actionEnum.add("removeProvisioner");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The operation to perform. "
        + "createGroup = create a new group (fails if exists). "
        + "createOrUpdateGroup = create a new group or update an existing one without replacing all settings. "
        + "updateGroupPart = update specific fields on an existing group without replacing all settings. "
        + "addGroupType = assign an object type (e.g., policy, ref, basis) to a group. "
        + "removeGroupType = remove an object type from a group. "
        + "addComposite = make a group a composite of two other groups. "
        + "updateComposite = update the composite definition (type or factor groups) on an existing composite group. "
        + "removeComposite = remove the composite definition from a group. "
        + "addEligibilityRequirement = assign an eligibility requirement to restrict who can be a member. "
        + "removeEligibilityRequirement = remove an eligibility requirement from a group. "
        + "addProvisioner = enable provisioning of a group to a target. "
        + "removeProvisioner = disable provisioning of a group from a target.");
    properties.set("action", actionProp);

    // --- groupName parameter (required for all actions) ---
    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name (e.g., 'stem1:stem2:groupName'). "
        + "Required for all actions.");
    properties.set("groupName", groupNameProp);

    // --- parameters for createGroup and updateGroupPart ---
    ObjectNode descriptionProp = objectMapper.createObjectNode();
    descriptionProp.put("type", "string");
    descriptionProp.put("description",
        "Description of the group. Used with createGroup, createOrUpdateGroup, and updateGroupPart.");
    properties.set("description", descriptionProp);

    ObjectNode displayExtensionProp = objectMapper.createObjectNode();
    displayExtensionProp.put("type", "string");
    displayExtensionProp.put("description",
        "Display extension (friendly name) of the group. "
        + "If not provided on createGroup, defaults to the extension portion of the group name. "
        + "Used with createGroup, createOrUpdateGroup, and updateGroupPart.");
    properties.set("displayExtension", displayExtensionProp);

    ObjectNode typeOfGroupProp = objectMapper.createObjectNode();
    typeOfGroupProp.put("type", "string");
    typeOfGroupProp.put("description",
        "Type of group: 'group', 'role', 'entity'. Defaults to 'group'. "
        + "Used with createGroup and createOrUpdateGroup.");
    properties.set("typeOfGroup", typeOfGroupProp);

    // --- parameters for addGroupType and removeGroupType ---
    ObjectNode objectTypeProp = objectMapper.createObjectNode();
    objectTypeProp.put("type", "string");
    objectTypeProp.put("description",
        "The object type name to add or remove. "
        + "Valid values: basis, ref, policy, etc, grouperSecurity, org, app, service, readOnly, test, manual, intermediate. "
        + "Used with addGroupType and removeGroupType.");
    properties.set("objectType", objectTypeProp);

    ObjectNode dataOwnerProp = objectMapper.createObjectNode();
    dataOwnerProp.put("type", "string");
    dataOwnerProp.put("description",
        "Data owner for the object type. Applicable for types: ref, basis, policy, org, manual. "
        + "Used with addGroupType.");
    properties.set("dataOwner", dataOwnerProp);

    ObjectNode memberDescriptionProp = objectMapper.createObjectNode();
    memberDescriptionProp.put("type", "string");
    memberDescriptionProp.put("description",
        "Member description for the object type. Applicable for types: ref, basis, policy, org, manual. "
        + "Used with addGroupType.");
    properties.set("memberDescription", memberDescriptionProp);

    ObjectNode serviceNameProp = objectMapper.createObjectNode();
    serviceNameProp.put("type", "string");
    serviceNameProp.put("description",
        "Service name for the object type. Applicable for type: app. "
        + "Used with addGroupType.");
    properties.set("serviceName", serviceNameProp);

    // --- parameters for addComposite ---
    ObjectNode compositeTypeProp = objectMapper.createObjectNode();
    compositeTypeProp.put("type", "string");
    ArrayNode compositeTypeEnum = objectMapper.createArrayNode();
    compositeTypeEnum.add("COMPLEMENT");
    compositeTypeEnum.add("INTERSECTION");
    compositeTypeProp.set("enum", compositeTypeEnum);
    compositeTypeProp.put("description",
        "Composite type. COMPLEMENT = members in left but not right, "
        + "INTERSECTION = members in both left and right. "
        + "Required for addComposite and updateComposite.");
    properties.set("compositeType", compositeTypeProp);

    ObjectNode leftGroupNameProp = objectMapper.createObjectNode();
    leftGroupNameProp.put("type", "string");
    leftGroupNameProp.put("description",
        "Fully qualified name of the left factor group for a composite. "
        + "Required for addComposite and updateComposite.");
    properties.set("leftGroupName", leftGroupNameProp);

    ObjectNode rightGroupNameProp = objectMapper.createObjectNode();
    rightGroupNameProp.put("type", "string");
    rightGroupNameProp.put("description",
        "Fully qualified name of the right factor group for a composite. "
        + "Required for addComposite and updateComposite.");
    properties.set("rightGroupName", rightGroupNameProp);

    // --- parameters for addEligibilityRequirement and removeEligibilityRequirement ---
    ObjectNode configIdProp = objectMapper.createObjectNode();
    configIdProp.put("type", "string");
    configIdProp.put("description",
        "The eligibility requirement config ID. This is the identifier from the "
        + "grouper.membershipRequirement.{configId}.* configuration that defines "
        + "the requirement (e.g., which population group members must be in). "
        + "Required for addEligibilityRequirement and removeEligibilityRequirement.");
    properties.set("configId", configIdProp);

    // --- parameters for addProvisioner and removeProvisioner ---
    ObjectNode targetNameProp = objectMapper.createObjectNode();
    targetNameProp.put("type", "string");
    targetNameProp.put("description",
        "The provisioning target name to add or remove. "
        + "Used with addProvisioner and removeProvisioner.");
    properties.set("targetName", targetNameProp);

    inputSchema.set("properties", properties);

    // both action and groupName are required for all operations
    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    required.add("groupName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the group_save tool by dispatching to the appropriate handler
   * based on the action parameter.
   *
   * <p>Flow:
   * 1. Parse and validate the action and groupName (required for all actions)
   * 2. Check that the group is not a protected system group
   * 3. Dispatch to the appropriate handler method based on the action
   * 4. Each handler method parses its own action-specific parameters,
   *    calls the appropriate Grouper API, and returns the result</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user
   * @return the MCP tool result containing the operation result or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    // parse the required parameters that apply to all actions
    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required.");
    }
    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    // block modifications to protected system groups and the etc stem
    if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
    }

    // check readwrite scope restrictions (OAuth only).
    // note: composite factor group names (leftGroupName, rightGroupName) are not
    // checked because they are only referenced (read), not modified.
    if (authUser.isOAuthAuthenticated()) {
      if (!authUser.isGroupInReadwriteScope(groupName)) {
        return buildErrorResult("Access denied: group '" + groupName
            + "' is outside your consented read-write scope.");
      }
    }

    // start a GrouperSession as the authenticated user so that all API calls
    // (GroupSave, CompositeSave, GdgTypeGroupSave, ProvisionableGroupSave, etc.)
    // check privileges against the calling user, not root
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject());
    try {
      // dispatch to the appropriate handler based on the action
      switch (action) {
        case "createGroup":
          return executeCreateGroup(arguments, groupName);
        case "createOrUpdateGroup":
          return executeCreateOrUpdateGroup(arguments, groupName);
        case "updateGroupPart":
          return executeUpdateGroupPart(arguments, groupName);
        case "addGroupType":
          return executeAddGroupType(arguments, groupName);
        case "removeGroupType":
          return executeRemoveGroupType(arguments, groupName);
        case "addComposite":
          return executeAddComposite(arguments, groupName);
        case "updateComposite":
          return executeUpdateComposite(arguments, groupName);
        case "removeComposite":
          return executeRemoveComposite(groupName);
        case "addEligibilityRequirement":
          return executeAddEligibilityRequirement(arguments, groupName);
        case "removeEligibilityRequirement":
          return executeRemoveEligibilityRequirement(arguments, groupName);
        case "addProvisioner":
          return executeAddProvisioner(arguments, groupName);
        case "removeProvisioner":
          return executeRemoveProvisioner(arguments, groupName);
        default:
          return buildErrorResult("Unknown action: " + action
              + ". Valid actions: createGroup, createOrUpdateGroup, updateGroupPart, addGroupType, "
              + "removeGroupType, addComposite, updateComposite, removeComposite, addEligibilityRequirement, "
              + "removeEligibilityRequirement, addProvisioner, removeProvisioner.");
      }
    } catch (Exception e) {
      LOG.error("Error in group_save action '" + action + "' for group: " + groupName, e);
      return buildErrorResult("Error in group_save action '" + action + "': " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Create a new group using GroupSave with SaveMode.INSERT.
   * Fails if the group already exists.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name to create
   * @return the MCP tool result
   */
  private static ObjectNode executeCreateGroup(JsonNode arguments, String groupName) throws Exception {

    String description = arguments.has("description")
        ? arguments.get("description").asText() : null;
    String displayExtension = arguments.has("displayExtension")
        ? arguments.get("displayExtension").asText() : null;
    String typeOfGroup = arguments.has("typeOfGroup")
        ? arguments.get("typeOfGroup").asText() : null;

    // use GroupSave directly (not WS) with INSERT mode so it fails if the group exists
    GroupSave groupSave = new GroupSave()
        .assignName(groupName)
        .assignSaveMode(SaveMode.INSERT)
        .assignCreateParentStemsIfNotExist(true);

    if (StringUtils.isNotBlank(description)) {
      groupSave.assignDescription(description);
    }
    if (StringUtils.isNotBlank(displayExtension)) {
      groupSave.assignDisplayExtension(displayExtension);
    }
    if (StringUtils.isNotBlank(typeOfGroup)) {
      groupSave.assignTypeOfGroup(typeOfGroup);
    }

    Group group = groupSave.save();
    SaveResultType saveResultType = groupSave.getSaveResultType();

    // build the response with the created group details
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "createGroup");
    resultNode.put("resultCode", saveResultType.name());
    resultNode.put("success", true);
    resultNode.put("name", group.getName());
    if (StringUtils.isNotBlank(group.getDisplayExtension())) {
      resultNode.put("displayExtension", group.getDisplayExtension());
    }
    if (StringUtils.isNotBlank(group.getDescription())) {
      resultNode.put("description", group.getDescription());
    }
    resultNode.put("uuid", group.getUuid());

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Create a new group or update an existing one using GroupSave with
   * SaveMode.INSERT_OR_UPDATE and replaceAllSettings=false.
   * If the group does not exist it will be created (parent stems are created
   * automatically). If the group already exists only the explicitly provided
   * fields are changed; other settings are preserved.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name to create or update
   * @return the MCP tool result
   */
  private static ObjectNode executeCreateOrUpdateGroup(JsonNode arguments, String groupName) throws Exception {

    String description = arguments.has("description")
        ? arguments.get("description").asText() : null;
    String displayExtension = arguments.has("displayExtension")
        ? arguments.get("displayExtension").asText() : null;
    String typeOfGroup = arguments.has("typeOfGroup")
        ? arguments.get("typeOfGroup").asText() : null;

    // use INSERT_OR_UPDATE so the group is created if it doesn't exist,
    // and replaceAllSettings=false so existing settings are preserved on update
    GroupSave groupSave = new GroupSave()
        .assignName(groupName)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignReplaceAllSettings(false)
        .assignCreateParentStemsIfNotExist(true);

    if (StringUtils.isNotBlank(description)) {
      groupSave.assignDescription(description);
    }
    if (StringUtils.isNotBlank(displayExtension)) {
      groupSave.assignDisplayExtension(displayExtension);
    }
    if (StringUtils.isNotBlank(typeOfGroup)) {
      groupSave.assignTypeOfGroup(typeOfGroup);
    }

    Group group = groupSave.save();
    SaveResultType saveResultType = groupSave.getSaveResultType();

    // build the response with the group details
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "createOrUpdateGroup");
    resultNode.put("resultCode", saveResultType.name());
    resultNode.put("success", true);
    resultNode.put("name", group.getName());
    if (StringUtils.isNotBlank(group.getDisplayExtension())) {
      resultNode.put("displayExtension", group.getDisplayExtension());
    }
    if (StringUtils.isNotBlank(group.getDescription())) {
      resultNode.put("description", group.getDescription());
    }
    resultNode.put("uuid", group.getUuid());

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Update specific fields on an existing group without replacing all settings.
   * Uses GroupSave with replaceAllSettings=false and SaveMode.UPDATE so that
   * only explicitly provided fields are changed; other settings are preserved.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name to update
   * @return the MCP tool result
   */
  private static ObjectNode executeUpdateGroupPart(JsonNode arguments, String groupName) throws Exception {

    String description = arguments.has("description")
        ? arguments.get("description").asText() : null;
    String displayExtension = arguments.has("displayExtension")
        ? arguments.get("displayExtension").asText() : null;

    // use GroupSave with replaceAllSettings=false so only assigned fields change
    GroupSave groupSave = new GroupSave()
        .assignName(groupName)
        .assignReplaceAllSettings(false)
        .assignSaveMode(SaveMode.UPDATE);

    if (StringUtils.isNotBlank(description)) {
      groupSave.assignDescription(description);
    }
    if (StringUtils.isNotBlank(displayExtension)) {
      groupSave.assignDisplayExtension(displayExtension);
    }

    Group group = groupSave.save();
    SaveResultType saveResultType = groupSave.getSaveResultType();

    // build the response with the updated group details
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "updateGroupPart");
    resultNode.put("resultCode", saveResultType.name());
    resultNode.put("success", true);
    resultNode.put("name", group.getName());
    if (StringUtils.isNotBlank(group.getDisplayExtension())) {
      resultNode.put("displayExtension", group.getDisplayExtension());
    }
    if (StringUtils.isNotBlank(group.getDescription())) {
      resultNode.put("description", group.getDescription());
    }
    resultNode.put("uuid", group.getUuid());

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Assign an object type (e.g., policy, ref, basis, manual, app, org, test, service)
   * to a group using the GdgTypeGroupSave API.
   *
   * <p>Some types accept optional extra parameters:
   * - ref, basis, policy, org, manual: dataOwner and memberDescription
   * - app: serviceName</p>
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeAddGroupType(JsonNode arguments, String groupName) throws Exception {

    String objectType = arguments.has("objectType")
        ? arguments.get("objectType").asText() : null;
    String dataOwner = arguments.has("dataOwner")
        ? arguments.get("dataOwner").asText() : null;
    String memberDescription = arguments.has("memberDescription")
        ? arguments.get("memberDescription").asText() : null;
    String serviceName = arguments.has("serviceName")
        ? arguments.get("serviceName").asText() : null;

    if (StringUtils.isBlank(objectType)) {
      return buildErrorResult("objectType is required for addGroupType.");
    }

    // use GdgTypeGroupSave to assign the type to the group
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave()
        .assignGroupName(groupName)
        .assignType(objectType);

    if (StringUtils.isNotBlank(dataOwner)) {
      gdgTypeGroupSave.assignDataOwner(dataOwner);
    }
    if (StringUtils.isNotBlank(memberDescription)) {
      gdgTypeGroupSave.assignMemberDescription(memberDescription);
    }
    if (StringUtils.isNotBlank(serviceName)) {
      gdgTypeGroupSave.assignServiceName(serviceName);
    }

    GrouperObjectTypesAttributeValue result = gdgTypeGroupSave.save();
    SaveResultType saveResultType = gdgTypeGroupSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "addGroupType");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("objectType", objectType);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Remove an object type from a group using the GdgTypeGroupSave API
   * with SaveMode.DELETE.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeRemoveGroupType(JsonNode arguments, String groupName) throws Exception {

    String objectType = arguments.has("objectType")
        ? arguments.get("objectType").asText() : null;

    if (StringUtils.isBlank(objectType)) {
      return buildErrorResult("objectType is required for removeGroupType.");
    }

    // use GdgTypeGroupSave with DELETE mode to remove the type from the group
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave()
        .assignGroupName(groupName)
        .assignType(objectType)
        .assignSaveMode(SaveMode.DELETE);

    gdgTypeGroupSave.save();
    SaveResultType saveResultType = gdgTypeGroupSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "removeGroupType");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("objectType", objectType);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Make a group a composite of two other groups using CompositeSave.
   * A composite group's membership is automatically computed from the left and right
   * factor groups using the specified composite type (COMPLEMENT or INTERSECTION).
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name (the owner/composite group)
   * @return the MCP tool result
   */
  private static ObjectNode executeAddComposite(JsonNode arguments, String groupName) throws Exception {

    String compositeType = arguments.has("compositeType")
        ? arguments.get("compositeType").asText() : null;
    String leftGroupName = arguments.has("leftGroupName")
        ? arguments.get("leftGroupName").asText() : null;
    String rightGroupName = arguments.has("rightGroupName")
        ? arguments.get("rightGroupName").asText() : null;

    // validate required parameters for composite creation
    if (StringUtils.isBlank(compositeType)) {
      return buildErrorResult("compositeType is required for addComposite.");
    }
    if (StringUtils.isBlank(leftGroupName)) {
      return buildErrorResult("leftGroupName is required for addComposite.");
    }
    if (StringUtils.isBlank(rightGroupName)) {
      return buildErrorResult("rightGroupName is required for addComposite.");
    }

    // use CompositeSave to add the composite definition to the group
    CompositeSave compositeSave = new CompositeSave()
        .assignOwnerName(groupName)
        .assignLeftFactorName(leftGroupName)
        .assignRightFactorName(rightGroupName)
        .assignType(compositeType);

    Composite composite = compositeSave.save();
    SaveResultType saveResultType = compositeSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "addComposite");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("compositeType", compositeType);
    resultNode.put("leftGroupName", leftGroupName);
    resultNode.put("rightGroupName", rightGroupName);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Update the composite definition on an existing composite group using CompositeSave
   * with SaveMode.UPDATE. Allows changing the composite type and/or factor groups.
   * Fails if the group does not already have a composite.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name (the owner/composite group)
   * @return the MCP tool result
   */
  private static ObjectNode executeUpdateComposite(JsonNode arguments, String groupName) throws Exception {

    String compositeType = arguments.has("compositeType")
        ? arguments.get("compositeType").asText() : null;
    String leftGroupName = arguments.has("leftGroupName")
        ? arguments.get("leftGroupName").asText() : null;
    String rightGroupName = arguments.has("rightGroupName")
        ? arguments.get("rightGroupName").asText() : null;

    // validate required parameters for composite update
    if (StringUtils.isBlank(compositeType)) {
      return buildErrorResult("compositeType is required for updateComposite.");
    }
    if (StringUtils.isBlank(leftGroupName)) {
      return buildErrorResult("leftGroupName is required for updateComposite.");
    }
    if (StringUtils.isBlank(rightGroupName)) {
      return buildErrorResult("rightGroupName is required for updateComposite.");
    }

    // use CompositeSave with UPDATE mode so it fails if no composite exists
    CompositeSave compositeSave = new CompositeSave()
        .assignOwnerName(groupName)
        .assignLeftFactorName(leftGroupName)
        .assignRightFactorName(rightGroupName)
        .assignType(compositeType)
        .assignSaveMode(SaveMode.UPDATE);

    Composite composite = compositeSave.save();
    SaveResultType saveResultType = compositeSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "updateComposite");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("compositeType", compositeType);
    resultNode.put("leftGroupName", leftGroupName);
    resultNode.put("rightGroupName", rightGroupName);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Remove the composite definition from a group using CompositeSave with SaveMode.DELETE.
   * After removal, the group will no longer have automatically computed membership
   * from factor groups.
   *
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeRemoveComposite(String groupName) throws Exception {

    // use CompositeSave with DELETE mode; only the owner group name is needed
    CompositeSave compositeSave = new CompositeSave()
        .assignOwnerName(groupName)
        .assignSaveMode(SaveMode.DELETE);

    compositeSave.save();
    SaveResultType saveResultType = compositeSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "removeComposite");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Assign an eligibility requirement to a group. Eligibility requirements restrict
   * who can be a member of the group by requiring members to also be in a specific
   * population group. The requirement is identified by its configId, which corresponds
   * to a grouper.membershipRequirement.{configId}.* configuration entry.
   *
   * <p>This works by assigning the requirement's attribute to the group using
   * AttributeAssignToGroupSave. Once assigned, the membership hook will prevent
   * non-eligible members from being added, and the change log listener will
   * remove existing members who don't meet the requirement.</p>
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeAddEligibilityRequirement(JsonNode arguments, String groupName) throws Exception {

    String configId = arguments.has("configId")
        ? arguments.get("configId").asText() : null;

    if (StringUtils.isBlank(configId)) {
      return buildErrorResult("configId is required for addEligibilityRequirement.");
    }

    // look up the config bean for this configId to get the attribute name
    MembershipRequireConfigBean configBean = findConfigBeanByConfigId(configId);
    if (configBean == null) {
      return buildErrorResult("Unknown eligibility requirement configId: " + configId
          + ". Check the grouper.membershipRequirement.{configId}.* configuration.");
    }

    // assign the requirement's attribute to the group
    AttributeAssignToGroupSave attributeAssignSave = new AttributeAssignToGroupSave()
        .assignNameOfAttributeDefName(configBean.getAttributeName())
        .assignGroupName(groupName);

    attributeAssignSave.save();
    SaveResultType saveResultType = attributeAssignSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "addEligibilityRequirement");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("configId", configId);
    resultNode.put("requireGroupName", configBean.getRequireGroupName());

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Remove an eligibility requirement from a group. After removal, the requirement
   * will no longer restrict membership; existing members remain unchanged.
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeRemoveEligibilityRequirement(JsonNode arguments, String groupName) throws Exception {

    String configId = arguments.has("configId")
        ? arguments.get("configId").asText() : null;

    if (StringUtils.isBlank(configId)) {
      return buildErrorResult("configId is required for removeEligibilityRequirement.");
    }

    // look up the config bean for this configId to get the attribute name
    MembershipRequireConfigBean configBean = findConfigBeanByConfigId(configId);
    if (configBean == null) {
      return buildErrorResult("Unknown eligibility requirement configId: " + configId
          + ". Check the grouper.membershipRequirement.{configId}.* configuration.");
    }

    // remove the requirement's attribute from the group
    AttributeAssignToGroupSave attributeAssignSave = new AttributeAssignToGroupSave()
        .assignNameOfAttributeDefName(configBean.getAttributeName())
        .assignGroupName(groupName)
        .assignSaveMode(SaveMode.DELETE);

    attributeAssignSave.save();
    SaveResultType saveResultType = attributeAssignSave.getSaveResultType();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "removeEligibilityRequirement");
    resultNode.put("resultCode", saveResultType != null ? saveResultType.name() : "UNKNOWN");
    resultNode.put("success", true);
    resultNode.put("groupName", groupName);
    resultNode.put("configId", configId);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Look up a MembershipRequireConfigBean by its configId.
   * Iterates through all configured membership requirement beans to find
   * the one matching the given configId.
   *
   * @param configId the config ID to look up
   * @return the matching config bean, or null if not found
   */
  private static MembershipRequireConfigBean findConfigBeanByConfigId(String configId) {
    List<MembershipRequireConfigBean> allBeans = MembershipRequireEngine.membershipRequireConfigBeans();
    for (MembershipRequireConfigBean bean : GrouperUtil.nonNull(allBeans)) {
      if (StringUtils.equals(configId, bean.getConfigId())) {
        return bean;
      }
    }
    return null;
  }

  /**
   * Enable provisioning of a group to a specific provisioning target.
   * Sets up the provisioning attributes on the group so that the provisioner daemon
   * will include this group in its next sync cycle.
   *
   * <p>Uses {@link ProvisionableGroupSave} which validates the target name and checks
   * that the session user has permission to assign provisioning for this target
   * (member of the target's groupAllowedToAssign group, or wheel/root).</p>
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeAddProvisioner(JsonNode arguments, String groupName) throws Exception {

    String targetName = arguments.has("targetName")
        ? arguments.get("targetName").asText() : null;

    if (StringUtils.isBlank(targetName)) {
      return buildErrorResult("targetName is required for addProvisioner.");
    }

    // ProvisionableGroupSave checks isTargetEditable internally (member of
    // groupAllowedToAssign or wheel/root) and validates the target name
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName(targetName)
        .assignGroupName(groupName)
        .save();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "addProvisioner");
    resultNode.put("success", true);
    resultNode.put("resultType", provisionableGroupSave.getSaveResultType().name());
    resultNode.put("groupName", groupName);
    resultNode.put("targetName", targetName);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * Disable provisioning of a group from a specific provisioning target.
   * Removes the provisioning attribute assignment so the provisioner daemon
   * will no longer sync this group to the target.
   *
   * <p>Uses {@link ProvisionableGroupSave} with DELETE mode which validates
   * that the session user has permission to modify provisioning for this target
   * (member of the target's groupAllowedToAssign group, or wheel/root).</p>
   *
   * @param arguments the MCP request arguments
   * @param groupName the fully qualified group name
   * @return the MCP tool result
   */
  private static ObjectNode executeRemoveProvisioner(JsonNode arguments, String groupName) throws Exception {

    String targetName = arguments.has("targetName")
        ? arguments.get("targetName").asText() : null;

    if (StringUtils.isBlank(targetName)) {
      return buildErrorResult("targetName is required for removeProvisioner.");
    }

    // ProvisionableGroupSave checks isTargetEditable internally (member of
    // groupAllowedToAssign or wheel/root) and handles the delete
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName(targetName)
        .assignGroupName(groupName)
        .assignSaveMode(SaveMode.DELETE)
        .save();

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("action", "removeProvisioner");
    resultNode.put("success", true);
    resultNode.put("resultType", provisionableGroupSave.getSaveResultType().name());
    resultNode.put("groupName", groupName);
    resultNode.put("targetName", targetName);

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

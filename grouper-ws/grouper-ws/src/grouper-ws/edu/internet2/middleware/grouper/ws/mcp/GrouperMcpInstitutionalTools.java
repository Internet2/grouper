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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshOutputLine;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputValidationType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateSecurityRunType;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP tool for discovering and executing institution-specific GSH templates
 * that have been flagged as MCP-enabled.  Supports two actions:
 * <ul>
 *   <li>{@code schema} - list MCP-enabled templates the user can see, with their metadata,
 *       input definitions (including mcpScopeType for scope-validated inputs), whether they
 *       execute on groups/folders, and whether they are readonly or readwrite</li>
 *   <li>{@code execute} - execute an MCP-enabled template, enforcing:
 *     <ul>
 *       <li>MCP readonly vs readwrite access (templates with mcpReadonly=true can be run
 *           by readonly MCP users; others require readwrite)</li>
 *       <li>GSH template security (wheel, specifiedGroup, privilegeOnObject, everyone)</li>
 *       <li>Input scope validation (inputs configured with mcpScopeType are validated against
 *           the user's approved readwrite folders/groups/subjects scopes)</li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>Configuration in the GSH template wizard:</p>
 * <ul>
 *   <li>{@code mcpEnabled} - boolean, default false. Enables MCP for this template.</li>
 *   <li>{@code mcpReadonly} - boolean, default false. If true, readonly MCP users can execute
 *       this template. Only shown when mcpEnabled is true.</li>
 *   <li>{@code input.N.mcpScopeType} - dropdown (folders, groups, subjects). Restricts this
 *       input to approved readwrite scopes. Only shown when mcpEnabled is true and mcpReadonly
 *       is false.</li>
 * </ul>
 *
 * @author mchyzer
 */
public class GrouperMcpInstitutionalTools {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpInstitutionalTools.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for institutional_tools
   * @param authUser the authenticated user (used to determine which tools are visible)
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the tool definition as a Jackson ObjectNode, or null if no institutional tools
   *         are available for this user (so the tool should not be advertised)
   */
  public static ObjectNode toolDefinition(GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) {

    // build a dynamic list of available tool names for this user
    List<String> availableToolNames = retrieveAvailableToolNames(authUser, hasReadwriteAccess);

    // if no tools are available, don't advertise this tool at all
    if (availableToolNames.isEmpty()) {
      return null;
    }

    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "institutional_tools");

    StringBuilder description = new StringBuilder();
    description.append("Discover and execute institution-specific tools (GSH templates) that the deployer has made "
        + "available via MCP. ");
    description.append("Available tools: ");
    description.append(GrouperUtil.join(availableToolNames.iterator(), ", "));
    description.append(". ");

    description.append("Use action 'schema' to list available tools with their configId, name, "
        + "description, input definitions (names, types, required, validation, mcpScopeType), "
        + "whether they execute on a group name, folder name, or both, and whether they are "
        + "mcpReadonly (accessible with readonly MCP access) or require readwrite MCP access. "
        + "Templates with securityRunType 'privilegeOnObject' are always listed but authorization "
        + "is checked at execution time based on the user's privilege on the specific group or folder. "
        + "Inputs with mcpScopeType (folders, groups, or subjects) are validated against the user's "
        + "approved readwrite scopes. "
        + "Use action 'execute' to run a specific tool by configId, providing the required inputs.");

    tool.put("description", description.toString());

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    // action
    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("schema");
    actionEnum.add("execute");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The action to perform. 'schema' returns available MCP-enabled tools with their "
        + "configId, name, description, inputs (names, types, required, validation), "
        + "and whether executeOnGroupName or executeOnFolderName or both are applicable. "
        + "'execute' runs a specific tool.");
    properties.set("action", actionProp);

    // configId
    ObjectNode configIdProp = objectMapper.createObjectNode();
    configIdProp.put("type", "string");
    configIdProp.put("description",
        "The config ID of the template to execute. Required for 'execute' action. "
        + "Use 'schema' action to discover available config IDs.");
    properties.set("configId", configIdProp);

    // ownerType
    ObjectNode ownerTypeProp = objectMapper.createObjectNode();
    ownerTypeProp.put("type", "string");
    ArrayNode ownerTypeEnum = objectMapper.createArrayNode();
    ownerTypeEnum.add("group");
    ownerTypeEnum.add("stem");
    ownerTypeProp.set("enum", ownerTypeEnum);
    ownerTypeProp.put("description",
        "Owner type for execution context. 'group' if running on a group, 'stem' if running on a folder. "
        + "Optional for 'execute' action. Check the schema to see which types are applicable.");
    properties.set("ownerType", ownerTypeProp);

    // ownerGroupName
    ObjectNode ownerGroupNameProp = objectMapper.createObjectNode();
    ownerGroupNameProp.put("type", "string");
    ownerGroupNameProp.put("description",
        "The fully qualified group name to execute on (e.g., 'stem1:stem2:groupName'). "
        + "Required when ownerType is 'group'.");
    properties.set("ownerGroupName", ownerGroupNameProp);

    // ownerStemName
    ObjectNode ownerStemNameProp = objectMapper.createObjectNode();
    ownerStemNameProp.put("type", "string");
    ownerStemNameProp.put("description",
        "The fully qualified folder name to execute on (e.g., 'stem1:stem2'). "
        + "Required when ownerType is 'stem'.");
    properties.set("ownerStemName", ownerStemNameProp);

    // inputs
    ObjectNode inputsProp = objectMapper.createObjectNode();
    inputsProp.put("type", "object");
    inputsProp.put("description",
        "Key-value pairs of input values for the template. Keys are the input names "
        + "as returned by the 'schema' action. The gsh_input_ prefix is not needed but accepted. "
        + "Required for 'execute' action if the template has inputs.");
    ObjectNode inputsAdditionalProps = objectMapper.createObjectNode();
    inputsAdditionalProps.put("type", "string");
    inputsProp.set("additionalProperties", inputsAdditionalProps);
    properties.set("inputs", inputsProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * retrieve the names of MCP-enabled GSH templates that the authenticated user
   * can see, respecting security (wheel, specifiedGroup, privilegeOnObject, everyone)
   * and readonly/readwrite access.
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return list of template display names (may be empty)
   */
  private static List<String> retrieveAvailableToolNames(GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) {

    List<String> toolNames = new ArrayList<String>();

    try {
      List<GshTemplateConfiguration> allConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();

      Subject subject = authUser.getSubject();
      boolean isWheelOrRoot = edu.internet2.middleware.grouper.privs.PrivilegeHelper.isWheelOrRoot(subject);

      // pass 1: collect MCP-enabled templates and specifiedGroup groups for batch membership check
      Map<String, GshTemplateConfig> mcpEnabledConfigs = new LinkedHashMap<String, GshTemplateConfig>();
      MembershipFinder membershipFinder = null;

      for (GshTemplateConfiguration gshTemplateConfiguration : allConfigs) {

        if (!gshTemplateConfiguration.isEnabled()) {
          continue;
        }

        String configId = gshTemplateConfiguration.getConfigId();

        GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
        templateConfig.populateConfiguration();

        if (!templateConfig.isEnabled() || !templateConfig.isMcpEnabled()) {
          continue;
        }

        GshTemplateSecurityRunType securityRunType = templateConfig.getGshTemplateSecurityRunType();

        if (securityRunType == GshTemplateSecurityRunType.wheel && !isWheelOrRoot) {
          continue;
        }

        mcpEnabledConfigs.put(configId, templateConfig);

        if (securityRunType == GshTemplateSecurityRunType.specifiedGroup && !isWheelOrRoot) {
          Group groupThatCanRun = templateConfig.getGroupThatCanRun();
          if (groupThatCanRun != null) {
            if (membershipFinder == null) {
              membershipFinder = new MembershipFinder()
                  .addSubject(subject)
                  .addField(Group.getDefaultList())
                  .assignCheckSecurity(false);
            }
            membershipFinder.addGroup(groupThatCanRun);
          }
        }
      }

      // batch membership check
      MembershipResult membershipResult = null;
      if (membershipFinder != null) {
        membershipResult = membershipFinder.findMembershipResult();
      }

      // pass 2: filter by authorization and collect names
      for (Map.Entry<String, GshTemplateConfig> entry : mcpEnabledConfigs.entrySet()) {

        GshTemplateConfig templateConfig = entry.getValue();
        GshTemplateSecurityRunType securityRunType = templateConfig.getGshTemplateSecurityRunType();

        boolean canRun = false;
        if (securityRunType == GshTemplateSecurityRunType.everyone
            || securityRunType == GshTemplateSecurityRunType.privilegeOnObject) {
          canRun = true;
        } else if (securityRunType == GshTemplateSecurityRunType.wheel) {
          canRun = true;
        } else if (securityRunType == GshTemplateSecurityRunType.specifiedGroup) {
          if (isWheelOrRoot) {
            canRun = true;
          } else {
            Group groupThatCanRun = templateConfig.getGroupThatCanRun();
            canRun = groupThatCanRun != null && membershipResult != null
                && membershipResult.hasGroupMembership(groupThatCanRun.getName(), subject);
          }
        }

        if (!canRun) {
          continue;
        }

        if (!hasReadwriteAccess && !templateConfig.isMcpReadonly()) {
          continue;
        }

        String name = templateConfig.getTemplateNameForUi();
        if (StringUtils.isNotBlank(name)) {
          toolNames.add(name);
        } else {
          toolNames.add(entry.getKey());
        }
      }
    } catch (Exception e) {
      LOG.error("Error retrieving available institutional tool names for tool description", e);
    }

    return toolNames;
  }

  /**
   * execute the institutional_tools tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required. Use 'schema' or 'execute'.");
    }

    try {
      if ("schema".equals(action)) {
        return schema(authUser, hasReadwriteAccess);
      }

      if ("execute".equals(action)) {
        return executeTemplate(arguments, authUser, hasReadwriteAccess);
      }

      return buildErrorResult("Unknown action '" + action + "'. Use 'schema' or 'execute'.");

    } catch (Exception e) {
      LOG.error("Error in institutional_tools", e);
      return buildErrorResult("Error in institutional_tools: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * return schema information for all MCP-enabled GSH templates that the
   * authenticated user is authorized to see.
   * @param authUser the authenticated user
   * @return the MCP tool result with template schemas
   */
  private static ObjectNode schema(GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) throws Exception {

    List<GshTemplateConfiguration> allConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();

    Subject subject = authUser.getSubject();
    boolean isWheelOrRoot = edu.internet2.middleware.grouper.privs.PrivilegeHelper.isWheelOrRoot(subject);

    // pass 1: collect MCP-enabled templates and the specifiedGroup groups we need to check
    // use a linked map to preserve order
    Map<String, GshTemplateConfig> mcpEnabledConfigs = new LinkedHashMap<String, GshTemplateConfig>();
    MembershipFinder membershipFinder = null;

    for (GshTemplateConfiguration gshTemplateConfiguration : allConfigs) {

      if (!gshTemplateConfiguration.isEnabled()) {
        continue;
      }

      String configId = gshTemplateConfiguration.getConfigId();

      GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
      templateConfig.populateConfiguration();

      if (!templateConfig.isEnabled()) {
        continue;
      }

      if (!templateConfig.isMcpEnabled()) {
        continue;
      }

      GshTemplateSecurityRunType securityRunType = templateConfig.getGshTemplateSecurityRunType();

      // for wheel type, only wheel users can see them
      if (securityRunType == GshTemplateSecurityRunType.wheel && !isWheelOrRoot) {
        continue;
      }

      mcpEnabledConfigs.put(configId, templateConfig);

      // collect specifiedGroup groups for batch membership check
      if (securityRunType == GshTemplateSecurityRunType.specifiedGroup && !isWheelOrRoot) {
        Group groupThatCanRun = templateConfig.getGroupThatCanRun();
        if (groupThatCanRun != null) {
          if (membershipFinder == null) {
            membershipFinder = new MembershipFinder()
                .addSubject(subject)
                .addField(Group.getDefaultList())
                .assignCheckSecurity(false);
          }
          membershipFinder.addGroup(groupThatCanRun);
        }
      }
    }

    // batch membership check for all specifiedGroup groups in one query
    MembershipResult membershipResult = null;
    if (membershipFinder != null) {
      membershipResult = membershipFinder.findMembershipResult();
    }

    // pass 2: build the schema output, filtering by authorization
    ArrayNode toolsArray = objectMapper.createArrayNode();

    for (Map.Entry<String, GshTemplateConfig> entry : mcpEnabledConfigs.entrySet()) {

      String configId = entry.getKey();
      GshTemplateConfig templateConfig = entry.getValue();
      GshTemplateSecurityRunType securityRunType = templateConfig.getGshTemplateSecurityRunType();

      // check authorization
      boolean canRun = false;
      if (securityRunType == GshTemplateSecurityRunType.everyone
          || securityRunType == GshTemplateSecurityRunType.privilegeOnObject) {
        canRun = true;
      } else if (securityRunType == GshTemplateSecurityRunType.wheel) {
        // already filtered non-wheel users in pass 1
        canRun = true;
      } else if (securityRunType == GshTemplateSecurityRunType.specifiedGroup) {
        // wheel users can also run specifiedGroup templates
        if (isWheelOrRoot) {
          canRun = true;
        } else {
          Group groupThatCanRun = templateConfig.getGroupThatCanRun();
          canRun = groupThatCanRun != null && membershipResult != null
              && membershipResult.hasGroupMembership(groupThatCanRun.getName(), subject);
        }
      }

      if (!canRun) {
        continue;
      }

      // if user only has readonly access, skip non-readonly templates
      if (!hasReadwriteAccess && !templateConfig.isMcpReadonly()) {
        continue;
      }

      ObjectNode toolNode = objectMapper.createObjectNode();
      toolNode.put("configId", configId);
      toolNode.put("name", templateConfig.getTemplateNameForUi());
      toolNode.put("description", templateConfig.getTemplateDescriptionForUi());
      toolNode.put("securityRunType", securityRunType.name());
      toolNode.put("mcpReadonly", templateConfig.isMcpReadonly());

      // executeOnGroupName / executeOnFolderName
      toolNode.put("executeOnGroupName", templateConfig.isShowOnGroups());
      toolNode.put("executeOnFolderName", templateConfig.isShowOnFolders());

      // inputs
      ArrayNode inputsArray = objectMapper.createArrayNode();
      for (GshTemplateInputConfig inputConfig : templateConfig.getGshTemplateInputConfigs()) {

        ObjectNode inputNode = objectMapper.createObjectNode();
        // strip gsh_input_ prefix for AI consumers; they can use the short name
        String inputName = inputConfig.getName();
        if (inputName.startsWith("gsh_input_")) {
          inputName = inputName.substring("gsh_input_".length());
        }
        inputNode.put("name", inputName);

        if (inputConfig.getGshTemplateInputType() != null) {
          inputNode.put("type", inputConfig.getGshTemplateInputType().name().toLowerCase());
        }

        inputNode.put("required", inputConfig.isRequired());

        String label = inputConfig.getLabelForUi();
        if (StringUtils.isNotBlank(label)) {
          inputNode.put("label", label);
        }

        String description = inputConfig.getDescriptionForUi();
        if (StringUtils.isNotBlank(description)) {
          inputNode.put("description", description);
        }

        if (StringUtils.isNotBlank(inputConfig.getDefaultValue())) {
          inputNode.put("defaultValue", inputConfig.getDefaultValue());
        }

        if (inputConfig.getConfigItemFormElement() != null) {
          inputNode.put("formElement", inputConfig.getConfigItemFormElement().name().toLowerCase());
        }

        // validation
        if (inputConfig.getGshTemplateInputValidationType() != null) {
          ObjectNode validationNode = objectMapper.createObjectNode();
          validationNode.put("type", inputConfig.getGshTemplateInputValidationType().name().toLowerCase());

          if (inputConfig.getGshTemplateInputValidationType() == GshTemplateInputValidationType.regex
              && StringUtils.isNotBlank(inputConfig.getValidationRegex())) {
            validationNode.put("regex", inputConfig.getValidationRegex());
          }

          String validationMessage = inputConfig.getValidationMessage();
          if (StringUtils.isNotBlank(validationMessage)) {
            validationNode.put("message", validationMessage);
          }

          inputNode.set("validation", validationNode);
        }

        if (inputConfig.getMaxLength() != null && inputConfig.getMaxLength() > 0) {
          inputNode.put("maxLength", inputConfig.getMaxLength());
        }

        // MCP scope type
        if (StringUtils.isNotBlank(inputConfig.getMcpScopeType())) {
          inputNode.put("mcpScopeType", inputConfig.getMcpScopeType());
        }

        // dropdown values if applicable
        if (inputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
          if (StringUtils.isNotBlank(inputConfig.getDropdownCsvValue())) {
            inputNode.put("dropdownValues", inputConfig.getDropdownCsvValue());
          } else if (StringUtils.isNotBlank(inputConfig.getDropdownJsonValue())) {
            inputNode.put("dropdownValuesJson", inputConfig.getDropdownJsonValue());
          }
        }

        inputsArray.add(inputNode);
      }

      toolNode.set("inputs", inputsArray);
      toolsArray.add(toolNode);
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("toolCount", toolsArray.size());
    resultNode.set("tools", toolsArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * execute an MCP-enabled GSH template
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private static ObjectNode executeTemplate(JsonNode arguments, GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) throws Exception {

    String configId = arguments.has("configId")
        ? arguments.get("configId").asText() : null;

    if (StringUtils.isBlank(configId)) {
      return buildErrorResult("configId is required for 'execute' action. "
          + "Use 'schema' action to discover available config IDs.");
    }

    // load the template config
    GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
    try {
      templateConfig.populateConfiguration();
    } catch (Exception e) {
      return buildErrorResult("Error loading template config '" + configId + "': " + e.getMessage());
    }

    if (!templateConfig.isEnabled()) {
      return buildErrorResult("Template '" + configId + "' is not enabled.");
    }

    if (!templateConfig.isMcpEnabled()) {
      return buildErrorResult("Template '" + configId + "' is not MCP-enabled.");
    }

    // if user only has readonly access, they can only execute readonly templates
    if (!hasReadwriteAccess && !templateConfig.isMcpReadonly()) {
      return buildErrorResult("Access denied: template '" + configId + "' requires MCP readwrite access.");
    }

    // verify security run type is not unsupported (all types are currently supported)
    GshTemplateSecurityRunType securityRunType = templateConfig.getGshTemplateSecurityRunType();
    if (securityRunType == null) {
      return buildErrorResult("Template '" + configId + "' does not have a securityRunType configured.");
    }

    Subject subject = authUser.getSubject();

    // set up the exec
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(configId);
    exec.assignCurrentUser(subject);

    // owner type
    String ownerType = arguments.has("ownerType")
        ? arguments.get("ownerType").asText() : null;

    if (StringUtils.isNotBlank(ownerType)) {
      if ("group".equals(ownerType)) {
        String ownerGroupName = arguments.has("ownerGroupName")
            ? arguments.get("ownerGroupName").asText() : null;
        if (StringUtils.isBlank(ownerGroupName)) {
          return buildErrorResult("ownerGroupName is required when ownerType is 'group'.");
        }
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
        exec.assignOwnerGroupName(ownerGroupName);
      } else if ("stem".equals(ownerType)) {
        String ownerStemName = arguments.has("ownerStemName")
            ? arguments.get("ownerStemName").asText() : null;
        if (StringUtils.isBlank(ownerStemName)) {
          return buildErrorResult("ownerStemName is required when ownerType is 'stem'.");
        }
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
        exec.assignOwnerStemName(ownerStemName);
      } else {
        return buildErrorResult("Invalid ownerType '" + ownerType + "'. Must be 'group' or 'stem'.");
      }
    } else if (!templateConfig.isAllowWsFromNoOwner()) {
      // no owner type specified and allowWsFromNoOwner is false,
      // try to resolve a default group or folder from config (same as UI)
      ObjectNode resolveError = resolveDefaultOwner(templateConfig, configId, exec);
      if (resolveError != null) {
        return resolveError;
      }
    }

    // inputs
    JsonNode inputsNode = arguments.has("inputs") ? arguments.get("inputs") : null;
    if (inputsNode != null && inputsNode.isObject()) {
      java.util.Iterator<String> fieldNames = inputsNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        // auto-prepend gsh_input_ if not already present
        String inputName = fieldName.startsWith("gsh_input_") ? fieldName : "gsh_input_" + fieldName;
        String fieldValue = inputsNode.get(fieldName).asText();
        GshTemplateInput input = new GshTemplateInput();
        input.assignName(inputName);
        input.assignValue(fieldValue);
        exec.addGshTemplateInput(input);
      }
    }

    // validate MCP scope restrictions on inputs for non-readonly templates
    if (!templateConfig.isMcpReadonly()) {
      for (GshTemplateInputConfig inputConfig : templateConfig.getGshTemplateInputConfigs()) {
        String mcpScopeType = inputConfig.getMcpScopeType();
        if (StringUtils.isBlank(mcpScopeType)) {
          continue;
        }
        // find the matching input value
        String inputName = inputConfig.getName();
        String shortName = inputName.startsWith("gsh_input_") ? inputName.substring("gsh_input_".length()) : inputName;
        String inputValue = null;
        if (inputsNode != null && inputsNode.isObject()) {
          if (inputsNode.has(inputName)) {
            inputValue = inputsNode.get(inputName).asText();
          } else if (inputsNode.has(shortName)) {
            inputValue = inputsNode.get(shortName).asText();
          }
        }
        if (StringUtils.isBlank(inputValue)) {
          continue;
        }
        // split, trim, and validate each value
        String[] values = GrouperUtil.splitTrim(inputValue, ",");
        for (String value : values) {
          if (StringUtils.isBlank(value)) {
            continue;
          }
          boolean inScope = true;
          if ("folders".equals(mcpScopeType)) {
            inScope = authUser.isStemInReadwriteScope(value);
          } else if ("groups".equals(mcpScopeType)) {
            inScope = authUser.isGroupInReadwriteScope(value);
          } else if ("subjects".equals(mcpScopeType)) {
            inScope = authUser.isSubjectInReadwriteScope(value);
          }
          if (!inScope) {
            return buildErrorResult("Access denied: value '" + value + "' for input '" + shortName
                + "' is not within the approved " + mcpScopeType + " readwrite scope.");
          }
        }
      }
    }

    // execute
    final GshTemplateExec finalExec = exec;
    GshTemplateExecOutput output = (GshTemplateExecOutput) GrouperSession.internal_callbackRootGrouperSession(
        new GrouperSessionHandler() {
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            return finalExec.execute();
          }
        });

    // build result
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("configId", configId);
    resultNode.put("success", output.isSuccess());
    resultNode.put("valid", output.isValid());

    if (output.getGshTemplateOutput() != null) {

      // validation lines
      List<GshValidationLine> validationLines = output.getGshTemplateOutput().getValidationLines();
      if (validationLines != null && !validationLines.isEmpty()) {
        ArrayNode validationArray = objectMapper.createArrayNode();
        for (GshValidationLine line : validationLines) {
          ObjectNode lineNode = objectMapper.createObjectNode();
          if (StringUtils.isNotBlank(line.getInputName())) {
            lineNode.put("inputName", line.getInputName());
          }
          lineNode.put("text", line.getText());
          validationArray.add(lineNode);
        }
        resultNode.set("validationLines", validationArray);
      }

      // output lines
      List<GshOutputLine> outputLines = output.getGshTemplateOutput().getOutputLines();
      if (outputLines != null && !outputLines.isEmpty()) {
        ArrayNode outputArray = objectMapper.createArrayNode();
        for (GshOutputLine line : outputLines) {
          ObjectNode lineNode = objectMapper.createObjectNode();
          lineNode.put("messageType", line.getMessageType());
          lineNode.put("text", line.getText());
          outputArray.add(lineNode);
        }
        resultNode.set("outputLines", outputArray);
      }

      // wsOutput
      if (output.getGshTemplateOutput().getWsOutput() != null) {
        try {
          String wsOutputJson = GrouperUtil.jsonConvertTo(output.getGshTemplateOutput().getWsOutput(), false);
          resultNode.set("wsOutput", objectMapper.readTree(wsOutputJson));
        } catch (Exception e) {
          resultNode.put("wsOutput", String.valueOf(output.getGshTemplateOutput().getWsOutput()));
        }
      }
    }

    if (output.getException() != null) {
      resultNode.put("error", output.getException().getMessage());
    }

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return output.isSuccess() ? buildSuccessResult(resultText) : buildErrorResult(resultText);
  }

  /**
   * resolve a default owner group or folder from the template config when no owner
   * was specified by the caller. uses the runButtonGroupOrFolder and
   * defaultRunButtonGroupUuidOrName / defaultRunButtonFolderUuidOrName config,
   * same as the UI run button.
   * @param templateConfig the template config
   * @param configId the template config id
   * @param exec the exec to assign the owner on
   * @return an error ObjectNode if no default could be resolved, or null on success
   */
  private static ObjectNode resolveDefaultOwner(GshTemplateConfig templateConfig, String configId, GshTemplateExec exec) {

    GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
    gshTemplateConfiguration.setConfigId(configId);

    String runButtonType = gshTemplateConfiguration.getDefaultRunButtonType();

    if ("group".equals(runButtonType)) {
      try {
        String groupId = gshTemplateConfiguration.getGroupId();
        Group group = GroupFinder.findByUuid(groupId, false);
        if (group != null) {
          exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
          exec.assignOwnerGroupName(group.getName());
          return null;
        }
      } catch (Exception e) {
        LOG.error("Error resolving default run button group for template: " + configId, e);
      }
    } else if ("folder".equals(runButtonType)) {
      try {
        String folderId = gshTemplateConfiguration.getFolderId();
        Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderId, false);
        if (stem != null) {
          exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
          exec.assignOwnerStemName(stem.isRootStem() ? ":" : stem.getName());
          return null;
        }
      } catch (Exception e) {
        LOG.error("Error resolving default run button folder for template: " + configId, e);
      }
    }

    // could not resolve a default
    StringBuilder message = new StringBuilder();
    message.append("This template requires a group or folder to execute on. ");
    if (templateConfig.isShowOnGroups()) {
      message.append("Provide ownerType='group' and ownerGroupName. ");
    }
    if (templateConfig.isShowOnFolders()) {
      message.append("Provide ownerType='stem' and ownerStemName. ");
    }
    return buildErrorResult(message.toString().trim());
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

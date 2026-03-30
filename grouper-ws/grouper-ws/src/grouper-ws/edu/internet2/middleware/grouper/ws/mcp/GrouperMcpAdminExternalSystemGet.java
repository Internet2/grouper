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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureApiCommands;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureUser;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxApiCommands;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxUser;
import edu.internet2.middleware.grouper.app.datadog.DatadogApiCommands;
import edu.internet2.middleware.grouper.app.datadog.DatadogSettings;
import edu.internet2.middleware.grouper.app.datadog.DatadogUser;
import edu.internet2.middleware.grouper.app.google.GoogleGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleApiCommands;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleUser;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoApiCommands;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoUser;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterApiCommands;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterUser;
import edu.internet2.middleware.grouper.app.remedy.RemedyDigitalMarketplaceGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.remedy.RemedyGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyApiCommands;
import edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyUser;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceApiCommands;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceUser;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimSettings;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixApiCommands;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixExternalSystem;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixUser;
import edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryApiCommands;
import edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryUser;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperDuo.DuoGrouperExternalSystem;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP admin tool for looking up users in configured external systems (Azure, Datadog, Duo, SCIM, Box, Google, Remedy, Remedy Digital Marketplace, TeamDynamix, FreshService Requesters, TrueFoundry).
 * Supports two actions:
 * <ul>
 *   <li>{@code listExternalSystems} - list external systems configured for MCP user lookups</li>
 *   <li>{@code getUser} - look up a user in an external system by translating a Grouper subject</li>
 * </ul>
 *
 * @author mchyzer
 */
public class GrouperMcpAdminExternalSystemGet {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminExternalSystemGet.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * pattern to match external system config IDs from config keys like
   * grouper.mcp.adminExternalSystem.&lt;id&gt;.subjectIdTranslationJexl
   */
  private static final Pattern EXTERNAL_SYSTEM_CONFIG_PATTERN = Pattern.compile(
      "^grouper\\.mcp\\.adminExternalSystem\\.([^.]+)\\.subjectIdTranslationJexl$");

  /**
   * return the MCP tool definition for admin_external_system_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_external_system_get");
    tool.put("description",
        "Look up users in external systems (Azure, Datadog, Duo, SCIM, Box, Google, Remedy, Remedy Digital Marketplace, TeamDynamix, FreshService Requesters, TrueFoundry) configured in Grouper. "
        + "Use action 'listExternalSystems' to discover which external systems are configured "
        + "for user lookups. "
        + "Use action 'getUser' with an externalSystemConfigId and a Grouper subjectIdOrIdentifier "
        + "to look up a user in the external system. The Grouper subject is translated to the "
        + "external system user identifier via a JEXL expression configured by the administrator.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("listExternalSystems");
    actionEnum.add("getUser");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The action to perform. 'listExternalSystems' returns external systems configured "
        + "for user lookups. 'getUser' looks up a user in an external system.");
    properties.set("action", actionProp);

    ObjectNode externalSystemConfigIdProp = objectMapper.createObjectNode();
    externalSystemConfigIdProp.put("type", "string");
    externalSystemConfigIdProp.put("description",
        "External system config ID. Required for 'getUser' action. "
        + "Use 'listExternalSystems' to discover available IDs.");
    properties.set("externalSystemConfigId", externalSystemConfigIdProp);

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "Grouper subject ID or identifier to look up in the external system. "
        + "Required for 'getUser' action. The subject is resolved in Grouper and then "
        + "translated to the external system user identifier via a JEXL expression.");
    properties.set("subjectIdOrIdentifier", subjectIdProp);

    ObjectNode subjectSourceIdProp = objectMapper.createObjectNode();
    subjectSourceIdProp.put("type", "string");
    subjectSourceIdProp.put("description",
        "Optional subject source ID to disambiguate subjects when multiple sources exist. "
        + "Only used with 'getUser' action.");
    properties.set("subjectSourceId", subjectSourceIdProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_external_system_get tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required. Use 'listExternalSystems' or 'getUser'.");
    }

    try {
      if ("listExternalSystems".equals(action)) {
        return listExternalSystems();
      }

      if ("getUser".equals(action)) {
        String externalSystemConfigId = arguments.has("externalSystemConfigId")
            ? arguments.get("externalSystemConfigId").asText() : null;
        String subjectIdOrIdentifier = arguments.has("subjectIdOrIdentifier")
            ? arguments.get("subjectIdOrIdentifier").asText() : null;
        String subjectSourceId = arguments.has("subjectSourceId")
            ? arguments.get("subjectSourceId").asText() : null;

        if (StringUtils.isBlank(externalSystemConfigId)) {
          return buildErrorResult("externalSystemConfigId is required for 'getUser' action. "
              + "Use 'listExternalSystems' to discover available IDs.");
        }
        if (StringUtils.isBlank(subjectIdOrIdentifier)) {
          return buildErrorResult("subjectIdOrIdentifier is required for 'getUser' action.");
        }

        return getUser(externalSystemConfigId, subjectIdOrIdentifier, subjectSourceId);
      }

      return buildErrorResult("Unknown action '" + action + "'. Use 'listExternalSystems' or 'getUser'.");

    } catch (Exception e) {
      LOG.error("Error in admin_external_system_get", e);
      return buildErrorResult("Error in admin_external_system_get: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
    }
  }

  /**
   * list all external systems configured for MCP user lookups by scanning config for
   * grouper.mcp.adminExternalSystem.&lt;id&gt;.subjectIdTranslationJexl properties.
   * @return the MCP tool result with the list of external systems
   */
  private static ObjectNode listExternalSystems() throws Exception {

    Set<String> configIds = findConfiguredExternalSystemIds();

    ArrayNode systemsArray = objectMapper.createArrayNode();
    for (String configId : configIds) {

      ObjectNode systemNode = objectMapper.createObjectNode();
      systemNode.put("configId", configId);

      String type = detectExternalSystemType(configId);
      systemNode.put("type", type != null ? type : "unknown");

      String lookupField = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.adminExternalSystem." + configId + ".externalSystemLookupField", "");
      if (StringUtils.isNotBlank(lookupField)) {
        systemNode.put("lookupField", lookupField);
      }

      String jexl = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.adminExternalSystem." + configId + ".subjectIdTranslationJexl", "");
      if (StringUtils.isNotBlank(jexl)) {
        systemNode.put("subjectIdTranslationJexl", jexl);
      }

      String documentation = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.adminExternalSystem." + configId + ".documentationForAiClient", "");
      if (StringUtils.isNotBlank(documentation)) {
        systemNode.put("documentation", documentation);
      }

      systemsArray.add(systemNode);
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("externalSystemCount", systemsArray.size());
    resultNode.set("externalSystems", systemsArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in an external system.
   * resolves the Grouper subject, translates via JEXL, queries the external system.
   * @param externalSystemConfigId the external system config ID
   * @param subjectIdOrIdentifier the Grouper subject ID or identifier
   * @param subjectSourceId optional subject source ID
   * @return the MCP tool result with the external system user data
   */
  private static ObjectNode getUser(String externalSystemConfigId,
      String subjectIdOrIdentifier, String subjectSourceId) throws Exception {

    // verify the external system has MCP config
    String jexl = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.adminExternalSystem." + externalSystemConfigId
            + ".subjectIdTranslationJexl");
    if (StringUtils.isBlank(jexl)) {
      return buildErrorResult("External system '" + externalSystemConfigId
          + "' does not have a subjectIdTranslationJexl configured. "
          + "Configure grouper.mcp.adminExternalSystem." + externalSystemConfigId
          + ".subjectIdTranslationJexl in grouper.properties.");
    }

    String lookupField = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.adminExternalSystem." + externalSystemConfigId
            + ".externalSystemLookupField");
    if (StringUtils.isBlank(lookupField)) {
      return buildErrorResult("External system '" + externalSystemConfigId
          + "' does not have an externalSystemLookupField configured. "
          + "Configure grouper.mcp.adminExternalSystem." + externalSystemConfigId
          + ".externalSystemLookupField in grouper.properties.");
    }

    // detect external system type
    String type = detectExternalSystemType(externalSystemConfigId);
    if (type == null) {
      return buildErrorResult("External system '" + externalSystemConfigId
          + "' could not be identified as a supported type (azure, datadog, duo, scim, box, google, remedy, remedyDigitalMarketplace, teamDynamix, freshserviceRequesters, trueFoundry). "
          + "Verify the external system connector is configured. For WsBearerToken-based systems "
          + "(SCIM, FreshService, Datadog, TrueFoundry) you may need to set grouper.mcp.adminExternalSystem."
          + externalSystemConfigId + ".externalSystemType");
    }

    // resolve the Grouper subject
    Subject subject;
    try {
      if (StringUtils.isNotBlank(subjectSourceId)) {
        subject = SubjectFinder.findByIdOrIdentifierAndSource(
            subjectIdOrIdentifier, subjectSourceId, false);
      } else {
        subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
      }
    } catch (Exception e) {
      return buildErrorResult("Error resolving subject '" + subjectIdOrIdentifier + "'"
          + (StringUtils.isNotBlank(subjectSourceId) ? " in source '" + subjectSourceId + "'" : "")
          + ": " + e.getMessage());
    }

    if (subject == null) {
      return buildErrorResult("Subject not found: '" + subjectIdOrIdentifier + "'"
          + (StringUtils.isNotBlank(subjectSourceId) ? " in source '" + subjectSourceId + "'" : "")
          + ". Verify the subject ID or identifier is correct.");
    }

    // translate subject to external system user ID via JEXL
    String externalUserId;
    try {
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("subject", subject);
      externalUserId = GrouperUtil.substituteExpressionLanguage(jexl, variableMap, true, true);
    } catch (Exception e) {
      return buildErrorResult("Error evaluating JEXL expression for subject '"
          + subjectIdOrIdentifier + "': " + e.getMessage()
          + "\nJEXL: " + jexl);
    }

    if (StringUtils.isBlank(externalUserId)) {
      return buildErrorResult("JEXL expression returned blank for subject '"
          + subjectIdOrIdentifier + "'. "
          + "JEXL: " + jexl);
    }

    // build result with metadata
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("externalSystemConfigId", externalSystemConfigId);
    resultNode.put("externalSystemType", type);
    resultNode.put("lookupField", lookupField);
    resultNode.put("translatedLookupValue", externalUserId);
    resultNode.put("grouperSubjectId", subject.getId());
    resultNode.put("grouperSubjectSourceId", subject.getSourceId());

    // query the external system
    if ("azure".equals(type)) {
      return getUserAzure(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("datadog".equals(type)) {
      return getUserDatadog(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("duo".equals(type)) {
      return getUserDuo(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("scim".equals(type)) {
      return getUserScim(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("box".equals(type)) {
      return getUserBox(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("google".equals(type)) {
      return getUserGoogle(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("remedy".equals(type)) {
      return getUserRemedy(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("remedyDigitalMarketplace".equals(type)) {
      return getUserRemedyDigitalMarketplace(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("teamDynamix".equals(type)) {
      return getUserTeamDynamix(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("freshserviceRequesters".equals(type)) {
      return getUserFreshserviceRequesters(externalSystemConfigId, lookupField, externalUserId, resultNode);
    } else if ("trueFoundry".equals(type)) {
      return getUserTrueFoundry(externalSystemConfigId, lookupField, externalUserId, resultNode);
    }

    return buildErrorResult("Unsupported external system type: " + type);
  }

  /**
   * look up a user in Azure.
   * First finds the user via the provisioner API to get basic fields and confirm the user exists,
   * then makes a direct Graph API call to retrieve extended attributes (mail, userType,
   * onPremisesSamAccountName, proxyAddresses, licenseDetails, etc.) that the provisioner
   * does not return.
   *
   * TODO: when the provisioner API (GrouperAzureApiCommands) supports retrieving extended
   * user attributes, remove the direct Graph call here and use the provisioner API only.
   */
  private static ObjectNode getUserAzure(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    // step 1: use provisioner API to find the user and get their Azure id
    List<GrouperAzureUser> users = GrouperAzureApiCommands.retrieveAzureUsers(
        configId, Collections.singletonList(lookupValue), lookupField);

    if (users == null || users.isEmpty()) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    GrouperAzureUser azureUser = users.get(0);
    resultNode.put("userFound", true);

    // step 2: make a direct Graph API call with more $select fields
    // TODO: move this to GrouperAzureApiCommands when it supports extended attributes
    try {
      String bearerToken = AzureGrouperExternalSystem.retrieveBearerTokenForAzureConfigId(
          new HashMap<String, Object>(), configId);
      String resourceEndpoint = GrouperLoaderConfig.retrieveConfig()
          .propertyValueStringRequired("grouper.azureConnector." + configId + ".resourceEndpoint");

      // look up by the user's Azure id for a direct /users/{id} call
      String userId = azureUser.getId();
      String url = resourceEndpoint + (resourceEndpoint.endsWith("/") ? "" : "/")
          + "users/" + GrouperUtil.escapeUrlEncode(userId)
          + "?$select=accountEnabled,displayName,id,mail,mailNickname,"
          + "onPremisesImmutableId,onPremisesLastSyncDateTime,onPremisesSamAccountName,"
          + "proxyAddresses,showInAddressList,userPrincipalName,userType";

      GrouperHttpClient httpClient = new GrouperHttpClient();
      httpClient.assignUrl(url);
      httpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      httpClient.addHeader("Content-Type", "application/json");
      httpClient.addHeader("Authorization", "Bearer " + bearerToken);

      httpClient.executeRequest();
      int code = httpClient.getResponseCode();

      if (code == 200) {
        JsonNode extendedUserNode = objectMapper.readTree(httpClient.getResponseBody());

        // merge extended attributes into the provisioner user JSON
        ObjectNode userNode = (ObjectNode) azureUser.toJson(null);
        // add fields not in the provisioner's fieldsToSelect
        if (extendedUserNode.has("mail")) {
          userNode.set("mail", extendedUserNode.get("mail"));
        }
        if (extendedUserNode.has("userType")) {
          userNode.set("userType", extendedUserNode.get("userType"));
        }
        if (extendedUserNode.has("onPremisesSamAccountName")) {
          userNode.set("onPremisesSamAccountName", extendedUserNode.get("onPremisesSamAccountName"));
        }
        if (extendedUserNode.has("onPremisesLastSyncDateTime")) {
          userNode.set("onPremisesLastSyncDateTime", extendedUserNode.get("onPremisesLastSyncDateTime"));
        }
        if (extendedUserNode.has("proxyAddresses")) {
          userNode.set("proxyAddresses", extendedUserNode.get("proxyAddresses"));
        }
        if (extendedUserNode.has("showInAddressList")) {
          userNode.set("showInAddressList", extendedUserNode.get("showInAddressList"));
        }
        resultNode.set("user", userNode);

        // step 3: get license details
        // TODO: move this to GrouperAzureApiCommands
        try {
          String licenseUrl = resourceEndpoint + (resourceEndpoint.endsWith("/") ? "" : "/")
              + "users/" + GrouperUtil.escapeUrlEncode(userId) + "/licenseDetails";

          GrouperHttpClient licenseClient = new GrouperHttpClient();
          licenseClient.assignUrl(licenseUrl);
          licenseClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
          licenseClient.addHeader("Content-Type", "application/json");
          licenseClient.addHeader("Authorization", "Bearer " + bearerToken);

          licenseClient.executeRequest();
          int licenseCode = licenseClient.getResponseCode();

          if (licenseCode == 200) {
            JsonNode licenseNode = objectMapper.readTree(licenseClient.getResponseBody());
            JsonNode licenseValue = licenseNode.get("value");
            if (licenseValue != null) {
              userNode.set("licenseDetails", licenseValue);
            }
          }
        } catch (Exception e) {
          // license details are optional, don't fail the whole request
          LOG.warn("Could not retrieve Azure license details for user " + userId + " in configId " + configId, e);
        }
      } else {
        // extended call failed, fall back to provisioner data only
        resultNode.set("user", azureUser.toJson(null));
      }
    } catch (Exception e) {
      // extended call failed, fall back to provisioner data only
      LOG.warn("Could not retrieve extended Azure attributes for configId " + configId, e);
      resultNode.set("user", azureUser.toJson(null));
    }

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Duo
   */
  private static ObjectNode getUserDuo(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    GrouperDuoUser duoUser = null;
    if ("id".equals(lookupField)) {
      duoUser = GrouperDuoApiCommands.retrieveDuoUser(configId, lookupValue);
    } else {
      // default to username lookup
      duoUser = GrouperDuoApiCommands.retrieveDuoUserByName(configId, lookupValue);
    }

    if (duoUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);

    // build user JSON from Duo user fields
    ObjectNode userNode = objectMapper.createObjectNode();
    if (duoUser.getId() != null) {
      userNode.put("user_id", duoUser.getId());
    }
    if (duoUser.getUserName() != null) {
      userNode.put("username", duoUser.getUserName());
    }
    if (duoUser.getEmail() != null) {
      userNode.put("email", duoUser.getEmail());
    }
    if (duoUser.getFirstName() != null) {
      userNode.put("firstname", duoUser.getFirstName());
    }
    if (duoUser.getLastName() != null) {
      userNode.put("lastname", duoUser.getLastName());
    }
    if (duoUser.getRealName() != null) {
      userNode.put("realname", duoUser.getRealName());
    }
    if (duoUser.getStatus() != null) {
      userNode.put("status", duoUser.getStatus());
    }
    if (duoUser.getAlias1() != null) {
      userNode.put("alias1", duoUser.getAlias1());
    }
    if (duoUser.getAlias2() != null) {
      userNode.put("alias2", duoUser.getAlias2());
    }
    if (duoUser.getAlias3() != null) {
      userNode.put("alias3", duoUser.getAlias3());
    }
    if (duoUser.getAlias4() != null) {
      userNode.put("alias4", duoUser.getAlias4());
    }
    resultNode.set("user", userNode);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Datadog by email
   */
  private static ObjectNode getUserDatadog(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    DatadogSettings datadogSettings = new DatadogSettings();

    DatadogUser datadogUser = null;
    if ("email".equals(lookupField)) {
      datadogUser = DatadogApiCommands.retrieveUserByEmail(configId, datadogSettings, lookupValue, true);
    } else {
      // default to email lookup
      datadogUser = DatadogApiCommands.retrieveUserByEmail(configId, datadogSettings, lookupValue, true);
    }

    if (datadogUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", datadogUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in SCIM
   */
  private static ObjectNode getUserScim(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    ScimSettings scimSettings = new ScimSettings();
    GrouperScim2User scimUser = GrouperScim2ApiCommands.retrieveScimUser(
        configId, lookupField, lookupValue, null, scimSettings);

    if (scimUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", scimUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Box
   */
  private static ObjectNode getUserBox(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    Set<String> allAttributes = GrouperBoxUser.grouperBoxUserToBoxSpecificAttributeNames.keySet();
    GrouperBoxUser boxUser = null;
    if ("id".equals(lookupField)) {
      boxUser = GrouperBoxApiCommands.retrieveBoxUser(configId, lookupValue, allAttributes);
    } else {
      // login or name: use filter term search and match exactly
      List<GrouperBoxUser> boxUsers = GrouperBoxApiCommands.retrieveBoxUsers(
          configId, lookupValue, allAttributes);
      if (boxUsers != null) {
        for (GrouperBoxUser candidate : boxUsers) {
          if ("login".equals(lookupField) && StringUtils.equals(candidate.getLogin(), lookupValue)) {
            boxUser = candidate;
            break;
          }
          if ("name".equals(lookupField) && StringUtils.equals(candidate.getName(), lookupValue)) {
            boxUser = candidate;
            break;
          }
        }
      }
    }

    if (boxUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", boxUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Google. the Google API accepts either user ID or primaryEmail
   * as the lookup key in retrieveGoogleUser.
   */
  private static ObjectNode getUserGoogle(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    // Google API retrieves by id or primaryEmail (both work as the userId parameter)
    GrouperGoogleUser googleUser = GrouperGoogleApiCommands.retrieveGoogleUser(
        configId, lookupValue);

    if (googleUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", googleUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Remedy. The V2 API retrieves by login ID.
   */
  private static ObjectNode getUserRemedy(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    GrouperRemedyUser remedyUser = GrouperRemedyApiCommands.retrieveRemedyUser(
        configId, lookupValue);

    if (remedyUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", remedyUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in Remedy Digital Marketplace. Retrieves by login name.
   */
  private static ObjectNode getUserRemedyDigitalMarketplace(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    GrouperDigitalMarketplaceUser dmUser = GrouperDigitalMarketplaceApiCommands
        .retrieveDigitalMarketplaceUser(configId, lookupValue);

    if (dmUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", dmUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in TeamDynamix. Supports lookup by id, externalId, or username.
   */
  private static ObjectNode getUserTeamDynamix(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    TeamDynamixUser tdxUser = null;
    if ("id".equals(lookupField)) {
      tdxUser = TeamDynamixApiCommands.retrieveTeamDynamixUser(configId, lookupValue);
    } else {
      // externalId or username
      tdxUser = TeamDynamixApiCommands.retrieveTeamDynamixUserBySearchTerm(
          configId, lookupField, lookupValue, null);
    }

    if (tdxUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", tdxUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in FreshService Requesters
   */
  private static ObjectNode getUserFreshserviceRequesters(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    FreshRequesterUser freshUser = null;
    if ("id".equals(lookupField)) {
      freshUser = FreshRequesterApiCommands.retrieveRequesterUserById(
          configId, Long.parseLong(lookupValue), false);
    } else if ("email".equals(lookupField)) {
      freshUser = FreshRequesterApiCommands.retrieveRequesterUserByEmail(
          configId, lookupValue, false);
    } else {
      // externalId or custom fields
      freshUser = FreshRequesterApiCommands.retrieveRequesterUserByAttribute(
          configId, lookupField, lookupValue);
    }

    if (freshUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", freshUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * look up a user in TrueFoundry by email.
   * TrueFoundry is email-based so email is the only supported lookup field.
   */
  private static ObjectNode getUserTrueFoundry(String configId, String lookupField,
      String lookupValue, ObjectNode resultNode) throws Exception {

    TrueFoundryUser trueFoundryUser = TrueFoundryApiCommands.retrieveUserByEmail(
        configId, lookupValue, true);

    if (trueFoundryUser == null) {
      resultNode.put("userFound", false);
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);
    }

    resultNode.put("userFound", true);
    resultNode.set("user", trueFoundryUser.toJson(null));

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * find all external system config IDs that have MCP configuration
   * by scanning config for grouper.mcp.adminExternalSystem.&lt;id&gt;.subjectIdTranslationJexl
   * @return ordered set of config IDs
   */
  private static Set<String> findConfiguredExternalSystemIds() {
    Set<String> configIds = new LinkedHashSet<String>();

    Set<String> propertyNames = GrouperConfig.retrieveConfig().propertyNames();
    for (String key : propertyNames) {
      Matcher matcher = EXTERNAL_SYSTEM_CONFIG_PATTERN.matcher(key);
      if (matcher.matches()) {
        String id = matcher.group(1);
        String jexlValue = GrouperConfig.retrieveConfig().propertyValueString(key, "");
        if (StringUtils.isNotBlank(jexlValue)) {
          configIds.add(id);
        }
      }
    }

    return configIds;
  }

  /**
   * detect the type of external system (azure, datadog, duo, scim, box, freshserviceRequesters, trueFoundry)
   * by checking if an explicit type is configured, or by matching against configured connectors.
   * An explicit type is needed when auto-detection is ambiguous (e.g. SCIM, FreshService, Datadog,
   * and TrueFoundry all use WsBearerTokenExternalSystem).
   * @param configId the external system config ID
   * @return "azure", "datadog", "duo", "scim", "box", "freshserviceRequesters", "trueFoundry", or null if not detected
   */
  static String detectExternalSystemType(String configId) {

    // check for explicit type override first (needed for WsBearerToken ambiguity)
    String explicitType = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.adminExternalSystem." + configId + ".externalSystemType");
    if (StringUtils.isNotBlank(explicitType)) {
      return explicitType;
    }

    List<GrouperExternalSystem> allSystems = GrouperExternalSystem.retrieveAllGrouperExternalSystems();
    for (GrouperExternalSystem externalSystem : allSystems) {
      if (StringUtils.equals(configId, externalSystem.getConfigId())) {
        if (externalSystem instanceof AzureGrouperExternalSystem) {
          return "azure";
        }
        if (externalSystem instanceof DuoGrouperExternalSystem) {
          return "duo";
        }
        if (externalSystem instanceof BoxGrouperExternalSystem) {
          return "box";
        }
        if (externalSystem instanceof GoogleGrouperExternalSystem) {
          return "google";
        }
        if (externalSystem instanceof RemedyGrouperExternalSystem) {
          return "remedy";
        }
        if (externalSystem instanceof RemedyDigitalMarketplaceGrouperExternalSystem) {
          return "remedyDigitalMarketplace";
        }
        if (externalSystem instanceof TeamDynamixExternalSystem) {
          return "teamDynamix";
        }
        if (externalSystem instanceof WsBearerTokenExternalSystem) {
          return "scim";
        }
      }
    }

    return null;
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

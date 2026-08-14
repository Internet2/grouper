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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireConfigBean;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireEngine;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter;

/**
 * MCP tool handler for finding Grouper groups.
 * Supports searching by name (exact or approximate), by stem, by attribute,
 * with paging and sorting.
 *
 * <p>Delegates to {@link GrouperServiceLogic#findGroups} for the actual search,
 * then optionally enriches results with Grouper object type names (e.g., policy, ref,
 * basis, manual) via {@link GrouperObjectTypesConfiguration}.</p>
 *
 * <p>The object types are not part of the WS response, so when includeGdgTypes is true,
 * we do a second lookup: re-fetch the Group objects by name, then batch-retrieve their
 * type attributes. This adds some overhead but avoids exposing the underlying attribute
 * framework complexity to the MCP client.</p>
 *
 * <p>Similarly, when includeGroupEligibilityRequirement is true, we use
 * {@link MembershipRequireEngine} to look up membership eligibility requirements
 * (e.g., requireEmployee, requireAffiliate) configured via
 * grouper.membershipRequirement.* properties. These requirements restrict who can
 * be a member of the group (members must also be in a specified population group).
 * The result is returned as a comma-separated string of configIds.</p>
 *
 * <p>When includeCompositeInfo is true, we check each group to see if it is a
 * composite (factor) group. A composite group's membership is defined by a set
 * operation (union, intersection, or complement) on two other groups (the left
 * and right factors). If a group is composite, we include the composite type and
 * the names of the left and right factor groups in the response.</p>
 *
 * <p>When includeProvisioning is true, we look up which provisioning targets
 * (e.g., LDAP, Active Directory, Google) are actively provisioning each group.
 * Uses {@link GrouperProvisioningService#getProvisioningAttributeValues} to get
 * provisioning config, filters to targets where doProvision is set, and checks
 * {@link GrouperProvisioningService#isTargetViewable} to ensure the authenticated
 * user has VIEW privilege on each target (either WHEEL/ROOT/VIEWONLY_ROOT, or
 * membership in the target's groupAllowedToView group).</p>
 *
 * @author mchyzer
 */
public class GrouperMcpFindGroups {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpFindGroups.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Return the MCP tool definition for group_find.
   * This builds the JSON Schema that describes the tool's input parameters
   * to the MCP client (e.g., an AI model).
   * @return the tool definition as a Jackson ObjectNode conforming to the MCP tool schema
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_find");
    tool.put("description",
        "Search for Grouper groups by name, stem, or attribute. "
        + "Supports exact and approximate name matching, "
        + "searching within a specific stem, and paging/sorting.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode queryFilterTypeProp = objectMapper.createObjectNode();
    queryFilterTypeProp.put("type", "string");
    ArrayNode qftEnum = objectMapper.createArrayNode();
    qftEnum.add("FIND_BY_GROUP_NAME_EXACT");
    qftEnum.add("FIND_BY_GROUP_NAME_APPROXIMATE");
    qftEnum.add("FIND_BY_STEM_NAME");
    qftEnum.add("FIND_BY_GROUP_UUID");
    qftEnum.add("FIND_BY_APPROXIMATE_ATTRIBUTE");
    queryFilterTypeProp.set("enum", qftEnum);
    queryFilterTypeProp.put("description",
        "Type of search to perform. "
        + "FIND_BY_GROUP_NAME_EXACT = exact name match, "
        + "FIND_BY_GROUP_NAME_APPROXIMATE = approximate name match (most common), "
        + "FIND_BY_STEM_NAME = all groups in a stem, "
        + "FIND_BY_GROUP_UUID = find by UUID, "
        + "FIND_BY_APPROXIMATE_ATTRIBUTE = search by attribute value.");
    properties.set("queryFilterType", queryFilterTypeProp);

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "Group name to search for. Used with FIND_BY_GROUP_NAME_EXACT "
        + "or FIND_BY_GROUP_NAME_APPROXIMATE.");
    properties.set("groupName", groupNameProp);

    ObjectNode groupUuidProp = objectMapper.createObjectNode();
    groupUuidProp.put("type", "string");
    groupUuidProp.put("description",
        "Group UUID. Used with FIND_BY_GROUP_UUID.");
    properties.set("groupUuid", groupUuidProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Stem name to search within. Used with FIND_BY_STEM_NAME, "
        + "or to scope a FIND_BY_GROUP_NAME_APPROXIMATE search.");
    properties.set("stemName", stemNameProp);

    ObjectNode stemNameScopeProp = objectMapper.createObjectNode();
    stemNameScopeProp.put("type", "string");
    ArrayNode scopeEnum = objectMapper.createArrayNode();
    scopeEnum.add("ONE_LEVEL");
    scopeEnum.add("ALL_IN_SUBTREE");
    stemNameScopeProp.set("enum", scopeEnum);
    stemNameScopeProp.put("description",
        "Scope when searching in a stem. ONE_LEVEL = direct children only, "
        + "ALL_IN_SUBTREE = all descendants (default).");
    properties.set("stemNameScope", stemNameScopeProp);

    ObjectNode groupAttributeValueProp = objectMapper.createObjectNode();
    groupAttributeValueProp.put("type", "string");
    groupAttributeValueProp.put("description",
        "Attribute value to search for. Used with FIND_BY_APPROXIMATE_ATTRIBUTE.");
    properties.set("groupAttributeValue", groupAttributeValueProp);

    ObjectNode typeOfGroupsProp = objectMapper.createObjectNode();
    typeOfGroupsProp.put("type", "string");
    typeOfGroupsProp.put("description",
        "Comma-separated types of groups to return: group, role, entity. "
        + "Default is all types.");
    properties.set("typeOfGroups", typeOfGroupsProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of results per page. Default is 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number (1-indexed). Default is 1.");
    pageNumberProp.put("default", 1);
    properties.set("pageNumber", pageNumberProp);

    ObjectNode sortStringProp = objectMapper.createObjectNode();
    sortStringProp.put("type", "string");
    sortStringProp.put("description",
        "Field to sort by: name, displayName, extension, displayExtension.");
    properties.set("sortString", sortStringProp);

    ObjectNode ascendingProp = objectMapper.createObjectNode();
    ascendingProp.put("type", "boolean");
    ascendingProp.put("description",
        "Sort ascending (true, default) or descending (false).");
    properties.set("ascending", ascendingProp);

    ObjectNode includeGdgTypesProp = objectMapper.createObjectNode();
    includeGdgTypesProp.put("type", "boolean");
    includeGdgTypesProp.put("description",
        "If true, include Grouper Deployment Guide (GDG) type names (e.g., policy, ref, basis, manual, app, org, test, service, readOnly, etc.) "
        + "for each group in the results. These are different from typeOfGroups (group, role, entity) which is a structural classification. Defaults to false.");
    properties.set("includeGdgTypes", includeGdgTypesProp);

    ObjectNode includeEligibilityProp = objectMapper.createObjectNode();
    includeEligibilityProp.put("type", "boolean");
    includeEligibilityProp.put("description",
        "If true, include membership eligibility requirements "
        + "(e.g., requireEmployee, requireAffiliate) for each group in the results. "
        + "These are configured requirements that restrict who can be added as a member "
        + "(the member must also belong to a specified population group). "
        + "Returned as a comma-separated string of requirement configIds. Defaults to false.");
    properties.set("includeGroupEligibilityRequirement", includeEligibilityProp);

    ObjectNode includeProvisioningProp = objectMapper.createObjectNode();
    includeProvisioningProp.put("type", "boolean");
    includeProvisioningProp.put("description",
        "If true, include provisioning target names for each group that is being "
        + "provisioned to external systems (e.g., LDAP, Active Directory, Google). "
        + "Only targets the authenticated user is allowed to view are returned. "
        + "Returned as a comma-separated string. Defaults to false.");
    properties.set("includeProvisioning", includeProvisioningProp);

    ObjectNode includeCompositeInfoProp = objectMapper.createObjectNode();
    includeCompositeInfoProp.put("type", "boolean");
    includeCompositeInfoProp.put("description",
        "If true, include composite (factor) information for each group. "
        + "If a group is a composite group, the response will include "
        + "compositeType (union, intersection, or complement) and the names "
        + "of the left and right factor groups. Defaults to false.");
    properties.set("includeCompositeInfo", includeCompositeInfoProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("queryFilterType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * Execute the group_find tool by delegating to the WS service logic.
   *
   * <p>Flow:
   * 1. Parse and validate input arguments from the MCP request
   * 2. Build a WsQueryFilter and call GrouperServiceLogic.findGroups()
   * 3. If includeGdgTypes is requested, do a secondary lookup to fetch
   *    Grouper object type attributes (policy, ref, basis, etc.) for each group
   * 4. If includeGroupEligibilityRequirement is requested, use MembershipRequireEngine
   *    to look up membership requirement configIds for each group
   * 5. If includeProvisioning is requested, look up provisioning targets for each group
   *    and filter by the user's view privilege on each target
   * 6. If includeCompositeInfo is requested, check each group for composite ownership
   *    and include composite type and left/right factor group names
   * 7. Build a clean JSON response with group details and optional enrichment info</p>
   *
   * @param arguments the tool arguments from the MCP request (JSON object)
   * @param authUser the authenticated user (used for access control upstream)
   * @return the MCP tool result containing group data or an error message
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    // parse all input parameters from the MCP request arguments
    String queryFilterType = arguments != null && arguments.has("queryFilterType")
        ? arguments.get("queryFilterType").asText() : null;
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String groupUuid = arguments != null && arguments.has("groupUuid")
        ? arguments.get("groupUuid").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String stemNameScope = arguments != null && arguments.has("stemNameScope")
        ? arguments.get("stemNameScope").asText() : null;
    String groupAttributeValue = arguments != null && arguments.has("groupAttributeValue")
        ? arguments.get("groupAttributeValue").asText() : null;
    String typeOfGroups = arguments != null && arguments.has("typeOfGroups")
        ? arguments.get("typeOfGroups").asText() : null;
    int pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt(50) : 50;
    int pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt(1) : 1;
    String sortString = arguments != null && arguments.has("sortString")
        ? arguments.get("sortString").asText() : null;
    String ascending = arguments != null && arguments.has("ascending")
        ? (arguments.get("ascending").asBoolean(true) ? "T" : "F") : null;
    boolean includeGdgTypes = arguments != null && arguments.has("includeGdgTypes")
        && arguments.get("includeGdgTypes").asBoolean(false);
    boolean includeGroupEligibilityRequirement = arguments != null
        && arguments.has("includeGroupEligibilityRequirement")
        && arguments.get("includeGroupEligibilityRequirement").asBoolean(false);
    boolean includeProvisioning = arguments != null
        && arguments.has("includeProvisioning")
        && arguments.get("includeProvisioning").asBoolean(false);
    boolean includeCompositeInfo = arguments != null
        && arguments.has("includeCompositeInfo")
        && arguments.get("includeCompositeInfo").asBoolean(false);

    if (StringUtils.isBlank(queryFilterType)) {
      return buildErrorResult("queryFilterType is required.");
    }

    try {

      // build the WS query filter from the MCP arguments
      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.setQueryFilterType(queryFilterType);

      if (StringUtils.isNotBlank(groupName)) {
        wsQueryFilter.setGroupName(groupName);
      }
      if (StringUtils.isNotBlank(groupUuid)) {
        wsQueryFilter.setGroupUuid(groupUuid);
      }
      if (StringUtils.isNotBlank(stemName)) {
        wsQueryFilter.setStemName(stemName);
      }
      if (StringUtils.isNotBlank(stemNameScope)) {
        wsQueryFilter.setStemNameScope(stemNameScope);
      }
      if (StringUtils.isNotBlank(groupAttributeValue)) {
        wsQueryFilter.setGroupAttributeValue(groupAttributeValue);
      }
      if (StringUtils.isNotBlank(typeOfGroups)) {
        wsQueryFilter.setTypeOfGroups(typeOfGroups);
      }
      // only set paging params for query types that support it
      // (exact name and UUID lookups return at most one result and reject paging)
      boolean supportsPaging = !"FIND_BY_GROUP_NAME_EXACT".equals(queryFilterType)
          && !"FIND_BY_GROUP_UUID".equals(queryFilterType);
      if (supportsPaging) {
        wsQueryFilter.setPageSize(String.valueOf(pageSize));
        wsQueryFilter.setPageNumber(String.valueOf(pageNumber));
      }
      if (StringUtils.isNotBlank(sortString)) {
        wsQueryFilter.setSortString(sortString);
      }
      if (StringUtils.isNotBlank(ascending)) {
        wsQueryFilter.setAscending(ascending);
      }

      WsFindGroupsResults wsResults = GrouperServiceLogic.findGroups(
          GrouperVersion.currentVersion(),
          wsQueryFilter,
          null,   // actAsSubjectLookup
          false,  // includeGroupDetail
          null,   // params
          null    // wsGroupLookups
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      ObjectNode resultNode = objectMapper.createObjectNode();
      WsGroup[] groups = wsResults.getGroupResults();
      int groupCount = GrouperUtil.length(groups);
      resultNode.put("totalGroupsReturned", groupCount);
      resultNode.put("pageSize", pageSize);
      resultNode.put("pageNumber", pageNumber);

      // If includeGdgTypes or includeProvisioning is requested, we need to re-fetch
      // the actual Group objects from the database (the WS only returns WsGroup DTOs).
      // We fetch them once and share across both lookups to avoid duplicate queries.
      Map<String, Group> groupObjectsByName = null;
      if ((includeGdgTypes || includeProvisioning || includeCompositeInfo) && groupCount > 0) {
        Set<String> groupNames = new HashSet<>();
        for (WsGroup wsGroup : groups) {
          if (StringUtils.isNotBlank(wsGroup.getName())) {
            groupNames.add(wsGroup.getName());
          }
        }
        if (groupNames.size() > 0) {
          Set<Group> groupObjects = new GroupFinder().assignGroupNames(groupNames).findGroups();
          groupObjectsByName = new HashMap<>();
          for (Group g : groupObjects) {
            groupObjectsByName.put(g.getName(), g);
          }
        }
      }

      // Optionally look up Grouper object types (policy, ref, basis, manual, etc.)
      // for each group. Uses the GrouperObjectTypesConfiguration API to batch-retrieve
      // the type attributes. The result is a map from group name to a list of type values,
      // which we later add as an "gdgTypes" array on each group.
      Map<String, List<GrouperObjectTypesAttributeValue>> groupNameToTypes = null;
      if (includeGdgTypes && groupObjectsByName != null && groupObjectsByName.size() > 0) {

        // batch-retrieve object type attributes for all groups at once
        Map<GrouperObject, List<GrouperObjectTypesAttributeValue>> typesMap =
            GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(groupObjectsByName.values());

        // convert to a name-keyed map for easy lookup when building the response
        groupNameToTypes = new HashMap<>();
        for (Map.Entry<GrouperObject, List<GrouperObjectTypesAttributeValue>> entry : typesMap.entrySet()) {
          if (entry.getKey() instanceof Group) {
            groupNameToTypes.put(((Group) entry.getKey()).getName(), entry.getValue());
          }
        }
      }

      // Optionally look up membership eligibility requirements for each group.
      // These are configured via grouper.membershipRequirement.* properties and restrict
      // who can be a member (e.g., requireEmployee means members must be in the employee group).
      // MembershipRequireEngine.groupNameToConfigBeanAssigned() checks both direct group-level
      // and inherited stem-level attribute assignments. Results are cached for 5 minutes.
      // We build a map from group name to a comma-separated string of requirement configIds.
      Map<String, String> groupNameToEligibility = null;
      if (includeGroupEligibilityRequirement && groupCount > 0) {
        groupNameToEligibility = new HashMap<>();
        for (WsGroup wsGroup : groups) {
          if (StringUtils.isNotBlank(wsGroup.getName())) {
            Set<MembershipRequireConfigBean> configBeans =
                MembershipRequireEngine.groupNameToConfigBeanAssigned(wsGroup.getName());
            if (configBeans != null && configBeans.size() > 0) {
              StringBuilder sb = new StringBuilder();
              for (MembershipRequireConfigBean configBean : configBeans) {
                if (StringUtils.isNotBlank(configBean.getConfigId())) {
                  if (sb.length() > 0) {
                    sb.append(", ");
                  }
                  sb.append(configBean.getConfigId());
                }
              }
              if (sb.length() > 0) {
                groupNameToEligibility.put(wsGroup.getName(), sb.toString());
              }
            }
          }
        }
      }

      // Optionally look up which provisioning targets are actively provisioning each group.
      // Uses GrouperProvisioningService.getProvisioningAttributeValues() to get all provisioning
      // config for a group, then filters to targets where doProvision is set (actively provisioning).
      // Each target is also checked with isTargetViewable() to respect the user's view privilege
      // (WHEEL/ROOT/VIEWONLY_ROOT or membership in the target's groupAllowedToView group).
      // We pre-load the targets map once to avoid repeated lookups.
      Map<String, String> groupNameToProvisioning = null;
      if (includeProvisioning && groupObjectsByName != null && groupObjectsByName.size() > 0) {
        groupNameToProvisioning = new HashMap<>();

        // load all configured provisioning targets once
        Map<String, GrouperProvisioningTarget> targets = GrouperProvisioningSettings.getTargets(true);

        for (Map.Entry<String, Group> entry : groupObjectsByName.entrySet()) {
          Group groupObject = entry.getValue();

          // get all provisioning attribute values for this group (direct and inherited)
          List<GrouperProvisioningAttributeValue> provValues =
              GrouperProvisioningService.getProvisioningAttributeValues(groupObject);

          if (provValues != null && provValues.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (GrouperProvisioningAttributeValue provValue : provValues) {

              // only include targets that are actively provisioning this group
              if (provValue.isDoProvision() && StringUtils.isNotBlank(provValue.getTargetName())) {

                // check if the authenticated user is allowed to view this target
                GrouperProvisioningTarget target = targets.get(provValue.getTargetName());
                if (target != null
                    && GrouperProvisioningService.isTargetViewable(target, authUser.getSubject(), groupObject)) {
                  if (sb.length() > 0) {
                    sb.append(", ");
                  }
                  sb.append(provValue.getTargetName());
                }
              }
            }
            if (sb.length() > 0) {
              groupNameToProvisioning.put(entry.getKey(), sb.toString());
            }
          }
        }
      }

      // Optionally look up composite (factor) information for each group.
      // A composite group is one whose membership is defined by a set operation
      // (union, intersection, or complement) on two other groups (left and right factors).
      // Uses Group.getComposite(false) to check if a group is a composite owner,
      // and if so retrieves the composite type and the left/right factor group names.
      Map<String, ObjectNode> groupNameToCompositeInfo = null;
      if (includeCompositeInfo && groupObjectsByName != null && groupObjectsByName.size() > 0) {
        groupNameToCompositeInfo = new HashMap<>();
        for (Map.Entry<String, Group> entry : groupObjectsByName.entrySet()) {
          Group groupObject = entry.getValue();
          Composite composite = groupObject.getComposite(false);
          if (composite != null) {
            ObjectNode compositeNode = objectMapper.createObjectNode();
            compositeNode.put("compositeType", composite.getType().toString());
            try {
              compositeNode.put("leftFactorGroupName", composite.getLeftGroup().getName());
            } catch (Exception e) {
              LOG.warn("Could not resolve left factor group for composite on group: "
                  + entry.getKey(), e);
            }
            try {
              compositeNode.put("rightFactorGroupName", composite.getRightGroup().getName());
            } catch (Exception e) {
              LOG.warn("Could not resolve right factor group for composite on group: "
                  + entry.getKey(), e);
            }
            groupNameToCompositeInfo.put(entry.getKey(), compositeNode);
          }
        }
      }

      // build the response array with each group's details
      ArrayNode groupsArray = objectMapper.createArrayNode();
      if (groupCount > 0) {
        for (WsGroup group : groups) {
          ObjectNode groupNode = objectMapper.createObjectNode();
          groupNode.put("name", group.getName());
          if (StringUtils.isNotBlank(group.getDisplayName())) {
            groupNode.put("displayName", group.getDisplayName());
          }
          if (StringUtils.isNotBlank(group.getExtension())) {
            groupNode.put("extension", group.getExtension());
          }
          if (StringUtils.isNotBlank(group.getDescription())) {
            groupNode.put("description", group.getDescription());
          }
          if (StringUtils.isNotBlank(group.getUuid())) {
            groupNode.put("uuid", group.getUuid());
          }
          if (StringUtils.isNotBlank(group.getTypeOfGroup())) {
            groupNode.put("typeOfGroup", group.getTypeOfGroup());
          }
          // append object type names (e.g., "policy", "ref") if types were requested and found
          if (groupNameToTypes != null && StringUtils.isNotBlank(group.getName())) {
            List<GrouperObjectTypesAttributeValue> typeValues = groupNameToTypes.get(group.getName());
            if (typeValues != null && typeValues.size() > 0) {
              ArrayNode typesArray = objectMapper.createArrayNode();
              for (GrouperObjectTypesAttributeValue typeValue : typeValues) {
                if (StringUtils.isNotBlank(typeValue.getObjectTypeName())) {
                  typesArray.add(typeValue.getObjectTypeName());
                }
              }
              if (typesArray.size() > 0) {
                groupNode.set("gdgTypes", typesArray);
              }
            }
          }
          // append eligibility requirement configIds (e.g., "requireEmployee") if requested
          if (groupNameToEligibility != null && StringUtils.isNotBlank(group.getName())) {
            String eligibility = groupNameToEligibility.get(group.getName());
            if (StringUtils.isNotBlank(eligibility)) {
              groupNode.put("eligibilityRequirement", eligibility);
            }
          }
          // append provisioning target names (e.g., "ldapProvisioner") if requested and viewable
          if (groupNameToProvisioning != null && StringUtils.isNotBlank(group.getName())) {
            String provisioning = groupNameToProvisioning.get(group.getName());
            if (StringUtils.isNotBlank(provisioning)) {
              groupNode.put("provisioning", provisioning);
            }
          }
          // append composite info (compositeType, leftFactorGroupName, rightFactorGroupName)
          // if requested and the group is a composite group
          if (groupNameToCompositeInfo != null && StringUtils.isNotBlank(group.getName())) {
            ObjectNode compositeInfo = groupNameToCompositeInfo.get(group.getName());
            if (compositeInfo != null) {
              groupNode.put("isComposite", true);
              groupNode.set("compositeInfo", compositeInfo);
            } else {
              groupNode.put("isComposite", false);
            }
          }
          groupsArray.add(groupNode);
        }
      }
      resultNode.set("groups", groupsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error finding groups", e);
      return buildErrorResult("Error finding groups: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
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

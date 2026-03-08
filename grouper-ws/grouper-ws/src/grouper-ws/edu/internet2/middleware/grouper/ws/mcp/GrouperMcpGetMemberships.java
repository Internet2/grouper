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

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembership;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for retrieving memberships from Grouper.
 * Returns membership details including enabled/disabled dates and membership type.
 * Supports querying by groups, subjects, stems, and attribute definitions.
 * Supports point-in-time queries for historical membership data.
 * Delegates to the WS getMemberships service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetMemberships {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetMemberships.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for memberships_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "memberships_get");
    tool.put("description",
        "Get memberships and privileges from Grouper. Returns membership details including "
        + "start date (startTime), end date (endTime), membership type, and list name. "
        + "Can query by group names, subject IDs/identifiers, stem names, and/or attribute "
        + "definition names. Supports point-in-time queries for historical membership data. "
        + "Use this tool when you need membership dates or detailed membership/privilege information.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    // groupNames - array of group names
    ObjectNode groupNamesProp = objectMapper.createObjectNode();
    groupNamesProp.put("type", "array");
    ObjectNode groupNamesItemsNode = objectMapper.createObjectNode();
    groupNamesItemsNode.put("type", "string");
    groupNamesProp.set("items", groupNamesItemsNode);
    groupNamesProp.put("description",
        "Array of fully qualified group names to query memberships for "
        + "(e.g., ['stem1:stem2:groupName']).");
    properties.set("groupNames", groupNamesProp);

    // subjectIds - array of subject IDs
    ObjectNode subjectIdsProp = objectMapper.createObjectNode();
    subjectIdsProp.put("type", "array");
    ObjectNode subjectIdsItemsNode = objectMapper.createObjectNode();
    subjectIdsItemsNode.put("type", "string");
    subjectIdsProp.set("items", subjectIdsItemsNode);
    subjectIdsProp.put("description",
        "Array of subject IDs to query memberships for.");
    properties.set("subjectIds", subjectIdsProp);

    // subjectIdentifiers - array of subject identifiers
    ObjectNode subjectIdentifiersProp = objectMapper.createObjectNode();
    subjectIdentifiersProp.put("type", "array");
    ObjectNode subjectIdentifiersItemsNode = objectMapper.createObjectNode();
    subjectIdentifiersItemsNode.put("type", "string");
    subjectIdentifiersProp.set("items", subjectIdentifiersItemsNode);
    subjectIdentifiersProp.put("description",
        "Array of subject identifiers (e.g., usernames) to query memberships for.");
    properties.set("subjectIdentifiers", subjectIdentifiersProp);

    // subjectSourceIds - array of source IDs to restrict subject lookup
    ObjectNode subjectSourceIdsProp = objectMapper.createObjectNode();
    subjectSourceIdsProp.put("type", "array");
    ObjectNode subjectSourceIdsItemsNode = objectMapper.createObjectNode();
    subjectSourceIdsItemsNode.put("type", "string");
    subjectSourceIdsProp.set("items", subjectSourceIdsItemsNode);
    subjectSourceIdsProp.put("description",
        "Array of source IDs to restrict subject lookups to specific sources.");
    properties.set("subjectSourceIds", subjectSourceIdsProp);

    // stemNames - array of stem (folder) names that own the memberships (for stem privileges)
    ObjectNode stemNamesProp = objectMapper.createObjectNode();
    stemNamesProp.put("type", "array");
    ObjectNode stemNamesItemsNode = objectMapper.createObjectNode();
    stemNamesItemsNode.put("type", "string");
    stemNamesProp.set("items", stemNamesItemsNode);
    stemNamesProp.put("description",
        "Array of fully qualified stem (folder) names to query privileges for "
        + "(e.g., ['stem1:stem2']).");
    properties.set("stemNames", stemNamesProp);

    // attributeDefNames - array of attribute definition names
    ObjectNode attributeDefNamesProp = objectMapper.createObjectNode();
    attributeDefNamesProp.put("type", "array");
    ObjectNode attributeDefNamesItemsNode = objectMapper.createObjectNode();
    attributeDefNamesItemsNode.put("type", "string");
    attributeDefNamesProp.set("items", attributeDefNamesItemsNode);
    attributeDefNamesProp.put("description",
        "Array of attribute definition names to query privileges for.");
    properties.set("attributeDefNames", attributeDefNamesProp);

    // memberFilter
    ObjectNode memberFilterProp = objectMapper.createObjectNode();
    memberFilterProp.put("type", "string");
    ArrayNode memberFilterEnum = objectMapper.createArrayNode();
    memberFilterEnum.add("All");
    memberFilterEnum.add("Immediate");
    memberFilterEnum.add("Effective");
    memberFilterEnum.add("Composite");
    memberFilterEnum.add("NonImmediate");
    memberFilterProp.set("enum", memberFilterEnum);
    memberFilterProp.put("description",
        "Membership filter type. "
        + "All = all members (default), Immediate = direct members only, "
        + "Effective = indirect members only, Composite = composite members, "
        + "NonImmediate = non-direct members only.");
    properties.set("memberFilter", memberFilterProp);

    // privilegeListName (fieldName)
    ObjectNode privilegeListNameProp = objectMapper.createObjectNode();
    privilegeListNameProp.put("type", "string");
    ArrayNode privilegeListNameEnum = objectMapper.createArrayNode();
    privilegeListNameEnum.add(Field.FIELD_NAME_ADMINS);
    privilegeListNameEnum.add(Field.FIELD_NAME_UPDATERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_READERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_VIEWERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_OPTINS);
    privilegeListNameEnum.add(Field.FIELD_NAME_OPTOUTS);
    privilegeListNameEnum.add(Field.FIELD_NAME_GROUP_ATTR_READERS);
    privilegeListNameEnum.add(Field.FIELD_NAME_GROUP_ATTR_UPDATERS);
    privilegeListNameProp.set("enum", privilegeListNameEnum);
    privilegeListNameProp.put("description",
        "Privilege list name to query instead of membership. "
        + "If omitted, returns the standard membership list. "
        + "Use this to query which subjects have a specific privilege.");
    properties.set("privilegeListName", privilegeListNameProp);

    // scopeStemName - filter within a stem
    ObjectNode scopeStemNameProp = objectMapper.createObjectNode();
    scopeStemNameProp.put("type", "string");
    scopeStemNameProp.put("description",
        "Stem name to limit results to memberships within a specific stem (folder). "
        + "Used with scopeType.");
    properties.set("scopeStemName", scopeStemNameProp);

    // scopeType
    ObjectNode scopeTypeProp = objectMapper.createObjectNode();
    scopeTypeProp.put("type", "string");
    ArrayNode stemScopeEnum = objectMapper.createArrayNode();
    stemScopeEnum.add("ONE_LEVEL");
    stemScopeEnum.add("ALL_IN_SUBTREE");
    scopeTypeProp.set("enum", stemScopeEnum);
    scopeTypeProp.put("description",
        "How deep under scopeStemName to search. ONE_LEVEL = immediate children only, "
        + "ALL_IN_SUBTREE = all descendants (default).");
    properties.set("scopeType", scopeTypeProp);

    // enabled
    ObjectNode enabledProp = objectMapper.createObjectNode();
    enabledProp.put("type", "string");
    ArrayNode enabledEnum = objectMapper.createArrayNode();
    enabledEnum.add("T");
    enabledEnum.add("F");
    enabledEnum.add("A");
    enabledProp.set("enum", enabledEnum);
    enabledProp.put("description",
        "Filter by enabled status. T = enabled only (default), "
        + "F = disabled only, A = all (both enabled and disabled).");
    properties.set("enabled", enabledProp);

    // pointInTimeFrom
    ObjectNode pointInTimeFromProp = objectMapper.createObjectNode();
    pointInTimeFromProp.put("type", "string");
    pointInTimeFromProp.put("description",
        "Start of point-in-time query range, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/01/01 00:00:00.000'). "
        + "Used to query historical membership data.");
    properties.set("pointInTimeFrom", pointInTimeFromProp);

    // pointInTimeTo
    ObjectNode pointInTimeToProp = objectMapper.createObjectNode();
    pointInTimeToProp.put("type", "string");
    pointInTimeToProp.put("description",
        "End of point-in-time query range, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/12/31 23:59:59.000'). "
        + "Used to query historical membership data.");
    properties.set("pointInTimeTo", pointInTimeToProp);

    // pageSize
    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of memberships to return per page. Defaults to 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    // pageNumber
    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number to return (1-based). Defaults to 1.");
    pageNumberProp.put("default", 1);
    properties.set("pageNumber", pageNumberProp);

    inputSchema.set("properties", properties);

    // no required fields - user can query by groups, subjects, or both
    inputSchema.set("required", objectMapper.createArrayNode());

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the memberships_get tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    // parse groupNames array
    WsGroupLookup[] wsGroupLookups = null;
    if (arguments != null && arguments.has("groupNames") && arguments.get("groupNames").isArray()) {
      JsonNode groupNamesArray = arguments.get("groupNames");
      wsGroupLookups = new WsGroupLookup[groupNamesArray.size()];
      for (int i = 0; i < groupNamesArray.size(); i++) {
        WsGroupLookup wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setGroupName(groupNamesArray.get(i).asText());
        wsGroupLookups[i] = wsGroupLookup;
      }
    }

    // parse subjectIds and subjectIdentifiers arrays into WsSubjectLookup[]
    WsSubjectLookup[] wsSubjectLookups = null;
    JsonNode subjectIdsArray = arguments != null && arguments.has("subjectIds")
        ? arguments.get("subjectIds") : null;
    JsonNode subjectIdentifiersArray = arguments != null && arguments.has("subjectIdentifiers")
        ? arguments.get("subjectIdentifiers") : null;
    int subjectIdCount = subjectIdsArray != null && subjectIdsArray.isArray() ? subjectIdsArray.size() : 0;
    int subjectIdentifierCount = subjectIdentifiersArray != null && subjectIdentifiersArray.isArray()
        ? subjectIdentifiersArray.size() : 0;
    if (subjectIdCount + subjectIdentifierCount > 0) {
      wsSubjectLookups = new WsSubjectLookup[subjectIdCount + subjectIdentifierCount];
      int idx = 0;
      for (int i = 0; i < subjectIdCount; i++) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        wsSubjectLookup.setSubjectId(subjectIdsArray.get(i).asText());
        wsSubjectLookups[idx++] = wsSubjectLookup;
      }
      for (int i = 0; i < subjectIdentifierCount; i++) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        wsSubjectLookup.setSubjectIdentifier(subjectIdentifiersArray.get(i).asText());
        wsSubjectLookups[idx++] = wsSubjectLookup;
      }
    }

    // parse subjectSourceIds
    String[] sourceIds = null;
    if (arguments != null && arguments.has("subjectSourceIds") && arguments.get("subjectSourceIds").isArray()) {
      JsonNode sourceIdsArray = arguments.get("subjectSourceIds");
      sourceIds = new String[sourceIdsArray.size()];
      for (int i = 0; i < sourceIdsArray.size(); i++) {
        sourceIds[i] = sourceIdsArray.get(i).asText();
      }
    }

    // parse stemNames array (owner stems for stem privileges)
    WsStemLookup[] wsOwnerStemLookups = null;
    if (arguments != null && arguments.has("stemNames") && arguments.get("stemNames").isArray()) {
      JsonNode stemNamesArray = arguments.get("stemNames");
      wsOwnerStemLookups = new WsStemLookup[stemNamesArray.size()];
      for (int i = 0; i < stemNamesArray.size(); i++) {
        WsStemLookup wsStemLookup = new WsStemLookup();
        wsStemLookup.setStemName(stemNamesArray.get(i).asText());
        wsOwnerStemLookups[i] = wsStemLookup;
      }
    }

    // parse attributeDefNames array
    WsAttributeDefLookup[] wsOwnerAttributeDefLookups = null;
    if (arguments != null && arguments.has("attributeDefNames")
        && arguments.get("attributeDefNames").isArray()) {
      JsonNode attrDefNamesArray = arguments.get("attributeDefNames");
      wsOwnerAttributeDefLookups = new WsAttributeDefLookup[attrDefNamesArray.size()];
      for (int i = 0; i < attrDefNamesArray.size(); i++) {
        WsAttributeDefLookup wsAttributeDefLookup = new WsAttributeDefLookup();
        wsAttributeDefLookup.setName(attrDefNamesArray.get(i).asText());
        wsOwnerAttributeDefLookups[i] = wsAttributeDefLookup;
      }
    }

    // scalar params
    String memberFilterString = arguments != null && arguments.has("memberFilter")
        ? arguments.get("memberFilter").asText() : "All";
    String fieldNameString = arguments != null && arguments.has("privilegeListName")
        ? arguments.get("privilegeListName").asText() : null;
    if (StringUtils.isBlank(fieldNameString) && arguments != null && arguments.has("fieldName")) {
      fieldNameString = arguments.get("fieldName").asText();
    }
    String scope = arguments != null && arguments.has("scopeStemName")
        ? arguments.get("scopeStemName").asText() : null;
    String stemScopeString = arguments != null && arguments.has("scopeType")
        ? arguments.get("scopeType").asText() : null;
    String enabled = arguments != null && arguments.has("enabled")
        ? arguments.get("enabled").asText() : "T";
    String pointInTimeFromString = arguments != null && arguments.has("pointInTimeFrom")
        ? arguments.get("pointInTimeFrom").asText() : null;
    String pointInTimeToString = arguments != null && arguments.has("pointInTimeTo")
        ? arguments.get("pointInTimeTo").asText() : null;
    Integer pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : 50;
    Integer pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt() : 1;

    // need at least one query parameter
    if (wsGroupLookups == null && wsSubjectLookups == null
        && wsOwnerStemLookups == null && wsOwnerAttributeDefLookups == null) {
      return buildErrorResult("At least one of groupNames, subjectIds, subjectIdentifiers, "
          + "stemNames, or attributeDefNames is required.");
    }

    try {

      // convert memberFilter
      WsMemberFilter wsMemberFilter = null;
      if (StringUtils.isNotBlank(memberFilterString)) {
        wsMemberFilter = GrouperServiceUtils.convertMemberFilter(memberFilterString);
      }

      // convert fieldName
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // convert stemScope
      StemScope stemScope = null;
      if (StringUtils.isNotBlank(stemScopeString)) {
        stemScope = StemScope.valueOfIgnoreCase(stemScopeString);
      }

      // convert point-in-time timestamps
      Timestamp pointInTimeFrom = GrouperServiceUtils.stringToTimestamp(pointInTimeFromString);
      Timestamp pointInTimeTo = GrouperServiceUtils.stringToTimestamp(pointInTimeToString);
      Boolean pointInTimeRetrieve = (pointInTimeFrom != null || pointInTimeTo != null)
          ? Boolean.TRUE : null;

      // scope stem lookup (for filtering within a stem)
      WsStemLookup wsStemLookup = null;
      if (StringUtils.isNotBlank(scope)) {
        wsStemLookup = new WsStemLookup();
        wsStemLookup.setStemName(scope);
      }

      // delegate to the WS service logic
      WsGetMembershipsResults wsResults = GrouperServiceLogic.getMemberships(
          GrouperVersion.currentVersion(),
          wsGroupLookups,
          wsSubjectLookups,
          wsMemberFilter,
          null,   // actAsSubjectLookup - uses REMOTE_USER
          field,
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          false,  // includeGroupDetail
          null,   // params
          sourceIds,
          null,   // scope string (not the stem scope)
          wsStemLookup,
          stemScope,
          enabled,
          null,   // membershipIds
          wsOwnerStemLookups,
          wsOwnerAttributeDefLookups,
          null,   // fieldType
          null,   // serviceRole
          null,   // serviceLookup
          pageSize,
          pageNumber,
          null,   // sortString
          null,   // ascending
          null,   // pageSizeForMember
          null,   // pageNumberForMember
          null,   // sortStringForMember
          null,   // ascendingForMember
          null,   // pageIsCursor
          null,   // pageLastCursorField
          null,   // pageLastCursorFieldType
          null,   // pageCursorFieldIncludesLastRetrieved
          null,   // pageIsCursorForMember
          null,   // pageLastCursorFieldForMember
          null,   // pageLastCursorFieldTypeForMember
          null,   // pageCursorFieldIncludesLastRetrievedForMember
          pointInTimeRetrieve,
          pointInTimeFrom,
          pointInTimeTo
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("success", true);

      WsMembership[] memberships = wsResults.getWsMemberships();
      ArrayNode membershipsArray = objectMapper.createArrayNode();

      if (memberships != null) {
        for (WsMembership membership : memberships) {
          ObjectNode membershipNode = objectMapper.createObjectNode();
          if (StringUtils.isNotBlank(membership.getMembershipId())) {
            membershipNode.put("membershipId", membership.getMembershipId());
          }
          if (StringUtils.isNotBlank(membership.getGroupName())) {
            membershipNode.put("groupName", membership.getGroupName());
          }
          if (StringUtils.isNotBlank(membership.getOwnerStemName())) {
            membershipNode.put("ownerStemName", membership.getOwnerStemName());
          }
          if (StringUtils.isNotBlank(membership.getOwnerNameOfAttributeDef())) {
            membershipNode.put("ownerAttributeDefName", membership.getOwnerNameOfAttributeDef());
          }
          if (StringUtils.isNotBlank(membership.getSubjectId())) {
            membershipNode.put("subjectId", membership.getSubjectId());
          }
          if (StringUtils.isNotBlank(membership.getSubjectSourceId())) {
            membershipNode.put("subjectSourceId", membership.getSubjectSourceId());
          }
          if (StringUtils.isNotBlank(membership.getListName())) {
            membershipNode.put("listName", membership.getListName());
          }
          if (StringUtils.isNotBlank(membership.getListType())) {
            membershipNode.put("listType", membership.getListType());
          }
          if (StringUtils.isNotBlank(membership.getMembershipType())) {
            membershipNode.put("membershipType", membership.getMembershipType());
          }
          if (StringUtils.isNotBlank(membership.getEnabled())) {
            membershipNode.put("enabled", "T".equals(membership.getEnabled()));
          }
          if (StringUtils.isNotBlank(membership.getEnabledTime())) {
            membershipNode.put("startTime", membership.getEnabledTime());
          }
          if (StringUtils.isNotBlank(membership.getDisabledTime())) {
            membershipNode.put("endTime", membership.getDisabledTime());
          }
          if (StringUtils.isNotBlank(membership.getCreateTime())) {
            membershipNode.put("createTime", membership.getCreateTime());
          }
          membershipsArray.add(membershipNode);
        }
      }

      resultNode.put("totalMemberships", membershipsArray.size());
      resultNode.set("memberships", membershipsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting memberships", e);
      return buildErrorResult("Error getting memberships: " + e.getMessage());
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

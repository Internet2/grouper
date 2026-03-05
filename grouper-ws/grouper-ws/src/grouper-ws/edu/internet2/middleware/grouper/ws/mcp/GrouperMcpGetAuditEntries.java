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

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAuditEntry;
import edu.internet2.middleware.grouper.ws.coresoap.WsAuditEntryColumn;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAuditEntriesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * MCP tool handler for getting audit log entries from Grouper.
 * Supports filtering by audit type, group, stem, subject, date range, and paging.
 * Delegates to the WS getAuditEntries service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpGetAuditEntries {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetAuditEntries.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for audit_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "audit_get");
    tool.put("description",
        "Get audit log entries from Grouper. "
        + "Supports filtering by audit type, group, stem, subject, "
        + "actions performed by a specific subject, and date range. "
        + "Defaults to page size 50 to prevent returning too many results.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode auditTypeProp = objectMapper.createObjectNode();
    auditTypeProp.put("type", "string");
    auditTypeProp.put("description",
        "Audit type to filter by (e.g., 'group', 'membership').");
    properties.set("auditType", auditTypeProp);

    ObjectNode auditActionIdProp = objectMapper.createObjectNode();
    auditActionIdProp.put("type", "string");
    auditActionIdProp.put("description",
        "Audit action ID to filter by.");
    properties.set("auditActionId", auditActionIdProp);

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "Filter to audit entries for this group "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Filter to audit entries for this stem "
        + "(e.g., 'stem1:stem2').");
    properties.set("stemName", stemNameProp);

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "Subject ID to get audit entries about.");
    properties.set("subjectId", subjectIdProp);

    ObjectNode subjectSourceIdProp = objectMapper.createObjectNode();
    subjectSourceIdProp.put("type", "string");
    subjectSourceIdProp.put("description",
        "Source ID for the subject to get audit entries about.");
    properties.set("subjectSourceId", subjectSourceIdProp);

    ObjectNode actionsPerformedBySubjectIdProp = objectMapper.createObjectNode();
    actionsPerformedBySubjectIdProp.put("type", "string");
    actionsPerformedBySubjectIdProp.put("description",
        "Subject ID of the user who performed the actions.");
    properties.set("actionsPerformedBySubjectId", actionsPerformedBySubjectIdProp);

    ObjectNode actionsPerformedBySubjectSourceIdProp = objectMapper.createObjectNode();
    actionsPerformedBySubjectSourceIdProp.put("type", "string");
    actionsPerformedBySubjectSourceIdProp.put("description",
        "Source ID for the subject who performed the actions.");
    properties.set("actionsPerformedBySubjectSourceId", actionsPerformedBySubjectSourceIdProp);

    ObjectNode fromDateProp = objectMapper.createObjectNode();
    fromDateProp.put("type", "string");
    fromDateProp.put("description",
        "Start date for the audit query, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/01/01 00:00:00.000').");
    properties.set("fromDate", fromDateProp);

    ObjectNode toDateProp = objectMapper.createObjectNode();
    toDateProp.put("type", "string");
    toDateProp.put("description",
        "End date for the audit query, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/12/31 23:59:59.000').");
    properties.set("toDate", toDateProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of audit entries to return. Defaults to 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    inputSchema.set("properties", properties);

    // no required fields - all filters are optional

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the audit_get tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String auditType = arguments != null && arguments.has("auditType")
        ? arguments.get("auditType").asText() : null;
    String auditActionId = arguments != null && arguments.has("auditActionId")
        ? arguments.get("auditActionId").asText() : null;
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String subjectId = arguments != null && arguments.has("subjectId")
        ? arguments.get("subjectId").asText() : null;
    String subjectSourceId = arguments != null && arguments.has("subjectSourceId")
        ? arguments.get("subjectSourceId").asText() : null;
    String actionsPerformedBySubjectId = arguments != null
        && arguments.has("actionsPerformedBySubjectId")
        ? arguments.get("actionsPerformedBySubjectId").asText() : null;
    String actionsPerformedBySubjectSourceId = arguments != null
        && arguments.has("actionsPerformedBySubjectSourceId")
        ? arguments.get("actionsPerformedBySubjectSourceId").asText() : null;
    String fromDateString = arguments != null && arguments.has("fromDate")
        ? arguments.get("fromDate").asText() : null;
    String toDateString = arguments != null && arguments.has("toDate")
        ? arguments.get("toDate").asText() : null;
    Integer pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : 50;

    try {

      WsGroupLookup wsGroupLookup = null;
      if (StringUtils.isNotBlank(groupName)) {
        wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setGroupName(groupName);
      }

      WsStemLookup wsStemLookup = null;
      if (StringUtils.isNotBlank(stemName)) {
        wsStemLookup = new WsStemLookup(stemName, null);
      }

      WsSubjectLookup wsSubjectLookup = null;
      if (StringUtils.isNotBlank(subjectId)) {
        wsSubjectLookup = new WsSubjectLookup(subjectId, subjectSourceId, null);
      }

      WsSubjectLookup actionsPerformedByLookup = null;
      if (StringUtils.isNotBlank(actionsPerformedBySubjectId)) {
        actionsPerformedByLookup = new WsSubjectLookup(
            actionsPerformedBySubjectId, actionsPerformedBySubjectSourceId, null);
      }

      Timestamp fromTimestamp = GrouperServiceUtils.stringToTimestamp(fromDateString);
      Timestamp toTimestamp = GrouperServiceUtils.stringToTimestamp(toDateString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsGetAuditEntriesResults wsResults = GrouperServiceLogic.getAuditEntries(
          GrouperVersion.currentVersion(),
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          auditType,
          auditActionId,
          wsGroupLookup,
          wsStemLookup,
          null,   // wsAttributeDefLookup
          null,   // wsAttributeDefNameLookup
          wsSubjectLookup,
          actionsPerformedByLookup,
          null,   // params
          pageSize,
          null, null,  // sort
          null, null, null, null,  // cursor paging
          fromTimestamp,
          toTimestamp
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAuditEntry[] auditEntries = wsResults.getWsAuditEntries();
      if (GrouperUtil.length(auditEntries) == 0) {
        return buildSuccessResult("No audit entries found matching the criteria.");
      }

      ArrayNode entriesArray = objectMapper.createArrayNode();
      for (WsAuditEntry auditEntry : auditEntries) {
        entriesArray.add(convertAuditEntryToJson(auditEntry));
      }

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(entriesArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting audit entries", e);
      return buildErrorResult("Error getting audit entries: " + e.getMessage());
    }
  }

  /**
   * convert a WsAuditEntry to a clean JSON object for MCP consumption.
   * @param auditEntry the WS audit entry
   * @return clean JSON object
   */
  private static ObjectNode convertAuditEntryToJson(WsAuditEntry auditEntry) {
    ObjectNode entryNode = objectMapper.createObjectNode();
    entryNode.put("id", auditEntry.getId());
    if (StringUtils.isNotBlank(auditEntry.getActionName())) {
      entryNode.put("actionName", auditEntry.getActionName());
    }
    if (StringUtils.isNotBlank(auditEntry.getAuditCategory())) {
      entryNode.put("auditCategory", auditEntry.getAuditCategory());
    }
    if (StringUtils.isNotBlank(auditEntry.getTimestamp())) {
      entryNode.put("timestamp", auditEntry.getTimestamp());
    }

    // include audit entry columns as key-value pairs
    WsAuditEntryColumn[] columns = auditEntry.getAuditEntryColumns();
    if (GrouperUtil.length(columns) > 0) {
      ObjectNode entriesObject = objectMapper.createObjectNode();
      for (WsAuditEntryColumn column : columns) {
        String label = column.getLabel();
        String value = column.getValueString();
        if (StringUtils.isBlank(value)) {
          value = column.getValueInt();
        }
        if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(value)) {
          entriesObject.put(label, value);
        }
      }
      if (entriesObject.size() > 0) {
        entryNode.set("entries", entriesObject);
      }
    }

    return entryNode;
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

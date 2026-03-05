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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP admin tool for retrieving recent daemon job information from the
 * grouper_loader_log table.  Returns useful columns (but NOT job_message)
 * for the most recent 100 rows matching the optional filters.
 * The user can filter by job_name and/or status.  If only status is
 * provided (without job_name), that is fine.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminGetDaemonJobs {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminGetDaemonJobs.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum number of rows to return */
  static final int MAX_ROWS = 100;

  /**
   * return the MCP tool definition for admin_daemon_logs
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_daemon_logs");
    tool.put("description",
        "Get recent daemon job entries from the Grouper loader log. "
        + "Returns useful columns (id, job_name, status, started_time, ended_time, "
        + "millis, job_type, job_description, host, insert_count, update_count, "
        + "delete_count, total_count, unresolvable_subject_count, parent_job_name, "
        + "last_updated) for the most recent 100 rows matching the filters. "
        + "Does NOT return job_message (use admin_daemon_job_message for that). "
        + "At least one of jobName or status must be provided. "
        + "Use admin_daemon_names to find job names first.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode jobNameProp = objectMapper.createObjectNode();
    jobNameProp.put("type", "string");
    jobNameProp.put("description",
        "Filter by job name (exact match). Use admin_daemon_names "
        + "to find job names first.");
    properties.set("jobName", jobNameProp);

    ObjectNode statusProp = objectMapper.createObjectNode();
    statusProp.put("type", "string");
    statusProp.put("description",
        "Filter by job status (exact match, case-insensitive). "
        + "Common values: SUCCESS, ERROR, STARTED, WARNING, CONFIG_ERROR, "
        + "SUBJECT_PROBLEMS, RUNNING.");
    properties.set("status", statusProp);

    ObjectNode startedAfterProp = objectMapper.createObjectNode();
    startedAfterProp.put("type", "string");
    startedAfterProp.put("description",
        "Filter to jobs started after this date/time. "
        + "Format: yyyy/MM/dd HH:mm:ss (e.g., 2025/01/15 00:00:00). Optional.");
    properties.set("startedAfter", startedAfterProp);

    ObjectNode startedBeforeProp = objectMapper.createObjectNode();
    startedBeforeProp.put("type", "string");
    startedBeforeProp.put("description",
        "Filter to jobs started before this date/time. "
        + "Format: yyyy/MM/dd HH:mm:ss (e.g., 2025/01/16 00:00:00). Optional.");
    properties.set("startedBefore", startedBeforeProp);

    inputSchema.set("properties", properties);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_daemon_logs tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String jobName = arguments != null && arguments.has("jobName")
        ? arguments.get("jobName").asText() : null;
    String status = arguments != null && arguments.has("status")
        ? arguments.get("status").asText() : null;
    String startedAfter = arguments != null && arguments.has("startedAfter")
        ? arguments.get("startedAfter").asText() : null;
    String startedBefore = arguments != null && arguments.has("startedBefore")
        ? arguments.get("startedBefore").asText() : null;

    if (StringUtils.isBlank(jobName) && StringUtils.isBlank(status)) {
      return buildErrorResult("At least one of jobName or status is required.");
    }

    try {
      StringBuilder sql = new StringBuilder(
          "SELECT id, job_name, status, started_time, ended_time, millis, "
          + "job_type, job_description, host, insert_count, update_count, "
          + "delete_count, total_count, unresolvable_subject_count, "
          + "parent_job_name, last_updated "
          + "FROM grouper_loader_log WHERE 1=1");

      List<Object> bindVars = new ArrayList<Object>();

      if (StringUtils.isNotBlank(jobName)) {
        sql.append(" AND job_name = ?");
        bindVars.add(jobName.trim());
      }

      if (StringUtils.isNotBlank(status)) {
        sql.append(" AND UPPER(status) = UPPER(?)");
        bindVars.add(status.trim());
      }

      if (StringUtils.isNotBlank(startedAfter)) {
        sql.append(" AND started_time >= ?");
        bindVars.add(java.sql.Timestamp.valueOf(
            startedAfter.trim().replace("/", "-")));
      }

      if (StringUtils.isNotBlank(startedBefore)) {
        sql.append(" AND started_time <= ?");
        bindVars.add(java.sql.Timestamp.valueOf(
            startedBefore.trim().replace("/", "-")));
      }

      sql.append(" ORDER BY started_time DESC");

      GcDbAccess gcDbAccess = new GcDbAccess()
          .readOnly(true)
          .paging(1, MAX_ROWS)
          .sql(sql.toString());

      for (Object bindVar : bindVars) {
        gcDbAccess.addBindVar(bindVar);
      }

      List<? extends Map<String, Object>> rows = gcDbAccess.selectListMap();

      // build JSON array of row objects
      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (Map<String, Object> row : rows) {
        ObjectNode rowNode = objectMapper.createObjectNode();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
          if (entry.getValue() == null) {
            rowNode.putNull(entry.getKey());
          } else {
            rowNode.put(entry.getKey(), String.valueOf(entry.getValue()));
          }
        }
        resultsArray.add(rowNode);
      }

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("rowCount", rows.size());
      if (rows.size() >= MAX_ROWS) {
        resultNode.put("message", "Returned the maximum of " + MAX_ROWS
            + " most recent rows. Use date filters to narrow results.");
      }
      resultNode.set("rows", resultsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting daemon job entries via MCP", e);
      return buildErrorResult("Error getting daemon job entries: " + e.getMessage());
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

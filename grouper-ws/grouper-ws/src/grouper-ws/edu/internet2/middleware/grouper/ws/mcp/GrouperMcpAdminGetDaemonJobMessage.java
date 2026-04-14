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

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP admin tool for retrieving the job message of a specific daemon job entry
 * from the grouper_loader_log table.  Takes a loader log id (primary key)
 * and returns the first 20,000 characters of the job_message (or job_message_clob
 * if job_message is null).  Use admin_daemon_logs to find the id first.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminGetDaemonJobMessage {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminGetDaemonJobMessage.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum characters of message to return */
  static final int MAX_MESSAGE_CHARS = 20000;

  /**
   * return the MCP tool definition for admin_daemon_job_message
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_daemon_job_message");
    tool.put("description",
        "Get the job message for a specific daemon job entry from the Grouper loader log. "
        + "Takes the loader log id (primary key from admin_daemon_logs) and returns "
        + "the first " + MAX_MESSAGE_CHARS + " characters of the job message. "
        + "This is separated from admin_daemon_logs because job messages can be very "
        + "large and should only be retrieved when needed. "
        + "Use admin_daemon_logs to find the id first.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode idProp = objectMapper.createObjectNode();
    idProp.put("type", "string");
    idProp.put("description",
        "The id (primary key) of the loader log entry. "
        + "Use admin_daemon_logs to find the id.");
    properties.set("id", idProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("id");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_daemon_job_message tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String id = arguments != null && arguments.has("id")
        ? arguments.get("id").asText() : null;

    if (StringUtils.isBlank(id)) {
      return buildErrorResult("id is required.");
    }

    try {
      // try job_message first, then fall back to job_message_clob
      String sql = "SELECT job_name, status, started_time, job_message, job_message_clob "
          + "FROM grouper_loader_log WHERE id = ?";

      GcDbAccess gcDbAccess = new GcDbAccess()
          .readOnly(true)
          .sql(sql)
          .addBindVar(id.trim());

      java.util.List<? extends java.util.Map<String, Object>> rows = gcDbAccess.selectListMap();

      if (rows.isEmpty()) {
        return buildErrorResult("No loader log entry found with id: " + id);
      }

      java.util.Map<String, Object> row = rows.get(0);

      String jobName = row.get("job_name") != null ? String.valueOf(row.get("job_name")) : null;
      String status = row.get("status") != null ? String.valueOf(row.get("status")) : null;
      String startedTime = row.get("started_time") != null ? String.valueOf(row.get("started_time")) : null;

      // prefer job_message, fall back to job_message_clob
      String message = null;
      if (row.get("job_message") != null) {
        message = String.valueOf(row.get("job_message"));
      }
      if (StringUtils.isBlank(message) && row.get("job_message_clob") != null) {
        message = String.valueOf(row.get("job_message_clob"));
      }

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("id", id);
      if (jobName != null) {
        resultNode.put("jobName", jobName);
      }
      if (status != null) {
        resultNode.put("status", status);
      }
      if (startedTime != null) {
        resultNode.put("startedTime", startedTime);
      }

      if (StringUtils.isBlank(message)) {
        resultNode.put("jobMessage", "(no message)");
      } else {
        boolean truncated = message.length() > MAX_MESSAGE_CHARS;
        if (truncated) {
          message = message.substring(0, MAX_MESSAGE_CHARS);
          resultNode.put("truncated", true);
          resultNode.put("truncatedAt", MAX_MESSAGE_CHARS);
        }
        resultNode.put("jobMessage", message);
      }

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting daemon job message via MCP", e);
      return buildErrorResult("Error getting daemon job message: " + e.getMessage()
          + "\n\n" + GrouperUtil.getFullStackTrace(e));
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

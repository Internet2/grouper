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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP admin tool for triggering a daemon job to run on the daemon server.
 * Takes a job name (from the Quartz scheduler) and triggers it asynchronously
 * on the daemon. The job must be enabled (not paused) to be triggered.
 * Use admin_daemon_names to find job names first.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminRunDaemonJob {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminRunDaemonJob.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for admin_daemon_job_run
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_daemon_job_run");
    tool.put("description",
        "Trigger a daemon job to run on the daemon server. "
        + "The job is triggered asynchronously via the Quartz scheduler and runs "
        + "on the daemon container/server. The job must be enabled (not paused). "
        + "Use admin_daemon_names to find job names first. "
        + "Use admin_daemon_logs to check job status afterward.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode jobNameProp = objectMapper.createObjectNode();
    jobNameProp.put("type", "string");
    jobNameProp.put("description",
        "The exact job name to trigger. Use admin_daemon_names "
        + "to find available job names.");
    properties.set("jobName", jobNameProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("jobName");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_daemon_job_run tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String jobName = arguments != null && arguments.has("jobName")
        ? arguments.get("jobName").asText() : null;

    if (StringUtils.isBlank(jobName)) {
      return buildErrorResult("jobName is required.");
    }

    jobName = jobName.trim();

    try {
      // use the session from the MCP servlet (user is already verified as admin)
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();

      // run on daemon (asynchronous trigger via Quartz scheduler)
      String result = GrouperLoader.runOnceByJobName(grouperSession, jobName, true);

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("jobName", jobName);
      resultNode.put("status", "TRIGGERED");
      resultNode.put("message", result);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error triggering daemon job via MCP: " + jobName, e);
      String errorMessage = e.getMessage();
      if (e.getCause() != null) {
        errorMessage = e.getCause().getMessage();
      }
      return buildErrorResult("Error triggering daemon job '" + jobName + "': " + errorMessage);
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

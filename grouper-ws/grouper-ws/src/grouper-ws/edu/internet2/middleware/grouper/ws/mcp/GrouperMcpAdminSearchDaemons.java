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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP admin tool for searching daemon job names in the grouper_loader_log table.
 * Takes a search string, splits it by whitespace, and searches for distinct
 * job_name values where each whitespace-delimited term matches (using LIKE with
 * user-supplied wildcards via prepared statement bind variables).
 *
 * @author mchyzer
 */
public class GrouperMcpAdminSearchDaemons {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAdminSearchDaemons.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum number of results to return */
  static final int MAX_RESULTS = 200;

  /**
   * return the MCP tool definition for admin_daemon_names
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "admin_daemon_names");
    tool.put("description",
        "Search for daemon job names in the Grouper loader log. "
        + "Takes a search string which is split by whitespace into terms. "
        + "Each term is matched against the job_name column using LIKE "
        + "(case-insensitive). You can use '%' as a wildcard in each term. "
        + "If a term does not contain a '%', it is automatically wrapped "
        + "with '%' on both sides. Returns distinct job names matching all terms. "
        + "Examples: 'provisioner' finds all provisioner job names, "
        + "'SQL%config' finds job names containing 'SQL' followed by 'config', "
        + "'CHANGE_LOG%recent' finds change log jobs with 'recent' in the name.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode searchStringProp = objectMapper.createObjectNode();
    searchStringProp.put("type", "string");
    searchStringProp.put("description",
        "Search string for daemon job names. Split by whitespace into terms. "
        + "Each term is matched against job_name using LIKE (case-insensitive). "
        + "Use '%' as a wildcard. If a term does not contain '%', it is "
        + "automatically wrapped with '%' on both sides. All terms must match "
        + "(AND logic).");
    properties.set("searchString", searchStringProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("searchString");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the admin_daemon_names tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String searchString = arguments != null && arguments.has("searchString")
        ? arguments.get("searchString").asText() : null;

    if (StringUtils.isBlank(searchString)) {
      return buildErrorResult("searchString is required.");
    }

    // split by whitespace into search terms
    String[] terms = searchString.trim().split("\\s+");

    if (terms.length == 0) {
      return buildErrorResult("searchString must contain at least one search term.");
    }

    try {
      // build query with one LOWER(job_name) LIKE LOWER(?) condition per term
      StringBuilder sql = new StringBuilder(
          "SELECT DISTINCT job_name FROM grouper_loader_log WHERE job_name IS NOT NULL");

      List<Object> bindVars = new ArrayList<Object>();

      for (String term : terms) {
        sql.append(" AND LOWER(job_name) LIKE LOWER(?)");
        // if the user didn't include wildcards, wrap with %
        if (!term.contains("%")) {
          bindVars.add("%" + term + "%");
        } else {
          bindVars.add(term);
        }
      }

      sql.append(" ORDER BY job_name");

      GcDbAccess gcDbAccess = new GcDbAccess()
          .readOnly(true)
          .sql(sql.toString());

      for (Object bindVar : bindVars) {
        gcDbAccess.addBindVar(bindVar);
      }

      List<String> jobNames = gcDbAccess.selectList(String.class);

      // truncate if too many
      boolean truncated = false;
      if (jobNames.size() > MAX_RESULTS) {
        jobNames = jobNames.subList(0, MAX_RESULTS);
        truncated = true;
      }

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("jobNameCount", jobNames.size());
      if (truncated) {
        resultNode.put("truncated", true);
        resultNode.put("message", "Results truncated to " + MAX_RESULTS
            + ". Use a more specific search to narrow results.");
      }

      ArrayNode jobNamesArray = objectMapper.createArrayNode();
      for (String jobName : jobNames) {
        jobNamesArray.add(jobName);
      }
      resultNode.set("jobNames", jobNamesArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error searching daemon job names via MCP", e);
      return buildErrorResult("Error searching daemon job names: " + e.getMessage()
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

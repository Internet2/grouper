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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP tool handler for counting the rows returned by a SQL SELECT query.
 * Wraps the user's SELECT in a COUNT(*) subquery and returns just the count.
 * Useful for checking result size before fetching data with grouperSqlSelect.
 *
 * @author mchyzer
 */
public class GrouperMcpSqlSelectCount {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpSqlSelectCount.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for sql_select_count
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "sql_select_count");
    tool.put("description",
        "Count the number of rows that a SQL SELECT query would return, without "
        + "fetching the actual data. Useful for checking result size before using "
        + "sql_select. The query is wrapped in SELECT COUNT(*) FROM (your_query). "
        + "Only SELECT statements are allowed.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode sqlProp = objectMapper.createObjectNode();
    sqlProp.put("type", "string");
    sqlProp.put("description",
        "The SQL SELECT query to count rows for. Must be a SELECT statement; "
        + "INSERT, UPDATE, DELETE, and DDL statements are not allowed. "
        + "Do not include a trailing semicolon. Do not include ORDER BY "
        + "(it is not needed for counting and may cause errors on some databases).");
    properties.set("sql", sqlProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("sql");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the sql_select_count tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String sql = arguments != null && arguments.has("sql")
        ? arguments.get("sql").asText() : null;

    if (StringUtils.isBlank(sql)) {
      return buildErrorResult("sql is required.");
    }

    // validate SQL is read-only (reuse validation from GrouperMcpSqlSelect)
    String validationError = GrouperMcpSqlSelect.validateReadOnlySql(sql);
    if (validationError != null) {
      return buildErrorResult(validationError);
    }

    // wrap in COUNT(*)
    String countSql = "SELECT COUNT(*) AS cnt FROM (" + sql + ") countQuery";

    // get the external system (connection name) for SQL tools
    String externalSystem = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sqlGrouperExternalSystem", "grouper");

    try {
      long count = new GcDbAccess()
          .connectionName(externalSystem)
          .readOnly(true)
          .sql(countSql)
          .select(Long.class);

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("count", count);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error executing SQL count query via MCP", e);
      return buildErrorResult("Error executing SQL count query: " + e.getMessage());
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

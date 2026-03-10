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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * MCP tool handler for executing read-only SQL SELECT queries against the
 * Grouper database or other configured external systems.  Only SELECT
 * statements are allowed; DML and DDL are rejected.  Results are returned
 * as a JSON array of row objects.
 * Uses paging (pageSize/pageNumber) and a read-only JDBC connection
 * for defense-in-depth.  The default database connection can be configured
 * via grouper.mcp.sqlGrouperExternalSystem.  Additional external systems
 * are available when configured with grouper.mcp.&lt;id&gt;.sqlTablesViews
 * or grouper.mcp.&lt;id&gt;.sqlTablesViewsQuery.
 * Supports countOnly mode to return just the row count without fetching data.
 *
 * @author mchyzer
 */
public class GrouperMcpSqlSelect {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpSqlSelect.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum page size (rows per page) */
  static final int MAX_ROWS = 2000;

  /** default page size */
  static final int DEFAULT_PAGE_SIZE = 200;

  /** maximum characters in the response text */
  static final int MAX_RESPONSE_CHARS = 100000;

  /**
   * return the MCP tool definition for sql_select
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "sql_select");
    tool.put("description",
        "Execute a read-only SQL SELECT query against the Grouper database (or another "
        + "configured external system) and return the results as a JSON array of row objects. "
        + "Only SELECT statements are allowed. "
        + "Results are paged; use pageSize (default " + DEFAULT_PAGE_SIZE
        + ", max " + MAX_ROWS + ") and pageNumber (1-based, default 1) to page through "
        + "large result sets. An ORDER BY clause is required when paging beyond page 1 "
        + "to ensure deterministic results. "
        + "Set countOnly to true to return just the row count without fetching data. "
        + "Use sql_get_schema with action 'listExternalSystems' to discover available databases, "
        + "'listTables' to see table/view names, and 'tableInfo' to get column details. "
        + "Use externalSystemId to query a different configured database connection.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode sqlProp = objectMapper.createObjectNode();
    sqlProp.put("type", "string");
    sqlProp.put("description",
        "The SQL SELECT query to execute. Must be a SELECT statement; "
        + "INSERT, UPDATE, DELETE, and DDL statements are not allowed. "
        + "Do not include a trailing semicolon. Do not include LIMIT or OFFSET "
        + "clauses; use the pageSize and pageNumber parameters instead.");
    properties.set("sql", sqlProp);

    ObjectNode countOnlyProp = objectMapper.createObjectNode();
    countOnlyProp.put("type", "boolean");
    countOnlyProp.put("description",
        "If true, return only the row count without fetching data. "
        + "Useful for checking result size before fetching. Default is false.");
    countOnlyProp.put("default", false);
    properties.set("countOnly", countOnlyProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of rows per page (default " + DEFAULT_PAGE_SIZE + ", max " + MAX_ROWS + "). "
        + "Ignored when countOnly is true.");
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number, 1-based (default 1). Use with pageSize to page through large result sets. "
        + "The SQL query must include an ORDER BY clause when using pageNumber > 1. "
        + "Ignored when countOnly is true.");
    properties.set("pageNumber", pageNumberProp);

    ObjectNode externalSystemIdProp = objectMapper.createObjectNode();
    externalSystemIdProp.put("type", "string");
    externalSystemIdProp.put("description",
        "Optional external system ID to query a different database connection. "
        + "Defaults to the Grouper database. The external system must be configured "
        + "by the administrator with grouper.mcp.sql.<id>.sqlTablesViews or "
        + "grouper.mcp.sql.<id>.sqlTablesViewsQuery. Use sql_get_schema with "
        + "action 'listExternalSystems' to see which external systems are available.");
    properties.set("externalSystemId", externalSystemIdProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("sql");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the sql_select tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String sql = arguments != null && arguments.has("sql")
        ? arguments.get("sql").asText() : null;
    boolean countOnly = arguments != null && arguments.has("countOnly")
        && arguments.get("countOnly").asBoolean(false);
    int pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt(DEFAULT_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
    int pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt(1) : 1;
    String externalSystemId = arguments != null && arguments.has("externalSystemId")
        ? arguments.get("externalSystemId").asText() : null;

    if (StringUtils.isBlank(sql)) {
      return buildErrorResult("sql is required.");
    }

    // validate SQL is read-only
    String validationError = validateReadOnlySql(sql);
    if (validationError != null) {
      return buildErrorResult(validationError);
    }

    // validate the external system is allowed
    String externalSystemError = validateExternalSystemAllowed(externalSystemId);
    if (externalSystemError != null) {
      return buildErrorResult(externalSystemError);
    }

    // resolve to the actual database connection name
    String connectionName = resolveConnectionName(externalSystemId);

    if (countOnly) {
      return executeCount(sql, connectionName);
    }

    return executeSelect(sql, pageSize, pageNumber, connectionName);
  }

  /**
   * execute a count-only query by wrapping the SQL in SELECT COUNT(*)
   * @param sql the original SELECT query
   * @param externalSystem the connection name
   * @return the MCP tool result with the count
   */
  private static ObjectNode executeCount(String sql, String externalSystem) {
    String countSql = "SELECT COUNT(*) AS cnt FROM (" + sql + ") countQuery";

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
   * execute a paged SELECT query and return results as JSON
   * @param sql the SELECT query
   * @param pageSize the page size
   * @param pageNumber the 1-based page number
   * @param externalSystem the connection name
   * @return the MCP tool result with rows
   */
  private static ObjectNode executeSelect(String sql, int pageSize, int pageNumber,
      String externalSystem) {

    // enforce page size limits
    if (pageSize < 1 || pageSize > MAX_ROWS) {
      pageSize = DEFAULT_PAGE_SIZE;
    }

    // enforce page number minimum
    if (pageNumber < 1) {
      pageNumber = 1;
    }

    // if paging beyond page 1, require an ORDER BY clause so results are deterministic
    if (pageNumber > 1 && !sql.toUpperCase().contains("ORDER BY")) {
      return buildErrorResult(
          "When using paging (pageNumber > 1), the SQL query must include an ORDER BY clause "
          + "so that results are deterministic across pages. Without ORDER BY, rows may be "
          + "duplicated or skipped between pages.");
    }

    try {
      List<? extends Map<String, Object>> rows = new GcDbAccess()
          .connectionName(externalSystem)
          .readOnly(true)
          .paging(pageNumber, pageSize)
          .sql(sql)
          .selectListMap();

      int totalRows = rows.size();

      // build JSON array of row objects
      ArrayNode resultsArray = objectMapper.createArrayNode();
      int rowsIncluded = 0;

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
        rowsIncluded++;
      }

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.put("rowCount", totalRows);
      resultNode.put("pageNumber", pageNumber);
      resultNode.put("pageSize", pageSize);
      resultNode.set("rows", resultsArray);

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);

      // truncate if too large
      if (resultText.length() > MAX_RESPONSE_CHARS) {
        resultText = resultText.substring(0, MAX_RESPONSE_CHARS)
            + "\n\n[Response truncated at " + MAX_RESPONSE_CHARS + " characters. "
            + rowsIncluded + " of " + totalRows + " total rows were in the full response. "
            + "Use a more specific WHERE clause or smaller pageSize to reduce result size.]";
      }

      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error executing SQL query via MCP", e);
      return buildErrorResult("Error executing SQL query: " + e.getMessage());
    }
  }

  /**
   * resolve the database connection name from the externalSystemId parameter.
   * "grouper" (or null/blank) maps to the configured grouper.mcp.sqlGrouperExternalSystem
   * value so the caller doesn't need to know the actual connection name.
   * other external system IDs are returned as-is.
   * @param externalSystemId the external system ID from the request, or null
   * @return the resolved database connection name
   */
  static String resolveConnectionName(String externalSystemId) {
    if (StringUtils.isBlank(externalSystemId) || "grouper".equals(externalSystemId.trim())) {
      return GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.sqlGrouperExternalSystem", "grouper");
    }
    return externalSystemId.trim();
  }

  /**
   * check if the given externalSystemId refers to the Grouper database.
   * null, blank, or "grouper" all mean the Grouper database.
   * @param externalSystemId the external system ID from the request, or null
   * @return true if this is the Grouper database
   */
  static boolean isGrouperDb(String externalSystemId) {
    return StringUtils.isBlank(externalSystemId) || "grouper".equals(externalSystemId.trim());
  }

  /**
   * validate that the external system is allowed for MCP SQL queries.
   * the Grouper database ("grouper" or null/blank) is always allowed.
   * other external systems require
   * grouper.mcp.&lt;id&gt;.sqlTablesViews or grouper.mcp.&lt;id&gt;.sqlTablesViewsQuery
   * to be configured.
   * @param externalSystemId the external system ID from the request (before resolution)
   * @return null if allowed, error message if not allowed
   */
  static String validateExternalSystemAllowed(String externalSystemId) {
    // the Grouper database is always allowed
    if (isGrouperDb(externalSystemId)) {
      return null;
    }

    String id = externalSystemId.trim();

    // check if the external system has sqlTablesViews or sqlTablesViewsQuery configured
    String sqlTablesViews = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sql." + id + ".sqlTablesViews", "");
    if (StringUtils.isNotBlank(sqlTablesViews)) {
      return null;
    }

    String sqlTablesViewsQuery = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sql." + id + ".sqlTablesViewsQuery", "");
    if (StringUtils.isNotBlank(sqlTablesViewsQuery)) {
      return null;
    }

    return "External system '" + id + "' is not configured for MCP SQL queries. "
        + "The administrator must configure grouper.mcp.sql." + id + ".sqlTablesViews "
        + "or grouper.mcp.sql." + id + ".sqlTablesViewsQuery to enable this external system.";
  }

  /**
   * pattern for dangerous SQL keywords (word boundaries, case-insensitive).
   * these keywords should not appear as standalone words in a read-only query.
   */
  private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
      "\\b(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE|GRANT|REVOKE|MERGE|CALL|EXEC)\\b",
      Pattern.CASE_INSENSITIVE);

  /**
   * validate that the SQL is a read-only SELECT statement.
   * @param sql the SQL to validate
   * @return null if valid, error message string if invalid
   */
  static String validateReadOnlySql(String sql) {
    if (StringUtils.isBlank(sql)) {
      return "SQL query is required.";
    }

    // reject semicolons (no multi-statement)
    if (sql.contains(";")) {
      return "SQL query must not contain semicolons. Submit a single SELECT statement.";
    }

    // normalize whitespace and check first keyword is SELECT
    String normalized = sql.trim().replaceAll("\\s+", " ");
    if (!normalized.toUpperCase().startsWith("SELECT ") && !normalized.toUpperCase().startsWith("SELECT\t")) {
      if (normalized.toUpperCase().equals("SELECT")) {
        return "SQL query must be a complete SELECT statement.";
      }
      return "Only SELECT statements are allowed. Your query starts with: "
          + normalized.substring(0, Math.min(normalized.length(), 20));
    }

    // check for dangerous keywords
    Matcher matcher = DANGEROUS_SQL_PATTERN.matcher(normalized);
    if (matcher.find()) {
      return "SQL query contains a prohibited keyword: " + matcher.group(1).toUpperCase()
          + ". Only SELECT statements are allowed.";
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

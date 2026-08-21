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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP tool handler for executing read-only SQL SELECT queries against the
 * database external systems the administrator has made available to MCP.
 * Only SELECT statements are allowed; DML and DDL are rejected.  Results are
 * returned as a JSON array of row objects.
 * Uses paging (pageSize/pageNumber) and a read-only JDBC connection
 * for defense-in-depth.
 * There is no default database, not even the Grouper database.  The externalSystemId
 * is both what the caller passes and the grouperClient.jdbc connection name used, and
 * it is available only when the administrator configured a
 * grouper.mcp.sql.&lt;externalSystemId&gt;.* property for it.
 * Supports countOnly mode to return just the row count without fetching data.
 *
 * @author mchyzer
 */
public class GrouperMcpSqlSelect {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpSqlSelect.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** maximum page size (rows per page) */
  static final int MAX_ROWS = 5000;

  /** default page size */
  static final int DEFAULT_PAGE_SIZE = 500;

  /** maximum characters in the response text */
  static final int MAX_RESPONSE_CHARS = 1000000;

  /**
   * return the MCP tool definition for sql_select
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "sql_select");
    tool.put("description",
        "Execute a read-only SQL SELECT query against one of the databases the Grouper "
        + "administrator has made available and return the results as a JSON array of row "
        + "objects. Only SELECT statements are allowed. "
        + "The externalSystemId parameter is required, there is no default database. "
        + "Call sql_get_schema with action 'listExternalSystems' first to discover which "
        + "databases are available, 'listTables' to see table/view names, and 'tableInfo' "
        + "to get column details. Use table and view names exactly as sql_get_schema "
        + "returns them, including any schema qualification. "
        + "Results are paged; use pageSize (default " + DEFAULT_PAGE_SIZE
        + ", max " + MAX_ROWS + ") and pageNumber (1-based, default 1) to page through "
        + "large result sets. An ORDER BY clause is required when paging beyond page 1 "
        + "to ensure deterministic results. "
        + "Set countOnly to true to return just the row count without fetching data.");

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
        "The ID of the database to query. Required; there is no default database and the "
        + "Grouper database is not available unless the administrator configured it. "
        + "Use sql_get_schema with action 'listExternalSystems' to see which external "
        + "systems are available.");
    properties.set("externalSystemId", externalSystemIdProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("sql");
    required.add("externalSystemId");
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

    // validate the external system is configured, there is no default
    String externalSystemError = validateExternalSystemAllowed(externalSystemId);
    if (externalSystemError != null) {
      return buildErrorResult(externalSystemError);
    }

    // the external system ID is the database connection name
    String externalSystem = externalSystemId.trim();

    if (countOnly) {
      return executeCount(sql, externalSystem, authUser);
    }

    return executeSelect(sql, pageSize, pageNumber, externalSystem, authUser);
  }

  /**
   * execute a count-only query by wrapping the SQL in SELECT COUNT(*)
   * @param sql the original SELECT query
   * @param externalSystem the connection name
   * @param authUser the authenticated user, to decide if errors include a stack trace
   * @return the MCP tool result with the count
   */
  private static ObjectNode executeCount(String sql, String externalSystem,
      GrouperMcpAuthUser authUser) {
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
      return buildErrorResult("Error executing SQL count query: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * execute a paged SELECT query and return results as JSON
   * @param sql the SELECT query
   * @param pageSize the page size
   * @param pageNumber the 1-based page number
   * @param externalSystem the connection name
   * @param authUser the authenticated user, to decide if errors include a stack trace
   * @return the MCP tool result with rows
   */
  private static ObjectNode executeSelect(String sql, int pageSize, int pageNumber,
      String externalSystem, GrouperMcpAuthUser authUser) {

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
      return buildErrorResult("Error executing SQL query: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * pattern which matches the config keys that can make an external system available to
   * the MCP SQL tools, e.g. grouper.mcp.sql.hr_db.sqlTablesViews.  the external system ID
   * is also the grouperClient.jdbc connection name which is used.
   */
  private static final Pattern EXTERNAL_SYSTEM_CONFIG_PATTERN = Pattern.compile(
      "^grouper\\.mcp\\.sql\\.([^.]+)\\.(grouperDatabase|sqlTablesViews|sqlTablesViewsQuery)$");

  /**
   * the external system IDs available to the MCP SQL tools.  no database is available by
   * default, including the Grouper database.  one is available when it has tables the AI
   * can discover, which means sqlTablesViews or sqlTablesViewsQuery is configured for it,
   * or grouperDatabase is true so the tables come from the built-in Grouper DDL.  the
   * other grouper.mcp.sql.&lt;id&gt;.* properties describe a system, they do not make one
   * available on their own, since a system with no tables is of no use to the AI.
   * @return the IDs, empty if none are configured
   */
  static Set<String> externalSystemIds() {

    Set<String> externalSystemIds = new LinkedHashSet<String>();

    Set<String> propertyNames = GrouperConfig.retrieveConfig().propertyNames();
    for (String key : propertyNames) {
      Matcher matcher = EXTERNAL_SYSTEM_CONFIG_PATTERN.matcher(key);
      if (matcher.matches()) {
        String id = matcher.group(1);
        if ("grouperDatabase".equals(matcher.group(2))) {
          // grouperDatabase = false says this is not a Grouper database, it does not make
          // one available
          if (isGrouperDatabase(id)) {
            externalSystemIds.add(id);
          }
        } else if (StringUtils.isNotBlank(
            GrouperConfig.retrieveConfig().propertyValueString(key, ""))) {
          externalSystemIds.add(id);
        }
      }
    }

    return externalSystemIds;
  }

  /**
   * if any database is configured.  when none is, the SQL tools are not advertised to the
   * AI client, since there is nothing they could query.
   * @return true if at least one external system is configured
   */
  static boolean anyConfigured() {
    return !externalSystemIds().isEmpty();
  }

  /**
   * the message sent to the AI client when the administrator has made no database
   * available at all, which is how MCP ships
   * @return the message
   */
  static String noDatabasesConfiguredMessage() {
    return "No databases are configured for MCP SQL queries. The administrator must make "
        + "each database available with grouper.mcp.sql.<externalSystemId>.sqlTablesViews "
        + "or .sqlTablesViewsQuery, or with .grouperDatabase = true for a Grouper "
        + "database. The Grouper database is not available by default.";
  }

  /**
   * validate that the external system in the request is available.  there is no default,
   * so a blank external system ID is an error.
   * @param externalSystemId the external system ID from the request
   * @return null if allowed, the error message to send to the AI client if not
   */
  static String validateExternalSystemAllowed(String externalSystemId) {

    Set<String> externalSystemIds = externalSystemIds();

    if (externalSystemIds.isEmpty()) {
      return noDatabasesConfiguredMessage();
    }

    if (StringUtils.isBlank(externalSystemId)) {
      return "externalSystemId is required, there is no default database. "
          + "Available external systems: " + StringUtils.join(externalSystemIds, ", ")
          + ". Use sql_get_schema with action 'listExternalSystems' for details on each one.";
    }

    String id = externalSystemId.trim();

    if (externalSystemIds.contains(id)) {
      return null;
    }

    return "External system '" + id + "' is not configured for MCP SQL queries. "
        + "Available external systems: " + StringUtils.join(externalSystemIds, ", ") + ". "
        + "The administrator makes another one available with grouper.mcp.sql." + id
        + ".sqlTablesViews or .sqlTablesViewsQuery, or .grouperDatabase = true for a "
        + "Grouper database.";
  }

  /**
   * if the external system points at a Grouper database, in which case schema discovery
   * uses the built-in Grouper DDL as the base list of tables and views.
   * @param externalSystemId the external system ID
   * @return true if this is a Grouper database
   */
  static boolean isGrouperDatabase(String externalSystemId) {

    return GrouperConfig.retrieveConfig().propertyValueBoolean(
        "grouper.mcp.sql." + StringUtils.trim(externalSystemId) + ".grouperDatabase", false);
  }

  /**
   * the schema which holds the Grouper tables and views, when it is not the connection's
   * default schema.  the AI is shown schema qualified names so the SQL it writes finds them.
   * @param externalSystemId the external system ID
   * @return the schema, or blank if none is configured
   */
  static String grouperDatabaseSchema(String externalSystemId) {

    return GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.sql." + StringUtils.trim(externalSystemId) + ".grouperDatabaseSchema", "");
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

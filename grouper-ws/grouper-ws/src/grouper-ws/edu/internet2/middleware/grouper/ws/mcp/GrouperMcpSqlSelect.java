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
 * Grouper database.  Only SELECT statements are allowed; DML and DDL are
 * rejected.  Results are returned as a JSON array of row objects.
 * Uses paging (pageSize/pageNumber) and a read-only JDBC connection
 * for defense-in-depth.  The database connection can be configured to
 * point to a read replica via grouper.mcp.sqlGrouperExternalSystem.
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
        "Execute a read-only SQL SELECT query against the Grouper database and return "
        + "the results as a JSON array of row objects. Only SELECT statements are allowed. "
        + "Results are paged; use pageSize (default " + DEFAULT_PAGE_SIZE
        + ", max " + MAX_ROWS + ") and pageNumber (1-based, default 1) to page through "
        + "large result sets. An ORDER BY clause is required when paging beyond page 1 "
        + "to ensure deterministic results. "
        + "Use sql_select_count first to check total row count. "
        + "Use sql_get_schema to discover table and view names and their columns.");

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

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of rows per page (default " + DEFAULT_PAGE_SIZE + ", max " + MAX_ROWS + "). "
        + "Use grouperSqlSelectCount first to check total rows if the query may return many rows.");
    properties.set("pageSize", pageSizeProp);

    ObjectNode pageNumberProp = objectMapper.createObjectNode();
    pageNumberProp.put("type", "integer");
    pageNumberProp.put("description",
        "Page number, 1-based (default 1). Use with pageSize to page through large result sets. "
        + "The SQL query must include an ORDER BY clause when using pageNumber > 1.");
    properties.set("pageNumber", pageNumberProp);

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
    int pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt(DEFAULT_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
    int pageNumber = arguments != null && arguments.has("pageNumber")
        ? arguments.get("pageNumber").asInt(1) : 1;

    if (StringUtils.isBlank(sql)) {
      return buildErrorResult("sql is required.");
    }

    // enforce page size limits
    if (pageSize < 1 || pageSize > MAX_ROWS) {
      pageSize = DEFAULT_PAGE_SIZE;
    }

    // enforce page number minimum
    if (pageNumber < 1) {
      pageNumber = 1;
    }

    // validate SQL is read-only
    String validationError = validateReadOnlySql(sql);
    if (validationError != null) {
      return buildErrorResult(validationError);
    }

    // if paging beyond page 1, require an ORDER BY clause so results are deterministic
    if (pageNumber > 1 && !sql.toUpperCase().contains("ORDER BY")) {
      return buildErrorResult(
          "When using paging (pageNumber > 1), the SQL query must include an ORDER BY clause "
          + "so that results are deterministic across pages. Without ORDER BY, rows may be "
          + "duplicated or skipped between pages.");
    }

    // get the external system (connection name) for SQL tools
    String externalSystem = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sqlGrouperExternalSystem", "grouper");

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

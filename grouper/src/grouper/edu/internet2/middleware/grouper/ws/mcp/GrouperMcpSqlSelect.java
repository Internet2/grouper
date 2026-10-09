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
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcConnectionCallback;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionEnd;

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

  /** default page size.  public so the tool registration can declare it, see GrouperTool.pageSizeWhenNotGiven */
  public static final int DEFAULT_PAGE_SIZE = 500;

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
    String externalSystemError = validateExternalSystemAllowed(externalSystemId, authUser);
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
      long count = runInReadOnlyTransaction(externalSystem, new GcTransactionCallback<Long>() {

        @Override
        public Long callback(GcDbAccess dbAccess) {
          return new GcDbAccess()
              .connectionName(externalSystem)
              .queryTimeoutSeconds(queryTimeoutSeconds())
              .sql(countSql)
              .select(Long.class);
        }
      });

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
      final int thePageNumber = pageNumber;
      final int thePageSize = pageSize;
      List<? extends Map<String, Object>> rows = runInReadOnlyTransaction(externalSystem,
          new GcTransactionCallback<List<? extends Map<String, Object>>>() {

        @Override
        public List<? extends Map<String, Object>> callback(GcDbAccess dbAccess) {
          return new GcDbAccess()
              .connectionName(externalSystem)
              .queryTimeoutSeconds(queryTimeoutSeconds())
              .paging(thePageNumber, thePageSize)
              .sql(sql)
              .selectListMap();
        }
      });

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
  public static boolean anyConfigured() {
    return !externalSystemIds().isEmpty();
  }

  /**
   * the external system IDs this caller may query.  a caller on the all tier of sql gets every
   * configured database.  a caller on the limited tier only gets those whose
   * grouper.mcp.sql.&lt;id&gt;.limitedAccessGroup they are in (GRP-7415).  anybody else gets none
   * @param authUser the caller
   * @return the IDs, empty if none
   */
  static Set<String> externalSystemIdsFor(GrouperMcpAuthUser authUser) {
    Set<String> result = new LinkedHashSet<String>();
    for (String id : externalSystemIds()) {
      if (GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.sql,
          GrouperToolAccess.SQL_CONFIG_PREFIX, id, authUser)) {
        result.add(id);
      }
    }
    return result;
  }

  /**
   * if any database is available to this caller.  when none is, the SQL tools are not
   * advertised to them
   * @param authUser the caller
   * @return true if at least one external system is available to the caller
   */
  public static boolean anyAvailableFor(GrouperMcpAuthUser authUser) {
    return !externalSystemIdsFor(authUser).isEmpty();
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
   * validate that the external system in the request is available to this caller.  there is
   * no default, so a blank external system ID is an error.
   * @param externalSystemId the external system ID from the request
   * @param authUser the caller.  a caller who is not on the all tier only gets the databases
   * opened to them on the limited tier, and is not told about any others
   * @return null if allowed, the error message to send to the AI client if not
   */
  static String validateExternalSystemAllowed(String externalSystemId,
      GrouperMcpAuthUser authUser) {

    boolean allTier = isAllTier(authUser);

    // the databases this caller may query.  for the all tier that is every configured database
    Set<String> externalSystemIds = externalSystemIdsFor(authUser);

    if (externalSystemIds.isEmpty()) {
      return allTier ? noDatabasesConfiguredMessage() : NONE_AVAILABLE_MESSAGE;
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

    // a caller who is not on the all tier gets the same answer whether or not a database by that
    // name is configured, so they cannot find out which are, and only hears about their own
    if (!allTier) {
      return "External system '" + id + "' is not available to you for MCP SQL queries. "
          + "Available external systems: " + StringUtils.join(externalSystemIds, ", ") + ".";
    }

    return "External system '" + id + "' is not configured for MCP SQL queries. "
        + "Available external systems: " + StringUtils.join(externalSystemIds, ", ") + ". "
        + "The administrator makes another one available with grouper.mcp.sql." + id
        + ".sqlTablesViews or .sqlTablesViewsQuery, or .grouperDatabase = true for a "
        + "Grouper database.";
  }

  /** what a caller who is not on the all tier is told when no database is opened to them */
  static final String NONE_AVAILABLE_MESSAGE = "No databases are available to you for MCP SQL "
      + "queries. Your Grouper administrator can make a database available to you.";

  /**
   * whether the caller is on the all tier of sql, so may query every configured database
   * @param authUser the caller
   * @return true if on the all tier
   */
  static boolean isAllTier(GrouperMcpAuthUser authUser) {
    return GrouperToolAccess.isAllTier(GrouperToolCategory.sql,
        authUser == null ? null : authUser.getSubject());
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
   * seconds before an MCP SQL query is cancelled, so a slow or sleeping query does not hold a connection
   * @return the timeout, or null for no timeout
   */
  static Integer queryTimeoutSeconds() {
    int seconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.sql.queryTimeoutSeconds", 120);
    return seconds > 0 ? seconds : null;
  }

  /**
   * run MCP SQL in a read-only transaction which is always rolled back.  The keyword checks in
   * validateReadOnlySql() are not enough on their own: setReadOnly(true) on an autocommit connection is not
   * enforced by the postgres driver (readOnlyMode=transaction) or by oracle, so a SELECT that writes
   * (SELECT ... INTO, setval(), etc) would otherwise be committed.  On postgres, oracle, and mysql this runs
   * SET TRANSACTION READ ONLY first so the database itself refuses writes, on others it uses setReadOnly(true)
   * @param externalSystem the connection name
   * @param query runs the query, must use a new GcDbAccess with this connection name (it joins the transaction)
   * @return what the query returns
   */
  static <T> T runInReadOnlyTransaction(final String externalSystem, final GcTransactionCallback<T> query) {
    return new GcDbAccess().connectionName(externalSystem).callbackTransaction(new GcTransactionCallback<T>() {

      @Override
      public T callback(GcDbAccess dbAccess) {
        final boolean[] previousReadOnly = new boolean[] {false};
        final boolean[] setReadOnly = new boolean[] {false};
        try {
          // this is the first statement of the transaction, as these databases require
          new GcDbAccess().connectionName(externalSystem).callbackConnection(new GcConnectionCallback<Void>() {

            @Override
            public Void callback(java.sql.Connection connection) {
              try {
                String productName = StringUtils.defaultString(connection.getMetaData().getDatabaseProductName()).toLowerCase();
                if (productName.contains("postgres") || productName.contains("oracle") || productName.contains("mysql")
                    || productName.contains("mariadb")) {
                  try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute("SET TRANSACTION READ ONLY");
                  }
                } else {
                  previousReadOnly[0] = connection.isReadOnly();
                  connection.setReadOnly(true);
                  setReadOnly[0] = true;
                }
              } catch (java.sql.SQLException sqle) {
                throw new RuntimeException("Cannot start a read-only transaction for MCP SQL", sqle);
              }
              return null;
            }
          });
          return query.callback(dbAccess);
        } finally {
          // never commit anything the query might have done.  endOnlyIfStarted is false since this joined the
          // transaction (it did not start it), the outer callbackTransaction then commits an empty transaction
          GcDbAccess.transactionEnd(GcTransactionEnd.rollback, false, externalSystem);
          if (setReadOnly[0]) {
            new GcDbAccess().connectionName(externalSystem).callbackConnection(new GcConnectionCallback<Void>() {

              @Override
              public Void callback(java.sql.Connection connection) {
                try {
                  connection.setReadOnly(previousReadOnly[0]);
                } catch (Exception e) {
                  LOG.debug("Cannot reset read only on connection", e);
                }
                return null;
              }
            });
          }
        }
      }
    });
  }

  /**
   * pattern for dangerous SQL keywords (word boundaries, case-insensitive).
   * these keywords should not appear as standalone words in a read-only query.
   * INTO (SELECT ... INTO new_table creates a table on postgres), OUTFILE / DUMPFILE (mysql writes a file)
   */
  private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
      "\\b(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE|GRANT|REVOKE|MERGE|CALL|EXEC|INTO|OUTFILE|DUMPFILE)\\b",
      Pattern.CASE_INSENSITIVE);

  /**
   * pattern for function calls that change state, read server files, kill sessions, or sleep, which a read-only
   * transaction does not always stop (e.g. pg_terminate_backend, dblink_exec which uses its own connection).
   * Only matches a call, i.e. the name followed by a paren, so a column named e.g. sleep is fine
   */
  private static final Pattern DANGEROUS_SQL_FUNCTION_PATTERN = Pattern.compile(
      "\\b(pg_terminate_backend|pg_cancel_backend|pg_sleep\\w*|pg_read_\\w+|pg_ls_\\w+|pg_stat_file|pg_reload_conf"
      + "|pg_rotate_logfile|pg_switch_wal|pg_promote|pg_\\w*advisory\\w*|pg_create_\\w+|pg_drop_\\w+|pg_logical_\\w+"
      + "|lo_\\w+|dblink\\w*|setval|nextval|set_config|dbms_\\w+|utl_\\w+|sleep|benchmark|load_file|get_lock"
      + "|release_lock)\\s*\\(",
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

    // check for dangerous function calls
    Matcher functionMatcher = DANGEROUS_SQL_FUNCTION_PATTERN.matcher(normalized);
    if (functionMatcher.find()) {
      return "SQL query calls a prohibited function: " + functionMatcher.group(1)
          + ". Only read-only SELECT statements are allowed.";
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

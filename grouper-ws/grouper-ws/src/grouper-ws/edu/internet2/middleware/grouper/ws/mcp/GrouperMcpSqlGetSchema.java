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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcConnectionCallback;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * MCP tool handler for retrieving database schema (DDL) information.
 * Loads the vendor-appropriate DDL install script from the classpath
 * and returns either a list of table/view names or the DDL for a specific table or view.
 * Additionally, if {@code grouper.mcp.sqlGrouperTablesViews} is configured with a
 * comma-separated list of extra table/view names, those will be included in the schema
 * results with column metadata and comments retrieved from the database.
 *
 * @author mchyzer
 */
public class GrouperMcpSqlGetSchema {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpSqlGetSchema.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** cached DDL content (loaded once from classpath) */
  private static String cachedDdlContent = null;

  /**
   * cache for extra table/view metadata.
   * key is the lowercased table/view name, value is the metadata string (pseudo-DDL with comments).
   * caches for 1 hour (3600 seconds).
   */
  private static GrouperCache<String, String> extraTableMetadataCache =
      new GrouperCache<String, String>(
          GrouperMcpSqlGetSchema.class.getName() + ".extraTableMetadataCache",
          500, false, 3600, 3600, false);

  /**
   * cache for the extra table/view names list.
   * key is "extraTableNames", value is the list of names.
   * caches for 1 hour (3600 seconds).
   */
  private static GrouperCache<String, List<String>> extraTableNamesCache =
      new GrouperCache<String, List<String>>(
          GrouperMcpSqlGetSchema.class.getName() + ".extraTableNamesCache",
          10, false, 3600, 3600, false);

  /**
   * return the MCP tool definition for sql_get_schema
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "sql_get_schema");
    tool.put("description",
        "Get Grouper database schema information. "
        + "Without a tableName parameter, returns the list of all table and view names "
        + "(including any additional tables/views configured by the administrator). "
        + "With a tableName parameter, returns the full CREATE TABLE or CREATE VIEW DDL "
        + "for that specific table or view, or column metadata with comments for "
        + "administrator-configured extra tables/views.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode tableNameProp = objectMapper.createObjectNode();
    tableNameProp.put("type", "string");
    tableNameProp.put("description",
        "Optional table or view name to get DDL for. "
        + "If omitted, returns the list of all table and view names. "
        + "Case-insensitive.");
    properties.set("tableName", tableNameProp);

    inputSchema.set("properties", properties);
    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the sql_get_schema tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String tableName = arguments != null && arguments.has("tableName")
        ? arguments.get("tableName").asText() : null;

    try {
      String ddlContent = loadDdlContent();
      if (ddlContent == null) {
        return buildErrorResult("Could not load DDL schema file from classpath.");
      }

      if (StringUtils.isBlank(tableName)) {
        // return list of all table and view names (including extras)
        return listTablesAndViews(ddlContent);
      } else {
        // return DDL for a specific table or view
        return getTableDdl(ddlContent, tableName.trim());
      }

    } catch (Exception e) {
      LOG.error("Error getting schema info", e);
      return buildErrorResult("Error getting schema info: " + e.getMessage());
    }
  }

  /**
   * load the DDL content from the classpath, cached after first load
   * @return the DDL content string, or null if not found
   */
  private static synchronized String loadDdlContent() {
    if (cachedDdlContent != null) {
      return cachedDdlContent;
    }

    String dbType;
    if (GrouperDdlUtils.isPostgres()) {
      dbType = "postgres";
    } else if (GrouperDdlUtils.isOracle()) {
      dbType = "oracle";
    } else if (GrouperDdlUtils.isMysql()) {
      dbType = "mysql";
    } else {
      LOG.error("Unknown database type for DDL schema loading");
      return null;
    }

    String resourceName = "ddl/GrouperDdl_Grouper_install_" + dbType + ".sql";
    try {
      cachedDdlContent = GrouperUtil.readResourceIntoString(resourceName, true);
      return cachedDdlContent;
    } catch (Exception e) {
      LOG.error("Error loading DDL resource: " + resourceName, e);
      return null;
    }
  }

  /**
   * pattern to match CREATE TABLE and CREATE VIEW/CREATE OR REPLACE VIEW statements
   */
  private static final Pattern CREATE_TABLE_PATTERN =
      Pattern.compile("^CREATE\\s+TABLE\\s+(\\S+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  private static final Pattern CREATE_VIEW_PATTERN =
      Pattern.compile("^CREATE(?:\\s+OR\\s+REPLACE)?\\s+VIEW\\s+(\\S+)",
          Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  /**
   * get the list of extra table/view names from config.
   * the list is parsed from the comma-separated config property and cached for 1 hour.
   * @return the list of lowercased extra table/view names, or empty list if none configured
   */
  private static List<String> getExtraTableNames() {
    List<String> cached = extraTableNamesCache.get("extraTableNames");
    if (cached != null) {
      return cached;
    }

    String configValue = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sqlGrouperTablesViews", "");

    List<String> names = new ArrayList<String>();
    if (StringUtils.isNotBlank(configValue)) {
      String[] parts = configValue.split(",");
      for (String part : parts) {
        String trimmed = part.trim();
        if (StringUtils.isNotBlank(trimmed)) {
          names.add(trimmed.toLowerCase());
        }
      }
    }

    extraTableNamesCache.put("extraTableNames", names);
    return names;
  }

  /**
   * list all table and view names from the DDL content, plus any extras from config
   */
  private static ObjectNode listTablesAndViews(String ddlContent) throws Exception {
    List<String> tableNames = new ArrayList<String>();
    List<String> viewNames = new ArrayList<String>();

    Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(ddlContent);
    while (tableMatcher.find()) {
      tableNames.add(tableMatcher.group(1).toLowerCase());
    }

    Matcher viewMatcher = CREATE_VIEW_PATTERN.matcher(ddlContent);
    while (viewMatcher.find()) {
      viewNames.add(viewMatcher.group(1).toLowerCase());
    }

    // add extra tables/views from config
    List<String> extraNames = getExtraTableNames();
    List<String> extraTableNames = new ArrayList<String>();
    List<String> extraViewNames = new ArrayList<String>();
    if (!extraNames.isEmpty()) {
      categorizeExtraTablesViews(extraNames, extraTableNames, extraViewNames);
      for (String name : extraTableNames) {
        if (!tableNames.contains(name)) {
          tableNames.add(name);
        }
      }
      for (String name : extraViewNames) {
        if (!viewNames.contains(name)) {
          viewNames.add(name);
        }
      }
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("tableCount", tableNames.size());
    resultNode.put("viewCount", viewNames.size());

    ArrayNode tablesArray = objectMapper.createArrayNode();
    for (String name : tableNames) {
      tablesArray.add(name);
    }
    resultNode.set("tables", tablesArray);

    ArrayNode viewsArray = objectMapper.createArrayNode();
    for (String name : viewNames) {
      viewsArray.add(name);
    }
    resultNode.set("views", viewsArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
  }

  /**
   * categorize extra table/view names into tables and views by checking the database.
   * uses JDBC metadata to determine whether each name is a TABLE or a VIEW.
   * names that cannot be found are treated as tables.
   * @param extraNames the list of extra names
   * @param extraTableNames output list for tables
   * @param extraViewNames output list for views
   */
  private static void categorizeExtraTablesViews(final List<String> extraNames,
      final List<String> extraTableNames, final List<String> extraViewNames) {

    String externalSystem = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sqlGrouperExternalSystem", "grouper");

    try {
      new GcDbAccess().connectionName(externalSystem).readOnly(true)
          .callbackConnection(new GcConnectionCallback<Object>() {
        @Override
        public Object callback(Connection connection) {
          try {
            DatabaseMetaData metaData = connection.getMetaData();
            for (String name : extraNames) {
              String tableType = getTableType(metaData, name);
              if ("VIEW".equalsIgnoreCase(tableType)) {
                extraViewNames.add(name);
              } else {
                extraTableNames.add(name);
              }
            }
          } catch (Exception e) {
            LOG.error("Error categorizing extra tables/views", e);
            // fall back to treating all as tables
            for (String name : extraNames) {
              if (!extraTableNames.contains(name) && !extraViewNames.contains(name)) {
                extraTableNames.add(name);
              }
            }
          }
          return null;
        }
      });
    } catch (Exception e) {
      LOG.error("Error connecting to database for table categorization", e);
      // fall back to treating all as tables
      for (String name : extraNames) {
        extraTableNames.add(name);
      }
    }
  }

  /**
   * get the table type (TABLE or VIEW) for a given name from JDBC metadata.
   * tries the name as-is, then uppercase, then lowercase.
   * @param metaData the database metadata
   * @param tableName the table/view name
   * @return the table type string, or "TABLE" if not found
   */
  private static String getTableType(DatabaseMetaData metaData, String tableName) {
    try {
      // try the name as-is
      ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE", "VIEW"});
      try {
        if (rs.next()) {
          return rs.getString("TABLE_TYPE");
        }
      } finally {
        rs.close();
      }
      // try uppercase
      rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE", "VIEW"});
      try {
        if (rs.next()) {
          return rs.getString("TABLE_TYPE");
        }
      } finally {
        rs.close();
      }
      // try lowercase
      rs = metaData.getTables(null, null, tableName.toLowerCase(), new String[]{"TABLE", "VIEW"});
      try {
        if (rs.next()) {
          return rs.getString("TABLE_TYPE");
        }
      } finally {
        rs.close();
      }
    } catch (Exception e) {
      LOG.warn("Error getting table type for " + tableName, e);
    }
    return "TABLE";
  }

  /**
   * get the DDL for a specific table or view.
   * first checks the DDL file, then checks if it is an extra table/view from config.
   * extracts from CREATE TABLE/VIEW to the next CREATE TABLE/VIEW or end of file.
   */
  private static ObjectNode getTableDdl(String ddlContent, String tableName) throws Exception {

    // build pattern to find CREATE TABLE or CREATE VIEW for this specific table
    String escapedName = Pattern.quote(tableName);
    Pattern specificPattern = Pattern.compile(
        "^(CREATE(?:\\s+OR\\s+REPLACE)?\\s+(?:TABLE|VIEW)\\s+" + escapedName + "\\b.*?)(?=^CREATE\\s|\\Z)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    Matcher matcher = specificPattern.matcher(ddlContent);
    if (matcher.find()) {
      String ddl = matcher.group(1).trim();
      return buildSuccessResult(ddl);
    }

    // check if this is an extra table/view from config
    List<String> extraNames = getExtraTableNames();
    String lowerTableName = tableName.toLowerCase();
    if (extraNames.contains(lowerTableName)) {
      String metadata = getExtraTableMetadata(lowerTableName);
      if (metadata != null) {
        return buildSuccessResult(metadata);
      }
      return buildErrorResult("Could not retrieve metadata for table or view '"
          + tableName + "' from the database.");
    }

    return buildErrorResult("Table or view '" + tableName
        + "' not found in the DDL schema. Use sql_get_schema without a tableName "
        + "parameter to see all available tables and views.");
  }

  /**
   * get metadata for an extra table/view, with 1-hour caching.
   * retrieves column names, types, nullable, and comments from the database.
   * @param tableNameLower the lowercased table/view name
   * @return the metadata string, or null if it could not be retrieved
   */
  private static String getExtraTableMetadata(String tableNameLower) {

    String cached = extraTableMetadataCache.get(tableNameLower);
    if (cached != null) {
      return cached;
    }

    String externalSystem = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sqlGrouperExternalSystem", "grouper");

    try {
      String metadata = new GcDbAccess().connectionName(externalSystem).readOnly(true)
          .callbackConnection(new GcConnectionCallback<String>() {
        @Override
        public String callback(Connection connection) {
          try {
            return buildExtraTableMetadata(connection, tableNameLower);
          } catch (Exception e) {
            LOG.error("Error building metadata for " + tableNameLower, e);
            return null;
          }
        }
      });

      if (metadata != null) {
        extraTableMetadataCache.put(tableNameLower, metadata);
      }
      return metadata;
    } catch (Exception e) {
      LOG.error("Error retrieving metadata for " + tableNameLower, e);
      return null;
    }
  }

  /**
   * build metadata string for an extra table/view from JDBC metadata and database comments.
   * @param connection the JDBC connection
   * @param tableNameLower the lowercased table/view name
   * @return the metadata string
   */
  private static String buildExtraTableMetadata(Connection connection, String tableNameLower)
      throws Exception {

    DatabaseMetaData metaData = connection.getMetaData();
    String dbProductName = connection.getMetaData().getDatabaseProductName();
    boolean isPostgres = dbProductName != null && dbProductName.toLowerCase().contains("postgresql");
    boolean isOracle = dbProductName != null && dbProductName.toLowerCase().contains("oracle");

    // determine the actual table name casing in the database
    String actualTableName = resolveActualTableName(metaData, tableNameLower);
    if (actualTableName == null) {
      return null;
    }

    // get the table type
    String tableType = getTableType(metaData, actualTableName);

    // get table/view comment
    String tableComment = null;
    if (isPostgres) {
      tableComment = getPostgresTableComment(connection, actualTableName);
    } else if (isOracle) {
      tableComment = getOracleTableComment(connection, actualTableName);
    }

    // get columns
    List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
    ResultSet colRs = metaData.getColumns(null, null, actualTableName, null);
    try {
      while (colRs.next()) {
        Map<String, String> col = new LinkedHashMap<String, String>();
        col.put("name", colRs.getString("COLUMN_NAME"));
        col.put("type", colRs.getString("TYPE_NAME"));
        int size = colRs.getInt("COLUMN_SIZE");
        int decimalDigits = colRs.getInt("DECIMAL_DIGITS");
        if (size > 0) {
          col.put("size", String.valueOf(size));
        }
        if (decimalDigits > 0) {
          col.put("decimalDigits", String.valueOf(decimalDigits));
        }
        col.put("nullable", "YES".equals(colRs.getString("IS_NULLABLE")) ? "true" : "false");
        String remarks = colRs.getString("REMARKS");
        if (StringUtils.isNotBlank(remarks)) {
          col.put("comment", remarks);
        }
        columns.add(col);
      }
    } finally {
      colRs.close();
    }

    // if JDBC didn't return column comments (some drivers don't), try DB-specific queries
    if (isPostgres || isOracle) {
      Map<String, String> colComments;
      if (isPostgres) {
        colComments = getPostgresColumnComments(connection, actualTableName);
      } else {
        colComments = getOracleColumnComments(connection, actualTableName);
      }
      for (Map<String, String> col : columns) {
        String colName = col.get("name");
        if (!col.containsKey("comment") || StringUtils.isBlank(col.get("comment"))) {
          String comment = colComments.get(colName.toLowerCase());
          if (comment == null) {
            comment = colComments.get(colName.toUpperCase());
          }
          if (comment == null) {
            comment = colComments.get(colName);
          }
          if (StringUtils.isNotBlank(comment)) {
            col.put("comment", comment);
          }
        }
      }
    }

    // build the result JSON
    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("name", tableNameLower);
    resultNode.put("type", tableType);
    if (StringUtils.isNotBlank(tableComment)) {
      resultNode.put("comment", tableComment);
    }
    resultNode.put("source", "database_metadata");

    ArrayNode columnsArray = objectMapper.createArrayNode();
    for (Map<String, String> col : columns) {
      ObjectNode colNode = objectMapper.createObjectNode();
      for (Map.Entry<String, String> entry : col.entrySet()) {
        colNode.put(entry.getKey(), entry.getValue());
      }
      columnsArray.add(colNode);
    }
    resultNode.set("columns", columnsArray);

    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
  }

  /**
   * resolve the actual table name casing from the database.
   * tries as-is, uppercase, lowercase.
   * @param metaData the database metadata
   * @param tableNameLower the lowercased table name
   * @return the actual table name, or null if not found
   */
  private static String resolveActualTableName(DatabaseMetaData metaData, String tableNameLower)
      throws Exception {

    // try lowercase
    ResultSet rs = metaData.getTables(null, null, tableNameLower, new String[]{"TABLE", "VIEW"});
    try {
      if (rs.next()) {
        return rs.getString("TABLE_NAME");
      }
    } finally {
      rs.close();
    }

    // try uppercase
    rs = metaData.getTables(null, null, tableNameLower.toUpperCase(), new String[]{"TABLE", "VIEW"});
    try {
      if (rs.next()) {
        return rs.getString("TABLE_NAME");
      }
    } finally {
      rs.close();
    }

    return null;
  }

  /**
   * get the table/view comment from PostgreSQL using pg_catalog.
   * @param connection the JDBC connection
   * @param tableName the table name (actual casing)
   * @return the comment string, or null
   */
  private static String getPostgresTableComment(Connection connection, String tableName) {
    try {
      String sql = "SELECT obj_description(c.oid) AS table_comment "
          + "FROM pg_catalog.pg_class c "
          + "JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "
          + "WHERE c.relname = ? AND n.nspname = current_schema()";
      java.sql.PreparedStatement ps = connection.prepareStatement(sql);
      try {
        ps.setString(1, tableName);
        ResultSet rs = ps.executeQuery();
        try {
          if (rs.next()) {
            return rs.getString("table_comment");
          }
        } finally {
          rs.close();
        }
      } finally {
        ps.close();
      }
    } catch (Exception e) {
      LOG.warn("Error getting PostgreSQL table comment for " + tableName, e);
    }
    return null;
  }

  /**
   * get column comments from PostgreSQL using pg_catalog.
   * @param connection the JDBC connection
   * @param tableName the table name (actual casing)
   * @return map of lowercased column name to comment
   */
  private static Map<String, String> getPostgresColumnComments(Connection connection, String tableName) {
    Map<String, String> comments = new LinkedHashMap<String, String>();
    try {
      String sql = "SELECT a.attname AS column_name, "
          + "col_description(c.oid, a.attnum) AS column_comment "
          + "FROM pg_catalog.pg_class c "
          + "JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "
          + "JOIN pg_catalog.pg_attribute a ON a.attrelid = c.oid "
          + "WHERE c.relname = ? AND n.nspname = current_schema() "
          + "AND a.attnum > 0 AND NOT a.attisdropped";
      java.sql.PreparedStatement ps = connection.prepareStatement(sql);
      try {
        ps.setString(1, tableName);
        ResultSet rs = ps.executeQuery();
        try {
          while (rs.next()) {
            String colName = rs.getString("column_name");
            String comment = rs.getString("column_comment");
            if (StringUtils.isNotBlank(comment)) {
              comments.put(colName.toLowerCase(), comment);
            }
          }
        } finally {
          rs.close();
        }
      } finally {
        ps.close();
      }
    } catch (Exception e) {
      LOG.warn("Error getting PostgreSQL column comments for " + tableName, e);
    }
    return comments;
  }

  /**
   * get the table/view comment from Oracle using ALL_TAB_COMMENTS.
   * @param connection the JDBC connection
   * @param tableName the table name (actual casing)
   * @return the comment string, or null
   */
  private static String getOracleTableComment(Connection connection, String tableName) {
    try {
      String sql = "SELECT comments FROM all_tab_comments "
          + "WHERE table_name = ? AND owner = SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')";
      java.sql.PreparedStatement ps = connection.prepareStatement(sql);
      try {
        ps.setString(1, tableName.toUpperCase());
        ResultSet rs = ps.executeQuery();
        try {
          if (rs.next()) {
            return rs.getString("comments");
          }
        } finally {
          rs.close();
        }
      } finally {
        ps.close();
      }
    } catch (Exception e) {
      LOG.warn("Error getting Oracle table comment for " + tableName, e);
    }
    return null;
  }

  /**
   * get column comments from Oracle using ALL_COL_COMMENTS.
   * @param connection the JDBC connection
   * @param tableName the table name (actual casing)
   * @return map of uppercased column name to comment
   */
  private static Map<String, String> getOracleColumnComments(Connection connection, String tableName) {
    Map<String, String> comments = new LinkedHashMap<String, String>();
    try {
      String sql = "SELECT column_name, comments FROM all_col_comments "
          + "WHERE table_name = ? AND owner = SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')";
      java.sql.PreparedStatement ps = connection.prepareStatement(sql);
      try {
        ps.setString(1, tableName.toUpperCase());
        ResultSet rs = ps.executeQuery();
        try {
          while (rs.next()) {
            String colName = rs.getString("column_name");
            String comment = rs.getString("comments");
            if (StringUtils.isNotBlank(comment)) {
              comments.put(colName.toUpperCase(), comment);
            }
          }
        } finally {
          rs.close();
        }
      } finally {
        ps.close();
      }
    } catch (Exception e) {
      LOG.warn("Error getting Oracle column comments for " + tableName, e);
    }
    return comments;
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

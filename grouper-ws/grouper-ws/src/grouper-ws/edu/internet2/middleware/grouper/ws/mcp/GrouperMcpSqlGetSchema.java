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
import java.util.Set;
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
 * Supports three actions:
 * <ul>
 *   <li>{@code listExternalSystems} - list available external system IDs</li>
 *   <li>{@code listTables} - list table/view names for an external system</li>
 *   <li>{@code tableInfo} - get DDL or column metadata for a specific table</li>
 * </ul>
 * No database is available by default.  Each external system is made available with
 * grouper.mcp.sql.&lt;externalSystemId&gt;.* properties, and the external system ID is
 * also the database connection name.  For one flagged as a Grouper database
 * (grouperDatabase = true), the built-in DDL provides the base schema and extra
 * tables/views from config are additive.  For other external systems, the table list
 * comes entirely from the per-system config.
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
   * key is "externalSystem__tableName", value is the metadata string (pseudo-DDL with comments).
   * caches for 1 hour (3600 seconds).
   */
  private static GrouperCache<String, String> extraTableMetadataCache =
      new GrouperCache<String, String>(
          GrouperMcpSqlGetSchema.class.getName() + ".extraTableMetadataCache",
          500, false, 3600, 3600, false);

  /**
   * cache for the extra table/view names list.
   * key is "extraTableNames__externalSystemId", value is the list of names.
   * caches for 1 hour (3600 seconds).
   */
  private static GrouperCache<String, List<String>> extraTableNamesCache =
      new GrouperCache<String, List<String>>(
          GrouperMcpSqlGetSchema.class.getName() + ".extraTableNamesCache",
          50, false, 3600, 3600, false);

  /**
   * return the MCP tool definition for sql_get_schema
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "sql_get_schema");
    tool.put("description",
        "Get database schema information. Use action 'listExternalSystems' to discover "
        + "which database connections the Grouper administrator has made available; there "
        + "is no default database, so start here. "
        + "Use action 'listTables' with an externalSystemId to list table and view names. "
        + "Use action 'tableInfo' with an externalSystemId and tableName to get the full "
        + "CREATE TABLE/VIEW DDL or column metadata for a specific table or view. "
        + "Table and view names are returned as they should be written in SQL, so when a "
        + "name comes back schema qualified, use it qualified in your queries.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("listExternalSystems");
    actionEnum.add("listTables");
    actionEnum.add("tableInfo");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The action to perform. 'listExternalSystems' returns available database connections. "
        + "'listTables' returns table/view names for an external system. "
        + "'tableInfo' returns DDL or column metadata for a specific table.");
    properties.set("action", actionProp);

    ObjectNode externalSystemIdProp = objectMapper.createObjectNode();
    externalSystemIdProp.put("type", "string");
    externalSystemIdProp.put("description",
        "External system ID identifying which database to look at. "
        + "Required for the 'listTables' and 'tableInfo' actions; there is no default. "
        + "Use 'listExternalSystems' to discover available IDs.");
    properties.set("externalSystemId", externalSystemIdProp);

    ObjectNode tableNameProp = objectMapper.createObjectNode();
    tableNameProp.put("type", "string");
    tableNameProp.put("description",
        "Table or view name to get info for. Required for 'tableInfo' action. "
        + "Case-insensitive.");
    properties.set("tableName", tableNameProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    inputSchema.set("required", required);

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

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;
    String tableName = arguments != null && arguments.has("tableName")
        ? arguments.get("tableName").asText() : null;
    String externalSystemId = arguments != null && arguments.has("externalSystemId")
        ? arguments.get("externalSystemId").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required. Use 'listExternalSystems', "
          + "'listTables', or 'tableInfo'.");
    }

    try {
      if ("listExternalSystems".equals(action)) {
        return listExternalSystems();
      }

      // listTables and tableInfo require an external system which is configured for MCP
      String externalSystemError =
          GrouperMcpSqlSelect.validateExternalSystemAllowed(externalSystemId);
      if (externalSystemError != null) {
        return buildErrorResult(externalSystemError);
      }

      // the external system ID is both the config key and the database connection name
      String configId = externalSystemId.trim();

      boolean isGrouperDb = GrouperMcpSqlSelect.isGrouperDatabase(configId);

      // the schema the Grouper tables live in, when it is not the connection's default
      String grouperDatabaseSchema = GrouperMcpSqlSelect.grouperDatabaseSchema(configId);

      if ("listTables".equals(action)) {
        if (isGrouperDb) {
          String ddlContent = loadDdlContent();
          if (ddlContent == null) {
            return buildErrorResult("Could not load DDL schema file from classpath.");
          }
          return listTablesAndViews(ddlContent, configId, grouperDatabaseSchema);
        } else {
          return listExternalSystemTablesAndViews(configId);
        }

      } else if ("tableInfo".equals(action)) {
        if (StringUtils.isBlank(tableName)) {
          return buildErrorResult("tableName is required for 'tableInfo' action.");
        }
        if (isGrouperDb) {
          String ddlContent = loadDdlContent();
          if (ddlContent == null) {
            return buildErrorResult("Could not load DDL schema file from classpath.");
          }
          return getTableDdl(ddlContent, tableName.trim(), configId, grouperDatabaseSchema);
        } else {
          return getExternalSystemTableDdl(tableName.trim(), configId);
        }

      } else {
        return buildErrorResult("Unknown action '" + action + "'. Use 'listExternalSystems', "
            + "'listTables', or 'tableInfo'.");
      }

    } catch (Exception e) {
      LOG.error("Error getting schema info", e);
      return buildErrorResult("Error getting schema info: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * list the external system IDs the administrator has made available to the MCP SQL
   * tools.  no database is available by default, including the Grouper database.
   * @return the MCP tool result with the list of external systems
   */
  private static ObjectNode listExternalSystems() throws Exception {

    Set<String> externalSystemIds = GrouperMcpSqlSelect.externalSystemIds();

    if (externalSystemIds.isEmpty()) {
      return buildSuccessResult(GrouperMcpSqlSelect.noDatabasesConfiguredMessage());
    }

    ArrayNode systemsArray = objectMapper.createArrayNode();
    for (String id : externalSystemIds) {
      ObjectNode systemNode = objectMapper.createObjectNode();
      systemNode.put("id", id);
      boolean isGrouperDb = GrouperMcpSqlSelect.isGrouperDatabase(id);
      systemNode.put("isGrouperDb", isGrouperDb);

      // only a Grouper database has names built from the DDL for the schema to qualify.
      // everything else is named exactly as the administrator configured it
      if (isGrouperDb) {
        String grouperDatabaseSchema = GrouperMcpSqlSelect.grouperDatabaseSchema(id);
        if (StringUtils.isNotBlank(grouperDatabaseSchema)) {
          systemNode.put("tableSchema", grouperDatabaseSchema);
        }
      }

      String documentation = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.mcp.sql." + id + ".documentationForAiClient", "");
      if (StringUtils.isNotBlank(documentation)) {
        systemNode.put("documentation", documentation);
      }

      systemsArray.add(systemNode);
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.set("externalSystems", systemsArray);

    String resultText = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(resultNode);
    return buildSuccessResult(resultText);
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
   * get the list of extra table/view names from config for a given external system.
   * first checks grouper.mcp.sql.&lt;configId&gt;.sqlTablesViews (comma-separated),
   * then grouper.mcp.sql.&lt;configId&gt;.sqlTablesViewsQuery (SQL query).
   * the result is cached for 1 hour keyed by config ID.
   * @param configId the external system ID, which is also the database connection name
   * @return the list of lowercased extra table/view names, or empty list if none configured
   */
  private static List<String> getExtraTableNames(String configId) {
    String cacheKey = "extraTableNames__" + configId;
    List<String> cached = extraTableNamesCache.get(cacheKey);
    if (cached != null) {
      return cached;
    }

    List<String> names = new ArrayList<String>();

    // first try comma-separated list
    String configValue = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sql." + configId + ".sqlTablesViews", "");

    if (StringUtils.isNotBlank(configValue)) {
      String[] parts = configValue.split(",");
      for (String part : parts) {
        String trimmed = part.trim();
        if (StringUtils.isNotBlank(trimmed)) {
          names.add(trimmed.toLowerCase());
        }
      }
    }

    // then try SQL query (additive if both are configured)
    String queryConfig = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.sql." + configId + ".sqlTablesViewsQuery", "");

    if (StringUtils.isNotBlank(queryConfig)) {
      try {
        List<String> queryResults = new GcDbAccess()
            .connectionName(configId)
            .readOnly(true)
            .sql(queryConfig)
            .selectList(String.class);
        for (String name : queryResults) {
          if (StringUtils.isNotBlank(name)) {
            String lowerName = name.trim().toLowerCase();
            if (!names.contains(lowerName)) {
              names.add(lowerName);
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Error executing sqlTablesViewsQuery for external system '"
            + configId + "': " + queryConfig, e);
      }
    }

    extraTableNamesCache.put(cacheKey, names);
    return names;
  }

  /**
   * list all table and view names from the DDL content, plus any extras from config.
   * used for the Grouper database external system.
   * @param ddlContent the loaded DDL content
   * @param configId the external system ID, which is also the database connection name
   * @param grouperDatabaseSchema the schema to qualify DDL names with, blank for none
   */
  private static ObjectNode listTablesAndViews(String ddlContent, String configId,
      String grouperDatabaseSchema) throws Exception {
    List<String> tableNames = new ArrayList<String>();
    List<String> viewNames = new ArrayList<String>();

    Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(ddlContent);
    while (tableMatcher.find()) {
      tableNames.add(qualify(grouperDatabaseSchema, tableMatcher.group(1).toLowerCase()));
    }

    Matcher viewMatcher = CREATE_VIEW_PATTERN.matcher(ddlContent);
    while (viewMatcher.find()) {
      viewNames.add(qualify(grouperDatabaseSchema, viewMatcher.group(1).toLowerCase()));
    }

    // add extra tables/views from config (additive for grouper)
    List<String> extraNames = getExtraTableNames(configId);
    List<String> extraTableNames = new ArrayList<String>();
    List<String> extraViewNames = new ArrayList<String>();
    if (!extraNames.isEmpty()) {
      categorizeExtraTablesViews(extraNames, extraTableNames, extraViewNames, configId);
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
    if (StringUtils.isNotBlank(grouperDatabaseSchema)) {
      resultNode.put("tableSchema", grouperDatabaseSchema);
    }

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
   * prefix a table or view name with the schema it lives in, so the SQL the AI writes
   * finds it.  a name which is already qualified, and the case where no schema is
   * configured, are returned unchanged.
   * @param grouperDatabaseSchema the configured schema, blank for none
   * @param tableName the table or view name
   * @return the name the AI should use in SQL
   */
  private static String qualify(String grouperDatabaseSchema, String tableName) {
    if (StringUtils.isBlank(grouperDatabaseSchema) || tableName.contains(".")) {
      return tableName;
    }
    return grouperDatabaseSchema.trim() + "." + tableName;
  }

  /**
   * remove the configured schema from the front of a name, since the AI is shown
   * qualified names but the DDL holds unqualified ones.  a name without the prefix is
   * returned unchanged, so the AI can ask by either form.
   * @param grouperDatabaseSchema the configured schema, blank for none
   * @param tableName the table or view name as the AI asked for it
   * @return the name to look for in the DDL
   */
  private static String stripSchema(String grouperDatabaseSchema, String tableName) {
    if (StringUtils.isBlank(grouperDatabaseSchema)) {
      return tableName;
    }
    String prefix = grouperDatabaseSchema.trim() + ".";
    if (tableName.regionMatches(true, 0, prefix, 0, prefix.length())) {
      return tableName.substring(prefix.length());
    }
    return tableName;
  }

  /**
   * list all table and view names for a non-Grouper external system.
   * the table list comes entirely from config (sqlTablesViews or sqlTablesViewsQuery).
   * @param configId the external system ID, which is also the database connection name
   */
  private static ObjectNode listExternalSystemTablesAndViews(String configId)
      throws Exception {

    List<String> extraNames = getExtraTableNames(configId);
    if (extraNames.isEmpty()) {
      return buildErrorResult("No tables or views are configured for external system '"
          + configId + "'. The administrator must configure grouper.mcp.sql."
          + configId + ".sqlTablesViews or grouper.mcp.sql." + configId
          + ".sqlTablesViewsQuery.");
    }

    List<String> tableNames = new ArrayList<String>();
    List<String> viewNames = new ArrayList<String>();

    categorizeExtraTablesViews(extraNames, tableNames, viewNames, configId);

    ObjectNode resultNode = objectMapper.createObjectNode();
    resultNode.put("externalSystemId", configId);
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
   * @param externalSystem the external system connection name
   */
  private static void categorizeExtraTablesViews(final List<String> extraNames,
      final List<String> extraTableNames, final List<String> extraViewNames,
      final String externalSystem) {

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
   * get the DDL for a specific table or view from the Grouper database.
   * first checks the DDL file, then checks if it is an extra table/view from config.
   * extracts from CREATE TABLE/VIEW to the next CREATE TABLE/VIEW or end of file.
   * @param ddlContent the loaded DDL content
   * @param tableName the table or view name
   * @param configId the external system ID, which is also the database connection name
   * @param grouperDatabaseSchema the schema the tables live in, blank for none
   */
  private static ObjectNode getTableDdl(String ddlContent, String tableName,
      String configId, String grouperDatabaseSchema) throws Exception {

    // the AI is shown schema qualified names, the DDL holds unqualified ones.  accept
    // either form so it can ask by the name it was given or by the bare table name
    String ddlTableName = stripSchema(grouperDatabaseSchema, tableName);

    // build pattern to find CREATE TABLE or CREATE VIEW for this specific table
    String escapedName = Pattern.quote(ddlTableName);
    Pattern specificPattern = Pattern.compile(
        "^(CREATE(?:\\s+OR\\s+REPLACE)?\\s+(?:TABLE|VIEW)\\s+" + escapedName + "\\b.*?)(?=^CREATE\\s|\\Z)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    Matcher matcher = specificPattern.matcher(ddlContent);
    if (matcher.find()) {
      String ddl = matcher.group(1).trim();
      // the DDL says the bare name, so say which name to actually query it by
      if (StringUtils.isNotBlank(grouperDatabaseSchema)) {
        ddl = "-- query this as " + qualify(grouperDatabaseSchema, ddlTableName.toLowerCase())
            + "\n" + ddl;
      }
      return buildSuccessResult(ddl);
    }

    // check if this is an extra table/view from config
    List<String> extraNames = getExtraTableNames(configId);
    String lowerTableName = tableName.toLowerCase();
    if (extraNames.contains(lowerTableName)) {
      String metadata = getExtraTableMetadata(lowerTableName, configId);
      if (metadata != null) {
        return buildSuccessResult(metadata);
      }
      return buildErrorResult("Could not retrieve metadata for table or view '"
          + tableName + "' from the database.");
    }

    return buildErrorResult("Table or view '" + tableName
        + "' not found in the DDL schema. Use sql_get_schema with action 'listTables' "
        + "to see all available tables and views.");
  }

  /**
   * get the DDL for a specific table or view from a non-Grouper external system.
   * the table must be in the configured table list for the external system.
   * @param tableName the table or view name
   * @param configId the external system ID, which is also the database connection name
   */
  private static ObjectNode getExternalSystemTableDdl(String tableName, String configId)
      throws Exception {

    List<String> extraNames = getExtraTableNames(configId);
    String lowerTableName = tableName.toLowerCase();
    if (extraNames.contains(lowerTableName)) {
      String metadata = getExtraTableMetadata(lowerTableName, configId);
      if (metadata != null) {
        return buildSuccessResult(metadata);
      }
      return buildErrorResult("Could not retrieve metadata for table or view '"
          + tableName + "' from external system '" + configId + "'.");
    }

    return buildErrorResult("Table or view '" + tableName
        + "' not found in external system '" + configId
        + "'. Use sql_get_schema with externalSystemId='" + configId
        + "' without a tableName parameter to see all available tables and views.");
  }

  /**
   * get metadata for an extra table/view, with 1-hour caching.
   * retrieves column names, types, nullable, and comments from the database.
   * @param tableNameLower the lowercased table/view name
   * @param configId the external system ID, which is also the database connection name
   * @return the metadata string, or null if it could not be retrieved
   */
  private static String getExtraTableMetadata(final String tableNameLower,
      final String configId) {

    String cacheKey = configId + "__" + tableNameLower;
    String cached = extraTableMetadataCache.get(cacheKey);
    if (cached != null) {
      return cached;
    }

    try {
      String metadata = new GcDbAccess().connectionName(configId).readOnly(true)
          .callbackConnection(new GcConnectionCallback<String>() {
        @Override
        public String callback(Connection connection) {
          try {
            return buildExtraTableMetadata(connection, tableNameLower);
          } catch (Exception e) {
            LOG.error("Error building metadata for " + tableNameLower
                + " on external system " + configId, e);
            return null;
          }
        }
      });

      if (metadata != null) {
        extraTableMetadataCache.put(cacheKey, metadata);
      }
      return metadata;
    } catch (Exception e) {
      LOG.error("Error retrieving metadata for " + tableNameLower
          + " on external system " + configId, e);
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

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
package edu.internet2.middleware.grouper.mcp;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

/**
 * ORM class for the grouper_mcp_tool_log table.
 * Audit log of MCP tool calls including request, response, timing, and error info.
 *
 * @author mchyzer
 */
@GcPersistableClass(tableName = "grouper_mcp_tool_log", defaultFieldPersist = GcPersist.doPersist)
public class GrouperMcpToolLog implements GcSqlAssignPrimaryKey {

  // -------- table and column name constants --------

  public static final String TABLE_GROUPER_MCP_TOOL_LOG = "grouper_mcp_tool_log";

  public static final String COLUMN_INTERNAL_ID = "internal_id";

  public static final String COLUMN_OAUTH_CLIENT_INTERNAL_ID = "oauth_client_internal_id";

  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  public static final String COLUMN_TOOL_NAME = "tool_name";

  public static final String COLUMN_TOOL_CATEGORY = "tool_category";

  public static final String COLUMN_REQUEST = "request";

  public static final String COLUMN_RESPONSE_OR_ERROR = "response_or_error";

  public static final String COLUMN_IS_ERROR = "is_error";

  public static final String COLUMN_STARTED_MICROS = "started_micros";

  public static final String COLUMN_DURATION_MICROS = "duration_micros";

  // -------- tool category constants --------

  public static final String CATEGORY_READONLY = "readonly";

  public static final String CATEGORY_READWRITE = "readwrite";

  public static final String CATEGORY_SQL = "sql";

  public static final String CATEGORY_ADMIN_READONLY = "admin_readonly";

  public static final String CATEGORY_ADMIN_READWRITE = "admin_readwrite";

  // -------- fields --------

  @GcPersistableField(primaryKey = true, primaryKeyManuallyAssigned = true, columnName = "internal_id")
  private long internalId = -1;

  @GcPersistableField(columnName = "oauth_client_internal_id")
  private Long oauthClientInternalId;

  @GcPersistableField(columnName = "member_internal_id")
  private Long memberInternalId;

  @GcPersistableField(columnName = "tool_name")
  private String toolName;

  @GcPersistableField(columnName = "tool_category")
  private String toolCategory;

  @GcPersistableField(columnName = "request")
  private String request;

  @GcPersistableField(columnName = "response_or_error")
  private String responseOrError;

  @GcPersistableField(columnName = "is_error")
  private String isError;

  @GcPersistableField(columnName = "started_micros")
  private long startedMicros;

  @GcPersistableField(columnName = "duration_micros")
  private Long durationMicros;

  // -------- primary key assignment --------

  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.mcpToolLog);
    return true;
  }

  // -------- tool category mapping --------

  /**
   * return the tool category for the given tool name.
   * @param toolName the tool name
   * @return the tool category string
   */
  public static String getToolCategory(String toolName) {
    if (toolName == null) {
      return CATEGORY_READONLY;
    }
    switch (toolName) {
      case "attribute_assignment_get":
      case "attribute_def_name_find":
      case "audit_get":
      case "entity_get":
      case "entity_get_groups":
      case "folder_find":
      case "group_find":
      case "group_get_members":
      case "group_has_member":
      case "memberships_get":
      case "privilege_get":
        return CATEGORY_READONLY;
      case "attribute_assignment_save":
      case "group_add_member":
      case "group_remove_member":
      case "group_save":
      case "privilege_assign":
        return CATEGORY_READWRITE;
      case "sql_get_schema":
      case "sql_select":
      case "sql_select_count":
        return CATEGORY_SQL;
      case "admin_config_search":
      case "admin_daemon_job_message":
      case "admin_daemon_logs":
      case "admin_daemon_names":
        return CATEGORY_ADMIN_READONLY;
      case "admin_daemon_job_run":
        return CATEGORY_ADMIN_READWRITE;
      default:
        return CATEGORY_READONLY;
    }
  }

  // -------- getters and setters --------

  /**
   * @return the internal id (primary key)
   */
  public long getInternalId() {
    return this.internalId;
  }

  /**
   * @param internalId1 the internal id
   */
  public void setInternalId(long internalId1) {
    this.internalId = internalId1;
  }

  /**
   * @return the oauth client internal id (soft link to grouper_oauth_client)
   */
  public Long getOauthClientInternalId() {
    return this.oauthClientInternalId;
  }

  /**
   * @param oauthClientInternalId1 the oauth client internal id
   */
  public void setOauthClientInternalId(Long oauthClientInternalId1) {
    this.oauthClientInternalId = oauthClientInternalId1;
  }

  /**
   * @return the member internal id
   */
  public Long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * @param memberInternalId1 the member internal id
   */
  public void setMemberInternalId(Long memberInternalId1) {
    this.memberInternalId = memberInternalId1;
  }

  /**
   * @return the tool name
   */
  public String getToolName() {
    return this.toolName;
  }

  /**
   * @param toolName1 the tool name
   */
  public void setToolName(String toolName1) {
    this.toolName = toolName1;
  }

  /**
   * @return the tool category
   */
  public String getToolCategory() {
    return this.toolCategory;
  }

  /**
   * @param toolCategory1 the tool category
   */
  public void setToolCategory(String toolCategory1) {
    this.toolCategory = toolCategory1;
  }

  /**
   * @return the request JSON arguments (truncated to 4000 chars)
   */
  public String getRequest() {
    return this.request;
  }

  /**
   * @param request1 the request JSON arguments
   */
  public void setRequest(String request1) {
    this.request = request1;
  }

  /**
   * @return the response text or error message (truncated to 4000 chars)
   */
  public String getResponseOrError() {
    return this.responseOrError;
  }

  /**
   * @param responseOrError1 the response text or error message
   */
  public void setResponseOrError(String responseOrError1) {
    this.responseOrError = responseOrError1;
  }

  /**
   * @return "T" if error, "F" otherwise
   */
  public String getIsError() {
    return this.isError;
  }

  /**
   * @param isError1 "T" or "F"
   */
  public void setIsError(String isError1) {
    this.isError = isError1;
  }

  /**
   * @return micros since 1970 when the tool call started
   */
  public long getStartedMicros() {
    return this.startedMicros;
  }

  /**
   * @param startedMicros1 micros since 1970
   */
  public void setStartedMicros(long startedMicros1) {
    this.startedMicros = startedMicros1;
  }

  /**
   * @return duration of the tool call in microseconds
   */
  public Long getDurationMicros() {
    return this.durationMicros;
  }

  /**
   * @param durationMicros1 duration in microseconds
   */
  public void setDurationMicros(Long durationMicros1) {
    this.durationMicros = durationMicros1;
  }
}

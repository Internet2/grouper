/**
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
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthCode;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthPendingRequest;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;

/**
 * DDL for Grouper 7.0.0 OAuth and MCP tables.
 * @author mchyzer
 */
public class GrouperDdl7_0_0 {

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    boolean buildingToThisVersionAtLeast = GrouperDdl.V47.getVersion() <= buildingToVersion;
    return buildingToThisVersionAtLeast;
  }

  // ------- grouper_oauth_client -------

  /**
   * add grouper_oauth_client table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthClientTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthClientTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthClient.TABLE_GROUPER_OAUTH_CLIENT);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_CLIENT_ID,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_CLIENT_NAME,
        Types.VARCHAR, "255", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_REDIRECT_URIS,
        Types.VARCHAR, "4000", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_CLIENT_SECRET,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_REGISTERED_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_CODE_COUNT,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthClient.COLUMN_LAST_CODE_MICROS,
        Types.BIGINT, "20", false, false);
  }

  /**
   * add grouper_oauth_client indexes
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperOAuthClientIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthClientIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthClient.TABLE_GROUPER_OAUTH_CLIENT);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grouper_oauth_client_idx", true,
        GrouperOAuthClient.COLUMN_CLIENT_ID);
  }

  /**
   * add grouper_oauth_client comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthClientComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthClientComments", true)) {
      return;
    }

    final String tableName = GrouperOAuthClient.TABLE_GROUPER_OAUTH_CLIENT;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "table to store dynamically registered OAuth clients");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_INTERNAL_ID,
        "auto-incrementing bigint primary key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_CLIENT_ID,
        "unique OAuth client identifier");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_CLIENT_NAME,
        "display name for the client");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_REDIRECT_URIS,
        "comma-separated list of registered redirect URIs");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_CLIENT_SECRET,
        "encrypted client secret");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_REGISTERED_MICROS,
        "micros since 1970 when the client was registered");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_MEMBER_INTERNAL_ID,
        "member internal id of the first user who got an authorization code (nullable)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_CODE_COUNT,
        "number of authorization codes issued for this client");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthClient.COLUMN_LAST_CODE_MICROS,
        "micros since 1970 when the last authorization code was issued");
  }

  // ------- grouper_oauth_code -------

  /**
   * add grouper_oauth_code table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthCodeTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthCodeTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthCode.TABLE_GROUPER_OAUTH_CODE);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_CODE,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_REDIRECT_URI,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_CODE_CHALLENGE,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_CODE_CHALLENGE_METHOD,
        Types.VARCHAR, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_IS_USED,
        Types.VARCHAR, "1", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_CREATED_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_EXPIRES_MICROS,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthCode.COLUMN_CONSENT_DETAILS,
        Types.VARCHAR, "4000", false, false);
  }

  /**
   * add grouper_oauth_code indexes
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperOAuthCodeIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthCodeIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthCode.TABLE_GROUPER_OAUTH_CODE);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grouper_oauth_code_idx", true,
        GrouperOAuthCode.COLUMN_CODE);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grouper_oauth_code_exp_idx", false,
        GrouperOAuthCode.COLUMN_EXPIRES_MICROS);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_oauth_code_client_idx", false,
        GrouperOAuthCode.COLUMN_OAUTH_CLIENT_INTERNAL_ID);
  }

  /**
   * add grouper_oauth_code comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthCodeComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthCodeComments", true)) {
      return;
    }

    final String tableName = GrouperOAuthCode.TABLE_GROUPER_OAUTH_CODE;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "table to store issued OAuth authorization codes");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_INTERNAL_ID,
        "auto-incrementing bigint primary key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_CODE,
        "unique authorization code");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        "internal id from grouper_oauth_client (soft link, not a FK)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_REDIRECT_URI,
        "redirect URI provided in the authorization request");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_CODE_CHALLENGE,
        "PKCE code challenge (S256)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_CODE_CHALLENGE_METHOD,
        "PKCE code challenge method (S256)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_MEMBER_INTERNAL_ID,
        "member internal id of the user who approved");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_IS_USED,
        "T or F whether this code has been exchanged for a token");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_CREATED_MICROS,
        "micros since 1970 when the code was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_EXPIRES_MICROS,
        "micros since 1970 when the code expires");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthCode.COLUMN_CONSENT_DETAILS,
        "JSON object with consent details (granted scopes, etc.)");
  }

  // ------- grouper_oauth_pend_authz_req -------

  /**
   * add grouper_oauth_pend_authz_req table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthPendAuthzReqTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthPendAuthzReqTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthPendingRequest.TABLE_GROUPER_OAUTH_PEND_AUTHZ_REQ);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_REQUEST_ID,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_REDIRECT_URI,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_CODE_CHALLENGE,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_CODE_CHALLENGE_METHOD,
        Types.VARCHAR, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_STATE,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_SCOPE,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_CREATED_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperOAuthPendingRequest.COLUMN_EXPIRES_MICROS,
        Types.BIGINT, "20", false, false);
  }

  /**
   * add grouper_oauth_pend_authz_req indexes
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperOAuthPendAuthzReqIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthPendAuthzReqIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperOAuthPendingRequest.TABLE_GROUPER_OAUTH_PEND_AUTHZ_REQ);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_oauth_pend_req_idx", true,
        GrouperOAuthPendingRequest.COLUMN_REQUEST_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_oauth_pend_exp_idx", false,
        GrouperOAuthPendingRequest.COLUMN_EXPIRES_MICROS);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_oauth_pend_client_idx", false,
        GrouperOAuthPendingRequest.COLUMN_OAUTH_CLIENT_INTERNAL_ID);
  }

  /**
   * add grouper_oauth_pend_authz_req comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperOAuthPendAuthzReqComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperOAuthPendAuthzReqComments", true)) {
      return;
    }

    final String tableName = GrouperOAuthPendingRequest.TABLE_GROUPER_OAUTH_PEND_AUTHZ_REQ;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "table to store pending OAuth authorization requests awaiting user approval");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_INTERNAL_ID,
        "auto-incrementing bigint primary key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_REQUEST_ID,
        "unique public-facing request identifier");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        "internal id from grouper_oauth_client (soft link, not a FK)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_REDIRECT_URI,
        "redirect URI for the response");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_CODE_CHALLENGE,
        "PKCE code challenge (S256)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_CODE_CHALLENGE_METHOD,
        "PKCE code challenge method (S256)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_STATE,
        "OAuth state parameter passed through to redirect");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_SCOPE,
        "requested OAuth scope");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_CREATED_MICROS,
        "micros since 1970 when the request was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperOAuthPendingRequest.COLUMN_EXPIRES_MICROS,
        "micros since 1970 when the request expires");
  }

  // ------- grouper_mcp_tool_log -------

  /**
   * add grouper_mcp_tool_log table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperMcpToolLogTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperMcpToolLogTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperMcpToolLog.TABLE_GROUPER_MCP_TOOL_LOG);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_TOOL_NAME,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_TOOL_CATEGORY,
        Types.VARCHAR, "64", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_REQUEST,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_RESPONSE_OR_ERROR,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_IS_ERROR,
        Types.VARCHAR, "1", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_STARTED_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_DURATION_MICROS,
        Types.BIGINT, "20", false, false);
  }

  /**
   * add grouper_mcp_tool_log indexes
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperMcpToolLogIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperMcpToolLogIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperMcpToolLog.TABLE_GROUPER_MCP_TOOL_LOG);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_mcp_tool_log_member_idx", false,
        GrouperMcpToolLog.COLUMN_MEMBER_INTERNAL_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_mcp_tool_log_started_idx", false,
        GrouperMcpToolLog.COLUMN_STARTED_MICROS);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_mcp_tool_log_name_idx", false,
        GrouperMcpToolLog.COLUMN_TOOL_NAME);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_mcp_tool_log_oauth_idx", false,
        GrouperMcpToolLog.COLUMN_OAUTH_CLIENT_INTERNAL_ID);
  }

  /**
   * add grouper_mcp_tool_log comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperMcpToolLogComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_0_0_addGrouperMcpToolLogComments", true)) {
      return;
    }

    final String tableName = GrouperMcpToolLog.TABLE_GROUPER_MCP_TOOL_LOG;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "audit log of MCP tool calls including request, response, timing, and error info");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_INTERNAL_ID,
        "auto-incrementing bigint primary key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_OAUTH_CLIENT_INTERNAL_ID,
        "internal id from grouper_oauth_client (soft link, not a FK, so audits survive client deletion)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_MEMBER_INTERNAL_ID,
        "member internal id of the authenticated user who called the tool");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_TOOL_NAME,
        "name of the MCP tool that was called (e.g. findGroups, addMember)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_TOOL_CATEGORY,
        "category of the tool: readonly, readwrite, sql, admin_readonly, admin_readwrite");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_REQUEST,
        "JSON arguments of the tool call, truncated to 4000 chars");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_RESPONSE_OR_ERROR,
        "success result text or error message, truncated to 4000 chars");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_IS_ERROR,
        "T if the tool call resulted in an error, F otherwise");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_STARTED_MICROS,
        "micros since 1970 when the tool call started");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperMcpToolLog.COLUMN_DURATION_MICROS,
        "duration of the tool call in microseconds");
  }
}

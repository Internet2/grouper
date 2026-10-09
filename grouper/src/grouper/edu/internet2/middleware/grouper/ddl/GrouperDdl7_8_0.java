/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentCallLog;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentUsage;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;

/**
 * DDL for Grouper 7.8.0, for database compares: grouper_mcp_tool_log.entry_path and the
 * grouper_ai_agent_usage and grouper_ai_agent_call_log tables, for the AI agent in the Grouper UI.
 * existing installs get these from UpgradeTaskV46, new installs from the install DDL
 */
public class GrouperDdl7_8_0 {

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    // GrouperDdl.V47 is the latest and only active version
    return GrouperDdl.V47.getVersion() <= buildingToVersion;
  }

  // ------- grouper_mcp_tool_log.entry_path -------

  /**
   * add grouper_mcp_tool_log.entry_path.  the table is created by GrouperDdl7_0_0, which runs first
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperMcpToolLogEntryPath(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperMcpToolLogEntryPath", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperMcpToolLog.TABLE_GROUPER_MCP_TOOL_LOG);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperMcpToolLog.COLUMN_ENTRY_PATH,
        Types.VARCHAR, "32", false, false);
  }

  /**
   * add the grouper_mcp_tool_log.entry_path comment
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperMcpToolLogEntryPathComment(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperMcpToolLogEntryPathComment", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GrouperMcpToolLog.TABLE_GROUPER_MCP_TOOL_LOG, GrouperMcpToolLog.COLUMN_ENTRY_PATH,
        "which front door the call came through: mcp for an MCP client, ui for the AI agent in the Grouper UI.  null for calls logged before this column existed");
  }

  // ------- grouper_ai_agent_usage -------

  /**
   * add grouper_ai_agent_usage table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperAiAgentUsageTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentUsageTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperAiAgentUsage.TABLE_GROUPER_AI_AGENT_USAGE);

    // the primary key is the user and the day
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_USAGE_DAY,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_MESSAGE_COUNT,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_MODEL_CALL_COUNT,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_INPUT_TOKENS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_CACHED_INPUT_TOKENS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_OUTPUT_TOKENS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentUsage.COLUMN_LAST_UPDATED_MICROS,
        Types.BIGINT, "20", false, true);
  }

  /**
   * add grouper_ai_agent_usage index, for deleting old rows by day
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperAiAgentUsageIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentUsageIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperAiAgentUsage.TABLE_GROUPER_AI_AGENT_USAGE);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_ai_agent_usage_day_idx", false,
        GrouperAiAgentUsage.COLUMN_USAGE_DAY);
  }

  /**
   * add grouper_ai_agent_usage comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperAiAgentUsageComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentUsageComments", true)) {
      return;
    }

    final String tableName = GrouperAiAgentUsage.TABLE_GROUPER_AI_AGENT_USAGE;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "what each user of the AI agent in the Grouper UI used each day, for the daily token limit and for reporting");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_MEMBER_INTERNAL_ID,
        "member internal id of the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_USAGE_DAY,
        "the day, as yyyymmdd in grouper.ai.agent.timeZone, the server time zone if blank");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_MESSAGE_COUNT,
        "messages the user sent, which the daily question limit counts.  approving or declining changes does not count");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_MODEL_CALL_COUNT,
        "calls made to the AI provider");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_INPUT_TOKENS,
        "input tokens sent to the AI provider, including those read from its cache");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_CACHED_INPUT_TOKENS,
        "input tokens the AI provider read from its cache");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_OUTPUT_TOKENS,
        "output tokens the AI provider returned");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentUsage.COLUMN_LAST_UPDATED_MICROS,
        "micros since 1970 when this row last changed");
  }

  // ------- grouper_ai_agent_call_log -------

  /**
   * add grouper_ai_agent_call_log table
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperAiAgentCallLogTable(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentCallLogTable", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperAiAgentCallLog.TABLE_GROUPER_AI_AGENT_CALL_LOG);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_CONVERSATION_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_PROVIDER,
        Types.VARCHAR, "32", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_MODEL,
        Types.VARCHAR, "255", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_CALL_TYPE,
        Types.VARCHAR, "16", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_OUTCOME,
        Types.VARCHAR, "16", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_STOP_REASON,
        Types.VARCHAR, "32", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_TOOL_CALL_COUNT,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_INPUT_TOKENS,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_CACHED_INPUT_TOKENS,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_CACHE_WRITE_INPUT_TOKENS,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_OUTPUT_TOKENS,
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_PROVIDER_REQUEST_ID,
        Types.VARCHAR, "100", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_ERROR_SUMMARY,
        Types.VARCHAR, "1000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_STARTED_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperAiAgentCallLog.COLUMN_DURATION_MICROS,
        Types.BIGINT, "20", false, true);
  }

  /**
   * add grouper_ai_agent_call_log indexes: by time, for deleting old rows, and by user and time
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperAiAgentCallLogIndexes(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentCallLogIndexes", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperAiAgentCallLog.TABLE_GROUPER_AI_AGENT_CALL_LOG);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_ai_agent_call_started_idx", false,
        GrouperAiAgentCallLog.COLUMN_STARTED_MICROS);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "grp_ai_agent_call_member_idx", false,
        GrouperAiAgentCallLog.COLUMN_MEMBER_INTERNAL_ID, GrouperAiAgentCallLog.COLUMN_STARTED_MICROS);
  }

  /**
   * add grouper_ai_agent_call_log comments
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperAiAgentCallLogComments(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_8_0_addGrouperAiAgentCallLogComments", true)) {
      return;
    }

    final String tableName = GrouperAiAgentCallLog.TABLE_GROUPER_AI_AGENT_CALL_LOG;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        tableName,
        "one row per call the AI agent in the Grouper UI made to its AI provider, worked or failed, to match the provider bill call by call and trace failures.  no conversation text is kept");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_INTERNAL_ID,
        "primary key, from grouper_table_index");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_MEMBER_INTERNAL_ID,
        "member internal id of the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_CONVERSATION_ID,
        "id of the conversation in the UI session of the user, to group the calls of one conversation");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_PROVIDER,
        "the AI provider: anthropic or openai");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_MODEL,
        "the model asked for, from grouper.ai.agent.model");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_CALL_TYPE,
        "turn for a step of answering the user, summary for summarizing the older messages of a long conversation");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_OUTCOME,
        "ok if the provider answered, error if the call failed, contextTooLong if the provider said the conversation is too long for the model");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_STOP_REASON,
        "why the model stopped: endTurn, toolUse, maxTokens, refusal or other.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_TOOL_CALL_COUNT,
        "tools the model asked to run.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_INPUT_TOKENS,
        "input tokens sent to the AI provider, including those read from or written to its cache.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_CACHED_INPUT_TOKENS,
        "input tokens the AI provider read from its cache.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_CACHE_WRITE_INPUT_TOKENS,
        "input tokens the AI provider wrote to its cache, 0 if the provider does not report them.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_OUTPUT_TOKENS,
        "output tokens the AI provider returned.  null if the call failed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_PROVIDER_REQUEST_ID,
        "the id the AI provider gave the request, which its support can look up, or the call id of a gateway in front of it, from the headers in grouper.ai.agent.requestIdHeaders.  null if none was sent");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_ERROR_SUMMARY,
        "what went wrong: the HTTP status and the error type and code from the provider, or the java exception types.  no text from the response.  null if the call worked");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_STARTED_MICROS,
        "micros since 1970 when the call started");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        tableName, GrouperAiAgentCallLog.COLUMN_DURATION_MICROS,
        "how long the call took, in micros");
  }

}

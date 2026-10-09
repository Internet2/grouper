/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * v7 upgrade task that bundles the DDL changes shipping in 7.8.0 (one task per release; add new
 * release DDL here rather than creating another task).  a new install gets all of this from the
 * install DDL, not from this task.
 *
 * <p>the AI agent in the Grouper UI:</p>
 * <ul>
 *   <li>add grouper_mcp_tool_log.entry_path, which records which front door a tool call came
 *   through: mcp for an MCP client, ui for the AI agent in the Grouper UI.  the two share one tool
 *   layer and one log, and someone reading the log later needs to tell a call the user made
 *   themselves in the UI from one an MCP client made on their behalf.  nullable, since rows logged
 *   before this existed have no value.</li>
 *   <li>add grouper_ai_agent_usage, what each user of the AI agent used each day, for the daily
 *   token limit and for reporting.</li>
 *   <li>add grouper_ai_agent_call_log, one row per call the AI agent made to its AI provider, so a
 *   provider's bill can be matched call by call and failures traced.</li>
 * </ul>
 * <p>each piece is skipped if it is already there.</p>
 */
public class UpgradeTaskV46 implements UpgradeTasksInterface {

  /** the tool log table */
  private static final String TABLE_MCP_TOOL_LOG = "grouper_mcp_tool_log";

  /** the column being added */
  private static final String COLUMN_ENTRY_PATH = "entry_path";

  /** the AI agent usage table */
  private static final String TABLE_AI_AGENT_USAGE = "grouper_ai_agent_usage";

  /** the AI agent usage index, by day */
  private static final String INDEX_AI_AGENT_USAGE_DAY = "grp_ai_agent_usage_day_idx";

  /** the AI agent per call log */
  private static final String TABLE_AI_AGENT_CALL_LOG = "grouper_ai_agent_call_log";

  /** the AI agent per call log index, by time */
  private static final String INDEX_AI_AGENT_CALL_STARTED = "grp_ai_agent_call_started_idx";

  /** the AI agent per call log index, by user and time */
  private static final String INDEX_AI_AGENT_CALL_MEMBER = "grp_ai_agent_call_member_idx";

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.8.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    boolean workToDo = false;

    // the AI agent in the Grouper UI
    workToDo |= aiAgentHasWorkToDo();

    // (additional 7.8.0 DDL checks for this task can be OR-ed in here)

    return workToDo;
  }

  @Override
  public void updateVersionFromPrevious(final OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        aiAgentDdl(otherJobInput);
        return null;
      }
    });
  }

  /**
   * whether the AI agent's DDL still has anything to add
   * @return true if a column, table or index is missing
   */
  private static boolean aiAgentHasWorkToDo() {
    return entryPathHasWorkToDo()
        || !GrouperDdlUtils.assertTableThere(true, TABLE_AI_AGENT_USAGE)
        || !GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_USAGE, INDEX_AI_AGENT_USAGE_DAY)
        || !GrouperDdlUtils.assertTableThere(true, TABLE_AI_AGENT_CALL_LOG)
        || !GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_CALL_LOG, INDEX_AI_AGENT_CALL_STARTED)
        || !GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_CALL_LOG, INDEX_AI_AGENT_CALL_MEMBER);
  }

  /**
   * whether the tool log is there and still needs its entry_path column.  if the table itself is
   * missing there is nothing to add it to; UpgradeTaskV38 creates the table
   * @return true if the column still needs to be added
   */
  private static boolean entryPathHasWorkToDo() {
    if (!GrouperDdlUtils.assertTableThere(true, TABLE_MCP_TOOL_LOG)) {
      return false;
    }
    return !GrouperDdlUtils.assertColumnThere(true, TABLE_MCP_TOOL_LOG, COLUMN_ENTRY_PATH);
  }

  /**
   * the AI agent in the Grouper UI: grouper_mcp_tool_log.entry_path, and the
   * grouper_ai_agent_usage and grouper_ai_agent_call_log tables with their indexes and comments.
   * each piece is skipped if it is already there
   * @param otherJobInput
   */
  private static void aiAgentDdl(OtherJobInput otherJobInput) {

    // ==================== grouper_mcp_tool_log.entry_path ====================
    if (entryPathHasWorkToDo()) {

      if (GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("ALTER TABLE grouper_mcp_tool_log ADD entry_path VARCHAR2(32)").executeSql();
      } else {
        new GcDbAccess().sql("ALTER TABLE grouper_mcp_tool_log ADD COLUMN entry_path VARCHAR(32)").executeSql();
      }

      // mysql does not support COMMENT ON
      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_mcp_tool_log.entry_path IS 'which front door the call came through: mcp for an MCP client, ui for the AI agent in the Grouper UI.  null for calls logged before this column existed'").executeSql();
      }

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_mcp_tool_log.entry_path");
      }
    }

    // ==================== grouper_ai_agent_usage ====================
    if (!GrouperDdlUtils.assertTableThere(true, TABLE_AI_AGENT_USAGE)) {

      if (GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql(
            "CREATE TABLE grouper_ai_agent_usage ("
            + "  member_internal_id NUMBER(20) NOT NULL,"
            + "  usage_day NUMBER(20) NOT NULL,"
            + "  message_count NUMBER(20) NOT NULL,"
            + "  model_call_count NUMBER(20) NOT NULL,"
            + "  input_tokens NUMBER(20) NOT NULL,"
            + "  cached_input_tokens NUMBER(20) NOT NULL,"
            + "  output_tokens NUMBER(20) NOT NULL,"
            + "  last_updated_micros NUMBER(20) NOT NULL,"
            + "  PRIMARY KEY (member_internal_id, usage_day)"
            + ")"
        ).executeSql();
      } else {
        new GcDbAccess().sql(
            "CREATE TABLE grouper_ai_agent_usage ("
            + "  member_internal_id BIGINT NOT NULL,"
            + "  usage_day BIGINT NOT NULL,"
            + "  message_count BIGINT NOT NULL,"
            + "  model_call_count BIGINT NOT NULL,"
            + "  input_tokens BIGINT NOT NULL,"
            + "  cached_input_tokens BIGINT NOT NULL,"
            + "  output_tokens BIGINT NOT NULL,"
            + "  last_updated_micros BIGINT NOT NULL,"
            + "  PRIMARY KEY (member_internal_id, usage_day)"
            + ")"
        ).executeSql();
      }

      // mysql does not support COMMENT ON
      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("COMMENT ON TABLE grouper_ai_agent_usage IS 'what each user of the AI agent in the Grouper UI used each day, for the daily token limit and for reporting'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.member_internal_id IS 'member internal id of the user'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.usage_day IS 'the day, as yyyymmdd in grouper.ai.agent.timeZone, the server time zone if blank'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.message_count IS 'messages the user sent, which the daily question limit counts.  approving or declining changes does not count'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.model_call_count IS 'calls made to the AI provider'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.input_tokens IS 'input tokens sent to the AI provider, including those read from its cache'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.cached_input_tokens IS 'input tokens the AI provider read from its cache'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.output_tokens IS 'output tokens the AI provider returned'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_usage.last_updated_micros IS 'micros since 1970 when this row last changed'").executeSql();
      }

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_ai_agent_usage");
      }
    }

    if (!GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_USAGE, INDEX_AI_AGENT_USAGE_DAY)) {
      new GcDbAccess().sql(
          "CREATE INDEX grp_ai_agent_usage_day_idx ON grouper_ai_agent_usage (usage_day)"
      ).executeSql();
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_ai_agent_usage_day_idx");
      }
    }

    // ==================== grouper_ai_agent_call_log ====================
    if (!GrouperDdlUtils.assertTableThere(true, TABLE_AI_AGENT_CALL_LOG)) {

      if (GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql(
            "CREATE TABLE grouper_ai_agent_call_log ("
            + "  internal_id NUMBER(20) NOT NULL,"
            + "  member_internal_id NUMBER(20) NOT NULL,"
            + "  conversation_id VARCHAR2(40) NOT NULL,"
            + "  provider VARCHAR2(32) NOT NULL,"
            + "  model VARCHAR2(255) NOT NULL,"
            + "  call_type VARCHAR2(16) NOT NULL,"
            + "  outcome VARCHAR2(16) NOT NULL,"
            + "  stop_reason VARCHAR2(32),"
            + "  tool_call_count NUMBER(20),"
            + "  input_tokens NUMBER(20),"
            + "  cached_input_tokens NUMBER(20),"
            + "  cache_write_input_tokens NUMBER(20),"
            + "  output_tokens NUMBER(20),"
            + "  provider_request_id VARCHAR2(100),"
            + "  error_summary VARCHAR2(1000),"
            + "  started_micros NUMBER(20) NOT NULL,"
            + "  duration_micros NUMBER(20) NOT NULL,"
            + "  PRIMARY KEY (internal_id)"
            + ")"
        ).executeSql();
      } else {
        new GcDbAccess().sql(
            "CREATE TABLE grouper_ai_agent_call_log ("
            + "  internal_id BIGINT NOT NULL,"
            + "  member_internal_id BIGINT NOT NULL,"
            + "  conversation_id VARCHAR(40) NOT NULL,"
            + "  provider VARCHAR(32) NOT NULL,"
            + "  model VARCHAR(255) NOT NULL,"
            + "  call_type VARCHAR(16) NOT NULL,"
            + "  outcome VARCHAR(16) NOT NULL,"
            + "  stop_reason VARCHAR(32),"
            + "  tool_call_count BIGINT,"
            + "  input_tokens BIGINT,"
            + "  cached_input_tokens BIGINT,"
            + "  cache_write_input_tokens BIGINT,"
            + "  output_tokens BIGINT,"
            + "  provider_request_id VARCHAR(100),"
            + "  error_summary VARCHAR(1000),"
            + "  started_micros BIGINT NOT NULL,"
            + "  duration_micros BIGINT NOT NULL,"
            + "  PRIMARY KEY (internal_id)"
            + ")"
        ).executeSql();
      }

      // mysql does not support COMMENT ON
      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("COMMENT ON TABLE grouper_ai_agent_call_log IS 'one row per call the AI agent in the Grouper UI made to its AI provider, worked or failed, to match the provider bill call by call and trace failures.  no conversation text is kept'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.internal_id IS 'primary key, from grouper_table_index'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.member_internal_id IS 'member internal id of the user'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.conversation_id IS 'id of the conversation in the UI session of the user, to group the calls of one conversation'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.provider IS 'the AI provider: anthropic or openai'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.model IS 'the model asked for, from grouper.ai.agent.model'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.call_type IS 'turn for a step of answering the user, summary for summarizing the older messages of a long conversation'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.outcome IS 'ok if the provider answered, error if the call failed, contextTooLong if the provider said the conversation is too long for the model'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.stop_reason IS 'why the model stopped: endTurn, toolUse, maxTokens, refusal or other.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.tool_call_count IS 'tools the model asked to run.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.input_tokens IS 'input tokens sent to the AI provider, including those read from or written to its cache.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.cached_input_tokens IS 'input tokens the AI provider read from its cache.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.cache_write_input_tokens IS 'input tokens the AI provider wrote to its cache, 0 if the provider does not report them.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.output_tokens IS 'output tokens the AI provider returned.  null if the call failed'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.provider_request_id IS 'the id the AI provider gave the request, which its support can look up, or the call id of a gateway in front of it, from the headers in grouper.ai.agent.requestIdHeaders.  null if none was sent'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.error_summary IS 'what went wrong: the HTTP status and the error type and code from the provider, or the java exception types.  no text from the response.  null if the call worked'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.started_micros IS 'micros since 1970 when the call started'").executeSql();
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_ai_agent_call_log.duration_micros IS 'how long the call took, in micros'").executeSql();
      }

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_ai_agent_call_log");
      }
    }

    if (!GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_CALL_LOG, INDEX_AI_AGENT_CALL_STARTED)) {
      new GcDbAccess().sql(
          "CREATE INDEX grp_ai_agent_call_started_idx ON grouper_ai_agent_call_log (started_micros)"
      ).executeSql();
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_ai_agent_call_started_idx");
      }
    }

    if (!GrouperDdlUtils.assertIndexExists(TABLE_AI_AGENT_CALL_LOG, INDEX_AI_AGENT_CALL_MEMBER)) {
      new GcDbAccess().sql(
          "CREATE INDEX grp_ai_agent_call_member_idx ON grouper_ai_agent_call_log (member_internal_id, started_micros)"
      ).executeSql();
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_ai_agent_call_member_idx");
      }
    }
  }

}

/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * what each user's AI agent uses, per day, in grouper_ai_agent_usage, and the daily token limit
 * (grouper.ai.agent.maxTokensPerUserPerDay).
 *
 * <p>kept in the database because the UI runs on several nodes: every node adds to the same row,
 * and the database does the adding, so work on two nodes cannot overwrite each other's counts.
 * one row per user per day, the day in the agent's time zone (grouper.ai.agent.timeZone, the
 * server's if blank), so every node counts the same moment toward the same day.</p>
 *
 * <p>tokens counted against the limit are input tokens not read from the provider's cache, plus
 * output tokens.  cached input costs a fraction of the rest, so counting it in full would make a
 * long conversation look far more expensive than it is.</p>
 *
 * <p>checking fails closed and counting fails open.  if a limit is set and today's usage cannot be
 * read, the check throws and the agent refuses the message, since a limit an admin set should hold.
 * if recording fails, the error is logged and the turn carries on, since what was used is already
 * spent.  with no limit set the table is not read at all.</p>
 *
 * <p>each model call is also kept on its own in grouper_ai_agent_call_log, see
 * {@link GrouperAiAgentCallLog}.</p>
 */
public class GrouperAiAgentUsage implements GrouperAiAgentUsageRecorder {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAiAgentUsage.class);

  /** the table */
  public static final String TABLE_GROUPER_AI_AGENT_USAGE = "grouper_ai_agent_usage";

  /** member internal id of the user */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** the day, as yyyymmdd in the agent's time zone, see {@link #timeZone()} */
  public static final String COLUMN_USAGE_DAY = "usage_day";

  /** messages the user sent, which the daily question limit counts */
  public static final String COLUMN_MESSAGE_COUNT = "message_count";

  /** model calls made */
  public static final String COLUMN_MODEL_CALL_COUNT = "model_call_count";

  /** input tokens, including those read from the cache */
  public static final String COLUMN_INPUT_TOKENS = "input_tokens";

  /** input tokens read from the cache */
  public static final String COLUMN_CACHED_INPUT_TOKENS = "cached_input_tokens";

  /** output tokens */
  public static final String COLUMN_OUTPUT_TOKENS = "output_tokens";

  /** when the row last changed, micros since 1970 */
  public static final String COLUMN_LAST_UPDATED_MICROS = "last_updated_micros";

  /** the user */
  private long memberInternalId;

  /** most tokens a day, null for no limit */
  private Long maxTokensPerDay;

  /** most messages a day, null for no limit */
  private Long maxQuestionsPerDay;

  /**
   * @param theMemberInternalId the user
   * @param settings the settings, for the limits
   */
  public GrouperAiAgentUsage(long theMemberInternalId, GrouperAiAgentSettings settings) {
    this.memberInternalId = theMemberInternalId;
    this.maxTokensPerDay = settings.getMaxTokensPerUserPerDay();
    this.maxQuestionsPerDay = settings.getMaxQuestionsPerUserPerDay();
  }

  /**
   * @return today, as yyyymmdd in the agent's time zone, see {@link #timeZone()}
   */
  public static long today() {
    return Long.parseLong(dayFormat().format(new Date()));
  }

  /**
   * @return a formatter for days as the usage table keeps them, yyyymmdd in the agent's time zone
   */
  public static SimpleDateFormat dayFormat() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    simpleDateFormat.setTimeZone(timeZone());
    return simpleDateFormat;
  }

  /**
   * the time zone a day is counted in, for the daily limits, the usage table, the report and its
   * cleanup: grouper.ai.agent.timeZone, e.g. America/New_York, or the server's if blank.  set it
   * when UI nodes might not all run in the same time zone, or the same moment could count toward
   * different days on different nodes.  a value which is not a time zone is logged and the server's
   * is used, since this only decides when the day turns over
   * @return the time zone
   */
  public static TimeZone timeZone() {
    String timeZoneId = GrouperConfig.retrieveConfig().propertyValueString("grouper.ai.agent.timeZone");
    if (StringUtils.isBlank(timeZoneId)) {
      return TimeZone.getDefault();
    }
    try {
      // TimeZone.getTimeZone quietly gives GMT for a name it does not know, so check it first
      return TimeZone.getTimeZone(ZoneId.of(timeZoneId.trim()));
    } catch (RuntimeException re) {
      LOG.error("grouper.ai.agent.timeZone is not a time zone: '" + timeZoneId + "'.  Using the server's", re);
      return TimeZone.getDefault();
    }
  }

  /**
   * the tokens counted against the limit
   * @param inputTokens input tokens, including cached
   * @param cachedInputTokens input tokens read from the cache
   * @param outputTokens output tokens
   * @return input not read from the cache, plus output
   */
  public static long limitTokens(long inputTokens, long cachedInputTokens, long outputTokens) {
    return Math.max(0, inputTokens - cachedInputTokens) + outputTokens;
  }

  /**
   * a user's usage for a day
   * @param memberInternalId the user
   * @param usageDay the day, yyyymmdd
   * @return messages, model calls, input tokens, cached input tokens, output tokens, all 0 if there
   * is no row
   */
  public static long[] retrieveUsage(long memberInternalId, long usageDay) {
    List<Object[]> rows = new GcDbAccess()
        .sql("select " + COLUMN_MESSAGE_COUNT + ", " + COLUMN_MODEL_CALL_COUNT + ", " + COLUMN_INPUT_TOKENS
            + ", " + COLUMN_CACHED_INPUT_TOKENS + ", " + COLUMN_OUTPUT_TOKENS
            + " from " + TABLE_GROUPER_AI_AGENT_USAGE
            + " where " + COLUMN_MEMBER_INTERNAL_ID + " = ? and " + COLUMN_USAGE_DAY + " = ?")
        .addBindVar(memberInternalId)
        .addBindVar(usageDay)
        .selectList(Object[].class);
    long[] result = new long[5];
    if (rows != null && rows.size() > 0) {
      Object[] row = rows.get(0);
      for (int i = 0; i < 5; i++) {
        result[i] = GrouperUtil.longValue(row[i], 0);
      }
    }
    return result;
  }

  /**
   * @return the most tokens a day, null for no limit
   */
  public Long getMaxTokensPerDay() {
    return this.maxTokensPerDay;
  }

  /**
   * @see GrouperAiAgentUsageRecorder#isWithinLimit()
   */
  public boolean isWithinLimit() {
    if (this.maxTokensPerDay == null) {
      return true;
    }
    long[] usage = retrieveUsage(this.memberInternalId, today());
    return limitTokens(usage[2], usage[3], usage[4]) < this.maxTokensPerDay;
  }

  /**
   * @see GrouperAiAgentUsageRecorder#isWithinQuestionLimit()
   */
  public boolean isWithinQuestionLimit() {
    if (this.maxQuestionsPerDay == null) {
      return true;
    }
    long[] usage = retrieveUsage(this.memberInternalId, today());
    return usage[0] < this.maxQuestionsPerDay;
  }

  /**
   * @see GrouperAiAgentUsageRecorder#recordUserMessage()
   */
  public void recordUserMessage() {
    add(1, 0, 0, 0, 0);
  }

  /**
   * @see GrouperAiAgentUsageRecorder#recordModelCall(GrouperAiLlmResponse)
   */
  public void recordModelCall(GrouperAiLlmResponse response) {
    add(0, 1, response.getInputTokens(), response.getCacheReadTokens(), response.getOutputTokens());
  }

  /**
   * @see GrouperAiAgentUsageRecorder#recordCallLog(GrouperAiAgentCallLog)
   */
  public void recordCallLog(GrouperAiAgentCallLog callLog) {
    try {
      callLog.setMemberInternalId(this.memberInternalId);
      new GcDbAccess().storeToDatabase(callLog);
    } catch (RuntimeException re) {
      LOG.error("Error recording an AI agent call for member " + this.memberInternalId, re);
    }
  }

  /**
   * add to today's row, creating it if this is the user's first use today
   * @param messages messages to add
   * @param modelCalls model calls to add
   * @param inputTokens input tokens to add
   * @param cachedInputTokens cached input tokens to add
   * @param outputTokens output tokens to add
   */
  private void add(long messages, long modelCalls, long inputTokens, long cachedInputTokens, long outputTokens) {
    try {
      long usageDay = today();
      if (update(usageDay, messages, modelCalls, inputTokens, cachedInputTokens, outputTokens) > 0) {
        return;
      }
      try {
        new GcDbAccess()
          .sql("insert into " + TABLE_GROUPER_AI_AGENT_USAGE + " (" + COLUMN_MEMBER_INTERNAL_ID + ", "
              + COLUMN_USAGE_DAY + ", " + COLUMN_MESSAGE_COUNT + ", " + COLUMN_MODEL_CALL_COUNT + ", "
              + COLUMN_INPUT_TOKENS + ", " + COLUMN_CACHED_INPUT_TOKENS + ", " + COLUMN_OUTPUT_TOKENS + ", "
              + COLUMN_LAST_UPDATED_MICROS + ") values (?, ?, ?, ?, ?, ?, ?, ?)")
          .addBindVar(this.memberInternalId)
          .addBindVar(usageDay)
          .addBindVar(messages)
          .addBindVar(modelCalls)
          .addBindVar(inputTokens)
          .addBindVar(cachedInputTokens)
          .addBindVar(outputTokens)
          .addBindVar(System.currentTimeMillis() * 1000L)
          .executeSql();
      } catch (RuntimeException insertException) {
        // another node inserted today's row first, so the primary key refused this one: add to theirs
        if (update(usageDay, messages, modelCalls, inputTokens, cachedInputTokens, outputTokens) == 0) {
          throw insertException;
        }
      }
    } catch (RuntimeException re) {
      LOG.error("Error recording AI agent usage for member " + this.memberInternalId, re);
    }
  }

  /**
   * add to a day's row in the database, so two nodes adding at once both count
   * @param usageDay the day
   * @param messages messages to add
   * @param modelCalls model calls to add
   * @param inputTokens input tokens to add
   * @param cachedInputTokens cached input tokens to add
   * @param outputTokens output tokens to add
   * @return rows changed, 0 if there is no row for the day yet
   */
  private int update(long usageDay, long messages, long modelCalls, long inputTokens,
      long cachedInputTokens, long outputTokens) {
    return new GcDbAccess()
      .sql("update " + TABLE_GROUPER_AI_AGENT_USAGE + " set "
          + COLUMN_MESSAGE_COUNT + " = " + COLUMN_MESSAGE_COUNT + " + ?, "
          + COLUMN_MODEL_CALL_COUNT + " = " + COLUMN_MODEL_CALL_COUNT + " + ?, "
          + COLUMN_INPUT_TOKENS + " = " + COLUMN_INPUT_TOKENS + " + ?, "
          + COLUMN_CACHED_INPUT_TOKENS + " = " + COLUMN_CACHED_INPUT_TOKENS + " + ?, "
          + COLUMN_OUTPUT_TOKENS + " = " + COLUMN_OUTPUT_TOKENS + " + ?, "
          + COLUMN_LAST_UPDATED_MICROS + " = ?"
          + " where " + COLUMN_MEMBER_INTERNAL_ID + " = ? and " + COLUMN_USAGE_DAY + " = ?")
      .addBindVar(messages)
      .addBindVar(modelCalls)
      .addBindVar(inputTokens)
      .addBindVar(cachedInputTokens)
      .addBindVar(outputTokens)
      .addBindVar(System.currentTimeMillis() * 1000L)
      .addBindVar(this.memberInternalId)
      .addBindVar(usageDay)
      .executeSql();
  }

}

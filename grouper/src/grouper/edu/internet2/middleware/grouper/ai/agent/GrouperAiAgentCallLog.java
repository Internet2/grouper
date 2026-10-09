/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

/**
 * one row in grouper_ai_agent_call_log: one call the AI agent in the Grouper UI made to its AI
 * provider, whether it worked or not.  grouper_ai_agent_usage adds these up per user per day; this
 * keeps each call, so a provider's bill can be matched call by call (its request id) and a
 * failure or a slow call can be traced.
 *
 * <p>nothing from the conversation is kept: no text, no tool arguments or results, and no text
 * from a provider's error, only its status, request id and error type and code.</p>
 */
@GcPersistableClass(tableName = GrouperAiAgentCallLog.TABLE_GROUPER_AI_AGENT_CALL_LOG, defaultFieldPersist = GcPersist.doPersist)
public class GrouperAiAgentCallLog implements GcSqlAssignPrimaryKey {

  // -------- table and column name constants --------

  /** the table */
  public static final String TABLE_GROUPER_AI_AGENT_CALL_LOG = "grouper_ai_agent_call_log";

  /** primary key */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** member internal id of the user */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** the conversation the call was for */
  public static final String COLUMN_CONVERSATION_ID = "conversation_id";

  /** the provider, e.g. anthropic or openai */
  public static final String COLUMN_PROVIDER = "provider";

  /** the model asked for */
  public static final String COLUMN_MODEL = "model";

  /** turn or summary */
  public static final String COLUMN_CALL_TYPE = "call_type";

  /** ok, error or contextTooLong */
  public static final String COLUMN_OUTCOME = "outcome";

  /** why the model stopped */
  public static final String COLUMN_STOP_REASON = "stop_reason";

  /** tools the model asked for */
  public static final String COLUMN_TOOL_CALL_COUNT = "tool_call_count";

  /** input tokens, including those read from or written to the cache */
  public static final String COLUMN_INPUT_TOKENS = "input_tokens";

  /** input tokens read from the cache */
  public static final String COLUMN_CACHED_INPUT_TOKENS = "cached_input_tokens";

  /** input tokens written to the cache */
  public static final String COLUMN_CACHE_WRITE_INPUT_TOKENS = "cache_write_input_tokens";

  /** output tokens */
  public static final String COLUMN_OUTPUT_TOKENS = "output_tokens";

  /** the provider's id for the request */
  public static final String COLUMN_PROVIDER_REQUEST_ID = "provider_request_id";

  /** what went wrong, with no text from the provider's response */
  public static final String COLUMN_ERROR_SUMMARY = "error_summary";

  /** when the call started, micros since 1970 */
  public static final String COLUMN_STARTED_MICROS = "started_micros";

  /** how long the call took, in micros */
  public static final String COLUMN_DURATION_MICROS = "duration_micros";

  // -------- call type and outcome constants --------

  /** a step of a turn: the model answering the user or asking for tools */
  public static final String CALL_TYPE_TURN = "turn";

  /** summarizing older exchanges of a long conversation */
  public static final String CALL_TYPE_SUMMARY = "summary";

  /** the provider answered */
  public static final String OUTCOME_OK = "ok";

  /** the call failed */
  public static final String OUTCOME_ERROR = "error";

  /** the provider said the conversation is too long for the model */
  public static final String OUTCOME_CONTEXT_TOO_LONG = "contextTooLong";

  // -------- fields --------

  /** primary key */
  @GcPersistableField(primaryKey = true, primaryKeyManuallyAssigned = true, columnName = COLUMN_INTERNAL_ID)
  private long internalId = -1;

  /** member internal id of the user */
  @GcPersistableField(columnName = COLUMN_MEMBER_INTERNAL_ID)
  private long memberInternalId;

  /** the conversation the call was for */
  @GcPersistableField(columnName = COLUMN_CONVERSATION_ID)
  private String conversationId;

  /** the provider */
  @GcPersistableField(columnName = COLUMN_PROVIDER)
  private String provider;

  /** the model asked for */
  @GcPersistableField(columnName = COLUMN_MODEL)
  private String model;

  /** turn or summary */
  @GcPersistableField(columnName = COLUMN_CALL_TYPE)
  private String callType;

  /** ok, error or contextTooLong */
  @GcPersistableField(columnName = COLUMN_OUTCOME)
  private String outcome;

  /** why the model stopped, null if the call failed */
  @GcPersistableField(columnName = COLUMN_STOP_REASON)
  private String stopReason;

  /** tools the model asked for, null if the call failed */
  @GcPersistableField(columnName = COLUMN_TOOL_CALL_COUNT)
  private Long toolCallCount;

  /** input tokens, null if the call failed */
  @GcPersistableField(columnName = COLUMN_INPUT_TOKENS)
  private Long inputTokens;

  /** input tokens read from the cache, null if the call failed */
  @GcPersistableField(columnName = COLUMN_CACHED_INPUT_TOKENS)
  private Long cachedInputTokens;

  /** input tokens written to the cache, null if the call failed */
  @GcPersistableField(columnName = COLUMN_CACHE_WRITE_INPUT_TOKENS)
  private Long cacheWriteInputTokens;

  /** output tokens, null if the call failed */
  @GcPersistableField(columnName = COLUMN_OUTPUT_TOKENS)
  private Long outputTokens;

  /** the provider's id for the request, or a gateway's call id, see
   * grouper.ai.agent.requestIdHeaders.  null if none was sent */
  @GcPersistableField(columnName = COLUMN_PROVIDER_REQUEST_ID)
  private String providerRequestId;

  /** what went wrong, null if the call worked */
  @GcPersistableField(columnName = COLUMN_ERROR_SUMMARY)
  private String errorSummary;

  /** when the call started, micros since 1970 */
  @GcPersistableField(columnName = COLUMN_STARTED_MICROS)
  private long startedMicros;

  /** how long the call took, in micros */
  @GcPersistableField(columnName = COLUMN_DURATION_MICROS)
  private long durationMicros;

  // -------- primary key assignment --------

  /**
   * @see GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.aiAgentCallLog);
    return true;
  }

  // -------- getters and setters --------

  /**
   * @return the primary key
   */
  public long getInternalId() {
    return this.internalId;
  }

  /**
   * @param internalId1 the primary key
   */
  public void setInternalId(long internalId1) {
    this.internalId = internalId1;
  }

  /**
   * @return member internal id of the user
   */
  public long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * @param memberInternalId1 member internal id of the user
   */
  public void setMemberInternalId(long memberInternalId1) {
    this.memberInternalId = memberInternalId1;
  }

  /**
   * @return the conversation the call was for
   */
  public String getConversationId() {
    return this.conversationId;
  }

  /**
   * @param conversationId1 the conversation the call was for
   */
  public void setConversationId(String conversationId1) {
    this.conversationId = conversationId1;
  }

  /**
   * @return the provider
   */
  public String getProvider() {
    return this.provider;
  }

  /**
   * @param provider1 the provider
   */
  public void setProvider(String provider1) {
    this.provider = provider1;
  }

  /**
   * @return the model asked for
   */
  public String getModel() {
    return this.model;
  }

  /**
   * @param model1 the model asked for
   */
  public void setModel(String model1) {
    this.model = model1;
  }

  /**
   * @return turn or summary
   */
  public String getCallType() {
    return this.callType;
  }

  /**
   * @param callType1 turn or summary
   */
  public void setCallType(String callType1) {
    this.callType = callType1;
  }

  /**
   * @return ok, error or contextTooLong
   */
  public String getOutcome() {
    return this.outcome;
  }

  /**
   * @param outcome1 ok, error or contextTooLong
   */
  public void setOutcome(String outcome1) {
    this.outcome = outcome1;
  }

  /**
   * @return why the model stopped, null if the call failed
   */
  public String getStopReason() {
    return this.stopReason;
  }

  /**
   * @param stopReason1 why the model stopped
   */
  public void setStopReason(String stopReason1) {
    this.stopReason = stopReason1;
  }

  /**
   * @return tools the model asked for, null if the call failed
   */
  public Long getToolCallCount() {
    return this.toolCallCount;
  }

  /**
   * @param toolCallCount1 tools the model asked for
   */
  public void setToolCallCount(Long toolCallCount1) {
    this.toolCallCount = toolCallCount1;
  }

  /**
   * @return input tokens, including those read from or written to the cache, null if the call failed
   */
  public Long getInputTokens() {
    return this.inputTokens;
  }

  /**
   * @param inputTokens1 input tokens
   */
  public void setInputTokens(Long inputTokens1) {
    this.inputTokens = inputTokens1;
  }

  /**
   * @return input tokens read from the cache, null if the call failed
   */
  public Long getCachedInputTokens() {
    return this.cachedInputTokens;
  }

  /**
   * @param cachedInputTokens1 input tokens read from the cache
   */
  public void setCachedInputTokens(Long cachedInputTokens1) {
    this.cachedInputTokens = cachedInputTokens1;
  }

  /**
   * @return input tokens written to the cache, null if the call failed
   */
  public Long getCacheWriteInputTokens() {
    return this.cacheWriteInputTokens;
  }

  /**
   * @param cacheWriteInputTokens1 input tokens written to the cache
   */
  public void setCacheWriteInputTokens(Long cacheWriteInputTokens1) {
    this.cacheWriteInputTokens = cacheWriteInputTokens1;
  }

  /**
   * @return output tokens, null if the call failed
   */
  public Long getOutputTokens() {
    return this.outputTokens;
  }

  /**
   * @param outputTokens1 output tokens
   */
  public void setOutputTokens(Long outputTokens1) {
    this.outputTokens = outputTokens1;
  }

  /**
   * @return the provider's id for the request, null if it sent none
   */
  public String getProviderRequestId() {
    return this.providerRequestId;
  }

  /**
   * @param providerRequestId1 the provider's id for the request
   */
  public void setProviderRequestId(String providerRequestId1) {
    this.providerRequestId = providerRequestId1;
  }

  /**
   * @return what went wrong, null if the call worked
   */
  public String getErrorSummary() {
    return this.errorSummary;
  }

  /**
   * @param errorSummary1 what went wrong, with no text from the provider's response
   */
  public void setErrorSummary(String errorSummary1) {
    this.errorSummary = errorSummary1;
  }

  /**
   * @return when the call started, micros since 1970
   */
  public long getStartedMicros() {
    return this.startedMicros;
  }

  /**
   * @param startedMicros1 when the call started, micros since 1970
   */
  public void setStartedMicros(long startedMicros1) {
    this.startedMicros = startedMicros1;
  }

  /**
   * @return how long the call took, in micros
   */
  public long getDurationMicros() {
    return this.durationMicros;
  }

  /**
   * @param durationMicros1 how long the call took, in micros
   */
  public void setDurationMicros(long durationMicros1) {
    this.durationMicros = durationMicros1;
  }

}

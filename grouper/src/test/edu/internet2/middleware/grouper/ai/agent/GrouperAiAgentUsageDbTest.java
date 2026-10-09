/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * the agent's usage and call log in the database: the real agent and the real usage recorder,
 * with a fake model and fake tools, so nothing calls a provider.  the UI layer above, which only
 * builds the agent and hands it a GrouperAiAgentUsage, is not covered
 */
public class GrouperAiAgentUsageDbTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperAiAgentUsageDbTest.class);
  }

  /**
   *
   */
  public GrouperAiAgentUsageDbTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperAiAgentUsageDbTest(String name) {
    super(name);
  }

  /** the fake model */
  private GrouperAiAgentTest.FakeLlmClient llmClient;

  /** the settings */
  private GrouperAiAgentSettings settings;

  /** member internal id of test.subject.0, the user */
  private long memberInternalId;

  /** member internal id of test.subject.1 */
  private long memberInternalId1;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.memberInternalId = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getInternalId();
      this.memberInternalId1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getInternalId();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    this.llmClient = new GrouperAiAgentTest.FakeLlmClient();
    this.settings = new GrouperAiAgentSettings();
    this.settings.setModel("fake-model");
  }

  /**
   * @return an agent for test.subject.0 which records to the database.  a new one reads the
   * database afresh, as another UI node would
   */
  private GrouperAiAgent agent() {
    GrouperAiAgent agent = new GrouperAiAgent(this.llmClient, new GrouperAiAgentTest.FakeToolExecution(),
        this.settings);
    agent.setUsageRecorder(new GrouperAiAgentUsage(this.memberInternalId, this.settings));
    return agent;
  }

  /**
   * @param memberInternalId1 the user
   * @return the user's call log rows, oldest first
   */
  private static List<GrouperAiAgentCallLog> retrieveCallLogs(long memberInternalId1) {
    return new GcDbAccess()
        .sql("select * from grouper_ai_agent_call_log where member_internal_id = ? order by internal_id")
        .addBindVar(memberInternalId1)
        .selectList(GrouperAiAgentCallLog.class);
  }

  /**
   * @param theMemberInternalId the user
   * @param usageDay the day, yyyymmdd
   * @param messages messages
   * @param inputTokens input tokens
   * @param outputTokens output tokens
   */
  private static void insertUsage(long theMemberInternalId, long usageDay, long messages,
      long inputTokens, long outputTokens) {
    new GcDbAccess()
      .sql("insert into grouper_ai_agent_usage (member_internal_id, usage_day, message_count, "
          + "model_call_count, input_tokens, cached_input_tokens, output_tokens, last_updated_micros) "
          + "values (?, ?, ?, ?, ?, ?, ?, ?)")
      .addBindVar(theMemberInternalId)
      .addBindVar(usageDay)
      .addBindVar(messages)
      .addBindVar(messages)
      .addBindVar(inputTokens)
      .addBindVar(0L)
      .addBindVar(outputTokens)
      .addBindVar(System.currentTimeMillis() * 1000L)
      .executeSql();
  }

  /**
   * a message with a tool call adds to today's usage row and keeps a call log row per model call
   */
  public void testUsageAndCallLogRecorded() {

    GrouperAiLlmResponse first = GrouperAiAgentTest.response(GrouperAiLlmStopReason.toolUse, null,
        GrouperAiAgentTest.readCall("c1"));
    first.setCacheReadTokens(4);
    first.setCacheWriteTokens(3);
    first.setProviderRequestId("req_1");
    this.llmClient.responses.add(first);
    this.llmClient.responses.add(GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "Found it"));

    GrouperAiAgentConversation conversation = new GrouperAiAgentConversation();
    GrouperAiAgentTurnResult result = agent().sendUserMessage(conversation, "find test");
    assertEquals(GrouperAiAgentTurnResult.Status.answered, result.getStatus());

    // messages, model calls, input, cached input, output
    long[] usage = GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, GrouperAiAgentUsage.today());
    assertEquals(1, usage[0]);
    assertEquals(2, usage[1]);
    assertEquals(20, usage[2]);
    assertEquals(4, usage[3]);
    assertEquals(10, usage[4]);

    List<GrouperAiAgentCallLog> callLogs = retrieveCallLogs(this.memberInternalId);
    assertEquals(2, callLogs.size());

    GrouperAiAgentCallLog callLog = callLogs.get(0);
    assertTrue(callLog.getInternalId() > 0);
    assertEquals(this.memberInternalId, callLog.getMemberInternalId());
    assertEquals(conversation.getId(), callLog.getConversationId());
    assertEquals("fake", callLog.getProvider());
    assertEquals("fake-model", callLog.getModel());
    assertEquals(GrouperAiAgentCallLog.CALL_TYPE_TURN, callLog.getCallType());
    assertEquals(GrouperAiAgentCallLog.OUTCOME_OK, callLog.getOutcome());
    assertEquals("toolUse", callLog.getStopReason());
    assertEquals(1L, callLog.getToolCallCount().longValue());
    assertEquals(10L, callLog.getInputTokens().longValue());
    assertEquals(4L, callLog.getCachedInputTokens().longValue());
    assertEquals(3L, callLog.getCacheWriteInputTokens().longValue());
    assertEquals(5L, callLog.getOutputTokens().longValue());
    assertEquals("req_1", callLog.getProviderRequestId());
    assertNull(callLog.getErrorSummary());
    assertTrue(callLog.getStartedMicros() > 0);
    assertTrue(callLog.getDurationMicros() >= 0);

    callLog = callLogs.get(1);
    assertEquals("endTurn", callLog.getStopReason());
    assertEquals(0L, callLog.getToolCallCount().longValue());
    assertNull(callLog.getProviderRequestId());

    // nobody else's rows
    assertEquals(0, retrieveCallLogs(this.memberInternalId1).size());
  }

  /**
   * failed calls are kept in the call log with no tokens, and the message still counts
   */
  public void testFailedCallRecorded() {

    this.llmClient.responses.add(null);
    try {
      agent().sendUserMessage(new GrouperAiAgentConversation(), "find test");
      fail("The provider failure should be thrown");
    } catch (RuntimeException re) {
      assertEquals("test: the provider failed", re.getMessage());
    }

    GrouperAiAgentConversation conversation = new GrouperAiAgentConversation();
    this.llmClient.responses.add(GrouperAiAgentTest.CONTEXT_TOO_LONG);
    GrouperAiAgentTurnResult result = agent().sendUserMessage(conversation, "find everything");
    assertEquals(GrouperAiAgentTurnResult.Status.contextFull, result.getStatus());

    long[] usage = GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, GrouperAiAgentUsage.today());
    assertEquals(2, usage[0]);
    assertEquals(0, usage[1]);
    assertEquals(0, usage[2]);

    List<GrouperAiAgentCallLog> callLogs = retrieveCallLogs(this.memberInternalId);
    assertEquals(2, callLogs.size());

    GrouperAiAgentCallLog callLog = callLogs.get(0);
    assertEquals(GrouperAiAgentCallLog.OUTCOME_ERROR, callLog.getOutcome());
    assertEquals("RuntimeException", callLog.getErrorSummary());
    assertNull(callLog.getStopReason());
    assertNull(callLog.getToolCallCount());
    assertNull(callLog.getInputTokens());
    assertNull(callLog.getCachedInputTokens());
    assertNull(callLog.getCacheWriteInputTokens());
    assertNull(callLog.getOutputTokens());

    callLog = callLogs.get(1);
    assertEquals(conversation.getId(), callLog.getConversationId());
    assertEquals(GrouperAiAgentCallLog.OUTCOME_CONTEXT_TOO_LONG, callLog.getOutcome());
    assertEquals("req_test", callLog.getProviderRequestId());
    assertEquals("test: prompt is too long", callLog.getErrorSummary());
    assertNull(callLog.getInputTokens());
  }

  /**
   * the daily question limit is read from the database, so a new agent, as on another UI node, is
   * held to it too
   */
  public void testQuestionLimit() {

    this.settings.setMaxQuestionsPerUserPerDay(1L);

    this.llmClient.responses.add(GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "Hello"));
    GrouperAiAgentConversation conversation = new GrouperAiAgentConversation();
    GrouperAiAgent agent = agent();
    assertEquals(GrouperAiAgentTurnResult.Status.answered,
        agent.sendUserMessage(conversation, "hello").getStatus());

    assertEquals(GrouperAiAgentTurnResult.Status.questionLimitReached,
        agent.sendUserMessage(conversation, "hello again").getStatus());

    assertEquals(GrouperAiAgentTurnResult.Status.questionLimitReached,
        agent().sendUserMessage(new GrouperAiAgentConversation(), "hello from another node").getStatus());

    // the model was only called for the first message, and refused messages are not counted
    assertEquals(1, this.llmClient.requests.size());
    assertEquals(1, GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, GrouperAiAgentUsage.today())[0]);
  }

  /**
   * the daily token limit is read from the database and counts uncached input plus output
   */
  public void testTokenLimit() {

    this.settings.setMaxTokensPerUserPerDay(15L);

    // 10 input, 4 of them cached, and 5 output: 11 count toward the limit
    GrouperAiLlmResponse first = GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "one");
    first.setCacheReadTokens(4);
    this.llmClient.responses.add(first);
    GrouperAiAgentConversation conversation = new GrouperAiAgentConversation();
    assertEquals(GrouperAiAgentTurnResult.Status.answered,
        agent().sendUserMessage(conversation, "one").getStatus());

    // under the limit, so the next message goes ahead and takes it over
    this.llmClient.responses.add(GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "two"));
    assertEquals(GrouperAiAgentTurnResult.Status.answered,
        agent().sendUserMessage(conversation, "two").getStatus());

    assertEquals(GrouperAiAgentTurnResult.Status.limitReached,
        agent().sendUserMessage(conversation, "three").getStatus());
    assertEquals(2, this.llmClient.requests.size());
  }

  /**
   * two recorders for the same user, as on two UI nodes, both add to the one row for today
   */
  public void testAddsToExistingRow() {

    GrouperAiAgentUsage usage1 = new GrouperAiAgentUsage(this.memberInternalId, this.settings);
    GrouperAiAgentUsage usage2 = new GrouperAiAgentUsage(this.memberInternalId, this.settings);

    usage1.recordUserMessage();
    usage1.recordModelCall(GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "one"));
    usage2.recordUserMessage();
    usage2.recordModelCall(GrouperAiAgentTest.response(GrouperAiLlmStopReason.endTurn, "two"));

    long[] usage = GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, GrouperAiAgentUsage.today());
    assertEquals(2, usage[0]);
    assertEquals(2, usage[1]);
    assertEquals(20, usage[2]);
    assertEquals(10, usage[4]);

    assertEquals(1, new GcDbAccess()
        .sql("select count(*) from grouper_ai_agent_usage where member_internal_id = ?")
        .addBindVar(this.memberInternalId)
        .select(int.class).intValue());
  }

  /**
   * the usage report adds up each user's days in the range, and leaves out days outside it
   */
  public void testUsageReport() {

    insertUsage(this.memberInternalId, 20251231L, 100, 1000, 1000);
    insertUsage(this.memberInternalId, 20260101L, 1, 10, 5);
    insertUsage(this.memberInternalId, 20260102L, 2, 20, 10);
    insertUsage(this.memberInternalId1, 20260102L, 4, 40, 20);

    List<GrouperAiAgentUsageReportRow> rows = GrouperAiAgentUsageReportRow.retrieveReport(20260101L, 20260102L);
    assertEquals(2, rows.size());

    GrouperAiAgentUsageReportRow row0 = null;
    GrouperAiAgentUsageReportRow row1 = null;
    for (GrouperAiAgentUsageReportRow row : rows) {
      if (row.getMemberInternalId() == this.memberInternalId) {
        row0 = row;
      } else if (row.getMemberInternalId() == this.memberInternalId1) {
        row1 = row;
      }
    }
    assertNotNull(row0);
    assertNotNull(row1);

    assertEquals(SubjectTestHelper.SUBJ0_ID, row0.getSubjectId());
    assertEquals(3, row0.getMessages());
    assertEquals(30, row0.getInputTokens());
    assertEquals(15, row0.getOutputTokens());

    assertEquals(SubjectTestHelper.SUBJ1_ID, row1.getSubjectId());
    assertEquals(4, row1.getMessages());
    assertEquals(40, row1.getInputTokens());
  }

  /**
   * the cleanup daemon deletes usage and call log rows older than their retention, and keeps the
   * rest
   */
  public void testCleanup() {

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.ai_agent_usage.days", "365");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.ai_agent_call_log.days", "365");

    try {
      Calendar calendar = GregorianCalendar.getInstance(GrouperAiAgentUsage.timeZone());
      calendar.add(Calendar.DAY_OF_YEAR, -800);
      long oldDay = Long.parseLong(GrouperAiAgentUsage.dayFormat().format(calendar.getTime()));
      long oldMicros = calendar.getTimeInMillis() * 1000L;
      long nowMicros = System.currentTimeMillis() * 1000L;

      insertUsage(this.memberInternalId, oldDay, 1, 10, 5);
      insertUsage(this.memberInternalId, GrouperAiAgentUsage.today(), 1, 10, 5);

      for (long startedMicros : new long[] {oldMicros, nowMicros}) {
        GrouperAiAgentCallLog callLog = new GrouperAiAgentCallLog();
        callLog.setMemberInternalId(this.memberInternalId);
        callLog.setConversationId("conversation");
        callLog.setProvider("fake");
        callLog.setModel("fake-model");
        callLog.setCallType(GrouperAiAgentCallLog.CALL_TYPE_TURN);
        callLog.setOutcome(GrouperAiAgentCallLog.OUTCOME_OK);
        callLog.setStartedMicros(startedMicros);
        callLog.setDurationMicros(1000L);
        new GcDbAccess().storeToDatabase(callLog);
      }

      GrouperDaemonDeleteOldRecords.deleteOldAiAgentUsage(null, null);
      GrouperDaemonDeleteOldRecords.deleteOldAiAgentCallLogs(null, null);

      assertEquals(0, GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, oldDay)[0]);
      assertEquals(1, GrouperAiAgentUsage.retrieveUsage(this.memberInternalId, GrouperAiAgentUsage.today())[0]);

      List<GrouperAiAgentCallLog> callLogs = retrieveCallLogs(this.memberInternalId);
      assertEquals(1, callLogs.size());
      assertEquals(nowMicros, callLogs.get(0).getStartedMicros());
    } finally {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("loader.retain.db.ai_agent_usage.days");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("loader.retain.db.ai_agent_call_log.days");
    }
  }

}

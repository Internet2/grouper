/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * one user's AI agent usage over a range of days, for the admin usage report, or the total for
 * everybody
 */
public class GrouperAiAgentUsageReportRow {

  /**
   * usage per user over a range of days
   * @param fromDay first day, yyyymmdd
   * @param toDay last day, yyyymmdd, inclusive
   * @return a row per user who used the agent in the range, in no particular order
   */
  public static List<GrouperAiAgentUsageReportRow> retrieveReport(long fromDay, long toDay) {

    // left join: a member deleted since still shows, by internal id
    List<Object[]> rows = new GcDbAccess()
        .sql("select u.member_internal_id, m.subject_source, m.subject_id, m.name,"
            + " sum(u.message_count), sum(u.model_call_count), sum(u.input_tokens),"
            + " sum(u.cached_input_tokens), sum(u.output_tokens)"
            + " from grouper_ai_agent_usage u"
            + " left join grouper_members m on m.internal_id = u.member_internal_id"
            + " where u.usage_day >= ? and u.usage_day <= ?"
            + " group by u.member_internal_id, m.subject_source, m.subject_id, m.name")
        .addBindVar(fromDay)
        .addBindVar(toDay)
        .selectList(Object[].class);

    List<GrouperAiAgentUsageReportRow> result = new ArrayList<GrouperAiAgentUsageReportRow>();
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      GrouperAiAgentUsageReportRow reportRow = new GrouperAiAgentUsageReportRow();
      reportRow.memberInternalId = GrouperUtil.longValue(row[0], 0);
      reportRow.subjectSourceId = (String)row[1];
      reportRow.subjectId = (String)row[2];
      reportRow.subjectName = (String)row[3];
      reportRow.messages = GrouperUtil.longValue(row[4], 0);
      reportRow.modelCalls = GrouperUtil.longValue(row[5], 0);
      reportRow.inputTokens = GrouperUtil.longValue(row[6], 0);
      reportRow.cachedInputTokens = GrouperUtil.longValue(row[7], 0);
      reportRow.outputTokens = GrouperUtil.longValue(row[8], 0);
      result.add(reportRow);
    }
    return result;
  }

  /**
   * add another row's counts to this one, for the total
   * @param other the other row
   */
  public void add(GrouperAiAgentUsageReportRow other) {
    this.messages += other.messages;
    this.modelCalls += other.modelCalls;
    this.inputTokens += other.inputTokens;
    this.cachedInputTokens += other.cachedInputTokens;
    this.outputTokens += other.outputTokens;
  }

  /** member internal id of the user */
  private long memberInternalId;

  /** the user's subject source, null if the member is gone */
  private String subjectSourceId;

  /** the user's subject id, null if the member is gone */
  private String subjectId;

  /** the user's name as Grouper last saw it, may be null */
  private String subjectName;

  /** messages sent */
  private long messages;

  /** model calls */
  private long modelCalls;

  /** input tokens, including cached */
  private long inputTokens;

  /** input tokens read from the cache */
  private long cachedInputTokens;

  /** output tokens */
  private long outputTokens;

  /**
   * @return member internal id of the user
   */
  public long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * @return the user's subject source, null if the member is gone
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * @return the user's subject id, null if the member is gone
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * @return the user's name as Grouper last saw it, may be null
   */
  public String getSubjectName() {
    return this.subjectName;
  }

  /**
   * @return messages sent
   */
  public long getMessages() {
    return this.messages;
  }

  /**
   * @return model calls
   */
  public long getModelCalls() {
    return this.modelCalls;
  }

  /**
   * @return input tokens, including cached
   */
  public long getInputTokens() {
    return this.inputTokens;
  }

  /**
   * @return input tokens read from the cache
   */
  public long getCachedInputTokens() {
    return this.cachedInputTokens;
  }

  /**
   * @return output tokens
   */
  public long getOutputTokens() {
    return this.outputTokens;
  }

  /**
   * @return tokens counted as for the limits: uncached input plus output
   */
  public long getLimitTokens() {
    return GrouperAiAgentUsage.limitTokens(this.inputTokens, this.cachedInputTokens, this.outputTokens);
  }

}

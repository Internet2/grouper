/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentTurnResult;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentUsageReportRow;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentConversationService;

/**
 * what the AI agent screen shows, filled in by UiV2AiAgent
 */
public class AiAgentContainer {

  /**
   * a count for the screen, with the digits grouped the way the browser's locale does it, e.g.
   * 1,234,567, since token counts run to seven or eight digits
   * @param number the count
   * @return the count, formatted
   */
  public String formatNumber(long number) {
    return NumberFormat.getIntegerInstance(locale()).format(number);
  }

  /**
   * @return the browser's locale, or the server's if there is no request
   */
  private static Locale locale() {
    Locale locale = GrouperUiFilter.retrieveLocale();
    return locale == null ? Locale.getDefault() : locale;
  }

  /**
   * one tool category the user may choose for the session
   */
  public static class GuiAiAgentScopeOption {

    /** the category name, which is also the checkbox value */
    private String category;

    /** if it is chosen */
    private boolean selected;

    /**
     * @param theCategory the category name
     * @param theSelected if it is chosen
     */
    public GuiAiAgentScopeOption(String theCategory, boolean theSelected) {
      this.category = theCategory;
      this.selected = theSelected;
    }

    /**
     * @return the category name
     */
    public String getCategory() {
      return this.category;
    }

    /**
     * @return if it is chosen
     */
    public boolean isSelected() {
      return this.selected;
    }
  }

  /** the conversation as screen lines */
  private List<GuiAiAgentMessage> guiMessages = new ArrayList<GuiAiAgentMessage>();

  /** writes waiting for approval */
  private List<GrouperAiAgentTurnResult.PendingConfirmation> pendingConfirmations =
      new ArrayList<GrouperAiAgentTurnResult.PendingConfirmation>();

  /** if older messages were summarized */
  private boolean summarized;

  /** if a turn is running */
  private boolean running;

  /** the running turn's id, for the poll */
  private String runId;

  /** how long the running turn has been going */
  private long elapsedSeconds;

  /** if the user asked the running turn to stop */
  private boolean stopping;

  /**
   * @return if the user asked the running turn to stop
   */
  public boolean isStopping() {
    return this.stopping;
  }

  /**
   * @param stopping1 if the user asked the running turn to stop
   */
  public void setStopping(boolean stopping1) {
    this.stopping = stopping1;
  }

  /** seconds left in the running turn's wait for the AI provider's rate limit to clear, 0 if it is
   * not waiting */
  private long rateLimitWaitSeconds;

  /**
   * @return seconds left in the running turn's wait for the AI provider's rate limit to clear, 0
   * if it is not waiting
   */
  public long getRateLimitWaitSeconds() {
    return this.rateLimitWaitSeconds;
  }

  /**
   * @param rateLimitWaitSeconds1 seconds left in the running turn's wait for the AI provider's rate
   * limit to clear
   */
  public void setRateLimitWaitSeconds(long rateLimitWaitSeconds1) {
    this.rateLimitWaitSeconds = rateLimitWaitSeconds1;
  }

  /** categories the user's groups allow, for the scope form */
  private List<GuiAiAgentScopeOption> scopeOptions = new ArrayList<GuiAiAgentScopeOption>();

  /** input tokens in this conversation */
  private long inputTokens;

  /** input tokens read from the cache in this conversation */
  private long cacheReadTokens;

  /** output tokens in this conversation */
  private long outputTokens;

  /** the admin usage report, one row per user, most tokens first */
  private List<GrouperAiAgentUsageReportRow> usageReportRows = new ArrayList<GrouperAiAgentUsageReportRow>();

  /** the admin usage report total for everybody */
  private GrouperAiAgentUsageReportRow usageReportTotal;

  /** the report's first day, yyyy-mm-dd, for the form */
  private String usageReportFrom;

  /** the report's last day, yyyy-mm-dd, for the form */
  private String usageReportTo;

  /** if more users used the agent than the report shows */
  private boolean usageReportTruncated;

  /**
   * @return the admin usage report, one row per user, most tokens first
   */
  public List<GrouperAiAgentUsageReportRow> getUsageReportRows() {
    return this.usageReportRows;
  }

  /**
   * @param usageReportRows1 the admin usage report
   */
  public void setUsageReportRows(List<GrouperAiAgentUsageReportRow> usageReportRows1) {
    this.usageReportRows = usageReportRows1;
  }

  /**
   * @return the admin usage report total for everybody
   */
  public GrouperAiAgentUsageReportRow getUsageReportTotal() {
    return this.usageReportTotal;
  }

  /**
   * @param usageReportTotal1 the admin usage report total
   */
  public void setUsageReportTotal(GrouperAiAgentUsageReportRow usageReportTotal1) {
    this.usageReportTotal = usageReportTotal1;
  }

  /**
   * @return the report's first day, yyyy-mm-dd
   */
  public String getUsageReportFrom() {
    return this.usageReportFrom;
  }

  /**
   * @param usageReportFrom1 the report's first day, yyyy-mm-dd
   */
  public void setUsageReportFrom(String usageReportFrom1) {
    this.usageReportFrom = usageReportFrom1;
  }

  /**
   * @return the report's last day, yyyy-mm-dd
   */
  public String getUsageReportTo() {
    return this.usageReportTo;
  }

  /**
   * @param usageReportTo1 the report's last day, yyyy-mm-dd
   */
  public void setUsageReportTo(String usageReportTo1) {
    this.usageReportTo = usageReportTo1;
  }

  /**
   * @return if more users used the agent than the report shows
   */
  public boolean isUsageReportTruncated() {
    return this.usageReportTruncated;
  }

  /**
   * @param usageReportTruncated1 if more users used the agent than the report shows
   */
  public void setUsageReportTruncated(boolean usageReportTruncated1) {
    this.usageReportTruncated = usageReportTruncated1;
  }

  /**
   * @return true if the admin usage report link should show: Grouper sysadmins only, since it names
   * individual users and what they used, and only where the agent has been set up, so installs that
   * never use it do not get an empty report in their admin menu.  set up means turned on, or an
   * external system configured, so the link stays after the agent is turned off and past usage can
   * still be looked at.  only the link: the report itself is open to sysadmins either way
   */
  public boolean isCanSeeUsageReportLink() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    boolean setUp = grouperConfig.propertyValueBoolean("grouper.ai.agent.enabled", false)
        || !StringUtils.isBlank(grouperConfig.propertyValueString("grouper.ai.agent.externalSystemConfigId"));
    if (!setUp) {
      return false;
    }
    return PrivilegeHelper.isWheelOrRoot(GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /** messages the user sent today */
  private long questionsUsedToday;

  /** the daily question limit, null if there is none */
  private Long maxQuestionsPerDay;

  /**
   * @return messages the user sent today
   */
  public long getQuestionsUsedToday() {
    return this.questionsUsedToday;
  }

  /**
   * @param questionsUsedToday1 messages the user sent today
   */
  public void setQuestionsUsedToday(long questionsUsedToday1) {
    this.questionsUsedToday = questionsUsedToday1;
  }

  /**
   * @return the daily question limit, null if there is none or it could not be read
   */
  public Long getMaxQuestionsPerDay() {
    return this.maxQuestionsPerDay;
  }

  /**
   * @param maxQuestionsPerDay1 the daily question limit
   */
  public void setMaxQuestionsPerDay(Long maxQuestionsPerDay1) {
    this.maxQuestionsPerDay = maxQuestionsPerDay1;
  }

  /** tokens this conversation used, counted as for the limits */
  private long conversationTokensUsed;

  /** the conversation budget, null if there is none */
  private Long maxTokensPerConversation;

  /**
   * @return tokens this conversation used, counted as for the limits
   */
  public long getConversationTokensUsed() {
    return this.conversationTokensUsed;
  }

  /**
   * @param conversationTokensUsed1 tokens this conversation used
   */
  public void setConversationTokensUsed(long conversationTokensUsed1) {
    this.conversationTokensUsed = conversationTokensUsed1;
  }

  /**
   * @return the conversation budget, null if there is none
   */
  public Long getMaxTokensPerConversation() {
    return this.maxTokensPerConversation;
  }

  /**
   * @param maxTokensPerConversation1 the conversation budget
   */
  public void setMaxTokensPerConversation(Long maxTokensPerConversation1) {
    this.maxTokensPerConversation = maxTokensPerConversation1;
  }

  /** tokens the user used today, counted as for the daily limit */
  private long tokensUsedToday;

  /** the daily limit, null if there is none */
  private Long maxTokensPerDay;

  /**
   * @return tokens the user used today, counted as for the daily limit
   */
  public long getTokensUsedToday() {
    return this.tokensUsedToday;
  }

  /**
   * @param tokensUsedToday1 tokens the user used today
   */
  public void setTokensUsedToday(long tokensUsedToday1) {
    this.tokensUsedToday = tokensUsedToday1;
  }

  /**
   * @return the daily limit, null if there is none or it could not be read
   */
  public Long getMaxTokensPerDay() {
    return this.maxTokensPerDay;
  }

  /**
   * @param maxTokensPerDay1 the daily limit
   */
  public void setMaxTokensPerDay(Long maxTokensPerDay1) {
    this.maxTokensPerDay = maxTokensPerDay1;
  }

  /**
   * @return true if the link to the screen should show: the agent is turned on and the user is in
   * the group allowed to use it
   */
  public boolean isCanSeeAiAgentLink() {
    return GrouperUiAgentConversationService.isAllowed(GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /**
   * @return the conversation as screen lines
   */
  public List<GuiAiAgentMessage> getGuiMessages() {
    return this.guiMessages;
  }

  /**
   * @param guiMessages1 the conversation as screen lines
   */
  public void setGuiMessages(List<GuiAiAgentMessage> guiMessages1) {
    this.guiMessages = guiMessages1;
  }

  /**
   * @return writes waiting for approval
   */
  public List<GrouperAiAgentTurnResult.PendingConfirmation> getPendingConfirmations() {
    return this.pendingConfirmations;
  }

  /**
   * @param pendingConfirmations1 writes waiting for approval
   */
  public void setPendingConfirmations(List<GrouperAiAgentTurnResult.PendingConfirmation> pendingConfirmations1) {
    this.pendingConfirmations = pendingConfirmations1;
  }

  /**
   * @return how many writes are waiting for approval
   */
  public int getPendingConfirmationCount() {
    return this.pendingConfirmations == null ? 0 : this.pendingConfirmations.size();
  }

  /**
   * @return a fingerprint of exactly which writes the approval form shows, sent back with the form
   * so one left open in another tab cannot decide on changes it never showed, see
   * {@link #pendingFingerprint(Collection)}
   */
  public String getPendingConfirmationsFingerprint() {
    List<String> toolCallIds = new ArrayList<String>();
    if (this.pendingConfirmations != null) {
      for (GrouperAiAgentTurnResult.PendingConfirmation pendingConfirmation : this.pendingConfirmations) {
        toolCallIds.add(pendingConfirmation.getToolCallId());
      }
    }
    return pendingFingerprint(toolCallIds);
  }

  /**
   * one value for a set of tool call ids, the same whatever their order.  a form sends it back as
   * one field: the UI's ajax sends only the last of several hidden fields with the same name, and
   * the ids are from the AI provider, so could contain any separator
   * @param toolCallIds the ids
   * @return the sha256 of the sorted ids, one per line
   */
  public static String pendingFingerprint(Collection<String> toolCallIds) {
    List<String> sorted = new ArrayList<String>(toolCallIds);
    Collections.sort(sorted);
    StringBuilder joined = new StringBuilder();
    for (String toolCallId : sorted) {
      joined.append(toolCallId).append('\n');
    }
    return DigestUtils.sha256Hex(joined.toString());
  }

  /** a turn's status, shown in the conversation next to the approval box rather than at the top of
   * the page, null for none.  text from the text file, not from the model */
  private String turnStatusText;

  /** if the turn's status is an error rather than information */
  private boolean turnStatusError;

  /**
   * @return a turn's status, shown in the conversation, null for none
   */
  public String getTurnStatusText() {
    return this.turnStatusText;
  }

  /**
   * @param turnStatusText1 a turn's status, shown in the conversation.  text from the text file
   */
  public void setTurnStatusText(String turnStatusText1) {
    this.turnStatusText = turnStatusText1;
  }

  /**
   * @return if the turn's status is an error rather than information
   */
  public boolean isTurnStatusError() {
    return this.turnStatusError;
  }

  /**
   * @param turnStatusError1 if the turn's status is an error rather than information
   */
  public void setTurnStatusError(boolean turnStatusError1) {
    this.turnStatusError = turnStatusError1;
  }

  /**
   * @return if older messages were summarized
   */
  public boolean isSummarized() {
    return this.summarized;
  }

  /**
   * @param summarized1 if older messages were summarized
   */
  public void setSummarized(boolean summarized1) {
    this.summarized = summarized1;
  }

  /**
   * @return if a turn is running
   */
  public boolean isRunning() {
    return this.running;
  }

  /**
   * @param running1 if a turn is running
   */
  public void setRunning(boolean running1) {
    this.running = running1;
  }

  /**
   * @return the running turn's id
   */
  public String getRunId() {
    return this.runId;
  }

  /**
   * @param runId1 the running turn's id
   */
  public void setRunId(String runId1) {
    this.runId = runId1;
  }

  /**
   * @return how long the running turn has been going, in seconds
   */
  public long getElapsedSeconds() {
    return this.elapsedSeconds;
  }

  /**
   * @param elapsedSeconds1 how long the running turn has been going
   */
  public void setElapsedSeconds(long elapsedSeconds1) {
    this.elapsedSeconds = elapsedSeconds1;
  }

  /**
   * @return categories the user's groups allow
   */
  public List<GuiAiAgentScopeOption> getScopeOptions() {
    return this.scopeOptions;
  }

  /**
   * @param scopeOptions1 categories the user's groups allow
   */
  public void setScopeOptions(List<GuiAiAgentScopeOption> scopeOptions1) {
    this.scopeOptions = scopeOptions1;
  }

  /**
   * @return input tokens in this conversation
   */
  public long getInputTokens() {
    return this.inputTokens;
  }

  /**
   * @param inputTokens1 input tokens in this conversation
   */
  public void setInputTokens(long inputTokens1) {
    this.inputTokens = inputTokens1;
  }

  /**
   * @return input tokens read from the cache in this conversation
   */
  public long getCacheReadTokens() {
    return this.cacheReadTokens;
  }

  /**
   * @param cacheReadTokens1 input tokens read from the cache
   */
  public void setCacheReadTokens(long cacheReadTokens1) {
    this.cacheReadTokens = cacheReadTokens1;
  }

  /**
   * @return output tokens in this conversation
   */
  public long getOutputTokens() {
    return this.outputTokens;
  }

  /**
   * @param outputTokens1 output tokens in this conversation
   */
  public void setOutputTokens(long outputTokens1) {
    this.outputTokens = outputTokens1;
  }

}

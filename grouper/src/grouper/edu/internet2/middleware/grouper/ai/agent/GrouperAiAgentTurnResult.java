/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * what came of one user message (or one set of approvals): the answer, or the writes waiting for
 * the user, plus what the tools did along the way so the screen can show it
 */
public class GrouperAiAgentTurnResult {

  /**
   * how the turn ended
   */
  public static enum Status {

    /** the model answered */
    answered,

    /** the model asked for writes, which are waiting for the user to approve or decline */
    needsConfirmation,

    /** the model was still calling tools when it used up the steps allowed for one message */
    stepLimitReached,

    /** the model declined to answer */
    refused,

    /** the model's answer was cut off at the output token limit */
    truncated,

    /** the user's message was too long and was not sent */
    messageTooLong,

    /** the user stopped the turn */
    cancelled,

    /** the user reached their daily token limit */
    limitReached,

    /** the user sent as many messages today as the daily question limit allows */
    questionLimitReached,

    /** the conversation used its token budget, so the user has to start a new one */
    conversationLimitReached,

    /** a daily limit is set but today's usage could not be read, so the message was refused */
    usageUnavailable,

    /** the conversation is more than the model can take in, so the user has to start a new one */
    contextFull,

    /** the agent was turned off, or the user lost access to it, so the turn stopped */
    notAllowed,

    /** the AI provider, or a gateway in front of it, refused the call as over its rate limit */
    rateLimited;
  }

  /**
   * what happened to one tool call
   */
  public static enum ToolActivityStatus {

    /** it ran */
    ran,

    /** it was not run because the call could not be read or checked */
    failed,

    /** the user declined it */
    declined,

    /** the session scope does not allow it, so it was not run or offered for approval */
    refusedByScope,

    /** the model asked for more look-ups in one step than maxLookupsPerStep, so this one was not
     * run.  changes are not counted */
    tooManyLookups,

    /** it is waiting for the user */
    awaitingConfirmation;
  }

  /**
   * a write waiting for the user
   */
  public static class PendingConfirmation implements Serializable {

    /** kept in the conversation, which is kept in the HTTP session */
    private static final long serialVersionUID = 1L;

    /** the tool call id, which the user's approval names */
    private String toolCallId;

    /** which tool */
    private String toolName;

    /** the arguments as the model supplied them */
    private String argumentsJson;

    /** what the user is shown, built by code from the arguments */
    private String summary;

    /**
     * @param theToolCallId the tool call id
     * @param theToolName which tool
     * @param theArgumentsJson the arguments
     * @param theSummary what the user is shown
     */
    public PendingConfirmation(String theToolCallId, String theToolName, String theArgumentsJson,
        String theSummary) {
      this.toolCallId = theToolCallId;
      this.toolName = theToolName;
      this.argumentsJson = theArgumentsJson;
      this.summary = theSummary;
    }

    /**
     * @return the tool call id
     */
    public String getToolCallId() {
      return this.toolCallId;
    }

    /**
     * @return which tool
     */
    public String getToolName() {
      return this.toolName;
    }

    /**
     * @return the arguments as the model supplied them
     */
    public String getArgumentsJson() {
      return this.argumentsJson;
    }

    /**
     * @return what the user is shown
     */
    public String getSummary() {
      return this.summary;
    }
  }

  /**
   * one tool call made during the turn
   */
  public static class ToolActivity {

    /** which tool */
    private String toolName;

    /** the arguments as the model supplied them */
    private String argumentsJson;

    /** what the tool returned, as the model saw it */
    private String resultText;

    /** if the result is an error */
    private boolean error;

    /** what happened to it */
    private ToolActivityStatus status;

    /**
     * @param theToolName which tool
     * @param theArgumentsJson the arguments
     * @param theResultText what the tool returned
     * @param theError if the result is an error
     * @param theStatus what happened to it
     */
    public ToolActivity(String theToolName, String theArgumentsJson, String theResultText,
        boolean theError, ToolActivityStatus theStatus) {
      this.toolName = theToolName;
      this.argumentsJson = theArgumentsJson;
      this.resultText = theResultText;
      this.error = theError;
      this.status = theStatus;
    }

    /**
     * @return which tool
     */
    public String getToolName() {
      return this.toolName;
    }

    /**
     * @return the arguments as the model supplied them
     */
    public String getArgumentsJson() {
      return this.argumentsJson;
    }

    /**
     * @return what the tool returned, as the model saw it
     */
    public String getResultText() {
      return this.resultText;
    }

    /**
     * @return if the result is an error
     */
    public boolean isError() {
      return this.error;
    }

    /**
     * @return what happened to it
     */
    public ToolActivityStatus getStatus() {
      return this.status;
    }
  }

  /** how the turn ended */
  private Status status;

  /** the model's text, may be null */
  private String answerText;

  /** writes waiting for the user */
  private List<PendingConfirmation> pendingConfirmations = new ArrayList<PendingConfirmation>();

  /** tool calls made during the turn, in order */
  private List<ToolActivity> toolActivity = new ArrayList<ToolActivity>();

  /**
   * @return how the turn ended
   */
  public Status getStatus() {
    return this.status;
  }

  /**
   * @param status1 how the turn ended
   */
  public void setStatus(Status status1) {
    this.status = status1;
  }

  /**
   * @return the model's text, may be null
   */
  public String getAnswerText() {
    return this.answerText;
  }

  /**
   * @param answerText1 the model's text
   */
  public void setAnswerText(String answerText1) {
    this.answerText = answerText1;
  }

  /**
   * @return writes waiting for the user
   */
  public List<PendingConfirmation> getPendingConfirmations() {
    return this.pendingConfirmations;
  }

  /**
   * @return tool calls made during the turn, in order
   */
  public List<ToolActivity> getToolActivity() {
    return this.toolActivity;
  }

}

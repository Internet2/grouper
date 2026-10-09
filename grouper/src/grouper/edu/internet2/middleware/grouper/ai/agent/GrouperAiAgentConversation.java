/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * one conversation with the AI agent: the history the model sees, and the writes waiting for the
 * user to approve them.
 *
 * <p>kept by the front door between requests (the UI keeps it in the HTTP session), since a write
 * waiting for approval cannot hold a request open.  nothing here is trusted to decide what is
 * allowed: approval is given per call by the user, and scope and permissions are checked again when
 * each tool runs.  in particular the summary of older turns is text the model wrote, and approval
 * and scope are never carried in it.</p>
 */
public class GrouperAiAgentConversation implements Serializable {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /** identifies this conversation */
  private String id = GrouperUtil.uniqueId();

  /** when this conversation started */
  private long createdMillis = System.currentTimeMillis();

  /** the history the model sees, oldest first */
  private List<GrouperAiAgentMessage> messages = new ArrayList<GrouperAiAgentMessage>();

  /** the model's summary of turns that were compacted out of the history, null if none */
  private String summary;

  /** writes the model asked for which are waiting for the user */
  private List<GrouperAiAgentToolCall> pendingToolCalls = new ArrayList<GrouperAiAgentToolCall>();

  /**
   * results for the reads the model asked for in the same turn as the pending writes.  they already
   * ran, and go back together with the writes' results once the user has decided
   */
  private List<GrouperAiAgentToolResult> pendingReadResults = new ArrayList<GrouperAiAgentToolResult>();

  /** what the user is shown for each write waiting for approval, in the order they were asked for */
  private List<GrouperAiAgentTurnResult.PendingConfirmation> pendingConfirmations =
      new ArrayList<GrouperAiAgentTurnResult.PendingConfirmation>();

  /** identifies the tool definitions the history was produced with */
  private String toolDefinitionsFingerprint;

  /**
   * the tool definitions offered since the latest user message, so approving writes continues with
   * the same tools the model had when it asked for them
   */
  private String toolDefinitionsJson;

  /** how many times the model has been called for the current user message */
  private int llmCallsThisMessage;

  /** input tokens used by this conversation */
  private long inputTokens;

  /** output tokens used by this conversation */
  private long outputTokens;

  /** input tokens read from the provider's prompt cache */
  private long cacheReadTokens;

  /** input tokens written to the provider's prompt cache */
  private long cacheWriteTokens;

  /**
   * the provider refused a call because this conversation is more than the model can take in.  it
   * only grows, so every later message would be refused the same way: they are refused here
   * instead, without calling the provider, until the user starts a new conversation
   */
  private boolean contextFull;

  /**
   * @return true if the conversation is more than the model can take in
   */
  public boolean isContextFull() {
    return this.contextFull;
  }

  /**
   * @param contextFull1 true if the conversation is more than the model can take in
   */
  public void setContextFull(boolean contextFull1) {
    this.contextFull = contextFull1;
  }

  /**
   * @return identifies this conversation
   */
  public String getId() {
    return this.id;
  }

  /**
   * @return when this conversation started
   */
  public long getCreatedMillis() {
    return this.createdMillis;
  }

  /**
   * @return the history the model sees, oldest first
   */
  public List<GrouperAiAgentMessage> getMessages() {
    return this.messages;
  }

  /**
   * @param messages1 the history the model sees, oldest first
   */
  public void setMessages(List<GrouperAiAgentMessage> messages1) {
    this.messages = messages1;
  }

  /**
   * @return the model's summary of compacted turns, null if none
   */
  public String getSummary() {
    return this.summary;
  }

  /**
   * @param summary1 the model's summary of compacted turns
   */
  public void setSummary(String summary1) {
    this.summary = summary1;
  }

  /**
   * @return writes waiting for the user, never null
   */
  public List<GrouperAiAgentToolCall> getPendingToolCalls() {
    return this.pendingToolCalls;
  }

  /**
   * @param pendingToolCalls1 writes waiting for the user
   */
  public void setPendingToolCalls(List<GrouperAiAgentToolCall> pendingToolCalls1) {
    this.pendingToolCalls = pendingToolCalls1;
  }

  /**
   * @return results of reads from the same turn as the pending writes, never null
   */
  public List<GrouperAiAgentToolResult> getPendingReadResults() {
    return this.pendingReadResults;
  }

  /**
   * @param pendingReadResults1 results of reads from the same turn as the pending writes
   */
  public void setPendingReadResults(List<GrouperAiAgentToolResult> pendingReadResults1) {
    this.pendingReadResults = pendingReadResults1;
  }

  /**
   * @return identifies the tool definitions the history was produced with
   */
  public String getToolDefinitionsFingerprint() {
    return this.toolDefinitionsFingerprint;
  }

  /**
   * @param toolDefinitionsFingerprint1 identifies the tool definitions
   */
  public void setToolDefinitionsFingerprint(String toolDefinitionsFingerprint1) {
    this.toolDefinitionsFingerprint = toolDefinitionsFingerprint1;
  }

  /**
   * @return what the user is shown for each write waiting for approval
   */
  public List<GrouperAiAgentTurnResult.PendingConfirmation> getPendingConfirmations() {
    return this.pendingConfirmations;
  }

  /**
   * @param pendingConfirmations1 what the user is shown for each write waiting for approval
   */
  public void setPendingConfirmations(List<GrouperAiAgentTurnResult.PendingConfirmation> pendingConfirmations1) {
    this.pendingConfirmations = pendingConfirmations1;
  }

  /**
   * @return the tool definitions offered since the latest user message
   */
  public String getToolDefinitionsJson() {
    return this.toolDefinitionsJson;
  }

  /**
   * @param toolDefinitionsJson1 the tool definitions offered since the latest user message
   */
  public void setToolDefinitionsJson(String toolDefinitionsJson1) {
    this.toolDefinitionsJson = toolDefinitionsJson1;
  }

  /**
   * @return how many times the model has been called for the current user message
   */
  public int getLlmCallsThisMessage() {
    return this.llmCallsThisMessage;
  }

  /**
   * @param llmCallsThisMessage1 how many times the model has been called for the current message
   */
  public void setLlmCallsThisMessage(int llmCallsThisMessage1) {
    this.llmCallsThisMessage = llmCallsThisMessage1;
  }

  /**
   * add what one call to the model used
   * @param response the model's response
   */
  public void addUsage(GrouperAiLlmResponse response) {
    this.inputTokens += response.getInputTokens();
    this.outputTokens += response.getOutputTokens();
    this.cacheReadTokens += response.getCacheReadTokens();
    this.cacheWriteTokens += response.getCacheWriteTokens();
  }

  /**
   * @return input tokens used by this conversation
   */
  public long getInputTokens() {
    return this.inputTokens;
  }

  /**
   * @return output tokens used by this conversation
   */
  public long getOutputTokens() {
    return this.outputTokens;
  }

  /**
   * @return input tokens read from the provider's prompt cache
   */
  public long getCacheReadTokens() {
    return this.cacheReadTokens;
  }

  /**
   * @return input tokens written to the provider's prompt cache
   */
  public long getCacheWriteTokens() {
    return this.cacheWriteTokens;
  }

}

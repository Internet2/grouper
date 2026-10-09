/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * what the model sent back from one call, in a form no provider owns
 */
public class GrouperAiLlmResponse {

  /** the model's turn: its text, the tools it asked for, and its own output to replay */
  private GrouperAiAgentMessage assistantMessage;

  /** why the model stopped */
  private GrouperAiLlmStopReason stopReason;

  /** input tokens this call used */
  private long inputTokens;

  /** output tokens this call used */
  private long outputTokens;

  /** input tokens read from the provider's prompt cache */
  private long cacheReadTokens;

  /** input tokens written to the provider's prompt cache */
  private long cacheWriteTokens;

  /** the provider's id for the request, null if it sent none */
  private String providerRequestId;

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
   * @param theAssistantMessage the model's turn
   * @param theStopReason why the model stopped
   */
  public GrouperAiLlmResponse(GrouperAiAgentMessage theAssistantMessage,
      GrouperAiLlmStopReason theStopReason) {
    this.assistantMessage = theAssistantMessage;
    this.stopReason = theStopReason;
  }

  /**
   * @return the model's turn
   */
  public GrouperAiAgentMessage getAssistantMessage() {
    return this.assistantMessage;
  }

  /**
   * @return why the model stopped
   */
  public GrouperAiLlmStopReason getStopReason() {
    return this.stopReason;
  }

  /**
   * @return input tokens this call used
   */
  public long getInputTokens() {
    return this.inputTokens;
  }

  /**
   * @param inputTokens1 input tokens this call used
   */
  public void setInputTokens(long inputTokens1) {
    this.inputTokens = inputTokens1;
  }

  /**
   * @return output tokens this call used
   */
  public long getOutputTokens() {
    return this.outputTokens;
  }

  /**
   * @param outputTokens1 output tokens this call used
   */
  public void setOutputTokens(long outputTokens1) {
    this.outputTokens = outputTokens1;
  }

  /**
   * @return input tokens read from the provider's prompt cache
   */
  public long getCacheReadTokens() {
    return this.cacheReadTokens;
  }

  /**
   * @param cacheReadTokens1 input tokens read from the provider's prompt cache
   */
  public void setCacheReadTokens(long cacheReadTokens1) {
    this.cacheReadTokens = cacheReadTokens1;
  }

  /**
   * @return input tokens written to the provider's prompt cache
   */
  public long getCacheWriteTokens() {
    return this.cacheWriteTokens;
  }

  /**
   * @param cacheWriteTokens1 input tokens written to the provider's prompt cache
   */
  public void setCacheWriteTokens(long cacheWriteTokens1) {
    this.cacheWriteTokens = cacheWriteTokens1;
  }

}

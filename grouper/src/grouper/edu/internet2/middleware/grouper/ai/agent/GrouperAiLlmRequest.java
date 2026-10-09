/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * one call to the model, in a form no provider owns.  each client translates it into its own
 * request.  the system prompt and tool definitions come first and are kept identical from call to
 * call, so providers that cache a request's prefix can reuse it
 */
public class GrouperAiLlmRequest {

  /** which model */
  private String model;

  /** the system prompt */
  private String systemPrompt;

  /** the conversation so far, oldest first */
  private List<GrouperAiAgentMessage> messages;

  /**
   * the tools the model may ask for, as the tool layer defines them: name, description and
   * inputSchema.  may be empty
   */
  private ArrayNode toolDefinitions;

  /** the most the model may write in one response */
  private int maxOutputTokens;

  /**
   * @param theModel which model
   * @param theSystemPrompt the system prompt
   * @param theMessages the conversation so far, oldest first
   * @param theToolDefinitions the tools the model may ask for, may be empty
   * @param theMaxOutputTokens the most the model may write in one response
   */
  public GrouperAiLlmRequest(String theModel, String theSystemPrompt,
      List<GrouperAiAgentMessage> theMessages, ArrayNode theToolDefinitions,
      int theMaxOutputTokens) {
    this.model = theModel;
    this.systemPrompt = theSystemPrompt;
    this.messages = theMessages;
    this.toolDefinitions = theToolDefinitions;
    this.maxOutputTokens = theMaxOutputTokens;
  }

  /**
   * @return which model
   */
  public String getModel() {
    return this.model;
  }

  /**
   * @return the system prompt
   */
  public String getSystemPrompt() {
    return this.systemPrompt;
  }

  /**
   * @return the conversation so far, oldest first
   */
  public List<GrouperAiAgentMessage> getMessages() {
    return this.messages;
  }

  /**
   * @return the tools the model may ask for, may be null or empty
   */
  public ArrayNode getToolDefinitions() {
    return this.toolDefinitions;
  }

  /**
   * @return the most the model may write in one response
   */
  public int getMaxOutputTokens() {
    return this.maxOutputTokens;
  }

}

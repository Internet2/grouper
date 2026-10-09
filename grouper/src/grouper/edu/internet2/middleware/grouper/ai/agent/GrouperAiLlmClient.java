/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * one LLM provider's API.  the agent loop only talks to this, so a provider is added by writing
 * one of these and nothing else changes.
 *
 * <p>the model never touches Grouper: a client sends text and tool definitions and reads back text
 * and tool requests.  running a tool is always the agent loop's decision.</p>
 */
public interface GrouperAiLlmClient {

  /**
   * @return a short name for the provider, recorded with each assistant turn so its raw output is
   * only replayed to the provider that produced it
   */
  public String providerName();

  /**
   * call the model once
   * @param request what to send
   * @return what came back
   * @throws RuntimeException if the provider could not be reached or answered with an error
   */
  public GrouperAiLlmResponse send(GrouperAiLlmRequest request);

  /**
   * a provider's raw assistant output, made safe to replay after the history in front of it was
   * edited (older turns replaced by a summary, long tool results from earlier questions replaced
   * with a stub, or different tool definitions).  a provider that
   * binds its reasoning to the exact history strips it; one that does not returns the output as is
   * @param rawJson the provider's own output for an assistant turn
   * @return the output to replay from now on, or null to rebuild the turn from its text and tool
   * calls
   */
  public String stripReasoningForEditedHistory(String rawJson);

}

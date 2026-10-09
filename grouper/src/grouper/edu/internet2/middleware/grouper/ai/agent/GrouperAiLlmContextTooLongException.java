/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * the provider refused a request because the conversation is more than the model can take in.
 * sending it again cannot work, since the conversation only grows, so the agent tells the user to
 * start a new conversation rather than reporting a general failure
 */
public class GrouperAiLlmContextTooLongException extends RuntimeException {

  /** serial */
  private static final long serialVersionUID = 1L;

  /**
   * @param cause the provider's error
   */
  public GrouperAiLlmContextTooLongException(GrouperAiLlmHttpException cause) {
    super("The conversation is too long for the model: " + cause.getMessage(), cause);
  }

}

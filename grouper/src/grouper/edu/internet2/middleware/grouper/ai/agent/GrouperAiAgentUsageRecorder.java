/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * counts what one user's agent uses and says whether they are within their limit.  optional: an
 * agent without one counts nothing and has no limit, which is what the tests use
 */
public interface GrouperAiAgentUsageRecorder {

  /**
   * @return true if the user may send another message today, under the daily token limit.  checked
   * only when a message starts, so a user can go over by one message
   * @throws RuntimeException if there is a limit and usage cannot be read.  the agent refuses the
   * message, so a limit is never silently skipped
   */
  public boolean isWithinLimit();

  /**
   * @return true if the user may send another message today, under the daily question limit.
   * checked only when a message starts
   * @throws RuntimeException if there is a limit and usage cannot be read, as for isWithinLimit
   */
  public boolean isWithinQuestionLimit();

  /**
   * the user sent a message.  approving or declining changes is not a message and is not counted
   */
  public void recordUserMessage();

  /**
   * a model call finished
   * @param response what came back, with the tokens it used
   */
  public void recordModelCall(GrouperAiLlmResponse response);

  /**
   * keep one model call, whether it worked or failed, in the per call log
   * @param callLog the call, filled in except for the user
   */
  public void recordCallLog(GrouperAiAgentCallLog callLog);

}

/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * why the model stopped, in terms the agent loop acts on.  each provider's own reasons are mapped
 * onto these
 */
public enum GrouperAiLlmStopReason {

  /** the model finished; its text is the answer */
  endTurn,

  /** the model asked for one or more tools */
  toolUse,

  /** the model ran out of output tokens, so the text or a tool call's arguments may be cut off */
  maxTokens,

  /** the provider declined to answer */
  refusal,

  /** anything else.  the agent treats it like maxTokens, so a reason it does not know fails safe */
  other;
}

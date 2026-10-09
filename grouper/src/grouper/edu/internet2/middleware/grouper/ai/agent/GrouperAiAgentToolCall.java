/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.Serializable;

/**
 * a tool the model asked to run.  the arguments are kept as the JSON text the model produced, so
 * the conversation can live in an HTTP session without depending on how a JSON library serializes
 */
public class GrouperAiAgentToolCall implements Serializable {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /** the provider's id for this call, which the result has to quote back */
  private String id;

  /** which tool */
  private String name;

  /** the arguments as JSON text */
  private String argumentsJson;

  /**
   * @param theId the provider's id for this call
   * @param theName which tool
   * @param theArgumentsJson the arguments as JSON text
   */
  public GrouperAiAgentToolCall(String theId, String theName, String theArgumentsJson) {
    this.id = theId;
    this.name = theName;
    this.argumentsJson = theArgumentsJson;
  }

  /**
   * @return the provider's id for this call
   */
  public String getId() {
    return this.id;
  }

  /**
   * @return which tool
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the arguments as JSON text
   */
  public String getArgumentsJson() {
    return this.argumentsJson;
  }

}

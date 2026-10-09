/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.Serializable;

/**
 * what came back from a tool call, as the text the model reads.  a failure is a result too, with
 * the error flag set, so the model can explain it rather than the conversation ending
 */
public class GrouperAiAgentToolResult implements Serializable {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /** the id of the call this answers */
  private String toolCallId;

  /** which tool */
  private String toolName;

  /** the result text */
  private String content;

  /** true if the call failed, was refused, or was declined */
  private boolean error;

  /**
   * @param theToolCallId the id of the call this answers
   * @param theToolName which tool
   * @param theContent the result text
   * @param theError true if the call failed, was refused, or was declined
   */
  public GrouperAiAgentToolResult(String theToolCallId, String theToolName, String theContent,
      boolean theError) {
    this.toolCallId = theToolCallId;
    this.toolName = theToolName;
    this.content = theContent;
    this.error = theError;
  }

  /**
   * @return the id of the call this answers
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
   * @return the result text
   */
  public String getContent() {
    return this.content;
  }

  /**
   * @return true if the call failed, was refused, or was declined
   */
  public boolean isError() {
    return this.error;
  }

}

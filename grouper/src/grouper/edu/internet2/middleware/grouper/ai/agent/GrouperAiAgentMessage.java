/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * one entry in the conversation history, in a form no provider owns.
 *
 * <p>an assistant turn also keeps the provider's own output verbatim, in rawJson.  both providers
 * require their output to be sent back exactly as it came when a tool is being answered: Anthropic
 * binds thinking blocks to them, and OpenAI requires reasoning items to travel with the function
 * calls they led to.  the text and tool calls here are the provider neutral reading of the same
 * turn, used for everything else and to rebuild the turn if the raw form cannot be replayed.</p>
 */
public class GrouperAiAgentMessage implements Serializable {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /** who a history entry is from */
  public static enum Role {

    /** something the user typed */
    user,

    /** what the model said and which tools it asked for */
    assistant,

    /** the results of the tools the previous assistant turn asked for */
    toolResults;
  }

  /** who this is from */
  private Role role;

  /** the text, for a user or assistant entry */
  private String text;

  /** the tools the model asked for, for an assistant entry */
  private List<GrouperAiAgentToolCall> toolCalls = new ArrayList<GrouperAiAgentToolCall>();

  /** the tool results, for a tool results entry */
  private List<GrouperAiAgentToolResult> toolResults = new ArrayList<GrouperAiAgentToolResult>();

  /** the provider's own output for an assistant turn, as JSON text, to replay verbatim */
  private String rawJson;

  /** which provider produced rawJson, so it is only replayed to the same one */
  private String rawProvider;

  /**
   * @param text what the user typed
   * @return the entry
   */
  public static GrouperAiAgentMessage user(String text) {
    GrouperAiAgentMessage message = new GrouperAiAgentMessage();
    message.role = Role.user;
    message.text = text;
    return message;
  }

  /**
   * @param text what the model said, may be null
   * @param toolCalls the tools it asked for, may be empty
   * @param rawJson the provider's own output as JSON text
   * @param rawProvider which provider produced it
   * @return the entry
   */
  public static GrouperAiAgentMessage assistant(String text, List<GrouperAiAgentToolCall> toolCalls,
      String rawJson, String rawProvider) {
    GrouperAiAgentMessage message = new GrouperAiAgentMessage();
    message.role = Role.assistant;
    message.text = text;
    if (toolCalls != null) {
      message.toolCalls.addAll(toolCalls);
    }
    message.rawJson = rawJson;
    message.rawProvider = rawProvider;
    return message;
  }

  /**
   * @param toolResults the results, one for each tool the previous assistant turn asked for
   * @return the entry
   */
  public static GrouperAiAgentMessage toolResults(List<GrouperAiAgentToolResult> toolResults) {
    GrouperAiAgentMessage message = new GrouperAiAgentMessage();
    message.role = Role.toolResults;
    message.toolResults.addAll(toolResults);
    return message;
  }

  /**
   * @return who this is from
   */
  public Role getRole() {
    return this.role;
  }

  /**
   * @return the text, may be null
   */
  public String getText() {
    return this.text;
  }

  /**
   * @param text1 the text
   */
  public void setText(String text1) {
    this.text = text1;
  }

  /**
   * @return the tools the model asked for, never null
   */
  public List<GrouperAiAgentToolCall> getToolCalls() {
    return this.toolCalls;
  }

  /**
   * @return the tool results, never null
   */
  public List<GrouperAiAgentToolResult> getToolResults() {
    return this.toolResults;
  }

  /**
   * @return the provider's own output as JSON text, may be null
   */
  public String getRawJson() {
    return this.rawJson;
  }

  /**
   * @param rawJson1 the provider's own output as JSON text
   */
  public void setRawJson(String rawJson1) {
    this.rawJson = rawJson1;
  }

  /**
   * @return which provider produced the raw output, may be null
   */
  public String getRawProvider() {
    return this.rawProvider;
  }

}

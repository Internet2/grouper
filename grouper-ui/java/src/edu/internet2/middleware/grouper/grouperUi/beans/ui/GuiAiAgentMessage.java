/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentConversation;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentMessage;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentToolCall;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentToolResult;

/**
 * one line of the AI agent conversation as the screen shows it: something the user said,
 * something the assistant said, or a tool the assistant used with what came back.
 *
 * <p>everything here came from the user, the model or Grouper data, so the JSP escapes all of
 * it.</p>
 */
public class GuiAiAgentMessage {

  /**
   * what kind of line
   */
  public static enum Type {

    /** the user */
    user,

    /** the assistant */
    assistant,

    /** a tool the assistant used */
    tool;
  }

  /** what kind of line */
  private Type type;

  /** what the user or the assistant said */
  private String text;

  /** the tool, for a tool line */
  private String toolName;

  /** the arguments the assistant gave the tool */
  private String argumentsJson;

  /** what the tool returned, null if it has not run */
  private String resultText;

  /** if the result is an error, including a declined or refused call */
  private boolean error;

  /** if the call is waiting for the user's approval */
  private boolean awaitingApproval;

  /**
   * the conversation as screen lines.  reads a copy of the message list, so it is safe to call
   * while a turn is appending to it; at worst it is a moment behind
   * @param conversation the conversation, may be null
   * @param pendingUserText a message just sent which is not in the conversation yet, may be null
   * @return the lines, in order
   */
  public static List<GuiAiAgentMessage> convert(GrouperAiAgentConversation conversation, String pendingUserText) {

    List<GuiAiAgentMessage> result = new ArrayList<GuiAiAgentMessage>();

    if (conversation != null) {

      // a copy: ArrayList's copy does not check for concurrent change, and the messages
      // themselves are only ever added, never changed in a way that matters here
      List<GrouperAiAgentMessage> messages = new ArrayList<GrouperAiAgentMessage>(conversation.getMessages());

      Map<String, GrouperAiAgentToolResult> resultsById = new HashMap<String, GrouperAiAgentToolResult>();
      for (GrouperAiAgentMessage message : messages) {
        if (message != null && message.getRole() == GrouperAiAgentMessage.Role.toolResults) {
          for (GrouperAiAgentToolResult toolResult : message.getToolResults()) {
            resultsById.put(toolResult.getToolCallId(), toolResult);
          }
        }
      }

      // look-ups from the same step as changes waiting for approval have already run.  their
      // results are held back from the conversation until the user decides, but are shown now
      for (GrouperAiAgentToolResult toolResult : new ArrayList<GrouperAiAgentToolResult>(conversation.getPendingReadResults())) {
        if (toolResult != null) {
          resultsById.put(toolResult.getToolCallId(), toolResult);
        }
      }

      Map<String, Boolean> pendingIds = new HashMap<String, Boolean>();
      for (GrouperAiAgentToolCall toolCall : new ArrayList<GrouperAiAgentToolCall>(conversation.getPendingToolCalls())) {
        pendingIds.put(toolCall.getId(), Boolean.TRUE);
      }

      for (GrouperAiAgentMessage message : messages) {

        if (message == null) {
          continue;
        }

        if (message.getRole() == GrouperAiAgentMessage.Role.user) {
          GuiAiAgentMessage guiMessage = new GuiAiAgentMessage();
          guiMessage.type = Type.user;
          guiMessage.text = message.getText();
          result.add(guiMessage);

        } else if (message.getRole() == GrouperAiAgentMessage.Role.assistant) {

          if (!StringUtils.isBlank(message.getText())) {
            GuiAiAgentMessage guiMessage = new GuiAiAgentMessage();
            guiMessage.type = Type.assistant;
            guiMessage.text = message.getText();
            result.add(guiMessage);
          }

          for (GrouperAiAgentToolCall toolCall : message.getToolCalls()) {
            GuiAiAgentMessage guiMessage = new GuiAiAgentMessage();
            guiMessage.type = Type.tool;
            guiMessage.toolName = toolCall.getName();
            guiMessage.argumentsJson = toolCall.getArgumentsJson();
            GrouperAiAgentToolResult toolResult = resultsById.get(toolCall.getId());
            if (toolResult != null) {
              guiMessage.resultText = toolResult.getContent();
              guiMessage.error = toolResult.isError();
            } else if (pendingIds.containsKey(toolCall.getId())) {
              guiMessage.awaitingApproval = true;
            }
            result.add(guiMessage);
          }
        }
      }
    }

    if (!StringUtils.isBlank(pendingUserText)) {
      GuiAiAgentMessage guiMessage = new GuiAiAgentMessage();
      guiMessage.type = Type.user;
      guiMessage.text = pendingUserText;
      result.add(guiMessage);
    }

    return result;
  }

  /**
   * @return what kind of line, as a string for the JSP
   */
  public String getType() {
    return this.type.name();
  }

  /**
   * @return what the user or the assistant said
   */
  public String getText() {
    return this.text;
  }

  /**
   * @return the tool, for a tool line
   */
  public String getToolName() {
    return this.toolName;
  }

  /**
   * @return the arguments the assistant gave the tool
   */
  public String getArgumentsJson() {
    return this.argumentsJson;
  }

  /**
   * @return what the tool returned, null if it has not run
   */
  public String getResultText() {
    return this.resultText;
  }

  /**
   * @return if the result is an error
   */
  public boolean isError() {
    return this.error;
  }

  /**
   * @return if the call is waiting for the user's approval
   */
  public boolean isAwaitingApproval() {
    return this.awaitingApproval;
  }

}

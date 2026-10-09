/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentToolExecution;
import edu.internet2.middleware.grouper.mcp.GrouperTool;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.mcp.GrouperToolRegistry;
import edu.internet2.middleware.subject.Subject;

/**
 * the agent's tools for one UI user, over the same tool layer MCP uses.  works from the caller
 * captured on the request thread, so it can be used from the agent's background thread
 */
public class GrouperUiAgentToolExecution implements GrouperAiAgentToolExecution {

  /** who the agent is working for */
  private GrouperUiAgentCaller caller;

  /**
   * @param theCaller who the agent is working for
   */
  public GrouperUiAgentToolExecution(GrouperUiAgentCaller theCaller) {
    this.caller = theCaller;
  }

  /**
   * every tool the user's MCP groups allow, whatever the session scope, so the list does not change
   * when the user changes the scope
   * @see GrouperAiAgentToolExecution#retrieveToolDefinitions()
   */
  public ArrayNode retrieveToolDefinitions() {
    return GrouperUiAgentToolRunner.retrieveToolDefinitionsForAllScopes(this.caller);
  }

  /**
   * the same check as the screen: the agent is on, the user is in its group, and their MCP groups
   * give it something to do.  cached for a minute on each node
   * @see GrouperAiAgentToolExecution#isAgentAllowed()
   */
  public boolean isAgentAllowed() {
    return GrouperUiAgentConversationService.isAllowed(this.caller.getSubject());
  }

  /**
   * @see GrouperAiAgentToolExecution#isWrite(String, JsonNode)
   */
  public boolean isWrite(String toolName, JsonNode arguments) {
    GrouperTool grouperTool = GrouperToolRegistry.find(toolName);
    if (grouperTool == null) {
      return false;
    }
    return grouperTool.category(arguments).isWrite();
  }

  /**
   * only the session scope is judged here.  an unknown tool, or one the user's groups do not allow,
   * is left for the tool layer to refuse with the real reason
   * @see GrouperAiAgentToolExecution#isAllowedInScope(String, JsonNode)
   */
  public boolean isAllowedInScope(String toolName, JsonNode arguments) {
    GrouperTool grouperTool = GrouperToolRegistry.find(toolName);
    if (grouperTool == null) {
      return true;
    }
    GrouperToolCategory category = grouperTool.category(arguments);
    if (!GrouperUiAgentToolRunner.isAllowedByMembership(this.caller, category)) {
      return true;
    }
    return GrouperToolAccess.isAllowed(category, GrouperUiAgentToolRunner.retrieveAuthUser(this.caller));
  }

  /**
   * @see GrouperAiAgentToolExecution#confirmationSummary(String, JsonNode)
   */
  public String confirmationSummary(String toolName, JsonNode arguments) {
    GrouperTool grouperTool = GrouperToolRegistry.find(toolName);
    if (grouperTool == null) {
      return null;
    }
    return grouperTool.confirmationSummary(arguments);
  }

  /**
   * @see GrouperAiAgentToolExecution#executeTool(String, JsonNode)
   */
  public ObjectNode executeTool(String toolName, JsonNode arguments) {
    return GrouperUiAgentToolRunner.executeTool(this.caller, toolName, arguments);
  }

  /**
   * the user logged in to the UI, the same subject every tool runs as
   * @see GrouperAiAgentToolExecution#retrieveUser()
   */
  public Subject retrieveUser() {
    return this.caller.getSubject();
  }

}

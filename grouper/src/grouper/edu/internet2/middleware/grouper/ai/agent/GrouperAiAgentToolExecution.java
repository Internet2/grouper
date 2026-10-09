/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.subject.Subject;

/**
 * how the agent loop reaches the tools, for one user.  the UI implements this over the tool layer;
 * tests implement it with fakes, so the loop can be tested without a database or a provider
 */
public interface GrouperAiAgentToolExecution {

  /**
   * the tools to offer the model.  this should be the same list for the whole conversation, so it
   * does not change with the scope the user chose for the session: a changing list invalidates the
   * provider's cache and, for Anthropic, the model's earlier reasoning.  the scope is enforced by
   * {@link #isAllowedInScope(String, JsonNode)} and when the tool runs
   * @return tool definitions with name, description and inputSchema
   */
  public ArrayNode retrieveToolDefinitions();

  /**
   * whether the user may still use the agent at all: it is turned on, and they are in the group
   * allowed to use it.  checked when a message starts, before every model call and before each
   * lookup in a step, so turning the agent off or removing someone stops a turn already running,
   * whether or not their screen is still open
   * @return true if the user may still use the agent
   */
  public boolean isAgentAllowed();

  /**
   * @param toolName which tool
   * @param arguments as the model supplied them
   * @return true if the call changes something, so the user has to approve it first.  an unknown
   * tool is not a write; it fails when it runs
   */
  public boolean isWrite(String toolName, JsonNode arguments);

  /**
   * @param toolName which tool
   * @param arguments as the model supplied them
   * @return false only if the user's groups allow this call but the scope the user chose for the
   * session does not.  such a call, read or write, is refused with a message pointing at the
   * session scope, rather than reaching the tool layer, whose refusal names the group needed and
   * would send the user the wrong way.  a call the groups do not allow either returns true, so the
   * tool layer refuses it with the group it needs
   */
  public boolean isAllowedInScope(String toolName, JsonNode arguments);

  /**
   * @param toolName which tool
   * @param arguments as the model supplied them
   * @return what the user is shown before a write runs, built from the arguments by code, or null
   */
  public String confirmationSummary(String toolName, JsonNode arguments);

  /**
   * run a tool as the user
   * @param toolName which tool
   * @param arguments as the model supplied them
   * @return the result, {content: [{type: text, text}], isError}
   */
  public ObjectNode executeTool(String toolName, JsonNode arguments);

  /**
   * @return the user the agent works for, so the model can be told who "me" is, or null if not
   * known.  only the subject id, source and name are used
   */
  public Subject retrieveUser();

}

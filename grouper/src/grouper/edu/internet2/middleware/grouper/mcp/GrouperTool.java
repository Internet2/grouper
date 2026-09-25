/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;

/**
 * one thing the agent, or an MCP client, can ask Grouper to do.
 *
 * <p>a tool declares its own name, schema and category, so that everything a caller needs in
 * order to decide whether to run it sits on the tool itself.  before this, the name lived in one
 * list, the category in a switch somewhere else, and the dispatch in a third place, which is how
 * a tool ends up classified differently depending on which of the three you read.</p>
 *
 * <p>front doors do not implement this differently.  the MCP servlet and the UI both look a tool
 * up in {@link GrouperToolRegistry} and run it; neither the registry nor the tool knows which one
 * is calling, beyond what the caller puts in the auth user for the audit record.</p>
 */
public interface GrouperTool {

  /**
   * @return the tool name as callers refer to it, e.g. "group_add_member".  must be one of
   * {@link GrouperMcpToolNames}
   */
  public String name();

  /**
   * the name, description and JSON Schema for the arguments.  keep the schema to the subset every
   * provider accepts -- object, string, boolean, integer, array, enum, required, description --
   * since each LLM adapter has to express it in its own dialect
   *
   * <p>it takes the user because a couple of tools describe themselves differently depending on
   * who is asking: the institutional tools and recipe tools list what this institution has
   * configured, and what the caller is allowed to do with it</p>
   *
   * @param authUser who is asking
   * @return the definition, or null if there is nothing to offer this caller
   */
  public ObjectNode toolDefinition(GrouperMcpAuthUser authUser);

  /**
   * what this tool needs to be allowed to do.  it takes the arguments because a few tools are a
   * read or a write depending on what they were asked to do: the recipe tool reads when it is
   * asked to list or get and writes when it is asked to update
   *
   * @param arguments as the caller supplied them, may be null
   * @return the category, never null
   */
  public GrouperToolCategory category(JsonNode arguments);

  /**
   * what a user is shown before a write runs, built from the arguments by code rather than by the
   * model, so that what the user approves is the thing which is about to happen and not a
   * description of it.  reads return null and are not gated
   *
   * @param arguments as the caller supplied them
   * @return one line, e.g. "Add 3 subjects to group school:stem:biology:admins", or null
   */
  public default String confirmationSummary(JsonNode arguments) {
    return null;
  }

  /**
   * whether this tool is offered to callers.  false for names kept working for older clients
   * which should not show up in a tool list
   * @return true if the tool should be advertised
   */
  public default boolean advertised() {
    return true;
  }

  /**
   * whether there is any point offering this tool to this caller right now, beyond whether they
   * are allowed to use it.  a few tools have nothing to work on unless an institution has set
   * something up, or unless the caller's scope reaches the kind of thing they act on, and
   * listing those would only invite a call which is going to fail
   *
   * <p>this is separate from the category check, which asks whether the caller is permitted.  a
   * tool which is not available is simply not offered; it is not a refusal.</p>
   *
   * @param authUser who is asking
   * @return true if the tool is worth offering
   */
  public default boolean availableFor(GrouperMcpAuthUser authUser) {
    return true;
  }

  /**
   * @param arguments as the caller supplied them
   * @param authUser who is calling, with their scope
   * @return the result, including failures the caller should be able to explain rather than
   * exceptions
   */
  public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser);

}

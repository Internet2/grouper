/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpRecipeTool;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpToolLogUtil;

/**
 * runs a tool: the checks, the session, the call, and the audit record.
 *
 * <p>this is what a front door calls.  it used to be spread across the MCP servlet -- an allow
 * list check, a per category authorization check written out once per tool, a dispatch switch and
 * a logging wrapper.  gathering it here is what lets the UI run a tool without reimplementing any
 * of that, and means a check added in one place applies to both front doors.</p>
 */
public class GrouperToolExecutor {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperToolExecutor.class);

  /** for building results */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * the session this executor started as the caller, while a tool runs or while the tools are
   * being listed on this thread.  set only for that span, so it cannot outlive the call on a
   * pooled thread
   */
  private static ThreadLocal<GrouperSession> threadLocalToolGrouperSession =
      new ThreadLocal<GrouperSession>();

  /**
   * the session a running tool should use: the one this executor started as the caller.  the web
   * service logic the tools call asks for this before anything else, so a tool runs as the caller
   * whichever front door it came through, and never takes the web service's own login and actAs
   * path, which belongs to web service clients
   * @return the session, or null if no tool is running or being listed on this thread
   */
  public static GrouperSession retrieveToolGrouperSession() {
    return threadLocalToolGrouperSession.get();
  }

  /**
   * run a tool for this caller.
   *
   * @param toolName which tool
   * @param arguments as the caller supplied them, may be null
   * @param authUser who is calling, carrying their scope and which front door they came through
   * @return the result, including failures the tool itself decided on
   * @throws GrouperToolException if there is no such tool, or the server broke
   */
  public static ObjectNode executeTool(String toolName, JsonNode arguments,
      final GrouperMcpAuthUser authUser) {

    final GrouperTool grouperTool = GrouperToolRegistry.find(toolName);

    // what this call needs to be allowed to do.  the arguments matter because a tool can be a
    // read or a write depending on what it was asked for.  an unknown name has no category, and
    // is refused below once the allow list has had its say, which is the order the MCP servlet
    // used and so the order a client already sees
    final GrouperToolCategory category = grouperTool == null ? GrouperToolCategory.readonly
        : grouperTool.category(arguments);
    final String toolCategory = category.getLegacyCategory();

    final String requestJson = arguments == null ? null : arguments.toString();

    String throttleError = GrouperMcpToolLogUtil.checkThrottle(authUser, toolCategory);
    if (throttleError != null) {
      GrouperMcpToolLogUtil.logToolCall(authUser, toolName, toolCategory,
          requestJson, throttleError, true, System.currentTimeMillis() * 1000L, null);
      return buildErrorResult(throttleError);
    }

    final long startedMicros = System.currentTimeMillis() * 1000L;
    final long startNanos = System.nanoTime();

    // run as the authenticated user, so object level security applies to whatever the tool does
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject(), false);

    final ObjectNode[] resultHolder = new ObjectNode[1];
    final boolean[] isErrorHolder = new boolean[] { false };
    final String[] responseTextHolder = new String[] { null };
    final GrouperToolException[] toolExceptionHolder = new GrouperToolException[1];

    // a tool which returns a list returns at most so many rows per call, whatever it was asked for,
    // with a limit for each front door.  the audit row above keeps what was asked
    final Integer[] pageSizeLimitHolder = new Integer[1];
    final JsonNode theArguments = applyPageSizeLimit(grouperTool, authUser.getEntryPath(), arguments,
        pageSizeLimitHolder);

    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

          // put back whatever was there when this finishes, rather than clearing, in case a tool
          // is ever run from inside another
          GrouperSession previousToolGrouperSession = threadLocalToolGrouperSession.get();
          threadLocalToolGrouperSession.set(theGrouperSession);

          try {

            // an institution can turn individual tools off.  checked before whether the tool
            // exists, so that a name which is both disabled and unknown reads as disabled, the
            // way it did when this was the servlet's dispatch method
            if (!GrouperMcpToolNames.isToolAllowedByConfig(toolName)) {
              resultHolder[0] = buildErrorResult("Access denied: tool '" + toolName
                  + "' is not allowed by server configuration.");

            } else if (grouperTool == null) {
              // no tool ran, so there is no tool result this could be reported in
              toolExceptionHolder[0] = new GrouperToolException(
                  GrouperToolException.Kind.notFound, "Unknown tool: " + toolName);
              isErrorHolder[0] = true;
              responseTextHolder[0] = "Unknown tool: " + toolName;
              return null;

            } else if (!GrouperToolAccess.isAllowed(category, authUser)) {
              resultHolder[0] = buildErrorResult("Access denied: user is not authorized for "
                  + toolName + ". " + category.getDeniedDetail());

            } else if (!GrouperToolAccess.isAllowed(grouperTool, category, authUser)) {
              // allowed in the category, but only on its limited tier, and this tool has not
              // been opened to them (GRP-7415)
              resultHolder[0] = buildErrorResult("Access denied: user is not authorized for "
                  + toolName + ". This tool has not been made available to your limited MCP "
                  + "access, ask your Grouper administrator.");

            } else {
              resultHolder[0] = grouperTool.execute(theArguments, authUser);
            }

            isErrorHolder[0] = resultHolder[0].has("isError")
                && resultHolder[0].get("isError").asBoolean(false);
            responseTextHolder[0] = retrieveResultText(resultHolder[0]);

            // after the response text is captured, so the audit row keeps the tool's own answer.
            // only when the cap could have cut the list short, so a model is not sent after a
            // next page which is empty
            if (pageSizeLimitHolder[0] != null && !isErrorHolder[0]
                && isPossiblyFullPage(responseTextHolder[0], pageSizeLimitHolder[0])) {
              appendPageSizeNote(resultHolder[0], pageSizeLimitHolder[0]);
            }

            // a failed call is the one moment a model is certainly reading, so if this
            // institution has a recipe for this tool the failure carries it rather than leaving
            // the caller at a dead end.  after the response text is captured, so the audit row
            // keeps the tool's own message and does not grow by the length of a recipe
            if (isErrorHolder[0]) {
              GrouperMcpRecipeTool.appendRecipesToError(resultHolder[0], toolName, authUser);
            }

          } catch (Exception e) {
            // the tool did not report this, it escaped, so it is a fault here rather than
            // something the tool decided.  held rather than thrown so the audit row is still
            // written below, then rethrown
            LOG.error("Error in tool call: " + toolName, e);
            toolExceptionHolder[0] = new GrouperToolException(
                GrouperToolException.Kind.internalError, "Internal error: " + e.getMessage(), e);
            isErrorHolder[0] = true;
            responseTextHolder[0] = "Internal error: " + e.getMessage();
          } finally {
            if (previousToolGrouperSession == null) {
              threadLocalToolGrouperSession.remove();
            } else {
              threadLocalToolGrouperSession.set(previousToolGrouperSession);
            }
          }
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    long durationMicros = (System.nanoTime() - startNanos) / 1000;

    // errors writing the audit row do not change the answer the caller gets
    GrouperMcpToolLogUtil.logToolCall(authUser, toolName, toolCategory,
        requestJson, responseTextHolder[0], isErrorHolder[0], startedMicros, durationMicros);

    if (toolExceptionHolder[0] != null) {
      throw toolExceptionHolder[0];
    }

    return resultHolder[0];
  }

  /**
   * the most rows a tool returns in one call, for the front door the call came through.  the AI
   * agent in the UI is capped by default, since every row it is given goes to the AI provider on
   * this institution's account, again on each later step.  MCP is not capped by default, since the
   * user's own AI account carries the cost of what they ask for; an institution can set one to
   * protect the server from very large queries
   * @param toolName the tool
   * @param entryPath the front door
   * @return for the UI, grouper.ai.agent.tool.toolName.maxPageSize if set, otherwise
   * grouper.ai.agent.maxPageSize, default 200.  for MCP, grouper.mcp.tool.toolName.maxPageSize if
   * set, otherwise grouper.mcp.maxPageSize, default none.  0 or less means no limit
   */
  static int maxPageSize(String toolName, GrouperToolEntryPath entryPath) {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    if (entryPath == GrouperToolEntryPath.ui) {
      return grouperConfig.propertyValueInt("grouper.ai.agent.tool." + toolName + ".maxPageSize",
          grouperConfig.propertyValueInt("grouper.ai.agent.maxPageSize", 200));
    }
    return grouperConfig.propertyValueInt("grouper.mcp.tool." + toolName + ".maxPageSize",
        grouperConfig.propertyValueInt("grouper.mcp.maxPageSize", 0));
  }

  /**
   * cap the pageSize of a tool which returns a list.  a pageSize over the limit is lowered to it,
   * and a missing one is set to it if the tool would otherwise return more.  the caller's arguments
   * are not changed; a copy is returned when anything changes
   * @param grouperTool the tool, may be null
   * @param entryPath the front door the call came through, which decides the limit
   * @param arguments as the caller supplied them, may be null
   * @param limitedTo set to the limit if pageSize was changed
   * @return the arguments to run the tool with
   */
  static JsonNode applyPageSizeLimit(GrouperTool grouperTool, GrouperToolEntryPath entryPath,
      JsonNode arguments, Integer[] limitedTo) {

    if (grouperTool == null || (arguments != null && !arguments.isObject())) {
      return arguments;
    }

    Integer pageSizeWhenNotGiven = grouperTool.pageSizeWhenNotGiven(arguments);
    if (pageSizeWhenNotGiven == null) {
      return arguments;
    }

    return applyPageSizeLimit(pageSizeWhenNotGiven, maxPageSize(grouperTool.name(), entryPath),
        arguments, limitedTo);
  }

  /**
   * @param pageSizeWhenNotGiven the tool's page size when none is given, 0 for everything
   * @param maxPageSize the limit, 0 or less for none
   * @param arguments as the caller supplied them, an object or null
   * @param limitedTo set to the limit if pageSize was changed
   * @return the arguments to run the tool with
   */
  static JsonNode applyPageSizeLimit(int pageSizeWhenNotGiven, int maxPageSize, JsonNode arguments,
      Integer[] limitedTo) {

    if (maxPageSize <= 0) {
      return arguments;
    }

    JsonNode pageSizeNode = arguments == null ? null : arguments.get("pageSize");
    boolean given = pageSizeNode != null;

    // a given pageSize is read as the tools read it.  one below 1 counts as over the limit: the
    // tools do not refuse it, and 0 reaches the database as no limit at all.  a value which is not a
    // number reads as 0, and one too large for an int can read as 0 or less, so they are caught too.
    // so is an explicit null, which some tools read as 0 rather than as not given
    boolean overLimit = given ? (pageSizeNode.asInt(0) < 1 || pageSizeNode.asInt(0) > maxPageSize)
        : (pageSizeWhenNotGiven <= 0 || pageSizeWhenNotGiven > maxPageSize);
    if (!overLimit) {
      return arguments;
    }

    ObjectNode limited = arguments == null ? objectMapper.createObjectNode() : ((ObjectNode)arguments).deepCopy();
    limited.put("pageSize", maxPageSize);
    limitedTo[0] = maxPageSize;
    return limited;
  }

  /**
   * whether a result the cap applied to could have been cut short by it: some list in it has as
   * many entries as the cap.  the tools return JSON with the page somewhere in it, under a name
   * which differs by tool, so every list is looked at.  a result which is not JSON counts as
   * possibly full, so the note is only left off when the result shows it is not needed
   * @param resultText the text the tool returned
   * @param maxPageSize the limit
   * @return true if the result might not be everything
   */
  static boolean isPossiblyFullPage(String resultText, int maxPageSize) {
    if (StringUtils.isBlank(resultText)) {
      return false;
    }
    JsonNode resultJson = null;
    try {
      resultJson = objectMapper.readTree(resultText);
    } catch (Exception e) {
      return true;
    }
    if (resultJson == null || !resultJson.isContainerNode()) {
      return true;
    }
    return hasListOfAtLeast(resultJson, maxPageSize);
  }

  /**
   * @param jsonNode a node of a result
   * @param size how many entries
   * @return true if this node, or any node in it, is a list with at least that many entries
   */
  private static boolean hasListOfAtLeast(JsonNode jsonNode, int size) {
    if (jsonNode.isArray() && jsonNode.size() >= size) {
      return true;
    }
    // the elements of a list, or the values of an object
    for (JsonNode child : jsonNode) {
      if (hasListOfAtLeast(child, size)) {
        return true;
      }
    }
    return false;
  }

  /**
   * tell the caller the results were capped, so a model does not take a page for the whole list
   * @param result the tool result
   * @param maxPageSize the limit
   */
  private static void appendPageSizeNote(ObjectNode result, int maxPageSize) {
    JsonNode content = result.get("content");
    if (content == null || !content.isArray()) {
      return;
    }
    ObjectNode note = ((ArrayNode)content).addObject();
    note.put("type", "text");
    note.put("text", "Note: this tool returns at most " + maxPageSize + " results per call, so this "
        + "may not be everything.  Use pageNumber for the next page if the tool takes it, or narrow "
        + "the request.");
  }

  /**
   * the definitions of the tools this caller should be offered, in the order they have always
   * been advertised in.  this is the one place that decides what to offer, so that MCP's
   * tools/list and the UI agent cannot drift apart on it.  a tool is offered when the caller is
   * allowed to use it, when there is any point offering it (both of which the tool answers for
   * itself), and when the institution has not turned it off.  offering is only a convenience:
   * {@link #executeTool} checks access again on every call.
   *
   * @param authUser who is asking, carrying their scope
   * @return the tool definitions, never null
   */
  public static ArrayNode retrieveToolDefinitions(final GrouperMcpAuthUser authUser) {

    final ArrayNode toolsArray = objectMapper.createArrayNode();

    // as the caller, since a couple of tools describe themselves according to what the caller can
    // see and do
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject(), false);
    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

          // the same as while a tool runs, so that a tool which calls the web service logic to
          // describe itself does so as the caller, from the UI as well as from MCP
          GrouperSession previousToolGrouperSession = threadLocalToolGrouperSession.get();
          threadLocalToolGrouperSession.set(theGrouperSession);

          try {

            for (GrouperTool grouperTool : GrouperToolRegistry.advertisedTools()) {

              // includes, for a caller on the limited tier of a category, whether this tool has
              // been opened to them (GRP-7415)
              if (!GrouperToolAccess.isAllowed(grouperTool, grouperTool.category(null), authUser)) {
                continue;
              }

              if (!grouperTool.availableFor(authUser)) {
                continue;
              }

              // null when the tool has nothing to offer this caller
              ObjectNode toolDef = grouperTool.toolDefinition(authUser);
              if (toolDef == null) {
                continue;
              }

              String toolName = toolDef.get("name").asText();

              // a tool which is not in GrouperMcpToolNames cannot be pointed at by a recipe,
              // because the recipe configuration validates against that list.  failing here rather
              // than quietly advertising it means adding a tool without registering it is caught
              // the first time the list is built, not months later by somebody wondering why their
              // recipe does nothing
              if (!GrouperMcpToolNames.isToolName(toolName)) {
                throw new RuntimeException("MCP tool '" + toolName + "' is not in GrouperMcpToolNames. "
                    + "Add it there so recipes can point at it.");
              }

              if (GrouperMcpToolNames.isToolAllowedByConfig(toolName)) {
                toolsArray.add(toolDef);
              }
            }

          } finally {
            if (previousToolGrouperSession == null) {
              threadLocalToolGrouperSession.remove();
            } else {
              threadLocalToolGrouperSession.set(previousToolGrouperSession);
            }
          }

          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // a recipe which names tools gets a pointer added to those tools' own descriptions.  it is
    // done here, over the finished list, so it covers every tool without each one having to know
    // about recipes, and so the recipes are looked up once rather than per tool
    GrouperMcpRecipeTool.appendRecipePointers(toolsArray, authUser);

    return toolsArray;
  }

  /**
   * @param result a tool result
   * @return the text a caller would read, or null
   */
  private static String retrieveResultText(ObjectNode result) {

    if (result == null || !result.has("content") || !result.get("content").isArray()) {
      return null;
    }

    ArrayNode content = (ArrayNode) result.get("content");
    if (content.size() == 0) {
      return null;
    }

    JsonNode firstContent = content.get(0);
    if (!firstContent.has("text")) {
      return null;
    }

    return firstContent.get("text").asText();
  }

  /**
   * @param errorMessage what went wrong
   * @return a result the caller can read and explain
   */
  public static ObjectNode buildErrorResult(String errorMessage) {

    ObjectNode errorResult = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();

    textContent.put("type", "text");
    textContent.put("text", errorMessage);
    content.add(textContent);

    errorResult.set("content", content);
    errorResult.put("isError", true);

    return errorResult;
  }

}

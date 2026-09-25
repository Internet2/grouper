/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
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
    final JsonNode theArguments = arguments;

    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

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

            } else {
              resultHolder[0] = grouperTool.execute(theArguments, authUser);
            }

            isErrorHolder[0] = resultHolder[0].has("isError")
                && resultHolder[0].get("isError").asBoolean(false);
            responseTextHolder[0] = retrieveResultText(resultHolder[0]);

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

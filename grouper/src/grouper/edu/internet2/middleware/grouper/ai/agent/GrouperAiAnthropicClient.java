/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * the Anthropic Messages API (POST /v1/messages) over plain HTTP.
 *
 * <p>what this relies on from the API:</p>
 * <ul>
 *   <li>an assistant turn is replayed exactly as it came back, thinking blocks included, since the
 *   model's reasoning is bound to it.  the agent edits history in three ways: it summarizes older
 *   turns, it replaces long tool results from earlier questions with a stub, and it offers a changed
 *   tool list.  after any of them the thinking blocks of the turns after the edit are stripped (see
 *   {@link #stripReasoningForEditedHistory(String)}): they were produced with the history as it was,
 *   and replaying them after it changed is rejected.</li>
 *   <li>every tool_use gets a tool_result, all of one turn's results in one user message</li>
 *   <li>tools and the system prompt are identical on every call and come first, so the system block
 *   is marked for caching (which covers the tools in front of it), and the top level marker caches
 *   the conversation so far</li>
 * </ul>
 */
public class GrouperAiAnthropicClient implements GrouperAiLlmClient {

  /** the provider name recorded on assistant turns */
  public static final String PROVIDER = "anthropic";

  /** the API version header */
  static final String ANTHROPIC_VERSION = "2023-06-01";

  /** beta header for server side fallback when the model declines */
  static final String SERVER_SIDE_FALLBACK_BETA = "server-side-fallback-2026-07-01";

  /** for building and reading JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the bearer token external system with the endpoint and key */
  private String externalSystemConfigId;

  /** if the provider should rerun a declined request on a fallback model */
  private boolean serverSideFallback;

  /** how long to wait for the provider */
  private int timeoutMillis;

  /**
   * @param theExternalSystemConfigId the bearer token external system with the endpoint (e.g.
   * https://api.anthropic.com/v1) and key.  it must send the key in the x-api-key header with no
   * Bearer prefix
   * @param theServerSideFallback if the provider should rerun a declined request on a fallback model
   * @param theTimeoutMillis how long to wait for the provider
   */
  public GrouperAiAnthropicClient(String theExternalSystemConfigId, boolean theServerSideFallback,
      int theTimeoutMillis) {
    this.externalSystemConfigId = theExternalSystemConfigId;
    this.serverSideFallback = theServerSideFallback;
    this.timeoutMillis = theTimeoutMillis;
  }

  /**
   * @see GrouperAiLlmClient#providerName()
   */
  public String providerName() {
    return PROVIDER;
  }

  /**
   * @see GrouperAiLlmClient#send(GrouperAiLlmRequest)
   */
  public GrouperAiLlmResponse send(GrouperAiLlmRequest request) {

    ObjectNode body = buildRequestBody(request, this.serverSideFallback);

    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("anthropic-version", ANTHROPIC_VERSION);
    if (this.serverSideFallback) {
      headers.put("anthropic-beta", SERVER_SIDE_FALLBACK_BETA);
    }

    GrouperAiLlmHttp.HttpResult httpResult = null;
    try {
      httpResult = GrouperAiLlmHttp.postJson(this.externalSystemConfigId, "/messages",
          body.toString(), headers, this.timeoutMillis);
    } catch (GrouperAiLlmHttpException httpException) {
      if (isContextTooLong(httpException.getResponseCode(), httpException.getResponseBody())) {
        throw new GrouperAiLlmContextTooLongException(httpException);
      }
      throw httpException;
    }

    GrouperAiLlmResponse response = null;
    try {
      response = parseResponse(httpResult.getBody());
    } catch (RuntimeException re) {
      throw GrouperAiLlmHttp.unreadableResponse(this.externalSystemConfigId, httpResult, re);
    }
    response.setProviderRequestId(httpResult.getRequestId());
    return response;
  }

  /**
   * whether an error from the provider means the request is more than the model can take in.
   * Anthropic answers 400 invalid_request_error with "prompt is too long: N tokens > M maximum",
   * or, when the input fits but not with max_tokens on top, "input length and `max_tokens` exceed
   * context limit: ...", or 413 request_too_large for a request over its size limit
   * @param responseCode the HTTP status
   * @param responseBody the body, may be null
   * @return true if the conversation is too long
   */
  static boolean isContextTooLong(int responseCode, String responseBody) {
    if (responseCode == 413) {
      return true;
    }
    if (responseCode != 400 || StringUtils.isBlank(responseBody)) {
      return false;
    }
    try {
      String message = objectMapper.readTree(responseBody).path("error").path("message").asText();
      return StringUtils.contains(message, "prompt is too long")
          || StringUtils.contains(message, "exceed context limit");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * build the request body
   * @param request what to send
   * @param serverSideFallback if the provider should rerun a declined request on a fallback model
   * @return the body
   */
  static ObjectNode buildRequestBody(GrouperAiLlmRequest request, boolean serverSideFallback) {

    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", request.getModel());
    body.put("max_tokens", request.getMaxOutputTokens());

    // stable prefix first.  the system block's cache marker also covers the tools rendered before it
    ArrayNode system = body.putArray("system");
    ObjectNode systemBlock = system.addObject();
    systemBlock.put("type", "text");
    systemBlock.put("text", request.getSystemPrompt());
    systemBlock.putObject("cache_control").put("type", "ephemeral");

    ArrayNode toolDefinitions = request.getToolDefinitions();
    if (toolDefinitions != null && toolDefinitions.size() > 0) {
      ArrayNode tools = body.putArray("tools");
      for (JsonNode toolDefinition : toolDefinitions) {
        ObjectNode tool = tools.addObject();
        tool.put("name", toolDefinition.get("name").asText());
        tool.put("description", toolDefinition.path("description").asText(""));
        tool.set("input_schema", toolDefinition.get("inputSchema"));
      }
    }

    // caches the conversation so far on each call, so each step of the loop reads the previous
    // steps from the cache
    body.putObject("cache_control").put("type", "ephemeral");

    if (serverSideFallback) {
      body.put("fallbacks", "default");
    }

    ArrayNode messages = body.putArray("messages");

    for (GrouperAiAgentMessage message : request.getMessages()) {

      ObjectNode messageNode = messages.addObject();

      if (message.getRole() == GrouperAiAgentMessage.Role.user) {

        messageNode.put("role", "user");
        ArrayNode content = messageNode.putArray("content");
        ObjectNode textBlock = content.addObject();
        textBlock.put("type", "text");
        textBlock.put("text", message.getText());

      } else if (message.getRole() == GrouperAiAgentMessage.Role.assistant) {

        messageNode.put("role", "assistant");
        messageNode.set("content", assistantContent(message));

      } else {

        // every result for one assistant turn goes back in a single user message
        messageNode.put("role", "user");
        ArrayNode content = messageNode.putArray("content");
        for (GrouperAiAgentToolResult toolResult : message.getToolResults()) {
          ObjectNode resultBlock = content.addObject();
          resultBlock.put("type", "tool_result");
          resultBlock.put("tool_use_id", toolResult.getToolCallId());
          resultBlock.put("content", StringUtils.defaultString(toolResult.getContent()));
          if (toolResult.isError()) {
            resultBlock.put("is_error", true);
          }
        }
      }
    }

    return body;
  }

  /**
   * the content of an assistant turn: the model's own output verbatim if it came from this
   * provider, otherwise rebuilt from the text and tool calls
   * @param message the assistant turn
   * @return the content blocks
   */
  private static ArrayNode assistantContent(GrouperAiAgentMessage message) {

    if (StringUtils.equals(PROVIDER, message.getRawProvider())
        && !StringUtils.isBlank(message.getRawJson())) {
      JsonNode raw = readJson(message.getRawJson());
      // a response can come back with no content, e.g. on a refusal, and an assistant turn with no
      // content is rejected, so that one is rebuilt below
      if (raw.isArray() && raw.size() > 0) {
        return (ArrayNode)raw;
      }
    }

    ArrayNode content = objectMapper.createArrayNode();

    if (!StringUtils.isBlank(message.getText())) {
      ObjectNode textBlock = content.addObject();
      textBlock.put("type", "text");
      textBlock.put("text", message.getText());
    }

    for (GrouperAiAgentToolCall toolCall : message.getToolCalls()) {
      ObjectNode toolUse = content.addObject();
      toolUse.put("type", "tool_use");
      toolUse.put("id", toolCall.getId());
      toolUse.put("name", toolCall.getName());
      toolUse.set("input", readJson(StringUtils.defaultIfBlank(toolCall.getArgumentsJson(), "{}")));
    }

    // an assistant turn cannot be empty
    if (content.size() == 0) {
      ObjectNode textBlock = content.addObject();
      textBlock.put("type", "text");
      textBlock.put("text", "(no text)");
    }

    return content;
  }

  /**
   * read the response body
   * @param responseBody the JSON the provider sent back
   * @return the response
   */
  static GrouperAiLlmResponse parseResponse(String responseBody) {

    JsonNode root = readJson(responseBody);

    JsonNode content = root.get("content");
    if (content == null || !content.isArray()) {
      // the body is not in the message: it is logged, and a response can hold Grouper data
      throw new RuntimeException("Anthropic response has no content, stop reason: "
          + GrouperUtil.abbreviate(root.path("stop_reason").asText(null), 100));
    }

    StringBuilder text = new StringBuilder();
    List<GrouperAiAgentToolCall> toolCalls = new ArrayList<GrouperAiAgentToolCall>();

    // thinking, redacted thinking and fallback blocks are not read here, but they stay in the raw
    // content and go back unchanged
    for (JsonNode block : content) {
      String type = block.path("type").asText();
      if (StringUtils.equals("text", type)) {
        if (text.length() > 0) {
          text.append("\n\n");
        }
        text.append(block.path("text").asText());
      } else if (StringUtils.equals("tool_use", type)) {
        JsonNode input = block.get("input");
        toolCalls.add(new GrouperAiAgentToolCall(block.path("id").asText(),
            block.path("name").asText(), input == null ? "{}" : input.toString()));
      }
    }

    String stopReasonString = root.path("stop_reason").asText();
    GrouperAiLlmStopReason stopReason = null;
    if (StringUtils.equals("tool_use", stopReasonString)) {
      stopReason = GrouperAiLlmStopReason.toolUse;
    } else if (StringUtils.equals("end_turn", stopReasonString)
        || StringUtils.equals("stop_sequence", stopReasonString)) {
      stopReason = GrouperAiLlmStopReason.endTurn;
    } else if (StringUtils.equals("max_tokens", stopReasonString)
        || StringUtils.equals("model_context_window_exceeded", stopReasonString)) {
      // the second is the model running out of context while writing: cut off the same way
      stopReason = GrouperAiLlmStopReason.maxTokens;
    } else if (StringUtils.equals("refusal", stopReasonString)) {
      stopReason = GrouperAiLlmStopReason.refusal;
    } else {
      stopReason = GrouperAiLlmStopReason.other;
    }

    GrouperAiAgentMessage assistantMessage = GrouperAiAgentMessage.assistant(
        text.length() == 0 ? null : text.toString(), toolCalls, content.toString(), PROVIDER);

    GrouperAiLlmResponse response = new GrouperAiLlmResponse(assistantMessage, stopReason);

    // Anthropic's input_tokens leaves out tokens read from or written to the cache.  they are added
    // back so input tokens mean the same thing for every provider: all of them, cached or not
    JsonNode usage = root.path("usage");
    long cacheReadTokens = usage.path("cache_read_input_tokens").asLong(0);
    long cacheWriteTokens = usage.path("cache_creation_input_tokens").asLong(0);
    response.setInputTokens(usage.path("input_tokens").asLong(0) + cacheReadTokens + cacheWriteTokens);
    response.setOutputTokens(usage.path("output_tokens").asLong(0));
    response.setCacheReadTokens(cacheReadTokens);
    response.setCacheWriteTokens(cacheWriteTokens);

    return response;
  }

  /**
   * drop thinking and redacted thinking blocks.  text, tool calls and anything else stay
   * @see GrouperAiLlmClient#stripReasoningForEditedHistory(String)
   */
  public String stripReasoningForEditedHistory(String rawJson) {

    if (StringUtils.isBlank(rawJson)) {
      return rawJson;
    }

    JsonNode content = readJson(rawJson);
    if (!content.isArray()) {
      return rawJson;
    }

    ArrayNode kept = objectMapper.createArrayNode();
    for (JsonNode block : content) {
      String type = block.path("type").asText();
      if (StringUtils.equals("thinking", type) || StringUtils.equals("redacted_thinking", type)) {
        continue;
      }
      kept.add(block);
    }

    // nothing left worth replaying: let the turn be rebuilt from its text and tool calls
    if (kept.size() == 0) {
      return null;
    }

    return kept.toString();
  }

  /**
   * @param json JSON text
   * @return the parsed JSON
   */
  private static JsonNode readJson(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      // not chained: the parser's message can quote the text it could not read, which is logged
      throw new RuntimeException("Cannot parse JSON from the AI provider: " + e.getClass().getSimpleName());
    }
  }

}

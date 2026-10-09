/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * the OpenAI Responses API (POST /v1/responses) over plain HTTP.
 *
 * <p>what this relies on from the API:</p>
 * <ul>
 *   <li>store is false, so OpenAI does not keep the conversation, which carries group and
 *   membership data.  the whole history is sent on every call instead of pointing at a stored
 *   previous response</li>
 *   <li>an assistant turn is replayed as every output item that came back, verbatim.  a reasoning
 *   model's reasoning items have to travel with the function calls they led to.  with nothing
 *   stored, OpenAI cannot look them up, so requests ask for them to come back carrying their
 *   encrypted content (include reasoning.encrypted_content), which is what makes the replay work.
 *   grouper.ai.agent.openai.includeReasoningContent turns that off for a model without
 *   reasoning</li>
 *   <li>each function call is answered by a function_call_output with the same call_id.  the API
 *   has no error flag on those, so a failed call's output says so in its text</li>
 * </ul>
 */
public class GrouperAiOpenAiClient implements GrouperAiLlmClient {

  /** the provider name recorded on assistant turns */
  public static final String PROVIDER = "openai";

  /** for building and reading JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the bearer token external system with the endpoint and key */
  private String externalSystemConfigId;

  /** if requests ask for reasoning items to come back with their encrypted content */
  private boolean includeReasoningContent;

  /** how long to wait for the provider */
  private int timeoutMillis;

  /**
   * @param theExternalSystemConfigId the bearer token external system with the endpoint (e.g.
   * https://api.openai.com/v1) and key, sent as Authorization: Bearer
   * @param theIncludeReasoningContent if requests ask for reasoning items to come back with their
   * encrypted content.  needed for a reasoning model; a model without reasoning may refuse it
   * @param theTimeoutMillis how long to wait for the provider
   */
  public GrouperAiOpenAiClient(String theExternalSystemConfigId, boolean theIncludeReasoningContent,
      int theTimeoutMillis) {
    this.externalSystemConfigId = theExternalSystemConfigId;
    this.includeReasoningContent = theIncludeReasoningContent;
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

    ObjectNode body = buildRequestBody(request, this.includeReasoningContent);

    GrouperAiLlmHttp.HttpResult httpResult = null;
    try {
      httpResult = GrouperAiLlmHttp.postJson(this.externalSystemConfigId, "/responses",
          body.toString(), null, this.timeoutMillis);
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
   * OpenAI answers 400 with the error code context_length_exceeded, or 413 for a request over its
   * size limit
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
      JsonNode error = objectMapper.readTree(responseBody).path("error");
      return StringUtils.equals("context_length_exceeded", error.path("code").asText());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * build the request body
   * @param request what to send
   * @param includeReasoningContent if reasoning items should come back with their encrypted content
   * @return the body
   */
  static ObjectNode buildRequestBody(GrouperAiLlmRequest request, boolean includeReasoningContent) {

    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", request.getModel());
    body.put("instructions", request.getSystemPrompt());
    body.put("max_output_tokens", request.getMaxOutputTokens());
    body.put("store", false);

    // with nothing stored, a reasoning item can only be replayed if it carries its own content, and
    // it only comes back with that content when asked for.  a model without reasoning has no
    // reasoning items and may refuse the request, so it can be turned off
    if (includeReasoningContent) {
      body.putArray("include").add("reasoning.encrypted_content");
    }

    ArrayNode toolDefinitions = request.getToolDefinitions();
    if (toolDefinitions != null && toolDefinitions.size() > 0) {
      ArrayNode tools = body.putArray("tools");
      for (JsonNode toolDefinition : toolDefinitions) {
        ObjectNode tool = tools.addObject();
        tool.put("type", "function");
        tool.put("name", toolDefinition.get("name").asText());
        tool.put("description", toolDefinition.path("description").asText(""));
        tool.set("parameters", toolDefinition.get("inputSchema"));
        // strict needs every property listed as required and no additional properties, which the
        // tool schemas do not promise, since they have optional arguments
        tool.put("strict", false);
      }
    }

    ArrayNode input = body.putArray("input");

    for (GrouperAiAgentMessage message : request.getMessages()) {

      if (message.getRole() == GrouperAiAgentMessage.Role.user) {

        ObjectNode item = input.addObject();
        item.put("role", "user");
        item.put("content", message.getText());

      } else if (message.getRole() == GrouperAiAgentMessage.Role.assistant) {

        addAssistantItems(input, message);

      } else {

        for (GrouperAiAgentToolResult toolResult : message.getToolResults()) {
          ObjectNode item = input.addObject();
          item.put("type", "function_call_output");
          item.put("call_id", toolResult.getToolCallId());
          String output = StringUtils.defaultString(toolResult.getContent());
          item.put("output", toolResult.isError() ? ("Error: " + output) : output);
        }
      }
    }

    return body;
  }

  /**
   * add an assistant turn to the input: the model's own output items verbatim if they came from
   * this provider, otherwise rebuilt from the text and tool calls
   * @param input the input items
   * @param message the assistant turn
   */
  private static void addAssistantItems(ArrayNode input, GrouperAiAgentMessage message) {

    if (StringUtils.equals(PROVIDER, message.getRawProvider())
        && !StringUtils.isBlank(message.getRawJson())) {
      JsonNode output = readJson(message.getRawJson());
      for (JsonNode outputItem : output) {
        input.add(outputItem);
      }
      return;
    }

    if (!StringUtils.isBlank(message.getText())) {
      ObjectNode item = input.addObject();
      item.put("role", "assistant");
      item.put("content", message.getText());
    }

    for (GrouperAiAgentToolCall toolCall : message.getToolCalls()) {
      ObjectNode item = input.addObject();
      item.put("type", "function_call");
      item.put("call_id", toolCall.getId());
      item.put("name", toolCall.getName());
      item.put("arguments", StringUtils.defaultIfBlank(toolCall.getArgumentsJson(), "{}"));
    }
  }

  /**
   * read the response body
   * @param responseBody the JSON the provider sent back
   * @return the response
   */
  static GrouperAiLlmResponse parseResponse(String responseBody) {

    JsonNode root = readJson(responseBody);

    // neither message carries text from the body: it is logged, and a response can hold Grouper data
    JsonNode error = root.get("error");
    if (error != null && !error.isNull()) {
      throw new RuntimeException("OpenAI response has an error, type: "
          + GrouperUtil.abbreviate(error.path("type").asText(null), 100)
          + ", code: " + GrouperUtil.abbreviate(error.path("code").asText(null), 100));
    }

    JsonNode output = root.get("output");
    if (output == null || !output.isArray()) {
      throw new RuntimeException("OpenAI response has no output, status: "
          + GrouperUtil.abbreviate(root.path("status").asText(null), 100));
    }

    StringBuilder text = new StringBuilder();
    List<GrouperAiAgentToolCall> toolCalls = new ArrayList<GrouperAiAgentToolCall>();
    boolean refused = false;

    // reasoning items are not read here, but they stay in the raw output and go back unchanged
    for (JsonNode item : output) {
      String type = item.path("type").asText();
      if (StringUtils.equals("message", type)) {
        for (JsonNode part : item.path("content")) {
          String partType = part.path("type").asText();
          if (StringUtils.equals("output_text", partType)) {
            appendText(text, part.path("text").asText());
          } else if (StringUtils.equals("refusal", partType)) {
            refused = true;
            appendText(text, part.path("refusal").asText());
          }
        }
      } else if (StringUtils.equals("function_call", type)) {
        toolCalls.add(new GrouperAiAgentToolCall(item.path("call_id").asText(),
            item.path("name").asText(), item.path("arguments").asText("{}")));
      }
    }

    GrouperAiLlmStopReason stopReason = null;
    String incompleteReason = root.path("incomplete_details").path("reason").asText();
    if (refused || StringUtils.equals("content_filter", incompleteReason)) {
      stopReason = GrouperAiLlmStopReason.refusal;
    } else if (StringUtils.equals("max_output_tokens", incompleteReason)) {
      stopReason = GrouperAiLlmStopReason.maxTokens;
    } else if (StringUtils.equals("incomplete", root.path("status").asText())) {
      // incomplete for a reason not handled above.  checked before the tool calls, since a call in
      // a response which did not finish may itself be incomplete; the agent runs none of them
      stopReason = GrouperAiLlmStopReason.other;
    } else if (toolCalls.size() > 0) {
      stopReason = GrouperAiLlmStopReason.toolUse;
    } else if (StringUtils.equals("completed", root.path("status").asText())) {
      stopReason = GrouperAiLlmStopReason.endTurn;
    } else {
      stopReason = GrouperAiLlmStopReason.other;
    }

    GrouperAiAgentMessage assistantMessage = GrouperAiAgentMessage.assistant(
        text.length() == 0 ? null : text.toString(), toolCalls, output.toString(), PROVIDER);

    GrouperAiLlmResponse response = new GrouperAiLlmResponse(assistantMessage, stopReason);

    JsonNode usage = root.path("usage");
    response.setInputTokens(usage.path("input_tokens").asLong(0));
    response.setOutputTokens(usage.path("output_tokens").asLong(0));
    response.setCacheReadTokens(usage.path("input_tokens_details").path("cached_tokens").asLong(0));

    return response;
  }

  /**
   * reasoning items are kept: a reasoning model requires them alongside the function calls they
   * led to, and they are not bound to the history in front of them the way Anthropic's thinking is
   * @see GrouperAiLlmClient#stripReasoningForEditedHistory(String)
   */
  public String stripReasoningForEditedHistory(String rawJson) {
    return rawJson;
  }

  /**
   * @param text what has been read so far
   * @param more more text
   */
  private static void appendText(StringBuilder text, String more) {
    if (StringUtils.isBlank(more)) {
      return;
    }
    if (text.length() > 0) {
      text.append("\n\n");
    }
    text.append(more);
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

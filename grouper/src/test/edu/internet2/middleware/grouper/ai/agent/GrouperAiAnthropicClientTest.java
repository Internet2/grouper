/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * the Anthropic Messages API request body and response parsing.  no provider needed
 */
public class GrouperAiAnthropicClientTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperAiAnthropicClientTest.class);
  }

  /**
   * @param name
   */
  public GrouperAiAnthropicClientTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @return one tool definition as the tool layer gives it
   */
  private static ArrayNode toolDefinitions() {
    ArrayNode result = objectMapper.createArrayNode();
    ObjectNode tool = result.addObject();
    tool.put("name", "group_find");
    tool.put("description", "find groups");
    tool.putObject("inputSchema").put("type", "object");
    return result;
  }

  /**
   * tools, system prompt, caching, fallback and every kind of message
   */
  public void testBuildRequestBody() {

    List<GrouperAiAgentMessage> messages = new ArrayList<GrouperAiAgentMessage>();
    messages.add(GrouperAiAgentMessage.user("find test"));

    // from this provider: replayed verbatim, thinking included
    List<GrouperAiAgentToolCall> toolCalls = new ArrayList<GrouperAiAgentToolCall>();
    toolCalls.add(new GrouperAiAgentToolCall("toolu_1", "group_find", "{\"name\":\"test\"}"));
    String raw = "[{\"type\":\"thinking\",\"thinking\":\"hmm\",\"signature\":\"sig\"},"
        + "{\"type\":\"tool_use\",\"id\":\"toolu_1\",\"name\":\"group_find\",\"input\":{\"name\":\"test\"}}]";
    messages.add(GrouperAiAgentMessage.assistant(null, toolCalls, raw, GrouperAiAnthropicClient.PROVIDER));

    List<GrouperAiAgentToolResult> toolResults = new ArrayList<GrouperAiAgentToolResult>();
    toolResults.add(new GrouperAiAgentToolResult("toolu_1", "group_find", "no such group", true));
    messages.add(GrouperAiAgentMessage.toolResults(toolResults));

    // not from this provider: rebuilt from text and tool calls
    List<GrouperAiAgentToolCall> otherToolCalls = new ArrayList<GrouperAiAgentToolCall>();
    otherToolCalls.add(new GrouperAiAgentToolCall("call_2", "group_find", "{\"name\":\"t\"}"));
    messages.add(GrouperAiAgentMessage.assistant("Trying again", otherToolCalls, "[]", GrouperAiOpenAiClient.PROVIDER));

    GrouperAiLlmRequest request = new GrouperAiLlmRequest("claude-opus-5", "be helpful", messages,
        toolDefinitions(), 16000);

    ObjectNode body = GrouperAiAnthropicClient.buildRequestBody(request, true);

    assertEquals("claude-opus-5", body.get("model").asText());
    assertEquals(16000, body.get("max_tokens").asInt());
    assertEquals("default", body.get("fallbacks").asText());
    assertEquals("ephemeral", body.path("cache_control").path("type").asText());
    assertNull(body.get("thinking"));

    JsonNode system = body.get("system").get(0);
    assertEquals("be helpful", system.get("text").asText());
    assertEquals("ephemeral", system.path("cache_control").path("type").asText());

    JsonNode tool = body.get("tools").get(0);
    assertEquals("group_find", tool.get("name").asText());
    assertEquals("object", tool.path("input_schema").path("type").asText());

    JsonNode messagesNode = body.get("messages");
    assertEquals(4, messagesNode.size());

    assertEquals("user", messagesNode.get(0).get("role").asText());
    assertEquals("find test", messagesNode.get(0).get("content").get(0).get("text").asText());

    assertEquals("assistant", messagesNode.get(1).get("role").asText());
    assertEquals("thinking", messagesNode.get(1).get("content").get(0).get("type").asText());
    assertEquals("sig", messagesNode.get(1).get("content").get(0).get("signature").asText());

    JsonNode toolResult = messagesNode.get(2).get("content").get(0);
    assertEquals("user", messagesNode.get(2).get("role").asText());
    assertEquals("tool_result", toolResult.get("type").asText());
    assertEquals("toolu_1", toolResult.get("tool_use_id").asText());
    assertTrue(toolResult.get("is_error").asBoolean());

    JsonNode rebuilt = messagesNode.get(3).get("content");
    assertEquals("Trying again", rebuilt.get(0).get("text").asText());
    assertEquals("tool_use", rebuilt.get(1).get("type").asText());
    assertEquals("call_2", rebuilt.get(1).get("id").asText());
    assertEquals("t", rebuilt.get(1).path("input").path("name").asText());

    ObjectNode noFallback = GrouperAiAnthropicClient.buildRequestBody(request, false);
    assertNull(noFallback.get("fallbacks"));
  }

  /**
   * a turn which came back with no content is not replayed empty
   */
  public void testEmptyContentRebuilt() {

    List<GrouperAiAgentMessage> messages = new ArrayList<GrouperAiAgentMessage>();
    messages.add(GrouperAiAgentMessage.user("something"));
    messages.add(GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[],\"stop_reason\":\"refusal\"}").getAssistantMessage());
    messages.add(GrouperAiAgentMessage.user("something else"));

    ObjectNode body = GrouperAiAnthropicClient.buildRequestBody(
        new GrouperAiLlmRequest("claude-opus-5", "be helpful", messages, null, 16000), false);

    JsonNode content = body.get("messages").get(1).get("content");
    assertEquals(1, content.size());
    assertEquals("(no text)", content.get(0).get("text").asText());
  }

  /**
   * text, tool calls, stop reason, usage, and the raw content kept to replay
   */
  public void testParseResponse() {

    String responseBody = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
        + "\"content\":[{\"type\":\"thinking\",\"thinking\":\"hmm\",\"signature\":\"sig\"},"
        + "{\"type\":\"text\",\"text\":\"Looking\"},"
        + "{\"type\":\"tool_use\",\"id\":\"toolu_1\",\"name\":\"group_find\",\"input\":{\"name\":\"test\"}}],"
        + "\"stop_reason\":\"tool_use\","
        + "\"usage\":{\"input_tokens\":100,\"output_tokens\":20,\"cache_read_input_tokens\":50,\"cache_creation_input_tokens\":30}}";

    GrouperAiLlmResponse response = GrouperAiAnthropicClient.parseResponse(responseBody);

    assertEquals(GrouperAiLlmStopReason.toolUse, response.getStopReason());
    GrouperAiAgentMessage message = response.getAssistantMessage();
    assertEquals("Looking", message.getText());
    assertEquals(1, message.getToolCalls().size());
    assertEquals("toolu_1", message.getToolCalls().get(0).getId());
    assertEquals("{\"name\":\"test\"}", message.getToolCalls().get(0).getArgumentsJson());
    assertEquals(GrouperAiAnthropicClient.PROVIDER, message.getRawProvider());
    assertTrue(message.getRawJson().contains("\"signature\":\"sig\""));

    // input tokens include the cached ones, as they do for OpenAI
    assertEquals(180, response.getInputTokens());
    assertEquals(20, response.getOutputTokens());
    assertEquals(50, response.getCacheReadTokens());
    assertEquals(30, response.getCacheWriteTokens());

    assertEquals(GrouperAiLlmStopReason.endTurn, GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[{\"type\":\"text\",\"text\":\"hi\"}],\"stop_reason\":\"end_turn\"}").getStopReason());
    assertEquals(GrouperAiLlmStopReason.maxTokens, GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[],\"stop_reason\":\"max_tokens\"}").getStopReason());
    assertEquals(GrouperAiLlmStopReason.maxTokens, GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[],\"stop_reason\":\"model_context_window_exceeded\"}").getStopReason());
    assertEquals(GrouperAiLlmStopReason.other, GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[],\"stop_reason\":\"something_new\"}").getStopReason());
    assertEquals(GrouperAiLlmStopReason.refusal, GrouperAiAnthropicClient.parseResponse(
        "{\"content\":[],\"stop_reason\":\"refusal\"}").getStopReason());
  }

  /**
   * thinking goes, everything else stays, and a turn with nothing left is rebuilt
   */
  public void testStripReasoning() {

    GrouperAiAnthropicClient client = new GrouperAiAnthropicClient("test", true, 1000);

    String stripped = client.stripReasoningForEditedHistory(
        "[{\"type\":\"thinking\",\"thinking\":\"hmm\",\"signature\":\"sig\"},"
        + "{\"type\":\"redacted_thinking\",\"data\":\"x\"},"
        + "{\"type\":\"text\",\"text\":\"Looking\"}]");
    assertFalse(stripped, stripped.contains("thinking"));
    assertTrue(stripped, stripped.contains("Looking"));

    assertNull(client.stripReasoningForEditedHistory("[{\"type\":\"thinking\",\"thinking\":\"hmm\"}]"));
  }

  /**
   * a conversation too long for the model is told apart from other errors
   */
  public void testContextTooLong() {
    assertTrue(GrouperAiAnthropicClient.isContextTooLong(400, "{\"type\":\"error\",\"error\":{\"type\":"
        + "\"invalid_request_error\",\"message\":\"prompt is too long: 1000500 tokens > 1000000 maximum\"}}"));
    assertTrue(GrouperAiAnthropicClient.isContextTooLong(400, "{\"type\":\"error\",\"error\":{\"type\":"
        + "\"invalid_request_error\",\"message\":\"input length and `max_tokens` exceed context limit: "
        + "190000 + 16000 > 200000, decrease input length or `max_tokens` and try again\"}}"));
    assertTrue(GrouperAiAnthropicClient.isContextTooLong(413, "{\"type\":\"error\",\"error\":{\"type\":"
        + "\"request_too_large\",\"message\":\"Request exceeds the maximum allowed number of bytes.\"}}"));

    assertFalse(GrouperAiAnthropicClient.isContextTooLong(400, "{\"type\":\"error\",\"error\":{\"type\":"
        + "\"invalid_request_error\",\"message\":\"max_tokens: must be greater than 0\"}}"));
    assertFalse(GrouperAiAnthropicClient.isContextTooLong(529, "{\"type\":\"error\",\"error\":{\"type\":"
        + "\"overloaded_error\",\"message\":\"Overloaded\"}}"));
    assertFalse(GrouperAiAnthropicClient.isContextTooLong(400, "not json"));
    assertFalse(GrouperAiAnthropicClient.isContextTooLong(400, null));
  }

}

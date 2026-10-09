/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * the OpenAI Responses API request body and response parsing.  no provider needed
 */
public class GrouperAiOpenAiClientTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperAiOpenAiClientTest.class);
  }

  /**
   * @param name
   */
  public GrouperAiOpenAiClientTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * tools, instructions, storage off, and every kind of input item
   */
  public void testBuildRequestBody() {

    ArrayNode toolDefinitions = objectMapper.createArrayNode();
    ObjectNode toolDefinition = toolDefinitions.addObject();
    toolDefinition.put("name", "group_find");
    toolDefinition.put("description", "find groups");
    toolDefinition.putObject("inputSchema").put("type", "object");

    List<GrouperAiAgentMessage> messages = new ArrayList<GrouperAiAgentMessage>();
    messages.add(GrouperAiAgentMessage.user("find test"));

    // from this provider: every output item replayed, reasoning included
    List<GrouperAiAgentToolCall> toolCalls = new ArrayList<GrouperAiAgentToolCall>();
    toolCalls.add(new GrouperAiAgentToolCall("call_1", "group_find", "{\"name\":\"test\"}"));
    String raw = "[{\"type\":\"reasoning\",\"id\":\"rs_1\",\"encrypted_content\":\"enc\",\"summary\":[]},"
        + "{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"group_find\",\"arguments\":\"{\\\"name\\\":\\\"test\\\"}\"}]";
    messages.add(GrouperAiAgentMessage.assistant(null, toolCalls, raw, GrouperAiOpenAiClient.PROVIDER));

    List<GrouperAiAgentToolResult> toolResults = new ArrayList<GrouperAiAgentToolResult>();
    toolResults.add(new GrouperAiAgentToolResult("call_1", "group_find", "no such group", true));
    messages.add(GrouperAiAgentMessage.toolResults(toolResults));

    // not from this provider: rebuilt from text and tool calls
    List<GrouperAiAgentToolCall> otherToolCalls = new ArrayList<GrouperAiAgentToolCall>();
    otherToolCalls.add(new GrouperAiAgentToolCall("toolu_2", "group_find", "{\"name\":\"t\"}"));
    messages.add(GrouperAiAgentMessage.assistant("Trying again", otherToolCalls, "[]", GrouperAiAnthropicClient.PROVIDER));

    GrouperAiLlmRequest request = new GrouperAiLlmRequest("gpt-test", "be helpful", messages,
        toolDefinitions, 16000);

    ObjectNode body = GrouperAiOpenAiClient.buildRequestBody(request, true);

    assertEquals("gpt-test", body.get("model").asText());
    assertEquals("be helpful", body.get("instructions").asText());
    assertEquals(16000, body.get("max_output_tokens").asInt());
    assertFalse(body.get("store").asBoolean());

    // without this, reasoning items come back empty and replaying them is rejected when nothing is
    // stored
    assertEquals(1, body.get("include").size());
    assertEquals("reasoning.encrypted_content", body.get("include").get(0).asText());

    // turned off for a model without reasoning
    assertNull(GrouperAiOpenAiClient.buildRequestBody(request, false).get("include"));

    JsonNode tool = body.get("tools").get(0);
    assertEquals("function", tool.get("type").asText());
    assertEquals("group_find", tool.get("name").asText());
    assertEquals("object", tool.path("parameters").path("type").asText());
    assertFalse(tool.get("strict").asBoolean());

    JsonNode input = body.get("input");
    assertEquals(6, input.size());

    assertEquals("user", input.get(0).get("role").asText());
    assertEquals("find test", input.get(0).get("content").asText());

    assertEquals("reasoning", input.get(1).get("type").asText());
    assertEquals("enc", input.get(1).get("encrypted_content").asText());
    assertEquals("function_call", input.get(2).get("type").asText());

    assertEquals("function_call_output", input.get(3).get("type").asText());
    assertEquals("call_1", input.get(3).get("call_id").asText());
    assertEquals("Error: no such group", input.get(3).get("output").asText());

    assertEquals("assistant", input.get(4).get("role").asText());
    assertEquals("Trying again", input.get(4).get("content").asText());
    assertEquals("function_call", input.get(5).get("type").asText());
    assertEquals("toolu_2", input.get(5).get("call_id").asText());
    assertEquals("{\"name\":\"t\"}", input.get(5).get("arguments").asText());
  }

  /**
   * text, function calls, stop reason, usage, and the raw output kept to replay
   */
  public void testParseResponse() {

    String responseBody = "{\"id\":\"resp_1\",\"status\":\"completed\",\"output\":["
        + "{\"type\":\"reasoning\",\"id\":\"rs_1\",\"encrypted_content\":\"enc\",\"summary\":[]},"
        + "{\"type\":\"message\",\"role\":\"assistant\",\"content\":[{\"type\":\"output_text\",\"text\":\"Looking\"}]},"
        + "{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"group_find\",\"arguments\":\"{\\\"name\\\":\\\"test\\\"}\"}],"
        + "\"usage\":{\"input_tokens\":100,\"input_tokens_details\":{\"cached_tokens\":40},\"output_tokens\":20}}";

    GrouperAiLlmResponse response = GrouperAiOpenAiClient.parseResponse(responseBody);

    assertEquals(GrouperAiLlmStopReason.toolUse, response.getStopReason());
    GrouperAiAgentMessage message = response.getAssistantMessage();
    assertEquals("Looking", message.getText());
    assertEquals(1, message.getToolCalls().size());
    assertEquals("call_1", message.getToolCalls().get(0).getId());
    assertEquals("{\"name\":\"test\"}", message.getToolCalls().get(0).getArgumentsJson());
    assertEquals(GrouperAiOpenAiClient.PROVIDER, message.getRawProvider());
    assertTrue(message.getRawJson().contains("\"encrypted_content\":\"enc\""));

    assertEquals(100, response.getInputTokens());
    assertEquals(20, response.getOutputTokens());
    assertEquals(40, response.getCacheReadTokens());

    assertEquals(GrouperAiLlmStopReason.endTurn, GrouperAiOpenAiClient.parseResponse(
        "{\"status\":\"completed\",\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":\"hi\"}]}]}")
        .getStopReason());
    assertEquals(GrouperAiLlmStopReason.maxTokens, GrouperAiOpenAiClient.parseResponse(
        "{\"status\":\"incomplete\",\"incomplete_details\":{\"reason\":\"max_output_tokens\"},\"output\":[]}")
        .getStopReason());
    assertEquals(GrouperAiLlmStopReason.refusal, GrouperAiOpenAiClient.parseResponse(
        "{\"status\":\"incomplete\",\"incomplete_details\":{\"reason\":\"content_filter\"},\"output\":[]}")
        .getStopReason());

    // incomplete for another reason, with a call in it: not a tool call to run
    assertEquals(GrouperAiLlmStopReason.other, GrouperAiOpenAiClient.parseResponse(
        "{\"status\":\"incomplete\",\"incomplete_details\":{\"reason\":\"something_new\"},\"output\":["
        + "{\"type\":\"function_call\",\"call_id\":\"call_1\",\"name\":\"group_find\",\"arguments\":\"{\"}]}")
        .getStopReason());

    GrouperAiLlmResponse refusal = GrouperAiOpenAiClient.parseResponse(
        "{\"status\":\"completed\",\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"refusal\",\"refusal\":\"no\"}]}]}");
    assertEquals(GrouperAiLlmStopReason.refusal, refusal.getStopReason());
    assertEquals("no", refusal.getAssistantMessage().getText());
  }

  /**
   * an error in the body is thrown with its type and code, but not its message, which is free text
   * that could echo the request, and exception messages are logged
   */
  public void testParseError() {
    try {
      GrouperAiOpenAiClient.parseResponse("{\"error\":{\"message\":\"secret group a:b\",\"type\":"
          + "\"invalid_request_error\",\"code\":\"invalid_value\"},\"output\":[]}");
      fail("Expected an exception");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("invalid_request_error"));
      assertTrue(re.getMessage(), re.getMessage().contains("invalid_value"));
      assertFalse(re.getMessage(), re.getMessage().contains("secret group"));
    }

    // a body with no output: nothing from it in the message
    try {
      GrouperAiOpenAiClient.parseResponse("{\"status\":\"completed\",\"note\":\"member jsmith\"}");
      fail("Expected an exception");
    } catch (RuntimeException re) {
      assertFalse(re.getMessage(), re.getMessage().contains("jsmith"));
    }

    // not JSON: the parser's message, which can quote the text, is not passed on
    try {
      GrouperAiOpenAiClient.parseResponse("member jsmith {");
      fail("Expected an exception");
    } catch (RuntimeException re) {
      assertFalse(re.getMessage(), re.getMessage().contains("jsmith"));
      assertNull(re.getCause());
    }
  }

  /**
   * the Retry-After header is read as seconds, rounded up
   */
  public void testRetryAfterSeconds() {
    Map<String, String> headers = new LinkedHashMap<String, String>();
    assertNull(GrouperAiLlmHttp.retryAfterSeconds(null));
    assertNull(GrouperAiLlmHttp.retryAfterSeconds(headers));

    headers.put("Retry-After", "30");
    assertEquals(Integer.valueOf(30), GrouperAiLlmHttp.retryAfterSeconds(headers));

    headers.put("Retry-After", " 1.5 ");
    assertEquals(Integer.valueOf(2), GrouperAiLlmHttp.retryAfterSeconds(headers));

    // the HTTP date form, and nonsense, are not read
    headers.put("Retry-After", "Fri, 02 Oct 2026 13:00:00 GMT");
    assertNull(GrouperAiLlmHttp.retryAfterSeconds(headers));
    headers.put("Retry-After", "-5");
    assertNull(GrouperAiLlmHttp.retryAfterSeconds(headers));
    headers.put("Retry-After", "NaN");
    assertNull(GrouperAiLlmHttp.retryAfterSeconds(headers));
  }

  /**
   * an HTTP error is described by its request id and error type and code, never its message
   */
  public void testDescribeError() {
    String described = GrouperAiLlmHttp.describeError("{\"type\":\"error\",\"error\":{\"type\":"
        + "\"invalid_request_error\",\"message\":\"messages.0: group a:secret\"}}", "req_123");
    assertEquals(", request id req_123, error type invalid_request_error", described);

    assertEquals(", response body is not JSON", GrouperAiLlmHttp.describeError("<html>group a:secret</html>", null));
    assertEquals("", GrouperAiLlmHttp.describeError(null, null));
  }

  /**
   * the request id comes from the first configured header the response has, case insensitive.  by
   * default the provider's own id, then a LiteLLM gateway's call id
   */
  public void testRequestId() {
    List<String> defaultHeaderNames = GrouperUtil.splitTrimToList(
        GrouperAiLlmHttp.REQUEST_ID_HEADERS_DEFAULT, ",");
    Map<String, String> headers = new LinkedHashMap<String, String>();
    assertNull(GrouperAiLlmHttp.requestId(null, defaultHeaderNames));
    assertNull(GrouperAiLlmHttp.requestId(headers, defaultHeaderNames));

    headers.put("X-Litellm-Call-Id", "e956fbbb-e31a-469c-8dc1-262b425222f5");
    assertEquals("e956fbbb-e31a-469c-8dc1-262b425222f5", GrouperAiLlmHttp.requestId(headers, defaultHeaderNames));

    // the provider's own id is earlier in the list, so it wins whichever header comes first
    headers.put("request-id", "req_789");
    assertEquals("req_789", GrouperAiLlmHttp.requestId(headers, defaultHeaderNames));

    // a blank value is passed over
    headers.clear();
    headers.put("x-request-id", " ");
    headers.put("X-Litellm-Call-Id", "call_1");
    assertEquals("call_1", GrouperAiLlmHttp.requestId(headers, defaultHeaderNames));

    // another gateway's header, once configured
    headers.clear();
    headers.put("cf-aig-log-id", "log_1");
    assertNull(GrouperAiLlmHttp.requestId(headers, defaultHeaderNames));
    List<String> configured = GrouperUtil.splitTrimToList("request-id, cf-aig-log-id", ",");
    assertEquals("log_1", GrouperAiLlmHttp.requestId(headers, configured));
  }

  /**
   * a 2xx response that cannot be read keeps the provider's request id, and no text from the body
   */
  public void testUnreadableResponse() {
    GrouperAiLlmHttp.HttpResult httpResult = new GrouperAiLlmHttp.HttpResult(200,
        "{\"output\":\"group a:secret\"", "req_456");
    RuntimeException parseException = null;
    try {
      GrouperAiOpenAiClient.parseResponse(httpResult.getBody());
      fail("The body is not complete JSON");
    } catch (RuntimeException re) {
      parseException = re;
    }

    GrouperAiLlmHttpException httpException = GrouperAiLlmHttp.unreadableResponse("openai", httpResult,
        parseException);
    assertEquals(200, httpException.getResponseCode());
    assertEquals("req_456", httpException.getRequestId());
    assertTrue(httpException.getMessage(), httpException.getMessage().startsWith(
        "AI provider returned HTTP 200 from external system 'openai', request id req_456, but the "
        + "response could not be read: "));
    assertFalse(httpException.getMessage(), httpException.getMessage().contains("secret"));
  }

  /**
   * a conversation too long for the model is told apart from other errors
   */
  public void testContextTooLong() {
    assertTrue(GrouperAiOpenAiClient.isContextTooLong(400, "{\"error\":{\"message\":\"Your input exceeds "
        + "the context window of this model.\",\"type\":\"invalid_request_error\",\"param\":\"input\","
        + "\"code\":\"context_length_exceeded\"}}"));
    assertTrue(GrouperAiOpenAiClient.isContextTooLong(413, null));

    assertFalse(GrouperAiOpenAiClient.isContextTooLong(400, "{\"error\":{\"message\":\"bad\","
        + "\"type\":\"invalid_request_error\",\"code\":\"invalid_value\"}}"));
    assertFalse(GrouperAiOpenAiClient.isContextTooLong(429, "{\"error\":{\"code\":\"rate_limit_exceeded\"}}"));
    assertFalse(GrouperAiOpenAiClient.isContextTooLong(400, "not json"));
    assertFalse(GrouperAiOpenAiClient.isContextTooLong(400, null));
  }

}

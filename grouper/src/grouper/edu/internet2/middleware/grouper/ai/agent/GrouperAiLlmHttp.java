/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * the HTTP call both provider clients make: a JSON POST to the provider, with the API key and base
 * URL taken from a web service bearer token external system (grouper.wsBearerToken.configId.*), so
 * the key is kept like every other external system secret.
 *
 * <p>request and response bodies are not logged: they carry the conversation, which carries group
 * and membership data, and the key header is not logged either.</p>
 */
public class GrouperAiLlmHttp {

  /**
   * a dropped connection is retried, a timeout is not.  a model call can legitimately take minutes,
   * so a call that timed out would most likely time out again, while the user waits and the
   * conversation stays locked
   */
  static final Predicate<Throwable> RETRY_NETWORK_ISSUE_NOT_TIMEOUT = new Predicate<Throwable>() {

    @Override
    public boolean test(Throwable throwable) {

      Throwable cause = throwable;

      // dont loop forever if the causes are cyclic
      for (int i = 0; i < 10 && cause != null; i++) {
        // SocketTimeoutException and ConnectTimeoutException extend InterruptedIOException
        if (cause instanceof InterruptedIOException) {
          return false;
        }
        if (cause.getCause() == cause) {
          break;
        }
        cause = cause.getCause();
      }

      // the default also retries anything whose stack mentions a timeout
      String fullStackTrace = GrouperUtil.getFullStackTrace(throwable);
      if (StringUtils.contains(fullStackTrace, "timed out")) {
        return false;
      }

      return GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT.test(throwable);
    }
  };

  /**
   * what the provider sent back from a call which worked
   */
  public static class HttpResult {

    /** the HTTP status */
    private int responseCode;

    /** the response body */
    private String body;

    /** the provider's id for the request, null if it sent none */
    private String requestId;

    /**
     * @param theResponseCode the HTTP status
     * @param theBody the response body
     * @param theRequestId the provider's id for the request, null if it sent none
     */
    public HttpResult(int theResponseCode, String theBody, String theRequestId) {
      this.responseCode = theResponseCode;
      this.body = theBody;
      this.requestId = theRequestId;
    }

    /**
     * @return the HTTP status
     */
    public int getResponseCode() {
      return this.responseCode;
    }

    /**
     * @return the response body
     */
    public String getBody() {
      return this.body;
    }

    /**
     * @return the provider's id for the request, null if it sent none
     */
    public String getRequestId() {
      return this.requestId;
    }
  }

  /**
   * post a JSON body to the provider and return the response body
   * @param externalSystemConfigId the bearer token external system with the endpoint and key
   * @param path appended to the external system's endpoint, e.g. /messages
   * @param bodyJson the request body
   * @param extraHeaders more headers to send, may be null
   * @param timeoutMillis how long to wait for the provider
   * @return the response body and the provider's request id
   * @throws GrouperAiLlmHttpException if the provider answered with anything other than 2xx
   */
  public static HttpResult postJson(String externalSystemConfigId, String path, String bodyJson,
      Map<String, String> extraHeaders, int timeoutMillis) {

    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignUrl(GrouperUtil.stripLastSlashIfExists(endpoint) + path);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    grouperHttpClient.addHeader("Content-Type", "application/json");

    if (extraHeaders != null) {
      for (String headerName : extraHeaders.keySet()) {
        grouperHttpClient.addHeader(headerName, extraHeaders.get(headerName));
      }
    }

    // the key goes in the header the external system is configured for: Authorization: Bearer for
    // OpenAI, x-api-key with no prefix for Anthropic
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient,
        externalSystemConfigId);

    grouperHttpClient.assignDoNotLogRequestBody(true);
    grouperHttpClient.assignDoNotLogResponseBody(true);

    // the http client only masks Authorization regardless of case, and matches other names
    // exactly, so the header the key is actually sent in is named here as configured
    Set<String> doNotLogHeaders = new HashSet<String>();
    doNotLogHeaders.add("Authorization");
    doNotLogHeaders.add("x-api-key");
    String keyHeader = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.wsBearerToken." + externalSystemConfigId + ".httpHeader");
    if (!StringUtils.isBlank(keyHeader)) {
      doNotLogHeaders.add(keyHeader);
    }
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);

    // somebody is waiting on the screen, so a throttled or dropped call is retried briefly rather
    // than with the minute long waits a background sync uses.  the provider's Retry-After is not
    // followed here, since the http client would sleep up to 20 minutes on it.  the agent waits out
    // a rate limit itself, up to grouper.ai.agent.rateLimitMaxWaitSeconds, where Stop can end the wait
    grouperHttpClient.setRetryForThrottlingOrNetworkIssues(2);
    grouperHttpClient.setRetryForThrottlingOrNetworkIssuesSleepMillis(2000);
    grouperHttpClient.setRetryForThrottlingOrNetworkIssuesBackOffMillis(2000);
    grouperHttpClient.assignRetryForThrottlingUseRetryAfter(false);
    grouperHttpClient.assignRetryNetworkIssuePredicate(RETRY_NETWORK_ISSUE_NOT_TIMEOUT);
    grouperHttpClient.assignTimeoutMillies(timeoutMillis);

    grouperHttpClient.assignBody(bodyJson);
    grouperHttpClient.executeRequest();

    int responseCode = grouperHttpClient.getResponseCode();
    String responseBody = grouperHttpClient.getResponseBody();
    String requestId = requestId(grouperHttpClient.getResponseHeaders(), requestIdHeaderNames());

    if (responseCode < 200 || responseCode > 299) {
      // the message is logged, so it carries no text from the body: a proxy, a gateway or a future
      // provider could echo the request, which is the conversation.  the status, the provider's
      // request id and its error type and code say what went wrong, and the request id lets the
      // provider find the rest.  the body stays on the exception for code which needs to read it
      GrouperAiLlmHttpException httpException = new GrouperAiLlmHttpException("AI provider returned HTTP "
          + responseCode + " from external system '" + externalSystemConfigId + "'"
          + describeError(responseBody, requestId), responseCode, responseBody, requestId);
      // so the agent can wait out a rate limit for as long as the provider says
      httpException.setRetryAfterSeconds(retryAfterSeconds(grouperHttpClient.getResponseHeaders()));
      throw httpException;
    }

    return new HttpResult(responseCode, responseBody, requestId);
  }

  /**
   * the provider answered, but its response could not be read.  it may have billed for the call, so
   * the error carries the provider's request id for the call log and for the provider's support
   * @param externalSystemConfigId the bearer token external system
   * @param httpResult what the provider sent back
   * @param parseException what went wrong reading it.  the provider clients' parse errors carry no
   * text from the body, so its message is kept
   * @return the error to throw
   */
  public static GrouperAiLlmHttpException unreadableResponse(String externalSystemConfigId,
      HttpResult httpResult, RuntimeException parseException) {
    StringBuilder message = new StringBuilder("AI provider returned HTTP " + httpResult.getResponseCode()
        + " from external system '" + externalSystemConfigId + "'");
    if (httpResult.getRequestId() != null) {
      message.append(", request id ").append(httpResult.getRequestId());
    }
    message.append(", but the response could not be read: ")
      .append(GrouperUtil.abbreviate(parseException.getMessage(), 300));
    GrouperAiLlmHttpException httpException = new GrouperAiLlmHttpException(message.toString(),
        httpResult.getResponseCode(), null, httpResult.getRequestId());
    httpException.initCause(parseException);
    return httpException;
  }

  /** response headers the request id is read from, in order, when grouper.ai.agent.requestIdHeaders
   * is blank: Anthropic's, OpenAI's, then a LiteLLM gateway's own call id */
  static final String REQUEST_ID_HEADERS_DEFAULT = "request-id, x-request-id, x-litellm-call-id";

  /**
   * the response headers a request id is read from, first found wins:
   * grouper.ai.agent.requestIdHeaders, so an institution behind a gateway which sends its own id
   * can add that header without a code change
   * @return the header names, in order
   */
  static List<String> requestIdHeaderNames() {
    String headerNames = GrouperConfig.retrieveConfig().propertyValueString("grouper.ai.agent.requestIdHeaders");
    if (StringUtils.isBlank(headerNames)) {
      headerNames = REQUEST_ID_HEADERS_DEFAULT;
    }
    return GrouperUtil.splitTrimToList(headerNames, ",");
  }

  /**
   * the id of a request, which the provider's support, or a gateway's logs, can look it up by.  the
   * first of the header names which the response has, with a value, wins
   * @param responseHeaders the response headers, may be null
   * @param headerNames the headers to look in, in order, case insensitive
   * @return the request id, cut to 100 characters, or null if there is none
   */
  static String requestId(Map<String, String> responseHeaders, List<String> headerNames) {
    if (responseHeaders == null || headerNames == null) {
      return null;
    }
    for (String wantedHeaderName : headerNames) {
      for (String headerName : responseHeaders.keySet()) {
        if (!StringUtils.equalsIgnoreCase(wantedHeaderName, headerName)) {
          continue;
        }
        String value = responseHeaders.get(headerName);
        if (!StringUtils.isBlank(value)) {
          return GrouperUtil.abbreviate(value, 100);
        }
      }
    }
    return null;
  }

  /**
   * how long the provider said to wait before trying again
   * @param responseHeaders the response headers, may be null
   * @return the Retry-After header in seconds, or null if there is none or it is not a number of
   * seconds (the HTTP date form is not read; the agent then waits its default)
   */
  static Integer retryAfterSeconds(Map<String, String> responseHeaders) {
    if (responseHeaders == null) {
      return null;
    }
    for (String headerName : responseHeaders.keySet()) {
      if (!StringUtils.equalsIgnoreCase("retry-after", headerName)) {
        continue;
      }
      String value = StringUtils.trim(responseHeaders.get(headerName));
      if (StringUtils.isBlank(value)) {
        return null;
      }
      try {
        // some gateways send fractions, e.g. 1.5
        double seconds = Double.parseDouble(value);
        if (Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds < 0) {
          return null;
        }
        return (int)Math.ceil(seconds);
      } catch (NumberFormatException nfe) {
        return null;
      }
    }
    return null;
  }

  /**
   * what can safely be logged about a provider's error response: its request id and its error
   * type and code, which are short identifiers, never the free text
   * @param responseBody the body, may be null
   * @param requestId the request id from the response headers, may be null
   * @return e.g. ", request id req_123, error type invalid_request_error", or an empty string
   */
  static String describeError(String responseBody, String requestId) {
    StringBuilder result = new StringBuilder();

    if (requestId != null) {
      result.append(", request id ").append(requestId);
    }

    // Anthropic: {"type":"error","error":{"type":...,"message":...}}.  OpenAI:
    // {"error":{"type":...,"code":...,"message":...}}.  the message is left out
    if (!StringUtils.isBlank(responseBody)) {
      try {
        JsonNode error = new ObjectMapper().readTree(responseBody).path("error");
        String type = error.path("type").asText(null);
        String code = error.path("code").asText(null);
        if (!StringUtils.isBlank(type)) {
          result.append(", error type ").append(GrouperUtil.abbreviate(type, 100));
        }
        if (!StringUtils.isBlank(code)) {
          result.append(", error code ").append(GrouperUtil.abbreviate(code, 100));
        }
      } catch (Exception e) {
        // not JSON, e.g. an html page from a proxy.  nothing from it is logged
        result.append(", response body is not JSON");
      }
    }

    return result.toString();
  }

}

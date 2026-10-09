/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

/**
 * the AI provider answered with an HTTP status other than 2xx, or with a 2xx response that could
 * not be read.  carries the status and body so a provider client can tell what went wrong, e.g.
 * that the conversation is too long for the model, and the provider's request id for the call log
 */
public class GrouperAiLlmHttpException extends RuntimeException {

  /** serial */
  private static final long serialVersionUID = 1L;

  /** the HTTP status */
  private int responseCode;

  /** the body the provider sent back, may be null */
  private String responseBody;

  /** the provider's id for the request, null if it sent none */
  private String requestId;

  /**
   * @param message what went wrong.  it is logged, so it must carry no text from the body
   * @param theResponseCode the HTTP status
   * @param theResponseBody the body the provider sent back, may be null
   * @param theRequestId the provider's id for the request, null if it sent none
   */
  public GrouperAiLlmHttpException(String message, int theResponseCode, String theResponseBody,
      String theRequestId) {
    super(message);
    this.responseCode = theResponseCode;
    this.responseBody = theResponseBody;
    this.requestId = theRequestId;
  }

  /**
   * @return the provider's id for the request, null if it sent none
   */
  public String getRequestId() {
    return this.requestId;
  }

  /** seconds the provider's Retry-After header said to wait, null if it sent none or not as seconds */
  private Integer retryAfterSeconds;

  /**
   * @return seconds the provider's Retry-After header said to wait, null if it sent none or not as
   * seconds
   */
  public Integer getRetryAfterSeconds() {
    return this.retryAfterSeconds;
  }

  /**
   * @param retryAfterSeconds1 seconds the provider's Retry-After header said to wait
   */
  public void setRetryAfterSeconds(Integer retryAfterSeconds1) {
    this.retryAfterSeconds = retryAfterSeconds1;
  }

  /**
   * @return the HTTP status
   */
  public int getResponseCode() {
    return this.responseCode;
  }

  /**
   * @return the body the provider sent back, may be null
   */
  public String getResponseBody() {
    return this.responseBody;
  }

}

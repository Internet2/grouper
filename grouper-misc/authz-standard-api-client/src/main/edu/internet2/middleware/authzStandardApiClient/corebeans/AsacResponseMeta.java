package edu.internet2.middleware.authzStandardApiClient.corebeans;

/**
 * metadata about the request/response that goes from server to client
 * @author mchyzer
 *
 */
public class AsacResponseMeta {

  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   */
  private String responseTimestamp;
  
  /**
   * number of milliseconds that the server took in processing this request
   */
  private long millis;
  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   */
  private String requestProcessed;

  /**
   * number of the HTTP httpStatusCode code
   */
  private int httpStatusCode;
  
  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   * @return the responseTimestamp
   */
  public String getResponseTimestamp() {
    return this.responseTimestamp;
  }

  
  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   * @param responseTimestamp1 the responseTimestamp to set
   */
  public void setResponseTimestamp(String responseTimestamp1) {
    this.responseTimestamp = responseTimestamp1;
  }

  
  /**
   * number of milliseconds that the server took in processing this request
   * @return the millis
   */
  public long getMillis() {
    return this.millis;
  }

  
  /**
   * number of milliseconds that the server took in processing this request
   * @param millis1 the millis to set
   */
  public void setMillis(long millis1) {
    this.millis = millis1;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @return the requestProcessed
   */
  public String getRequestProcessed() {
    return this.requestProcessed;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @param requestProcessed1 the requestProcessed to set
   */
  public void setRequestProcessed(String requestProcessed1) {
    this.requestProcessed = requestProcessed1;
  }

  
  /**
   * number of the HTTP httpStatusCode code
   * @return the httpgetHttpStatusCodee
   */
  public int getHttpStatusCode() {
    return this.httpStatusCode;
  }

  
  /**
   * number of the HTTP httpStatusCode code
   *setHttpStatusCodetatus1 the httpStatusCode to set
   */
  public void setHttpStatusCode(int status1) {
    this.httpStatusCode = status1;
  }
  
}

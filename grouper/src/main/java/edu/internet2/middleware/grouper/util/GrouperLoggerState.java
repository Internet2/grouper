package edu.internet2.middleware.grouper.util;

/**
 * state for logging e.g. for WS (request id and correlation id)
 * @author mchyzer
 *
 */
public class GrouperLoggerState {

  private String requestId;
  
  private String correlationId;

  
  public String getRequestId() {
    return requestId;
  }

  
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  
  public String getCorrelationId() {
    return correlationId;
  }

  
  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }
  
  
  
}

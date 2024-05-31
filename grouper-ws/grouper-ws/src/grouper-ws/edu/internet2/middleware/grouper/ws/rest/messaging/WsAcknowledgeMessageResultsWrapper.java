package edu.internet2.middleware.grouper.ws.rest.messaging;

import edu.internet2.middleware.grouper.ws.coresoap.WsMessageAcknowledgeResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAcknowledgeMessageResultsWrapper {
  WsMessageAcknowledgeResults WsAcknowledgeMessageResults = null;

  @ApiModelProperty(name = "WsAcknowledgeMessageResults", value = "Identifies the response of an acknowledge message request")
  public WsMessageAcknowledgeResults getWsAcknowledgeMessageResults() {
    return WsAcknowledgeMessageResults;
  }

  
  public void setWsAcknowledgeMessageResults(WsMessageAcknowledgeResults wsAcknowledgeMessageResults) {
    WsAcknowledgeMessageResults = wsAcknowledgeMessageResults;
  }
  

}

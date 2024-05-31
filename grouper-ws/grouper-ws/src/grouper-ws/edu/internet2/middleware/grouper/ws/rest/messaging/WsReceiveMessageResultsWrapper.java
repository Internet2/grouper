package edu.internet2.middleware.grouper.ws.rest.messaging;

import edu.internet2.middleware.grouper.ws.coresoap.WsMessageResults;
import io.swagger.annotations.ApiModelProperty;

public class WsReceiveMessageResultsWrapper {
  WsMessageResults WsReceiveMessageResults = null;

  @ApiModelProperty(name = "WsMessageResults", value = "Identifies the response of a seceive message request")
  public WsMessageResults getWsReceiveMessageResults() {
    return WsReceiveMessageResults;
  }

  
  public void setWsReceiveMessageResults(WsMessageResults wsReceiveMessageResults) {
    WsReceiveMessageResults = wsReceiveMessageResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.messaging;

import edu.internet2.middleware.grouper.ws.coresoap.WsMessageResults;
import io.swagger.annotations.ApiModelProperty;

public class WsSendMessageResultsWrapper {
  WsMessageResults WsSendMessageResults = null;

  @ApiModelProperty(name = "WsMessageResults", value = "Identifies the response of a send message request")
  public WsMessageResults getWsSendMessageResults() {
    return WsSendMessageResults;
  }

  
  public void setWsSendMessageResults(WsMessageResults wsSendMessageResults) {
    WsSendMessageResults = wsSendMessageResults;
  }
  

}

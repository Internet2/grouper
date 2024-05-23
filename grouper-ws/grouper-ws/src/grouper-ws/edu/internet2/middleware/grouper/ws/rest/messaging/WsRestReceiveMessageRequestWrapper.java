package edu.internet2.middleware.grouper.ws.rest.messaging;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to receive a message")
public class WsRestReceiveMessageRequestWrapper {

  private WsRestReceiveMessageRequest wsRestReceiveMessageRequest;
  
  @ApiModelProperty(name = "WsRestReceiveMessageRequest", value = "Identifies the request as an receive message request")
  public WsRestReceiveMessageRequest getWsRestReceiveMessageRequest() {
    return wsRestReceiveMessageRequest;
  }

  
  public void setWsRestReceiveMessageRequest(
      WsRestReceiveMessageRequest wsRestReceiveMessageRequest) {
    this.wsRestReceiveMessageRequest = wsRestReceiveMessageRequest;
  }
  
  
  
}

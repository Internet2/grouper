package edu.internet2.middleware.grouper.ws.rest.messaging;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to send a message")
public class WsRestSendMessageRequestWrapper {

  private WsRestSendMessageRequest wsRestSendMessageRequest;
  
  @ApiModelProperty(name = "WsRestSendMessageRequest", value = "Identifies the request as an send message request")
  public WsRestSendMessageRequest getWsRestSendMessageRequest() {
    return wsRestSendMessageRequest;
  }

  
  public void setWsRestSendMessageRequest(
      WsRestSendMessageRequest wsRestSendMessageRequest) {
    this.wsRestSendMessageRequest = wsRestSendMessageRequest;
  }
  
  
  
}

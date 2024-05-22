package edu.internet2.middleware.grouper.ws.rest.messaging;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to acknowledge a message")
public class WsRestAcknowledgeMessageRequestWrapper {

  private WsRestAcknowledgeMessageRequest wsRestAcknowledgeMessageRequest;
  
  @ApiModelProperty(name = "WsRestAcknowledgeMessageRequest", value = "Identifies the request as an acknowledge message request")
  public WsRestAcknowledgeMessageRequest getWsRestAcknowledgeMessageRequest() {
    return wsRestAcknowledgeMessageRequest;
  }

  
  public void setWsRestAcknowledgeMessageRequest(
      WsRestAcknowledgeMessageRequest wsRestAcknowledgeMessageRequest) {
    this.wsRestAcknowledgeMessageRequest = wsRestAcknowledgeMessageRequest;
  }
  
  
  
}

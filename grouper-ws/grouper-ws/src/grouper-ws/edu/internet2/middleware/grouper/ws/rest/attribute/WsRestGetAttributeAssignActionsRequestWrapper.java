package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get attribute assign actions")
public class WsRestGetAttributeAssignActionsRequestWrapper {

  private WsRestGetAttributeAssignActionsRequest wsRestGetAttributeAssignActionsRequest;
  
  @ApiModelProperty(name = "WsRestGetAttributeAssignActionsRequest", value = "Identifies the request as a get attribute assign actions request")
  public WsRestGetAttributeAssignActionsRequest getWsRestGetAttributeAssignActionsRequest() {
    return wsRestGetAttributeAssignActionsRequest;
  }

  
  public void setWsRestGetAttributeAssignActionsRequest(
      WsRestGetAttributeAssignActionsRequest wsRestGetAttributeAssignActionsRequest) {
    this.wsRestGetAttributeAssignActionsRequest = wsRestGetAttributeAssignActionsRequest;
  }
  
  
  
}

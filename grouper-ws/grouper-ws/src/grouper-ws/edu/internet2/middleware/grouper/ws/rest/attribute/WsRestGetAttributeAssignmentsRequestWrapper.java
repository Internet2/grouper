package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get attribute assign actions")
public class WsRestGetAttributeAssignmentsRequestWrapper {

  private WsRestGetAttributeAssignmentsRequest wsRestGetAttributeAssignmentsRequest;
  
  @ApiModelProperty(name = "WsRestGetAttributeAssignmentsRequest", value = "Identifies the request as a get attribute assign actions request")
  public WsRestGetAttributeAssignmentsRequest getWsRestGetAttributeAssignmentsRequest() {
    return wsRestGetAttributeAssignmentsRequest;
  }

  
  public void setWsRestGetAttributeAssignmentsRequest(
      WsRestGetAttributeAssignmentsRequest wsRestGetAttributeAssignmentsRequest) {
    this.wsRestGetAttributeAssignmentsRequest = wsRestGetAttributeAssignmentsRequest;
  }
  
  
  
}

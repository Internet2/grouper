package edu.internet2.middleware.grouper.ws.rest.subject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get subjects")
public class WsRestGetSubjectsRequestWrapper {

  private WsRestGetSubjectsRequest wsRestGetSubjectsRequest;
  
  @ApiModelProperty(name = "WsRestGetSubjectsRequest", value = "Identifies the request as a get subjects request")
  public WsRestGetSubjectsRequest getWsRestGetSubjectsRequest() {
    return wsRestGetSubjectsRequest;
  }

  
  public void setWsRestGetSubjectsRequest(
      WsRestGetSubjectsRequest wsRestGetSubjectsRequest) {
    this.wsRestGetSubjectsRequest = wsRestGetSubjectsRequest;
  }
  
  
  
}

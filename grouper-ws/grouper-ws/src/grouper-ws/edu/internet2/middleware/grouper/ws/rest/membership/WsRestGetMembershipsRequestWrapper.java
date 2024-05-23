package edu.internet2.middleware.grouper.ws.rest.membership;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get memberships")
public class WsRestGetMembershipsRequestWrapper {

  private WsRestGetMembershipsRequest wsRestGetMembershipsRequest;
  
  @ApiModelProperty(name = "WsRestGetMembershipsRequest", value = "Identifies the request as a get memberships request")
  public WsRestGetMembershipsRequest getWsRestGetMembershipsRequest() {
    return wsRestGetMembershipsRequest;
  }

  
  public void setWsRestGetMembershipsRequest(
      WsRestGetMembershipsRequest wsRestGetMembershipsRequest) {
    this.wsRestGetMembershipsRequest = wsRestGetMembershipsRequest;
  }
  
  
  
}

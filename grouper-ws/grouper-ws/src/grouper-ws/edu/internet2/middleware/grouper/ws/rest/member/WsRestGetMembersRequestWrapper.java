package edu.internet2.middleware.grouper.ws.rest.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get members")
public class WsRestGetMembersRequestWrapper {

  private WsRestGetMembersRequest wsRestGetMembersRequest;
  
  @ApiModelProperty(name = "WsRestGetMembersRequest", value = "Identifies the request as a get members request")
  public WsRestGetMembersRequest getWsRestGetMembersRequest() {
    return wsRestGetMembersRequest;
  }
  public void setWsRestGetMembersRequest(WsRestGetMembersRequest wsRestGetMembersRequest1) {
    wsRestGetMembersRequest = wsRestGetMembersRequest1;
  }
}

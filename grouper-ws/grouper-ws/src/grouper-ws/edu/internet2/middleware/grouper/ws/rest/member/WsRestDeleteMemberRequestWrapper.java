package edu.internet2.middleware.grouper.ws.rest.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete member")
public class WsRestDeleteMemberRequestWrapper {

  private WsRestDeleteMemberRequest wsRestDeleteMemberRequest;
  
  @ApiModelProperty(name = "WsRestDeleteMemberRequest", value = "Identifies the request as a delete member request")
  public WsRestDeleteMemberRequest getWsRestDeleteMemberRequest() {
    return wsRestDeleteMemberRequest;
  }
  public void setWsRestDeleteMemberRequest(WsRestDeleteMemberRequest wsRestDeleteMemberRequest1) {
    wsRestDeleteMemberRequest = wsRestDeleteMemberRequest1;
  }
}

package edu.internet2.middleware.grouper.ws.rest.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to add member")
public class WsRestAddMemberRequestWrapper {

  private WsRestAddMemberRequest wsRestAddMemberRequest;
  
  @ApiModelProperty(name = "WsRestAddMemberRequest", value = "Identifies the request as a add member request")
  public WsRestAddMemberRequest getWsRestAddMemberRequest() {
    return wsRestAddMemberRequest;
  }
  public void setWsRestAddMemberRequest(WsRestAddMemberRequest wsRestAddMemberRequest1) {
    wsRestAddMemberRequest = wsRestAddMemberRequest1;
  }
}

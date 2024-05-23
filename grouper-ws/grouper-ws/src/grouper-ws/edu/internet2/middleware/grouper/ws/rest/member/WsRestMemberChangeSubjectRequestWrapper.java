package edu.internet2.middleware.grouper.ws.rest.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to member change subjects")
public class WsRestMemberChangeSubjectRequestWrapper {

  private WsRestMemberChangeSubjectRequest wsRestMemberChangeSubjectRequest;
  
  @ApiModelProperty(name = "WsRestMemberChangeSubjectRequest", value = "Identifies the request as a member change subjects request")
  public WsRestMemberChangeSubjectRequest getWsRestMemberChangeSubjectRequest() {
    return wsRestMemberChangeSubjectRequest;
  }
  public void setWsRestMemberChangeSubjectRequest(WsRestMemberChangeSubjectRequest wsRestMemberChangeSubjectRequest1) {
    wsRestMemberChangeSubjectRequest = wsRestMemberChangeSubjectRequest1;
  }
}

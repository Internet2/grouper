package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGetMembersLiteResultsWrapper {
  WsGetMembersLiteResult WsGetMembersLiteResult = null;

  @ApiModelProperty(name = "WsGetMembersLiteResult", value = "Identifies the response of a get members lite request")
  public WsGetMembersLiteResult getWsGetMembersLiteResult() {
    return WsGetMembersLiteResult;
  }

  
  public void setWsGetMembersLiteResult(WsGetMembersLiteResult wsGetMembersLiteResult) {
    WsGetMembersLiteResult = wsGetMembersLiteResult;
  }
  

}

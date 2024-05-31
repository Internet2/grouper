package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAddMemberLiteResultsWrapper {
  WsAddMemberLiteResult WsAddMemberLiteResult = null;

  @ApiModelProperty(name = "WsAddMemberLiteResults", value = "Identifies the response of an add member lite request")
  public WsAddMemberLiteResult getWsAddMemberLiteResults() {
    return WsAddMemberLiteResult;
  }

  
  public void setWsAddMemberLiteResults(WsAddMemberLiteResult wsAddMemberLiteResults) {
    WsAddMemberLiteResult = wsAddMemberLiteResults;
  }
  

}

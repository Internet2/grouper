package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsDeleteMemberLiteResultsWrapper {
  WsDeleteMemberLiteResult WsDeleteMemberLiteResult = null;

  @ApiModelProperty(name = "WsDeleteMemberLiteResult", value = "Identifies the response of a delete member  lite request")
  public WsDeleteMemberLiteResult getWsDeleteMemberLiteResults() {
    return WsDeleteMemberLiteResult;
  }

  
  public void setWsDeleteMemberLiteResults(WsDeleteMemberLiteResult wsDeleteMemberLiteResult) {
    WsDeleteMemberLiteResult = wsDeleteMemberLiteResult;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults;
import io.swagger.annotations.ApiModelProperty;

public class WsDeleteMemberResultsWrapper {
  WsDeleteMemberResults WsDeleteMemberResults = null;

  @ApiModelProperty(name = "WsDeleteMemberResults", value = "Identifies the response of a delete member request")
  public WsDeleteMemberResults getWsDeleteMemberResults() {
    return WsDeleteMemberResults;
  }

  
  public void setWsDeleteMemberResults(WsDeleteMemberResults wsDeleteMemberResults) {
    WsDeleteMemberResults = wsDeleteMemberResults;
  }
  

}

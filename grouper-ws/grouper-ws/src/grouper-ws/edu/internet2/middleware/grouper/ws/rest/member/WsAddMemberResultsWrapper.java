package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAddMemberResultsWrapper {
  WsAddMemberResults WsAddMemberResults = null;

  @ApiModelProperty(name = "WsAddMemberResults", value = "Identifies the response of an add member request")
  public WsAddMemberResults getWsAddMemberResults() {
    return WsAddMemberResults;
  }

  
  public void setWsAddMemberResults(WsAddMemberResults wsAddMemberResults) {
    WsAddMemberResults = wsAddMemberResults;
  }
  

}

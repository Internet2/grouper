package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetMembersResultsWrapper {
  WsGetMembersResults WsGetMembersResults = null;

  @ApiModelProperty(name = "WsGetMembersResults", value = "Identifies the response of a get members request")
  public WsGetMembersResults getWsGetMembersResults() {
    return WsGetMembersResults;
  }

  
  public void setWsGetMembersResults(WsGetMembersResults wsGetMembersResults) {
    WsGetMembersResults = wsGetMembersResults;
  }
  

}

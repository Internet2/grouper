package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetGroupsResultsWrapper {
  WsGetGroupsResults WsGetGroupsResults = null;

  @ApiModelProperty(name = "WsGetGroupsResults", value = "Identifies the response of a get groups request")
  public WsGetGroupsResults getWsGetGroupsResults() {
    return WsGetGroupsResults;
  }

  
  public void setWsGetGroupsResults(WsGetGroupsResults wsGetGroupsResults) {
    WsGetGroupsResults = wsGetGroupsResults;
  }
  

}

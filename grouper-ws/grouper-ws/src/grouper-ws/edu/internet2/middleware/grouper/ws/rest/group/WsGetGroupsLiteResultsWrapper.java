package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGetGroupsLiteResultsWrapper {
  WsGetGroupsLiteResult WsGetGroupsLiteResults = null;

  @ApiModelProperty(name = "WsGetGroupsLiteResult", value = "Identifies the response of an Get groups request")
  public WsGetGroupsLiteResult getWsGetGroupsLiteResults() {
    return WsGetGroupsLiteResults;
  }

  
  public void setWsGetGroupsLiteResults(WsGetGroupsLiteResult wsGetGroupsLiteResults) {
    WsGetGroupsLiteResults = wsGetGroupsLiteResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGetGrouperPrivilegesResultsWrapper {
  WsGetGrouperPrivilegesLiteResult WsGetGrouperPrivilegesResults = null;

  @ApiModelProperty(name = "WsGetGrouperPrivilegesResults", value = "Identifies the response of a get grouper privileges request")
  public WsGetGrouperPrivilegesLiteResult getWsGetGrouperPrivilegesResults() {
    return WsGetGrouperPrivilegesResults;
  }

  
  public void setWsGetGrouperPrivilegesResults(WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesResults) {
    WsGetGrouperPrivilegesResults = wsGetGrouperPrivilegesResults;
  }
  

}

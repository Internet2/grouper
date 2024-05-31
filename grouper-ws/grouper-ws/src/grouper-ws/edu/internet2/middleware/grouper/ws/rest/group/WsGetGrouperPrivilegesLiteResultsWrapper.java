package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGetGrouperPrivilegesLiteResultsWrapper {
  WsGetGrouperPrivilegesLiteResult WsGetGrouperPrivilegesLiteResult = null;

  @ApiModelProperty(name = "WsGetGrouperPrivilegesLiteResult", value = "Identifies the response of an Get grouper privileges request")
  public WsGetGrouperPrivilegesLiteResult getWsGetGrouperPrivilegesLiteResults() {
    return WsGetGrouperPrivilegesLiteResult;
  }

  
  public void setWsGetGrouperPrivilegesLiteResults(WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult) {
    WsGetGrouperPrivilegesLiteResult = wsGetGrouperPrivilegesLiteResult;
  }
  

}

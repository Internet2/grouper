package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignGrouperPrivilegesLiteResultsWrapper {
  WsAssignGrouperPrivilegesLiteResult WsAssignGrouperPrivilegesLiteResults = null;

  @ApiModelProperty(name = "WsAssignGrouperPrivilegesLiteResult", value = "Identifies the response of an assign grouper privileges request")
  public WsAssignGrouperPrivilegesLiteResult getWsAssignGrouperPrivilegesLiteResults() {
    return WsAssignGrouperPrivilegesLiteResults;
  }

  
  public void setWsAssignGrouperPrivilegesLiteResults(WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResults) {
    WsAssignGrouperPrivilegesLiteResults = wsAssignGrouperPrivilegesLiteResults;
  }
  

}

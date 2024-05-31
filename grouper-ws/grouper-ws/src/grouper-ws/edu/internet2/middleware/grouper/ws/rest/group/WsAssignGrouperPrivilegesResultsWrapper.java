package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignGrouperPrivilegesResultsWrapper {
  WsAssignGrouperPrivilegesResults WsAssignGrouperPrivilegesResults = null;

  @ApiModelProperty(name = "WsAssignGrouperPrivilegesResults", value = "Identifies the response of an assign grouper privileges request")
  public WsAssignGrouperPrivilegesResults getWsAssignGrouperPrivilegesResults() {
    return WsAssignGrouperPrivilegesResults;
  }

  
  public void setWsAssignGrouperPrivilegesResults(WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults) {
    WsAssignGrouperPrivilegesResults = wsAssignGrouperPrivilegesResults;
  }
  

}

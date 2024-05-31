package edu.internet2.middleware.grouper.ws.rest.permission;


import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsLiteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignPermissionsLiteResultsWrapper {
  WsAssignPermissionsLiteResults WsAssignPermissionsLiteResult = null;

  @ApiModelProperty(name = "WsAssignPermissionsLiteResults", value = "Identifies the response of an assign PermissionsLite request")
  public WsAssignPermissionsLiteResults getWsAssignPermissionsLiteResults() {
    return WsAssignPermissionsLiteResult;
  }

  
  public void setWsAssignPermissionsLiteResults(WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults) {
    WsAssignPermissionsLiteResult = wsAssignPermissionsLiteResults;
  }
  

}

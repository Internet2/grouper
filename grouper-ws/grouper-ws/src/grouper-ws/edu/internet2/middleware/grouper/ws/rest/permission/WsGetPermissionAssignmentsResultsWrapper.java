package edu.internet2.middleware.grouper.ws.rest.permission;


import edu.internet2.middleware.grouper.ws.coresoap.WsGetPermissionAssignmentsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetPermissionAssignmentsResultsWrapper {
  WsGetPermissionAssignmentsResults WsGetPermissionAssignmentsResults = null;

  @ApiModelProperty(name = "WsGetPermissionAssignmentsResults", value = "Identifies the response of a get permission assignments request")
  public WsGetPermissionAssignmentsResults getWsGetPermissionAssignmentsResults() {
    return WsGetPermissionAssignmentsResults;
  }

  
  public void setWsGetPermissionAssignmentsResults(WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults) {
    WsGetPermissionAssignmentsResults = wsGetPermissionAssignmentsResults;
  }
  

}

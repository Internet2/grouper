package edu.internet2.middleware.grouper.ws.rest.permission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get permission assignments")
public class WsRestGetPermissionAssignmentsRequestWrapper {

  private WsRestGetPermissionAssignmentsRequest wsRestGetPermissionAssignmentsRequest;
  
  @ApiModelProperty(name = "WsRestGetPermissionAssignmentsRequest", value = "Identifies the request as a get permission assignments request")
  public WsRestGetPermissionAssignmentsRequest getWsRestGetPermissionAssignmentsRequest() {
    return wsRestGetPermissionAssignmentsRequest;
  }

  
  public void setWsRestGetPermissionAssignmentsRequest(
      WsRestGetPermissionAssignmentsRequest wsRestGetPermissionAssignmentsRequest) {
    this.wsRestGetPermissionAssignmentsRequest = wsRestGetPermissionAssignmentsRequest;
  }
  
  
  
}

package edu.internet2.middleware.grouper.ws.rest.permission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign a permission")
public class WsRestAssignPermissionsRequestWrapper {

  private WsRestAssignPermissionsRequest wsRestAssignPermissionsRequest;
  
  @ApiModelProperty(name = "WsRestAssignPermissionsRequest", value = "Identifies the request as an assign a permission request")
  public WsRestAssignPermissionsRequest getWsRestAssignPermissionsRequest() {
    return wsRestAssignPermissionsRequest;
  }

  
  public void setWsRestAssignPermissionsRequest(
      WsRestAssignPermissionsRequest wsRestAssignPermissionsRequest) {
    this.wsRestAssignPermissionsRequest = wsRestAssignPermissionsRequest;
  }
  
  
  
}

package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign grouper privileges")
public class WsRestAssignGrouperPrivilegesRequestWrapper {

  private WsRestAssignGrouperPrivilegesRequest wsRestAssignGrouperPrivilegesRequest;
  
  @ApiModelProperty(name = "WsRestAssignGrouperPrivilegesRequest", value = "Identifies the request as an assign grouper privileges request")
  public WsRestAssignGrouperPrivilegesRequest getWsRestAssignGrouperPrivilegesRequest() {
    return wsRestAssignGrouperPrivilegesRequest;
  }
  public void setWsRestAssignGrouperPrivilegesRequest(WsRestAssignGrouperPrivilegesRequest wsRestAssignGrouperPrivilegesRequest1) {
    wsRestAssignGrouperPrivilegesRequest = wsRestAssignGrouperPrivilegesRequest1;
  }
}

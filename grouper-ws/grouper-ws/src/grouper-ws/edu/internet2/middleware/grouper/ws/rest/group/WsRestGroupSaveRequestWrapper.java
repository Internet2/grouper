package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to save a group")
public class WsRestGroupSaveRequestWrapper {

  private WsRestGroupSaveRequest wsRestGroupSaveRequest;
  
  @ApiModelProperty(name = "WsRestGroupSaveRequest", value = "Identifies the request as a save group request")
  public WsRestGroupSaveRequest getWsRestGroupSaveRequest() {
    return wsRestGroupSaveRequest;
  }
  public void setWsRestGroupSaveRequest(WsRestGroupSaveRequest wsRestGroupSaveRequest1) {
    wsRestGroupSaveRequest = wsRestGroupSaveRequest1;
  }
}

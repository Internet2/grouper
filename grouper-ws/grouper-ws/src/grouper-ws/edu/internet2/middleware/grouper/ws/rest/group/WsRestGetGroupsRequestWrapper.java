package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get groups")
public class WsRestGetGroupsRequestWrapper {

  private WsRestGetGroupsRequest wsRestGetGroupsRequest;
  
  @ApiModelProperty(name = "WsRestgetGroupsRequest", value = "Identifies the request as a getGroups request")
  public WsRestGetGroupsRequest getWsRestgetGroupsRequest() {
    return wsRestGetGroupsRequest;
  }
  public void setWsRestgetGroupsRequest(WsRestGetGroupsRequest wsRestgetGroupsRequest1) {
    wsRestGetGroupsRequest = wsRestgetGroupsRequest1;
  }
}
